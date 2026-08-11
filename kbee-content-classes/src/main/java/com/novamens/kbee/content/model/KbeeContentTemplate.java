package com.novamens.kbee.content.model;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.novamens.content.base.ContentClass;
import com.novamens.content.base.ResourceTag;
import com.novamens.content.form.EForm;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.AttributeTemplate;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ClassifierTemplate;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.ExtractionRule;
import com.novamens.content.model.ModelElementTemplate;
import com.novamens.content.model.ModelSection;
import com.novamens.content.model.Multiplicity;
import com.novamens.content.model.RelationTemplate;
import com.novamens.content.model.SubsectionTemplate;
import com.novamens.content.relationshipsbycriteria.RelationshipByCriteriaTemplate;
import com.novamens.content.workflow.ContentProcedure;
import com.novamens.content.workflow.ProcessLauncher;
import com.novamens.kbee.content.base.KbeeContentClass;
import com.novamens.kbee.content.base.KbeeResourceTag;

import com.novamens.kbee.content.form.KbeeEForm;
import com.novamens.kbee.content.relationshipsbycriteria.KbeeRelationshipByCriteriaTemplate;
import com.novamens.kbee.content.workflow.KbeeContentProcedure;
import com.novamens.kbee.content.workflow.KbeeProcedure;
import com.novamens.kbee.dom.KbeeModelObject;
import com.novamens.kbee.security.acl.KbeeAcl;
import com.novamens.kbee.security.acl.KbeePermission;
import com.novamens.security.User;
import com.novamens.security.acl.Acl;
import com.novamens.workflow.Procedure;


/**
 * 
 * 
 * 
 *
 */
