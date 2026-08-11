CREATE SEQUENCE SBD02.contentresourceid_sequence
	INCREMENT by 1
	START with 100
	MINVALUE 1
	MAXVALUE 9223372036854775807;

	CREATE SEQUENCE SBD02.timer_sequence
	INCREMENT by 1
	START with 100
	MINVALUE 1
	MAXVALUE 9223372036854775807;
	

	-- Sequencer -----------------------------------------------------------------------------------------------
	
	
	alter table contentresource add id number(19);

	update contentresource set id = contentresourceid_sequence.nextval;

	alter table contentresource drop constraint contentresource_pkey cascade;
	alter table contentresource modify (id not null);
	alter table contentresource add constraint contentresource_pkey PRIMARY KEY (id); 
	

	ALTER TABLE contentresource ADD CONSTRAINT contentresource_unique UNIQUE (content_id, resource_id);

	ALTER SEQUENCE contentresourceid_sequence INCREMENT BY 50;
	ALTER SEQUENCE log_sequence INCREMENT BY 50;
	ALTER SEQUENCE resourceid_sequence INCREMENT BY 50;
	ALTER SEQUENCE workflow_sequence INCREMENT BY 50;

	
	CREATE TABLE kb_timer (
		id number(19) NOT NULL,
		creationdate TIMESTAMP  WITH TIME ZONE DEFAULT current_timestamp,
		duedate TIMESTAMP  WITH TIME ZONE,
		callback BLOB,
		CONSTRAINT timer_id_pk PRIMARY KEY (id)
	) TABLESPACE SBD02_DATA;

	alter table kb_timer add attemps number(2);
	alter table kb_timer add error_message varchar2(256);
	
	alter table kb_domain_settings add id number(19);
	
	update kb_domain_settings  set id=objectid_sequence.nextval;
	
	
	alter table kb_domain_settings modify (id not null);
	alter table kb_domain_settings  drop constraint ds_pkey;
	alter table kb_domain_settings add primary key(id);
	alter table kb_domain_settings add constraint domain_cat_unique unique(domain_id, category);

	alter table savedquery add statement2 clob;
	update savedquery set statement2=statement;
	alter table savedquery drop column statement;
	alter table savedquery rename column statement2 to statementDATA;

	drop table po_viewcontent;


CREATE TABLE po_viewcontent
(
    po_id number(19) NOT NULL,
    content_id number(19) NOT NULL,
    titlemode number(2) DEFAULT 0,
    isabstract number(1) DEFAULT 1,
    ismetadata number(1) DEFAULT 1,
    isviewer number(1) DEFAULT 0,
    bodytemplate number(2) DEFAULT 1,
    isresources number(1) DEFAULT 0,
    resourcesmode number(2) DEFAULT 0,
    resourcesids varchar2(2048),
    content_oid number(19) not null,
    CONSTRAINT viewcontent_pkey PRIMARY KEY (po_id),
    CONSTRAINT viewcontent_content_fk FOREIGN KEY (content_id)   REFERENCES content (id) ON DELETE CASCADE
) TABLESPACE SBD02_DATA;

-- Indices para los Reportes ------------------------------------------------------------------------------ 

 CREATE INDEX log_visit_time_idx ON po_sitelogin(visit_time) 				TABLESPACE SBD02_INDEX;
 -- CREATE INDEX log_site_title_idx ON po_sitelogin(site_title, visit_time)	TABLESPACE SBD02_INDEX;
		   
 CREATE INDEX log_site_id_idx ON po_sitelogin(site_id, visit_time) 			TABLESPACE SBD02_INDEX;
 CREATE INDEX log_user_id_idx ON po_sitelogin(user_id, visit_time)   		TABLESPACE SBD02_INDEX;
 CREATE INDEX log_page_id_idx ON po_sitelogin(page_id, visit_time)   		TABLESPACE SBD02_INDEX;
 		   
 CREATE INDEX log_user_name_idx  ON po_sitelogin(user_name, visit_time)  	TABLESPACE SBD02_INDEX;
 CREATE INDEX log_page_title_idx ON po_sitelogin(page_title, visit_time) 	TABLESPACE SBD02_INDEX;
	       
------------------------------------------------------------------------------------------------------------
 
