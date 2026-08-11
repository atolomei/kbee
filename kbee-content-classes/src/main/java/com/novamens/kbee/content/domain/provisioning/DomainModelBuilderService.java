package com.novamens.kbee.content.domain.provisioning;


import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


import com.novamens.content.base.ContentCreationException;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.model.AccessStrategy;
import com.novamens.content.model.AttributeTemplate;
import com.novamens.content.model.AttributeType;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ClassifierTemplate;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetElementTemplate;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.DataSetType;
import com.novamens.content.model.LauncherGroup;
import com.novamens.content.model.Multiplicity;
import com.novamens.content.service.DOMObjectService;
import com.novamens.content.service.ObjectFactoryService;
import com.novamens.content.service.SecurityContentMgmtService;
import com.novamens.content.workflow.ProcessLauncher;
import com.novamens.content.workflow.WorkflowDomainService;
import com.novamens.dom.Domain;
import com.novamens.dom.DomainType;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.content.model.KbeeAttribute;
import com.novamens.kbee.content.model.KbeeAttributeTemplate;
import com.novamens.kbee.content.model.KbeeClassifier;
import com.novamens.kbee.content.model.KbeeClassifierTemplate;
import com.novamens.kbee.content.model.KbeeContentTemplate;
import com.novamens.kbee.content.model.KbeeDataSet;
import com.novamens.kbee.content.model.KbeeDataSetElementTemplate;
import com.novamens.kbee.content.model.KbeeDataSetMember;
import com.novamens.kbee.content.model.KbeeEntitySet;
import com.novamens.kbee.content.model.KbeeLabelSet;
import com.novamens.kbee.content.model.KbeeValueSet;
import com.novamens.kbee.content.workflow.KbeeProcessLauncher;

import com.novamens.kbee.security.acl.KbeeAcl;
import com.novamens.kbee.security.acl.KbeeAclEntry;
import com.novamens.kbee.security.acl.KbeePermission;
import com.novamens.logging.DataSetValueCreateEvent;
import com.novamens.logging.ModelCreateEvent;
import com.novamens.logging.ModelUpdateEvent;
import com.novamens.security.User;
import com.novamens.security.acl.AclEntry;
import com.novamens.security.acl.Group;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.security.acl.Permission;
import com.novamens.service.ObjectService;
import com.novamens.service.ServiceLocator;
import com.novamens.workflow.Procedure;


/**
 * Model
 */
public class DomainModelBuilderService extends BaseDomainBuilder implements ObjectService {

	/** Logger that works synchronously in the TRX thread */
	static private Logger txlogger = LogManager.getLogger("TxLogger");
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(DomainModelBuilderService.class.getName());
	
	boolean createMembers = true;
	boolean isStandard = false; 
	boolean isCompliance = false; 
	boolean isAssign = true;

	private KbeeDataSet d_site_project;
	private KbeeClassifier c_site_project = null;
	
	private KbeeDataSet d_type;
	private KbeeDataSet d_status;
	//private DataSet d_user_list;
	private KbeeEntitySet d_secured_access;
	private KbeeDataSet d_property;
	//private KbeeEntitySet d_tag;
	private KbeeEntitySet d_department;
	//private KbeeDataSet d_unit = null;
	
	private KbeeClassifier c_type = null;
	private KbeeClassifier c_property = null;
	private KbeeClassifier c_status = null;
	private KbeeClassifier c_tag = null;
	private KbeeClassifier c_department = null;
	private KbeeClassifier c_secured_access = null;
	
	private KbeeClassifier c_unit = null;
	
	
	//private Classifier c_user_list = null;
	
	// KbeeAttribute at_unit = null;
	//ContentTemplate content_template_onesite_file;	
	
	private KbeeAttribute at_effective_date = null;
	private KbeeAttribute at_create_date = null;
	private KbeeAttribute at_fileid = null;

	private List<Classifier> system_properties_classifiers = new ArrayList<Classifier>();
	
	

	public DomainModelBuilderService() {
	}
	
	public DomainModelBuilderService(Domain domain) {
		super(domain);
	}

	/**
	 * basic 
	 * Premium-api (assign) same as basic 
	 * 
	 * @throws ContentMgmtException
	 * @throws ContentCreationException
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	public void build() throws ContentMgmtException, ContentCreationException {

		logger.debug("build");
		build_datasets(DomainType.EXPRESS.getAlias());
		build_launcher_groups();
		build_content_classes(DomainType.EXPRESS.getAlias());
	}
	

	/**
	 * 
	 * @param imodeltype
	 * @throws ContentMgmtException
	 * @throws ContentCreationException
	 * 
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	public void build(String imodeltype) throws ContentMgmtException, ContentCreationException {
					
		logger.debug("build ->  "+imodeltype);
		
		if (imodeltype.equals("premium-none"))
			return;
		
		build_datasets(imodeltype);
		build_content_classes(imodeltype);
	}
	
	public boolean isCreateMembers() {
		return createMembers;
	}

	public void setCreateMembers(boolean createMembers) {
		this.createMembers = createMembers;
	}


	public boolean isStandard() {
		return isStandard;
	}


	public void setStandard(boolean isStandard) {
		this.isStandard = isStandard;
	}


	public boolean isCompliance() {
		return isCompliance;
	}


	public void setCompliance(boolean isCompliance) {
		this.isCompliance = isCompliance;
	}


	public boolean isAssign() {
		return isAssign;
	}

	public void setAssign(boolean isAssign) {
		this.isAssign = isAssign;
	}

	private void addMemebers(DataSet dataset, String key, String default_values) {
		
		if (this.isCreateMembers()) {
			
			String vals=getContentDao().findSystemParameterValueByKey(key, default_values);
			logger.debug("addMemebers -> " + vals);
			
			String vs[] = vals.split(";");
			
			for (String str: vs )
				addDataSetMember(dataset, str);
		}
	}
	
	
	@Transactional(propagation = Propagation.REQUIRED)
	public ContentTemplate buildResourcesContentTemplate() {
		logger.debug("building Resources ContentTemplate");
		return buildResourcesContentTemplateNoTrx();
	}
	
	
	/**
	 * When called from Async Commands
	 */
	
