------------------------------------------------------------------------------------
--- PORTAL IDOC7
---

alter table po_viewbk alter column abstract set data type character varying(2048);
alter table po_viewbk add column style character varying(64);
update po_viewbk set style='width:100%;';
alter table PO_BLOCK_VIEW_LIST add column subtitle_mode integer default 0;
update PO_BLOCK_VIEW_LIST set subtitle_mode=0;

CREATE TABLE po_block_wall_viewer
(
  block_id bigint NOT NULL,
  CONSTRAINT block_wall_viewer_pkey PRIMARY KEY (block_id),
  CONSTRAINT block_fk FOREIGN KEY (block_id)
      REFERENCES po_block_view_list (block_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);
ALTER TABLE po_block_wall_viewer OWNER TO kbee;

alter table kcomment add column site_id bigint;
CREATE INDEX ON KComment (site_id, date_submitted desc);
CREATE INDEX ON KComment (referenced_content_id, date_submitted desc);

CREATE TABLE PO_BLOCK_GALLERY_VIEWER
(
  block_id bigint NOT NULL,
  CONSTRAINT block_GALLERY_VIEWER_pkey PRIMARY KEY (block_id),
  CONSTRAINT block_GALLERY_VIEWER_fk FOREIGN KEY (block_id)
      REFERENCES po_block_view_list (block_id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);
ALTER TABLE PO_BLOCK_GALLERY_VIEWER OWNER TO kbee;


 
 alter table po_page add column usage_info character varying(2048)

 
alter table po_viewbklink alter column  link set data type character varying(1024);



update po_portalobject PO set parent_id=(select block_id from po_viewbk VX where VX.po_id=PO.id) where PO.parent_id is null and PO.id in (select po_id from po_viewbk);


alter table po_area add column areaclass character varying(128);
alter table po_block add column block_body_style character varying(1024);

#alter table po_area add areaclass varchar2(128);
#alter table po_block add block_body_style varchar2(1024);

update po_site set site_type=1 where site_type is null;
update po_site set uri=to_char(po_id, 'FM999999999') where uri is null;
update po_viewbk set style=null;
 
alter table po_viewbkcontent add column is_gallery boolean default false;
alter table po_viewbkcontent add column is_resources  boolean default true;

Alter table po_viewcontent add titlemode integer default 0;
Alter table po_viewcontent add isabstract boolean default true;
Alter table po_viewcontent add ismetadata boolean default true;
Alter table po_viewcontent add isviewer boolean default false;
Alter table po_viewcontent add bodytemplate integer default 0;
Alter table po_viewcontent add  isresources boolean default false;
Alter table po_viewcontent add  resourcesmode integer default 0;
Alter table po_viewcontent add  resourcesids character varying(4096);

alter table po_viewcontent drop column site_id;

CREATE INDEX ON po_viewcontent (content_id);
 
delete from po_viewcontent;
alter table po_viewcontent add column content_oid bigint not null;
create index view_detail_contentoid on po_viewcontent(content_oid, content_id);

insert into contentclass(id, enabled, name, javaclass, indexable) values('KbeeView', true, 'View', 'com.novamens.kbee.portal.model.publish.KbeeViewDetailContent', true);
 
alter table po_block_x add column file_id bigint;
alter table po_block_x add column jsondata text;
alter table po_block_x add column xurl  character varying(1024);
alter table po_block_x add column content_id bigint;

alter table po_viewbk add column iconcss character varying(64);
alter table po_block add column block_separator_css character varying(64);
alter table PO_BLOCK_VIEW_LIST add column element_orientation_css character varying(64);
alter table po_viewcontent add column issearchable boolean default true;
	
alter table po_portalobject add column nextversion integer;
update po_portalobject set version=1, nextversion=2 where version=0 or nextversion is null;
