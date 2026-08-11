package com.novamens.kbee.content.model;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;

import com.novamens.content.model.AccessStrategy;
import com.novamens.content.model.AttributeTemplate;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ClassifierTemplate;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetElementTemplate;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.DataSetType;
import com.novamens.content.model.ExtractionRule;
import com.novamens.content.model.ModelElementTemplate;
import com.novamens.indexer.query.ResultSet;
import com.novamens.kbee.dom.AbstractObject;
import com.novamens.kbee.dom.KbeeModelObject;
import com.novamens.security.acl.Acl;

/**
 * 
 * 
 * 
 *
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "TYPE", discriminatorType = DiscriminatorType.INTEGER)
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="entity")
@Table(name = "DATASET")
public abstract class KbeeDataSet extends KbeeModelObject implements DataSet {
	
	@Column(name = "ALTERNATIVE_DISPLAY")
	private String alternative_display;

	@Column(name = "TYPE", nullable=false, insertable = false, updatable = false)
	@Enumerated(EnumType.ORDINAL)
	@Type(type="com.novamens.kbee.content.model.DataSetTypeUserType")
	private DataSetType type;
	
	@Column(name = "ENABLED")
	private boolean enabled = true;
	
	@Column(name = "HIERARCHICAL")
	private boolean hierachical = false;
	
	@Column(name = "CANONICAL")
	private boolean canonical = false;

	@Column(name = "SUGGESTER")
	private boolean suggester = true;
	
	@Column(name="external_id")
	private String externalId;
	
	@Column(name="readonly")
	private boolean readonly;
	
	@Column(name="aggregation")
	private boolean aggregation;
	
	@Column(name="displayname_rule")
	private String displayNameTemplate;
	
	/* this field is used by consoles when we require a different display name for sorting */
	@Column(name="console_displayname_rule")
	private String consoleDisplayNameTemplate;
	
	@Column(name="subline_rule")
	private String sublineTemplate;
	
	@Column(name="isdisplaynameeditable")
	private boolean isDisplayNameEditable = true;
	
	@Column(name="uniquevalues")
	private boolean uniqueValues = true;
	
	@Column(name = "access_strategy")
	@Enumerated(EnumType.ORDINAL)
	@Type(type="com.novamens.kbee.content.model.AccessStrategyUserType")
	private AccessStrategy access_strategy;
	
	// This should be a list of ClassifierTemplate
	// TODO: Set updatable to false so that no Cascade error occurs when deleting the Classifier ???
	@OneToMany(fetch = FetchType.LAZY, cascade=CascadeType.PERSIST, targetEntity = KbeeClassifier.class)
	@JoinTable(name = "DATASETCLASSIFIER", joinColumns = { @JoinColumn(name = "DATASET_ID") }, inverseJoinColumns = { @JoinColumn(name = "CLASSIFIER_ID") })
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region="entity")
	List<Classifier> classifiers = new ArrayList<Classifier>();
	
	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, targetEntity=KbeeAttributeTemplate.class)
	@JoinTable(name = "Kb_DataSetAttribute", 
		joinColumns = {	@JoinColumn(name = "DataSet_Id", nullable = false, updatable = false) }, 
		inverseJoinColumns = { @JoinColumn(name = "AttributeTemplate_Id", nullable = false, updatable = false) })
	@OrderColumn(name="position")
	List<AttributeTemplate> attributestemplates = new ArrayList<AttributeTemplate>();
		
	@OneToMany(orphanRemoval=true, fetch = FetchType.LAZY, cascade=CascadeType.ALL, targetEntity = KbeeDataSetElementTemplate.class)
	@JoinColumn(name = "dataset_id", nullable=false) 
	@OrderColumn(name="position")
	List<ModelElementTemplate> elementstemplates = new ArrayList<ModelElementTemplate>();
	
	public KbeeDataSet() {
	}
	
	public KbeeDataSet(String name, DataSetType type) {
		super();
		this.setCreationOffsetDateTime(OffsetDateTime.now());
		setName(name);
		setDataSetType(type);
		setEnabled(true);
	}
	
	public KbeeDataSet(String name) {
		this(name, DataSetType.STRING);
	}
	
	@Override
	public void setEnabled(boolean value) {
		this.enabled = value;
	}
	
	@Override
	public boolean isEnabled() {
		return this.enabled;
	}
	
	@Override
	public boolean isHierachical() {
		return this.hierachical;
	}
	
	//@Override
	public void setHierachical(boolean value) {
		this.hierachical = value;
	}
	
	@Override
	public boolean isCanonical() {
		return this.canonical;
	}
	
	public void setCanonical(boolean value) {
		this.canonical = value;
	}

	@Override
	public boolean isSuggester() {
		return this.suggester;
	}
	
	public void setSuggester(boolean value) {
		this.suggester = value;
	}
	
	@Override
	public AccessStrategy getAccessStrategy() {
		return access_strategy;
	}

	public void setAccessStrategy(AccessStrategy strategy) {
		access_strategy=strategy;
	}
	
	public void setDataSetType(DataSetType type) {
		this.type = type;
	}
	
	@Override
	public DataSetType getDataSetType() {
		return this.type;
	}
	
	@Override
	public String getDisplayName() {
		return getName();
	}
	
	@Override
	public void setName(String name) {
		super.setName(name);
		if (getAlias()==null)
			makeAlias(name);
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
	
	@Override
	public String getAlternativeDisplayName() {
		return alternative_display;
	}
	
	public void setAlternativeDisplayName(String dname) {
		this.alternative_display=dname;
	}
	
	@Override
	public ResultSet getData() {
		return null;
	}
	
	@Override
	public abstract DataSetMember createMember();
	
	@Override
	public Acl getACL() {
		return null;
	}
	
	@Override
	public List<Classifier> getClassifiers() {
		List<Classifier> classifiers = new ArrayList<Classifier>();
		for (ModelElementTemplate template : getStructure()) {
			if (template!=null && template.getElement() instanceof Classifier && ((ClassifierTemplate)template).getClassifier()!=null) {
				classifiers.add((Classifier)template.getElement());
			}
		}
		return classifiers;
	}
	
	@Override
	public void setClassifiers(List<Classifier> classifiers) {
		this.classifiers.clear();
		this.classifiers.addAll(classifiers);
	}
	
	@Override
	public List<AttributeTemplate> getAttributes() {
		List<AttributeTemplate> templates = new ArrayList<AttributeTemplate>();
		for (ModelElementTemplate template : getStructure()) {
			if (template!=null && template instanceof AttributeTemplate && ((AttributeTemplate)template).getAttribute()!=null) {
				templates.add((AttributeTemplate)template);
			}
		}
		return templates;
	}
	
	@Override
	public void setAttributes(List<AttributeTemplate> attributes) {
		attributestemplates.clear();;
		attributestemplates.addAll(attributes);
	}
	
	public List<ModelElementTemplate> getStructure() {
		return elementstemplates;
	}
	
	public void setStructure(List<DataSetElementTemplate> structure) {
		this.elementstemplates.clear();
		this.elementstemplates.addAll(structure);
	}
	
	@Override
	public boolean isAFunctionOf(DataSet dataset) {
		for (Classifier classifier : dataset.getClassifiers()) {
			if (classifier.getDataSet().equals(this))
				return true;
		}
		return false;
	}
	
	@Override
	public boolean isExternal() { 
		return false;
	}
	
	public DataSet clone() {
		return null;
	}
	
	@Override
	public String getExternalId() {
		return externalId;
	}

	@Override
	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}
	
	@Override
	public boolean isReadonly() {
		return readonly;
	}

	//@Override
	public void setReadonly(boolean readonly) {
		this.readonly = readonly;
	}
	
	public void setDisplayNameTemplate(String template) {
		this.displayNameTemplate = template;
	}

	
	@Override
	public String getDisplayNameTemplate() {
		return this.displayNameTemplate;
	}
	
	public void setConsoleDisplayNameTemplate(String template) {
		this.consoleDisplayNameTemplate = template;
	}

	
	@Override
	public String getConsoleDisplayNameTemplate() {
		if (this.consoleDisplayNameTemplate==null)
			return getDisplayNameTemplate();
		
		return this.consoleDisplayNameTemplate;
	}
	
	
	@Override
	public ExtractionRule getDisplayNameRule() {
		return getDisplayNameTemplate()!=null ? ExtractionRuleParser.Get().getRule(getDisplayNameTemplate()) : null;
	}
	
	public void setDisplayNameRule(ExtractionRule rule) {
		setDisplayNameTemplate(ExtractionRuleParser.Get().getJson(rule));
	}
	
	public void setSublineTemplate(String template) {
		this.sublineTemplate = template;
	}

	public String getSublineTemplate() {
		return this.sublineTemplate;
	}
	
	@Override
	public ExtractionRule getSublineRule() {
		return this.sublineTemplate!=null ? ExtractionRuleParser.Get().getRule(getSublineTemplate()) : null;
	}
	
	public void setSublineRule(ExtractionRule rule) {
		setSublineTemplate(ExtractionRuleParser.Get().getJson(rule));
	}
	
	@Override
	public boolean isDisplayNameEditable() {
		return isDisplayNameEditable;
	}
	
	public boolean getDisplayNameEditable() {
		return isDisplayNameEditable;
	}
	
	public void setDisplayNameEditable(boolean value) {
		isDisplayNameEditable = value;
	}
	
	@Override
	public boolean isAggregation() {
		return aggregation;
	}

	public void setAggregation(boolean value) {
		this.aggregation= value;
	}
	
	@Override
	public boolean isUniqueValues() {
		return uniqueValues;
	}

	public void setUniqueValues(boolean value) {
		this.uniqueValues= value;
	}
	
	public void onClone(DataSet clone) {
		super.onClone((AbstractObject) clone);
		((KbeeDataSet)clone).setReadonly(isReadonly());
		clone.setName(getName());
		((KbeeDataSet)clone).setAlternativeDisplayName(this.getAlternativeDisplayName());
		
		clone.setState(getState());
		((KbeeDataSet)clone).setAlias(getAlias());
		((KbeeDataSet)clone).setCanonical(isCanonical());
		((KbeeDataSet)clone).setDataSetType(getDataSetType());
		((KbeeDataSet)clone).setHierachical(isHierachical());
		((KbeeDataSet)clone).setAlternativeDisplayName(getAlternativeDisplayName());
		
		((KbeeDataSet)clone).setConsoleDisplayNameTemplate( this.getConsoleDisplayNameTemplate());
		((KbeeDataSet)clone).setSuggester(isSuggester());
	}
	
	@Override
	public boolean equals(Object object) {
		if (!(object instanceof KbeeDataSet)) return false;
		return ((KbeeDataSet)object).getId().equals(getId());
	}
	
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append(super.toString());
		
		str.append("\nName: " + getName());
		str.append("\nAlias " + getAlias());
		str.append("\nEnabled: " + (enabled?"Yes":"No"));
		
		if (getDataSetType()!=null)
			str.append("\nType: " + getDataSetType().getLabel());
		
		if (getState()!=null)
			str.append("\nState: " + getState());
		
		return str.toString();
	}

	protected DataSet clone(DataSet clone) {
		super.onClone((AbstractObject) clone);
		((KbeeDataSet)clone).setReadonly(isReadonly());
		clone.setName(getName());
		clone.setState(getState());
		((KbeeDataSet)clone).setAlias(getAlias());
		((KbeeDataSet)clone).setCanonical(isCanonical());
		((KbeeDataSet)clone).setDataSetType(getDataSetType());
		((KbeeDataSet)clone).setHierachical(isHierachical());
		((KbeeDataSet)clone).setAlternativeDisplayName(getAlternativeDisplayName());
		((KbeeDataSet)clone).setSuggester(isSuggester());
		((KbeeDataSet)clone).setConsoleDisplayNameTemplate( this.getConsoleDisplayNameTemplate());
		return clone;
	}
	
	
}