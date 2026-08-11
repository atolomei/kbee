#----------------------------------------------------------------------------------
# 12 Sep 2017
#

alter table api_logevent add column event_domain character varying(128);

alter table api_soapevent add column event_domain character varying(128);