	public ContentTemplate buildResourcesContentTemplateNoTrx() {
		
		
		for (ContentTemplate t:getContentDao().getTemplates( getBuildingDomain())) {
			if (	 t.getAlias()!=null &&
					 t.getAlias().toLowerCase().equals(ContentTemplate.RESOURCES))
					return t;
		}
		
		for (Classifier c:getContentDao().getClassifiers(getBuildingDomain())) {
			if (c_type==null && c.isContentType()) {
				c_type = (KbeeClassifier) c;
			}
			else if (c_status==null && c.getAlias()!=null &&  c.getAlias().toLowerCase().equals(Classifier.STATUS_CLASSIFIER_ALIAS)) {
				c_status= (KbeeClassifier) c;
			}
						
			else if (c_tag==null && c.getDataSetType()==DataSetType.LABEL) {
				c_tag = (KbeeClassifier) c;
			}
		}
		
		KbeeAcl faclx = new KbeeAcl(); 
		faclx.setCreationOffsetDateTime(OffsetDateTime.now());
		faclx.setLastModifiedUser(getSessionUser());
		faclx.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		List<Group> fgroups = null;
		fgroups = ServiceLocator.getService(SecurityContentMgmtService.class).getDefaultGroups(getBuildingDomain());
		Group fusers = null;
		for (Group group: fgroups) {
			if (group.getName().equals(KbeeGlobalRole.USER.getId())) {
				fusers = group;
				break;
			}
		}
		
		AclEntry fentryx = new KbeeAclEntry(faclx, fusers, false);
		List<Permission> fpermissions = new ArrayList<Permission>();
		fpermissions.add(KbeePermission.CREATE);
		fentryx.setPermissions(fpermissions);
		faclx.addEntry(getSessionUser(), fentryx);
		getContentDao().save(faclx);
		
		// ----------------
		// Resource
		//
		KbeeContentTemplate content_template_mybox = new KbeeContentTemplate();
		
		
		Locale locale = getBuildingDomain().getLocale();
		
		
		content_template_mybox.setName( getLanguageService().getString("resources", locale) );
				
		
		content_template_mybox.setAlias(ContentTemplate.RESOURCES);
		content_template_mybox.setContentClass(getContentDao().findContentClassByName("IDoc"));
		content_template_mybox.setContentClassCode(ContentTemplate.RESOURCES);
		
		content_template_mybox.setDescription(getLanguageService().getString("resources-items", locale));
		content_template_mybox.setOnlyRootEdit(true);
		content_template_mybox.setPrivateNotes(false);
		content_template_mybox.setCustomAttributes(false);
		

		ClassifierTemplate ctf_type=null;
		ClassifierTemplate ctf_tag=null;
		
		logger.debug("c_type  -> " + c_type!=null?c_type:"null");
		logger.debug("c_label -> " + c_tag!=null?c_tag:"null");
		
		if (c_type!=null) {
			DataSet dt= c_type.getDataSet();
			KbeeDataSetMember m=(KbeeDataSetMember) ServiceLocator.getService(ObjectFactoryService.class).createMember(dt);
			m.setStrValue(getLanguageService().getString("resource", locale));
			m.setKey(DataSetMember.TYPE_RESOURCE_KEY);
			m.setSystem(true);
			m.getService(DOMObjectService.class).update("value");
			logger.debug("Created DataSetMember -> " + m.getStrValue());
			ctf_type  = new KbeeClassifierTemplate(c_type, 0);
			ctf_type.setMetadataSubtitle(true);
			((KbeeClassifierTemplate) ctf_type).setAccessibility(AccessStrategy.All);
			content_template_mybox.addClassifier(ctf_type);
		}

	 
		
		if (c_tag!=null) {
			ctf_tag = new KbeeClassifierTemplate(c_tag,2);		
			ctf_tag.setMetadataSubtitle(false);
			((KbeeClassifierTemplate) ctf_tag ).setAccessibility(AccessStrategy.All);
			content_template_mybox.addClassifier(ctf_tag);
		}							
		
	 
		
		content_template_mybox.setDomain(getBuildingDomain());
		content_template_mybox.setLastModifiedUser(getSessionUser());
		content_template_mybox.setInstantiable(false);
		content_template_mybox.setMultimedia(false);
		content_template_mybox.setTemplate(false);
		content_template_mybox.setAcl(faclx);
		content_template_mybox.setResources(true);
		content_template_mybox.setOnlyRootEdit(true);
		
		content_template_mybox.setResourcesLabel(getLanguageService().getString("resources", locale));

		content_template_mybox.setAbstract(true);
		content_template_mybox.setAbstract_label(getLanguageService().getString("text", locale));
		
		content_template_mybox.setPrivateNotes(false);
		content_template_mybox.setLinkResources(false);
		content_template_mybox.setDocument(false);
		
		getContentDao().save(content_template_mybox);
		
//		KbeeModelSection section = new KbeeModelSection(content_template_mybox);
//		getContentDao().save(section);
//		
//		section.setName( getLanguageService().getString("attributes", locale));
//		List<ModelSection> list = new ArrayList<ModelSection>();
//		list.add(section);

		//content_template_mybox.setSections(list);
		getContentDao().save(content_template_mybox);
		txlogger.info(new ModelCreateEvent(content_template_mybox, "create"));
		
		logger.debug("Saved -> " + content_template_mybox.getName());
		
		return content_template_mybox;
		
	}

	
	/**
	 * 
	 * @param imodeltype
	 * 
	 * basic
	 * premium-api
	 * premium-noapi 
	 * 
	 */
	
