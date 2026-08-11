package com.novamens.kbee.content.service;

import com.novamens.content.entity.Person;
import com.novamens.content.service.UrlService;
import com.novamens.dom.Domain;


public class KbeeDomainUrlService extends KbeeAbstractUrlService implements UrlService {

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeDomainUrlService.class.getName());
	
	private Domain domain;
	
	public KbeeDomainUrlService (Domain domain) {
		super();
		this.domain=domain;
	}
	
	protected Domain getDomain() {
		return domain;
	}

	@Override
	public String getRelativeUrl() {
		 return super.getServerUrl(domain);
	}

	@Override
	public String getUrl() {
		return super.getServerUrl(domain);
	}

	@Override
	public String getPublicUrl() {
		return super.getServerUrl(domain);
	}


	@Override
	public String getPublicUrl(Person person) {
		return super.getServerUrl(domain);
	}


	@Override
	public String getUrl(boolean include_server) {
		return super.getServerUrl(domain);
	}


	@Override
	public String getPublicUrl(String password) {
		logger.error("getPublicUrl(String password) not implemented ");
		return null;
	}


	@Override
	public String getPublicUrl(Person person, String password) {
		logger.error("getPublicUrl(Person person, String password) not implemented");
		return null;
	}
}

