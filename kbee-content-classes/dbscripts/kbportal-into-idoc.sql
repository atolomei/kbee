CREATE SEQUENCE public.logsites_sequence  
    INCREMENT 1
    START 36749
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

ALTER SEQUENCE public.logsites_sequence
    OWNER TO postgres;
	
	
CREATE SEQUENCE public.portalid_sequence
    INCREMENT 1
    START 28473
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

ALTER SEQUENCE public.portalid_sequence
    OWNER TO postgres;
	
--
-- PostgreSQL database dump
--

-- Dumped from database version 9.6.1
-- Dumped by pg_dump version 9.6.1

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

SET search_path = public, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: po_area; Type: TABLE; Schema: public; Owner: postgres
--

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

--
-- Name: po_area area_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_area
    ADD CONSTRAINT area_pkey PRIMARY KEY (po_id);


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
-- Name: po_block block_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_block
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
-- Name: po_contentblock contentblock_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_contentblock
    ADD CONSTRAINT contentblock_pkey PRIMARY KEY (block_id, content_id);


--
-- Name: po_page page_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_page
    ADD CONSTRAINT page_pkey PRIMARY KEY (po_id);


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
-- Name: po_site site_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_site
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
-- Name: po_viewcontent unique_site_content; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_viewcontent
    ADD CONSTRAINT unique_site_content UNIQUE (site_id, content_id);


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
-- Name: block_cumpleanos_id_idx; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX block_cumpleanos_id_idx ON po_block_cumpleanos USING btree (block_id);


--
-- Name: block_x_id_idx; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX block_x_id_idx ON po_block_x USING btree (block_id);


--
-- Name: contentblock_block_id_orden_idx; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX contentblock_block_id_orden_idx ON po_contentblock USING btree (block_id, orden);


--
-- Name: po_area_page_id_orden_idx; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX po_area_page_id_orden_idx ON po_area USING btree (page_id, orden);


--
-- Name: po_area_po_id_idx; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX po_area_po_id_idx ON po_area USING btree (po_id);


--
-- Name: po_block_area_id_section_orden_idx; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX po_block_area_id_section_orden_idx ON po_block USING btree (area_id, section, orden);


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

CREATE INDEX po_block_po_id_idx ON po_block USING btree (po_id);


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

CREATE INDEX po_page_po_id_idx ON po_page USING btree (po_id);


--
-- Name: po_page_site_id_po_id_idx; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX po_page_site_id_po_id_idx ON po_page USING btree (site_id, po_id);


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

CREATE INDEX po_site_po_id_idx ON po_site USING btree (po_id);


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
-- Name: site_security_rule_object_idx; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX site_security_rule_object_idx ON po_site_securityrule USING btree (related_object_id);


--
-- Name: po_area area_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_area
    ADD CONSTRAINT area_fk FOREIGN KEY (po_id) REFERENCES po_portalobject(id) ON DELETE CASCADE;


