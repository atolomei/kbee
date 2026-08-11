package com.novamens.kbee.content.userlist;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.response.QueryResponse;

import com.novamens.content.userlist.UserListItem;
import com.novamens.indexer.query.Cursor;
import com.novamens.indexer.query.Facet;
import com.novamens.indexer.query.FacetOptions;
import com.novamens.indexer.query.Member;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.ResultSet;
import com.novamens.indexer.query.SearchResult;
import com.novamens.solr.indexer.query.SolrResultSet;
import com.novamens.solr.indexer.query.SolrSearchResult;
import com.novamens.solr.indexer.service.SolrIndex;


/**
 * 
 * Wrapper for <@code UserList}
 * 
 *  {@code DatasetMemebersUserListQuery}
 *    {@code DatasetMemebersUserListQuery}
 *    
 *
 */
public class UserListResultSetWrapper implements ResultSet, SolrResultSet {
	ResultSet resultset;
	
	public UserListResultSetWrapper(ResultSet resultset) {
		this.resultset = resultset;
	}
	public void close() {
		resultset.close();
	}
	public int size() {
		return resultset.size();
	}
	public void absolute(int position) {
		resultset.absolute(position);
	}
	public List<String> getFacetsNames() {
		return resultset.getFacetsNames();
	}
	public List<Facet> getFacets() {
		return resultset.getFacets();
	}
	public List<Member> getMembers(String facetName) {
		return resultset.getMembers(facetName);
	}
	public List<Member> getMembers(Facet facet) {
		return resultset.getMembers(facet);
	}
	public List<Member> getMembers(Facet facet, int max) {
		return resultset.getMembers(facet, max);
	}
	public void setOptions(Map<String, FacetOptions> options) {
		resultset.setOptions(options);
	}
	public Cursor getCursor() {
		return resultset.getCursor();
	}
	public boolean hasNext() {
		return resultset.hasNext();
	}
	public SearchResult next() {
		SearchResult result = resultset.next();
		Object object = result.getObject();
		if (object instanceof UserListItem) {
			object = ((UserListItem)object).getObject();
			result = new SolrSearchResult(object);
		}
		return result;
	}
	public Iterator<SearchResult> iterator() {
		return resultset.iterator();
	}
	public QueryResponse getQueryResponse() {
		return ((SolrResultSet)resultset).getQueryResponse();
	}
	public Query getQuery() {
		return ((SolrResultSet)resultset).getQuery();
	}
	public void setPageSize(int size) {
		((SolrResultSet)resultset).setPageSize(size);
	}
	public SolrIndex getIndex() {
		return ((SolrResultSet)resultset).getIndex();
	}
	protected ResultSet getResultSet() {
		return resultset;
	}
}
