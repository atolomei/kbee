package com.novamens.kbee.content.indexer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.BeanNameAware;

import com.novamens.beans.BeansService;

import com.novamens.content.base.ContentClass;
import com.novamens.content.base.DomainProxy;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.event.EventsDispatcher;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.AttributeTemplate;
import com.novamens.content.model.Classificable;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetType;
import com.novamens.content.model.ModelObject;
import com.novamens.content.security.ContentSecurityDao;
import com.novamens.content.security.EntityRole;
import com.novamens.content.security.Role;
import com.novamens.dom.Domain;
import com.novamens.event.Event;
import com.novamens.event.EventListener;
import com.novamens.indexer.java.ConstantFieldSchema;
import com.novamens.indexer.java.CustomFieldSchema;
import com.novamens.indexer.java.DocumentSchema;
import com.novamens.indexer.java.Extractor;
import com.novamens.indexer.java.FieldSchema;
import com.novamens.indexer.query.Facet;
import com.novamens.indexer.service.Cube;
import com.novamens.indexer.service.Index;
import com.novamens.indexer.service.ObjectBuilder;
import com.novamens.kbee.content.event.AppModelUpdateEvent;
import com.novamens.kbee.content.multidimensional.AttributeExtractor;
import com.novamens.kbee.content.multidimensional.AttributeFacet;
import com.novamens.kbee.content.multidimensional.AttributeValueExtractor;
import com.novamens.kbee.content.multidimensional.ClassificationDisplayNameExtractor;
import com.novamens.kbee.content.multidimensional.ClassificationExtractor;
import com.novamens.kbee.content.multidimensional.ClassifierHierarchicalFacet;
import com.novamens.kbee.content.multidimensional.EntityRolesExtractor;
import com.novamens.kbee.content.multidimensional.HierarchicalNodeUpdateListener;
import com.novamens.kbee.content.multidimensional.ParentsDisplayNameExtractor;
import com.novamens.kbee.content.multidimensional.RuleClassificationExtractor;
import com.novamens.kbee.content.multidimensional.SortableValueExtractor;
import com.novamens.service.ServiceLocator;
import com.novamens.solr.indexer.multidimensional.SolrCube;
import com.novamens.solr.indexer.service.SolrCore;
import com.novamens.solr.indexer.service.SolrIndex;
import com.novamens.util.KbeeRuntimeException;

/**
 * 
content-index.xml the class schema
  *
  * For classes that are not content:
  * JavaContentIndexFactory
  * List <DocumentSchema> getSchemas (Index index, Domain domain) {
  * 
  *  
  *  * 
 * content-index-context.xml
 * 
 */			
public class JavaContentIndexFactory implements JavaIndexFactory, EventListener, BeanNameAware  {
			
	private String beanName;
	private SolrCore solrcore;
	private EventsDispatcher memberUpdateListener;
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(JavaContentIndexFactory.class.getName());
	
	private Map<Serializable, Index> cache = Collections.synchronizedMap(new HashMap<Serializable, Index>());
	
