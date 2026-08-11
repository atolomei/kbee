package com.novamens.kbee.content.workflow;

import com.novamens.indexer.service.Index;

import com.novamens.solr.indexer.query.SolrParametersQuery;

public class ActivitiesQuery extends SolrParametersQuery {
	private static final long serialVersionUID = 1L;

	public ActivitiesQuery(Index index) {
		super(index);
		
		getParameters().put("type", "[text, idoc]");
		getParameters().put("sort", "modified");
		getParameters().put("ascending", "true");
		getParameters().put("inworkspace", "true");
	}
}
