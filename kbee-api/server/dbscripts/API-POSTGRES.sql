CREATE SEQUENCE api_sequence
	START WITH 1000
	INCREMENT BY 10
	NO MINVALUE
	NO MAXVALUE
	CACHE 1;
	
CREATE TABLE api_logevent (
	event_id bigint NOT NULL,
	event_domain character,
	event_file character varying(128),
	event_time timestamp with time zone DEFAULT now(),
	event_user character varying(128),
	event_transaction bigint,
	event_uri character varying(256),
	event_method character varying(15),
	event_request text,
	event_status int,
	event_respone text,
	event_processing_time bigint,
	CONSTRAINT api_logevent_pkey PRIMARY KEY (event_id)
)
WITH (
	OIDS=FALSE 
);

CREATE TABLE api_soapevent (
	event_id bigint NOT NULL,
	event_domain character varying(128),
	event_file character varying(128),
	event_time timestamp with time zone DEFAULT now(),
	event_user character varying(128),
	event_transaction bigint,
	event_uri character varying(256),
	event_method character varying(15),
	event_request text,
	event_status int,
	event_respone text,
	event_processing_time bigint,
	CONSTRAINT api_soapevent_pkey PRIMARY KEY (event_id)
)
WITH (
	OIDS=FALSE 
);