	private void build_datasets(String imodeltype) {
		
		logger.debug("build_datasets -> " + imodeltype);

		// Effective Date --------------------------------------------------------
		//
		at_effective_date = new KbeeAttribute();
		at_effective_date.setType(AttributeType.DATE);
		at_effective_date.setMultiplicity(Multiplicity.M01);
		at_effective_date.setName(getContentDao().findSystemParameterValueByKey("attribute_effective_date.name", "Effective Date"));
		at_effective_date.setUniqueName("attr01");  // must be on of the solr predefined (schema.xml) attr01 - attr15
		at_effective_date.setDomain(getBuildingDomain());
		at_effective_date.setMetadataSubtitle(false);
		at_effective_date.setLastModifiedUser(getSessionUser());
		at_effective_date.setAlias(makeAlias(at_effective_date.getName()));
		getContentDao().save(at_effective_date);
		

		// Create Date --------------------------------------------------------
		//
		at_create_date = new KbeeAttribute();
		at_create_date.setType(AttributeType.DATE);
		at_create_date.setMultiplicity(Multiplicity.M01);
		at_create_date.setName(getContentDao().findSystemParameterValueByKey("attribute_create_date.name", "Create Date"));
		at_create_date.setUniqueName("attr02");  // must be on of the solr predefined (schema.xml) attr01 - attr15
		at_create_date.setDomain(getBuildingDomain());
		at_create_date.setMetadataSubtitle(false);
		at_create_date.setLastModifiedUser(getSessionUser());
		at_create_date.setAlias(makeAlias(at_create_date.getName()));
		getContentDao().save(at_create_date);

					
		
		// Fileid --------------------------------------------------------
		//
		at_fileid = new KbeeAttribute();
		at_fileid.setType(AttributeType.STRING);
		at_fileid.setMultiplicity(Multiplicity.M01);									
		at_fileid.setName(getContentDao().findSystemParameterValueByKey("attribute_fileid.name", "FileID"));
		at_fileid.setUniqueName("attr04");  // must be on of the solr predefined (schema.xml) attr02 - attr15
		at_fileid.setDomain(getBuildingDomain());
		at_fileid.setMetadataSubtitle(false);
		at_fileid.setLastModifiedUser(getSessionUser());
		at_fileid.setAlias(makeAlias(at_fileid.getName()));
		getContentDao().save(at_fileid);

		
		// DataSet: Site Project ---------------------------------------------------------
								
		d_site_project = new KbeeEntitySet();
		d_site_project.setDomain(getBuildingDomain());
		d_site_project.setCanonical(true);
		d_site_project.setReadonly(true);
		d_site_project.setName(getContentDao().findSystemParameterValueByKey("dataset_site_project.name", "Portal Project"));
		d_site_project.setAlias(DataSet.PORTAL_PROJECTS);
		d_site_project.setAccessStrategy(AccessStrategy.All);
		d_site_project.setLastModifiedUser(getSessionUser());
		getContentDao().save(		d_site_project);
		txlogger.info(new ModelCreateEvent(d_site_project, "create"));

		
		
		// Classifier: Site Project
		//
		c_site_project = new KbeeClassifier();
		c_site_project.setDomain(getBuildingDomain());
		c_site_project.setName(d_site_project.getName());
		c_site_project.setSearchable(true);
		c_site_project.setAPIClassifier(true);
		c_site_project.setUniqueName(Classifier.PORTAL_PROJECTS_SOLR); // tiene que ser consistente con el esquema solr fijo en schema.xml
		c_site_project.setPredicate(Classifier.PORTAL_PROJECTS_PREDICATE);
		c_site_project.setMultiplicity(Multiplicity.M0N);
		c_site_project.setContentType(false);
		c_site_project.setMetadataSubtitle(false);
		c_site_project.setRuleCondition(true);
		c_site_project.addDataSet(d_site_project);
		c_site_project.setAlias(d_site_project.getAlias());
		c_site_project.setLastModifiedUser(getSessionUser());
		getContentDao().save(c_site_project);
		txlogger.info(new ModelCreateEvent(c_property, "create"));
		
		
		// DataSet: Type ---------------------------------------------------------
		//				
		d_type = new KbeeEntitySet();
		d_type.setDomain(getBuildingDomain());
		d_type.setCanonical(true);
		d_type.setReadonly(getContentDao().findSystemParameterValueByKey("dataset_type.readonly", "no").toLowerCase().trim().equals("yes"));
		d_type.setName(getContentDao().findSystemParameterValueByKey("dataset_type.name", "Document Type"));
		d_type.setAlias(makeAlias(d_type.getName()));
		d_type.setAccessStrategy(getContentDao().findSystemParameterValueByKey("dataste_type.access_strategy", "all").equals("all") ? AccessStrategy.All : AccessStrategy.Roles);
		d_type.setLastModifiedUser(getSessionUser());
		getContentDao().save(d_type);
		txlogger.info(new ModelCreateEvent(d_type, "create"));

		addMemebers(d_type, "dataset_type.values", DEFAULT_TYPES);
		
		
		// Classifier: Type ---------------------------------------------------------
		//
		c_type = new KbeeClassifier();
		c_type.setDomain(getBuildingDomain());
		c_type.setContentType(true);
		c_type.setName(d_type.getName());
		c_type.setPredicate("Type");
		c_type.setAPIClassifier(true);
		c_type.setMultiplicity(Multiplicity.M1N);
		c_type.setUniqueName("type");
		c_type.setRuleCondition(true);
		c_type.addDataSet(d_type);
		c_type.setAlias(makeAlias(c_type.getName()));
		c_type.setMetadataSubtitle(true);
		
		c_type.setSearchable(getContentDao().findSystemParameterValueByKey("classifier_type.searchable", "yes").equals("yes"));
		
		c_type.setDefaultStructure(true);
		c_type.setLastModifiedUser(getSessionUser());
		getContentDao().save(c_type);
		
		
		txlogger.info(new ModelCreateEvent(c_type, "create"));

		
		// DataSet: Status ---------------------------------------------------------
		//
		d_status = new KbeeEntitySet();
		d_status.setDomain(getBuildingDomain());
		d_status.setCanonical(true);
		
		((KbeeEntitySet) d_status).setAccessStrategy(AccessStrategy.All);
		d_status.setReadonly(getContentDao().findSystemParameterValueByKey("dataset_status.readonly", "no").toLowerCase().trim().equals("yes"));
		d_status.setName(DataSet.STATUS);
		d_status.setLastModifiedUser(getSessionUser());
		d_status.setAlias(makeAlias(d_status.getName()));
		d_status.setAccessStrategy(getContentDao().findSystemParameterValueByKey("dataset_status.access_strategy", "all").equals("all") ? AccessStrategy.All : AccessStrategy.Roles);
		getContentDao().save(d_status);
		txlogger.info(new ModelCreateEvent(d_status, "create"));

		addMemebers(d_status, "dataset_status.values", DEFAULT_STATUS);
		
		
		// Classifier: Status
		//
		c_status = new KbeeClassifier();
		c_status.setDomain(getBuildingDomain());
		c_status.setName(d_status.getName());
		c_status.setAPIClassifier(true);
		c_status.setUniqueName(Classifier.STATUS_CLASSIFIER_SOLR);
		c_status.setPredicate(Classifier.STATUS_CLASSIFIER_PREDICATE);
		c_status.setRuleCondition(false);
		c_status.setContentType(false);
		c_status.setMetadataSubtitle(false);
		c_status.setDefaultStructure(true);
		c_status.setAlias(d_status.getAlias());
		c_status.setWorkflowStatus(true);
		c_status.setSearchable(getContentDao().findSystemParameterValueByKey("classifier_status.searchable", "yes").equals("yes"));

		c_status.setMultiplicity(Multiplicity.M11);									
		c_status.addDataSet(d_status);
		c_status.setLastModifiedUser(getSessionUser());
		getContentDao().save(c_status);

		txlogger.info(new ModelCreateEvent(c_status, "create"));
		
	
		// DataSet: Secured Accesss ---------------------------------------------------------
		//
	    d_secured_access = new  KbeeEntitySet();
		d_secured_access.setDomain(getBuildingDomain());
		d_secured_access.setCanonical(true);
		((KbeeEntitySet) d_secured_access).setAccessStrategy(AccessStrategy.All);
		
		d_secured_access.setReadonly(getContentDao().findSystemParameterValueByKey("dataset_secured_access.readonly", "no").toLowerCase().trim().equals("yes"));
		d_secured_access.setName(getContentDao().findSystemParameterValueByKey("dataset_secured_access.name", "Secured Access"));
		
		d_secured_access.setAlias(makeAlias(d_secured_access.getName()));
		d_secured_access.setLastModifiedUser(getSessionUser());
		getContentDao().save(d_secured_access);
		txlogger.info(new ModelCreateEvent(d_secured_access, "create"));
		
		addMemebers(d_secured_access, "dataset_secured_access.values", DEFAULT_SECURED_ACCESS);


		// Classifier 
		//
		//
		c_secured_access = new KbeeClassifier();
		c_secured_access.setDomain(getBuildingDomain());
		c_secured_access.setName(d_secured_access.getName());
		c_secured_access.setAPIClassifier(true);

		c_secured_access.setUniqueName("secureaccess");
		c_secured_access.setPredicate(getContentDao().findSystemParameterValueByKey("dataset_secured_access.predicate", "securedaccess").toLowerCase().trim());
		c_secured_access.setRuleCondition(false);
		c_secured_access.setContentType(false);
		c_secured_access.setMetadataSubtitle(false);
		c_secured_access.setRuleCondition(true);
		c_secured_access.setMultiplicity(Multiplicity.M11);
		c_secured_access.addDataSet(d_secured_access);
		c_secured_access.setLastModifiedUser(getSessionUser());
		c_secured_access.setDefaultStructure(true);
		c_secured_access.setAlias(d_secured_access.getAlias());
		getContentDao().save(c_secured_access);
		txlogger.info(new ModelCreateEvent(c_secured_access, "create"));
		
		
		// DataSet: Property o Site Name  ---------------------------------------------------------
		//
		d_property = new KbeeEntitySet();
		d_property.setDomain(getBuildingDomain());
		d_property.setCanonical(true);
		d_property.setReadonly(getContentDao().findSystemParameterValueByKey("dataset_property.readonly","no").toLowerCase().trim().equals("yes"));
		d_property.setName(getContentDao().findSystemParameterValueByKey("dataset_property.name", "Site Name"));
		d_property.setAlias("sitename");
		d_property.setAccessStrategy(getContentDao().findSystemParameterValueByKey("dataset_property.access_strategy", "all").equals("all") ? AccessStrategy.All : AccessStrategy.Roles);
		d_property.setAccessStrategy(getContentDao().findSystemParameterValueByKey("dataset_property.access_strategy", "all").equals("all") ? AccessStrategy.All : AccessStrategy.Roles);
		d_property.setLastModifiedUser(getSessionUser());
		getContentDao().save(d_property);
		txlogger.info(new ModelCreateEvent(d_property, "create"));
		addMemebers(d_property, "dataset_property.values", "");
		
		
		
		// Classifier: Property
		//
		c_property = new KbeeClassifier();
		c_property.setDomain(getBuildingDomain());
		c_property.setName(d_property.getName());
		c_property.setSearchable(getContentDao().findSystemParameterValueByKey("classifier_property.searchable", "yes").equals("yes"));
		c_property.setAPIClassifier(true);
		c_property.setUniqueName("property"); // tiene que ser consistente con el esquema solr fijo en schema.xml
		c_property.setPredicate(getContentDao().findSystemParameterValueByKey("classifier_property.predicate", "Site Name"));
		c_property.setMultiplicity(Multiplicity.M0N);
		c_property.setContentType(false);
		c_property.setMetadataSubtitle(false);
		c_property.setRuleCondition(true);
		c_property.addDataSet(d_property);
		c_property.setAlias(d_property.getAlias());
		c_property.setLastModifiedUser(getSessionUser());
		getContentDao().save(c_property);
		txlogger.info(new ModelCreateEvent(c_property, "create"));
		
		// BuiltIn Unit DataSet and Classifier
		//Unit dataSet
		KbeeEntitySet aggregation = new KbeeEntitySet();
		aggregation.setName(getContentDao().findSystemParameterValueByKey("dataset_unit.name", "Unit"));
		aggregation.setAlias("unit");
		aggregation.setSuggester(false);
		aggregation.setAccessStrategy(AccessStrategy.All);
		aggregation.setAggregation(true);
		
		aggregation.setDomain(getBuildingDomain());
		aggregation.setLastModifiedUser(getSessionUser());
		aggregation.setCreationOffsetDateTime(OffsetDateTime.now());
		aggregation.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		aggregation.setEnabled(true);
		getContentDao().save(aggregation);

		// unit classifier
		KbeeClassifier classifier = (KbeeClassifier) ServiceLocator.getService(ObjectFactoryService.class).createClassifier(aggregation);
		classifier.setMultiplicity(Multiplicity.M01);

		getContentDao().save(classifier);
		List<DataSetElementTemplate> structure = new ArrayList<DataSetElementTemplate>();
		KbeeDataSetElementTemplate template = new KbeeDataSetElementTemplate();
		template.setClassifier(c_property);
		template.setMultiplicity(Multiplicity.M01);
		template.setReadOnly(true);
		template.setDomain(getBuildingDomain());
		
		template.setCanonical(true);
		structure.add(template);
		
		aggregation.setStructure(structure);
		
		getContentDao().save(aggregation);

		c_unit = (KbeeClassifier) classifier;
		
		// d_unit = (KbeeDataSet) d_property.getService(DataSetService.class).createAggregation(getContentDao().findSystemParameterValueByKey("dataset_unit.name", "Unit"));
		// getContentDao().save(d_unit);
		//c_unit = (KbeeClassifier) d_unit.getService(DataSetService.class).getMainClassifier();
		
				
		// DataSet: Label  ---------------------------------------------------------
		//
		KbeeLabelSet d_tag = new KbeeLabelSet();
		d_tag.setDomain(getBuildingDomain());
		d_tag.setCanonical(true);
		d_tag.setReadonly(false);
		d_tag.setName(getContentDao().findSystemParameterValueByKey("dataset_label.name", "Label"));
		d_tag.setLastModifiedUser(getSessionUser());
		d_tag.setAlias(makeAlias(d_tag.getName()));
		((KbeeLabelSet) d_tag).setAccessStrategy(AccessStrategy.All);
		getContentDao().save(d_tag);
		txlogger.info(new ModelCreateEvent(d_tag, "create"));
		
		addMemebers(d_tag, "dataset_label.values", "follow up; duplicate; delete; draft");
		

		// Classifier: Label
		//
		c_tag = new KbeeClassifier();
		c_tag.setDomain(getBuildingDomain());
		c_tag.setName(d_tag.getName());
		c_tag.setAPIClassifier(false);
		c_tag.setAlias(d_tag.getAlias());
		c_tag.setUniqueName("tag"); // tiene que ser consistente con el esquema solr fijo en schema.xml
		c_tag.setPredicate("tag");
		c_tag.setMultiplicity(Multiplicity.M0N);
		c_tag.setContentType(false);
		c_tag.setMetadataSubtitle(false);
		c_tag.setRuleCondition(false);
		c_tag.addDataSet(d_tag);
		c_tag.setDefaultStructure(true);
		
		c_tag.setLastModifiedUser(getSessionUser());
		getContentDao().save(c_tag);
		txlogger.info(new ModelCreateEvent(c_tag, "create"));

		
		// DataSet: Department ---------------------------------------------------------
		//
		d_department = new KbeeEntitySet();
		d_department.setDomain(getBuildingDomain());
		d_department.setAlias("department");
		d_department.setCanonical(true);
		d_department.setReadonly(getContentDao().findSystemParameterValueByKey("dataset_department.readonly", "no").toLowerCase().trim().equals("yes"));
		d_department.setName(getContentDao().findSystemParameterValueByKey("dataset_department.name", "Department"));
		d_department.setLastModifiedUser(getSessionUser());
		getContentDao().save(d_department);
		txlogger.info(new ModelCreateEvent(d_department, "create"));

		addMemebers(d_department, "dataset_department.values", DEFAULT_DEPARTMENT);
		
		// Classifier: Department
		//
		//
		c_department = new KbeeClassifier();
		c_department.setDomain(getBuildingDomain());
		c_department.setAPIClassifier(true);
		c_department.setName(d_department.getName());
		c_department.setUniqueName("department");
		c_department.setPredicate("Department");
		c_department.setMultiplicity(Multiplicity.M0N);
		c_department.addDataSet(d_department);
		c_department.setContentType(false);
		c_department.setMetadataSubtitle(false);
		c_department.setLastModifiedUser(getSessionUser());
		c_department.setRuleCondition(false);
		c_department.setAlias(c_department.getAlias());
		getContentDao().save(c_department);
		txlogger.info(new ModelCreateEvent(c_department, "create"));
		
		
		// DataSet: dataset_1 to 10 ---------------------------------------------------------
		//

		/*	
		    Site Id
		    Cabinet
		    Pmc Id
		    Document Entity
		    Packet Type
		*/
		
		DataSet ds;
		KbeeClassifier cs;
		
		system_properties_classifiers = new ArrayList<Classifier>();
		
		for (int cn=0;cn<20;cn++) {
			
					// Dataset
					//
					String dn = "dataset" +String.valueOf(cn);
					String dname=getContentDao().findSystemParameterValueByKey(dn, "null");
					
					
					boolean exists = 
					(d_type 			!= null && dname.toLowerCase().trim().equals(d_type.getName().toLowerCase().trim())) || 
					(d_status	 		!= null && dname.toLowerCase().trim().equals(d_status.getName().toLowerCase().trim())) ||
					(d_secured_access 	!= null && dname.toLowerCase().trim().equals(d_secured_access.getName().toLowerCase().trim())) ||
					(d_property 		!= null && dname.toLowerCase().trim().equals(d_property.getName().toLowerCase().trim())) ||
					(d_tag 				!= null && dname.toLowerCase().trim().equals(d_tag.getName().toLowerCase().trim())) ||
					(d_department 		!= null && dname.toLowerCase().trim().equals(d_department.getName().toLowerCase().trim()));
					
					
					if (!exists && !dname.equals("null")) {
						if 		(getContentDao().findSystemParameterValueByKey(dn+".type","entity").trim().toLowerCase().equals("entity"))			ds = new KbeeEntitySet();
						else if (getContentDao().findSystemParameterValueByKey(dn+".type","entity").trim().toLowerCase().equals("label")) 			ds = new KbeeLabelSet();
						else
							ds = new KbeeValueSet();
						
						ds.setDomain(getBuildingDomain());
						((KbeeDataSet) ds).setAccessStrategy(AccessStrategy.All);
						((KbeeDataSet) ds).setCanonical(true);
						((KbeeDataSet) ds).setReadonly(getContentDao().findSystemParameterValueByKey(dn+".readonly","yes").trim().toLowerCase().equals("yes"));
						ds.setName(dname);
						((KbeeDataSet) ds).setAlias(makeAlias(ds.getName()));
						ds.setLastModifiedUser(getSessionUser());
						ds.setCreationOffsetDateTime(OffsetDateTime.now());
						getContentDao().save(ds);
						txlogger.info(new ModelCreateEvent(ds, "create"));
					
						addMemebers(ds, dn+".values", "");
		
						// Classifier
						//
						cs = new KbeeClassifier();
						
						cs.setAPIClassifier(getContentDao().findSystemParameterValueByKey(dn+".classifier.isapi","yes").trim().toLowerCase().equals("yes"));
						
						cs.setDomain(getBuildingDomain());
						cs.setName(dname);
						cs.setDefaultGridColumn(false);
						cs.setSemantic(false);
						cs.setRuleCondition(getContentDao().findSystemParameterValueByKey(dn+".classifier.isrule","no").trim().toLowerCase().equals("yes"));
						cs.setDisplayable(true);

						cs.setSearchable(getContentDao().findSystemParameterValueByKey(dn+".classifier.searchable", "no").equals("yes"));
						
						cs.setMetadataSubtitle(getContentDao().findSystemParameterValueByKey(dn+".classifier.ismetadata","no").trim().toLowerCase().equals("yes"));
						cs.setUniqueName("clsf"+ (cn<10?"0":"")+String.valueOf(cn));
						cs.setPredicate(dname.replace(" ", "").toLowerCase().trim());
					
						Multiplicity mu=Multiplicity.M0N;
						String mu_str=getContentDao().findSystemParameterValueByKey(dn+".classifier.multitplicity", "M0N");
						if 				(mu_str.equals("M0N")) mu=Multiplicity.M0N;
						else if  		(mu_str.equals("M01")) mu=Multiplicity.M01;
						else if  		(mu_str.equals("M11")) mu=Multiplicity.M11;
						else if  		(mu_str.equals("M1N")) mu=Multiplicity.M1N;
						cs.setMultiplicity(mu);
						cs.addDataSet(ds);
						
						cs.setContentType(getContentDao().findSystemParameterValueByKey(dn+".classifier.iscontenttype","no").trim().toLowerCase().equals("yes"));
						cs.setLastModifiedUser(getSessionUser());
						getContentDao().save(cs);
						txlogger.info(new ModelCreateEvent(cs, "create"));
						system_properties_classifiers.add(cs);
					}
		}
	}

	
	private void build_launcher_groups() {
		String lgs=getContentDao().findSystemParameterValueByKey("launchergrous", "Compliance;Accounting;Legal;Corporate");
		
		logger.debug("build_launcher_groups() -> " + lgs);
		
		for (String s:lgs.split(";")) {
			ServiceLocator.getService(ObjectFactoryService.class).createLauncherGroup(s, this.getBuildingDomain());
			logger.debug("LauncherGroup -> "+ s);
		}
	}
	
	
	/****
	 * 
	 * 
	 */
	private void build_content_classes(String imodeltype) {
		build_file();
		buildResourcesContentTemplate();
	}

