package com.novamens.solr.indexer.multidimensional;

import java.util.Locale;

import com.novamens.indexer.query.Member;

public class SolrMember implements Member {
	private static final long serialVersionUID = 1L;
	private int count;
	private boolean navigable = false;
	private String path;
	private String facet;
	private String facetDisplayName;
	private String displayName;
	private Member parent;
	
	public String getPath() {
		return path;
	}
	
	public void setPath(String path) {
		this.path = path;
	}
	
	public String getFacet() {
		return facet;
	}
	
	public void setFacet(String facet) {
		this.facet = facet;
	}
	
	public String getFacetDisplayName() {
		return facetDisplayName;
	}
	
	public void setFacetDisplayName(String label) {
		this.facetDisplayName = label;
	}
	
	public void setDisplayName(String label) {
		this.displayName = label;
	}
	
	public String getDisplayName() {
		return displayName;
	}
	
	public Member getParent() {
		return parent;
	}
	
	public void setParent(Member parent) {
		this.parent = parent;
	}
	
	public int getCount() {
		return count;
	}
	
	public void setCount(int count) {
		this.count = count;
	}
	
	public boolean isNavigable() {
		return navigable;
	}
	
	public void setNavigable(boolean value) {
		this.navigable = value;
	}

	@Override
	public String getDisplayName(Locale locale) {
		return getDisplayName();
	}

}
