package com.novamens.hibernate.query;

import java.io.Serializable;

import com.novamens.indexer.query.Cursor;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.SearchResult;

public class HibernateCursor implements Cursor, Serializable {

	
	private long size;
				
	private static final long serialVersionUID = 1L;

	public HibernateCursor (HibernateResultSet resultSet) {
		this(resultSet, 0);
	}
	
	public HibernateCursor (HibernateResultSet resultSet, long index) {
		this.size = (long) resultSet.size();
	}
	
	public Query getQuery() {
		return null;
	}
	
	@Override
	public SearchResult get(long index) {
		return null;
	}

	@Override
	public SearchResult next() {
		return null;
	}

	@Override
	public SearchResult previous() {
		return null;
	}

	@Override
	public boolean hasMoreElements() {
		return false;
	}

	@Override
	public void setIndex(long index) {
	}

	@Override
	public long getIndex() {
		return 0;
	}

	@Override
	public long size() {
		return size;
	}
}
