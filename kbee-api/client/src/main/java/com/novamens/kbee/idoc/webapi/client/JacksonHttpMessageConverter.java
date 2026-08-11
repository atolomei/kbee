package com.novamens.kbee.idoc.webapi.client;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

public class JacksonHttpMessageConverter extends MappingJackson2HttpMessageConverter {

    public JacksonHttpMessageConverter() {
        setObjectMapper(new RestObjectMapper());
    }
}