	// ContentTemplate: File ---------------------------------------------------------------
	//
	// type, property, status, tag, department
	//
	
	
	/**
	 * 
	 * 
	 * 
	 * 
	 */
	
	private void build_file() {
		
		logger.debug("build_file");
		
		ClassifierTemplate ctf_type   		  = new KbeeClassifierTemplate(c_type, 0);						ctf_type.setMetadataSubtitle(true);
		ClassifierTemplate ctf_property 	  = new KbeeClassifierTemplate(c_property, 1); 					ctf_property.setMetadataSubtitle(true);
		ClassifierTemplate ctf_status 		  = new KbeeClassifierTemplate(c_status, 3);					ctf_status.setMetadataSubtitle(false);
		ClassifierTemplate ctf_department 	  = new KbeeClassifierTemplate(c_department, 4);				ctf_department.setMetadataSubtitle(false);
		ClassifierTemplate ctf_tag 			  = new KbeeClassifierTemplate(c_tag, 5 );						ctf_tag.setMetadataSubtitle(false);
		ClassifierTemplate ctf_secured_access = new KbeeClassifierTemplate(c_secured_access, 6);			ctf_secured_access.setMetadataSubtitle(false);
		ClassifierTemplate ctf_unit 		  = new KbeeClassifierTemplate(c_unit, 2); 						ctf_property.setMetadataSubtitle(true);
		
		
		((KbeeClassifierTemplate) ctf_type).setAccessibility(AccessStrategy.All);
		((KbeeClassifierTemplate) ctf_property).setAccessibility(AccessStrategy.All);
		((KbeeClassifierTemplate) ctf_status ).setAccessibility(AccessStrategy.All);
		((KbeeClassifierTemplate) ctf_department ).setAccessibility(AccessStrategy.All);
		((KbeeClassifierTemplate) ctf_tag ).setAccessibility(AccessStrategy.All);
		((KbeeClassifierTemplate) ctf_secured_access ).setAccessibility(AccessStrategy.All);
		
				
				
		KbeeAcl facl = new KbeeAcl(); 
		facl.setCreationOffsetDateTime(OffsetDateTime.now());
		facl.setLastModifiedUser(getSessionUser());
		facl.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		List<Group> fgroups = null;
		fgroups = ServiceLocator.getService(SecurityContentMgmtService.class).getDefaultGroups(getBuildingDomain());
		Group fusers = null;
		for (Group group: fgroups) {
			if (group.getName().equals(KbeeGlobalRole.USER.getId())) {
				fusers = group;
				break;
			}
		}
		
		AclEntry fentry = new KbeeAclEntry(facl, fusers, false);
		List<Permission> fpermissions = new ArrayList<Permission>();
		fpermissions.add(KbeePermission.CREATE);
		fentry.setPermissions(fpermissions);
		facl.addEntry(getSessionUser(), fentry);
		getContentDao().save(facl);
		
		
		// ----------------
		// File
		//
		ContentTemplate content_template_file = new KbeeContentTemplate();
		content_template_file.setName(getContentDao().findSystemParameterValueByKey("content_class.file.name", "File"));
		content_template_file.setContentClass(getContentDao().findContentClassByName("IDoc"));
		content_template_file.setContentClassCode("FILE");
		content_template_file.setPrivateNotes(getBuildingDomain().getDomainType()!=DomainType.EXPRESS);
		content_template_file.setCustomAttributes(getContentDao().findSystemParameterValueByKey("content_class.file.custom-attributes", "no").toLowerCase().trim().equals("yes"));
		// ((KbeeContentTemplate) content_template_file).setTitleRule(getContentDao().findSystemParameterValueByKey("content_class.file.consoletitlerule", "$classifier:"+ c_type.getName()+"$ - $classifier:"+ c_property.getName() +"$ "));
		
		
		content_template_file.addClassifier(ctf_type);
		content_template_file.addClassifier(ctf_status);
		content_template_file.addClassifier(ctf_property);
		content_template_file.addClassifier(ctf_department);
		content_template_file.addClassifier(ctf_tag);
		content_template_file.addClassifier(ctf_secured_access);
		content_template_file.addClassifier(ctf_unit);

		//((KbeeContentTemplate) content_template_file).setConsoleSubtitleRule(getContentDao().findSystemParameterValueByKey("content_class.file.consolesubtitlerule", "$classifier:Document Type$));
		//((KbeeContentTemplate) content_template_file).setPortalsSubtitleRule(getContentDao().findSystemParameterValueByKey("content_class.file.portalsubtitlerule", "$classifier:Document Type$"));
		
		
		KbeeAttributeTemplate ftemplate_edate = new com.novamens.kbee.content.model.KbeeAttributeTemplate(); ftemplate_edate.setAttribute(at_effective_date); ftemplate_edate.setMultiplicity(Multiplicity.M01);
		
		// KbeeAttributeTemplate ftemplate_unit  = new com.novamens.kbee.content.model.KbeeAttributeTemplate(); ftemplate_unit.setAttribute(at_unit);  ftemplate_unit.setMultiplicity(Multiplicity.M01);
		
		List<AttributeTemplate> fattributes = new ArrayList<AttributeTemplate>();
		fattributes.add(ftemplate_edate);
		//fattributes.add(ftemplate_unit);
		content_template_file.setAttributes(fattributes);
		
		content_template_file.setDomain(getBuildingDomain());
		content_template_file.setLastModifiedUser(getSessionUser());
		content_template_file.setInstantiable(true);
		content_template_file.setMultimedia(false);
		content_template_file.setTemplate(false);
		((KbeeContentTemplate) content_template_file).setAcl(facl);
		
		content_template_file.setResources(true);
		content_template_file.setResourcesLabel(getContentDao().findSystemParameterValueByKey("content_class.file.resources.name", "Resources"));

		content_template_file.setAbstract(true);
		content_template_file.setAbstract_label(getContentDao().findSystemParameterValueByKey("content_class.file.notes.name", "Notes"));
		
		content_template_file.setPrivate_notes_label(getContentDao().findSystemParameterValueByKey("content_class.file.internalinfo.name", "Internal Information"));
		content_template_file.setPrivateNotes(true);
		
		content_template_file.setLinkResources(true);
		content_template_file.setDocument(true);
		
		getContentDao().save(content_template_file);
		
		
		//KbeeModelSection section = new KbeeModelSection(content_template_file);
		//getContentDao().save(section);
		
		
//		ctf_type.setSection(section);   		 
//		ctf_property.setSection(section); 	 
//		ctf_status.setSection(section); 		 
//		ctf_department.setSection(section); 	 
//		ctf_tag.setSection(section); 			 
//		ctf_secured_access.setSection(section);
		//section.setName(getContentDao().findSystemParameterValueByKey("content_class.file.section", "Attributes"));
		//List<ModelSection> list = new ArrayList<ModelSection>();
		//list.add(section);

		
//		for (ClassifierTemplate ct:content_template_file.getClassifiers()) {
//			if (ct.getSection()==null)
//				ct.setSection(section);
//		}
//		
//		for (AttributeTemplate ct:content_template_file.getAttributes()) {
//			if (ct.getSection()==null)
//				ct.setSection(section);
//		}
		
		
		//((KbeeContentTemplate) content_template_file).setSections(list);
		getContentDao().save(content_template_file);
		txlogger.info(new ModelCreateEvent(content_template_file, "create"));
										
		
			// File: Launcher for procedure Assign / Standard / Compliance ---------------------------------------------
			//
			if (fusers!=null) {
				
					LauncherGroup lg = null;
					
					List<LauncherGroup> lgs =  getRepository(LauncherGroup.class).findAll(getBuildingDomain());
					if (lgs!=null && lgs.size()>0)
						lg=lgs.get(0);
					
					List<Procedure> procs = getBuildingDomain().getService(WorkflowDomainService.class).getProceduresLibrary();
					
					List<ProcessLauncher> launchers = new ArrayList<ProcessLauncher>();
					
					for (Procedure pr: procs) {
						if (pr.getName()!=null && pr.getName().toLowerCase().equals("assign")) {
							KbeeProcessLauncher launcher = new KbeeProcessLauncher();
							if (lg!=null)
								launcher.setLauncherGroup(lg);
							launcher.setDomain(getBuildingDomain());
							launcher.setLabel(content_template_file.getName());
							launcher.setAcl(facl);
							launcher.setContentTemplate(content_template_file);
							launcher.setLibrary(true);
							launcher.setEnabled(true);
							launcher.setProcedure(pr);
							launchers.add(launcher);
							
							logger.info("Setting Assign launcher");
						}
					}
					
					if (isStandard) {
						KbeeProcessLauncher launcher_standard = new KbeeProcessLauncher();
						launcher_standard.setDomain(getBuildingDomain());
						if (lg!=null)
							launcher_standard .setLauncherGroup(lg);
						launcher_standard.setLabel(content_template_file.getName() + " Standard");
						launcher_standard.setAcl(facl);
						launcher_standard.setContentTemplate(content_template_file);
						launcher_standard.setLibrary(true);
						launcher_standard.setEnabled(true);
						for (Procedure pr: procs) {
							if (pr.getName()!=null && pr.getName().toLowerCase().equals("standard")) {
								launcher_standard.setProcedure(pr);
								launchers.add(launcher_standard);
								logger.info("Setting Standard launcher");
								break;
							}
						}
					}
					
					if (isCompliance) {
						KbeeProcessLauncher launcher_compliance = new KbeeProcessLauncher();
						
						if (lg!=null)
							 launcher_compliance.setLauncherGroup(lg);
						
						 launcher_compliance.setDomain(getBuildingDomain());
						 launcher_compliance.setLabel(content_template_file.getName() + " Compliance");
						 launcher_compliance.setAcl(facl);
						 launcher_compliance.setContentTemplate(content_template_file);
						 launcher_compliance.setLibrary(true);
						 launcher_compliance.setEnabled(true);
						
						for (Procedure pr: procs) {
							if (pr.getName()!=null && pr.getName().toLowerCase().startsWith("compliance")) {
								 launcher_compliance.setProcedure(pr);
								launchers.add( launcher_compliance);
								logger.info("Setting Compliance launcher");
								break;
							}
						}
					}
					
//					if (!launchers.isEmpty())
//						content_template_file.setProcessLaunchers(launchers);
//					
					getContentDao().save(content_template_file);
					txlogger.info(new ModelUpdateEvent(content_template_file, "launchers"));
				}
				else
					logger.error("Group Users does not exists");
	}
	
