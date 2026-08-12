package com.novamens.indexer.query;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 */
public interface ResultSet extends Iterator<SearchResult>, Iterable<SearchResult> {
	public void close();
	public int size();
	
	
	/**
	 * Por cuestiones de otros paneles. 
	 * Absolute tiene que empezar por 1
	 */
	public void absolute(int position);
	
	public List<String> getFacetsNames();
	public List<Facet> getFacets();
	public List<Member> getMembers(String facetName);
	public List<Member> getMembers(Facet facet);
	public List<Member> getMembers(Facet facet, int max);
	public void setOptions(Map<String, FacetOptions> options);
	public Cursor getCursor();
}
