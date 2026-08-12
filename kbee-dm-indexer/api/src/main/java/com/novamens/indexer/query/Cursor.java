package com.novamens.indexer.query;

public interface Cursor {
	public SearchResult get(long index);
	public SearchResult next();
	public SearchResult previous();
	public boolean hasMoreElements();
	public void setIndex(long index);
	public long getIndex();
	public long size();
}
