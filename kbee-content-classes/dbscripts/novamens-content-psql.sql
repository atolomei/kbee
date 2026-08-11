--
-- PostgreSQL database dump
--

-- Dumped from database version 10.0
-- Dumped by pg_dump version 10.0

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


--
-- Name: file_fdw; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS file_fdw WITH SCHEMA public;


--
-- Name: EXTENSION file_fdw; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION file_fdw IS 'foreign-data wrapper for flat file access';


--
-- Name: pg_stat_statements; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS pg_stat_statements WITH SCHEMA public;


--
-- Name: EXTENSION pg_stat_statements; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION pg_stat_statements IS 'track execution statistics of all SQL statements executed';


SET search_path = public, pg_catalog;

--
-- Name: createdomain(bigint, bigint, bigint, bigint, bigint, bigint, character varying, character varying); Type: FUNCTION; Schema: public; Owner: postgres
--

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


ALTER FUNCTION public.createdomain(id_dominio bigint, id_user bigint, id_group bigint, id_profile bigint, id_person bigint, id_template bigint, nombredominio character varying, mail character varying) OWNER TO postgres;

--
-- Name: last_agg(anyelement, anyelement); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION last_agg(anyelement, anyelement) RETURNS anyelement
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
SELECT $2;
$_$;


ALTER FUNCTION public.last_agg(anyelement, anyelement) OWNER TO postgres;

--
-- Name: last(anyelement); Type: AGGREGATE; Schema: public; Owner: postgres
--

CREATE AGGREGATE last(anyelement) (
    SFUNC = last_agg,
    STYPE = anyelement
);


ALTER AGGREGATE public.last(anyelement) OWNER TO postgres;

--
-- Name: fileserver; Type: SERVER; Schema: -; Owner: postgres
--

CREATE SERVER fileserver FOREIGN DATA WRAPPER file_fdw;


ALTER SERVER fileserver OWNER TO postgres;

--
-- Name: aclentry_sequence; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE aclentry_sequence
    START WITH 1000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE aclentry_sequence OWNER TO postgres;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: api_logevent; Type: TABLE; Schema: public; Owner: postgres
--

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
    event_closed boolean DEFAULT false
);


ALTER TABLE api_logevent OWNER TO postgres;

--
-- Name: api_sequence; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE api_sequence
    START WITH 1000
    INCREMENT BY 10
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE api_sequence OWNER TO postgres;

--
-- Name: api_soapevent; Type: TABLE; Schema: public; Owner: postgres
--

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
    event_contentclass character varying(64)
);


ALTER TABLE api_soapevent OWNER TO postgres;

--
-- Name: authorities; Type: TABLE; Schema: public; Owner: kbee
--

CREATE TABLE authorities (
    username character varying(50) NOT NULL,
    authority character varying(50) NOT NULL
);


ALTER TABLE authorities OWNER TO kbee;

--
-- Name: classification; Type: TABLE; Schema: public; Owner: kbee
--

CREATE TABLE classification (
    id bigint NOT NULL,
    state integer,
    datevalue timestamp with time zone,
    content_id bigint NOT NULL,
    classifier_id bigint NOT NULL,
    datasetmember_id bigint,
    "position" integer
);


ALTER TABLE classification OWNER TO kbee;

--
-- Name: classificationid_sequence; Type: SEQUENCE; Schema: public; Owner: kbee
--

CREATE SEQUENCE classificationid_sequence
    START WITH 1000
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE classificationid_sequence OWNER TO kbee;

--
-- Name: classifiercontent; Type: TABLE; Schema: public; Owner: kbee
--

CREATE TABLE classifiercontent (
    classifier_id bigint NOT NULL,
    contentclass_id character varying(64) NOT NULL
);


ALTER TABLE classifiercontent OWNER TO kbee;

--
-- Name: content; Type: TABLE; Schema: public; Owner: kbee
--

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

--
-- Name: contentclass; Type: TABLE; Schema: public; Owner: kbee
--

CREATE TABLE contentclass (
    id character varying(64) NOT NULL,
    enabled boolean DEFAULT true,
    name character varying(128),
    javaclass character varying(128),
    indexable boolean DEFAULT true,
    selectable boolean DEFAULT true
);


ALTER TABLE contentclass OWNER TO kbee;

--
-- Name: contentid_sequence; Type: SEQUENCE; Schema: public; Owner: kbee
--

CREATE SEQUENCE contentid_sequence
    START WITH 1000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 30;


ALTER TABLE contentid_sequence OWNER TO kbee;

--
-- Name: contentproperties; Type: TABLE; Schema: public; Owner: kbee
--

CREATE TABLE contentproperties (
    content_id bigint NOT NULL,
    contentproperties bytea,
    lastmodifieddate timestamp with time zone DEFAULT now(),
    lastmodifieduser bigint NOT NULL
);


ALTER TABLE contentproperties OWNER TO kbee;

--
-- Name: contentresource; Type: TABLE; Schema: public; Owner: kbee
--

CREATE TABLE contentresource (
    content_id bigint NOT NULL,
    resource_id bigint NOT NULL,
    "position" integer,
    id bigint NOT NULL
);


ALTER TABLE contentresource OWNER TO kbee;

--
-- Name: contentresourceid_sequence; Type: SEQUENCE; Schema: public; Owner: kbee
--

CREATE SEQUENCE contentresourceid_sequence
    START WITH 100
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE contentresourceid_sequence OWNER TO kbee;

--
-- Name: contentstat; Type: TABLE; Schema: public; Owner: kbee
--

CREATE TABLE contentstat (
    content_id bigint NOT NULL,
    views integer,
    shared integer,
    favorites integer,
    votes integer
);


ALTER TABLE contentstat OWNER TO kbee;

--
-- Name: databasechangelog; Type: TABLE; Schema: public; Owner: postgres
--

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

--
-- Name: databasechangeloglock; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE databasechangeloglock (
    id integer NOT NULL,
    locked boolean NOT NULL,
    lockgranted timestamp without time zone,
    lockedby character varying(255)
);


ALTER TABLE databasechangeloglock OWNER TO postgres;

--
-- Name: dataset; Type: TABLE; Schema: public; Owner: kbee
--

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
    aggregation boolean DEFAULT false
);


ALTER TABLE dataset OWNER TO kbee;

--
-- Name: datasetclassifier; Type: TABLE; Schema: public; Owner: kbee
--

CREATE TABLE datasetclassifier (
    dataset_id bigint NOT NULL,
    classifier_id bigint NOT NULL
);


ALTER TABLE datasetclassifier OWNER TO kbee;

--
-- Name: datasetmember; Type: TABLE; Schema: public; Owner: kbee
--

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

--
-- Name: domain; Type: TABLE; Schema: public; Owner: kbee
--

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
    logo bigint
);


ALTER TABLE domain OWNER TO kbee;

--
-- Name: domainid_sequence; Type: SEQUENCE; Schema: public; Owner: kbee
--

CREATE SEQUENCE domainid_sequence
    START WITH 2
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE domainid_sequence OWNER TO kbee;

--
-- Name: drb_answer; Type: TABLE; Schema: public; Owner: kbee
--

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

--
-- Name: drb_question; Type: TABLE; Schema: public; Owner: kbee
--

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

--
-- Name: entity; Type: TABLE; Schema: public; Owner: kbee
--

CREATE TABLE entity (
    id bigint NOT NULL,
    creationdate timestamp with time zone DEFAULT now(),
    lastmodifieddate timestamp with time zone DEFAULT now(),
    lastmodifieduser bigint NOT NULL,
    state integer,
    domain_id bigint
);


ALTER TABLE entity OWNER TO kbee;

--
-- Name: entityid_sequence; Type: SEQUENCE; Schema: public; Owner: kbee
--

CREATE SEQUENCE entityid_sequence
    START WITH 1000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE entityid_sequence OWNER TO kbee;

--
-- Name: entitymatching; Type: TABLE; Schema: public; Owner: kbee
--

CREATE TABLE entitymatching (
    kbee_id character varying(36) NOT NULL,
    kbee_class_name character varying(150),
    lastmodifieddate timestamp with time zone DEFAULT now(),
    class_name character varying(150),
    id character varying(36) NOT NULL,
    url character varying(36) NOT NULL
);


ALTER TABLE entitymatching OWNER TO kbee;

--
-- Name: externalresource; Type: TABLE; Schema: public; Owner: kbee
--

CREATE TABLE externalresource (
    resource_id bigint NOT NULL,
    url character varying(2048),
    description character varying(1024),
    in_portal boolean DEFAULT true
);


ALTER TABLE externalresource OWNER TO kbee;

--
-- Name: gallery; Type: TABLE; Schema: public; Owner: kbee
--

CREATE TABLE gallery (
    title character varying(512),
    subtitle character varying(256),
    description character varying(1024),
    resource_id bigint NOT NULL,
    gdate date,
    in_portal boolean DEFAULT true
);


ALTER TABLE gallery OWNER TO kbee;

--
-- Name: galleryfile; Type: TABLE; Schema: public; Owner: kbee
--

CREATE TABLE galleryfile (
    gallery_id bigint NOT NULL,
    file_id bigint NOT NULL,
    gorder integer
);


ALTER TABLE galleryfile OWNER TO kbee;

--
-- Name: hibernate_sequence; Type: SEQUENCE; Schema: public; Owner: kbee
--

CREATE SEQUENCE hibernate_sequence
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


ALTER TABLE hibernate_sequence OWNER TO kbee;

--
-- Name: idoc; Type: TABLE; Schema: public; Owner: kbee
--

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

--
-- Name: idocsection; Type: TABLE; Schema: public; Owner: kbee
--

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

--
-- Name: idocsectionresource; Type: TABLE; Schema: public; Owner: kbee
--

CREATE TABLE idocsectionresource (
    section_id bigint NOT NULL,
    resource_id bigint NOT NULL,
    "position" integer
);


ALTER TABLE idocsectionresource OWNER TO kbee;

--
-- Name: kb_acl; Type: TABLE; Schema: public; Owner: kbee
--

CREATE TABLE kb_acl (
    id bigint NOT NULL,
    lastmodifieddate timestamp with time zone DEFAULT now(),
    lastmodifieduser bigint,
    name character varying(120),
    creationdate timestamp with time zone DEFAULT now()
);


ALTER TABLE kb_acl OWNER TO kbee;

--
-- Name: kb_aclentry; Type: TABLE; Schema: public; Owner: kbee
--

CREATE TABLE kb_aclentry (
    acl bigint NOT NULL,
    principal bigint NOT NULL,
    permissions character varying(1024) NOT NULL,
    negative boolean NOT NULL,
    id bigint NOT NULL
);


ALTER TABLE kb_aclentry OWNER TO kbee;

--
-- Name: kb_action_rule; Type: TABLE; Schema: public; Owner: postgres
--

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

--
-- Name: kb_api_usage_stat; Type: TABLE; Schema: public; Owner: postgres
--

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

--
-- Name: kb_assignable_role; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE kb_assignable_role (
    role_id bigint NOT NULL,
    assignablerole_id bigint NOT NULL
);


ALTER TABLE kb_assignable_role OWNER TO postgres;

--
-- Name: kb_attribute; Type: TABLE; Schema: public; Owner: postgres
--

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
    description text
);


ALTER TABLE kb_attribute OWNER TO postgres;

--
-- Name: kb_attributetemplate; Type: TABLE; Schema: public; Owner: kbee
--

CREATE TABLE kb_attributetemplate (
    id bigint NOT NULL,
    metadatasubtitle boolean DEFAULT false,
    attribute_id bigint,
    portalsubtitle boolean DEFAULT false,
    korder integer DEFAULT 0,
    multiplicity integer DEFAULT 1,
    section_id bigint,
    subsection character varying(128),
    isvisible boolean DEFAULT true
);


ALTER TABLE kb_attributetemplate OWNER TO kbee;

--
-- Name: kb_cabinet; Type: TABLE; Schema: public; Owner: postgres
--

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

--
-- Name: kb_cabinet_reader; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE kb_cabinet_reader (
    cabinet_id bigint NOT NULL,
    group_id bigint NOT NULL
);


ALTER TABLE kb_cabinet_reader OWNER TO postgres;

--
-- Name: kb_classifier; Type: TABLE; Schema: public; Owner: kbee
--

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
    description text
);


ALTER TABLE kb_classifier OWNER TO kbee;

--
-- Name: kb_classifiertemplate; Type: TABLE; Schema: public; Owner: kbee
--

CREATE TABLE kb_classifiertemplate (
    id bigint NOT NULL,
    contenttemplate_id bigint NOT NULL,
    classifier_id bigint NOT NULL,
    root_id bigint,
    "position" integer,
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
    criteria character varying(256)
);


ALTER TABLE kb_classifiertemplate OWNER TO kbee;

--
-- Name: kb_comment; Type: TABLE; Schema: public; Owner: postgres
--

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

--
-- Name: kb_content_relation; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE kb_content_relation (
    id bigint NOT NULL,
    source_id bigint NOT NULL,
    target_id bigint NOT NULL,
    template_id bigint NOT NULL,
    "position" integer
);


ALTER TABLE kb_content_relation OWNER TO postgres;

--
-- Name: kb_content_rsbycriteria; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE kb_content_rsbycriteria (
    id bigint NOT NULL,
    template_id bigint NOT NULL,
    source_id bigint NOT NULL,
    condition text
);


ALTER TABLE kb_content_rsbycriteria OWNER TO postgres;

--
-- Name: kb_contentattribute; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE kb_contentattribute (
    contenttemplate_id bigint NOT NULL,
    attributetemplate_id bigint NOT NULL,
    "position" integer
);


ALTER TABLE kb_contentattribute OWNER TO postgres;

--
-- Name: kb_contentresourcegroup; Type: TABLE; Schema: public; Owner: kbee
--

CREATE TABLE kb_contentresourcegroup (
    template_id bigint NOT NULL,
    group_id bigint NOT NULL,
    "position" integer
);


ALTER TABLE kb_contentresourcegroup OWNER TO kbee;

--
-- Name: kb_contenttemplate; Type: TABLE; Schema: public; Owner: kbee
--

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
    contentclasscode character varying(24),
    istreefile boolean DEFAULT false,
    treefile_label character varying(256),
    isresources boolean DEFAULT true,
    resources_label character varying(256) DEFAULT 'Resources'::character varying,
    isexternal boolean DEFAULT false,
    increlationshipsbycriteria boolean DEFAULT false,
    acceptsrelationshipsbycriteria boolean DEFAULT false,
    iscompliance boolean DEFAULT false,
    treefileresource boolean DEFAULT false,
    consolesubtitlerule character varying(2048),
    portalssubtitlerule character varying(2048),
    includesrelationshipsbycriteria boolean DEFAULT false,
    description text
);


ALTER TABLE kb_contenttemplate OWNER TO kbee;

--
-- Name: kb_cronjob; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE kb_cronjob (
    id bigint NOT NULL,
    lastmodifieddate timestamp with time zone DEFAULT now(),
    lastmodifieduser bigint NOT NULL,
    name character varying(256),
    description character varying(2048),
    cronexpression character varying(256),
    clazz character varying(1024),
    parameter text,
    isenabled boolean DEFAULT true
);


ALTER TABLE kb_cronjob OWNER TO postgres;

--
-- Name: kb_datasetattribute; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE kb_datasetattribute (
    dataset_id bigint NOT NULL,
    attributetemplate_id bigint NOT NULL,
    "position" integer
);


ALTER TABLE kb_datasetattribute OWNER TO postgres;

--
-- Name: kb_delta_sql; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE kb_delta_sql (
    id bigint NOT NULL,
    kb_version character varying(128) NOT NULL,
    script_name character varying(128) NOT NULL,
    executedby character varying(128),
    executeddate timestamp with time zone DEFAULT now(),
    success boolean DEFAULT false,
    results character varying(2048),
    comments character varying(2048)
);


ALTER TABLE kb_delta_sql OWNER TO postgres;

--
-- Name: kb_domain_settings; Type: TABLE; Schema: public; Owner: kbee
--

CREATE TABLE kb_domain_settings (
    domain_id bigint NOT NULL,
    category character varying(64) NOT NULL,
    values_json text,
    lastmodifieddate timestamp with time zone DEFAULT now(),
    id bigint NOT NULL
);


ALTER TABLE kb_domain_settings OWNER TO kbee;

--
-- Name: kb_ds_element_template; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE kb_ds_element_template (
    id bigint NOT NULL,
    dataset_id bigint NOT NULL,
    "position" integer,
    classifier_id bigint,
    attribute_id bigint,
    multiplicity integer,
    readonly boolean,
    aggregation boolean
);


ALTER TABLE kb_ds_element_template OWNER TO postgres;

--
-- Name: kb_email_template; Type: TABLE; Schema: public; Owner: postgres
--

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

--
-- Name: kb_enotirule; Type: TABLE; Schema: public; Owner: kbee
--

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

--
-- Name: kb_enotirule_principal; Type: TABLE; Schema: public; Owner: kbee
--

CREATE TABLE kb_enotirule_principal (
    rule_id bigint NOT NULL,
    principal_id bigint NOT NULL
);


ALTER TABLE kb_enotirule_principal OWNER TO kbee;

--
-- Name: kb_facet_wrapper; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE kb_facet_wrapper (
    id bigint NOT NULL,
    domain_id bigint,
    name character varying(128),
    display_name character varying(256),
    visibility text,
    lastmodifieddate timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    lastmodifieduser bigint,
    state integer,
    creationdate timestamp with time zone DEFAULT now(),
    "order" integer
);


ALTER TABLE kb_facet_wrapper OWNER TO postgres;

--
-- Name: kb_file_loader; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE kb_file_loader (
    id bigint NOT NULL,
    name character varying(128),
    javaclass character varying(128)
);


ALTER TABLE kb_file_loader OWNER TO postgres;

