package com.novamens.kbee.content.webapi.type;

import com.novamens.dom.Domain;

import kbee.api.model.ApiDomain;

public class IDomainAdapter implements Adapter<Domain, ApiDomain> {
	
	public IDomainAdapter() {
	}
	
	public ApiDomain adapt(Domain domain) {
		ApiDomain idomain = new ApiDomain();
		idomain.setName(domain.getName());
		idomain.setDisplayName(domain.getOrganization());
		idomain.setLastModifiedDate(domain.getLastModifiedOffsetDateTime());
		return idomain;	
	}
}