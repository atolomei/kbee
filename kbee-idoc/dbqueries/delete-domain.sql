

-- CONTENT (CLASSIFICATION, contentproperties, property)

				
delete from kp_tip 			where domain_id in (select id from content where domain_id= (select_id from domain where name='windsor'));


-- CONTENT
delete from contentresource where content_id in (select id from content where domain_id= (select_id from domain where name='windsor'));
delete from contentstat		where content_id in (select id from content where domain_id= (select_id from domain where name='windsor'));


delete from idocsectionresource	where section_id in (select id from idocsection where idoc_id in (select content_id from content where where domain_id= (select_id from domain where name='windsor')));
delete from idocsection			idoc_id in (select content_id from content where where domain_id= (select_id from domain where name='windsor'));
delete from idoc 				where domain_id= (select_id from domain where name='windsor');


-- kb_vote CONTENT

delete from content 			where domain_id= (select_id from domain where name='windsor');
delete from api_logevent 		where domain_id= (select_id from domain where name='windsor');
delete from logevent 			where domain_id= (select_id from domain where name='windsor');


-- RESOURCE
delete from externalresource		where domain_id= (select_id from domain where name='windsor');


-- DATASETMEMBER
delete from datasetmember 		where domain_id= (select_id from domain where name='windsor');


-- SECURITY
-- kb_user_property USER
-- kb_user_note USER
-- kb_user_role USER
-- savedquery USERPROFILE

---
delete from entity 	 		where domain_id= (select_id from domain where name='windsor');



-- MODEL
classifiercontent
contentclass

delete from datasetclassifier	where dataset_id in  select (id from dataset where domain_id= (select_id from domain where name='windsor'));				
delete from dataset 	 		where domain_id= (select_id from domain where name='windsor');

delete from kb_attribute 	 		where domain_id= (select_id from domain where name='windsor');
delete from kb_classifier 		where domain_id= (select_id from domain where name='windsor');
delete from kb_contenttemplate	where domain_id= (select_id from domain where name='windsor');
			
			


			

-- RULES
delete from kb_enotirule 	where domain_id= (select_id from domain where name='windsor');

-- domain

delete from kb_domain_settings 	where domain_id= (select_id from domain where name='windsor');
			
-- kb_usage_stat DOMAIN


delete from kb_comment 			where domain_id= (select_id from domain where name='windsor');
delete from kb_cabinet 			where domain_id= (select_id from domain where name='windsor');



delete from kb_email_template 	where domain_id= (select_id from domain where name='windsor');
delete from kb_domain_settings	where domain_id= (select_id from domain where name='windsor');
delete from domain			 	where domain_id= (select_id from domain where name='windsor');














-------
008
006
007

