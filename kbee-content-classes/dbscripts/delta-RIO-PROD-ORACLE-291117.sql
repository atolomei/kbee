 -- AT 8 Mayo 2017
	
	
	alter table kb_contenttemplate add  isphoto 	number(1) default 0;	
	alter table kb_contenttemplate add  isvideo    	number(1) default 0;

	alter table kgroup add derived number(1) default 0;

	
	alter table kb_contenttemplate add istool number(1) default 0;
	alter table kb_contenttemplate add isactivity number(1) default 0;	
	alter table classifier add metadatasubtitle number(1) default 0;
	alter table  KB_CLASSIFIERTEMPLATE add ismetadatasubtitle number(1) default 0;
	alter table wf_launcher add isenabled number(1) default 1;

	CREATE TABLE kb_tip
	(
	  id 			  number(19)  NULL,
	  domain_id 	  number(19) ,
	  status  		 number(6),
	  tip_area		 varchar2(18),	
	  lastmodifieddate 	timestamp with time zone DEFAULT sysdate,
	  lastmodifieduser 	number(19),
	  tip_title 		      VARCHAR2(256),
	  tip_text 			clob,
	  tip_texyid  		VARCHAR2(256),
	  tip_lang	   		VARCHAR2(32),
	  CONSTRAINT tip_pkey PRIMARY KEY (id)
	) TABLESPACE SBD02_DATA;

	
	CREATE INDEX kb_tip_text_idx ON kb_tip(tip_lang, tip_area, tip_title) TABLESPACE SBD02_INDEX;

	alter table userprofile add tipoftheday number(1) default 1;
	alter table  kb_contenttemplate add linkresources number(1) default 1;

	alter table dataset add abbreviation character(18);
	alter table domain add istemplate number(1) default 0;
	update domain set type=4 where name='kbee';
	alter table domain add maxusers number(1) default 0;
	alter table users add active number(1) default 1;
	alter table aclentry modify (permissions varchar2(128));
	commit;

	
	#-- ok
	
	alter table wf_procedure add initial_rules clob;
	alter table users add creationdate timestamp with time zone default CURRENT_TIMESTAMP;
	alter table profile add  creationdate timestamp with time zone default CURRENT_TIMESTAMP;;
	alter table principal add creationdate timestamp with time zone default CURRENT_TIMESTAMP;
	alter table kb_domain_settings add lastmodifieddate timestamp with time zone default CURRENT_TIMESTAMP;
	alter table kcomment add creationdate timestamp with time zone default CURRENT_TIMESTAMP;
	alter table userlabel add creationdate timestamp with time zone default CURRENT_TIMESTAMP;
	alter table kb_enotirule add creationdate timestamp with time zone default CURRENT_TIMESTAMP;
	
	alter table securityrule rename to kb_security_rule;
	alter table kb_security_rule add creationdate timestamp with time zone default CURRENT_TIMESTAMP;
	
	alter table acl add creationdate timestamp with time zone default CURRENT_TIMESTAMP;
	alter table users add timezone character varying(256) default 'US/Central';

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

	drop table banner;
	drop table htmltext;
	drop table tool;

	alter table acl 	   rename to kb_acl;
	alter table aclentry   rename to kb_aclentry;
	alter table vote       rename to kb_vote;

	alter table kb_vote add id number(19);
	update kb_vote set id=-1 * (user_id  * 10000000 + content_id);
	
	alter table domain add tipoftheday number(1) default 1;
	alter table UserProfile add editperson number(1) default 1;

	update kgroup set name = 'Templates' where name ='ROLE_FORMS_LIBRARY';

	#--  ok
	
	alter table notification rename to kb_notification;
	
	alter table kcomment rename to kb_comment;
	
	alter table content drop constraint prev_versionCont_fk;
	CREATE TABLE kb_email_template
	(
		id NUMBER(19) NOT NULL,
		creationdate TIMESTAMP WITH TIME ZONE DEFAULT current_timestamp,
		lastmodifieddate TIMESTAMP WITH TIME ZONE DEFAULT current_timestamp,
		lastmodifieduser NUMBER(6) NOT NULL,
		state NUMBER(2),
		domain_id NUMBER(4),
		lang		 varchar2(24),
		xkey		 varchar2(256),
		title 		 varchar2(256),
		fromstr	 	 varchar2(512),
		subject 	 character varying(512),
		strtext 	 clob,    
		CONSTRAINT email_template_pkey PRIMARY KEY (id),
		CONSTRAINT emtdomainCont_fk FOREIGN KEY (domain_id) REFERENCES domain(id),
		CONSTRAINT emtdlk unique (domain_id, lang, xkey),
		CONSTRAINT emtuserCont_fk FOREIGN KEY (lastModifiedUser) REFERENCES users(id)		
	 )  TABLESPACE SBD02_DATA;
	 
	alter table kfile drop constraint file_fk;
	alter table person add constraint file_photo_fk foreign key(photo) references kfile(resource_id);
	alter table person add constraint file_fk foreign key(photo) references kfile(resource_id)   on delete set null;

	alter table scheduler add  title VARCHAR2(64);
	alter table logevent add event_procedure VARCHAR2(64); 

	alter table dataset add secured number(1) default 0;
	alter table kb_attributetemplate add metadatasubtitle number(1) default 0;
	
	
	CREATE TABLE kb_attribute (
		id 					NUMBER(19) NOT NULL,
		creationdate		TIMESTAMP WITH TIME ZONE DEFAULT current_timestamp,
		lastmodifieddate	TIMESTAMP WITH TIME ZONE DEFAULT current_timestamp,
		lastmodifieduser	NUMBER(6) NOT NULL,
		state				NUMBER(2),
		domain_id			NUMBER(4),
		name		 		VARCHAR2(128),
		type				NUMBER(3),
		multiplicity		NUMBER(4) default 1000,
		uniquename			VARCHAR2(128),
		korder				NUMBER(3),
		iscanonical			NUMBER(1) DEFAULT 0,
		metadatasubtitle	NUMBER(1) DEFAULT 0,
		visibility			CLOB,
		CONSTRAINT attribute_pkey PRIMARY KEY (id),
		CONSTRAINT domainAttr_fk FOREIGN KEY (domain_id) REFERENCES domain(id) ON DELETE CASCADE,
		CONSTRAINT userAttr_fk FOREIGN KEY (lastModifiedUser) REFERENCES users(id) ON DELETE SET NULL		
	) TABLESPACE SBD02_DATA;
	
	
	commit;
	
	CREATE TABLE kb_user_property
	(
	    id number(19) NOT NULL,
	    type number(4),
	    name varchar2(128),
	    user_id number(19) NOT NULL,
	    value clob,    
		CONSTRAINT userproperty_pkey PRIMARY KEY (id),
	    CONSTRAINT user_prop_fk FOREIGN KEY (user_id)     REFERENCES users (id) ON DELETE CASCADE    
	) TABLESPACE SBD02_DATA;
	
	CREATE INDEX property_user_id_idx ON kb_user_property(user_id) TABLESPACE SBD02_INDEX;

	alter table  classifier rename to kb_classifier;
	alter table  kb_attributetemplate drop column name;
	alter table  kb_attributetemplate drop column type;
	alter table  kb_attributetemplate drop column multiplicity;
	alter table  kb_attributetemplate drop column mandatory;

	alter  table datasetmember add rule_id NUMBER(19);
	alter  table datasetmember add CONSTRAINT rulemember_fk FOREIGN KEY (rule_id) REFERENCES kb_security_rule (id);

	alter  table datasetmember add group_id NUMBER(19);
	alter  table datasetmember add CONSTRAINT groupmember_fk FOREIGN KEY (group_id) REFERENCES kgroup (id);

	alter table  kb_attributetemplate add attribute_id NUMBER(19);
	alter table	 kb_attributetemplate add CONSTRAINT attributetemplate_fk FOREIGN KEY (attribute_id) REFERENCES kb_attribute (id);

	commit;
	
	alter table  kb_classifiertemplate rename column ismetadatasubtitle to metadatasubtitle;

	alter table kgroupmember drop constraint group_fk;
	alter table kgroupmember add constraint group_fk foreign key(kgroup) references kgroup(id) on delete cascade;
	alter  table kb_security_rule add  derived NUMBER(1) default 0;
				
	alter table domain add lang character(6) default 'es';

	alter table classification drop constraint contentcl_fk;
	ALTER TABLE classification ADD CONSTRAINT contentcl_fk FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE CASCADE;


	alter table po_viewbk add ntab number(1) default 0;

	update po_site set site_type=1 where site_type is null;
	update po_site set uri=to_char(po_id, 'FM999999999') where uri is null;

	#--  alter table kb_security_rule add displaycondition clob;
 	#--  alter table kb_security_rule add parent_objectid  varchar2(48);

 	update po_viewbk set style=null;
 	 

 	# -- alter table content add user_defined_properties clob;
 	# -- alter table content add external_id varchar2(32);

 	#-- alter table kb_contenttemplate add isadd number(1) default 0;
 	# --alter table kb_contenttemplate add iscustomattributes number(1) default 0;
 	
 	commit;
 	
 	
 	
