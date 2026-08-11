CREATE SEQUENCE hibernate_sequence 
	INCREMENT BY 1
	MINVALUE 1000
	MAXVALUE 9223372036854775807
	START WITH 1000;
CREATE SEQUENCE domainid_sequence
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 9223372036854775807
	START WITH 10;
CREATE SEQUENCE lock_sequence
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 9223372036854775807
	START WITH 1;
CREATE SEQUENCE log_sequence
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 9223372036854775807
	START WITH 1;
CREATE SEQUENCE query_sequence
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 9223372036854775807
	START WITH 1;
CREATE SEQUENCE objectid_sequence
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 9223372036854775807
	START WITH 1000;
CREATE SEQUENCE resourceid_sequence
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 9223372036854775807
	START WITH 1000;
CREATE SEQUENCE contentid_sequence
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 9223372036854775807
	START WITH 1000;
CREATE SEQUENCE entityid_sequence
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 9223372036854775807
	START WITH 1000;
CREATE SEQUENCE classificationid_sequence
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 9223372036854775807
	START WITH 1000;
CREATE SEQUENCE security_sequence
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 9223372036854775807
	START WITH 100;
CREATE SEQUENCE scheduler_sequence
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 9223372036854775807
	START WITH 3;
CREATE SEQUENCE qaid_sequence
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 9223372036854775807
	START WITH 1;
CREATE SEQUENCE propertyid_sequence
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 9223372036854775807
	START WITH 1000;
CREATE SEQUENCE workflow_sequence
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 9223372036854775807
	START WITH 1000;
CREATE SEQUENCE portalid_sequence
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 9223372036854775807
	START WITH 1;
	
CREATE TABLE DOMAIN (
	id					NUMBER(4) NOT NULL, 
	lastModifiedDate	TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
	lastModifiedUser	NUMBER(6),
	state				NUMBER(2),
	enabled				NUMBER(1) DEFAULT 1,
	email				VARCHAR2(128),
	address				VARCHAR2(256),
	phone				VARCHAR2(128),
	website				VARCHAR2(128),
	name				VARCHAR2(128),
	creationDate 	    timestamp with time zone DEFAULT sysdate,
	
	CONSTRAINT id_pkey PRIMARY KEY (id)
) TABLESPACE SBD02_DATA;
CREATE INDEX Domain_Name ON Domain (LOWER(name)) TABLESPACE SBD02_INDEX;

CREATE TABLE USERS (
	id					NUMBER(6),
	lastModifiedDate	TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
	lastModifiedUser	NUMBER(6),
	state				NUMBER(2),
	username			VARCHAR2(120) NOT NULL,
	password			VARCHAR2(48),
	password_md5		VARCHAR2(48),
	seed				VARCHAR2(64),
	firstName			VARCHAR2(120),
	lastName			VARCHAR2(120),
	locale_str			CHAR(6) default 'eng',
	email				VARCHAR2(256),
	enabled				NUMBER(1) DEFAULT 1,
	canonical			NUMBER(1) DEFAULT 0,
	CONSTRAINT user_pkey PRIMARY KEY (id),
	CONSTRAINT user_fk FOREIGN KEY (lastModifiedUser) REFERENCES Users(id),
	CONSTRAINT username UNIQUE (username)
) TABLESPACE SBD02_DATA;


CREATE TABLE ENTITY (
	id					NUMBER(6) NOT NULL,					 
	creationDate		TIMESTAMP WITH TIME ZONE DEFAULT current_timestamp,
	lastModifiedDate	TIMESTAMP WITH TIME ZONE DEFAULT current_timestamp,
	lastModifiedUser	NUMBER(6) NOT NULL,
	state				NUMBER(2),
	domain_id			NUMBER(4),
	CONSTRAINT entity_pkey PRIMARY KEY (id),
	CONSTRAINT domain_fk FOREIGN KEY (domain_id) REFERENCES domain(id) 
) TABLESPACE SBD02_DATA;

CREATE TABLE USERLABEL (
	id			NUMBER(6) NOT NULL,					 
	user_id 	NUMBER(6),
	label		VARCHAR2(120),
	css			VARCHAR(24),
	short_label	VARCHAR(8),
	CONSTRAINT userlabel_pkey PRIMARY KEY (id),
	CONSTRAINT userlabel_user_fk FOREIGN KEY (user_id) REFERENCES users (id) 
) TABLESPACE SBD02_DATA;

CREATE TABLE PERSON (
	entity_id 		NUMBER(6) NOT NULL,
	email			VARCHAR2(120),
	address			VARCHAR2(256),
	phone			VARCHAR2(120),
	website			VARCHAR2(120),
	firstname		VARCHAR2(120),
	lastname		VARCHAR2(120),
	description		VARCHAR2(2048),
	birthdate		DATE,
	CONSTRAINT person_pk PRIMARY KEY (entity_id),
	CONSTRAINT entity_fk FOREIGN KEY (entity_id)  REFERENCES entity (id) 
) TABLESPACE SBD02_DATA;

CREATE TABLE PROFILE (
	id 					NUMBER(6) NOT NULL,
	lastmodifieddate	timestamp with time zone DEFAULT sysdate,
	lastmodifieduser	NUMBER(6) NOT NULL,
	entity				NUMBER(6),
	domain_id			NUMBER(4),
	CONSTRAINT profile_pkey PRIMARY KEY (id),
	CONSTRAINT domainPr_fk FOREIGN KEY (domain_id) REFERENCES Domain (id),
    constraint entity_profile_fk foreign key(entity) REFERENCES entity(id),
	CONSTRAINT profile_user_fk FOREIGN KEY (lastmodifieduser) REFERENCES users (id)
) TABLESPACE SBD02_DATA;

CREATE TABLE PRINCIPAL (
	id					NUMBER(6) NOT null,
	lastModifiedDate	TIMESTAMP WITH TIME ZONE DEFAULT current_timestamp,
	lastModifiedUser	NUMBER(6),
	domain_id			NUMBER(4),
	CONSTRAINT principal_pkey PRIMARY KEY (id),
	CONSTRAINT domainPrin_fk FOREIGN KEY (domain_id) REFERENCES Domain (id),
	CONSTRAINT userPrin_fk FOREIGN KEY (lastModifiedUser) REFERENCES users(id) 
) TABLESPACE SBD02_DATA;

CREATE TABLE KGROUP (
	id			NUMBER(6) NOT null,
	name		VARCHAR2(128) not null,
	description VARCHAR2(128),
	canonical   NUMBER(1),
	CONSTRAINT group_pkey PRIMARY KEY (id),
	CONSTRAINT principal_fk FOREIGN KEY (id) REFERENCES Principal(id) 
) TABLESPACE SBD02_DATA;


CREATE TABLE KGROUPMEMBER (
	kgroup		NUMBER(6) NOT null,
	principal	NUMBER(6),
	CONSTRAINT groupmember_pkey PRIMARY KEY (kgroup, principal),
	CONSTRAINT group_fk FOREIGN KEY (kgroup) REFERENCES KGroup(id),
	CONSTRAINT principalGr_fk FOREIGN KEY (principal) REFERENCES Principal(id) 
) TABLESPACE SBD02_DATA;
CREATE INDEX kgroupmember_principal_idx ON kgroupmember(principal) TABLESPACE SBD02_INDEX;


