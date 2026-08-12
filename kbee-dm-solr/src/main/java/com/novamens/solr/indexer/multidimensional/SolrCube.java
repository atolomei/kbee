package com.novamens.solr.indexer.multidimensional;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.response.FacetField.Count;

import com.novamens.indexer.query.CubeHelper;
import com.novamens.indexer.query.Facet;
import com.novamens.indexer.query.Member;
import com.novamens.indexer.service.Cube;

public class SolrCube implements Cube {
				
	//private static org.apache.logging.log4j.Logger logger = LogManager.getLogger(SolrCube.class.getName());
	
	private Map<String, Facet> facetsmap = new HashMap<String, Facet>();
	private List<Facet> facets;

	public SolrCube () {
		CubeHelper.setInstance(new SolrCubeHelper(this));
	}

	public Collection<Facet> getFacets() {
		return facets;
	}

	public void setFacets(List<Facet> facets) {

		for (Facet facet : facets) {
			this.facetsmap.put(facet.getName(), facet);
		}
		
		Collections.sort(facets, new Comparator<Facet>() {
			public int compare(Facet facet1, Facet facet2) {
				try {
				//return facet1.getOrder() - facet2.getOrder();
				  return facet1.getDisplayName().compareToIgnoreCase(facet2.getDisplayName());
				} catch (Exception e) {
					return 0;
				}
			}
		});
		this.facets = facets;
	}

	public Facet getFacet(String name) {
		return this.facetsmap.get(name);
	}
	
	public Member getMember(String path) {
		int s = path.indexOf("/");
		if (s<0) return null;
		String factename = path.substring(0,s);
		String value = path.substring(s+1);
		Facet facet = getFacet(factename);
		if (facet==null) return null;
		Count count =new Count(null, null, (long)0);
		count.setName(value);
		Member member = ((SolrFacet)facet).getMember(count);
		return member;
	}
}
