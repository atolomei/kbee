 package com.novamens.kbee.content.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Cacheable;
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
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Type;

import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.Multiplicity;
import com.novamens.content.model.RelationTemplate;
import com.novamens.kbee.dom.AbstractObject;
import com.novamens.security.audit.AuditSet;

@Entity
@Inheritance(strategy=InheritanceType.JOINED)
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "entity")
@Table(name = "Kb_Relation_Template")
public class KbeeRelationTemplate extends AbstractObject implements RelationTemplate {
	
	@Id
	@SequenceGenerator(name = "template_sequencer", sequenceName = "domainid_sequence")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "template_sequencer")
	@Column(name = "id")
	private Long id;

	@Column(name = "name")
	private String name;
	
	@Column(name = "aggregation")
	private boolean aggregation;
	
	@Column(name = "source_label")
	private String sourceLabel;
	
	@Column(name = "source_display_mode")
	private int sourceDisplayMode;
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = KbeeContentTemplate.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "sourcetemplate_Id", insertable=false, updatable=false, nullable=false)
	private ContentTemplate sourceTemplate;
	
	@Column(name = "target_label")
	private String targetLabel;
	
	@Column(name = "target_display_mode")
	private int targetDisplayMode;
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = KbeeContentTemplate.class)
	@JoinColumn(name = "targettemplate_id")
	private ContentTemplate targetTemplate;
	
	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, targetEntity = KbeeContentTemplate.class)
	@JoinTable(name = "kb_relation_target",  
		joinColumns 		= {@JoinColumn(name = "relationtemplate_id") }, 
		inverseJoinColumns 	= {@JoinColumn(name = "targettemplate_id") }
	)
	private List<ContentTemplate> targetTemplates = new ArrayList<ContentTemplate>();
	
	@Column(name = "multiplicity")
	@Enumerated(EnumType.ORDINAL)
	@Type(type="com.novamens.kbee.content.model.MultiplicityUserType")
	private Multiplicity multiplicity;
	
	@Column(name = "target_order")
	private int targetOrder = 0;
	
	@Column(name = "reverse_order")
	private int reverseOrder = 0;
	
	@Column(name = "keep_version")
	private boolean keepVersion;
	
	
	public KbeeRelationTemplate() {
		super();
	}
	
	public KbeeRelationTemplate(KbeeRelationTemplate src) {
		super(src);
		
		this.name=src.name;
		this.aggregation=src.aggregation;
		this.sourceLabel=src.sourceLabel;
		this.sourceDisplayMode=src.sourceDisplayMode;
		this.sourceTemplate=src.sourceTemplate;
		this.targetLabel= src.targetLabel;
		this.targetDisplayMode=src.targetDisplayMode;
		this.targetTemplate=src.targetTemplate;
		this.targetTemplates=src.targetTemplates;
		this.multiplicity=src.multiplicity;
	}
	
	
	public Long getId()	{
		return id;
	}
	
	public void setId(Serializable id) {
		this.id = (Long)id;
	}
	
	public String getDisplayName() {
		return name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public Multiplicity getMultiplicity() {
		return multiplicity;
	}
	
	public void setMultiplicity(Multiplicity multiplicity) {
		this.multiplicity = multiplicity;
	}
	
	@Override
	public int getReverseOrder() {
		return reverseOrder;
	}
	
	public void setReverseOrder(int order) {
		this.reverseOrder = order;
	}
	
	@Override
	public int getTargetOrder() {
		return targetOrder;
	}
	
	public void setTargetOrder(int order) {
		this.targetOrder = order;
	}
	
	@Override
	public boolean isMandatory() {
		return getMultiplicity()!=null && (getMultiplicity().equals(Multiplicity.M11) || getMultiplicity().equals(Multiplicity.M1N));
	}
	
	public String getTargetLabel() {
		return targetLabel;
	}
	
	public void setTargetLabel(String label) {
		this.targetLabel = label;
	}
	
	public ContentTemplate getTargetTemplate() {
		return targetTemplate;
	}
	
	public List<ContentTemplate> getTargetTemplates() {
		return targetTemplates;
	}
	
	public void setTargetTemplate(ContentTemplate template) {
		targetTemplate = template;
	}
	
	public void setTargetTemplates(List<ContentTemplate> templates) {
		targetTemplates = templates;
	}
	
	public int getTargetDisplayMode() {
		return targetDisplayMode;
	}
	
	public void setTargetDisplayMode(int mode) {
		targetDisplayMode = mode;
	}
	
	public String getReverseLabel() {
		return sourceLabel;
	}
	
	public void setReverseLabel(String label) {
		this.sourceLabel = label;
	}
	
	public int getReverseDisplayMode() {
		return sourceDisplayMode;
	}
	
	public void setReverseDisplayMode(int mode) {
		sourceDisplayMode = mode;
	}
	
	public ContentTemplate getSourceTemplate() {
		return sourceTemplate;
	}
	
	public void setSourceTemplate(ContentTemplate template) {
		sourceTemplate = template;
	}
	
	@Override
	public boolean isAggregation() {
		return aggregation;
	}
	
	public void setAggregation(boolean value) {
		this.aggregation = value;
	} 
	
	public boolean isKeepVersion() {
		return keepVersion;
	}
	
	public boolean keepVersion() {
		return keepVersion;
	}

	public void setKeepVersion(boolean keepVersion) {
		this.keepVersion = keepVersion;
	}

	public AuditSet getAuditSet() {
		return AuditSet.MODEL;
	}
	
	@Override
	public boolean equals(Object object) {
		if (!(object instanceof RelationTemplate)) return false;
		return ((RelationTemplate)object).getId().equals(getId());
	}
}
