package com.novamens.content.multidimensional;

import java.util.List;

import com.novamens.indexer.query.Facet;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.service.Index;
import com.novamens.service.ObjectService;

public interface FacetService extends ObjectService {
	public List<Facet> getFacets(Index index);
	public List<Facet> getFacets(Query query);
	public void update(Facet facet,  List<String> updatedParts);
}