--
-- Name: kb_file_proxy; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE kb_file_proxy (
    resource_id bigint NOT NULL,
    file_loader bigint NOT NULL,
    url character varying(512),
    size integer DEFAULT '-1'::integer
);


ALTER TABLE kb_file_proxy OWNER TO postgres;

--
-- Name: kb_group_role; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE kb_group_role (
    role_id bigint NOT NULL,
    group_id bigint NOT NULL
);


ALTER TABLE kb_group_role OWNER TO postgres;

--
-- Name: kb_import_data; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE kb_import_data (
    server_url character varying(256) NOT NULL,
    remote_domain character varying(64) NOT NULL,
    object_class character varying(128) NOT NULL,
    remote_id bigint NOT NULL,
    local_id bigint NOT NULL,
    local_domain character varying(64) NOT NULL,
    import_time timestamp with time zone DEFAULT now()
);


ALTER TABLE kb_import_data OWNER TO postgres;

--
-- Name: kb_language_string; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE kb_language_string (
    id bigint NOT NULL,
    key character varying(256) NOT NULL,
    locale character varying(128) NOT NULL,
    value character varying(1024)
);


ALTER TABLE kb_language_string OWNER TO postgres;

--
-- Name: kb_member_role; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE kb_member_role (
    id bigint NOT NULL,
    entity_id bigint NOT NULL,
    role_id bigint NOT NULL,
    securityrule_id bigint,
    group_id bigint
);


ALTER TABLE kb_member_role OWNER TO postgres;

--
-- Name: kb_model_section; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE kb_model_section (
    id bigint NOT NULL,
    contenttemplate_id bigint NOT NULL,
    name character varying(128),
    description text,
    "position" integer DEFAULT 0
);


ALTER TABLE kb_model_section OWNER TO postgres;

--
-- Name: kb_move; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE kb_move (
    content_id bigint NOT NULL,
    domain character varying(64) NOT NULL,
    status integer,
    error_message character varying(256)
);


ALTER TABLE kb_move OWNER TO postgres;

--
-- Name: kb_notification; Type: TABLE; Schema: public; Owner: kbee
--

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
    type integer DEFAULT 1,
    notification_state integer DEFAULT 1,
    notification_type integer DEFAULT 10 NOT NULL,
    work_note_id bigint,
    deleteonaccept boolean DEFAULT true,
    dateread timestamp with time zone,
    isalert boolean DEFAULT true,
    isbillboard boolean DEFAULT true
);


ALTER TABLE kb_notification OWNER TO kbee;

--
-- Name: kb_object_property; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE kb_object_property (
    id bigint NOT NULL,
    type integer,
    name character varying(128),
    object_id character varying(64) NOT NULL,
    value text,
    uset character varying(128),
    lastmodifieddate timestamp with time zone DEFAULT now()
);


ALTER TABLE kb_object_property OWNER TO postgres;

--
-- Name: kb_organizationaldata; Type: TABLE; Schema: public; Owner: kbee
--

CREATE TABLE kb_organizationaldata (
    id bigint NOT NULL,
    person_id bigint,
    group_id bigint,
    securityrule_id bigint
);


ALTER TABLE kb_organizationaldata OWNER TO kbee;

--
-- Name: kb_preference; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE kb_preference (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    name character varying(128) NOT NULL,
    properties text
);


ALTER TABLE kb_preference OWNER TO postgres;

--
-- Name: kb_preference_domain; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE kb_preference_domain (
    id bigint NOT NULL,
    domain_id bigint,
    name character varying(256) NOT NULL,
    properties text
);


ALTER TABLE kb_preference_domain OWNER TO postgres;

--
-- Name: kb_relation_target; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE kb_relation_target (
    relationtemplate_id bigint NOT NULL,
    targettemplate_id bigint NOT NULL
);


ALTER TABLE kb_relation_target OWNER TO postgres;

--
-- Name: kb_relation_template; Type: TABLE; Schema: public; Owner: postgres
--

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
    state integer DEFAULT 1 NOT NULL,
    domain_id bigint,
    lastmodifieduser bigint,
    lastmodifieddate timestamp with time zone DEFAULT now(),
    creationdate timestamp with time zone DEFAULT now()
);


ALTER TABLE kb_relation_template OWNER TO postgres;

--
-- Name: kb_report; Type: TABLE; Schema: public; Owner: kbee
--

CREATE TABLE kb_report (
    user_id bigint NOT NULL,
    content_id bigint NOT NULL,
    report integer,
    reportdate timestamp with time zone DEFAULT now(),
    id bigint NOT NULL
);


ALTER TABLE kb_report OWNER TO kbee;

--
-- Name: kb_role; Type: TABLE; Schema: public; Owner: postgres
--

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

--
-- Name: kb_rsbycriteria_template; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE kb_rsbycriteria_template (
    id bigint NOT NULL,
    name character varying(128),
    source_label character varying(128),
    sourcetemplate_id bigint NOT NULL,
    target_label character varying(128),
    "position" integer DEFAULT 0
);


ALTER TABLE kb_rsbycriteria_template OWNER TO postgres;

--
-- Name: kb_searcher_homeblock; Type: TABLE; Schema: public; Owner: postgres
--

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

--
-- Name: kb_security_rule; Type: TABLE; Schema: public; Owner: kbee
--

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

--
-- Name: kb_securitydata; Type: TABLE; Schema: public; Owner: kbee
--

CREATE TABLE kb_securitydata (
    id bigint NOT NULL,
    person_id bigint,
    group_id bigint,
    securityrule_id bigint
);


ALTER TABLE kb_securitydata OWNER TO kbee;

--
-- Name: kb_sendemailevent; Type: TABLE; Schema: public; Owner: kbee
--

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
    event_generator_action character varying(128)
);


ALTER TABLE kb_sendemailevent OWNER TO kbee;

--
-- Name: kb_source; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE kb_source (
    id bigint NOT NULL,
    creationdate timestamp with time zone DEFAULT now(),
    lastmodifieddate timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    lastmodifieduser bigint,
    domain_id bigint,
    name character varying(128),
    display_name character varying(256),
    state integer
);


ALTER TABLE kb_source OWNER TO postgres;

--
-- Name: kb_subscription; Type: TABLE; Schema: public; Owner: kbee
--

CREATE TABLE kb_subscription (
    user_id bigint NOT NULL,
    content_oid bigint NOT NULL,
    event_id integer NOT NULL,
    subscription_date timestamp with time zone DEFAULT now(),
    type_id integer
);


ALTER TABLE kb_subscription OWNER TO kbee;

--
-- Name: kb_system_properties; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE kb_system_properties (
    key character varying(256) NOT NULL,
    value character varying(2048),
    area character varying(64) DEFAULT 'system'::character varying
);


ALTER TABLE kb_system_properties OWNER TO postgres;

--
-- Name: kb_timer; Type: TABLE; Schema: public; Owner: kbee
--

CREATE TABLE kb_timer (
    id bigint NOT NULL,
    creationdate timestamp with time zone DEFAULT now(),
    duedate timestamp with time zone,
    callback bytea,
    attemps smallint,
    error_message character varying(256)
);


ALTER TABLE kb_timer OWNER TO kbee;

--
-- Name: kb_tip; Type: TABLE; Schema: public; Owner: kbee
--

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

--
-- Name: kb_tree_file; Type: TABLE; Schema: public; Owner: postgres
--

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
    in_portal boolean DEFAULT true
);


ALTER TABLE kb_tree_file OWNER TO postgres;

--
-- Name: kb_tree_idoc; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE kb_tree_idoc (
    content_id bigint NOT NULL,
    tree_file_id bigint
);


ALTER TABLE kb_tree_idoc OWNER TO postgres;

--
-- Name: kb_tree_resource; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE kb_tree_resource (
    resource_id bigint NOT NULL,
    treefile_id bigint NOT NULL
);


ALTER TABLE kb_tree_resource OWNER TO postgres;

--
-- Name: kb_usage_stat; Type: TABLE; Schema: public; Owner: kbee
--

CREATE TABLE kb_usage_stat (
    domain_id bigint NOT NULL,
    ts timestamp with time zone DEFAULT now() NOT NULL,
    hard_disk_usage bigint,
    users bigint,
    contents bigint,
    resources bigint,
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
    database_usage bigint DEFAULT 0
);


ALTER TABLE kb_usage_stat OWNER TO kbee;

--
-- Name: kb_user_note; Type: TABLE; Schema: public; Owner: postgres
--

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

--
-- Name: kb_user_property; Type: TABLE; Schema: public; Owner: kbee
--

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

--
-- Name: kb_user_role; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE kb_user_role (
    id bigint NOT NULL,
    userprofile_id bigint NOT NULL,
    role_id bigint NOT NULL,
    entity_id bigint,
    user_id bigint NOT NULL
);


ALTER TABLE kb_user_role OWNER TO postgres;

--
-- Name: kb_vote; Type: TABLE; Schema: public; Owner: kbee
--

CREATE TABLE kb_vote (
    user_id bigint NOT NULL,
    content_id bigint NOT NULL,
    vote integer,
    votedate timestamp with time zone DEFAULT now(),
    id bigint NOT NULL
);


ALTER TABLE kb_vote OWNER TO kbee;

--
-- Name: kb_work_note; Type: TABLE; Schema: public; Owner: postgres
--

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
    is_first_version boolean DEFAULT true,
    isalert boolean DEFAULT true,
    isemail boolean DEFAULT false,
    send_notification boolean DEFAULT true,
    isbillboard boolean DEFAULT false,
    glyphicon character varying(64)
);


ALTER TABLE kb_work_note OWNER TO postgres;

--
-- Name: kb_work_note_user_read; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE kb_work_note_user_read (
    id bigint NOT NULL,
    work_note_id bigint NOT NULL,
    user_id bigint NOT NULL,
    readdate timestamp with time zone DEFAULT now()
);


ALTER TABLE kb_work_note_user_read OWNER TO postgres;

--
-- Name: kb_worknote_principal; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE kb_worknote_principal (
    note_id bigint NOT NULL,
    principal_id bigint NOT NULL
);


ALTER TABLE kb_worknote_principal OWNER TO postgres;

--
-- Name: kfile; Type: TABLE; Schema: public; Owner: kbee
--

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
    in_portal boolean DEFAULT true
);


ALTER TABLE kfile OWNER TO kbee;

--
-- Name: kgroup; Type: TABLE; Schema: public; Owner: kbee
--

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

--
-- Name: kgroupmember; Type: TABLE; Schema: public; Owner: kbee
--

CREATE TABLE kgroupmember (
    kgroup bigint NOT NULL,
    principal bigint NOT NULL
);


ALTER TABLE kgroupmember OWNER TO kbee;

--
-- Name: klock; Type: TABLE; Schema: public; Owner: kbee
--

CREATE TABLE klock (
    lock_id integer NOT NULL,
    lock_object_id character varying(100),
    lock_date timestamp with time zone,
    lock_user_id character varying(50) NOT NULL,
    lock_scope character varying(50),
    lock_timeout timestamp without time zone
);


ALTER TABLE klock OWNER TO kbee;

--
-- Name: kresource; Type: TABLE; Schema: public; Owner: kbee
--

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

--
-- Name: loadavg; Type: FOREIGN TABLE; Schema: public; Owner: postgres
--

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

--
-- Name: lock_sequence; Type: SEQUENCE; Schema: public; Owner: kbee
--

CREATE SEQUENCE lock_sequence
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE lock_sequence OWNER TO kbee;

--
-- Name: log_sequence; Type: SEQUENCE; Schema: public; Owner: kbee
--

CREATE SEQUENCE log_sequence
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE log_sequence OWNER TO kbee;

--
-- Name: logevent; Type: TABLE; Schema: public; Owner: kbee
--

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
    event_resource_id bigint
);


ALTER TABLE logevent OWNER TO kbee;

--
-- Name: logsites_sequence; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE logsites_sequence
    START WITH 36749
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE logsites_sequence OWNER TO postgres;

--
-- Name: memberclassification; Type: TABLE; Schema: public; Owner: kbee
--

CREATE TABLE memberclassification (
    id bigint NOT NULL,
    state integer,
    sourcemember_id bigint NOT NULL,
    classifier_id bigint NOT NULL,
    targetmember_id bigint,
    "position" integer
);


ALTER TABLE memberclassification OWNER TO kbee;

--
-- Name: meminfo; Type: FOREIGN TABLE; Schema: public; Owner: postgres
--

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

--
-- Name: objectid_sequence; Type: SEQUENCE; Schema: public; Owner: kbee
--

CREATE SEQUENCE objectid_sequence
    START WITH 1000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE objectid_sequence OWNER TO kbee;

--
-- Name: organization; Type: TABLE; Schema: public; Owner: kbee
--

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

--
-- Name: organizationaltext; Type: TABLE; Schema: public; Owner: kbee
--

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

--
-- Name: orgchart; Type: TABLE; Schema: public; Owner: kbee
--

CREATE TABLE orgchart (
    content_id bigint NOT NULL,
    name character varying(256),
    description character varying(2048),
    mision character varying(2048),
    xmlchart text
);


ALTER TABLE orgchart OWNER TO kbee;

--
-- Name: person; Type: TABLE; Schema: public; Owner: kbee
--

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
    photo_domain_logo boolean DEFAULT false
);


ALTER TABLE person OWNER TO kbee;

--
-- Name: po_area; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE po_area (
    po_id bigint,
    page_id bigint,
    area_type integer,
    orden integer,
    full_width_canvas boolean,
    areaclass character varying(128),
    custom_values text
);


ALTER TABLE po_area OWNER TO postgres;

--
-- Name: po_block; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE po_block (
    po_id bigint,
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
    custom_values text,
    content_id bigint
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
    subtitle_mode integer DEFAULT 0,
    element_orientation_css character varying(512)
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
-- Name: po_diagrammable_area; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE po_diagrammable_area (
    po_id bigint NOT NULL,
    page_id bigint NOT NULL,
    area_type integer,
    orden integer,
    full_width_canvas boolean DEFAULT false,
    areaclass character varying(128)
);


ALTER TABLE po_diagrammable_area OWNER TO postgres;

--
-- Name: po_diagrammable_block; Type: TABLE; Schema: public; Owner: postgres
--

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

--
-- Name: po_diagrammable_page; Type: TABLE; Schema: public; Owner: postgres
--

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

--
-- Name: po_diagrammable_site; Type: TABLE; Schema: public; Owner: postgres
--

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

--
-- Name: po_page; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE po_page (
    po_id bigint,
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
    ishead boolean DEFAULT true,
    nextversion bigint
);


ALTER TABLE po_portalobject OWNER TO postgres;

--
-- Name: po_site; Type: TABLE; Schema: public; Owner: postgres
--

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
    custom_values text,
    alias character varying(128),
    isdisplayvalidversion boolean DEFAULT false
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
    user_agent character varying(512),
    object_id character varying(64)
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
    ntab boolean DEFAULT false,
    iconcss character varying(50)
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

--
-- Name: po_viewcontentrelation; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE po_viewcontentrelation (
    view_id bigint NOT NULL,
    target_id bigint NOT NULL,
    "position" integer
);


ALTER TABLE po_viewcontentrelation OWNER TO postgres;

--
-- Name: portalid_sequence; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE portalid_sequence
    START WITH 28473
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE portalid_sequence OWNER TO postgres;

--
-- Name: principal; Type: TABLE; Schema: public; Owner: kbee
--

CREATE TABLE principal (
    id bigint NOT NULL,
    lastmodifieddate timestamp with time zone DEFAULT now(),
    lastmodifieduser bigint,
    domain_id bigint,
    creationdate timestamp with time zone DEFAULT now()
);


ALTER TABLE principal OWNER TO kbee;

--
-- Name: profile; Type: TABLE; Schema: public; Owner: kbee
--

CREATE TABLE profile (
    id bigint NOT NULL,
    lastmodifieddate timestamp with time zone DEFAULT now(),
    lastmodifieduser bigint NOT NULL,
    entity bigint,
    domain_id bigint,
    creationdate timestamp with time zone DEFAULT now()
);


ALTER TABLE profile OWNER TO kbee;

--
-- Name: property; Type: TABLE; Schema: public; Owner: kbee
--

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

--
-- Name: propertyid_sequence; Type: SEQUENCE; Schema: public; Owner: kbee
--

CREATE SEQUENCE propertyid_sequence
    START WITH 1000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE propertyid_sequence OWNER TO kbee;

--
-- Name: qaid_sequence; Type: SEQUENCE; Schema: public; Owner: kbee
--

CREATE SEQUENCE qaid_sequence
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE qaid_sequence OWNER TO kbee;

--
-- Name: query_sequence; Type: SEQUENCE; Schema: public; Owner: kbee
--

CREATE SEQUENCE query_sequence
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE query_sequence OWNER TO kbee;

--
-- Name: resourcefile; Type: TABLE; Schema: public; Owner: kbee
--

CREATE TABLE resourcefile (
    resource_id bigint NOT NULL,
    file_id bigint NOT NULL,
    listorder integer,
    text character varying(128)
);


ALTER TABLE resourcefile OWNER TO kbee;

--
-- Name: resourcegroup; Type: TABLE; Schema: public; Owner: kbee
--

CREATE TABLE resourcegroup (
    id bigint NOT NULL,
    name character varying(256)
);


ALTER TABLE resourcegroup OWNER TO kbee;

--
-- Name: resourceid_sequence; Type: SEQUENCE; Schema: public; Owner: kbee
--

CREATE SEQUENCE resourceid_sequence
    START WITH 1000
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE resourceid_sequence OWNER TO kbee;

--
-- Name: rs_windsor_activity_pivot; Type: TABLE; Schema: public; Owner: kbee
--

CREATE TABLE rs_windsor_activity_pivot (
    id bigint NOT NULL,
    process bigint NOT NULL,
    procedure character varying(128),
    task character varying(128),
    user_id bigint NOT NULL,
    starttime timestamp with time zone,
    endtime timestamp with time zone,
    event character varying(128),
    content_id bigint,
    content_oid bigint,
    perfect boolean,
    approved boolean,
    type bigint,
    pmc bigint,
    property bigint,
    specialist bigint,
    date timestamp with time zone,
    running_process boolean,
    tag character varying(64),
    reason character varying(64),
    status bigint,
    duedate timestamp with time zone,
    content_template_alias character varying(24),
    submissiontime timestamp with time zone
);


ALTER TABLE rs_windsor_activity_pivot OWNER TO kbee;

--
-- Name: rs_windsor_process_pivot; Type: VIEW; Schema: public; Owner: kbee
--

CREATE VIEW rs_windsor_process_pivot AS
 SELECT rs_windsor_activity_pivot.process,
    min(rs_windsor_activity_pivot.starttime) AS starttime,
    last(rs_windsor_activity_pivot.endtime) AS endtime,
    sum(
        CASE
            WHEN (((rs_windsor_activity_pivot.task)::text = 'Submission'::text) OR ((rs_windsor_activity_pivot.task)::text = 'Resubmission'::text)) THEN 1
            ELSE 0
        END) AS submissions,
    last(rs_windsor_activity_pivot.type) AS type,
    last(rs_windsor_activity_pivot.pmc) AS pmc,
    last(rs_windsor_activity_pivot.property) AS property,
    last(rs_windsor_activity_pivot.specialist) AS specialist,
    last(rs_windsor_activity_pivot.perfect) AS perfect,
    last(rs_windsor_activity_pivot.approved) AS approved,
    last(rs_windsor_activity_pivot.running_process) AS running
   FROM rs_windsor_activity_pivot
  GROUP BY rs_windsor_activity_pivot.process;


ALTER TABLE rs_windsor_process_pivot OWNER TO kbee;

--
-- Name: savedquery; Type: TABLE; Schema: public; Owner: kbee
--

CREATE TABLE savedquery (
    id bigint NOT NULL,
    userprofile_id bigint,
    title character varying(512),
    statement text,
    "position" integer,
    console character varying(24),
    is_system boolean DEFAULT false
);


ALTER TABLE savedquery OWNER TO kbee;

--
-- Name: scheduler; Type: TABLE; Schema: public; Owner: kbee
--

CREATE TABLE scheduler (
    id bigint NOT NULL,
    request bytea,
    "time" timestamp with time zone DEFAULT now(),
    priority integer,
    error_count integer,
    description text,
    error_message character varying(512),
    title character varying(64),
    objectid character varying(256)
);


ALTER TABLE scheduler OWNER TO kbee;

--
-- Name: scheduler_sequence; Type: SEQUENCE; Schema: public; Owner: kbee
--

CREATE SEQUENCE scheduler_sequence
    START WITH 3
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE scheduler_sequence OWNER TO kbee;

--
-- Name: security_sequence; Type: SEQUENCE; Schema: public; Owner: kbee
--

CREATE SEQUENCE security_sequence
    START WITH 1000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE security_sequence OWNER TO kbee;

--
-- Name: sendemail_log_sequence; Type: SEQUENCE; Schema: public; Owner: kbee
--

CREATE SEQUENCE sendemail_log_sequence
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE sendemail_log_sequence OWNER TO kbee;

--
-- Name: timer_sequence; Type: SEQUENCE; Schema: public; Owner: kbee
--

CREATE SEQUENCE timer_sequence
    START WITH 100
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE timer_sequence OWNER TO kbee;

--
-- Name: userlabel; Type: TABLE; Schema: public; Owner: kbee
--

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

--
-- Name: userprofile; Type: TABLE; Schema: public; Owner: kbee
--

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
    email_notifications_pending boolean DEFAULT true
);


