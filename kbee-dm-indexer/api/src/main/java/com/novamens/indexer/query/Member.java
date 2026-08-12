package com.novamens.indexer.query;

import java.io.Serializable;
import java.util.Locale;

public interface Member extends Serializable {
	public boolean isNavigable();
	public String getPath();
	public String getFacet();
	public String getFacetDisplayName();
	public String getDisplayName();
	public String getDisplayName(Locale locale);
	public Member getParent();
	public int getCount();
}