CREATE TABLE AUTHORITIES(
	username	VARCHAR2(50) NOT NULL,
	authority	VARCHAR2(50) NOT NULL,
	CONSTRAINT	authorities_pk PRIMARY KEY (username, authority),
	CONSTRAINT fk_authorities_users FOREIGN KEY (username)
	REFERENCES users (username) 
) TABLESPACE SBD02_DATA;

CREATE TABLE ACL (
	id					NUMBER(6) NOT null,
	lastModifiedDate	TIMESTAMP WITH TIME ZONE DEFAULT current_timestamp,
	lastModifiedUser	NUMBER(6),
	name				VARCHAR2(120),
	CONSTRAINT acl_pkey PRIMARY KEY (id)
) TABLESPACE SBD02_DATA;

CREATE TABLE ACLENTRY(

	acl				NUMBER(6) NOT NULL,
	principal		NUMBER(6),
	permissions		VARCHAR2(50) NOT NULL,
	negative		NUMBER(1),
	CONSTRAINT aclentry_pkey PRIMARY KEY (acl, principal, negative),
	CONSTRAINT acl_fk FOREIGN KEY (acl) REFERENCES Acl(id) ON DELETE CASCADE,
	CONSTRAINT principalAcl_fk FOREIGN KEY (principal) REFERENCES Principal(id) ON DELETE CASCADE
) TABLESPACE SBD02_DATA;


CREATE TABLE SECURITYRULE (
	id					NUMBER(6) NOT null,
	lastModifiedDate	TIMESTAMP WITH TIME ZONE DEFAULT current_timestamp,
	lastModifiedUser	NUMBER(6),
	domain_id			NUMBER(4),
	type				NUMBER(2) DEFAULT 1,
	name				VARCHAR2(150),
	condition			VARCHAR2(4000),
	description			VARCHAR2(4000),
	related_object_id   VARCHAR2(48),
	acl					NUMBER(6) NOT NULL,
	CONSTRAINT securityrule_pkey PRIMARY KEY (id),
	CONSTRAINT securityrule_domainPrin_fk FOREIGN KEY (domain_id) REFERENCES Domain (id),
	CONSTRAINT securityrule_aclSR_fk FOREIGN KEY (acl) REFERENCES Acl(id),
	CONSTRAINT securityrule_userSR_fk FOREIGN KEY (lastModifiedUser) REFERENCES users(id)
	) TABLESPACE SBD02_DATA;


	CREATE TABLE USERPROFILE (
	id 					NUMBER(6) NOT NULL,
	state 				NUMBER(2),
	confidencelevel		NUMBER(2) DEFAULT 0.0,
	user_id 			NUMBER(6),
	email_notifications NUMBER(1) default 1,
	CONSTRAINT userprofile_pkey PRIMARY KEY (id),
	CONSTRAINT profileUP_fk FOREIGN KEY (id)  REFERENCES profile (id),
	CONSTRAINT userUP_fk FOREIGN KEY (user_id)   REFERENCES users (id)
) TABLESPACE SBD02_DATA;

CREATE TABLE KLOCK(
	lock_id			NUMBER(19) NOT NULL,
	lock_object_id	VARCHAR2(100),
	lock_date		TIMESTAMP WITH TIME ZONE,
	lock_user_id	VARCHAR2(50) NOT NULL,
	lock_scope		VARCHAR2(50),
	lock_timeout	timestamp,
	CONSTRAINT lock_pkey PRIMARY KEY (lock_id)
) TABLESPACE SBD02_DATA;
CREATE INDEX klock_lock_object_id_idx  ON klock (lock_object_id) TABLESPACE SBD02_INDEX;

CREATE TABLE KB_ENOTIRULE(
	id					NUMBER(19) NOT NULL,
	lastModifiedDate	TIMESTAMP WITH TIME ZONE,
	lastModifiedUser	NUMBER(6),
	domain_id			NUMBER(4),
	name				VARCHAR2(150),
	condition			VARCHAR2(4000),
	description			VARCHAR2(4000),
	enabled				NUMBER(1) default 1,
	acl					NUMBER(6) NOT NULL,
	CONSTRAINT enotirule_pkey PRIMARY KEY (id),
	CONSTRAINT acl_enr_fk FOREIGN KEY (acl) REFERENCES Acl(id),
	CONSTRAINT domain_enr_fk FOREIGN KEY (domain_id) REFERENCES Domain (id),
	CONSTRAINT user_enr_fk FOREIGN KEY (lastModifiedUser) REFERENCES users(id)
) TABLESPACE SBD02_DATA;


CREATE TABLE KB_ENOTIRULE_PRINCIPAL (
	rule_id				NUMBER(19) NOT NULL,
	principal_id		NUMBER(19) NOT NULL,	
	CONSTRAINT enotirule_principal_pkey PRIMARY KEY (rule_id, principal_id),
	CONSTRAINT enotirule_fk FOREIGN KEY (rule_id) REFERENCES KB_ENotiRule(id),
	CONSTRAINT enotiruleprincipal_fk FOREIGN KEY (principal_id) REFERENCES principal(id)
) TABLESPACE SBD02_DATA;


CREATE TABLE LOGEVENT(
	event_id			NUMBER(19) NOT NULL,
	event_type			VARCHAR2(32) NOT NULL,
	event_object_id		VARCHAR2(32),
	event_content_id	VARCHAR2(32),
	event_version		NUMBER(3),
	event_time			timestamp with time zone DEFAULT sysdate,
	event_user			NUMBER(6),
	event_user_to		NUMBER(6),
	event_task			VARCHAR2(128),
	event_parameters	VARCHAR(256),
	event_domain_id 	NUMBER(6),
	event_title 		VARCHAR2(256),
	CONSTRAINT logevent_pkey PRIMARY KEY (event_id)
) TABLESPACE SBD02_DATA;
CREATE INDEX logevent_event_content_id_idx ON LogEvent(event_content_id) TABLESPACE SBD02_INDEX;


CREATE SEQUENCE sendemail_log_sequence 
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 9223372036854775807
	START WITH 1;

CREATE TABLE Kb_SendEmailEvent (
	event_id			NUMBER(19) NOT NULL,
	event_type			VARCHAR2(32) NOT NULL,
	event_time			timestamp with time zone DEFAULT sysdate,
	event_user			NUMBER(19),
	event_domain_id 	NUMBER(19),
	event_object_id		VARCHAR2(32),
	event_generator_action 		character varying(64), 
	email_from          VARCHAR2(128),
	email_to            VARCHAR2(128),
	email_subject  		VARCHAR2(256),
	email_text  	    CLOB,
	email_attachments   CLOB,
	event_result        VARCHAR2(64),
     CONSTRAINT sendEmailEvent_pkey PRIMARY KEY (event_id)

) TABLESPACE SBD02_DATA;

