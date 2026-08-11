package com.novamens.kbee.content.workflow.multidimensional;

import java.io.Serializable;

import org.apache.solr.client.solrj.response.FacetField.Count;

import com.novamens.indexer.query.Member;
import com.novamens.solr.indexer.multidimensional.SolrFacet;
import com.novamens.solr.indexer.multidimensional.SolrMember;

public class TaskFacet extends SolrFacet implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public TaskFacet() {
	}
	
	public Member getMember(Count count) {
		SolrMember member = new SolrMember();
		member.setDisplayName(count.getName());
		member.setPath(getName()+"/"+count.getName());
		member.setFacet(super.getName());
		member.setFacetDisplayName(super.getDisplayName());
		member.setCount((int)count.getCount());
		return member;
	}
}