	/**
	 * 
	 * 
	 * 	

	private void build_onesite() {
		logger.debug("build_onesite()");
		// ONESITE FILE ----------------------------------------------------------------------------------------
		ClassifierTemplate ct_type   = new KbeeClassifierTemplate(c_type, 0);					ct_type.setMetadataSubtitle(true);
		ClassifierTemplate ct_property = new KbeeClassifierTemplate(c_property, 1 );			ct_property.setMetadataSubtitle(true);
		ClassifierTemplate ct_unit     = new KbeeClassifierTemplate(c_unit, 2 );				ct_unit.setMetadataSubtitle(true);
		ClassifierTemplate ct_status = new KbeeClassifierTemplate(c_status, 3);					ct_status.setMetadataSubtitle(false);
		
		ClassifierTemplate ct_department = new KbeeClassifierTemplate(c_department, 4);			ct_department.setMetadataSubtitle(false);
		ClassifierTemplate ct_secured_access = new KbeeClassifierTemplate(c_secured_access, 5);	ct_secured_access.setMetadataSubtitle(false);
		
		KbeeAttributeTemplate template_edate = new com.novamens.kbee.content.model.KbeeAttributeTemplate();  template_edate.setAttribute(this.at_effective_date); template_edate.setMultiplicity(Multiplicity.M01);
		KbeeAttributeTemplate template_cdate = new com.novamens.kbee.content.model.KbeeAttributeTemplate();  template_cdate.setAttribute(this.at_create_date); template_cdate.setMultiplicity(Multiplicity.M01);
		
		// KbeeAttributeTemplate template_unit = new com.novamens.kbee.content.model.KbeeAttributeTemplate(); 	 template_unit.setAttribute(at_unit); template_unit.setMultiplicity(Multiplicity.M01);
		
		
		KbeeAttributeTemplate template_fileid = new com.novamens.kbee.content.model.KbeeAttributeTemplate(); template_fileid.setAttribute(this.at_fileid); template_fileid.setMultiplicity(Multiplicity.M01);
		
		int n=5;
		List<ClassifierTemplate> lct= new ArrayList<ClassifierTemplate>();
		for (Classifier c: system_properties_classifiers) 
			lct.add(new KbeeClassifierTemplate(c,n++));

		KbeeAcl acl = new KbeeAcl(); 
		acl.setCreationOffsetDateTime(OffsetDateTime.now());
		acl.setLastModifiedUser(getSessionUser());
		acl.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		List<Group> groups = null;
		groups = ServiceLocator.getService(SecurityContentMgmtService.class).getDefaultGroups(getBuildingDomain());
		Group users = null;
		for (Group group: groups) {
						if (group.getName().equals(KbeeGlobalRole.USER.getId())) {
						users = group;
					break;
			}
		}

		AclEntry entry = new KbeeAclEntry(acl, users, false);
		List<Permission> permissions = new ArrayList<Permission>();
		permissions.add(KbeePermission.CREATE);
		entry.setPermissions(permissions);
		acl.addEntry(getSessionUser(), entry);
		getContentDao().save(acl);
						
		content_template_onesite_file = new KbeeContentTemplate();
		content_template_onesite_file.setName(getContentDao().findSystemParameterValueByKey("content_class.api.file.name", "OneSite File"));
		content_template_onesite_file.setContentClass(getContentDao().findContentClassByName("IDoc"));
		content_template_onesite_file.setContentClassCode("OSFILE");
		
		((KbeeContentTemplate) content_template_onesite_file).setConsoleSubtitleRule(getContentDao().findSystemParameterValueByKey("content_class.api.file.consolesubtitlerule", "$classifier:Document Type$ - $classifier:Site Name$ "));
		((KbeeContentTemplate) content_template_onesite_file).setPortalsSubtitleRule(getContentDao().findSystemParameterValueByKey("content_class.api.file.portalsubtitlerule", "$classifier:Document Type$ - $classifier:Site Name$ "));
		((KbeeContentTemplate) content_template_onesite_file).setTitleRule(getContentDao().findSystemParameterValueByKey("content_class.onesitefile.consoletitlerule", null));

		content_template_onesite_file.setCustomAttributes(true);
		content_template_onesite_file.addClassifier(ct_type);
		content_template_onesite_file.addClassifier(ct_status);
		content_template_onesite_file.addClassifier(ct_property);
		content_template_onesite_file.addClassifier(ct_unit);
		content_template_onesite_file.addClassifier(ct_department);
		content_template_onesite_file.addClassifier(ct_secured_access);

		for (ClassifierTemplate cter: lct)  
			content_template_onesite_file.addClassifier(cter);

		// Add attributes
		List<AttributeTemplate> attributes = new ArrayList<AttributeTemplate>();
								
		attributes.add(template_edate);  	// Effective Date
		attributes.add(template_cdate); 	// Create Date
		attributes.add(template_fileid); 	// File Id
		// attributes.add(template_unit); 	// Unit
		
		content_template_onesite_file.setAttributes(attributes);
		
		content_template_onesite_file.setDomain(getBuildingDomain());
		content_template_onesite_file.setLastModifiedUser(getSessionUser());
		content_template_onesite_file.setInstantiable(true);
		content_template_onesite_file.setMultimedia(false);
		content_template_onesite_file.setAPIContentClass(true);

		
		((KbeeContentTemplate) content_template_onesite_file).setAcl(acl);
		
		
		content_template_onesite_file.setTemplate(false);
		
		content_template_onesite_file.setResources(true);
		content_template_onesite_file.setResourcesLabel("Resources");

		content_template_onesite_file.setAbstract(true);
		content_template_onesite_file.setAbstract_label("Notes");
		
		content_template_onesite_file.setPrivate_notes_label("Internal Information");
		content_template_onesite_file.setPrivateNotes(true);

		
		content_template_onesite_file.setLinkResources(true);
		content_template_onesite_file.setDocument(true);

		getContentDao().save(content_template_onesite_file);
		
		KbeeModelSection section = new KbeeModelSection(content_template_onesite_file);
		getContentDao().save(section);
		
//		ct_type.setSection(section);   		 
//		ct_property.setSection(section);
//		ct_unit.setSection(section);
//		ct_status.setSection(section); 		 
//		ct_department.setSection(section); 	 
//		ct_secured_access.setSection(section);
//		template_edate.setSection(section);
//		template_cdate.setSection(section);
//		// template_unit.setSection(section);
//		template_fileid.setSection(section);
		
		

//		for (ClassifierTemplate ct:content_template_onesite_file.getClassifiers()) {
//			if (ct.getSection()==null)
//				ct.setSection(section);
//		}
//		
//		for (AttributeTemplate ct:content_template_onesite_file.getAttributes()) {
//			if (ct.getSection()==null)
//				ct.setSection(section);
//		}

		
		section.setName(getContentDao().findSystemParameterValueByKey("content_class.onesitefile.section", "Attributes"));
		List<ModelSection> list = new ArrayList<ModelSection>();
		list.add(section);
		((KbeeContentTemplate)content_template_onesite_file).setSections(list);
		
		getContentDao().save(content_template_onesite_file);
		
		txlogger.info(new ModelCreateEvent(content_template_onesite_file, "create"));

	}
	
	 */
	
