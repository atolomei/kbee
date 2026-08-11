package com.novamens.kbee.content.multidimensional;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import com.novamens.content.multidimensional.FacetDao;
import com.novamens.content.multidimensional.FacetService;
import com.novamens.dom.Domain;
import com.novamens.indexer.query.Facet;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.service.Index;
import com.novamens.logging.ObjectUpdateEvent;
import com.novamens.solr.indexer.query.SolrQuery;

public class KbeeFacetService implements FacetService {
	
	private Domain domain;
	private FacetDao facetDao;
	
	static private Logger txLogger = LogManager.getLogger("TxLogger");
	
	public KbeeFacetService() {
	}
	
	public KbeeFacetService(Domain domain) {
		 this.domain = domain;
	}
	
	public List<Facet> getFacets(Index index) {
		return getFacetDao().getFacets(index, getDomain());
	}
	
	public List<Facet> getFacets(Query query) {
		if (query instanceof SolrQuery) {
			return getFacetDao().getFacets(((SolrQuery)query).getIndex(), getDomain());
		}
		else {
			return new ArrayList<Facet>();
		}
	}
	
	@Transactional
	public void update(Facet facet, List<String> updatedParts) {
		getFacetDao().save(facet);
		txLogger.info(new ObjectUpdateEvent<KbeeFacetWrapper>((KbeeFacetWrapper)facet, updatedParts));
	}
	
	public Domain getDomain() {
		return domain;
	}
	
	public void setFacetDao(FacetDao dao) {
		this.facetDao = dao;
	}
	
	private FacetDao getFacetDao() {
		return facetDao;
	}
}