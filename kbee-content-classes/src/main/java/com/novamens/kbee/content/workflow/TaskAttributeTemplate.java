package com.novamens.kbee.content.workflow;

import java.io.Serializable;

import org.hibernate.SessionFactory;

import com.novamens.beans.BeansService;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.AttributeSource;
import com.novamens.content.model.AttributeTemplate;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ModelElement;
import com.novamens.content.model.ModelSection;
import com.novamens.content.model.Multiplicity;
import com.novamens.dom.Domain;
import com.novamens.kbee.content.model.KbeeAttribute;
import com.novamens.kbee.content.model.KbeeClassifier;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeRuntimeException;

public class TaskAttributeTemplate implements AttributeTemplate, Serializable {
	private static final long serialVersionUID = 1L;
	
	private Serializable attributeId, parentId;;
	private String subsection = null;
	private boolean readOnly = false;
	private int order;
	private Multiplicity multiplicity;
	private AttributeSource source = AttributeSource.UserInput;
	private String calculation;
	
	public Attribute getAttribute() {
		if (attributeId==null) 
			return null;
		SessionFactory sf = (SessionFactory)ServiceLocator.getService(BeansService.class).getBean("sessionFactory");
		Attribute attribute = (Attribute)sf.getCurrentSession().load(KbeeAttribute.class, this.attributeId);
		return attribute;
	}
	
	@Override
	public ModelElement getElement() {
		return getAttribute();
	}
	
	public void setAttribute(Attribute attribute) {
		this.attributeId = attribute!=null ? attribute.getId() : null;
	}
	
	@Override
	public String getDisplayName() {
		String name = getAttribute()!=null ? getAttribute().getDisplayName() : "-";
		return getParent()!=null ? getParent().getDisplayName() + "->" + name : name;
	}
	
	@Override
	public String getName() {
		return getDisplayName();
	}
	
	public void  setName(String value) {
	}
	
	public boolean isReadOnly() {
		return readOnly;
	}
	
	public void setReadOnly(boolean value) {
		this.readOnly = value;
	}
	
	public boolean isMetadataSubtitle() {
		return false;
	}
	
	public void setMetadataSubtitle(boolean value) {
		
	}

	@Override
	public boolean isPortalSubtitle() {
		return false;
	}
	
	public Multiplicity getMultiplicity() {
		return this.multiplicity;
	}
	
	public void setMultiplicity(Multiplicity multiplicity) {
		this.multiplicity=multiplicity;
	}
	
	public void setOrder(int value)	{
		this.order = value;
	}
	
	@Override
	public int getOrder()	{
		return order;
	}
	
//	@Override
//	public ModelSection getSection() {
//		return null;
//	}
	
//	@Override
//	public void setSection(ModelSection section) {
//	}
	
	public boolean isVisible() {
		return true;
	}
	
	public void setVisible(boolean b) {
	}
	
	public boolean isInherited() {
		return false;
	}
	
	public void setInherited(boolean b) {
	}
	
	public String getSubsection() {
		return subsection;
	}
	
	public void setSubsection(String subsection) {
		this.subsection = subsection;
	}
	
	public void setParent(ModelElement element) {
		this.parentId = element!=null ? element.getId() : null;
	}
	
	public ModelElement getParent() {
		if (parentId==null) return null;
		SessionFactory sf = (SessionFactory)ServiceLocator.getService(BeansService.class).getBean("sessionFactory");
		Classifier parent = (Classifier)sf.getCurrentSession().load(KbeeClassifier.class, this.parentId);
		return parent;
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
	public Domain getDomain() {
		return getAttribute().getDomain();
	}

	@Override
	public void setDomain(Domain domain) {
		throw new KbeeRuntimeException("Can not be assigned outside of the Classifier");
	}

	@Override
	public boolean isMandatory() {
		return getMultiplicity()!=null &&
			(getMultiplicity() == Multiplicity.M1N||
			getMultiplicity() == Multiplicity.M11 );
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
	
	public String getValuesCriteria() {
		return null;
	}
}