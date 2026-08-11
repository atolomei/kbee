package com.novamens.kbee.content.base;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.novamens.content.base.Content;
import com.novamens.content.base.ContentLink;
import com.novamens.content.base.ContentProxy;
import com.novamens.content.base.CustomAttribute;
import com.novamens.content.base.Relation;
import com.novamens.content.base.RelationshipByCriteria;
import com.novamens.content.base.Resource;
import com.novamens.content.base.ResourceContainer;
import com.novamens.content.base.ResourceFolder;
import com.novamens.content.base.ResourceNode;
import com.novamens.content.base.ResourceTag;
import com.novamens.content.base.ResourceURI;
import com.novamens.content.base.Source;
import com.novamens.content.document.IDoc;
import com.novamens.content.document.TreeFile;
import com.novamens.content.form.EForm;
import com.novamens.content.form.EFormData;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.Classification;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.RelationTemplate;
import com.novamens.content.resource.KBFile;
import com.novamens.content.text.Text;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.content.dao.Proxy;
import com.novamens.security.User;
import com.novamens.security.acl.Acl;
import com.novamens.security.audit.AuditSet;
import com.novamens.service.ObjectService;
import com.novamens.service.ServiceLocator;
import com.novamens.service.ServiceNotFoundException;

@Entity
@PrimaryKeyJoinColumn(name="id")
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="content")
@Table(name = "KB_CONTENT_PROXY")
public class KbeeContentProxy extends KbeeContent implements ContentProxy, ResourceContainer, IDoc {
	
//	@Id 
//	@SequenceGenerator(name = "content_sequencer", sequenceName = "objectid_sequence")
//	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "content_sequencer")
//	@Column(name = "ID")
//	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity=KbeeContent.class)
	@JoinColumn(name="content_id")
	private Content content;
	
//	@Column(name = "WORKSPACE")
//	private Long workspace;
//	
//	@Column(name = "LOCKED")
//	private boolean locked = false;

	
//	public Long getProxyId()	{
//		return id;
//	}
//	
//	public Long getId()	{
//		return id;
//	}
//	
//	public void setId(Serializable id) {
//		this.id = (Long)id;
//	}
	
	public Content getContent() {
		return (Content)Proxy.Unproxy(content);
	}
	
	public void setContent(Content content) {
		this.content = content;
	}
	
//	public boolean isLocked() {
//		return locked;
//	}
//	
//	public void setLocked(boolean lock) {
//		this.locked = lock;
//	}	
		
	public int getVersion() {
		return getContent().getVersion();
	}
	
	public int getNextVersion() {
		return getContent().getNextVersion();
	}
	
	public void incVersionCounter() {
		getContent().incVersionCounter();
	}	
		
//	public Long getWorkspace() {
//		return workspace;
//	}	
//	
//	public void setWorkspace(Long workspace) {
//		this.workspace = workspace;
//	}	
		
	public void setOId(Long id) {
		getContent().setOId(id);
	}
	
	public Long getOId() {
		return getContent().getOId();
	}	
		
	public String getExternalId() {
		return getContent().getExternalId();
	}
	
	public OffsetDateTime getExternalTime() {
		return getContent().getExternalTime();
	}
	
	public void setContentTemplate(ContentTemplate contenttemplate) {
		getContent().setContentTemplate(contenttemplate);
	}
	
	public ContentTemplate getContentTemplate() {
		return getContent().getContentTemplate();
	}
		
	public Source getSource() {
		return getContent().getSource();
	}
		
	public String getName() {
		return getContent().getName();
	}
	
	public void setName(String name) {
		getContent().setName(name);
	}
		
	public String getTitle() {
		return getContent().getTitle();
	}
	
	public void setTitle(String title) {
		getContent().setTitle(title);
	}
	
	public void setState(ObjectState state) {
		getContent().setState(state);
	}
	
	public ObjectState getState() {
		return getContent().getState();
	}
	
	public void setClassification(List<Classification> classification) {
		getContent().setClassification(classification);
	}
	
	public void setClassification(Classifier classifier, List<DataSetMember> members) {
		getContent().setClassification(classifier, members);
	}

	public void setClassification(Classifier classifier, DataSetMember member) {
		getContent().setClassification(classifier, member);
	}