CREATE INDEX sendmailevent_idx ON Kb_SendEmailEvent(event_domain_id, event_time desc) TABLESPACE SBD02_INDEX;










CREATE TABLE SAVEDQUERY (
	id				NUMBER(6) NOT NULL,
	userprofile_id	NUMBER(6),
	title			VARCHAR2(256),
	statement		VARCHAR2(512),
	position		NUMBER(6),
	console			VARCHAR2(24),
	CONSTRAINT savedquery_pk PRIMARY KEY (id),
	CONSTRAINT userprofile_id FOREIGN KEY (userprofile_id) REFERENCES userprofile (id) 
) TABLESPACE SBD02_DATA;

CREATE TABLE SCHEDULER (
	id				NUMBER(19) NOT NULL,
	request			BLOB,
	time			timestamp with time zone DEFAULT sysdate,
	priority		NUMBER(1),
	error_count		NUMBER(1),
	description		VARCHAR2(256),
	error_message	VARCHAR2(256),
	CONSTRAINT scheduler_pk PRIMARY KEY (id)
) TABLESPACE SBD02_DATA;
CREATE INDEX scheduler_priority_idx ON scheduler (priority, time) TABLESPACE SBD02_INDEX;


CREATE TABLE ORGANIZATION( 
	id					NUMBER(6) not null,					 
	lastModifiedDate	TIMESTAMP WITH TIME ZONE DEFAULT current_timestamp,
	lastModifiedUser	NUMBER(6) NOT NULL,
	state				NUMBER(2),
	domain_id			NUMBER(4),
	email				VARCHAR2(120),
	address				VARCHAR2(256),
	phone				VARCHAR2(120),
	website				VARCHAR2(120),
	name				VARCHAR2(256),
	CONSTRAINT organization_pkey PRIMARY KEY (id),
	CONSTRAINT userOrg_fk FOREIGN KEY (lastModifiedUser) REFERENCES users(id),
	CONSTRAINT domainOrg_fk FOREIGN KEY (domain_id) REFERENCES domain(id)
) TABLESPACE SBD02_DATA;
CREATE INDEX organization_name_idx ON Organization (lower(name)) TABLESPACE SBD02_INDEX;

CREATE TABLE RESOURCEGROUP (
	id				NUMBER(6) not null,					 
	name			VARCHAR2(256),
	CONSTRAINT resourcegroup_pkey PRIMARY KEY (id)
) TABLESPACE SBD02_DATA;


CREATE TABLE KRESOURCE (
	id 					NUMBER(19) NOT NULL,
	oid 				NUMBER(19),
	lastModifiedDate	TIMESTAMP WITH TIME ZONE DEFAULT current_timestamp,
	creationDate		TIMESTAMP WITH TIME ZONE DEFAULT current_timestamp,
	lastModifiedUser	NUMBER(6) NOT NULL,
	state				NUMBER(2),
	domain_id			NUMBER(4) default 1,
	type				NUMBER(2),
	entity_id			NUMBER(19),
	group_id			NUMBER(6),
	name				VARCHAR2(256),
	title 				VARCHAR2(256),
	version				NUMBER(3),
	prev_version		NUMBER(19) UNIQUE,
	kmode				NUMBER(2),
	seed				VARCHAR2(16),
	ishead				NUMBER(1) DEFAULT 1,
	ksize				NUMBER(9),
	CONSTRAINT resource_pkey PRIMARY KEY (id),
	CONSTRAINT versionKRes_fk FOREIGN KEY (prev_version) REFERENCES KResource(id),
	CONSTRAINT userKRes_fk FOREIGN KEY (lastModifiedUser) REFERENCES users(id),
	CONSTRAINT domainKRes_fk FOREIGN KEY (domain_id) REFERENCES domain(id)
) TABLESPACE SBD02_DATA;


CREATE TABLE KFile(
	resource_id			NUMBER(19) not null,					 
	path 				VARCHAR2(256),
	file_type			VARCHAR2(5),
	title				VARCHAR2(256),
	subtitle			VARCHAR2(256),
	description			VARCHAR2(2048), 
	thumbnailsmall		VARCHAR2(128),
	thumbnaillarge  	VARCHAR2(128),
	CONSTRAINT file_pkey PRIMARY KEY (resource_id),
	CONSTRAINT file_fk	FOREIGN KEY (resource_id) REFERENCES kresource(id) 
) TABLESPACE SBD02_DATA;


CREATE TABLE ExternalResource (
	resource_id		NUMBER(19) not null,
	url				VARCHAR2(2048),
	description		VARCHAR2(1024), 
	CONSTRAINT externalresource_pkey PRIMARY KEY (resource_id),
	CONSTRAINT externalresource_fk FOREIGN KEY (resource_id) REFERENCES kresource(id)
) TABLESPACE SBD02_DATA;


CREATE TABLE HTMLText (
	resource_id			NUMBER(19) not null,					 
	htmltext 			CLOB,
	CONSTRAINT htmltext_pkey PRIMARY KEY (resource_id),
	CONSTRAINT htmltext_fk FOREIGN KEY (resource_id) REFERENCES kresource(id) 
) TABLESPACE SBD02_DATA;

CREATE TABLE ResourceFile (
	resource_id 	NUMBER(19) not null,
	file_id			NUMBER(19) not null,					 
	listorder		NUMBER(2),
	text			VARCHAR2(128),
	CONSTRAINT resourcefile_pkey PRIMARY KEY (resource_id, file_id),
	CONSTRAINT resourceRF_fk FOREIGN KEY (resource_id)	REFERENCES kresource(id),
	CONSTRAINT fileRF_fk FOREIGN KEY (file_id) REFERENCES kresource(id)
) TABLESPACE SBD02_DATA;

CREATE TABLE ContentClass(
	id 			VARCHAR2(64) not null,					 
	enabled		NUMBER(1) DEFAULT 1,
	indexable	NUMBER(1) DEFAULT 1,
	name 		VARCHAR2(128),
	javaclass	VARCHAR2(128),
 	CONSTRAINT	contentclass_pkey PRIMARY KEY (id)
) TABLESPACE SBD02_DATA;
CREATE INDEX contentclass_name_idx ON ContentClass(lower(name)) TABLESPACE SBD02_INDEX;

CREATE TABLE Kb_ContentTemplate(
	id					NUMBER(4) NOT NULL,
	creationDate		TIMESTAMP WITH TIME ZONE DEFAULT current_timestamp,
	lastModifiedDate	TIMESTAMP WITH TIME ZONE DEFAULT current_timestamp,
	lastModifiedUser	NUMBER(6) NOT NULL,
	state				NUMBER(2),
	orden 				NUMBER(2) default 1,
	domain_id			NUMBER(4),
	contentclass_id		VARCHAR2(64) not null,
	name 				VARCHAR2(128),
	instantiable		NUMBER(1) DEFAULT 1,
	relations			NUMBER(1) DEFAULT 0,
	ismultimedia		NUMBER(1) DEFAULT 0,
	hasdetailpage 		NUMBER(1) DEFAULT 1,
	abstract			NUMBER(1) DEFAULT 1,
	acl					NUMBER(6),
	isvideo				NUMBER(1) DEFAULT 0,
	istemplate 			NUMBER(1) DEFAULT 0,
	title_rule 			varchar2(256),
	
	CONSTRAINT contenttemplate_pkey PRIMARY KEY (id),
	CONSTRAINT aclct_fk FOREIGN KEY (acl) REFERENCES Acl(id) 
) TABLESPACE SBD02_DATA;

