SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

CREATE SCHEMA kbee;
SELECT pg_catalog.set_config('search_path', 'kbee,public', false);

CREATE EXTENSION IF NOT EXISTS file_fdw;
COMMENT ON EXTENSION file_fdw IS 'foreign-data wrapper for flat file access';
CREATE EXTENSION IF NOT EXISTS pg_stat_statements;
COMMENT ON EXTENSION pg_stat_statements IS 'track execution statistics of all SQL statements executed';
CREATE FUNCTION createdomain(id_dominio bigint, id_user bigint, id_group bigint, id_profile bigint, id_person bigint, id_template bigint, nombredominio character varying, mail character varying) RETURNS void
    LANGUAGE plpgsql
    AS $$

BEGIN
    INSERT INTO users(id, username, state,  firstname, lastname, password, password_md5, lastModifiedUser) VALUES(id_user, 'root@'||nombreDominio, 1, '' , 'root', '3b6144f35f3e2f80a1f9446fafc389dd', 'root', null);
    INSERT INTO domain(id, lastmodifieduser, state, enabled, name) VALUES(id_dominio, id_user, 1, TRUE, nombreDominio);
    INSERT INTO principal(id, lastmodifieduser, domain_id) VALUES(id_user, id_user, id_dominio);

    INSERT INTO principal(id, lastmodifieduser, domain_id) VALUES(id_group, id_user, id_dominio);
    INSERT INTO kgroup(id, name, canonical) VALUES(id_group, 'ROLE_USER', FALSE);
	
    INSERT INTO kgroupmember(kgroup, principal) VALUES(id_group, id_user);
    INSERT INTO entity(id, lastmodifieduser, state, domain_id) VALUES(id_person, id_user, 1, id_dominio);
    INSERT INTO person(entity_id, email) VALUES(id_person, mail);
    INSERT INTO profile(id, lastmodifieduser, entity, domain_id) VALUES(id_profile, id_user, id_person, id_dominio);
    INSERT INTO userprofile(id, user_id, confidencelevel) VALUES(id_profile, id_user, 90);
    
    INSERT INTO ContentTemplate(id, lastmodifieduser, state, domain_id, contentclass_id, name) VALUES(id_template, id_user, 1, id_dominio, 'KbeeIDoc', 'IDoc');
   
    --INSERT INTO ContentTemplate(id, lastmodifieduser, state, domain_id, contentclass_id, name) VALUES(2, id_user, 1, id_dominio, 'KbeeBanner', 'Banner');
    --INSERT INTO ContentTemplate(id, lastmodifieduser, state, domain_id, contentclass_id, name) VALUES(3, id_user, 1, id_dominio, 'KbeeQuestion', 'Question');
    --INSERT INTO ContentTemplate(id, lastmodifieduser, state, domain_id, contentclass_id, name) VALUES(4, id_user, 1, id_dominio, 'KbeeComment', 'Comment');
    --INSERT INTO ContentTemplate(id, lastmodifieduser, state, domain_id, contentclass_id, name) VALUES(5, id_user, 1, id_dominio, 'KbeeAnswer', 'Answer');
    --INSERT INTO ContentTemplate(id, lastmodifieduser, state, domain_id, contentclass_id, name) VALUES(6, id_user, 1, id_dominio, 'KbeeOrganizationalText', 'OrganizationalText');
END;
 $$;
ALTER FUNCTION createdomain(id_dominio bigint, id_user bigint, id_group bigint, id_profile bigint, id_person bigint, id_template bigint, nombredominio character varying, mail character varying) OWNER TO postgres;
CREATE FUNCTION last_agg(anyelement, anyelement) RETURNS anyelement
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
SELECT $2;
$_$;
ALTER FUNCTION last_agg(anyelement, anyelement) OWNER TO postgres;
CREATE AGGREGATE last(anyelement) (
    SFUNC = last_agg,
    STYPE = anyelement
);
ALTER AGGREGATE last(anyelement) OWNER TO postgres;
CREATE SERVER fileserver FOREIGN DATA WRAPPER file_fdw;
ALTER SERVER fileserver OWNER TO postgres;
CREATE SEQUENCE aclentry_sequence
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
ALTER TABLE aclentry_sequence OWNER TO postgres;
SET default_tablespace = '';
SET default_with_oids = false;
CREATE TABLE api_logevent (
    event_id bigint NOT NULL,
    event_domain character varying(128) NOT NULL,
    event_file character varying(128),
    event_time timestamp with time zone DEFAULT now(),
    event_user character varying(128),
    event_transaction bigint,
    event_uri character varying(256),
    event_method character varying(15),
    event_request text,
    event_status integer,
    event_response text,
    event_processing_time bigint,
    event_retry bigint,
    event_retrynumber integer,
    event_source character varying(32),
    event_contentclass character varying(64),
    event_closed boolean DEFAULT false,
    event_filesource character varying(128)
);
ALTER TABLE api_logevent OWNER TO postgres;
CREATE SEQUENCE api_sequence
    START WITH 10000
    INCREMENT BY 10
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
ALTER TABLE api_sequence OWNER TO postgres;
CREATE TABLE api_soapevent (
    event_id bigint NOT NULL,
    event_domain character varying(128) NOT NULL,
    event_file character varying(128),
    event_time timestamp with time zone DEFAULT now(),
    event_user character varying(128),
    event_transaction bigint,
    event_uri character varying(256),
    event_method character varying(15),
    event_request text,
    event_status integer,
    event_response text,
    event_processing_time bigint,
    event_source character varying(32),
    event_retry bigint,
    event_retrynumber integer,
    event_closed boolean DEFAULT false,
    event_contentclass character varying(64),
    event_filesource character varying(128)
);
ALTER TABLE api_soapevent OWNER TO postgres;
CREATE TABLE authorities (
    username character varying(50) NOT NULL,
    authority character varying(50) NOT NULL
);
ALTER TABLE authorities OWNER TO kbee;
CREATE TABLE classification (
    id bigint NOT NULL,
    state integer,
    datevalue timestamp with time zone,
    content_id bigint NOT NULL,
    classifier_id bigint NOT NULL,
    datasetmember_id bigint,
    "position" integer DEFAULT 0
);
ALTER TABLE classification OWNER TO kbee;
CREATE SEQUENCE classificationid_sequence
    START WITH 10000
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
ALTER TABLE classificationid_sequence OWNER TO kbee;
CREATE TABLE classifiercontent (
    classifier_id bigint NOT NULL,
    contentclass_id character varying(64) NOT NULL
);
ALTER TABLE classifiercontent OWNER TO kbee;
CREATE TABLE content (
    id bigint NOT NULL,
    oid bigint,
    lastmodifieddate timestamp with time zone DEFAULT now(),
    creationdate timestamp with time zone DEFAULT now(),
    lastmodifieduser bigint NOT NULL,
    state integer,
    domain_id bigint,
    base bigint,
    lang character(3),
    title character varying(512),
    content_abstract character varying(8192),
    name character varying(256),
    version integer,
    nextversion integer,
    prev_version bigint,
    ishead boolean DEFAULT true,
    contenttemplate bigint,
    comments boolean DEFAULT true,
    locked boolean,
    workspace bigint,
    qastate integer DEFAULT 0,
    qamsg character varying(128),
    attributes character varying(2048),
    user_defined_properties text,
    external_id character varying(128),
    private_notes character varying(8192),
    checkindate timestamp with time zone,
    external_time timestamp with time zone,
    source_id bigint,
    acl bigint
);
ALTER TABLE content OWNER TO kbee;
CREATE TABLE contentclass (
    id character varying(64) NOT NULL,
    enabled boolean DEFAULT true,
    name character varying(128),
    javaclass character varying(128),
    indexable boolean DEFAULT true,
    selectable boolean DEFAULT true
);
ALTER TABLE contentclass OWNER TO kbee;
CREATE SEQUENCE contentid_sequence
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 30;
ALTER TABLE contentid_sequence OWNER TO kbee;
CREATE TABLE contentproperties (
    content_id bigint NOT NULL,
    contentproperties bytea,
    lastmodifieddate timestamp with time zone DEFAULT now(),
    lastmodifieduser bigint NOT NULL
);
ALTER TABLE contentproperties OWNER TO kbee;
CREATE TABLE contentresource (
    content_id bigint NOT NULL,
    resource_id bigint NOT NULL,
    "position" integer,
    id bigint NOT NULL,
    ispublic boolean DEFAULT true,
    group_id bigint
);
ALTER TABLE contentresource OWNER TO kbee;
CREATE SEQUENCE contentresourceid_sequence
    START WITH 10000
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
ALTER TABLE contentresourceid_sequence OWNER TO kbee;
CREATE TABLE contentstat (
    content_id bigint NOT NULL,
    views integer,
    shared integer,
    favorites integer,
    votes integer
);
ALTER TABLE contentstat OWNER TO kbee;
CREATE TABLE databasechangelog (
    id character varying(255) NOT NULL,
    author character varying(255) NOT NULL,
    filename character varying(255) NOT NULL,
    dateexecuted timestamp without time zone NOT NULL,
    orderexecuted integer NOT NULL,
    exectype character varying(10) NOT NULL,
    md5sum character varying(35),
    description character varying(255),
    comments character varying(255),
    tag character varying(255),
    liquibase character varying(20),
    contexts character varying(255),
    labels character varying(255),
    deployment_id character varying(10)
);
ALTER TABLE databasechangelog OWNER TO postgres;
CREATE TABLE databasechangeloglock (
    id integer NOT NULL,
    locked boolean NOT NULL,
    lockgranted timestamp without time zone,
    lockedby character varying(255)
);
ALTER TABLE databasechangeloglock OWNER TO postgres;
CREATE TABLE dataset (
    id bigint NOT NULL,
    creationdate timestamp with time zone DEFAULT now(),
    lastmodifieddate timestamp with time zone DEFAULT now(),
    lastmodifieduser bigint NOT NULL,
    state integer,
    enabled boolean DEFAULT true,
    domain_id bigint,
    description character varying(4096),
    name character varying(256),
    alternative_display character varying(256),
    type integer DEFAULT 1,
    hierarchical boolean DEFAULT false,
    suggester boolean DEFAULT true,
    group_id bigint,
    canonical boolean DEFAULT false,
    secured boolean DEFAULT false,
    external_subtype integer DEFAULT 0,
    readonly boolean DEFAULT false,
    external_id character varying(128),
    alias character varying(256),
    aggregation boolean DEFAULT false,
    classifier_id bigint,
    access_strategy integer DEFAULT 2,
    onlyroot boolean DEFAULT false
);
ALTER TABLE dataset OWNER TO kbee;
CREATE TABLE datasetclassifier (
    dataset_id bigint NOT NULL,
    classifier_id bigint NOT NULL
);
ALTER TABLE datasetclassifier OWNER TO kbee;
CREATE TABLE datasetmember (
    id bigint NOT NULL,
    creationdate timestamp with time zone DEFAULT now(),
    lastmodifieddate timestamp with time zone DEFAULT now(),
    lastmodifieduser bigint NOT NULL,
    state integer,
    domain_id bigint,
    entity_id bigint,
    type integer DEFAULT 1,
    alternative_display character varying(256),
    strvalue character varying(256),
    datevalue timestamp with time zone,
    parent bigint,
    dataset_id bigint NOT NULL,
    external_id character varying(128),
    external_url character varying(1024),
    attributes character varying(2048),
    rule_id bigint,
    group_id bigint,
    notes text,
    labelcolor integer DEFAULT 1,
    external_member_id bigint,
    securityrule_id bigint
);
ALTER TABLE datasetmember OWNER TO kbee;
CREATE TABLE domain (
    id bigint NOT NULL,
    lastmodifieddate timestamp with time zone DEFAULT now(),
    lastmodifieduser bigint NOT NULL,
    state integer,
    enabled boolean DEFAULT true,
    email character varying(128),
    address character varying(256),
    phone character varying(128),
    website character varying(128),
    name character varying(128),
    creationdate timestamp with time zone DEFAULT now(),
    organization character varying(512),
    type integer DEFAULT 1,
    service integer DEFAULT 1,
    description text,
    quota integer DEFAULT 0,
    file_reader_directory character varying(4096),
    password_renew_months integer DEFAULT 0,
    istemplate boolean DEFAULT false,
    maxusers integer DEFAULT 0,
    tipoftheday boolean DEFAULT true,
    lang character(6) DEFAULT 'en'::bpchar,
    cabinet_template boolean DEFAULT true,
    cabinet_kbase boolean DEFAULT false,
    cabinet_external boolean DEFAULT false,
    logo_url character varying(256),
    isapienabled boolean DEFAULT true,
    timezone character varying(256) DEFAULT 'US/Central'::character varying,
    storagemode integer DEFAULT 1,
    external_id character varying(128),
    portal_library boolean DEFAULT false,
    locale_str character(6) DEFAULT 'en'::bpchar,
    logo bigint,
    encrypt_files boolean DEFAULT false NOT NULL,
    defaultpassword character varying(256)
);
ALTER TABLE domain OWNER TO kbee;
CREATE SEQUENCE domainid_sequence
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
ALTER TABLE domainid_sequence OWNER TO kbee;
CREATE TABLE drb_answer (
    content_id bigint NOT NULL,
    question_id bigint,
    date_submitted timestamp with time zone DEFAULT now(),
    date_edited_admin timestamp with time zone,
    title character varying(512),
    text text,
    user_id bigint NOT NULL,
    accepted boolean,
    date_accepted timestamp with time zone,
    votes integer
);
ALTER TABLE drb_answer OWNER TO kbee;
CREATE TABLE drb_question (
    content_id bigint NOT NULL,
    title character varying(512),
    text text,
    user_id bigint NOT NULL,
    votes integer,
    num_answers integer DEFAULT 0,
    state integer DEFAULT 0,
    date_edited_admin timestamp with time zone,
    date_submitted timestamp with time zone DEFAULT now()
);
ALTER TABLE drb_question OWNER TO kbee;
CREATE TABLE entity (
    id bigint NOT NULL,
    creationdate timestamp with time zone DEFAULT now(),
    lastmodifieddate timestamp with time zone DEFAULT now(),
    lastmodifieduser bigint NOT NULL,
    state integer,
    domain_id bigint
);
ALTER TABLE entity OWNER TO kbee;
CREATE SEQUENCE entityid_sequence
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
ALTER TABLE entityid_sequence OWNER TO kbee;
CREATE TABLE entitymatching (
    kbee_id character varying(36) NOT NULL,
    kbee_class_name character varying(150),
    lastmodifieddate timestamp with time zone DEFAULT now(),
    class_name character varying(150),
    id character varying(36) NOT NULL,
    url character varying(36) NOT NULL
);
ALTER TABLE entitymatching OWNER TO kbee;
CREATE TABLE externalresource (
    resource_id bigint NOT NULL,
    url character varying(2048),
    description character varying(1024),
    in_portal boolean DEFAULT true
);
ALTER TABLE externalresource OWNER TO kbee;
CREATE TABLE gallery (
    title character varying(512),
    subtitle character varying(256),
    description character varying(1024),
    resource_id bigint NOT NULL,
    gdate date,
    in_portal boolean DEFAULT true
);
ALTER TABLE gallery OWNER TO kbee;
CREATE TABLE galleryfile (
    gallery_id bigint NOT NULL,
    file_id bigint NOT NULL,
    gorder integer
);
ALTER TABLE galleryfile OWNER TO kbee;
CREATE SEQUENCE hibernate_sequence
    START WITH 10000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;