ALTER TABLE userprofile OWNER TO kbee;

--
-- Name: users; Type: TABLE; Schema: public; Owner: kbee
--

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
    uitheme character varying(32) DEFAULT 'rpdm'::character varying
);


ALTER TABLE users OWNER TO kbee;

--
-- Name: wf_activity; Type: TABLE; Schema: public; Owner: kbee
--

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

--
-- Name: wf_launcher; Type: TABLE; Schema: public; Owner: kbee
--

CREATE TABLE wf_launcher (
    id bigint NOT NULL,
    domain_id bigint NOT NULL,
    label character varying(128),
    contenttemplate_id bigint,
    procedure_id bigint,
    contextual boolean,
    acl bigint,
    isenabled boolean DEFAULT true
);


ALTER TABLE wf_launcher OWNER TO kbee;

--
-- Name: wf_procedure; Type: TABLE; Schema: public; Owner: kbee
--

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
    roles character varying(256),
    launcher character varying(128),
    diagram bigint,
    version integer DEFAULT 1
);


ALTER TABLE wf_procedure OWNER TO kbee;

--
-- Name: wf_process; Type: TABLE; Schema: public; Owner: kbee
--

CREATE TABLE wf_process (
    id bigint NOT NULL,
    procedure character varying(128),
    startime timestamp with time zone,
    endtime timestamp with time zone,
    status character varying(20),
    procedure_id bigint
);


ALTER TABLE wf_process OWNER TO kbee;

--
-- Name: workflow_sequence; Type: SEQUENCE; Schema: public; Owner: kbee
--

CREATE SEQUENCE workflow_sequence
    START WITH 1000
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE workflow_sequence OWNER TO kbee;

--
-- Name: kb_acl acl_pkey; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_acl
    ADD CONSTRAINT acl_pkey PRIMARY KEY (id);


--
-- Name: kb_aclentry aclentry_pkey; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_aclentry
    ADD CONSTRAINT aclentry_pkey PRIMARY KEY (id);


--
-- Name: kb_action_rule action_rule_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_action_rule
    ADD CONSTRAINT action_rule_pk PRIMARY KEY (id);


--
-- Name: drb_answer answer_pkey; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY drb_answer
    ADD CONSTRAINT answer_pkey PRIMARY KEY (content_id);


--
-- Name: api_logevent api_logevent_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY api_logevent
    ADD CONSTRAINT api_logevent_pkey PRIMARY KEY (event_id);


--
-- Name: api_soapevent api_soapevent_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY api_soapevent
    ADD CONSTRAINT api_soapevent_pkey PRIMARY KEY (event_id);


--
-- Name: po_diagrammable_area area_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_diagrammable_area
    ADD CONSTRAINT area_pkey PRIMARY KEY (po_id);


--
-- Name: kb_attribute attribute_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_attribute
    ADD CONSTRAINT attribute_pkey PRIMARY KEY (id);


--
-- Name: kb_attributetemplate attributetemplate_pkey; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_attributetemplate
    ADD CONSTRAINT attributetemplate_pkey PRIMARY KEY (id);


--
-- Name: authorities authorities_pk; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY authorities
    ADD CONSTRAINT authorities_pk PRIMARY KEY (username, authority);


--
-- Name: po_block_banners block_banners_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_block_banners
    ADD CONSTRAINT block_banners_pkey PRIMARY KEY (block_id);


--
-- Name: po_block_contact block_contact_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_block_contact
    ADD CONSTRAINT block_contact_pkey PRIMARY KEY (block_id);


--
-- Name: po_block_content_list block_content_list_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_block_content_list
    ADD CONSTRAINT block_content_list_pkey PRIMARY KEY (block_id);


--
-- Name: po_block_cumpleanos block_cumpleanos_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_block_cumpleanos
    ADD CONSTRAINT block_cumpleanos_pkey PRIMARY KEY (block_id);


--
-- Name: po_block_footer block_footer_view_list_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_block_footer
    ADD CONSTRAINT block_footer_view_list_pkey PRIMARY KEY (block_id);


--
-- Name: po_block_gallery_viewer block_gallery_viewer_pkey; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY po_block_gallery_viewer
    ADD CONSTRAINT block_gallery_viewer_pkey PRIMARY KEY (block_id);


--
-- Name: po_block_image_viewer block_image_viewer_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_block_image_viewer
    ADD CONSTRAINT block_image_viewer_pkey PRIMARY KEY (block_id);


--
-- Name: po_diagrammable_block block_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_diagrammable_block
    ADD CONSTRAINT block_pkey PRIMARY KEY (po_id);


--
-- Name: po_block_view_recent_list block_recent_view_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_block_view_recent_list
    ADD CONSTRAINT block_recent_view_pkey PRIMARY KEY (block_id);


--
-- Name: po_block_site_components block_sc_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_block_site_components
    ADD CONSTRAINT block_sc_pkey PRIMARY KEY (block_id);


--
-- Name: po_block_select_list block_select_list_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_block_select_list
    ADD CONSTRAINT block_select_list_pkey PRIMARY KEY (block_id);


--
-- Name: po_block_selector block_selector_view_list_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_block_selector
    ADD CONSTRAINT block_selector_view_list_pkey PRIMARY KEY (block_id);


--
-- Name: po_block_site_list block_site_list_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_block_site_list
    ADD CONSTRAINT block_site_list_pkey PRIMARY KEY (block_id);


--
-- Name: po_block_text block_text_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_block_text
    ADD CONSTRAINT block_text_pkey PRIMARY KEY (block_id);


--
-- Name: po_block_view_list block_view_list_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_block_view_list
    ADD CONSTRAINT block_view_list_pkey PRIMARY KEY (block_id);


--
-- Name: po_block_wall_viewer block_wall_viewer_pkey; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY po_block_wall_viewer
    ADD CONSTRAINT block_wall_viewer_pkey PRIMARY KEY (block_id);


--
-- Name: po_block_x block_x_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_block_x
    ADD CONSTRAINT block_x_pkey PRIMARY KEY (block_id);


--
-- Name: kb_cabinet cabinet_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_cabinet
    ADD CONSTRAINT cabinet_pkey PRIMARY KEY (id);


--
-- Name: kb_cabinet_reader cabinet_reader_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_cabinet_reader
    ADD CONSTRAINT cabinet_reader_pk PRIMARY KEY (cabinet_id, group_id);


--
-- Name: kb_classifier classifier_pkey; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_classifier
    ADD CONSTRAINT classifier_pkey PRIMARY KEY (id);


--
-- Name: classifiercontent classifiercontent_pkey; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY classifiercontent
    ADD CONSTRAINT classifiercontent_pkey PRIMARY KEY (classifier_id, contentclass_id);


--
-- Name: kb_classifiertemplate classifiertemplate_pkey; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_classifiertemplate
    ADD CONSTRAINT classifiertemplate_pkey PRIMARY KEY (id);


--
-- Name: kb_comment comment_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_comment
    ADD CONSTRAINT comment_pkey PRIMARY KEY (content_id);


--
-- Name: content content_oiversion_unique; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY content
    ADD CONSTRAINT content_oiversion_unique UNIQUE (oid, version);


--
-- Name: content content_pkey; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY content
    ADD CONSTRAINT content_pkey PRIMARY KEY (id);


--
-- Name: kb_contentattribute contentattribute_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_contentattribute
    ADD CONSTRAINT contentattribute_pkey PRIMARY KEY (contenttemplate_id, attributetemplate_id);


--
-- Name: po_contentblock contentblock_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_contentblock
    ADD CONSTRAINT contentblock_pkey PRIMARY KEY (block_id, content_id);


--
-- Name: contentclass contentclass_pkey; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY contentclass
    ADD CONSTRAINT contentclass_pkey PRIMARY KEY (id);


--
-- Name: classification contentclassification_pkey; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY classification
    ADD CONSTRAINT contentclassification_pkey PRIMARY KEY (id);


--
-- Name: contentproperties contentproperties_pkey; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY contentproperties
    ADD CONSTRAINT contentproperties_pkey PRIMARY KEY (content_id);


--
-- Name: property contentproperty_pkey; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY property
    ADD CONSTRAINT contentproperty_pkey PRIMARY KEY (id);


--
-- Name: kb_content_relation contentrelation_id_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_content_relation
    ADD CONSTRAINT contentrelation_id_pk PRIMARY KEY (id);


--
-- Name: contentresource contentresource_pkey; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY contentresource
    ADD CONSTRAINT contentresource_pkey PRIMARY KEY (id);


--
-- Name: contentresource contentresource_unique; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY contentresource
    ADD CONSTRAINT contentresource_unique UNIQUE (content_id, resource_id);


--
-- Name: contentstat contentstat_pkey; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY contentstat
    ADD CONSTRAINT contentstat_pkey PRIMARY KEY (content_id);


--
-- Name: kb_contenttemplate contenttemplate_pkey; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_contenttemplate
    ADD CONSTRAINT contenttemplate_pkey PRIMARY KEY (id);


--
-- Name: kb_contentresourcegroup contenttemplateresourcegroup_pkey; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_contentresourcegroup
    ADD CONSTRAINT contenttemplateresourcegroup_pkey PRIMARY KEY (template_id, group_id);


--
-- Name: databasechangeloglock databasechangeloglock_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY databasechangeloglock
    ADD CONSTRAINT databasechangeloglock_pkey PRIMARY KEY (id);


--
-- Name: dataset dataset_pkey; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY dataset
    ADD CONSTRAINT dataset_pkey PRIMARY KEY (id);


--
-- Name: kb_datasetattribute datasetattribute_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_datasetattribute
    ADD CONSTRAINT datasetattribute_pkey PRIMARY KEY (dataset_id, attributetemplate_id);


--
-- Name: datasetclassifier datasetclassifier_pkey; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY datasetclassifier
    ADD CONSTRAINT datasetclassifier_pkey PRIMARY KEY (dataset_id, classifier_id);


--
-- Name: datasetmember datasetmember_pkey; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY datasetmember
    ADD CONSTRAINT datasetmember_pkey PRIMARY KEY (id);


--
-- Name: kb_email_template dlk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_email_template
    ADD CONSTRAINT dlk UNIQUE (domain_id, lang, xkey);


--
-- Name: kb_domain_settings domain_cat_unique; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_domain_settings
    ADD CONSTRAINT domain_cat_unique UNIQUE (domain_id, category);


--
-- Name: kb_ds_element_template dse_template_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_ds_element_template
    ADD CONSTRAINT dse_template_pk PRIMARY KEY (id);


--
-- Name: kb_email_template email_template_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_email_template
    ADD CONSTRAINT email_template_pkey PRIMARY KEY (id);


--
-- Name: kb_enotirule enotirule_pkey; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_enotirule
    ADD CONSTRAINT enotirule_pkey PRIMARY KEY (id);


--
-- Name: kb_enotirule_principal enotirule_principal_pkey; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_enotirule_principal
    ADD CONSTRAINT enotirule_principal_pkey PRIMARY KEY (rule_id, principal_id);


--
-- Name: entity entity_pkey; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY entity
    ADD CONSTRAINT entity_pkey PRIMARY KEY (id);


--
-- Name: entitymatching entitymatching_pkey; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY entitymatching
    ADD CONSTRAINT entitymatching_pkey PRIMARY KEY (kbee_id, url);


--
-- Name: externalresource externalresource_pkey; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY externalresource
    ADD CONSTRAINT externalresource_pkey PRIMARY KEY (resource_id);


--
-- Name: kb_facet_wrapper facet_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_facet_wrapper
    ADD CONSTRAINT facet_pk PRIMARY KEY (id);


--
-- Name: kfile file_pkey; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kfile
    ADD CONSTRAINT file_pkey PRIMARY KEY (resource_id);


--
-- Name: kb_file_loader fileloader_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_file_loader
    ADD CONSTRAINT fileloader_pk PRIMARY KEY (id);


--
-- Name: kb_file_proxy fileproxy_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_file_proxy
    ADD CONSTRAINT fileproxy_pk PRIMARY KEY (resource_id);


--
-- Name: gallery gallery_pkey; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY gallery
    ADD CONSTRAINT gallery_pkey PRIMARY KEY (resource_id);


--
-- Name: galleryfile galleryfile_pkey; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY galleryfile
    ADD CONSTRAINT galleryfile_pkey PRIMARY KEY (gallery_id, file_id);


--
-- Name: kgroup group_pkey; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kgroup
    ADD CONSTRAINT group_pkey PRIMARY KEY (id);


--
-- Name: kgroupmember groupmember_pkey; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kgroupmember
    ADD CONSTRAINT groupmember_pkey PRIMARY KEY (kgroup, principal);


--
-- Name: domain id_pkey; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY domain
    ADD CONSTRAINT id_pkey PRIMARY KEY (id);


--
-- Name: idoc idoc_pkey; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY idoc
    ADD CONSTRAINT idoc_pkey PRIMARY KEY (content_id);


--
-- Name: idocsection idocsection_pkey; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY idocsection
    ADD CONSTRAINT idocsection_pkey PRIMARY KEY (id);


--
-- Name: idocsectionresource idocsectionresource_pkey; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY idocsectionresource
    ADD CONSTRAINT idocsectionresource_pkey PRIMARY KEY (section_id, resource_id);


--
-- Name: kb_import_data import_data_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_import_data
    ADD CONSTRAINT import_data_pkey PRIMARY KEY (server_url, remote_domain, local_domain, object_class, remote_id);


--
-- Name: kb_cronjob kb_cronjob_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_cronjob
    ADD CONSTRAINT kb_cronjob_pk PRIMARY KEY (id);


--
-- Name: kb_delta_sql kb_delta_sql_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_delta_sql
    ADD CONSTRAINT kb_delta_sql_pk PRIMARY KEY (id);


--
-- Name: kb_domain_settings kb_domain_settings_pkey; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_domain_settings
    ADD CONSTRAINT kb_domain_settings_pkey PRIMARY KEY (id);


--
-- Name: kb_language_string kb_language_string_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_language_string
    ADD CONSTRAINT kb_language_string_pkey PRIMARY KEY (id);


--
-- Name: kb_preference_domain kb_preference_domain_domain_id_name_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_preference_domain
    ADD CONSTRAINT kb_preference_domain_domain_id_name_key UNIQUE (domain_id, name);


