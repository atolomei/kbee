package com.novamens.solr.indexer.query;

import java.io.Serializable;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;

import com.novamens.content.userlist.UserListItem;
import com.novamens.indexer.query.Cursor;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.SearchResult;
import com.novamens.indexer.service.IndexerDocument;

public class SolrCursor implements Cursor, Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public static String DOCUMENT_ID_FIELD = "id";
	
	private long index, pagenumber, size;
	private Query query;
	private SolrDocumentList page;
	public int PAGE_SIZE = SolrResultSet.PAGE_SIZE;

	

	
	public SolrCursor (SolrResultSet resultSet) {
		this(resultSet, 0);
	}
	
	public SolrCursor (SolrResultSet resultSet, long index) {
		page =  resultSet.getQueryResponse().getResults();
		query = resultSet.getQuery();
		this.index = index;
		this.size =  (long) resultSet.size();
		pagenumber = index/PAGE_SIZE;
	}
	
	
	public Query getQuery() {
		return this.query;
	}

	@Override
	public long size () {
		if (query==null)
			return 0;
		return size;
	}

	
	public SearchResult next() {
		return get(++index);
	}
	
	public SearchResult previous() {
		return get(--index);
	}
	
	public SearchResult get(long index) {
		if (index<0)
			return null;
		long i = index%PAGE_SIZE;
		long pn = index/PAGE_SIZE;
		
		if (pn!=pagenumber) {
			SolrResultSet resultSet = (SolrResultSet)query.execute();
			resultSet.absolute((int)index);
			page =  ((SolrResultSet)resultSet).getQueryResponse().getResults();
			pagenumber = index/PAGE_SIZE;
		}
		
		IndexerDocument document = new IndexerDocument();
		SolrDocument solrdocument = page.get((int)i);
		Object documentId = solrdocument.getFieldValue(DOCUMENT_ID_FIELD);
		document.setId(documentId.toString());
		for (String field : solrdocument.getFieldNames()) {
			document.addField(field, solrdocument.getFieldValue(field).toString());
		}
		Object object = ((SolrQuery)query).getIndex().getObjectBuilder().build(document);
		if (object instanceof UserListItem) { 
			object = ((UserListItem)object).getObject();
			if (object instanceof HibernateProxy) {
				HibernateProxy proxy = (HibernateProxy)object;
				LazyInitializer initializer = proxy.getHibernateLazyInitializer();
				object = initializer.getImplementation();
			}
		}
		SolrSearchResult result = new SolrSearchResult(object);
		return result;
	}
	
	public boolean hasMoreElements() {
		return index<size-1;
	}
	
	public long getIndex() {
		return index;
	}
	
	public void setIndex(long index) {
		this.index = index;
		pagenumber = index/PAGE_SIZE;
	}
}