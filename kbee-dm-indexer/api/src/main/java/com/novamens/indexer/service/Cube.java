package com.novamens.indexer.service;

import java.util.Collection;

import com.novamens.indexer.query.Facet;
import com.novamens.indexer.query.Member;

public interface Cube {
	public Collection<Facet> getFacets();
	public Member getMember(String path);
	public Facet getFacet(String name);
}