--
-- Name: kb_preference_domain kb_preference_domain_domain_id_name_key1; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_preference_domain
    ADD CONSTRAINT kb_preference_domain_domain_id_name_key1 UNIQUE (domain_id, name);


--
-- Name: kb_preference_domain kb_preference_domain_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_preference_domain
    ADD CONSTRAINT kb_preference_domain_pkey PRIMARY KEY (id);


--
-- Name: kb_searcher_homeblock kb_searcher_homeblock_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_searcher_homeblock
    ADD CONSTRAINT kb_searcher_homeblock_pkey PRIMARY KEY (id);


--
-- Name: kb_tree_file kb_tree_file_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_tree_file
    ADD CONSTRAINT kb_tree_file_pkey PRIMARY KEY (id);


--
-- Name: kb_tree_idoc kb_tree_idoc_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_tree_idoc
    ADD CONSTRAINT kb_tree_idoc_pkey PRIMARY KEY (content_id);


--
-- Name: kb_api_usage_stat kb_ts_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_api_usage_stat
    ADD CONSTRAINT kb_ts_pk PRIMARY KEY (ts);


--
-- Name: kb_worknote_principal kb_worknote_principal_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_worknote_principal
    ADD CONSTRAINT kb_worknote_principal_pk PRIMARY KEY (note_id, principal_id);


--
-- Name: kresource kresource_prev_version_key; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kresource
    ADD CONSTRAINT kresource_prev_version_key UNIQUE (prev_version);


--
-- Name: kb_language_string localekey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_language_string
    ADD CONSTRAINT localekey UNIQUE (locale, key);


--
-- Name: klock lock_pkey; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY klock
    ADD CONSTRAINT lock_pkey PRIMARY KEY (lock_id);


--
-- Name: logevent logevent_pkey; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY logevent
    ADD CONSTRAINT logevent_pkey PRIMARY KEY (event_id);


--
-- Name: memberclassification memberclassification_pkey; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY memberclassification
    ADD CONSTRAINT memberclassification_pkey PRIMARY KEY (id);


--
-- Name: kb_member_role memberrole_id_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_member_role
    ADD CONSTRAINT memberrole_id_pk PRIMARY KEY (id);


--
-- Name: kb_model_section modelsection_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_model_section
    ADD CONSTRAINT modelsection_pk PRIMARY KEY (id);


--
-- Name: kb_move move_id_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_move
    ADD CONSTRAINT move_id_pk PRIMARY KEY (content_id, domain);


--
-- Name: kb_notification notification_pkey; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_notification
    ADD CONSTRAINT notification_pkey PRIMARY KEY (id);


--
-- Name: kb_object_property objectproperty_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_object_property
    ADD CONSTRAINT objectproperty_pkey PRIMARY KEY (id);


--
-- Name: organization organization_pkey; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY organization
    ADD CONSTRAINT organization_pkey PRIMARY KEY (id);


--
-- Name: kb_organizationaldata organizationaldata_pkey; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_organizationaldata
    ADD CONSTRAINT organizationaldata_pkey PRIMARY KEY (id);


--
-- Name: orgchart orgchart_pkey; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY orgchart
    ADD CONSTRAINT orgchart_pkey PRIMARY KEY (content_id);


--
-- Name: organizationaltext ot_pkey; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY organizationaltext
    ADD CONSTRAINT ot_pkey PRIMARY KEY (content_id);


--
-- Name: po_diagrammable_page page_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_diagrammable_page
    ADD CONSTRAINT page_pkey PRIMARY KEY (po_id);


--
-- Name: person person_pk; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY person
    ADD CONSTRAINT person_pk PRIMARY KEY (entity_id);


--
-- Name: po_portalobject po_oiversion_unique; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_portalobject
    ADD CONSTRAINT po_oiversion_unique UNIQUE (oid, version);


--
-- Name: po_portalobject po_portalobject_prev_version_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_portalobject
    ADD CONSTRAINT po_portalobject_prev_version_key UNIQUE (prev_version);


--
-- Name: po_site po_site_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_site
    ADD CONSTRAINT po_site_pkey PRIMARY KEY (po_id);


--
-- Name: po_sitelogin po_sitelogin_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_sitelogin
    ADD CONSTRAINT po_sitelogin_pkey PRIMARY KEY (id);


--
-- Name: po_sitelogout po_sitelogout_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_sitelogout
    ADD CONSTRAINT po_sitelogout_pkey PRIMARY KEY (id);


--
-- Name: po_portalobject portalobject_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_portalobject
    ADD CONSTRAINT portalobject_pkey PRIMARY KEY (id);


--
-- Name: principal principal_pkey; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY principal
    ADD CONSTRAINT principal_pkey PRIMARY KEY (id);


--
-- Name: profile profile_pkey; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY profile
    ADD CONSTRAINT profile_pkey PRIMARY KEY (id);


--
-- Name: drb_question question_pkey; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY drb_question
    ADD CONSTRAINT question_pkey PRIMARY KEY (content_id);


--
-- Name: kb_relation_template relationtemplate_id_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_relation_template
    ADD CONSTRAINT relationtemplate_id_pk PRIMARY KEY (id);


--
-- Name: kb_report report_pkey; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_report
    ADD CONSTRAINT report_pkey PRIMARY KEY (id);


--
-- Name: kresource resource_pkey; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kresource
    ADD CONSTRAINT resource_pkey PRIMARY KEY (id);


--
-- Name: resourcefile resourcefile_pkey; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY resourcefile
    ADD CONSTRAINT resourcefile_pkey PRIMARY KEY (resource_id, file_id);


--
-- Name: resourcegroup resourcegroup_pkey; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY resourcegroup
    ADD CONSTRAINT resourcegroup_pkey PRIMARY KEY (id);


--
-- Name: kb_role role_id_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_role
    ADD CONSTRAINT role_id_pk PRIMARY KEY (id);


--
-- Name: rs_windsor_activity_pivot rs_activity_id_pkey; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY rs_windsor_activity_pivot
    ADD CONSTRAINT rs_activity_id_pkey PRIMARY KEY (id);


--
-- Name: kb_content_rsbycriteria rsbycriteria_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_content_rsbycriteria
    ADD CONSTRAINT rsbycriteria_pk PRIMARY KEY (id);


--
-- Name: kb_rsbycriteria_template rsbycriteria_template_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_rsbycriteria_template
    ADD CONSTRAINT rsbycriteria_template_pk PRIMARY KEY (id);


--
-- Name: savedquery savedquery_pk; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY savedquery
    ADD CONSTRAINT savedquery_pk PRIMARY KEY (id);


--
-- Name: scheduler scheduler_pk; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY scheduler
    ADD CONSTRAINT scheduler_pk PRIMARY KEY (id);


--
-- Name: kb_securitydata securitydata_pkey; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_securitydata
    ADD CONSTRAINT securitydata_pkey PRIMARY KEY (id);


--
-- Name: kb_security_rule securityrule_pkey; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_security_rule
    ADD CONSTRAINT securityrule_pkey PRIMARY KEY (id);


--
-- Name: kb_sendemailevent sendemailevent_pkey; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_sendemailevent
    ADD CONSTRAINT sendemailevent_pkey PRIMARY KEY (event_id);


--
-- Name: po_site_favorites_list site_favorites_list_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_site_favorites_list
    ADD CONSTRAINT site_favorites_list_pkey PRIMARY KEY (list_id, site_oid);


--
-- Name: po_site_favorites site_favorites_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_site_favorites
    ADD CONSTRAINT site_favorites_pkey PRIMARY KEY (id);


--
-- Name: po_diagrammable_site site_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_diagrammable_site
    ADD CONSTRAINT site_pkey PRIMARY KEY (po_id);


--
-- Name: po_site_securityrule site_securityrule_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_site_securityrule
    ADD CONSTRAINT site_securityrule_pkey PRIMARY KEY (rule_id);


--
-- Name: po_site_subscription site_subscription_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_site_subscription
    ADD CONSTRAINT site_subscription_pkey PRIMARY KEY (user_id, site_oid, event_id);


--
-- Name: po_siteuser siteuser_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_siteuser
    ADD CONSTRAINT siteuser_pkey PRIMARY KEY (site_id, user_id);


--
-- Name: po_siteuserrights siteuserrights_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_siteuserrights
    ADD CONSTRAINT siteuserrights_pk PRIMARY KEY (site_id, user_id);


--
-- Name: kb_source source_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_source
    ADD CONSTRAINT source_pk PRIMARY KEY (id);


--
-- Name: kb_system_properties sp_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_system_properties
    ADD CONSTRAINT sp_pkey PRIMARY KEY (key);


--
-- Name: kb_subscription subscription_pkey; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_subscription
    ADD CONSTRAINT subscription_pkey PRIMARY KEY (user_id, content_oid, event_id);


--
-- Name: kb_timer timer_id_pk; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_timer
    ADD CONSTRAINT timer_id_pk PRIMARY KEY (id);


--
-- Name: kb_tip tip_pkey; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_tip
    ADD CONSTRAINT tip_pkey PRIMARY KEY (id);


--
-- Name: kb_tree_resource treeresource_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_tree_resource
    ADD CONSTRAINT treeresource_pk PRIMARY KEY (resource_id);


--
-- Name: idocsection unique_section_idoc; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY idocsection
    ADD CONSTRAINT unique_section_idoc UNIQUE (idoc_id, sectionorder);


--
-- Name: kb_usage_stat usage_stat_pkey; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_usage_stat
    ADD CONSTRAINT usage_stat_pkey PRIMARY KEY (domain_id, ts);


--
-- Name: kb_preference user_name_fk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_preference
    ADD CONSTRAINT user_name_fk UNIQUE (user_id, name);


--
-- Name: kb_user_note user_note_id_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_user_note
    ADD CONSTRAINT user_note_id_pk PRIMARY KEY (id);


--
-- Name: users user_pkey; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY users
    ADD CONSTRAINT user_pkey PRIMARY KEY (id);


--
-- Name: userlabel userlabel_pkey; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY userlabel
    ADD CONSTRAINT userlabel_pkey PRIMARY KEY (id);


--
-- Name: users username; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY users
    ADD CONSTRAINT username UNIQUE (username);


--
-- Name: userprofile userprofile_pkey; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY userprofile
    ADD CONSTRAINT userprofile_pkey PRIMARY KEY (id);


--
-- Name: kb_user_property userproperty_pkey; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_user_property
    ADD CONSTRAINT userproperty_pkey PRIMARY KEY (id);


--
-- Name: kb_user_role userrole_id_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_user_role
    ADD CONSTRAINT userrole_id_pk PRIMARY KEY (id);


--
-- Name: po_viewcontentrelation view_content_relation_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_viewcontentrelation
    ADD CONSTRAINT view_content_relation_pkey PRIMARY KEY (view_id, target_id);


--
-- Name: po_viewbk viewbk_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_viewbk
    ADD CONSTRAINT viewbk_pkey PRIMARY KEY (po_id);


--
-- Name: po_viewbkblock viewbkblock_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_viewbkblock
    ADD CONSTRAINT viewbkblock_pkey PRIMARY KEY (view_id);


--
-- Name: po_viewbkcontent viewbkcontent_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_viewbkcontent
    ADD CONSTRAINT viewbkcontent_pkey PRIMARY KEY (view_id);


--
-- Name: po_viewbklink viewbklink_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_viewbklink
    ADD CONSTRAINT viewbklink_pkey PRIMARY KEY (view_id);


--
-- Name: po_viewbksite viewbksite_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_viewbksite
    ADD CONSTRAINT viewbksite_pkey PRIMARY KEY (view_id);


--
-- Name: po_viewcontent viewcontent_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_viewcontent
    ADD CONSTRAINT viewcontent_pkey PRIMARY KEY (po_id);


--
-- Name: kb_vote vote_pkey; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_vote
    ADD CONSTRAINT vote_pkey PRIMARY KEY (id);


--
-- Name: wf_activity wf_activity_pkey; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY wf_activity
    ADD CONSTRAINT wf_activity_pkey PRIMARY KEY (id);


--
-- Name: wf_launcher wf_launcher_pkey; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY wf_launcher
    ADD CONSTRAINT wf_launcher_pkey PRIMARY KEY (id);


--
-- Name: kb_work_note wn_id_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_work_note
    ADD CONSTRAINT wn_id_pk PRIMARY KEY (id);


--
-- Name: kb_work_note_user_read wnur_id_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_work_note_user_read
    ADD CONSTRAINT wnur_id_pk PRIMARY KEY (id);


--
-- Name: wf_procedure workflowprocedure_pkey; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY wf_procedure
    ADD CONSTRAINT workflowprocedure_pkey PRIMARY KEY (id);


--
-- Name: wf_process workflowprocess_pkey; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY wf_process
    ADD CONSTRAINT workflowprocess_pkey PRIMARY KEY (id);


--
-- Name: api_logevent_date_asc_idx; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX api_logevent_date_asc_idx ON api_logevent USING btree (event_time);


--
-- Name: api_logevent_domain_date; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX api_logevent_domain_date ON api_logevent USING btree (event_domain, event_time DESC);


--
-- Name: api_logevent_event_time_idx; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX api_logevent_event_time_idx ON api_logevent USING btree (event_time DESC);


--
-- Name: api_logevent_file; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX api_logevent_file ON api_logevent USING btree (event_file);


--
-- Name: api_logevent_status_date_idx; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX api_logevent_status_date_idx ON api_logevent USING btree (event_status, event_time DESC);


--
-- Name: api_soapevent_event_time_idx; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX api_soapevent_event_time_idx ON api_soapevent USING btree (event_time DESC);


--
-- Name: block_cumpleanos_id_idx; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX block_cumpleanos_id_idx ON po_block_cumpleanos USING btree (block_id);


--
-- Name: block_x_id_idx; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX block_x_id_idx ON po_block_x USING btree (block_id);


--
-- Name: classification_content_id_classifier_id_idx; Type: INDEX; Schema: public; Owner: kbee
--

CREATE INDEX classification_content_id_classifier_id_idx ON classification USING btree (content_id, classifier_id);


--
-- Name: classifier_id_idx; Type: INDEX; Schema: public; Owner: kbee
--

CREATE INDEX classifier_id_idx ON kb_classifier USING btree (id);


--
-- Name: classifier_lower_idx; Type: INDEX; Schema: public; Owner: kbee
--

CREATE INDEX classifier_lower_idx ON kb_classifier USING btree (lower((name)::text));


--
-- Name: classifiercontent_classifier_id_contentclass_id_idx; Type: INDEX; Schema: public; Owner: kbee
--

CREATE INDEX classifiercontent_classifier_id_contentclass_id_idx ON classifiercontent USING btree (classifier_id, contentclass_id);


--
-- Name: classifiercontent_contentclass_id_classifier_id_idx; Type: INDEX; Schema: public; Owner: kbee
--

CREATE INDEX classifiercontent_contentclass_id_classifier_id_idx ON classifiercontent USING btree (contentclass_id, classifier_id);


--
-- Name: content_dom_id_name_idx; Type: INDEX; Schema: public; Owner: kbee
--

CREATE INDEX content_dom_id_name_idx ON content USING btree (domain_id, lower((name)::text));


--
-- Name: content_id_idx; Type: INDEX; Schema: public; Owner: kbee
--

CREATE INDEX content_id_idx ON content USING btree (id);


--
-- Name: content_lastmoddate_idx; Type: INDEX; Schema: public; Owner: kbee
--

CREATE INDEX content_lastmoddate_idx ON content USING btree (lastmodifieddate DESC);


--
-- Name: content_name_idx; Type: INDEX; Schema: public; Owner: kbee
--

CREATE INDEX content_name_idx ON content USING btree (lower((name)::text));


--
-- Name: content_title_idx; Type: INDEX; Schema: public; Owner: kbee
--

CREATE INDEX content_title_idx ON content USING btree (lower((title)::text));


--
-- Name: contentblock_block_id_orden_idx; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX contentblock_block_id_orden_idx ON po_contentblock USING btree (block_id, orden);


--
-- Name: contentclass_name_idx; Type: INDEX; Schema: public; Owner: kbee
--

CREATE INDEX contentclass_name_idx ON contentclass USING btree (lower((name)::text));


--
-- Name: contentproperties_content_id_idx; Type: INDEX; Schema: public; Owner: kbee
--

CREATE INDEX contentproperties_content_id_idx ON contentproperties USING btree (content_id);


--
-- Name: contentresource_content_id_resource_id_idx; Type: INDEX; Schema: public; Owner: kbee
--

CREATE INDEX contentresource_content_id_resource_id_idx ON contentresource USING btree (content_id, resource_id);


--
-- Name: contentstat_content_id_idx; Type: INDEX; Schema: public; Owner: kbee
--

CREATE INDEX contentstat_content_id_idx ON contentstat USING btree (content_id);


--
-- Name: contenttemplate_name_idx; Type: INDEX; Schema: public; Owner: kbee
--

CREATE INDEX contenttemplate_name_idx ON kb_contenttemplate USING btree (lower((name)::text));


--
-- Name: dataset_domain_id_lower_idx; Type: INDEX; Schema: public; Owner: kbee
--

CREATE INDEX dataset_domain_id_lower_idx ON dataset USING btree (domain_id, lower((name)::text));


--
-- Name: dataset_id_idx; Type: INDEX; Schema: public; Owner: kbee
--

CREATE INDEX dataset_id_idx ON dataset USING btree (id);


--
-- Name: domain_id; Type: INDEX; Schema: public; Owner: kbee
--

CREATE INDEX domain_id ON domain USING btree (id);


--
-- Name: domain_modified; Type: INDEX; Schema: public; Owner: kbee
--

CREATE INDEX domain_modified ON domain USING btree (lastmodifieddate DESC);


