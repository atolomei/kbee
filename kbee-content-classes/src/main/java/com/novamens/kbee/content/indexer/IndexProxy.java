package com.novamens.kbee.content.indexer;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Date;

import com.novamens.beans.BeansService;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.indexer.query.TextQuery;
import com.novamens.indexer.service.Cube;
import com.novamens.indexer.service.Document;
import com.novamens.indexer.service.Index;
import com.novamens.indexer.service.IndexerException;
import com.novamens.indexer.service.JavaIndex;
import com.novamens.indexer.service.ObjectBuilder;
import com.novamens.security.User;
import com.novamens.service.ObjectService;
import com.novamens.service.ServiceLocator;
import com.novamens.service.ServiceNotFoundException;

public class IndexProxy implements com.novamens.indexer.service.IndexProxy {
	private static final long serialVersionUID = 1L;
	
	private transient JavaIndex index;
	private String factoryName;
	private Domain domain;
	
	public IndexProxy(Index index, JavaIndexFactory factory, Domain domain) {
		setDomain(domain);
		setFactory(factory);
	}
	
	public void index(Object object, boolean metainfo) throws IndexerException {
		getIndex().index(object, metainfo);
	}
	
	public void index(Object object, boolean metainfo, boolean aggregations) throws IndexerException {
		getIndex().index(object, metainfo, aggregations);
	}
	
	public void index(Object object, boolean metainfo, boolean aggregations, boolean force) throws IndexerException {
		getIndex().index(object, metainfo, aggregations, force);
	}
	
	public void index(Object object) throws IndexerException {
		getIndex().index(object);
	}
	
	public void indexDocument(Document document) throws IndexerException {
		getIndex().indexDocument(document);
	}
	
	public void reindexDocument(Document document, String...field) throws IndexerException {
		getIndex().reindexDocument(document, field);
	}
	
	public void delete(Serializable id) throws IndexerException {
		getIndex().delete(id);
	}
	
	public Object execute(TextQuery query) throws IndexerException {
		return getIndex().execute(query);
	}
	
	public boolean isIndexable(Object object) {
		return getIndex().isIndexable(object);
	}
	
	public ObjectBuilder getObjectBuilder() {
		return getIndex().getObjectBuilder();
	}
	
	public void commit() throws IndexerException {
		getIndex().commit();
	}
	
	public Cube getCube() {
		return getIndex().getCube();
	}
	
	public Serializable getId() {
		return null;
	}
	
	public void setId(Serializable id) {
	}
	
	public <T extends ObjectService> T getService(Class<T> service) throws ServiceNotFoundException {
		return null;
	}

	public User getLastModifiedUser() {
		return null;
	}
	
	public Date getLastModifiedOffsertDate() {
		return null;
	}
											
	public void setLastModifiedDate(Date date) {
	}
	
	public void setLastModifiedUser(User user) {
	}
	
	public void setState(ObjectState enabled) {
	}
	
	public ObjectState getState() {
		return null;
	}
	
	public JavaIndex getIndex() {
		if (index==null) 
			index = (JavaIndex)getFactory().getIndex(getDomain());
		return index;
	} 
	
	public Domain getDomain() {
		return domain;
	}
	
	public JavaIndexFactory getFactory() {
		return (JavaIndexFactory)ServiceLocator.getService(BeansService.class).getBean(factoryName);
	}
	
	protected void setDomain(Domain domain) {
		this.domain = domain;
	}
	
	protected void setFactory(JavaIndexFactory factory) {
		this.factoryName = factory.getName();
	}
	
	protected void setIndex(Index index) {
		this.index = (JavaIndex)index;
	}
	
	@Override
	public void setLastModifiedOffsetDateTime(OffsetDateTime date) {
	}

	@Override
	public OffsetDateTime getLastModifiedOffsetDateTime() {
		return null;
	}

	@Override
	public OffsetDateTime getCreationOffsetDateTime() {
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getLastModifiedOffsetDateTimeColloquial(String css) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getCreationOffsetDateTimeColloquial() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDisplayName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setCreationOffsetDateTime(OffsetDateTime date) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDefaultAudit() {
		// TODO Auto-generated method stub
		
	}
}
