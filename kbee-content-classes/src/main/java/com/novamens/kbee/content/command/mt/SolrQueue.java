package com.novamens.kbee.content.command.mt;


import java.util.Iterator;
import java.util.Map;

import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
 import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.java.KbeeJavaIndex;
import com.novamens.indexer.query.TextQuery;
import com.novamens.indexer.service.Index;
import com.novamens.indexer.service.IndexProxy;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeRuntimeException;

/**
 * Indexable 
 */
@SuppressWarnings("serial")
public class SolrQueue implements Queue<SolrDocument> {
			
	private Map<String, Object> parameters;
	private String statement;
	private long size = -1;
	private int index = 0;
	protected Iterator<?> iterator = null;
	private	SolrDocumentList page = null;
						
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SolrQueue.class.getName());													
	
	@Override
	public synchronized SolrDocument dequeue() throws QueueException {
		
		SolrDocumentList page = getPage();
		
		if (page==null || index>=page.size()) {
			int size0 = page!=null ? page.size() : 0;
			page = getNextPage();
			int size1 = page.size();
			if (size0==size1 && size0==1) {
				return null;
			}
			setPage(page);
			index = 0;
		}
		
		if (page.isEmpty()) {
			return null;
		}
		
		SolrDocument document = page.get(index++);
				
		return document;
	}
	
	public void enqueue(SolrDocument file) throws QueueException {
	}
	
	public void remove(SolrDocument file) throws QueueException {
	}
	
	public long size() throws QueueException {
		if (size<=0) {
			this.size=calculateSize();
		}	
		return size;
	}
		
	public void setParameters(Map<String, Object> parameters) {
		this.parameters = parameters;
	}
	
	public Map<String, Object> getParameters() {
		return this.parameters;
	}
	
	public String getStatement() {
		if (statement!=null)
			return statement;
		synchronized (this) {
			if (getParameters()!=null && getParameters().containsKey("statement"))
				statement = (String)getParameters().get("statement");
			else if (getParameters()!=null && getParameters().containsKey("query"))
				statement = (String)getParameters().get("query");
			return statement;
		}
	}
	
	public long getLimit() {
		return (getParameters()!=null && getParameters().get("limit")!=null) ? Long.valueOf((String)getParameters().get("limit")) : -1;
	}
	
	public void close() {
		
	}
	
	protected void setPage(SolrDocumentList page) {
		this.page = page;
	}

	protected SolrDocumentList getPage() {
		return page;
	}
	
	protected synchronized void init() {
		this.size = calculateSize();
	}
	
	protected SolrDocumentList getNextPage() {
		String statement = getStatement();
		if (getPage()!=null) {
			Long lastmodifiedtime = (Long)getPage().get(getPage().size()-1).get("lastmodifiedtime");
			if (!"".equals(statement)) statement += " AND ";
			statement += "lastmodifiedtime:[";
			statement += String.valueOf(lastmodifiedtime);
			statement += " TO * ]";
		}
		TextQuery query = new TextQuery(statement) {
			public String[] fields() {
				String[] fields = {"id", "lastmodifiedtime"};
				return fields;
			}
		};
		query.setPageSize(10000);
		query.setSortField("lastmodifiedtime");
		query.setAscending(true);
		query.execute();
		QueryResponse response = (QueryResponse)getIndex().execute(query);
		SolrDocumentList page = response.getResults();
		return page;
	}
	
	protected synchronized long calculateSize() {
		try {
			TextQuery query = new TextQuery(getStatement()) {
				public String[] fields() {
					String[] fields = {"id"};
					return fields;
				}
			};
			query.setSortField("lastmodifiedtime");
			query.setAscending(true);
			QueryResponse response = (QueryResponse) getIndex().execute(query);
			
			if (response==null) {
				logger.error(" calculateSize() -> response is null");
				return 0;
			}
			return response.getResults().getNumFound();
		}
		catch (Exception e) {
			logger.error(e, getStatement());
			throw new KbeeRuntimeException(e);
		}
	}
	
	private Index getIndex() {
		KbeeJavaIndex index = (KbeeJavaIndex)((IndexProxy) getDomain().getService(JavaIndexerService.class).getIndex()).getIndex();
		return index;
	}

	private Domain getDomain() {

		return ServiceLocator.getService(UserService.class).getSessionUserProfile().getDomain();
	}
}
