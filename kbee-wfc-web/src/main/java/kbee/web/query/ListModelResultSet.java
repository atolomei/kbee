package kbee.web.query;


import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wicket.model.IModel;

import com.novamens.indexer.query.Cursor;
import com.novamens.indexer.query.Facet;
import com.novamens.indexer.query.FacetOptions;
import com.novamens.indexer.query.Member;
import com.novamens.indexer.query.ResultSet;
import com.novamens.indexer.query.SearchResult;
import com.novamens.util.KbeeRuntimeException;

import kbee.web.cursor.ListModelSearchResult;

public class ListModelResultSet<T> implements ResultSet {

	static Logger logger = LogManager.getLogger(ListModelResultSet.class.getName());
			
	private List<IModel<T>> list;
	private Iterator<IModel<T>> it;
	
	public ListModelResultSet(List<IModel<T>> list) {
		this.list=list;
		it = list.iterator();
	}
	
	@Override
	public boolean hasNext() {
		return it.hasNext();
	}

	@Override
	public SearchResult next() {
		IModel<T> obj = it.next();
		return new ListModelSearchResult<T>(obj);
	}

	//@Override
	public void remove() {
		
	}

	protected Iterator<IModel<T>> getIterator() {
		return it;
	}
	
	//@Override
	public Iterator<SearchResult> iterator() {
		throw new KbeeRuntimeException("not implemented");
	}

	@Override
	public void close() {
		list.forEach((model) -> model.detach());
	}

	@Override
	public int size() {
		return this.list.size();
	}

	@Override
	public void absolute(int indexFromOne) {
			it = list.iterator();
			int n = 1;
			while (n++<indexFromOne && it.hasNext())
				it.next();
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
		return null;
	}
}