	@Deprecated
	public void setValues(Classifier classifier, List<OffsetDateTime> values) {
		getContent().setValues(classifier, values);
	}
		
	public List<Classification> getClassification() {
		return getContent().getClassification();
	}
	
	public List<Classification> getContentClassification() {
		return getContent().getContentClassification();
	}
	
	public List<Classification> getClassification(Classifier classifier) {
		return getContent().getClassification(classifier);
	}

	@Deprecated
	public void addClassification(Classifier classifier, OffsetDateTime date) {
		getContent().addClassification(classifier, date);
	}

	public void addClassification(Classifier classifier, DataSetMember member) {
		getContent().addClassification(classifier, member);
	}
	
	public void removeAllClassification(Classifier classifier) {
		getContent().removeAllClassification(classifier);
	}
	
	public void addClassification(Classification classification) {
		getContent().addClassification(classification);
	}
	
	public List<Classifier> getClassifiers() {
		return getContent().getClassifiers();
	}
		
	public void setAttributeValues(Attribute attribute, List<String> values) {
		getContent().setAttributeValues(attribute, values);
	}
	
	public List<String> getAttributeValues(Attribute name) {
		return getContent().getAttributeValues(name);
	}
	
	public Map<String, List<String>> getAttributesAsMap() {
		return getContent().getAttributesAsMap();
	}
		
	public List<Relation> getRelations() {
		return getContent().getRelations();
	}
	
	public List<Relation> getRelations(RelationTemplate template) {
		return getContent().getRelations(template);
	}
	
	public void setRelations(List<Relation> relations) {
		getContent().setRelations(relations);
	}
	
	public void setRelations(RelationTemplate template, List<Relation> relations) {
		getContent().setRelations(template, relations);
	}
	
	public void addRelation(Relation relation) {
		getContent().addRelation(relation);
	}
	
	public void removeRelation(Relation relation) {
		getContent().removeRelation(relation);
	}
	
	public List<Relation> getReverseRelations() {
		return getContent().getReverseRelations();
	}
	
	public List<Relation> getReverseRelations(RelationTemplate template) {
		return getContent().getReverseRelations(template);
	}
		
	public List<ContentLink> getLinks() {
		return getContent().getLinks();
	}
	
	public List<ContentLink> getReverseLinks() {
		return getContent().getReverseLinks();
	}
	
	public void setLinks(List<ContentLink> links) {
		getContent().setLinks(links);
	}
	
	public void addLink(ContentLink link) {
		getContent().addLink(link);
	}
	
	public List<RelationshipByCriteria> getRelationshipsByCriteria() {
		return getContent().getRelationshipsByCriteria();
	}
	
	public void addRelation(RelationshipByCriteria relation) {
		getContent().addRelation(relation);
	}
		
	public EFormData getFormData(EForm form) {
		return getContent().getFormData(form);
	}
	
	public void unsign(EFormData data) {
		getContent().unsign(data);
	}
	
	public EFormData getKbeeData(EForm form) {
		return getContent().getKbeeData(form);
	}
	
	public void setFormData(EFormData data) {
		getContent().setFormData(data);
	}

		
	public Domain getDomain() {
		return getContent().getDomain();
	}
	
	public void setDomain(Domain domain) {
		getContent().setDomain(domain);
	}
	
	public void setLanguage(String lang) {
		getContent().setLanguage(lang);
	}
	
	public String getLanguage() {
		return getContent().getLanguage();
	}
		
	public DataSetMember getDataSetMember(String classifier_name) {
		return getContent().getDataSetMember(classifier_name);
	}
		
	public void  setCommentsEnabled(boolean b) {
		getContent().setCommentsEnabled(b);
	}
	
	public boolean isCommentsEnabled() {
		return getContent().isCommentsEnabled();
	}
	
	public boolean isHeadVersion() {
		return getContent().isHeadVersion();
	}
		
	public String getDisplayName() {
		return getContent().getDisplayName();
	}

	public String getMetadataAsString() {
		return getContent().getMetadataAsString();
	}
	
	public String getLastModifiedOffsetDateTimeColloquial() {
		return getContent().getLastModifiedOffsetDateTimeColloquial();
	}
		
