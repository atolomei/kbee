package com.novamens.kbee.content.base.query;

import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.indexer.iql.IqlService;
import com.novamens.indexer.service.Index;
import com.novamens.security.User;
import com.novamens.service.ServiceLocator;
import com.novamens.solr.indexer.query.SolrParametersQuery;

public abstract class DomainSolrQuery extends SolrParametersQuery {
	private static final long serialVersionUID = 1L;

	public DomainSolrQuery(Index index) {
		super(index);
	}
	
	public Domain getDomain() {
		UserProfile profile = ServiceLocator.getService(UserService.class).getSessionUserProfile();
		Domain domain = profile.getDomain();
		return domain;
	}
	
	public User getUser() {
		UserProfile profile = ServiceLocator.getService(UserService.class).getSessionUserProfile();
		return profile.getUser();
	}
	
	@Override
	public IqlService getIqlService() {
		return getDomain().getService(IqlService.class);
	}
}
