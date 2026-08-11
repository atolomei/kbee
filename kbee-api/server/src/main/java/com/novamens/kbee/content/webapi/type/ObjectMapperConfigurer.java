package com.novamens.kbee.content.webapi.type;

import java.util.function.Consumer;

import com.fasterxml.jackson.databind.ObjectMapper;


public class ObjectMapperConfigurer implements Consumer<ObjectMapper> {
	public void accept(ObjectMapper mapper) {
		System.out.println("OK");
	}
}
