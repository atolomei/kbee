delete from contentresource where content_id in (select id from content where domain_id=2000);
delete from content where domain_id=2000;
delete from wf_launcher where domain_id=2000;
delete from wf_process where procedure_id in (select id from wf_procedure where domain_id=2000);
delete from wf_procedure where domain_id=2000;
delete from kb_contentattribute where contenttemplate_id in (select id from kb_contenttemplate where domain_id=2000);

delete from kb_relation_template where targettemplate_id in (select id from kb_contenttemplate where domain_id=2000);

delete from kb_contenttemplate where domain_id=2000;
delete from kb_attributetemplate where attribute_id in (select id from kb_attribute where domain_id=2000);
update kb_role set classifier_id=null where domain_id=2000;

delete from kb_ds_element_template where dataset_id in (select id from dataset where domain_id=2000);


delete from datasetclassifier where dataset_id in (select id from dataset where domain_id=2000);

delete from memberclassification where classifier_id in (select id from kb_classifier where dataset_id in (select id from dataset where domain_id=2000));

delete from kb_classifier where domain_id=2000;


delete from kb_classifier where dataset_id in (select id from dataset where domain_id=2000);


delete from kb_attribute where domain_id=2000;
delete from kb_member_role where role_id in (select id from kb_role where domain_id=2000);
delete from kb_user_role where role_id in (select id from kb_role where domain_id=2000);
delete from datasetmember where domain_id=2000;

delete from datasetmember where dataset_id in (select id from dataset where domain_id=2000);

delete from dataset where domain_id=2000 and type<>4;
delete from kb_template_resource_tag where tag_id in (select id from kb_resource_tag where domain_id=2000);
delete from kb_resource_tag where domain_id=2000;
delete from datasetmember where domain_id=2000;
delete from kb_user_role where userprofile_id in (select id from profile where domain_id=2000);
delete from userprofile  where id in (select id from profile where domain_id=2000);
delete from profile  where domain_id=2000;
delete from person where entity_id in (select id from entity  where domain_id=2000);
delete from entity  where domain_id=2000;
delete from kb_role where domain_id=2000;

delete from kb_security_rule where domain_id=2000;
delete from kgroup where id in (select id from principal where domain_id=2000);
update kfile set uploadeduser=null where resource_id in (select id from kresource where domain_id=2000);
update kresource set lastmodifieduser=1, domain_id=null where domain_id=2000;
update principal set lastmodifieduser=1 where domain_id=2000;

delete from kb_email_template where domain_id=2000;

delete from po_area where po_id in (select id from po_portalobject where domain_id=2000);
delete from po_portalobject where domain_id=2000;

delete from kb_facet_wrapper where domain_id=2000;
delete from kb_cabinet where domain_id=2000;

delete from users where id in (select id from principal where domain_id=2000);
delete from principal where domain_id=2000;
delete from domain where id=2000;
delete from logevent where event_domain_id=2000;
delete from kb_import_data where local_domain ='demo'