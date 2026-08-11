--
-- PostgreSQL database dump
--

-- Dumped from database version 9.6.2
-- Dumped by pg_dump version 9.6.2

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


SET search_path = public, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

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
    datevalue date,
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
    INCREMENT BY 1
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
    attributes character varying(2048)
);


ALTER TABLE content OWNER TO kbee;

--
-- Name: contentclass; Type: TABLE; Schema: public; Owner: kbee
--

CREATE TABLE contentclass (
    id character varying(64) NOT NULL,
    enabled boolean DEFAULT true,
    indexable boolean DEFAULT true,
    name character varying(128),
    javaclass character varying(128)
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
-- Name: contentrelation; Type: TABLE; Schema: public; Owner: kbee
--

CREATE TABLE contentrelation (
    source_id bigint NOT NULL,
    target_id bigint NOT NULL,
    "position" integer
);


ALTER TABLE contentrelation OWNER TO kbee;

--
-- Name: contentresource; Type: TABLE; Schema: public; Owner: kbee
--

CREATE TABLE contentresource (
    content_id bigint NOT NULL,
    resource_id bigint NOT NULL,
    "position" integer
);


ALTER TABLE contentresource OWNER TO kbee;

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
    abbreviation character(18)
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
    datevalue date,
    parent bigint,
    dataset_id bigint NOT NULL,
    external_id bigint,
    attributes character varying(2048)
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
    description character varying(512),
    quota integer DEFAULT 0,
    file_reader_directory character varying(4096),
    password_renew_months integer DEFAULT 0,
    istemplate boolean DEFAULT false,
    maxusers integer DEFAULT 0,
    tipoftheday boolean DEFAULT true
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
    date_accepted date,
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
    url character varying(512) NOT NULL
);


ALTER TABLE entitymatching OWNER TO kbee;

--
-- Name: externalresource; Type: TABLE; Schema: public; Owner: kbee
--

CREATE TABLE externalresource (
    resource_id bigint NOT NULL,
    url character varying(2048),
    description character varying(1024)
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
    gdate date
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
    template_id bigint
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
    permissions character varying(128) NOT NULL,
    negative boolean NOT NULL
);


ALTER TABLE kb_aclentry OWNER TO kbee;

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
    visibility text
);


ALTER TABLE kb_attribute OWNER TO postgres;

--
-- Name: kb_attributetemplate; Type: TABLE; Schema: public; Owner: kbee
--

CREATE TABLE kb_attributetemplate (
    id bigint NOT NULL,
    attribute_id bigint,
    metadatasubtitle boolean DEFAULT false
);


ALTER TABLE kb_attributetemplate OWNER TO kbee;

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
    multiplicity integer DEFAULT 4,
    is_content_type boolean DEFAULT false,
    mandatory boolean DEFAULT false,
    ordered boolean DEFAULT false,
    korder integer DEFAULT 1,
    visibility text,
    dataset_id bigint,
    dataset2_id bigint,
    dataset3_id bigint,
    ismetadatasubtitle boolean DEFAULT true,
    metadatasubtitle boolean DEFAULT true
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
    ismetadatasubtitle boolean DEFAULT false,
    metadatasubtitle boolean DEFAULT false
);


ALTER TABLE kb_classifiertemplate OWNER TO kbee;

--
-- Name: kb_comment; Type: TABLE; Schema: public; Owner: kbee
--

CREATE TABLE kb_comment (
    content_id bigint NOT NULL,
    referenced_content_id bigint NOT NULL,
    commentdate date,
    title character varying(512),
    text text,
    user_id bigint NOT NULL,
    date_submitted timestamp with time zone DEFAULT now(),
    creationdate timestamp with time zone DEFAULT now()
);


ALTER TABLE kb_comment OWNER TO kbee;

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
    istemplate boolean DEFAULT false,
    isvideo boolean DEFAULT false,
    hasdetailpage boolean DEFAULT true,
    relations boolean DEFAULT false,
    abstract boolean DEFAULT true,
    acl bigint,
    title_rule character varying(256),
    isdefault boolean DEFAULT false,
    isaudio boolean DEFAULT false,
    istext boolean DEFAULT false,
    isdocument boolean DEFAULT false,
    isphoto boolean DEFAULT false,
    istool boolean DEFAULT false,
    isactivity boolean DEFAULT false,
    linkresources boolean DEFAULT true
);


ALTER TABLE kb_contenttemplate OWNER TO kbee;

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
-- Name: kb_domain_settings; Type: TABLE; Schema: public; Owner: kbee
--