--
-- Name: domain_name; Type: INDEX; Schema: public; Owner: kbee
--

CREATE INDEX domain_name ON domain USING btree (lower((name)::text));


--
-- Name: drb_answer_content_id_idx; Type: INDEX; Schema: public; Owner: kbee
--

CREATE INDEX drb_answer_content_id_idx ON drb_answer USING btree (content_id);


--
-- Name: drb_answer_question_id_votes_idx; Type: INDEX; Schema: public; Owner: kbee
--

CREATE INDEX drb_answer_question_id_votes_idx ON drb_answer USING btree (question_id, votes);


--
-- Name: drb_question_content_id_idx; Type: INDEX; Schema: public; Owner: kbee
--

CREATE INDEX drb_question_content_id_idx ON drb_question USING btree (content_id);


--
-- Name: file_path_idx; Type: INDEX; Schema: public; Owner: kbee
--

CREATE INDEX file_path_idx ON kfile USING btree (path);


--
-- Name: gallery_lower_idx; Type: INDEX; Schema: public; Owner: kbee
--

CREATE INDEX gallery_lower_idx ON gallery USING btree (lower((title)::text));


--
-- Name: gallery_resource_id_idx; Type: INDEX; Schema: public; Owner: kbee
--

CREATE INDEX gallery_resource_id_idx ON gallery USING btree (resource_id);


--
-- Name: galleryfile_gallery_id_file_id_idx; Type: INDEX; Schema: public; Owner: kbee
--

CREATE INDEX galleryfile_gallery_id_file_id_idx ON galleryfile USING btree (gallery_id, file_id);


--
-- Name: galleryfile_gallery_id_gorder_idx; Type: INDEX; Schema: public; Owner: kbee
--

CREATE INDEX galleryfile_gallery_id_gorder_idx ON galleryfile USING btree (gallery_id, gorder);


--
-- Name: idoc_content_id_idx; Type: INDEX; Schema: public; Owner: kbee
--

CREATE INDEX idoc_content_id_idx ON idoc USING btree (content_id);


--
-- Name: idoc_editorialstate_idx; Type: INDEX; Schema: public; Owner: kbee
--

CREATE INDEX idoc_editorialstate_idx ON idoc USING btree (editorialstate);


--
-- Name: idoc_lower_idx; Type: INDEX; Schema: public; Owner: kbee
--

CREATE INDEX idoc_lower_idx ON idoc USING btree (lower((title)::text));


--
-- Name: idocsection_idoc_id_sectionorder_idx; Type: INDEX; Schema: public; Owner: kbee
--

CREATE INDEX idocsection_idoc_id_sectionorder_idx ON idocsection USING btree (idoc_id, sectionorder);


--
-- Name: idocsectionresource_section_id_position_idx; Type: INDEX; Schema: public; Owner: kbee
--

CREATE INDEX idocsectionresource_section_id_position_idx ON idocsectionresource USING btree (section_id, "position");


--
-- Name: kb_cronjob_name_idx; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX kb_cronjob_name_idx ON kb_cronjob USING btree (lower((name)::text));


--
-- Name: kb_email_template_domain_idx; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX kb_email_template_domain_idx ON kb_email_template USING btree (domain_id, lang, xkey);


--
-- Name: kb_enoti_owner_idx; Type: INDEX; Schema: public; Owner: kbee
--

CREATE INDEX kb_enoti_owner_idx ON kb_enotirule USING btree (owner, lower((name)::text));


--
-- Name: kb_external_id_idx; Type: INDEX; Schema: public; Owner: kbee
--

CREATE INDEX kb_external_id_idx ON content USING btree (external_id);


--
-- Name: kb_notification_receiver_alert_idx; Type: INDEX; Schema: public; Owner: kbee
--

CREATE INDEX kb_notification_receiver_alert_idx ON kb_notification USING btree (receiver_id, isbillboard, state, creationdate);


--
-- Name: kb_notification_receiver_billboard_idx; Type: INDEX; Schema: public; Owner: kbee
--

CREATE INDEX kb_notification_receiver_billboard_idx ON kb_notification USING btree (receiver_id, isbillboard, state, creationdate);


--
-- Name: kb_notification_user_state_idx; Type: INDEX; Schema: public; Owner: kbee
--

CREATE INDEX kb_notification_user_state_idx ON kb_notification USING btree (receiver_id, notification_state);


--
-- Name: kb_sendemailevent_event_domain_id_event_time_idx; Type: INDEX; Schema: public; Owner: kbee
--

CREATE INDEX kb_sendemailevent_event_domain_id_event_time_idx ON kb_sendemailevent USING btree (event_domain_id, event_time DESC);


--
-- Name: kb_subscription_content_oid_event_id_idx; Type: INDEX; Schema: public; Owner: kbee
--

CREATE INDEX kb_subscription_content_oid_event_id_idx ON kb_subscription USING btree (content_oid, event_id);


--
-- Name: kb_tip_id_idx; Type: INDEX; Schema: public; Owner: kbee
--

CREATE INDEX kb_tip_id_idx ON kb_tip USING btree (tip_lang, area, lower((tip_title)::text));


--
-- Name: kb_un_user_idx; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX kb_un_user_idx ON kb_user_note USING btree (user_id, creationdate DESC);


--
-- Name: kb_usage_domain_idx; Type: INDEX; Schema: public; Owner: kbee
--

CREATE INDEX kb_usage_domain_idx ON kb_usage_stat USING btree (domain_id, ts);


--
-- Name: kb_user_role_role_idx; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX kb_user_role_role_idx ON kb_user_role USING btree (role_id);


--
-- Name: kb_wn_user_idx; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX kb_wn_user_idx ON kb_work_note USING btree (domain_id, creationdate DESC);


--
-- Name: kb_wnur_user_idx; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX kb_wnur_user_idx ON kb_work_note_user_read USING btree (user_id, work_note_id);


--
-- Name: kcomment_referenced_content_id_date_submitted_idx; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX kcomment_referenced_content_id_date_submitted_idx ON kb_comment USING btree (referenced_content_id, date_submitted DESC);


--
-- Name: kfile_bucket_idx; Type: INDEX; Schema: public; Owner: kbee
--

CREATE INDEX kfile_bucket_idx ON kfile USING btree (bucketname, objectname);


--
-- Name: kfile_storagemode_idx; Type: INDEX; Schema: public; Owner: kbee
--

CREATE INDEX kfile_storagemode_idx ON kfile USING btree (storagemode, bucketname);


--
-- Name: kgroupmember_principal_idx; Type: INDEX; Schema: public; Owner: kbee
--

CREATE INDEX kgroupmember_principal_idx ON kgroupmember USING btree (principal);


--
-- Name: kgroupname_idx; Type: INDEX; Schema: public; Owner: kbee
--

CREATE INDEX kgroupname_idx ON kgroup USING btree (lower((name)::text));


--
-- Name: klock_lock_object_id_idx; Type: INDEX; Schema: public; Owner: kbee
--

CREATE INDEX klock_lock_object_id_idx ON klock USING btree (lock_object_id);


--
-- Name: logevent_activity_idx; Type: INDEX; Schema: public; Owner: kbee
--

CREATE INDEX logevent_activity_idx ON logevent USING btree (event_activity_id, event_time);


--
-- Name: logevent_event_content_id_idx; Type: INDEX; Schema: public; Owner: kbee
--

CREATE INDEX logevent_event_content_id_idx ON logevent USING btree (event_content_id);


--
-- Name: logevent_object_idx; Type: INDEX; Schema: public; Owner: kbee
--

CREATE INDEX logevent_object_idx ON logevent USING btree (event_object_id);


--
-- Name: notification_id_idx; Type: INDEX; Schema: public; Owner: kbee
--

CREATE INDEX notification_id_idx ON kb_notification USING btree (id);


--
-- Name: notification_receiver_id_state_datesend_idx; Type: INDEX; Schema: public; Owner: kbee
--

CREATE INDEX notification_receiver_id_state_datesend_idx ON kb_notification USING btree (receiver_id, state, datesend);


--
-- Name: organization_id_idx; Type: INDEX; Schema: public; Owner: kbee
--

CREATE INDEX organization_id_idx ON organization USING btree (id);


--
-- Name: organization_lastmoddate_idx; Type: INDEX; Schema: public; Owner: kbee
--

CREATE INDEX organization_lastmoddate_idx ON organization USING btree (lastmodifieddate DESC);


--
-- Name: organization_name_idx; Type: INDEX; Schema: public; Owner: kbee
--

CREATE INDEX organization_name_idx ON organization USING btree (lower((name)::text));


--
-- Name: organizationaltext_content_id_idx; Type: INDEX; Schema: public; Owner: kbee
--

CREATE INDEX organizationaltext_content_id_idx ON organizationaltext USING btree (content_id);


--
-- Name: orgchart_content_id_idx; Type: INDEX; Schema: public; Owner: kbee
--

CREATE INDEX orgchart_content_id_idx ON orgchart USING btree (content_id);


--
-- Name: po_area_page_id_orden_idx; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX po_area_page_id_orden_idx ON po_diagrammable_area USING btree (page_id, orden);


--
-- Name: po_area_po_id_idx; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX po_area_po_id_idx ON po_diagrammable_area USING btree (po_id);


--
-- Name: po_block_area_id_section_orden_idx; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX po_block_area_id_section_orden_idx ON po_diagrammable_block USING btree (area_id, section, orden);


--
-- Name: po_block_banners_block_id_idx; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX po_block_banners_block_id_idx ON po_block_banners USING btree (block_id);


--
-- Name: po_block_contact_block_id_idx; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX po_block_contact_block_id_idx ON po_block_contact USING btree (block_id);


--
-- Name: po_block_content_list_block_id_idx; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX po_block_content_list_block_id_idx ON po_block_content_list USING btree (block_id);


--
-- Name: po_block_footer_block_id_idx; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX po_block_footer_block_id_idx ON po_block_footer USING btree (block_id);


--
-- Name: po_block_po_id_idx; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX po_block_po_id_idx ON po_diagrammable_block USING btree (po_id);


--
-- Name: po_block_selector_block_id_idx; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX po_block_selector_block_id_idx ON po_block_selector USING btree (block_id);


--
-- Name: po_block_site_components_block_id_idx; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX po_block_site_components_block_id_idx ON po_block_site_components USING btree (block_id);


--
-- Name: po_block_site_list_block_id_idx; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX po_block_site_list_block_id_idx ON po_block_site_list USING btree (block_id);


--
-- Name: po_block_text_block_id_idx; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX po_block_text_block_id_idx ON po_block_text USING btree (block_id);


--
-- Name: po_block_view_list_block_id_idx; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX po_block_view_list_block_id_idx ON po_block_view_list USING btree (block_id);


--
-- Name: po_block_view_recent_list_block_id_idx; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX po_block_view_recent_list_block_id_idx ON po_block_view_recent_list USING btree (block_id);


--
-- Name: po_page_po_id_idx; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX po_page_po_id_idx ON po_diagrammable_page USING btree (po_id);


--
-- Name: po_page_site_id_po_id_idx; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX po_page_site_id_po_id_idx ON po_diagrammable_page USING btree (site_id, po_id);


--
-- Name: po_portalobject_domain_id_lower_idx; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX po_portalobject_domain_id_lower_idx ON po_portalobject USING btree (domain_id, lower((title)::text));


--
-- Name: po_portalobject_domain_id_state_lastmodifieddate_idx; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX po_portalobject_domain_id_state_lastmodifieddate_idx ON po_portalobject USING btree (domain_id, state, lastmodifieddate);


--
-- Name: po_portalobject_domain_id_state_lower_idx; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX po_portalobject_domain_id_state_lower_idx ON po_portalobject USING btree (domain_id, state, lower((title)::text));


--
-- Name: po_portalobject_oid_version_idx; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX po_portalobject_oid_version_idx ON po_portalobject USING btree (oid, version);


--
-- Name: po_site_po_id_idx; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX po_site_po_id_idx ON po_diagrammable_site USING btree (po_id);


--
-- Name: po_sitelogin_site_id_page_id_visit_time_idx; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX po_sitelogin_site_id_page_id_visit_time_idx ON po_sitelogin USING btree (site_id, page_id, visit_time);


--
-- Name: po_sitelogin_visit_time_idx; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX po_sitelogin_visit_time_idx ON po_sitelogin USING btree (visit_time);


--
-- Name: po_siteuser_site_id_permission_user_id_idx; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX po_siteuser_site_id_permission_user_id_idx ON po_siteuser USING btree (site_id, permission, user_id);


--
-- Name: po_siteuserrights_site_id_user_id_idx; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX po_siteuserrights_site_id_user_id_idx ON po_siteuserrights USING btree (site_id, user_id);


--
-- Name: property_content_id_idx; Type: INDEX; Schema: public; Owner: kbee
--

CREATE INDEX property_content_id_idx ON property USING btree (content_id);


--
-- Name: property_object_id_idx; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX property_object_id_idx ON kb_object_property USING btree (object_id);


--
-- Name: property_user_id_idx; Type: INDEX; Schema: public; Owner: kbee
--

CREATE INDEX property_user_id_idx ON kb_user_property USING btree (user_id);


--
-- Name: relation_source_idx; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX relation_source_idx ON kb_content_relation USING btree (source_id);


--
-- Name: report_content_id_user_id_idx; Type: INDEX; Schema: public; Owner: kbee
--

CREATE INDEX report_content_id_user_id_idx ON kb_report USING btree (content_id, user_id);


--
-- Name: report_user_id_content_id_idx; Type: INDEX; Schema: public; Owner: kbee
--

CREATE INDEX report_user_id_content_id_idx ON kb_report USING btree (user_id, content_id);


--
-- Name: resource_domain_id_idx; Type: INDEX; Schema: public; Owner: kbee
--

CREATE INDEX resource_domain_id_idx ON kresource USING btree (domain_id, lower((title)::text));


--
-- Name: resource_modified; Type: INDEX; Schema: public; Owner: kbee
--

CREATE INDEX resource_modified ON kresource USING btree (domain_id, lastmodifieddate DESC);


--
-- Name: resource_name; Type: INDEX; Schema: public; Owner: kbee
--

CREATE INDEX resource_name ON kresource USING btree (domain_id, lower((name)::text));


--
-- Name: resource_name_global; Type: INDEX; Schema: public; Owner: kbee
--

CREATE INDEX resource_name_global ON kresource USING btree (lower((name)::text));


--
-- Name: scheduler_id_idx; Type: INDEX; Schema: public; Owner: kbee
--

CREATE INDEX scheduler_id_idx ON scheduler USING btree (id);


--
-- Name: scheduler_priority_time_idx; Type: INDEX; Schema: public; Owner: kbee
--

CREATE INDEX scheduler_priority_time_idx ON scheduler USING btree (priority, "time");


--
-- Name: site_security_rule_object_idx; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX site_security_rule_object_idx ON po_site_securityrule USING btree (related_object_id);


--
-- Name: sp_lower_case_key; Type: INDEX; Schema: public; Owner: postgres
--

CREATE UNIQUE INDEX sp_lower_case_key ON kb_system_properties USING btree (lower((key)::text));


--
-- Name: users_id_idx; Type: INDEX; Schema: public; Owner: kbee
--

CREATE INDEX users_id_idx ON users USING btree (id);


--
-- Name: users_lastmodifieddate_idx; Type: INDEX; Schema: public; Owner: kbee
--

CREATE INDEX users_lastmodifieddate_idx ON users USING btree (lastmodifieddate DESC);


--
-- Name: users_name_idx; Type: INDEX; Schema: public; Owner: kbee
--

CREATE INDEX users_name_idx ON users USING btree (username);


--
-- Name: vote_content_id_user_id_idx; Type: INDEX; Schema: public; Owner: kbee
--

CREATE INDEX vote_content_id_user_id_idx ON kb_vote USING btree (content_id, user_id);


--
-- Name: vote_user_id_content_id_idx; Type: INDEX; Schema: public; Owner: kbee
--

CREATE INDEX vote_user_id_content_id_idx ON kb_vote USING btree (user_id, content_id);


--
-- Name: ws_end; Type: INDEX; Schema: public; Owner: kbee
--

CREATE INDEX ws_end ON rs_windsor_activity_pivot USING btree (endtime);


--
-- Name: ws_pmc; Type: INDEX; Schema: public; Owner: kbee
--

CREATE INDEX ws_pmc ON rs_windsor_activity_pivot USING btree (pmc, property, starttime, endtime);


--
-- Name: ws_process; Type: INDEX; Schema: public; Owner: kbee
--

CREATE INDEX ws_process ON rs_windsor_activity_pivot USING btree (process);


--
-- Name: ws_property; Type: INDEX; Schema: public; Owner: kbee
--

CREATE INDEX ws_property ON rs_windsor_activity_pivot USING btree (property, starttime);


--
-- Name: ws_start; Type: INDEX; Schema: public; Owner: kbee
--

CREATE INDEX ws_start ON rs_windsor_activity_pivot USING btree (starttime);


--
-- Name: kb_aclentry acl_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_aclentry
    ADD CONSTRAINT acl_fk FOREIGN KEY (acl) REFERENCES kb_acl(id) ON DELETE CASCADE;


--
-- Name: kb_security_rule acl_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_security_rule
    ADD CONSTRAINT acl_fk FOREIGN KEY (acl) REFERENCES kb_acl(id) ON DELETE RESTRICT;


--
-- Name: kb_contenttemplate acl_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_contenttemplate
    ADD CONSTRAINT acl_fk FOREIGN KEY (acl) REFERENCES kb_acl(id) ON DELETE RESTRICT;


--
-- Name: wf_launcher acl_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY wf_launcher
    ADD CONSTRAINT acl_fk FOREIGN KEY (acl) REFERENCES kb_acl(id);


--
-- Name: content acl_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY content
    ADD CONSTRAINT acl_fk FOREIGN KEY (acl) REFERENCES kb_acl(id) ON DELETE RESTRICT;