	public Index getIndex(Domain domain) {
		Index index = cache.get(domain.getId());
		if (index == null) {
			synchronized (domain) {
				index = createIndex(domain);
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
	
	public void setMemberUpdateListener(EventsDispatcher listener) {
		memberUpdateListener = listener;
	}
	
	public EventsDispatcher getMemberUpdateListener() {
		return memberUpdateListener;
	}
	
	
/**
* 
* <p>Index schema cache
* What event can change that index scheme
*
* eSchema solr of a content class
* when modifying a classifier of the content class
* the solr schema of the related class changes	 
* </p>
*  
*/
	public boolean listen(Event event) {
		return (event.getObject() instanceof AppModelUpdateEvent || 
				event.getObject() instanceof Classifier || 
				event.getObject() instanceof Attribute || 
				event.getObject() instanceof DataSet || 
				event.getObject() instanceof Domain || // ver si hay que sacarlo 
				event.getObject() instanceof ContentTemplate);
	}
	
	public void onEvent(Event event) {
		Serializable domainid = null;
		if (event instanceof AppModelUpdateEvent) {
			if (((AppModelUpdateEvent)event).getObject() instanceof Domain) {
				Domain domain = (Domain)((AppModelUpdateEvent)event).getObject();
				domainid = domain.getId();
			}
		}
		else
		if (event.getObject() instanceof ModelObject) {
			try {
				domainid = ((ModelObject)event.getObject()).getDomain().getId();
			} 
			catch (NullPointerException e) {
				logger.error(e);
				throw new KbeeRuntimeException ("Likely the ModelObject Domain is null.");
			}
		}	
		else {
			if (event.getObject() instanceof Domain) {
				domainid = ((Domain)event.getObject()).getId();
			}
			else {
				domainid = ((ContentTemplate)event.getObject()).getDomain().getId();
			}
		}
		
		Index index = cache.get(domainid);
		if (index!=null && index instanceof SolrIndex) {
			for (DocumentSchema documentschema : ((SolrIndex)index).getSchemas()) {
				for (FieldSchema fieldschema : documentschema.getFieldsSchemas()) {
					if (fieldschema.getExtractor() instanceof EventListener) {
						getMemberUpdateListener().removeListener((EventListener)fieldschema.getExtractor());
					}
				}
			}
		}
		
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
		index.setCube((SolrCube)createCube(domain));
		index.setObjectBuilder(getObjectBuilder());
		return index;
	} 
	
	@SuppressWarnings("unused")
	private Index getIndexBean(Domain domain) {
 		if (!ServiceLocator.getService(BeansService.class).containsBean(domain.getName()+"-index"))
			return null;
		else
			return (Index)ServiceLocator.getService(BeansService.class).getBean(domain.getName()+"-index");
	}
	
	/**
	 * 
	 * Content Class are dynamically added
	 * 
	 * @param index
	 * @param domain
	 * @return
	 */
	private List<DocumentSchema> getSchemas(Index index, Domain domain) {
		List<DocumentSchema> schemas = new ArrayList<DocumentSchema>();
		
		for (ContentClass contentclass : getClasses(domain)) {
			schemas.add(createSchema(index, contentclass, domain));
		}
		
		schemas.add(createFileSchema(domain));
		schemas.add(createDataSetMemberSchema(index, domain));
		schemas.add(createGroupSchema(domain));
		schemas.add(createBillboardSchema(domain));
		schemas.add(createSecurityRuleSchema(domain));
		
		
		schemas.add(createRoleSchema(domain));
		schemas.add(createDomainSchema(domain));
		schemas.add(createTreeFileSchema(domain));
		
		schemas.add(createProgressNoteSchema(domain));
		
		return schemas;
	}
	
	private DocumentSchema createSchema(Index index, ContentClass contentclass, Domain domain) {
		DocumentSchema schema = getDefaultSchema(contentclass);
		
		if (isClassificable(contentclass))
		for (Classifier classifier : getContentDao().getClassifiers(domain)) {
			
			if (classifier.getUniqueName()!=null) {
				CustomFieldSchema fieldschema = new CustomFieldSchema();
			
				
				fieldschema.setFieldName(classifier.getUniqueName()+"member");
				
				Extractor extractor = new ClassificationExtractor(classifier);
				fieldschema.setExtractor(extractor);
				schema.addFieldSchema(fieldschema);
				if (classifier.isOrdered()) {
					CustomFieldSchema fieldnameschema = new CustomFieldSchema();
					fieldnameschema.setFieldName(classifier.getUniqueName()+"name");
					ClassificationDisplayNameExtractor nameextractor = new ClassificationDisplayNameExtractor();
					nameextractor.setClassifier(classifier);
					nameextractor.setIdFieldName(classifier.getUniqueName()+"member");
					nameextractor.setNameFieldName(classifier.getUniqueName()+"name");
					nameextractor.setType(getType(schema));
					nameextractor.setIndex(new IndexProxy(index, this, new DomainProxy(domain)));
					nameextractor.setEventsDispatcher(getMemberUpdateListener());
					fieldnameschema.setExtractor(nameextractor);
					schema.addFieldSchema(fieldnameschema);
				}
				if (classifier.isHierarchical()) {
					CustomFieldSchema fieldnameschema = new CustomFieldSchema();
					fieldnameschema.setFieldName("tags");
					ParentsDisplayNameExtractor nameextractor = new ParentsDisplayNameExtractor();
					nameextractor.setClassifier(classifier);
					nameextractor.setIdFieldName(classifier.getUniqueName()+"member");
					nameextractor.setNameFieldName("tags");
					nameextractor.setType(getType(schema));
					nameextractor.setIndex(new IndexProxy(index, this, new DomainProxy(domain)));
					nameextractor.setEventsDispatcher(getMemberUpdateListener());
					fieldnameschema.setExtractor(nameextractor);
					schema.addFieldSchema(fieldnameschema);
				}
			}
		}
		
		Class<?> javaclass = null;
		try {
			javaclass = Class.forName(contentclass.getJavaClass());
		}
		catch (ClassNotFoundException e) {
			
		}
		
		if (javaclass!=null && Classificable.class.isAssignableFrom(javaclass)) {
			for (Attribute attribute : getContentDao().getAttributes(domain)) {
				if (attribute.getUniqueName()!=null) {
					CustomFieldSchema valuefieldschema = new CustomFieldSchema();
					valuefieldschema.setFieldName(attribute.getUniqueName()+"name");
					AttributeValueExtractor valueextractor = new AttributeValueExtractor(attribute);
					valueextractor.setMaxChars(25000);
					valuefieldschema.setExtractor(valueextractor);
					schema.addFieldSchema(valuefieldschema);
					if (attribute.isDate() || attribute.isFilterable()) {
						CustomFieldSchema memberfieldschema = new CustomFieldSchema();
						memberfieldschema.setFieldName(attribute.getUniqueName()+"member");
						AttributeExtractor memberextractor = new AttributeExtractor(attribute);
						memberfieldschema.setExtractor(memberextractor);						
						schema.addFieldSchema(memberfieldschema);
					}
					if (attribute.isOrdered()) {
						CustomFieldSchema sortablefieldschema = new CustomFieldSchema();
						sortablefieldschema.setFieldName(attribute.getUniqueName()+"name_sort");
						SortableValueExtractor sortablevalueextractor = new SortableValueExtractor(attribute);
						sortablevalueextractor.setMaxChars(256);
						sortablefieldschema.setExtractor(sortablevalueextractor);
						schema.addFieldSchema(sortablefieldschema);
					}
				}
			}
		}
		
		return schema;
	}
	
	private boolean isClassificable(ContentClass contentclass) {
		try {
			Class<?> clazz = Class.forName(contentclass.getJavaClass());
			Object instance = clazz.newInstance();
			return instance instanceof Classificable;
		}
		catch (ClassNotFoundException e) {
			logger.error(e);
			throw new RuntimeException(e);
		}
		catch (InstantiationException e) {
			logger.error(e);
			throw new RuntimeException(e);
		}
		catch (IllegalAccessException e) {
			logger.error(e);
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * 
	 * Agrega el displayname en el document solr que indexa
	 * 
	 * @param index
	 * @param domain
	 * @return
	 */
	private DocumentSchema createDataSetMemberSchema(Index index, Domain domain) {

		try {
				
			
			DocumentSchema schema = getDefaultDataSetMemberSchema();
				
				for (Classifier classifier : getDataSetsRelations(domain)) {
					
					CustomFieldSchema fieldschema = new CustomFieldSchema();
					fieldschema.setFieldName(classifier.getUniqueName()+"member");
					Extractor extractor = new ClassificationExtractor(classifier);
					fieldschema.setExtractor(extractor);
					schema.addFieldSchema(fieldschema);
					if (classifier.isOrdered()) {
						CustomFieldSchema fieldnameschema = new CustomFieldSchema();
						fieldnameschema.setFieldName(classifier.getUniqueName()+"name");
		
						/**
						 * saca el displayname del clasificador relacionado y lo agrega en el doc solr
						 * estamos "importando un campo"
						 *  ademas funciona como listener del evento de actualizacion del clasificador relacionado
						 **/
						ClassificationDisplayNameExtractor nameextractor = new ClassificationDisplayNameExtractor();
						
						nameextractor.setClassifier(classifier);
						nameextractor.setIdFieldName(classifier.getUniqueName()+"member");
						nameextractor.setNameFieldName(classifier.getUniqueName()+"name");
						nameextractor.setType(getType(schema));
						nameextractor.setIndex(new IndexProxy(index, this, new DomainProxy(domain)));
						nameextractor.setEventsDispatcher(getMemberUpdateListener());
						fieldnameschema.setExtractor(nameextractor);
						schema.addFieldSchema(fieldnameschema);
					}
					
					if (classifier.isHierarchical()) {
						HierarchicalNodeUpdateListener listener = new HierarchicalNodeUpdateListener(classifier);
						listener.setIndex(new IndexProxy(index, this, new DomainProxy(domain)));
						getMemberUpdateListener().addListener(listener);
						
						CustomFieldSchema fieldnameschema = new CustomFieldSchema();
						fieldnameschema.setFieldName("tags");
						ParentsDisplayNameExtractor nameextractor = new ParentsDisplayNameExtractor();
						nameextractor.setClassifier(classifier);
						nameextractor.setIdFieldName(classifier.getUniqueName()+"member");
						nameextractor.setNameFieldName("tags");
						nameextractor.setType(getType(schema));
						nameextractor.setIndex(new IndexProxy(index, this, new DomainProxy(domain)));
						nameextractor.setEventsDispatcher(getMemberUpdateListener());
						fieldnameschema.setExtractor(nameextractor);
						schema.addFieldSchema(fieldnameschema);
					}
					
					if (classifier.getDataSet().getDataSetType().equals(DataSetType.ENTITY)) {
						fieldschema = new CustomFieldSchema();
						fieldschema.setFieldName(classifier.getUniqueName()+"role");
						extractor = new EntityRolesExtractor(classifier);
						fieldschema.setExtractor(extractor);
						schema.addFieldSchema(fieldschema);
					}
				}
				
				for (AttributeTemplate template : getDataSetsAttributes(domain)) {
					Attribute attribute = template.getAttribute();
					if (attribute.getUniqueName()!=null) {
						CustomFieldSchema valuefieldschema = new CustomFieldSchema();
						valuefieldschema.setFieldName(attribute.getUniqueName()+"name");
						AttributeValueExtractor valueextractor = new AttributeValueExtractor(attribute);
						valuefieldschema.setExtractor(valueextractor);
						schema.addFieldSchema(valuefieldschema);
						if (attribute.isDate() || attribute.isFilterable()) {
							CustomFieldSchema memberfieldschema = new CustomFieldSchema();
							memberfieldschema.setFieldName(attribute.getUniqueName()+"member");
							AttributeExtractor memberextractor = new AttributeExtractor(attribute);
							memberfieldschema.setExtractor(memberextractor);						
							schema.addFieldSchema(memberfieldschema);
						}
					}
				}
		
				return schema;
		}
		catch (Exception e) {
			logger.error(e);
			throw e;
		}
	}
	
	private DocumentSchema createFileSchema(Domain domain) {
		DocumentSchema schema = getDefaultFileSchema();
		return schema;
	}
	
	private DocumentSchema createSecurityRuleSchema(Domain domain) {
		DocumentSchema schema = getSecurityRuleSchema();
		for (Classifier classifier : getClassifiers(domain)) {
			if (classifier.getUniqueName()!=null) {
				CustomFieldSchema fieldschema = new CustomFieldSchema();
				fieldschema.setFieldName(classifier.getUniqueName()+"member");
				Extractor extractor = new RuleClassificationExtractor(classifier);
				fieldschema.setExtractor(extractor);
				schema.addFieldSchema(fieldschema);
			}
		}
		return schema;
	}
	
						
	private DocumentSchema createRoleSchema(Domain domain) {
		DocumentSchema schema = getDefaultRoleSchema();
		return schema;
	}
	
	private DocumentSchema createDomainSchema(Domain domain) {
		DocumentSchema schema = getDefaultDomainSchema();
		return schema;
	}
										
	private DocumentSchema createTreeFileSchema(Domain domain) {
		DocumentSchema schema = getDefaultTreeFileSchema();
		return schema;
	}
	
	private DocumentSchema createProgressNoteSchema(Domain domain) {
		DocumentSchema schema = getDefaultProgressNoteSchema();
		return schema;
	}
	
	private DocumentSchema createGroupSchema(Domain domain) {
		DocumentSchema schema = getDefaultGroupSchema();
		return schema;
	}
	
	private DocumentSchema createBillboardSchema(Domain domain) {
		DocumentSchema schema = getDefaultBillboardSchema();
		return schema;
	}
	
	private Set<Classifier> getDataSetsRelations(Domain domain) {
		Set<Classifier> classifiers = new HashSet<Classifier>();
		for (DataSet dataset : getContentDao().getDataSets(domain)) {
			classifiers.addAll(dataset.getClassifiers());
		}
			for (Role role : getContentSecurityDao().getRoles(domain)) {
				if (role.isEntity()) {
					EntityRole entityrole = (EntityRole)getContentDao().reload(role);
					Classifier classifier = entityrole.getClassifier();
					if (classifier!=null)
					classifiers.add(classifier);
				}
			}
		return classifiers;
	}
	
	private Set<AttributeTemplate> getDataSetsAttributes(Domain domain) {
		Set<AttributeTemplate> templates = new HashSet<AttributeTemplate>();
		for (DataSet dataset : getContentDao().getDataSets(domain)) {
			templates.addAll(dataset.getAttributes());
		}
		return templates;
	}
	
	private List<Classifier> getClassifiers(Domain domain) {
		return getContentDao().getClassifiers(domain);
	}
	
	private String getType(DocumentSchema schema) {
		for (FieldSchema field :schema.getFieldsSchemas()) {
			if ("type".equals(field.getFieldName())) {
				if (field instanceof ConstantFieldSchema) {
					String type = ((ConstantFieldSchema)field).getValue();
					return type;
				}
			}
		}
		return null;
	}
	
	private Set<ContentClass> getClasses(Domain domain) {
		Set<ContentClass> classes = new HashSet<ContentClass>();
		for (ContentClass contentclass : getContentDao().getClasses()) {
			if (contentclass.isIndexable())
			classes.add(contentclass);
		};
		return classes;
	}
	
	private Cube createCube(Domain domain) {
		SolrCube cube = getDefaultCube();
		List<Facet> facets = new ArrayList<Facet>();
		facets.addAll(cube.getFacets());
		for (Classifier classifier : getContentDao().getClassifiers(domain)) {
			if (classifier.getDataSet()!=null && classifier.getUniqueName()!=null && classifier.getDataSet().isHierachical()) {
				ClassifierHierarchicalFacet facet = new ClassifierHierarchicalFacet();
				facet.setDisplayName(classifier.getName());
				facet.setName(classifier.getUniqueName()+"member");
				facet.setOrder(classifier.getOrder());
				facet.setNavigable(true);
				facet.setClassifier(classifier);
				facet.setFilterable(true);
				facets.add(facet);
			}
			else {
				if (classifier.getDataSet()!=null && classifier.getUniqueName()!=null) {
					ClassifierHierarchicalFacet facet = new ClassifierHierarchicalFacet();
					facet.setDisplayName(classifier.getName());
					facet.setName(classifier.getUniqueName()+"member");
					facet.setOrder(classifier.getOrder());
					facet.setClassifier(classifier);
					facet.setFilterable(true);
					facets.add(facet);
				}
				if (classifier.getDataSet()!=null 
					&& classifier.getDataSet().getDataSetType().equals(DataSetType.ENTITY)	
					&& classifier.getUniqueName()!=null) {
					ClassifierHierarchicalFacet facet = new ClassifierHierarchicalFacet();
					facet.setDisplayName(classifier.getName() + " Roles");
					facet.setName(classifier.getUniqueName()+"role");
					facet.setOrder(classifier.getOrder());
					facet.setClassifier(classifier);
					facet.setFilterable(true);
					facets.add(facet);
				}
			}
		}	
		
		for (Attribute attribute : getContentDao().getAttributes(domain)) {
			if (attribute.isFilterable()) {
				AttributeFacet facet = new AttributeFacet();
				facet.setDisplayName(attribute.getName());
				facet.setName(attribute.getUniqueName()+"member");
				facet.setOrder(attribute.getOrder());
				facet.setAttribute(attribute);
				facet.setFilterable(true);
				facets.add(facet);
			}
		}	
		
 		Collections.sort(facets, new Comparator<Facet>() {
			@Override
			public int compare(Facet a, Facet b) {
				try {
					return a.getDisplayName().compareTo(b.getDisplayName());
				} 
				catch (Exception e) {
					logger.error(e);
					return 0;
				}
			}
		}); 
		cube.setFacets(facets);
		return cube;
	}
	
	private ContentDao getContentDao() {
		BeansService beans = ServiceLocator.getService(BeansService.class);
		ContentDao dao = (ContentDao) beans.getBean("contentDao");
		return dao;
	}
	
	private ContentSecurityDao getContentSecurityDao() {
		return (ContentSecurityDao)ServiceLocator.getService(BeansService.class).getBean("contentSecurityDao");
	}
	
	private SolrCube getDefaultCube() {
		return (SolrCube)ServiceLocator.getService(BeansService.class).getBean("default-content-cube");
	}
	
	private DocumentSchema getDefaultSchema(ContentClass contentclass) {
		return (DocumentSchema)ServiceLocator.getService(BeansService.class).getBean("default-"+contentclass.getName().toLowerCase()  +"-schema");
	}
	
	private DocumentSchema getDefaultDataSetMemberSchema() {
		return (DocumentSchema)ServiceLocator.getService(BeansService.class).getBean("default-datasetmember-schema");
	}
	
	private DocumentSchema getDefaultFileSchema() {
		return (DocumentSchema)ServiceLocator.getService(BeansService.class).getBean("default-kbfile-schema");
	}
	
	private DocumentSchema getDefaultTreeFileSchema() {
		return (DocumentSchema)ServiceLocator.getService(BeansService.class).getBean("default-treefile-schema");
	}
	
	private DocumentSchema getDefaultProgressNoteSchema() {
		return (DocumentSchema)ServiceLocator.getService(BeansService.class).getBean("default-progressnote-schema");
	}
	
	private ObjectBuilder getObjectBuilder() {
		return (ObjectBuilder)ServiceLocator.getService(BeansService.class).getBean("object-builder");
	}
	
	private DocumentSchema getSecurityRuleSchema() {
		return (DocumentSchema)ServiceLocator.getService(BeansService.class).getBean("default-securityrule-schema");
	}
	
	private DocumentSchema getDefaultDomainSchema() {
		return (DocumentSchema)ServiceLocator.getService(BeansService.class).getBean("default-domain-schema");
	}
	
	private DocumentSchema getDefaultRoleSchema() {
		return (DocumentSchema)ServiceLocator.getService(BeansService.class).getBean("default-role-schema");
	}
	
	private DocumentSchema getDefaultGroupSchema() {
		return (DocumentSchema)ServiceLocator.getService(BeansService.class).getBean("default-group-schema");
	}
	
	private DocumentSchema getDefaultBillboardSchema() {
		return (DocumentSchema)ServiceLocator.getService(BeansService.class).getBean("default-billboard-schema");
	}
}
