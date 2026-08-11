

INSERT INTO ContentClass(id, enabled, name, javaclass)            VALUES('KbeeIDoc',                TRUE, 'IDoc','com.novamens.kbee.content.document.KbeeIDoc');
INSERT INTO ContentClass(id, enabled, name, javaclass)            VALUES('KbeeOrganizationalText',  TRUE, 'OrganizationalText','com.novamens.kbee.content.communication.KbeeOrganizationalText');
INSERT INTO ContentClass(id, enabled, name, javaclass)            VALUES('KbeeQuestion',            TRUE, 'Question', 'com.novamens.kbee.content.questionanswer.KbeeQuestion');
INSERT INTO ContentClass(id, enabled, name, javaclass)            VALUES('KbeeLinkView',            TRUE, 'LinkView', 'com.novamens.kbee.portal.model.KbeeViewBKLink');

INSERT INTO ContentClass(id, enabled, name, javaclass)            VALUES('KbeeView', 	            TRUE, 'KbeeView', 'com.novamens.kbee.portal.model.KbeeViewDetailContent');
INSERT INTO ContentClass(id, enabled, name, javaclass, indexable) VALUES('KbeeContent',             TRUE, 'Content','com.novamens.kbee.content.base.KbeeContent', false);
INSERT INTO ContentClass(id, enabled, name, javaclass, indexable) VALUES('KbeeAnswer',              TRUE, 'Answer','com.novamens.kbee.content.questionanswer.KbeeAnswer', false);
INSERT INTO ContentClass(id, enabled, name, javaclass, indexable) VALUES('KbeeComment',             TRUE, 'Comment','com.novamens.kbee.content.social.KbeeComment', false);
																

--- KBEE Domain
-- Kbee Root pwd: 
INSERT INTO users(id, username, state,  firstname, lastname, password, password_md5, lastModifiedUser) VALUES(1, 'root@kbee', 1, '' , 'root', '{MD5}{1}3b6144f35f3e2f80a1f9446fafc389dd', 'root', null);
INSERT INTO domain(id, lastmodifieduser, state, enabled, name, type, service, quota, istemplate, description, organization) VALUES(1, 1, 1, TRUE, 'kbee', 4, 1, 0, FALSE, 'RPDM Factory', 'KBEE');
INSERT INTO kb_domain_settings(id, domain_id, category, values_json) VALUES(1, 1, 'kbee', '{"emailServiceNoReply":"noreply@realpage.com","emailServiceStatus":"enabled"}');

--- Root user
INSERT INTO principal(id, lastmodifieduser, domain_id) VALUES(1, 1, 1);

-- Groups kbee (user, domain-admin, workflow)
INSERT INTO principal(id, lastmodifieduser, domain_id) VALUES(2, 1, 1);
INSERT INTO kgroup(id, name, canonical) VALUES(2, 'user', TRUE);

INSERT INTO principal(id, lastmodifieduser, domain_id) VALUES(4, 1, 1);
INSERT INTO kgroup(id, name, canonical) VALUES(4, 'domain-admin', TRUE);

INSERT INTO principal(id, lastmodifieduser, domain_id) VALUES(10, 1, 1);
INSERT INTO kgroup(id, name, canonical) VALUES(10, 'workflow', TRUE);

-- Root user (1)
INSERT INTO entity(id, lastmodifieduser, state, domain_id) VALUES(1, 1, 1, 1);
INSERT INTO person(entity_id, lastname, phone, email) VALUES(1, 'Factory Root', '1234', 'atolomei@novamens.com');
INSERT INTO profile(id, lastmodifieduser, entity, domain_id) VALUES(1, 1, 1, 1);
INSERT INTO userprofile(id, user_id, confidencelevel) VALUES(1, 1, 99);

INSERT INTO kgroupmember(kgroup, principal) VALUES(2, 1);
INSERT INTO kgroupmember(kgroup, principal) VALUES(4, 1);


-- Workflow user (20)
INSERT INTO users(id, username, state,  firstname, lastname, password, password_md5, lastModifiedUser) VALUES(20, 'workflow@kbee', 1, '' , 'pending', '3b6144f35f3e2f80a1f9446fafc389dd', 'root', 1);
INSERT INTO principal(id, lastmodifieduser, domain_id) VALUES(20, 1, 1);
INSERT INTO entity(id, lastmodifieduser, state, domain_id) VALUES(20, 1, 1, 1);
INSERT INTO person(entity_id, lastname, phone, email) VALUES(20, 'Workflow', '1234', 'info@novamens.com');
INSERT INTO profile(id, lastmodifieduser, entity, domain_id) VALUES(20, 1, 20, 1);
INSERT INTO userprofile(id, user_id) VALUES(20, 20);
INSERT INTO kgroupmember(kgroup, principal) VALUES(10, 20);