alter table kb_security_rule add notes clob;
alter table kb_enotirule add notes clob;
alter table kb_user_property add uset varchar2(256);
alter table property add uset varchar2(256);
alter table property add lastModifiedDate timestamp with time zone default sysdate;
alter table kb_user_property add lastModifiedDate timestamp with time zone default sysdate;
alter table po_viewcontent add titlemode number(1) default 0;


------------------------------------------------------------------------------------------------------------

alter table domain add isapienabled number(1) default 1;

alter table kb_contenttemplate add abstract_label 			varchar2(128);
alter table kb_contenttemplate add private_notes_label  	varchar2(128);
alter table kb_contenttemplate add text_notes_label 		varchar2(128);
alter table kb_contenttemplate add text_label 				varchar2(128);
alter table kb_contenttemplate add customattributes_label 	varchar2(128);


------------------------------------------------------------------------------------------------------------

alter table kfile  add uploadeddate timestamp with time zone DEFAULT sysdate;
alter table kfile  add uploadeduser number(19);
update      kfile set uploadeduser = (select id from users where username='root@kbee');
alter table kfile add CONSTRAINT des_user_fk FOREIGN KEY (uploadeduser) REFERENCES users (id);

update kfile set uploadeduser = (select k.lastmodifieduser from kresource k where k.id=resource_id);
update kfile set uploadeddate  = (select k.lastmodifieddate from kresource k where k.id=resource_id);

------------------------------------------------------------------------------------------------------------

	CREATE TABLE kb_work_note
	(
	    id      NUMBER(19) NOT NULL,
	    user_id NUMBER(19) NOT NULL,
	    title varchar2(256),
	    notetext clob,
	    creationdate timestamp with time zone DEFAULT sysdate,
	    lastmodifieddate timestamp with time zone DEFAULT sysdate,
	    lastmodifieduser NUMBER(19) NOT NULL,
	    priority varchar2(24),
	    domain_id NUMBER(19) NOT NULL,
	    CONSTRAINT kwn_id_pk PRIMARY KEY (id),
	    CONSTRAINT kwn_domain_fk FOREIGN KEY (domain_id)  REFERENCES domain (id)    ON DELETE CASCADE,
	    CONSTRAINT kwn_lmu_id_fk FOREIGN KEY (user_id)        REFERENCES users (id) ON DELETE SET NULL
    
    ) TABLESPACE SBD02_DATA;

CREATE INDEX kb_wn_user_idx ON kb_work_note (domain_id, creationdate DESC) TABLESPACE SBD02_INDEX;

alter table kb_notification drop column contentid;

------------------------------------------------------------------------------------------------------------

CREATE TABLE kb_user_note (
    id NUMBER(19) NOT NULL,
    user_id NUMBER(19) NOT NULL,
    title varchar2(256),
    notetext clob,
    creationdate timestamp with time zone DEFAULT sysdate,
    lastmodifieddate timestamp with time zone DEFAULT sysdate,
    lastmodifieduser NUMBER(19) NOT NULL,
    priority varchar2(24),
    domain_id NUMBER(19) NOT NULL,
    CONSTRAINT user_note_id_pk PRIMARY KEY (id),
    CONSTRAINT un_domain_fk FOREIGN KEY (domain_id)     REFERENCES domain (id) ON DELETE CASCADE,
    CONSTRAINT user_note_lmu_id_fk FOREIGN KEY (user_id)     REFERENCES users (id) ON DELETE CASCADE
) TABLESPACE SBD02_DATA;;

CREATE INDEX kb_un_user_idx  ON kb_user_note (user_id, creationdate DESC) TABLESPACE SBD02_INDEX;


------------------------------------------------------------------------------------------------------------
alter table wf_procedure add code character(12);

update wf_procedure set code='AS' where name='Assign';
update wf_procedure set code='CS' where name='Submission2';
update wf_procedure set code='CN' where name='Submission';
update wf_procedure set code='ST' where name='Standard';
update wf_procedure set code='CF' where name like 'Compliance %';


------------------------------------------------------------------------------------------------------------
alter table datasetmember modify datevalue timestamp with time zone;


alter table classification add datevalue2 timestamp with time zone;
update classification set datevalue2=datevalue;
alter table classification drop column datevalue;
alter table classification rename column datevalue2 to datevalue;

alter table entitymatching  modify lastmodifieddate timestamp with time zone;
alter table drb_answer  modify date_accepted  timestamp with time zone;
alter table drb_answer  modify date_submitted  timestamp with time zone;
alter table drb_answer  modify date_Edited_Admin  timestamp with time zone;
alter table drb_question  modify date_submitted  timestamp with time zone;
alter table drb_question  modify date_edited_admin  timestamp with time zone;

