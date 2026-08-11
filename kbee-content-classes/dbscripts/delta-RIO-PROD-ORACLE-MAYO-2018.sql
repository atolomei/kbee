alter table content add external_time timestamp with time zone;

CREATE TABLE kb_file_loader (
	id number(19) NOT NULL,
	name varchar2(128),
	javaclass varchar2(128),
	CONSTRAINT fileloader_pk PRIMARY KEY (id)
) TABLESPACE SBD02_DATA;


CREATE TABLE kb_file_proxy (
	resource_id number(19) NOT NULL,
	file_loader number(19) NOT NULL,
	CONSTRAINT fileproxy_pk PRIMARY KEY (resource_id),
	CONSTRAINT fileproxy_loader_fk FOREIGN KEY (file_loader) REFERENCES kb_file_loader(id),
	CONSTRAINT fileproxy_resource_fk FOREIGN KEY (resource_id) REFERENCES kresource(id)
) TABLESPACE SBD02_DATA;


-- 25 Abril

alter table kb_security_rule add description2 clob;
alter table kb_security_rule add condition2 clob;
alter table kb_security_rule add displaycondition2  clob;

update kb_security_rule set description2=description, condition2=condition, displaycondition2=displaycondition;

alter table kb_security_rule drop column description;
alter table kb_security_rule rename column description2 to description;

alter table kb_security_rule drop column condition;
alter table kb_security_rule rename column condition2 to condition;

alter table kb_security_rule drop column displaycondition;
alter table kb_security_rule rename column displaycondition2 to displaycondition;

-- 26 Abril
alter table kb_classifier add is_api        number(1) default 0;
alter table kb_attribute add is_api         number(1) default 0;
alter table kb_contenttemplate  add  is_api number(1) default 0;
 
-- 29 Abril
alter table kfile add shard  number(1) default 1;

--esto es para el api realpage 
insert into kb_file_loader(id, name, javaclass) values(1, 'realpage file', 'com.novamens.realpage.resource.FileLoader');
insert into kb_file_loader(id, name, javaclass) values(2, 'realpage certificate', 'com.novamens.realpage.resource.CertificateLoader');

--4 de mayo

CREATE TABLE api_logevent
(
    event_id number(19) NOT NULL,
    event_domain varchar2(128) NOT NULL,
    event_file varchar2(128) ,
    event_time timestamp with time zone,
    event_user varchar2(128) ,
    event_transaction number(19),
    event_uri varchar2(256),
    event_method varchar2(15) ,
    event_request clob,
    event_status number(6),
    event_respone clob ,
    event_processing_time number(19),
    CONSTRAINT api_logevent_pkey PRIMARY KEY (event_id)
) TABLESPACE SBD02_DATA;


alter table api_logevent add  event_retry number(19);
alter table api_logevent add  event_retrynumber number(4);
alter table api_logevent add  event_source varchar2(32);

insert into contentclass(id, enabled, name, javaclass, indexable) values('KbeeView', 1 , 'View', 'com.novamens.kbee.portal.model.publish.KbeeViewDetailContent', 1 );
insert into contentclass(id, enabled, name, javaclass, indexable) values('KbeeLinkView', 1, 'LinkView', 'com.novamens.kbee.portal.model.publish.KbeeViewBKLink', 1);


-- 19 Mar
-- 
alter table po_block_x add  file_id number(19);
alter table po_block_x add  jsondata clob;
alter table po_block_x add  xurl  varchar2(1024);
alter table po_block_x add  content_id number(19);

alter table po_viewbk add  iconcss varchar2(64);
alter table po_block add  block_separator_css varchar2(64);
alter table po_block add  element_orientation_css varchar2(64);
alter table po_viewcontent add  issearchable number(1) default 1 ;
alter table po_portalobject add  nextversion number(6);

update po_portalobject set version=1, nextversion=2 where version=0 or nextversion is null;

alter table KB_SECURITY_RULE add displaycondition clob;
alter table KB_SECURITY_RULE add condition clob; 


-- AuditSet
alter table logevent add auditset integer default 0;
update logevent set auditset=10 where not event_content_id is null;
						
alter table userprofile add uitheme varchar2(32) default 'kbee';
alter table users add         uitheme varchar2(32) default 'kbee';



CREATE INDEX kfile_storagemode_idx ON kfile (storagemode, bucketname);
CREATE INDEX kfile_bucket_idx ON kfile (bucketname, objectname);


