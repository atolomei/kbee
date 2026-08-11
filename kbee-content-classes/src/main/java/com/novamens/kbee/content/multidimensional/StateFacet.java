package com.novamens.kbee.content.multidimensional;

import java.io.Serializable;

import org.apache.solr.client.solrj.response.FacetField.Count;

import com.novamens.dom.ObjectState;
import com.novamens.indexer.query.Member;
import com.novamens.solr.indexer.multidimensional.SolrFacet;
import com.novamens.solr.indexer.multidimensional.SolrMember;

public class StateFacet extends SolrFacet implements Serializable {
	private static final long serialVersionUID = 1L;

	public StateFacet() {
	}
	
	public Member getMember(Count count) {
		SolrMember member = new SolrMember();
		if ("6".equals(count.getName()) || "8".equals(count.getName()))
			member.setDisplayName("6".equals(count.getName())?"Enabled":"Disabled");
		else
			member.setDisplayName(getState(count.getName()).getLabel());
		member.setPath(getName()+"/"+count.getName());
		member.setFacet(super.getName());
		member.setFacetDisplayName(super.getDisplayName());
		member.setCount((int)count.getCount());
		return member;
	}
	
	public ObjectState getState(String id) {
		for (ObjectState state : ObjectState.values()) {
			if (Integer.valueOf(id).equals(state.getId()))
				return state;
		}
		return null;
	}
}