ALTER TABLE hibernate_sequence OWNER TO kbee;
CREATE TABLE idoc (
    content_id bigint NOT NULL,
    title character varying(256),
    subtitle character varying(256),
    summary character varying(1024),
    editorialstate integer,
    template_id bigint,
    tree_file_id bigint
);
ALTER TABLE idoc OWNER TO kbee;
CREATE TABLE idocsection (
    id bigint NOT NULL,
    creationdate timestamp with time zone DEFAULT now(),
    lastmodifieddate timestamp with time zone DEFAULT now(),
    lastmodifieduser bigint NOT NULL,
    state integer,
    idoc_id bigint NOT NULL,
    sectionorder integer,
    name character varying(256),
    description character varying(1024),
    attributejson text
);
ALTER TABLE idocsection OWNER TO kbee;
CREATE TABLE idocsectionresource (
    section_id bigint NOT NULL,
    resource_id bigint NOT NULL,
    "position" integer
);
ALTER TABLE idocsectionresource OWNER TO kbee;
CREATE TABLE kb_acl (
    id bigint NOT NULL,
    lastmodifieddate timestamp with time zone DEFAULT now(),
    lastmodifieduser bigint,
    name character varying(120),
    creationdate timestamp with time zone DEFAULT now()
);
ALTER TABLE kb_acl OWNER TO kbee;
CREATE TABLE kb_aclentry (
    acl bigint NOT NULL,
    principal bigint NOT NULL,
    permissions character(2048) NOT NULL,
    negative boolean NOT NULL,
    id bigint
);
ALTER TABLE kb_aclentry OWNER TO kbee;
CREATE TABLE kb_action_rule (
    id bigint NOT NULL,
    creationdate timestamp with time zone DEFAULT now(),
    domain_id bigint,
    name character varying(128),
    display_name character varying(256),
    condition text,
    action text,
    description text,
    notes text,
    lastmodifieddate timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    lastmodifieduser bigint,
    state integer,
    displaycondition text
);
ALTER TABLE kb_action_rule OWNER TO postgres;
CREATE TABLE kb_api_usage_stat (
    ts timestamp with time zone DEFAULT now() NOT NULL,
    total bigint,
    mean_time_total double precision,
    total_post bigint,
    mean_time_post double precision,
    totdel bigint,
    meantimedel double precision,
    total_bounced bigint
);
ALTER TABLE kb_api_usage_stat OWNER TO postgres;
CREATE TABLE kb_assignable_role (
    role_id bigint NOT NULL,
    assignablerole_id bigint NOT NULL
);
ALTER TABLE kb_assignable_role OWNER TO postgres;
CREATE TABLE kb_attribute (
    id bigint NOT NULL,
    creationdate timestamp with time zone DEFAULT now(),
    lastmodifieddate timestamp with time zone DEFAULT now(),
    lastmodifieduser bigint NOT NULL,
    state integer,
    domain_id bigint,
    name character varying(128),
    type integer,
    multiplicity integer,
    uniquename character varying(128),
    korder integer,
    iscanonical boolean DEFAULT false,
    metadatasubtitle boolean DEFAULT false,
    visibility text,
    is_api boolean DEFAULT false,
    isfilterable boolean DEFAULT false,
    predicate character varying(128),
    alias character varying(128),
    inportal boolean DEFAULT true,
    portalsubtitle boolean DEFAULT false,
    description text,
    searchable boolean DEFAULT false,
    sortable boolean DEFAULT true,
    is_rule_condition boolean DEFAULT false,
    default_structure boolean DEFAULT false,
    validator text,
    onlyroot boolean DEFAULT false
);
ALTER TABLE kb_attribute OWNER TO postgres;
CREATE TABLE kb_attributetemplate (
    id bigint NOT NULL,
    metadatasubtitle boolean DEFAULT false,
    attribute_id bigint,
    portalsubtitle boolean DEFAULT false,
    korder integer DEFAULT 0,
    multiplicity integer DEFAULT 1,
    section_id bigint,
    subsection character varying(128),
    isvisible boolean DEFAULT true,
    parent_id bigint
);
ALTER TABLE kb_attributetemplate OWNER TO kbee;
CREATE TABLE kb_cabinet (
    id bigint NOT NULL,
    domain_id bigint NOT NULL,
    lastmodifieddate timestamp with time zone DEFAULT now(),
    creationdate timestamp with time zone DEFAULT now(),
    lastmodifieduser bigint NOT NULL,
    display_name character varying(256),
    criteria character varying(256),
    state integer,
    readonly boolean,
    reader_group bigint,
    key character varying(24),
    listorder integer,
    canonical boolean DEFAULT false,
    name character varying(256),
    description text
);
ALTER TABLE kb_cabinet OWNER TO postgres;
CREATE TABLE kb_cabinet_reader (
    cabinet_id bigint NOT NULL,
    group_id bigint NOT NULL
);
ALTER TABLE kb_cabinet_reader OWNER TO postgres;
CREATE TABLE kb_classifier (
    id bigint NOT NULL,
    creationdate timestamp with time zone DEFAULT now(),
    lastmodifieddate timestamp with time zone DEFAULT now(),
    lastmodifieduser bigint NOT NULL,
    state integer,
    domain_id bigint,
    base bigint,
    iscanonical boolean,
    displayable boolean,
    semantic boolean DEFAULT false,
    name character varying(128),
    uniquename character varying(128),
    predicate character varying(128),
    multiplicity integer DEFAULT 1000,
    is_content_type boolean DEFAULT false,
    mandatory boolean DEFAULT false,
    ordered boolean DEFAULT false,
    korder integer DEFAULT 1,
    dataset_id bigint,
    dataset2_id bigint,
    dataset3_id bigint,
    visibility text,
    metadatasubtitle boolean DEFAULT false,
    is_rule_condition boolean DEFAULT true,
    is_api boolean DEFAULT false,
    alias character varying(64),
    key character varying(128),
    inportal boolean DEFAULT true,
    portalsubtitle boolean DEFAULT false,
    description text,
    searchable boolean DEFAULT false,
    default_structure boolean DEFAULT false,
    access_strategy integer DEFAULT 0,
    onlyroot boolean DEFAULT false
);
ALTER TABLE kb_classifier OWNER TO kbee;
CREATE TABLE kb_classifiertemplate (
    id bigint NOT NULL,
    contenttemplate_id bigint NOT NULL,
    classifier_id bigint NOT NULL,
    root_id bigint,
    "position" integer DEFAULT 0,
    isvisible boolean DEFAULT true,
    inherited boolean DEFAULT false,
    iscanonical boolean DEFAULT true,
    metadatasubtitle boolean DEFAULT false,
    multiplicity integer DEFAULT 4,
    portalsubtitle boolean DEFAULT false,
    korder integer DEFAULT 0,
    section_id bigint,
    subsection character varying(128),
    parent_id bigint,
    accessibility integer DEFAULT 1,
    criteria character varying(256),
    reverseof_id bigint,
    reverse boolean DEFAULT false
);
ALTER TABLE kb_classifiertemplate OWNER TO kbee;
CREATE TABLE kb_comment (
    content_id bigint NOT NULL,
    referenced_content_id bigint NOT NULL,
    commentdate date,
    title character varying(256),
    text text,
    user_id bigint NOT NULL,
    date_submitted timestamp with time zone DEFAULT now(),
    site_id bigint,
    parent_comment bigint,
    isfirstlevel boolean DEFAULT true,
    creationdate timestamp with time zone DEFAULT now()
);
ALTER TABLE kb_comment OWNER TO postgres;
CREATE TABLE kb_content_relation (
    id bigint NOT NULL,
    source_id bigint NOT NULL,
    target_id bigint NOT NULL,
    template_id bigint NOT NULL,
    "position" integer
);
ALTER TABLE kb_content_relation OWNER TO postgres;
CREATE TABLE kb_content_rsbycriteria (
    id bigint NOT NULL,
    template_id bigint NOT NULL,
    source_id bigint NOT NULL,
    condition text
);
ALTER TABLE kb_content_rsbycriteria OWNER TO postgres;
CREATE TABLE kb_contentattribute (
    contenttemplate_id bigint NOT NULL,
    attributetemplate_id bigint NOT NULL,
    "position" integer
);
ALTER TABLE kb_contentattribute OWNER TO postgres;
CREATE TABLE kb_contentresourcegroup (
    template_id bigint NOT NULL,
    group_id bigint NOT NULL,
    "position" integer
);
ALTER TABLE kb_contentresourcegroup OWNER TO kbee;
CREATE TABLE kb_contenttemplate (
    id bigint NOT NULL,
    creationdate timestamp with time zone DEFAULT now(),
    lastmodifieddate timestamp with time zone DEFAULT now(),
    lastmodifieduser bigint NOT NULL,
    state integer,
    orden integer DEFAULT 1,
    domain_id bigint,
    contentclass_id character varying(64) NOT NULL,
    name character varying(128),
    instantiable boolean DEFAULT true,
    ismultimedia boolean DEFAULT false,
    relations boolean DEFAULT false,
    abstract boolean DEFAULT true,
    acl bigint,
    istemplate boolean DEFAULT false,
    hasdetailpage boolean DEFAULT true,
    isvideo boolean DEFAULT false,
    title_rule character varying(256),
    isdefault boolean DEFAULT false,
    isaudio boolean DEFAULT false,
    istext boolean DEFAULT false,
    isdocument boolean DEFAULT false,
    isphoto boolean DEFAULT false,
    istool boolean DEFAULT false,
    isactivity boolean DEFAULT false,
    linkresources boolean DEFAULT true,
    isadd boolean DEFAULT false,
    iscustomattributes boolean DEFAULT false,
    iskbase boolean DEFAULT false,
    private_notes boolean DEFAULT false,
    abstract_label character varying(128),
    private_notes_label character varying(128),
    text_notes_label character varying(128),
    text_label character varying(128),
    customattributes_label character varying(128),
    is_api boolean DEFAULT false,
    contentclasscode character varying(64),
    istreefile boolean DEFAULT false,
    treefile_label character varying(256),
    isresources boolean DEFAULT true,
    resources_label character varying(256) DEFAULT 'Resources'::character varying,
    isexternal boolean DEFAULT false,
    includesrelationshipsbycriteria boolean DEFAULT false,
    acceptsrelationshipsbycriteria boolean DEFAULT false,
    increlationshipsbycriteria boolean DEFAULT false,
    iscompliance boolean DEFAULT false,
    treefileresource boolean DEFAULT false,
    consolesubtitlerule character varying(2048),
    portalssubtitlerule character varying(2048),
    description text,
    alias character varying(64),
    onlyroot boolean DEFAULT false
);
ALTER TABLE kb_contenttemplate OWNER TO kbee;
CREATE TABLE kb_cronjob (
    id bigint NOT NULL,
    lastmodifieddate timestamp with time zone DEFAULT now(),
    lastmodifieduser bigint NOT NULL,
    name character varying(256),
    description character varying(2048),
    cronexpression character varying(256),
    clazz character varying(1024),
    parameter text,
    isenabled boolean DEFAULT true,
    lastexecution timestamp with time zone,
    execoldtriggers boolean DEFAULT false,
    domain bigint DEFAULT 1,
    timezone character varying(256) DEFAULT 'US/Central'::character varying
);
ALTER TABLE kb_cronjob OWNER TO postgres;
CREATE TABLE kb_datasetattribute (
    dataset_id bigint NOT NULL,
    attributetemplate_id bigint NOT NULL,
    "position" integer
);
ALTER TABLE kb_datasetattribute OWNER TO postgres;
CREATE TABLE kb_domain_settings (
    domain_id bigint NOT NULL,
    category character varying(64) NOT NULL,
    values_json text,
    lastmodifieddate timestamp with time zone DEFAULT now(),
    id bigint NOT NULL
);
ALTER TABLE kb_domain_settings OWNER TO kbee;
CREATE TABLE kb_ds_element_template (
    id bigint NOT NULL,
    dataset_id bigint NOT NULL,
    "position" integer DEFAULT 0,
    classifier_id bigint,
    attribute_id bigint,
    multiplicity integer,
    readonly boolean,
    aggregation boolean
);
ALTER TABLE kb_ds_element_template OWNER TO postgres;
CREATE TABLE kb_email_template (
    id bigint NOT NULL,
    creationdate timestamp with time zone DEFAULT now(),
    lastmodifieddate timestamp with time zone DEFAULT now(),
    lastmodifieduser bigint NOT NULL,
    state integer,
    domain_id bigint,
    lang character varying(24),
    xkey character varying(256),
    title character varying(256),
    fromstr character varying(512),
    subject character varying(512),
    strtext text,
    available_macros text,
    isdefault boolean DEFAULT false
);
ALTER TABLE kb_email_template OWNER TO postgres;
CREATE TABLE kb_enotirule (
    id bigint NOT NULL,
    lastmodifieddate timestamp with time zone DEFAULT now(),
    lastmodifieduser bigint,
    domain_id bigint,
    name character varying(150),
    condition character varying(4096),
    description character varying(4096),
    enabled boolean DEFAULT true,
    owner bigint,
    event_type integer DEFAULT 0,
    state integer,
    creationdate timestamp with time zone DEFAULT now(),
    notes text,
    is_system boolean DEFAULT false,
    isalert boolean DEFAULT false,
    isemail boolean DEFAULT true,
    key character varying(64)
);
ALTER TABLE kb_enotirule OWNER TO kbee;
CREATE TABLE kb_enotirule_principal (
    rule_id bigint NOT NULL,
    principal_id bigint NOT NULL
);
ALTER TABLE kb_enotirule_principal OWNER TO kbee;
CREATE TABLE kb_enotirule_role (
    rule_id bigint NOT NULL,
    role_id bigint NOT NULL
);
ALTER TABLE kb_enotirule_role OWNER TO postgres;
CREATE TABLE kb_facet_wrapper (
    id bigint NOT NULL,
    domain_id bigint,
    name character varying(128),
    display_name character varying(256),
    visibility text,
    lastmodifieddate timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    lastmodifieduser bigint,
    "order" integer,
    state integer,
    creationdate timestamp with time zone DEFAULT now(),
    viewmode integer DEFAULT 0
);
ALTER TABLE kb_facet_wrapper OWNER TO postgres;
CREATE TABLE kb_file_loader (
    id bigint NOT NULL,
    name character varying(128),
    javaclass character varying(128)
);
ALTER TABLE kb_file_loader OWNER TO postgres;
CREATE TABLE kb_file_proxy (
    resource_id bigint NOT NULL,
    file_loader bigint NOT NULL,
    url character varying(512),
    size integer DEFAULT '-1'::integer
);
ALTER TABLE kb_file_proxy OWNER TO postgres;
CREATE TABLE kb_form (
    id bigint NOT NULL,
    domain_id bigint NOT NULL,
    lastmodifieddate timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    creationdate timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    lastmodifieduser bigint NOT NULL,
    state integer,
    name character varying(256),
    display_name character varying(256),
    components text
);
ALTER TABLE kb_form OWNER TO postgres;
CREATE TABLE kb_form_data (
    id bigint NOT NULL,
    lastmodifieddate timestamp with time zone DEFAULT now(),
    creationdate timestamp with time zone DEFAULT now(),
    lastmodifieduser bigint,
    content_id bigint,
    form_id bigint,
    data text
);
ALTER TABLE kb_form_data OWNER TO postgres;
CREATE TABLE kb_form_template (
    form_id bigint,
    contenttemplate_id bigint
);
ALTER TABLE kb_form_template OWNER TO postgres;
CREATE TABLE kb_group_role (
    role_id bigint NOT NULL,
    group_id bigint NOT NULL
);
ALTER TABLE kb_group_role OWNER TO postgres;
CREATE TABLE kb_language_string (
    id bigint NOT NULL,
    key character varying(256) NOT NULL,
    locale character varying(128) NOT NULL,
    value text
);
ALTER TABLE kb_language_string OWNER TO postgres;
CREATE TABLE kb_launcher_group (
    id bigint NOT NULL,
    lastmodifieddate timestamp with time zone DEFAULT now(),
    creationdate timestamp with time zone DEFAULT now(),
    lastmodifieduser bigint,
    state integer DEFAULT 1,
    domain_id bigint,
    alias character varying(256) NOT NULL,
    name character varying(256) NOT NULL,
    description character varying(2048),
    "position" integer DEFAULT 0,
    visible boolean DEFAULT true
);
ALTER TABLE kb_launcher_group OWNER TO postgres;
CREATE TABLE kb_member_role (
    id bigint NOT NULL,
    entity_id bigint NOT NULL,
    role_id bigint NOT NULL,
    securityrule_id bigint,
    group_id bigint
);
ALTER TABLE kb_member_role OWNER TO postgres;
CREATE TABLE kb_model_section (
    id bigint NOT NULL,
    contenttemplate_id bigint NOT NULL,
    name character varying(128),
    description text,
    "position" integer DEFAULT 0,
    isportal boolean DEFAULT true
);
ALTER TABLE kb_model_section OWNER TO postgres;
CREATE TABLE kb_notification (
    id bigint NOT NULL,
    creationdate timestamp with time zone DEFAULT now(),
    lastmodifieddate timestamp with time zone DEFAULT now(),
    lastmodifieduser bigint NOT NULL,
    state integer,
    domain_id bigint,
    title character varying(256),
    text character varying(1024),
    content_id bigint,
    sender_id bigint NOT NULL,
    receiver_id bigint NOT NULL,
    datesend timestamp with time zone DEFAULT now(),
    dateread date,
    type integer DEFAULT 1,
    notification_state integer DEFAULT 1,
    notification_type integer DEFAULT 10 NOT NULL,
    work_note_id bigint,
    deleteonaccept boolean DEFAULT true,
    isalert boolean DEFAULT true,
    isbillboard boolean DEFAULT true,
    startpub timestamp with time zone DEFAULT now(),
    endpub timestamp with time zone,
    generating_enoti_rule bigint,
    generating_action_rule bigint
);
ALTER TABLE kb_notification OWNER TO kbee;
CREATE TABLE kb_object_property (
    id bigint NOT NULL,
    type integer,
    name character varying(128),
    object_id character varying(64) NOT NULL,
    value text,
    uset character varying(128),
    lastmodifieddate timestamp with time zone DEFAULT now(),
    domain_id bigint
);
ALTER TABLE kb_object_property OWNER TO postgres;
CREATE TABLE kb_organizationaldata (
    id bigint NOT NULL,
    person_id bigint,
    group_id bigint,
    securityrule_id bigint
);
ALTER TABLE kb_organizationaldata OWNER TO kbee;
CREATE TABLE kb_preference (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    name character varying(128) NOT NULL,
    properties text
);
ALTER TABLE kb_preference OWNER TO postgres;
CREATE TABLE kb_preference_domain (
    id bigint NOT NULL,
    domain_id bigint,
    name character varying(256) NOT NULL,
    properties text
);
ALTER TABLE kb_preference_domain OWNER TO postgres;
CREATE TABLE kb_relation_target (
    relationtemplate_id bigint NOT NULL,
    targettemplate_id bigint NOT NULL
);
ALTER TABLE kb_relation_target OWNER TO postgres;
CREATE TABLE kb_relation_template (
    id bigint NOT NULL,
    name character varying(128),
    source_label character varying(128),
    sourcetemplate_id bigint NOT NULL,
    target_label character varying(128),
    targettemplate_id bigint,
    multiplicity integer DEFAULT 4,
    aggregation boolean DEFAULT false,
    "position" integer DEFAULT 0,
    source_display_mode integer DEFAULT 0,
    target_display_mode integer DEFAULT 0,
    domain_id bigint,
    lastmodifieduser bigint,
    lastmodifieddate timestamp with time zone DEFAULT now(),
    creationdate timestamp with time zone DEFAULT now(),
    state integer DEFAULT 1 NOT NULL,
    target_order integer DEFAULT 0,
    reverse_order integer DEFAULT 0
);
ALTER TABLE kb_relation_template OWNER TO postgres;
CREATE TABLE kb_report (
    user_id bigint NOT NULL,
    content_id bigint NOT NULL,
    report integer,
    reportdate timestamp with time zone DEFAULT now(),
    id bigint NOT NULL
);
ALTER TABLE kb_report OWNER TO kbee;
CREATE TABLE kb_report_subscription (
    id bigint NOT NULL,
    lastmodifieddate timestamp with time zone DEFAULT now(),
    lastmodifieduser bigint,
    domain_id bigint,
    report_export_sched_id character varying(150) NOT NULL,
    enabled boolean NOT NULL,
    usr bigint,
    creationdate timestamp with time zone DEFAULT now(),
    last_export_sent timestamp with time zone,
    state integer DEFAULT 1
);
ALTER TABLE kb_report_subscription OWNER TO postgres;
CREATE TABLE kb_resource_group (
    id bigint NOT NULL,
    name character varying(256),
    creationdate timestamp with time zone DEFAULT now(),
    lastmodifieddate timestamp with time zone DEFAULT now(),
    lastmodifieduser bigint,
    domain_id bigint,
    alias character varying(256),
    state integer DEFAULT 1,
    createuser bigint,
    type integer DEFAULT 1 NOT NULL
);
ALTER TABLE kb_resource_group OWNER TO kbee;
CREATE TABLE kb_role (
    id bigint NOT NULL,
    name character varying(128),
    domain_id bigint NOT NULL,
    state integer,
    type integer,
    classifier_id bigint,
    condition text,
    displaycondition text,
    permissions character varying(1024),
    negative_permissions character varying(1024),
    canonical boolean DEFAULT false,
    securityrule_id bigint,
    creationdate timestamp with time zone DEFAULT now(),
    lastmodifieddate timestamp with time zone DEFAULT now(),
    lastmodifieduser bigint NOT NULL,
    alias character varying(64),
    api_enabled boolean DEFAULT true,
    description text,
    group_id bigint,
    enable_useradmin boolean DEFAULT false,
    enable_usercreation boolean DEFAULT false,
    isdefault boolean DEFAULT false
);
ALTER TABLE kb_role OWNER TO postgres;
CREATE TABLE kb_rsbycriteria_template (
    id bigint NOT NULL,
    name character varying(128),
    source_label character varying(128),
    sourcetemplate_id bigint NOT NULL,
    target_label character varying(128),
    "position" integer DEFAULT 0
);
ALTER TABLE kb_rsbycriteria_template OWNER TO postgres;
CREATE TABLE kb_searcher_homeblock (
    state integer DEFAULT 1,
    creationdate timestamp with time zone DEFAULT now(),
    lastmodifieddate timestamp with time zone DEFAULT now(),
    lastmodifieduser bigint,
    domain_id bigint,
    id bigint NOT NULL,
    lang character varying(256),
    title character varying(256),
    name character varying(256),
    iql text,
    sortstr character varying(32),
    formatstr character varying(32),
    custom_values json,
    abstract text
);
ALTER TABLE kb_searcher_homeblock OWNER TO postgres;
CREATE TABLE kb_security_rule (
    id bigint NOT NULL,
    lastmodifieddate timestamp with time zone DEFAULT now(),
    lastmodifieduser bigint,
    domain_id bigint,
    name character varying(150),
    type integer DEFAULT 1,
    condition text,
    description text,
    related_object_id character varying(48),
    acl bigint NOT NULL,
    creationdate timestamp with time zone DEFAULT now(),
    derived boolean DEFAULT false,
    displaycondition text,
    parent_objectid character varying(48),
    notes text,
    role_rule boolean DEFAULT false,
    state integer DEFAULT 4
);
ALTER TABLE kb_security_rule OWNER TO kbee;
CREATE TABLE kb_securitydata (
    id bigint NOT NULL,
    person_id bigint,
    group_id bigint,
    securityrule_id bigint
);
ALTER TABLE kb_securitydata OWNER TO kbee;
CREATE TABLE kb_sendemailevent (
    event_id bigint NOT NULL,
    event_type character varying(64) NOT NULL,
    event_time timestamp with time zone DEFAULT now(),
    event_user bigint,
    event_domain_id bigint,
    event_object_id character varying(32),
    email_from character varying(128),
    email_to character varying(128),
    email_subject character varying(256),
    email_text text,
    email_attachments text,
    event_result character varying(64),
    event_generator_action character varying(128),
    event_audit_resource_id bigint
);
ALTER TABLE kb_sendemailevent OWNER TO kbee;
CREATE TABLE kb_source (
    id bigint NOT NULL,
    creationdate timestamp with time zone DEFAULT now(),
    lastmodifieddate timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    lastmodifieduser bigint,
    domain_id bigint,
    name character varying(128),
    display_name character varying(256),
    state integer,
    createuser bigint
);
ALTER TABLE kb_source OWNER TO postgres;
CREATE TABLE kb_subscription (
    user_id bigint NOT NULL,
    content_oid bigint NOT NULL,
    event_id integer NOT NULL,
    subscription_date timestamp with time zone DEFAULT now(),
    type_id integer
);
ALTER TABLE kb_subscription OWNER TO kbee;
CREATE TABLE kb_subsectiontemplate (
    id bigint NOT NULL,
    contenttemplate_id bigint,
    name character varying(128),
    section_id bigint,
    korder integer
);
ALTER TABLE kb_subsectiontemplate OWNER TO postgres;
CREATE TABLE kb_system_properties (
    key character varying(256) NOT NULL,
    value text,
    area character varying(64) DEFAULT 'system'::character varying
);
ALTER TABLE kb_system_properties OWNER TO postgres;
CREATE TABLE kb_timer (
    id bigint NOT NULL,
    creationdate timestamp with time zone DEFAULT now(),
    duedate timestamp with time zone,
    callback bytea,
    attemps smallint,
    error_message character varying(256)
);
ALTER TABLE kb_timer OWNER TO kbee;
CREATE TABLE kb_tip (
    id bigint NOT NULL,
    domain_id bigint,
    area character(6),
    status integer,
    lastmodifieddate timestamp with time zone DEFAULT now(),
    lastmodifieduser bigint,
    tip_title character varying(256) NOT NULL,
    tip_text text,
    tip_texyid character varying(32),
    tip_lang character varying(32),
    tip_area character(18)
);
ALTER TABLE kb_tip OWNER TO kbee;
CREATE TABLE kb_tree_file (
    id bigint NOT NULL,
    parent_id bigint,
    isdirectory boolean DEFAULT true,
    dir_name character varying(1024),
    resource_id bigint,
    title character varying(256),
    "position" integer DEFAULT 0,
    state integer DEFAULT 1,
    creationdate timestamp with time zone DEFAULT now(),
    lastmodifieddate timestamp with time zone DEFAULT now(),
    domain_id bigint,
    lastmodifieduser bigint,
    type character varying(64),
    isaccesspoint boolean DEFAULT false,
    tree_idoc_id bigint,
    in_portal boolean DEFAULT true,
    oid bigint,
    ishead boolean DEFAULT true,
    version integer DEFAULT 0,
    prev_version bigint
);
ALTER TABLE kb_tree_file OWNER TO postgres;
CREATE TABLE kb_tree_idoc (
    content_id bigint NOT NULL,
    tree_file_id bigint
);
ALTER TABLE kb_tree_idoc OWNER TO postgres;
CREATE TABLE kb_tree_resource (
    resource_id bigint NOT NULL,
    treefile_id bigint NOT NULL
);
ALTER TABLE kb_tree_resource OWNER TO postgres;
CREATE TABLE kb_usage_stat (
    domain_id bigint NOT NULL,
    ts timestamp with time zone DEFAULT now() NOT NULL,
    hard_disk_usage bigint DEFAULT 0,
    users bigint DEFAULT 0,
    contents bigint DEFAULT 0,
    resources bigint DEFAULT 0,
    attributes text,
    hard_disk_usage_gateway bigint DEFAULT 0,
    resources_external bigint DEFAULT 0,
    kbfs1_hard_disk_usage bigint DEFAULT 0,
    kbfs2_hard_disk_usage bigint DEFAULT 0,
    kbfs2archive_hard_disk_usage bigint DEFAULT 0,
    contents_external bigint DEFAULT 0,
    contents_external_library bigint DEFAULT 0,
    contents_external_archive bigint DEFAULT 0,
    contents_external_recycle bigint DEFAULT 0,
    solr_content_items bigint DEFAULT 0,
    solr_audit_items bigint DEFAULT 0,
    solr_file_items bigint DEFAULT 0,
    database_usage bigint DEFAULT 0,
    s3_hard_disk_usage bigint DEFAULT 0,
    glacier_hard_disk_usage bigint DEFAULT 0,
    billable_users bigint DEFAULT 0 NOT NULL,
    billable_sites bigint DEFAULT 0 NOT NULL,
    units bigint DEFAULT 0 NOT NULL
);
ALTER TABLE kb_usage_stat OWNER TO kbee;
CREATE TABLE kb_user_list (
    id bigint NOT NULL,
    lastmodifieddate timestamp with time zone DEFAULT now(),
    creationdate timestamp with time zone DEFAULT now(),
    lastmodifieduser bigint,
    state integer DEFAULT 1,
    domain_id bigint,
    owner_id bigint,
    console character varying(256) NOT NULL,
    title character varying(2048) NOT NULL,
    description character varying(2048),
    total_items integer DEFAULT 0,
    version_match integer DEFAULT 1
);
ALTER TABLE kb_user_list OWNER TO postgres;
CREATE TABLE kb_user_list_item (
    id bigint NOT NULL,
    lastmodifieddate timestamp with time zone DEFAULT now(),
    creationdate timestamp with time zone DEFAULT now(),
    lastmodifieduser bigint,
    state integer DEFAULT 1,
    domain_id bigint,
    userlist_id bigint,
    content_id bigint,
    datasetmember_id bigint,
    user_id bigint,
    title character varying(1024),
    type integer DEFAULT 1,
    version_match integer DEFAULT 0,
    console character varying(256),
    owner_id bigint,
    oid bigint
);
ALTER TABLE kb_user_list_item OWNER TO postgres;
CREATE TABLE kb_user_note (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    title character varying(256),
    notetext text,
    creationdate timestamp with time zone DEFAULT now(),
    lastmodifieddate timestamp with time zone DEFAULT now(),
    lastmodifieduser bigint NOT NULL,
    priority character varying(24),
    domain_id bigint NOT NULL
);
ALTER TABLE kb_user_note OWNER TO postgres;
CREATE TABLE kb_user_property (
    id bigint NOT NULL,
    type integer,
    name character varying(128),
    user_id bigint NOT NULL,
    value text,
    uset character varying(256),
    lastmodifieddate timestamp with time zone DEFAULT now()
);
ALTER TABLE kb_user_property OWNER TO kbee;
CREATE TABLE kb_user_role (
    id bigint NOT NULL,
    userprofile_id bigint NOT NULL,
    role_id bigint NOT NULL,
    entity_id bigint,
    user_id bigint NOT NULL
);
ALTER TABLE kb_user_role OWNER TO postgres;
CREATE TABLE kb_userlistclassification (
    id bigint NOT NULL,
    classifier_id bigint,
    datasetmember_id bigint,
    "position" integer DEFAULT 0,
    user_list_item_id bigint
);
ALTER TABLE kb_userlistclassification OWNER TO postgres;
CREATE TABLE kb_vote (
    user_id bigint NOT NULL,
    content_id bigint NOT NULL,
    vote integer,
    votedate timestamp with time zone DEFAULT now(),
    id bigint NOT NULL
);
ALTER TABLE kb_vote OWNER TO kbee;
CREATE TABLE kb_work_note (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    title character varying(256),
    notetext text,
    creationdate timestamp with time zone DEFAULT now(),
    lastmodifieddate timestamp with time zone DEFAULT now(),
    lastmodifieduser bigint NOT NULL,
    priority character varying(24),
    domain_id bigint NOT NULL,
    send_notification boolean DEFAULT true,
    is_first_version boolean DEFAULT true,
    isalert boolean DEFAULT true,
    isemail boolean DEFAULT false,
    isbillboard boolean DEFAULT false,
    glyphicon character varying(64),
    startpub timestamp with time zone DEFAULT now(),
    endpub timestamp with time zone,
    cronexpression character varying(256)
);
ALTER TABLE kb_work_note OWNER TO postgres;
CREATE TABLE kb_work_note_user_read (
    id bigint NOT NULL,
    work_note_id bigint NOT NULL,
    user_id bigint NOT NULL,
    readdate timestamp with time zone DEFAULT now()
);
ALTER TABLE kb_work_note_user_read OWNER TO postgres;
CREATE TABLE kb_worknote_principal (
    note_id bigint NOT NULL,
    principal_id bigint NOT NULL
);
ALTER TABLE kb_worknote_principal OWNER TO postgres;
CREATE TABLE kfile (
    resource_id bigint NOT NULL,
    path character varying(512),
    file_type character(5),
    title character varying(256),
    subtitle character varying(256),
    description character varying(4096),
    thumbnailsmall character varying(128),
    thumbnaillarge character varying(128),
    width bigint DEFAULT 0,
    height bigint DEFAULT 0,
    crc32str character(8),
    uploadeddate timestamp with time zone DEFAULT now(),
    uploadeduser bigint,
    externallystored boolean DEFAULT false,
    storagemode integer DEFAULT 1,
    bucketname character varying(512),
    objectname character varying(512),
    shard integer DEFAULT 1,
    kfsize integer DEFAULT 0,
    fsid character varying(64),
    in_portal boolean DEFAULT true,
    isencrypted boolean DEFAULT false NOT NULL,
    minor boolean DEFAULT false
);
ALTER TABLE kfile OWNER TO kbee;
CREATE TABLE kgroup (
    id bigint NOT NULL,
    name character varying(120) NOT NULL,
    canonical boolean,
    description character varying(256),
    derived boolean DEFAULT false,
    enabled boolean DEFAULT true,
    onlyportal boolean DEFAULT false,
    onlydomainkbee boolean DEFAULT false,
    onlyinternaluse boolean DEFAULT false,
    areacode character varying(64)
);
ALTER TABLE kgroup OWNER TO kbee;
CREATE TABLE kgroupmember (
    kgroup bigint NOT NULL,
    principal bigint NOT NULL
);
ALTER TABLE kgroupmember OWNER TO kbee;
CREATE TABLE klock (
    lock_id integer NOT NULL,
    lock_object_id character varying(100),
    lock_date timestamp with time zone,
    lock_user_id character varying(50) NOT NULL,
    lock_scope character varying(50),
    lock_timeout timestamp without time zone
);
ALTER TABLE klock OWNER TO kbee;
CREATE TABLE kresource (
    id bigint NOT NULL,
    oid bigint,
    lastmodifieddate timestamp with time zone DEFAULT now(),
    creationdate timestamp with time zone DEFAULT now(),
    lastmodifieduser bigint NOT NULL,
    state integer,
    domain_id bigint DEFAULT 1,
    group_id bigint,
    title character varying(256),
    name character varying(256),
    version integer,
    prev_version bigint,
    kmode integer,
    seed character(16),
    ishead boolean DEFAULT true,
    ksize bigint,
    ispublic boolean DEFAULT true,
    in_portal boolean DEFAULT true
);
ALTER TABLE kresource OWNER TO kbee;
CREATE FOREIGN TABLE loadavg (
    one text,
    five text,
    fifteen text,
    scheduled text,
    pid text
)
SERVER fileserver
OPTIONS (
    delimiter ' ',
    filename '/proc/loadavg',
    format 'text'
);
ALTER FOREIGN TABLE loadavg OWNER TO postgres;
CREATE SEQUENCE lock_sequence
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
ALTER TABLE lock_sequence OWNER TO kbee;
CREATE SEQUENCE log_sequence
    START WITH 10000
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
ALTER TABLE log_sequence OWNER TO kbee;
CREATE TABLE logevent (
    event_id bigint NOT NULL,
    event_type character varying(64) NOT NULL,
    event_object_id character varying(64),
    event_content_id character varying(64),
    event_version integer,
    event_time timestamp with time zone DEFAULT now(),
    event_user bigint,
    event_user_to bigint,
    event_task character varying(128),
    event_parameters text,
    event_domain_id bigint,
    event_title character varying(256),
    event_kbeeclass character varying(64),
    event_procedure character varying(64),
    auditset integer DEFAULT 0,
    event_activity_id bigint DEFAULT '-1'::integer,
    event_resource_id bigint,
    event_content_xid bigint,
    event_type_int integer,
    event_audit_resource_id bigint
);
ALTER TABLE logevent OWNER TO kbee;
CREATE TABLE logevent_legacy (
    event_id bigint NOT NULL,
    event_type character varying(64),
    event_object_id character varying(64),
    event_content_id character varying(64),
    event_version integer,
    event_time timestamp with time zone,
    event_user bigint,
    event_user_to bigint,
    event_kbeeclass character varying(64),
    event_task character varying(128),
    event_parameters text,
    event_domain_id bigint,
    event_title character varying(256),
    event_procedure character varying(64),
    auditset integer
);
ALTER TABLE logevent_legacy OWNER TO postgres;
CREATE SEQUENCE logsites_sequence
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
ALTER TABLE logsites_sequence OWNER TO postgres;
CREATE TABLE memberclassification (
    id bigint NOT NULL,
    state integer,
    sourcemember_id bigint NOT NULL,
    classifier_id bigint NOT NULL,
    targetmember_id bigint,
    "position" integer DEFAULT 0
);
ALTER TABLE memberclassification OWNER TO kbee;
CREATE FOREIGN TABLE meminfo (
    stat text,
    value text
)
SERVER fileserver
OPTIONS (
    delimiter ':',
    filename '/proc/meminfo',
    format 'csv'
);
ALTER FOREIGN TABLE meminfo OWNER TO postgres;
CREATE SEQUENCE objectid_sequence
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
ALTER TABLE objectid_sequence OWNER TO kbee;
CREATE TABLE organization (
    id bigint NOT NULL,
    lastmodifieddate timestamp with time zone DEFAULT now(),
    lastmodifieduser bigint NOT NULL,
    state integer,
    domain_id bigint,
    email character varying(120),
    address character varying(256),
    phone character varying(120),
    website character varying(120),
    name character varying(256)
);
ALTER TABLE organization OWNER TO kbee;
CREATE TABLE organizationaltext (
    content_id bigint NOT NULL,
    subtitle character varying(256),
    contentdate timestamp with time zone,
    author_id bigint,
    media character varying(256),
    communitacion_class character(6),
    text text,
    summary text
);
ALTER TABLE organizationaltext OWNER TO kbee;
CREATE TABLE orgchart (
    content_id bigint NOT NULL,
    name character varying(256),
    description character varying(2048),
    mision character varying(2048),
    xmlchart text
);
ALTER TABLE orgchart OWNER TO kbee;
CREATE TABLE person (
    entity_id bigint NOT NULL,
    email character varying(120),
    address character varying(256),
    phone character varying(120),
    website character varying(120),
    firstname character varying(120),
    lastname character varying(120),
    description character varying(2048),
    birthdate date,
    photo bigint,
    workposition character varying(256),
    photo_domain_logo boolean DEFAULT false,
    emailvalidated boolean DEFAULT false
);
ALTER TABLE person OWNER TO kbee;
CREATE TABLE po_area (
    po_id bigint NOT NULL,
    page_id bigint,
    area_type integer,
    orden integer,
    full_width_canvas boolean,
    areaclass character varying(128),
    custom_values text,
    parent_area_id bigint
);
ALTER TABLE po_area OWNER TO postgres;
CREATE TABLE po_block (
    po_id bigint NOT NULL,
    area_id bigint,
    section integer,
    orden integer,
    subtitle character varying(256),
    textstyle character varying(128),
    image_id bigint,
    new_tab boolean,
    maxlements integer,
    quantity_visible boolean,
    title_visible boolean,
    intro_visible boolean,
    image_visible boolean,
    intro_only_image boolean,
    external_link character varying(128),
    page_link bigint,
    content_link bigint,
    block_image bigint,
    block_menu_enabled boolean,
    description character varying(4096),
    block_css character varying(128),
    usage_info character varying(2048),
    image_css character varying(64),
    block_body_style character varying(1024),
    content_id bigint,
    custom_values text
);
ALTER TABLE po_block OWNER TO postgres;
CREATE TABLE po_block_banners (
    block_id bigint NOT NULL
);
ALTER TABLE po_block_banners OWNER TO postgres;
CREATE TABLE po_block_contact (
    block_id bigint NOT NULL,
    emailto character varying(256)
);
ALTER TABLE po_block_contact OWNER TO postgres;
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
CREATE TABLE po_block_cumpleanos (
    block_id bigint NOT NULL,
    date_from timestamp without time zone,
    date_to timestamp without time zone,
    image_visible boolean DEFAULT true,
    feriados text
);
ALTER TABLE po_block_cumpleanos OWNER TO postgres;
CREATE TABLE po_block_footer (
    block_id bigint NOT NULL,
    element_css character varying(512)
);
ALTER TABLE po_block_footer OWNER TO postgres;
CREATE TABLE po_block_gallery_viewer (
    block_id bigint NOT NULL
);
ALTER TABLE po_block_gallery_viewer OWNER TO kbee;
CREATE TABLE po_block_image_viewer (
    block_id bigint NOT NULL,
    link_container_css character varying(64),
    image_container_css character varying(64),
    imageviewer_id bigint,
    url character varying(256)
);
ALTER TABLE po_block_image_viewer OWNER TO postgres;
CREATE TABLE po_block_search_external (
    block_id bigint NOT NULL,
    container_css character varying(64),
    element_css character varying(64),
    url character varying(2048)
);
ALTER TABLE po_block_search_external OWNER TO postgres;
CREATE TABLE po_block_select_list (
    block_id bigint NOT NULL,
    select_container_css character varying(64),
    select_css character varying(64),
    select_list_str character varying(8192)
);
ALTER TABLE po_block_select_list OWNER TO postgres;
CREATE TABLE po_block_selector (
    block_id bigint NOT NULL,
    element_css character varying(512)
);
ALTER TABLE po_block_selector OWNER TO postgres;
CREATE TABLE po_block_site_components (
    block_id bigint NOT NULL,
    site_id bigint NOT NULL,
    block_type integer DEFAULT 0
);
ALTER TABLE po_block_site_components OWNER TO postgres;
CREATE TABLE po_block_site_list (
    block_id bigint NOT NULL,
    query character varying(256),
    element_title_enabled boolean DEFAULT true
);
ALTER TABLE po_block_site_list OWNER TO postgres;
CREATE TABLE po_block_text (
    block_id bigint NOT NULL,
    text_css character varying(64),
    max_description_length integer DEFAULT 0
);
ALTER TABLE po_block_text OWNER TO postgres;
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
    subtitle_mode integer DEFAULT 0,
    element_orientation_css character varying(512)
);
ALTER TABLE po_block_view_list OWNER TO postgres;
CREATE TABLE po_block_view_recent_list (
    block_id bigint NOT NULL,
    global boolean DEFAULT false
);
ALTER TABLE po_block_view_recent_list OWNER TO postgres;
CREATE TABLE po_block_wall_viewer (
    block_id bigint NOT NULL
);
ALTER TABLE po_block_wall_viewer OWNER TO kbee;
CREATE TABLE po_block_x (
    block_id bigint NOT NULL
);
ALTER TABLE po_block_x OWNER TO postgres;
CREATE TABLE po_contentblock (
    block_id bigint NOT NULL,
    content_id bigint NOT NULL,
    orden integer
);
ALTER TABLE po_contentblock OWNER TO postgres;
CREATE TABLE po_diagrammable_area (
    po_id bigint NOT NULL,
    page_id bigint NOT NULL,
    area_type integer,
    orden integer,
    full_width_canvas boolean DEFAULT false,
    areaclass character varying(128)
);
ALTER TABLE po_diagrammable_area OWNER TO postgres;
CREATE TABLE po_diagrammable_block (
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
    block_body_style character varying(1024),
    block_separator_css character varying(512),
    content_id bigint
);
ALTER TABLE po_diagrammable_block OWNER TO postgres;
CREATE TABLE po_diagrammable_page (
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
ALTER TABLE po_diagrammable_page OWNER TO postgres;
CREATE TABLE po_diagrammable_site (
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
ALTER TABLE po_diagrammable_site OWNER TO postgres;
CREATE TABLE po_page (
    po_id bigint NOT NULL,
    site_id bigint,
    description character varying(128),
    relative_url character varying(128),
    is_admin boolean,
    issection boolean,
    ishome boolean,
    orden integer,
    page_type integer,
    content_link bigint,
    is_header_container boolean,
    contentid character varying(256),
    menus_visible boolean,
    usage_info character varying(2048),
    custom_values text
);
ALTER TABLE po_page OWNER TO postgres;
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
    ishead boolean DEFAULT true,
    nextversion integer DEFAULT 0
);
ALTER TABLE po_portalobject OWNER TO postgres;
CREATE TABLE po_site (
    po_id bigint NOT NULL,
    site_type integer,
    ispublic boolean DEFAULT true,
    isexternal boolean,
    subtitle character varying(512),
    description character varying(512),
    uri character varying(256),
    detail_comments_enabled boolean,
    detail_votes_enabled boolean,
    detail_follow_enabled boolean,
    detail_related_enabled boolean,
    detail_send_enabled boolean,
    footer_block_id bigint,
    header_block_id bigint,
    email_contact character varying(512),
    site_template integer,
    page_header_footer_id bigint,
    site_image bigint,
    isimagevisible boolean,
    isdisplayvalidversion boolean DEFAULT false,
    custom_values text,
    alias character varying(128)
);
ALTER TABLE po_site OWNER TO postgres;
CREATE TABLE po_site_favorites (
    id bigint NOT NULL,
    user_id bigint NOT NULL
);
ALTER TABLE po_site_favorites OWNER TO postgres;
CREATE TABLE po_site_favorites_list (
    list_id bigint NOT NULL,
    site_oid bigint NOT NULL,
    orden integer
);
ALTER TABLE po_site_favorites_list OWNER TO postgres;
CREATE TABLE po_site_securityrule (
    rule_id bigint NOT NULL,
    related_object_id character varying(48)
);
ALTER TABLE po_site_securityrule OWNER TO postgres;
CREATE TABLE po_site_subscription (
    user_id bigint NOT NULL,
    site_oid bigint NOT NULL,
    event_id integer NOT NULL,
    subscription_date timestamp with time zone DEFAULT now(),
    type_id integer
);
ALTER TABLE po_site_subscription OWNER TO postgres;
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
    user_agent character varying(512),
    object_id character varying(64),
    content_oid bigint,
    version integer,
    content_long_id bigint
);
ALTER TABLE po_sitelogin OWNER TO postgres;
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
CREATE TABLE po_siteuser (
    site_id bigint NOT NULL,
    user_id bigint NOT NULL,
    permission integer
);
ALTER TABLE po_siteuser OWNER TO postgres;
CREATE TABLE po_siteuserrights (
    site_id bigint NOT NULL,
    user_id bigint NOT NULL,
    permissions integer,
    lastmodifieddate timestamp with time zone DEFAULT now(),
    lastmodifieduser bigint NOT NULL
);
ALTER TABLE po_siteuserrights OWNER TO postgres;
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
    ntab boolean DEFAULT false,
    iconcss character varying(64)
);
ALTER TABLE po_viewbk OWNER TO postgres;
CREATE TABLE po_viewbkblock (
    view_id bigint NOT NULL,
    block_id bigint
);
ALTER TABLE po_viewbkblock OWNER TO postgres;
CREATE TABLE po_viewbkcontent (
    view_id bigint NOT NULL,
    content_id bigint,
    is_gallery boolean DEFAULT false,
    is_resources boolean DEFAULT true
);
ALTER TABLE po_viewbkcontent OWNER TO postgres;
CREATE TABLE po_viewbklink (
    view_id bigint NOT NULL,
    link character varying(1024)
);
ALTER TABLE po_viewbklink OWNER TO postgres;
CREATE TABLE po_viewbksite (
    view_id bigint NOT NULL,
    site_id bigint
);
ALTER TABLE po_viewbksite OWNER TO postgres;
CREATE TABLE po_viewcontent (
    po_id bigint NOT NULL,
    content_id bigint NOT NULL,
    titlemode integer DEFAULT 0,
    isabstract boolean DEFAULT true,
    ismetadata boolean DEFAULT true,
    isviewer boolean DEFAULT false,
    bodytemplate integer DEFAULT 0,
    isresources boolean DEFAULT false,
    resourcesmode integer DEFAULT 0,
    resourcesids character varying(4096),
    content_oid bigint
);
ALTER TABLE po_viewcontent OWNER TO postgres;
CREATE TABLE po_viewcontentrelation (
    view_id bigint NOT NULL,
    target_id bigint NOT NULL,
    "position" integer
);
ALTER TABLE po_viewcontentrelation OWNER TO postgres;
CREATE SEQUENCE portalid_sequence
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
ALTER TABLE portalid_sequence OWNER TO postgres;
CREATE TABLE principal (
    id bigint NOT NULL,
    lastmodifieddate timestamp with time zone DEFAULT now(),
    lastmodifieduser bigint,
    domain_id bigint,
    creationdate timestamp with time zone DEFAULT now()
);
ALTER TABLE principal OWNER TO kbee;
CREATE TABLE profile (
    id bigint NOT NULL,
    lastmodifieddate timestamp with time zone DEFAULT now(),
    lastmodifieduser bigint NOT NULL,
    entity bigint,
    domain_id bigint,
    creationdate timestamp with time zone DEFAULT now()
);
ALTER TABLE profile OWNER TO kbee;
CREATE TABLE property (
    id bigint NOT NULL,
    type integer,
    name character varying(128),
    content_id bigint NOT NULL,
    value text,
    uset character varying(256),
    lastmodifieddate timestamp with time zone DEFAULT now()
);
ALTER TABLE property OWNER TO kbee;
CREATE SEQUENCE propertyid_sequence
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
ALTER TABLE propertyid_sequence OWNER TO kbee;
CREATE SEQUENCE qaid_sequence
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
ALTER TABLE qaid_sequence OWNER TO kbee;
CREATE SEQUENCE query_sequence
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
ALTER TABLE query_sequence OWNER TO kbee;
CREATE TABLE resourcefile (
    resource_id bigint NOT NULL,
    file_id bigint NOT NULL,
    listorder integer,
    text character varying(128)
);
ALTER TABLE resourcefile OWNER TO kbee;
CREATE SEQUENCE resourceid_sequence
    START WITH 10000
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
ALTER TABLE resourceid_sequence OWNER TO kbee;
CREATE TABLE rs_content_pivot (
    content_id bigint NOT NULL,
    content_oid bigint,
    pivot_last_update timestamp with time zone,
    data jsonb
);
ALTER TABLE rs_content_pivot OWNER TO postgres;
CREATE TABLE rs_user_pivot (
    user_id bigint NOT NULL,
    pivot_last_update timestamp with time zone,
    data jsonb
);
ALTER TABLE rs_user_pivot OWNER TO postgres;
CREATE TABLE savedquery (
    id bigint NOT NULL,
    title character varying(512),
    statement text,
    "position" integer,
    console character varying(24),
    is_system boolean DEFAULT false,
    user_id bigint,
    lastmodifieddate timestamp with time zone DEFAULT now(),
    creationdate timestamp with time zone DEFAULT now(),
    lastmodifieduser bigint,
    state integer DEFAULT 1,
    domain_id bigint
);
ALTER TABLE savedquery OWNER TO kbee;
CREATE TABLE scheduler (
    id bigint NOT NULL,
    request bytea,
    "time" timestamp with time zone DEFAULT now(),
    priority integer,
    error_count integer,
    description text,
    error_message character varying(512),
    title character varying(64),
    objectid character varying(256),
    execute_after timestamp with time zone,
    command_class_name character varying(1024),
    command_parameters character varying(2048)
);
ALTER TABLE scheduler OWNER TO kbee;
CREATE SEQUENCE scheduler_sequence
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
ALTER TABLE scheduler_sequence OWNER TO kbee;
CREATE SEQUENCE security_sequence
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
ALTER TABLE security_sequence OWNER TO kbee;
CREATE SEQUENCE sendemail_log_sequence
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
ALTER TABLE sendemail_log_sequence OWNER TO kbee;
CREATE SEQUENCE seqtmp
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
ALTER TABLE seqtmp OWNER TO postgres;
CREATE SEQUENCE timer_sequence
    START WITH 10000
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
ALTER TABLE timer_sequence OWNER TO kbee;
CREATE TABLE userlabel (
    id bigint NOT NULL,
    user_id bigint,
    label character varying(128),
    css character(24),
    short_label character(8),
    scope integer DEFAULT 1,
    context character varying(128),
    creationdate timestamp with time zone DEFAULT now()
);
ALTER TABLE userlabel OWNER TO kbee;
CREATE TABLE userprofile (
    id bigint NOT NULL,
    state integer,
    confidencelevel double precision DEFAULT 0.0,
    user_id bigint,
    email_notifications boolean DEFAULT true,
    tipoftheday boolean DEFAULT true,
    editperson boolean DEFAULT true,
    email_rule_notifications boolean DEFAULT true,
    sendfilesemail boolean DEFAULT true,
    uitheme character varying(32) DEFAULT 'rpdm'::character varying,
    startpage character varying(64),
    lastlogindate timestamp with time zone,
    email_notifications_pending boolean DEFAULT true,
    changepassword boolean DEFAULT true,
    isclient boolean DEFAULT true
);
ALTER TABLE userprofile OWNER TO kbee;
CREATE TABLE users (
    id bigint NOT NULL,
    lastmodifieddate timestamp with time zone DEFAULT now(),
    lastmodifieduser bigint,
    state integer,
    username character varying(120) NOT NULL,
    password character varying(128) DEFAULT 'root'::character varying,
    password_md5 character varying(512),
    seed bytea,
    firstname character varying(120),
    lastname character varying(120),
    email character varying(256),
    locale_str character(6) DEFAULT 'eng'::bpchar,
    enabled boolean DEFAULT true,
    canonical boolean DEFAULT false,
    active boolean DEFAULT true,
    creationdate timestamp with time zone DEFAULT now(),
    timezone character varying(256) DEFAULT 'US/Central'::character varying,
    uitheme character varying(32) DEFAULT 'rpdm'::character varying,
    passwordlastmodifieddate timestamp with time zone,
    is_billable boolean DEFAULT true
);
ALTER TABLE users OWNER TO kbee;
CREATE TABLE wf_activity (
    id bigint NOT NULL,
    process_id bigint NOT NULL,
    task character varying(128),
    user_id bigint NOT NULL,
    assigned_by bigint,
    content_id bigint NOT NULL,
    startime timestamp with time zone,
    endtime timestamp with time zone,
    event character varying(128),
    note text,
    resolution text,
    status character varying(20),
    resolutiontitle character varying(256),
    event_label character varying(64)
);
ALTER TABLE wf_activity OWNER TO kbee;
CREATE TABLE wf_launcher (
    id bigint NOT NULL,
    domain_id bigint NOT NULL,
    label character varying(128),
    contenttemplate_id bigint,
    procedure_id bigint,
    contextual boolean,
    acl bigint,
    isenabled boolean DEFAULT true,
    alias character varying(64),
    lastmodifieddate timestamp with time zone DEFAULT now(),
    creationdate timestamp with time zone DEFAULT now(),
    lastmodifieduser bigint,
    state integer DEFAULT 1,
    launchergroup_id bigint
);
ALTER TABLE wf_launcher OWNER TO kbee;
CREATE TABLE wf_procedure (
    id bigint NOT NULL,
    alias character varying(64),
    lastmodifieddate timestamp with time zone DEFAULT now(),
    creationdate timestamp with time zone DEFAULT now(),
    lastmodifieduser bigint NOT NULL,
    domain_id bigint NOT NULL,
    state integer,
    name character varying(128),
    tasks text,
    states character varying(128),
    initial_rules text,
    code character(12),
    roles character varying(512),
    launcher character varying(128),
    diagram bigint,
    version integer DEFAULT 1,
    description text
);
ALTER TABLE wf_procedure OWNER TO kbee;
CREATE TABLE wf_process (
    id bigint NOT NULL,
    procedure character varying(128),
    startime timestamp with time zone,
    endtime timestamp with time zone,
    status character varying(20),
    procedure_id bigint
);
ALTER TABLE wf_process OWNER TO kbee;
CREATE SEQUENCE workflow_sequence
    START WITH 10000
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
ALTER TABLE workflow_sequence OWNER TO kbee;
COPY api_logevent (event_id, event_domain, event_file, event_time, event_user, event_transaction, event_uri, event_method, event_request, event_status, event_response, event_processing_time, event_retry, event_retrynumber, event_source, event_contentclass, event_closed, event_filesource) FROM stdin;
\.
COPY api_soapevent (event_id, event_domain, event_file, event_time, event_user, event_transaction, event_uri, event_method, event_request, event_status, event_response, event_processing_time, event_source, event_retry, event_retrynumber, event_closed, event_contentclass, event_filesource) FROM stdin;
\.
COPY authorities (username, authority) FROM stdin;
\.
COPY classification (id, state, datevalue, content_id, classifier_id, datasetmember_id, "position") FROM stdin;
\.
COPY classifiercontent (classifier_id, contentclass_id) FROM stdin;
\.
COPY content (id, oid, lastmodifieddate, creationdate, lastmodifieduser, state, domain_id, base, lang, title, content_abstract, name, version, nextversion, prev_version, ishead, contenttemplate, comments, locked, workspace, qastate, qamsg, attributes, user_defined_properties, external_id, private_notes, checkindate, external_time, source_id, acl) FROM stdin;
2	1	2019-09-05 17:11:06.173-03	2019-09-05 17:11:06.148-03	1	4	1	\N	\N	Upload and Create Container	Container of KBFile uploaded in the Upload and Create Page	\N	1	0	\N	f	10	t	f	1	2	Type	\N	\N	\N	\N	\N	\N	\N	\N
\.
COPY contentclass (id, enabled, name, javaclass, indexable, selectable) FROM stdin;
KbeeIDoc	t	IDoc	com.novamens.kbee.content.document.KbeeIDoc	t	t
KbeeOrganizationalText	t	OrganizationalText	com.novamens.kbee.content.communication.KbeeOrganizationalText	t	t
KbeeQuestion	t	Question	com.novamens.kbee.content.questionanswer.KbeeQuestion	t	t
KbeeContent	t	Content	com.novamens.kbee.content.base.KbeeContent	f	t
KbeeAnswer	t	Answer	com.novamens.kbee.content.questionanswer.KbeeAnswer	f	t
KbeeComment	t	Comment	com.novamens.kbee.content.social.KbeeComment	f	t
KbeeView	t	View	com.novamens.kbee.portal.model.KbeeViewDetailContent	t	t
KbeeLinkView	t	LinkView	com.novamens.kbee.portal.model.KbeeViewBKLink	t	t
KbeeSite	t	Site	com.novamens.kbee.portal.model.KbeeSite	t	t
KbeeUserListItem	t	UserListItem	com.novamens.kbee.content.userlist.KbeeUserListItem	t	t
\.
COPY contentproperties (content_id, contentproperties, lastmodifieddate, lastmodifieduser) FROM stdin;
\.
COPY contentresource (content_id, resource_id, "position", id, ispublic, group_id) FROM stdin;
\.
COPY contentstat (content_id, views, shared, favorites, votes) FROM stdin;
\.
COPY databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) FROM stdin;
liquibase_path_normalization	grodriguez	scripts/v5.4.3.xml	2019-09-27 01:33:12.469942	1	EXECUTED	8:a6f0211d558dcbb8f6dc5477a3010af2	sql; sql		\N	3.7.0	\N	\N	9565998590
versionTag	grodriguez	scripts/v5.4.3.xml	2019-09-27 01:33:12.476534	2	EXECUTED	8:0c648fb23bccd01deeec657a548257f1	tagDatabase		5.4.3	3.7.0	\N	\N	9565998590
user_password_to_len128	grodriguez	scripts/v5.4.3.xml	2019-09-27 01:33:12.490926	3	EXECUTED	8:61611325328a6c745ccd09d4a9c5a3f4	modifyDataType columnName=password, tableName=users		\N	3.7.0	\N	\N	9565998590
password_migration_spring_sec_5	grodriguez	scripts/v5.4.3.xml	2019-09-27 01:33:12.941796	4	EXECUTED	8:6c76980511d94ef14ccb51bfbd65337d	sql		\N	3.7.0	\N	\N	9565998590
wf_procedure_version	aferraria	scripts/v5.4.3.xml	2019-09-27 01:33:12.999715	5	EXECUTED	8:d5fc0d0282cea379e3728591a1c2bfdd	addColumn tableName=wf_procedure		\N	3.7.0	\N	\N	9565998590
add_column_isdefault_template	atolomei	scripts/v5.4.3.xml	2019-09-27 01:33:31.741733	6	EXECUTED	8:c7235a21a9877cdb9fc44f6b183a495a	addColumn tableName=kb_email_template		\N	3.7.0	\N	\N	9566017803
pivot_content_template_alias	grodriguez	scripts/v5.4.3.xml	2019-09-27 01:36:09.836736	7	EXECUTED	8:8e5b1628080339ea8dbe390b2a7c5509	addColumn tableName=rs_windsor_activity_pivot; sql		\N	3.7.0	\N	\N	9566024132
pivot_windsor_submissiontime	grodriguez	scripts/v5.4.3.xml	2019-09-27 01:38:39.040832	8	EXECUTED	8:927cbfa9c818a9e527bd6ac1bafc24fb	addColumn tableName=rs_windsor_activity_pivot; sql		\N	3.7.0	\N	\N	9566024132
pivot_windsor_update_type	grodriguez	scripts/v5.4.3.xml	2019-09-27 01:38:58.450595	9	EXECUTED	8:4f66bd52a6adcb964fb3526796438276	sql		\N	3.7.0	\N	\N	9566024132
userprofile_email_pending	atolomei	scripts/v5.4.3.xml	2019-09-27 01:38:59.686333	10	EXECUTED	8:4296b8f736696b084c32f865c6e01c7e	addColumn tableName=userprofile		\N	3.7.0	\N	\N	9566024132
database_usage	atolomei	scripts/v5.4.3.xml	2019-09-27 01:39:00.512739	11	EXECUTED	8:a93f962068ce99dec7245bf5c436f35c	addColumn tableName=kb_usage_stat		\N	3.7.0	\N	\N	9566024132
email_template_title	atolomei	scripts/v5.4.3.xml	2019-09-27 01:39:00.544933	12	EXECUTED	8:fddb039c5440ca8765f416f248d06b05	sql		\N	3.7.0	\N	\N	9566024132
email_template_task_name	atolomei	scripts/v5.4.3.xml	2019-09-27 01:39:00.56661	13	EXECUTED	8:964850b38d95c655debaa2d66b4b512f	sql		\N	3.7.0	\N	\N	9566024132
dataset_aggregation	aferraria	scripts/v5.4.3.xml	2019-11-18 23:29:23.419145	14	EXECUTED	8:bf5cad61ec8f11cbec5dc3e2937e3ed1	addColumn tableName=dataset		\N	3.7.0	\N	\N	4141154494
po_site_favorites_list_0	atolomei	scripts/v5.4.3.xml	2019-11-18 23:29:23.466293	15	EXECUTED	8:64d6fb5b28e17878f95c4d865fdc885a	sql		\N	3.7.0	\N	\N	4141154494
po_site_favorites_list	atolomei	scripts/v5.4.3.xml	2019-11-18 23:29:23.553504	16	EXECUTED	8:2f73c6f13704fbded020a557acafc722	sql		\N	3.7.0	\N	\N	4141154494
site_indexable_3	aferraria	scripts/v5.4.3.xml	2019-11-18 23:29:23.568498	17	EXECUTED	8:048ebdf0aacacbbec4cfce722ff8c398	sql		\N	3.7.0	\N	\N	4141154494
dataset_elements_template_drop	grodriguez	scripts/v5.4.3.xml	2019-11-18 23:29:23.633661	18	MARK_RAN	8:756213f7c02252aa8ded64b6666fec04	sql		\N	3.7.0	\N	\N	4141154494
dataset_elements_template_create	aferraria	scripts/v5.4.3.xml	2019-11-18 23:29:23.687803	19	EXECUTED	8:b192fce490e0d342d7ba92c22b35c7c3	sql		\N	3.7.0	\N	\N	4141154494
po_viewbk_iconcss	atolomei	scripts/v5.4.3.xml	2019-11-18 23:29:23.704776	20	EXECUTED	8:2a78768ac0b1e40da9c59acd58428bd8	addColumn tableName=po_viewbk		\N	3.7.0	\N	\N	4141154494
classifier_accessibility	aferraria	scripts/v5.4.3.xml	2019-11-18 23:29:23.741621	21	EXECUTED	8:3f0fb38215cc2026344972211339c373	addColumn tableName=kb_classifiertemplate		\N	3.7.0	\N	\N	4141154494
po_viewbk_iconcss	aferraria	scripts/v5.4.3.xml	2019-11-18 23:29:23.770793	22	EXECUTED	8:89a0509bc647786dc9ecac5088f2390c	addColumn tableName=kb_attributetemplate		\N	3.7.0	\N	\N	4141154494
kb_work_note_isbillboard	atolomei	scripts/v5.4.3.xml	2019-11-18 23:29:23.84593	23	EXECUTED	8:3ac3220e371d052dcb9f4deacca52708	addColumn tableName=kb_work_note		\N	3.7.0	\N	\N	4141154494
kb_work_note_glyphicon	atolomei	scripts/v5.4.3.xml	2019-11-18 23:29:23.863499	24	EXECUTED	8:0149fe394e9ed4566eb0c925059450e8	addColumn tableName=kb_work_note		\N	3.7.0	\N	\N	4141154494
kb_notification_isalert	atolomei	scripts/v5.4.3.xml	2019-11-18 23:29:23.94617	25	EXECUTED	8:b9d415a5022bc096240d994f7bf92035	addColumn tableName=kb_notification		\N	3.7.0	\N	\N	4141154494
kb_isbillboard_isalert	atolomei	scripts/v5.4.3.xml	2019-11-18 23:29:24.015465	26	EXECUTED	8:86df85935bdf45ae5cab37de46998838	addColumn tableName=kb_notification		\N	3.7.0	\N	\N	4141154494
notification_indexes_2	atolomei	scripts/v5.4.3.xml	2019-11-18 23:29:24.045513	27	EXECUTED	8:0feb33092c46d5fb60d16d0640dbd0f5	sql		\N	3.7.0	\N	\N	4141154494
ds_elements_template_multiplicity_2	aferraria	scripts/v5.4.3.xml	2019-11-18 23:29:24.06209	28	EXECUTED	8:48850bfb47376392843cd88798a60099	sql		\N	3.7.0	\N	\N	4141154494
logevent_indexes	atolomei	scripts/v5.4.3.xml	2019-11-18 23:30:58.840637	29	EXECUTED	8:1cb83c174229398f8f615d73119c37d9	sql		\N	3.7.0	\N	\N	4141154494
notifaction_datestart	atolomei	scripts/v5.4.3.xml	2019-11-18 23:30:58.976445	30	EXECUTED	8:7e5a16c842f45f81ef7794c7f59b3caa	addColumn tableName=kb_notification		\N	3.7.0	\N	\N	4141154494
notifaction_dateend	atolomei	scripts/v5.4.3.xml	2019-11-18 23:30:58.987024	31	EXECUTED	8:88cc959f5e30815b870ef7ffed57cd44	addColumn tableName=kb_notification		\N	3.7.0	\N	\N	4141154494
classifier_template_criteria	aferraria	scripts/v5.4.3.xml	2019-11-18 23:30:59.003541	32	EXECUTED	8:c185c90c2be54815059ca2371e4a99f0	addColumn tableName=kb_classifiertemplate		\N	3.7.0	\N	\N	4141154494
versionTag	grodriguez	scripts/v5.4.5.xml	2019-11-18 23:30:59.008901	33	EXECUTED	8:4d2c1fdd2278b6387d88b9f2f285c5cc	tagDatabase		5.4.5	3.7.0	\N	\N	4141154494
worknote_datestart	atolomei	scripts/v5.4.5.xml	2019-11-18 23:30:59.058025	34	EXECUTED	8:e513a52eba866432f3b2d793ebdf7fbf	addColumn tableName=kb_work_note		\N	3.7.0	\N	\N	4141154494
worknote_dateend	atolomei	scripts/v5.4.5.xml	2019-11-18 23:30:59.071246	35	EXECUTED	8:238881eda2c4ade47b7fa7ada3c07820	addColumn tableName=kb_work_note		\N	3.7.0	\N	\N	4141154494
notifaction_dateend_fix	atolomei	scripts/v5.4.5.xml	2019-11-18 23:30:59.090877	36	EXECUTED	8:4291fd51b801dd281a06ccbd8962d29a	sql		\N	3.7.0	\N	\N	4141154494
notifaction_index_fix	atolomei	scripts/v5.4.5.xml	2019-11-18 23:30:59.128636	37	EXECUTED	8:739395d2af093bb3e4a9adf34dad74d7	sql		\N	3.7.0	\N	\N	4141154494
email_templates_alerts	atolomei	scripts/v5.4.5.xml	2019-11-18 23:30:59.156158	38	EXECUTED	8:506d6951731580e4934680f9af31be63	sql		\N	3.7.0	\N	\N	4141154494
email_templates_alerts_2	atolomei	scripts/v5.4.5.xml	2019-11-18 23:30:59.165315	39	EXECUTED	8:794712ed3138cfc6ad9e89ad0f59be7a	sql		\N	3.7.0	\N	\N	4141154494
pivot_reason_update	grodriguez	scripts/v5.4.5.xml	2019-11-18 23:31:43.96637	40	EXECUTED	8:084a733e86f54c0f4cd538808868ec64	sql		\N	3.7.0	\N	\N	4141154494
cron_job_delete_api_log_event_2_days	grodriguez	scripts/v5.4.5.xml	2019-11-18 23:31:43.98951	41	EXECUTED	8:247c5d5830fe372051bf32559f161c6a	sql		\N	3.7.0	\N	\N	4141154494
dataset_alias	atolomei	scripts/v5.4.5.xml	2019-11-18 23:31:44.02914	42	EXECUTED	8:6ffa13f11a02eaecac8f23447854e16f	sql		\N	3.7.0	\N	\N	4141154494
cron_job_delete_api_log_event_2_days_v2	atolomei	scripts/v5.4.5.xml	2019-11-18 23:31:44.040894	43	EXECUTED	8:0f57085d4b26b59a3ea60b56240b339d	sql		\N	3.7.0	\N	\N	4141154494
classifier_pmc	atolomei	scripts/v5.4.5.xml	2019-11-18 23:31:44.055866	44	EXECUTED	8:764f4703865594627ed6caab29f273c9	sql		\N	3.7.0	\N	\N	4141154494
logevent_1	atolomei	scripts/v5.4.5.xml	2019-11-18 23:34:04.136945	45	EXECUTED	8:e1c7a70df3ddc70eaba2d7053fd6a9b6	sql		\N	3.7.0	\N	\N	4141154494
predicate_type	atolomei	scripts/v5.4.5.xml	2019-11-18 23:34:04.147603	46	EXECUTED	8:6030d58388aaecd2a4c81337765615fa	sql		\N	3.7.0	\N	\N	4141154494
predicate_compliance_manager	atolomei	scripts/v5.4.5.xml	2019-11-18 23:34:04.158119	47	EXECUTED	8:dc1e40d91b9740001461fb5b7f12e3c7	sql		\N	3.7.0	\N	\N	4141154494
kb_subscription_if_not_exists_v2	atolomei	scripts/v5.4.5.xml	2019-11-18 23:34:04.170116	48	EXECUTED	8:72998d35ff9a8d282a85f28f38498a28	sql		\N	3.7.0	\N	\N	4141154494
kb_language_string_es_2	atolomei	scripts/v5.4.5.xml	2019-11-18 23:34:04.195545	49	EXECUTED	8:0296cd904d1356b1930aba6c87f4f89c	sql		\N	3.7.0	\N	\N	4141154494
kb_ logevent_audit_resource	atolomei	scripts/v5.4.5.xml	2019-11-18 23:34:04.206771	50	EXECUTED	8:a738df33ae5e19e90665ed4fe6314d03	sql		\N	3.7.0	\N	\N	4141154494
cron_job_delete_api_log_event_2_days_v3	atolomei	scripts/v5.4.5.xml	2019-11-18 23:34:04.221013	51	EXECUTED	8:e2048eb5436cc34f4dcdfc3ee2152e9f	sql		\N	3.7.0	\N	\N	4141154494
send_email_event_1	atolomei	scripts/v5.4.5.xml	2019-11-18 23:34:04.229202	52	EXECUTED	8:ef72f4eb052ef55f92dc0a29bb7092ea	sql		\N	3.7.0	\N	\N	4141154494
default_preferences_domain_1	atolomei	scripts/v5.4.5.xml	2019-11-18 23:34:04.325527	53	EXECUTED	8:c416bbe5056da460489379b8395e1f66	sql		\N	3.7.0	\N	\N	4141154494
action_rules_job_delete	atolomei	scripts/v5.4.5.xml	2019-11-18 23:34:04.330836	54	EXECUTED	8:f5e4c9bd1c9eb4466fd31fcbbc33a456	sql		\N	3.7.0	\N	\N	4141154494
action_rules_job_0	atolonmei	scripts/v5.4.5.xml	2019-11-18 23:34:04.338664	55	EXECUTED	8:1a2eef95ebecf943886b2bc408d91205	sql		\N	3.7.0	\N	\N	4141154494
object_property_domain_1	atolomei	scripts/v5.4.5.xml	2019-11-18 23:34:04.370783	56	EXECUTED	8:e806d1bb8fda06a7db7b7e3eef448885	sql		\N	3.7.0	\N	\N	4141154494
ping_1	atolomei	scripts/v5.4.5.xml	2019-11-18 23:34:04.380272	57	EXECUTED	8:cb0216fbfeaa400a73b49fb7e639a98d	sql		\N	3.7.0	\N	\N	4141154494
reverseof	aferraria	scripts/v5.4.5.xml	2019-11-18 23:34:04.388397	58	EXECUTED	8:aea1bb52f8e9ed4b41236d60f85e2035	sql		\N	3.7.0	\N	\N	4141154494
certification_report_index	grodriguez	scripts/v5.4.5.xml	2019-11-18 23:34:06.666626	59	EXECUTED	8:24d07b91a457ad9690cc0c56abed9a50	sql		\N	3.7.0	\N	\N	4141154494
reports_kb_language_string_en	grodriguez	scripts/v5.4.5.xml	2019-11-18 23:34:06.688729	60	EXECUTED	8:6dca9d5da74242111acce7a3a1b4e0dc	sql		\N	3.7.0	\N	\N	4141154494
reports_kb_language_string_en_2	grodriguez	scripts/v5.4.5.xml	2019-11-18 23:34:06.701972	61	EXECUTED	8:34a7c4f00d83c92f131928180e3e615e	sql		\N	3.7.0	\N	\N	4141154494
IDX_WF_ACTIVITY_PROCESS_ID	grodriguez	scripts/v5.4.5.xml	2020-03-18 23:28:57.437358	62	EXECUTED	8:0705531f3de9d22f63c21d618954378f	sql		\N	3.7.0	\N	\N	4592137395
CRON_LAST_EXECUTION_DATE	grodriguez	scripts/v5.4.5.xml	2020-03-18 23:28:57.572399	63	EXECUTED	8:7d64e019d48baf0e6ead640b6432a4e1	addColumn tableName=kb_cronjob; addColumn tableName=kb_cronjob		\N	3.7.0	\N	\N	4592137395
reports_string_1_po_sitelogin_1	atolomei	scripts/v5.4.5.xml	2020-03-18 23:28:57.595831	64	EXECUTED	8:27dc33f6957158db33ff651822f75997	sql		\N	3.7.0	\N	\N	4592137395
reports_string_2	atolomei	scripts/v5.4.5.xml	2020-03-18 23:28:57.608156	65	EXECUTED	8:715a41d069dae9079f64de9990832386	sql		\N	3.7.0	\N	\N	4592137395
reports_string_3	atolomei	scripts/v5.4.5.xml	2020-03-18 23:28:57.618729	66	EXECUTED	8:36c0e8fab9005da7a00dee814b3101ec	sql		\N	3.7.0	\N	\N	4592137395
reports_content_long_id	atolomei	scripts/v5.4.5.xml	2020-03-18 23:28:57.627867	67	EXECUTED	8:c2b567cc8c77f1908b8dad4d4146652d	sql		\N	3.7.0	\N	\N	4592137395
scheduler_execute_on	atolomei	scripts/v5.4.5.xml	2020-03-18 23:28:57.638346	68	EXECUTED	8:83aec2352c41516780d4dc3e67324e45	sql		\N	3.7.0	\N	\N	4592137395
attribute_searcheable	aferraria	scripts/v5.4.5.xml	2020-03-18 23:28:57.675545	69	EXECUTED	8:17cfacbcd1ac5ac5ddb3d95eca8d4302	sql		\N	3.7.0	\N	\N	4592137395
classifier_searcheable	aferraria	scripts/v5.4.5.xml	2020-03-18 23:28:57.715055	70	EXECUTED	8:adbeb524e7cf7c363f6ba650c1db7de0	sql		\N	3.7.0	\N	\N	4592137395
securedset_classifier	aferraria	scripts/v5.4.5.xml	2020-03-18 23:28:57.759304	71	EXECUTED	8:9ac7c0d4deb01235d3aecfdc0da775ee	sql		\N	3.7.0	\N	\N	4592137395
reports_string_4	atolomei	scripts/v5.4.5.xml	2020-03-18 23:28:57.776203	72	EXECUTED	8:b9c8c6d41dd99de6ad21cda8445c50f2	sql		\N	3.7.0	\N	\N	4592137395
windsor_submission	aferraria	scripts/v5.4.5.xml	2020-03-18 23:28:57.782673	73	EXECUTED	8:b8ca30ee495f5c10ced252661a1ba2fb	sql		\N	3.7.0	\N	\N	4592137395
attribute_sortable	atolomei	scripts/v5.4.5.xml	2020-03-18 23:28:57.809864	74	EXECUTED	8:8c564495cc50710b324a771d337a8880	sql		\N	3.7.0	\N	\N	4592137395
workflow_alerts_job	aferraria	scripts/v5.4.5.xml	2020-03-18 23:28:57.817208	75	EXECUTED	8:c22f64d6c955f1e96c004298d96f21ce	sql		\N	3.7.0	\N	\N	4592137395
enoti_rule_role	aferraria	scripts/v5.4.5.xml	2020-03-18 23:28:57.833574	76	EXECUTED	8:c93eadbf72604f58ebd4ed3e3915fcad	sql		\N	3.7.0	\N	\N	4592137395
subsection_template	aferraria	scripts/v5.4.5.xml	2020-03-18 23:28:57.845962	77	EXECUTED	8:5032a667f03d3faa3ef2c04afb900929	sql		\N	3.7.0	\N	\N	4592137395
contentresource	aferraria	scripts/v5.4.5.xml	2020-03-18 23:31:46.9209	78	EXECUTED	8:4ab211c086f88f5d5fd4a5fce374c0bd	sql		\N	3.7.0	\N	\N	4592137395
userprofile	aferraria	scripts/v5.4.5.xml	2020-03-18 23:31:47.016755	79	EXECUTED	8:44b7b9fb43991d98e94f045456ff8d91	sql		\N	3.7.0	\N	\N	4592137395
userprofile_rename	aferraria	scripts/v5.4.5.xml	2020-03-18 23:31:47.022779	80	EXECUTED	8:774002e4d1b738071dbed37bf7d1ba9d	sql		\N	3.7.0	\N	\N	4592137395
notification_rule_id_2	atolomei	scripts/v5.4.5.xml	2020-03-18 23:31:47.034647	81	EXECUTED	8:cb713ee862135c278dfac1ed86a0002f	sql		\N	3.7.0	\N	\N	4592137395
form_template	aferraria	scripts/v5.4.5.xml	2020-03-18 23:31:47.059914	82	EXECUTED	8:ad4e24e35a37e69bea89fae846da3c3f	sql		\N	3.7.0	\N	\N	4592137395
notification_rule_1	atolomei	scripts/v5.4.5.xml	2020-03-18 23:31:47.077331	83	EXECUTED	8:11359cffdaa9dffbaf57a65c6d6e47a7	sql		\N	3.7.0	\N	\N	4592137395
scheduler index execution_after	atolomei	scripts/v5.4.5.xml	2020-03-18 23:31:47.096634	84	EXECUTED	8:7a96f8d2cb6b3bb61ba656719ec6179d	sql		\N	3.7.0	\N	\N	4592137395
scheduler command_classname_1	atolomei	scripts/v5.4.5.xml	2020-03-18 23:31:47.107839	85	EXECUTED	8:156b27fd41fcfc12cb7c87979dbd5826	sql		\N	3.7.0	\N	\N	4592137395
createemailtemplates	atolomei	scripts/v5.4.5.xml	2020-03-18 23:31:47.117152	86	EXECUTED	8:ae4e077026526f8ce0323074e71f55e7	sql		\N	3.7.0	\N	\N	4592137395
event_filesource	atolomei	scripts/v5.4.5.xml	2020-03-18 23:31:47.132164	87	EXECUTED	8:93530d418affc4d65e14bf850a988bbb	sql		\N	3.7.0	\N	\N	4592137395
contentresource group id	atolomei	scripts/v5.4.5.xml	2020-03-18 23:31:47.143984	88	EXECUTED	8:2ffdfeb888f173190e83b45a25a6f082	sql		\N	3.7.0	\N	\N	4592137395
resourcegroup1	atolomei	scripts/v5.4.5.xml	2020-03-18 23:31:47.206316	89	EXECUTED	8:93c3e7472d5676dca77dcc80e74ca447	sql		\N	3.7.0	\N	\N	4592137395
kb_classifiertemplate position_1	atolomei	scripts/v5.4.5.xml	2020-03-18 23:31:47.216307	90	EXECUTED	8:5c0847b9f5ef2e240366e680e8c8408f	sql		\N	3.7.0	\N	\N	4592137395
kb_ds_element_template position_1	atolomei	scripts/v5.4.5.xml	2020-03-18 23:31:47.228471	91	EXECUTED	8:e6adc7abb2e160e332d875524ccb06b6	sql		\N	3.7.0	\N	\N	4592137395
kb_ds_element_template position_2	atolomei	scripts/v5.4.5.xml	2020-03-18 23:31:49.931389	92	EXECUTED	8:632169e9ad11cac336ea828273c34587	sql		\N	3.7.0	\N	\N	4592137395
kb_classifiertemplate position_1_update	grodriguez	scripts/v5.4.5.xml	2020-03-18 23:31:49.936737	93	EXECUTED	8:4538dc5932a0b1c19cdc216445709ad0	sql		\N	3.7.0	\N	\N	4592137395
fix_pivot_delete_endtime	grodriguez	scripts/v5.4.5.xml	2020-03-18 23:35:31.11954	94	EXECUTED	8:7ee360a4e6a708a74dcbd9feb522d5db	sql		\N	3.7.0	\N	\N	4592137395
user pwd last	atolomei	scripts/v5.4.5.xml	2020-03-18 23:35:31.130059	95	EXECUTED	8:4b83695c777ba4fac14af984b86bce3c	sql		\N	3.7.0	\N	\N	4592137395
user pwd last update	atolomei	scripts/v5.4.5.xml	2020-03-18 23:35:31.322948	96	EXECUTED	8:daf0076eb6d95f545080d2896aefbe81	sql		\N	3.7.0	\N	\N	4592137395
versionTag	atolomei	scripts/v6.1-preflight.xml	2020-04-30 17:40:42.331202	97	EXECUTED	8:4a52a9471f7ccb3299c6a30aa6c2cefc	tagDatabase		5.6.1	3.7.0	\N	\N	8286442280
facet view mode	aferraria	scripts/v6.1-preflight.xml	2020-04-30 17:40:42.635241	98	EXECUTED	8:8dd230823ff5908a7652fe5ab459dfab	sql		\N	3.7.0	\N	\N	8286442280
kb_usage_stat s3	atolomei	scripts/v6.1-preflight.xml	2020-04-30 17:40:42.862741	99	EXECUTED	8:a016a1d1736eff9b5882206749ce977d	sql		\N	3.7.0	\N	\N	8286442280
kb_contenttemplate contentclasscode	atolomei	scripts/v6.1-preflight.xml	2020-04-30 17:40:42.899249	100	EXECUTED	8:f6a7ddc703cd3a050bea5962f86724f2	sql		\N	3.7.0	\N	\N	8286442280
rs_windsor_activity_pivot	atolomei	scripts/v6.1-preflight.xml	2020-04-30 17:40:42.90947	101	EXECUTED	8:1ce61dfc82e8ef874a7ee904373bf007	sql		\N	3.7.0	\N	\N	8286442280
kresource oid	atolomei	scripts/v6.1-preflight.xml	2020-04-30 17:40:42.924887	102	EXECUTED	8:c397e11e76f34e32805f82ee58b8295a	sql		\N	3.7.0	\N	\N	8286442280
Kfile_isencrypted	grodriguez	scripts/v6.1-preflight.xml	2020-04-30 17:42:26.407139	103	EXECUTED	8:da1bf78ccb707bab808104f8513cbcd1	sql		\N	3.7.0	\N	\N	8286442280
Kfile_isencrypted_init	grodriguez	scripts/v6.1-preflight.xml	2020-04-30 17:45:53.666299	104	EXECUTED	8:ca87a83e767b7202436a2b21ed35d0c9	sql		\N	3.7.0	\N	\N	8286442280
kb_tree_file_oid	atolomei	scripts/v6.1-preflight.xml	2020-04-30 17:45:53.691277	105	EXECUTED	8:e1a488edc9f0672369c0d615a767698d	sql		\N	3.7.0	\N	\N	8286442280
kb_attribute rule_condition	aferraria	scripts/v6.1-preflight.xml	2020-04-30 17:45:53.758384	106	EXECUTED	8:224aa68fa00d705ab94e1dc8596d32d5	sql		\N	3.7.0	\N	\N	8286442280
kresource update oid2	atolomei	scripts/v6.1-preflight.xml	2020-04-30 17:52:56.655743	107	EXECUTED	8:6d3e01dfae59cfb3ce284cc1212819d2	sql		\N	3.7.0	\N	\N	8286442280
kb_tree_file oid2	atolomei	scripts/v6.1-preflight.xml	2020-04-30 17:52:56.790206	108	EXECUTED	8:9ab024c8c4c5810291c27bb2e051a39b	sql		\N	3.7.0	\N	\N	8286442280
cl default structure	atolomei	scripts/v6.1-preflight.xml	2020-04-30 17:52:56.833513	109	EXECUTED	8:c5bc0454598d3f005689a4309f061eb4	sql		\N	3.7.0	\N	\N	8286442280
at default structure	atolomei	scripts/v6.1-preflight.xml	2020-04-30 17:52:56.854257	110	EXECUTED	8:3dbc5ab1cfa5903b5b61780a7700b39b	sql		\N	3.7.0	\N	\N	8286442280
default structure search	atolomei	scripts/v6.1-preflight.xml	2020-04-30 17:52:56.919074	111	EXECUTED	8:f7fe339dc45eba1985d8be251a618953	sql		\N	3.7.0	\N	\N	8286442280
kb_classifier access_strategy 4	atolomei	scripts/v6.1-preflight.xml	2020-04-30 17:52:56.946609	112	EXECUTED	8:c7e0ceede617516f414a0106e5d84a9d	sql		\N	3.7.0	\N	\N	8286442280
dataset access_strategy	aferraria	scripts/v6.1-preflight.xml	2020-04-30 17:52:56.984554	113	EXECUTED	8:fc22519fc5bb37e5c354e9c46cce27f0	sql		\N	3.7.0	\N	\N	8286442280
wf_launcher structure 1	atolomei	scripts/v6.1-preflight.xml	2020-04-30 17:52:57.024566	114	EXECUTED	8:c5fac7c01c025ef0ff6a73b403ddc0b6	sql		\N	3.7.0	\N	\N	8286442280
kb_aclentry permissions 2048	atolomei	scripts/v6.1-preflight.xml	2020-04-30 17:52:57.270884	115	EXECUTED	8:970b58e861b3c39180390e3287ea99a3	sql		\N	3.7.0	\N	\N	8286442280
kb_user_list_2	atolomei	scripts/v6.1-preflight.xml	2020-04-30 17:52:57.336927	116	EXECUTED	8:4b4f2d41993d21e15fae5225bfa7db9c	sql		\N	3.7.0	\N	\N	8286442280
list item 2	atolomei	scripts/v6.1-preflight.xml	2020-04-30 17:52:57.406037	117	EXECUTED	8:5cddd7a5ee5fbf330afe3ad930daecf0	sql		\N	3.7.0	\N	\N	8286442280
savedq 3	atolomei	scripts/v6.1-preflight.xml	2020-04-30 17:52:57.493969	118	EXECUTED	8:7e39851bb89c0b8bd5484a11a21abfd8	sql		\N	3.7.0	\N	\N	8286442280
kb_tree_file version	aferraria	scripts/v6.1-preflight.xml	2020-04-30 17:52:57.502493	119	EXECUTED	8:f592bcf31b06f47d33c41d4e0af08741	sql		\N	3.7.0	\N	\N	8286442280
domain_encrypt_files	grodriguez	scripts/v6.1-preflight.xml	2020-04-30 17:52:57.543411	120	EXECUTED	8:edb0b85ba7cdbba5b54870cce644f231	sql		\N	3.7.0	\N	\N	8286442280
index user list	atolomei	scripts/v6.1-preflight.xml	2020-04-30 17:52:57.564211	121	EXECUTED	8:4ebaf56bffe1fd02a8bad7d164bf6373	sql		\N	3.7.0	\N	\N	8286442280
user list version match	atolomei	scripts/v6.1-preflight.xml	2020-04-30 17:52:57.588629	122	EXECUTED	8:31a15264cd4777036c25af8563a689e6	sql		\N	3.7.0	\N	\N	8286442280
user list item console owner 2	atolomei	scripts/v6.1-preflight.xml	2020-04-30 17:52:57.600692	123	EXECUTED	8:2a6dcee947a9968fd933562337ce496e	sql		\N	3.7.0	\N	\N	8286442280
user list item index for grid	atolomei	scripts/v6.1-preflight.xml	2020-04-30 17:52:57.611756	124	EXECUTED	8:60a24d6d302a8d225a9a1a3d5e3ea49d	sql		\N	3.7.0	\N	\N	8286442280
saved query 1	atolomei	scripts/v6.1-preflight.xml	2020-04-30 17:52:57.777356	125	EXECUTED	8:42766017c6c17c74795d306966afc0e9	sql		\N	3.7.0	\N	\N	8286442280
user list item oid	atolomei	scripts/v6.1-preflight.xml	2020-04-30 17:52:57.793991	126	EXECUTED	8:16d4baec61b25652b6181ea42db1f132	sql		\N	3.7.0	\N	\N	8286442280
uitheme	atolomei	scripts/v6.1-preflight.xml	2020-04-30 17:52:57.832268	127	EXECUTED	8:0fa884422e7c5c228df5e4a784e6641f	sql		\N	3.7.0	\N	\N	8286442280
locale	atolomei	scripts/v6.1-preflight.xml	2020-04-30 17:52:57.849729	128	EXECUTED	8:2651ab62daa0eb682ac274e2cc84e323	sql		\N	3.7.0	\N	\N	8286442280
reverse classifier	aferraria	scripts/v6.1-preflight.xml	2020-04-30 17:52:57.863353	129	EXECUTED	8:fa4bb313e1f89ba54f932cf3116e23d0	sql		\N	3.7.0	\N	\N	8286442280
versionTag	atolomei	scripts/v6.1.xml	2020-04-30 17:52:57.866861	130	EXECUTED	8:4a52a9471f7ccb3299c6a30aa6c2cefc	tagDatabase		5.6.1	3.7.0	\N	\N	8286442280
user pwd last update_1	atolomei	scripts/v6.1.xml	2020-04-30 17:52:57.874853	131	EXECUTED	8:3d244bbb3d8bd3b1525cb5e588713b8c	sql		\N	3.7.0	\N	\N	8286442280
cronjob_health_check_8	atolomei	scripts/v6.1.xml	2020-04-30 17:52:57.892547	132	EXECUTED	8:bc21fdf57853086caf7a85275ddbe9ac	sql		\N	3.7.0	\N	\N	8286442280
rename resourcegroup	aferraria	scripts/v6.1.xml	2020-04-30 17:52:57.902012	133	EXECUTED	8:f4a69df332ab30e64886f3d412064d5e	sql		\N	3.7.0	\N	\N	8286442280
resourcegroup type_1	atolomei	scripts/v6.1.xml	2020-04-30 17:52:57.909788	134	EXECUTED	8:086753ea0900b8c33f83b595003ea926	sql		\N	3.7.0	\N	\N	8286442280
contenttemplate_alias_1	atolomei	scripts/v6.1.xml	2020-04-30 17:52:57.931595	135	EXECUTED	8:dd1dccc75e2e839fbe8158e1206114b4	sql		\N	3.7.0	\N	\N	8286442280
savedquery index2	atolomei	scripts/v6.1.xml	2020-04-30 17:52:57.949838	136	EXECUTED	8:3a31d682fe8b5db769b442158c24f8b0	sql		\N	3.7.0	\N	\N	8286442280
savedquery userprofile	atolomei	scripts/v6.1.xml	2020-04-30 17:52:57.960299	137	EXECUTED	8:df8122fcee9a58e92f8e0d27a0898aa6	sql		\N	3.7.0	\N	\N	8286442280
user list item content class 1	aferraria	scripts/v6.1.xml	2020-04-30 17:52:57.970356	138	EXECUTED	8:e35c9889ba141247f0371bb95aa03407	sql		\N	3.7.0	\N	\N	8286442280
portal admin area	aferraria	scripts/v6.1.xml	2020-04-30 17:52:57.991539	139	EXECUTED	8:c06b26d519c2d50eba6314a8dcc2f5a0	sql		\N	3.7.0	\N	\N	8286442280
library criteria	atolomei	scripts/v6.1.xml	2020-04-30 17:52:58.00168	140	EXECUTED	8:d9a9969a2bc26da39991a7526c5329d3	sql		\N	3.7.0	\N	\N	8286442280
versionTag	aferraria	scripts/v6.2-preflight.xml	2020-04-30 17:52:58.004738	141	EXECUTED	8:cdc6ba4452922b306e3feea2d4bc77d2	tagDatabase		5.6.2	3.7.0	\N	\N	8286442280
attribute validator	aferraria	scripts/v6.2-preflight.xml	2020-04-30 17:52:58.012622	142	EXECUTED	8:2ba95aa1bd2cf42c526e544cca56da20	sql		\N	3.7.0	\N	\N	8286442280
versionTag	aferraria	scripts/v6.2.xml	2020-04-30 17:52:58.015457	143	EXECUTED	8:cdc6ba4452922b306e3feea2d4bc77d2	tagDatabase		5.6.2	3.7.0	\N	\N	8286442280
resourcegroup fallo en deploy anterior	atolomei	scripts/v6.2.xml	2020-04-30 17:52:58.032967	144	EXECUTED	8:acd92e17edae634ca94448d6a253cdab	sql		\N	3.7.0	\N	\N	8286442280
default pwd1	atolomei	scripts/v6.2.xml	2020-04-30 17:52:58.042488	145	EXECUTED	8:d1ad6626435df0dc0b01b8df7b08afe3	sql		\N	3.7.0	\N	\N	8286442280
resourcegroup type_2	atolomei	scripts/v6.1-preflight.xml	2020-04-30 18:03:17.791999	146	EXECUTED	8:acd92e17edae634ca94448d6a253cdab	sql		\N	3.7.0	\N	\N	8287797728
userproperty	atolomei	scripts/v6.2.xml	2020-05-01 03:13:19.779204	147	EXECUTED	8:2dd07cfded5a45b80a7bf6e841c23d4f	sql		\N	3.7.0	\N	\N	8320799680
userprofile isclient	atolomei	scripts/v6.2-preflight.xml	2020-05-06 02:23:35.833509	148	EXECUTED	8:c1ef443d4059a03ce4ce4a2c0fc5f123	sql		\N	3.7.0	\N	\N	8749815650
relation order	aferraria	scripts/v6.2-preflight.xml	2020-05-11 02:57:11.386886	149	EXECUTED	8:2479addb30b0a47a7d4345f7936e1400	sql		\N	3.7.0	\N	\N	9183831232
launchergroup 3	atolomei	scripts/v6.2-preflight.xml	2020-05-11 02:57:11.498862	150	EXECUTED	8:c8bb873577cb40104d4a88431c4b2a12	sql		\N	3.7.0	\N	\N	9183831232
launcher group visible	atolomei	scripts/v6.2-preflight.xml	2020-05-13 02:51:12.082709	151	EXECUTED	8:4b23471205c093e761157f7dc5fd50f9	sql		\N	3.7.0	\N	\N	9356271998
launcher group items	atolomei	scripts/v6.2-preflight.xml	2020-05-13 02:51:12.111528	152	EXECUTED	8:7075204cc9b223060c0a44391a53e7b0	sql		\N	3.7.0	\N	\N	9356271998
com.novamens.content.contentclass.parentsenabled 1	atolomei	scripts/v6.2.xml	2020-05-13 02:51:12.138538	153	EXECUTED	8:b763f84af790c257a921bb42c3625130	sql		\N	3.7.0	\N	\N	9356271998
classification datasetmember_id idx	grodriguez	scripts/v6.2-preflight.xml	2020-05-14 23:13:45.276893	154	EXECUTED	8:f23de1dc18c13a565f86e7d0c34d713a	sql		\N	3.7.0	\N	\N	9516017473
onlyRootEdit	atolomei	scripts/v6.2-preflight.xml	2020-05-14 23:13:45.39838	155	EXECUTED	8:e11080aeae0962274b8d486207e58cf3	sql		\N	3.7.0	\N	\N	9516017473
kbee_stats_bill	grodriguez	scripts/v6.2.xml	2020-05-14 23:13:45.412245	156	EXECUTED	8:c659fda3a4194242b0a07bab59d713fb	sql		\N	3.7.0	\N	\N	9516017473
user_bill	grodriguez	scripts/v6.2.xml	2020-05-14 23:13:45.499456	157	EXECUTED	8:5430fe680301133937cc44e23db5b67c	sql		\N	3.7.0	\N	\N	9516017473
kbee_stats_bill_default2	grodriguez	scripts/v6.2.xml	2020-05-14 23:13:45.682354	158	EXECUTED	8:6258104df4d41507f40e0c5f734b7b95	sql		\N	3.7.0	\N	\N	9516017473
kbee_stats_3	atolomei	scripts/v6.2.xml	2020-05-15 00:42:21.775519	159	EXECUTED	8:adf83232c41418270884fddc0d80853c	sql		\N	3.7.0	\N	\N	9521341682
Kb_Model_Section isportal	atolomei	scripts/v6.2-preflight.xml	2020-05-20 01:37:04.944996	160	EXECUTED	8:47225553bf947461275d0d3be4d03d56	sql		\N	3.7.0	\N	\N	9956624867
attribute parent	aferraria	scripts/v6.2-preflight.xml	2020-05-26 02:09:23.413112	161	EXECUTED	8:2a1c0f9bfd313de78198638018dbf852	sql		\N	3.7.0	\N	\N	0476963302
launcher group items_2	atolomei	scripts/v6.2-preflight.xml	2020-06-01 15:57:42.250013	162	EXECUTED	8:3c6dd0727a82470055d4c01b42114914	sql		\N	3.7.0	\N	\N	1045062169
rs_windsor_activity_pivot team	grodriguez	scripts/v6.2.xml	2020-06-17 13:20:37.571717	163	EXECUTED	8:0915300487c5725c03aacbb2326ef716	sql		\N	3.7.0	\N	\N	2418037498
rs_windsor_activity_pivot team update	grodriguez	scripts/v6.2.xml	2020-06-17 13:20:37.582841	164	EXECUTED	8:0915300487c5725c03aacbb2326ef716	sql		\N	3.7.0	\N	\N	2418037498
rs_windsor_activity_pivot team update3	grodriguez	scripts/v6.2.xml	2020-06-22 14:51:07.329251	165	EXECUTED	8:ef5613f5c14c968e7bd766a3274f1613	sql		\N	3.7.0	\N	\N	2855347496
restore compliance file alias	grodriguez	scripts/v6.2.xml	2020-06-22 14:51:07.36122	166	EXECUTED	8:5d5191e192dc6c7d2d630363692666a0	sql		\N	3.7.0	\N	\N	2855347496
rs_windsor_activity_pivot restore compliance file alias	grodriguez	scripts/v6.2.xml	2020-06-22 14:51:15.192406	167	EXECUTED	8:68327c544cb76110d538c8879d147788	sql		\N	3.7.0	\N	\N	2855347496
rs_content_pivot2	grodriguez	scripts/v6.2.xml	2020-07-01 02:39:05.475527	168	EXECUTED	8:c67c88a4000a1d587780569079c60bef	sql		\N	3.7.0	\N	\N	3589145344
rs_user_pivot2	grodriguez	scripts/v6.2.xml	2020-07-01 02:39:05.496695	169	EXECUTED	8:01b1d0cc2bf477c7ba6ada45f5a42d51	sql		\N	3.7.0	\N	\N	3589145344
rs_windsor_user_stat3	grodriguez	scripts/v6.2.xml	2020-07-01 02:39:05.510734	170	EXECUTED	8:4a4111a8dba648a8f3bdac3fc7691057	sql		\N	3.7.0	\N	\N	3589145344
rs_windsor_user_stat cronjob	grodriguez	scripts/v6.2.xml	2020-07-01 02:39:05.524354	171	EXECUTED	8:578a8c83b53e6440721ce3409719f014	sql		\N	3.7.0	\N	\N	3589145344
rs_windsor_user_stat cronjob2	grodriguez	scripts/v6.2.xml	2020-07-01 02:54:46.674699	172	EXECUTED	8:5da35eb9b7f0c69a4c522473929848e9	sql		\N	3.7.0	\N	\N	3590086599
rs_windsor_user_stat update5	grodriguez	scripts/v6.2.xml	2020-07-01 03:07:15.865488	173	EXECUTED	8:d9c1401e84445d98b52ea34c3ced553d	sql		\N	3.7.0	\N	\N	3590787742
rs_windsor_user_stat cronjob3	grodriguez	scripts/v6.2.xml	2020-07-01 03:07:15.885312	174	EXECUTED	8:8b3d64b22bba706f25ae549488cd7732	sql		\N	3.7.0	\N	\N	3590787742
rs_windsor_user_stat cronjob5	grodriguez	scripts/v6.2.xml	2020-07-03 13:39:09.541546	175	EXECUTED	8:76090206f0b483c69d0ee49b80adea63	sql		\N	3.7.0	\N	\N	3801549447
rs_windsor_activity_pivot business_hours	grodriguez	scripts/v6.2.xml	2020-07-03 13:39:09.567602	176	EXECUTED	8:f4bcea6a5eabec45266ecb886c9b2451	sql		\N	3.7.0	\N	\N	3801549447
rs_windsor_user_stat drop	grodriguez	scripts/v6.2.xml	2020-07-03 13:39:09.605428	177	EXECUTED	8:d985c18e12e34c5c9c6bf25e3cef18e8	sql		\N	3.7.0	\N	\N	3801549447
rs_windsor_user_stat5	grodriguez	scripts/v6.2.xml	2020-07-03 13:39:09.668532	178	EXECUTED	8:a4d665c9a880c737d0e960f6a357a4b4	sql		\N	3.7.0	\N	\N	3801549447
rs_windsor_user_stat cronjob6	grodriguez	scripts/v6.2.xml	2020-07-06 08:13:01.159761	179	EXECUTED	8:8988680e2f5086154256e461a7f8a5ee	sql		\N	3.7.0	\N	\N	4041181077
rs_windsor_user_stat drop6	grodriguez	scripts/v6.2.xml	2020-07-06 08:13:01.200886	180	EXECUTED	8:d985c18e12e34c5c9c6bf25e3cef18e8	sql		\N	3.7.0	\N	\N	4041181077
rs_windsor_user_stat6	grodriguez	scripts/v6.2.xml	2020-07-06 08:13:01.245692	181	EXECUTED	8:c370a78c854eb9bf24e4ae1ace0b447d	sql		\N	3.7.0	\N	\N	4041181077
rs_windsor_user_stat cronjob11	grodriguez	scripts/v6.2.xml	2020-07-07 10:34:41.008182	182	EXECUTED	8:3e22cab4ca82c44ecd813590064e81f7	sql		\N	3.7.0	\N	\N	4136080911
rs_windsor_user_stat drop11	grodriguez	scripts/v6.2.xml	2020-07-07 10:34:41.048951	183	EXECUTED	8:d985c18e12e34c5c9c6bf25e3cef18e8	sql		\N	3.7.0	\N	\N	4136080911
rs_windsor_user_stat11	grodriguez	scripts/v6.2.xml	2020-07-07 10:34:41.106911	184	EXECUTED	8:c3ad55801f4d372e7fcc8fd60a0d69dd	sql		\N	3.7.0	\N	\N	4136080911
rs_windsor_user_stat cronjob12	grodriguez	scripts/v6.2.xml	2020-07-14 13:30:38.44292	185	EXECUTED	8:a3bc454f272f63875951633ced8aca85	sql		\N	3.7.0	\N	\N	4751438350
Update traffictokens parameter name 	grodriguez	scripts/v6.2.xml	2020-07-31 15:33:07.825611	186	EXECUTED	8:e6a035432d3a78523a02974ee287037a	sql		\N	3.7.0	\N	\N	6227587763
versionTag	atolomei	scripts/v6.3.xml	2020-07-31 15:33:07.838208	187	EXECUTED	8:0603a23901ce130940d420d885ba2d23	tagDatabase		6.3	3.7.0	\N	\N	6227587763
launcher group	atolomei	scripts/v6.3.xml	2020-07-31 15:33:07.854708	188	EXECUTED	8:17441c51a5a6d8fb17d363e84a95c74c	sql		\N	3.7.0	\N	\N	6227587763
po_area parent	atolomei	scripts/v6.3.xml	2020-07-31 15:33:07.940293	189	EXECUTED	8:66059ebd4d5a19c7ddbfea6abe8f5e54	sql		\N	3.7.0	\N	\N	6227587763
proc desc	atolomei	scripts/v6.3.xml	2020-07-31 15:33:07.957811	190	EXECUTED	8:32f00931993836eef248970ffddb16a8	sql		\N	3.7.0	\N	\N	6227587763
report abstract	atolomei	scripts/v6.3.xml	2020-07-31 15:33:07.980113	191	EXECUTED	8:9fe5d62358bd7c44becc1a959c025b9f	sql		\N	3.7.0	\N	\N	6227587763
kb_language_string text	atolomei	scripts/v6.3.xml	2020-07-31 15:33:08.001114	192	EXECUTED	8:d4d7def86f0e3d23390b29d59c111eb6	sql		\N	3.7.0	\N	\N	6227587763
kb_classifier alias not null	atolomei	scripts/v6.3.xml	2020-07-31 15:33:08.017703	193	EXECUTED	8:f54bc8a3a888fe34f0f7bd7b824d1929	sql		\N	3.7.0	\N	\N	6227587763
kbfile minor	atolomei	scripts/v6.3.xml	2020-07-31 15:35:01.166243	194	EXECUTED	8:a00d0f8962a404d82346660bbde53979	sql		\N	3.7.0	\N	\N	6227587763
kbeeJson Jackson Migration 1	grodriguez	scripts/v6.3.xml	2020-07-31 15:37:47.970082	195	EXECUTED	8:0767768fc4b6e1eab598f3c703b62627	sql		\N	3.7.0	\N	\N	6227587763
kbeeJson Jackson Migration 2	grodriguez	scripts/v6.3.xml	2020-08-03 09:38:51.24552	196	EXECUTED	8:64136277eb13b92663a9f73a9d138284	sql		\N	3.7.0	\N	\N	6465530970
library index	atolomei	scripts/v6.3.xml	2020-08-03 09:40:46.842234	197	EXECUTED	8:e04d00126feb7f72258e95c2ad1c9a72	sql		\N	3.7.0	\N	\N	6465646621
eform data	aferraria	scripts/v6.3.xml	2020-08-03 09:40:47.064755	198	EXECUTED	8:7407d5adac9a0ad62e75eb05a721b39a	sql		\N	3.7.0	\N	\N	6465646621
eform template	aferraria	scripts/v6.3.xml	2020-08-03 09:40:47.106671	199	EXECUTED	8:e92db08c8a18ecd8dd4c8ec658d06988	sql		\N	3.7.0	\N	\N	6465646621
emailvalidated	atolomei	scripts/v6.3.xml	2020-08-17 11:06:14.881695	200	EXECUTED	8:b6ae1fa3f1087e8a8584d9d76bbb21a4	sql		\N	3.7.0	\N	\N	7680374634
versionTag	atolomei	scripts/v6.4.xml	2020-09-04 13:37:35.130488	201	EXECUTED	8:468176c91b19ffe1def912f0c79f8875	tagDatabase		6.4	3.7.0	\N	\N	9237455092
worknote cron expression	atolomei	scripts/v6.4.xml	2020-09-04 13:37:35.14426	202	EXECUTED	8:dcf5292f95bdf2e1615cfeb3f6f6689e	sql		\N	3.7.0	\N	\N	9237455092
kb_cronjob	atolomei	scripts/v6.4.xml	2020-09-04 13:37:35.159391	203	EXECUTED	8:1dc56f505fdaf2709a39cff56f15d2c8	sql		\N	3.7.0	\N	\N	9237455092
kb_cronjob timezone	atolomei	scripts/v6.4.xml	2020-09-04 13:37:35.165179	204	EXECUTED	8:ca476061059514e3fe8134543ffe2578	sql		\N	3.7.0	\N	\N	9237455092
\.
COPY databasechangeloglock (id, locked, lockgranted, lockedby) FROM stdin;
\.
COPY dataset (id, creationdate, lastmodifieddate, lastmodifieduser, state, enabled, domain_id, description, name, alternative_display, type, hierarchical, suggester, group_id, canonical, secured, external_subtype, readonly, external_id, alias, aggregation, classifier_id, access_strategy, onlyroot) FROM stdin;
1	2017-08-30 18:13:28.023877-03	2017-08-30 18:13:28.023877-03	1	1	t	1	\N	Users	\N	4	f	f	\N	f	f	0	f	\N	Users	f	\N	2	f
\.
COPY datasetclassifier (dataset_id, classifier_id) FROM stdin;
\.
COPY datasetmember (id, creationdate, lastmodifieddate, lastmodifieduser, state, domain_id, entity_id, type, alternative_display, strvalue, datevalue, parent, dataset_id, external_id, external_url, attributes, rule_id, group_id, notes, labelcolor, external_member_id, securityrule_id) FROM stdin;
57	2017-08-30 18:13:28.025048-03	2020-04-30 20:37:19.633-03	1	1	1	1	3	\N	\N	\N	\N	1	\N	\N	\N	\N	\N	\N	1	\N	\N
\.
COPY domain (id, lastmodifieddate, lastmodifieduser, state, enabled, email, address, phone, website, name, creationdate, organization, type, service, description, quota, file_reader_directory, password_renew_months, istemplate, maxusers, tipoftheday, lang, cabinet_template, cabinet_kbase, cabinet_external, logo_url, isapienabled, timezone, storagemode, external_id, portal_library, locale_str, logo, encrypt_files, defaultpassword) FROM stdin;
1	2019-10-12 15:36:05.548-03	1	1	t	\N	\N	\N		kbee	2017-08-30 18:13:27.932658-03	RealPage Factory	4	1	RPDM Factory	0	\N	0	f	0	t	en    	t	f	f	\N	t	US/Central	1		f	en    	\N	f	\N
\.
COPY drb_answer (content_id, question_id, date_submitted, date_edited_admin, title, text, user_id, accepted, date_accepted, votes) FROM stdin;
\.
COPY drb_question (content_id, title, text, user_id, votes, num_answers, state, date_edited_admin, date_submitted) FROM stdin;
\.
COPY entity (id, creationdate, lastmodifieddate, lastmodifieduser, state, domain_id) FROM stdin;
1	2017-08-30 18:13:28.014708-03	2020-06-04 03:12:59.025-03	1	1	1
\.
COPY entitymatching (kbee_id, kbee_class_name, lastmodifieddate, class_name, id, url) FROM stdin;
\.
COPY externalresource (resource_id, url, description, in_portal) FROM stdin;
\.
COPY gallery (title, subtitle, description, resource_id, gdate, in_portal) FROM stdin;
\.
COPY galleryfile (gallery_id, file_id, gorder) FROM stdin;
\.
COPY idoc (content_id, title, subtitle, summary, editorialstate, template_id, tree_file_id) FROM stdin;
2	\N	\N	\N	0	\N	\N
\.
COPY idocsection (id, creationdate, lastmodifieddate, lastmodifieduser, state, idoc_id, sectionorder, name, description, attributejson) FROM stdin;
\.
COPY idocsectionresource (section_id, resource_id, "position") FROM stdin;
\.
COPY kb_acl (id, lastmodifieddate, lastmodifieduser, name, creationdate) FROM stdin;
\.
COPY kb_aclentry (acl, principal, permissions, negative, id) FROM stdin;
\.
COPY kb_action_rule (id, creationdate, domain_id, name, display_name, condition, action, description, notes, lastmodifieddate, lastmodifieduser, state, displaycondition) FROM stdin;
\.
COPY kb_api_usage_stat (ts, total, mean_time_total, total_post, mean_time_post, totdel, meantimedel, total_bounced) FROM stdin;
\.
COPY kb_assignable_role (role_id, assignablerole_id) FROM stdin;
\.
COPY kb_attribute (id, creationdate, lastmodifieddate, lastmodifieduser, state, domain_id, name, type, multiplicity, uniquename, korder, iscanonical, metadatasubtitle, visibility, is_api, isfilterable, predicate, alias, inportal, portalsubtitle, description, searchable, sortable, is_rule_condition, default_structure, validator, onlyroot) FROM stdin;
\.
COPY kb_attributetemplate (id, metadatasubtitle, attribute_id, portalsubtitle, korder, multiplicity, section_id, subsection, isvisible, parent_id) FROM stdin;
\.
COPY kb_cabinet (id, domain_id, lastmodifieddate, creationdate, lastmodifieduser, display_name, criteria, state, readonly, reader_group, key, listorder, canonical, name, description) FROM stdin;
1	1	2017-09-02 03:58:05.153-03	2017-09-02 03:58:05.153-03	1	All	\N	1	f	6	all	10	t	\N	\N
\.
COPY kb_cabinet_reader (cabinet_id, group_id) FROM stdin;
\.
COPY kb_classifier (id, creationdate, lastmodifieddate, lastmodifieduser, state, domain_id, base, iscanonical, displayable, semantic, name, uniquename, predicate, multiplicity, is_content_type, mandatory, ordered, korder, dataset_id, dataset2_id, dataset3_id, visibility, metadatasubtitle, is_rule_condition, is_api, alias, key, inportal, portalsubtitle, description, searchable, default_structure, access_strategy, onlyroot) FROM stdin;
\.
COPY kb_classifiertemplate (id, contenttemplate_id, classifier_id, root_id, "position", isvisible, inherited, iscanonical, metadatasubtitle, multiplicity, portalsubtitle, korder, section_id, subsection, parent_id, accessibility, criteria, reverseof_id, reverse) FROM stdin;
\.
COPY kb_comment (content_id, referenced_content_id, commentdate, title, text, user_id, date_submitted, site_id, parent_comment, isfirstlevel, creationdate) FROM stdin;
\.
COPY kb_content_relation (id, source_id, target_id, template_id, "position") FROM stdin;
\.
COPY kb_content_rsbycriteria (id, template_id, source_id, condition) FROM stdin;
\.
COPY kb_contentattribute (contenttemplate_id, attributetemplate_id, "position") FROM stdin;
\.
COPY kb_contentresourcegroup (template_id, group_id, "position") FROM stdin;
\.
COPY kb_contenttemplate (id, creationdate, lastmodifieddate, lastmodifieduser, state, orden, domain_id, contentclass_id, name, instantiable, ismultimedia, relations, abstract, acl, istemplate, hasdetailpage, isvideo, title_rule, isdefault, isaudio, istext, isdocument, isphoto, istool, isactivity, linkresources, isadd, iscustomattributes, iskbase, private_notes, abstract_label, private_notes_label, text_notes_label, text_label, customattributes_label, is_api, contentclasscode, istreefile, treefile_label, isresources, resources_label, isexternal, includesrelationshipsbycriteria, acceptsrelationshipsbycriteria, increlationshipsbycriteria, iscompliance, treefileresource, consolesubtitlerule, portalssubtitlerule, description, alias, onlyroot) FROM stdin;
10	2017-08-30 18:17:04.84-03	2017-08-30 18:17:04.849-03	1	1	0	1	KbeeIDoc	File	t	f	f	t	\N	f	f	f	\N	f	f	f	t	f	f	f	f	f	f	f	t	\N	\N	\N	\N	\N	f	file	f	\N	t	Resources	f	f	f	f	f	f	\N	\N	\N	file	f
\.
COPY kb_cronjob (id, lastmodifieddate, lastmodifieduser, name, description, cronexpression, clazz, parameter, isenabled, lastexecution, execoldtriggers, domain, timezone) FROM stdin;
1	2018-09-27 05:24:47.476614-03	1	LogUsageServiceRequest	Logs daily usage Hard Disk, Contents, Users, Resources for every domain	0 32 23 * * *	com.novamens.kbee.logging.usage.LogUsageServiceRequest	\N	t	\N	f	1	US/Central
2	2018-09-27 05:24:47.489696-03	1	LogApiUsageServiceRequest 	Log API Usage last day	0 13 1 * * *	com.novamens.kbee.logging.usage.LogApiUsageServiceRequest	\N	t	\N	f	1	US/Central
3	2018-09-27 05:24:58.923768-03	1	RecycleBinCleanUpServiceRequest	Deletes old Contents in the Recycle Bin	0 20 3 * * *	com.novamens.kbee.content.command.RecycleBinCleanUpServiceRequest	\N	t	\N	f	1	US/Central
4	2018-09-27 05:24:58.936435-03	1	ReprocessAPIRequestsCronJobRequest	(ReprocessCommand) every Sunday at 0:35:00, size is the max elements to process 	0 35 0 ? * SUN	com.novamens.kbee.content.webapi.command.ReprocessAPIRequestsCronJobRequest	8000	t	\N	f	1	US/Central
5	2019-11-19 02:34:04.2145-03	1	SQLCronJobRequest clean API LogEvent	delete from API LogEvent oldest 2 days provided we keep 1 year	0 15 7 * * *	com.novamens.kbee.content.service.datamanagement.SQLCronJobRequest	DELETE FROM api_logevent WHERE event_id IN (SELECT event_id FROM api_logevent where event_time < NOW() - INTERVAL '1 year' and event_time < (select min(event_time) from api_logevent) + INTERVAL '2 day')	t	\N	f	1	US/Central
6	2019-11-19 02:34:04.334384-03	1	Evaluacion de Reglas	Evaluacion de Reglas	11 30 0 ? * *	com.novamens.kbee.command.CommandExecutionJob	command=RulesCommand	t	\N	f	1	US/Central
7	2019-11-19 02:34:04.374012-03	1	PingServiceRequest	Ping. System Parameters: ping.enabled = yes/no|  ping.notify = yes/no | ping.email = email to send Ping error.	15 * * * * *	com.novamens.kbee.content.command.PingServiceRequest	\N	t	\N	f	1	US/Central
8	2020-03-19 01:28:57.813278-03	1	Alertas de Workflow	Alertas de Workflow	00 45 0 ? * *	com.novamens.kbee.command.CommandExecutionJob	command=WorkflowAlertsCommand	t	\N	f	1	US/Central
9	2020-04-30 19:52:57.879977-03	1	HealthMetricsCommand night	HealthMetricsCommand at 19:48 PM Server Time	15 48 19 * * *	com.novamens.kbee.command.CommandExecutionJob	command=HealthMetricsCommand	t	\N	f	1	US/Central
10	2020-04-30 19:52:57.879977-03	1	HealthMetricsCommand noon	HealthMetricsCommand at 8:48 PM Server Time	15 48 8 * * *	com.novamens.kbee.command.CommandExecutionJob	command=HealthMetricsCommand	t	\N	f	1	US/Central
\.
COPY kb_datasetattribute (dataset_id, attributetemplate_id, "position") FROM stdin;
\.
COPY kb_domain_settings (domain_id, category, values_json, lastmodifieddate, id) FROM stdin;
1	kbee	[{"emailServiceNoReply":"noreply@realpage.com","emailServiceStatus":"enabled"}]	2017-08-30 18:13:27.935207-03	13583
\.
COPY kb_ds_element_template (id, dataset_id, "position", classifier_id, attribute_id, multiplicity, readonly, aggregation) FROM stdin;
\.
COPY kb_email_template (id, creationdate, lastmodifieddate, lastmodifieduser, state, domain_id, lang, xkey, title, fromstr, subject, strtext, available_macros, isdefault) FROM stdin;
\.
COPY kb_enotirule (id, lastmodifieddate, lastmodifieduser, domain_id, name, condition, description, enabled, owner, event_type, state, creationdate, notes, is_system, isalert, isemail, key) FROM stdin;
\.
COPY kb_enotirule_principal (rule_id, principal_id) FROM stdin;
\.
COPY kb_enotirule_role (rule_id, role_id) FROM stdin;
\.
COPY kb_facet_wrapper (id, domain_id, name, display_name, visibility, lastmodifieddate, lastmodifieduser, "order", state, creationdate, viewmode) FROM stdin;
\.
COPY kb_file_loader (id, name, javaclass) FROM stdin;
1	realpage file	com.novamens.realpage.resource.FileLoader
2	realpage certificate	com.novamens.realpage.resource.CertificateLoader
3	realpage accounting file	com.novamens.realpage.resource.AccountingFileLoader
\.
COPY kb_file_proxy (resource_id, file_loader, url, size) FROM stdin;
\.
COPY kb_form (id, domain_id, lastmodifieddate, creationdate, lastmodifieduser, state, name, display_name, components) FROM stdin;
\.
COPY kb_form_data (id, lastmodifieddate, creationdate, lastmodifieduser, content_id, form_id, data) FROM stdin;
\.
COPY kb_form_template (form_id, contenttemplate_id) FROM stdin;
\.
COPY kb_group_role (role_id, group_id) FROM stdin;
2	7
2	9
2	5
\.
COPY kb_language_string (id, key, locale, value) FROM stdin;
1	audit	es	Auditoría
2	system-audit	es	Sistema
3	reports	es	Reportes
4	access	es	Acceso
5	application-start	es	Inicion de Aplicación
6	email	es	Correo electrónico
7	hard-disk-usage	es	Uso de Disco
8	user-activity	es	Actividad de Usuario
9	compliance	es	Indicadores de Gestión Regulatoria
10	compliance-submission	es	Proceso Regulatorios
11	report-subscription	es	Suscripción a Reportes
12	audit	en	Audit
13	system-audit	en	System
14	reports	en	Reports
15	access	en	Access
16	application-start	en	Application Start
17	email	en	Email
18	hard-disk-usage	en	Hard Disk Usage
19	user-activity	en	User Activity
20	report-subscription	en	Report Subscription
21	compliance	en	Performance Measurement
22	compliance-submission	en	Compliance Status
23	content-users-audit	en	Content - Visits
24	content-users-audit	es	Contenido - Detalle de Visitas
25	user-visits-audit	es	Usuario - Contenidos leídos
26	user-visits-audit	en	User - Files read
27	user-alerts-audit	es	Usuario - Alertas recibidas
28	user-alerts-audit	en	User - Alerts received
29	audit-abstract	es	Auditoría abstract
30	system-audit-abstract	es	Sistema abstract
31	reports-abstract	es	Reportes abstract
32	access-abstract	es	Acceso abstract
33	application-start-abstract	es	Inicion de Aplicación abstract
34	email-abstract	es	Correo electrónico abstract
35	hard-disk-usage-abstract	es	Uso de Disco abstract
36	user-activity-abstract	es	Actividad de Usuario abstract
37	compliance-abstract	es	Indicadores de Gestión Regulatoria abstract
38	compliance-submission-abstract	es	Proceso Regulatorios abstract
39	report-subscription-abstract	es	Suscripción a Reportes abstract
40	audit-abstract	en	Audit abstract
41	system-audit-abstract	en	System abstract
42	reports-abstract	en	Reports abstract
43	access-abstract	en	Access abstract
44	application-start-abstract	en	Application Start abstract
45	email-abstract	en	Email abstract
46	hard-disk-usage-abstract	en	Hard Disk Usage abstract
47	user-activity-abstract	en	User Activity abstract
48	report-subscription-abstract	en	Report Subscription abstract
49	compliance-abstract	en	Performance Measurement abstract
50	compliance-submission-abstract	en	Compliance Status abstract
51	content-users-audit-abstract	en	Content - Visits abstract
52	content-users-audit-abstract	es	Contenido - Detalle de Visitas abstract
53	user-visits-audit-abstract	es	Usuario - Contenidos leídos abstract
54	user-visits-audit-abstract	en	User - Files read abstract
55	user-alerts-audit-abstract	es	Usuario - Alertas recibidas abstract
56	user-alerts-audit-abstract	en	User - Alerts received abstract
\.
COPY kb_launcher_group (id, lastmodifieddate, creationdate, lastmodifieduser, state, domain_id, alias, name, description, "position", visible) FROM stdin;
\.
COPY kb_member_role (id, entity_id, role_id, securityrule_id, group_id) FROM stdin;
\.
COPY kb_model_section (id, contenttemplate_id, name, description, "position", isportal) FROM stdin;
\.
COPY kb_notification (id, creationdate, lastmodifieddate, lastmodifieduser, state, domain_id, title, text, content_id, sender_id, receiver_id, datesend, dateread, type, notification_state, notification_type, work_note_id, deleteonaccept, isalert, isbillboard, startpub, endpub, generating_enoti_rule, generating_action_rule) FROM stdin;
\.
COPY kb_object_property (id, type, name, object_id, value, uset, lastmodifieddate, domain_id) FROM stdin;
\.
COPY kb_organizationaldata (id, person_id, group_id, securityrule_id) FROM stdin;
\.
COPY kb_preference (id, user_id, name, properties) FROM stdin;
\.
COPY kb_preference_domain (id, domain_id, name, properties) FROM stdin;
\.
COPY kb_relation_target (relationtemplate_id, targettemplate_id) FROM stdin;
\.
COPY kb_relation_template (id, name, source_label, sourcetemplate_id, target_label, targettemplate_id, multiplicity, aggregation, "position", source_display_mode, target_display_mode, domain_id, lastmodifieduser, lastmodifieddate, creationdate, state, target_order, reverse_order) FROM stdin;
\.
COPY kb_report (user_id, content_id, report, reportdate, id) FROM stdin;
\.
COPY kb_report_subscription (id, lastmodifieddate, lastmodifieduser, domain_id, report_export_sched_id, enabled, usr, creationdate, last_export_sent, state) FROM stdin;
\.
COPY kb_resource_group (id, name, creationdate, lastmodifieddate, lastmodifieduser, domain_id, alias, state, createuser, type) FROM stdin;
\.
COPY kb_role (id, name, domain_id, state, type, classifier_id, condition, displaycondition, permissions, negative_permissions, canonical, securityrule_id, creationdate, lastmodifieddate, lastmodifieduser, alias, api_enabled, description, group_id, enable_useradmin, enable_usercreation, isdefault) FROM stdin;
2	Support	1	1	1	\N	\N	\N	\N	\N	t	\N	2019-03-29 11:46:23.873-03	2019-03-29 11:46:23.876-03	1	support	f	\N	\N	f	f	f
3	Reports	1	1	1	\N			\N	\N	t	\N	2019-03-29 11:46:23.878-03	2020-03-19 15:38:07.436-03	1	reports	f	\N	\N	f	f	f
1	Super User	1	1	1	\N	\N	\N	\N	\N	t	\N	2019-03-29 11:46:23.867-03	2019-03-29 11:46:23.871-03	1	super-user	f	\N	\N	f	f	f
\.
COPY kb_rsbycriteria_template (id, name, source_label, sourcetemplate_id, target_label, "position") FROM stdin;
\.
COPY kb_searcher_homeblock (state, creationdate, lastmodifieddate, lastmodifieduser, domain_id, id, lang, title, name, iql, sortstr, formatstr, custom_values, abstract) FROM stdin;
\.
COPY kb_security_rule (id, lastmodifieddate, lastmodifieduser, domain_id, name, type, condition, description, related_object_id, acl, creationdate, derived, displaycondition, parent_objectid, notes, role_rule, state) FROM stdin;
\.
COPY kb_securitydata (id, person_id, group_id, securityrule_id) FROM stdin;
\.
COPY kb_sendemailevent (event_id, event_type, event_time, event_user, event_domain_id, event_object_id, email_from, email_to, email_subject, email_text, email_attachments, event_result, event_generator_action, event_audit_resource_id) FROM stdin;
\.
COPY kb_source (id, creationdate, lastmodifieddate, lastmodifieduser, domain_id, name, display_name, state, createuser) FROM stdin;
\.
COPY kb_subscription (user_id, content_oid, event_id, subscription_date, type_id) FROM stdin;
\.
COPY kb_subsectiontemplate (id, contenttemplate_id, name, section_id, korder) FROM stdin;
\.
COPY kb_system_properties (key, value, area) FROM stdin;
remote_server_importer	http://windocs.realpage.com/api	system
local_user_importer	root@windsor	system
remote_user_importer	root@windsor	system
remote_password_importer	w1nR00tw	system
criteria_importer	property(Ashton Village) or property(Castle Square) or property(Parkview Towers) or property(Point Natomas)	system
welcome-note.title	What is My Notepad ?	system
welcome-note.text	<p>The Notepad is a panel easily accesible from the toolbar where you can create and manage simple notes.</p><p>Notes can include&nbsp;<a href=\\"http://www.realpage.com\\">links</a> and formats like <strong>bold</strong> or <em>italic</em>.</p><p>&nbsp;</p><p>Notes are private, no one else can read or edit them.</p>	system
nonworkabledays	11/22/2018; 11/23/2018; 9/3/2018; 5/28/2018; 7/4/2018; 12/24;12/25;  12/26; 12/31; 1/1	system
kbase.task.suggestions	32	system
dataset1	Secured Access	system
dataset2	Site ID	system
dataset3	Cabinet	system
dataset4	Pmc Id	system
dataset5	Document Entity	system
dataset6	Packet Type	system
cabinet.external	OneSite	system
labels.default	Draft;Delete;Follow up;Duplicate;Review	system
dataset_type.default	Training;Tenant Selection Plan;Contract;EOM Financials;Mortgage Statement;Lease Agreement;Rent Schedule;Management Agreements;Non-Disclosure Agreement;Shareholder Meetings;Lawsuits;Acquisitions;Due Diligence;Territory Assignments;Sales Incentives;Compensation Plan;Hardware;Software;System Logs;Benefits;Organizational Chart;Annual Reviews;Offer Letters;Signage;Brochures;Flyers	system
dataset_status.default	Draft;Under Review;Approved;Final;Cancelled	system
dataset_department.default	Marketing;HR;IT;Sales;Legal;Finance;Compliance;Property Management;Facilities;Training	system
attribute_fileid.name	FileID	system
support1.email	idocsupport1@realpage.com	system
support2.email	idocsupport2@realpage.com	system
noreply.email	noreply@idoc.realpage.com	system
permissions.reason.domainspage	Must be user of domain kbee	system
com.novamens.content.webapi.realpage.secretkey	3b672429071d0392e744de75bdd183617ac25976cf4e1533667afcc452a6266a	system
library_external.criteria	isExternal(true)	system
library_kbase.criteria	isKnowledgebase(true)	system
library_templates.criteria	isTemplate(true)	system
library_standard	Enterprise	system
library_external	OneSite	system
library_kbase	Knowledge Base	system
library_templates	Templates	system
library_all	All	system
library_external.readonly	true	system
library_knowledgebase	Knowledge Base	system
library_knowledgebase.criteria	isKnowledgebase(true)	system
product	RealPage Compliance Services	system
product.name	RealPage Compliance Services	system
com.novamens.workflow.permissions	id	system
product.brand	RPCS	system
subproduct.brand	RPCS	system
library_standard.criteria	ishead(true) and isTemplate(false) and isExternal(false) and isKnowledgebase(false)	system
ping.email	atolomei@novamens.com; aferraria@novamens.com; grodriguez@novamens.com	system
libraries	standard, external, knowledgebase, smartsource, templates	system
ping.enabled	yes	system
com.novamens.content.contentclass.parentsenabled	true	system
library_smartsource	Smart Source	system
library_smartsource_criteria	ishead(true) and isTemplate(false) and isExternal(false) and isKnowledgebase(false)	system
roles.api	Visible in Unified Login	system
ping.notify	no	system
\.
COPY kb_timer (id, creationdate, duedate, callback, attemps, error_message) FROM stdin;
\.
COPY kb_tip (id, domain_id, area, status, lastmodifieddate, lastmodifieduser, tip_title, tip_text, tip_texyid, tip_lang, tip_area) FROM stdin;
\.
COPY kb_tree_file (id, parent_id, isdirectory, dir_name, resource_id, title, "position", state, creationdate, lastmodifieddate, domain_id, lastmodifieduser, type, isaccesspoint, tree_idoc_id, in_portal, oid, ishead, version, prev_version) FROM stdin;
\.
COPY kb_tree_idoc (content_id, tree_file_id) FROM stdin;
\.
COPY kb_tree_resource (resource_id, treefile_id) FROM stdin;
\.
COPY kb_usage_stat (domain_id, ts, hard_disk_usage, users, contents, resources, attributes, hard_disk_usage_gateway, resources_external, kbfs1_hard_disk_usage, kbfs2_hard_disk_usage, kbfs2archive_hard_disk_usage, contents_external, contents_external_library, contents_external_archive, contents_external_recycle, solr_content_items, solr_audit_items, solr_file_items, database_usage, s3_hard_disk_usage, glacier_hard_disk_usage, billable_users, billable_sites, units) FROM stdin;
\.
COPY kb_user_list (id, lastmodifieddate, creationdate, lastmodifieduser, state, domain_id, owner_id, console, title, description, total_items, version_match) FROM stdin;
\.
COPY kb_user_list_item (id, lastmodifieddate, creationdate, lastmodifieduser, state, domain_id, userlist_id, content_id, datasetmember_id, user_id, title, type, version_match, console, owner_id, oid) FROM stdin;
\.
COPY kb_user_note (id, user_id, title, notetext, creationdate, lastmodifieddate, lastmodifieduser, priority, domain_id) FROM stdin;
\.
COPY kb_user_property (id, type, name, user_id, value, uset, lastmodifieddate) FROM stdin;
\.
COPY kb_user_role (id, userprofile_id, role_id, entity_id, user_id) FROM stdin;
\.
COPY kb_userlistclassification (id, classifier_id, datasetmember_id, "position", user_list_item_id) FROM stdin;
\.
COPY kb_vote (user_id, content_id, vote, votedate, id) FROM stdin;
\.
COPY kb_work_note (id, user_id, title, notetext, creationdate, lastmodifieddate, lastmodifieduser, priority, domain_id, send_notification, is_first_version, isalert, isemail, isbillboard, glyphicon, startpub, endpub, cronexpression) FROM stdin;
\.
COPY kb_work_note_user_read (id, work_note_id, user_id, readdate) FROM stdin;
\.
COPY kb_worknote_principal (note_id, principal_id) FROM stdin;
\.
COPY kfile (resource_id, path, file_type, title, subtitle, description, thumbnailsmall, thumbnaillarge, width, height, crc32str, uploadeddate, uploadeduser, externallystored, storagemode, bucketname, objectname, shard, kfsize, fsid, in_portal, isencrypted, minor) FROM stdin;
\.
COPY kgroup (id, name, canonical, description, derived, enabled, onlyportal, onlydomainkbee, onlyinternaluse, areacode) FROM stdin;
6	library	t	\N	f	f	f	f	f	content
10	workflow	t	\N	f	t	f	f	t	workflow
2	user	t	\N	f	t	f	f	t	user
11	templates	t	\N	f	t	f	f	f	content
3	root	t	\N	f	t	f	f	f	root
4	domain-admin	t	\N	f	t	f	f	f	admin
8	security	t	\N	f	t	f	f	f	security
12	information-model	t	\N	f	t	f	f	f	settings
9	monitor	t	\N	f	f	f	f	f	workflow
5	mytasks	t	\N	f	t	f	f	f	workflow
7	archive	t	\N	f	t	f	f	f	content
\.
COPY kgroupmember (kgroup, principal) FROM stdin;
2	1
3	1
4	1
\.
COPY klock (lock_id, lock_object_id, lock_date, lock_user_id, lock_scope, lock_timeout) FROM stdin;
\.
COPY kresource (id, oid, lastmodifieddate, creationdate, lastmodifieduser, state, domain_id, group_id, title, name, version, prev_version, kmode, seed, ishead, ksize, ispublic, in_portal) FROM stdin;
\.
COPY logevent (event_id, event_type, event_object_id, event_content_id, event_version, event_time, event_user, event_user_to, event_task, event_parameters, event_domain_id, event_title, event_kbeeclass, event_procedure, auditset, event_activity_id, event_resource_id, event_content_xid, event_type_int, event_audit_resource_id) FROM stdin;
\.
COPY logevent_legacy (event_id, event_type, event_object_id, event_content_id, event_version, event_time, event_user, event_user_to, event_kbeeclass, event_task, event_parameters, event_domain_id, event_title, event_procedure, auditset) FROM stdin;
\.
COPY memberclassification (id, state, sourcemember_id, classifier_id, targetmember_id, "position") FROM stdin;
\.
COPY organization (id, lastmodifieddate, lastmodifieduser, state, domain_id, email, address, phone, website, name) FROM stdin;
\.
COPY organizationaltext (content_id, subtitle, contentdate, author_id, media, communitacion_class, text, summary) FROM stdin;
\.
COPY orgchart (content_id, name, description, mision, xmlchart) FROM stdin;
\.
COPY person (entity_id, email, address, phone, website, firstname, lastname, description, birthdate, photo, workposition, photo_domain_logo, emailvalidated) FROM stdin;
1	root@kbee.com	\N	1111	\N		Admin	\N	\N	\N	Factory Manager	f	t
\.
COPY po_area (po_id, page_id, area_type, orden, full_width_canvas, areaclass, custom_values, parent_area_id) FROM stdin;
\.
COPY po_block (po_id, area_id, section, orden, subtitle, textstyle, image_id, new_tab, maxlements, quantity_visible, title_visible, intro_visible, image_visible, intro_only_image, external_link, page_link, content_link, block_image, block_menu_enabled, description, block_css, usage_info, image_css, block_body_style, content_id, custom_values) FROM stdin;
\.
COPY po_block_banners (block_id) FROM stdin;
\.
COPY po_block_contact (block_id, emailto) FROM stdin;
\.
COPY po_block_content_list (block_id, query, block_subtype, thumbnail_enabled, metadata_enabled, description_enabled, max_description_length, population_mode, thumbnail_size_mode, thumbnail_pos, element_title_enabled) FROM stdin;
\.
COPY po_block_cumpleanos (block_id, date_from, date_to, image_visible, feriados) FROM stdin;
\.
COPY po_block_footer (block_id, element_css) FROM stdin;
\.
COPY po_block_gallery_viewer (block_id) FROM stdin;
\.
COPY po_block_image_viewer (block_id, link_container_css, image_container_css, imageviewer_id, url) FROM stdin;
\.
COPY po_block_search_external (block_id, container_css, element_css, url) FROM stdin;
\.
COPY po_block_select_list (block_id, select_container_css, select_css, select_list_str) FROM stdin;
\.
COPY po_block_selector (block_id, element_css) FROM stdin;
\.
COPY po_block_site_components (block_id, site_id, block_type) FROM stdin;
\.
COPY po_block_site_list (block_id, query, element_title_enabled) FROM stdin;
\.
COPY po_block_text (block_id, text_css, max_description_length) FROM stdin;
\.
COPY po_block_view_list (block_id, block_subtype, thumbnail_enabled, metadata_enabled, description_enabled, max_description_length, thumbnail_size_mode, thumbnail_pos, population_mode, element_title_enabled, element_css, inline_filter, hitpanelmenu_enabled, sorted, element_link_resource, sort_type, title_type, block_helper, multiblockstyle, layoutmode, subtitle_mode, element_orientation_css) FROM stdin;
\.
COPY po_block_view_recent_list (block_id, global) FROM stdin;
\.
COPY po_block_wall_viewer (block_id) FROM stdin;
\.
COPY po_block_x (block_id) FROM stdin;
\.
COPY po_contentblock (block_id, content_id, orden) FROM stdin;
\.
COPY po_diagrammable_area (po_id, page_id, area_type, orden, full_width_canvas, areaclass) FROM stdin;
\.
COPY po_diagrammable_block (po_id, area_id, section, orden, subtitle, textstyle, image_id, new_tab, maxlements, quantity_visible, title_visible, intro_visible, image_visible, intro_only_image, external_link, page_link, content_link, block_image, block_menu_enabled, description, block_css, usage_info, image_css, block_body_style, block_separator_css, content_id) FROM stdin;
\.
COPY po_diagrammable_page (po_id, site_id, description, relative_url, is_admin, issection, ishome, orden, page_type, content_link, is_header_container, contentid, menus_visible, usage_info) FROM stdin;
\.
COPY po_diagrammable_site (po_id, site_type, ispublic, isexternal, subtitle, description, uri, detail_comments_enabled, detail_votes_enabled, detail_follow_enabled, detail_related_enabled, detail_send_enabled, footer_block_id, header_block_id, email_contact, site_template, page_header_footer_id, site_image, isimagevisible) FROM stdin;
\.
COPY po_page (po_id, site_id, description, relative_url, is_admin, issection, ishome, orden, page_type, content_link, is_header_container, contentid, menus_visible, usage_info, custom_values) FROM stdin;
\.
COPY po_portalobject (id, oid, parent_id, creationdate, lastmodifieddate, lastmodifieduser, state, domain_id, name, title, version, prev_version, kmode, ishead, nextversion) FROM stdin;
\.
COPY po_site (po_id, site_type, ispublic, isexternal, subtitle, description, uri, detail_comments_enabled, detail_votes_enabled, detail_follow_enabled, detail_related_enabled, detail_send_enabled, footer_block_id, header_block_id, email_contact, site_template, page_header_footer_id, site_image, isimagevisible, isdisplayvalidversion, custom_values, alias) FROM stdin;
\.
COPY po_site_favorites (id, user_id) FROM stdin;
\.
COPY po_site_favorites_list (list_id, site_oid, orden) FROM stdin;
\.
COPY po_site_securityrule (rule_id, related_object_id) FROM stdin;
\.
COPY po_site_subscription (user_id, site_oid, event_id, subscription_date, type_id) FROM stdin;
\.
COPY po_sitelogin (id, user_id, user_name, site_id, site_title, page_id, page_type, page_title, content_title, visit_time, src, browser, device, os, ip, domain_id, content_id, render_milisecs, session_id, user_agent, object_id, content_oid, version, content_long_id) FROM stdin;
\.
COPY po_sitelogout (id, user_id, user_name, site_id, site_title, page_id, page_type, page_title, block_id, block_title, view_id, view_type, view_content_id, view_link, view_site_id, visit_time, browser, device, os, ip, domain_id, view_title) FROM stdin;
\.
COPY po_siteuser (site_id, user_id, permission) FROM stdin;
\.
COPY po_siteuserrights (site_id, user_id, permissions, lastmodifieddate, lastmodifieduser) FROM stdin;
\.
COPY po_viewbk (po_id, block_id, "position", title, abstract, image_id, metadata, style_width, style_height, style, ntab, iconcss) FROM stdin;
\.
COPY po_viewbkblock (view_id, block_id) FROM stdin;
\.
COPY po_viewbkcontent (view_id, content_id, is_gallery, is_resources) FROM stdin;
\.
COPY po_viewbklink (view_id, link) FROM stdin;
\.
COPY po_viewbksite (view_id, site_id) FROM stdin;
\.
COPY po_viewcontent (po_id, content_id, titlemode, isabstract, ismetadata, isviewer, bodytemplate, isresources, resourcesmode, resourcesids, content_oid) FROM stdin;
\.
COPY po_viewcontentrelation (view_id, target_id, "position") FROM stdin;
\.
COPY principal (id, lastmodifieddate, lastmodifieduser, domain_id, creationdate) FROM stdin;
3	2017-08-30 18:13:27.942749-03	1	1	2017-08-30 18:13:27.942749-03
10	2017-08-30 18:13:28.011506-03	1	1	2017-08-30 18:13:28.011506-03
7	2015-11-08 12:30:39-03	1	1	2017-08-30 18:13:28.00259-03
4	2015-11-08 12:31:06-03	1	1	2017-08-30 18:13:27.959223-03
6	2015-11-05 20:50:54-03	1	1	2017-08-30 18:13:27.994044-03
12	2015-11-08 12:30:02-03	1	1	2017-08-30 18:13:28.013856-03
9	2015-11-05 20:51:43-03	1	1	2017-08-30 18:13:28.010594-03
5	2015-11-05 20:50:23-03	1	1	2017-08-30 18:13:27.975648-03
8	2015-11-08 12:29:37-03	1	1	2017-08-30 18:13:28.007914-03
11	2015-11-14 13:35:10-03	1	1	2017-08-30 18:13:28.012982-03
2	2015-11-05 15:31:07-03	1	1	2017-08-30 18:13:27.939711-03
1	2020-06-01 14:03:02.466-03	1	1	2017-08-30 18:13:27.938328-03
\.
COPY profile (id, lastmodifieddate, lastmodifieduser, entity, domain_id, creationdate) FROM stdin;
1	2020-06-04 03:12:59.025-03	1	1	1	2017-08-30 18:13:28.016352-03
\.
COPY property (id, type, name, content_id, value, uset, lastmodifieddate) FROM stdin;
\.
COPY resourcefile (resource_id, file_id, listorder, text) FROM stdin;
\.
COPY rs_content_pivot (content_id, content_oid, pivot_last_update, data) FROM stdin;
\.
COPY rs_user_pivot (user_id, pivot_last_update, data) FROM stdin;
\.
COPY savedquery (id, title, statement, "position", console, is_system, user_id, lastmodifieddate, creationdate, lastmodifieduser, state, domain_id) FROM stdin;
\.
COPY scheduler (id, request, "time", priority, error_count, description, error_message, title, objectid, execute_after, command_class_name, command_parameters) FROM stdin;
\.
COPY userlabel (id, user_id, label, css, short_label, scope, context, creationdate) FROM stdin;
\.
COPY userprofile (id, state, confidencelevel, user_id, email_notifications, tipoftheday, editperson, email_rule_notifications, sendfilesemail, uitheme, startpage, lastlogindate, email_notifications_pending, changepassword, isclient) FROM stdin;
1	\N	99	1	t	t	t	t	t	rpdm	dashboard	2020-09-07 16:56:59.532-03	t	t	f
\.
COPY users (id, lastmodifieddate, lastmodifieduser, state, username, password, password_md5, seed, firstname, lastname, email, locale_str, enabled, canonical, active, creationdate, timezone, uitheme, passwordlastmodifieddate, is_billable) FROM stdin;
1	2017-08-30 18:13:27.927209-03	\N	1	root@kbee	{bcrypt}$2a$10$1Fh0/Rog6KO7CRzUctDNquUpjReW1gT.FZrcLdVuCYdepzGAbwl1q	\N	\N		root	\N	en    	t	f	t	2017-08-30 18:13:27.927209-03	US/Central	rpdm	2020-04-30 20:37:19.627-03	f
\.
COPY wf_activity (id, process_id, task, user_id, assigned_by, content_id, startime, endtime, event, note, resolution, status, resolutiontitle, event_label) FROM stdin;
\.
COPY wf_launcher (id, domain_id, label, contenttemplate_id, procedure_id, contextual, acl, isenabled, alias, lastmodifieddate, creationdate, lastmodifieduser, state, launchergroup_id) FROM stdin;
\.
COPY wf_procedure (id, alias, lastmodifieddate, creationdate, lastmodifieduser, domain_id, state, name, tasks, states, initial_rules, code, roles, launcher, diagram, version, description) FROM stdin;
\.
COPY wf_process (id, procedure, startime, endtime, status, procedure_id) FROM stdin;
\.
SELECT pg_catalog.setval('aclentry_sequence', 7977, true);
SELECT pg_catalog.setval('api_sequence', 1000, false);
SELECT pg_catalog.setval('classificationid_sequence', 3084618, true);
SELECT pg_catalog.setval('contentid_sequence', 2713061, true);
SELECT pg_catalog.setval('contentresourceid_sequence', 4237666, true);
SELECT pg_catalog.setval('domainid_sequence', 217, true);
SELECT pg_catalog.setval('entityid_sequence', 2112, true);
SELECT pg_catalog.setval('hibernate_sequence', 6959811, true);
SELECT pg_catalog.setval('lock_sequence', 1542, true);
SELECT pg_catalog.setval('log_sequence', 21070441, true);
SELECT pg_catalog.setval('logsites_sequence', 79547, true);
SELECT pg_catalog.setval('objectid_sequence', 59377, true);
SELECT pg_catalog.setval('portalid_sequence', 29712, true);
SELECT pg_catalog.setval('propertyid_sequence', 1000, false);
SELECT pg_catalog.setval('qaid_sequence', 1, false);
SELECT pg_catalog.setval('query_sequence', 1, false);
SELECT pg_catalog.setval('resourceid_sequence', 346046478, true);
SELECT pg_catalog.setval('scheduler_sequence', 50330242, true);
SELECT pg_catalog.setval('security_sequence', 38995, true);
SELECT pg_catalog.setval('sendemail_log_sequence', 50693, true);
SELECT pg_catalog.setval('seqtmp', 66, true);
SELECT pg_catalog.setval('timer_sequence', 100, false);
SELECT pg_catalog.setval('workflow_sequence', 3920047, true);
ALTER TABLE ONLY kb_acl
    ADD CONSTRAINT acl_pkey PRIMARY KEY (id);
