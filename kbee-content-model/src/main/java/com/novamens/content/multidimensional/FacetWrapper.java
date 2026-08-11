package com.novamens.content.multidimensional;


import com.novamens.indexer.query.Facet;

import com.novamens.security.Auditable;

public interface FacetWrapper extends Facet, Auditable {
	public Facet getFacet();
	public boolean isVisible(String context);
}
