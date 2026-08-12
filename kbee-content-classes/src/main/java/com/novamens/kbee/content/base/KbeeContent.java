package com.novamens.kbee.content.base;


import java.io.Serializable;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;

import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderColumn;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Type;
import org.springframework.util.Assert;

import com.novamens.content.base.Content;
import com.novamens.content.base.ContentLink;
import com.novamens.content.base.CustomAttribute;

import com.novamens.content.base.Relation;
import com.novamens.content.base.RelationshipByCriteria;
import com.novamens.content.base.Source;
import com.novamens.content.form.EForm;
import com.novamens.content.form.EFormData;
import com.novamens.content.form.EFormField;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.AttributeTemplate;
import com.novamens.content.model.AttributeType;
import com.novamens.content.model.Classification;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ClassifierTemplate;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.DataSetType;
import com.novamens.content.model.Multiplicity;
import com.novamens.content.model.RelationTemplate;
import com.novamens.content.text.Text;
import com.novamens.datetime.DateTimeService;
import com.novamens.dom.Json;
import com.novamens.dom.Object;
import com.novamens.dom.ObjectState;
import com.novamens.dom.Versionable;
import com.novamens.kbee.content.form.KbeeEFormData;
import com.novamens.kbee.content.form.KbeeEMemContentData;

import com.novamens.kbee.content.model.KbeeClassification;
import com.novamens.kbee.content.model.KbeeContentTemplate;
import com.novamens.kbee.content.model.KbeeRelation;
import com.novamens.kbee.content.model.KbeeRelationshipByCriteria;
import com.novamens.kbee.content.text.KbeeText;
import com.novamens.kbee.content.workflow.KbeeTaskForm;
import com.novamens.kbee.dom.AbstractObject;
import com.novamens.kbee.json.KbeeJson;
import com.novamens.kbee.security.acl.KbeeAcl;
import com.novamens.security.Identifiable;
import com.novamens.security.acl.Acl;
import com.novamens.security.audit.AuditSet;
import com.novamens.service.ServiceLocator;

/**
 * 
 * 
 * 
 *
 */
