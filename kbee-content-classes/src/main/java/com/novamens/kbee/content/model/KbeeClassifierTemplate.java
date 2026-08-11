package com.novamens.kbee.content.model;


import java.io.Serializable;

import javax.persistence.Cacheable;
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
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ClassifierTemplate;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.ModelElement;
import com.novamens.content.model.Multiplicity;
import com.novamens.dom.Domain;
import com.novamens.security.Identifiable;
import com.novamens.service.ObjectService;
import com.novamens.service.ServiceLocator;
import com.novamens.service.ServiceNotFoundException;
import com.novamens.util.KbeeRuntimeException;

@Entity
@Inheritance(strategy=InheritanceType.JOINED)
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "entity")
@Table(name = "Kb_ClassifierTemplate")
public class KbeeClassifierTemplate implements ClassifierTemplate, Identifiable {
	
	@Id
	@SequenceGenerator(name = "template_sequencer", sequenceName = "domainid_sequence")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "template_sequencer")
	@Column(name = "id")
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = KbeeClassifier.class)
	@JoinColumn(name = "classifier_id", updatable=false)
	private Classifier classifier;
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = KbeeDataSetMember.class)
	@JoinColumn(name = "root_id", updatable=false)
	private DataSetMember root;
				
	@Column(name = "isvisible")
	private boolean isvisible;
	
	@Column(name = "inherited")
	private boolean inherited;
	
	@Column(name = "metadatasubtitle")
	private boolean ismetadatasubtitle;

	@Column(name = "portalsubtitle")
	private boolean isportalsubtitle;
	
	@Column(name = "multiplicity")
	@Enumerated(EnumType.ORDINAL)
	@Type(type="com.novamens.kbee.content.model.MultiplicityUserType")
	private Multiplicity multiplicity;
	
	@Column(name = "accessibility")
	@Enumerated(EnumType.ORDINAL)
	@Type(type="com.novamens.kbee.content.model.AccessStrategyUserType")
	private AccessStrategy accessibility;
	

	
	
	
	@Column(name = "criteria")
	private String criteria;

	@Column(name = "subsection")
	private String subsection;
	
	@Column(name = "korder")
	private int order;
	
	@Column(name = "position", insertable=false, updatable=false)
	private int position;
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = KbeeClassifier.class)
	@JoinColumn(name = "parent_id", updatable=false)
	private Classifier parent;
	
	@Column(name = "reverse")
	private boolean reverse;
	
	@Column(name = "canonical")
	private boolean canonical;
	
	@Column(name = "calculation")
	private String calculation;
	
	public KbeeClassifierTemplate() {
		super();
	}
	
	public KbeeClassifierTemplate(ClassifierTemplate src_tempalte) {
		super();
		
		setClassifier(src_tempalte.getClassifier());
		setVisible(src_tempalte.isVisible());
		setMetadataSubtitle(src_tempalte.isMetadataSubtitle());
		setPosition(src_tempalte.getPosition());
		
		if (src_tempalte.getRoot()!=null)
			setRoot(src_tempalte.getRoot());
		
		setVisible(src_tempalte.isVisible());
		setInherited(src_tempalte.isInherited());
		setMetadataSubtitle(src_tempalte.isMetadataSubtitle());

		setMultiplicity(src_tempalte.getMultiplicity()!=null?src_tempalte.getMultiplicity():Multiplicity.M0N);
		setAccessibility( src_tempalte.getAccessibility()!=null?src_tempalte.getAccessibility():AccessStrategy.All);
	}
	
	
	public KbeeClassifierTemplate(Classifier classifier) {
		this(classifier,0);
	}
	
	
	/**
	 * Multiplicity 
	 * Accesibility (Clasaifier Selector Strategy)
	 * 
	 * 
	 * @param classifier
	 * @param pos
	 */
	
	public KbeeClassifierTemplate(Classifier classifier, int pos) {
		super();
		setClassifier(classifier);
		setVisible(true);
		setPosition(pos);
		if (classifier.getMultiplicity()!=null) {
			if (classifier.getMultiplicity()==Multiplicity.M11 ||classifier.getMultiplicity()==Multiplicity.M1N) 
				setMetadataSubtitle(true);
			
			setMultiplicity(classifier.getMultiplicity()!=null?classifier.getMultiplicity():Multiplicity.M0N);
		}
		else
			setMultiplicity(Multiplicity.M0N);
		this.setAccessibility(AccessStrategy.All);
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
	
	@Override
	public ModelElement getElement() {
		return classifier;
	}
	
	@Override
	public DataSetMember getRoot() {
		return root;
	}
	
	@Override
	public void setRoot(DataSetMember member) {
		this.root = member;
	}
	
	@Override
	public boolean isReadOnly() {
		return false;
	}
	
	public void setReadOnly(boolean value) {
	}
	
	@Override
	public boolean isVisible() {
		return  isvisible;
	}
	
	@Override
	public void setVisible(boolean b) {
		 isvisible=b;
	}

	@Override
	public boolean isInherited() {
		return  inherited;
	}
	
	@Override
	public void setInherited(boolean b) {
		inherited=b;
	}

	@Override
	public boolean isMetadataSubtitle() {
		return  this.ismetadatasubtitle;
	}
	
	@Override
	public void setMetadataSubtitle(boolean b) {
		ismetadatasubtitle = b;
	}
	
	@Override
	public String getDisplayName() {
		String name = getClassifier()!=null ? getClassifier().getName() : "-";
		return getParent()!=null ? getParent().getDisplayName() + "->" + name : name;
	}
	
	public String getName() {
		return getDisplayName();
	}
	
	public void  setName(String value) {
	}
	
	@Override
	public Multiplicity getMultiplicity() {
		if (multiplicity==null)
			return getClassifier().getMultiplicity();
		return multiplicity;
	}
	
	@Override
	public void setMultiplicity(Multiplicity multiplicity) {
		this.multiplicity=multiplicity;
	}
	
	@Override
	public AccessStrategy getAccessibility() {
		//if (accessibility==null)
		//	return AccessStrategy.Roles;
		return accessibility;
	}
	
	public void setAccessibility(AccessStrategy accessibility) {
		this.accessibility=accessibility;
	}
	
	@Override
	public String getValuesCriteria()	{
		return criteria;
	}
	
	public void setValuesCriteria(String criteria) {
		this.criteria = criteria;
	}
	
	@Override
	public boolean isMandatory() {
		if (multiplicity==null)
			return getClassifier().isMandatory();
		if (getMultiplicity().equals(Multiplicity.M11) || getMultiplicity().equals(Multiplicity.M1N))
			return true;
		return false;
	}
	

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
		return this.isMetadataSubtitle();
	}
	
	public int getOrder()	{
		return order;
	}
	
	public void setOrder(int value) {
		this.order = value;
	}
	
	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}
	
	public ModelElement getParent() {
		return parent;
	}
	
	public void setParent(ModelElement element) {
		this.parent = (Classifier)element;
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
		return canonical;
	}
	
	public void setCanonical(boolean value) {
		this.canonical = value;
	}
	
	@Override
	public String getSelectionScript() {
		return calculation;
	}
	
	public void setSelectionScript(String code) {
		this.calculation = code;
	}
	
	public void setCalculation(String code) {
		this.calculation = code;
	}
	
	public <T extends ObjectService> T getService(Class<T> service) throws ServiceNotFoundException {
		return ServiceLocator.getService(this, service);
	}
	
	@Override
	public void setDefaultValues() {
		if (classifier!=null)
			multiplicity = classifier.getMultiplicity();
		if (multiplicity==null)
			multiplicity=Multiplicity.M01;
		accessibility= AccessStrategy.All;
	}


}
