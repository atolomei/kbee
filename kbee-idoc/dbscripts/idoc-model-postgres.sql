
CREATE TABLE RS_WINDSOR_ACTIVITY_PIVOT (
	
	id			 	 bigint NOT  NULL,
	process			 bigint NOT  NULL,
	procedure		 character  varying(128),
	task			 character  varying(128),
	user_id			 bigint NOT  NULL,
	starttime		 TIMESTAMP  WITH TIME ZONE,
	endtime			 TIMESTAMP  WITH TIME ZONE,
	event			 character  varying(128),
	content_id		 bigint,
	content_oid		 bigint,
	perfect 		 boolean,
	approved 		 boolean,
	type			 bigint,
	pmc				 bigint,
	property		 bigint,
	specialist		 bigint,
	date			 TIMESTAMP  WITH TIME ZONE,
	running_process	 boolean,		
	CONSTRAINT rs_activity_id_pkey  PRIMARY KEY (id)
)
WITH (
	OIDS=FALSE
);

ALTER TABLE RS_WINDSOR_ACTIVITY_PIVOT OWNER TO kbee;

CREATE OR REPLACE FUNCTION public.last_agg ( anyelement, anyelement )
RETURNS anyelement LANGUAGE SQL IMMUTABLE STRICT AS $$
	SELECT $2;
$$;
 
CREATE AGGREGATE public.LAST (
	sfunc    = public.last_agg,
	basetype = anyelement,
	stype    = anyelement
);

CREATE OR REPLACE VIEW RS_WINDSOR_PROCESS_PIVOT AS 
	SELECT rs_windsor_activity_pivot.process,
		min(starttime) AS starttime,
		last(endtime) AS endtime,
		sum(
		CASE WHEN task::text = 'Submission'::text or task::text = 'Resubmission'::text THEN 
			1
		ELSE 
			0
		END) AS submissions,
		last(type) AS type,
		last(pmc) AS pmc,
		last(property) AS property,
		last(specialist) AS specialist,
		last(perfect) as perfect,
		last(approved) as approved,
		last(running_process) as running
	FROM 
		rs_windsor_activity_pivot 
	GROUP BY 
		process;

ALTER TABLE rs_WINDSOR_process_pivot   OWNER TO kbee;