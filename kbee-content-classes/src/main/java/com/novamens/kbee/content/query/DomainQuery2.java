package com.novamens.kbee.content.query;

import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.indexer.iql.IqlService;
import com.novamens.indexer.service.Index;
import com.novamens.security.User;
import com.novamens.service.ServiceLocator;
import com.novamens.solr.indexer.query.SolrParametersQuery;

public abstract class DomainQuery2 extends SolrParametersQuery {

	private static final long serialVersionUID = -7144764235011510578L;

	private Domain domain;
	
	public DomainQuery2(Index index) {
		super(index);
	}

	public DomainQuery2(Domain domain, Index index) {
		super(index);
		this.domain=domain;
	}
	
	public Domain getDomain() {
		if (domain!=null)
			return domain;
		UserProfile profile = ServiceLocator.getService(UserService.class).getSessionUserProfile();
		domain = profile.getDomain();
		return domain;
	}

	
	public User getSessionUserProfile() {
		UserProfile profile = ServiceLocator.getService(UserService.class).getSessionUserProfile();
		return profile.getUser();
	}
	
	@Override
	public IqlService getIqlService() {
		return getDomain().getService(IqlService.class);
	}
}
