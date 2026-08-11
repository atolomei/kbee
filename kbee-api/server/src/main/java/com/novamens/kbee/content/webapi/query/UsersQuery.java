package com.novamens.kbee.content.webapi.query;

import com.novamens.content.model.DataSet;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;

import com.novamens.indexer.iql.IqlService;
import com.novamens.indexer.service.Index;
import com.novamens.service.ServiceLocator;
import com.novamens.solr.indexer.query.SolrParametersQuery;

public class UsersQuery extends SolrParametersQuery {
	private static final long serialVersionUID = 1L;

	public UsersQuery(Index index, DataSet dataset) {
		super(index);
		getParameters().put("type", "datasetmember");
		getParameters().put("sort", "modified");
		getParameters().put("ascending", "false");
		getParameters().put("state", STATE_ENABLED_ARCHIVED );
		getParameters().put("dataset", String.valueOf(dataset.getId()));
	}
	
	@Override
	public boolean includeScore() {
		return true;
	}
	
	@Override
	public IqlService getIqlService() {
		return getDomain().getService(IqlService.class);
	}
	
	public Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
}