-- DataSet Users 
INSERT INTO dataset(id, domain_id, name, type, state, suggester, lastmodifieduser) values(1,1,'Users',4,1, false,1);

-- UserSet
INSERT INTO datasetmember (id, lastmodifieduser, state, dataset_id, domain_id, entity_id, type) values(1,1,1,1,1,1,3);
INSERT INTO datasetmember (id, lastmodifieduser, state, dataset_id, domain_id, entity_id,  type) values(2,1,1,1,1,20,3);


-- Email Templates-----------
--
INSERT INTO kb_email_template (id, lastmodifieduser,  state, domain_id, lang, xkey, title, fromstr, subject, strtext) VALUES (1, 1, 1, 1, 'en', 'welcome', 'Welcome to RPDM', '${domain-noreply}' , 'Welcome to RealPage Document Management - ${domain-name}', '<p>${person-displayname},</p><p>Welcome to <b>RealPage Document Management (RP-DOC)</b>!. RP-DOC is an enterprise document management solution designed to meet the enterprise document management needs of any size property management company. RP-DOC allows you store documents for all areas of your business while having the ability to restrict access by user so you can give view only access to investors and auditors or control access by document type. Never lose a document again, RP-DOC superior search functionality will help you find in seconds any document ever stored.</p><p>Your username is: <b>${username}</b></p> <p>To set up your account password, please visit the link below and follow the instructions:</p><p>${url}</p>' );
INSERT INTO kb_email_template (id, lastmodifieduser,  state, domain_id, lang, xkey, title, fromstr, subject, strtext) VALUES (8, 1, 1, 1, 'en', 'forgot-username', 'Forgot Username','${domain-noreply}'  , 'Username for ${domain-name} - RPDM'  , '<p>${person-displayname},</p><p>We have received your request to send your RPDM username. <br/>We have the following user account associated to the email address and phone:</p><p>Email: ${person-email-address}<br/>Phone: ${person-phone-last-four-digits}</p><p>User Account: ${user-username}</p>');
INSERT INTO kb_email_template (id, lastmodifieduser,  state, domain_id, lang, xkey, title, fromstr, subject, strtext) VALUES (9, 1, 1, 1, 'en', 'admin-sends-reset-password', 'Password Reset', '${domain-noreply}' , 'Password Reset for ${domain-name} - RPDM', '${person-displayname},<br/><p>The Admin user has sent you this link to reset your account password. Please visit the link below and follow the instructions:</p><p>${url}</p> <p>For security reasons, this link will expire in 30 minutes after your initial request was made.</p>');
INSERT INTO kb_email_template (id, lastmodifieduser,  state, domain_id, lang, xkey, title, fromstr, subject, strtext) VALUES (7, 1, 1, 1, 'en', 'forgot-password',   'Password Reset'  , '${domain-noreply}', 'Password Reset for ${domain-name} - RPDM','<p>${person-displayname},</p><p>We have received your request to reset your account password. Please visit the link below and follow the instructions to reset your password:</p><p>${url}</p><p>For security reasons, this link will expire in 30 minutes after your initial request was made. If you did not request to reset your password, you can safely ignore this email.</p>');
INSERT INTO kb_email_template (id, lastmodifieduser,  state, domain_id, lang, xkey, title, fromstr, subject, strtext) VALUES (2, 1, 1, 1, 'en', 'send-email', 		 'Send by Email'   , '${from}',      '${title} - RPDM'  , '${text}');
INSERT INTO kb_email_template (id, lastmodifieduser,  state, domain_id, lang, xkey, title, fromstr, subject, strtext) VALUES (6, 1, 1, 1, 'en', 'alert-rule-publish',  'Alert Notification'  , '${domain-noreply}', '${event-name} - ${file-title} - RPDM'  , '<p>${person-display-name} has published: ${file-title}</p> <p>Please go to Library at ${url} to retrieve additional information about this file.</p>');
INSERT INTO kb_email_template (id, lastmodifieduser,  state, domain_id, lang, xkey, title, fromstr, subject, strtext) VALUES (3, 1, 1, 1, 'en', 'assign-task',  'New Task'  , '${from}'  , '${title} - ${task-name} - RPDM'  , '<p>${from-displayname} has assigned the following task:</p><p>Task: ${task-name} <br/> File: ${title}.</p><p>Please go to <a href="${url}">My Tasks</a> to retrieve additional information about this file.</p><p>Comment:<br/>${comment}</p>');
INSERT INTO kb_email_template (id, lastmodifieduser,  state, domain_id, lang, xkey, title, fromstr, subject, strtext) VALUES (4, 1, 1, 1, 'en', 'reassign-task-receiver',   'Reassign task receiver'          , '${from}'  , '${task-name} - ${title} - RPDM'  , '<p>{from-displayname} has assigned the following task: ${task} - ${title}.<br/>Please go to My Tasks at ${url} to retrieve additional information about this file.</p><p>Comment:<br/>${comment}</p>');
INSERT INTO kb_email_template (id, lastmodifieduser,  state, domain_id, lang, xkey, title, fromstr, subject, strtext) VALUES (5, 1, 1, 1, 'en', 'reassign-task-former-owner',  'Reassign task former owner'   , '${from}'  , '${task-name} - ${title} - RPDM'  , '<p>{from-displayname} has assigned the following task: ${task} - ${title}.<br/>Please go to My Tasks at ${url} to retrieve additional information about this file.</p><p>Comment:<br/>${comment}</p>');

