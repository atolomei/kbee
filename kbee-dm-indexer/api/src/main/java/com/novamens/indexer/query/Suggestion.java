package com.novamens.indexer.query;

import java.io.Serializable;

public interface Suggestion extends Serializable {
	public Object getObject();
	public String getText();
	public String getFacet();
	public float getScore();
	public boolean isOutstanding();
}
