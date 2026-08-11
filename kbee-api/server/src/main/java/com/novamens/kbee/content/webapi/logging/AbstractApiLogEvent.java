package com.novamens.kbee.content.webapi.logging;

import java.time.OffsetDateTime;
import org.springframework.http.HttpStatus;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.novamens.kbee.content.webapi.type.gson.OffsetDateTimeAdapter;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

public class AbstractApiLogEvent implements ApiLogEvent {
	
	private Long id;
	private OffsetDateTime time;
	private Long transaction;
	private String source;
	
	private String domain;
	private String fileSource;
	private String file;
	
	private String contentclass;
	
	private String user;
	private String uri;
	private String method;
	private int status;
	private String request;
	private String response;
	private Long retry;
	private int retryNumber = 0;
	private long processingTime;
	private boolean closed;
	
	public AbstractApiLogEvent() {
		setTime(OffsetDateTime.now());
	}
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public Long getRetry() {
		return retry;
	}
	
	public void setRetry(Long id) {
		this.retry = id;
	}
	
	public int getRetryNumber() {
		return retryNumber;
	}
	
	public void setRetryNumber(int value) {
		this.retryNumber = value;
	}
	
	public String getSource() {
		return source;
	}
	
	public void setSource(String domain) {
		this.source = domain;
	}

	@Override
	public String getContentClass() {
		return this.contentclass;
	}
	
	@Override
	public void setContentClass(String c) {
		this.contentclass = c;
	}
	
	public OffsetDateTime getTime() {
		return time;
	}
	
	public void setTime(OffsetDateTime time) {
		this.time = time;
	}
	
	public String getUser() {
		return user;
	}
	
	public void setUser(String user) {
		this.user = user;
	}
	
	public void setUser() {
		User user = ServiceLocator.getService(SecurityService.class).getSessionUser();
		String username = user!=null ? user.getName() : null;
		this.user = username;
	}
	
	public Long getTransaction() {
		return transaction;
	}
	
	public void setTransaction(Long id) {
		this.transaction = id;
	}
	
	public String getUri() {
		return uri;
	}
	
	public void setUri(String uri) {
		this.uri = uri;;
	}
	
	public String getMethod() {
		return method;
	}
	
	public void setMethod(String method) {
		this.method = method;
	}
	
	public String getRequest() {
		return request;
	}
	
	public void setRequest(String request) {
		this.request = request;
	}
	
	public String getDomain() {
		return domain;
	}
	
	public void setDomain(String domain) {
		this.domain = domain;
	}
	
	public String getFileSource() {
		return fileSource;
	}
	
	public void setFileSource(String source) {
		this.fileSource = source;
	}	
	
	public String getFile() {
		return file;
	}
	
	public void setFile(String file) {
		this.file = file;
	}
	
	public HttpStatus getStatus() {
		return HttpStatus.valueOf(status);
	}
	
	public void setStatus(HttpStatus status) {
		this.status = status.value();
	}	
	
	public void setResponse(String response) {
		this.response = response;
	}	
	
	public String getResponse() {
		return response;
	}
	
	public void setProcessingTime(long time) {
		this.processingTime = time; 
	}	
	
	public long getProcessingTime() {
		return processingTime;
	}
	
	public void setClosed(boolean value) {
		this.closed = value; 
	}	
	
	public boolean isClosed() {
		return closed;
	}
	
	protected String toJson(Object object) {
		GsonBuilder b = new GsonBuilder();
		b.registerTypeAdapterFactory(OffsetDateTimeAdapter.FACTORY);
		Gson gson = b.create();
		String json = gson.toJson(object);
		return json;
	}
}
