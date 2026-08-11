package com.novamens.content.base;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.novamens.content.form.EForm;
import com.novamens.content.form.EFormData;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.Classificable;
import com.novamens.content.model.Classification;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.RelationTemplate;
import com.novamens.content.text.Text;
import com.novamens.dom.Domain;
import com.novamens.dom.DomainObject;
import com.novamens.dom.Indexable;
import com.novamens.security.acl.Acl;

public interface Content extends com.novamens.dom.Object, DomainObject, Indexable, Classificable {
	
	static public final String CLASS_CODE = "cn";
	
	public boolean isLocked();
	public void setLocked(boolean lock);
	
	public int getVersion();
	public int getNextVersion();
	public void incVersionCounter();
	
	public Long getWorkspace();
	public void setWorkspace(Long workspace);	
	
	public void setOId(Long id);
	public Long getOId();
	
	public String getExternalId();
	public OffsetDateTime getExternalTime();
	
	public Source getSource();
	
	public String getName();
	public void setName(String name);
	
	public String getTitle();
	public void setTitle(String title);
	
	public boolean isHeadVersion();
	
	public void setContentTemplate(ContentTemplate contenttemplate);
	public ContentTemplate getContentTemplate();
	
	public void setClassification(List<Classification> classification);
	public void setClassification(Classifier classifier, List<DataSetMember> members);
	public void setClassification(Classifier classifier, DataSetMember member);

	public void setValues(Classifier classifier, List<OffsetDateTime> values);
	
	public List<Classification> getClassification();
	
	@Deprecated
	public void addClassification(Classifier classifier, OffsetDateTime date);
	
	public void addClassification(Classifier classifier, DataSetMember member);
	public void removeClassification(Classification classification);
	
	public List<Classification> getContentClassification();
	public List<Classification> getStatusClassification();
	
	public void setAttributeValues(Attribute attribute, List<String> values); 
	public List<String> getAttributeValues(Attribute name); 
	
	public List<Relation> getRelations();
	public List<Relation> getRelations(RelationTemplate template);
	public void setRelations(List<Relation> relations);
	public void setRelations(RelationTemplate template, List<Relation> relations);
	public void addRelation(Relation relation);
	public void removeRelation(Relation relation);
	public List<Relation> getReverseRelations();
	public List<Relation> getReverseRelations(RelationTemplate template);
	
	public List<ContentLink> getLinks();
	public List<ContentLink> getReverseLinks();
	public void setLinks(List<ContentLink> links);
	public void addLink(ContentLink link);
	
	public List<RelationshipByCriteria> getRelationshipsByCriteria();
	public void addRelation(RelationshipByCriteria relation);
	
	public EFormData getFormData(EForm form);
	public void unsign(EFormData data);
	public EFormData getKbeeData(EForm form);
	public void setFormData(EFormData data);

	
	public Domain getDomain();
	public void setDomain(Domain domain);
	
	public void setLanguage(String lang);
	public String getLanguage();
	
	public DataSetMember getDataSetMember(String classifier_name);
	
	public void  setCommentsEnabled(boolean b);
	public boolean isCommentsEnabled();
	
	public String getDisplayName();

	public String getMetadataAsString();
	public String getLastModifiedOffsetDateTimeColloquial();
	
	public List<String> getDescriptionAsList();
	public List<String> getDescriptionAsList(String language);
	public List<String> getDescriptionAsList(String langstr, String css_ago);
	
	public Text getAbstract();
	public void setAbstract(String abs);
	public void setAbstract(Text abs);
	
	public Text getPrivateNotes();
	public void setPrivateNotes(String abs);
	public void setPrivateNotes(Text abs);
	
	public double getSemanticDistance(Content co);
	
	public String getClassCode();

	public boolean isRecycled();
	public boolean isEnabled();
	public boolean isArchived();
	
	public List<CustomAttribute> getUserDefinedAttributes();
	public void setUserDefinedAttributes(List<CustomAttribute> attributes);
	public boolean isExternal();
	
	public void setCheckinOffsetDateTime(OffsetDateTime now);
	public OffsetDateTime getCheckinOffsetDateTime();
	
	public List<String> getMetadataAsList();
	
	default public String getLastModifiedOffsetDateTimeColloquial(String css_ago) {return getLastModifiedOffsetDateTimeColloquial(null);}
	
	public Acl getAcl();
	public String getIdInfo();

	public String getContentTypeClassificationAsString();
	public String getWorkflowStatusClassificationAsString();
	
	public Map<String, List<String>> getClassificationAsMapString();
	public Map<String, List<String>> getClassificationAsMapString(Locale locale, String timeZone);
	
	public Map<String, List<String>> getPortalClassificationAsMapString();
	public Map<String, List<String>> getAttributesAsMap(Locale locale, String timeZone);
}
