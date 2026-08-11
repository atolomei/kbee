package com.novamens.content.web.admin.api;

import java.io.Serializable;

import org.apache.wicket.model.IDetachable;

import com.novamens.content.entity.Person;
import com.novamens.content.user.UserService;
import com.novamens.service.ServiceLocator;

public class APIReportFilter implements IDetachable, Serializable {
			
	private static final long serialVersionUID = 1L;
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(APIReportFilter.class.getName());

	private String domain = "All";
	private String externalId;
	private String requestText, responseText;
	
	private String range = "1 hour";
	private String status = "All";
	
	private String method = "ALL";
	
	
	private Boolean json = Boolean.valueOf(false);
	
	private Boolean closed = Boolean.valueOf(false);
	
	public APIReportFilter() {
		if (isDomainKbee())
			domain="All";
		else
			domain = ServiceLocator.getService(UserService.class).getDomain().getName();
	}
	
	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	public String getRequestText() {
		return requestText;
	}

	public void setRequestText(String text) {
		this.requestText = text;
	}
	
	public String getResponseText() {
		return responseText;
	}

	public void setResponseText(String text) {
		this.responseText = text;
	}
	
	public Boolean getJson() {
		return json;
	}

	public void setJson(Boolean json) {
		this.json = json;
	}
	
	public Boolean getClosed() {
		return closed;
	}

	public void setClosed(Boolean value) {
		this.closed = value;
	}

	@Override
	public void detach() {
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	public String getMethod() {
		return method;
	}

	public void setMethod(String status) {
		this.method = status;
	}
	

	public String getRange() {
		return range;
	}

	public void setRange(String range) {
		this.range = range;
	}
	
	private Person getPerson() {
		return ServiceLocator.getService(UserService.class).getSessionUserProfile().getPerson();
	}
	
	private boolean isDomainKbee() {
		try {
			return getPerson().getDomain().getName().toLowerCase().trim().equals("kbee");
		} 
		catch (Exception e) {
			logger.error(" isDomainKbee " + e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
			return false;
		}
	}
}
