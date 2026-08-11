package com.novamens.kbee.content.model;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Proxy;
import org.hibernate.annotations.Type;


import com.novamens.content.base.ContentClass;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetType;
import com.novamens.content.model.Multiplicity;
import com.novamens.dom.Json;

import com.novamens.kbee.dom.KbeeModelObject;
import com.novamens.kbee.json.KbeeJson;
import com.novamens.security.acl.Acl;

@Entity
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="entity")
@Table(name = "KB_CLASSIFIER")
@Proxy(lazy=false)
public class KbeeClassifier extends KbeeModelObject implements Classifier  {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeClassifier.class.getName());

	@Column(name = "uniquename")
	private String uniquename;
	
	// Usado por funciones semánticas
	@Column(name = "semantic")
	private boolean semantic = false;

	@Column(name = "default_structure")
	private boolean default_structure;
	
	// Enabled for grid columns by default
	@Column(name = "iscanonical")
	private boolean isgridcolumn = true;
	
	// Used in subtitles Metadata
	@Column(name = "metadatasubtitle")
	private boolean ismetadatasubtitle = false;

	@Column(name = "distribution")
	private boolean distribution = false;

	@Column(name = "organization")
	private boolean organization = false;
	
	@Column(name = "hierarchical")
	private boolean hierarchical = false;

	@Column(name = "identitydocumenttype")
	private boolean identityDocumentType = false;
	
	@Column(name = "is_content_type")
	private boolean content_type = false;
	
	@Column(name = "mydocument")
	private boolean mydocument = false;


	// Used in Metadata subtitles
	@Column(name = "portalsubtitle")
	private boolean portalSubtitle = false;
	
	@Column(name = "inportal")
	private boolean inportal = false;
	
	@Column(name = "searchable")
	private boolean issearchable = false;
	
	@Column(name = "ordered")
	private boolean ordered = true;
	
	// Se usa en rutinas semántica que requiere content type

	@Column(name = "workflow_status")
	private boolean workflow_status = false;
	
	// Usado para definir si es mostrable en forma estandar o de uso interno
	// para condiciones de reglas por ejemplo
	@Column(name = "displayable") // o internal
	private boolean displayable = true;
	
	@Column(name = "is_api") // Is used by the API, then the name, status, and multiplicity  can not be changed from the UI
	private boolean is_api = false;
	
	@Column(name = "is_rule_condition") // o internal
	private boolean is_rule_condition = false;
	
	@Column(name = "predicate")
	private String predicate;
	
	@Column(name = "korder")
	private int order;
	
	// Visible como faceta y columnas en las distintas consolas. 
	@Column(name = "visibility")
	@Enumerated(EnumType.ORDINAL)
	@Type(type="com.novamens.kbee.json.JsonType")
	private Json visibility;
	
	@Column(name = "multiplicity")
	@Enumerated(EnumType.ORDINAL)
	@Type(type="com.novamens.kbee.content.model.MultiplicityUserType")
	private Multiplicity multiplicity;

	@Column(name = "mandatory")
	private boolean mandatory;
	
	@Column(name = "hashome")
	private boolean hasHome;
	
	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, targetEntity = com.novamens.kbee.content.base.KbeeContentClass.class)
	@JoinTable(name = "ClassifierContent",  
		joinColumns 		= {@JoinColumn(name = "classifier_id") }, 
		inverseJoinColumns 	= {@JoinColumn(name = "contentclass_id") }
	)
	private List<ContentClass> enabledclasses = new ArrayList<ContentClass>();
	
	@ManyToOne(fetch = FetchType.EAGER, targetEntity=KbeeDataSet.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name ="DATASET_ID")
	private DataSet dataset1;
	
	@ManyToOne(fetch = FetchType.EAGER, targetEntity=KbeeDataSet.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name ="DATASET2_ID")
	private DataSet dataset2;
	
	@ManyToOne(fetch = FetchType.EAGER, targetEntity=KbeeDataSet.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name ="DATASET3_ID")
	private DataSet dataset3;

	@Transient
	private List<DataSet> datasets = null;
	
	
	
	public KbeeClassifier()	{
		super();
	}
	
	public KbeeClassifier(DataSet dataset) {
		this(dataset, dataset.getName());
	}
	
	public KbeeClassifier(DataSet dataset, String name) {
		super();
		this.setCreationOffsetDateTime(OffsetDateTime.now());
		addDataSet(dataset);
		setName(name);
	}

	@Override
	public boolean isPortalSubtitle() {
		return portalSubtitle;
	}

	public void setPortalSubtitle(boolean isportalsubtitle) {
		this.portalSubtitle = isportalsubtitle;
	}

	@Override
	public boolean isPortal() {
		return inportal;
	}

	public void setPortal(boolean inportal) {
		this.inportal = inportal;
	}
	
	public String getUniqueName() {
		return uniquename;
	}
	
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
	
	@Override
	public int getOrder() {
		return order;
	}
	
	@Override
	public void setOrder(int order) {
		this.order=order;
	}
	
	public boolean isOrdered() {
		return ordered;
	}
	
	public void setOrdered(boolean value)	{
		this.ordered = value;
	}

	public boolean isMandatory() {
		if (getMultiplicity().equals(Multiplicity.M11) || getMultiplicity().equals(Multiplicity.M1N))
			return true;
		return mandatory;
	}
	
	public void setMandatory(boolean value)	{
		this.mandatory = value;
	}

	public boolean isDisplayable() {
		return displayable;
	}
	
	public void setDisplayable(boolean value)	{
		this.displayable = value;
	}
	
	@Override
	public boolean isSearchable() {
		return issearchable;
	}
	
	public void setSearchable(boolean value) {
		issearchable = value;
	}

	@Override
	public boolean isRuleCondition() {
		return is_rule_condition;
	}
	
	public void setRuleCondition(boolean value)	{
		this.is_rule_condition = value;
	}
	
	@Override
	public boolean hasHome() {
		return hasHome;
	}

	public void setHasHome(boolean hasHome) {
		this.hasHome = hasHome;
	}

	public int getDataSetCount() {
		return getDataSets().size();
	}
	
	public void cleanDatasets() {
		dataset1 = null;
		dataset2 = null;
		dataset3 = null;
		datasets = null;
	}
	
	public void addDataSet(DataSet dataset) {
		if (dataset1==null)	dataset1=dataset;
			
		else if (dataset2==null)
			dataset2=dataset;
			
		else if (dataset3==null)
			dataset3=dataset;
		else {
			throw new RuntimeException("can not add more dataset (max. is 3)");
		}
		
		datasets=null;
	}
	
	public List<DataSet> getDataSets() {
		if (datasets==null) {
			datasets = new ArrayList<DataSet>();
			
		datasets.add(dataset1);
		
		if (dataset2!=null)
			datasets.add(dataset2);
		
		if (dataset3!=null)
			datasets.add(dataset3);
		}
		return datasets;
	}
	
	public DataSet getDataSet() {
		return dataset1;
	}
	
	public void setDataSet(DataSet dataset) {
		dataset1 = dataset;
	}
	
	public DataSet getDataSet2() {
		return dataset2;
	}
	
	public void setDataSet2(DataSet dataset) {
		dataset2 = dataset;
	}
	
	@Override
	public DataSetType getDataSetType() {
		return getDataSet().getDataSetType();
	}
	
	@Override
	public boolean includes(DataSet dataset) {
		return getDataSet().equals(dataset) || (dataset!=null && dataset.equals(getDataSet2()));  
	}
	
	@Override
	public boolean isMetadataSubtitle() {
		return ismetadatasubtitle;
	}
	
	public void setMetadataSubtitle(boolean can) {
		ismetadatasubtitle = can;
	}
	
	@Override
	public List<ContentClass> getContentClassesEnabled() {
		return enabledclasses;
	}

	@Override
	public void removeContentClass(ContentClass contentclass) {
		enabledclasses.remove(contentclass);	
	}

	@Override
	public void addContentClass(ContentClass contentclass)  {
		enabledclasses.add(contentclass);
	}
 	
	public Acl getACL() {
 		return null;
	}
	
	@Override
	public boolean equals(Object object) {
		if (!(object instanceof KbeeClassifier)) return false;
		return ((KbeeClassifier)object).getId().equals(getId());
	}

	@Override
	public boolean isSemantic() {
		return semantic;
	}

	public void setSemantic(boolean semantic) {
		this.semantic=semantic;
	}
	
	@Override
	public String getPredicate() {
		String predicate = this.predicate;
		if (predicate == null) {
			if (getName()!=null) {
				predicate = getName().toLowerCase().trim();
				predicate = predicate.replaceAll(" ", "");
			}
		}
		return predicate;
	}

	//@Override
	public void setPredicate(String predicate) {
		this.predicate = predicate;
	}

	@Override
	public boolean isContentType() {
		return content_type;
	}

	public void setContentType(boolean b) {
		content_type=b;
	}

	@Override		
	public boolean isWorkflowStatus() {
		return this.workflow_status;
	}

	public void setWorkflowStatus(boolean b) {
		workflow_status=b;
	}
	
	@Override
	public void setVisibility(String context, boolean value) {
		if(visibility==null) 
			visibility = new KbeeJson();
		visibility.put(context, Boolean.valueOf(value).toString());
	}
	
	@Override
	public boolean isVisible(String context) {
		try {
			if (visibility == null || visibility.get(context)==null) 
				return getDataSet()!=null? ( !(getDataSet().getDataSetType()==DataSetType.EXTERNAL || getDataSet().getDataSetType()==DataSetType.LABEL)): true;
			return "true".equals(visibility.get(context));
		} catch (Exception e) {
			logger.error(e);
			return false;
		}
	}
	
	public void setDefaultStructure(boolean b) {
		this.default_structure=b;
	}
	
	@Override
	public Classifier clone() {
		KbeeClassifier clone = new KbeeClassifier();

		clone.setMetadataSubtitle(this.isMetadataSubtitle());
		clone.setName(getName());
		clone.setRuleCondition(this.isRuleCondition());
		clone.setUniqueName(getUniqueName());
		clone.setDataSet(getDataSet());		
		clone.setDataSet2(getDataSet2());
		clone.setMultiplicity(getMultiplicity());
		clone.setOrder(getOrder());
		clone.setContentType(isContentType());
		clone.setMandatory(isMandatory());
		clone.setPredicate(getPredicate());
		clone.setDefaultGridColumn(this.isDefaultGridColumn());
		clone.setVisibilityJson(this.visibility);
		clone.setAPIClassifier(this.is_api);
		clone.setDefaultStructure(this.isDefaultStructure());
		clone.setOrganization(isOrganization());
		clone.setIdentityDocumentType(isIdentityDocumentType());
		
		return clone;
	}

	@Override
	public boolean isAPIClassifier() {
		return this.is_api;
	}

	public void setAPIClassifier(boolean value) {
		this.is_api=value;
	}

	public void setVisibilityJson(Json json_visibility) {
		this.visibility=json_visibility;
	}

	@Override
	public String getDisplayName() {
		return getName();
	}

	@Override
	public boolean isDefaultGridColumn() {
		return this.isgridcolumn;
	}

	public void setDefaultGridColumn(boolean val) {
		this.isgridcolumn = val;
	}

	@Override
	public boolean isDefaultStructure() {
		return this.default_structure;
	}

	@Override
	public boolean isDistribution() {
		return distribution;
	}

	public void setDistribution(boolean distribution) {
		this.distribution=distribution;
	}
	
	@Override
	public boolean isOrganization() {
		return organization;
	}

	public void setOrganization(boolean organization) {
		this.organization = organization;
	}
	
	@Override
	public boolean isHierarchical() {
		return hierarchical;
	}

	public void setHierarchical(boolean value) {
		this.hierarchical = value;
	}

	@Override
	public boolean isIdentityDocumentType() {
		return identityDocumentType;
	}

	public void setIdentityDocumentType(boolean identityDocumentType) {
		this.identityDocumentType = identityDocumentType;
	}
	
	@Override
	public boolean isMyDocument() {
		return mydocument;
	}

	public void setMyDocument(boolean mydocument) {
		this.mydocument = mydocument;
	}

	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append(super.toString());
		str.append(" | " + "Name: " + getName());
		
		if (getState()!=null)
			str.append(" | " + "State: " + getState());
		
		str.append(" | " + "Mandatory: " + (mandatory?"yes":"no"));
		str.append(" | " + "Multiplicity: " + multiplicity);
		
		if (getDataSetCount()==1 && getDataSet()!=null)	
			str.append(" | " + "Dataset: " + getDataSet().getName()!=null?getDataSet().getName():"null");
		else {
			List<DataSet> list = getDataSets();
			if (list.size()>0) {
				for (DataSet ds: list) {
					if (ds!=null)
						str.append(" | " + "Dataset: " + ds.getName()!=null?ds.getName():"null");
				}
			}
		}
		return str.toString();
	}
}