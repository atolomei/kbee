package com.novamens.solr.indexer.query;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import com.novamens.indexer.query.Cursor;
import com.novamens.indexer.query.Facet;
import com.novamens.indexer.query.FacetOptions;
import com.novamens.indexer.query.Member;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.SearchResult;
import com.novamens.indexer.service.IndexerDocument;
import com.novamens.indexer.service.IndexerException;
import com.novamens.solr.indexer.multidimensional.SolrCube;
import com.novamens.solr.indexer.service.SolrIndex;

import kbee.util.logging.Logger;

public class SolrResultSetV2 implements SolrResultSet {

	private static Logger logger = Logger.getLogger(SolrResultSetV2.class.getName());

	private SolrDocumentList page;
	private SolrQuery query;
	private QueryResponse response;
	private int position = 1;
	private int pagesize = SolrResultSet.PAGE_SIZE;
	private int size = -1;

	private long numFound =-1;
	
	public static String DOCUMENT_ID_FIELD = "id";

	public SolrResultSetV2(SolrQuery query) {
		this.query = query;
		setPageSize(query.getPageSize());
	}

	public SearchResult next() {
		
		SolrDocument solrdocument; 
		SolrDocumentList page = getPage();
		
		int index = position - 1;
		
		if (index>=page.getStart() && index<page.getStart()+page.size()) {
			solrdocument = page.get((int)(index-page.getStart()));
		}
		else {
			this.page = null;
			page = getPage();
			solrdocument = page.get((int)(index-page.getStart()));
		}
		
		SearchResult result = getResult(solrdocument);
		position++;
		return result;
	}

	public boolean hasNext() {
		return position<=size() && size()>0;
	}
	
	public Iterator<SearchResult> iterator() {
		throw new RuntimeException("iterator() not implemented");
	}
	
	public void remove() {
	}
	
	public int size() {
		try {
			if (size == -1)
				size = (int)getPage().getNumFound();
			return size;
			
		} 
		catch (Exception e) {
			logger.error(e);
			throw(e);
		}
	}
	
	public void absolute(int position) {
		this.position = position;
	}
	
	public List<String> getFacetsNames() {
		List<String> names = new ArrayList<String>();
		for (FacetField facetField : getQueryResponse().getFacetFields()) {
			if (getCube().getFacet(facetField.getName())!=null)
				names.add(facetField.getName());
		}
		return names;		
	}
	
	public List<Facet> getFacets() {
		List<Facet> facets = new ArrayList<Facet>();
		for (String name : getFacetsNames()) {
			facets.add(getCube().getFacet(name));
		}
		return facets;
	}

	public List<Member> getMembers(String facetName) {
		return getMembers(getCube().getFacet(facetName));
	}

	public List<Member> getMembers(Facet facet) {
		return facet.getMembers(this, 50);
	}

	public List<Member> getMembers(Facet facet, int max) {
		return null;
	}
	
	public void setOptions(Map<String,FacetOptions> options) {
	}; 
	
	public void close()  {
	}
	
	public long getNumFound() {
		if (numFound<0)
			numFound = getPage().getNumFound();
		return numFound;
	}
	
	public QueryResponse getQueryResponse() {
		if (response==null) 
			getPage();
		return response;
	}
	
	public SolrIndex getIndex() {
		return this.query.getIndex();
	}
	
	public SolrCube getCube() {
		return getIndex().getCube();
	}
	
	public Query getQuery() {
		return query;
	}
	
	public Cursor getCursor() {
		return new SolrCursor(this);
	}
	
	public void setPageSize(int pageSize) {
		this.pagesize = pageSize;
	}
	
	protected Object getObject(IndexerDocument document) {
		return query.getIndex().getObjectBuilder().build(document);
	}
	
	protected IndexerDocument getDocument(SolrDocument solrdocument) {
		IndexerDocument document = new IndexerDocument();
		Object documentId = solrdocument.getFieldValue(DOCUMENT_ID_FIELD);
		
		document.setId(documentId.toString());
		for (String field : solrdocument.getFieldNames()) {
			document.addField(field, solrdocument.getFieldValue(field).toString());
		}
		return document;
	}
	
	protected SearchResult getResult(SolrDocument solrdocument) {
		Object documentId = solrdocument.getFieldValue(DOCUMENT_ID_FIELD);
		
		Object object = getObject(getDocument(solrdocument));
		SolrSearchResult result = new SolrSearchResult(object);
		
		if (query.includeScore()) {
			result.setScore(Float.valueOf(solrdocument.getFieldValue("score").toString()));
		}
		
		if (response.getHighlighting()!=null) {
			Map<String, List<String>> snippetsmap = response.getHighlighting().get(documentId);
			if (snippetsmap!=null) {
				for (List<String> snippets : snippetsmap.values()) {
					result.setSnippets(snippets);
					break;
				}
			}
		}
		return result;
	}
	
	protected SolrDocumentList getPage() throws IndexerException {
		try {
			if (page==null) {
				int start = ((position-1)/pagesize)*pagesize;
				response  = this.getIndex().select(query, start);
				page = response.getResults();
			}
			return page;
		} 
		catch (Exception e) {
			logger.error(e);
			throw(e);
		}
	}
}
