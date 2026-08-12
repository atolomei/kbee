package com.novamens.solr.indexer.multidimensional;

import com.novamens.indexer.query.Facet;
import com.novamens.indexer.query.Member;
import org.apache.solr.client.solrj.response.FacetField.Count;

public class SolrCubeHelper extends com.novamens.indexer.query.CubeHelper{
	private SolrCube cube;
	
	public SolrCubeHelper(SolrCube cube) {
		this.cube = cube;
	}
	
	public Member getMember(String path) {
		int s = path.indexOf("/");
		if (s<0) return null;
		String factename = path.substring(0,s);
		String value = path.substring(s+1);
		Facet facet = getCube().getFacet(factename);
		if (facet==null) return null;
		Count count =new Count(null, null, (long)0);
		count.setName(value);
		Member member = ((SolrFacet)facet).getMember(count);
		return member;
	}
	
	public SolrCube getCube() {
		return cube;
	}
	
}