ALTER TABLE ONLY kb_action_rule
    ADD CONSTRAINT action_rule_pk PRIMARY KEY (id);
ALTER TABLE ONLY drb_answer
    ADD CONSTRAINT answer_pkey PRIMARY KEY (content_id);
ALTER TABLE ONLY api_logevent
    ADD CONSTRAINT api_logevent_pkey PRIMARY KEY (event_id);
ALTER TABLE ONLY api_soapevent
    ADD CONSTRAINT api_soapevent_pkey PRIMARY KEY (event_id);
ALTER TABLE ONLY po_diagrammable_area
    ADD CONSTRAINT area_pkey PRIMARY KEY (po_id);
ALTER TABLE ONLY kb_attribute
    ADD CONSTRAINT attribute_pkey PRIMARY KEY (id);
ALTER TABLE ONLY kb_attributetemplate
    ADD CONSTRAINT attributetemplate_pkey PRIMARY KEY (id);
ALTER TABLE ONLY authorities
    ADD CONSTRAINT authorities_pk PRIMARY KEY (username, authority);
ALTER TABLE ONLY po_block_banners
    ADD CONSTRAINT block_banners_pkey PRIMARY KEY (block_id);
ALTER TABLE ONLY po_block_contact
    ADD CONSTRAINT block_contact_pkey PRIMARY KEY (block_id);
