package com.novamens.content.web.admin.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wicket.model.IDetachable;

import com.novamens.content.entity.Person;
import com.novamens.content.user.UserService;
import com.novamens.service.ServiceLocator;

public class APISOAPReportFilter implements IDetachable {
			
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	static private Logger logger = LogManager.getLogger(APISOAPReportFilter.class.getName());
	
	private String domain = "All";
	private String externalId;
	private String text;
	
	private String range = "1h";
	private String status = "All";
	
	private Boolean json = Boolean.valueOf(false);
	
	public APISOAPReportFilter() {
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

	
	public Boolean getJson() {
		return json;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public void setJson(Boolean json) {
		this.json = json;
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
			logger.error(e.getStackTrace());
			return false;
		}
	}


}
