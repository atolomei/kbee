package com.novamens.content.model;

import java.util.List;

import com.novamens.content.base.ContentClass;
import com.novamens.content.base.ResourceTag;
import com.novamens.content.form.EForm;
import com.novamens.content.relationshipsbycriteria.RelationshipByCriteriaTemplate;
import com.novamens.content.workflow.ProcessLauncher;
import com.novamens.dom.Domain;
import com.novamens.dom.Indexable;
import com.novamens.security.acl.Acl;
import com.novamens.workflow.Procedure;

/**
 * 
 * <p>A Content Template is where the app defines the structure of Files that created using this Template.
  what tags that a <{@link Content} supports. This is called Template Structure.
Sample Content Templates: File, Compliance File, DocuSign Certificate, Work Order Request.</p>
 *
 */
public interface ContentTemplate extends ModelObject, Indexable  {
	
	public static final String RESOURCES = "resources";
	
	public String getName();
	public void setName(String name);
	
	public boolean isReadable();
	
	public ContentClass getContentClass();
	public void setContentClass(ContentClass clazz);
	
	public Domain getDomain();
	
	public boolean isInstantiable();
	public void setInstantiable(boolean value);
	
	public boolean isDefault();
	public void setDefault(boolean value);
	
	public boolean includesRelations();
	public void setIncludesRelations(boolean value);
	
	public boolean includesRelationshipsByCriteria();
	public void setIncludesRelationshipsByCriteria(boolean value);
	
	public boolean acceptsRelationshipsByCriteria();
	public void setAcceptsRelationshipsByCriteria(boolean value);
	
	public boolean isAbstract();
	public void setAbstract(boolean value);
	
	public int getOrder();
	public void setOrder(int orden);
	
	public void setHasDetailPage(boolean value);
	public boolean hasDetailPage();
	

	public int getDefaultViewMode();
	public void setDefaultViewMode(int b);
	
	
	/**
	 * @return
	 */
	
	boolean isTitleEditable();
	void setTitleEditable(boolean b);

	
	public String getTitleRuleTemplate();
	
	
	public ExtractionRule getTitleRule();
	
	// Classifiers and Attributes
	public void setClassifiers(List<ClassifierTemplate> classifiers);
	public void addClassifier(ClassifierTemplate classifier);
	public List<ClassifierTemplate> getClassifiers();
	public void removeClassifier(ClassifierTemplate template);
	
	public List<AttributeTemplate> getAttributes();
	public void setAttributes(List<AttributeTemplate> attributes);
	
	public List<SubsectionTemplate> getSubsections();

	public void setStructure(List<ModelElementTemplate> structure);
	public List<ModelElementTemplate> getStructure();
	
	public List<ModelSection> getSections();
	//public List<ModelSection> getPortalSections();
	//public void addSection(ModelSection section);
	
	// Relationships
	public List<RelationTemplate> getRelations();
	public List<RelationTemplate> getReverseRelations();
	
	public List<RelationshipByCriteriaTemplate> getRelationshipsByCriteria();
	
	// Workflow
 	public List<Procedure> getProcedures();
 	public List<ProcessLauncher> getProcessLaunchers();
	//public void setProcessLaunchers(List<ProcessLauncher> launchers);
	
	// Resources
	public List<ResourceTag> getResourceTags();
	boolean isLinkResources();
	boolean isTreeFileResources();
	
	void setLinkResources(boolean value);
	
	public Acl getAcl();
	
	public ContentTemplate clone();

	public String getDisplayName();
	
	
	boolean isCustomAttributes();
	void setCustomAttributes(boolean value);

	boolean isPrivateNotes();
	void setPrivateNotes(boolean value);

	
	/** Information Architecture ---------------------------- **/
	
	public boolean isTemplate();
	public void setTemplate(boolean value);
	public boolean isTemplatesCabinet();
	
	public boolean isKnowledgeBaseCabinet();
	public void setKnowledgeBaseCabinet(boolean b);
	
	public boolean isComplianceCabinet();
	public void setComplianceCabinet(boolean b);

	
	/** Semantic Structure ------------------------------	 **/
	
	public void setDocument(boolean value);
	
	
	boolean isActivity();
	void setActivity(boolean doc);
	
	boolean isAd();
	void setAd(boolean doc);
	
	public boolean isMultimedia();
	public void setMultimedia(boolean value);

	public boolean isVideo();
	public void setVideo(boolean video);

	public boolean isAudio();
	public void setAudio(boolean audio);
	
	public boolean isText();
	public void setText(boolean text);

	public boolean isImage();
	public void setImage(boolean bimage);

	boolean isTool();
	void setTool(boolean doc);


	
	public boolean isDocument();
	
	public String getGlyphIcon();
	public String getAbstract_label();
	public String getPrivate_notes_label();
	public String getText_label();
	public String getCustomattributes_label();
			
	public void setAbstract_label(String abstract_label);
	public void setPrivate_notes_label(String private_notes_label);
	public void setText_label(String text_label);
	public void setCustomattributes_label(String customattributes_label);
	
	boolean isAPIContentClass();
	void setAPIContentClass(boolean value);
	
	void addAttribute(AttributeTemplate template);
	
	String getContentClassCode();
	void setContentClassCode(String contentclasscode);
					
	/** Tree File */
	public String getTreeFileLabel();
	public void setTreeFileLabel(String t);
	public boolean isTreeFile();
	public void setTreeFile(boolean istreefile);
	
	/** Resources */
	public boolean isResources();
	public String getResourcesLabel();
	void setResourcesLabel(String label);
	void setResources(boolean b);
	
	/** Internal data object or external reference (site or url) */
	public void setExternalReference(boolean value);
	public boolean isExternalReference();
	public boolean getExternalReference();
	
	
	/** Display */
	public String getConsoleSubtitleRule();
	public String getPortalsSubtitleRule();
	void setResourceTags(List<ResourceTag> tags);

	/** EForms */
	public List<EForm> getForms();

	default public String getModelObjectClassName() { return "Content Template";}
	boolean isInstanceTimeBasedNotification();
}