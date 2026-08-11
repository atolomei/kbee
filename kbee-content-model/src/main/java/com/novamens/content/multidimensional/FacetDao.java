package com.novamens.content.multidimensional;

import java.util.List;

import com.novamens.dom.Domain;
import com.novamens.indexer.query.Facet;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.service.Index;

public interface FacetDao {
	public List<Facet> getFacets(Index index, Domain domain);
	public List<Facet> getFacets(Query query, Domain domain);
	public void save(Facet facet);
}
