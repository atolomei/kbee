package com.novamens.kbee.content.model;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
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

import com.novamens.content.model.ModelElement;
import com.novamens.content.model.ModelSection;
import com.novamens.content.model.Multiplicity;
import com.novamens.content.model.Subsection;
import com.novamens.content.model.SubsectionTemplate;
import com.novamens.dom.Domain;
import com.novamens.security.Identifiable;

//@Entity
//@Inheritance(strategy=InheritanceType.JOINED)
//@Table(name = "Kb_SubsectionTemplate")
public class KbeeSubsectionTemplate implements SubsectionTemplate, Identifiable {
	
	@Id
	@SequenceGenerator(name = "template_sequencer", sequenceName = "domainid_sequence")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "template_sequencer")
	@Column(name = "id")
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY, cascade=CascadeType.ALL, targetEntity=KbeeModelSection.class)
	@JoinColumn(name = "section_id")
	private ModelSection section;
	
	@Column(name = "name")
	private String name;
	
	@Column(name = "korder")
	private int order;

	private transient Subsection subsection;
	
	
	public KbeeSubsectionTemplate() {
	}
	
	public KbeeSubsectionTemplate(Subsection subsection) {
		this.subsection = subsection;
		this.name = subsection.getName();
	}
	
	public Long getId()	{
		return id;
	}
	
	public void setId(Serializable id) {
		this.id = (Long)id;
	}
	
	public ModelElement getElement() {
		if (subsection==null) {
			subsection = new KbeeSubsection(getName());
		}
		return subsection;
	}
	
	@Override
	public Multiplicity getMultiplicity() {
		return Multiplicity.M11;
	}
	
	@Override
	public String getDisplayName() {
		return getElement()!=null ? getElement().getDisplayName() : null;
	}
	
	public String getName()	{
		return name;
	}
	
	public void setName(String value) {
		((KbeeSubsection)getElement()).setName(value);
		this.name = value;
	}
	
	public int getOrder()	{
		return order;
	}
	
	public void setOrder(int value) {
		this.order = value;
	}
	
	public String getSubsection() {
		return getName();
	}
	
	public void setSubsection(String name) {
	}
	
	public ModelSection getSection() {
		return section;
	}
	
//	@Override
//	public void setSection(ModelSection section) {
//		this.section = section;
//	}
	
	public ModelElement getParent() {
		return null;
	}
	
	public Domain getDomain() {
		return null;
	}
	
	public void setDomain(Domain domain) {
	}

	@Override
	public boolean isMandatory() {
		return false;
	}
	
	@Override
	public boolean isReverse() {
		return false;
	}
	
	@Override
	public boolean isCanonical() {
		return false;
	}
	
	public void setReverse(boolean value) {
		
	}

}