alter table domain add timezone varchar2(256) DEFAULT 'US/Central';
alter table kb_classifier add is_rule_condition NUMBER(1) default 1;


------------------------------------------------------------------------------------------------------------
CREATE INDEX kb_resource_name_global_idx  ON kresource (lower(name)) TABLESPACE SBD02_INDEX;

alter table content add checkindate timestamp with time zone;
update content set checkindate = lastmodifieddate;

-- rio testing

							
-- Revisar sintaxis de esto
alter table scheduler modify  description     clob;
alter table scheduler modify  error_message   clob;

alter table wf_activity add role varchar2(512);
alter table wf_procedure add roles varchar2(512);

-- indices en logevent

alter table po_block_x add content_id number(19);
alter table po_block_x add jsondata clob;
alter table po_block_x add xurl  varchar2(1024);

insert into contentclass(id, enabled, name, javaclass, indexable) values('KbeeView', 1, 'View', 'com.novamens.kbee.portal.model.publish.KbeeViewDetailContent', 1);

-------------------------------
-- Mayo 11
-- 
 alter table userprofile add email_rule_notifications number(1) default 1;
 
 
alter table kfile add  externallystored number(1) default 1;
alter table kfile add storagemode number(1) default 1;

alter table kresource add  bucket varchar2(64);
alter table userprofile  add  sendfilesemail number(1) default 1;

alter table kfile add bucketname varchar2(512);
alter table kfile add objectname varchar2(512);

update kfile K set bucketName=(select name from domain D where D.id=(select domain_id from kresource R where R.id=K.resource_id and R.domain_id=D.id));
update kfile K set objectname=path;

----------------------------------------------------------------------------------------------------------------

# 23 Abril

alter table content add column external_time timestamp with time zone;

CREATE TABLE kb_file_loader (
	id bigint NOT NULL,
	name character varying(128),
	javaclass character varying(128),
	CONSTRAINT fileloader_pk PRIMARY KEY (id)
)
WITH (
	OIDS = FALSE
);

CREATE TABLE kb_file_proxy (
	resource_id bigint NOT NULL,
	file_loader bigint NOT NULL,
	CONSTRAINT fileproxy_pk PRIMARY KEY (resource_id),
	CONSTRAINT fileproxy_loader_fk FOREIGN KEY (file_loader) REFERENCES kb_file_loader(id) ON DELETE RESTRICT,
	CONSTRAINT fileproxy_resource_fk FOREIGN KEY (resource_id) REFERENCES kresource(id) ON DELETE RESTRICT
)
WITH (
	OIDS = FALSE
);


-- 25 Abril

alter table kb_security_rule alter column description set data type text;
alter table kb_security_rule alter column condition set data type text;
alter table kb_security_rule alter column displaycondition set data type text;


-- 26 Abril
alter table kb_classifier add column is_api boolean default false;
alter table kb_attribute add column is_api boolean default false;
alter table kb_contenttemplate  add column is_api boolean default false;
 
-- 29 Abril
alter table kfile add column shard integer default 1;

// esto es para el api realpage 
insert into kb_file_loader(id, name, javaclass) values(1, 'realpage file', 'com.novamens.realpage.resource.FileLoader');
insert into kb_file_loader(id, name, javaclass) values(2, 'realpage certificate', 'com.novamens.realpage.resource.CertificateLoader');


--4 de mayo

CREATE TABLE public.api_logevent
(
    event_id bigint NOT NULL,
    event_domain character varying(128) COLLATE pg_catalog."default" NOT NULL,
    event_file character varying(128) COLLATE pg_catalog."default",
    event_time timestamp with time zone DEFAULT now(),
    event_user character varying(128) COLLATE pg_catalog."default",
    event_transaction bigint,
    event_uri character varying(256) COLLATE pg_catalog."default",
    event_method character varying(15) COLLATE pg_catalog."default",
    event_request text COLLATE pg_catalog."default",
    event_status integer,
    event_respone text COLLATE pg_catalog."default",
    event_processing_time bigint,
    CONSTRAINT api_logevent_pkey PRIMARY KEY (event_id)
)
WITH (
    OIDS = FALSE
);

alter table api_logevent add column event_retry bigint;
alter table api_logevent add column event_retrynumber int;
alter table api_logevent add column event_source character varying(32);