ALTER TABLE ONLY po_block_content_list
    ADD CONSTRAINT block_content_list_pkey PRIMARY KEY (block_id);
ALTER TABLE ONLY po_block_cumpleanos
    ADD CONSTRAINT block_cumpleanos_pkey PRIMARY KEY (block_id);
ALTER TABLE ONLY po_block_footer
    ADD CONSTRAINT block_footer_view_list_pkey PRIMARY KEY (block_id);
ALTER TABLE ONLY po_block_gallery_viewer
    ADD CONSTRAINT block_gallery_viewer_pkey PRIMARY KEY (block_id);
ALTER TABLE ONLY po_block_image_viewer
    ADD CONSTRAINT block_image_viewer_pkey PRIMARY KEY (block_id);
ALTER TABLE ONLY po_diagrammable_block
    ADD CONSTRAINT block_pkey PRIMARY KEY (po_id);
ALTER TABLE ONLY po_block_view_recent_list
    ADD CONSTRAINT block_recent_view_pkey PRIMARY KEY (block_id);
ALTER TABLE ONLY po_block_site_components
    ADD CONSTRAINT block_sc_pkey PRIMARY KEY (block_id);
ALTER TABLE ONLY po_block_select_list
    ADD CONSTRAINT block_select_list_pkey PRIMARY KEY (block_id);