@Entity
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="content")
@Inheritance(strategy=InheritanceType.JOINED)
@Table(name = "CONTENT")
@DynamicInsert
public abstract class KbeeContent extends AbstractObject implements Versionable<Content>, Content {
						
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeContent.class.getName());

	@Id 
	@SequenceGenerator(name = "content_sequencer", sequenceName = "objectid_sequence")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "content_sequencer")
	@Column(name = "ID")
	private Long id;
	
	@Column(name = "LANG")
	private String 	language;  // ISO 639‑3 (3 letters code)
	
	@Column(name = "title")
	private String 	title;
	
	@Column(name = "content_abstract")
	private String 	content_abstract;
	
	@Column(name = "name")
	private String name;
	
	@Column(name = "version")
	private int version = 1;
	
	@Column(name = "NEXTVERSION")
	private int nextVersion = 0;
					
	@Column(name = "ISHEAD")
	private boolean ishead = true;
	
	@Column(name = "LOCKED")
	private boolean locked = false;
	
	@Column(name = "WORKSPACE")
	private Long workspace;

	@Column(name = "OID")
	private Long oid = null;
	
	@Column(name = "EXTERNAL_ID")
	private String externalId;
	
	@Column(name = "EXTERNAL_TIME")
	private OffsetDateTime externalTime;
	
	@Column(name = "COMMENTS")
	private boolean comments_enabled = true;
	
	@Column(name = "PRIVATE_NOTES")
	private String privateNotes;
	
	@OneToOne(fetch = FetchType.LAZY, targetEntity = KbeeContent.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name ="PREV_VERSION")
	private Content previousVersion;

	/** Classifications are Deleted with the Content (CascadeType.ALL) **/
	@OneToMany(orphanRemoval=true, fetch = FetchType.LAZY, cascade=CascadeType.ALL, targetEntity = KbeeClassification.class)
	@JoinColumn(name = "content_id", nullable=false) 
	@OrderColumn(name="position")
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region="content")
	List<Classification> classifications = new ArrayList<Classification>();

	@ManyToOne(fetch = FetchType.LAZY, targetEntity = KbeeContentTemplate.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "contenttemplate", updatable=true)
	private ContentTemplate contenttemplate;
	 
	/** Relation entries are Deleted with the Content (CascadeType.ALL)  */
	@OneToMany(orphanRemoval=true, fetch = FetchType.LAZY, cascade=CascadeType.ALL, targetEntity = KbeeRelation.class)
	@JoinColumn(name = "source_id", nullable=false, insertable=true) 
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="content")
	List<Relation> relations = new ArrayList<Relation>();
	
	/** Reverse Relations entries are Deleted with the Content (CascadeType.ALL) */
	//@OneToMany(orphanRemoval=true, fetch = FetchType.LAZY, cascade=CascadeType.ALL, targetEntity = KbeeRelation.class)
	//@JoinColumn(name="target_id", insertable=false, updatable=false, nullable=false)
	
	
	@OneToMany(
		    fetch = FetchType.LAZY,
		    targetEntity = KbeeRelation.class
		)
		@JoinColumn(
		    name = "target_id",
		    insertable = false,
		    updatable = false,
		    nullable = false
		)
		@Cache(
		    usage = CacheConcurrencyStrategy.READ_WRITE,
		    region = "content"
		)
	List<Relation> reverserelations = new ArrayList<Relation>();
	
	/** eForms deleted with content */
	@OneToMany(orphanRemoval=true, fetch = FetchType.LAZY, cascade=CascadeType.ALL, targetEntity = KbeeEFormData.class)
	@JoinColumn(name="content_id", insertable=false, nullable=false)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="content")
	List<EFormData> formsdata = new ArrayList<EFormData>();
	
	/** deleted with content */
	@OneToMany(orphanRemoval=true, fetch = FetchType.LAZY, cascade=CascadeType.ALL, targetEntity = KbeeRelationshipByCriteria.class)
	@JoinColumn(name = "source_id", nullable=false) 
	List<RelationshipByCriteria> relationshipsbycriteria = new ArrayList<>();
	
	@OneToMany(orphanRemoval=true, fetch = FetchType.LAZY, cascade=CascadeType.ALL, targetEntity = KbeeContentLink.class)
	@JoinColumn(name = "source_id", nullable=false, insertable=true) 
	List<ContentLink> links = new ArrayList<ContentLink>();
	
	@OneToMany(orphanRemoval=true, fetch = FetchType.LAZY, cascade=CascadeType.ALL, targetEntity = KbeeContentLink.class)
	@JoinColumn(name="target_id", insertable=false, updatable=false, nullable=false)
	List<ContentLink> reverselinks = new ArrayList<ContentLink>();
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = KbeeSource.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "source_id")
	private Source source;
	
	@Column(name = "ATTRIBUTES")
	@Enumerated(EnumType.ORDINAL)
	@Type(type="com.novamens.kbee.json.JsonType")
	private Json attributes;
	
	@Column(name = "USER_DEFINED_PROPERTIES")
	@Enumerated(EnumType.ORDINAL)
	@Type(type="com.novamens.kbee.json.JsonType")
	private Json customattributes;
	
	@Column(name = "checkinDate")
	private OffsetDateTime checkinDate;
	
	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, targetEntity = KbeeAcl.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "acl")
	private Acl acl;
	
	@Transient
	List<Classification> content_classification = null;
	
	@Transient
	List<Classification> status_classification = null;
	
	@Transient
	private Map<Long, Classification> map = null;

	@Transient
	private String last_modified_date_colloquial = null;

	@Transient
	private String content_type_str = null;

	@Transient
	private String status_str = null;

	@Transient
	private Map<String, List<String>> a_map = null;

	@Transient
	Map<String, List<String>> ret_map;
	
	@Transient
	Map<String, List<String>> classificationmap;

	public List<Classifier> getClassifiers() {
		List<Classifier> list = new ArrayList<Classifier>();
		getContentTemplate().getClassifiers().forEach(item -> list.add(item.getClassifier()));
		return list;
	}
	
	public KbeeContent() {
			super();
	}
	
	public KbeeContent(ContentTemplate ct) {
		super();
		setContentTemplate(ct);
	}
	
	@Override
	public Long getId()	{
		return id;
	}
	
	public void setId(Serializable id) {
		this.id = (Long)id;
	}

	@Override
	public DataSetMember getDataSetMember(String classifier_name) {
		for (Classification clasi: getClassification()) {
			if (clasi.getClassifier().getName().equals(classifier_name))
				return clasi.getDataSetMember();
		}
		return null;
	}
	
	public void setContentTemplate(ContentTemplate contenttemplate) {
		Assert.isTrue(!(contenttemplate.getContentClass().getName().equals(this.getClass().getName())),	"ContentTemplate must be for ContentClass "  +  this.getClass().getName() + " and it is of class " + contenttemplate.getContentClass().getName());
		this.contenttemplate=contenttemplate;
	}
	
	public ContentTemplate getContentTemplate() {
		return contenttemplate;
	}
	
	public boolean isCommentsEnabled() {
		return comments_enabled;
	}
	
	public void setCommentsEnabled(boolean b) {
		comments_enabled=b;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setVersion(int version) {
		this.version = version;
		this.nextVersion =  version+1;
	}
	
	public int getVersion() {
		return version;
	}
	
	public void incVersionCounter() {
		this.nextVersion++;
	}
	
	public int getNextVersion() {
		if (nextVersion==0)
			nextVersion = version+1;
		return nextVersion;
	}
	
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String lang) {
		this.language=lang;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String t) {
		if (t!=null)
			this.title=t.trim();
		else
			this.title=null;
	}
	
	public Content getPreviousVersion() {
		return previousVersion;
	}
	
	public void setPreviousVersion(Object object) {
		//Assert.isInstanceOf(Content.class, object);
		previousVersion=(Content)object;
	}
	
	public boolean isHeadVersion() {
		return ishead;
	}
	
	public void setHeadVersion(boolean value) {
		ishead = value;
	}
	
	@Override
	public Content clone() {
		return null; // REVISAR
	}
	
	@Override
	public boolean isLocked() {
		return locked;
	}
	
	@Override
 	public void setLocked(boolean locked) {
		this.locked=locked;
	}
	
	@Override
	public boolean isRecycled() {
		return getState()==ObjectState.DELETED;
	}
	
	@Override
	public boolean isEnabled() {
		return getState()==ObjectState.ENABLED;
	}
	
	@Override
	public boolean isArchived() {
		return getState()==ObjectState.ARCHIVED;
	}
	
	@Override
	public Long getWorkspace() {
		return workspace;
	}
	
	@Override
	public void setWorkspace(Long workspace) {
		this.workspace=workspace;
	}
	
	@Override
	public Text getAbstract() {
		if (this.content_abstract==null)
			return null;
		return new KbeeText(this.content_abstract);
	}
	
	@Override
	public void setAbstract(String text) {
		if (text!=null)
			this.content_abstract = text;
		else
			this.content_abstract=null;
	}
	
	@Override
	public void setAbstract(Text text) {
		if (text!=null)
			this.content_abstract = text.asString();
	}
	
	@Override
	public Text getPrivateNotes() {
		if (this.privateNotes==null)
			return null;
		return new KbeeText(this.privateNotes);
	}
	
	@Override
	public void setPrivateNotes(String text) {
		if (text!=null)
			this.privateNotes = text;
		else
			this.privateNotes=null;
	}
	
	@Override
	public void setPrivateNotes(Text text) {
		if (text!=null)
			this.privateNotes = text.asString();
	}
	
	
	@Override
	public void addClassification(Classifier classifier,OffsetDateTime date) {
		if (classifier.getDataSetType()==DataSetType.DATE) {
			Classification classification = new KbeeClassification(classifier, date, this);
			getClassification().add(classification);
			cleanUp();
		}
		else
			throw new IllegalArgumentException("Classifier " + (classifier!=null?classifier.getName():"null")+ " is not of type DATE");
	}

	@Override
	public void addClassification(Classification c) {
		if (!this.classifications.contains(c)) {
			this.classifications.add(c);
			cleanUp();
		}
	}
	
	@Override
	public void addClassification(Classifier classifier, DataSetMember datasetmember) {
		Classification classification = new KbeeClassification(classifier, datasetmember, this);
		getClassification().add(classification);
		cleanUp();
	}
	
	@Override
	public void setClassification(List<Classification> list) {
		for (Classification classification : list) 
			((KbeeClassification)classification).setContent(this);
		classifications = list;
		cleanUp();
	}
	
	@Override
	public void setClassification(Classifier classifier, DataSetMember member) {
		List<DataSetMember> members = new ArrayList<DataSetMember>();
		if (member!=null)
		members.add(member);
		setClassification(classifier, members);
	}
	@Override
	public void setClassification(Classifier classifier, List<DataSetMember> members) {
		
		List<Classification> oldclassification = getClassification().
			stream().
			filter(classification -> classification!=null && classification.getClassifier().equals(classifier)).
			collect(Collectors.toList());
		
		int i = 0;
		boolean date = classifier.getDataSet().getDataSetType().equals(DataSetType.DATE);
		List<Classification> newclassification = new ArrayList<Classification>();
		for (DataSetMember member : members) {
			if (member!=null) {
				KbeeClassification classification = i<oldclassification.size() ? (KbeeClassification)oldclassification.get(i++) : new KbeeClassification();
				classification.setClassifier(classifier);
				classification.setContent(this);
				if (date) 
					classification.setDateValue(member.getDateValue());
				else
					classification.setDataSetMember(member);
				newclassification.add(classification);
			}
		}
		
		getClassification().removeAll(oldclassification);
		getClassification().addAll(newclassification);
		
		for (ClassifierTemplate template : getContentTemplate().getClassifiers()) {
			//if (!template.isVisible() &&
			if(template.getMultiplicity().equals(Multiplicity.M11) &&
				classifier.equals(template.getParent())) {
				
				List<DataSetMember> values = new ArrayList<DataSetMember>();
				for (DataSetMember member : members) {
					if (member!=null) {
						for (Classification classification : member.getClassification(template.getClassifier())) {
							if (classification!=null && !values.contains(classification.getDataSetMember())) {
								values.add(classification.getDataSetMember());
							}
						}
					}
				}
				if (values.size()==1) {
					setClassification(template.getClassifier(), values);
				}
				else {
					//if (values.isEmpty()) {
						setClassification(template.getClassifier(), new ArrayList<DataSetMember>());
					//}
				}
			}
		}
		
		for (AttributeTemplate template : getContentTemplate().getAttributes()) {
			//if (!template.isVisible() &&
			if (template.getMultiplicity().equals(Multiplicity.M11) &&
				classifier.equals(template.getParent())) {
				List<String> values = new ArrayList<String>();
				for (DataSetMember member : members) {
					if (member!=null) {
						values.addAll(member.getAttributeValues(template.getAttribute()));
					}
				}
				if (values.size()==1) {
					setAttributeValues(template.getAttribute(), values);
				}
				else {
					if (values.isEmpty()) {
						setAttributeValues(template.getAttribute(), new ArrayList<String>());
					}
				}
			}
		}
		
		cleanUp();
	}
	
	@Override
	public void setValues(Classifier classifier, List<OffsetDateTime> values) {
		
		List<Classification> oldclassification = getClassification().
			stream().
			filter(classification -> classification!=null && classification.getClassifier().equals(classifier)).
			collect(Collectors.toList());
			
		int i = 0;
		List<Classification> newclassification = new ArrayList<Classification>();
		for (OffsetDateTime date : values) {
			if (date!=null) {
				KbeeClassification classification = i<oldclassification.size() ? (KbeeClassification)oldclassification.get(i++) : new KbeeClassification();
				classification.setClassifier(classifier);
				classification.setContent(this);
				classification.setDateValue(date);
				newclassification.add(classification);
			}
		}
			
		getClassification().removeAll(oldclassification);
		getClassification().addAll(newclassification);
		cleanUp();
	}
	
	@Override
	public List<Classification> getClassification()	{
		return classifications;
	}
	
	@Override
	public List<Classification> getClassification(Classifier classifier)	{
		List<Classification> selection = new ArrayList<Classification>();
		for (Classification classification : classifications) {
			if (classification!=null && classification.getClassifier().equals(classifier))
				selection.add(classification);
		}
		return selection;
	}
	
	@Override
	public void removeClassification(Classification classification){
		classifications.remove(classification);
		cleanUp();
	}
	
	@Override
	public void setAttributeValues(Attribute name, List<String> values) {
		if(attributes==null)
			attributes= new KbeeJson();
		
		if (values==null || values.isEmpty())
			attributes.remove(String.valueOf(name.getId()));
		else {
			List<String> x_values = new ArrayList<String>();
			for (String value : values) {
				if (value!=null && !"".equals(value.trim())) x_values.add(value);
			}
			if (!x_values.isEmpty()) {
				attributes.put(String.valueOf(name.getId()), x_values);
			}
			else {
				attributes.remove(String.valueOf(name.getId()));
			}
		}
		cleanUp();
	}
	
	@Override
	public List<String> getAttributeValues(Attribute name) {
	
		if(attributes==null)
			attributes= new KbeeJson(); 
		try {
			return  attributes.getValues(String.valueOf(name.getId()));
		} 
		catch (Exception e) {
			logger.error(e);
			attributes= new KbeeJson();
			return  attributes.getValues(String.valueOf(name.getId())); 
		}
	}
	
	public void removeAributeValues(String name) {
		if(attributes!=null) {
			attributes.remove(name);
			cleanUp();
		}
	}
	
	public void removeAributes() {
		attributes=null;
		cleanUp();
	}
	
	@Override
	public void setOId(Long id) {
		oid = id;
	}
	
	@Override
	public Long getOId() {
		return oid;
	}
	
	@Override
	public String getExternalId() {
		return externalId;
	}
	
	public void setExternalId(String id) {
		externalId = id;
	}
	
	@Override
	public Source getSource() {
		return source;
	}
	
	public void setSource(Source source) {
		this.source = source;
	}
	
	@Override
	public OffsetDateTime getExternalTime() {
		return externalTime;
	}
	
	public void setExternalTime(OffsetDateTime time) {
		externalTime = time;
	}
	
	@Override
	public Acl getAcl() {
		return acl;
	}
	
	public void setAcl(Acl acl) {
		this.acl = acl;
	}

	/** 
	 * <p>The latest versions of each related content other than draft and are not deleted.</p>
	 */
	@Override
	public List<Relation> getRelations() {
		List<Relation> relations = new ArrayList<Relation>();
		for (Relation relation1 : this.relations) {
			if (relation1!=null) {
				Content target1 = relation1.getTarget();
				boolean checked = false;
				for (int i=0; i<relations.size(); i++) {
					Relation relation2 = relations.get(i);
					Content target2 = relation2.getTarget();
					if (target1.getOId().equals(target2.getOId())) {
						checked = true;
						if (target1.getVersion()>target2.getVersion() && target1.getWorkspace()==null) {
							if (!target1.getState().equals(ObjectState.DELETED)) {
								relations.set(i, relation1);
							}
							else {
								if (target1.isHeadVersion()) {
									relations.remove(i);
								}
							}
						}
						break;
					}
				}
				if (!checked && !target1.getState().equals(ObjectState.DELETED)) {
				//if (!checked && target1.getWorkspace()==null && !target1.getState().equals(ObjectState.DELETED)) {
					relations.add(relation1);
				}
			}
		}
		return relations;
	}
	
	@Override
	public List<Relation> getRelations(RelationTemplate template) {
		List<Relation> relations = new ArrayList<Relation>(); 
		relations.addAll(getRelations().stream().filter(relation -> relation.getTemplate().equals(template)).collect(Collectors.toList()));
		relations.addAll(getReverseRelations().stream().filter(relation -> relation.getTemplate().equals(template)).collect(Collectors.toList()));
		return relations;
	}
	
	/** 
	 * <p>The latest versions of each related content other than draft and are not deleted</p> 
	 */
	@Override
	public List<Relation> getReverseRelations() {
		List<Relation> relations = new ArrayList<Relation>();
		for (Relation relation1 : this.reverserelations) {
			if (relation1!=null) {
				Content source1 = relation1.getSource();
				boolean checked = false;
				for (int i=0; i<relations.size(); i++) {
					Relation relation2 = relations.get(i);
					Content source2 = relation2.getSource();
					if (source1.getOId().equals(source2.getOId())) {
						checked = true;
						if (source1.getVersion()>source2.getVersion() && source1.getWorkspace()==null) {
							if (!source1.getState().equals(ObjectState.DELETED)) {
								relations.set(i, relation1);
							}
							else {
								if (source1.isHeadVersion()) {
									relations.remove(i);
								}
							}
						}
						break;
					}
				}
				if (!checked && !source1.getState().equals(ObjectState.DELETED)) {
				//if (!checked && source1.getWorkspace()==null && !source1.getState().equals(ObjectState.DELETED)) {
					relations.add(relation1);
				}
			}
		}
		return relations;
	}
	
	@Override
	public List<Relation> getReverseRelations(RelationTemplate template) {
		return getReverseRelations().stream().filter(relation -> relation.getTemplate().equals(template)).collect(Collectors.toList());
	}
	
	@Override
	public List<RelationshipByCriteria> getRelationshipsByCriteria() {
		return relationshipsbycriteria;
	}

	@Override
	public void setRelations(List<Relation> relations) {
	    for (Relation relation : this.relations) {
	        ((KbeeRelation) relation).setSource(null);
	    }
		for (Relation relation : relations) {
			((KbeeRelation)relation).setSource(this);
		}
		this.relations.clear();
		this.relations.addAll(relations);
	}
	
	@Override
	public void setRelations(RelationTemplate template, List<Relation> relations) {
		for (Relation relation : relations) {
			((KbeeRelation)relation).setSource(this);
		}
		this.relations.removeIf(relation -> relation.getTemplate().equals(template));
		this.relations.addAll(relations);
	}

	
	@Override
	public void addRelation(Relation relation) {
		this.relations.add(relation);
	}
	
	@Override
	public void removeRelation(Relation relation2delete) {
		for (Relation relation : this.relations) {
			if (relation.getTemplate().equals(relation2delete.getTemplate()) && 
					relation.getTarget().getId().equals(relation2delete.getTarget().getId())) {
				this.relations.remove(relation);
				break;
			}
		}
	}
	
	public void setReverseRelations(List<Relation> relations) {
		for (Relation relation : this.reverserelations) {
			boolean found = false;
			for (Relation relation1 : relations) {
				if (relation.getTemplate().equals(relation1.getTemplate()) && 
					relation.getSource().getId().equals(relation1.getSource().getId())) {
					found = true;
					break;
				}
			}
			if (!found) {
				relation.getSource().removeRelation(relation);
				relation.getSource().setState(ObjectState.ARCHIVED);
			}
		}
	}
	
	@Override
	public List<ContentLink> getLinks() {
		List<ContentLink> links = new ArrayList<ContentLink>();
		links = this.links;
		return links;
	}
	
	@Override
	public void addLink(ContentLink link) {
		((KbeeContentLink)link).setSource(this);
		this.links.add(link);
	}
	
	@Override
	public void setLinks(List<ContentLink> links) {
		for (ContentLink link : links) {
			((KbeeContentLink)link).setSource(this);
		}
		List<ContentLink> list = new ArrayList<>();
		for (ContentLink linktoadd : links) {
			for (ContentLink link : this.links) {
				if (link.getTarget().equals(linktoadd.getTarget()) &&
					((link.getAnchor()!=null && link.getAnchor().equals(linktoadd.getAnchor()) ||
					link.getAnchor()==null && linktoadd.getAnchor()==null))) {
					linktoadd = link;
				}
//				list.add(linktoadd);
			}
			list.add(linktoadd);

		}
		this.links.clear();
		this.links.addAll(list);
	}
	
	@Override
	public List<ContentLink> getReverseLinks() {
		return this.reverselinks;
	}
	
	@Override
	public EFormData getFormData(EForm form) {
		EFormData data = null, kbeedata = null;
		if (form instanceof Identifiable) {
			for (EFormData contentdata : getFormsData()) {
				if (contentdata.getForm().equals(form)) {
					data = contentdata;
					break;
				}
			}
			if (data == null) {
				data = new KbeeEFormData(this, form);
				getFormsData().add(data);
			}
		}
		else {
			EForm eform = form instanceof KbeeTaskForm ? ((KbeeTaskForm)form).getForm() : form;
			for (EFormData contentdata : getFormsData()) {
				if (((KbeeTaskForm)contentdata.getForm()).getForm().equals(eform)) {
					kbeedata = contentdata;
					break;
				}
			}
			data = new KbeeEMemContentData(form, this);
			if (kbeedata!=null)
			data.setSignatures(kbeedata.getSignatures());
			for (EFormField<?> field : form.getFields()) {
				field.get(this, data);
			}	
		}
		return data;
	}
	
	@Override
	public void unsign(EFormData data) {
		data = getKbeeData(data.getForm());
		data.clearSignatures();
	}
	
	@Override
	public EFormData getKbeeData(EForm form) {
		EFormData data = null;
		EForm eform = form instanceof KbeeTaskForm ? ((KbeeTaskForm)form).getForm() : form;
		if (eform instanceof Identifiable) {
			for (EFormData contentdata : getFormsData()) {
				if (((KbeeTaskForm)contentdata.getForm()).getForm().equals(eform)) {
					data = contentdata;
					break;
				}
			}
			if (data == null) {
				data = new KbeeEFormData(this, eform);
				getFormsData().add(data);
			}
		}
		return data;
	}
	
	public void setFormData(EFormData data) {
		for (EFormField<?> field : data.getForm().getFields()) {
			//if ("Tipo de Documento".equals(field.getName()))
			field.set(this, data);
		}
	}
	
	public List<EFormData> getFormsData() {
		return formsdata;
	}
	
	public void setRelationshipsByCriteria(List<RelationshipByCriteria> relations) {
		for (RelationshipByCriteria relation : relations) {
			((KbeeRelationshipByCriteria)relation).setSource(this);
		}
		this.relationshipsbycriteria.clear();
		this.relationshipsbycriteria.addAll(relations);
	}
	
	@Override
	public void addRelation(RelationshipByCriteria relation) {
		this.relationshipsbycriteria.add(relation);
	}
	
	@Override
	public OffsetDateTime getCheckinOffsetDateTime() {
		return this.checkinDate;
	}
	
	@Override
	public void setCheckinOffsetDateTime( OffsetDateTime d) {
		this.checkinDate=d;
	}
	
	@Override
	public boolean isExternal() {
		return getExternalId()!=null;
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<CustomAttribute> getUserDefinedAttributes() {
		List<CustomAttribute> attributes = new ArrayList<CustomAttribute>();
		if (this.customattributes == null) {
			return attributes;
		}
		List<Map> attributesmaps = (List<Map>)this.customattributes.get("attributes");
		if (attributesmaps == null) {
			return attributes;
		}
		for (Map attributemap : attributesmaps) {
			if (attributemap.keySet().size()==1) {
				String name = (String)attributemap.keySet().toArray()[0];
				String value = (String)attributemap.get(name);
				if (value!=null && !"".equals(value.trim())) {
					attributes.add(new KbeeCustomAttribute(name, value));
				}
			}
		}
		return attributes;
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void setUserDefinedAttributes(List<CustomAttribute> attributes) {
		if (attributes==null || attributes.isEmpty()) {
			this.customattributes = null;
		}
		else {
			this.customattributes = new KbeeJson();
			List<Map> attributesmaps = new ArrayList<Map>();
			for (CustomAttribute attribute : attributes) {
				if (attribute.getValue()!=null && !"".equals(attribute.getValue())) {
					Map map = new HashMap();
					map.put(attribute.getName(), attribute.getValue());
					attributesmaps.add(map);
				}
			}
			this.customattributes.put("attributes", attributesmaps);
		}
	}


	/**
	 * 
	 * 
	 */
	public Map<String, List<String>> getAttributesAsMap() {
		if (this.getDomain()!=null && this.getDomain().getLocale()!=null && this.getDomain().getTimeZone()!=null)
			return getAttributesAsMap(this.getDomain().getLocale(), this.getDomain().getTimeZone());
		return getAttributesAsMap(Locale.getDefault(), ZoneId.systemDefault().getId());
	}
	
	
	@Override
	public Map<String, List<String>> getAttributesAsMap(Locale locale, String time_zone) {
		
		if (a_map!=null)
			return a_map;
		
		a_map = new HashMap<String, List<String>>();
		
		for (AttributeTemplate tem:getContentTemplate().getAttributes()) {
		if ( tem.getAttribute().getState() == ObjectState.ENABLED) {
					if (tem.getAttribute().getType()==AttributeType.DATE || tem.getAttribute().getType()==AttributeType.TIMESTAMP) {
						List<String> li=this.getAttributeValues(tem.getAttribute());
						List<String> rli=new ArrayList<String>();
						for (String s:li) {
							try {
							OffsetDateTime xdate;
							xdate = ServiceLocator.getService(DateTimeService.class).parseStrDate(s);
							ZoneId zid = ZoneId.of(time_zone);
							if (tem.getAttribute().getType()==AttributeType.DATE)
								rli.add(ServiceLocator.getService(DateTimeService.class).format(xdate, zid.getId(), locale, DateTimeService.Dow_Month_Day_year_z));
							else
								rli.add(ServiceLocator.getService(DateTimeService.class).format(xdate, zid.getId(), locale, DateTimeService.Dow_Month_Day_Year_hh_mm_z));
								
							} catch (Exception e) {
								logger.error(e);
								rli.add(s + " err -> " + e.getClass().getSimpleName());
							}
						}
						a_map.put( tem.getAttribute().getName(), rli); 
						
					}
					else
						a_map.put( tem.getAttribute().getName(), this.getAttributeValues(tem.getAttribute()));
				
				}
		}
		
		return a_map;
	}
	
	
	@Override
	public Map<String, List<String>> getClassificationAsMapString() {
		if (this.getDomain()!=null && this.getDomain().getLocale()!=null && this.getDomain().getTimeZone()!=null)
			return getClassificationAsMapString(this.getDomain().getLocale(), getDomain().getTimeZone());
		return getClassificationAsMapString(Locale.getDefault(), ZoneId.systemDefault().getId());
	}
	
	
	/**
	 * 
	 * 
	 */
	public Map<String, List<String>> getClassificationAsMapString(Locale locale, String time_zone) {

		if (classificationmap!=null)
			return classificationmap;
			
		classificationmap = new HashMap<String, List<String>>();
		
		for (Classification classification : getClassification()) {
			if (classification!=null && classification.getClassifier()!=null) {
				String classifierlabel = classification.getClassifier().getName();
				List<String> values = classificationmap.get(classifierlabel);
				if (values == null) {
					values = new ArrayList<String>();
					classificationmap.put(classifierlabel, values);
				}
				values.add(classification.getStrValue());
			}
		}
		
		
		for (ClassifierTemplate classifier : getContentTemplate().getClassifiers()) {
			if (classifier.getClassifier().getState()==ObjectState.ENABLED) {
					String classifierlabel = classifier.getName();
					if(!classificationmap.containsKey(classifierlabel)){
						classificationmap.put(classifierlabel, new ArrayList<>());
					}
			}
		}


		for (Map.Entry<String, List<String>> e: getAttributesAsMap(locale, time_zone).entrySet()) {
			classificationmap.put(e.getKey(), e.getValue());
		}
		return classificationmap;
	}


	
	
	public Map<String, List<String>> getPortalClassificationAsMapString() {
	
		if (ret_map!=null)
			return ret_map;
		
		Map<Classifier, ClassifierTemplate> map = new HashMap<Classifier, ClassifierTemplate>();
		for (ClassifierTemplate template: this.getContentTemplate().getClassifiers()) {
			map.put(template.getClassifier(), template);
		}

		ret_map = new HashMap<String, List<String>>();
		
		for (Classification classification : getClassification()) {
			if (classification!=null && classification.getClassifier()!=null) {
				if (map.get(classification.getClassifier())!=null) {
					if (map.get(classification.getClassifier()).isPortalSubtitle() && map.get(classification.getClassifier()).isVisible()) {
						if (!ret_map.containsKey(classification.getClassifier().getName())) {
							ret_map.put(classification.getClassifier().getName(), new ArrayList<String>());
						}
						ret_map.get(classification.getClassifier().getName()).add(classification.getStrValue());
					}
				}
			}
		}
		return ret_map;
		
	}

	@Override
	public double getSemanticDistance(Content co) {
		Map<Long, Classification> xm = getSemanticClassifications();
		int shared = 0, subco = 0;
		for (Classification clasi: co.getClassification()) {
			 	if (clasi.getClassifier().isSemantic()) {
			 			subco++;
			 			if (xm.containsKey(clasi.getDataSetMember().getId()))
			 					shared++;
			 	}
		}
		int max = (xm.size()<subco?xm.size():subco);
		if (max==0)
			return 0;
		return (double) (shared*100/max);
	}
	
	@Override
	public List<Classification> getContentClassification() {
		if (content_classification==null) {
			content_classification = new ArrayList<Classification>();
			for( Classification clasi: getClassification()){
				if (clasi!=null && clasi.getClassifier().isContentType()) {
					content_classification.add(clasi);
					break;
				}
			}
		}
		return content_classification;
	}

	
	
	@Override						
	public List<Classification> getStatusClassification() {
		
		if (status_classification==null) {
			status_classification = new ArrayList<Classification>();
			for( Classification clasi: getClassification()){
				if (clasi!=null && clasi.getClassifier().isWorkflowStatus()) {
					status_classification.add(clasi);
					break;
				}
			}
		}
		return status_classification;
	}
	
	@Override
	public void removeAllClassification(Classifier classifier) {
		if (this.classifications!=null) {
			List<Classification> lc= new ArrayList<Classification>();
				for (Classification cla: this.classifications) {
					if (cla.getClassifier().equals(classifier)) 
							lc.add(cla);
				}
			if (!lc.isEmpty()) {
				lc.forEach(item -> this.classifications.remove(item));
				cleanUp();
			}
		}
	}	

	@Override
	public String getClassCode() {
		return Content.CLASS_CODE;
	}

	@Override
	public String getDisplayName() {
		return getTitle();
	}
	
	@Override
	public AuditSet getAuditSet() {
		return AuditSet.CONTENT;
	}
	
	@Override
	public List<String> getMetadataAsList() {
		List<String> list = new ArrayList<String>();
		Map<String, List<String>> classificationmap = new HashMap<String, List<String>>();
		try {
			for (Classification classification : getClassification()) {
				if (classification!=null && classification.getClassifier().isDefaultGridColumn()) {
					String classifierlabel = classification.getClassifier().getName();
					List<String> values = classificationmap.get(classifierlabel);
					if (values == null) {
						values = new ArrayList<String>();
						classificationmap.put(classifierlabel, values);
					}
					values.add(classification.getStrValue());
				}
			}
			for (String classifierlabel : classificationmap.keySet()) {
				StringBuilder label = new StringBuilder();
				List<String> values = classificationmap.get(classifierlabel);
				int i = 0;
				for (String value : values) {
					if (i>0) label.append(" - ");
					label.append(value);
					i++;
				}
				list.add(classifierlabel + ". " +  label.toString());
			}
		} catch (Exception e) {
			 list.add(e.getClass().getName()+ " | " + e.getMessage());
			 logger.error(e);

		}
		return list;
		
		
	}
	
	@Override
	public String getContentTypeClassificationAsString() {
		
		if (this.content_type_str==null) {
			
			StringBuilder str = new StringBuilder();
			
			for (Classification clasi: getClassification()) {
				if (clasi!=null && clasi.getClassifier().isContentType()) {
					if (clasi.getStrValue()!=null) {
						if (str.length()>0)
							str.append(", ");
						str.append(clasi.getStrValue());
					}
				}
			}
			this.content_type_str=str.toString();
		}
		return this.content_type_str;
	}
	

	
	
	@Override	
	public String getWorkflowStatusClassificationAsString() {

		if (this.status_str==null) {
			
			StringBuilder str = new StringBuilder();
			for (Classification clasi: getClassification()) {
				if (clasi.getClassifier().isWorkflowStatus()) {
					if (clasi.getStrValue()!=null) {
						if (str.length()>0)
							str.append(", ");
						str.append(clasi.getStrValue());
					}
				}
			}
			this.status_str=str.toString();
		}
		return this.status_str;
	}
	
	public String getMetadataAsString() {
		
		StringBuilder str = new StringBuilder();
		Map<String, List<String>> classificationmap = new HashMap<String, List<String>>();
		
		try {
			for (Classification classification : getClassification()) {
				if (classification!=null && classification.getClassifier().isDefaultGridColumn()) {
					String classifierlabel = classification.getClassifier().getName();
					List<String> values = classificationmap.get(classifierlabel);
					if (values == null) {
						values = new ArrayList<String>();
						classificationmap.put(classifierlabel, values);
					}
					values.add(classification.getStrValue());
				}
			}
			
			int counter = 0;
			int total=classificationmap.keySet().size()-1;
			for (String classifierlabel : classificationmap.keySet()) {
				StringBuilder label = new StringBuilder(); 
				List<String> values = classificationmap.get(classifierlabel);
				int i = 0;
				for (String value : values) {
					if (i>0) label.append(" - ");
					label.append(value);
					i++;
				}
				
				str.append(label.toString() + ((counter++<total) ?". ":""));
			}
		} catch (Exception e) {
			 str.append(e.getClass().getName()+ " | " + e.getMessage());
			 logger.error(e);
		}
		return str.toString();
	}
	
	
	/**
	 * <p>toString is used to display info of the Object for the developers</p>
	 */

	public String toString() {
		StringBuilder str = new StringBuilder();
		
		str.append(super.toString());
		
		
		str.append( "Id: " + (getId()!=null? getId().toString() :"null") );
		
		str.append(" | " + "OID: "+getOId().toString());
		
		str.append(" | " + "Head: "+(isHeadVersion()?"YES":"NO"));
		
		if (workspace!=null && workspace!=0)
			str.append(" | "+ "Workspace: "+getWorkspace());
		str.append("Version: "+ String.valueOf(getVersion()));
		if (getPreviousVersion()!=null)
			str.append(" | "+ "Previous: "+((Content)getPreviousVersion()).getTitle() + "  "+ ((Versionable<?>)getPreviousVersion()).getVersion());
		str.append(" | " + "Locked: "+(isLocked()?"YES":"NO"));
		if (getState()!=null)
			str.append( " | " + "state: " +  (getState()!=null?getState().getLabel():"null"));
		if(getLastModifiedOffsetDateTime()!=null)  
			str.append( " | " + "modified: " + getLastModifiedOffsetDateTimeColloquial());
		if(getLastModifiedUser()!=null)
			str.append( " | " + "user: " + getLastModifiedUser().getFirstLastName());
		if (getDomain()!=null)
			str.append(" | " + "domain: " +getDomain().getId().toString());
		if (getTitle()!=null)
			str.append(" | " + "Title: "+getTitle());
		
		if (getName()!=null)
			str.append(" | " + "Name: "+getName());
		
		
		if (getClassification()!=null) {
			for(Classification clasi: getClassification())
				if (clasi!=null && clasi.getDataSetMember()!=null)
					str.append(" | "+clasi.getClassifier().getName() +" -> " + clasi.getDataSetMember().getDisplayName());  			
		}
		return str.toString();
	}
	
	@Override
	public List<String> getDescriptionAsList() {
		return getDescriptionAsList(getLanguage());
	}
	
	@Override
	public List<String> getDescriptionAsList(String langstr) {
		return getDescriptionAsList(langstr, null);
	}
	
	@Override
	public List<String> getDescriptionAsList(String langstr, String css_ago) {
		
		List<String> description = new ArrayList<String>();
		Map<Classifier, List<String>> classificationmap = new HashMap<Classifier, List<String>>();
		
		try {
			if (!isHeadVersion() && getVersion()>1) { 
				String elapsedstr= getCreationOffsetDateTimeColloquial(); 
				String ck="Checkout v" + String.valueOf(getVersion())+". " + elapsedstr;
				description.add(ck);
			}
				
			for (AttributeTemplate template : getContentTemplate().getAttributes()) {
				
				if (!getAttributeValues(template.getAttribute()).isEmpty()) {
					
					StringBuilder label = new StringBuilder(); 
					label.append(template.getAttribute().getName()+":  ");
					
					List<String> values = new ArrayList<String>();
					for (String val: getAttributeValues(template.getAttribute())) {
						if (template.getAttribute().getType()==AttributeType.DATE)
							values.add(ServiceLocator.getService(DateTimeService.class).getDateDisplayString(val, getSessionUser().getLocale(),DateTimeService.Month_Day_Year));
						else
							values.add(val);
					}
					
					int i = 0;
					for (String value : values) {
						if (i>0) label.append(" - ");
						label.append(value);
						i++;
					}
					description.add(label.toString());
				}
			}
			
			int o = 0;
			final Map<Long, Integer> classiferorder = new HashMap<Long, Integer>(); 
			for (ClassifierTemplate template : getContentTemplate().getClassifiers()) {
				classiferorder.put((Long) template.getClassifier().getId(), o);
				o++;
			}
	
			for (Classification classification : getClassification()) {
				if (classification!=null) {
					List<String> values = classificationmap.get(classification.getClassifier());
					if (values == null) {
						values = new ArrayList<String>();
						if (classification.getClassifier()!=null)
							classificationmap.put(classification.getClassifier(), values);
					}
					if (classification.getDataSetType().equals(DataSetType.DATE)) {
						if (classification.getDateValue()!=null) { 
							values.add(classification.getStrValue());
						}
						else {
							classificationmap.remove(classification.getClassifier());
						}	
					}
					else
						values.add(classification.getStrValue());
				}
			}
			
			List<Classifier> keylist = new ArrayList<Classifier>(); 
			keylist.addAll(classificationmap.keySet());
			Collections.sort(keylist, new Comparator<Classifier>() {
				@Override
				public int compare(Classifier a, Classifier b) {
					try {
						int oa = classiferorder.get(a.getId())!=null ? classiferorder.get(a.getId()) : 100;
						int ob = classiferorder.get(b.getId())!=null ? classiferorder.get(b.getId()) : 100;
						return oa-ob;
					} catch (Exception e) {
						return 0;
					}
				}
			}); 
			
			for (Classifier classifier : keylist) {
				StringBuilder label = new StringBuilder(); 
				label.append(classifier.getName()+":  ");
				List<String> values = classificationmap.get(classifier);
				int i = 0;
				for (String value : values) {
					if (i>0) label.append(" - ");
					label.append(value);
					i++;
				}
				description.add(label.toString());
			}
		}
		 catch (Exception e) {
			 description.add(e.getClass().getName());
			 logger.error(e);
		 }
		return description;
	}
	
	protected Map<Long, Classification> getSemanticClassifications() {
		if (map==null) {
			map = new HashMap<Long, Classification>();
			for (Classification clasi: getClassification()) {
				if (clasi.getClassifier().isSemantic()) {
					map.put((Long) clasi.getDataSetMember().getId(), clasi);
				}
			}
		}
		return map;	
	}
	
	
	@Override
	public String getIdInfo() {
		return  String.valueOf(getId()) +" / " + String.valueOf(getOId());
	}
	
	private void cleanUp() {
		content_classification = null;
		status_classification = null;
		a_map=null;
		map = null;
		content_type_str = null;
		ret_map = null;
		classificationmap=null;
	}
	
	
	
}
