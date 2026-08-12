
------
alter table content add column external_id_tmp character varying(64);
update content set checkindate = lastmodifieddate where checkindate is null and external_id is not null and ishead=true;
update content set external_id_tmp = external_id where external_id is not null; 


------ Pre start

update content CO set wc_site_id_str =  (select DM.strvalue from content C, kb_classifier CL, dataset D, datasetmember DM, classification F where  lower(CL.name)='site id' and C.id=F.content_id and CL.dataset_id=D.id and CL.id=F.classifier_id and F.datasetmember_id=DM.id and C.id=CO.id) where  CO.external_id is not null and CO.lastmodifieddate <   '2018-07-01';
update content CO set wc_site_id_str =  (select DM.strvalue from content C, kb_classifier CL, dataset D, datasetmember DM, classification F where  lower(CL.name)='site id' and C.id=F.content_id and CL.dataset_id=D.id and CL.id=F.classifier_id and F.datasetmember_id=DM.id and C.id=CO.id) where  CO.external_id is not null and CO.lastmodifieddate >=  '2018-07-01' and CO.lastmodifieddate < '2018-08-01';
update content CO set wc_site_id_str =  (select DM.strvalue from content C, kb_classifier CL, dataset D, datasetmember DM, classification F where  lower(CL.name)='site id' and C.id=F.content_id and CL.dataset_id=D.id and CL.id=F.classifier_id and F.datasetmember_id=DM.id and C.id=CO.id) where  CO.external_id is not null and CO.lastmodifieddate >=  '2018-08-01' and CO.lastmodifieddate < '2018-08-15';
update content CO set wc_site_id_str =  (select DM.strvalue from content C, kb_classifier CL, dataset D, datasetmember DM, classification F where  lower(CL.name)='site id' and C.id=F.content_id and CL.dataset_id=D.id and CL.id=F.classifier_id and F.datasetmember_id=DM.id and C.id=CO.id) where  CO.external_id is not null and CO.lastmodifieddate >=  '2018-08-15';

update content CO set wc_pmc_id_str  =  (select DM.strvalue from content C, kb_classifier CL, dataset D, datasetmember DM, classification F where  lower(CL.name)='pmc id' and C.id=F.content_id and CL.dataset_id=D.id and CL.id=F.classifier_id and F.datasetmember_id=DM.id and C.id=CO.id)   where  CO.external_id is not null and CO.lastmodifieddate <   '2018-07-01';
update content CO set wc_pmc_id_str  =  (select DM.strvalue from content C, kb_classifier CL, dataset D, datasetmember DM, classification F where  lower(CL.name)='pmc id' and C.id=F.content_id and CL.dataset_id=D.id and CL.id=F.classifier_id and F.datasetmember_id=DM.id and C.id=CO.id)   where  CO.external_id is not null and CO.lastmodifieddate >=  '2018-07-01' and CO.lastmodifieddate < '2018-08-01';
update content CO set wc_pmc_id_str  =  (select DM.strvalue from content C, kb_classifier CL, dataset D, datasetmember DM, classification F where  lower(CL.name)='pmc id' and C.id=F.content_id and CL.dataset_id=D.id and CL.id=F.classifier_id and F.datasetmember_id=DM.id and C.id=CO.id)   where  CO.external_id is not null and CO.lastmodifieddate >=  '2018-08-01' and CO.lastmodifieddate < '2018-08-15';
update content CO set wc_pmc_id_str  =  (select DM.strvalue from content C, kb_classifier CL, dataset D, datasetmember DM, classification F where  lower(CL.name)='pmc id' and C.id=F.content_id and CL.dataset_id=D.id and CL.id=F.classifier_id and F.datasetmember_id=DM.id and C.id=CO.id)   where  CO.external_id is not null and CO.lastmodifieddate >=  '2018-08-15';



-- After Tibco Stops
--------------------
update content set external_id_tmp   = external_id where external_id is not null and lastmodifieddate>(now() - interval '120 minutes')::timestamp;
update content CO set wc_site_id_str =  (select DM.strvalue from content C, kb_classifier CL, dataset D, datasetmember DM, classification F where  lower(CL.name)='site id' and C.id=F.content_id and CL.dataset_id=D.id and CL.id=F.classifier_id and F.datasetmember_id=DM.id and C.id=CO.id) where  CO.external_id is not null and  CO.lastmodifieddate>(now() - interval '120 minutes')::timestamp;
update content CO set wc_pmc_id_str  =  (select DM.strvalue from content C, kb_classifier CL, dataset D, datasetmember DM, classification F where  lower(CL.name)='pmc id' and C.id=F.content_id and CL.dataset_id=D.id and CL.id=F.classifier_id and F.datasetmember_id=DM.id and C.id=CO.id)   where  CO.external_id is not null and CO.lastmodifieddate>(now() - interval '120 minutes')::timestamp;


-- update external_ids
update content CO set external_id  =  concat( trim( both ' ' from wc_pmc_id_str),'-', trim(both ' ' from wc_site_id_str) ,'-',external_id) where external_id is not null and wc_site_id_str is not null and wc_pmc_id_str is not null;

----------
   
-- VALIDATION 
-------------
-- Visual
select C.title, C.external_id, CL.name, C.wc_pmc_id_str, C.wc_site_id_str, DM.strvalue from content C, kb_classifier CL, dataset D, datasetmember DM, classification F where  C.id=F.content_id and CL.dataset_id=D.id and CL.id=F.classifier_id and F.datasetmember_id=DM.id and C.external_id is not null and (lower(CL.name)='site id' or lower(CL.name)='pmc id') limit 100;
 

-- Si esta todo bien da 0
select C.id, C.title, C.wc_site_id_str, CL.name, DM.strvalue from content C, kb_classifier CL, dataset D, datasetmember DM, classification F where  lower(CL.name)='site id' and C.wc_site_id_str is not null and C.id=F.content_id and CL.dataset_id=D.id and CL.id=F.classifier_id and F.datasetmember_id=DM.id and C.wc_site_id_str!=DM.strvalue;

-- Si esta todo bien lista todo
select C.id, C.title, C.wc_site_id_str, CL.name, DM.strvalue from content C, kb_classifier CL, dataset D, datasetmember DM, classification F where  lower(CL.name)='site id' and C.wc_site_id_str is not null and C.id=F.content_id and CL.dataset_id=D.id and CL.id=F.classifier_id and F.datasetmember_id=DM.id and C.wc_site_id_str==DM.strvalue limit 100;


-- CLASSIFIERS OF A CONTENT (id=50928) (C.wc_pmc_id_str, C.wc_site_id_str deben ser iguales a las filas qe dicen Site id y Pmc ID respect.)
select C.id, C.title, CL.name, C.wc_pmc_id_str, C.wc_site_id_str, DM.strvalue from content C, kb_classifier CL, dataset D, datasetmember DM, classification F where  C.id=F.content_id and CL.dataset_id=D.id and CL.id=F.classifier_id and F.datasetmember_id=DM.id and C.id=50928