-- -----------------------------------------
-- 27 Junio 2017
-- 
	
CREATE TABLE Kb_Cabinet (
	id				 		 number(19) NOT NULL,
	domain_id				 number(19) NOT NULL,
	lastModifiedDate		 TIMESTAMP WITH TIME ZONE DEFAULT current_timestamp,
	creationDate			 TIMESTAMP WITH TIME ZONE DEFAULT current_timestamp,
	lastModifiedUser		 number(19) NOT NULL,
	display_name 			 varchar2(256),
	criteria				 varchar2(256),
	state					 number(2),
	readonly				 number(1) default 0,
	reader_group			 number(19),
	listorder				 number(2),	
	CONSTRAINT cabinet_pkey PRIMARY KEY (id),
	CONSTRAINT  cab_lastModifiedUser_fk 	FOREIGN KEY (lastModifiedUser) REFERENCES  users(id),		
	CONSTRAINT cab_domain_fk FOREIGN KEY (domain_id) REFERENCES domain (id) ON DELETE CASCADE,
	CONSTRAINT cab_group_fk FOREIGN KEY (reader_group) REFERENCES content(id)		
) TABLESPACE SBD02_DATA;



CREATE INDEX  cabinet_domain_idx ON kb_cabinet (domain_id, listorder) TABLESPACE SBD02_INDEX;

alter table datasetmember add notes clob;
alter table kb_contenttemplate add iskbase number(1) default 0;


alter table domain add  cabinet_template number(1) default 0;
alter table domain add  cabinet_kbase  number(1) default 0;
alter table domain add  cabinet_external  number(1) default 0;

alter table kb_cabinet add  key 	 varchar2(24);
update kb_cabinet  set key=lower(display_name);
update kb_cabinet  set key='kbase' where key like 'k%';


alter table kb_classifiertemplate add CONSTRAINT classifier_fk FOREIGN KEY (classifier_id) REFERENCES kb_classifier (id) ON DELETE RESTRICT;













 	
insert into kb_cabinet (id, domain_id, key, display_name, criteria, state, lastmodifieduser, listorder) values (13, 3, 'standard',  'Corporativo', 	    'type=[text, idoc];head=true;state=1;-istemplate=true;',        1, 1, 0);
insert into kb_cabinet (id, domain_id, key, display_name, criteria, state, lastmodifieduser, listorder) values (14, 3, 'templates', 'Plantillas',  	 	'type=[text, idoc];head=true;state=1;istemplate=true', 		    1, 1, 1);																							
insert into kb_cabinet (id, domain_id, key, display_name, criteria, state, lastmodifieduser, listorder) values (15, 3, 'all',       'Todo', 		     	'type=[text, idoc];head=true;state=1;', 					1, 1, 10);

 	
alter table kb_classifiertemplate add CONSTRAINT classifier_templatefk FOREIGN KEY (classifier_id) REFERENCES kb_classifier (id);


alter table domain add  logo_url varchar2(256);
update datasetmember set type = 7 where type=6

 	commit;
 	

# ---------------------------------------------------------------------------------------- 	
	
 	
CREATE TABLE kb_system_properties
(
    key    varchar2(256) not null,
	value  varchar2(2048),
	CONSTRAINT sp_pkey PRIMARY KEY (key)
) TABLESPACE SBD02_DATA;

delete from kb_system_properties;

insert into kb_system_properties (key, value) values ('support1.pwd', '1Aqqqqqq');
insert into kb_system_properties (key, value) values ('support1.email','ryan.garneau@realpage.com');
insert into kb_system_properties (key, value) values ('support1.phone','802-316-3521');

insert into kb_system_properties (key, value) values ('support2.pwd','1Aqqqqqq');
insert into kb_system_properties (key, value) values ('support2.email','james.burbo@realpage.com');
insert into kb_system_properties (key, value) values ('support2.phone','802-316-3525');
																	
insert into kb_system_properties (key, value) values ('default_timezone','US/Central');
insert into kb_system_properties (key, value) values ('default_noreply','noreply@rpdm.realpage.com');
insert into kb_system_properties (key, value) values ('default_labels',	'Draft;Delete;Follow up;Duplicate');
																				
insert into kb_system_properties (key, value) values ('default_type_values','Training;Tenant Selection Plan;Contract;EOM Financials;Mortgage Statement;Lease Agreement;Rent Schedule;Management Agreements;Non-Disclosure Agreement;Shareholder Meetings;Lawsuits;Acquisitions;Due Diligence;Territory Assignments;Sales Incentives;Compensation Plan;Hardware;Software;System Logs;Benefits;Organizational Chart;Annual Reviews;Offer Letters;Signage;Brochures;Flyers');
insert into kb_system_properties (key, value) values ('default_status_values','Draft;Under Review;Approved;Final;Cancelled');
insert into kb_system_properties (key, value) values ('default_department_values','Marketing;HR;IT;Sales;Legal;Finance;Compliance;Property Management;Facilities;Training');
 	
	commit;
	
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

commit;

alter table content add private_notes clob;
alter table kb_contenttemplate add private_notes char(1) default 0;
commit;