CREATE INDEX contenttemplate_name_idx ON ContentTemplate(lower(name)) TABLESPACE SBD02_INDEX;

CREATE TABLE kb_contentresourcegroup (
	template_id		NUMBER(4) NOT NULL,
	group_id		NUMBER(6) NOT NULL,
	position		NUMBER(2),
	CONSTRAINT KbCntTemplateRsGroup_pkey PRIMARY KEY (template_id, group_id),
	CONSTRAINT KbtemplateCTRG_fk FOREIGN KEY (template_id) REFERENCES contenttemplate(id),
	CONSTRAINT KbresourcegroupCTRG_fk FOREIGN KEY (group_id) REFERENCES resourcegroup(id)
) TABLESPACE SBD02_DATA;

CREATE TABLE Content (
	id					NUMBER(19) NOT NULL,
	oid					NUMBER(19),
	lastModifiedDate	TIMESTAMP WITH TIME ZONE DEFAULT current_timestamp,
	creationDate		TIMESTAMP WITH TIME ZONE DEFAULT current_timestamp,
	lastModifiedUser	NUMBER(6) NOT NULL,
	state				NUMBER(2),
	domain_id			NUMBER(4),
	base				NUMBER(4),
	lang				CHAR(3),
	title				VARCHAR2(256),
	content_abstract	CLOB,
	name 				VARCHAR2(256),
	version				NUMBER(19),
	nextversion			NUMBER(19),
	prev_version		NUMBER(19) UNIQUE,
	ishead				NUMBER(1) DEFAULT 1,
	contenttemplate		NUMBER(4), 
	comments 			NUMBER(1) DEFAULT 1,
	locked				NUMBER(1),
	workspace			NUMBER(6),
	attributes			VARCHAR2(2048),
	qastate				NUMBER(1) default 0,
	qamsg				VARCHAR2(128),
	CONSTRAINT  contenttemplate_fk   FOREIGN KEY (contenttemplate)	REFERENCES contenttemplate(id),
	CONSTRAINT  content_pkey PRIMARY KEY (id),
	CONSTRAINT  userCont_fk FOREIGN KEY (lastModifiedUser) REFERENCES users(id),
	CONSTRAINT  workspaceCont_fk FOREIGN KEY (workspace) REFERENCES users(id),
	CONSTRAINT  domainCont_fk FOREIGN KEY (domain_id) REFERENCES domain(id),
	CONSTRAINT prev_versionCont_fk FOREIGN KEY (prev_version) REFERENCES content (id),
	CONSTRAINT  content_oiversion_unique  UNIQUE (oid, version)
) TABLESPACE SBD02_DATA;
CREATE INDEX content_lastModDate_idx ON Content (lastModifiedDate  DESC) TABLESPACE SBD02_INDEX;
CREATE INDEX content_name_idx ON Content (lower(name)) TABLESPACE SBD02_INDEX;
CREATE INDEX content_title_idx ON Content(lower(title)) TABLESPACE SBD02_INDEX; 

CREATE TABLE ContentRelation (
	source_id		NUMBER(19) not null, 
	target_id		NUMBER(19) not null, 
	position		NUMBER(2),
	CONSTRAINT relation_pkey PRIMARY KEY (source_id, target_id),
	CONSTRAINT sourceCR_id_fk FOREIGN KEY (source_id) REFERENCES content (id) ON DELETE CASCADE,
	CONSTRAINT targetCR_id_fk FOREIGN KEY (target_id) REFERENCES content (id) ON DELETE CASCADE
) TABLESPACE SBD02_DATA;

CREATE TABLE Property (
	id 				NUMBER(19) not null, 
	type			NUMBER(2),
	name			VARCHAR2(128),
	content_id		NUMBER(19) not null,
	value 			VARCHAR2(1024),
	CONSTRAINT contentproperty_pkey PRIMARY KEY (id),
	CONSTRAINT content_fk FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE CASCADE
) TABLESPACE SBD02_DATA;
CREATE INDEX property_content_id_idx ON property(content_id) TABLESPACE SBD02_INDEX;

CREATE TABLE OrganizationalText (
	content_id 			NUMBER(19) not null, 
	subtitle			VARCHAR2(256),
	contentdate			DATE,
	author_id			NUMBER(6),
	media	 			VARCHAR2(256),
	communitacion_class	VARCHAR2(6),
	text				CLOB,
	summary				CLOB,
	CONSTRAINT ot_pkey PRIMARY KEY (content_id),
	CONSTRAINT contentOT_fk FOREIGN KEY (content_id)  REFERENCES content(id)
) TABLESPACE SBD02_DATA;

CREATE TABLE Activity (
 	content_id 				NUMBER(19) not null, 
	subtitle				VARCHAR2(256),
	location				VARCHAR2(128),
	contentdate				DATE,
	fromdate				DATE,
	activity_class 			VARCHAR2(6),
	summary					CLOB,
	todate					date,
	fromhour				date,
	tohour					date,
	ktext					CLOB,
	CONSTRAINT activity_pkey PRIMARY KEY (content_id),
 	CONSTRAINT contentAct_fk FOREIGN KEY (content_id) REFERENCES content(id) 
) TABLESPACE SBD02_DATA;
CREATE INDEX activity_content_id_idx ON Activity (activity_class, content_id) TABLESPACE SBD02_INDEX;
 

 CREATE TABLE Tool (
	content_id		NUMBER(19) not null, 
	subtitle		VARCHAR2(256),
	url				VARCHAR2(256),
	tool_class		VARCHAR2(6),
	CONSTRAINT tool_pkey PRIMARY KEY (content_id),
	CONSTRAINT contentTool_fk FOREIGN KEY (content_id)  REFERENCES content(id) 
) TABLESPACE SBD02_DATA;
CREATE INDEX tool_class_content_id_idx ON Tool (tool_class, content_id) TABLESPACE SBD02_INDEX; 
 
 CREATE TABLE banner(
	content_id		NUMBER(19) not null, 
	bannertext		CLOB,
	link 			VARCHAR2(256),
	external		NUMBER(1),
	ga 				VARCHAR2(512),
	CONSTRAINT banner_pkey PRIMARY KEY (content_id),
	CONSTRAINT contentBan_fk FOREIGN KEY (content_id) REFERENCES content(id)
) TABLESPACE SBD02_DATA;

CREATE TABLE ContentResource (
	content_id 	NUMBER(19) not null,
	resource_id	NUMBER(19) not null, 
	position NUMBER(6),
	CONSTRAINT contentresource_pkey PRIMARY KEY (content_id, resource_id),
	CONSTRAINT contentCR_fk FOREIGN KEY (content_id) REFERENCES content(id),
	CONSTRAINT resourceCR_fk FOREIGN KEY (resource_id) REFERENCES kresource(id)
) TABLESPACE SBD02_DATA;

