package com.novamens.kbee.content.multidimensional;

import java.io.Serializable;

import org.apache.solr.client.solrj.response.FacetField.Count;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.ContentTemplate;
import com.novamens.indexer.query.Member;
 import com.novamens.service.ServiceLocator;
import com.novamens.solr.indexer.multidimensional.SolrFacet;
import com.novamens.solr.indexer.multidimensional.SolrMember;

public class ContentTemplateFacet extends SolrFacet implements Serializable {
	private static final long serialVersionUID = 1L;

	public ContentTemplateFacet() {
	}
	
	public Member getMember(Count count) {
		SolrMember member = new SolrMember();
		ContentTemplate template = getTemplate(count.getName());
		if (template!=null)
			member.setDisplayName(template.getDisplayName());
		member.setPath(getName()+"/"+count.getName());
		member.setFacet(super.getName());
		member.setFacetDisplayName(super.getDisplayName());
		member.setCount((int)count.getCount());
		return member;
	}
	
	private ContentTemplate getTemplate(String id) {
		ContentTemplate template = (ContentTemplate)getContentDao().findModelObjectById(ContentTemplate.class, Long.valueOf(id));
		return template;
	}
	
	private ContentDao getContentDao() {
		return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}

}
