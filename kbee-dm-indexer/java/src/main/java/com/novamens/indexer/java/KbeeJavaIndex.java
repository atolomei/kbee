package com.novamens.indexer.java;



import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.BeanNameAware;

import com.novamens.beans.BeansService;
import com.novamens.indexer.service.Document;
import com.novamens.indexer.service.IndexerException;
import com.novamens.indexer.service.JavaIndex;
import com.novamens.indexer.service.ObjectBuilder;
import com.novamens.metrics.SystemMetricsService;
import com.novamens.service.ServiceLocator;

public abstract class KbeeJavaIndex extends com.novamens.dom.AbstractTransientObject implements JavaIndex, BeanNameAware {
	private Map<Class<?>,DocumentSchema> schemas;
	private Set<Class<?>> noschemas = new HashSet<Class<?>>();
	private String name;
	private ObjectBuilder objectBuilder;
	private SessionFactory sessionFactory;
														
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeJavaIndex.class.getName());
	
	public void index(Object object) throws IndexerException {
		index(object, true, false);
	}
	
	public void index(Object object, boolean onlymetainfo) throws IndexerException {
		index(object, onlymetainfo, false);
	}
	
	public void index(Object object, boolean onlymetainfo, boolean aggregations) throws IndexerException {
		index(object, onlymetainfo, aggregations, false);
	}
	
	public void index(Object object, boolean onlymetainfo, boolean aggregations, boolean force) throws IndexerException {
		if (isIndexable(object, aggregations)) {
			if (onlymetainfo) {
				indexDocument(buildDocument(object, true), force);
				ServiceLocator.getService(SystemMetricsService.class).getMeterIndexMetainfoTasks().mark();
			}
			else {
				OffsetDateTime version = getVersion(object);
				Document document = buildDocument(object, false);
				// lock
				if (version==null || sameversion(version, getVersion(reload(object)))) {
//				if (version==null || version.equals(getVersion(reload(object)))) {
					indexDocument(document, force);
					ServiceLocator.getService(SystemMetricsService.class).getMeterIndexAttachmentsTasks().mark();
				}
				else {
					logger.warn("version check fail : " + object !=null ? object.toString() : "");
				}
				// unlock
			}
		}	
		else {
			logger.debug("not indexable-> " + object.toString());
		}
	}
	
	public void reindex(Object object, String...fields) throws IndexerException {
		OffsetDateTime version = getVersion(object);
		Document document = buildDocument(object, fields);
		if (version==null || sameversion(version, getVersion(reload(object)))) {
		//if (version==null || version.equals(getVersion(reload(object)))) {
			reindexDocument(document, fields);
		}
		else {
			logger.warn("version check fail : " + object !=null ? object.toString() : "");
		}	
	}
	
	public abstract void indexDocument(Document document, boolean force) throws IndexerException;
	
	public abstract void indexDocument(Document document) throws IndexerException;
	
	public void setSchemas(List<DocumentSchema> schemas) {
		this.schemas = new HashMap<Class<?>, DocumentSchema>();
		for (DocumentSchema schema : schemas) {
			this.schemas.put(schema.getJavaClass(), schema);
		}
	}
	
	public List<DocumentSchema> getSchemas() {
		return new ArrayList<DocumentSchema>(schemas.values());
	}
	
	public boolean isIndexable(Object object) {
		return isIndexable(object, false);
	}
	
	public boolean isIndexable(Object object, boolean aggregations) {
		DocumentSchema schema = getSchema(object);
		return object!=null && schema!=null && (aggregations || !schema.isAggregation());
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setBeanName(String name) {
		this.name = name;
	}
	
	@Override
	public String getName() {
		return this.name;
	}
	
	public void setObjectBuilder(ObjectBuilder builder) {
		this.objectBuilder = builder;
	}
	
	public ObjectBuilder getObjectBuilder() {
		return this.objectBuilder;
	}
	
	public Document buildDocument(Object object, boolean onlymetainfo) throws IndexerException {
		
		DocumentBuilder db = getDocumentBuilder(getSchema(object));
		if (db==null)
			return null;
		return db.build(object, onlymetainfo);
	}
	
	public Document buildDocument(Object object, String...field) throws IndexerException {
		return getDocumentBuilder(getSchema(object)).build(object, field);
	}
	
	protected DocumentSchema getSchema(Object object) {
		DocumentSchema schema = schemas.get(object.getClass());
		if (schema==null) {
			if (!noschemas.contains(object.getClass())) {
				for (Class<?> javaclass : schemas.keySet()) {
					if (javaclass.isAssignableFrom(object.getClass())) {
						schema = schemas.get(javaclass);
						schemas.put(object.getClass(), schema);
						break;
					}
				}
				if (schema==null) {
					noschemas.add(object.getClass());
				}
			}
		}
		return schema;
	}
	
	protected DocumentBuilder getDocumentBuilder(DocumentSchema schema) {
		return new DocumentBuilder(schema);
	}
	
	
	protected OffsetDateTime getVersion(Object object) {
		return object instanceof com.novamens.dom.Object ? ((com.novamens.dom.Object)object).getLastModifiedOffsetDateTime() : null;
	}
	
	// as precision in nanos differs between jvm and base it is necessary to do the following
	protected boolean sameversion(OffsetDateTime version1, OffsetDateTime version2) {
		if (version1.equals(version2)) return true;
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern ("yyyy-MM-dd'T'HH:mm:ss");
		String version1string = formatter.format(version1);
		String version2string = formatter.format(version2);
		if (version1string.equals(version2string)) {
			int n1 = version1.getNano();
			int n2 = version2.getNano();
			int d = n1>n2 ? n1-n2 : n2-n1;
			return d<999;
		}
		return false;
	}
	
	protected Object reload(Object object) {
		getCurrentSession().refresh(object);
		return object;
	}
	
	protected Session getCurrentSession() {
		if (sessionFactory == null)
			sessionFactory = (SessionFactory)ServiceLocator.getService(BeansService.class).getBean("sessionFactory");
		return sessionFactory.getCurrentSession();
	}
}