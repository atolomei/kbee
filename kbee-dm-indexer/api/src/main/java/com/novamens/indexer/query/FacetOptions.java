package com.novamens.indexer.query;

import java.io.Serializable;

public class FacetOptions implements Serializable {
	private static final long serialVersionUID = 1L;
	public int maxMembers = 10;
	public int maxVisibleMembers = 10;
	public String filter = null;
	public boolean orderByName = false;
}