-- Create Database
------------------------------
createdb -Eutf8 idoc-windsor

-- more paramteres  (user posgres, server localhost, -W to enter password)
createdb -U postgres -W -h localhost -Eutf8 idoc-windsor


-- dump/import database
----------------------------------------
pgdump -U postgres -h localhost -f idoc-windsor-2018-08-12.sql idoc-windsor

-- import database
psql -f idoc-windsor-2018-08-12.sql idoc-windsor	


-- psql client 
--------------------
psql idoc-windsor
psql -U postgres -W idoc-windsor
psql -U postgres -W -h localhost idoc-windsor

-- Vacuum (inside pasql)
vacuum analyze verbose;

-- Full Vacuum (reduces db size)
vacuum full analyze verbose;


-- Sample Queries
----------------------------

-- LIST OF USERS WITH THEIR DOMAIN

select PE.firstname, PE.lastname, U.username, D.id, D.name from entity E, profile P, userprofile  UP, Person PE, Users U, Domain D where  (D.id=P.domain_id) and (U.id=UP.user_id) and (UP.id=P.id) and (E.id=P.entity) and (PE.entity_id=E.id);


-- CLASSIFIERS OF A CONTENT (id=50928)
select C.id, C.title, CL.name, DM.strvalue from content C, kb_classifier CL, dataset D, datasetmember DM, classification F where  C.id=F.content_id and CL.dataset_id=D.id and CL.id=F.classifier_id and F.datasetmember_id=DM.id and C.id=50928


-- ATTRIBUTES OF A CONTENT (id=50928)
select C.id, C.title, CL.name, DM.strvalue from content C, kb_classifier CL, dataset D, datasetmember DM, classification F where  C.id=F.content_id and CL.dataset_id=D.id and CL.id=F.classifier_id and F.datasetmember_id=DM.id and C.id=50928

-- KB USAGE STATISTICS
select date(ts) as Day, D.name, hard_disk_usage as bytes, contents, resources from kb_usage_stat H, domain D where H.domain_id=D.id order by D.id, Day;


--- LIST OF USERNAME, GROUP NAME FOR ALL HYDER SUERS
select U.username, G.name  from kgroup G, users U, principal P, kgroupmember KM where P.id=U.id and G.id=KM.kgroup and KM.principal=U.id and U.username like '%@hyder';


--- TASKS
select U.username Person, count(*) Tasks from content C, users U where U.id=C.workspace and workspace>0  and C.domain_id=(select id from domain where name='hyder')  group by (workspace, U.id) order by U.username;


-- LIST of CONTENTS PUBLISHED BY ARASCON after 1 May 2018
select title, date(checkindate) from content where ishead=true and lastmodifieduser=(select id from users where username='arascon@hyder') and date(checkindate)>='2018-05-01' order by checkindate;


-- Windsor Compliance Reports
select m.strvalue as property, count(*) as files,sum(CASE WHEN submissions=1 and not running and (approved or perfect) THEN 1 ELSE 0 END) as a1s ,
cast(sum(CASE WHEN approved and not running THEN 1 ELSE 0 END) as float)/count(*)*100  as approved, cast(sum(CASE WHEN perfect and not running THEN 1 ELSE 0 END) as float)/count(*)*100  as perfect, 
(select cast(sum(CASE WHEN perfect THEN 1 ELSE 0 END) as float)/count(*)*100  as ytdperfect from rs_windsor_process_pivot t where t.property = p.property and extract(month from starttime) < 10),
cast(sum(CASE WHEN (approved or perfect) and endtime is not null THEN submissions ELSE 0 END) as float)/nullif(sum(CASE WHEN (approved or perfect) and endtime is not null THEN 1 ELSE 0 END),0) 
as submissionstoapproval,cast((select sum(CASE WHEN (approved or perfect) and endtime is not null THEN submissions ELSE 0 END) from rs_windsor_process_pivot t where t.property = p.property and 
extract(month from starttime) < 10) as float)/nullif((select sum(CASE WHEN (approved or perfect) and endtime is not null THEN 1 ELSE 0 END) 
from rs_windsor_process_pivot t where t.property = p.property and extract(month from starttime) < 10),0) as ytdsubmissionstoapproval, 
sum(CASE WHEN running THEN 1 ELSE 0 END) as monitor from rs_windsor_process_pivot p, datasetmember m where extract(year from starttime) = 2017 and 
extract(month from starttime) = 10 and m.id = p.property and pmc=71067 group by property, strvalue order by property