CREATE TABLE kb_domain_settings (
    domain_id bigint NOT NULL,
    category character varying(64) NOT NULL,
    values_json text,
    lastmodifieddate timestamp with time zone DEFAULT now()
);


ALTER TABLE kb_domain_settings OWNER TO kbee;

--
-- Name: kb_email_template; Type: TABLE; Schema: public; Owner: kbee
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
    subject character varying(512),
    strtext text,
    fromstr character varying(512)
);


ALTER TABLE kb_email_template OWNER TO kbee;

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
    creationdate timestamp with time zone DEFAULT now()
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
    contentid character varying(42),
    sender_id bigint NOT NULL,
    receiver_id bigint NOT NULL,
    datesend timestamp with time zone DEFAULT now(),
    dateread date,
    type integer DEFAULT 1,
    notification_state integer DEFAULT 1
);


ALTER TABLE kb_notification OWNER TO kbee;

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
    event_type character varying(32) NOT NULL,
    event_time timestamp with time zone DEFAULT now(),
    event_user bigint,
    event_domain_id bigint,
    event_object_id character varying(32),
    event_generator_action character varying(64),
    email_from character varying(128),
    email_to character varying(128),
    email_subject character varying(256),
    email_text text,
    email_attachments text,
    event_result character varying(64)
);


ALTER TABLE kb_sendemailevent OWNER TO kbee;

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
-- Name: kb_tip; Type: TABLE; Schema: public; Owner: kbee
--

CREATE TABLE kb_tip (
    id bigint NOT NULL,
    domain_id bigint,
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
-- Name: kb_usage_stat; Type: TABLE; Schema: public; Owner: kbee
--

CREATE TABLE kb_usage_stat (
    domain_id bigint NOT NULL,
    ts timestamp with time zone DEFAULT now() NOT NULL,
    hard_disk_usage bigint,
    users bigint,
    contents bigint,
    resources bigint,
    attributes text
);


ALTER TABLE kb_usage_stat OWNER TO kbee;

--
-- Name: kb_vote; Type: TABLE; Schema: public; Owner: kbee
--

CREATE TABLE kb_vote (
    user_id bigint NOT NULL,
    content_id bigint NOT NULL,
    vote integer,
    votedate timestamp with time zone DEFAULT now()
);


ALTER TABLE kb_vote OWNER TO kbee;

--
-- Name: kfile; Type: TABLE; Schema: public; Owner: kbee
--

CREATE TABLE kfile (
    resource_id bigint NOT NULL,
    path character varying(512),
    file_type character(5),
    title character varying(256),
    subtitle character varying(256),
    description character varying(2048),
    thumbnailsmall character varying(128),
    thumbnaillarge character varying(128),
    width bigint DEFAULT 0,
    height bigint DEFAULT 0,
    crc32str character(8)
);


ALTER TABLE kfile OWNER TO kbee;

--
-- Name: kgroup; Type: TABLE; Schema: public; Owner: kbee
--

CREATE TABLE kgroup (
    id bigint NOT NULL,
    name character varying(120) NOT NULL,
    description character varying(256),
    canonical boolean
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
    ksize bigint
);


ALTER TABLE kresource OWNER TO kbee;

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
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE log_sequence OWNER TO kbee;

--
-- Name: logevent; Type: TABLE; Schema: public; Owner: kbee
--

CREATE TABLE logevent (
    event_id bigint NOT NULL,
    event_type character varying(32) NOT NULL,
    event_object_id character varying(32),
    event_content_id character varying(32),
    event_version integer,
    event_time timestamp with time zone DEFAULT now(),
    event_user bigint,
    event_user_to bigint,
    event_kbeeclass character varying(64),
    event_task character varying(128),
    event_procedure character varying(64),
    event_parameters text,
    event_domain_id bigint,
    event_title character varying(256)
);


ALTER TABLE logevent OWNER TO kbee;

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
    contentdate date,
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
    photo bigint
);


ALTER TABLE person OWNER TO kbee;

--
-- Name: portalid_sequence; Type: SEQUENCE; Schema: public; Owner: kbee
--

CREATE SEQUENCE portalid_sequence
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


ALTER TABLE portalid_sequence OWNER TO kbee;

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
    value text
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
-- Name: report; Type: TABLE; Schema: public; Owner: kbee
--

CREATE TABLE report (
    user_id bigint NOT NULL,
    content_id bigint NOT NULL,
    report integer,
    reportdate timestamp with time zone DEFAULT now()
);


