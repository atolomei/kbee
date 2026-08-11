package com.novamens.kbee.content.webapi.logging;

import java.time.OffsetDateTime;

import org.springframework.http.HttpStatus;

public interface ApiLogEvent {
	
	public Long getId();
	
	public String getDomain();
	
	public String getFile();
	
	public String getFileSource();
	
	public String getUser();
	public OffsetDateTime getTime();
	
	public String getSource();
	
	public Long getTransaction();
	
	public String getUri();
	public String getMethod();
	public String getRequest();
	public HttpStatus getStatus();
	public String getResponse();
	public long getProcessingTime();
	
	public Long getRetry();
	public int getRetryNumber();

	public String getContentClass();
	public void setContentClass(String c);
	
	public boolean isClosed();
}