alter table kresource add  ispublic char(1) default 1;
commit;

 	
 	# ---------------------------------------------------------------------------------------- 	
 	#-- alter table content add external_id varchar2(64);
 	#-- alter table po_viewbkcontent add is_gallery number(1) default 0;
	#--alter table po_viewbkcontent add is_resources number(1) default 0;
	#--alter table kb_aclentry drop constraint principal_fk;
	#--alter table kb_aclentry add constraint principal_fk FOREIGN KEY (principal) REFERENCES Principal(id) ON DELETE CASCADE;
								
	#----------------------------
	# Tips

	delete from kb_tip;
																																		
	insert into kb_tip (id, tip_area,  tip_title, tip_texyid, tip_lang, tip_text) values (-999, 'portal', 'Sabías que ?', 'texy_welcome_left'   ,'es', '<p>Soy <span class="highlight">Texy</span>, la nueva mascota del software kbee de la Intranet. Me vas a ver cada tanto...</p>');
	insert into kb_tip (id, tip_area,  tip_title, tip_texyid, tip_lang, tip_text) values (-1000,  'portal',  'Sabías que ?',   'texy_help_right' ,'es', '<p>Los sitios se componen de <span class="highlight">Páginas</span>, y cada página a su vez se arma con  <span class="highlight">Areas</span>, que contienen  <span class="highlight">Secciones</span> donde van los  <span class="highlight">Blocks</span>. Los contenidos se publican en los Blocks.</p>');
	insert into kb_tip (id, tip_area,  tip_title, tip_texyid, tip_lang, tip_text) values (-1001,  'portal',  'Sabías que ?',   'texy_help_right' ,'es', '<p>Las páginas son de dos tipos: <span class="highlight">Agregadoras</span> y Páginas de <span class="highlight">Detalle</span>. Las Agregadoras son las que se diagraman y agregan contenidos desde el publicador. Las páginas de Detalle no se diagraman, se generan automáticamente según la plantilla de cada tipo de contenido.</p>.</p>');
	insert into kb_tip (id, tip_area,  tip_title, tip_texyid, tip_lang, tip_text) values (-1002,  'portal',  'Sabías que ?',   'texy_help_right' ,'es', '<p>Los componentes fundamentales del publicador son jerárquicos: <span class="highlight">Sitio</span> / <span class="highlight">Página</span> / <span class="highlight">Area</span> / <span class="highlight">Sección</span> / <span class="highlight">Block</span>. Los contenidos se publican en los Blocks. </p>');
	insert into kb_tip (id, tip_area,  tip_title, tip_texyid, tip_lang, tip_text) values (-1003,  'portal',  'Sabías que ?',   'texy_help_right' ,'es', '<p>Un Area es una <span class="highlight">franja horizontal</span> que ocupa todo el ancho de la pantalla. Contiene Secciones, que contienen Blocks. Las secciones de un Area son <span class="highlight">fijas</span>, no se pueden agregar ni quitar. Los Blocks son <span class="highlight">dinámicos</span>.</p>');
	insert into kb_tip (id, tip_area,  tip_title, tip_texyid, tip_lang, tip_text) values (-1004,  'portal',  'Sabías que ?',   'texy_hi_right' ,'es', '<p>Los Sitios son de dos tipos:  <span class="highlight">Internos</span> o <span class="highlight">Externos</span>. Los Sitios externos son básicamente un nombre y un enlace al sitio externo correspondiente. Los Sitios internos pueden tener 2 plantillas: <span class="highlight">Area</span> o <span class="highlight">Grupo de Interés</span>. La plantilla de Sitio determina como se muestra el encabezado del Sitio.</p>');
	insert into kb_tip (id, tip_area,  tip_title, tip_texyid, tip_lang, tip_text) values (-1005,  'portal',  'Sabías que ?',   'texy_hi_right' ,'es', '<p>Todos los usuarios puede acceder a los <span class="highlight">sitios públicos</span>. Los sitios de acceso privado pueden accederlos usuarios con permiso de <span class="highlight">Lectura</span>. Para publicar contenidos en ambos casos se requiere permiso <span class="highlight">Escritura</span> y para diagramar <span class="highlight">Admin</span>.</p>');
	insert into kb_tip (id, tip_area,  tip_title, tip_texyid, tip_lang, tip_text) values (-1006,  'portal',  'Sabías que ?',   'texy_help_right' ,'es', '<p>Los componentes del publicador pueden tener 4 estados: <span class="highlight">Borrador</span>, <span class="highlight">Publicado</span>, <span class="highlight">Archivado</span>, <span class="highlight">Borrado</span>. Sólo los componentes publicados son visibles en el sitio,  Los componentes en estado Borrador o Archivo se ven en el publicador con color <span class="highlight">naranja</span>.</p>');
	insert into kb_tip (id, tip_area,  tip_title, tip_texyid, tip_lang, tip_text) values (-1007,  'portal',  'Sabías que ?',   'texy_help_right' ,'es', '<p>Existen 8 tipos de Areas diferentes. <br/> 
	<span class="highlight">Area 1S</span>. 1 sóla sección en todo el ancho. <br/>
	<span class="highlight">Area 2S 33x66</span>. 2 Secciones, 33% Izquierda y 66% derecha. <br/>
	<span class="highlight">Aera 2S 50x50</span>. 2 Secciones, 50% Cad una. <br/>
	<span class="highlight">Aera 2S 66x33</span>. 2 Secciones, 66% Izquierda y 33% derecha. <br/>
	<span class="highlight">Aera 2S 75x25</span>. 2 Secciones, 75% Izquierda y 25% derecha. <br/>
	<span class="highlight">AREA 3S  3x33</span>. 3 Secciones, 33% cada una. <br/>
	<span class="highlight">Area 2S L66(2x50+1x100)xR33</span>. 2 Secciones 66% y 33%, la izquierda contiene dos subareas arriba: 2 secciones 50% cada una, y abajo 1 seccion de 100%. <br/>
	<span class="highlight">Area 1H</span>. 1 Sección Horizontal, para Encabezado y Pie de Página global. <br/></p>');	

	insert into kb_tip (id, tip_area,  tip_title, tip_texyid, tip_lang, tip_text) values (-1008, 'portal', 'Sabías que ?', 'texy_hi_left'  ,'es', '<p>El software maneja separadamente <span class="highlight">diagramación</span>, <span class="highlight">contenidos</span> y <span class="highlight">publicación</span>. Esto ofrece una gran versatilidad. Por ejemplo es posible  cambiar la estructura de un Sitio o Area, subir o bajar un Block y volver atrás sin problemas.</p>');
	insert into kb_tip (id, tip_area,  tip_title, tip_texyid, tip_lang, tip_text) values (-1009, 'portal', 'Sabías que ?', 'texy_help_left'  ,'es', '<p>Cuando se edita un contenido que está publicado la edición se realiza sobre una <span class="highlight">copia de trabajo</span>; la versión original sigue publicada en el sitio. Al enviar a la base la copia de trabajo, el sitio se actualiza automáticamente.</p>');
	insert into kb_tip (id, tip_area,  tip_title, tip_texyid, tip_lang, tip_text) values (-1011, 'portal', 'Sabías que ?', 'texy_training_left'  ,'es', '<p>En el menú de Sitio podés registrarte para <span class="highlight">Seguir el Sitio</span>, y cuando el Sitio se actualiza te <span class="highlight">envía un email</span></p>');
	insert into kb_tip (id, tip_area,  tip_title, tip_texyid, tip_lang, tip_text) values (-1012, 'portal', 'Sabías que ?', 'texy_help_left'  ,'es', '<p>El tab <span class="highlight">Bibliotecas</span> en el Editor de Block permite publicar de forma ágil contenidos recientemente enviados a la Base.</p>');
	insert into kb_tip (id, tip_area,  tip_title, tip_texyid, tip_lang, tip_text) values (-1013, 'portal', 'Sabías que ?', 'texy_help_left'  ,'es', '<p>El Block <span class="highlight">Grilla</span> sirve para publicar una gran cantidad de contenidos, con posibilidad de paginar.</p>');
	insert into kb_tip (id, tip_area,  tip_title, tip_texyid, tip_lang, tip_text) values (-1014, 'portal', 'Sabías que ?', 'texy_training_left'  ,'es', '<p>Los editores de los componentes (Sitio, Página, Area, Block) tienen siempre estos tres tabs:<br/>Tab para administrar los <span class="highlight">elementos</span> que contiene<br/> Tab <span class="highlight">Publicación</span> para publicar el componente.<br/> Tab <span class="highlight">Información General</span> para los parámetros generales.</p>');
	insert into kb_tip (id, tip_area,  tip_title, tip_texyid, tip_lang, tip_text) values (-1015, 'portal', 'Sabías que ?', 'texy_help_left'  ,'es', '<p>Una diferencia sutil entre usar <span class="highlight">Block Grilla</span> y poner Blocks en un <span class="highlight">Area 3S</span>: En un block grilla los contenidos de una fila se alinean con el alto del mayor de ellos. En un Area 3S, cada columna es independiente, las filas no se alinean.</p>');
	insert into kb_tip (id, tip_area,  tip_title, tip_texyid, tip_lang, tip_text) values (-1016, 'portal', 'Sabías que ?', 'texy_training_right' ,'es', '<p>El Block <span class="highlight">Grilla</span> soporta tres formatos:<br/>tres columnas con imagen grande en el centro <br/>dos columnas con imagen chica a la derecha<br/>dos columnas con imagen chica a la izquierda</p>');
	insert into kb_tip (id, tip_area,  tip_title, tip_texyid, tip_lang, tip_text) values (-1017, 'portal', 'Sabías que ?', 'texy_help_right' ,'es', '<p><span class="highlight">Archivar</span> una Página, Area o Block hace que deje de estar publicado pero sigue disponible en el Editor de Sitio y puede ser republicado.</p>');
	insert into kb_tip (id, tip_area,  tip_title, tip_texyid, tip_lang, tip_text) values (-1018, 'portal', 'Sabías que ?', 'texy_help_right' ,'es', '<p>Cada tipo de Block que soporta publicación manual, determina qué acepta:  <span class="highlight">Vista de Contenidos</span>,  <span class="highlight">Vista de Sitio</span>,  <span class="highlight">Vista de Enlace</span>. Y en función de eso adapta el selector de <span class="highlight">Agregar</span></p>');
	insert into kb_tip (id, tip_area,  tip_title, tip_texyid, tip_lang, tip_text) values (-1019, 'portal', 'Sabías que ?', 'texy_help_right' ,'es', '<p>Lo que se publica en los Blocks se llama <span class="highlight">Vista</span>. Una Vista puede ser de un <span class="highlight">Contenido</span>, un <span class="highlight">Sitio</span> o un <span class="highlight">Enlace Externo</span>. En la Vista se puede editar el título, agregar o cambiar la foto, descripción, y definir si abre en un nuevo tab.</p>');
	insert into kb_tip (id, tip_area,  tip_title, tip_texyid, tip_lang, tip_text) values (-1020, 'portal', 'Sabías que ?', 'texy_hi_left'    ,'es', '<p>En el menú contextual de cada Vista hay una opción para <span class="highlight">moverla a otro Block</span> de la página (que acepte ese tipo de Vista).</p>');
	insert into kb_tip (id, tip_area,  tip_title, tip_texyid, tip_lang, tip_text) values (-1021, 'portal', 'Sabías que ?', 'texy_hi_left'    ,'es', '<p>En el primer tab de cada Editor se listan los elementos que contiene:<br/>El  <span class="highlight">Editor de Sitio</span> lista la <span class="highlight">Páginas</span> que contiene<br/> El <span class="highlight">Editor de Página</span> lista las <span class="highlight">Areas</span> que contiene<br/>El <span class="highlight">Editor de Area</span> lista los <span class="highlight">Blocks</span> que contiene<br/>El <span class="highlight">Editor de Block</span> lista las <span class="highlight">Vistas</span> (Contenido, Sitio, Enlace) que contiene.</p>');
	insert into kb_tip (id, tip_area,  tip_title, tip_texyid, tip_lang, tip_text) values (-1022, 'portal', 'Sabías que ?', 'texy_general_left'   ,'es', '<p>Mi nombre es <span class="highlight">Texy</span>, soy la mascota del software kbee de la Intranet</p>');
	insert into kb_tip (id, tip_area,  tip_title, tip_texyid, tip_lang, tip_text) values (-1023, 'portal', 'Sabías que ?', 'texy_help_right'  ,'es', '<p>En <span class="highlight">Mi Cuenta</span> en la aplicación de gestión, podés habilitar y des-habilitar recibir el <span class="highlight">Tip del día</span>.</p>');
	insert into kb_tip (id, tip_area,  tip_title, tip_texyid, tip_lang, tip_text) values (-1024, 'portal', 'Sabías que ?', 'texy_help_right'  ,'es', '<p>El administrador de la Intranet puede deshabilitar los <span class="highlight">Tip del día</span> para todos los usuarios.</p>');
	insert into kb_tip (id, tip_area,  tip_title, tip_texyid, tip_lang, tip_text) values (-1025, 'portal', 'Sabías que ?', 'texy_help_right'  ,'es', '<p>El <span class="highlight">Tipo Jerárquico</span> de una página determina si es la Portada del Sitio, una Sección del Menú del Sitio, o si es una página general accesible por un enlace en los blocks.</p>');
	insert into kb_tip (id, tip_area,  tip_title, tip_texyid, tip_lang, tip_text) values (-1026, 'portal', 'Sabías que ?', 'texy_help_right'  ,'es', '<p>Podés deshabilitar el menú <span class="highlight">contextual rojo</span> para todos los elementos de la página en el Editor <span class="highlight">Info Página</span>.</p>');
	insert into kb_tip (id, tip_area,  tip_title, tip_texyid, tip_lang, tip_text) values (-1027, 'portal', 'Sabías que ?', 'texy_help_right'  ,'es', '<p>Sólo los usuarios administradores de Sitio o con permiso de Escritura ven el <span class="highlight">menú contextual rojo</span> en los elementos de la página. Los demás usuarios ven un enlace común.</span>.</p>');
	insert into kb_tip (id, tip_area,  tip_title, tip_texyid, tip_lang, tip_text) values (-1028, 'portal', 'Sabías que ?', 'texy_help_right'  ,'es', '<p><span class="highlight">El Tipo Jerárquico</span> de una página determina si es la Portada del Sitio, una Sección del Menú del Sitio, o si es una página general accesible por un enlace en los blocks.</p>');
	insert into kb_tip (id, tip_area,  tip_title, tip_texyid, tip_lang, tip_text) values (-1029, 'portal', 'Sabías que ?', 'texy_help_right'  ,'es', '<p>La   <span class="highlight">Clase Css</span> de Info Area define el estilo de la raya de separación con la anterior y siguiente. Probálo.</p>');
	insert into kb_tip (id, tip_area,  tip_title, tip_texyid, tip_lang, tip_text) values (-1030, 'portal', 'Sabías que ?', 'texy_help_right'  ,'es', '<p>Todos los Block tienen un encabezado común compuesto por título, subtítulo, imagen y bajada. En el Editor  <span class="highlight">Info Block</span> se pueden editar.</p>');
	insert into kb_tip (id, tip_area,  tip_title, tip_texyid, tip_lang, tip_text) values (-1031, 'portal', 'Sabías que ?', 'texy_help_right'  ,'es', '<p>El tab  <span class="highlight">Info Contenido</span> del Editor de Block permite editar las características de cada elemento listado en el block.</p>');
	insert into kb_tip (id, tip_area,  tip_title, tip_texyid, tip_lang, tip_text) values (-1032, 'portal', 'Sabías que ?', 'texy_help_right'  ,'es', '<p>Si un Editor de Block no tiene tab Info Contenido es porque el formato de presentación de los elementos es fijo.</p>');
	insert into kb_tip (id, tip_area,  tip_title, tip_texyid, tip_lang, tip_text) values (-1033, 'portal', 'Sabías que ?', 'texy_help_right'  ,'es', '<p>En los Blocks de tipo Lista, en Info Contenido se puede agregar un  <span class="highlight">Filtro</span> que funciona  <span class="highlight">mientras el usuario tipea</span>.</p>');
	insert into kb_tip (id, tip_area,  tip_title, tip_texyid, tip_lang, tip_text) values (-1034, 'portal', 'Sabías que ?', 'texy_help_right'  ,'es', '<p>Las Areas tienen 1 o más Secciones, dependiendo del tipo de Area (Izquierda, Centro, Derecha, etc.)</p>');
	insert into kb_tip (id, tip_area,  tip_title, tip_texyid, tip_lang, tip_text) values (-1035, 'portal', 'Sabías que ?', 'texy_help_right'  ,'es', '<p>Al agregar un Block, el selector de Sección contiene las secciones habilitadas para esa Area.</p>');
	insert into kb_tip (id, tip_area,  tip_title, tip_texyid, tip_lang, tip_text) values (-1036, 'portal', 'Sabías que ?', 'texy_help_right'  ,'es', '<p>Un Block  <span class="highlight">MultiBlock</span> es un Block con Tabs y cada tab contiene un Block.</p>');
	insert into kb_tip (id, tip_area,  tip_title, tip_texyid, tip_lang, tip_text) values (-1037, 'portal', 'Sabías que ?', 'texy_help_right'  ,'es', '<p>Para publicar un Block en un Block de tipo MultiBlock se lo debe agregar a la sección  <span class="highlight">Interno en MultiBlock</span>, y luego en el Multiblock aparecerá seleccionable en los elementos.</p>');
	insert into kb_tip (id, tip_area,  tip_title, tip_texyid, tip_lang, tip_text) values (-1038, 'portal', 'Sabías que ?', 'texy_help_right'  ,'es', '<p>Para publicar un Block en un Block de tipo MultiBlock se lo debe agregar a la sección  <span class="highlight">Interno en MultiBlock</span>, y luego en el Multiblock aparecerá seleccionable en los elementos.</p>');
	insert into kb_tip (id, tip_area,  tip_title, tip_texyid, tip_lang, tip_text) values (-1039, 'portal', 'Sabías que ?', 'texy_help_right'  ,'es', '<p>Las Páginas que se pueden agregar a un Sitio son de tres tipos:<br/><span class="highlight">Estándar</span>. Página agregadora standard.<br/><span class="highlight">Enlace</span>. Referencia a un enlace externo (Sirve para agregar en el Menú del Sitio una sección que sea una página externa).<br/><span class="highlight">Muro</span>. Muro de Fotos del sitio</p>');
	insert into kb_tip (id, tip_area,  tip_title, tip_texyid, tip_lang, tip_text) values (-1040, 'portal', 'Sabías que ?', 'texy_help_right'  ,'es', '<p>Es posible agregar en el Menú del Sitio una Sección que sea un enlace a una página externa.<br/>Para eso se debe crear una página de tipo <span class="highlight">Enlace</span> y agregar en la URL externa en el campo.</p>');
	insert into kb_tip (id, tip_area,  tip_title, tip_texyid, tip_lang, tip_text) values (-1041, 'portal', 'Sabías que ?', 'texy_help_right'  ,'es', '<p>Una página de tipo <span class="highlight">Enlace</span> es un link a una URL externo, por eso no tiene habilitado agregar Areas.</p>');
	insert into kb_tip (id, tip_area,  tip_title, tip_texyid, tip_lang, tip_text) values (-1042, 'portal', 'Sabías que ?', 'texy_help_right'  ,'es', '<p>En el Tab <span class="highlight">Publicación</span> del Editor de Página se puede cambiar el <span class="highlight">Estado</span> (Publicado, Archivado, etc) y el <span class="highlight">Orden</span> en el que se muestran en el Menú del Sitio.</p>');
	insert into kb_tip (id, tip_area,  tip_title, tip_texyid, tip_lang, tip_text) values (-1043, 'portal', 'Sabías que ?', 'texy_help_right'  ,'es', '<p>Al Agregar una <span class="highlight">Página</span>, <span class="highlight">Area</span>, <span class="highlight">Block</span> se agrega en estado <span class="highlight">Borrador</span>, es decir que no es visible para los usuarios.</p>');
	insert into kb_tip (id, tip_area,  tip_title, tip_texyid, tip_lang, tip_text) values (-1044, 'portal', 'Sabías que ?', 'texy_help_right'  ,'es', '<p>El <span class="highlight">Orden de las Areas</span> dentro de la Página se define en el Tab <span class="highlight">Publicación</span> del Editor de Página.</p>');
	insert into kb_tip (id, tip_area,  tip_title, tip_texyid, tip_lang, tip_text) values (-1045, 'portal', 'Sabías que ?', 'texy_help_right'  ,'es', '<p>Es posible publicar un Video de dos formas:<br/>En línea en un <span class="highlight">Player</span> en páginas agregadoras<br/>En una <span class="highlight">página propia</span></p>');
	insert into kb_tip (id, tip_area,  tip_title, tip_texyid, tip_lang, tip_text) values (-1046, 'portal', 'Sabías que ?', 'texy_help_right'  ,'es', '<p>Un Video tiene 1 sóla página de detalle, pero puede publicarse en línea (Player) muchas veces.</p>');
	insert into kb_tip (id, tip_area,  tip_title, tip_texyid, tip_lang, tip_text) values (-1047, 'portal', 'Sabías que ?', 'texy_help_right'  ,'es', '<p>Los <span class="highlight">Videos</span>, <span class="highlight">Audios</span>, <span class="highlight">Galerías de Fotos</span> y <span class="highlight">Muro de Fotos</span> pueden publicarse en un Player (Visor) en línea.</p>');
	insert into kb_tip (id, tip_area,  tip_title, tip_texyid, tip_lang, tip_text) values (-1048, 'portal', 'Sabías que ?', 'texy_help_right'  ,'es', '<p>El Block <span class="highlight">Cumpleaños</span> es un Block a medida del Santander Río, que toma los datos de los Sistemas internos del Banco.</p>');

	
	commit;
	
	insert into kb_tip (id, tip_area,  tip_title, tip_texyid, tip_lang, tip_text) values (-1049, 'portal', 'Sabías que ?', 'texy_help_right'  ,'es', '<p>El Block <span class="highlight">Multiblock</span> es un Block que contiene otros blocks, y se puede presentar de tres modos: tabs horizontales tabs verticales, y "acordeón" con tabs expandibles.</p>');
	insert into kb_tip (id, tip_area,  tip_title, tip_texyid, tip_lang, tip_text) values (-1050, 'portal', 'Sabías que ?', 'texy_help_right'  ,'es', '<p>El Menú <span class="highlight">Hamburguesa</span> desplegable arriba a la izquierda es un menú Global, que incorpora las demás acciones del encabezado a medida que la pantalla se achica. Esto permite utilizar la aplicación en teléfonos y otros dispositivos móviles.</p>');
	insert into kb_tip (id, tip_area,  tip_title, tip_texyid, tip_lang, tip_text) values (-1051, 'portal', 'Sabías que ?', 'texy_help_left'  ,'es', '<p>Mis Favoritos contiene tres tabs: 1. Los Sitios y Aplicaciones que marcaste como Favoritos<br />2. Los Sitios del Indice de Sitios de todos los tipos excepto Aplicación (Blog, Area, Temático, etc.).<br /> 3. Las Aplicaciones.</p>');
	insert into kb_tip (id, tip_area,  tip_title, tip_texyid, tip_lang, tip_text) values (-1052, 'portal', 'Sabías que ?', 'texy_help_left'  ,'es', '<p>Las imágenes en las páginas se muestran centradas y  <span class="highlight">del máximo tamaño posible sin deformarse</span>, es decir que siempre se verán de una de estas cuatro formas: <br/>1. Alto máximo y sobrando en el ancho, <br/>2. Ancho máximo y sobrando en alto, <br/>3. Sobrando en alto y ancho (cuando la imagen es más chica que el visor en ambas direcciones),  <br/>4. Ancho y alto máximo (cuando la imagen es exactamente igual al visor).</p>');
	insert into kb_tip (id, tip_area,  tip_title, tip_texyid, tip_lang, tip_text) values (-1053, 'portal', 'Sabías que ?', 'texy_help_left'  ,'es',  '<p>Al publicar la Vista de un Texto se puede seleccionar que las imágenes se muestren en un Visor grande bajo en título y abstract.</p>');
	insert into kb_tip (id, tip_area,  tip_title, tip_texyid, tip_lang, tip_text) values (-1054, 'portal', 'Sabías que ?', 'texy_help_left'  ,'es',  '<p>Al Reportar un contenido como desactualizado, se envía el mensaje por email al administrador del Sitio.</p>');
	insert into kb_tip (id, tip_area,  tip_title, tip_texyid, tip_lang, tip_text) values (-1055, 'portal', 'Sabías que ?', 'texy_help_left'  ,'es',  '<p>En el Muro se cargan las fotos de a 100, mediante el enlace <span class="highlight">Cargar Más</span> el usuario puede traer el siguiente grupo. Esto es así por temas de performance.</p>');
	insert into kb_tip (id, tip_area,  tip_title, tip_texyid, tip_lang, tip_text) values (-1056, 'portal', 'Sabías que ?', 'texy_help_left'  ,'es',  '<p>El sistema asigna una foto genérica a cada usuario al ser dado de alta. Esa foto la podés cambiar en cualquier momento en <span class="highlight">Mi Cuenta.</span></p>');


	commit;
	
	#----------------------------
	# Tips

	INSERT INTO users(id, username, state,  firstname, lastname, password, password_md5, lastModifiedUser) VALUES(10, 'root@kbee', 1, '' , 'root', '197d6efe2ed5c94f0715f672af0ba7f9', 'root', null);
	INSERT INTO domain(id, lastmodifieduser, state, enabled, name, type, service, quota, istemplate, description, organization) VALUES(1, 10, 1, 1, 'kbee', 4, 1, 0, 0, 'kbee Factory', 'Santander Río');
	INSERT INTO kb_domain_settings(domain_id, category, values_json) VALUES(1, 'kbee', '{"emailServiceNoReply":"noreply@santanderrio.com.ar","emailServiceStatus":"enabled"}');

	#----------------------------

		INSERT INTO principal(id, lastmodifieduser, domain_id) VALUES(10, 10, 1);

		commit;
		
		-- Groups (2-19)
		INSERT INTO principal(id, lastmodifieduser, domain_id) VALUES(5, 10, 1);
		INSERT INTO kgroup(id, name, canonical) VALUES(5, 'User', 1);
		
		INSERT INTO principal(id, lastmodifieduser, domain_id) VALUES(6, 10, 1);
		INSERT INTO kgroup(id, name, canonical) VALUES(6, 'ROLE_ROOT', 1);
		
		INSERT INTO principal(id, lastmodifieduser, domain_id) VALUES(7, 10, 1);
		INSERT INTO kgroup(id, name, canonical) VALUES(7, 'Domain Admin', 1);
		
		INSERT INTO principal(id, lastmodifieduser, domain_id) VALUES(8, 10, 1);
		INSERT INTO kgroup(id, name, canonical) VALUES(8, 'Workspace', 1);
		
		INSERT INTO principal(id, lastmodifieduser, domain_id) VALUES(9, 10, 1);
		INSERT INTO kgroup(id, name, canonical) VALUES(9, 'Content Base', 1);
		
		INSERT INTO principal(id, lastmodifieduser, domain_id) VALUES(20, 10, 1);
		INSERT INTO kgroup(id, name, canonical) VALUES(20, 'Archive', 1);
		
		INSERT INTO principal(id, lastmodifieduser, domain_id) VALUES(21, 10, 1);
		INSERT INTO kgroup(id, name, canonical) VALUES(21, 'Security', 1);
		
		INSERT INTO principal(id, lastmodifieduser, domain_id) VALUES(22, 10, 1);
		INSERT INTO kgroup(id, name, canonical) VALUES(22, 'Monitor', 1);
		
		INSERT INTO principal(id, lastmodifieduser, domain_id) VALUES(23, 10, 1);
		INSERT INTO kgroup(id, name, canonical) VALUES(23, 'Workflow', 1);
		
		INSERT INTO principal(id, lastmodifieduser, domain_id) VALUES(24, 10, 1);
		INSERT INTO kgroup(id, name, canonical) VALUES(24, 'Templates', 1);
		
		INSERT INTO principal(id, lastmodifieduser, domain_id) VALUES(25, 10, 1);
		INSERT INTO kgroup(id, name, canonical) VALUES(25, 'Information Model', 1);

	commit;

	-- Root user (1)
	INSERT INTO entity(id, lastmodifieduser, state, domain_id) VALUES(10, 10, 1, 1);
	INSERT INTO person(entity_id, lastname, phone, email) VALUES(10, 'KBEE Admin', '1234', 'info@novamens.com');
	INSERT INTO profile(id, lastmodifieduser, entity, domain_id) VALUES(10, 10, 10, 1);
	INSERT INTO userprofile(id, user_id, confidencelevel) VALUES(10, 10, 99);

	INSERT INTO kgroupmember(kgroup, principal) VALUES(5, 10);
	INSERT INTO kgroupmember(kgroup, principal) VALUES(6, 10);
	INSERT INTO kgroupmember(kgroup, principal) VALUES(7, 10);

	-- Workflow user (10)

	INSERT INTO users(id, username, state,  firstname, lastname, password, password_md5, lastModifiedUser) VALUES(11, 'workflow@kbee', 1, '' , 'pending', '3b6144f35f3e2f80a1f9446fafc389dd', 'root', 10);
	INSERT INTO principal(id, lastmodifieduser, domain_id) VALUES(11, 10, 1);
	INSERT INTO entity(id, lastmodifieduser, state, domain_id) VALUES(11, 10, 1, 1);
	INSERT INTO person(entity_id, lastname, phone, email) VALUES(11, 'Workflow', '1234', 'info@novamens.com');
	INSERT INTO profile(id, lastmodifieduser, entity, domain_id) VALUES(11, 10, 11, 1);
	INSERT INTO userprofile(id, user_id) VALUES(11, 11);
	
	INSERT INTO kgroupmember(kgroup, principal) VALUES(5, 11);

	INSERT INTO dataset       (id, domain_id, name, type, state, suggester, lastmodifieduser) values(10,1,'Users',4,1, 0,10);

	-- UserSet
	
	INSERT INTO datasetmember (id, lastmodifieduser, state, dataset_id, domain_id, entity_id, type) values(10,10,  1, 10, 1, 10, 3);
	INSERT INTO datasetmember (id, lastmodifieduser, state, dataset_id, domain_id, entity_id,  type) values(11,10, 1, 10, 1, 11, 3);

	commit;

	delete from kb_email_template;
	
	INSERT INTO kb_email_template (id, lastmodifieduser,  state, domain_id, lang, xkey, title, fromstr, subject, strtext) VALUES (1, 10, 1, 1, 'en', 'welcome', 'Welcome to RPDM', '${domain-noreply}' , 'Welcome to RealPage Document Management - ${domain-name}', '<p>${person-displayname},</p><p>Welcome to <b>RealPage Document Management (RP-DOC)</b>!. RP-DOC is an enterprise document management solution designed to meet the enterprise document management needs of any size property management company. RP-DOC allows you store documents for all areas of your business while having the ability to restrict access by user so you can give view only access to investors and auditors or control access by document type. Never lose a document again, RP-DOC superior search functionality will help you find in seconds any document ever stored.</p><p>Your username is: <b>${username}</b></p> <p>To set up your account password, please visit the link below and follow the instructions:</p><p>${url}</p>' );
	INSERT INTO kb_email_template (id, lastmodifieduser,  state, domain_id, lang, xkey, title, fromstr, subject, strtext) VALUES (8, 10, 1, 1, 'en', 'forgot-username', 'Forgot Username','${domain-noreply}'  , 'Username for ${domain-name} - RPDM'  , '<p>${person-displayname},</p><p>We have received your request to send your RPDM username. <br/>We have the following user account associated to the email address and phone:</p><p>Email: ${person-email-address}<br/>Phone: ${person-phone-last-four-digits}</p><p>User Account: ${user-username}</p>');
	INSERT INTO kb_email_template (id, lastmodifieduser,  state, domain_id, lang, xkey, title, fromstr, subject, strtext) VALUES (9, 10, 1, 1, 'en', 'admin-sends-reset-password', 'Password Reset', '${domain-noreply}' , 'Password Reset for ${domain-name} - RPDM', '${person-displayname},<br/><p>The Admin user has sent you this link to reset your account password. Please visit the link below and follow the instructions:</p><p>${url}</p> <p>For security reasons, this link will expire in 30 minutes after your initial request was made.</p>');
	INSERT INTO kb_email_template (id, lastmodifieduser,  state, domain_id, lang, xkey, title, fromstr, subject, strtext) VALUES (7, 10, 1, 1, 'en', 'forgot-password',   'Password Reset'  , '${domain-noreply}', 'Password Reset for ${domain-name} - RPDM','<p>${person-displayname},</p><p>We have received your request to reset your account password. Please visit the link below and follow the instructions to reset your password:</p><p>${url}</p><p>For security reasons, this link will expire in 30 minutes after your initial request was made. If you did not request to reset your password, you can safely ignore this email.</p>');
	INSERT INTO kb_email_template (id, lastmodifieduser,  state, domain_id, lang, xkey, title, fromstr, subject, strtext) VALUES (2, 10, 1, 1, 'en', 'send-email', 		 'Send by Email'   , '${from}',      '${title} - RPDM'  , '${text}');
	INSERT INTO kb_email_template (id, lastmodifieduser,  state, domain_id, lang, xkey, title, fromstr, subject, strtext) VALUES (6, 10, 1, 1, 'en', 'alert-rule-publish',  'Alert Notification'  , '${domain-noreply}', '${event-name} - ${file-title} - RPDM'  , '<p>${person-display-name} has published: ${file-title}</p> <p>Please go to Library at ${url} to retrieve additional information about this file.</p>');
	INSERT INTO kb_email_template (id, lastmodifieduser,  state, domain_id, lang, xkey, title, fromstr, subject, strtext) VALUES (3, 10, 1, 1, 'en', 'assign-task',  'New Task'  , '${from}'  , '${title} - ${task-name} - RPDM'  , '<p>${from-displayname} has assigned the following task:</p><p>Task: ${task-name} <br/> File: ${title}.</p><p>Please go to <a href="${url}">My Tasks</a> to retrieve additional information about this file.</p><p>Comment:<br/>${comment}</p>');
	INSERT INTO kb_email_template (id, lastmodifieduser,  state, domain_id, lang, xkey, title, fromstr, subject, strtext) VALUES (4, 10, 1, 1, 'en', 'reassign-task-receiver',   'Reassign task receiver'          , '${from}'  , '${task-name} - ${title} - RPDM'  , '<p>{from-displayname} has assigned the following task: ${task} - ${title}.<br/>Please go to My Tasks at ${url} to retrieve additional information about this file.</p><p>Comment:<br/>${comment}</p>');
	INSERT INTO kb_email_template (id, lastmodifieduser,  state, domain_id, lang, xkey, title, fromstr, subject, strtext) VALUES (5, 10, 1, 1, 'en', 'reassign-task-former-owner',  'Reassign task former owner'   , '${from}'  , '${task-name} - ${title} - RPDM'  , '<p>{from-displayname} has assigned the following task: ${task} - ${title}.<br/>Please go to My Tasks at ${url} to retrieve additional information about this file.</p><p>Comment:<br/>${comment}</p>');
	INSERT INTO kb_email_template (id, lastmodifieduser,  state, domain_id, lang, xkey, title, fromstr, subject, strtext) VALUES (10, 1, 1, 1, 'en', 'db-export',   'DB Export'  , '${domain-noreply}', 'DB Export for ${domain-name} - RPDM','<p>${person-displayname},</p><p>Your database export is ready.<br />You can download it as a zip file from this link:</p><p>${url}</p><p>For security reasons, this link will expire in 10 days after your initial request was made.</p>');

/*
# ---------------------------------------------------

1. Borrar el contenido del directorio 
./work

2.  copiar las imagenes de backup al directorio padre para su importacion
cp -v home/kbee/idoc/images/backup/* home/kbee/idoc/images

3. Editar content-web.properties
config help.enabled=no

4. Correr Delta DB 
con el Toad

5. Loguearse al dominio kbee
root@kbee / root  
y editar dominio "rio"  
Habilitar Portal en Dominio "rio"

6. Correr comando de imagenes de los usuarios. 
 DataManagement -> Commands
 Poner en parameters el nombre del dominio "rio"

7.  Reindexar y Clean dominio Rio 
  DataManagement -> Reindex Domain "Rio"
  
  Clean type=idoc 
  Clean type=text

# ---------------------------------------------------
#

8. Reformar el Modelo de Informacion 
9. Editar todos los content templates para poner los attributes correctos.

# ---------------------------------------------------


9. Loguearse al dominio Rio root@rio / root y desde la pag. "Sites"
Editar el Sitio Header y Footer Globales: Header y Footer



 

 
   
*/





alter table kb_security_rule add parent_objectid varchar2(48);
alter table kb_security_rule add displaycondition clob;



/*
# ---------------------------------------------------

NOVIEMMBRE 10

alter table domain add isapienabled NUMBER(1) default 1;

alter table person add workposition character varying(256); 

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
 
			
# ----------------------------------------
# 18 Sept
#
alter table kb_contenttemplate add abstract_label 			character varying(128);
alter table kb_contenttemplate add private_notes_label  	character varying(128);
alter table kb_contenttemplate add text_notes_label 		character varying(128);
alter table kb_contenttemplate add text_label 				character varying(128);
alter table kb_contenttemplate add customattributes_label 	character varying(128);



# ----------------------------------------
# 22 Sept
#


#Alter table kfile  add descriptionlastmodifieddate timestamp with time zone DEFAULT now(),
#Alter table kfile  add descriptionlastmodifieduser bigint;

#update      kfile set descriptionlastmodifieduser = (select id from users where username='root@kbee');
#alter table kfile add CONSTRAINT des_user_fk FOREIGN KEY (  descriptionlastmodifieduser) REFERENCES public.users (id) MATCH SIMPLE;

#alter table externalresource add descriptionlastmodifieddate timestamp with time zone DEFAULT now();
#alter table externalresource add descriptionlastmodifieduser bigint;
#update      externalresource set descriptionlastmodifieduser = (select id from users where username='root@kbee');
#alter table externalresource add CONSTRAINT des_user_fk FOREIGN KEY (  descriptionlastmodifieduser) REFERENCES public.users (id) MATCH SIMPLE;

#update kfile set descriptionlastmodifieduser  = (select k.lastmodifieduser from kresource k where k.id=resource_id);
#update kfile set descriptionlastmodifieddate  = (select k.lastmodifieddate from kresource k where k.id=resource_id);

#alter table kfile rename  descriptionlastmodifieduser to uploadeduser;
#alter table kfile rename  descriptionlastmodifieddate to uploadeddate;
--


alter table kfile  add uploadeddate timestamp with time zone DEFAULT sysdate;
alter table kfile  add uploadeduser number(6);
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

alter table kb_classifiertemplate add multiplicity integer default 4;


create table kb_user_note 
(
	id  number(19) NOT NULL,
	user_id number(19) NOT NULL,
	title varchar2(256),
	notetext clob,
	creationDate		TIMESTAMP  WITH TIME ZONE DEFAULT current_timestamp,
	lastmodifieddate 	TIMESTAMP  WITH TIME ZONE DEFAULT current_timestamp,
	lastmodifieduser 	number(19) NOT NULL,
	priority varchar2(24),
	domain_id number(19) NOT NULL,
	CONSTRAINT user_note_id_pk PRIMARY KEY(id),
	CONSTRAINT user_note_user_fk FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
	CONSTRAINT user_note_lmu_id_fk FOREIGN KEY (lastmodifieduser) REFERENCES users (id) ON DELETE SET NULL,
	CONSTRAINT un_domain_fk FOREIGN KEY (domain_id) REFERENCES domain (id)  ON DELETE CASCADE
) TABLESPACE SBD02_DATA;

insert into kb_system_properties (key, value) values ('welcome-note.title', 'What is My Notepad ?');
insert into kb_system_properties (key, value) values ('welcome-note.text', '<p>The Notepad is a panel easily accesible from the toolbar where you can create and manage simple notes.</p><p>Notes can include&nbsp;<a href=\"http://www.realpage.com\">links</a> and formats like <strong>bold</strong> or <em>italic</em>.</p><p>&nbsp;</p><p>Notes are private, no one else can read or edit them.</p>');



CREATE TABLE kb_work_note
(
	id  number(19) NOT NULL,
	user_id number(19) NOT NULL,
    title varchar2(256),
    notetext clob,
	creationDate		TIMESTAMP  WITH TIME ZONE DEFAULT current_timestamp,
	lastmodifieddate 	TIMESTAMP  WITH TIME ZONE DEFAULT current_timestamp,
	lastmodifieduser 	number(19) NOT NULL,
    priority varchar2(24),
	domain_id number(19) NOT NULL,
	CONSTRAINT wn_note_id_pk PRIMARY KEY(id),
	CONSTRAINT wn_note_user_fk FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
	CONSTRAINT wn_note_lmu_id_fk FOREIGN KEY (lastmodifieduser) REFERENCES users (id) ON DELETE SET NULL,
	CONSTRAINT wn_domain_fk FOREIGN KEY (domain_id) REFERENCES domain (id)  ON DELETE CASCADE
) TABLESPACE SBD02_DATA;


CREATE TABLE kb_work_note_user_read
(
	id  number(19) NOT NULL,
	work_note_id number(19) NOT NULL,
	user_id number(19) NOT NULL,
    readdate TIMESTAMP  WITH TIME ZONE DEFAULT current_timestamp,
	CONSTRAINT wnur_id_pk PRIMARY KEY (id),
    CONSTRAINT wnur_uid_fk FOREIGN KEY  (user_id)  REFERENCES users	(id) ON DELETE CASCADE,
	CONSTRAINT wnur_wnid_fk FOREIGN KEY (work_note_id)  REFERENCES kb_work_note (id) ON DELETE CASCADE
) TABLESPACE SBD02_DATA;

alter table kb_notification add notification_type NUMBER(2) default 10 NOT NULL;
alter table kb_notification add work_note_id NUMBER(19);
alter table kb_notification add constraint wn_fk foreign key (work_note_id) references kb_work_note (id) on delete cascade;
alter table kb_notification drop column contentid;

























	 





	 

																					
	  
		  


	 



