	/***
	 * 
	
	private void build_docusign() {
		
		logger.debug("build_docusign");
		
		// ------------------------------------------------------------------------------------------------
		// DOCUSIGN FILE
		//
					
		KbeeAcl docuacl = new KbeeAcl(); 
		docuacl.setCreationOffsetDateTime(OffsetDateTime.now());
		docuacl.setLastModifiedUser(getSessionUser());
		docuacl.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		List<Group> docugroups = null;
		docugroups = ServiceLocator.getService(SecurityContentMgmtService.class).getDefaultGroups(getBuildingDomain());
		Group docuusers = null;
		for (Group group: docugroups) {
			if (group.getName().equals(KbeeGlobalRole.USER.getId())) {
				docuusers = group;
				break;
			}
		}
		AclEntry docuentry = new KbeeAclEntry(docuacl, docuusers, false);
		List<Permission> docupermissions= new ArrayList<Permission>();
		docupermissions.add(KbeePermission.CREATE);
		docuentry.setPermissions(docupermissions);
		docuacl.addEntry(getSessionUser(), docuentry);
		getContentDao().save(docuacl);
		
		ContentTemplate content_template_docu_file = new KbeeContentTemplate();
		
		content_template_docu_file.setContentClassCode("DOCUSIGN");
		
		content_template_docu_file.setName(getContentDao().findSystemParameterValueByKey("content_class.api.docusign.name", "DocuSign Certificate"));
		content_template_docu_file.setContentClass(getContentDao().findContentClassByName("IDoc"));
		content_template_docu_file.setDomain(getBuildingDomain());
		
		((KbeeContentTemplate) content_template_docu_file).setConsoleSubtitleRule(getContentDao().findSystemParameterValueByKey("content_class.api.docusign.consolesubtitlerule", "$classifier:Document Type$ - $classifier:Site Name$ "));
		((KbeeContentTemplate) content_template_docu_file).setPortalsSubtitleRule(getContentDao().findSystemParameterValueByKey("content_class.api.docusign.portalsubtitlerule", "$classifier:Document Type$ - $classifier:Site Name$ "));
		((KbeeContentTemplate) content_template_docu_file).setTitleRule(getContentDao().findSystemParameterValueByKey("content_class.docusign.consoletitlerule", null));

		content_template_docu_file.setResources(true);
		content_template_docu_file.setResourcesLabel(getContentDao().findSystemParameterValueByKey("content_class.onesitefile.resources.name", "Resources"));

		content_template_docu_file.setAbstract(true);
		content_template_docu_file.setAbstract_label(getContentDao().findSystemParameterValueByKey("content_class.onesitefile.notes.name", "Notes"));
		
		content_template_docu_file.setPrivate_notes_label(getContentDao().findSystemParameterValueByKey("content_class.onesitefile.notes.internalinformation", "Internal Information"));
		content_template_docu_file.setPrivateNotes(true);

		content_template_docu_file.setCustomAttributes(true);
		
		content_template_docu_file.setDomain(getBuildingDomain());
		content_template_docu_file.setLastModifiedUser(getSessionUser());
		content_template_docu_file.setInstantiable(false);
		content_template_docu_file.setMultimedia(false);
		content_template_docu_file.setTemplate(false);
		content_template_docu_file.setAbstract(false);
		content_template_docu_file.setLinkResources(false);
		content_template_docu_file.setDocument(true);
						
		ClassifierTemplate docu_type   		= new KbeeClassifierTemplate(c_type, 0);				docu_type.setMetadataSubtitle(true);
		ClassifierTemplate docu_status  	= new KbeeClassifierTemplate(c_status, 3);				docu_status.setMetadataSubtitle(true);
		ClassifierTemplate docu_property  	= new KbeeClassifierTemplate(c_property, 1);			docu_property.setMetadataSubtitle(true);
		ClassifierTemplate docu_unit	  	= new KbeeClassifierTemplate(c_unit, 2);				docu_unit.setMetadataSubtitle(false);
		ClassifierTemplate docu_department  = new KbeeClassifierTemplate(c_department, 4);			docu_department.setMetadataSubtitle(false);
		ClassifierTemplate docu_secured_access = new KbeeClassifierTemplate(c_secured_access, 5);	docu_secured_access.setMetadataSubtitle(false);

		content_template_docu_file.addClassifier(docu_type);
		content_template_docu_file.addClassifier(docu_status);
		content_template_docu_file.addClassifier(docu_property);
		content_template_docu_file.addClassifier(docu_unit);
		content_template_docu_file.addClassifier(docu_department);
		content_template_docu_file.addClassifier(docu_secured_access);


		// DataSet: dataset_1 to DocuSign  ---------------------------------------------------------
		//
		int n=5;
		List<ClassifierTemplate> docu_lct = new ArrayList<ClassifierTemplate>();
		for (Classifier c: system_properties_classifiers) 
			docu_lct.add(new KbeeClassifierTemplate(c,n++));
		
		for (ClassifierTemplate cter: docu_lct) 
			content_template_docu_file.addClassifier(cter);

			
		((KbeeContentTemplate) content_template_docu_file).setAcl(docuacl);

		
		// Add attribute Create Date to DOCUSIGN ---------------------------------------------------------------
		
		List<AttributeTemplate> docu_attributes = new ArrayList<AttributeTemplate>();
		
		KbeeAttributeTemplate docu_template_cdate = new com.novamens.kbee.content.model.KbeeAttributeTemplate();	docu_template_cdate.setAttribute(at_create_date); docu_template_cdate.setMultiplicity(Multiplicity.M01); 
		KbeeAttributeTemplate docu_template_edate = new com.novamens.kbee.content.model.KbeeAttributeTemplate();	docu_template_edate.setAttribute(at_effective_date); docu_template_edate.setMultiplicity(Multiplicity.M01);
		KbeeAttributeTemplate docu_template_fileid = new com.novamens.kbee.content.model.KbeeAttributeTemplate();	docu_template_fileid.setAttribute(at_fileid); docu_template_fileid.setMultiplicity(Multiplicity.M01); 
		//KbeeAttributeTemplate docu_template_unit = new com.novamens.kbee.content.model.KbeeAttributeTemplate();	docu_template_unit.setAttribute(at_unit); docu_template_unit.setMultiplicity(Multiplicity.M01);
		
		docu_attributes.add(docu_template_cdate);
		docu_attributes.add(docu_template_edate);
		docu_attributes.add(docu_template_fileid);
		// docu_attributes.add(docu_template_unit);
		
		 
		// Add attribute FileId to DOCUSIGN ---------------------------------------------------------------
			
		content_template_docu_file.setAttributes(docu_attributes);
			
		// Relationship from DocuSign -> OneSite File ----------------------------------------------------------
		//
		KbeeRelationTemplate signs = new KbeeRelationTemplate();  
		signs.setName(getContentDao().findSystemParameterValueByKey("content_relationship.sign.name", "signs"));
		signs.setTargetLabel(getContentDao().findSystemParameterValueByKey("content_relationship.sign.targetlabel", "Signed Documents"));
		signs.setReverseLabel(getContentDao().findSystemParameterValueByKey("content_relationship.sign.reverselabel", "Signing Certificate"));
		content_template_docu_file.getRelations().add(signs);
		
		// signs.setTargetTemplate(content_template_onesite_file);
		
		signs.setMultiplicity(Multiplicity.M0N);
		signs.setState(ObjectState.ENABLED);
		
		
		getContentDao().save(content_template_docu_file);
		
		KbeeModelSection section = new KbeeModelSection(content_template_docu_file);
		getContentDao().save(section);
		
//		docu_type.setSection(section);
//		docu_status.setSection(section);
//		docu_property.setSection(section);
//		docu_unit.setSection(section);
//		docu_department.setSection(section);
//		docu_secured_access.setSection(section);
//		docu_template_cdate.setSection(section); 
//		docu_template_edate.setSection(section);
//		docu_template_fileid.setSection(section); 
		
		
		
		// docu_template_unit.setSection(section);
		
		
//		for (ClassifierTemplate ct:content_template_docu_file.getClassifiers()) {
//			if (ct.getSection()==null)
//				ct.setSection(section);
//		}
//		
//		for (AttributeTemplate ct:content_template_docu_file.getAttributes()) {
//			if (ct.getSection()==null)
//				ct.setSection(section);
//		}
		
		section.setName(getContentDao().findSystemParameterValueByKey("content_class.docusign.section", "Attributes"));
		List<ModelSection> list = new ArrayList<ModelSection>();
		list.add(section);
		((KbeeContentTemplate)content_template_docu_file).setSections(list);

		
		
		
		
		getContentDao().save(content_template_docu_file);
		txlogger.info(new ModelCreateEvent(content_template_docu_file, "Create"));

		getContentDao().save(content_template_onesite_file);
		txlogger.info(new ModelUpdateEvent(content_template_onesite_file, "Add Relationship:" + getContentDao().findSystemParameterValueByKey("content_relationship.sign.name", "signs")));
	}

	 */
	
	/***
	 * 
	 * @param dataset
	 * @param value
	 * @return
	 * @throws ContentMgmtException
	 */
	private DataSetMember addDataSetMember(DataSet dataset, String value) throws ContentMgmtException {
		
		if (value==null || value.length()==0)
			return null;
		
		User domain_root = getRootUser(dataset.getDomain());
		DataSetMember mt_1 = dataset.createMember();
		mt_1.setDomain(dataset.getDomain());
		mt_1.setCreationOffsetDateTime(OffsetDateTime.now());
		mt_1.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		mt_1.setLastModifiedUser(domain_root);
		mt_1.setState(ObjectState.ENABLED);
		mt_1.setStrValue(value);
		getContentDao().save(mt_1);
		txlogger.info(new DataSetValueCreateEvent(mt_1, "create"));
		return mt_1;
	}
	
	
	private String makeAlias(String name) {
		if (name == null)
			return null;
		String s=name.toLowerCase().replaceAll("[°,¡!?¿:\\/\"-().\\s]", "")
				.replace("á", "a")
				.replace("é", "e")
				.replace("í", "i")
				.replace("ó", "o")
				.replace("ú", "o")
				.replace("ñ", "n")
				.trim();
		return s;
		
	}
}


