package com.novamens.kbee.content.workflow;

import java.io.Serializable;

import org.hibernate.SessionFactory;

import com.novamens.beans.BeansService;
import com.novamens.content.model.AccessStrategy;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ClassifierTemplate;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.ModelElement;
import com.novamens.content.model.ModelSection;
import com.novamens.content.model.Multiplicity;
import com.novamens.dom.Domain;
import com.novamens.kbee.content.model.KbeeClassifier;
import com.novamens.service.ObjectService;
import com.novamens.service.ServiceLocator;
import com.novamens.service.ServiceNotFoundException;
import com.novamens.util.KbeeRuntimeException;

public class TaskClassifierTemplate implements ClassifierTemplate, Serializable {
	private static final long serialVersionUID = 1L;

	private Serializable classifierId, parentId;
	
	private String subsection = null; 
	private boolean readOnly = false; 
	
	private Multiplicity multiplicity;
	private AccessStrategy accessibility;
	
	private boolean visible = true;  // ver si el setter se debe propagar al classifier ???
	private boolean reverse = false;
	private String valuesCriteria;
	private int order = 0;
	private String calculation;
	
	
	
	
	

	
	
	public Classifier getClassifier() {
		if (classifierId==null) return null;
		SessionFactory sf = (SessionFactory)ServiceLocator.getService(BeansService.class).getBean("sessionFactory");
		Classifier classifier = (Classifier)sf.getCurrentSession().load(KbeeClassifier.class, this.classifierId);
		return classifier;
	}
	
	public void setClassifier(Classifier classifier) {
		this.classifierId = classifier!=null ? classifier.getId() : null;
	}
	
	@Override
	public ModelElement getElement() {
		return getClassifier();
	}
	
	@Override
	public String getDisplayName() {
		String name = getClassifier()!=null ? getClassifier().getDisplayName() : "-";
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

	public DataSetMember getRoot() {
		return null;
	}

	@Override
	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean b) {
		visible=b;
	}
	
	@Override
	public boolean isInherited() {
		return false;
	}
	
	@Override
	public boolean isMetadataSubtitle() {
		return false;
	}

	@Override
	public void setMetadataSubtitle(boolean b) {
	}
	
	@Override
	public void setInherited(boolean b) {
	}

	@Override
	public void setRoot(DataSetMember member) {
	}

	@Override
	public Multiplicity getMultiplicity() {
		if (multiplicity==null)
			return getClassifier().getMultiplicity();
		return multiplicity;
	}

	@Override
	public void setMultiplicity(Multiplicity multiplicity) {
		this.multiplicity = multiplicity;
	}
	
	@Override
	public AccessStrategy getAccessibility() {
		return accessibility;
	}
	
	public void setAccessibility(AccessStrategy strategy) {	
		this.accessibility = strategy;
	}
	
	@Override
	public String getValuesCriteria() {
		return valuesCriteria;
	}
	
	public void setValuesCriteria(String criteria) {
		this.valuesCriteria = criteria;
	}

	@Override
	public boolean isMandatory() {
		if (multiplicity==null)
			return getClassifier().isMandatory();
		if (getMultiplicity().equals(Multiplicity.M11) || getMultiplicity().equals(Multiplicity.M1N))
			return true;
		return false;
	}
	
//	@Override
//	public ModelSection getSection() {
//		return null;
//	}
	
//	@Override
//	public void setSection(ModelSection section) {
//	}
//	
//	@Override
//	public String getSubsection() {
//		return subsection;
//	}

//	@Deprecated
//	public void setSubsection(String subsection) {
//		this.subsection = subsection;
//	}

	@Override
	public Domain getDomain() {
		return getClassifier().getDomain();
	}

	@Override
	public void setDomain(Domain domain) {
		throw new KbeeRuntimeException("Can not be assigned outside of the Classifier");
	}

	@Override
	public boolean isPortalSubtitle() {
		return false;
	}
	
	public void setOrder(int value)	{
		this.order = value;
	}
	
	@Override
	public int getOrder()	{
		return order;
	}
	
	public int getPosition() {
		return 0;
	}
	
	public void setParent(ModelElement element) {
		this.parentId = element!=null ? element.getId() : null;
	}
	
	@Override
	public boolean isReverse() {
		return reverse;
	}
	
	public void setReverse(boolean value) {
		this.reverse = value;
	}
	
	@Override
	public boolean isCanonical() {
		return false;
	}
	
	public ModelElement getParent() {
		if (parentId==null) return null;
		SessionFactory sf = (SessionFactory)ServiceLocator.getService(BeansService.class).getBean("sessionFactory");
		Classifier parent = (Classifier)sf.getCurrentSession().load(KbeeClassifier.class, this.parentId);
		return parent;
	}
	
	public ClassifierTemplate getReverseOf() {
		return null;
	}
	
	@Override
	public String getSelectionScript() {
		return calculation;
	}
	
	public void setSelectionScript(String code) {
		this.calculation = code;
	}
	
	public <T extends ObjectService> T getService(Class<T> service) throws ServiceNotFoundException {
		return ServiceLocator.getService(this, service);
	}
}
