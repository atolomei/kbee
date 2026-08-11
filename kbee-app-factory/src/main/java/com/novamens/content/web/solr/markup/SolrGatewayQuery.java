package com.novamens.content.web.solr.markup;

import org.apache.solr.common.SolrDocument;

import com.novamens.indexer.query.ResultSet;
import com.novamens.indexer.query.SearchResult;
import com.novamens.indexer.service.Index;
import com.novamens.indexer.service.IndexerDocument;
import com.novamens.solr.indexer.query.SolrQuery;
import com.novamens.solr.indexer.query.SolrResultSetV1;

public class SolrGatewayQuery extends SolrQuery {
	private static final long serialVersionUID = 1L;
	
	private String statement;
	
	public SolrGatewayQuery(Index index) {
		super(index);
	}
	
	@Override
	public ResultSet execute() {
		return new SolrResultSetV1(this) {
			@Override
			protected SearchResult getResult(SolrDocument solrdocument) {
				return new SolrGatewayResult(getDocument(solrdocument));
			}
			@Override
			protected Object getObject(IndexerDocument document) {
				return document;
			}
		};
	}

	public String getStatement() {
		if (statement==null && getParameters().get("statement")!=null) {
			return getParameters().get("statement").toString();
		}
		else 
			return statement;
	}
	
	public String getSolrStatement() {
		return getStatement();
	}
}