alter table organizationaltext drop column contentdate;
alter table organizationaltext add contentdate timestamp with time zone;



CREATE TABLE kb_contentrelation (
	id number(19) NOT NULL,
	source_id number(19) NOT NULL,
	target_id number(19) NOT NULL,
	template_id number(19) NOT NULL,
	"position" number(4),
	CONSTRAINT contentrelation_id_pk PRIMARY KEY (id),
	CONSTRAINT cr_source_fk FOREIGN KEY (source_id) REFERENCES content (id) ON DELETE CASCADE, 
	CONSTRAINT cr_target_fk FOREIGN KEY (target_id) REFERENCES content (id) ON DELETE CASCADE,
	CONSTRAINT cr_template_fk FOREIGN KEY (template_id) REFERENCES kb_relationtemplate(id)
) TABLESPACE SBD02_DATA;


alter table datasetmember  add external_member_id number(19);
update datasetmember set external_member_id = external_id;

alter table datasetmember  add external_id2 varchar2(128);
update datasetmember set external_id2 = to_char(external_id) where external_id is not null;
alter table datasetmember drop column external_id;
alter table datasetmember add external_id varchar2(128);
update datasetmember  set external_id = external_id2;

alter table domain  add external_id varchar2(128);
alter table datasetmember add labelcolor number(4) default 1;
alter table dataset add readonly number(1) default 0;
alter table kb_attribute add isfilterable number(1) default 0;

alter table kb_file_proxy add url varchar2(512);

update kfile K 			set bucketName=(select name from domain D where D.id=(select domain_id from kresource R where R.id=K.resource_id and R.domain_id=D.id)) where bucketname is null;
update kfile K 			set objectname=path where objectname is null;
update kb_file_proxy p 	set url = (select path from kfile f where f.resource_id = p.resource_id);
update kfile 			set kfsize=(select ksize from kresource where id=resource_id);



















---------------------------------------------------------------------------------------------
-- DESDE ACA SE AGREGO A PROD EL DIA 1 JUNIO 2018
-- 23 de mayo

--CREATE TABLE api_soapevent
--(   event_id number(19) NOT NULL,
--    event_file varchar2(128),
--    event_time timestamp with time zone DEFAULT current_timestamp,
--    event_user varchar2(128),
--    event_transaction number(19),
--    event_uri varchar2(256),
--    event_method varchar2(15),
--    event_request clob,
--    event_status number(6),
--    event_respone clob,
--    event_processing_time number(6),
--    event_domain varchar2(128),
--    CONSTRAINT api_soapevent_pkey PRIMARY KEY (event_id)
--   ) TABLESPACE SBD02_DATA;

--commit;

--CREATE INDEX api_soapevent_event_time_idx ON api_soapevent (event_time DESC) TABLESPACE SBD02_INDEX;

--alter table kb_usage_stat add hard_disk_usage_gateway number(19) default 0;
--alter table kb_usage_stat add resources_external number(19) default 0;

--CREATE INDEX api_logevent_time  ON api_logevent (event_time desc);
--CREATE INDEX api_soapevent_time ON api_soapevent (event_time desc);

--alter table kfile add  kfsize number(19) default 0;
--update kfile set  kfsize=(select ksize from kresource where id=resource_id);


--alter table organizationaltext drop column contentdate;
--alter table organizationaltext add contentdate timestamp with time zone;
--commit;

--alter table domain add storagemode number(4) default 1;

---------------------------------------------------------------------------------------------
-- HASTA ACA
---------------------------------------------------------------------------------------------

-------------------------------------------

-- CREATE TABLE kb_relationtemplate (
--	id number(19) NOT NULL,
--	name varchar2(128),
--	source_label varchar2(128),
--	sourcetemplate_id number(19) NOT NULL,
--	target_label varchar2(128),
--	targettemplate_id number(19) NOT NULL,
--	multiplicity number(4) DEFAULT 4,
--	aggregation number(1)  DEFAULT 0,
--	"position" number(4) default 0,
--	CONSTRAINT relationtemplate_id_pk PRIMARY KEY (id),
--	CONSTRAINT rt_source_fk FOREIGN KEY (sourcetemplate_id) REFERENCES kb_contenttemplate (id), 
--	CONSTRAINT rt_target_fk FOREIGN KEY (targettemplate_id) REFERENCES kb_contenttemplate (id)
--) TABLESPACE SBD02_DATA;

-- drop table contentrelation;

-------------------------------------------