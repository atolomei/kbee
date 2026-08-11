package com.novamens.kbee.content.multidimensional;

import java.io.Serializable;

import org.apache.solr.client.solrj.response.FacetField.Count;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSetMember;
import com.novamens.indexer.query.Member;
import com.novamens.service.ServiceLocator;
import com.novamens.solr.indexer.multidimensional.SolrFacet;
import com.novamens.solr.indexer.multidimensional.SolrMember;

public class ClassifierFacet extends SolrFacet implements Serializable {
	private static final long serialVersionUID = 1L;
	private Classifier classifier;
	
	public ClassifierFacet() {
	}
	
	public Member getMember(Count count) {
		SolrMember member = new SolrMember();
		DataSetMember datamember = (DataSetMember)getContentDao().findModelObjectById(DataSetMember.class, count.getName());
		member.setDisplayName(datamember!=null ? datamember.getDisplayName() : "-");
		member.setPath(getName()+"/"+(datamember!=null?datamember.getId():""));
		member.setFacet(super.getName());
		member.setFacetDisplayName(super.getDisplayName());
		member.setCount((int)count.getCount());
		return member;
	}
	
	public Classifier getClassifier() {
		return classifier;
	}
	
	public void setClassifier(Classifier classifier) {
		this.classifier = classifier;
	}
	
	public Member getMember(DataSetMember datamember) {
		SolrMember member = new SolrMember();
		member.setDisplayName(datamember.getDisplayName());
		member.setPath(getName()+"/"+datamember.getId());
		member.setFacet(super.getName());
		member.setFacetDisplayName(super.getDisplayName());
		member.setCount(0);
		return member;		
	}
	
	public void setContentDao(ContentDao dao) {
	}

	public ContentDao  getContentDao() {
		return	(ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
}