ALTER TABLE ONLY po_block_selector
    ADD CONSTRAINT block_selector_view_list_pkey PRIMARY KEY (block_id);
ALTER TABLE ONLY po_block_site_list
    ADD CONSTRAINT block_site_list_pkey PRIMARY KEY (block_id);
ALTER TABLE ONLY po_block_text
    ADD CONSTRAINT block_text_pkey PRIMARY KEY (block_id);
ALTER TABLE ONLY po_block_view_list
    ADD CONSTRAINT block_view_list_pkey PRIMARY KEY (block_id);
ALTER TABLE ONLY po_block_wall_viewer
    ADD CONSTRAINT block_wall_viewer_pkey PRIMARY KEY (block_id);
ALTER TABLE ONLY po_block_x
    ADD CONSTRAINT block_x_pkey PRIMARY KEY (block_id);
ALTER TABLE ONLY kb_cabinet
    ADD CONSTRAINT cabinet_pkey PRIMARY KEY (id);
ALTER TABLE ONLY kb_cabinet_reader
    ADD CONSTRAINT cabinet_reader_pk PRIMARY KEY (cabinet_id, group_id);
ALTER TABLE ONLY kb_classifier
    ADD CONSTRAINT classifier_pkey PRIMARY KEY (id);
ALTER TABLE ONLY classifiercontent
    ADD CONSTRAINT classifiercontent_pkey PRIMARY KEY (classifier_id, contentclass_id);
ALTER TABLE ONLY kb_classifiertemplate
    ADD CONSTRAINT classifiertemplate_pkey PRIMARY KEY (id);
ALTER TABLE ONLY kb_comment
    ADD CONSTRAINT comment_pkey PRIMARY KEY (content_id);
ALTER TABLE ONLY content
    ADD CONSTRAINT content_oiversion_unique UNIQUE (oid, version);
ALTER TABLE ONLY content
    ADD CONSTRAINT content_pkey PRIMARY KEY (id);
ALTER TABLE ONLY kb_contentattribute
    ADD CONSTRAINT contentattribute_pkey PRIMARY KEY (contenttemplate_id, attributetemplate_id);
ALTER TABLE ONLY po_contentblock
    ADD CONSTRAINT contentblock_pkey PRIMARY KEY (block_id, content_id);
ALTER TABLE ONLY contentclass
    ADD CONSTRAINT contentclass_pkey PRIMARY KEY (id);
ALTER TABLE ONLY classification
    ADD CONSTRAINT contentclassification_pkey PRIMARY KEY (id);
ALTER TABLE ONLY contentproperties
    ADD CONSTRAINT contentproperties_pkey PRIMARY KEY (content_id);
ALTER TABLE ONLY property
    ADD CONSTRAINT contentproperty_pkey PRIMARY KEY (id);
ALTER TABLE ONLY kb_content_relation
    ADD CONSTRAINT contentrelation_id_pk PRIMARY KEY (id);
ALTER TABLE ONLY contentresource
    ADD CONSTRAINT contentresource_pkey PRIMARY KEY (id);
ALTER TABLE ONLY contentresource
    ADD CONSTRAINT contentresource_unique UNIQUE (content_id, resource_id);
ALTER TABLE ONLY contentstat
    ADD CONSTRAINT contentstat_pkey PRIMARY KEY (content_id);
ALTER TABLE ONLY kb_contenttemplate
    ADD CONSTRAINT contenttemplate_pkey PRIMARY KEY (id);
ALTER TABLE ONLY kb_contentresourcegroup
    ADD CONSTRAINT contenttemplateresourcegroup_pkey PRIMARY KEY (template_id, group_id);
ALTER TABLE ONLY databasechangeloglock
    ADD CONSTRAINT databasechangeloglock_pkey PRIMARY KEY (id);
ALTER TABLE ONLY dataset
    ADD CONSTRAINT dataset_pkey PRIMARY KEY (id);
ALTER TABLE ONLY kb_datasetattribute
    ADD CONSTRAINT datasetattribute_pkey PRIMARY KEY (dataset_id, attributetemplate_id);
ALTER TABLE ONLY datasetclassifier
    ADD CONSTRAINT datasetclassifier_pkey PRIMARY KEY (dataset_id, classifier_id);
ALTER TABLE ONLY datasetmember
    ADD CONSTRAINT datasetmember_pkey PRIMARY KEY (id);
ALTER TABLE ONLY kb_email_template
    ADD CONSTRAINT dlk UNIQUE (domain_id, lang, xkey);
ALTER TABLE ONLY kb_domain_settings
    ADD CONSTRAINT domain_cat_unique UNIQUE (domain_id, category);
ALTER TABLE ONLY kb_ds_element_template
    ADD CONSTRAINT dse_template_pk PRIMARY KEY (id);
ALTER TABLE ONLY kb_email_template
    ADD CONSTRAINT email_template_pkey PRIMARY KEY (id);
ALTER TABLE ONLY kb_enotirule
    ADD CONSTRAINT enotirule_pkey PRIMARY KEY (id);
ALTER TABLE ONLY kb_enotirule_principal
    ADD CONSTRAINT enotirule_principal_pkey PRIMARY KEY (rule_id, principal_id);
ALTER TABLE ONLY kb_enotirule_role
    ADD CONSTRAINT enotirule_role_pkey PRIMARY KEY (rule_id, role_id);
ALTER TABLE ONLY entity
    ADD CONSTRAINT entity_pkey PRIMARY KEY (id);
ALTER TABLE ONLY entitymatching
    ADD CONSTRAINT entitymatching_pkey PRIMARY KEY (kbee_id, url);
ALTER TABLE ONLY externalresource
    ADD CONSTRAINT externalresource_pkey PRIMARY KEY (resource_id);
ALTER TABLE ONLY kb_facet_wrapper
    ADD CONSTRAINT facet_pk PRIMARY KEY (id);
ALTER TABLE ONLY kfile
    ADD CONSTRAINT file_pkey PRIMARY KEY (resource_id);
ALTER TABLE ONLY kb_file_loader
    ADD CONSTRAINT fileloader_pk PRIMARY KEY (id);
ALTER TABLE ONLY kb_file_proxy
    ADD CONSTRAINT fileproxy_pk PRIMARY KEY (resource_id);
ALTER TABLE ONLY kb_form
    ADD CONSTRAINT form_pkey PRIMARY KEY (id);
ALTER TABLE ONLY gallery
    ADD CONSTRAINT gallery_pkey PRIMARY KEY (resource_id);
ALTER TABLE ONLY galleryfile
    ADD CONSTRAINT galleryfile_pkey PRIMARY KEY (gallery_id, file_id);
ALTER TABLE ONLY kgroup
    ADD CONSTRAINT group_pkey PRIMARY KEY (id);
ALTER TABLE ONLY kgroupmember
    ADD CONSTRAINT groupmember_pkey PRIMARY KEY (kgroup, principal);
ALTER TABLE ONLY domain
    ADD CONSTRAINT id_pkey PRIMARY KEY (id);
ALTER TABLE ONLY idoc
    ADD CONSTRAINT idoc_pkey PRIMARY KEY (content_id);
ALTER TABLE ONLY idocsection
    ADD CONSTRAINT idocsection_pkey PRIMARY KEY (id);
ALTER TABLE ONLY idocsectionresource
    ADD CONSTRAINT idocsectionresource_pkey PRIMARY KEY (section_id, resource_id);
ALTER TABLE ONLY kb_cronjob
    ADD CONSTRAINT kb_cronjob_pk PRIMARY KEY (id);
ALTER TABLE ONLY kb_domain_settings
    ADD CONSTRAINT kb_domain_settings_pkey PRIMARY KEY (id);
ALTER TABLE ONLY kb_form_data
    ADD CONSTRAINT kb_form_data_pkey PRIMARY KEY (id);
ALTER TABLE ONLY kb_language_string
    ADD CONSTRAINT kb_language_string_pkey PRIMARY KEY (id);
ALTER TABLE ONLY kb_launcher_group
    ADD CONSTRAINT kb_launcher_group_pkey PRIMARY KEY (id);
ALTER TABLE ONLY kb_preference_domain
    ADD CONSTRAINT kb_preference_domain_domain_id_name_key UNIQUE (domain_id, name);
ALTER TABLE ONLY kb_preference_domain
    ADD CONSTRAINT kb_preference_domain_pkey PRIMARY KEY (id);
ALTER TABLE ONLY kb_searcher_homeblock
    ADD CONSTRAINT kb_searcher_homeblock_pkey PRIMARY KEY (id);
ALTER TABLE ONLY kb_tree_file
    ADD CONSTRAINT kb_tree_file_pkey PRIMARY KEY (id);
ALTER TABLE ONLY kb_tree_idoc
    ADD CONSTRAINT kb_tree_idoc_pkey PRIMARY KEY (content_id);
ALTER TABLE ONLY kb_api_usage_stat
    ADD CONSTRAINT kb_ts_pk PRIMARY KEY (ts);
ALTER TABLE ONLY kb_user_list_item
    ADD CONSTRAINT kb_user_list_item_pkey PRIMARY KEY (id);
ALTER TABLE ONLY kb_user_list
    ADD CONSTRAINT kb_user_list_pkey PRIMARY KEY (id);
