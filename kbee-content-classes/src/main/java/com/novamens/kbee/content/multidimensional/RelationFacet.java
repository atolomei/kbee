package com.novamens.kbee.content.multidimensional;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.response.FacetField.Count;

import com.novamens.beans.BeansService;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.ObjectId;
import com.novamens.indexer.query.Member;
import com.novamens.security.Identifiable;
import com.novamens.service.ServiceLocator;
import com.novamens.solr.indexer.multidimensional.SolrFacet;
import com.novamens.solr.indexer.multidimensional.SolrMember;
import com.novamens.util.JXPath;

public class RelationFacet extends SolrFacet implements Serializable {
	private static final long serialVersionUID = 1L;
	private String classname;
	private JXPath displayNamePath;

	static private Logger logger = LogManager.getLogger(RelationFacet.class.getName());
	
	
	
	public RelationFacet() {
	}

	public Member getMember(Count count) {
		SolrMember member = new SolrMember();
		Object datamember;
		try {
			datamember = getContentDao().findObjectById(new ObjectId(getClassName(), count.getName()));
			member.setDisplayName(getDisplayName(datamember));
			member.setPath(getName()+"/"+getId(datamember));
			member.setFacet(super.getName());
			member.setFacetDisplayName(super.getDisplayName());
			member.setCount((int)count.getCount());
			return member;
		} catch (ContentMgmtException e) {
			logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
			return null;
		}
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

	public void setClassName(String classname) {
		this.classname = classname;
	}

	public String getClassName() {
		return this.classname;
	}
	
	public void setDisplayNamePath(String path) {
		this.displayNamePath = new JXPath(path);
	}

	public JXPath getDisplayNamePath() {
		return this.displayNamePath;
	}

	public ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	private String getDisplayName(Object object) {
		try {
			List<Object> names = displayNamePath.evaluateAll(object);
			if (names!=null && !names.isEmpty())
				return names.get(0).toString();
			else
				return "-";
		}
		catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
		catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
	
	private String getId(Object object) {
		if (object instanceof Identifiable)
			return String.valueOf(((Identifiable)object).getId());
		else
			return null;
	}
}
