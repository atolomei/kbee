alter table kb_enotirule add column event_type integer default 0;


alter table person add column photo bigint;
ALTER table person add CONSTRAINT file_fk FOREIGN KEY (photo) REFERENCES kfile (resource_id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE RESTRICT; 


alter table kb_contenttemplate add isdefault boolean default false;
alter table kfile add column crc32str character(8);
alter table dataset add column canonical boolean default false;
alter table kb_enotirule add column state integer;
alter table person add workposition character varying(256);



CREATE TABLE kb_usage_stat
(
  domain_id bigint NOT NULL,
  ts timestamp with time zone DEFAULT now(),
  hard_disk_usage bigint,
  users bigint,
  contents bigint,
  resources bigint,
  attributes  text,

  CONSTRAINT usage_stat_pkey PRIMARY KEY (domain_id, ts),
  CONSTRAINT usage_stat_domain_id FOREIGN KEY (domain_id) REFERENCES domain(id) on delete cascade
  
)
WITH (
  OIDS=FALSE
);

ALTER TABLE kb_usage_stat  OWNER TO kbee;

CREATE INDEX kb_usage_domain_idx  ON kb_usage_stat USING btree (domain_id, ts);


CREATE TABLE kb_preference
(
  id  bigint NOT NULL,
  user_id bigint NOT NULL,
  name character varying(128) NOT NULL,
  properties text,
  CONSTRAINT user_name_fk UNIQUE(user_id, name),
  CONSTRAINT user_id_fk FOREIGN KEY (user_id)  REFERENCES users (id) MATCH SIMPLE  ON UPDATE NO ACTION ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);


alter table kb_classifiertemplate add iscanonical boolean default true;


alter table kb_contenttemplate add column isaudio    boolean default false;
alter table kb_contenttemplate add column istext 	 boolean default false;
alter table kb_contenttemplate add column isdocument boolean default false;
alter table kb_contenttemplate add column isphoto 	 boolean default false;	




alter table UserLabel add column context character varying(128);


CREATE TABLE kb_tip
(
  id 			bigint NOT NULL,
  domain_id 	bigint,

  area character(6),
  
  status  			integer,
  lastmodifieddate 	timestamp with time zone DEFAULT now(),
  lastmodifieduser 	bigint,
  
  tip_title 		character varying(256) NOT NULL,
  tip_text 			text,
  tip_texyid  		character varying(32),
  tip_lang	   		character varying(32),
	
  CONSTRAINT tip_pkey PRIMARY KEY (id)
  
)
WITH (
  OIDS=FALSE
);
ALTER TABLE kb_tip OWNER TO kbee;

CREATE INDEX kb_tip_id_idx  ON kb_tip  USING btree  (tip_lang, area, lower(tip_title));




alter table  classifier rename to kb_classifier;

alter table kb_contenttemplate add column istool boolean default false;
alter table kb_contenttemplate add column isactivity boolean default false;	

alter table kb_classifier add column metadatasubtitle boolean default false;
alter table KB_CLASSIFIERTEMPLATE add column ismetadatasubtitle boolean default false;
alter table wf_launcher add column isenabled boolean default true;




alter table kb_tip add column tip_area character(18);
alter table userprofile add column tipoftheday boolean default true;

 alter table  kb_contenttemplate add column linkresources boolean default true;



alter table dataset add column abbreviation character(18);



alter table domain add column istemplate boolean default false;
update domain set type=4 where name='kbee';
alter table domain add column maxusers integer default 0;

alter table users add column active boolean default true;

alter table aclentry alter column permissions type character varying(128);

alter table wf_procedure add column initial_rules text;



alter table users add column creationdate timestamp with time zone default now();
alter table profile add column creationdate timestamp with time zone default now();
alter table principal add column creationdate timestamp with time zone default now();
alter table kb_domain_settings add column lastmodifieddate timestamp with time zone default now();
alter table kcomment add column creationdate timestamp with time zone default now();
alter table userlabel add column creationdate timestamp with time zone default now();
alter table kb_enotirule add column creationdate timestamp with time zone default now();
alter table securityrule add column creationdate timestamp with time zone default now();					
alter table acl add column creationdate timestamp with time zone default now();
alter table users add column timezone character varying(256) default 'US/Central';
 

update kgroup set name = 'User' where name ='ROLE_USER';
update kgroup set name = 'Domain Admin' where name ='ROLE_DOMAIN_ADMIN';
update kgroup set name = 'Support' where name ='ROLE_SUPPORT';
update kgroup set name = 'Security' where name ='ROLE_SECURITY';
update kgroup set name = 'Information Model' where name ='ROLE_INFORMATION_MODEL';
update kgroup set name = 'Datasets Members' where name ='ROLE_DATASET_MEMBERS';
update kgroup set name = 'Monitor' where name ='ROLE_MONITOR';
update kgroup set name = 'Workspace' where name ='ROLE_WORKSPACE';
update kgroup set name = 'Content Base' where name ='ROLE_CONTENT_BASE';
update kgroup set name = 'Auditor' where name ='ROLE_AUDITOR';
update kgroup set name = 'Portal Admin' where name ='ROLE_PORTAL_ADMIN';
update kgroup set name = 'Forms Library' where name ='ROLE_FORMS_LIBRARY';



ALTER TABLE savedquery DROP CONSTRAINT userprofile_id; 
ALTER TABLE savedquery ADD CONSTRAINT userprofile_id FOREIGN KEY (userprofile_id)  REFERENCES public.userprofile (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE CASCADE;

drop table activity;
drop table banner;
drop table htmltext;
drop table tool;

alter table acl 	   rename to kb_acl;
alter table aclentry   rename to kb_aclentry;
alter table vote       rename to kb_vote;

alter table domain add column tipoftheday boolean default true;
alter table UserProfile add column editperson boolean default true;


update kgroup set name = 'Templates' where name ='ROLE_FORMS_LIBRARY';


alter table wf_launcher add column acl bigint;
alter table wf_launcher add CONSTRAINT acl_fk FOREIGN KEY (acl) REFERENCES kb_acl(id);

alter table notification rename to kb_notification;
alter table kcomment rename to kb_comment;
 
alter table content drop constraint content_prev_version_key;


CREATE TABLE kb_email_template
(
    id bigint NOT NULL,
    creationdate timestamp with time zone DEFAULT now(),
    lastmodifieddate timestamp with time zone DEFAULT now(),
    lastmodifieduser bigint NOT NULL,
    state integer,
    domain_id bigint,
	lang		 character varying(24),
	xkey			 character varying(256),
    title 		 character varying(256),
	fromstr	 	 character varying(512),
	subject 	 character varying(512),
    strtext 	 text,
    
    CONSTRAINT email_template_pkey PRIMARY KEY (id),
    CONSTRAINT dlk unique (domain_id, lang, xkey),	 
    CONSTRAINT domain_fk FOREIGN KEY (domain_id) REFERENCES domain (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE CASCADE,
    CONSTRAINT user_fk FOREIGN KEY (lastmodifieduser) REFERENCES users (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE RESTRICT
)
WITH (
    OIDS = FALSE
);


CREATE INDEX kb_email_template_domain_idx ON public.kb_email_template USING btree (domain_id, lang, xkey);

update domain set type=3 where name='rio';


alter table kfile drop constraint file_fk;

alter table kfile add constraint file_fk FOREIGN KEY (resource_id) REFERENCES public.kresource (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE CASCADE;
alter table person drop constraint file_fk;
alter table person add constraint file_fk foreign key(photo) references kfile(resource_id) match simple on update no action on delete set null;
 


-- create role kbee with password 'novamens' superuser;

alter table scheduler add column title character varying(64);
alter table logevent add column event_procedure character varying(64);


alter table dataset add column secured boolean default false;

alter table kb_attributetemplate add column metadatasubtitle boolean default false;


CREATE TABLE kb_attribute
(
	id bigint			 NOT NULL,
	creationdate		 timestamp with time zone DEFAULT now(),
	lastmodifieddate	 timestamp with time zone DEFAULT now(),
	lastmodifieduser	 bigint NOT NULL,
	state				 integer,
	domain_id			 bigint,
	name		 		 character varying(128),
	type				 integer,
	multiplicity		 integer,
	uniquename			 character varying(128),
	korder				 integer,
	iscanonical			 boolean default false,
	metadatasubtitle	 boolean default false,
	visibility			 text,
	
	CONSTRAINT attribute_pkey PRIMARY KEY (id),
	CONSTRAINT domain_fk FOREIGN KEY (domain_id) REFERENCES domain (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE CASCADE,
	CONSTRAINT user_fk FOREIGN KEY (lastmodifieduser) REFERENCES users (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE RESTRICT
)
WITH (
    OIDS = FALSE
);


alter table  kb_attributetemplate drop column name;
alter table  kb_attributetemplate drop column type;
alter table  kb_attributetemplate drop column multiplicity;
alter table  kb_attributetemplate drop column mandatory;


alter  table     datasetmember add column rule_id bigint;
alter  table	 datasetmember add CONSTRAINT rule_fk FOREIGN KEY (rule_id) REFERENCES securityrule (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE CASCADE;

alter  table     datasetmember add column group_id bigint;
alter  table	 datasetmember add CONSTRAINT group_fk FOREIGN KEY (group_id) REFERENCES kgroup (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE CASCADE;

alter table   kb_attributetemplate add  column attribute_id bigint;
alter table	  kb_attributetemplate add CONSTRAINT attribute_fk FOREIGN KEY (attribute_id) REFERENCES kb_attribute (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE RESTRICT;
alter table   kb_classifiertemplate rename column ismetadatasubtitle to metadatasubtitle;

alter table kgroupmember drop constraint group_fk;
alter table kgroupmember add constraint group_fk foreign key(kgroup) references kgroup(id) on delete cascade;

alter  table  securityrule add column derived boolean default false;

alter table domain add column lang character(6) default 'en';

alter  table  securityrule add column displaycondition character varying(4096);
update  securityrule set displaycondition=description;


alter table  datasetmember drop constraint rule_fk;
alter table	 datasetmember add CONSTRAINT rule_fk FOREIGN KEY (rule_id) REFERENCES securityrule (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE RESTRICT;

alter table datasetmember drop constraint group_fk;
alter table datasetmember add CONSTRAINT group_fk FOREIGN KEY (group_id) REFERENCES kgroup (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE RESTRICT;

ALTER TABLE classification drop constraint content_fk;
ALTER TABLE classification ADD CONSTRAINT content_fk FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE CASCADE;


alter table kgroup add column derived boolean default false;





delete from report;
alter table report rename  to kb_report;
alter table kb_report add column id bigint not null;
alter table kb_report drop constraint report_pkey;
alter table kb_report add constraint report_pkey primary key (id);

alter table kb_vote add column id bigint;
update kb_vote set id=-1 * (user_id  * 10000000 + content_id);

alter table kb_vote drop constraint vote_pkey;
alter table kb_vote add constraint vote_pkey primary key(id);  

alter table securityrule rename to kb_security_rule;
alter table kb_security_rule add column parent_objectid character varying(48);
 
alter table content add column user_defined_properties text;

alter table content add column external_id character varying(64);

alter table kb_contenttemplate add column isadd boolean default false;
alter table kb_contenttemplate add column iscustomattributes boolean default false;


alter table content alter column external_id type character varying(128);


CREATE TABLE public.kb_user_property
(
    id bigint NOT NULL,
    type integer,
    name character varying(128) COLLATE pg_catalog."default",
    user_id bigint NOT NULL,
    value text COLLATE pg_catalog."default",
    
	CONSTRAINT userproperty_pkey PRIMARY KEY (id),
    CONSTRAINT user_fk FOREIGN KEY (user_id)
        REFERENCES public.users (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE
)
WITH (
    OIDS = FALSE
)
TABLESPACE pg_default;

ALTER TABLE public.kb_user_property OWNER to kbee;

CREATE INDEX property_user_id_idx ON public.kb_user_property USING btree (user_id) TABLESPACE pg_default;
	

alter table kb_aclentry drop constraint principal_fk;
alter table kb_aclentry add constraint principal_fk FOREIGN KEY (principal) REFERENCES Principal(id) ON DELETE CASCADE;

	
CREATE TABLE Kb_Cabinet (
	id				 		 bigint  NOT  NULL,
	domain_id				 bigint 	NOT  NULL,
	lastModifiedDate		 TIMESTAMP WITH TIME ZONE DEFAULT current_timestamp,
	creationDate			 TIMESTAMP  WITH TIME ZONE DEFAULT current_timestamp,
	lastModifiedUser		 bigint  NOT NULL,
	display_name 			 character varying(256),
	criteria				 character varying(256),
	state					 int,
	readonly				 boolean default false,
	reader_group			 bigint,
	listorder				 int default 0,	
	CONSTRAINT cabinet_pkey PRIMARY KEY (id),
	CONSTRAINT lastModifiedUser_fk 	FOREIGN KEY (lastModifiedUser) REFERENCES  users(id),		
	CONSTRAINT domain_fk FOREIGN KEY (domain_id) REFERENCES domain (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE CASCADE,
	CONSTRAINT group_fk FOREIGN KEY (reader_group) REFERENCES content(id)		
)
WITH (
	OIDS=FALSE
);


CREATE INDEX cabinet_domain_idx ON public.kb_cabinet USING btree (domain_id, listorder) TABLESPACE pg_default;

alter table datasetmember add column notes text;
alter table kb_contenttemplate add column iskbase boolean default false;


alter table domain add column cabinet_template boolean default true;
alter table domain add column cabinet_kbase  boolean default false;
alter table domain add column cabinet_external  boolean default false;

alter table kb_cabinet add column key character varying(24);
update kb_cabinet  set key=lower(display_name);
update kb_cabinet  set key='kbase' where key like 'k%';


alter table domain add column logo_url character varying(1024);




alter table kb_classifiertemplate add CONSTRAINT classifier_fk FOREIGN KEY (classifier_id) REFERENCES kb_classifier (id) ON DELETE RESTRICT;
alter table datasetmember add column  external_url character varying(1024);


alter table dataset add column external_subtype integer DEFAULT 0;



CREATE TABLE public.kb_system_properties
(
    key    character varying(256) not null,
	value  character varying(2048),
	CONSTRAINT sp_pkey PRIMARY KEY (key)
)
WITH (
    OIDS = FALSE
)
TABLESPACE pg_default;


CREATE UNIQUE INDEX sp_lower_case_key ON kb_system_properties ((lower(key)));

delete from kb_system_properties;

insert into kb_system_properties (key, value) values ('support1.pwd', 				'1Aqqqqqq');
insert into kb_system_properties (key, value) values ('support1.email', 			'ryan.garneau@realpage.com');
insert into kb_system_properties (key, value) values ('support1.phone', 			'802-316-3521');

insert into kb_system_properties (key, value) values ('support2.pwd', 				'1Aqqqqqq');
insert into kb_system_properties (key, value) values ('support2.email', 			'james.burbo@realpage.com');
insert into kb_system_properties (key, value) values ('support2.phone', 			'802-316-3525');
																	
insert into kb_system_properties (key, value) values ('default_timezone', 			'US/Central');
insert into kb_system_properties (key, value) values ('default_noreply',   			'noreply@rpdm.realpage.com');
insert into kb_system_properties (key, value) values ('default_labels',   			'Draft;Delete;Follow up;Duplicate');
																				
insert into kb_system_properties (key, value) values ('default_type_values',         'Training;Tenant Selection Plan;Contract;EOM Financials;Mortgage Statement;Lease Agreement;Rent Schedule;Management Agreements;Non-Disclosure Agreement;Shareholder Meetings;Lawsuits;Acquisitions;Due Diligence;Territory Assignments;Sales Incentives;Compensation Plan;Hardware;Software;System Logs;Benefits;Organizational Chart;Annual Reviews;Offer Letters;Signage;Brochures;Flyers');
insert into kb_system_properties (key, value) values ('default_status_values',       'Draft;Under Review;Approved;Final;Cancelled');
insert into kb_system_properties (key, value) values ('default_department_values',   'Marketing;HR;IT;Sales;Legal;Finance;Compliance;Property Management;Facilities;Training');







insert into kb_system_properties (key, value) values ('dataset_type.name', 'File Type');
insert into kb_system_properties (key, value) values ('classifier_type.name', 'File Type');


insert into kb_system_properties (key, value) values ('dataset_status.name', 'Status');
insert into kb_system_properties (key, value) values ('classifier_status.name', 'Status');


insert into kb_system_properties (key, value) values ('"dataset_property.name', 'Property');
insert into kb_system_properties (key, value) values ('classifier_property.name', 'Property');


insert into kb_system_properties (key, value) values ('dataset_department.name', 'Department');
insert into kb_system_properties (key, value) values ('classifier_department.name', 'Department');


insert into kb_system_properties (key, value) values ('dataset_organization.name', 'Organization');
insert into kb_system_properties (key, value) values ('classifier_organization.name', 'Organization');


insert into kb_system_properties (key, value) values ('dataset_owner.name', 'Owner');
insert into kb_system_properties (key, value) values ('classifier_owner.name', 'Owner');


insert into kb_system_properties (key, value) values ('attribute_date.name.name', 'Effective Date');

insert into kb_system_properties (key, value) values ('cabinet_standard',	'Enterprise');
insert into kb_system_properties (key, value) values ('cabinet_templates',	'Templates');
insert into kb_system_properties (key, value) values ('cabinet_kbase',		'Knowledge Base');
insert into kb_system_properties (key, value) values ('cabinet_external',	'OneSite');
insert into kb_system_properties (key, value) values ('cabinet_all',		'All');
insert into kb_system_properties (key, value) values ('rpdm.login.banner.url', 'https://www.realpage.com/document-management-solutions/');
 
alter table content add column private_notes text;


alter table kb_contenttemplate add column private_notes boolean default false;


CREATE TABLE Kb_Import_Data (
	server_url				 character varying(256)  NOT  NULL,
	remote_domain			 character varying(64)   NOT  NULL,
	object_class			 character varying(128)  NOT  NULL,
	remote_id				 bigint  NOT  NULL,
	local_id				 bigint  NOT  NULL,
	local_domain			 character varying(64) NOT  NULL,
	import_time				 TIMESTAMP WITH TIME ZONE DEFAULT current_timestamp,
	CONSTRAINT import_data_pkey PRIMARY KEY (server_url, remote_domain, local_domain, object_class, remote_id)
)
WITH (
	OIDS=FALSE
);

alter table kresource add column ispublic boolean default true;


alter table domain add column isapienabled boolean default true;

update kgroup set name='domain-admin' where name='Domain Admin';
update kgroup set name='user' where name='User';
update kgroup set name='mytasks' where name='Workspace';
update kgroup set name='library' where name='Content Base';
update kgroup set name='archive' where name='Archive';
update kgroup set name='security' where name='Security';
update kgroup set name='monitor' where name='monitor';
update kgroup set name='workflow' where name='Workflow';
update kgroup set name='templates' where name='Templates';
update kgroup set name='information-model' where name='Information Model';
update kgroup set name='support' where name='Support';
update kgroup set name='dataset-values' where name like 'DataSet %';
update kgroup set name='system-log' where name='System Log';
update kgroup set name='auditor' where name='Auditor';
update kgroup set name='portal-admin' where name='Portal Admin';
update kgroup set name='external' where name like 'External %';
update kgroup set name='knowledge-base' where name='Knowledge Base';
update kgroup set name='enterprise' where name='Corporate';
update kgroup set name='corporate-admin' where name='Corporate Admin';
update kgroup set name='corporate-auditor' where name='Corporate Auditor';
update kgroup set name='corporate-editor' where name='Corporate Editor';


update kgroup set name='monitor' where name='Monitor';
update kgroup set name='external' where name='External';
update kgroup set name='templates' where name='Templates';	

update kgroup set name='knowledge-base' where name='Forms Library';
update kgroup set name='archive' where name='ROLE_ARCHIVE';
update kgroup set name='root' where name='ROLE_ROOT';

update kgroup set name='dataset-values' where name like 'dataSet-%';
 
			
alter table kb_contenttemplate add abstract_label 			character varying(128);
alter table kb_contenttemplate add private_notes_label  	character varying(128);
alter table kb_contenttemplate add text_notes_label 		character varying(128);
alter table kb_contenttemplate add text_label 				character varying(128);
alter table kb_contenttemplate add customattributes_label 	character varying(128);




alter table kfile  add uploadeddate timestamp with time zone DEFAULT now();
alter table kfile  add uploadeduser bigint;
update      kfile set uploadeduser = (select id from users where username='root@kbee');
alter table kfile add CONSTRAINT des_user_fk FOREIGN KEY (uploadeduser) REFERENCES public.users (id) MATCH SIMPLE;

update kfile set uploadeduser = (select k.lastmodifieduser from kresource k where k.id=resource_id);
update kfile set uploadeddate  = (select k.lastmodifieddate from kresource k where k.id=resource_id);



alter table wf_activity add resolutiontitle character varying(256);

alter table wf_procedure add code character(12);

update wf_procedure set code='AS' where name='Assign';
update wf_procedure set code='CS' where name='Submission2';
update wf_procedure set code='CN' where name='Submission';
update wf_procedure set code='ST' where name='Standard';
update wf_procedure set code='CF' where name like 'Compliance %';





CREATE TABLE po_area (
    po_id bigint NOT NULL,
    page_id bigint NOT NULL,
    area_type integer,
    orden integer,
    full_width_canvas boolean DEFAULT false,
    areaclass character varying(128)
);


ALTER TABLE po_area OWNER TO postgres;

--
-- Name: po_block; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE po_block (
    po_id bigint NOT NULL,
    area_id bigint NOT NULL,
    section integer,
    orden integer,
    subtitle character varying(256),
    textstyle character varying(128),
    image_id bigint,
    new_tab boolean DEFAULT false,
    maxlements integer,
    quantity_visible boolean DEFAULT false,
    title_visible boolean DEFAULT true,
    intro_visible boolean DEFAULT true,
    image_visible boolean DEFAULT false,
    intro_only_image boolean DEFAULT false,
    external_link character varying(128),
    page_link bigint,
    content_link bigint,
    block_image bigint,
    block_menu_enabled boolean DEFAULT true,
    description character varying(4096),
    block_css character varying(128),
    usage_info character varying(2048),
    image_css character varying(64),
    block_body_style character varying(1024)
);


ALTER TABLE po_block OWNER TO postgres;

--
-- Name: po_block_banners; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE po_block_banners (
    block_id bigint NOT NULL
);


ALTER TABLE po_block_banners OWNER TO postgres;

--
-- Name: po_block_contact; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE po_block_contact (
    block_id bigint NOT NULL,
    emailto character varying(256)
);


ALTER TABLE po_block_contact OWNER TO postgres;

--
-- Name: po_block_content_list; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE po_block_content_list (
    block_id bigint NOT NULL,
    query character varying(256),
    block_subtype integer DEFAULT 0,
    thumbnail_enabled boolean DEFAULT false,
    metadata_enabled boolean DEFAULT false,
    description_enabled boolean DEFAULT false,
    max_description_length integer DEFAULT 0,
    population_mode integer DEFAULT 1,
    thumbnail_size_mode integer DEFAULT 0,
    thumbnail_pos integer DEFAULT 1,
    element_title_enabled boolean DEFAULT true
);


ALTER TABLE po_block_content_list OWNER TO postgres;

--
-- Name: po_block_cumpleanos; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE po_block_cumpleanos (
    block_id bigint NOT NULL,
    date_from timestamp without time zone,
    date_to timestamp without time zone,
    image_visible boolean DEFAULT true,
    feriados text
);


ALTER TABLE po_block_cumpleanos OWNER TO postgres;

--
-- Name: po_block_footer; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE po_block_footer (
    block_id bigint NOT NULL,
    element_css character varying(512)
);


ALTER TABLE po_block_footer OWNER TO postgres;

--
-- Name: po_block_gallery_viewer; Type: TABLE; Schema: public; Owner: kbee
--

CREATE TABLE po_block_gallery_viewer (
    block_id bigint NOT NULL
);


ALTER TABLE po_block_gallery_viewer OWNER TO kbee;

--
-- Name: po_block_image_viewer; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE po_block_image_viewer (
    block_id bigint NOT NULL,
    link_container_css character varying(64),
    image_container_css character varying(64),
    imageviewer_id bigint,
    url character varying(256)
);


ALTER TABLE po_block_image_viewer OWNER TO postgres;

--
-- Name: po_block_search_external; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE po_block_search_external (
    block_id bigint NOT NULL,
    container_css character varying(64),
    element_css character varying(64),
    url character varying(2048)
);


ALTER TABLE po_block_search_external OWNER TO postgres;

--
-- Name: po_block_select_list; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE po_block_select_list (
    block_id bigint NOT NULL,
    select_container_css character varying(64),
    select_css character varying(64),
    select_list_str character varying(8192)
);


ALTER TABLE po_block_select_list OWNER TO postgres;

--
-- Name: po_block_selector; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE po_block_selector (
    block_id bigint NOT NULL,
    element_css character varying(512)
);


ALTER TABLE po_block_selector OWNER TO postgres;

--
-- Name: po_block_site_components; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE po_block_site_components (
    block_id bigint NOT NULL,
    site_id bigint NOT NULL,
    block_type integer DEFAULT 0
);


ALTER TABLE po_block_site_components OWNER TO postgres;

--
-- Name: po_block_site_list; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE po_block_site_list (
    block_id bigint NOT NULL,
    query character varying(256),
    element_title_enabled boolean DEFAULT true
);


ALTER TABLE po_block_site_list OWNER TO postgres;

--
-- Name: po_block_text; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE po_block_text (
    block_id bigint NOT NULL,
    text_css character varying(64),
    max_description_length integer DEFAULT 0
);


ALTER TABLE po_block_text OWNER TO postgres;

--
-- Name: po_block_view_list; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE po_block_view_list (
    block_id bigint NOT NULL,
    block_subtype integer DEFAULT 0,
    thumbnail_enabled boolean DEFAULT false,
    metadata_enabled boolean DEFAULT false,
    description_enabled boolean DEFAULT false,
    max_description_length integer DEFAULT 0,
    thumbnail_size_mode integer DEFAULT 0,
    thumbnail_pos integer DEFAULT 1,
    population_mode integer DEFAULT 1,
    element_title_enabled boolean DEFAULT true,
    element_css character varying(512),
    inline_filter boolean DEFAULT false,
    hitpanelmenu_enabled boolean DEFAULT true,
    sorted boolean DEFAULT false,
    element_link_resource boolean DEFAULT false,
    sort_type integer DEFAULT 0,
    title_type integer DEFAULT 0,
    block_helper character varying(2048),
    multiblockstyle integer DEFAULT 1,
    layoutmode integer DEFAULT 1,
    subtitle_mode integer DEFAULT 0
);


ALTER TABLE po_block_view_list OWNER TO postgres;

--
-- Name: po_block_view_recent_list; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE po_block_view_recent_list (
    block_id bigint NOT NULL,
    global boolean DEFAULT false
);


ALTER TABLE po_block_view_recent_list OWNER TO postgres;

--
-- Name: po_block_wall_viewer; Type: TABLE; Schema: public; Owner: kbee
--

CREATE TABLE po_block_wall_viewer (
    block_id bigint NOT NULL
);


ALTER TABLE po_block_wall_viewer OWNER TO kbee;

--
-- Name: po_block_x; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE po_block_x (
    block_id bigint NOT NULL
);


ALTER TABLE po_block_x OWNER TO postgres;

--
-- Name: po_contentblock; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE po_contentblock (
    block_id bigint NOT NULL,
    content_id bigint NOT NULL,
    orden integer
);


ALTER TABLE po_contentblock OWNER TO postgres;

--
-- Name: po_page; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE po_page (
    po_id bigint NOT NULL,
    site_id bigint,
    description character varying(128),
    relative_url character varying(128),
    is_admin boolean DEFAULT false,
    issection boolean DEFAULT false,
    ishome boolean DEFAULT false,
    orden integer DEFAULT 0,
    page_type integer DEFAULT 0,
    content_link bigint,
    is_header_container boolean DEFAULT false,
    contentid character varying(256),
    menus_visible boolean DEFAULT true,
    usage_info character varying(2048)
);


ALTER TABLE po_page OWNER TO postgres;

--
-- Name: po_portalobject; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE po_portalobject (
    id bigint NOT NULL,
    oid bigint,
    parent_id bigint,
    creationdate timestamp with time zone DEFAULT now(),
    lastmodifieddate timestamp with time zone DEFAULT now(),
    lastmodifieduser bigint NOT NULL,
    state integer,
    domain_id bigint DEFAULT 1,
    name character varying(256),
    title character varying(256),
    version integer,
    prev_version bigint,
    kmode integer,
    ishead boolean DEFAULT true
);


ALTER TABLE po_portalobject OWNER TO postgres;

--
-- Name: po_site; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE po_site (
    po_id bigint NOT NULL,
    site_type integer,
    ispublic boolean DEFAULT true,
    isexternal boolean DEFAULT false,
    subtitle character varying(512),
    description character varying(512),
    uri character varying(256),
    detail_comments_enabled boolean DEFAULT true,
    detail_votes_enabled boolean DEFAULT true,
    detail_follow_enabled boolean DEFAULT true,
    detail_related_enabled boolean DEFAULT true,
    detail_send_enabled boolean DEFAULT true,
    footer_block_id bigint,
    header_block_id bigint,
    email_contact character varying(512),
    site_template integer,
    page_header_footer_id bigint,
    site_image bigint,
    isimagevisible boolean DEFAULT false
);


ALTER TABLE po_site OWNER TO postgres;

--
-- Name: po_site_favorites; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE po_site_favorites (
    id bigint NOT NULL,
    user_id bigint NOT NULL
);


ALTER TABLE po_site_favorites OWNER TO postgres;

--
-- Name: po_site_favorites_list; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE po_site_favorites_list (
    list_id bigint NOT NULL,
    site_oid bigint NOT NULL,
    orden integer
);


ALTER TABLE po_site_favorites_list OWNER TO postgres;

--
-- Name: po_site_securityrule; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE po_site_securityrule (
    rule_id bigint NOT NULL,
    related_object_id character varying(48)
);


ALTER TABLE po_site_securityrule OWNER TO postgres;

--
-- Name: po_site_subscription; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE po_site_subscription (
    user_id bigint NOT NULL,
    site_oid bigint NOT NULL,
    event_id integer NOT NULL,
    subscription_date timestamp with time zone DEFAULT now(),
    type_id integer
);


ALTER TABLE po_site_subscription OWNER TO postgres;

--
-- Name: po_sitelogin; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE po_sitelogin (
    id bigint NOT NULL,
    user_id bigint,
    user_name character varying(256),
    site_id bigint,
    site_title character varying(256),
    page_id bigint,
    page_type character varying(18),
    page_title character varying(256),
    content_title character varying(256),
    visit_time timestamp with time zone DEFAULT now(),
    src character varying(256),
    browser character varying(128),
    device character varying(128),
    os character varying(128),
    ip character varying(48),
    domain_id bigint,
    content_id character varying(64),
    render_milisecs bigint,
    session_id character varying(48),
    user_agent character varying(512)
);


ALTER TABLE po_sitelogin OWNER TO postgres;

--
-- Name: po_sitelogout; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE po_sitelogout (
    id bigint NOT NULL,
    user_id bigint,
    user_name character varying(256),
    site_id bigint,
    site_title character varying(256),
    page_id bigint,
    page_type character varying(18),
    page_title character varying(256),
    block_id bigint,
    block_title character varying(128),
    view_id bigint,
    view_type character varying(18),
    view_content_id character varying(64),
    view_link character varying(128),
    view_site_id bigint,
    visit_time timestamp with time zone DEFAULT now(),
    browser character varying(128),
    device character varying(128),
    os character varying(128),
    ip character varying(48),
    domain_id bigint,
    view_title character varying(256)
);


ALTER TABLE po_sitelogout OWNER TO postgres;

--
-- Name: po_siteuser; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE po_siteuser (
    site_id bigint NOT NULL,
    user_id bigint NOT NULL,
    permission integer
);


ALTER TABLE po_siteuser OWNER TO postgres;

--
-- Name: po_siteuserrights; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE po_siteuserrights (
    site_id bigint NOT NULL,
    user_id bigint NOT NULL,
    permissions integer,
    lastmodifieddate timestamp with time zone DEFAULT now(),
    lastmodifieduser bigint NOT NULL
);


ALTER TABLE po_siteuserrights OWNER TO postgres;

--
-- Name: po_viewbk; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE po_viewbk (
    po_id bigint NOT NULL,
    block_id bigint NOT NULL,
    "position" integer,
    title character varying(256),
    abstract character varying(2048),
    image_id bigint,
    metadata character varying(128),
    style_width character varying(64),
    style_height character varying(64),
    style character varying(64),
    ntab boolean DEFAULT false
);


ALTER TABLE po_viewbk OWNER TO postgres;

--
-- Name: po_viewbkblock; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE po_viewbkblock (
    view_id bigint NOT NULL,
    block_id bigint
);


ALTER TABLE po_viewbkblock OWNER TO postgres;

--
-- Name: po_viewbkcontent; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE po_viewbkcontent (
    view_id bigint NOT NULL,
    content_id bigint,
    is_gallery boolean DEFAULT false,
    is_resources boolean DEFAULT true
);


ALTER TABLE po_viewbkcontent OWNER TO postgres;

--
-- Name: po_viewbklink; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE po_viewbklink (
    view_id bigint NOT NULL,
    link character varying(1024)
);


ALTER TABLE po_viewbklink OWNER TO postgres;

--
-- Name: po_viewbksite; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE po_viewbksite (
    view_id bigint NOT NULL,
    site_id bigint
);


ALTER TABLE po_viewbksite OWNER TO postgres;

--
-- Name: po_viewcontent; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE po_viewcontent (
    po_id bigint NOT NULL,
    site_id bigint NOT NULL,
    content_id bigint NOT NULL
);


ALTER TABLE po_viewcontent OWNER TO postgres;

--
-- Name: po_viewcontentrelation; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE po_viewcontentrelation (
    view_id bigint NOT NULL,
    target_id bigint NOT NULL,
    "position" integer
);


ALTER TABLE po_viewcontentrelation OWNER TO postgres;





