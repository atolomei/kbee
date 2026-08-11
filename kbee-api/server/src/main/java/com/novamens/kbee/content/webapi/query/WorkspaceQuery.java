package com.novamens.kbee.content.webapi.query;

import com.novamens.dom.ObjectState;
import com.novamens.indexer.service.Index;
import com.novamens.security.User;
import com.novamens.solr.indexer.query.SolrParametersQuery;

public class WorkspaceQuery extends SolrParametersQuery {
	private static final long serialVersionUID = 1L;

	public WorkspaceQuery(Index index, User user, boolean facets) {
		super(index);
		
		setIncludeFacets(facets);
		
		getParameters().put("type", "[text, idoc]");
		
		getParameters().put("sort", "modified");
		getParameters().put("ascending", "false");
		
		getParameters().put("state", String.valueOf(ObjectState.ENABLED.getId()));
		getParameters().put("workspace", String.valueOf(user.getId()));
	}
}