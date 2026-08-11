package com.novamens.kbee.content.command;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.novamens.indexer.query.Cursor;
import com.novamens.indexer.query.Facet;
import com.novamens.indexer.query.FacetOptions;
import com.novamens.indexer.query.Member;
import com.novamens.indexer.query.ResultSet;
import com.novamens.indexer.query.SearchResult;
import com.novamens.util.KbeeRuntimeException;

public class ListResultSet<T extends Serializable> implements ResultSet {
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ListResultSet.class.getName());
	
	private List<T> list;
	private Iterator<T> it;

	public ListResultSet(List<T> list) {
		this.list=list;
		it = list.iterator();
	}
	
	@Override
	public boolean hasNext() {
		return it.hasNext();
	}

	@Override
	public SearchResult next()
	{
		T obj = it.next();
		return new ListSearchResult<T>(obj);
	}

	@Override
	public void remove() {
		
	}

	protected Iterator<T> getIterator() {
		return it;
	}
	
	@Override
	public Iterator<SearchResult> iterator() {
		logger.debug("iterator");
		throw new KbeeRuntimeException("not implemented");
	}

	@Override
	public void close() {
	}

	@Override
	public int size() {
		return this.list.size();
	}

	
	/**
	 * Por cuestiones de otros paneles. 
	 * Absolute tiene que empezar por 1
	 */
	@Override
	public void absolute(int positionFroOme) {
		
		it = list.iterator();
		for (int p=0; p<positionFroOme-1; p++) {
			it.next();
		}
	}

	@Override
	public List<String> getFacetsNames() {
		return null;
	}

	@Override
	public List<Facet> getFacets() {
		return null;
	}

	@Override
	public List<Member> getMembers(String facetName) {
		return null;
	}

	@Override
	public List<Member> getMembers(Facet facet) {
		return null;
	}

	@Override
	public List<Member> getMembers(Facet facet, int max) {
		return null;
	}

	@Override
	public void setOptions(Map<String, FacetOptions> options) {
		
	}
	
	@Override
	public Cursor getCursor(){
		logger.debug("getCursor not implemented");
		throw new KbeeRuntimeException("not implemented");
	}
}