insert into kb_system_properties (key, value) values ('cabinet_standard',	'Enterprise');
insert into kb_system_properties (key, value) values ('cabinet_templates',	'Templates');
insert into kb_system_properties (key, value) values ('cabinet_kbase',		'Knowledge Base');
insert into kb_system_properties (key, value) values ('cabinet_external',	'OneSite');
insert into kb_system_properties (key, value) values ('cabinet_all',		'All');


-- 
-- DB Server info Load Avergage, Mem
--
CREATE EXTENSION file_fdw;
CREATE SERVER fileserver FOREIGN DATA WRAPPER file_fdw;
CREATE FOREIGN TABLE loadavg (one text, five text, fifteen text, scheduled text, pid text) SERVER fileserver OPTIONS (filename '/proc/loadavg', format 'text', delimiter ' ');
CREATE FOREIGN TABLE meminfo (stat text, value text) SERVER fileserver OPTIONS (filename '/proc/meminfo', format 'csv', delimiter ':');


--
--
-- ContentTemplate File
-- INSERT INTO kb_ContentTemplate(id, lastmodifieduser, state, domain_id, contentclass_id, name) VALUES(1, 1, 1, 1, 'KbeeIDoc', 'File');

-- INSERT INTO principal(id, lastmodifieduser, domain_id) VALUES(3, 1, 1);
-- INSERT INTO kgroup(id, name, canonical) VALUES(3, 'ROLE_ROOT', TRUE);
	
-- INSERT INTO principal(id, lastmodifieduser, domain_id) VALUES(5, 1, 1);
-- INSERT INTO kgroup(id, name, canonical) VALUES(5, 'mytasks', TRUE);
	
-- INSERT INTO principal(id, lastmodifieduser, domain_id) VALUES(6, 1, 1);
-- INSERT INTO kgroup(id, name, canonical) VALUES(6, 'enterprise', TRUE);
	
-- INSERT INTO principal(id, lastmodifieduser, domain_id) VALUES(7, 1, 1);
-- INSERT INTO kgroup(id, name, canonical) VALUES(7, 'archive', TRUE);

-- INSERT INTO principal(id, lastmodifieduser, domain_id) VALUES(8, 1, 1);
-- INSERT INTO kgroup(id, name, canonical) VALUES(8, 'security', TRUE);
	
-- INSERT INTO principal(id, lastmodifieduser, domain_id) VALUES(9, 1, 1);
-- INSERT INTO kgroup(id, name, canonical) VALUES(9, 'auditor', TRUE);
	
-- INSERT INTO principal(id, lastmodifieduser, domain_id) VALUES(10, 1, 1);
-- INSERT INTO kgroup(id, name, canonical) VALUES(10, 'workflow', TRUE);
	
--INSERT INTO principal(id, lastmodifieduser, domain_id) VALUES(11, 1, 1);
--INSERT INTO kgroup(id, name, canonical) VALUES(11, 'templates', TRUE);
	
-- INSERT INTO principal(id, lastmodifieduser, domain_id) VALUES(12, 1, 1);
-- INSERT INTO kgroup(id, name, canonical) VALUES(12, 'information-model', TRUE);