ALTER TABLE ONLY kb_userlistclassification
    ADD CONSTRAINT kb_userlistclassification_pkey PRIMARY KEY (id);
ALTER TABLE ONLY kb_worknote_principal
    ADD CONSTRAINT kb_worknote_principal_pk PRIMARY KEY (note_id, principal_id);
ALTER TABLE ONLY kresource
    ADD CONSTRAINT kresource_prev_version_key UNIQUE (prev_version);
ALTER TABLE ONLY kb_language_string
    ADD CONSTRAINT localekey UNIQUE (locale, key);
ALTER TABLE ONLY klock
    ADD CONSTRAINT lock_pkey PRIMARY KEY (lock_id);
ALTER TABLE ONLY logevent_legacy
    ADD CONSTRAINT logevent_legacy_pkey PRIMARY KEY (event_id);
ALTER TABLE ONLY logevent
    ADD CONSTRAINT logevent_pkey PRIMARY KEY (event_id);
ALTER TABLE ONLY memberclassification
    ADD CONSTRAINT memberclassification_pkey PRIMARY KEY (id);
ALTER TABLE ONLY kb_member_role
    ADD CONSTRAINT memberrole_id_pk PRIMARY KEY (id);
ALTER TABLE ONLY kb_model_section
    ADD CONSTRAINT modelsection_pk PRIMARY KEY (id);
ALTER TABLE ONLY kb_notification
    ADD CONSTRAINT notification_pkey PRIMARY KEY (id);
ALTER TABLE ONLY kb_object_property
    ADD CONSTRAINT objectproperty_pkey PRIMARY KEY (id);
ALTER TABLE ONLY organization
    ADD CONSTRAINT organization_pkey PRIMARY KEY (id);
ALTER TABLE ONLY kb_organizationaldata
    ADD CONSTRAINT organizationaldata_pkey PRIMARY KEY (id);
ALTER TABLE ONLY orgchart
    ADD CONSTRAINT orgchart_pkey PRIMARY KEY (content_id);
ALTER TABLE ONLY organizationaltext
    ADD CONSTRAINT ot_pkey PRIMARY KEY (content_id);
ALTER TABLE ONLY po_diagrammable_page
    ADD CONSTRAINT page_pkey PRIMARY KEY (po_id);
ALTER TABLE ONLY person
    ADD CONSTRAINT person_pk PRIMARY KEY (entity_id);
ALTER TABLE ONLY po_area
    ADD CONSTRAINT po_area_pkey PRIMARY KEY (po_id);
ALTER TABLE ONLY po_block
    ADD CONSTRAINT po_block_pkey PRIMARY KEY (po_id);
ALTER TABLE ONLY po_portalobject
    ADD CONSTRAINT po_oiversion_unique UNIQUE (oid, version);
ALTER TABLE ONLY po_page
    ADD CONSTRAINT po_page_pkey PRIMARY KEY (po_id);
ALTER TABLE ONLY po_portalobject
    ADD CONSTRAINT po_portalobject_prev_version_key UNIQUE (prev_version);
ALTER TABLE ONLY po_site
    ADD CONSTRAINT po_site_pkey PRIMARY KEY (po_id);
ALTER TABLE ONLY po_sitelogin
    ADD CONSTRAINT po_sitelogin_pkey PRIMARY KEY (id);
ALTER TABLE ONLY po_sitelogout
    ADD CONSTRAINT po_sitelogout_pkey PRIMARY KEY (id);
ALTER TABLE ONLY po_portalobject
    ADD CONSTRAINT portalobject_pkey PRIMARY KEY (id);
ALTER TABLE ONLY principal
    ADD CONSTRAINT principal_pkey PRIMARY KEY (id);
ALTER TABLE ONLY profile
    ADD CONSTRAINT profile_pkey PRIMARY KEY (id);
ALTER TABLE ONLY drb_question
    ADD CONSTRAINT question_pkey PRIMARY KEY (content_id);
ALTER TABLE ONLY kb_relation_template
    ADD CONSTRAINT relationtemplate_id_pk PRIMARY KEY (id);
ALTER TABLE ONLY kb_report
    ADD CONSTRAINT report_pkey PRIMARY KEY (id);
ALTER TABLE ONLY kb_report_subscription
    ADD CONSTRAINT report_subscription_pkey PRIMARY KEY (id);
ALTER TABLE ONLY kresource
    ADD CONSTRAINT resource_pkey PRIMARY KEY (id);
ALTER TABLE ONLY resourcefile
    ADD CONSTRAINT resourcefile_pkey PRIMARY KEY (resource_id, file_id);
ALTER TABLE ONLY kb_resource_group
    ADD CONSTRAINT resourcegroup_pkey PRIMARY KEY (id);
ALTER TABLE ONLY kb_role
    ADD CONSTRAINT role_id_pk PRIMARY KEY (id);
ALTER TABLE ONLY rs_content_pivot
    ADD CONSTRAINT rs_content_pivot_pkey PRIMARY KEY (content_id);
ALTER TABLE ONLY rs_user_pivot
    ADD CONSTRAINT rs_user_pivot_pkey PRIMARY KEY (user_id);
ALTER TABLE ONLY kb_content_rsbycriteria
    ADD CONSTRAINT rsbycriteria_pk PRIMARY KEY (id);
ALTER TABLE ONLY kb_rsbycriteria_template
    ADD CONSTRAINT rsbycriteria_template_pk PRIMARY KEY (id);
ALTER TABLE ONLY savedquery
    ADD CONSTRAINT savedquery_pk PRIMARY KEY (id);
ALTER TABLE ONLY scheduler
    ADD CONSTRAINT scheduler_pk PRIMARY KEY (id);
ALTER TABLE ONLY kb_securitydata
    ADD CONSTRAINT securitydata_pkey PRIMARY KEY (id);
ALTER TABLE ONLY kb_security_rule
    ADD CONSTRAINT securityrule_pkey PRIMARY KEY (id);
ALTER TABLE ONLY kb_sendemailevent
    ADD CONSTRAINT sendemailevent_pkey PRIMARY KEY (event_id);
ALTER TABLE ONLY po_site_favorites_list
    ADD CONSTRAINT site_favorites_list_pkey PRIMARY KEY (list_id, site_oid);
ALTER TABLE ONLY po_site_favorites
    ADD CONSTRAINT site_favorites_pkey PRIMARY KEY (id);
ALTER TABLE ONLY po_diagrammable_site
    ADD CONSTRAINT site_pkey PRIMARY KEY (po_id);
ALTER TABLE ONLY po_site_securityrule
    ADD CONSTRAINT site_securityrule_pkey PRIMARY KEY (rule_id);
ALTER TABLE ONLY po_site_subscription
    ADD CONSTRAINT site_subscription_pkey PRIMARY KEY (user_id, site_oid, event_id);
ALTER TABLE ONLY po_siteuser
    ADD CONSTRAINT siteuser_pkey PRIMARY KEY (site_id, user_id);
ALTER TABLE ONLY po_siteuserrights
    ADD CONSTRAINT siteuserrights_pk PRIMARY KEY (site_id, user_id);
ALTER TABLE ONLY kb_source
    ADD CONSTRAINT source_pk PRIMARY KEY (id);
ALTER TABLE ONLY kb_system_properties
    ADD CONSTRAINT sp_pkey PRIMARY KEY (key);
ALTER TABLE ONLY kb_subscription
    ADD CONSTRAINT subscription_pkey PRIMARY KEY (user_id, content_oid, event_id);
ALTER TABLE ONLY kb_subsectiontemplate
    ADD CONSTRAINT substemplate_pkey PRIMARY KEY (id);
ALTER TABLE ONLY kb_timer
    ADD CONSTRAINT timer_id_pk PRIMARY KEY (id);
ALTER TABLE ONLY kb_tip
    ADD CONSTRAINT tip_pkey PRIMARY KEY (id);
ALTER TABLE ONLY kb_tree_resource
    ADD CONSTRAINT treeresource_pk PRIMARY KEY (resource_id);
ALTER TABLE ONLY idocsection
    ADD CONSTRAINT unique_section_idoc UNIQUE (idoc_id, sectionorder);
ALTER TABLE ONLY kb_usage_stat
    ADD CONSTRAINT usage_stat_pkey PRIMARY KEY (domain_id, ts);
ALTER TABLE ONLY kb_preference
    ADD CONSTRAINT user_name_fk UNIQUE (user_id, name);
ALTER TABLE ONLY kb_user_note
    ADD CONSTRAINT user_note_id_pk PRIMARY KEY (id);
ALTER TABLE ONLY users
    ADD CONSTRAINT user_pkey PRIMARY KEY (id);
ALTER TABLE ONLY userlabel
    ADD CONSTRAINT userlabel_pkey PRIMARY KEY (id);
ALTER TABLE ONLY users
    ADD CONSTRAINT username UNIQUE (username);
ALTER TABLE ONLY userprofile
    ADD CONSTRAINT userprofile_pkey PRIMARY KEY (id);
ALTER TABLE ONLY kb_user_property
    ADD CONSTRAINT userproperty_pkey PRIMARY KEY (id);
ALTER TABLE ONLY kb_user_role
    ADD CONSTRAINT userrole_id_pk PRIMARY KEY (id);
ALTER TABLE ONLY po_viewcontentrelation
    ADD CONSTRAINT view_content_relation_pkey PRIMARY KEY (view_id, target_id);
ALTER TABLE ONLY po_viewbk
    ADD CONSTRAINT viewbk_pkey PRIMARY KEY (po_id);
ALTER TABLE ONLY po_viewbkblock
    ADD CONSTRAINT viewbkblock_pkey PRIMARY KEY (view_id);
ALTER TABLE ONLY po_viewbkcontent
    ADD CONSTRAINT viewbkcontent_pkey PRIMARY KEY (view_id);
ALTER TABLE ONLY po_viewbklink
    ADD CONSTRAINT viewbklink_pkey PRIMARY KEY (view_id);
ALTER TABLE ONLY po_viewbksite
    ADD CONSTRAINT viewbksite_pkey PRIMARY KEY (view_id);
ALTER TABLE ONLY po_viewcontent
    ADD CONSTRAINT viewcontent_pkey PRIMARY KEY (po_id);
ALTER TABLE ONLY kb_vote
    ADD CONSTRAINT vote_pkey PRIMARY KEY (id);
ALTER TABLE ONLY wf_activity
    ADD CONSTRAINT wf_activity_pkey PRIMARY KEY (id);
ALTER TABLE ONLY wf_launcher
    ADD CONSTRAINT wf_launcher_pkey PRIMARY KEY (id);
ALTER TABLE ONLY kb_work_note
    ADD CONSTRAINT wn_id_pk PRIMARY KEY (id);
ALTER TABLE ONLY kb_work_note_user_read
    ADD CONSTRAINT wnur_id_pk PRIMARY KEY (id);
ALTER TABLE ONLY wf_procedure
    ADD CONSTRAINT workflowprocedure_pkey PRIMARY KEY (id);
ALTER TABLE ONLY wf_process
    ADD CONSTRAINT workflowprocess_pkey PRIMARY KEY (id);
CREATE INDEX api_logevent_date_asc_idx ON api_logevent USING btree (event_time);
CREATE INDEX api_logevent_domain_date ON api_logevent USING btree (event_domain, event_time DESC);
CREATE INDEX api_logevent_event_time_idx ON api_logevent USING btree (event_time DESC);
CREATE INDEX api_logevent_file ON api_logevent USING btree (event_file);
CREATE INDEX api_logevent_status_date_idx ON api_logevent USING btree (event_status, event_time DESC);
CREATE INDEX api_soapevent_event_time_idx ON api_soapevent USING btree (event_time DESC);
CREATE INDEX block_cumpleanos_id_idx ON po_block_cumpleanos USING btree (block_id);
CREATE INDEX block_x_id_idx ON po_block_x USING btree (block_id);
CREATE INDEX classification_content_id_classifier_id_idx ON classification USING btree (content_id, classifier_id);
CREATE INDEX classifier_id_idx ON kb_classifier USING btree (id);
CREATE INDEX classifier_lower_idx ON kb_classifier USING btree (lower((name)::text));
CREATE INDEX classifiercontent_classifier_id_contentclass_id_idx ON classifiercontent USING btree (classifier_id, contentclass_id);
CREATE INDEX classifiercontent_contentclass_id_classifier_id_idx ON classifiercontent USING btree (contentclass_id, classifier_id);
CREATE INDEX content_dom_id_name_idx ON content USING btree (domain_id, lower((name)::text));
CREATE INDEX content_id_idx ON content USING btree (id);
CREATE INDEX content_lastmoddate_idx ON content USING btree (lastmodifieddate DESC);
CREATE INDEX content_name_idx ON content USING btree (lower((name)::text));
CREATE INDEX content_owner_console_userlist_userlistitem_idx ON kb_user_list_item USING btree (content_id, owner_id, console, userlist_id);
CREATE INDEX content_title_idx ON content USING btree (lower((title)::text));
CREATE INDEX contentblock_block_id_orden_idx ON po_contentblock USING btree (block_id, orden);
CREATE INDEX contentclass_name_idx ON contentclass USING btree (lower((name)::text));
CREATE INDEX contentproperties_content_id_idx ON contentproperties USING btree (content_id);
CREATE INDEX contentresource_content_id_resource_id_idx ON contentresource USING btree (content_id, resource_id);
CREATE INDEX contentstat_content_id_idx ON contentstat USING btree (content_id);
CREATE INDEX contenttemplate_name_idx ON kb_contenttemplate USING btree (lower((name)::text));
CREATE INDEX dataset_domain_id_lower_idx ON dataset USING btree (domain_id, lower((name)::text));
CREATE INDEX dataset_id_idx ON dataset USING btree (id);
CREATE INDEX domain_id ON domain USING btree (id);
CREATE INDEX domain_modified ON domain USING btree (lastmodifieddate DESC);
CREATE INDEX domain_name ON domain USING btree (lower((name)::text));
CREATE INDEX drb_answer_content_id_idx ON drb_answer USING btree (content_id);
CREATE INDEX drb_answer_question_id_votes_idx ON drb_answer USING btree (question_id, votes);
CREATE INDEX drb_question_content_id_idx ON drb_question USING btree (content_id);
CREATE INDEX external_id_idx ON content USING btree (source_id, external_id);
CREATE INDEX file_path_idx ON kfile USING btree (path);
CREATE INDEX gallery_lower_idx ON gallery USING btree (lower((title)::text));
CREATE INDEX gallery_resource_id_idx ON gallery USING btree (resource_id);
CREATE INDEX galleryfile_gallery_id_file_id_idx ON galleryfile USING btree (gallery_id, file_id);
CREATE INDEX galleryfile_gallery_id_gorder_idx ON galleryfile USING btree (gallery_id, gorder);
CREATE INDEX idoc_content_id_idx ON idoc USING btree (content_id);
CREATE INDEX idoc_editorialstate_idx ON idoc USING btree (editorialstate);
CREATE INDEX idoc_lower_idx ON idoc USING btree (lower((title)::text));
CREATE INDEX idocsection_idoc_id_sectionorder_idx ON idocsection USING btree (idoc_id, sectionorder);
CREATE INDEX idocsectionresource_section_id_position_idx ON idocsectionresource USING btree (section_id, "position");
CREATE INDEX idx_activity_starttime ON wf_activity USING btree (startime);
CREATE INDEX idx_classification_classifier_id ON classification USING btree (classifier_id);
CREATE INDEX idx_classification_mem ON classification USING btree (datasetmember_id);
CREATE INDEX idx_content_acl ON content USING btree (acl);
CREATE INDEX idx_content_contenttemplate ON content USING btree (contenttemplate);
CREATE INDEX idx_content_lastmodifieduser ON content USING btree (lastmodifieduser);
CREATE INDEX idx_content_workspace ON content USING btree (workspace);
CREATE INDEX idx_contentproperties_lastmodifieduser ON contentproperties USING btree (lastmodifieduser);
CREATE INDEX idx_contentresource_group_id ON contentresource USING btree (group_id);
CREATE INDEX idx_contentresource_resource_id ON contentresource USING btree (resource_id);
CREATE INDEX idx_dataset_classifier_id ON dataset USING btree (classifier_id);
CREATE INDEX idx_dataset_group_id ON dataset USING btree (group_id);
CREATE INDEX idx_dataset_lastmodifieduser ON dataset USING btree (lastmodifieduser);
CREATE INDEX idx_datasetclassifier_classifier_id ON datasetclassifier USING btree (classifier_id);
CREATE INDEX idx_datasetmember_dataset_id ON datasetmember USING btree (dataset_id);
CREATE INDEX idx_datasetmember_domain_id ON datasetmember USING btree (domain_id);
CREATE INDEX idx_datasetmember_entity_id ON datasetmember USING btree (entity_id);
CREATE INDEX idx_datasetmember_group_id ON datasetmember USING btree (group_id);
CREATE INDEX idx_datasetmember_lastmodifieduser ON datasetmember USING btree (lastmodifieduser);
CREATE INDEX idx_datasetmember_rule_id ON datasetmember USING btree (rule_id);
CREATE INDEX idx_datasetmember_securityrule_id ON datasetmember USING btree (securityrule_id);
CREATE INDEX idx_domain_logo ON domain USING btree (logo);
CREATE INDEX idx_drb_question_user_id ON drb_question USING btree (user_id);
CREATE INDEX idx_entity_domain_id ON entity USING btree (domain_id);
CREATE INDEX idx_galleryfile_file_id ON galleryfile USING btree (file_id);
CREATE INDEX idx_idoc_tree_file_id ON idoc USING btree (tree_file_id);
CREATE INDEX idx_idocsectionresource_resource_id ON idocsectionresource USING btree (resource_id);
CREATE INDEX idx_kb_aclentry_acl ON kb_aclentry USING btree (acl);
CREATE INDEX idx_kb_aclentry_principal ON kb_aclentry USING btree (principal);
CREATE INDEX idx_kb_action_rule_domain_id ON kb_action_rule USING btree (domain_id);
CREATE INDEX idx_kb_assignable_role_assignablerole_id ON kb_assignable_role USING btree (assignablerole_id);
CREATE INDEX idx_kb_assignable_role_role_id ON kb_assignable_role USING btree (role_id);
CREATE INDEX idx_kb_attribute_domain_id ON kb_attribute USING btree (domain_id);
CREATE INDEX idx_kb_attribute_lastmodifieduser ON kb_attribute USING btree (lastmodifieduser);
CREATE INDEX idx_kb_attributetemplate_attribute_id ON kb_attributetemplate USING btree (attribute_id);
CREATE INDEX idx_kb_attributetemplate_parent_id ON kb_attributetemplate USING btree (parent_id);
CREATE INDEX idx_kb_attributetemplate_section_id ON kb_attributetemplate USING btree (section_id);
CREATE INDEX idx_kb_cabinet_lastmodifieduser ON kb_cabinet USING btree (lastmodifieduser);
CREATE INDEX idx_kb_cabinet_reader_group ON kb_cabinet USING btree (reader_group);
CREATE INDEX idx_kb_cabinet_reader_group_id ON kb_cabinet_reader USING btree (group_id);
CREATE INDEX idx_kb_classifier_dataset2_id ON kb_classifier USING btree (dataset2_id);
CREATE INDEX idx_kb_classifier_dataset3_id ON kb_classifier USING btree (dataset3_id);
CREATE INDEX idx_kb_classifier_dataset_id ON kb_classifier USING btree (dataset_id);
CREATE INDEX idx_kb_classifier_domain_id ON kb_classifier USING btree (domain_id);
CREATE INDEX idx_kb_classifier_lastmodifieduser ON kb_classifier USING btree (lastmodifieduser);
CREATE INDEX idx_kb_classifiertemplate_classifier_id ON kb_classifiertemplate USING btree (classifier_id);
CREATE INDEX idx_kb_classifiertemplate_contenttemplate_id ON kb_classifiertemplate USING btree (contenttemplate_id);
CREATE INDEX idx_kb_classifiertemplate_parent_id ON kb_classifiertemplate USING btree (parent_id);
CREATE INDEX idx_kb_classifiertemplate_root_id ON kb_classifiertemplate USING btree (root_id);
CREATE INDEX idx_kb_classifiertemplate_section_id ON kb_classifiertemplate USING btree (section_id);
CREATE INDEX idx_kb_content_relation_source_id ON kb_content_relation USING btree (source_id);
CREATE INDEX idx_kb_content_relation_target_id ON kb_content_relation USING btree (target_id);
CREATE INDEX idx_kb_content_relation_template_id ON kb_content_relation USING btree (template_id);
CREATE INDEX idx_kb_content_rsbycriteria_source_id ON kb_content_rsbycriteria USING btree (source_id);
CREATE INDEX idx_kb_content_rsbycriteria_template_id ON kb_content_rsbycriteria USING btree (template_id);
CREATE INDEX idx_kb_contentattribute_attributetemplate_id ON kb_contentattribute USING btree (attributetemplate_id);
CREATE INDEX idx_kb_contentresourcegroup_group_id ON kb_contentresourcegroup USING btree (group_id);
CREATE INDEX idx_kb_contenttemplate_acl ON kb_contenttemplate USING btree (acl);
CREATE INDEX idx_kb_contenttemplate_domain_id ON kb_contenttemplate USING btree (domain_id);
CREATE INDEX idx_kb_cronjob_lastmodifieduser ON kb_cronjob USING btree (lastmodifieduser);
CREATE INDEX idx_kb_datasetattribute_attributetemplate_id ON kb_datasetattribute USING btree (attributetemplate_id);
CREATE INDEX idx_kb_ds_element_template_attribute_id ON kb_ds_element_template USING btree (attribute_id);
CREATE INDEX idx_kb_ds_element_template_classifier_id ON kb_ds_element_template USING btree (classifier_id);
CREATE INDEX idx_kb_ds_element_template_dataset_id ON kb_ds_element_template USING btree (dataset_id);
CREATE INDEX idx_kb_email_template_lastmodifieduser ON kb_email_template USING btree (lastmodifieduser);
CREATE INDEX idx_kb_enotirule_domain_id ON kb_enotirule USING btree (domain_id);
CREATE INDEX idx_kb_enotirule_lastmodifieduser ON kb_enotirule USING btree (lastmodifieduser);
CREATE INDEX idx_kb_enotirule_principal_principal_id ON kb_enotirule_principal USING btree (principal_id);
CREATE INDEX idx_kb_enotirule_role_role_id ON kb_enotirule_role USING btree (role_id);
CREATE INDEX idx_kb_facet_wrapper_domain_id ON kb_facet_wrapper USING btree (domain_id);
CREATE INDEX idx_kb_form_data_content_id ON kb_form_data USING btree (content_id);
CREATE INDEX idx_kb_form_data_form_id ON kb_form_data USING btree (form_id);
CREATE INDEX idx_kb_form_data_lastmodifieduser ON kb_form_data USING btree (lastmodifieduser);
CREATE INDEX idx_kb_form_domain_id ON kb_form USING btree (domain_id);
CREATE INDEX idx_kb_form_template_contenttemplate_id ON kb_form_template USING btree (contenttemplate_id);
CREATE INDEX idx_kb_form_template_form_id ON kb_form_template USING btree (form_id);
CREATE INDEX idx_kb_group_role_group_id ON kb_group_role USING btree (group_id);
CREATE INDEX idx_kb_group_role_role_id ON kb_group_role USING btree (role_id);
CREATE INDEX idx_kb_launcher_group_lastmodifieduser ON kb_launcher_group USING btree (lastmodifieduser);
CREATE INDEX idx_kb_member_role_entity_id ON kb_member_role USING btree (entity_id);
CREATE INDEX idx_kb_member_role_group_id ON kb_member_role USING btree (group_id);
CREATE INDEX idx_kb_member_role_role_id ON kb_member_role USING btree (role_id);
CREATE INDEX idx_kb_member_role_securityrule_id ON kb_member_role USING btree (securityrule_id);
CREATE INDEX idx_kb_model_section_contenttemplate_id ON kb_model_section USING btree (contenttemplate_id);
CREATE INDEX idx_kb_notification_content_id ON kb_notification USING btree (content_id);
CREATE INDEX idx_kb_notification_domain_id ON kb_notification USING btree (domain_id);
CREATE INDEX idx_kb_notification_lastmodifieduser ON kb_notification USING btree (lastmodifieduser);
CREATE INDEX idx_kb_notification_sender_id ON kb_notification USING btree (sender_id);
CREATE INDEX idx_kb_notification_work_note_id ON kb_notification USING btree (work_note_id);
CREATE INDEX idx_kb_object_property_domain_id ON kb_object_property USING btree (domain_id);
CREATE INDEX idx_kb_organizationaldata_group_id ON kb_organizationaldata USING btree (group_id);
CREATE INDEX idx_kb_organizationaldata_person_id ON kb_organizationaldata USING btree (person_id);
CREATE INDEX idx_kb_organizationaldata_securityrule_id ON kb_organizationaldata USING btree (securityrule_id);
CREATE INDEX idx_kb_relation_target_relationtemplate_id ON kb_relation_target USING btree (relationtemplate_id);
CREATE INDEX idx_kb_relation_target_targettemplate_id ON kb_relation_target USING btree (targettemplate_id);
CREATE INDEX idx_kb_relation_template_domain_id ON kb_relation_template USING btree (domain_id);
CREATE INDEX idx_kb_relation_template_lastmodifieduser ON kb_relation_template USING btree (lastmodifieduser);
CREATE INDEX idx_kb_relation_template_sourcetemplate_id ON kb_relation_template USING btree (sourcetemplate_id);
CREATE INDEX idx_kb_relation_template_targettemplate_id ON kb_relation_template USING btree (targettemplate_id);
CREATE INDEX idx_kb_report_subscription_domain_id ON kb_report_subscription USING btree (domain_id);
CREATE INDEX idx_kb_report_subscription_lastmodifieduser ON kb_report_subscription USING btree (lastmodifieduser);
CREATE INDEX idx_kb_report_subscription_usr ON kb_report_subscription USING btree (usr);
CREATE INDEX idx_kb_resource_group_createuser ON kb_resource_group USING btree (createuser);
CREATE INDEX idx_kb_resource_group_domain_id ON kb_resource_group USING btree (domain_id);
CREATE INDEX idx_kb_resource_group_lastmodifieduser ON kb_resource_group USING btree (lastmodifieduser);
CREATE INDEX idx_kb_role_classifier_id ON kb_role USING btree (classifier_id);
CREATE INDEX idx_kb_role_domain_id ON kb_role USING btree (domain_id);
CREATE INDEX idx_kb_role_group_id ON kb_role USING btree (group_id);
CREATE INDEX idx_kb_role_lastmodifieduser ON kb_role USING btree (lastmodifieduser);
CREATE INDEX idx_kb_role_securityrule_id ON kb_role USING btree (securityrule_id);
CREATE INDEX idx_kb_rsbycriteria_template_sourcetemplate_id ON kb_rsbycriteria_template USING btree (sourcetemplate_id);
CREATE INDEX idx_kb_searcher_homeblock_domain_id ON kb_searcher_homeblock USING btree (domain_id);
CREATE INDEX idx_kb_searcher_homeblock_lastmodifieduser ON kb_searcher_homeblock USING btree (lastmodifieduser);
CREATE INDEX idx_kb_security_rule_acl ON kb_security_rule USING btree (acl);
CREATE INDEX idx_kb_security_rule_domain_id ON kb_security_rule USING btree (domain_id);
CREATE INDEX idx_kb_security_rule_lastmodifieduser ON kb_security_rule USING btree (lastmodifieduser);
CREATE INDEX idx_kb_securitydata_group_id ON kb_securitydata USING btree (group_id);
CREATE INDEX idx_kb_securitydata_person_id ON kb_securitydata USING btree (person_id);
CREATE INDEX idx_kb_securitydata_securityrule_id ON kb_securitydata USING btree (securityrule_id);
CREATE INDEX idx_kb_source_domain_id ON kb_source USING btree (domain_id);
CREATE INDEX idx_kb_subsectiontemplate_contenttemplate_id ON kb_subsectiontemplate USING btree (contenttemplate_id);
CREATE INDEX idx_kb_subsectiontemplate_section_id ON kb_subsectiontemplate USING btree (section_id);
CREATE INDEX idx_kb_tree_file_domain_id ON kb_tree_file USING btree (domain_id);
CREATE INDEX idx_kb_tree_file_lastmodifieduser ON kb_tree_file USING btree (lastmodifieduser);
CREATE INDEX idx_kb_tree_file_resource_id ON kb_tree_file USING btree (resource_id);
CREATE INDEX idx_kb_tree_file_tree_idoc_id ON kb_tree_file USING btree (tree_idoc_id);
CREATE INDEX idx_kb_tree_idoc_tree_file_id ON kb_tree_idoc USING btree (tree_file_id);
CREATE INDEX idx_kb_tree_resource_treefile_id ON kb_tree_resource USING btree (treefile_id);
CREATE INDEX idx_kb_user_list_domain_id ON kb_user_list USING btree (domain_id);
CREATE INDEX idx_kb_user_list_item_datasetmember_id ON kb_user_list_item USING btree (datasetmember_id);
CREATE INDEX idx_kb_user_list_item_domain_id ON kb_user_list_item USING btree (domain_id);
CREATE INDEX idx_kb_user_list_item_lastmodifieduser ON kb_user_list_item USING btree (lastmodifieduser);
CREATE INDEX idx_kb_user_list_item_owner_id ON kb_user_list_item USING btree (owner_id);
CREATE INDEX idx_kb_user_list_item_user_id ON kb_user_list_item USING btree (user_id);
CREATE INDEX idx_kb_user_list_lastmodifieduser ON kb_user_list USING btree (lastmodifieduser);
CREATE INDEX idx_kb_user_note_domain_id ON kb_user_note USING btree (domain_id);
CREATE INDEX idx_kb_user_role_entity_id ON kb_user_role USING btree (entity_id);
CREATE INDEX idx_kb_user_role_user_id ON kb_user_role USING btree (user_id);
CREATE INDEX idx_kb_user_role_userprofile_id ON kb_user_role USING btree (userprofile_id);
CREATE INDEX idx_kb_userlistclassification_classifier_id ON kb_userlistclassification USING btree (classifier_id);
CREATE INDEX idx_kb_userlistclassification_datasetmember_id ON kb_userlistclassification USING btree (datasetmember_id);
CREATE INDEX idx_kb_userlistclassification_user_list_item_id ON kb_userlistclassification USING btree (user_list_item_id);
CREATE INDEX idx_kb_work_note_user_id ON kb_work_note USING btree (user_id);
CREATE INDEX idx_kb_work_note_user_read_work_note_id ON kb_work_note_user_read USING btree (work_note_id);
CREATE INDEX idx_kb_worknote_principal_principal_id ON kb_worknote_principal USING btree (principal_id);
CREATE INDEX idx_kfile_uploadeduser ON kfile USING btree (uploadeduser);
CREATE INDEX idx_kresource_group_id ON kresource USING btree (group_id);
CREATE INDEX idx_kresource_lastmodifieduser ON kresource USING btree (lastmodifieduser);
CREATE INDEX idx_memberclassification_classifier_id ON memberclassification USING btree (classifier_id);
CREATE INDEX idx_memberclassification_sourcemember_id ON memberclassification USING btree (sourcemember_id);
CREATE INDEX idx_memberclassification_targetmember_id ON memberclassification USING btree (targetmember_id);
CREATE INDEX idx_organization_domain_id ON organization USING btree (domain_id);
CREATE INDEX idx_organization_lastmodifieduser ON organization USING btree (lastmodifieduser);
CREATE INDEX idx_person_photo ON person USING btree (photo);
CREATE INDEX idx_po_area_page_id ON po_area USING btree (page_id);
CREATE INDEX idx_po_block_image_viewer_imageviewer_id ON po_block_image_viewer USING btree (imageviewer_id);
CREATE INDEX idx_po_block_search_external_block_id ON po_block_search_external USING btree (block_id);
CREATE INDEX idx_po_block_site_components_site_id ON po_block_site_components USING btree (site_id);
CREATE INDEX idx_po_contentblock_content_id ON po_contentblock USING btree (content_id);
CREATE INDEX idx_po_diagrammable_block_block_image ON po_diagrammable_block USING btree (block_image);
CREATE INDEX idx_po_diagrammable_block_content_link ON po_diagrammable_block USING btree (content_link);
CREATE INDEX idx_po_diagrammable_block_image_id ON po_diagrammable_block USING btree (image_id);
CREATE INDEX idx_po_diagrammable_block_page_link ON po_diagrammable_block USING btree (page_link);
CREATE INDEX idx_po_diagrammable_page_content_link ON po_diagrammable_page USING btree (content_link);
CREATE INDEX idx_po_diagrammable_site_footer_block_id ON po_diagrammable_site USING btree (footer_block_id);
CREATE INDEX idx_po_diagrammable_site_header_block_id ON po_diagrammable_site USING btree (header_block_id);
CREATE INDEX idx_po_diagrammable_site_page_header_footer_id ON po_diagrammable_site USING btree (page_header_footer_id);
CREATE INDEX idx_po_diagrammable_site_site_image ON po_diagrammable_site USING btree (site_image);
CREATE INDEX idx_po_page_content_link ON po_page USING btree (content_link);
CREATE INDEX idx_po_page_site_id ON po_page USING btree (site_id);
CREATE INDEX idx_po_portalobject_lastmodifieduser ON po_portalobject USING btree (lastmodifieduser);
CREATE INDEX idx_po_site_favorites_list_site_oid ON po_site_favorites_list USING btree (site_oid);
CREATE INDEX idx_po_site_favorites_user_id ON po_site_favorites USING btree (user_id);
CREATE INDEX idx_po_site_site_image ON po_site USING btree (site_image);
CREATE INDEX idx_po_siteuser_user_id ON po_siteuser USING btree (user_id);
CREATE INDEX idx_po_siteuserrights_user_id ON po_siteuserrights USING btree (user_id);
CREATE INDEX idx_po_viewbk_block_id ON po_viewbk USING btree (block_id);
CREATE INDEX idx_po_viewbk_image_id ON po_viewbk USING btree (image_id);
CREATE INDEX idx_po_viewbkblock_block_id ON po_viewbkblock USING btree (block_id);
CREATE INDEX idx_po_viewbkcontent_content_id ON po_viewbkcontent USING btree (content_id);
CREATE INDEX idx_po_viewbksite_site_id ON po_viewbksite USING btree (site_id);
CREATE INDEX idx_po_viewcontent_content_id ON po_viewcontent USING btree (content_id);
CREATE INDEX idx_po_viewcontentrelation_target_id ON po_viewcontentrelation USING btree (target_id);
CREATE INDEX idx_principal_domain_id ON principal USING btree (domain_id);
CREATE INDEX idx_principal_lastmodifieduser ON principal USING btree (lastmodifieduser);
CREATE INDEX idx_profile_domain_id ON profile USING btree (domain_id);
CREATE INDEX idx_profile_entity ON profile USING btree (entity);
CREATE INDEX idx_profile_lastmodifieduser ON profile USING btree (lastmodifieduser);
CREATE INDEX idx_resourcefile_file_id ON resourcefile USING btree (file_id);
CREATE INDEX idx_rs_user_pivot_clf ON rs_user_pivot USING gin ((((data -> 'personMember'::text) -> 'classifiers'::text)));
CREATE INDEX idx_savedquery_domain_id ON savedquery USING btree (domain_id);
CREATE INDEX idx_savedquery_lastmodifieduser ON savedquery USING btree (lastmodifieduser);
CREATE INDEX idx_userlabel_user_id ON userlabel USING btree (user_id);
CREATE INDEX idx_userprofile_user_id ON userprofile USING btree (user_id);
CREATE INDEX idx_wf_activity_assigned_by ON wf_activity USING btree (assigned_by);
CREATE INDEX idx_wf_activity_content_id ON wf_activity USING btree (content_id);
CREATE INDEX idx_wf_activity_process_id ON wf_activity USING btree (process_id);
CREATE INDEX idx_wf_activity_user_id ON wf_activity USING btree (user_id);
CREATE INDEX idx_wf_launcher_acl ON wf_launcher USING btree (acl);
CREATE INDEX idx_wf_launcher_contenttemplate_id ON wf_launcher USING btree (contenttemplate_id);
CREATE INDEX idx_wf_launcher_domain_id ON wf_launcher USING btree (domain_id);
CREATE INDEX idx_wf_launcher_lastmodifieduser ON wf_launcher USING btree (lastmodifieduser);
CREATE INDEX idx_wf_launcher_launchergroup_id ON wf_launcher USING btree (launchergroup_id);
CREATE INDEX idx_wf_launcher_procedure_id ON wf_launcher USING btree (procedure_id);
CREATE INDEX idx_wf_procedure_diagram ON wf_procedure USING btree (diagram);
CREATE INDEX idx_wf_procedure_domain_id ON wf_procedure USING btree (domain_id);
CREATE INDEX idx_wf_procedure_lastmodifieduser ON wf_procedure USING btree (lastmodifieduser);
CREATE INDEX idx_wf_process_procedure_id ON wf_process USING btree (procedure_id);
CREATE INDEX kb_cronjob_domain_idx ON kb_cronjob USING btree (domain, lower((name)::text));
CREATE INDEX kb_cronjob_name_idx ON kb_cronjob USING btree (lower((name)::text));
CREATE INDEX kb_email_template_domain_idx ON kb_email_template USING btree (domain_id, lang, xkey);
CREATE INDEX kb_enoti_owner_idx ON kb_enotirule USING btree (owner, lower((name)::text));
CREATE INDEX kb_external_id_idx ON content USING btree (external_id);
CREATE INDEX kb_notification_receiver_alert_idx ON kb_notification USING btree (receiver_id, isalert, state, creationdate);
CREATE INDEX kb_notification_receiver_billboard_idx ON kb_notification USING btree (receiver_id, isbillboard, state, startpub, endpub, creationdate);
CREATE INDEX kb_notification_user_state_idx ON kb_notification USING btree (receiver_id, notification_state);
CREATE INDEX kb_sendemailevent_event_domain_id_event_time_idx ON kb_sendemailevent USING btree (event_domain_id, event_time DESC);
CREATE INDEX kb_subscription_content_oid_event_id_idx ON kb_subscription USING btree (content_oid, event_id);
CREATE INDEX kb_tip_id_idx ON kb_tip USING btree (tip_lang, area, lower((tip_title)::text));
CREATE INDEX kb_un_user_idx ON kb_user_note USING btree (user_id, creationdate DESC);
CREATE INDEX kb_usage_domain_idx ON kb_usage_stat USING btree (domain_id, ts);
CREATE INDEX kb_user_list_item_content_oid_idx ON kb_user_list_item USING btree (oid, content_id);
CREATE INDEX kb_user_list_item_list_idx ON kb_user_list_item USING btree (userlist_id, title);
CREATE INDEX kb_user_list_owner_console_idx ON kb_user_list USING btree (owner_id, console, title);
CREATE INDEX kb_user_role_role_idx ON kb_user_role USING btree (role_id);
CREATE INDEX kb_wn_user_idx ON kb_work_note USING btree (domain_id, creationdate DESC);
CREATE INDEX kb_wnur_user_idx ON kb_work_note_user_read USING btree (user_id, work_note_id);
CREATE INDEX kcomment_referenced_content_id_date_submitted_idx ON kb_comment USING btree (referenced_content_id, date_submitted DESC);
CREATE INDEX kfile_bucket_idx ON kfile USING btree (bucketname, objectname);
CREATE INDEX kfile_storagemode_idx ON kfile USING btree (storagemode, bucketname);
CREATE INDEX kgroupmember_principal_idx ON kgroupmember USING btree (principal);
CREATE INDEX kgroupname_idx ON kgroup USING btree (lower((name)::text));
CREATE INDEX klock_lock_object_id_idx ON klock USING btree (lock_object_id);
CREATE INDEX launcher_group_domain_idx ON kb_launcher_group USING btree (domain_id, lower((name)::text));
CREATE INDEX library_domain_name_idx ON kb_cabinet USING btree (domain_id, lower((display_name)::text));
CREATE INDEX logevent_activity_idx ON logevent USING btree (event_activity_id, event_time);
CREATE INDEX logevent_addresources_idx ON logevent USING btree (event_content_id, event_type, event_time);
CREATE INDEX logevent_content_id_time_idx ON logevent USING btree (event_content_id, event_time DESC);
CREATE INDEX logevent_domain_event_time_idx ON logevent USING btree (event_domain_id, event_time);
CREATE INDEX logevent_evnet_user_time_idx ON logevent USING btree (event_user, event_time DESC);
CREATE INDEX logevent_object_id_time_idx ON logevent USING btree (event_object_id, event_time DESC);
CREATE INDEX logevent_object_idx ON logevent USING btree (event_object_id);
CREATE INDEX logevent_time_desc_idx ON logevent USING btree (event_time DESC);
CREATE INDEX logevent_time_idx ON logevent USING btree (event_time);
CREATE INDEX notification_id_idx ON kb_notification USING btree (id);
CREATE INDEX notification_receiver_id_state_datesend_idx ON kb_notification USING btree (receiver_id, state, datesend);
CREATE INDEX organization_id_idx ON organization USING btree (id);
CREATE INDEX organization_lastmoddate_idx ON organization USING btree (lastmodifieddate DESC);
CREATE INDEX organization_name_idx ON organization USING btree (lower((name)::text));
CREATE INDEX organizationaltext_content_id_idx ON organizationaltext USING btree (content_id);
CREATE INDEX orgchart_content_id_idx ON orgchart USING btree (content_id);
CREATE INDEX po_area_page_id_orden_idx ON po_diagrammable_area USING btree (page_id, orden);
CREATE INDEX po_area_po_id_idx ON po_diagrammable_area USING btree (po_id);
CREATE INDEX po_block_area_id_section_orden_idx ON po_diagrammable_block USING btree (area_id, section, orden);
CREATE INDEX po_block_banners_block_id_idx ON po_block_banners USING btree (block_id);
CREATE INDEX po_block_contact_block_id_idx ON po_block_contact USING btree (block_id);
CREATE INDEX po_block_content_list_block_id_idx ON po_block_content_list USING btree (block_id);
CREATE INDEX po_block_footer_block_id_idx ON po_block_footer USING btree (block_id);
CREATE INDEX po_block_po_id_idx ON po_diagrammable_block USING btree (po_id);
CREATE INDEX po_block_selector_block_id_idx ON po_block_selector USING btree (block_id);
CREATE INDEX po_block_site_components_block_id_idx ON po_block_site_components USING btree (block_id);
CREATE INDEX po_block_site_list_block_id_idx ON po_block_site_list USING btree (block_id);
CREATE INDEX po_block_text_block_id_idx ON po_block_text USING btree (block_id);
CREATE INDEX po_block_view_list_block_id_idx ON po_block_view_list USING btree (block_id);
CREATE INDEX po_block_view_recent_list_block_id_idx ON po_block_view_recent_list USING btree (block_id);
CREATE INDEX po_page_po_id_idx ON po_diagrammable_page USING btree (po_id);
CREATE INDEX po_page_site_id_po_id_idx ON po_diagrammable_page USING btree (site_id, po_id);
CREATE INDEX po_portalobject_domain_id_lower_idx ON po_portalobject USING btree (domain_id, lower((title)::text));
CREATE INDEX po_portalobject_domain_id_state_lastmodifieddate_idx ON po_portalobject USING btree (domain_id, state, lastmodifieddate);
CREATE INDEX po_portalobject_domain_id_state_lower_idx ON po_portalobject USING btree (domain_id, state, lower((title)::text));
CREATE INDEX po_portalobject_oid_version_idx ON po_portalobject USING btree (oid, version);
CREATE INDEX po_site_po_id_idx ON po_diagrammable_site USING btree (po_id);
CREATE INDEX po_sitelogin_site_id_page_id_visit_time_idx ON po_sitelogin USING btree (site_id, page_id, visit_time);
CREATE INDEX po_sitelogin_visit_time_idx ON po_sitelogin USING btree (visit_time);
CREATE INDEX po_siteuser_site_id_permission_user_id_idx ON po_siteuser USING btree (site_id, permission, user_id);
CREATE INDEX po_siteuserrights_site_id_user_id_idx ON po_siteuserrights USING btree (site_id, user_id);
CREATE INDEX property_content_id_idx ON property USING btree (content_id);
CREATE INDEX property_object_id_idx ON kb_object_property USING btree (object_id);
CREATE INDEX property_user_id_idx ON kb_user_property USING btree (user_id);
CREATE INDEX report_content_id_user_id_idx ON kb_report USING btree (content_id, user_id);
CREATE INDEX report_user_id_content_id_idx ON kb_report USING btree (user_id, content_id);
CREATE INDEX resource_domain_id_idx ON kresource USING btree (domain_id, lower((title)::text));
CREATE INDEX resource_modified ON kresource USING btree (domain_id, lastmodifieddate DESC);
CREATE INDEX resource_name ON kresource USING btree (domain_id, lower((name)::text));
CREATE INDEX resource_name_global ON kresource USING btree (lower((name)::text));
CREATE INDEX savedquery_user_console_idx ON savedquery USING btree (user_id, console, title);
CREATE INDEX scheduler_id_idx ON scheduler USING btree (id);
CREATE INDEX scheduler_priority_time_errorcount_idx ON scheduler USING btree (priority, "time", execute_after, error_count);
CREATE INDEX scheduler_priority_time_idx ON scheduler USING btree (priority, "time");
CREATE INDEX site_security_rule_object_idx ON po_site_securityrule USING btree (related_object_id);
CREATE UNIQUE INDEX sp_lower_case_key ON kb_system_properties USING btree (lower((key)::text));
CREATE INDEX userproperty_set_lastmodifieddate_desc_idx ON kb_user_property USING btree (user_id, uset, lastmodifieddate DESC);
CREATE INDEX users_id_idx ON users USING btree (id);
CREATE INDEX users_lastmodifieddate_idx ON users USING btree (lastmodifieddate DESC);
CREATE INDEX users_name_idx ON users USING btree (username);
CREATE INDEX vote_content_id_user_id_idx ON kb_vote USING btree (content_id, user_id);
CREATE INDEX vote_user_id_content_id_idx ON kb_vote USING btree (user_id, content_id);
ALTER TABLE ONLY kb_aclentry
    ADD CONSTRAINT acl_fk FOREIGN KEY (acl) REFERENCES kb_acl(id) ON DELETE CASCADE;
