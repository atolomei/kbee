package com.novamens.kbee.content.library;

import java.util.HashMap;
import java.util.Map;

import com.novamens.dom.Domain;
import com.novamens.indexer.iql.IqlQuery;
import com.novamens.indexer.iql.IqlService;
import com.novamens.indexer.query.Criteria;
import com.novamens.solr.indexer.iql.SolrIqlQuery;

public class IqlCriteria implements Criteria {
	
	private String statement;
	private Domain domain;
	
	public IqlCriteria(Domain domain, String statement) {
		this.statement = statement;
		this.domain = domain;
	}

	public Map<String, Object> getParameters() {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("head", "true");
		parameters.put("state", "1");
		String solrclause = getSolrClause();
		if (solrclause!=null)
		parameters.put("solrclause", solrclause);
		return parameters;
	}
	
	public String getStatement() {
		return statement;
	}
	
	public Domain getDomain() {
		return domain;
	}
	
	public String getSolrClause() {
		String iql = getStatement();
		
		if (iql==null || "".equals(iql))
			return null;
		
		iql = iql.replace("&", "");
		iql = iql.replace("'", "");
		IqlQuery query = getIqlService().getNewQuery(iql);
		String clause = ((SolrIqlQuery)query).getSolrStatement();
		
		return clause;
	}
	
	public IqlService getIqlService() {
		return getDomain().getService(IqlService.class);
	}
}
