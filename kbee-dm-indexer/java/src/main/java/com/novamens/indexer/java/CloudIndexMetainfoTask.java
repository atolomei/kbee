package com.novamens.indexer.java;


import java.util.Collections;

import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.novamens.beans.BeansService;
import com.novamens.indexer.service.Index;
import com.novamens.security.Identifiable;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeRuntimeException;

import kbee.api.model.ApiObject;
import kbee.api.service.ApiSerializer;

public class CloudIndexMetainfoTask extends ObjectIndexTaskServiceRequest {
	private static final long serialVersionUID = 1L;

	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(IndexMetainfoTask.class.getName());
	
	RestTemplate restTemplate = new RestTemplate();
	
	boolean onlymetainfo = true;
	
	public CloudIndexMetainfoTask(Object object, Index index) {
		super(object, index);
   		setName("IndexMetainfoTask");
	}
	
	@Override
	public void execute() {
	try {
		String objectId = String.valueOf(((Identifiable)getObject()).getId());
		
		org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		
		ObjectMapper mapper = new ObjectMapper();
				mapper.registerModule(new JavaTimeModule());
				mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		String json = mapper.writeValueAsString(serialize(getObject()));
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		System.out.println(json);
		
		String url = "http://localhost:8116/index/file/{id}";
		
		HttpEntity<String> entity =
		        new HttpEntity<>(json, headers);
		String response = restTemplate.postForObject(
		        url,
		        entity,
		        String.class,
		        objectId);
	   }
		catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
			throw new KbeeRuntimeException(e);
		}
	}
	
	public boolean isTransactional() {
		return false;
	}
	
	private ApiObject serialize(Object object) {
		return ((ApiSerializer)ServiceLocator
			.getService(BeansService.class)
			.getBean("ApiSerializer"))
			.serialize(object);
	}
}