ALTER TABLE ONLY kb_security_rule
    ADD CONSTRAINT acl_fk FOREIGN KEY (acl) REFERENCES kb_acl(id) ON DELETE RESTRICT;
ALTER TABLE ONLY kb_contenttemplate
    ADD CONSTRAINT acl_fk FOREIGN KEY (acl) REFERENCES kb_acl(id) ON DELETE RESTRICT;
ALTER TABLE ONLY wf_launcher
    ADD CONSTRAINT acl_fk FOREIGN KEY (acl) REFERENCES kb_acl(id);
ALTER TABLE ONLY content
    ADD CONSTRAINT acl_fk FOREIGN KEY (acl) REFERENCES kb_acl(id) ON DELETE RESTRICT;
ALTER TABLE ONLY kb_action_rule
    ADD CONSTRAINT action_rule_domain FOREIGN KEY (domain_id) REFERENCES domain(id) ON DELETE CASCADE;
ALTER TABLE ONLY po_diagrammable_area
    ADD CONSTRAINT area_fk FOREIGN KEY (po_id) REFERENCES po_portalobject(id) ON DELETE CASCADE;
ALTER TABLE ONLY po_diagrammable_block
    ADD CONSTRAINT area_fk FOREIGN KEY (area_id) REFERENCES po_diagrammable_area(po_id) ON UPDATE CASCADE ON DELETE CASCADE;
ALTER TABLE ONLY po_area
    ADD CONSTRAINT area_fk FOREIGN KEY (po_id) REFERENCES po_portalobject(id) ON DELETE RESTRICT;
ALTER TABLE ONLY po_block
    ADD CONSTRAINT area_fk FOREIGN KEY (po_id) REFERENCES po_portalobject(id) ON DELETE RESTRICT;
ALTER TABLE ONLY kb_assignable_role
    ADD CONSTRAINT assignablerole_fk FOREIGN KEY (assignablerole_id) REFERENCES kb_role(id);
ALTER TABLE ONLY wf_activity
    ADD CONSTRAINT assigned_fk FOREIGN KEY (assigned_by) REFERENCES users(id);
ALTER TABLE ONLY kb_contentattribute
    ADD CONSTRAINT attribute_fk FOREIGN KEY (attributetemplate_id) REFERENCES kb_attributetemplate(id) ON DELETE RESTRICT;
ALTER TABLE ONLY kb_datasetattribute
    ADD CONSTRAINT attribute_fk FOREIGN KEY (attributetemplate_id) REFERENCES kb_attributetemplate(id) ON DELETE RESTRICT;
ALTER TABLE ONLY kb_attributetemplate
    ADD CONSTRAINT attribute_fk FOREIGN KEY (attribute_id) REFERENCES kb_attribute(id) ON DELETE RESTRICT;
ALTER TABLE ONLY kb_attributetemplate
    ADD CONSTRAINT attributetemplate_section_fk FOREIGN KEY (section_id) REFERENCES kb_model_section(id) ON DELETE CASCADE;
ALTER TABLE ONLY po_block_contact
    ADD CONSTRAINT block_contact_fk FOREIGN KEY (block_id) REFERENCES po_diagrammable_block(po_id) ON DELETE CASCADE;
ALTER TABLE ONLY po_diagrammable_block
    ADD CONSTRAINT block_fk FOREIGN KEY (po_id) REFERENCES po_portalobject(id) ON DELETE CASCADE;
ALTER TABLE ONLY po_block_site_list
    ADD CONSTRAINT block_fk FOREIGN KEY (block_id) REFERENCES po_diagrammable_block(po_id) ON DELETE CASCADE;
ALTER TABLE ONLY po_block_text
    ADD CONSTRAINT block_fk FOREIGN KEY (block_id) REFERENCES po_diagrammable_block(po_id) ON DELETE CASCADE;
ALTER TABLE ONLY po_block_view_list
    ADD CONSTRAINT block_fk FOREIGN KEY (block_id) REFERENCES po_diagrammable_block(po_id) ON UPDATE CASCADE ON DELETE CASCADE;
ALTER TABLE ONLY po_block_content_list
    ADD CONSTRAINT block_fk FOREIGN KEY (block_id) REFERENCES po_diagrammable_block(po_id) ON DELETE CASCADE;
ALTER TABLE ONLY po_block_cumpleanos
    ADD CONSTRAINT block_fk FOREIGN KEY (block_id) REFERENCES po_diagrammable_block(po_id) ON UPDATE CASCADE ON DELETE CASCADE;
ALTER TABLE ONLY po_block_x
    ADD CONSTRAINT block_fk FOREIGN KEY (block_id) REFERENCES po_diagrammable_block(po_id) ON UPDATE CASCADE ON DELETE CASCADE;
ALTER TABLE ONLY po_contentblock
    ADD CONSTRAINT block_fk FOREIGN KEY (block_id) REFERENCES po_diagrammable_block(po_id) ON DELETE CASCADE;
ALTER TABLE ONLY po_block_banners
    ADD CONSTRAINT block_fk FOREIGN KEY (block_id) REFERENCES po_block_view_list(block_id) ON DELETE CASCADE;
ALTER TABLE ONLY po_block_image_viewer
    ADD CONSTRAINT block_fk FOREIGN KEY (block_id) REFERENCES po_diagrammable_block(po_id) ON DELETE RESTRICT;
ALTER TABLE ONLY po_block_select_list
    ADD CONSTRAINT block_fk FOREIGN KEY (block_id) REFERENCES po_diagrammable_block(po_id) ON DELETE RESTRICT;
ALTER TABLE ONLY po_block_search_external
    ADD CONSTRAINT block_fk FOREIGN KEY (block_id) REFERENCES po_diagrammable_block(po_id) ON DELETE RESTRICT;
ALTER TABLE ONLY po_block_wall_viewer
    ADD CONSTRAINT block_fk FOREIGN KEY (block_id) REFERENCES po_block_view_list(block_id) ON DELETE CASCADE;
ALTER TABLE ONLY po_block
    ADD CONSTRAINT block_fk FOREIGN KEY (po_id) REFERENCES po_portalobject(id) ON DELETE RESTRICT;
ALTER TABLE ONLY po_block_footer
    ADD CONSTRAINT block_footer_fk FOREIGN KEY (block_id) REFERENCES po_diagrammable_block(po_id) ON UPDATE CASCADE ON DELETE CASCADE;
ALTER TABLE ONLY po_block_gallery_viewer
    ADD CONSTRAINT block_gallery_viewer_fk FOREIGN KEY (block_id) REFERENCES po_block_view_list(block_id) ON DELETE CASCADE;
ALTER TABLE ONLY po_diagrammable_block
    ADD CONSTRAINT block_image_fk FOREIGN KEY (block_image) REFERENCES idoc(content_id) ON DELETE SET NULL;
ALTER TABLE ONLY po_block_view_recent_list
    ADD CONSTRAINT block_recent_view_fk FOREIGN KEY (block_id) REFERENCES po_block_view_list(block_id) ON DELETE CASCADE;
ALTER TABLE ONLY po_block_site_components
    ADD CONSTRAINT block_sc_fk FOREIGN KEY (block_id) REFERENCES po_diagrammable_block(po_id) ON DELETE CASCADE;
ALTER TABLE ONLY po_block_selector
    ADD CONSTRAINT block_selector_fk FOREIGN KEY (block_id) REFERENCES po_diagrammable_block(po_id) ON UPDATE CASCADE ON DELETE CASCADE;
ALTER TABLE ONLY kb_cabinet_reader
    ADD CONSTRAINT cabinet_reader_cabinet FOREIGN KEY (cabinet_id) REFERENCES kb_cabinet(id) ON DELETE CASCADE;
ALTER TABLE ONLY kb_cabinet_reader
    ADD CONSTRAINT cabinet_reader_group FOREIGN KEY (group_id) REFERENCES kgroup(id) ON DELETE CASCADE;
ALTER TABLE ONLY classifiercontent
    ADD CONSTRAINT classifier_fk FOREIGN KEY (classifier_id) REFERENCES kb_classifier(id) ON DELETE RESTRICT;
ALTER TABLE ONLY classification
    ADD CONSTRAINT classifier_fk FOREIGN KEY (classifier_id) REFERENCES kb_classifier(id) ON DELETE RESTRICT;
ALTER TABLE ONLY datasetclassifier
    ADD CONSTRAINT classifier_fk FOREIGN KEY (classifier_id) REFERENCES kb_classifier(id) ON DELETE RESTRICT;
ALTER TABLE ONLY memberclassification
    ADD CONSTRAINT classifier_fk FOREIGN KEY (classifier_id) REFERENCES kb_classifier(id) ON DELETE RESTRICT;
ALTER TABLE ONLY kb_classifiertemplate
    ADD CONSTRAINT classifier_fk FOREIGN KEY (classifier_id) REFERENCES kb_classifier(id) ON DELETE RESTRICT;
ALTER TABLE ONLY kb_classifiertemplate
    ADD CONSTRAINT classifiertemplate_section_fk FOREIGN KEY (section_id) REFERENCES kb_model_section(id) ON DELETE CASCADE;
ALTER TABLE ONLY kb_comment
    ADD CONSTRAINT comment_responses_fk FOREIGN KEY (parent_comment) REFERENCES kb_comment(content_id) ON DELETE CASCADE;
ALTER TABLE ONLY content
    ADD CONSTRAINT content_acl_fkey FOREIGN KEY (acl) REFERENCES kb_acl(id) ON DELETE RESTRICT;
ALTER TABLE ONLY property
    ADD CONSTRAINT content_fk FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE CASCADE;
ALTER TABLE ONLY contentresource
    ADD CONSTRAINT content_fk FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE RESTRICT;
ALTER TABLE ONLY idocsection
    ADD CONSTRAINT content_fk FOREIGN KEY (idoc_id) REFERENCES idoc(content_id) ON DELETE RESTRICT;
ALTER TABLE ONLY kb_notification
    ADD CONSTRAINT content_fk FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE SET NULL;
ALTER TABLE ONLY drb_question
    ADD CONSTRAINT content_fk FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE CASCADE;
ALTER TABLE ONLY drb_answer
    ADD CONSTRAINT content_fk FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE CASCADE;
ALTER TABLE ONLY orgchart
    ADD CONSTRAINT content_fk FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE RESTRICT;
ALTER TABLE ONLY wf_activity
    ADD CONSTRAINT content_fk FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE CASCADE;
ALTER TABLE ONLY classification
    ADD CONSTRAINT content_fk FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE CASCADE;
ALTER TABLE ONLY kb_comment
    ADD CONSTRAINT content_fk FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE CASCADE;
ALTER TABLE ONLY po_diagrammable_page
    ADD CONSTRAINT content_fk FOREIGN KEY (content_link) REFERENCES content(id) ON DELETE SET NULL;
ALTER TABLE ONLY po_diagrammable_block
    ADD CONSTRAINT content_fk FOREIGN KEY (content_link) REFERENCES content(id) ON DELETE SET NULL;
ALTER TABLE ONLY po_contentblock
    ADD CONSTRAINT content_fk FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE RESTRICT;
ALTER TABLE ONLY po_viewcontent
    ADD CONSTRAINT content_fk FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE CASCADE;
ALTER TABLE ONLY idoc
    ADD CONSTRAINT content_fk FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE CASCADE;
ALTER TABLE ONLY organizationaltext
    ADD CONSTRAINT content_fk FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE CASCADE;
ALTER TABLE ONLY kb_tree_idoc
    ADD CONSTRAINT content_fk FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE CASCADE;
ALTER TABLE ONLY po_page
    ADD CONSTRAINT content_fk FOREIGN KEY (content_link) REFERENCES content(id) ON DELETE SET NULL;
ALTER TABLE ONLY kb_vote
    ADD CONSTRAINT content_id_fk FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE CASCADE;
ALTER TABLE ONLY kb_report
    ADD CONSTRAINT content_id_fk FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE CASCADE;
ALTER TABLE ONLY contentstat
    ADD CONSTRAINT content_id_fk FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE RESTRICT;
ALTER TABLE ONLY contentproperties
    ADD CONSTRAINT content_id_fk FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE CASCADE;
ALTER TABLE ONLY classifiercontent
    ADD CONSTRAINT contentclass_fk FOREIGN KEY (contentclass_id) REFERENCES contentclass(id) ON DELETE RESTRICT;
ALTER TABLE ONLY contentresource
    ADD CONSTRAINT contentresource_group_id_fkey FOREIGN KEY (group_id) REFERENCES kb_resource_group(id) ON DELETE RESTRICT;
ALTER TABLE ONLY content
    ADD CONSTRAINT contenttemplate_fk FOREIGN KEY (contenttemplate) REFERENCES kb_contenttemplate(id) ON DELETE RESTRICT;
ALTER TABLE ONLY kb_classifiertemplate
    ADD CONSTRAINT contenttemplate_fk FOREIGN KEY (contenttemplate_id) REFERENCES kb_contenttemplate(id) ON DELETE CASCADE;
ALTER TABLE ONLY kb_contentattribute
    ADD CONSTRAINT contenttemplate_fk FOREIGN KEY (contenttemplate_id) REFERENCES kb_contenttemplate(id) ON DELETE RESTRICT;
ALTER TABLE ONLY wf_launcher
    ADD CONSTRAINT contenttemplate_fk FOREIGN KEY (contenttemplate_id) REFERENCES kb_contenttemplate(id) ON DELETE CASCADE;
ALTER TABLE ONLY kb_content_relation
    ADD CONSTRAINT cr_source_fk FOREIGN KEY (source_id) REFERENCES content(id) ON DELETE CASCADE;
ALTER TABLE ONLY kb_content_relation
    ADD CONSTRAINT cr_target_fk FOREIGN KEY (target_id) REFERENCES content(id) ON DELETE CASCADE;
ALTER TABLE ONLY kb_content_relation
    ADD CONSTRAINT cr_template_fk FOREIGN KEY (template_id) REFERENCES kb_relation_template(id) ON DELETE RESTRICT;
ALTER TABLE ONLY kb_classifier
    ADD CONSTRAINT dataset2_fk FOREIGN KEY (dataset2_id) REFERENCES dataset(id) ON DELETE RESTRICT;
ALTER TABLE ONLY kb_classifier
    ADD CONSTRAINT dataset3_fk FOREIGN KEY (dataset3_id) REFERENCES dataset(id) ON DELETE RESTRICT;
ALTER TABLE ONLY datasetmember
    ADD CONSTRAINT dataset_fk FOREIGN KEY (dataset_id) REFERENCES dataset(id) ON DELETE RESTRICT;
ALTER TABLE ONLY kb_classifier
    ADD CONSTRAINT dataset_fk FOREIGN KEY (dataset_id) REFERENCES dataset(id) ON DELETE RESTRICT;
ALTER TABLE ONLY datasetclassifier
    ADD CONSTRAINT dataset_fk FOREIGN KEY (dataset_id) REFERENCES dataset(id) ON DELETE RESTRICT;
ALTER TABLE ONLY kb_datasetattribute
    ADD CONSTRAINT dataset_fk FOREIGN KEY (dataset_id) REFERENCES dataset(id) ON DELETE RESTRICT;
ALTER TABLE ONLY classification
    ADD CONSTRAINT datasetmember_fk FOREIGN KEY (datasetmember_id) REFERENCES datasetmember(id) ON DELETE RESTRICT;
ALTER TABLE ONLY kfile
    ADD CONSTRAINT des_user_fk FOREIGN KEY (uploadeduser) REFERENCES users(id);
ALTER TABLE ONLY profile
    ADD CONSTRAINT domain_fk FOREIGN KEY (domain_id) REFERENCES domain(id) ON DELETE RESTRICT;
ALTER TABLE ONLY kb_security_rule
    ADD CONSTRAINT domain_fk FOREIGN KEY (domain_id) REFERENCES domain(id) ON DELETE RESTRICT;
ALTER TABLE ONLY kb_enotirule
    ADD CONSTRAINT domain_fk FOREIGN KEY (domain_id) REFERENCES domain(id) ON DELETE RESTRICT;
ALTER TABLE ONLY organization
    ADD CONSTRAINT domain_fk FOREIGN KEY (domain_id) REFERENCES domain(id) ON DELETE RESTRICT;
ALTER TABLE ONLY kresource
    ADD CONSTRAINT domain_fk FOREIGN KEY (domain_id) REFERENCES domain(id) ON DELETE RESTRICT;
ALTER TABLE ONLY kb_contenttemplate
    ADD CONSTRAINT domain_fk FOREIGN KEY (domain_id) REFERENCES domain(id) ON DELETE CASCADE;
ALTER TABLE ONLY content
    ADD CONSTRAINT domain_fk FOREIGN KEY (domain_id) REFERENCES domain(id) ON DELETE RESTRICT;
ALTER TABLE ONLY dataset
    ADD CONSTRAINT domain_fk FOREIGN KEY (domain_id) REFERENCES domain(id) ON DELETE CASCADE;
ALTER TABLE ONLY datasetmember
    ADD CONSTRAINT domain_fk FOREIGN KEY (domain_id) REFERENCES domain(id) ON DELETE RESTRICT;
ALTER TABLE ONLY kb_classifier
    ADD CONSTRAINT domain_fk FOREIGN KEY (domain_id) REFERENCES domain(id) ON DELETE CASCADE;
ALTER TABLE ONLY kb_notification
    ADD CONSTRAINT domain_fk FOREIGN KEY (domain_id) REFERENCES domain(id) ON DELETE CASCADE;
ALTER TABLE ONLY wf_procedure
    ADD CONSTRAINT domain_fk FOREIGN KEY (domain_id) REFERENCES domain(id) ON DELETE CASCADE;
ALTER TABLE ONLY wf_launcher
    ADD CONSTRAINT domain_fk FOREIGN KEY (domain_id) REFERENCES domain(id) ON DELETE CASCADE;
ALTER TABLE ONLY kb_email_template
    ADD CONSTRAINT domain_fk FOREIGN KEY (domain_id) REFERENCES domain(id) ON DELETE CASCADE;
ALTER TABLE ONLY kb_attribute
    ADD CONSTRAINT domain_fk FOREIGN KEY (domain_id) REFERENCES domain(id) ON DELETE CASCADE;
ALTER TABLE ONLY kb_cabinet
    ADD CONSTRAINT domain_fk FOREIGN KEY (domain_id) REFERENCES domain(id) ON DELETE CASCADE;
ALTER TABLE ONLY po_portalobject
    ADD CONSTRAINT domain_fk FOREIGN KEY (domain_id) REFERENCES domain(id) ON DELETE RESTRICT;
ALTER TABLE ONLY kb_tree_file
    ADD CONSTRAINT domain_fk FOREIGN KEY (domain_id) REFERENCES domain(id) ON DELETE CASCADE;
ALTER TABLE ONLY kb_report_subscription
    ADD CONSTRAINT domain_fk FOREIGN KEY (domain_id) REFERENCES domain(id) ON DELETE RESTRICT;
ALTER TABLE ONLY kb_searcher_homeblock
    ADD CONSTRAINT domain_fk FOREIGN KEY (domain_id) REFERENCES domain(id) ON DELETE RESTRICT;
ALTER TABLE ONLY principal
    ADD CONSTRAINT domain_fk FOREIGN KEY (domain_id) REFERENCES domain(id) ON DELETE CASCADE;
ALTER TABLE ONLY entity
    ADD CONSTRAINT domain_fk FOREIGN KEY (domain_id) REFERENCES domain(id) ON DELETE CASCADE;
ALTER TABLE ONLY kb_form
    ADD CONSTRAINT domain_fk FOREIGN KEY (domain_id) REFERENCES domain(id) ON DELETE CASCADE;
ALTER TABLE ONLY domain
    ADD CONSTRAINT domain_logo_fk FOREIGN KEY (logo) REFERENCES kfile(resource_id) ON DELETE SET NULL;
ALTER TABLE ONLY kb_domain_settings
    ADD CONSTRAINT ds_domain_fk FOREIGN KEY (domain_id) REFERENCES domain(id) ON DELETE CASCADE;
ALTER TABLE ONLY kb_ds_element_template
    ADD CONSTRAINT dse_attribute_fk FOREIGN KEY (attribute_id) REFERENCES kb_attribute(id);
ALTER TABLE ONLY kb_ds_element_template
    ADD CONSTRAINT dse_classifier_fk FOREIGN KEY (classifier_id) REFERENCES kb_classifier(id);
ALTER TABLE ONLY kb_ds_element_template
    ADD CONSTRAINT dse_dataset_fk FOREIGN KEY (dataset_id) REFERENCES dataset(id);
ALTER TABLE ONLY person
    ADD CONSTRAINT entity_fk FOREIGN KEY (entity_id) REFERENCES entity(id);
ALTER TABLE ONLY profile
    ADD CONSTRAINT entity_fk FOREIGN KEY (entity) REFERENCES entity(id) ON DELETE CASCADE;
ALTER TABLE ONLY datasetmember
    ADD CONSTRAINT entity_fk FOREIGN KEY (entity_id) REFERENCES entity(id) ON DELETE RESTRICT;
ALTER TABLE ONLY externalresource
    ADD CONSTRAINT externalresource_fk FOREIGN KEY (resource_id) REFERENCES kresource(id) ON DELETE RESTRICT;
ALTER TABLE ONLY kb_facet_wrapper
    ADD CONSTRAINT facet_domain_fk FOREIGN KEY (domain_id) REFERENCES domain(id) ON DELETE CASCADE;
ALTER TABLE ONLY resourcefile
    ADD CONSTRAINT file_fk FOREIGN KEY (file_id) REFERENCES kresource(id) ON DELETE RESTRICT;
ALTER TABLE ONLY galleryfile
    ADD CONSTRAINT file_fk FOREIGN KEY (file_id) REFERENCES kfile(resource_id) ON DELETE RESTRICT;
ALTER TABLE ONLY kfile
    ADD CONSTRAINT file_fk FOREIGN KEY (resource_id) REFERENCES kresource(id) ON DELETE CASCADE;
ALTER TABLE ONLY person
    ADD CONSTRAINT file_fk FOREIGN KEY (photo) REFERENCES kfile(resource_id) ON DELETE SET NULL;
ALTER TABLE ONLY kb_file_proxy
    ADD CONSTRAINT fileproxy_loader_fk FOREIGN KEY (file_loader) REFERENCES kb_file_loader(id) ON DELETE RESTRICT;
ALTER TABLE ONLY kb_file_proxy
    ADD CONSTRAINT fileproxy_resource_fk FOREIGN KEY (resource_id) REFERENCES kresource(id) ON DELETE RESTRICT;
ALTER TABLE ONLY authorities
    ADD CONSTRAINT fk_authorities_users FOREIGN KEY (username) REFERENCES users(username);
ALTER TABLE ONLY po_diagrammable_site
    ADD CONSTRAINT footer_block_fk FOREIGN KEY (footer_block_id) REFERENCES po_diagrammable_block(po_id) ON DELETE SET NULL;
ALTER TABLE ONLY gallery
    ADD CONSTRAINT gallery_fk FOREIGN KEY (resource_id) REFERENCES kresource(id) ON DELETE RESTRICT;
ALTER TABLE ONLY galleryfile
    ADD CONSTRAINT gallery_fk FOREIGN KEY (gallery_id) REFERENCES gallery(resource_id) ON DELETE RESTRICT;
ALTER TABLE ONLY kresource
    ADD CONSTRAINT group_fk FOREIGN KEY (group_id) REFERENCES kb_resource_group(id) ON DELETE RESTRICT;
ALTER TABLE ONLY dataset
    ADD CONSTRAINT group_fk FOREIGN KEY (group_id) REFERENCES kgroup(id) ON DELETE RESTRICT;
ALTER TABLE ONLY kb_organizationaldata
    ADD CONSTRAINT group_fk FOREIGN KEY (group_id) REFERENCES kgroup(id) ON DELETE CASCADE;
ALTER TABLE ONLY kb_securitydata
    ADD CONSTRAINT group_fk FOREIGN KEY (group_id) REFERENCES kgroup(id) ON DELETE CASCADE;
ALTER TABLE ONLY kgroupmember
    ADD CONSTRAINT group_fk FOREIGN KEY (kgroup) REFERENCES kgroup(id) ON DELETE CASCADE;
ALTER TABLE ONLY datasetmember
    ADD CONSTRAINT group_fk FOREIGN KEY (group_id) REFERENCES kgroup(id) ON DELETE RESTRICT;
ALTER TABLE ONLY kb_role
    ADD CONSTRAINT group_fk FOREIGN KEY (group_id) REFERENCES kgroup(id) ON DELETE RESTRICT;
ALTER TABLE ONLY kb_member_role
    ADD CONSTRAINT group_fk FOREIGN KEY (group_id) REFERENCES kgroup(id) ON DELETE RESTRICT;
