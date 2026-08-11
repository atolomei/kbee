package com.novamens.kbee.content.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import com.novamens.content.model.AccessStrategy;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.AttributeSource;
import com.novamens.content.model.AttributeTemplate;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ClassifierTemplate;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetElementTemplate;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.KbeeModelElement;
import com.novamens.content.model.ModelElement;
import com.novamens.content.model.ModelElementTemplate;
import com.novamens.content.model.ModelSection;
import com.novamens.content.model.Multiplicity;
import com.novamens.dom.Domain;
import com.novamens.kbee.content.model.KbeeAttribute;
import com.novamens.kbee.content.model.KbeeClassifier;
import com.novamens.kbee.content.model.KbeeDataSet;
import com.novamens.security.Identifiable;
import com.novamens.service.ObjectService;
import com.novamens.service.ServiceLocator;
import com.novamens.service.ServiceNotFoundException;

@Entity
@Table(name = "Kb_Ds_Element_Template")
public class KbeeDataSetElementTemplate implements DataSetElementTemplate, AttributeTemplate, ClassifierTemplate, Identifiable {
	
	@Id
	@SequenceGenerator(name = "template_sequencer", sequenceName = "domainid_sequence")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "template_sequencer")
	@Column(name = "id")
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity=KbeeDataSet.class)
	@JoinColumn(name="dataset_id", insertable=false, updatable=false, nullable=false)
	private DataSet dataset;
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = KbeeClassifier.class)
	@JoinColumn(name = "classifier_id", updatable=false)
	private Classifier classifier;
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = KbeeAttribute.class)
	@JoinColumn(name = "attribute_id", updatable=false)
	private Attribute attribute;
				
	@Column(name = "readonly")
	private boolean readonly;
	
	@Column(name = "aggregation")
	private boolean aggregation;
	
	@Column(name = "canonical")
	private boolean canonical;
	
	@Column(name = "multiplicity")
	@Enumerated(EnumType.ORDINAL)
	@Type(type="com.novamens.kbee.content.model.MultiplicityUserType")
	private Multiplicity multiplicity;
	
	@Column(name = "position", insertable=false, updatable=false)
	private int position;
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = KbeeClassifier.class)
	@JoinColumn(name = "parent_id", updatable=false)
	private Classifier parent;
	
	public KbeeDataSetElementTemplate() {
	}
	
	public Long getId()	{
		return id;
	}
	
	public void setId(Serializable id) {
		this.id = (Long)id;
	}
	
	public Classifier getClassifier() {
		return classifier;
	}
	
	public void setClassifier(Classifier classifier) {
		this.classifier=classifier;
	}
	
	public Attribute getAttribute() {
		return attribute;
	}
	
	public void setAttribute(Attribute attribute) {
		this.attribute = attribute;
	}
	
	@Override
	public ModelElement getElement() {
		return classifier != null ? classifier : attribute;
	}
	
	public void setElement(ModelElement element) {
		if (element instanceof KbeeModelElement) {
			if (((KbeeModelElement)element).getElement() instanceof Attribute) {
				this.attribute = (Attribute)((KbeeModelElement)element).getElement();
			}	
			else
			if (((KbeeModelElement)element).getElement() instanceof Classifier) {
				this.classifier = (Classifier)((KbeeModelElement)element).getElement();
			}	
			this.parent = (Classifier)((KbeeModelElement)element).getParent();
		}
		else
		if (element instanceof Attribute) {
			this.attribute = (Attribute)element;
		}	
		else
		if (element instanceof Classifier) {
			this.classifier = (Classifier)element;
		}	
	}

	
	@Override
	public boolean isAggregation() {
		return aggregation;
	}
	
	public void setAggregation(boolean value) {
		 aggregation=value;
	}

	@Override
	public boolean isReadOnly() {
		return readonly;
	}
	
	public void setReadOnly(boolean value) {
		readonly = value;
	}
	
	@Override
	public String getDisplayName() {
		String name = getElement()!=null ? getElement().getName() : "-";
		name = getParent()!=null ? getParent().getDisplayName() + "->" + name : name;
		return name;
	}
	
	@Override
	public String getName() {
		return getDisplayName();
	}
	
	public void setName(String name) {
	}
	
	@Override
	public Multiplicity getMultiplicity() {
		return multiplicity;
	}
	
	public void setMultiplicity(Multiplicity multiplicity) {
		this.multiplicity=multiplicity;
	}
	
	@Override
	public AccessStrategy getAccessibility() {
		return AccessStrategy.Roles;
	}
	
	@Override
	public boolean isMandatory() {
		if (multiplicity==null)
			return getClassifier().isMandatory();
		if (getMultiplicity().equals(Multiplicity.M11) || getMultiplicity().equals(Multiplicity.M1N))
			return true;
		return false;
	}
	
	public int getOrder() {
		return 0;
	}
	
	public ModelSection getSection() {
		return null;
	}
	
	public String getSubsection() {
		return null;
	}
	
	public void setSection(ModelSection section) {
	}
	
	@Override
	public String getValuesCriteria() {
		return null;
	}
	
	public void setParent(ModelElement element) {
		this.parent = (Classifier)element;
	}
	
	public ModelElement getParent() {
		return parent;
	}
	
	@Override
	public Domain getDomain() {
		return getElement()!=null ? getElement().getDomain() : null;
	}

	@Override
	public void setDomain(Domain domain) {
	}
	
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
	
	public boolean isMetadataSubtitle() {
		return false;
	}	
	
	public void setMetadataSubtitle(boolean b) {
	}
	
	public boolean isPortalSubtitle() {
		return false;
	}
	
	public DataSetMember getRoot() {
		return null;
	}
	
	public void setRoot(DataSetMember member) {
		
	}
	
	public ClassifierTemplate getReverseOf() {
		return null;
	}
	
	public DataSet getDataSet() {
		return dataset;
	}
	
	@Override
	public boolean isAggregation(DataSet aggregator) {
		if (getClassifier()==null || 
			!getClassifier().getDataSet().isAggregation()) 
			return false;
		boolean aggregation = false;
		for (ModelElementTemplate template :  getClassifier().getDataSet().getStructure()) {
			if (template.getElement() instanceof Classifier) {
				if (((Classifier)template.getElement()).getDataSet().equals(aggregator)) {
					if (aggregation) return false;
					aggregation = true;
				}
			}
		}
		return aggregation;	
	}

	public void setPosition(int p) {
		this.position=p;
	}
	
	@Override
	public int getPosition() {
		return position;
	}
	
	@Override
	public boolean isReverse() {
		return false;
	}
	
	@Override
	public boolean isCanonical() {
		return canonical;
	}
	
	public void setCanonical(boolean value) {
		this.canonical = value;
	}
	
	@Override
	public AttributeSource   getSource() {
		return null;
	}

	
	@Override
	public String getSelectionScript() {
		return null;
	}
	
	@Override
	public String getCalculationScript() {
		return null;
	}
	
	public void setSelectioScript(String code) {
	}
	
	public void setCalculationScript(String code) {
	}
	
	public <T extends ObjectService> T getService(Class<T> service) throws ServiceNotFoundException {
		return ServiceLocator.getService(this, service);
	}
} 
