package com.novamens.kbee.content.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Proxy;
import org.hibernate.annotations.Type;

import com.novamens.content.model.Attribute;
import com.novamens.content.model.AttributeType;
import com.novamens.content.model.AttributeValidator;
import com.novamens.content.model.Multiplicity;
import com.novamens.content.model.ValidatorParser;
import com.novamens.dom.Json;
import com.novamens.kbee.dom.KbeeModelObject;
import com.novamens.kbee.json.KbeeJson;

@Entity
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="entity")
@Table(name = "KB_ATTRIBUTE")
@Proxy(lazy=false)
public class KbeeAttribute extends KbeeModelObject implements Attribute  {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeAttribute.class.getName());
	
	@Column(name = "portalsubtitle")
	private boolean portalSubtitle = false;

	@Column(name = "inportal")
	private boolean inportal = false;
	
	// Solr
	@Column(name = "uniquename")
	private String uniquename;
	
	@Column(name = "iscanonical")
	private boolean isgridcolumn = true;
	
	@Column(name = "metadatasubtitle")
	private boolean ismetadatasubtitle = true;
	
	@Column(name = "korder")
	private int order;

	@Column(name = "is_api")
	private boolean is_api = false;
	
	@Column(name = "default_structure")
	private boolean default_structure;

	// build a search filter based on this attribute 
	@Column(name = "isfilterable")
	private boolean isfilterable = false;

	@Column(name = "sortable")
	private boolean sortable = true;

	@Column(name = "is_rule_condition") // o internal
	private boolean is_rule_condition = false;

	// in advanced search fields
	@Column(name = "searchable")
	private boolean issearchable = false;
	
	@Column(name = "identitydocument")
	private boolean identitydocument = false;
	
	@Column(name = "boostfactor")
	private int boostFactor;
	
	// default multiplicity overriden by forms
	@Column(name = "multiplicity")
	@Enumerated(EnumType.ORDINAL)
	@Type(type="com.novamens.kbee.content.model.MultiplicityUserType")
	private Multiplicity multiplicity;
	
	@Column(name = "TYPE")
	@Enumerated(EnumType.ORDINAL)
	@Type(type="com.novamens.kbee.content.model.AttributeTypeUserType")
	private AttributeType type;
	
	@Column(name = "predicate")
	private String predicate;
	
	// Which Sections include this field as a column in the grid 
	@Column(name = "visibility")
	@Enumerated(EnumType.ORDINAL)
	@Type(type="com.novamens.kbee.json.JsonType")
	private Json visibility;
	
	@Column(name = "validator")
	@Enumerated(EnumType.ORDINAL)
	@Type(type="com.novamens.kbee.json.JsonType")
	private Json validator;
	
	public KbeeAttribute()	{
		super();
	};
	
	public String getDisplayName() {
		return getName();
	}
	
	public String getUniqueName() {
		return uniquename;
	}
	
	/**
	 * @param name
	 * name must be one the predefined SolR fields assigned to Attributes.
	 * 
	 */
	public void setUniqueName(String name) {
		this.uniquename = name;
	}
	
	@Override
	public Multiplicity getMultiplicity() {
		return multiplicity;
	}
	
	public void setMultiplicity(Multiplicity multiplicity) {
		this.multiplicity=multiplicity;
	}
	
	public AttributeType getType() {
		return type;
	}
	
	public void setType(AttributeType type) {
		this.type = type;
	}
	
	public String getPredicate() {
		return predicate;
	}
	
	public void setPredicate(String name) {
		this.predicate = name;
	}
	
	@Override
	public int getOrder() {
		return order;
	}
	
	public void setOrder(int order) {
		this.order=order;
	}
	
	public  void setOrdered(boolean b) {
		this.sortable=b;
	}
	
	@Override
	public boolean isOrdered() {
		return this.sortable;
	}

	@Override
	public boolean isDate() {
		return getType().equals(AttributeType.DATE) || getType().equals(AttributeType.VALIDITY_FROM) || getType().equals(AttributeType.VALIDITY_TO);
	}
	
	@Override
	public boolean isMetadataSubtitle() {
		return ismetadatasubtitle;
	}
	
	public void setMetadataSubtitle(boolean can) {
		ismetadatasubtitle = can;
	}
	
	@Override
	public boolean isFilterable() {
		return isfilterable;
	}
	
	public void setFilterable(boolean value) {
		isfilterable = value;
	}
	
	@Override
	public boolean isIdentityDocument() {
		return identitydocument;
	}

	public void setIdentityDocument(boolean identitynumber) {
		this.identitydocument = identitynumber;
	}

	@Override
	public boolean isRuleCondition() {
		return is_rule_condition;
	}
	
	public void setRuleCondition(boolean value)	{
		this.is_rule_condition = value;
	}
	
	@Override
	public boolean isSearchable() {
		return issearchable;
	}
	
	public void setSearchable(boolean value) {
		issearchable = value;
	}
	
	@Override
	public int getBoostFactor() {
		return boostFactor;
	}
	
	public void setBoostFactor(int value)	{
		this.boostFactor = value;
	}
	
	public boolean isRequired() {
		if (getMultiplicity()!=null && (getMultiplicity().equals(Multiplicity.M11) || getMultiplicity().equals(Multiplicity.M1N)))
			return true;
		return false;
	}

	public void setVisibility(String context, boolean value) {
		if(visibility==null) visibility = new KbeeJson();
		visibility.put(context, Boolean.valueOf(value).toString());
	}
	
	@Override
	public boolean isVisible(String context) {
		try {
			if (visibility == null || visibility.get(context)==null) return true;
			return "true".equals(visibility.get(context));
		} 
		catch (Exception e) {
			logger.error(e);
			return false;
		}
	}

	@Override
	public boolean isAPIClassifier() {
		return this.is_api;
	}

	@Override
	public void setAPIClassifier(boolean value) {
		this.is_api=value;
	}
	
	@Override
	public boolean isDefaultGridColumn() {
		return this.isgridcolumn;
	}

	public void setDefaultGridColumn(boolean val) {
		this.isgridcolumn = val;
	}
	
	public void setVisibilityJson(Json json_visibility) {
		this.visibility=json_visibility;
	}
	
	@Override
	public boolean equals(Object object) {
		if (!(object instanceof KbeeAttribute)) return false;
		return ((KbeeAttribute)object).getId().equals(getId());
	}
	
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append(super.toString());
		str.append("\nname: " + getName());
		if (getState()!=null)
			str.append("\nstate: " + getState());
		str.append("\nmultiplicity: " + multiplicity);
		return str.toString();
	}

	@Override
	public boolean isPortalSubtitle() {
		return portalSubtitle;
	}

	@Override
	public void setPortalSubtitle(boolean isportalsubtitle) {
		this.portalSubtitle = isportalsubtitle;
	}

	@Override
	public boolean isPortal() {
		return inportal;
	}

	@Override
	public void setPortal(boolean inportal) {
		this.inportal = inportal;
	}

	@Override
	public boolean isDefaultStructure() {
		return this.default_structure;
	}
	
	public void setDefaultStructure(boolean b) {
		this.default_structure=b;
	}
	
	@Override
	public AttributeValidator getValidator() {
		return validator!=null ? ValidatorParser.Get().getValidator(validator) : null;
	}
	
	public void setValidator(AttributeValidator validator) {
		this.validator = ValidatorParser.Get().getJson(validator);
	}
	
	@Override
	public Attribute clone() {
		KbeeAttribute clone = new KbeeAttribute();
		super.onClone(clone);
		
		clone.setMetadataSubtitle(isMetadataSubtitle());
		clone.setName(getName());
		clone.setAlias(getAlias());
		clone.setFilterable(isFilterable());
		clone.setSearchable(isSearchable());
		clone.setUniqueName(getUniqueName());
		clone.setMultiplicity(getMultiplicity());
		clone.setOrder(getOrder());
		clone.setType(getType());
		clone.setAPIClassifier(this.is_api);
		clone.setDefaultStructure(this.isDefaultStructure());
		clone.setVisibilityJson(this.visibility);
		return clone;
	}
}
