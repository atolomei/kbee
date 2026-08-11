package com.novamens.kbee.content.model;

import java.io.Serializable;

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
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;

import com.novamens.content.model.AccessStrategy;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.AttributeSource;
import com.novamens.content.model.AttributeTemplate;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ModelElement;
import com.novamens.content.model.Multiplicity;
import com.novamens.dom.Domain;
import com.novamens.security.Identifiable;
import com.novamens.util.KbeeRuntimeException;


/**
 * 
 * 
 * private Multiplicity multiplicity;
 * AttributeSource
 *  
 *
 */
@Entity
@Inheritance(strategy=InheritanceType.JOINED)
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "entity")
@Table(name = "KB_AttributeTemplate")
public class KbeeAttributeTemplate implements AttributeTemplate, Identifiable {

	@Id
	@SequenceGenerator(name = "template_sequencer", sequenceName = "domainid_sequence")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "template_sequencer")
	@Column(name = "id")
	private Long id;
	
	//@ManyToOne(fetch = FetchType.LAZY, cascade=CascadeType.ALL, targetEntity = KbeeModelSection.class)
	//@JoinColumn(name = "section_id")
	//private ModelSection section;
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = KbeeAttribute.class)
	@JoinColumn(name = "attribute_id", updatable=false)
	private Attribute attribute;
	
	@Column(name = "metadatasubtitle")
	private boolean isMetadataSubtitle;

	@Column(name = "portalsubtitle")
	private boolean isportalSubtitle;
	
	@Column(name = "multiplicity")
	@Enumerated(EnumType.ORDINAL)
	@Type(type="com.novamens.kbee.content.model.MultiplicityUserType")
	private Multiplicity multiplicity;
	
	@Column(name = "subsection")
	private String subsection;
	
	@Column(name = "isvisible")
	private boolean isvisible;
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = KbeeClassifier.class)
	@JoinColumn(name = "parent_id", updatable=false)
	private Classifier parent;
	
	@Column(name = "source")
	@Enumerated(EnumType.ORDINAL)
	@Type(type="com.novamens.kbee.content.model.AttributeSourceUserType")
	private AttributeSource source = AttributeSource.UserInput;
	
	@Column(name = "korder")  
	private int order;
	
	@Column(name = "calculation")
	private String calculation;

	
	public KbeeAttributeTemplate() {
		super();
	}
	
	public KbeeAttributeTemplate(Attribute attribute) {
		setAttribute(attribute);
	}
	
	public KbeeAttributeTemplate(AttributeTemplate src) {
		super();
		
		setMetadataSubtitle(src.isMetadataSubtitle());
		setAttribute(src.getAttribute());
	}
	
	public Long getId()	{
		return id;
	}
	
	public void setId(Serializable id) {
		this.id = (Long)id;
	}
	
	public boolean isMetadataSubtitle() {
		return isMetadataSubtitle;
	}
	
	public void setMetadataSubtitle(boolean value) {
		isMetadataSubtitle = value;
	};
	
	@Override
	public String getDisplayName() {
		String name =  (getAttribute()!=null?getAttribute().getName():"");
		return getParent()!=null ? getParent().getDisplayName() + "->" + name : name;
	}
	
	public String getName() {
		return getDisplayName();
	}
	
	public void  setName(String value) {
	}
	
	public Attribute getAttribute() {
		return attribute;
	}
	
	@Override
	public ModelElement getElement() {
		return attribute;
	}
	
	public void setAttribute(Attribute attribute) {
		this.attribute = attribute;
	}
	
	public boolean isReadOnly() {
		return false;
	}
	
	public boolean isVisible() {
		return isvisible;
	}
	
	public void setVisible(boolean value) {
		isvisible = value;
	}

	public boolean isInherited() {
		return false;
	}
	
	public void setInherited(boolean b) {
	}
	
	@Override
	public boolean isMandatory() {
		return getMultiplicity()!=null &&
				(getMultiplicity() == Multiplicity.M1N ||
				getMultiplicity() == Multiplicity.M11 );
	}

	@Override
	public Multiplicity getMultiplicity() {
		return multiplicity;
	}
	
	public void setMultiplicity(Multiplicity multiplicity) {
		this.multiplicity=multiplicity;
	}
	
	public AccessStrategy getAccessibility() {
		return null;
	}
	
	public String getValuesCriteria() {
		return null;
	}
	
	@Override
	public Domain getDomain() {
		return getAttribute().getDomain();
	}

	@Override
	public void setDomain(Domain domain) {
		throw new KbeeRuntimeException("Can not be assigned outside of the Classifier");
	}

	@Override
	public boolean isPortalSubtitle() {
		return this.isMetadataSubtitle;
	}
	
	@Override
	public int getOrder()	{
		return order;
	}
	
	public void setOrder(int value) {
		this.order = value;
	}
	
//	@Override
//	public ModelSection getSection() {
//		return section;
//	}
	
//	public void setSection(ModelSection section) {
//		this.section = section;
//	}
	
//	@Deprecated
//	public String getSubsection() {
//		return this.subsection;
//	}
//	
//	@Deprecated
//	public void setSubsection(String subsection) {
//		this.subsection = subsection;
//	}
	
	public ModelElement getParent() {
		return parent;
	}
	
	public void setParent(ModelElement element) {
		this.parent = (Classifier)element;
	}
	
	@Override
	public boolean isReverse() {
		return false;
	}
	
	public void setReverse(boolean value) {
	}
	
	@Override
	public boolean isCanonical() {
		return false;
	}
	
	@Override
	public AttributeSource getSource() {
		return source;
	}
	
	public void setSource(AttributeSource source) {
		this.source = source;
	}

	
	@Override
	public String getCalculationScript() {
		return calculation;
	}
	
	public void setCalculationScript(String code) {
		this.calculation = code;
	}
	
	@Override
	public boolean equals(Object object) {
		if (!(object instanceof KbeeAttributeTemplate)) return false;
		return getId()!=null && getId().equals(((KbeeAttributeTemplate)object).getId());
	}
	
	@Override
	public void setDefaultValues() {
		if (attribute!=null)
			multiplicity = attribute.getMultiplicity();
		if (multiplicity==null)
			multiplicity=Multiplicity.M01;
		source = AttributeSource.UserInput;
	}



}
