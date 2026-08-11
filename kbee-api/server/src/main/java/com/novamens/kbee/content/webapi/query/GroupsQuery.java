package com.novamens.kbee.content.webapi.query;

import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.indexer.service.Index;
import com.novamens.service.ServiceLocator;
import com.novamens.solr.indexer.query.SolrParametersQuery;

public class GroupsQuery extends SolrParametersQuery {
	private static final long serialVersionUID = 1L;

	public GroupsQuery(Index index) {
		super(index);
		UserProfile profile = ServiceLocator.getService(UserService.class).getSessionUserProfile();
		Domain domain = profile.getDomain();
		getParameters().put("type", "group");
		getParameters().put("sort", "title_sort");
		getParameters().put("ascending", "true");
		getParameters().put("domain", String.valueOf(domain.getId()));
	}
	
	@Override
	public boolean includeScore() {
		return true;
	}
}