-- API Reports
--Inbound traffic Reqs/hour x 7 days (hours with 0 reqs will no be included)

select date_trunc('hour', event_time) D, count(*) from api_logevent where event_time >(now() - INTERVAL '24 hour')::timestamp group by D order by D

-- Mean processing time last hour
select sum(event_processing_time)/count(*) from api_logevent where     event_time >(now() - INTERVAL '1 hour')::timestamp

-- Mean processing time POST last hour
select sum(event_processing_time)/count(*) from api_logevent where event_method like 'POST%' and event_time >(now() - INTERVAL '1 hour')::timestamp

-- TOTAL 
select date_trunc('hour', event_time) D,  count(*) from api_logevent where event_time >(now() - INTERVAL '7 day')::timestamp group by D order by D

-- TOTAL OK 
select date_trunc('hour', event_time) D,  count(*) from api_logevent where event_status=200 and event_time >(now() - INTERVAL '7 day')::timestamp group by D order by D

-- TOTAL ERROR 
select date_trunc('hour', event_time) D,  count(*) from api_logevent where event_status!=200 and event_time >(now() - INTERVAL '7 day')::timestamp group by D order by D

-- TOTAL BOUNCED 
select date_trunc('hour', event_time) D,  count(*) from api_logevent where event_status=429 and event_time >(now() - INTERVAL '7 day')::timestamp group by D order by D

		
-- TOTAL DELETE 
select date_trunc('hour', event_time) D,  count(*) from api_logevent where event_method like 'DELE%' and event_time >(now() - INTERVAL '7 day')::timestamp group by D order by D
		
-- TOTAL POST 
select date_trunc('hour', event_time) D,  count(*) from api_logevent where event_method like 'POST%' and event_time >(now() - INTERVAL '7 day')::timestamp group by D order by D

	
--	MEAN PROCESSING TIME POST
select date_trunc('hour', event_time) D,  sum(event_processing_time)/count(*) from api_logevent where event_method like 'POST%' and event_time >(now() - INTERVAL '7 day')::timestamp group by D order by D
		
-- MEAN PROCESSING TIME DELETE
select date_trunc('hour', event_time) D,  sum(event_processing_time)/count(*) from api_logevent where event_method like 'DELETE%' and event_time >(now() - INTERVAL '7 day')::timestamp group by D order by D

-- MEAN PROCESSING TIME ALL
select date_trunc('hour', event_time) D,  sum(event_processing_time)/count(*) from api_logevent where event_time >(now() - INTERVAL '7 day')::timestamp group by D order by D


-- MEAN PROCESSING TIME POST / hour (2 days)
select date_trunc('hour', event_time) D,  sum(event_processing_time)/count(*) from api_logevent where
        event_method like 'POST%' and
        event_time >(now() - INTERVAL '15 day')::timestamp
group by D having count(*) > 0 order by D


----------------------------------------------------------
-- Contents published by user XXX between 		T1 and T2
-- Tasks realized by user XXX between     		T1 and T2 
-- Emails sent between 							T1 and T2
-- Emails sent by actions of user X between 	T1 and T2
-- Logins by user L between L1 and L2
----------------------------------------------------------

\copy (select * from tempTable limit 100) copy to 'filenameinquotes' with header delimiter as ','
\copy ( select title,wc_pmc_id,wc_site_id, external_id, concat(trim( both ' ' from to_char(wc_pmc_id , '999999999999')), '-', trim( both ' ' from to_char(wc_site_id, '999999999999')), '-', external_id) from content where checkindate >= '2018-08-01' and (wc_pmc_id is null or wc_site_id is null) ) copy to 'siteid-pmcid-null.txt' with header delimiter as ',';
 

 -- LIST TABLES
SELECT table_schema || '.' || table_name as show_tables FROM information_schema.tables WHERE table_type = 'BASE TABLE' AND table_schema NOT IN ('pg_catalog', 'information_schema') order by (table_schema || '.' || table_name)

-- TABLE STRUCTURE
select column_name, data_type, character_maximum_length from INFORMATION_SCHEMA.COLUMNS where table_name ='content' order by column_name----

 
 -- RESOURCES
 select * from kresource where lastmodifieddate >=  '2020-03-23 00:00:00.000 '  and lastmodifieddate <= ' 2020-03-24 00:00:00.000'  order by name