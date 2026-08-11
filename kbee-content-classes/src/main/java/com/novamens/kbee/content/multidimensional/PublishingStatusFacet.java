package com.novamens.kbee.content.multidimensional;

import java.io.Serializable;
import java.util.Locale;

import org.apache.solr.client.solrj.response.FacetField.Count;

import com.novamens.content.base.PublishingStatus;
import com.novamens.indexer.query.Member;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.solr.indexer.multidimensional.SolrFacet;
import com.novamens.solr.indexer.multidimensional.SolrMember;

public class PublishingStatusFacet extends SolrFacet implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public PublishingStatusFacet() {
	}
	
	public Member getMember(Count count) {
		SolrMember member = new SolrMember();
		try {
			PublishingStatus status = PublishingStatus.valueOf(count.getName());
			member.setDisplayName(status.getLabel(getUserLocale()));
			member.setPath(getName()+"/"+status.name());
			member.setFacet(super.getName());
			member.setFacetDisplayName(super.getDisplayName());
			member.setCount((int)count.getCount());
			
		} 
		catch (org.hibernate.ObjectNotFoundException e) {
			member.setDisplayName("error id: " + e.getIdentifier().toString());
			member.setFacet(super.getName());
			member.setFacetDisplayName(super.getDisplayName());
			member.setCount((int)count.getCount());
		}	 
		return member;
	}
	
	public Locale getUserLocale() {
		User user = ServiceLocator.getService(SecurityService.class).getSessionUser();
		Locale locale = user!=null ? user.getLocale() : Locale.getDefault();
		return locale;
	}
}