--
-- Name: kb_action_rule action_rule_domain; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_action_rule
    ADD CONSTRAINT action_rule_domain FOREIGN KEY (domain_id) REFERENCES domain(id) ON DELETE CASCADE;


--
-- Name: po_diagrammable_area area_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_diagrammable_area
    ADD CONSTRAINT area_fk FOREIGN KEY (po_id) REFERENCES po_portalobject(id) ON DELETE CASCADE;


--
-- Name: po_diagrammable_block area_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_diagrammable_block
    ADD CONSTRAINT area_fk FOREIGN KEY (area_id) REFERENCES po_diagrammable_area(po_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: kb_assignable_role assignablerole_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_assignable_role
    ADD CONSTRAINT assignablerole_fk FOREIGN KEY (assignablerole_id) REFERENCES kb_role(id);


--
-- Name: wf_activity assigned_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY wf_activity
    ADD CONSTRAINT assigned_fk FOREIGN KEY (assigned_by) REFERENCES users(id);


--
-- Name: kb_contentattribute attribute_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_contentattribute
    ADD CONSTRAINT attribute_fk FOREIGN KEY (attributetemplate_id) REFERENCES kb_attributetemplate(id) ON DELETE RESTRICT;


--
-- Name: kb_datasetattribute attribute_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_datasetattribute
    ADD CONSTRAINT attribute_fk FOREIGN KEY (attributetemplate_id) REFERENCES kb_attributetemplate(id) ON DELETE RESTRICT;


--
-- Name: kb_attributetemplate attribute_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_attributetemplate
    ADD CONSTRAINT attribute_fk FOREIGN KEY (attribute_id) REFERENCES kb_attribute(id) ON DELETE RESTRICT;


--
-- Name: kb_attributetemplate attributetemplate_section_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_attributetemplate
    ADD CONSTRAINT attributetemplate_section_fk FOREIGN KEY (section_id) REFERENCES kb_model_section(id) ON DELETE CASCADE;


--
-- Name: po_block_contact block_contact_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_block_contact
    ADD CONSTRAINT block_contact_fk FOREIGN KEY (block_id) REFERENCES po_diagrammable_block(po_id) ON DELETE CASCADE;


--
-- Name: po_diagrammable_block block_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_diagrammable_block
    ADD CONSTRAINT block_fk FOREIGN KEY (po_id) REFERENCES po_portalobject(id) ON DELETE CASCADE;


--
-- Name: po_block_site_list block_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_block_site_list
    ADD CONSTRAINT block_fk FOREIGN KEY (block_id) REFERENCES po_diagrammable_block(po_id) ON DELETE CASCADE;


--
-- Name: po_block_text block_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_block_text
    ADD CONSTRAINT block_fk FOREIGN KEY (block_id) REFERENCES po_diagrammable_block(po_id) ON DELETE CASCADE;


--
-- Name: po_block_view_list block_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_block_view_list
    ADD CONSTRAINT block_fk FOREIGN KEY (block_id) REFERENCES po_diagrammable_block(po_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: po_block_content_list block_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_block_content_list
    ADD CONSTRAINT block_fk FOREIGN KEY (block_id) REFERENCES po_diagrammable_block(po_id) ON DELETE CASCADE;


--
-- Name: po_block_cumpleanos block_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_block_cumpleanos
    ADD CONSTRAINT block_fk FOREIGN KEY (block_id) REFERENCES po_diagrammable_block(po_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: po_block_x block_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_block_x
    ADD CONSTRAINT block_fk FOREIGN KEY (block_id) REFERENCES po_diagrammable_block(po_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: po_contentblock block_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_contentblock
    ADD CONSTRAINT block_fk FOREIGN KEY (block_id) REFERENCES po_diagrammable_block(po_id) ON DELETE CASCADE;


--
-- Name: po_block_banners block_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_block_banners
    ADD CONSTRAINT block_fk FOREIGN KEY (block_id) REFERENCES po_block_view_list(block_id) ON DELETE CASCADE;


--
-- Name: po_block_image_viewer block_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_block_image_viewer
    ADD CONSTRAINT block_fk FOREIGN KEY (block_id) REFERENCES po_diagrammable_block(po_id) ON DELETE RESTRICT;


--
-- Name: po_block_select_list block_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_block_select_list
    ADD CONSTRAINT block_fk FOREIGN KEY (block_id) REFERENCES po_diagrammable_block(po_id) ON DELETE RESTRICT;


--
-- Name: po_block_search_external block_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_block_search_external
    ADD CONSTRAINT block_fk FOREIGN KEY (block_id) REFERENCES po_diagrammable_block(po_id) ON DELETE RESTRICT;


--
-- Name: po_block_wall_viewer block_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY po_block_wall_viewer
    ADD CONSTRAINT block_fk FOREIGN KEY (block_id) REFERENCES po_block_view_list(block_id) ON DELETE CASCADE;


--
-- Name: po_block_footer block_footer_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_block_footer
    ADD CONSTRAINT block_footer_fk FOREIGN KEY (block_id) REFERENCES po_diagrammable_block(po_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: po_block_gallery_viewer block_gallery_viewer_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY po_block_gallery_viewer
    ADD CONSTRAINT block_gallery_viewer_fk FOREIGN KEY (block_id) REFERENCES po_block_view_list(block_id) ON DELETE CASCADE;


--
-- Name: po_diagrammable_block block_image_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_diagrammable_block
    ADD CONSTRAINT block_image_fk FOREIGN KEY (block_image) REFERENCES idoc(content_id) ON DELETE SET NULL;


--
-- Name: po_block_view_recent_list block_recent_view_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_block_view_recent_list
    ADD CONSTRAINT block_recent_view_fk FOREIGN KEY (block_id) REFERENCES po_block_view_list(block_id) ON DELETE CASCADE;


--
-- Name: po_block_site_components block_sc_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_block_site_components
    ADD CONSTRAINT block_sc_fk FOREIGN KEY (block_id) REFERENCES po_diagrammable_block(po_id) ON DELETE CASCADE;


--
-- Name: po_block_selector block_selector_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_block_selector
    ADD CONSTRAINT block_selector_fk FOREIGN KEY (block_id) REFERENCES po_diagrammable_block(po_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: kb_cabinet_reader cabinet_reader_cabinet; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_cabinet_reader
    ADD CONSTRAINT cabinet_reader_cabinet FOREIGN KEY (cabinet_id) REFERENCES kb_cabinet(id) ON DELETE CASCADE;


--
-- Name: kb_cabinet_reader cabinet_reader_group; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_cabinet_reader
    ADD CONSTRAINT cabinet_reader_group FOREIGN KEY (group_id) REFERENCES kgroup(id) ON DELETE CASCADE;


--
-- Name: classifiercontent classifier_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY classifiercontent
    ADD CONSTRAINT classifier_fk FOREIGN KEY (classifier_id) REFERENCES kb_classifier(id) ON DELETE RESTRICT;


--
-- Name: classification classifier_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY classification
    ADD CONSTRAINT classifier_fk FOREIGN KEY (classifier_id) REFERENCES kb_classifier(id) ON DELETE RESTRICT;


--
-- Name: datasetclassifier classifier_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY datasetclassifier
    ADD CONSTRAINT classifier_fk FOREIGN KEY (classifier_id) REFERENCES kb_classifier(id) ON DELETE RESTRICT;


--
-- Name: memberclassification classifier_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY memberclassification
    ADD CONSTRAINT classifier_fk FOREIGN KEY (classifier_id) REFERENCES kb_classifier(id) ON DELETE RESTRICT;


--
-- Name: kb_classifiertemplate classifier_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_classifiertemplate
    ADD CONSTRAINT classifier_fk FOREIGN KEY (classifier_id) REFERENCES kb_classifier(id) ON DELETE RESTRICT;


--
-- Name: kb_classifiertemplate classifiertemplate_section_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_classifiertemplate
    ADD CONSTRAINT classifiertemplate_section_fk FOREIGN KEY (section_id) REFERENCES kb_model_section(id) ON DELETE CASCADE;


--
-- Name: classification clsf_content_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY classification
    ADD CONSTRAINT clsf_content_fk FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE CASCADE;


--
-- Name: kb_comment comment_content_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_comment
    ADD CONSTRAINT comment_content_fk FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE CASCADE;


--
-- Name: kb_comment comment_responses_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_comment
    ADD CONSTRAINT comment_responses_fk FOREIGN KEY (parent_comment) REFERENCES kb_comment(content_id) ON DELETE CASCADE;


--
-- Name: property content_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY property
    ADD CONSTRAINT content_fk FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE CASCADE;


--
-- Name: idocsection content_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY idocsection
    ADD CONSTRAINT content_fk FOREIGN KEY (idoc_id) REFERENCES idoc(content_id) ON DELETE RESTRICT;


--
-- Name: kb_notification content_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_notification
    ADD CONSTRAINT content_fk FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE SET NULL;


--
-- Name: drb_question content_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY drb_question
    ADD CONSTRAINT content_fk FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE CASCADE;


--
-- Name: drb_answer content_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY drb_answer
    ADD CONSTRAINT content_fk FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE CASCADE;


--
-- Name: orgchart content_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY orgchart
    ADD CONSTRAINT content_fk FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE RESTRICT;


--
-- Name: idoc content_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY idoc
    ADD CONSTRAINT content_fk FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE CASCADE;


--
-- Name: organizationaltext content_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY organizationaltext
    ADD CONSTRAINT content_fk FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE CASCADE;


--
-- Name: kb_report content_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_report
    ADD CONSTRAINT content_id_fk FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE CASCADE;


--
-- Name: classifiercontent contentclass_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY classifiercontent
    ADD CONSTRAINT contentclass_fk FOREIGN KEY (contentclass_id) REFERENCES contentclass(id) ON DELETE RESTRICT;


--
-- Name: content contenttemplate_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY content
    ADD CONSTRAINT contenttemplate_fk FOREIGN KEY (contenttemplate) REFERENCES kb_contenttemplate(id) ON DELETE RESTRICT;


--
-- Name: kb_classifiertemplate contenttemplate_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_classifiertemplate
    ADD CONSTRAINT contenttemplate_fk FOREIGN KEY (contenttemplate_id) REFERENCES kb_contenttemplate(id) ON DELETE CASCADE;


--
-- Name: kb_contentattribute contenttemplate_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_contentattribute
    ADD CONSTRAINT contenttemplate_fk FOREIGN KEY (contenttemplate_id) REFERENCES kb_contenttemplate(id) ON DELETE RESTRICT;


--
-- Name: wf_launcher contenttemplate_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY wf_launcher
    ADD CONSTRAINT contenttemplate_fk FOREIGN KEY (contenttemplate_id) REFERENCES kb_contenttemplate(id) ON DELETE CASCADE;


--
-- Name: kb_content_relation cr_source_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_content_relation
    ADD CONSTRAINT cr_source_fk FOREIGN KEY (source_id) REFERENCES content(id) ON DELETE CASCADE;


--
-- Name: kb_content_relation cr_target_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_content_relation
    ADD CONSTRAINT cr_target_fk FOREIGN KEY (target_id) REFERENCES content(id) ON DELETE CASCADE;


--
-- Name: kb_content_relation cr_template_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_content_relation
    ADD CONSTRAINT cr_template_fk FOREIGN KEY (template_id) REFERENCES kb_relation_template(id) ON DELETE RESTRICT;


--
-- Name: kb_classifier dataset2_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_classifier
    ADD CONSTRAINT dataset2_fk FOREIGN KEY (dataset2_id) REFERENCES dataset(id) ON DELETE RESTRICT;


--
-- Name: kb_classifier dataset3_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_classifier
    ADD CONSTRAINT dataset3_fk FOREIGN KEY (dataset3_id) REFERENCES dataset(id) ON DELETE RESTRICT;


--
-- Name: datasetmember dataset_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY datasetmember
    ADD CONSTRAINT dataset_fk FOREIGN KEY (dataset_id) REFERENCES dataset(id) ON DELETE RESTRICT;


--
-- Name: kb_classifier dataset_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_classifier
    ADD CONSTRAINT dataset_fk FOREIGN KEY (dataset_id) REFERENCES dataset(id) ON DELETE RESTRICT;


--
-- Name: datasetclassifier dataset_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY datasetclassifier
    ADD CONSTRAINT dataset_fk FOREIGN KEY (dataset_id) REFERENCES dataset(id) ON DELETE RESTRICT;


--
-- Name: kb_datasetattribute dataset_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_datasetattribute
    ADD CONSTRAINT dataset_fk FOREIGN KEY (dataset_id) REFERENCES dataset(id) ON DELETE RESTRICT;


--
-- Name: classification datasetmember_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY classification
    ADD CONSTRAINT datasetmember_fk FOREIGN KEY (datasetmember_id) REFERENCES datasetmember(id) ON DELETE RESTRICT;


--
-- Name: kfile des_user_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kfile
    ADD CONSTRAINT des_user_fk FOREIGN KEY (uploadeduser) REFERENCES users(id);


--
-- Name: entity domain_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY entity
    ADD CONSTRAINT domain_fk FOREIGN KEY (domain_id) REFERENCES domain(id) ON DELETE RESTRICT;


--
-- Name: profile domain_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY profile
    ADD CONSTRAINT domain_fk FOREIGN KEY (domain_id) REFERENCES domain(id) ON DELETE RESTRICT;


--
-- Name: principal domain_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY principal
    ADD CONSTRAINT domain_fk FOREIGN KEY (domain_id) REFERENCES domain(id) ON DELETE RESTRICT;


--
-- Name: kb_security_rule domain_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_security_rule
    ADD CONSTRAINT domain_fk FOREIGN KEY (domain_id) REFERENCES domain(id) ON DELETE RESTRICT;


--
-- Name: kb_enotirule domain_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_enotirule
    ADD CONSTRAINT domain_fk FOREIGN KEY (domain_id) REFERENCES domain(id) ON DELETE RESTRICT;


--
-- Name: organization domain_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY organization
    ADD CONSTRAINT domain_fk FOREIGN KEY (domain_id) REFERENCES domain(id) ON DELETE RESTRICT;


--
-- Name: kresource domain_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kresource
    ADD CONSTRAINT domain_fk FOREIGN KEY (domain_id) REFERENCES domain(id) ON DELETE RESTRICT;


--
-- Name: kb_contenttemplate domain_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_contenttemplate
    ADD CONSTRAINT domain_fk FOREIGN KEY (domain_id) REFERENCES domain(id) ON DELETE CASCADE;


--
-- Name: content domain_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY content
    ADD CONSTRAINT domain_fk FOREIGN KEY (domain_id) REFERENCES domain(id) ON DELETE RESTRICT;


--
-- Name: dataset domain_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY dataset
    ADD CONSTRAINT domain_fk FOREIGN KEY (domain_id) REFERENCES domain(id) ON DELETE CASCADE;


--
-- Name: datasetmember domain_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY datasetmember
    ADD CONSTRAINT domain_fk FOREIGN KEY (domain_id) REFERENCES domain(id) ON DELETE RESTRICT;


--
-- Name: kb_classifier domain_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_classifier
    ADD CONSTRAINT domain_fk FOREIGN KEY (domain_id) REFERENCES domain(id) ON DELETE CASCADE;


--
-- Name: kb_notification domain_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_notification
    ADD CONSTRAINT domain_fk FOREIGN KEY (domain_id) REFERENCES domain(id) ON DELETE CASCADE;


--
-- Name: wf_procedure domain_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY wf_procedure
    ADD CONSTRAINT domain_fk FOREIGN KEY (domain_id) REFERENCES domain(id) ON DELETE CASCADE;


--
-- Name: wf_launcher domain_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY wf_launcher
    ADD CONSTRAINT domain_fk FOREIGN KEY (domain_id) REFERENCES domain(id) ON DELETE CASCADE;


--
-- Name: kb_email_template domain_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_email_template
    ADD CONSTRAINT domain_fk FOREIGN KEY (domain_id) REFERENCES domain(id) ON DELETE CASCADE;


--
-- Name: kb_attribute domain_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_attribute
    ADD CONSTRAINT domain_fk FOREIGN KEY (domain_id) REFERENCES domain(id) ON DELETE CASCADE;


--
-- Name: kb_cabinet domain_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_cabinet
    ADD CONSTRAINT domain_fk FOREIGN KEY (domain_id) REFERENCES domain(id) ON DELETE CASCADE;


--
-- Name: po_portalobject domain_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_portalobject
    ADD CONSTRAINT domain_fk FOREIGN KEY (domain_id) REFERENCES domain(id) ON DELETE RESTRICT;


--
-- Name: kb_tree_file domain_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_tree_file
    ADD CONSTRAINT domain_fk FOREIGN KEY (domain_id) REFERENCES domain(id) ON DELETE CASCADE;


--
-- Name: kb_searcher_homeblock domain_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_searcher_homeblock
    ADD CONSTRAINT domain_fk FOREIGN KEY (domain_id) REFERENCES domain(id) ON DELETE RESTRICT;


--
-- Name: domain domain_logo_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY domain
    ADD CONSTRAINT domain_logo_fk FOREIGN KEY (logo) REFERENCES kfile(resource_id) ON DELETE SET NULL;


--
-- Name: kb_domain_settings ds_domain_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_domain_settings
    ADD CONSTRAINT ds_domain_fk FOREIGN KEY (domain_id) REFERENCES domain(id) ON DELETE CASCADE;


--
-- Name: kb_ds_element_template dse_attribute_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_ds_element_template
    ADD CONSTRAINT dse_attribute_fk FOREIGN KEY (attribute_id) REFERENCES kb_attribute(id);


--
-- Name: kb_ds_element_template dse_classifier_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_ds_element_template
    ADD CONSTRAINT dse_classifier_fk FOREIGN KEY (classifier_id) REFERENCES kb_classifier(id);


--
-- Name: kb_ds_element_template dse_dataset_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_ds_element_template
    ADD CONSTRAINT dse_dataset_fk FOREIGN KEY (dataset_id) REFERENCES dataset(id);


--
-- Name: person entity_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY person
    ADD CONSTRAINT entity_fk FOREIGN KEY (entity_id) REFERENCES entity(id);


--
-- Name: profile entity_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY profile
    ADD CONSTRAINT entity_fk FOREIGN KEY (entity) REFERENCES entity(id) ON DELETE CASCADE;


--
-- Name: datasetmember entity_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY datasetmember
    ADD CONSTRAINT entity_fk FOREIGN KEY (entity_id) REFERENCES entity(id) ON DELETE RESTRICT;


--
-- Name: externalresource externalresource_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY externalresource
    ADD CONSTRAINT externalresource_fk FOREIGN KEY (resource_id) REFERENCES kresource(id) ON DELETE RESTRICT;


--
-- Name: kb_facet_wrapper facet_domain_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_facet_wrapper
    ADD CONSTRAINT facet_domain_fk FOREIGN KEY (domain_id) REFERENCES domain(id) ON DELETE CASCADE;


--
-- Name: resourcefile file_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY resourcefile
    ADD CONSTRAINT file_fk FOREIGN KEY (file_id) REFERENCES kresource(id) ON DELETE RESTRICT;


--
-- Name: galleryfile file_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY galleryfile
    ADD CONSTRAINT file_fk FOREIGN KEY (file_id) REFERENCES kfile(resource_id) ON DELETE RESTRICT;


--
-- Name: kfile file_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kfile
    ADD CONSTRAINT file_fk FOREIGN KEY (resource_id) REFERENCES kresource(id) ON DELETE CASCADE;


--
-- Name: person file_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY person
    ADD CONSTRAINT file_fk FOREIGN KEY (photo) REFERENCES kfile(resource_id) ON DELETE SET NULL;


--
-- Name: kb_file_proxy fileproxy_loader_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_file_proxy
    ADD CONSTRAINT fileproxy_loader_fk FOREIGN KEY (file_loader) REFERENCES kb_file_loader(id) ON DELETE RESTRICT;


--
-- Name: kb_file_proxy fileproxy_resource_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_file_proxy
    ADD CONSTRAINT fileproxy_resource_fk FOREIGN KEY (resource_id) REFERENCES kresource(id) ON DELETE RESTRICT;


--
-- Name: authorities fk_authorities_users; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY authorities
    ADD CONSTRAINT fk_authorities_users FOREIGN KEY (username) REFERENCES users(username);


--
-- Name: po_diagrammable_site footer_block_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_diagrammable_site
    ADD CONSTRAINT footer_block_fk FOREIGN KEY (footer_block_id) REFERENCES po_diagrammable_block(po_id) ON DELETE SET NULL;


--
-- Name: gallery gallery_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY gallery
    ADD CONSTRAINT gallery_fk FOREIGN KEY (resource_id) REFERENCES kresource(id) ON DELETE RESTRICT;


--
-- Name: galleryfile gallery_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY galleryfile
    ADD CONSTRAINT gallery_fk FOREIGN KEY (gallery_id) REFERENCES gallery(resource_id) ON DELETE RESTRICT;


--
-- Name: kresource group_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kresource
    ADD CONSTRAINT group_fk FOREIGN KEY (group_id) REFERENCES resourcegroup(id) ON DELETE RESTRICT;


--
-- Name: dataset group_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY dataset
    ADD CONSTRAINT group_fk FOREIGN KEY (group_id) REFERENCES kgroup(id) ON DELETE RESTRICT;


--
-- Name: kb_organizationaldata group_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_organizationaldata
    ADD CONSTRAINT group_fk FOREIGN KEY (group_id) REFERENCES kgroup(id) ON DELETE CASCADE;


--
-- Name: kb_securitydata group_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_securitydata
    ADD CONSTRAINT group_fk FOREIGN KEY (group_id) REFERENCES kgroup(id) ON DELETE CASCADE;


--
-- Name: kgroupmember group_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kgroupmember
    ADD CONSTRAINT group_fk FOREIGN KEY (kgroup) REFERENCES kgroup(id) ON DELETE CASCADE;


--
-- Name: datasetmember group_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY datasetmember
    ADD CONSTRAINT group_fk FOREIGN KEY (group_id) REFERENCES kgroup(id) ON DELETE RESTRICT;


--
-- Name: kb_role group_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_role
    ADD CONSTRAINT group_fk FOREIGN KEY (group_id) REFERENCES kgroup(id) ON DELETE RESTRICT;


--
-- Name: kb_member_role group_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_member_role
    ADD CONSTRAINT group_fk FOREIGN KEY (group_id) REFERENCES kgroup(id) ON DELETE RESTRICT;


--
-- Name: kb_group_role grouprole_group_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_group_role
    ADD CONSTRAINT grouprole_group_fk FOREIGN KEY (group_id) REFERENCES kgroup(id);


--
-- Name: kb_group_role grouprole_role_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_group_role
    ADD CONSTRAINT grouprole_role_fk FOREIGN KEY (role_id) REFERENCES kb_role(id) ON DELETE CASCADE;


--
-- Name: po_diagrammable_site header_block_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_diagrammable_site
    ADD CONSTRAINT header_block_fk FOREIGN KEY (header_block_id) REFERENCES po_diagrammable_block(po_id) ON DELETE SET NULL;


--
-- Name: po_diagrammable_block image_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_diagrammable_block
    ADD CONSTRAINT image_fk FOREIGN KEY (image_id) REFERENCES kfile(resource_id) ON DELETE RESTRICT;


--
-- Name: po_block_image_viewer image_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_block_image_viewer
    ADD CONSTRAINT image_fk FOREIGN KEY (imageviewer_id) REFERENCES idoc(content_id) ON DELETE SET NULL;


--
-- Name: kb_cronjob kb_cronjob_modifieduser_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_cronjob
    ADD CONSTRAINT kb_cronjob_modifieduser_fk FOREIGN KEY (lastmodifieduser) REFERENCES users(id);


--
-- Name: kb_preference_domain kb_preference_domain_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_preference_domain
    ADD CONSTRAINT kb_preference_domain_domain_id_fkey FOREIGN KEY (domain_id) REFERENCES domain(id) ON DELETE CASCADE;


--
-- Name: kb_relation_template kb_relation_template_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_relation_template
    ADD CONSTRAINT kb_relation_template_domain_id_fkey FOREIGN KEY (domain_id) REFERENCES domain(id);


--
-- Name: kb_relation_template kb_relation_template_domain_id_fkey1; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_relation_template
    ADD CONSTRAINT kb_relation_template_domain_id_fkey1 FOREIGN KEY (domain_id) REFERENCES domain(id);


--
-- Name: kb_relation_template kb_relation_template_lastmodifieduser_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_relation_template
    ADD CONSTRAINT kb_relation_template_lastmodifieduser_fkey FOREIGN KEY (lastmodifieduser) REFERENCES users(id);


--
-- Name: kb_relation_template kb_relation_template_lastmodifieduser_fkey1; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_relation_template
    ADD CONSTRAINT kb_relation_template_lastmodifieduser_fkey1 FOREIGN KEY (lastmodifieduser) REFERENCES users(id);


--
-- Name: kb_worknote_principal kb_worknote_principal_note_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_worknote_principal
    ADD CONSTRAINT kb_worknote_principal_note_id_fkey FOREIGN KEY (note_id) REFERENCES kb_work_note(id) ON DELETE CASCADE;


--
-- Name: kb_worknote_principal kb_worknote_principal_principal_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_worknote_principal
    ADD CONSTRAINT kb_worknote_principal_principal_id_fkey FOREIGN KEY (principal_id) REFERENCES principal(id) ON DELETE CASCADE;


--
-- Name: contentproperties lastmodifieduser_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY contentproperties
    ADD CONSTRAINT lastmodifieduser_fk FOREIGN KEY (lastmodifieduser) REFERENCES users(id) ON DELETE CASCADE;


--
-- Name: wf_procedure lastmodifieduser_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY wf_procedure
    ADD CONSTRAINT lastmodifieduser_fk FOREIGN KEY (lastmodifieduser) REFERENCES users(id) ON DELETE CASCADE;


--
-- Name: kb_cabinet lastmodifieduser_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_cabinet
    ADD CONSTRAINT lastmodifieduser_fk FOREIGN KEY (lastmodifieduser) REFERENCES users(id);


--
-- Name: kb_classifiertemplate member_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_classifiertemplate
    ADD CONSTRAINT member_fk FOREIGN KEY (root_id) REFERENCES datasetmember(id) ON DELETE RESTRICT;


--
-- Name: datasetmember member_securityrule_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY datasetmember
    ADD CONSTRAINT member_securityrule_fk FOREIGN KEY (securityrule_id) REFERENCES kb_security_rule(id);


--
-- Name: kb_member_role memberrole_entity_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_member_role
    ADD CONSTRAINT memberrole_entity_fk FOREIGN KEY (entity_id) REFERENCES datasetmember(id);


--
-- Name: kb_member_role memberrole_role_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_member_role
    ADD CONSTRAINT memberrole_role_fk FOREIGN KEY (role_id) REFERENCES kb_role(id) ON DELETE CASCADE;


--
-- Name: kb_member_role memberrole_securityrule_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_member_role
    ADD CONSTRAINT memberrole_securityrule_fk FOREIGN KEY (securityrule_id) REFERENCES kb_security_rule(id);


--
-- Name: kb_model_section modelsection_template_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_model_section
    ADD CONSTRAINT modelsection_template_fk FOREIGN KEY (contenttemplate_id) REFERENCES kb_contenttemplate(id) ON DELETE CASCADE;


--
-- Name: kb_enotirule owner_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_enotirule
    ADD CONSTRAINT owner_fk FOREIGN KEY (owner) REFERENCES users(id) ON DELETE CASCADE;


--
-- Name: po_diagrammable_page page_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_diagrammable_page
    ADD CONSTRAINT page_fk FOREIGN KEY (po_id) REFERENCES po_portalobject(id) ON DELETE RESTRICT;


--
-- Name: po_diagrammable_area page_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_diagrammable_area
    ADD CONSTRAINT page_fk FOREIGN KEY (page_id) REFERENCES po_diagrammable_page(po_id) ON DELETE CASCADE;


--
-- Name: po_diagrammable_block page_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_diagrammable_block
    ADD CONSTRAINT page_fk FOREIGN KEY (page_link) REFERENCES po_diagrammable_page(po_id);


--
-- Name: po_diagrammable_site page_header_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_diagrammable_site
    ADD CONSTRAINT page_header_fk FOREIGN KEY (page_header_footer_id) REFERENCES po_diagrammable_page(po_id);


--
-- Name: datasetmember parent_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY datasetmember
    ADD CONSTRAINT parent_fk FOREIGN KEY (parent) REFERENCES datasetmember(id) ON DELETE RESTRICT;


--
-- Name: po_portalobject parent_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_portalobject
    ADD CONSTRAINT parent_fk FOREIGN KEY (parent_id) REFERENCES po_portalobject(id) ON DELETE RESTRICT;


--
-- Name: kb_tree_file parent_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_tree_file
    ADD CONSTRAINT parent_fk FOREIGN KEY (parent_id) REFERENCES kb_tree_file(id) ON DELETE CASCADE;


--
-- Name: kb_classifiertemplate parent_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_classifiertemplate
    ADD CONSTRAINT parent_fk FOREIGN KEY (parent_id) REFERENCES kb_classifier(id) ON DELETE RESTRICT;


--
-- Name: kb_organizationaldata person_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_organizationaldata
    ADD CONSTRAINT person_fk FOREIGN KEY (person_id) REFERENCES person(entity_id) ON DELETE CASCADE;


--
-- Name: kb_securitydata person_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_securitydata
    ADD CONSTRAINT person_fk FOREIGN KEY (person_id) REFERENCES person(entity_id) ON DELETE CASCADE;


--
-- Name: po_diagrammable_block po_dblk_content_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_diagrammable_block
    ADD CONSTRAINT po_dblk_content_fk FOREIGN KEY (content_link) REFERENCES content(id) ON DELETE SET NULL;


--
-- Name: po_contentblock pocbk_content_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_contentblock
    ADD CONSTRAINT pocbk_content_fk FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE RESTRICT;


--
-- Name: po_diagrammable_page podgpage_content_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_diagrammable_page
    ADD CONSTRAINT podgpage_content_fk FOREIGN KEY (content_link) REFERENCES content(id) ON DELETE SET NULL;


--
-- Name: po_viewcontent poview_content_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_viewcontent
    ADD CONSTRAINT poview_content_fk FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE CASCADE;


--
-- Name: content prev_version_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY content
    ADD CONSTRAINT prev_version_fk FOREIGN KEY (prev_version) REFERENCES content(id) ON DELETE RESTRICT;


--
-- Name: kgroup principal_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kgroup
    ADD CONSTRAINT principal_fk FOREIGN KEY (id) REFERENCES principal(id) ON DELETE RESTRICT;


--
-- Name: kb_enotirule_principal principal_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_enotirule_principal
    ADD CONSTRAINT principal_fk FOREIGN KEY (principal_id) REFERENCES principal(id) ON DELETE CASCADE;


--
-- Name: kgroupmember principal_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kgroupmember
    ADD CONSTRAINT principal_fk FOREIGN KEY (principal) REFERENCES principal(id) ON DELETE CASCADE;


--
-- Name: kb_aclentry principal_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_aclentry
    ADD CONSTRAINT principal_fk FOREIGN KEY (principal) REFERENCES principal(id) ON DELETE CASCADE;


--
-- Name: wf_launcher procedure_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY wf_launcher
    ADD CONSTRAINT procedure_fk FOREIGN KEY (procedure_id) REFERENCES wf_procedure(id);


--
-- Name: wf_process procedure_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY wf_process
    ADD CONSTRAINT procedure_fk FOREIGN KEY (procedure_id) REFERENCES wf_procedure(id);


--
-- Name: wf_activity process_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY wf_activity
    ADD CONSTRAINT process_fk FOREIGN KEY (process_id) REFERENCES wf_process(id);


--
-- Name: userprofile profile_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY userprofile
    ADD CONSTRAINT profile_fk FOREIGN KEY (id) REFERENCES profile(id);


--
-- Name: contentproperties prop_content_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY contentproperties
    ADD CONSTRAINT prop_content_fk FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE CASCADE;


--
-- Name: drb_answer question_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY drb_answer
    ADD CONSTRAINT question_fk FOREIGN KEY (question_id) REFERENCES drb_question(content_id) ON DELETE CASCADE;


--
-- Name: kb_cabinet reader_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_cabinet
    ADD CONSTRAINT reader_fk FOREIGN KEY (reader_group) REFERENCES kgroup(id) ON DELETE CASCADE;


--
-- Name: kb_notification receiver_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_notification
    ADD CONSTRAINT receiver_fk FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE CASCADE;


--
-- Name: kb_comment referenced_content_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_comment
    ADD CONSTRAINT referenced_content_id_fk FOREIGN KEY (referenced_content_id) REFERENCES content(id) ON DELETE CASCADE;


--
-- Name: kb_relation_target relation_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_relation_target
    ADD CONSTRAINT relation_fk FOREIGN KEY (relationtemplate_id) REFERENCES kb_relation_template(id) ON DELETE CASCADE;


--
-- Name: contentresource resource_content_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY contentresource
    ADD CONSTRAINT resource_content_fk FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE RESTRICT;


--
-- Name: resourcefile resource_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY resourcefile
    ADD CONSTRAINT resource_fk FOREIGN KEY (resource_id) REFERENCES kresource(id) ON DELETE RESTRICT;


--
-- Name: contentresource resource_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY contentresource
    ADD CONSTRAINT resource_fk FOREIGN KEY (resource_id) REFERENCES kresource(id) ON DELETE RESTRICT;


--
-- Name: idocsectionresource resource_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY idocsectionresource
    ADD CONSTRAINT resource_fk FOREIGN KEY (resource_id) REFERENCES kresource(id) ON DELETE RESTRICT;


--
-- Name: kb_contentresourcegroup resourcegroup_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_contentresourcegroup
    ADD CONSTRAINT resourcegroup_fk FOREIGN KEY (group_id) REFERENCES resourcegroup(id) ON DELETE RESTRICT;


--
-- Name: kb_role role_classifier_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_role
    ADD CONSTRAINT role_classifier_fk FOREIGN KEY (classifier_id) REFERENCES kb_classifier(id);


--
-- Name: kb_role role_domain_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_role
    ADD CONSTRAINT role_domain_fk FOREIGN KEY (domain_id) REFERENCES domain(id);


--
-- Name: kb_assignable_role role_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_assignable_role
    ADD CONSTRAINT role_fk FOREIGN KEY (role_id) REFERENCES kb_role(id) ON DELETE CASCADE;


--
-- Name: kb_role role_modifieduser_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_role
    ADD CONSTRAINT role_modifieduser_fk FOREIGN KEY (lastmodifieduser) REFERENCES users(id);


--
-- Name: kb_role rolesecurityrule__fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_role
    ADD CONSTRAINT rolesecurityrule__fk FOREIGN KEY (securityrule_id) REFERENCES kb_security_rule(id) ON DELETE RESTRICT;


--
-- Name: kb_content_rsbycriteria rsbycriteria_source_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_content_rsbycriteria
    ADD CONSTRAINT rsbycriteria_source_fk FOREIGN KEY (source_id) REFERENCES content(id) ON DELETE CASCADE;


--
-- Name: kb_content_rsbycriteria rsbycriteria_template_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_content_rsbycriteria
    ADD CONSTRAINT rsbycriteria_template_fk FOREIGN KEY (template_id) REFERENCES kb_rsbycriteria_template(id) ON DELETE CASCADE;


--
-- Name: kb_relation_template rt_source_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_relation_template
    ADD CONSTRAINT rt_source_fk FOREIGN KEY (sourcetemplate_id) REFERENCES kb_contenttemplate(id);


--
-- Name: kb_relation_template rt_target_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_relation_template
    ADD CONSTRAINT rt_target_fk FOREIGN KEY (targettemplate_id) REFERENCES kb_contenttemplate(id);


--
-- Name: kb_enotirule_principal rule_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_enotirule_principal
    ADD CONSTRAINT rule_fk FOREIGN KEY (rule_id) REFERENCES kb_enotirule(id) ON DELETE CASCADE;


--
-- Name: kb_organizationaldata rule_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_organizationaldata
    ADD CONSTRAINT rule_fk FOREIGN KEY (securityrule_id) REFERENCES kb_security_rule(id) ON DELETE CASCADE;


--
-- Name: kb_securitydata rule_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_securitydata
    ADD CONSTRAINT rule_fk FOREIGN KEY (securityrule_id) REFERENCES kb_security_rule(id) ON DELETE CASCADE;


--
-- Name: datasetmember rule_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY datasetmember
    ADD CONSTRAINT rule_fk FOREIGN KEY (rule_id) REFERENCES kb_security_rule(id) ON DELETE RESTRICT;


--
-- Name: po_site_securityrule rule_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_site_securityrule
    ADD CONSTRAINT rule_fk FOREIGN KEY (rule_id) REFERENCES kb_security_rule(id) ON DELETE CASCADE;


--
-- Name: idocsectionresource section_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY idocsectionresource
    ADD CONSTRAINT section_fk FOREIGN KEY (section_id) REFERENCES idocsection(id) ON DELETE RESTRICT;


--
-- Name: kb_notification sender_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_notification
    ADD CONSTRAINT sender_fk FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE SET NULL;


--
-- Name: po_site_favorites_list site_favorites_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_site_favorites_list
    ADD CONSTRAINT site_favorites_fk FOREIGN KEY (list_id) REFERENCES po_site_favorites(id) ON DELETE CASCADE;


--
-- Name: po_site_favorites_list site_favorites_site_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_site_favorites_list
    ADD CONSTRAINT site_favorites_site_fk FOREIGN KEY (site_oid) REFERENCES po_site(po_id) ON DELETE CASCADE;


--
-- Name: po_site_favorites site_favorites_user_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_site_favorites
    ADD CONSTRAINT site_favorites_user_fk FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;


--
-- Name: po_diagrammable_site site_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_diagrammable_site
    ADD CONSTRAINT site_fk FOREIGN KEY (po_id) REFERENCES po_portalobject(id) ON DELETE RESTRICT;


--
-- Name: po_siteuser site_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_siteuser
    ADD CONSTRAINT site_fk FOREIGN KEY (site_id) REFERENCES po_diagrammable_site(po_id) ON DELETE RESTRICT;


--
-- Name: po_diagrammable_page site_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_diagrammable_page
    ADD CONSTRAINT site_fk FOREIGN KEY (site_id) REFERENCES po_diagrammable_site(po_id) ON DELETE CASCADE;


--
-- Name: po_block_site_components site_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_block_site_components
    ADD CONSTRAINT site_fk FOREIGN KEY (site_id) REFERENCES po_diagrammable_site(po_id) ON DELETE SET NULL;


--
-- Name: po_siteuserrights site_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_siteuserrights
    ADD CONSTRAINT site_fk FOREIGN KEY (site_id) REFERENCES po_diagrammable_site(po_id) ON DELETE RESTRICT;


--
-- Name: po_site site_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_site
    ADD CONSTRAINT site_fk FOREIGN KEY (po_id) REFERENCES po_portalobject(id) ON DELETE RESTRICT;


--
-- Name: po_page site_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_page
    ADD CONSTRAINT site_fk FOREIGN KEY (site_id) REFERENCES po_site(po_id) ON DELETE CASCADE;


--
-- Name: po_diagrammable_site site_image_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_diagrammable_site
    ADD CONSTRAINT site_image_fk FOREIGN KEY (site_image) REFERENCES idoc(content_id) ON DELETE SET NULL;


--
-- Name: po_site site_image_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_site
    ADD CONSTRAINT site_image_fk FOREIGN KEY (site_image) REFERENCES idoc(content_id) ON DELETE SET NULL;


--
-- Name: po_site_subscription site_subscription_user_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_site_subscription
    ADD CONSTRAINT site_subscription_user_fk FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;


--
-- Name: kb_source source_domain; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_source
    ADD CONSTRAINT source_domain FOREIGN KEY (domain_id) REFERENCES domain(id) ON DELETE CASCADE;


--
-- Name: kb_rsbycriteria_template source_template_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_rsbycriteria_template
    ADD CONSTRAINT source_template_fk FOREIGN KEY (sourcetemplate_id) REFERENCES kb_contenttemplate(id) ON DELETE CASCADE;


--
-- Name: memberclassification sourcemember_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY memberclassification
    ADD CONSTRAINT sourcemember_fk FOREIGN KEY (sourcemember_id) REFERENCES datasetmember(id) ON DELETE RESTRICT;


--
-- Name: contentstat stat_content_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY contentstat
    ADD CONSTRAINT stat_content_fk FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE RESTRICT;


--
-- Name: kb_subscription subs_user_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_subscription
    ADD CONSTRAINT subs_user_fk FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;


--
-- Name: po_viewcontentrelation target_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_viewcontentrelation
    ADD CONSTRAINT target_id_fk FOREIGN KEY (target_id) REFERENCES content(id) ON DELETE CASCADE;


--
-- Name: memberclassification targetmember_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY memberclassification
    ADD CONSTRAINT targetmember_fk FOREIGN KEY (targetmember_id) REFERENCES datasetmember(id) ON DELETE RESTRICT;


--
-- Name: kb_contentresourcegroup template_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_contentresourcegroup
    ADD CONSTRAINT template_fk FOREIGN KEY (template_id) REFERENCES kb_contenttemplate(id) ON DELETE RESTRICT;


--
-- Name: kb_relation_target template_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_relation_target
    ADD CONSTRAINT template_fk FOREIGN KEY (targettemplate_id) REFERENCES kb_contenttemplate(id) ON DELETE CASCADE;


--
-- Name: kb_tree_idoc tree_content_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_tree_idoc
    ADD CONSTRAINT tree_content_fk FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE CASCADE;


--
-- Name: kb_tree_idoc tree_file_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_tree_idoc
    ADD CONSTRAINT tree_file_fk FOREIGN KEY (tree_file_id) REFERENCES kb_tree_file(id) ON DELETE RESTRICT;


--
-- Name: idoc tree_file_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY idoc
    ADD CONSTRAINT tree_file_fk FOREIGN KEY (tree_file_id) REFERENCES kb_tree_file(id) ON DELETE RESTRICT;


--
-- Name: kb_tree_file tree_idoc_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_tree_file
    ADD CONSTRAINT tree_idoc_id_fk FOREIGN KEY (tree_idoc_id) REFERENCES kb_tree_idoc(content_id) ON DELETE CASCADE;


--
-- Name: kb_tree_resource treeresource_tree_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_tree_resource
    ADD CONSTRAINT treeresource_tree_fk FOREIGN KEY (treefile_id) REFERENCES kb_tree_file(id);


--
-- Name: kb_user_note un_domain_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_user_note
    ADD CONSTRAINT un_domain_fk FOREIGN KEY (domain_id) REFERENCES domain(id) ON DELETE CASCADE;


--
-- Name: kb_usage_stat usage_stat_domain_id; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_usage_stat
    ADD CONSTRAINT usage_stat_domain_id FOREIGN KEY (domain_id) REFERENCES domain(id) ON DELETE CASCADE;


--
-- Name: users user_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY users
    ADD CONSTRAINT user_fk FOREIGN KEY (lastmodifieduser) REFERENCES users(id) ON DELETE RESTRICT;


--
-- Name: profile user_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY profile
    ADD CONSTRAINT user_fk FOREIGN KEY (lastmodifieduser) REFERENCES users(id) ON DELETE RESTRICT;


--
-- Name: principal user_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY principal
    ADD CONSTRAINT user_fk FOREIGN KEY (lastmodifieduser) REFERENCES users(id) ON DELETE RESTRICT;


--
-- Name: kb_security_rule user_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_security_rule
    ADD CONSTRAINT user_fk FOREIGN KEY (lastmodifieduser) REFERENCES users(id) ON DELETE RESTRICT;


--
-- Name: kb_enotirule user_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_enotirule
    ADD CONSTRAINT user_fk FOREIGN KEY (lastmodifieduser) REFERENCES users(id) ON DELETE RESTRICT;


--
-- Name: userprofile user_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY userprofile
    ADD CONSTRAINT user_fk FOREIGN KEY (user_id) REFERENCES users(id);


--
-- Name: organization user_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY organization
    ADD CONSTRAINT user_fk FOREIGN KEY (lastmodifieduser) REFERENCES users(id) ON DELETE RESTRICT;


--
-- Name: kresource user_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kresource
    ADD CONSTRAINT user_fk FOREIGN KEY (lastmodifieduser) REFERENCES users(id) ON DELETE RESTRICT;


--
-- Name: content user_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY content
    ADD CONSTRAINT user_fk FOREIGN KEY (lastmodifieduser) REFERENCES users(id) ON DELETE RESTRICT;


--
-- Name: dataset user_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY dataset
    ADD CONSTRAINT user_fk FOREIGN KEY (lastmodifieduser) REFERENCES users(id) ON DELETE RESTRICT;


--
-- Name: datasetmember user_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY datasetmember
    ADD CONSTRAINT user_fk FOREIGN KEY (lastmodifieduser) REFERENCES users(id) ON DELETE RESTRICT;


--
-- Name: kb_classifier user_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_classifier
    ADD CONSTRAINT user_fk FOREIGN KEY (lastmodifieduser) REFERENCES users(id) ON DELETE RESTRICT;


--
-- Name: kb_notification user_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_notification
    ADD CONSTRAINT user_fk FOREIGN KEY (lastmodifieduser) REFERENCES users(id) ON DELETE RESTRICT;


--
-- Name: drb_question user_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY drb_question
    ADD CONSTRAINT user_fk FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT;


--
-- Name: wf_activity user_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY wf_activity
    ADD CONSTRAINT user_fk FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;


--
-- Name: kb_email_template user_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_email_template
    ADD CONSTRAINT user_fk FOREIGN KEY (lastmodifieduser) REFERENCES users(id) ON DELETE RESTRICT;


--
-- Name: kb_attribute user_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_attribute
    ADD CONSTRAINT user_fk FOREIGN KEY (lastmodifieduser) REFERENCES users(id) ON DELETE RESTRICT;


--
-- Name: po_portalobject user_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_portalobject
    ADD CONSTRAINT user_fk FOREIGN KEY (lastmodifieduser) REFERENCES users(id) ON DELETE RESTRICT;


--
-- Name: po_siteuser user_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_siteuser
    ADD CONSTRAINT user_fk FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT;


--
-- Name: po_siteuserrights user_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_siteuserrights
    ADD CONSTRAINT user_fk FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT;


--
-- Name: kb_user_property user_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_user_property
    ADD CONSTRAINT user_fk FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;


--
-- Name: userlabel user_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY userlabel
    ADD CONSTRAINT user_fk FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;


--
-- Name: kb_tree_file user_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_tree_file
    ADD CONSTRAINT user_fk FOREIGN KEY (lastmodifieduser) REFERENCES users(id) ON DELETE RESTRICT;


--
-- Name: kb_searcher_homeblock user_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_searcher_homeblock
    ADD CONSTRAINT user_fk FOREIGN KEY (lastmodifieduser) REFERENCES users(id) ON DELETE RESTRICT;


--
-- Name: kb_vote user_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_vote
    ADD CONSTRAINT user_id_fk FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;


--
-- Name: kb_report user_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_report
    ADD CONSTRAINT user_id_fk FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;


--
-- Name: kb_preference user_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_preference
    ADD CONSTRAINT user_id_fk FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;


--
-- Name: kb_user_note user_note_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_user_note
    ADD CONSTRAINT user_note_id_fk FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;


--
-- Name: kb_user_note user_note_lmu_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_user_note
    ADD CONSTRAINT user_note_lmu_id_fk FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL;


--
-- Name: kb_user_role user_role_user_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_user_role
    ADD CONSTRAINT user_role_user_id_fk FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;


--
-- Name: savedquery userprofile_id; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY savedquery
    ADD CONSTRAINT userprofile_id FOREIGN KEY (userprofile_id) REFERENCES userprofile(id) ON DELETE CASCADE;


--
-- Name: kb_user_role userrole_entity_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_user_role
    ADD CONSTRAINT userrole_entity_fk FOREIGN KEY (entity_id) REFERENCES datasetmember(id);


--
-- Name: kb_user_role userrole_profile_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_user_role
    ADD CONSTRAINT userrole_profile_fk FOREIGN KEY (userprofile_id) REFERENCES userprofile(id);


--
-- Name: kb_user_role userrole_role_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_user_role
    ADD CONSTRAINT userrole_role_fk FOREIGN KEY (role_id) REFERENCES kb_role(id);


--
-- Name: kresource version_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kresource
    ADD CONSTRAINT version_fk FOREIGN KEY (prev_version) REFERENCES kresource(id) ON DELETE RESTRICT;


--
-- Name: po_portalobject version_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_portalobject
    ADD CONSTRAINT version_fk FOREIGN KEY (prev_version) REFERENCES po_portalobject(id) ON DELETE RESTRICT;


--
-- Name: po_viewcontentrelation view_content_relation_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_viewcontentrelation
    ADD CONSTRAINT view_content_relation_fk FOREIGN KEY (view_id) REFERENCES po_portalobject(id) ON DELETE CASCADE;


--
-- Name: po_viewbk viewbk_block_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_viewbk
    ADD CONSTRAINT viewbk_block_fk FOREIGN KEY (block_id) REFERENCES po_diagrammable_block(po_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: po_viewbk viewbk_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_viewbk
    ADD CONSTRAINT viewbk_fk FOREIGN KEY (po_id) REFERENCES po_portalobject(id) ON DELETE CASCADE;


--
-- Name: po_viewbk viewbk_image_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_viewbk
    ADD CONSTRAINT viewbk_image_fk FOREIGN KEY (image_id) REFERENCES kresource(id) ON DELETE RESTRICT;


--
-- Name: po_viewbkblock viewbkblock_site_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_viewbkblock
    ADD CONSTRAINT viewbkblock_site_fk FOREIGN KEY (block_id) REFERENCES po_diagrammable_block(po_id) ON DELETE SET NULL;


--
-- Name: po_viewbkblock viewbkblock_view_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_viewbkblock
    ADD CONSTRAINT viewbkblock_view_fk FOREIGN KEY (view_id) REFERENCES po_viewbk(po_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: po_viewbkcontent viewbkcontent_content_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_viewbkcontent
    ADD CONSTRAINT viewbkcontent_content_fk FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE SET NULL;


--
-- Name: po_viewbkcontent viewbkcontent_view_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_viewbkcontent
    ADD CONSTRAINT viewbkcontent_view_fk FOREIGN KEY (view_id) REFERENCES po_viewbk(po_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: po_viewbklink viewbklink_view_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_viewbklink
    ADD CONSTRAINT viewbklink_view_fk FOREIGN KEY (view_id) REFERENCES po_viewbk(po_id) ON DELETE CASCADE;


--
-- Name: po_viewbksite viewbksite_site_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_viewbksite
    ADD CONSTRAINT viewbksite_site_fk FOREIGN KEY (site_id) REFERENCES po_diagrammable_site(po_id) ON DELETE SET NULL;


--
-- Name: po_viewbksite viewbksite_view_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_viewbksite
    ADD CONSTRAINT viewbksite_view_fk FOREIGN KEY (view_id) REFERENCES po_viewbk(po_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: kb_vote vote_content_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_vote
    ADD CONSTRAINT vote_content_fk FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE CASCADE;


--
-- Name: wf_procedure wf_procedure_diagram_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY wf_procedure
    ADD CONSTRAINT wf_procedure_diagram_fkey FOREIGN KEY (diagram) REFERENCES kfile(resource_id) ON DELETE SET NULL;


--
-- Name: wf_activity wfactivity_content_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY wf_activity
    ADD CONSTRAINT wfactivity_content_fk FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE CASCADE;


--
-- Name: kb_work_note wn_domain_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_work_note
    ADD CONSTRAINT wn_domain_fk FOREIGN KEY (domain_id) REFERENCES domain(id) ON DELETE CASCADE;


--
-- Name: kb_notification wn_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_notification
    ADD CONSTRAINT wn_fk FOREIGN KEY (work_note_id) REFERENCES kb_work_note(id) ON DELETE CASCADE;


--
-- Name: kb_work_note wn_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_work_note
    ADD CONSTRAINT wn_id_fk FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;


--
-- Name: kb_work_note wn_lmu_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_work_note
    ADD CONSTRAINT wn_lmu_id_fk FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL;


--
-- Name: kb_work_note_user_read wnur_uid_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_work_note_user_read
    ADD CONSTRAINT wnur_uid_fk FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;


--
-- Name: kb_work_note_user_read wnur_wnid_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_work_note_user_read
    ADD CONSTRAINT wnur_wnid_fk FOREIGN KEY (work_note_id) REFERENCES kb_work_note(id) ON DELETE CASCADE;


--
-- Name: content workspace_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY content
    ADD CONSTRAINT workspace_fk FOREIGN KEY (workspace) REFERENCES users(id) ON DELETE RESTRICT;


--
-- PostgreSQL database dump complete
--