	public List<String> getDescriptionAsList() {
		return getContent().getDescriptionAsList();
	}
	
	public List<String> getDescriptionAsList(String lang) {
		return getContent().getDescriptionAsList(lang);
	}
	
	public List<String> getDescriptionAsList(String lang, String css_ago) {
		return getContent().getDescriptionAsList(lang, css_ago);
	}
		
	public Text getAbstract() {
		return getContent().getAbstract();
	}
	
	public void setAbstract(String abs) {
		getContent().setAbstract(abs);
	}
	
	public void setAbstract(Text abs) {
		getContent().setAbstract(abs);
	}
		
	public Text getPrivateNotes() {
		return getContent().getPrivateNotes();
	}
	
	public void setPrivateNotes(String abs) {
		getContent().setPrivateNotes(abs);
	}
	
	public void setPrivateNotes(Text abs) {
		getContent().setPrivateNotes(abs);
	}
		
	public double getSemanticDistance(Content content) {
		return getContent().getSemanticDistance(content);
	}
		
	public List<Classification> getStatusClassification() {
		return getContent().getStatusClassification();
	}
		
	public void removeClassification(Classification classification) {
		getContent().removeClassification(classification);
	}
	
	
	public List<KBFile> getFiles() {
		return ((ResourceContainer)getContent()).getFiles();
	}
	
	public List<KBFile> getFiles(String tag) {
		return ((ResourceContainer)getContent()).getFiles(tag);
	}
	
	public void addFile(KBFile file) {
		((ResourceContainer)getContent()).addFile(file);
	}
	
	public void addFile(KBFile file, ResourceTag tag) {
		((ResourceContainer)getContent()).addFile(file, tag);
	}
	
	public void removeFile(KBFile file) {
		((ResourceContainer)getContent()).removeFile(file);
	}
	
	public void restoreFile(KBFile file) {
		((ResourceContainer)getContent()).restoreFile(file);
	}
	
	public void setFiles(List<KBFile> files) {
		((ResourceContainer)getContent()).setFiles(files);
	}
	
	public boolean contains(KBFile file) {
		return ((ResourceContainer)getContent()).contains(file);
	}
	
	public KBFile getFirstFile() {
		return ((ResourceContainer)getContent()).getFirstFile();
	}

	public Resource getResource(String name) {
		return ((ResourceContainer)getContent()).getResource(name);
	}
	
	public Resource getResource(ResourceURI uri) {
		return ((ResourceContainer)getContent()).getResource(uri);
	}
	
	public ResourceURI getURI(Resource resource) {
		return ((ResourceContainer)getContent()).getURI(resource);
	}
	
	public void addResource(Resource resource) {
		((ResourceContainer)getContent()).addResource(resource);;
	}
	
	public void addResource(Resource resource, ResourceTag tag) {
		((ResourceContainer)getContent()).addResource(resource, tag);;
	}
	
	public void addResource(Resource resource, ResourceFolder folder, ResourceTag tag) {
		((ResourceContainer)getContent()).addResource(resource, folder, tag);;
	}
	
	public List<Resource> getResources() {
		return ((ResourceContainer)getContent()).getResources();
	}
	
	public List<Resource> getResources(String tag) {
		return ((ResourceContainer)getContent()).getResources(tag);
	}
	
	public void setResources(List<Resource> files) {
		((ResourceContainer)getContent()).setResources(files);
	}
	
	public void setResources(List<Resource> files, ResourceTag tag) {
		((ResourceContainer)getContent()).setResources(files, tag);
	}
	
	public void setResourceNodes(List<ResourceNode> files, ResourceTag tag) {
		((ResourceContainer)getContent()).setResourceNodes(files, tag);
	}
	
	public List<Resource> getPortalEnabledResources() {
		return ((ResourceContainer)getContent()).getPortalEnabledResources();
	}
	
	public void setTag(Resource resource, ResourceTag tag) {
		
	}
	
	public void setFolder(Resource resource, ResourceFolder folder) {
		
	}
	
	public ResourceTag getTag(Resource resource) {
		return ((ResourceContainer)getContent()).getTag(resource);
	}

	@Deprecated
	public void setPrivate(Resource resource) {
		
	}
	