ALTER TABLE report OWNER TO kbee;

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
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE resourceid_sequence OWNER TO kbee;

--
-- Name: savedquery; Type: TABLE; Schema: public; Owner: kbee
--

CREATE TABLE savedquery (
    id bigint NOT NULL,
    userprofile_id bigint,
    title character varying(512),
    statement character varying(512),
    "position" integer,
    console character varying(24)
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
    title character varying(64),
    error_count integer,
    description character varying(512),
    error_message character varying(512)
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
-- Name: securityrule; Type: TABLE; Schema: public; Owner: kbee
--

CREATE TABLE securityrule (
    id bigint NOT NULL,
    lastmodifieddate timestamp with time zone DEFAULT now(),
    lastmodifieduser bigint,
    domain_id bigint,
    name character varying(150),
    type integer DEFAULT 1,
    condition character varying(4096),
    description character varying(4096),
    related_object_id character varying(48),
    acl bigint NOT NULL,
    creationdate timestamp with time zone DEFAULT now()
);


ALTER TABLE securityrule OWNER TO kbee;

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
-- Name: userlabel; Type: TABLE; Schema: public; Owner: kbee
--

CREATE TABLE userlabel (
    id bigint NOT NULL,
    user_id bigint,
    scope integer DEFAULT 1,
    label character varying(128),
    css character(24),
    short_label character(8),
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
    editperson boolean DEFAULT true
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
    password character varying(48) DEFAULT 'root'::character varying,
    password_md5 bytea,
    seed bytea,
    firstname character varying(120),
    lastname character varying(120),
    email character varying(256),
    locale_str character(6) DEFAULT 'eng'::bpchar,
    enabled boolean DEFAULT true,
    canonical boolean DEFAULT false,
    active boolean DEFAULT true,
    creationdate timestamp with time zone DEFAULT now(),
    timezone character varying(128) DEFAULT 'US/Central'::character varying
);


ALTER TABLE users OWNER TO kbee;

--
-- Name: wf_activity; Type: TABLE; Schema: public; Owner: kbee
--

CREATE TABLE wf_activity (
    id bigint NOT NULL,
    process_id bigint NOT NULL,
    procedure character varying(128),
    task character varying(128),
    user_id bigint NOT NULL,
    content_id bigint NOT NULL,
    startime timestamp with time zone,
    endtime timestamp with time zone,
    note text,
    resolution text,
    status character varying(20),
    assigned_by bigint,
    event character varying(128)
);


ALTER TABLE wf_activity OWNER TO kbee;

--
-- Name: wf_launcher; Type: TABLE; Schema: public; Owner: kbee
--

CREATE TABLE wf_launcher (
    id bigint NOT NULL,
    label character varying(128),
    contenttemplate_id bigint,
    procedure_id bigint,
    contextual boolean,
    domain_id bigint,
    acl bigint,
    isenabled boolean DEFAULT true
);


ALTER TABLE wf_launcher OWNER TO kbee;

--
-- Name: wf_procedure; Type: TABLE; Schema: public; Owner: kbee
--

CREATE TABLE wf_procedure (
    id bigint NOT NULL,
    lastmodifieddate timestamp with time zone DEFAULT now(),
    lastmodifieduser bigint NOT NULL,
    domain_id bigint NOT NULL,
    name character varying(128),
    tasks text,
    states character varying(128),
    state integer,
    creationdate timestamp with time zone DEFAULT now(),
    alias character varying(64),
    initial_rules text
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
    INCREMENT BY 1
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
    ADD CONSTRAINT aclentry_pkey PRIMARY KEY (acl, principal, negative);


--
-- Name: drb_answer answer_pkey; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY drb_answer
    ADD CONSTRAINT answer_pkey PRIMARY KEY (content_id);


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
-- Name: kb_comment comment_pkey; Type: CONSTRAINT; Schema: public; Owner: kbee
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
-- Name: contentresource contentresource_pkey; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY contentresource
    ADD CONSTRAINT contentresource_pkey PRIMARY KEY (content_id, resource_id);


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
-- Name: kb_email_template dlk; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_email_template
    ADD CONSTRAINT dlk UNIQUE (domain_id, lang, xkey);


--
-- Name: kb_domain_settings ds_pkey; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_domain_settings
    ADD CONSTRAINT ds_pkey PRIMARY KEY (domain_id, category);


--
-- Name: kb_email_template email_template_pkey; Type: CONSTRAINT; Schema: public; Owner: kbee
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
-- Name: kfile file_pkey; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kfile
    ADD CONSTRAINT file_pkey PRIMARY KEY (resource_id);


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
-- Name: kresource kresource_prev_version_key; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kresource
    ADD CONSTRAINT kresource_prev_version_key UNIQUE (prev_version);


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
-- Name: kb_notification notification_pkey; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_notification
    ADD CONSTRAINT notification_pkey PRIMARY KEY (id);


--
-- Name: organization organization_pkey; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY organization
    ADD CONSTRAINT organization_pkey PRIMARY KEY (id);


--
-- Name: kb_securitydata organizationaldata_pkey; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_securitydata
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
-- Name: person person_pk; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY person
    ADD CONSTRAINT person_pk PRIMARY KEY (entity_id);


--
-- Name: kb_preference preference_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_preference
    ADD CONSTRAINT preference_pkey PRIMARY KEY (id);


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
-- Name: contentrelation relation_pkey; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY contentrelation
    ADD CONSTRAINT relation_pkey PRIMARY KEY (source_id, target_id);


--
-- Name: report report_pkey; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY report
    ADD CONSTRAINT report_pkey PRIMARY KEY (user_id, content_id);


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
-- Name: securityrule securityrule_pkey; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY securityrule
    ADD CONSTRAINT securityrule_pkey PRIMARY KEY (id);


--
-- Name: kb_sendemailevent sendemailevent_pkey; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_sendemailevent
    ADD CONSTRAINT sendemailevent_pkey PRIMARY KEY (event_id);


--
-- Name: kb_subscription subscription_pkey; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_subscription
    ADD CONSTRAINT subscription_pkey PRIMARY KEY (user_id, content_oid, event_id);


--
-- Name: kb_tip tip_pkey; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_tip
    ADD CONSTRAINT tip_pkey PRIMARY KEY (id);


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
-- Name: kb_vote vote_pkey; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_vote
    ADD CONSTRAINT vote_pkey PRIMARY KEY (user_id, content_id);


--
-- Name: wf_launcher wf_launcher_pkey; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY wf_launcher
    ADD CONSTRAINT wf_launcher_pkey PRIMARY KEY (id);


--
-- Name: wf_activity workflowactivity_pkey; Type: CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY wf_activity
    ADD CONSTRAINT workflowactivity_pkey PRIMARY KEY (id);


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
-- Name: kb_email_template_domain_idx; Type: INDEX; Schema: public; Owner: kbee
--

CREATE INDEX kb_email_template_domain_idx ON kb_email_template USING btree (xkey, lang, domain_id);


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

CREATE INDEX kb_tip_id_idx ON kb_tip USING btree (tip_lang, lower((tip_title)::text));


--
-- Name: kb_usage_domain_idx; Type: INDEX; Schema: public; Owner: kbee
--

CREATE INDEX kb_usage_domain_idx ON kb_usage_stat USING btree (domain_id, ts);


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
-- Name: logevent_event_content_id_idx; Type: INDEX; Schema: public; Owner: kbee
--

CREATE INDEX logevent_event_content_id_idx ON logevent USING btree (event_content_id);


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
-- Name: property_content_id_idx; Type: INDEX; Schema: public; Owner: kbee
--

CREATE INDEX property_content_id_idx ON property USING btree (content_id);


--
-- Name: report_content_id_user_id_idx; Type: INDEX; Schema: public; Owner: kbee
--

CREATE INDEX report_content_id_user_id_idx ON report USING btree (content_id, user_id);


--
-- Name: report_user_id_content_id_idx; Type: INDEX; Schema: public; Owner: kbee
--

CREATE INDEX report_user_id_content_id_idx ON report USING btree (user_id, content_id);


--
-- Name: scheduler_id_idx; Type: INDEX; Schema: public; Owner: kbee
--

CREATE INDEX scheduler_id_idx ON scheduler USING btree (id);


--
-- Name: scheduler_priority_time_idx; Type: INDEX; Schema: public; Owner: kbee
--

CREATE INDEX scheduler_priority_time_idx ON scheduler USING btree (priority, "time");


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
-- Name: kb_aclentry acl_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_aclentry
    ADD CONSTRAINT acl_fk FOREIGN KEY (acl) REFERENCES kb_acl(id) ON DELETE CASCADE;


--
-- Name: securityrule acl_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY securityrule
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
-- Name: property content_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY property
    ADD CONSTRAINT content_fk FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE CASCADE;


--
-- Name: organizationaltext content_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY organizationaltext
    ADD CONSTRAINT content_fk FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE RESTRICT;


--
-- Name: contentresource content_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY contentresource
    ADD CONSTRAINT content_fk FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE RESTRICT;


--
-- Name: classification content_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY classification
    ADD CONSTRAINT content_fk FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE RESTRICT;


--
-- Name: idoc content_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY idoc
    ADD CONSTRAINT content_fk FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE RESTRICT;


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
-- Name: kb_comment content_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_comment
    ADD CONSTRAINT content_fk FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE CASCADE;


--
-- Name: orgchart content_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY orgchart
    ADD CONSTRAINT content_fk FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE RESTRICT;


--
-- Name: wf_activity content_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY wf_activity
    ADD CONSTRAINT content_fk FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE CASCADE;


--
-- Name: kb_vote content_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_vote
    ADD CONSTRAINT content_id_fk FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE CASCADE;


--
-- Name: report content_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY report
    ADD CONSTRAINT content_id_fk FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE CASCADE;


--
-- Name: contentstat content_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY contentstat
    ADD CONSTRAINT content_id_fk FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE RESTRICT;


--
-- Name: contentproperties content_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY contentproperties
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
-- Name: securityrule domain_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY securityrule
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
-- Name: kb_email_template domain_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_email_template
    ADD CONSTRAINT domain_fk FOREIGN KEY (domain_id) REFERENCES domain(id) ON DELETE CASCADE;


--
-- Name: kb_attribute domain_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_attribute
    ADD CONSTRAINT domain_fk FOREIGN KEY (domain_id) REFERENCES domain(id) ON DELETE CASCADE;


--
-- Name: kb_domain_settings ds_domain_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_domain_settings
    ADD CONSTRAINT ds_domain_fk FOREIGN KEY (domain_id) REFERENCES domain(id) ON DELETE CASCADE;


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
-- Name: authorities fk_authorities_users; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY authorities
    ADD CONSTRAINT fk_authorities_users FOREIGN KEY (username) REFERENCES users(username);


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
-- Name: kgroupmember group_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kgroupmember
    ADD CONSTRAINT group_fk FOREIGN KEY (kgroup) REFERENCES kgroup(id) ON DELETE CASCADE;


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
-- Name: kb_securitydata group_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_securitydata
    ADD CONSTRAINT group_fk FOREIGN KEY (group_id) REFERENCES kgroup(id) ON DELETE CASCADE;


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
-- Name: kb_classifiertemplate member_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_classifiertemplate
    ADD CONSTRAINT member_fk FOREIGN KEY (root_id) REFERENCES datasetmember(id) ON DELETE RESTRICT;


--
-- Name: datasetmember parent_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY datasetmember
    ADD CONSTRAINT parent_fk FOREIGN KEY (parent) REFERENCES datasetmember(id) ON DELETE RESTRICT;


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
-- Name: kb_enotirule_principal principal_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_enotirule_principal
    ADD CONSTRAINT principal_fk FOREIGN KEY (principal_id) REFERENCES principal(id) ON DELETE CASCADE;


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
-- Name: drb_answer question_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY drb_answer
    ADD CONSTRAINT question_fk FOREIGN KEY (question_id) REFERENCES drb_question(content_id) ON DELETE CASCADE;


--
-- Name: kb_notification receiver_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_notification
    ADD CONSTRAINT receiver_fk FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE CASCADE;


--
-- Name: kb_comment referenced_content_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_comment
    ADD CONSTRAINT referenced_content_id_fk FOREIGN KEY (referenced_content_id) REFERENCES content(id) ON DELETE CASCADE;


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
-- Name: kb_enotirule_principal rule_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_enotirule_principal
    ADD CONSTRAINT rule_fk FOREIGN KEY (rule_id) REFERENCES kb_enotirule(id) ON DELETE CASCADE;


--
-- Name: kb_securitydata rule_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_securitydata
    ADD CONSTRAINT rule_fk FOREIGN KEY (securityrule_id) REFERENCES securityrule(id) ON DELETE CASCADE;


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
-- Name: contentrelation source_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY contentrelation
    ADD CONSTRAINT source_id_fk FOREIGN KEY (source_id) REFERENCES content(id) ON DELETE CASCADE;


--
-- Name: memberclassification sourcemember_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY memberclassification
    ADD CONSTRAINT sourcemember_fk FOREIGN KEY (sourcemember_id) REFERENCES datasetmember(id) ON DELETE RESTRICT;


--
-- Name: kb_subscription subs_user_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_subscription
    ADD CONSTRAINT subs_user_fk FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;


--
-- Name: contentrelation target_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY contentrelation
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
-- Name: userlabel user_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY userlabel
    ADD CONSTRAINT user_fk FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;


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
-- Name: securityrule user_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY securityrule
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
-- Name: kb_email_template user_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_email_template
    ADD CONSTRAINT user_fk FOREIGN KEY (lastmodifieduser) REFERENCES users(id) ON DELETE RESTRICT;


--
-- Name: kb_attribute user_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_attribute
    ADD CONSTRAINT user_fk FOREIGN KEY (lastmodifieduser) REFERENCES users(id) ON DELETE RESTRICT;


--
-- Name: kb_vote user_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kb_vote
    ADD CONSTRAINT user_id_fk FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;


--
-- Name: report user_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY report
    ADD CONSTRAINT user_id_fk FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;


--
-- Name: kb_preference user_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY kb_preference
    ADD CONSTRAINT user_id_fk FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;


--
-- Name: savedquery userprofile_id; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY savedquery
    ADD CONSTRAINT userprofile_id FOREIGN KEY (userprofile_id) REFERENCES userprofile(id) ON DELETE CASCADE;


--
-- Name: kresource version_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY kresource
    ADD CONSTRAINT version_fk FOREIGN KEY (prev_version) REFERENCES kresource(id) ON DELETE RESTRICT;


--
-- Name: content workspace_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY content
    ADD CONSTRAINT workspace_fk FOREIGN KEY (workspace) REFERENCES users(id) ON DELETE RESTRICT;


--
-- PostgreSQL database dump complete
------------------------------------------------------------------------------------------------------------------------------------------------


INSERT INTO ContentClass(id, enabled, name, javaclass) 				VALUES('KbeeIDoc',                TRUE, 'IDoc',                'com.novamens.kbee.content.document.KbeeIDoc');
INSERT INTO ContentClass(id, enabled, name, javaclass) 				VALUES('KbeeOrganizationalText',  TRUE, 'OrganizationalText',  'com.novamens.kbee.content.communication.KbeeOrganizationalText');
INSERT INTO ContentClass(id, enabled, name, javaclass) 				VALUES('KbeeOrgChart',            TRUE, 'OrgChart',            'com.novamens.kbee.content.orgchart.KbeeOrgChart');
INSERT INTO ContentClass(id, enabled, name, javaclass) 				VALUES('KbeeQuestion',            TRUE, 'Question',            'com.novamens.kbee.content.questionanswer.KbeeQuestion');
INSERT INTO ContentClass(id, enabled, name, javaclass, indexable)   VALUES('KbeeContent',  			  TRUE, 'Content',             'com.novamens.kbee.content.base.KbeeContent', false);
INSERT INTO ContentClass(id, enabled, name, javaclass, indexable)   VALUES('KbeeAnswer',   			  TRUE, 'Answer',              'com.novamens.kbee.content.questionanswer.KbeeAnswer', false);
INSERT INTO ContentClass(id, enabled, name, javaclass, indexable)   VALUES('KbeeComment',  			  TRUE, 'Comment',             'com.novamens.kbee.content.social.KbeeComment', false);


--- Root user
INSERT INTO users(id, username, state,  firstname, lastname, password, password_md5, lastModifiedUser) VALUES(1, 'root@kbee', 1, '' , 'root', '3b6144f35f3e2f80a1f9446fafc389dd', 'root', null);
INSERT INTO domain(id, lastmodifieduser, state, enabled, name, type, service, quota, istemplate, description, organization) VALUES(1, 1, 1, TRUE, 'kbee', 4, 1, 0, FALSE, 'RPDM Factory', 'RealPage');
INSERT INTO kb_domain_settings(domain_id, category, values_json) VALUES(1, 'kbee', '{"emailServiceNoReply":"noreply@realpage.com","emailServiceStatus":"enabled"}');

--- Root user
INSERT INTO principal(id, lastmodifieduser, domain_id) VALUES(1, 1, 1);

-- Groups (2-19)
INSERT INTO principal(id, lastmodifieduser, domain_id) VALUES(2, 1, 1);
INSERT INTO kgroup(id, name, canonical) VALUES(2, 'User', TRUE);

INSERT INTO principal(id, lastmodifieduser, domain_id) VALUES(3, 1, 1);
INSERT INTO kgroup(id, name, canonical) VALUES(3, 'ROLE_ROOT', TRUE);

INSERT INTO principal(id, lastmodifieduser, domain_id) VALUES(4, 1, 1);
INSERT INTO kgroup(id, name, canonical) VALUES(4, 'Domain Admin', TRUE);

INSERT INTO principal(id, lastmodifieduser, domain_id) VALUES(5, 1, 1);
INSERT INTO kgroup(id, name, canonical) VALUES(5, 'Workspace', TRUE);

INSERT INTO principal(id, lastmodifieduser, domain_id) VALUES(6, 1, 1);
INSERT INTO kgroup(id, name, canonical) VALUES(6, 'Content Base', TRUE);

INSERT INTO principal(id, lastmodifieduser, domain_id) VALUES(7, 1, 1);
INSERT INTO kgroup(id, name, canonical) VALUES(7, 'Archive', TRUE);

INSERT INTO principal(id, lastmodifieduser, domain_id) VALUES(8, 1, 1);
INSERT INTO kgroup(id, name, canonical) VALUES(8, 'Security', TRUE);

INSERT INTO principal(id, lastmodifieduser, domain_id) VALUES(9, 1, 1);
INSERT INTO kgroup(id, name, canonical) VALUES(9, 'Monitor', TRUE);

INSERT INTO principal(id, lastmodifieduser, domain_id) VALUES(10, 1, 1);
INSERT INTO kgroup(id, name, canonical) VALUES(10, 'Workflow', TRUE);

INSERT INTO principal(id, lastmodifieduser, domain_id) VALUES(11, 1, 1);
INSERT INTO kgroup(id, name, canonical) VALUES(11, 'Templates', TRUE);

INSERT INTO principal(id, lastmodifieduser, domain_id) VALUES(12, 1, 1);
INSERT INTO kgroup(id, name, canonical) VALUES(12, 'Information Model', TRUE);


-- Root user (1)
INSERT INTO entity(id, lastmodifieduser, state, domain_id) VALUES(1, 1, 1, 1);
INSERT INTO person(entity_id, lastname, phone, email) VALUES(1, 'RPDM Admin', '1234', 'info@novamens.com');
INSERT INTO profile(id, lastmodifieduser, entity, domain_id) VALUES(1, 1, 1, 1);
INSERT INTO userprofile(id, user_id, confidencelevel) VALUES(1, 1, 99);

INSERT INTO kgroupmember(kgroup, principal) VALUES(2, 1);
INSERT INTO kgroupmember(kgroup, principal) VALUES(3, 1);
INSERT INTO kgroupmember(kgroup, principal) VALUES(4, 1);

-- Workflow user (20)
INSERT INTO users(id, username, state,  firstname, lastname, password, password_md5, lastModifiedUser) VALUES(20, 'workflow@kbee', 1, '' , 'pending', '3b6144f35f3e2f80a1f9446fafc389dd', 'root', 1);
INSERT INTO principal(id, lastmodifieduser, domain_id) VALUES(20, 1, 1);
INSERT INTO entity(id, lastmodifieduser, state, domain_id) VALUES(20, 1, 1, 1);
INSERT INTO person(entity_id, lastname, phone, email) VALUES(20, 'Workflow', '1234', 'info@novamens.com');
INSERT INTO profile(id, lastmodifieduser, entity, domain_id) VALUES(20, 1, 20, 1);
INSERT INTO userprofile(id, user_id) VALUES(20, 20);
INSERT INTO kgroupmember(kgroup, principal) VALUES(10, 20);

-- ContentTemplate File
-- INSERT INTO kb_ContentTemplate(id, lastmodifieduser, state, domain_id, contentclass_id, name) VALUES(1, 1, 1, 1, 'KbeeIDoc', 'File');

-- DataSet Users 
INSERT INTO dataset       (id, domain_id, name, type, state, suggester, lastmodifieduser) values(1,1,'Users',4,1, false,1);

-- UserSet
INSERT INTO datasetmember (id, lastmodifieduser, state, dataset_id, domain_id, entity_id, type) values(1,1,1,1,1,1,3);
INSERT INTO datasetmember (id, lastmodifieduser, state, dataset_id, domain_id, entity_id,  type) values(2,1,1,1,1,20,3);


-- Email Templates-----------

delete from kb_email_template;

INSERT INTO kb_email_template (id, lastmodifieduser,  state, domain_id, lang, xkey, title, fromstr, subject, strtext) VALUES (1, 1, 1, 1, 'en', 'welcome', 'Welcome to RPDM', '${domain-noreply}' , 'Welcome to RealPage Document Management - ${domain-name}', '<p>${person-displayname},</p><p>Welcome to <b>RealPage Document Management (RP-DOC)</b>!. RP-DOC is an enterprise document management solution designed to meet the enterprise document management needs of any size property management company. RP-DOC allows you store documents for all areas of your business while having the ability to restrict access by user so you can give view only access to investors and auditors or control access by document type. Never lose a document again, RP-DOC superior search functionality will help you find in seconds any document ever stored.</p><p>Your username is: <b>${username}</b></p> <p>To set up your account password, please visit the link below and follow the instructions:</p><p>${url}</p>' );
INSERT INTO kb_email_template (id, lastmodifieduser,  state, domain_id, lang, xkey, title, fromstr, subject, strtext) VALUES (8, 1, 1, 1, 'en', 'forgot-username', 'Forgot Username','${domain-noreply}'  , 'Username for ${domain-name} - RPDM'  , '<p>${person-displayname},</p><p>We have received your request to send your RPDM username. <br/>We have the following user account associated to the email address and phone:</p><p>Email: ${person-email-address}<br/>Phone: ${person-phone-last-four-digits}</p><p>User Account: ${user-username}</p>');
INSERT INTO kb_email_template (id, lastmodifieduser,  state, domain_id, lang, xkey, title, fromstr, subject, strtext) VALUES (9, 1, 1, 1, 'en', 'admin-sends-reset-password', 'Password Reset', '${domain-noreply}' , 'Password Reset for ${domain-name} - RPDM', '${person-displayname},<br/><p>The Admin user has sent you this link to reset your account password. Please visit the link below and follow the instructions:</p><p>${url}</p> <p>For security reasons, this link will expire in 30 minutes after your initial request was made.</p>');
INSERT INTO kb_email_template (id, lastmodifieduser,  state, domain_id, lang, xkey, title, fromstr, subject, strtext) VALUES (7, 1, 1, 1, 'en', 'forgot-password',   'Password Reset'  , '${domain-noreply}', 'Password Reset for ${domain-name} - RPDM','<p>${person-displayname},</p><p>We have received your request to reset your account password. Please visit the link below and follow the instructions to reset your password:</p><p>${url}</p><p>For security reasons, this link will expire in 30 minutes after your initial request was made. If you did not request to reset your password, you can safely ignore this email.</p>');
INSERT INTO kb_email_template (id, lastmodifieduser,  state, domain_id, lang, xkey, title, fromstr, subject, strtext) VALUES (2, 1, 1, 1, 'en', 'send-email', 		 'Send by Email'   , '${from}',      '${title} - RPDM'  , '${text}');
INSERT INTO kb_email_template (id, lastmodifieduser,  state, domain_id, lang, xkey, title, fromstr, subject, strtext) VALUES (6, 1, 1, 1, 'en', 'alert-rule-publish',  'Alert Notification'  , '${domain-noreply}', '${event-name} - ${file-title} - RPDM'  , '<p>${person-display-name} has published: ${file-title}</p> <p>Please go to Library at ${url} to retrieve additional information about this file.</p>');

INSERT INTO kb_email_template (id, lastmodifieduser,  state, domain_id, lang, xkey, title, fromstr, subject, strtext) VALUES (3, 1, 1, 1, 'en', 'assign-task',  'New Task'  , '${from}'  , '${title} - ${task-name} - RPDM'  , '<p>${from-displayname} has assigned the following task:</p><p>Task: ${task-name} <br/> File: ${title}.</p><p>Please go to <a href="${url}">My Tasks</a> to retrieve additional information about this file.</p><p>Comment:<br/>${comment}</p>');

INSERT INTO kb_email_template (id, lastmodifieduser,  state, domain_id, lang, xkey, title, fromstr, subject, strtext) VALUES (4, 1, 1, 1, 'en', 'reassign-task-receiver',   'Reassign task receiver'          , '${from}'  , '${task-name} - ${title} - RPDM'  , '<p>{from-displayname} has assigned the following task: ${task} - ${title}.<br/>Please go to My Tasks at ${url} to retrieve additional information about this file.</p><p>Comment:<br/>${comment}</p>');
INSERT INTO kb_email_template (id, lastmodifieduser,  state, domain_id, lang, xkey, title, fromstr, subject, strtext) VALUES (5, 1, 1, 1, 'en', 'reassign-task-former-owner',  'Reassign task former owner'   , '${from}'  , '${task-name} - ${title} - RPDM'  , '<p>{from-displayname} has assigned the following task: ${task} - ${title}.<br/>Please go to My Tasks at ${url} to retrieve additional information about this file.</p><p>Comment:<br/>${comment}</p>');








