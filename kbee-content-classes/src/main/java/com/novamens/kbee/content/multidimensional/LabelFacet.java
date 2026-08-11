package com.novamens.kbee.content.multidimensional;

import java.io.Serializable;

import org.apache.solr.client.solrj.response.FacetField.Count;

import com.novamens.beans.BeansService;
import com.novamens.content.user.UserLabel;
import com.novamens.content.user.UserLabelDao;
import com.novamens.indexer.query.Member;
import com.novamens.service.ServiceLocator;
import com.novamens.solr.indexer.multidimensional.SolrFacet;
import com.novamens.solr.indexer.multidimensional.SolrMember;

public class LabelFacet extends SolrFacet implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public LabelFacet() {
	}
	
	public Member getMember(Count count) {
		SolrMember member = new SolrMember();
		try {
			
			UserLabel label = (UserLabel)getLabelDao().findLabelById(Long.valueOf(count.getName()));
			member.setDisplayName(label.getLabel());
			member.setPath(getName()+"/"+label.getId());
			member.setFacet(super.getName());
			member.setFacetDisplayName(super.getDisplayName());
			member.setCount((int)count.getCount());
			
		} catch (org.hibernate.ObjectNotFoundException e) {
			member.setDisplayName("error id: " + e.getIdentifier().toString());
			member.setFacet(super.getName());
			member.setFacetDisplayName(super.getDisplayName());
			member.setCount((int)count.getCount());
		}	 
		return member;
	}
	
	public UserLabelDao  getLabelDao() {
		return	(UserLabelDao)ServiceLocator.getService(BeansService.class).getBean("userLabelDao");
	}
}