@Entity
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="entity")
@Inheritance(strategy=InheritanceType.JOINED)
@Table(name = "Kb_ContentTemplate")
public class KbeeContentTemplate extends KbeeModelObject implements ContentTemplate  {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeContentTemplate.class.getName());

	@ManyToOne(fetch = FetchType.EAGER, targetEntity = KbeeContentClass.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "contentclass_id")
	private ContentClass contentclass;
	
	@OneToMany(orphanRemoval=true, fetch = FetchType.LAZY, cascade=CascadeType.ALL, targetEntity = KbeeClassifierTemplate.class)
	@JoinColumn(name = "contenttemplate_id", nullable=false)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "query")
	Set<ClassifierTemplate> classifierstemplates = new HashSet<ClassifierTemplate>();
	
	// Aggregation
	@OneToMany(orphanRemoval=true, fetch = FetchType.LAZY, cascade = CascadeType.ALL, targetEntity=KbeeAttributeTemplate.class)
	@JoinTable(name = "Kb_ContentAttribute", 
		joinColumns = {	@JoinColumn(name = "ContentTemplate_Id" ) }, 
		inverseJoinColumns = { @JoinColumn(name = "AttributeTemplate_Id") })
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "query")
	Set<AttributeTemplate> attributestemplates = new HashSet<>();
	
	// Aggregation
	@OneToMany(orphanRemoval=true, fetch = FetchType.LAZY, cascade=CascadeType.ALL, targetEntity = KbeeRelationTemplate.class)
	@JoinColumn(name = "sourcetemplate_id", nullable=false) 
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "query")
	@OrderColumn(name="position")
	List<RelationTemplate> relationstemplates = new ArrayList<>();
	
	
	@ManyToMany(fetch = FetchType.LAZY, targetEntity = KbeeRelationTemplate.class)
	@JoinTable(name = "kb_relation_target",  
		joinColumns 		= {@JoinColumn(name = "targettemplate_id") }, 
		inverseJoinColumns 	= {@JoinColumn(name = "relationtemplate_id") }
	)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "query")
	List<RelationTemplate> reverserelationstemplates = new ArrayList<>();
	
	// Aggregation
	@OneToMany(orphanRemoval=true, fetch = FetchType.LAZY, cascade=CascadeType.ALL, targetEntity = KbeeRelationshipByCriteriaTemplate.class)
	@JoinColumn(name = "sourcetemplate_id", nullable=false) 
	@OrderColumn(name="position")
	List<RelationshipByCriteriaTemplate> relationshipbycrtiteriatemplates = new ArrayList<RelationshipByCriteriaTemplate>();
	
	// Resource Tags
	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, targetEntity=KbeeResourceTag.class)
	@JoinTable(name = "Kb_Template_Resource_Tag", 
		joinColumns = {	@JoinColumn(name = "TEMPLATE_ID", nullable = false, updatable = false) }, 
		inverseJoinColumns = { @JoinColumn(name = "TAG_ID", nullable = false, updatable = false) })
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "query")
	@OrderColumn(name="position")
	List<ResourceTag> resourcegroups = new ArrayList<ResourceTag>();
	
	// Aggregation
	//@OneToMany(orphanRemoval=true, fetch = FetchType.LAZY, cascade=CascadeType.ALL, targetEntity = KbeeProcessLauncher.class)
	//@JoinColumn(name = "contenttemplate_id") 
	//List<ProcessLauncher> launchers = new ArrayList<ProcessLauncher>();
	
	// Aggregation
	@OneToMany(orphanRemoval=true, fetch = FetchType.LAZY, cascade=CascadeType.ALL, targetEntity = KbeeContentProcedure.class)
	@JoinColumn(name = "contenttemplate_id") 
	List<Procedure> procedures = new ArrayList<>();
	
	
	@OneToMany(orphanRemoval=true, fetch = FetchType.LAZY, cascade=CascadeType.ALL, targetEntity = KbeeEForm.class)
	@JoinTable(name = "kb_form_template",  
		joinColumns 		= {@JoinColumn(name = "contenttemplate_id") }, 
		inverseJoinColumns 	= {@JoinColumn(name = "form_id") }
	)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "query")
	@OrderColumn(name="position")
	List<EForm> forms = new ArrayList<EForm>();

	// Aggregation
	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, targetEntity = KbeeAcl.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "acl")
	private Acl acl;
	
	
	@Column(name="default_view_mode")
	private int default_view_mode;
	
	// Structure ---------------------------------------------------------------
	//
	@Column(name="contentclasscode")
	private String contentclasscode;
	
	@Column(name="isresources")
	private boolean isresources;
	
	@Column(name="resources_label")
	private String resources_label;
	
	@Column(name="abstract")
	private boolean showabstract;
	
	@Column(name="abstract_label")
	private String abstract_label;
	
	@Column(name="private_notes")
	private boolean private_notes;

	@Column(name="private_notes_label")
	private String private_notes_label;
	
	// text label (only for OrganizationalText)
	@Column(name="text_label")
	private String text_label;
	
	@Column(name="iscustomattributes")
	private boolean iscustomattributes;
	
	@Column(name="customattributes_label")
	private String customattributes_label;

	@Column(name="treefile_label")	
	private String treeFileLabel;

	@Column(name="istreefile")
	private boolean isTreeFile;
	
	@Column(name="relations")
	private boolean relations;

	@Column(name="linkresources")
	private boolean linkresources;
	
	@Column(name="treefileresource")
	private boolean treefileresource;

	@Column(name="hasdetailpage")
	private boolean hasdetailpage;

	
	// Content Creation and Edition --------------------------------------------
	//
	
	@Column(name="title_rule")
	private String titleRule;

	@Column(name="isdefault")
	private boolean isdefault;
	
	@Column(name="istitleeditable")
	private boolean istitleeditable = true;
	
	@Column(name="instantiable")
	private boolean instantiable;

	@Column(name="orden")
	private int orden;
	
	@Column(name="consoleSubtitleRule")
	private String consoleSubtitleRule;
	
	@Column(name="portalsSubtitleRule")
	private String portalsSubtitleRule;

	
	// Semantic ----------------------------------------------------------------
	//
	@Column(name="ismultimedia")
	private boolean ismultimedia;

	@Column(name="isdocument")
	private boolean isdocument = true;
	
	@Column(name="istext")
	private boolean istext;

	@Column(name="isvideo")
	private boolean isvideo;

	@Column(name="isaudio")
	private boolean isaudio;
	
	@Column(name="isadd")
	private boolean isadd;

	@Column(name="isphoto")   
	private boolean isphoto;

	@Column(name="istool")
	private boolean istool = false;
	
	@Column(name="isactivity")
	private boolean isactivity = false;

	@Column(name="is_api")
	private boolean is_api = false;
	
	
	// Information Architecture  ----------------------------------------------------------------
	//
	
	@Column(name="isexternal")
	private boolean externalReference;
	
	@Column(name="istemplate")
	private boolean istemplate;
	
	@Column(name="iskbase")
	private boolean iskbase;

	
	@Column(name="iscompliance")
	private boolean iscompliance;

	
	@Transient
	private String iconcss;
	
	
	// Relations  --------------------------------------------------------------
	//
	@Column(name="increlationshipsbycriteria")
	private boolean includesRelationshipsByCriteria = false;
	
	@Column(name="acceptsrelationshipsbycriteria")
	private boolean acceptsRelationshipsByCriteria = false;
	
	
	// Time based notifications by extension  ---------------------------------------------
	//
	@Column(name="instanceTimeBasedNotification")
	private boolean instanceTimeBasedNotification = false;
	
	
	
	public KbeeContentTemplate() {
		super();
	}

	 @Override
	public String getDisplayName() {
		return getName();
	}
	 
	@Override
	public void setName(String name) {
		super.setName(name);
		if (this.contentclasscode==null && name!=null) {
			String s=name.toUpperCase().trim().replace(" ","");
			this.contentclasscode=s.substring(0, s.length()>8?8:s.length());
		}
	}
	
	@Override
	public int getOrder() {
		return orden;
	}
	
	@Override
	public void setOrder(int order) {
		this.orden=order;
	}
	
	@Override				
	public void setDefault(boolean value) {
		isdefault = value;
	}
	
	@Override
	public boolean isDefault() {
		return isdefault;
	}
	
	@Override
	public void setInstantiable(boolean value) {
		instantiable = value;
	}

	@Override
	public boolean isInstantiable() {
		return instantiable;
	}

	@Override				
	public void setHasDetailPage(boolean value) {
		hasdetailpage = value;
	}
	
	@Override
	public boolean hasDetailPage() {
		return hasdetailpage;
	}
	
	public void setTitleRule(String rule) {
		this.titleRule = rule;
	}

	@Override
	public String getTitleRuleTemplate() {
		return titleRule;
	}
	
	@Override
	public ExtractionRule getTitleRule() {
		return ExtractionRuleParser.Get().getRule(titleRule);
	}
	
	public void setTitleRule(ExtractionRule rule) {
		titleRule = ExtractionRuleParser.Get().getJson(rule);
	}
	

	// ContentClass  -----------------------------------------------------------
	//

	@Override
	public ContentClass getContentClass() {
		return contentclass;
	}
	
	public void setContentClass(ContentClass clazz) {
		this.contentclass=clazz;
	}
	
	
	// Information Structure  --------------------------------------------------
	//
	
	@Override
	public boolean isPrivateNotes() {
		return this.private_notes;
	}
	
	@Override
	public void setPrivateNotes(boolean value) {
		this.private_notes = value;
	}

	@Override
	public void setPrivate_notes_label(String private_notes_label) {
		this.private_notes_label = private_notes_label;
	}
	
	@Override
	public boolean includesRelations() {
		return this.relations;
	}

	public String getConsoleSubtitleRule() {
		return consoleSubtitleRule;
	}

	public void setConsoleSubtitleRule(String consoleSubtitleRule) {
		this.consoleSubtitleRule = consoleSubtitleRule;
	}

	public String getPortalsSubtitleRule() {
		return portalsSubtitleRule;
	}

	public void setPortalsSubtitleRule(String portalsSubtitleRule) {
		this.portalsSubtitleRule = portalsSubtitleRule;
	}

	@Override
	public void setIncludesRelations(boolean rel) {
		this.relations=rel;
	}
	
	@Override
	public boolean includesRelationshipsByCriteria() {
		return includesRelationshipsByCriteria;
	}
	
	public boolean getIncludesRelationshipsByCriteria() {
		return includesRelationshipsByCriteria;
	}

	@Override
	public void setIncludesRelationshipsByCriteria(boolean value) {
		includesRelationshipsByCriteria = value;
	}
	
	@Override
	public boolean acceptsRelationshipsByCriteria() {
		return acceptsRelationshipsByCriteria;
	}
	
	public boolean getAcceptsRelationshipsByCriteria() {
		return acceptsRelationshipsByCriteria;
	}

	@Override
	public void setAcceptsRelationshipsByCriteria(boolean value) {
		this.acceptsRelationshipsByCriteria = value;
	}
	
	@Override
	public boolean isAbstract() {
		return showabstract;
	}

	@Override
	public void setAbstract(boolean value) {
		showabstract = value;
	}

	@Override
	public boolean isExternalReference() {
		return externalReference;
	}

	@Override
	public boolean getExternalReference() {
		return externalReference;
	}
		
	@Override
	public void setExternalReference(boolean value) {
		this.externalReference = value;
	}
	
	public boolean getCustomAttributes() {
		return this.iscustomattributes;
	}
	
	@Override
	public boolean isCustomAttributes() {
		return this.iscustomattributes;
	}

	@Override
	public void setCustomAttributes(boolean value) {
		this.iscustomattributes = value;
	}

	public boolean getTreeFileResource() {
		return this.treefileresource;
	}
	
	public void setTreeFileResource(boolean value) {
		this.treefileresource = value;
	}
	
	@Override
	public boolean isTreeFileResources() {
		return this.treefileresource;
	}
	
	@Override
	public boolean isLinkResources() {
		return this.linkresources;
	}
	
	@Override
	public void setLinkResources(boolean lr) {
		this.linkresources=lr;
	}

	@Override
	public String getAbstract_label() {
		if (abstract_label!=null)
			return abstract_label;
		return "Notes";
	}

	@Override
	public void setAbstract_label(String abstract_label) {
		this.abstract_label = abstract_label;
	}

	@Override
	public String getPrivate_notes_label() {
		if (private_notes_label!=null)
			return private_notes_label;
		return "Internal Information";
	}

	@Override
	public String getText_label() {
		if (text_label!=null)
			return text_label;
		return "Text";
	}

	@Override
	public void setText_label(String text_label) {
		this.text_label = text_label;
	}
	
	@Override
	public String getCustomattributes_label() {
		if (customattributes_label!=null)
				return customattributes_label;
			return "Custom Tags";
	}

	@Override
	public void setCustomattributes_label(String customattributes_label) {
		this.customattributes_label = customattributes_label;
	}

	// Attributes --------------------------------------------------------------
	//
	@Override
	public void addClassifier(ClassifierTemplate template) {
		classifierstemplates.add(template);
	}

	@Override
	public void setClassifiers(List<ClassifierTemplate> classifiers) {
		classifierstemplates.clear();
		for (ClassifierTemplate ct: classifiers) 
			if (ct.getMultiplicity()==null) ct.setMultiplicity(Multiplicity.M01);
		classifierstemplates.addAll(classifiers);
	}
	
	public List<ClassifierTemplate> getClassifiers() {
		List<ClassifierTemplate> classifiers = new ArrayList<ClassifierTemplate>();
		classifiers.addAll(classifierstemplates);
		return classifiers;
	}
	
	public void removeClassifier(ClassifierTemplate template) {
		classifierstemplates.remove(template);
	}

	public List<AttributeTemplate> getAttributes() {
		List<AttributeTemplate> attributes = new ArrayList<AttributeTemplate>();
		attributes.addAll(attributestemplates);
		return attributes;
	}
	
	@Override
	public void setAttributes(List<AttributeTemplate> attributes) {
		attributestemplates.clear();
		attributestemplates.addAll(attributes);
	}
	
	@Override
	public void addAttribute(AttributeTemplate template) {
		if (template.getMultiplicity()==null && (template instanceof KbeeAttributeTemplate)) 
			((KbeeAttributeTemplate) template).setMultiplicity(Multiplicity.M01);
		attributestemplates.add(template);
	}
	
	public List<SubsectionTemplate> getSubsections() {
		List<SubsectionTemplate> subsections = new ArrayList<SubsectionTemplate>();
		return subsections;
	}
	
	@Override
	public void setStructure(List<ModelElementTemplate> structure) {
		int order = 0;
		List<AttributeTemplate> attributes = new ArrayList<AttributeTemplate>();
		List<ClassifierTemplate> classifiers = new ArrayList<ClassifierTemplate>();
		for (ModelElementTemplate template : structure) {
			if (template instanceof AttributeTemplate) {
				((KbeeAttributeTemplate)template).setOrder(order);
				attributes.add((KbeeAttributeTemplate)template);
			}
			if (template instanceof ClassifierTemplate) {
				((KbeeClassifierTemplate)template).setOrder(order);
				classifiers.add((KbeeClassifierTemplate)template);
			}
			if (template instanceof KbeeModelElementTemplate) {
				if (template.getElement() instanceof Attribute) {
					KbeeAttributeTemplate attributetemplate = new KbeeAttributeTemplate();
					attributetemplate.setAttribute((Attribute)template.getElement());
					attributetemplate.setOrder(order);
					attributetemplate.setMetadataSubtitle(((KbeeModelElementTemplate)template).isMetadataSubtitle());
					attributetemplate.setVisible(((KbeeModelElementTemplate)template).isVisible());
					attributetemplate.setMultiplicity(((KbeeModelElementTemplate)template).getMultiplicity());
					attributetemplate.setSource(((KbeeModelElementTemplate)template).getSource());
					//attributetemplate.setSection(template.getSection());
					attributetemplate.setCalculationScript(((KbeeModelElementTemplate)template).getCalculationScript());
					attributetemplate.setParent(((KbeeModelElementTemplate)template).getParent());
					attributes.add(attributetemplate);
				}
				if (template.getElement() instanceof Classifier) {
					KbeeClassifierTemplate classifiertemplate = new KbeeClassifierTemplate();
					classifiertemplate.setClassifier((Classifier)template.getElement());
					classifiertemplate.setOrder(order);
					classifiertemplate.setMultiplicity(((KbeeModelElementTemplate)template).getMultiplicity());
					classifiertemplate.setAccessibility((((KbeeModelElementTemplate)template).getAccessibility()));
					classifiertemplate.setValuesCriteria(((((KbeeModelElementTemplate)template).getValuesCriteria())));
					classifiertemplate.setCalculation(((((KbeeModelElementTemplate)template).getCalculationScript())));
					classifiertemplate.setMetadataSubtitle(((KbeeModelElementTemplate)template).isMetadataSubtitle());
					classifiertemplate.setVisible(((KbeeModelElementTemplate)template).isVisible());
					//classifiertemplate.setSection(template.getSection());
					classifiertemplate.setParent(((KbeeModelElementTemplate)template).getParent());
					classifiertemplate.setReverse((((KbeeModelElementTemplate)template).isReverse()));
					classifiers.add(classifiertemplate);
				}
			}
			order++;
		}
		setAttributes(attributes);
		setClassifiers(classifiers);
	}
	
	@Override
	public List<ModelElementTemplate> getStructure() {
		List<ModelElementTemplate> structure = new ArrayList<ModelElementTemplate>();
		structure.addAll(getClassifiers());
		structure.addAll(getAttributes());
		Collections.sort(structure, new Comparator<ModelElementTemplate>() {
			@Override
			public int compare(ModelElementTemplate a, ModelElementTemplate b) {
				try {
					return a.getOrder() < b.getOrder() ? -1 : 1;
				} 
				catch (Exception e) {
					logger.error(e);
					return 0;
				}
			}
		});
		return structure;
	}
	
	@Override
	public List<ModelSection> getSections() {
//		//if (sections==null || sections.isEmpty()) {
			List<ModelSection> defaultsections = new ArrayList<ModelSection>();
			KbeeModelSection section = new KbeeModelSection(this);
			section.setDefault(true);
			section.setportal(true);
			defaultsections.add(section);
			return defaultsections;
//		//}
//		//return sections;
	}
	
	public void addSection(ModelSection section) {
//		//sections.add(section);
	}
	
	public void setSections(List<ModelSection> sections) {
		//this.sections.removeIf((section) -> !sections.contains(section));
		//this.sections.clear();
		//this.sections.addAll(sections);
	}
	
	
	// Relations ----------------------------------------------------------
	//
	public List<RelationTemplate> getRelations() {
		return relationstemplates;
	}
	
	public void setRelations(List<RelationTemplate> relations) {
		relationstemplates.clear();
		relationstemplates.addAll(relations);
	}
	
	public List<RelationTemplate> getReverseRelations() {
		return reverserelationstemplates;
	}
	
	@Override
	public List<RelationshipByCriteriaTemplate> getRelationshipsByCriteria() {
		return relationshipbycrtiteriatemplates;
	}
	
	// wORKFLOW  --------------------------------------------------------------
	//
	
	public List<Procedure> getProcedures() {
		return procedures;
	}
	
	public void addProcedure(Procedure procedure) {
		((KbeeContentProcedure)procedure).setContentTemplate(this);
		this.procedures.add(procedure);
	}
	
	public List<ProcessLauncher> getProcessLaunchers() {
		List<ProcessLauncher>  launchers = new ArrayList<>();
		for (Procedure procedure : getProcedures()) {
			launchers.addAll(((ContentProcedure)procedure).getProcessLaunchers());
		}
		return launchers;
	}
	
	@Override
	public void setResourceTags(List<ResourceTag> tags) {
		this.resourcegroups = tags;
	}
	
	public List<ResourceTag> getResourceTags() {
		return resourcegroups;
	}
	
	@Override
	public boolean isReadable() {
		if(getSessionUser()==null)
			return false;
		if (getSessionUser().getName().startsWith("root@") || getAcl()==null) 
			return true;
		return getAcl().checkPermission(getSessionUser(), KbeePermission.READ);
	}

	public Acl getAcl() {
		return acl;
	}
	
	public void setAcl(Acl acl) {
		this.acl = acl; 
	}
	
	@Override
	public void setLastModifiedUser(User user)	{
		super.setLastModifiedUser(user);
		for (ProcessLauncher launcher : getProcessLaunchers()) {
			if (launcher.getProcedure()!=null && launcher.getProcedure() instanceof KbeeProcedure) {
				((KbeeProcedure)launcher.getProcedure()).setLastModifiedUser(user);
			}
		}	
	}
	
	@Override
	public void setLastModifiedOffsetDateTime(OffsetDateTime date)	{
		super.setLastModifiedOffsetDateTime(date);
		for (ProcessLauncher launcher : getProcessLaunchers()) {
			if (launcher.getProcedure()!=null && launcher.getProcedure() instanceof KbeeProcedure) {
				((KbeeProcedure)launcher.getProcedure()).setLastModifiedOffsetDateTime(date);
			}
		}	
	}

	

	// ????  ----------------------------------------------------------
	//
	@Override
	public boolean isTemplate() {
		return istemplate;
	}

	@Override
	public void setTemplate(boolean value) {
		istemplate=value;
	}
	
	@Override
	public boolean isTemplatesCabinet() {
		return this.isTemplate();
	}
	
	@Override
	public boolean isKnowledgeBaseCabinet() {
		return this.iskbase;
	}
	
	public void setKnowledgeBaseCabinet(boolean b) {
		this.iskbase=b;
	}
	
	@Override
	public boolean isAPIContentClass() {
		return this.is_api;
	}

	@Override
	public void setAPIContentClass(boolean value) {
		this.is_api=value;
	}

	@Override
	public String getTreeFileLabel() {
		return treeFileLabel;
	}
			
	@Override
	public void setTreeFileLabel(String t) {
		this.treeFileLabel = t;
	}

	@Override
	public boolean isTreeFile() {
		return isTreeFile;
	}

	@Override
	public void setTreeFile(boolean istreefile) {
		this.isTreeFile = istreefile;
	}

	
	// Semantic Structure ------------------------------------------------------
	//
	
	@Override	public boolean isVideo() 			{return isvideo;}
	@Override	public boolean isAudio() 			{return isaudio;}
	@Override	public boolean isImage() 			{return isphoto;}
	@Override	public boolean isText() 			{return istext;}
	@Override	public boolean isDocument() 		{return isdocument;}
	@Override	public boolean isActivity() 		{return isactivity;}
	@Override	public boolean isTool() 			{return istool;}
	@Override	public boolean isAd() 				{return isadd;}
	@Override	public boolean isMultimedia() 		{return ismultimedia;}
	
	@Override	public void setDocument(boolean doc) 		{this.isdocument=doc;}
	@Override	public void setAd(boolean doc) 				{this.isadd=doc;}
	@Override	public void setVideo(boolean video) 		{this.isvideo=video;}
	@Override	public void setAudio(boolean audio) 		{this.isaudio=audio;}
	@Override	public void setImage(boolean photo) 		{this.isphoto=photo;}
	@Override	public void setText(boolean text) 			{this.istext=text;}
	@Override	public void setActivity(boolean doc) 		{this.isactivity=doc;}
	@Override	public void setTool(boolean doc) 			{this.istool=doc;}
	@Override	public void setMultimedia(boolean value) 	{this.ismultimedia = value;	}
	
	// ---------------------------------------------------------------------
	//
	
	@Override
	public ContentTemplate clone() {

		KbeeContentTemplate clone = new KbeeContentTemplate();
		
		super.onClone(clone);

		
		clone.setName(getName());
		clone.setContentClass(getContentClass());
		clone.setContentClassCode(getContentClassCode());
		
		clone.setTitleRule(this.getTitleRuleTemplate());
		clone.setState(this.getState());
	
		clone.setKnowledgeBaseCabinet(this.isKnowledgeBaseCabinet());
		clone.setDocument(this.isDocument());
		clone.setAbstract(this.isAbstract());
		clone.setDefault(this.isDefault());

		
		clone.setTreeFileLabel(this.getTreeFileLabel());
		clone.setTreeFile(this.isTreeFile());
		clone.setExternalReference(this.isExternalReference());
		clone.setTemplate(this.isTemplate());
		clone.setImage(this.isImage());
		clone.setVideo(this.isVideo());
		clone.setAudio(this.isAudio());
		clone.setMultimedia(this.isMultimedia());
		clone.setText(this.isText());
		clone.setPrivateNotes(this.isPrivateNotes());
		clone.setTool(this.isTool());
		clone.setCustomAttributes(this.isCustomAttributes());
		clone.setAd(this.isAd());
		clone.setActivity(this.isActivity());
		clone.setHasDetailPage(this.hasDetailPage());
		clone.setInstantiable(this.isInstantiable());

		clone.setLinkResources(this.isLinkResources());
		clone.setIncludesRelations(this.includesRelations());
		clone.setResources(this.isresources);
		clone.setResourcesLabel(this.resources_label);
		
		clone.setAbstract_label(this.getAbstract_label());
		clone.setPrivate_notes_label(this.getPrivate_notes_label());
		clone.setText_label(this.getText_label());
		clone.setCustomattributes_label(this.getCustomattributes_label());
		clone.setResourcesLabel(this.getResourcesLabel());
		
		clone.setAPIContentClass(this.is_api);

		
		// Classifier Template
		//
		List<ClassifierTemplate> classifiers = new ArrayList<ClassifierTemplate>();
		for (ClassifierTemplate template : getClassifiers()) {
			KbeeClassifierTemplate clonetemplate = new KbeeClassifierTemplate();
			clonetemplate.setClassifier(template.getClassifier());
			clonetemplate.setVisible(template.isVisible());
			clonetemplate.setMultiplicity(template.getMultiplicity());
			classifiers.add(clonetemplate);
		}
		clone.setClassifiers(classifiers);

		// Attribute Template
		//
		List<AttributeTemplate> attributes = new ArrayList<AttributeTemplate>();
		for (AttributeTemplate template : getAttributes()) {
			KbeeAttributeTemplate clonetemplate = new KbeeAttributeTemplate();
			clonetemplate.setAttribute(template.getAttribute());
			clonetemplate.setMetadataSubtitle(template.isMetadataSubtitle());
			clonetemplate.setMultiplicity(template.getMultiplicity());
			attributes.add(clonetemplate);
		}
		clone.setAttributes(attributes);
		
		// Relation Template
		//
		List<RelationTemplate> relations = new ArrayList<RelationTemplate>();
		for (RelationTemplate template : getRelations()) {
			KbeeRelationTemplate clonetemplate = new KbeeRelationTemplate();
			clonetemplate.setName(template.getName());
			clonetemplate.setTargetLabel(template.getTargetLabel());
			clonetemplate.setReverseLabel(template.getReverseLabel());
			clonetemplate.setMultiplicity(template.getMultiplicity());
			//clonetemplate.setTargetTemplate(template.getTargetTemplate());
			relations.add(clonetemplate);
		}
		clone.setRelations(relations);
		
		// Launchers are not cloned 
		return clone;
	}

	public String getContentclasscode() {
		return getAlias();
		// return contentclasscode;
	}
	
	@Override
	public String getContentClassCode() {
		return getAlias();
		//return contentclasscode;
	}

	@Override
	public void setContentClassCode(String contentclasscode) {
		this.contentclasscode = contentclasscode;
		if (this.contentclasscode!=null)
			setAlias(this.contentclasscode.toLowerCase().trim());
	}

	@Override
	public void setResources(boolean b) {
		this.isresources= b;
	}
	
	@Override
	public boolean isResources() {
		return this.isresources;
	}
	
	@Override
	public String getResourcesLabel() {
		return this.resources_label;
	}
	
	@Override
	public void setResourcesLabel( String label ) {
		this.resources_label=label;
	}

	
	
	@Override
	public boolean isTitleEditable() {return this.istitleeditable;}
	
	
	
	@Override
	public void setTitleEditable(boolean b) {this.istitleeditable=b;}

	
	@Override
	public String getGlyphIcon() {
	
		if (iconcss!=null)
			return iconcss;

		if (isExternalReference())
			iconcss = "fal fa-external-link";
				

		else if (isDocument())	iconcss = "fal fa-glasses";
		else if (isText())		iconcss = "fal fa-newspaper";
		else if (isAd())		iconcss = "fal fa-tv-retro";
		else if (isImage())		iconcss = "fal fa-camera";
		else if (isVideo())		iconcss = "fal fa-video";
		else if (isAudio())		iconcss = "fal fa-headphones";
		else if (isTool())		iconcss = "fal fa-wrench";
		else if (isActivity())	iconcss = "fal fa-calendar-alt";
		else
			iconcss = "fad fa-file";
		return iconcss;
	}

	@Override
	public boolean isComplianceCabinet() {
		return this.iscompliance;
	}

	@Override
	public void setComplianceCabinet(boolean b) {
		this.iscompliance=b;
	}

	//@Transient
	//private List<ModelSection> portal_sections  =null;
	
//	@Override
//	public List<ModelSection> getPortalSections() {
//		if (portal_sections!=null)
//			return portal_sections;
//		portal_sections = new ArrayList<ModelSection>();
//		for (ModelSection m: getSections()) {
//			if (m.isPortal())
//				portal_sections.add(m);
//		}
//		return portal_sections;
//	}

	

	@Override
	public boolean isInstanceTimeBasedNotification() {
		return instanceTimeBasedNotification;
	}

	public void setInstanceTimeBasedNotification(boolean instanceTimeBasedNotification) {
		this.instanceTimeBasedNotification = instanceTimeBasedNotification;
	}

	
	@Override
	public List<EForm> getForms() {
		return forms;
	}
	
	public void setForms(List<EForm> forms) {
		this.forms.clear();
		this.forms.addAll(forms);
	}
	
	@Override
	public int getDefaultViewMode() {
		return this.default_view_mode;
	}

	@Override
	public void setDefaultViewMode(int b) {
		default_view_mode=b;
		
	}
}