	@Deprecated
	public void setPublic(Resource resource) {
		
	}
	
	@Deprecated
	public boolean isPublic(Resource resource) {
		return ((ResourceContainer)getContent()).isPublic(resource);
	}
	
	@Deprecated
	public void addFile(KBFile file, boolean publicarea) {
		
	}
	
	@Deprecated
	public List<Resource> getResources(boolean publicArea) {
		return ((ResourceContainer)getContent()).getResources(publicArea);
	}
	
	@Deprecated
	public ResourceFolder getFolder(Resource resource) {
		return ((ResourceContainer)getContent()).getFolder(resource);
	}
	
	@Deprecated
	public void addFile(KBFile file, ResourceTag tag, boolean publicarea) {
		
	}
	
	public String getClassCode() {
		return getContent().getClassCode();
	}

	public boolean isRecycled() {
		return getContent().isRecycled();
	}
	
	public boolean isEnabled() {
		return getContent().isEnabled();
	}
	
	public boolean isArchived() {
		return getContent().isArchived();
	}
		
	public List<CustomAttribute> getUserDefinedAttributes() {
		return getContent().getUserDefinedAttributes();
	}
	
	public void setUserDefinedAttributes(List<CustomAttribute> attributes) {
		getContent().setUserDefinedAttributes(attributes);
	}
	
	public boolean isExternal() {
		return getContent().isExternal();
	}
		
	public void setCheckinOffsetDateTime(OffsetDateTime now) {
		getContent().setCheckinOffsetDateTime(now);
	}
	
	public OffsetDateTime getCheckinOffsetDateTime() {
		return getContent().getCheckinOffsetDateTime();
	}
		
	public List<String> getMetadataAsList() {
		return getContent().getMetadataAsList();
	}
		
	public Acl getAcl() {
		return getContent().getAcl();
	}
	
	public String getIdInfo() {
		return getContent().getIdInfo();
	}

	public String getContentTypeClassificationAsString() {
		return getContent().getContentTypeClassificationAsString();
	}
	
	public String getWorkflowStatusClassificationAsString() {
		return getContent().getWorkflowStatusClassificationAsString();
	}
		
	public Map<String, List<String>> getClassificationAsMapString() {
		return getContent().getClassificationAsMapString();
	}
	
	public Map<String, List<String>> getClassificationAsMapString(Locale locale, String timeZone) {
		return getContent().getClassificationAsMapString(locale, timeZone);
	}
		
	public Map<String, List<String>> getPortalClassificationAsMapString() {
		return getContent().getPortalClassificationAsMapString();
	}
	
	public Map<String, List<String>> getAttributesAsMap(Locale locale, String timeZone) {
		return getContent().getAttributesAsMap(locale, timeZone);
	}
	
	public void setCreationOffsetDateTime(OffsetDateTime date) {
		getContent().setCreationOffsetDateTime(date);
	}
	
	public String getCreationOffsetDateTimeColloquial() {
		return getContent().getCreationOffsetDateTimeColloquial();
	}
	
	public void setLastModifiedOffsetDateTime(OffsetDateTime date) {
		getContent().setLastModifiedOffsetDateTime(date);
	}
	
	public OffsetDateTime getCreationOffsetDateTime() {
		return getContent().getCreationOffsetDateTime();
	}
	
	public OffsetDateTime getLastModifiedOffsetDateTime() {
		return getContent().getLastModifiedOffsetDateTime();
	}
	
	public void setLastModifiedUser(User user)	{
		super.setLastModifiedUser(user);
		getContent().setLastModifiedUser(user);
	}
	
	public User getLastModifiedUser() {
		return getContent().getLastModifiedUser();
	}
	
	public <T extends ObjectService> T getService(Class<T> service) throws ServiceNotFoundException {
		return ServiceLocator.getService(this, service);
	}
	
	public void setDefaultAudit() {
		getContent().setDefaultAudit();
	}
	
	public AuditSet getAuditSet() {
		return getContent().getAuditSet();
	}
	
	public void setTreeFile(TreeFile tree_file) {
		
	}
	
	public void removeTreeFile() {
		
	}
	
	public TreeFile getTreeFile() {
		return null;
	}

}