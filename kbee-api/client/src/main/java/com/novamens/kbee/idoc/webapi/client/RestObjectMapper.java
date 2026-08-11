package com.novamens.kbee.idoc.webapi.client;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

public class RestObjectMapper  extends ObjectMapper {
	private static final long serialVersionUID = 1L;

	public RestObjectMapper() {
        super();
        configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        registerModule(new ParameterNamesModule());
        registerModule(new Jdk8Module());
        registerModule(new JavaTimeModule());
    }
}