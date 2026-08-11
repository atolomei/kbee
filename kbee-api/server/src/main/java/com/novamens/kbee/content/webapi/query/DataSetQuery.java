package com.novamens.kbee.content.webapi.query;

import com.novamens.content.model.DataSet;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.indexer.iql.IqlService;
import com.novamens.indexer.service.Index;
import com.novamens.service.ServiceLocator;
import com.novamens.solr.indexer.query.SolrParametersQuery;

public class DataSetQuery extends SolrParametersQuery {
	private static final long serialVersionUID = 1L;

	public DataSetQuery(Index index, DataSet dataset) {
		this(index, dataset, false);
	}
	
	public DataSetQuery(Index index, DataSet dataset, boolean facets) {
		super(index);
		setIncludeFacets(facets);
		getParameters().put("type", "datasetmember");
		getParameters().put("sort", "modified");
		getParameters().put("state", "["+String.valueOf(ObjectState.ENABLED.getId())+", "+String.valueOf(ObjectState.ARCHIVED.getId())+"]");
		getParameters().put("ascending", "false");
		getParameters().put("dataset", String.valueOf(dataset.getId()));
	}
	
	public DataSetQuery(Index index, DataSet dataset, String sort) {
		super(index);
		getParameters().put("type", "datasetmember");
		getParameters().put("sort", sort);
		getParameters().put("state", String.valueOf(ObjectState.ENABLED.getId()));
		getParameters().put("ascending", "true");
		getParameters().put("dataset", String.valueOf(dataset.getId()));
	}
	
	@Override
	public IqlService getIqlService() {
		return getDomain().getService(IqlService.class);
	}
	
	public Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
}