CREATE TABLE DataSet (
	id						NUMBER(6) NOT NULL,
	creationDate			TIMESTAMP WITH TIME ZONE DEFAULT current_timestamp,
	lastModifiedDate		TIMESTAMP WITH TIME ZONE DEFAULT current_timestamp,
	lastModifiedUser		NUMBER(6) NOT NULL,
	state					NUMBER(2),
	enabled					NUMBER(1) DEFAULT 1,
	domain_id				NUMBER(4),
	HIERARCHICAL			NUMBER(1) DEFAULT 0,
	SUGGESTER				NUMBER(1) DEFAULT 1,
	group_id				NUMBER(6),
	name 					VARCHAR2(256),
	alternative_display		VARCHAR2(256),
	description				VARCHAR2(4000),
	type 					NUMBER(2) default 1,
	CONSTRAINT dataset_pkey PRIMARY KEY (id),
	CONSTRAINT userDS_fk FOREIGN KEY (lastModifiedUser) REFERENCES users(id),
	CONSTRAINT domainDS_fk FOREIGN KEY (domain_id) REFERENCES domain(id) 
) TABLESPACE SBD02_DATA;
CREATE INDEX dataset_domain_id_idx ON dataset(domain_id, lower(name)) TABLESPACE SBD02_INDEX;


CREATE TABLE DatasetMember(
	id					NUMBER(19) not null,					 
	creationDate		TIMESTAMP WITH TIME ZONE DEFAULT current_timestamp,
	lastModifiedDate	TIMESTAMP WITH TIME ZONE DEFAULT current_timestamp,
	lastModifiedUser	NUMBER(6) NOT NULL,
	state				NUMBER(2),
	domain_id			NUMBER(4),
	entity_id			NUMBER(6),
	type				NUMBER(2) DEFAULT 1,
	alternative_display	VARCHAR2(256),
	strvalue			VARCHAR2(256),
	datevalue			date,
	parent				NUMBER(19),
	dataset_id			NUMBER(6) not null,
	external_id			NUMBER(6),
	attributes			varchar2(2048),
	CONSTRAINT datasetmember_pkey PRIMARY KEY (id),
	CONSTRAINT userDSM_fk FOREIGN KEY (lastModifiedUser) REFERENCES users(id),
	CONSTRAINT datasetDSM_fk FOREIGN KEY (dataset_id) REFERENCES dataset(id),
	CONSTRAINT entityDSM_fk FOREIGN KEY (entity_id) REFERENCES entity(id),
	CONSTRAINT parentDSM_fk FOREIGN KEY (parent) REFERENCES datasetmember(id),
	CONSTRAINT domainDSM_fk FOREIGN KEY (domain_id) REFERENCES domain(id) 
) TABLESPACE SBD02_DATA;
CREATE INDEX dsm_dataset_id_strvalue_idx ON datasetmember(dataset_id, lower(strvalue)) TABLESPACE SBD02_INDEX;
 
CREATE TABLE Kb_OrganizationalData(
	id					NUMBER(19) not null,					 
	person_id			NUMBER(19),
	group_id			NUMBER(19),
	securityrule_id		NUMBER(19),

	CONSTRAINT organizationaldata_pkey PRIMARY KEY (id),
	CONSTRAINT personOD_fk FOREIGN KEY (person_id) REFERENCES person(entity_id),
	CONSTRAINT groupOD_fk FOREIGN KEY (group_id) REFERENCES kgroup(id),
	CONSTRAINT ruleOD_fk FOREIGN KEY (securityrule_id) REFERENCES securityrule(id)
) TABLESPACE SBD02_DATA;
 
CREATE TABLE Classifier(
	id					NUMBER(6) NOT NULL,
	creationDate		TIMESTAMP WITH TIME ZONE DEFAULT current_timestamp,
	lastModifiedDate	TIMESTAMP WITH TIME ZONE DEFAULT current_timestamp,
	lastModifiedUser	NUMBER(6) NOT NULL,
	state				NUMBER(2),
	domain_id			NUMBER(4),
	base				NUMBER(4),
	iscanonical			NUMBER(1),
	is_content_type		NUMBER(1) default 0,
	displayable			NUMBER(1), 
	semantic			NUMBER(1) default 0,
	name				VARCHAR2(128),
	uniquename			VARCHAR2(128),
	predicate			VARCHAR2(128),
	multiplicity		NUMBER(4) default 1000,
	mandatory			NUMBER(1) DEFAULT 0,
	ordered				NUMBER(1) DEFAULT 0,
	visibility			CLOB,
	korder				NUMBER(4) default 1,
	dataset_id			NUMBER(6),
	dataset2_id			NUMBER(6),
	dataset3_id			NUMBER(6),
	CONSTRAINT classifier_pkey PRIMARY KEY (id),
	CONSTRAINT userClas_fk FOREIGN KEY (lastModifiedUser) REFERENCES users(id),
	CONSTRAINT datasetClas_fk FOREIGN KEY (dataset_id) REFERENCES dataset(id),
	CONSTRAINT dataset2Clas_fk FOREIGN KEY (dataset2_id) REFERENCES dataset(id),
	CONSTRAINT dataset3Clas_fk FOREIGN KEY (dataset3_id) REFERENCES dataset(id),
	CONSTRAINT domainClas_fk FOREIGN KEY (domain_id) REFERENCES domain(id) 
) TABLESPACE SBD02_DATA;
CREATE INDEX classifier_name_idx ON classifier(lower(name)) TABLESPACE SBD02_INDEX;

CREATE TABLE ClassifierContent(
	classifier_id		NUMBER(6) not null,					 
	contentclass_id		VARCHAR2(64),
	CONSTRAINT ClassifierContent_pkey PRIMARY KEY (classifier_id, contentclass_id),
	CONSTRAINT classifier_fk FOREIGN KEY (classifier_id) REFERENCES Classifier(id),
	CONSTRAINT contentclass_fk FOREIGN KEY (contentclass_id) REFERENCES ContentClass(id) 
) TABLESPACE SBD02_DATA;
CREATE INDEX classcont_class_classifier_idx ON ClassifierContent(contentclass_id, classifier_id) TABLESPACE SBD02_INDEX;

CREATE TABLE classification(
	id					NUMBER(19) not null, 
	state				NUMBER(2),
	datevalue			DATE,
	content_id 			NUMBER(19) not null,
	classifier_id		NUMBER(6) not null,
	datasetmember_id	NUMBER(19),
	position			NUMBER(3),
	CONSTRAINT contentclassif_pkey PRIMARY KEY (id),
	CONSTRAINT classifierCl_fk FOREIGN KEY (classifier_id) REFERENCES classifier(id),
	CONSTRAINT datasetmemberCl_fk FOREIGN KEY (datasetmember_id) REFERENCES datasetmember(id),
	CONSTRAINT contentCl_fk FOREIGN KEY (content_id) REFERENCES content(id)
) TABLESPACE SBD02_DATA;
CREATE INDEX classif_content_class_idx ON classification(content_id, classifier_id) TABLESPACE SBD02_INDEX;

