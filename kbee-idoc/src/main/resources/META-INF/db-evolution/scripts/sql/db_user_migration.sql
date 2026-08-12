-- RELEASE LOCK:
-- UPDATE DATABASECHANGELOGLOCK SET LOCKED=FALSE, LOCKGRANTED=null, LOCKEDBY=null where ID=1;
--
-- PASSWORD
-- ALTER USER pjsf1_app  WITH PASSWORD 'novamens';
-- ALTER USER pjsf1_admin  WITH PASSWORD 'novamens';
--

do $$
	DECLARE
		current_db text;
		admin_user text;
		app_user text;
		dml_role text;

	BEGIN
		select current_database() into current_db;
		admin_user:= current_db || '_admin';
		app_user:= current_db || '_app';
		dml_role:= current_db || '_dml_role';
		
		
		CREATE SCHEMA IF NOT EXISTS kbee;
		
		CREATE OR REPLACE FUNCTION kbee.exec(text) returns text
		AS $f$
		BEGIN
			EXECUTE $1;
			RETURN $1;
		END;
		$f$ language plpgsql;

		IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE  rolname = admin_user) THEN
		EXECUTE format('CREATE USER %I', admin_user);
		END IF;
		IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE  rolname =dml_role) THEN
		EXECUTE format('CREATE ROLE %I nologin', dml_role);
		END IF;
		IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE  rolname = app_user) THEN
		EXECUTE format('CREATE USER %I', app_user);
		END IF;

		REVOKE ALL ON SCHEMA public FROM public;

		
		EXECUTE format('ALTER DATABASE %I SET search_path TO kbee, public', current_db);
		set search_path = "kbee, public";


		
		PERFORM kbee.exec('ALTER TABLE ' || quote_ident(s.nspname) || '.' || quote_ident(s.relname) || ' OWNER TO ' || admin_user),
				kbee.exec('ALTER TABLE ' || quote_ident(s.nspname) || '.' || quote_ident(s.relname) || ' SET SCHEMA kbee')
			FROM (SELECT nspname, relname
			  FROM pg_class c JOIN pg_namespace n ON (c.relnamespace = n.oid)
			  WHERE nspname in ('kbee','public')  and relkind IN ('r') and relname not like 'databasechangelog%') s;

		PERFORM kbee.exec('ALTER SEQUENCE ' || quote_ident(s.nspname) || '.' || quote_ident(s.relname) || ' OWNER TO ' || admin_user),
				kbee.exec('ALTER SEQUENCE ' || quote_ident(s.nspname) || '.' || quote_ident(s.relname) || ' SET SCHEMA kbee')
			FROM (SELECT nspname, relname
			  FROM pg_class c JOIN pg_namespace n ON (c.relnamespace = n.oid)
			  WHERE nspname in ('kbee','public')  and relkind IN ('S') and relname not like 'databasechangelog%') s
			WHERE (nspname, relname) NOT IN (SELECT n.nspname nspname, s.relname AS relname --sequence linked to tables cannot be moved
						FROM pg_class AS t JOIN pg_attribute AS a ON a.attrelid = t.oid
							JOIN pg_depend AS d ON d.refobjid = t.oid AND d.refobjsubid = a.attnum
							JOIN pg_class AS s ON s.oid = d.objid
							JOIN pg_namespace n ON (s.relnamespace = n.oid)
						 WHERE  t.relkind IN ('r', 'P') AND s.relkind = 'S');

		PERFORM kbee.exec('ALTER FOREIGN TABLE ' || quote_ident(s.nspname) || '.' || quote_ident(s.relname) || ' OWNER TO ' || admin_user),
				kbee.exec('ALTER FOREIGN TABLE ' || quote_ident(s.nspname) || '.' || quote_ident(s.relname) || ' OWNER TO ' || admin_user)
			FROM (SELECT nspname, relname
			  FROM pg_class c JOIN pg_namespace n ON (c.relnamespace = n.oid)
			  WHERE nspname in ('kbee','public')  and relkind IN ('f')) s;


		PERFORM kbee.exec('ALTER FUNCTION ' || nspname || '.' ||p.proname ||'(' || pg_get_function_identity_arguments(p.oid) ||')  OWNER TO ' || admin_user),
				kbee.exec('ALTER FUNCTION ' || nspname || '.' ||p.proname ||'(' || pg_get_function_identity_arguments(p.oid) ||') SET SCHEMA kbee;')
		FROM   pg_proc p
				   JOIN   pg_namespace n ON n.oid = p.pronamespace
				WHERE  nspname in ('kbee','public') ;
				
		--PERFORM kbee.exec('ALTER TABLE public.databasechangelog OWNER TO ' || admin_user);
		--PERFORM kbee.exec('ALTER TABLE public.databasechangeloglock OWNER TO ' || admin_user);
		--PERFORM kbee.exec('ALTER SEQUENCE public.databasechangelog_seq OWNER TO ' || admin_user);
		--PERFORM kbee.exec('ALTER SEQUENCE public.databasechangeloglock_seq OWNER TO ' || admin_user);
		
		EXECUTE format('REVOKE ALL ON DATABASE %I FROM public', current_db);

		--kbee_db_1_dml_role setup
		EXECUTE format('GRANT USAGE ON SCHEMA kbee TO %I;', dml_role);
		EXECUTE format('GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA kbee TO %I;', dml_role);
		EXECUTE format('GRANT SELECT, UPDATE ON ALL SEQUENCES IN SCHEMA kbee TO %I', dml_role);

		--kbee_db_1_admin setup
		EXECUTE format('ALTER DATABASE %I OWNER TO %I', current_db, admin_user);
		EXECUTE format('GRANT ALL ON DATABASE %I TO %I', current_db, admin_user);
		EXECUTE format('GRANT ALL ON SCHEMA kbee TO %I', admin_user);
		--EXECUTE format('GRANT ALL ON SCHEMA public TO %I', admin_user);
		EXECUTE format('GRANT ALL PRIVILEGES ON SCHEMA  kbee to %I', admin_user);
		EXECUTE format('GRANT ALL PRIVILEGES ON SCHEMA  public to %I', admin_user);
		EXECUTE format('GRANT ALL ON ALL TABLES IN SCHEMA kbee TO %I;', admin_user);
		EXECUTE format('GRANT ALL ON ALL SEQUENCES IN SCHEMA kbee TO %I', admin_user);
		EXECUTE format('GRANT ALL ON ALL FUNCTIONS IN SCHEMA kbee TO %I', admin_user);
		

		EXECUTE format('ALTER DEFAULT PRIVILEGES FOR USER %I IN SCHEMA kbee grant SELECT, INSERT, UPDATE, DELETE ON TABLES to %I', admin_user, dml_role);
		EXECUTE format('ALTER DEFAULT PRIVILEGES FOR USER %I IN SCHEMA kbee grant SELECT, UPDATE ON SEQUENCES to %I', admin_user, dml_role);

		EXECUTE format('GRANT %I TO %I', dml_role, app_user);
		EXECUTE format('GRANT CONNECT ON DATABASE %I TO %I', current_db, app_user);
		
		drop function kbee.exec;
	END;
$$ language plpgsql;