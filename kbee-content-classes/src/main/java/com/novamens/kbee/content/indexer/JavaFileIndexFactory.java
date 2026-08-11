package com.novamens.kbee.content.indexer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.BeanNameAware;

import com.novamens.beans.BeansService;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.ModelObject;
import com.novamens.dom.Domain;
import com.novamens.event.Event;
import com.novamens.event.EventListener;
import com.novamens.indexer.java.DocumentSchema;
import com.novamens.indexer.service.Index;
import com.novamens.service.ServiceLocator;
import com.novamens.solr.indexer.service.SolrCore;
import com.novamens.solr.indexer.service.SolrIndex;

public class JavaFileIndexFactory implements JavaIndexFactory, EventListener, BeanNameAware {
	
	private String beanName;
	private SolrCore solrcore;
	
	private Map<Serializable, Index> cache = Collections.synchronizedMap(new HashMap<Serializable, Index>());
	
	public Index getIndex(Domain domain) {
		Index index = cache.get(domain.getId());
		if (index == null) {
			synchronized (domain) {
				index = getIndexBean(domain);
				if (index == null) {
					index = createIndex(domain);
				}
				cache.put(domain.getId(), index);
			}
		}
		return index;
	}
	
	public void setSolrCore(SolrCore solrcore) {
		this.solrcore = solrcore;
	}
	
	public SolrCore getSolrCore() {
		return solrcore;
	}
	
	public boolean listen(Event event) {
		return (event.getObject() instanceof Classifier || event.getObject() instanceof DataSet || event.getObject() instanceof ContentTemplate);
	}
	
	public void onEvent(Event event) {
		Serializable domainid = event.getObject() instanceof ModelObject ? ((ModelObject)event.getObject()).getDomain().getId() : ((ContentTemplate)event.getObject()).getDomain().getId();
		cache.remove(domainid);
	}
	
	public void setBeanName(String bean) {
		this.beanName = bean;
	}
	
	public String getName() {
		return beanName;
	}
	
	private Index createIndex(Domain domain) {
		SolrIndex index = new com.novamens.solr.indexer.service.SolrIndex();
		index.setServer(getSolrCore());
		index.setSchemas(getSchemas(index, domain));
		//index.setCube((SolrCube)getCube(domain));
		index.setObjectBuilder(new HibernateObjectBuilder());
		return index;
	} 
	
	private Index getIndexBean(Domain domain) {
 		if (!ServiceLocator.getService(BeansService.class).containsBean(domain.getName()+"-file-index"))
			return null;
		else
			return (Index)ServiceLocator.getService(BeansService.class).getBean(domain.getName()+"-file-index");
	}
	
	private List<DocumentSchema> getSchemas(Index index, Domain domain) {
		List<DocumentSchema> schemas = new ArrayList<DocumentSchema>();
		schemas.add(createFileSchema(domain));
		return schemas;
	}
	
	private DocumentSchema createFileSchema(Domain domain) {
		return getDefaultFileSchema();
	}
	
//	private Cube getCube(Domain domain) {
//		SolrCube cube = getDefaultCube();
//		return cube;
//	}
//	
//	private SolrCube getDefaultCube() {
//		return (SolrCube)ServiceLocator.getService(BeansService.class).getBean("default-log-cube");
//	}
	
	private DocumentSchema getDefaultFileSchema() {
		return (DocumentSchema)ServiceLocator.getService(BeansService.class).getBean("default-kbfile-schema");
	}
}