ALTER TABLE ONLY kb_group_role
    ADD CONSTRAINT grouprole_group_fk FOREIGN KEY (group_id) REFERENCES kgroup(id);
ALTER TABLE ONLY kb_group_role
    ADD CONSTRAINT grouprole_role_fk FOREIGN KEY (role_id) REFERENCES kb_role(id) ON DELETE CASCADE;
ALTER TABLE ONLY po_diagrammable_site
    ADD CONSTRAINT header_block_fk FOREIGN KEY (header_block_id) REFERENCES po_diagrammable_block(po_id) ON DELETE SET NULL;
ALTER TABLE ONLY po_diagrammable_block
    ADD CONSTRAINT image_fk FOREIGN KEY (image_id) REFERENCES kfile(resource_id) ON DELETE RESTRICT;
ALTER TABLE ONLY po_block_image_viewer
    ADD CONSTRAINT image_fk FOREIGN KEY (imageviewer_id) REFERENCES idoc(content_id) ON DELETE SET NULL;
ALTER TABLE ONLY kb_cronjob
    ADD CONSTRAINT kb_cronjob_modifieduser_fk FOREIGN KEY (lastmodifieduser) REFERENCES users(id);
ALTER TABLE ONLY kb_form_data
    ADD CONSTRAINT kb_form_data_content_id_fkey FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE CASCADE;
ALTER TABLE ONLY kb_form_data
    ADD CONSTRAINT kb_form_data_form_id_fkey FOREIGN KEY (form_id) REFERENCES kb_form(id) ON DELETE RESTRICT;
ALTER TABLE ONLY kb_form_data
    ADD CONSTRAINT kb_form_data_lastmodifieduser_fkey FOREIGN KEY (lastmodifieduser) REFERENCES users(id) ON DELETE SET NULL;
ALTER TABLE ONLY kb_form_template
    ADD CONSTRAINT kb_form_template_contenttemplate_id_fkey FOREIGN KEY (contenttemplate_id) REFERENCES kb_contenttemplate(id) ON DELETE CASCADE;
ALTER TABLE ONLY kb_form_template
    ADD CONSTRAINT kb_form_template_form_id_fkey FOREIGN KEY (form_id) REFERENCES kb_form(id) ON DELETE CASCADE;
ALTER TABLE ONLY kb_launcher_group
    ADD CONSTRAINT kb_launcher_group_domain_id_fkey FOREIGN KEY (domain_id) REFERENCES domain(id) ON DELETE CASCADE;
ALTER TABLE ONLY kb_launcher_group
    ADD CONSTRAINT kb_launcher_group_lastmodifieduser_fkey FOREIGN KEY (lastmodifieduser) REFERENCES users(id) ON DELETE SET NULL;
ALTER TABLE ONLY kb_object_property
    ADD CONSTRAINT kb_object_property_domain_id_fkey FOREIGN KEY (domain_id) REFERENCES domain(id) ON DELETE CASCADE;
ALTER TABLE ONLY kb_preference_domain
    ADD CONSTRAINT kb_preference_domain_domain_id_fkey FOREIGN KEY (domain_id) REFERENCES domain(id) ON DELETE CASCADE;
ALTER TABLE ONLY kb_relation_template
    ADD CONSTRAINT kb_relation_template_domain_id_fkey FOREIGN KEY (domain_id) REFERENCES domain(id);
ALTER TABLE ONLY kb_relation_template
    ADD CONSTRAINT kb_relation_template_domain_id_fkey1 FOREIGN KEY (domain_id) REFERENCES domain(id);
ALTER TABLE ONLY kb_relation_template
    ADD CONSTRAINT kb_relation_template_lastmodifieduser_fkey FOREIGN KEY (lastmodifieduser) REFERENCES users(id);
ALTER TABLE ONLY kb_relation_template
    ADD CONSTRAINT kb_relation_template_lastmodifieduser_fkey1 FOREIGN KEY (lastmodifieduser) REFERENCES users(id);
ALTER TABLE ONLY kb_tree_file
    ADD CONSTRAINT kb_tree_file_previousversion_fkey FOREIGN KEY (prev_version) REFERENCES kb_tree_file(id);
ALTER TABLE ONLY kb_user_list
    ADD CONSTRAINT kb_user_list_domain_id_fkey FOREIGN KEY (domain_id) REFERENCES domain(id) ON DELETE CASCADE;
ALTER TABLE ONLY kb_user_list_item
    ADD CONSTRAINT kb_user_list_item_content_id_fkey FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE SET NULL;
ALTER TABLE ONLY kb_user_list_item
    ADD CONSTRAINT kb_user_list_item_datasetmember_id_fkey FOREIGN KEY (datasetmember_id) REFERENCES datasetmember(id) ON DELETE SET NULL;
ALTER TABLE ONLY kb_user_list_item
    ADD CONSTRAINT kb_user_list_item_domain_id_fkey FOREIGN KEY (domain_id) REFERENCES domain(id) ON DELETE CASCADE;
ALTER TABLE ONLY kb_user_list_item
    ADD CONSTRAINT kb_user_list_item_lastmodifieduser_fkey FOREIGN KEY (lastmodifieduser) REFERENCES users(id) ON DELETE SET NULL;
ALTER TABLE ONLY kb_user_list_item
    ADD CONSTRAINT kb_user_list_item_owner_id_fkey FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE;
ALTER TABLE ONLY kb_user_list_item
    ADD CONSTRAINT kb_user_list_item_user_id_fkey FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL;
ALTER TABLE ONLY kb_user_list_item
    ADD CONSTRAINT kb_user_list_item_userlist_id_fkey FOREIGN KEY (userlist_id) REFERENCES kb_user_list(id) ON DELETE CASCADE;
ALTER TABLE ONLY kb_user_list
    ADD CONSTRAINT kb_user_list_lastmodifieduser_fkey FOREIGN KEY (lastmodifieduser) REFERENCES users(id) ON DELETE SET NULL;
ALTER TABLE ONLY kb_user_list
    ADD CONSTRAINT kb_user_list_owner_id_fkey FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE;
ALTER TABLE ONLY kb_userlistclassification
    ADD CONSTRAINT kb_userlistclassification_classifier_id_fkey FOREIGN KEY (classifier_id) REFERENCES kb_classifier(id) ON DELETE RESTRICT;
ALTER TABLE ONLY kb_userlistclassification
    ADD CONSTRAINT kb_userlistclassification_datasetmember_id_fkey FOREIGN KEY (datasetmember_id) REFERENCES datasetmember(id) ON DELETE RESTRICT;
ALTER TABLE ONLY kb_userlistclassification
    ADD CONSTRAINT kb_userlistclassification_user_list_item_id_fkey FOREIGN KEY (user_list_item_id) REFERENCES kb_user_list_item(id) ON DELETE CASCADE;
ALTER TABLE ONLY kb_worknote_principal
    ADD CONSTRAINT kb_worknote_principal_note_id_fkey FOREIGN KEY (note_id) REFERENCES kb_work_note(id) ON DELETE CASCADE;
ALTER TABLE ONLY kb_worknote_principal
    ADD CONSTRAINT kb_worknote_principal_principal_id_fkey FOREIGN KEY (principal_id) REFERENCES principal(id) ON DELETE CASCADE;
ALTER TABLE ONLY contentproperties
    ADD CONSTRAINT lastmodifieduser_fk FOREIGN KEY (lastmodifieduser) REFERENCES users(id) ON DELETE CASCADE;
ALTER TABLE ONLY kb_cabinet
    ADD CONSTRAINT lastmodifieduser_fk FOREIGN KEY (lastmodifieduser) REFERENCES users(id);
ALTER TABLE ONLY wf_procedure
    ADD CONSTRAINT lastmodifieduser_fk FOREIGN KEY (lastmodifieduser) REFERENCES users(id) ON DELETE RESTRICT;
ALTER TABLE ONLY kb_classifiertemplate
    ADD CONSTRAINT member_fk FOREIGN KEY (root_id) REFERENCES datasetmember(id) ON DELETE RESTRICT;
ALTER TABLE ONLY datasetmember
    ADD CONSTRAINT member_securityrule_fk FOREIGN KEY (securityrule_id) REFERENCES kb_security_rule(id);
ALTER TABLE ONLY kb_member_role
    ADD CONSTRAINT memberrole_entity_fk FOREIGN KEY (entity_id) REFERENCES datasetmember(id);
ALTER TABLE ONLY kb_member_role
    ADD CONSTRAINT memberrole_role_fk FOREIGN KEY (role_id) REFERENCES kb_role(id) ON DELETE CASCADE;
ALTER TABLE ONLY kb_member_role
    ADD CONSTRAINT memberrole_securityrule_fk FOREIGN KEY (securityrule_id) REFERENCES kb_security_rule(id);
ALTER TABLE ONLY kb_model_section
    ADD CONSTRAINT modelsection_template_fk FOREIGN KEY (contenttemplate_id) REFERENCES kb_contenttemplate(id) ON DELETE CASCADE;
ALTER TABLE ONLY kb_enotirule
    ADD CONSTRAINT owner_fk FOREIGN KEY (owner) REFERENCES users(id) ON DELETE CASCADE;
ALTER TABLE ONLY po_diagrammable_page
    ADD CONSTRAINT page_fk FOREIGN KEY (po_id) REFERENCES po_portalobject(id) ON DELETE RESTRICT;
ALTER TABLE ONLY po_diagrammable_area
    ADD CONSTRAINT page_fk FOREIGN KEY (page_id) REFERENCES po_diagrammable_page(po_id) ON DELETE CASCADE;
ALTER TABLE ONLY po_diagrammable_block
    ADD CONSTRAINT page_fk FOREIGN KEY (page_link) REFERENCES po_diagrammable_page(po_id);
ALTER TABLE ONLY po_page
    ADD CONSTRAINT page_fk FOREIGN KEY (po_id) REFERENCES po_portalobject(id) ON DELETE RESTRICT;
ALTER TABLE ONLY po_area
    ADD CONSTRAINT page_fk FOREIGN KEY (page_id) REFERENCES po_page(po_id) ON DELETE CASCADE;
ALTER TABLE ONLY po_diagrammable_site
    ADD CONSTRAINT page_header_fk FOREIGN KEY (page_header_footer_id) REFERENCES po_diagrammable_page(po_id);
ALTER TABLE ONLY datasetmember
    ADD CONSTRAINT parent_fk FOREIGN KEY (parent) REFERENCES datasetmember(id) ON DELETE RESTRICT;
ALTER TABLE ONLY po_portalobject
    ADD CONSTRAINT parent_fk FOREIGN KEY (parent_id) REFERENCES po_portalobject(id) ON DELETE RESTRICT;
ALTER TABLE ONLY kb_tree_file
    ADD CONSTRAINT parent_fk FOREIGN KEY (parent_id) REFERENCES kb_tree_file(id) ON DELETE CASCADE;
ALTER TABLE ONLY kb_classifiertemplate
    ADD CONSTRAINT parent_fk FOREIGN KEY (parent_id) REFERENCES kb_classifier(id) ON DELETE RESTRICT;
ALTER TABLE ONLY kb_attributetemplate
    ADD CONSTRAINT parent_fk FOREIGN KEY (parent_id) REFERENCES kb_classifier(id) ON DELETE RESTRICT;
ALTER TABLE ONLY kb_organizationaldata
    ADD CONSTRAINT person_fk FOREIGN KEY (person_id) REFERENCES person(entity_id) ON DELETE CASCADE;
ALTER TABLE ONLY kb_securitydata
    ADD CONSTRAINT person_fk FOREIGN KEY (person_id) REFERENCES person(entity_id) ON DELETE CASCADE;
ALTER TABLE ONLY po_area
    ADD CONSTRAINT po_area_parent_area_id_fkey FOREIGN KEY (parent_area_id) REFERENCES po_area(po_id) ON DELETE CASCADE;
ALTER TABLE ONLY po_diagrammable_site
    ADD CONSTRAINT po_site_site_image_fkey FOREIGN KEY (site_image) REFERENCES po_portalobject(id) ON DELETE RESTRICT;
ALTER TABLE ONLY content
    ADD CONSTRAINT prev_version_fk FOREIGN KEY (prev_version) REFERENCES content(id) ON DELETE RESTRICT;
ALTER TABLE ONLY kgroup
    ADD CONSTRAINT principal_fk FOREIGN KEY (id) REFERENCES principal(id) ON DELETE RESTRICT;
ALTER TABLE ONLY kb_enotirule_principal
    ADD CONSTRAINT principal_fk FOREIGN KEY (principal_id) REFERENCES principal(id) ON DELETE CASCADE;
ALTER TABLE ONLY kgroupmember
    ADD CONSTRAINT principal_fk FOREIGN KEY (principal) REFERENCES principal(id) ON DELETE CASCADE;
ALTER TABLE ONLY kb_aclentry
    ADD CONSTRAINT principal_fk FOREIGN KEY (principal) REFERENCES principal(id) ON DELETE CASCADE;
ALTER TABLE ONLY wf_launcher
    ADD CONSTRAINT procedure_fk FOREIGN KEY (procedure_id) REFERENCES wf_procedure(id);
ALTER TABLE ONLY wf_process
    ADD CONSTRAINT procedure_fk FOREIGN KEY (procedure_id) REFERENCES wf_procedure(id);
ALTER TABLE ONLY wf_activity
    ADD CONSTRAINT process_fk FOREIGN KEY (process_id) REFERENCES wf_process(id);
ALTER TABLE ONLY userprofile
    ADD CONSTRAINT profile_fk FOREIGN KEY (id) REFERENCES profile(id);
ALTER TABLE ONLY drb_answer
    ADD CONSTRAINT question_fk FOREIGN KEY (question_id) REFERENCES drb_question(content_id) ON DELETE CASCADE;
ALTER TABLE ONLY kb_cabinet
    ADD CONSTRAINT reader_fk FOREIGN KEY (reader_group) REFERENCES kgroup(id) ON DELETE CASCADE;
ALTER TABLE ONLY kb_notification
    ADD CONSTRAINT receiver_fk FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE CASCADE;
ALTER TABLE ONLY kb_comment
    ADD CONSTRAINT referenced_content_id_fk FOREIGN KEY (referenced_content_id) REFERENCES content(id) ON DELETE CASCADE;
ALTER TABLE ONLY kb_relation_target
    ADD CONSTRAINT relation_fk FOREIGN KEY (relationtemplate_id) REFERENCES kb_relation_template(id) ON DELETE CASCADE;
ALTER TABLE ONLY kb_report_subscription
    ADD CONSTRAINT report_subscription_user_fk FOREIGN KEY (usr) REFERENCES users(id) ON DELETE CASCADE;
ALTER TABLE ONLY resourcefile
    ADD CONSTRAINT resource_fk FOREIGN KEY (resource_id) REFERENCES kresource(id) ON DELETE RESTRICT;
ALTER TABLE ONLY contentresource
    ADD CONSTRAINT resource_fk FOREIGN KEY (resource_id) REFERENCES kresource(id) ON DELETE RESTRICT;
ALTER TABLE ONLY idocsectionresource
    ADD CONSTRAINT resource_fk FOREIGN KEY (resource_id) REFERENCES kresource(id) ON DELETE RESTRICT;
ALTER TABLE ONLY kb_tree_file
    ADD CONSTRAINT resource_id_fk FOREIGN KEY (resource_id) REFERENCES kresource(id) ON DELETE RESTRICT;
ALTER TABLE ONLY kb_resource_group
    ADD CONSTRAINT resourcegroup_createuser_fkey FOREIGN KEY (createuser) REFERENCES users(id) ON DELETE RESTRICT;
ALTER TABLE ONLY kb_resource_group
    ADD CONSTRAINT resourcegroup_domain_id_fkey FOREIGN KEY (domain_id) REFERENCES domain(id) ON DELETE CASCADE;
ALTER TABLE ONLY kb_contentresourcegroup
    ADD CONSTRAINT resourcegroup_fk FOREIGN KEY (group_id) REFERENCES kb_resource_group(id) ON DELETE RESTRICT;
ALTER TABLE ONLY kb_resource_group
    ADD CONSTRAINT resourcegroup_lastmodifieduser_fkey FOREIGN KEY (lastmodifieduser) REFERENCES users(id) ON DELETE RESTRICT;
ALTER TABLE ONLY kb_classifiertemplate
    ADD CONSTRAINT reverseof_fk FOREIGN KEY (reverseof_id) REFERENCES kb_classifiertemplate(id) ON DELETE RESTRICT;
ALTER TABLE ONLY kb_role
    ADD CONSTRAINT role_classifier_fk FOREIGN KEY (classifier_id) REFERENCES kb_classifier(id);
ALTER TABLE ONLY kb_role
    ADD CONSTRAINT role_domain_fk FOREIGN KEY (domain_id) REFERENCES domain(id);
ALTER TABLE ONLY kb_assignable_role
    ADD CONSTRAINT role_fk FOREIGN KEY (role_id) REFERENCES kb_role(id) ON DELETE CASCADE;
ALTER TABLE ONLY kb_enotirule_role
    ADD CONSTRAINT role_fk FOREIGN KEY (role_id) REFERENCES kb_role(id) ON DELETE CASCADE;
ALTER TABLE ONLY kb_role
    ADD CONSTRAINT role_modifieduser_fk FOREIGN KEY (lastmodifieduser) REFERENCES users(id);
ALTER TABLE ONLY kb_role
    ADD CONSTRAINT rolesecurityrule__fk FOREIGN KEY (securityrule_id) REFERENCES kb_security_rule(id) ON DELETE RESTRICT;
ALTER TABLE ONLY kb_content_rsbycriteria
    ADD CONSTRAINT rsbycriteria_source_fk FOREIGN KEY (source_id) REFERENCES content(id) ON DELETE CASCADE;
ALTER TABLE ONLY kb_content_rsbycriteria
    ADD CONSTRAINT rsbycriteria_template_fk FOREIGN KEY (template_id) REFERENCES kb_rsbycriteria_template(id) ON DELETE CASCADE;
ALTER TABLE ONLY kb_rsbycriteria_template
    ADD CONSTRAINT rsbycriteria_template_source_fk FOREIGN KEY (sourcetemplate_id) REFERENCES kb_contenttemplate(id) ON DELETE CASCADE;
ALTER TABLE ONLY kb_relation_template
    ADD CONSTRAINT rt_source_fk FOREIGN KEY (sourcetemplate_id) REFERENCES kb_contenttemplate(id);
ALTER TABLE ONLY kb_relation_template
    ADD CONSTRAINT rt_target_fk FOREIGN KEY (targettemplate_id) REFERENCES kb_contenttemplate(id);
ALTER TABLE ONLY kb_enotirule_principal
    ADD CONSTRAINT rule_fk FOREIGN KEY (rule_id) REFERENCES kb_enotirule(id) ON DELETE CASCADE;
ALTER TABLE ONLY kb_organizationaldata
    ADD CONSTRAINT rule_fk FOREIGN KEY (securityrule_id) REFERENCES kb_security_rule(id) ON DELETE CASCADE;
ALTER TABLE ONLY kb_securitydata
    ADD CONSTRAINT rule_fk FOREIGN KEY (securityrule_id) REFERENCES kb_security_rule(id) ON DELETE CASCADE;
ALTER TABLE ONLY datasetmember
    ADD CONSTRAINT rule_fk FOREIGN KEY (rule_id) REFERENCES kb_security_rule(id) ON DELETE RESTRICT;
ALTER TABLE ONLY po_site_securityrule
    ADD CONSTRAINT rule_fk FOREIGN KEY (rule_id) REFERENCES kb_security_rule(id) ON DELETE CASCADE;
ALTER TABLE ONLY savedquery
    ADD CONSTRAINT savedquery_domain_id_fkey FOREIGN KEY (domain_id) REFERENCES domain(id) ON DELETE CASCADE;
ALTER TABLE ONLY savedquery
    ADD CONSTRAINT savedquery_lastmodifieduser_fkey FOREIGN KEY (lastmodifieduser) REFERENCES users(id) ON DELETE SET NULL;
ALTER TABLE ONLY savedquery
    ADD CONSTRAINT savedquery_user_id_fkey FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
ALTER TABLE ONLY idocsectionresource
    ADD CONSTRAINT section_fk FOREIGN KEY (section_id) REFERENCES idocsection(id) ON DELETE RESTRICT;
ALTER TABLE ONLY dataset
    ADD CONSTRAINT secured_classifier_fk FOREIGN KEY (classifier_id) REFERENCES kb_classifier(id) ON DELETE SET NULL;
ALTER TABLE ONLY kb_notification
    ADD CONSTRAINT sender_fk FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE SET NULL;
ALTER TABLE ONLY po_site_favorites_list
    ADD CONSTRAINT site_favorites_fk FOREIGN KEY (list_id) REFERENCES po_site_favorites(id) ON DELETE CASCADE;
ALTER TABLE ONLY po_site_favorites_list
    ADD CONSTRAINT site_favorites_site_fk FOREIGN KEY (site_oid) REFERENCES po_site(po_id) ON DELETE CASCADE;
ALTER TABLE ONLY po_site_favorites
    ADD CONSTRAINT site_favorites_user_fk FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
ALTER TABLE ONLY po_diagrammable_site
    ADD CONSTRAINT site_fk FOREIGN KEY (po_id) REFERENCES po_portalobject(id) ON DELETE RESTRICT;
ALTER TABLE ONLY po_siteuser
    ADD CONSTRAINT site_fk FOREIGN KEY (site_id) REFERENCES po_diagrammable_site(po_id) ON DELETE RESTRICT;
ALTER TABLE ONLY po_diagrammable_page
    ADD CONSTRAINT site_fk FOREIGN KEY (site_id) REFERENCES po_diagrammable_site(po_id) ON DELETE CASCADE;
ALTER TABLE ONLY po_block_site_components
    ADD CONSTRAINT site_fk FOREIGN KEY (site_id) REFERENCES po_diagrammable_site(po_id) ON DELETE SET NULL;
ALTER TABLE ONLY po_siteuserrights
    ADD CONSTRAINT site_fk FOREIGN KEY (site_id) REFERENCES po_diagrammable_site(po_id) ON DELETE RESTRICT;
ALTER TABLE ONLY po_site
    ADD CONSTRAINT site_fk FOREIGN KEY (po_id) REFERENCES po_portalobject(id) ON DELETE RESTRICT;
ALTER TABLE ONLY po_page
    ADD CONSTRAINT site_fk FOREIGN KEY (site_id) REFERENCES po_site(po_id) ON DELETE CASCADE;
ALTER TABLE ONLY po_diagrammable_site
    ADD CONSTRAINT site_image_fk FOREIGN KEY (site_image) REFERENCES idoc(content_id) ON DELETE SET NULL;
ALTER TABLE ONLY po_site
    ADD CONSTRAINT site_image_fk FOREIGN KEY (site_image) REFERENCES idoc(content_id) ON DELETE SET NULL;
ALTER TABLE ONLY po_site_subscription
    ADD CONSTRAINT site_subscription_user_fk FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
ALTER TABLE ONLY kb_source
    ADD CONSTRAINT source_domain_fk FOREIGN KEY (domain_id) REFERENCES domain(id) ON DELETE CASCADE;
ALTER TABLE ONLY content
    ADD CONSTRAINT source_fk FOREIGN KEY (source_id) REFERENCES kb_source(id) ON DELETE RESTRICT;
ALTER TABLE ONLY memberclassification
    ADD CONSTRAINT sourcemember_fk FOREIGN KEY (sourcemember_id) REFERENCES datasetmember(id) ON DELETE RESTRICT;
ALTER TABLE ONLY kb_subscription
    ADD CONSTRAINT subs_user_fk FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
ALTER TABLE ONLY kb_subsectiontemplate
    ADD CONSTRAINT subscontenttemplate_fk FOREIGN KEY (contenttemplate_id) REFERENCES kb_contenttemplate(id) ON DELETE CASCADE;
ALTER TABLE ONLY kb_subsectiontemplate
    ADD CONSTRAINT subssection_fk FOREIGN KEY (section_id) REFERENCES kb_model_section(id) ON DELETE CASCADE;
ALTER TABLE ONLY po_viewcontentrelation
    ADD CONSTRAINT target_id_fk FOREIGN KEY (target_id) REFERENCES content(id) ON DELETE CASCADE;
ALTER TABLE ONLY memberclassification
    ADD CONSTRAINT targetmember_fk FOREIGN KEY (targetmember_id) REFERENCES datasetmember(id) ON DELETE RESTRICT;
ALTER TABLE ONLY kb_contentresourcegroup
    ADD CONSTRAINT template_fk FOREIGN KEY (template_id) REFERENCES kb_contenttemplate(id) ON DELETE RESTRICT;
ALTER TABLE ONLY kb_relation_target
    ADD CONSTRAINT template_fk FOREIGN KEY (targettemplate_id) REFERENCES kb_contenttemplate(id) ON DELETE CASCADE;
ALTER TABLE ONLY kb_tree_idoc
    ADD CONSTRAINT tree_file_fk FOREIGN KEY (tree_file_id) REFERENCES kb_tree_file(id) ON DELETE RESTRICT;
ALTER TABLE ONLY idoc
    ADD CONSTRAINT tree_file_fk FOREIGN KEY (tree_file_id) REFERENCES kb_tree_file(id) ON DELETE RESTRICT;
ALTER TABLE ONLY kb_tree_file
    ADD CONSTRAINT tree_idoc_id_fk FOREIGN KEY (tree_idoc_id) REFERENCES idoc(content_id) ON DELETE SET NULL;
ALTER TABLE ONLY kb_tree_resource
    ADD CONSTRAINT treeresource_tree_fk FOREIGN KEY (treefile_id) REFERENCES kb_tree_file(id);
ALTER TABLE ONLY kb_user_note
    ADD CONSTRAINT un_domain_fk FOREIGN KEY (domain_id) REFERENCES domain(id) ON DELETE CASCADE;
ALTER TABLE ONLY kb_usage_stat
    ADD CONSTRAINT usage_stat_domain_id FOREIGN KEY (domain_id) REFERENCES domain(id) ON DELETE CASCADE;
ALTER TABLE ONLY users
    ADD CONSTRAINT user_fk FOREIGN KEY (lastmodifieduser) REFERENCES users(id) ON DELETE RESTRICT;
ALTER TABLE ONLY profile
    ADD CONSTRAINT user_fk FOREIGN KEY (lastmodifieduser) REFERENCES users(id) ON DELETE RESTRICT;
ALTER TABLE ONLY principal
    ADD CONSTRAINT user_fk FOREIGN KEY (lastmodifieduser) REFERENCES users(id) ON DELETE RESTRICT;
ALTER TABLE ONLY kb_security_rule
    ADD CONSTRAINT user_fk FOREIGN KEY (lastmodifieduser) REFERENCES users(id) ON DELETE RESTRICT;
ALTER TABLE ONLY kb_enotirule
    ADD CONSTRAINT user_fk FOREIGN KEY (lastmodifieduser) REFERENCES users(id) ON DELETE RESTRICT;
ALTER TABLE ONLY userprofile
    ADD CONSTRAINT user_fk FOREIGN KEY (user_id) REFERENCES users(id);
ALTER TABLE ONLY organization
    ADD CONSTRAINT user_fk FOREIGN KEY (lastmodifieduser) REFERENCES users(id) ON DELETE RESTRICT;
ALTER TABLE ONLY kresource
    ADD CONSTRAINT user_fk FOREIGN KEY (lastmodifieduser) REFERENCES users(id) ON DELETE RESTRICT;
ALTER TABLE ONLY content
    ADD CONSTRAINT user_fk FOREIGN KEY (lastmodifieduser) REFERENCES users(id) ON DELETE RESTRICT;
ALTER TABLE ONLY dataset
    ADD CONSTRAINT user_fk FOREIGN KEY (lastmodifieduser) REFERENCES users(id) ON DELETE RESTRICT;
ALTER TABLE ONLY datasetmember
    ADD CONSTRAINT user_fk FOREIGN KEY (lastmodifieduser) REFERENCES users(id) ON DELETE RESTRICT;
ALTER TABLE ONLY kb_classifier
    ADD CONSTRAINT user_fk FOREIGN KEY (lastmodifieduser) REFERENCES users(id) ON DELETE RESTRICT;
ALTER TABLE ONLY kb_notification
    ADD CONSTRAINT user_fk FOREIGN KEY (lastmodifieduser) REFERENCES users(id) ON DELETE RESTRICT;
ALTER TABLE ONLY drb_question
    ADD CONSTRAINT user_fk FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT;
ALTER TABLE ONLY wf_activity
    ADD CONSTRAINT user_fk FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
ALTER TABLE ONLY kb_email_template
    ADD CONSTRAINT user_fk FOREIGN KEY (lastmodifieduser) REFERENCES users(id) ON DELETE RESTRICT;
ALTER TABLE ONLY kb_attribute
    ADD CONSTRAINT user_fk FOREIGN KEY (lastmodifieduser) REFERENCES users(id) ON DELETE RESTRICT;
ALTER TABLE ONLY po_portalobject
    ADD CONSTRAINT user_fk FOREIGN KEY (lastmodifieduser) REFERENCES users(id) ON DELETE RESTRICT;
ALTER TABLE ONLY po_siteuser
    ADD CONSTRAINT user_fk FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT;
ALTER TABLE ONLY po_siteuserrights
    ADD CONSTRAINT user_fk FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT;
ALTER TABLE ONLY kb_user_property
    ADD CONSTRAINT user_fk FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
ALTER TABLE ONLY userlabel
    ADD CONSTRAINT user_fk FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
ALTER TABLE ONLY kb_tree_file
    ADD CONSTRAINT user_fk FOREIGN KEY (lastmodifieduser) REFERENCES users(id) ON DELETE RESTRICT;
ALTER TABLE ONLY kb_report_subscription
    ADD CONSTRAINT user_fk FOREIGN KEY (lastmodifieduser) REFERENCES users(id) ON DELETE RESTRICT;
ALTER TABLE ONLY kb_searcher_homeblock
    ADD CONSTRAINT user_fk FOREIGN KEY (lastmodifieduser) REFERENCES users(id) ON DELETE RESTRICT;
ALTER TABLE ONLY kb_vote
    ADD CONSTRAINT user_id_fk FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
ALTER TABLE ONLY kb_report
    ADD CONSTRAINT user_id_fk FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
ALTER TABLE ONLY kb_preference
    ADD CONSTRAINT user_id_fk FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
ALTER TABLE ONLY kb_user_note
    ADD CONSTRAINT user_note_id_fk FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
ALTER TABLE ONLY kb_user_note
    ADD CONSTRAINT user_note_lmu_id_fk FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL;
ALTER TABLE ONLY kb_user_role
    ADD CONSTRAINT user_role_user_id_fk FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
ALTER TABLE ONLY kb_user_role
    ADD CONSTRAINT userrole_entity_fk FOREIGN KEY (entity_id) REFERENCES datasetmember(id);
ALTER TABLE ONLY kb_user_role
    ADD CONSTRAINT userrole_profile_fk FOREIGN KEY (userprofile_id) REFERENCES userprofile(id);
ALTER TABLE ONLY kb_user_role
    ADD CONSTRAINT userrole_role_fk FOREIGN KEY (role_id) REFERENCES kb_role(id);
ALTER TABLE ONLY kresource
    ADD CONSTRAINT version_fk FOREIGN KEY (prev_version) REFERENCES kresource(id) ON DELETE RESTRICT;
ALTER TABLE ONLY po_portalobject
    ADD CONSTRAINT version_fk FOREIGN KEY (prev_version) REFERENCES po_portalobject(id) ON DELETE RESTRICT;
ALTER TABLE ONLY po_viewcontentrelation
    ADD CONSTRAINT view_content_relation_fk FOREIGN KEY (view_id) REFERENCES po_portalobject(id) ON DELETE CASCADE;
ALTER TABLE ONLY po_viewbk
    ADD CONSTRAINT viewbk_block_fk FOREIGN KEY (block_id) REFERENCES po_diagrammable_block(po_id) ON UPDATE CASCADE ON DELETE CASCADE;
ALTER TABLE ONLY po_viewbk
    ADD CONSTRAINT viewbk_fk FOREIGN KEY (po_id) REFERENCES po_portalobject(id) ON DELETE CASCADE;
ALTER TABLE ONLY po_viewbk
    ADD CONSTRAINT viewbk_image_fk FOREIGN KEY (image_id) REFERENCES kresource(id) ON DELETE RESTRICT;
ALTER TABLE ONLY po_viewbkblock
    ADD CONSTRAINT viewbkblock_site_fk FOREIGN KEY (block_id) REFERENCES po_diagrammable_block(po_id) ON DELETE SET NULL;
ALTER TABLE ONLY po_viewbkblock
    ADD CONSTRAINT viewbkblock_view_fk FOREIGN KEY (view_id) REFERENCES po_viewbk(po_id) ON UPDATE CASCADE ON DELETE CASCADE;
ALTER TABLE ONLY po_viewbkcontent
    ADD CONSTRAINT viewbkcontent_content_fk FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE SET NULL;
ALTER TABLE ONLY po_viewbkcontent
    ADD CONSTRAINT viewbkcontent_view_fk FOREIGN KEY (view_id) REFERENCES po_viewbk(po_id) ON UPDATE CASCADE ON DELETE CASCADE;
ALTER TABLE ONLY po_viewbklink
    ADD CONSTRAINT viewbklink_view_fk FOREIGN KEY (view_id) REFERENCES po_viewbk(po_id) ON DELETE CASCADE;
ALTER TABLE ONLY po_viewbksite
    ADD CONSTRAINT viewbksite_site_fk FOREIGN KEY (site_id) REFERENCES po_diagrammable_site(po_id) ON DELETE SET NULL;
ALTER TABLE ONLY po_viewbksite
    ADD CONSTRAINT viewbksite_view_fk FOREIGN KEY (view_id) REFERENCES po_viewbk(po_id) ON UPDATE CASCADE ON DELETE CASCADE;
ALTER TABLE ONLY wf_launcher
    ADD CONSTRAINT wf_launcher_lastmodifieduser_fkey FOREIGN KEY (lastmodifieduser) REFERENCES users(id) ON DELETE RESTRICT;
ALTER TABLE ONLY wf_launcher
    ADD CONSTRAINT wf_launcher_launchergroup_id_fkey FOREIGN KEY (launchergroup_id) REFERENCES kb_launcher_group(id) ON DELETE RESTRICT;
ALTER TABLE ONLY wf_procedure
    ADD CONSTRAINT wf_procedure_diagram_fkey FOREIGN KEY (diagram) REFERENCES kfile(resource_id) ON DELETE SET NULL;
ALTER TABLE ONLY kb_work_note
    ADD CONSTRAINT wn_domain_fk FOREIGN KEY (domain_id) REFERENCES domain(id) ON DELETE CASCADE;
ALTER TABLE ONLY kb_notification
    ADD CONSTRAINT wn_fk FOREIGN KEY (work_note_id) REFERENCES kb_work_note(id) ON DELETE CASCADE;
ALTER TABLE ONLY kb_work_note
    ADD CONSTRAINT wn_id_fk FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
ALTER TABLE ONLY kb_work_note
    ADD CONSTRAINT wn_lmu_id_fk FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL;
ALTER TABLE ONLY kb_work_note_user_read
    ADD CONSTRAINT wnur_uid_fk FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
ALTER TABLE ONLY kb_work_note_user_read
    ADD CONSTRAINT wnur_wnid_fk FOREIGN KEY (work_note_id) REFERENCES kb_work_note(id) ON DELETE CASCADE;
ALTER TABLE ONLY content
    ADD CONSTRAINT workspace_fk FOREIGN KEY (workspace) REFERENCES users(id) ON DELETE RESTRICT;