CREATE TABLE KB_ClassifierTemplate(
	id					NUMBER(6) NOT NULL,
	contenttemplate_id	NUMBER(4) not null,
	classifier_id		NUMBER(6) not null,
	root_id				NUMBER(6),
	position			NUMBER(2),
	inherited  			NUMBER(1) default  0,
	CONSTRAINT classifiertemplate_pkey PRIMARY KEY (id),
	CONSTRAINT member_fk FOREIGN KEY (root_id) REFERENCES DataSetMember(id)
) TABLESPACE SBD02_DATA;

CREATE TABLE DataSetClassifier (
	dataset_id			NUMBER(6) not null,
	classifier_id		NUMBER(6) not null,
	CONSTRAINT datasetclassifier_pkey PRIMARY KEY (dataset_id, classifier_id),
	CONSTRAINT datasetDC_fk FOREIGN KEY (dataset_id) REFERENCES DataSet (id),
	CONSTRAINT classifierDC_fk FOREIGN KEY (classifier_id) REFERENCES Classifier(id)
) TABLESPACE SBD02_DATA;

CREATE TABLE MemberClassification(
	id					NUMBER(19) not null, 
	state				NUMBER(2),
	sourcemember_id		NUMBER(19) not null,
	classifier_id		NUMBER(6) not null,
	targetmember_id  	NUMBER(19) not null,
	position			NUMBER(3),
	CONSTRAINT memberclassification_pkey PRIMARY KEY (id),
	CONSTRAINT classifierMC_fk FOREIGN KEY (classifier_id) REFERENCES Classifier(id),
	CONSTRAINT targetmemberMC_fk FOREIGN KEY (targetmember_id) REFERENCES datasetmember(id),
	CONSTRAINT sourcememberMC_fk FOREIGN KEY (sourcemember_id) REFERENCES datasetmember(id)
) TABLESPACE SBD02_DATA;
  
CREATE TABLE Kb_AttributeTemplate(
	id					NUMBER(6) NOT NULL,
	name				VARCHAR2(128),
	type				NUMBER(3),
	multiplicity		NUMBER(4) default 1000,
	mandatory			NUMBER(1) DEFAULT 0,
	CONSTRAINT attributetemplate_pkey PRIMARY KEY (id)
) TABLESPACE SBD02_DATA;

CREATE TABLE idoc(
	content_id			NUMBER(19) not null,
	title				VARCHAR2(256),
	subtitle			VARCHAR2(256),
	summary				VARCHAR2(512),
	editorialstate		NUMBER(2),
	template_id			NUMBER(6),
	CONSTRAINT idoc_pkey PRIMARY KEY (content_id),
	CONSTRAINT contentIdoc_fk FOREIGN KEY (content_id) REFERENCES content(id)
) TABLESPACE SBD02_DATA;
CREATE INDEX idoc_title_idx ON idoc(lower(title)) TABLESPACE SBD02_INDEX;
CREATE INDEX idoc_editorialstate_idx ON idoc(editorialstate) TABLESPACE SBD02_INDEX;

CREATE TABLE IDOCSection(
	id					NUMBER(6) not null,					 
	creationDate		TIMESTAMP WITH TIME ZONE DEFAULT current_timestamp,
	lastModifiedDate	TIMESTAMP WITH TIME ZONE DEFAULT current_timestamp,
	lastModifiedUser	NUMBER(6) NOT NULL,
	state				NUMBER(2),
	idoc_id 			NUMBER(19) not null,					 
	sectionorder		NUMBER(4),
	name				VARCHAR2(256),
	description			VARCHAR2(256),
	attributejson		CLOB,
	CONSTRAINT idocsection_pkey PRIMARY KEY (id),
	CONSTRAINT unique_section_idoc unique (idoc_id, sectionorder),
	CONSTRAINT contentIdocS_fk FOREIGN KEY (idoc_id)  REFERENCES idoc(content_id) 
) TABLESPACE SBD02_DATA;

CREATE TABLE IDOCSectionResource(
	section_id 			NUMBER(19) not null,
	resource_id			NUMBER(19) not null,
	position			NUMBER(4),
	CONSTRAINT idocsectionresource_pkey PRIMARY KEY (section_id, resource_id),
	CONSTRAINT sectionISR_fk FOREIGN KEY (section_id) REFERENCES idocsection(id),
	CONSTRAINT resourceISR_fk FOREIGN KEY (resource_id) REFERENCES kresource(id)   
) TABLESPACE SBD02_DATA;

CREATE TABLE Notification (
	id					NUMBER(19)  not null,					 
	creationDate		TIMESTAMP WITH TIME ZONE DEFAULT current_timestamp,
	lastModifiedDate	TIMESTAMP WITH TIME ZONE DEFAULT current_timestamp,
	lastModifiedUser	NUMBER(6) NOT NULL,
	state				NUMBER(2),
	domain_id			NUMBER(4),
	title				VARCHAR2(256),
	text 				VARCHAR2(1024),
	content_id			NUMBER(19),
	contentid			VARCHAR(42),  
	sender_id			NUMBER(6) not null,
	receiver_id			NUMBER(6) not null,
	datesend			TIMESTAMP WITH TIME ZONE DEFAULT current_timestamp,
	dateread			DATE,
	type				NUMBER(2) DEFAULT 1,
	notification_state	NUMBER(2) DEFAULT 1,
	CONSTRAINT notification_pkey PRIMARY KEY (id),
	CONSTRAINT userNot_fk FOREIGN KEY (lastModifiedUser) REFERENCES users(id) ON DELETE SET NULL,  
	CONSTRAINT contentNot_fk FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE SET NULL,
	CONSTRAINT senderNot_fk FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE SET NULL,
	CONSTRAINT receiverNot_fk FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE CASCADE,
	CONSTRAINT domainNot_fk FOREIGN KEY (domain_id) REFERENCES domain(id)  ON DELETE CASCADE 
) TABLESPACE SBD02_DATA;
CREATE INDEX notif_receiver_state_lmd_idx ON notification (receiver_id, state, lastModifiedDate DESC) TABLESPACE SBD02_INDEX;

CREATE TABLE Drb_Question (
	content_id			NUMBER(19) not null, 
	title				VARCHAR2(256),
	text	 			CLOB,
	user_id				NUMBER(6) not null, 
	votes				NUMBER(4),
	num_answers			NUMBER(3) default 0,   
	state				NUMBER(2) default 0,   
	date_edited_admin	TIMESTAMP WITH TIME ZONE,
	date_submitted		TIMESTAMP WITH TIME ZONE DEFAULT current_timestamp,
	CONSTRAINT question_pkey PRIMARY KEY (content_id),
	CONSTRAINT contentQ_fk FOREIGN KEY (content_id) REFERENCES content(id),
	CONSTRAINT userQ_fk FOREIGN KEY (user_id) REFERENCES users(id)
) TABLESPACE SBD02_DATA;

