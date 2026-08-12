  
-- Avg. Time by Month from Submission to Approval (days) in 2018
----------------------------------------------------------------
select extract(month from S) "Month (2018)", avg(Duration)/86400.0 "Submission to Approval (days)" 
from (select date_trunc('month',  P.starttime) S, extract(epoch from (Q.starttime-P.endtime)) 
Duration, Q.event from rs_windsor_activity_pivot P,  rs_windsor_activity_pivot Q where P.process=Q.process and P.task='Submission' 
and Q.event='approve_request' and Q.starttime is not null  and P.procedure='59604' and extract(year from P.starttime)=2018)  Z group by S;



-- Avg. Time from Submission to Approval (days) in 2018
-------------------------------------------------------
select extract(year from S) "Year", avg(Duration)/86400.0 "Submission to Approval (days)" from (select date_trunc('year',  P.starttime) S, extract(epoch from (Q.starttime-P.endtime)) Duration, Q.event from rs_windsor_activity_pivot P,  rs_windsor_activity_pivot Q where P.process=Q.process and P.task='Submission' and Q.event='approve_request' and Q.starttime is not null  and P.procedure='59604' and extract(year from P.starttime)=2018)  Z group by S;


-- From Ack to Publish (Audit phase) in 2018
-------------------------------------------------------
select extract(month from S) "Month", round(cast(avg(Duration)/86400.0 as numeric), 2) "Ack to Publish (days)" from (select date_trunc('month',  P.starttime) S, extract(epoch from (Q.starttime-P.endtime)) Duration, Q.event from rs_windsor_activity_pivot P,  rs_windsor_activity_pivot Q where P.process=Q.process and P.event='acknowledge_response' and Q.event='publish_request' and Q.starttime is not null  and P.procedure='59604' and extract(year from P.starttime)=2018)  Z group by S;



------------------------------------------------------------------------------------
-- Agv. Time File Review (days) in 2018, from File Review that took less than 15 days: 1.61 Days
-- There are 280,562 File Reviews in 2018,of which 166 (0.06%) took more than 30 days
--
-------------------------------------------------------------------------------------
select avg(Duration)/86400 from (select P.task Task, extract(epoch from (P.endtime-P.starttime)) Duration, P.event 
from rs_windsor_activity_pivot P where P.task='File Review'  and P.endtime is not null  and P.procedure='59604' 
and extract(year from P.starttime)=2018  and extract(epoch from (P.endtime-P.starttime))<86400*30)  Z;


			
-- Agv. Time Audit Review (days) in 2018, from Audit Review that took less than 15 days: 1.23 Days
-- There are 166,507 File Reviews in 2018,of which 43 (0.03%) took more than 30 days
--
-------------------------------------------------------------------------------------
-- by year
select extract(year from S) "Year", round(cast(avg(Duration)/86400.0 as numeric), 2) "Submission to Approval (days)" from (select date_trunc('year',  P.starttime) S, extract(epoch from (Q.starttime-P.endtime)) Duration, Q.event from rs_windsor_activity_pivot P,  rs_windsor_activity_pivot Q where P.process=Q.process and P.task='Submission' and Q.event='approve_request' and Q.starttime is not null  and P.procedure='59604' and extract(year from P.starttime)=2018)  Z group by S;


-- by month						
select extract(month from S) "Month", round(cast(avg(Duration)/86400.0 as numeric), 2) "Submission to Approval (days)" from (select date_trunc('month',  P.starttime) S, extract(epoch from (Q.starttime-P.endtime)) Duration, Q.event from rs_windsor_activity_pivot P,  rs_windsor_activity_pivot Q where P.process=Q.process and P.task='Submission' and Q.event='approve_request' and Q.starttime is not null  and P.procedure='59604' and extract(year from P.starttime)=2018)  Z group by S;



-- FR By Specialist							
-------------------
select N "Specialist", TT "Total File Review",  round(cast(AD as numeric), 2) "Avg File Review (days)"  from (select specialist N, count(*) TT, avg(Duration)/86400.0 AD from (select P.task Task, P.specialist, extract(epoch from (P.endtime-P.starttime)) Duration, P.event from rs_windsor_activity_pivot P where P.task='File Review'   and P.endtime is not null  and P.procedure='59604' and extract(year from P.starttime)=2018  and extract(epoch from (P.endtime-P.starttime))<86400*30) Z group by specialist) W;

 
 -- AR By Specialist
--------------------
select N "Specialist", TT "Total Audit Review",  round(cast(AD as numeric), 2) "Avg Audit Review (days)"  from (select specialist N, count(*) TT, avg(Duration)/86400.0 AD from (select P.task Task, P.specialist, extract(epoch from (P.endtime-P.starttime)) Duration, P.event from rs_windsor_activity_pivot P where P.task='Audit Review'   and P.endtime is not null  and P.procedure='59604' and extract(year from P.starttime)=2018  and extract(epoch from (P.endtime-P.starttime))<86400*30) Z group by specialist) W;




-- From - To
-- PMC
select DM.strvalue, sub1.FRT, sub1.FRAV, sub1.FAT, sub1.FAAV 
from (select  SubFR.N ID, SubFR.TT FRT, SubFR.AV FRAV, SubAR.TT FAT, SubAR.AV FAAV from 
(select N, TT,  round(cast(AD as numeric), 2) AV   from (select specialist N, count(*) TT, avg(Duration)/86400.0 AD from (select P.task Task, P.specialist, extract(epoch from (P.endtime-P.starttime)) Duration, P.event from rs_windsor_activity_pivot P where P.task='File Review'    and P.endtime is not null  and P.procedure='59604' and extract(year from P.starttime)=2018  and extract(epoch from (P.endtime-P.starttime))<86400*30) Z group by specialist) W1) SubFR,
(select N, TT,  round(cast(AD as numeric), 2) AV   from (select specialist N, count(*) TT, avg(Duration)/86400.0 AD from (select P.task Task, P.specialist, extract(epoch from (P.endtime-P.starttime)) Duration, P.event from rs_windsor_activity_pivot P where P.task='Audit Review'   and P.endtime is not null  and P.procedure='59604' and extract(year from P.starttime)=2018  and extract(epoch from (P.endtime-P.starttime))<86400*30) Z group by specialist) W2) SubAR
where SubFR.N=SubAR.N) Sub1, DataSetMember DM where DM.id=Sub1.ID
order by DM.strvalue;





























 
