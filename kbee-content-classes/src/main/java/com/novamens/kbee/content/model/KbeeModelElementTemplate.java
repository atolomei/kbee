package com.novamens.kbee.content.model;

import com.novamens.content.model.AccessStrategy;
import com.novamens.content.model.AttributeSource;
import com.novamens.content.model.KbeeModelElement;
import com.novamens.content.model.ModelElement;
import com.novamens.content.model.ModelElementTemplate;
import com.novamens.content.model.ModelSection;
import com.novamens.content.model.Multiplicity;
import com.novamens.dom.Domain;

/**
 *  Esta clase funciona como un wrapper solo para la edición de la estructura. 
 *	Finalmente persiste como template de atributo o clasificador
 */
public class KbeeModelElementTemplate implements ModelElementTemplate {
	
	private ModelElement element;
	private String name;
	private ModelElement parent;
	private ModelSection section;
	private Domain domain;
	private boolean isMetadataSubtitle;
	private boolean isvisible;
	private boolean inherited;
	private boolean readOnly;
	private String subsection;
	private String criteria;
	private String selectionScript;
	private String calculationScript;
	private Multiplicity multiplicity;
	private AccessStrategy accessibility;
	private AttributeSource source = AttributeSource.UserInput;
	private boolean reverse = false;
	
	public void setElement(ModelElement element) {
		if (element instanceof KbeeModelElement) {
			this.element = ((KbeeModelElement)element).getElement();
			this.parent = ((KbeeModelElement)element).getParent();
			this.reverse = ((KbeeModelElement)element).isReverse();
		}
		else {
			this.element = element;
		}
	}
	
	public ModelElement getElement() {
		return this.element;
	}
	
	public String getName() {
		return name==null ? (getElement()!=null ? getElement().getName() : null) : name;
	}
	
	public void setName(String value) {
		this.name = value;
	}
	
	@Override
	public String getDisplayName() {
		return getParent()!=null ? 
			getParent().getDisplayName() + "->" + getName() : 
			(getElement()!=null ? getName() : "-");
	}
	
	public boolean isMetadataSubtitle() {
		return isMetadataSubtitle;
	}
	
	public void setMetadataSubtitle(boolean value) {
		isMetadataSubtitle = value;
	};
	
	public Multiplicity getMultiplicity() {
		if (multiplicity==null)
			return getElement().getMultiplicity();
		return multiplicity;
	}
	
	public void setMultiplicity(Multiplicity multiplicity) {
		this.multiplicity=multiplicity;
	}
	
	public AccessStrategy getAccessibility() {
		return accessibility;
	}
	
	public void setAccessibility(AccessStrategy accessibility) {
		this.accessibility = accessibility;
	}
	
	public boolean isVisible() {
		return isvisible;
	}
	
	public void setVisible(boolean value) {
		 isvisible = value;
	}
	
	public boolean isInherited() {
		return  inherited;
	}
	
	public void setInherited(boolean value) {
		inherited = value;
	}
	
	public boolean isReadOnly() {
		return readOnly;
	}
	
	public void setReadOnly(boolean value) {
		this.readOnly = value;
	}
	
//	@Override
//	public ModelSection getSection() {
//		return section;
//	}
	
//	@Override
//	public void setSection(ModelSection section) {
//		this.section = section;
//	}
	
	public String getSubsection() {
		return this.subsection;
	}
	
	public void setSubsection(String subsection) {
		this.subsection = subsection;
	}
	
	public String getValuesCriteria() {
		return criteria;
	}
	
	public void setValuesCriteria(String criteria) {
		this.criteria = criteria;
	}
	
	public ModelElement getParent() {
		return parent;
	}
	
	public void setParent(ModelElement parent) {
		this.parent = parent;
	}
	
	public AttributeSource getSource() {
		return source;
	}
	
	public String getSelectionScript() {
		return selectionScript;
	}
	
	public String getCalculationScript() {
		return calculationScript;
	}
	
	public void setDomain(Domain domain) {
		this.domain = domain;
	}
	
	public Domain getDomain() {
		return this.domain;
	}
	
	public int getOrder() {
		return 0;
	}

	@Override
	public boolean isMandatory() {
		return false;
	}
	
	@Override
	public boolean isReverse() {
		return reverse;
	}
	
	@Override
	public boolean isCanonical() {
		return false;
	}
}