CREATE TABLE Drb_Answer (
	content_id			NUMBER(19) not null, 
	question_id			NUMBER(19), 
	date_submitted		TIMESTAMP WITH TIME ZONE DEFAULT current_timestamp,
	date_edited_admin	TIMESTAMP WITH TIME ZONE,
	title				VARCHAR2(256),
	text				CLOB,
	user_id				NUMBER(6) not null, 
	accepted			NUMBER(1),
	date_accepted		date,
	votes				NUMBER(4),
	CONSTRAINT answer_pkey PRIMARY KEY (content_id),
	CONSTRAINT contentAns_fk FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE CASCADE,
	CONSTRAINT questionAns_fk FOREIGN KEY (question_id) REFERENCES drb_question(content_id) ON DELETE CASCADE
) TABLESPACE SBD02_DATA;
CREATE INDEX answer_question_votes_idx ON drb_Answer (question_id, votes) TABLESPACE SBD02_INDEX;

CREATE TABLE KComment (
	content_id				NUMBER(19) NOT NULL,
	referenced_content_id	NUMBER(19) NOT NULL,
	commentdate				date,
	title 					VARCHAR2(256),
	text 					CLOB,
	site_id 				number(19),
	user_id 				NUMBER(6) NOT NULL,
	date_submitted			timestamp with time zone DEFAULT sysdate,
	CONSTRAINT comment_pkey PRIMARY KEY (content_id),
	CONSTRAINT contentKCom_fk FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE CASCADE,
	CONSTRAINT referenced_content_id_fk FOREIGN KEY (referenced_content_id) REFERENCES content(id) ON DELETE CASCADE
) TABLESPACE SBD02_DATA;



CREATE INDEX site_date_KComment_idx ON KComment (site_id, date_submitted desc) TABLESPACE SBD02_INDEX;
CREATE INDEX content_date_KComment_idx ON KComment (referenced_content_id, date_submitted desc) TABLESPACE SBD02_INDEX;




CREATE TABLE Vote (
	user_id				NUMBER(6) not null, 
	content_id			NUMBER(19) not null, 
	vote				NUMBER(1),
	votedate			TIMESTAMP WITH TIME ZONE DEFAULT current_timestamp,
	CONSTRAINT vote_pkey PRIMARY KEY (user_id, content_id),
	CONSTRAINT content_id_fk FOREIGN KEY (content_id) REFERENCES  content(id) ON DELETE CASCADE,
 	CONSTRAINT user_id_fk FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) TABLESPACE SBD02_DATA;
CREATE INDEX vote_content_user_idx ON Vote (content_id, user_id) TABLESPACE SBD02_INDEX;

CREATE TABLE Report (
	user_id			NUMBER(6) NOT NULL,
	content_id		NUMBER(19) NOT NULL,
	report			NUMBER(2),
	reportdate		timestamp with time zone DEFAULT sysdate,
	CONSTRAINT report_pkey PRIMARY KEY (user_id, content_id),
	CONSTRAINT contentRep_id_fk FOREIGN KEY (content_id) REFERENCES content (id) ON DELETE CASCADE,
	CONSTRAINT userRep_id_fk FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) TABLESPACE SBD02_DATA;

CREATE TABLE ContentStat(
	content_id		NUMBER(19) not null, 
	views			NUMBER(2),
	shared			NUMBER(2),
	favorites		NUMBER(2),
	votes			NUMBER(2),
	CONSTRAINT contentstat_pkey PRIMARY KEY (content_id),
	CONSTRAINT contentStat_id_fk FOREIGN KEY (content_id) REFERENCES content(id) 
) TABLESPACE SBD02_DATA;
 

 CREATE TABLE ContentProperties (
	content_id			NUMBER(19) not null, 
	contentproperties	CLOB,
	lastModifiedDate	TIMESTAMP WITH TIME ZONE DEFAULT current_timestamp,
	lastModifiedUser	NUMBER(6) NOT NULL,
	CONSTRAINT ContentProperties_pkey PRIMARY KEY (content_id),
	CONSTRAINT contentProp_id_fk FOREIGN KEY (content_id) REFERENCES  content(id) ON DELETE CASCADE,
 	CONSTRAINT lastModifiedUserProp_fk 	FOREIGN KEY (lastModifiedUser) REFERENCES users(id) ON DELETE CASCADE
) TABLESPACE SBD02_DATA;

CREATE TABLE Gallery (
	title				VARCHAR2(128),
	subtitle			VARCHAR2(128),
	description			VARCHAR2(256),
	resource_id			NUMBER(19) not null,					 
	gdate				date,
	CONSTRAINT gallery_pkey PRIMARY KEY (resource_id),
	CONSTRAINT gallery_fk FOREIGN KEY (resource_id)  REFERENCES kresource(id)
) TABLESPACE SBD02_DATA;
CREATE INDEX gallery_title_idx ON Gallery (lower(title)) TABLESPACE SBD02_INDEX;

CREATE TABLE GalleryFile(
	gallery_id		NUMBER(19) not null,
	file_id			NUMBER(19) not null, 
	gorder			NUMBER(1),
	CONSTRAINT galleryfile_pkey PRIMARY KEY (gallery_id, file_id),
	CONSTRAINT galleryGF_fk FOREIGN KEY (gallery_id) REFERENCES gallery(resource_id),
	CONSTRAINT fileGF_fk FOREIGN KEY (file_id) REFERENCES kfile(resource_id)
) TABLESPACE SBD02_DATA;

CREATE TABLE OrgChart (
	content_id		NUMBER(19) not null, 
	name			VARCHAR2(256),
	description		VARCHAR2(256),
	mision			VARCHAR2(256),
	xmlchart		CLOB,
	CONSTRAINT OrgChart_pkey PRIMARY KEY (content_id),
	CONSTRAINT contentOrgC_fk FOREIGN KEY (content_id) REFERENCES content(id) 
) TABLESPACE SBD02_DATA;

CREATE TABLE kb_Subscription (
	user_id 				NUMBER(6) not null, 
	content_oid 			NUMBER(19) not null, 
	event_id				NUMBER(2) not null,
	subscription_date		TIMESTAMP WITH TIME ZONE DEFAULT current_timestamp,
	type_id					NUMBER(1),
	CONSTRAINT Subscription_pkey PRIMARY KEY (user_id, content_oid, event_id),
	CONSTRAINT Subscription_user_fk FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) TABLESPACE SBD02_DATA;
CREATE INDEX subs_content_event_idx ON kb_Subscription (content_oid, event_id, user_id) TABLESPACE SBD02_INDEX;


CREATE TABLE Wf_Procedure (
	id					NUMBER(19) NOT NULL,
	alias				VARCHAR2(64),
	lastModifiedDate	TIMESTAMP WITH TIME ZONE DEFAULT current_timestamp,
	creationDate		TIMESTAMP WITH TIME ZONE DEFAULT current_timestamp,
	lastModifiedUser	NUMBER(19) NOT NULL,
	domain_id			NUMBER(19) NOT NULL,
	state				NUMBER(6),
	name				VARCHAR2(128),
	tasks				CLOB,
	states				VARCHAR2(128),
	
	CONSTRAINT workflowprocedure_pkey PRIMARY KEY (id),
	CONSTRAINT wfprocedure_lastModifiedUser_fk 	FOREIGN KEY (lastModifiedUser) REFERENCES  users(id) ON DELETE CASCADE,		
	CONSTRAINT wfprocedure_domain_fk FOREIGN KEY (domain_id) REFERENCES domain (id) ON DELETE CASCADE
) TABLESPACE SBD02_DATA;

