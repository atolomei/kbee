package com.novamens.hibernate.query;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hibernate.query.Query;

import com.novamens.indexer.query.Cursor;
import com.novamens.indexer.query.Facet;
import com.novamens.indexer.query.FacetOptions;
import com.novamens.indexer.query.Member;
import com.novamens.indexer.query.ResultSet;
import com.novamens.indexer.query.SearchResult;
import com.novamens.util.KbeeRuntimeException;


public class HibernateResultSet implements ResultSet {

	static final public int DEFAULT_BUFFER_SIZE = 40;
	static final public int DEFAULT_FETCHSIZE = 1200;
														
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(HibernateResultSet.class.getName());
	
	@SuppressWarnings("unused")
	private int pageSize;
	private Iterator<?> it;
	
	@SuppressWarnings("unused")
	private int page = 0;   	 		/** Starts in 0 */
	private int offset = 0;  			/** Index inside the page [0..page.size()-1] */
	private int size= -1;           	/** Total Size */
	private int firstResult = 0;    	/** Punto de partida del HRS. Seteado en el constructor. Desde 0  */
	
	private HibernateQuery hibernateQuery;
	

	
	public com.novamens.indexer.query.Query getQuery() {
		return hibernateQuery;
	}
	/**
	 * @param hibernateQuery
	 */
	public HibernateResultSet(HibernateQuery hibernateQuery) {
		this.hibernateQuery = hibernateQuery;
		this.pageSize = this.hibernateQuery.getMaxResults()>10 ? this.hibernateQuery.getMaxResults() : DEFAULT_BUFFER_SIZE;
		this.firstResult = this.hibernateQuery.getFirstResult(); // 0 ?
		this.execute();
	}

	
	/**
	 * 
	 */
	public void close() {
	}

	
	/**
	 * Total Size
	 */
	public int size() {
		if (this.size==-1) {
				try {
					if (this.hibernateQuery.getSizeQuery()==null) {
						String arr [] = this.hibernateQuery.getStatement().split("order by");
						String hql = arr[0];
						this.size = ((Long) this.hibernateQuery.getSessionFactory().getCurrentSession().createQuery("select count(*) " + hql).uniqueResult()).intValue();
					} 
					else 
						this.size = ((Long) this.hibernateQuery.getSessionFactory().getCurrentSession().createQuery(hibernateQuery.getSizeQuery()).uniqueResult()).intValue();
				} catch (Exception e) {
					this.size=0;
					logger.error(e);
				}
		}
		return this.size;
	}
	
	
	/**
	 * 
	 */
	public boolean hasNext() {
		return this.it.hasNext();
	}
	
	
	public SearchResult next() {
 		if (hasNext()) {
 			this.offset++;
			Object next = it.next();
			return new HibernateSearchResult(next);
		}
		return null;
	}
	
	
	public Iterator<SearchResult> iterator() {
		logger.debug("iterator() not implemented");
		throw new KbeeRuntimeException("not done");
	}
	
	
	
	public void remove() {
		logger.debug("remove()");
		throw new KbeeRuntimeException("not done");
	}
	
	
	/**
	 * Absolute empieza por 1
	 * 
	 */
	public void absolute(int indexFromOne) {
	
		int delta;
		int index = indexFromOne - 1 + this.firstResult;
		if (index >= this.offset)  {
			delta = index - this.offset;
			while (delta>0) {
				next();
				delta--;
			}
			return;
		}
		execute();
		delta=index;
		while (delta>0) {
			next();
			delta--;
		}
	}
	
	public List<String> getFacetsNames() {
		return null;
	}
	
	public List<Facet> getFacets() {
		return null;
	}

	public List<Member> getMembers(String facetName) {
		return null;
	}
	
	public List<Member> getMembers(Facet facet) {
		return null;
	}

	public List<Member> getMembers(Facet facet, int max) {
		return null;
	}

	public void setOptions(Map<String, FacetOptions> options) {
	}

	/** ----
	 * WARNING THIS CURSOR IS NOT COMPLETE
	 * 
	  ----*/
	public Cursor getCursor() {
		return new HibernateCursor(this);
	}

	
	private void execute() {
		String hql = this.hibernateQuery.getStatement();
		Query<?> query = this.hibernateQuery.getSessionFactory().getCurrentSession().createQuery(hql);
		query.setFetchSize(DEFAULT_FETCHSIZE);
		if (hibernateQuery.getCacheMode()!=null)
		query.setCacheMode(hibernateQuery.getCacheMode());
		this.offset=0;
		this.it =query.list().iterator();
	}
}
