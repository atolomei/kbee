package com.novamens.kbee.content.multidimensional;

import java.io.Serializable;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.solr.client.solrj.response.FacetField.Count;

import com.novamens.content.model.Attribute;
import com.novamens.content.model.AttributeType;
import com.novamens.indexer.query.Member;
import com.novamens.security.User;
import com.novamens.service.ServiceLocator;
import com.novamens.solr.indexer.multidimensional.SolrFacet;
import com.novamens.solr.indexer.multidimensional.SolrMember;

public class AttributeFacet extends SolrFacet implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private Attribute attribute;
	
	public AttributeFacet() {
	}
	
	public Member getMember(Count count) {
		SolrMember member = new SolrMember();
		String displayName = getAttribute().getType().equals(AttributeType.BOOLEAN) ? 
			getLabel(count.getName()) : 
			count.getName();
		member.setDisplayName(displayName);
		member.setPath(getName()+"/"+count.getName());
		member.setFacet(super.getName());
		member.setFacetDisplayName(super.getDisplayName());
		member.setCount((int)count.getCount());
		return member;
	}
	
	public void setAttribute(Attribute attribute) {
		this.attribute = attribute;
	}
	
	public Attribute getAttribute() {
		return this.attribute;
	}
	
	public String getLabel(String value) {
		User user = getSessionUser();
		Locale locale = user!=null ? user.getLocale() : Locale.getDefault();
		ResourceBundle res = ResourceBundle.getBundle(getClass().getName(), locale);
		String label = res.getString(value);
		return label;
	}
	
	protected User getSessionUser() {
		return (User)ServiceLocator.getService(com.novamens.service.SecurityService.class).getSessionUser();
	}

}