--
-- Name: po_block area_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_block
    ADD CONSTRAINT area_fk FOREIGN KEY (area_id) REFERENCES po_area(po_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: po_block_contact block_contact_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_block_contact
    ADD CONSTRAINT block_contact_fk FOREIGN KEY (block_id) REFERENCES po_block(po_id) ON DELETE CASCADE;


--
-- Name: po_block block_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_block
    ADD CONSTRAINT block_fk FOREIGN KEY (po_id) REFERENCES po_portalobject(id) ON DELETE CASCADE;


--
-- Name: po_block_site_list block_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_block_site_list
    ADD CONSTRAINT block_fk FOREIGN KEY (block_id) REFERENCES po_block(po_id) ON DELETE CASCADE;


--
-- Name: po_block_text block_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_block_text
    ADD CONSTRAINT block_fk FOREIGN KEY (block_id) REFERENCES po_block(po_id) ON DELETE CASCADE;


--
-- Name: po_block_view_list block_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_block_view_list
    ADD CONSTRAINT block_fk FOREIGN KEY (block_id) REFERENCES po_block(po_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: po_block_content_list block_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_block_content_list
    ADD CONSTRAINT block_fk FOREIGN KEY (block_id) REFERENCES po_block(po_id) ON DELETE CASCADE;


--
-- Name: po_block_cumpleanos block_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_block_cumpleanos
    ADD CONSTRAINT block_fk FOREIGN KEY (block_id) REFERENCES po_block(po_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: po_block_x block_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_block_x
    ADD CONSTRAINT block_fk FOREIGN KEY (block_id) REFERENCES po_block(po_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: po_contentblock block_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_contentblock
    ADD CONSTRAINT block_fk FOREIGN KEY (block_id) REFERENCES po_block(po_id) ON DELETE CASCADE;


--
-- Name: po_block_banners block_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_block_banners
    ADD CONSTRAINT block_fk FOREIGN KEY (block_id) REFERENCES po_block_view_list(block_id) ON DELETE CASCADE;


--
-- Name: po_block_image_viewer block_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_block_image_viewer
    ADD CONSTRAINT block_fk FOREIGN KEY (block_id) REFERENCES po_block(po_id) ON DELETE RESTRICT;


--
-- Name: po_block_select_list block_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_block_select_list
    ADD CONSTRAINT block_fk FOREIGN KEY (block_id) REFERENCES po_block(po_id) ON DELETE RESTRICT;


--
-- Name: po_block_search_external block_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_block_search_external
    ADD CONSTRAINT block_fk FOREIGN KEY (block_id) REFERENCES po_block(po_id) ON DELETE RESTRICT;


--
-- Name: po_block_wall_viewer block_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY po_block_wall_viewer
    ADD CONSTRAINT block_fk FOREIGN KEY (block_id) REFERENCES po_block_view_list(block_id) ON DELETE CASCADE;


--
-- Name: po_block_footer block_footer_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_block_footer
    ADD CONSTRAINT block_footer_fk FOREIGN KEY (block_id) REFERENCES po_block(po_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: po_block_gallery_viewer block_gallery_viewer_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbee
--

ALTER TABLE ONLY po_block_gallery_viewer
    ADD CONSTRAINT block_gallery_viewer_fk FOREIGN KEY (block_id) REFERENCES po_block_view_list(block_id) ON DELETE CASCADE;


--
-- Name: po_block block_image_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_block
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
    ADD CONSTRAINT block_sc_fk FOREIGN KEY (block_id) REFERENCES po_block(po_id) ON DELETE CASCADE;


--
-- Name: po_block_selector block_selector_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_block_selector
    ADD CONSTRAINT block_selector_fk FOREIGN KEY (block_id) REFERENCES po_block(po_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: po_page content_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_page
    ADD CONSTRAINT content_fk FOREIGN KEY (content_link) REFERENCES content(id) ON DELETE SET NULL;


--
-- Name: po_block content_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_block
    ADD CONSTRAINT content_fk FOREIGN KEY (content_link) REFERENCES content(id) ON DELETE SET NULL;


--
-- Name: po_contentblock content_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_contentblock
    ADD CONSTRAINT content_fk FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE RESTRICT;


--
-- Name: po_viewcontent content_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_viewcontent
    ADD CONSTRAINT content_fk FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE CASCADE;


--
-- Name: po_portalobject domain_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_portalobject
    ADD CONSTRAINT domain_fk FOREIGN KEY (domain_id) REFERENCES domain(id) ON DELETE RESTRICT;


--
-- Name: po_site footer_block_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_site
    ADD CONSTRAINT footer_block_fk FOREIGN KEY (footer_block_id) REFERENCES po_block(po_id) ON DELETE SET NULL;


--
-- Name: po_site header_block_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_site
    ADD CONSTRAINT header_block_fk FOREIGN KEY (header_block_id) REFERENCES po_block(po_id) ON DELETE SET NULL;


--
-- Name: po_block image_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_block
    ADD CONSTRAINT image_fk FOREIGN KEY (image_id) REFERENCES kfile(resource_id) ON DELETE RESTRICT;


--
-- Name: po_block_image_viewer image_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_block_image_viewer
    ADD CONSTRAINT image_fk FOREIGN KEY (imageviewer_id) REFERENCES idoc(content_id) ON DELETE SET NULL;


--
-- Name: po_page page_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_page
    ADD CONSTRAINT page_fk FOREIGN KEY (po_id) REFERENCES po_portalobject(id) ON DELETE RESTRICT;


--
-- Name: po_area page_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_area
    ADD CONSTRAINT page_fk FOREIGN KEY (page_id) REFERENCES po_page(po_id) ON DELETE CASCADE;


--
-- Name: po_block page_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_block
    ADD CONSTRAINT page_fk FOREIGN KEY (page_link) REFERENCES po_page(po_id);


--
-- Name: po_site page_header_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_site
    ADD CONSTRAINT page_header_fk FOREIGN KEY (page_header_footer_id) REFERENCES po_page(po_id);


--
-- Name: po_portalobject parent_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_portalobject
    ADD CONSTRAINT parent_fk FOREIGN KEY (parent_id) REFERENCES po_portalobject(id) ON DELETE RESTRICT;


--
-- Name: po_site_securityrule rule_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_site_securityrule
    ADD CONSTRAINT rule_fk FOREIGN KEY (rule_id) REFERENCES kb_security_rule(id) ON DELETE CASCADE;


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
-- Name: po_site site_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_site
    ADD CONSTRAINT site_fk FOREIGN KEY (po_id) REFERENCES po_portalobject(id) ON DELETE RESTRICT;


--
-- Name: po_siteuser site_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_siteuser
    ADD CONSTRAINT site_fk FOREIGN KEY (site_id) REFERENCES po_site(po_id) ON DELETE RESTRICT;


--
-- Name: po_page site_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_page
    ADD CONSTRAINT site_fk FOREIGN KEY (site_id) REFERENCES po_site(po_id) ON DELETE CASCADE;


--
-- Name: po_block_site_components site_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_block_site_components
    ADD CONSTRAINT site_fk FOREIGN KEY (site_id) REFERENCES po_site(po_id) ON DELETE SET NULL;


--
-- Name: po_viewcontent site_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_viewcontent
    ADD CONSTRAINT site_fk FOREIGN KEY (site_id) REFERENCES po_site(po_id) ON DELETE CASCADE;


--
-- Name: po_siteuserrights site_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_siteuserrights
    ADD CONSTRAINT site_fk FOREIGN KEY (site_id) REFERENCES po_site(po_id) ON DELETE RESTRICT;


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
-- Name: po_viewcontentrelation target_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_viewcontentrelation
    ADD CONSTRAINT target_id_fk FOREIGN KEY (target_id) REFERENCES content(id) ON DELETE CASCADE;


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
    ADD CONSTRAINT viewbk_block_fk FOREIGN KEY (block_id) REFERENCES po_block(po_id) ON UPDATE CASCADE ON DELETE CASCADE;


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
    ADD CONSTRAINT viewbkblock_site_fk FOREIGN KEY (block_id) REFERENCES po_block(po_id) ON DELETE SET NULL;


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
    ADD CONSTRAINT viewbksite_site_fk FOREIGN KEY (site_id) REFERENCES po_site(po_id) ON DELETE SET NULL;


--
-- Name: po_viewbksite viewbksite_view_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY po_viewbksite
    ADD CONSTRAINT viewbksite_view_fk FOREIGN KEY (view_id) REFERENCES po_viewbk(po_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- PostgreSQL database dump complete
--