CREATE TABLE Wf_Process (
	id					NUMBER(19) NOT NULL,
	procedure_id		NUMBER(19) NOT NULL,
	startime			TIMESTAMP WITH TIME ZONE,
	endtime				TIMESTAMP WITH TIME ZONE,
	status				VARCHAR2(20),
	
	CONSTRAINT workflowprocess_pkey PRIMARY KEY (id),
	CONSTRAINT wfprocess_procedure_fk FOREIGN KEY (procedure_id) REFERENCES wf_procedure
) TABLESPACE SBD02_DATA;

CREATE TABLE Wf_Activity (
	id						NUMBER(19) NOT NULL,
	process_id				NUMBER(19) NOT NULL,
	task					VARCHAR2(128),
	user_id					NUMBER(19),
	assigned_by				NUMBER(19),
	content_id 				NUMBER(19) not null,
	startime				TIMESTAMP WITH TIME ZONE,
	endtime					TIMESTAMP WITH TIME ZONE,
	event					VARCHAR2(128),
	note					CLOB,
	resolution				CLOB,
	status					VARCHAR2(20),
	CONSTRAINT wf_content_fk FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE CASCADE,
	CONSTRAINT wfactivity_process_fk FOREIGN KEY (process_id) REFERENCES wf_process(id),		
	CONSTRAINT wfactivity_user_fk FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
	CONSTRAINT wfactivity_assigned_fk FOREIGN KEY (user_id) REFERENCES users(id)
)TABLESPACE SBD02_DATA;

CREATE TABLE Wf_Launcher (
	id	 					NUMBER(19)   NOT  NULL,
	domain_id				NUMBER(19) NOT  NULL,
	label					VARCHAR2(128),
	contenttemplate_id	 	NUMBER(19),
	procedure_id			NUMBER(19),
	acl						NUMBER(19)
	contextual				NUMBER(1),
		
	CONSTRAINT wf_launcher_pkey PRIMARY KEY (id),
	CONSTRAINT wflauncher_contenttemplate_fk FOREIGN KEY (contenttemplate_id) REFERENCES kb_contenttemplate ON DELETE CASCADE,		
	CONSTRAINT wflauncher_procedure_fk FOREIGN KEY (procedure_id) REFERENCES wf_procedure,
	CONSTRAINT wflauncher_fk FOREIGN KEY (acl) REFERENCES acl,
	CONSTRAINT wflauncher_domain_fk FOREIGN KEY (domain_id) REFERENCES domain (id) ON DELETE CASCADE
)TABLESPACE SBD02_DATA;

CREATE TABLE entitymatching (
	kbee_id				VARCHAR2(36) NOT NULL,
	kbee_class_name		VARCHAR2(150),
	lastModifiedDate	timestamp with time zone DEFAULT sysdate,
	class_name			VARCHAR2(150),
	id					VARCHAR2(36) NOT NULL,
	CONSTRAINT ENTITYMATCHING_PKEY PRIMARY KEY (kbee_id)
) TABLESPACE SBD02_DATA;



CREATE TABLE kb_domain_settings
(
  domain_id  NUMBER(19) NOT NULL,
  category   VARCHAR2(64),
  values_json CLOB,

  CONSTRAINT ds_pkey PRIMARY KEY (domain_id , category),
  CONSTRAINT ds_domain_fk FOREIGN KEY (domain_id) REFERENCES domain (id) ON DELETE CASCADE

) TABLESPACE SBD02_DATA;


INSERT INTO ContentClass(id, enabled, name, javaclass) VALUES('KbeeContent',            1, 'Content', 'com.novamens.kbee.content.base.KbeeContent');
INSERT INTO ContentClass(id, enabled, name, javaclass) VALUES('KbeeIDoc',               1, 'IDoc', 'com.novamens.kbee.content.document.KbeeIDoc');
INSERT INTO ContentClass(id, enabled, name, javaclass) VALUES('KbeeOrganizationalText', 1, 'OrganizationalText', 'com.novamens.kbee.content.communication.KbeeOrganizationalText');
INSERT INTO ContentClass(id, enabled, name, javaclass) VALUES('KbeeOrgChart',           1, 'OrgChart', 'com.novamens.kbee.content.orgchart.KbeeOrgChart');
INSERT INTO ContentClass(id, enabled, name, javaclass) VALUES('KbeeQuestion',           1, 'Question', 'com.novamens.kbee.content.questionanswer.KbeeQuestion');
INSERT INTO ContentClass(id, enabled, name, javaclass) VALUES('KbeeAnswer',             1, 'Answer', 'com.novamens.kbee.content.questionanswer.KbeeAnswer');
INSERT INTO ContentClass(id, enabled, name, javaclass) VALUES('KbeeComment',            1, 'Comment', 'com.novamens.kbee.content.social.KbeeComment');

create or replace PROCEDURE createDomain(id_dominio NUMBER, id_user NUMBER, id_group NUMBER, id_profile NUMBER, id_person NUMBER, id_template NUMBER, nombreDominio VARCHAR2, mail VARCHAR2) AS 
BEGIN
	INSERT INTO users(id, username, state,  firstname, lastname, password, password_md5, lastModifiedUser) VALUES(id_user, 'root@'||nombreDominio, 1, '' , 'root', '3b6144f35f3e2f80a1f9446fafc389dd', 'root', null);
    INSERT INTO domain(id, lastmodifieduser, state, enabled, name) VALUES(id_dominio, id_user, 1, 1, nombreDominio);
    INSERT INTO kb_domain_settings(domain_id, category) VALUES(id_dominio, nombreDominio);
    INSERT INTO principal(id, lastmodifieduser, domain_id) VALUES(id_user, id_user, id_dominio);
   
    INSERT INTO principal(id, lastmodifieduser, domain_id) VALUES(id_group, id_user, id_dominio);
    INSERT INTO kgroup(id, name, canonical) VALUES(id_group, 'ROLE_USER', 0);
    INSERT INTO kgroupmember(kgroup, principal) VALUES(id_group, id_user);
    INSERT INTO entity(id, lastmodifieduser, state, domain_id) VALUES(id_person, id_user, 1, id_dominio);
    INSERT INTO person(entity_id, email) VALUES(id_person, mail);
    INSERT INTO profile(id, lastmodifieduser, entity, domain_id) VALUES(id_profile, id_user, id_person, id_dominio);
    INSERT INTO userprofile(id, user_id, confidencelevel) VALUES(id_profile, id_user, 90);
    
    INSERT INTO ContentTemplate(id, lastmodifieduser, state, domain_id, contentclass_id, name) VALUES(id_template, id_user, 1, id_dominio, 'KbeeIDoc', 'IDoc');
END;
