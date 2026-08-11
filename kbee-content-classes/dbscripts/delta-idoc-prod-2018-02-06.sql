
CREATE SEQUENCE public.timer_sequence
	INCREMENT 1
	START 100
	MINVALUE 1
	MAXVALUE 9223372036854775807
	CACHE 1;
	
ALTER SEQUENCE public.timer_sequence OWNER TO kbee;

CREATE TABLE kb_timer (
	id bigint NOT NULL,
	creationdate timestamp with time zone DEFAULT now(),
	duedate timestamp with time zone,
	callback bytea,
	CONSTRAINT timer_id_pk PRIMARY KEY (id)
)
WITH (
	OIDS = FALSE
);

ALTER TABLE  public.kb_timer OWNER TO kbee;

alter table kb_timer add attemps smallint;
alter table kb_timer add error_message character varying(256);

alter table kb_domain_settings add column  id bigint;
update kb_domain_settings set id=nextval('objectid_sequence');

alter table domain add column  timezone character varying(256) DEFAULT 'US/Central';
alter table kb_classifier add column is_rule_condition boolean default true;
create index resource_name_global on kresource using btree (lower(name));
alter table content add column checkindate timestamp with time zone;
update content set checkindate = lastmodifieddate;
alter table savedquery alter column statement set data type text;

alter table kb_security_rule add column    notes text;
alter table kb_enotirule add column        notes text;

alter table kb_user_property add column uset character varying(256);	
alter table kb_user_property add column lastModifiedDate timestamp with time zone default now();

alter table property add column uset character varying(256);
alter table property add column lastModifiedDate timestamp with time zone default now();



#---------------------------------------------------------------------------------------------------------
#
# Esto es lo que falta
#
#---------------------------------------------------------------------------------------------------------

alter table wf_activity drop column duedate;
alter table kb_domain_settings drop constraint ds_pkey;
alter table kb_domain_settings alter column id set not null;
alter table kb_domain_settings add primary key(id);

alter table kb_domain_settings add constraint domain_cat_unique unique(domain_id, category);
alter table datasetmember 	alter column datevalue         set data type timestamp with time zone;
alter table classification 	alter column datevalue         set data type timestamp with time zone;
alter table entitymatching  alter column lastmodifieddate  set data type timestamp with time zone;
alter table drb_answer  	alter column date_accepted     set data type timestamp with time zone;
alter table drb_answer  	alter column date_submitted    set data type timestamp with time zone;
alter table drb_answer  	alter column date_Edited_Admin set data type timestamp with time zone;
alter table drb_question  	alter column date_submitted    set data type timestamp with time zone;
alter table drb_question  	alter column date_edited_admin set data type timestamp with time zone;

#-----------------------------------------------------------------------------------------------------------


























