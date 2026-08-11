package com.novamens.kbee.content.webapi.logging;

import java.time.OffsetDateTime;

import kbee.api.model.ApiFile;

public class SoapRequestEvent extends SoapEvent {
	
	private long start;

	public SoapRequestEvent(ApiFile file, String uri) {
		setTime(OffsetDateTime.now());
		setUser();
		setFile(file.getExternalId());
		setDomain(file.getDomain());
		setUri(uri);
		setMethod("POST");
		start = System.currentTimeMillis();
	}
	
	public SoapRequestEvent(String uri) {
		setTime(OffsetDateTime.now());
		setUser();
//		setFile(file.getExternalId());
//		setDomain(file.getDomain());
		setUri(uri);
		setMethod("POST");
		start = System.currentTimeMillis();
	}
	
	@Override
	public void setResponse(String response) {
		long now = System.currentTimeMillis();
		setProcessingTime(now-start);
		super.setResponse(response);
	}
}
