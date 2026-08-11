package com.novamens.kbee.content.model;


import java.io.Serializable;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.codesnippets4all.json.generators.JSONGenerator;
import com.codesnippets4all.json.generators.JsonGeneratorFactory;
import com.codesnippets4all.json.parsers.JSONParser;
import com.codesnippets4all.json.parsers.JsonParserFactory;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.AttributeTemplate;
import com.novamens.content.model.AttributeType;
import com.novamens.content.model.Classification;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ClassifierTemplate;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.DataSetType;
import com.novamens.content.text.Text;
import com.novamens.datetime.DateTimeService;
import com.novamens.dom.Json;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.content.text.KbeeText;
import com.novamens.kbee.dom.AbstractObject;
import com.novamens.kbee.json.KbeeJson;
import com.novamens.security.acl.Acl;
import com.novamens.security.audit.AuditSet;
import com.novamens.service.ServiceLocator;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "TYPE", discriminatorType = DiscriminatorType.INTEGER)
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="entity")
@Table(name = "DATASETMEMBER")
public abstract class KbeeDataSetMember extends AbstractObject implements DataSetMember {
				
	static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeDataSetMember.class.getName());
	
	@Id 
	@SequenceGenerator(name = "member_sequencer", sequenceName = "entityid_sequence")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "member_sequencer")
	@Column(name = "ID")
	private Long id;

	@Column(name = "STRVALUE")
	private String strvalue;
	
	@Column(name = "ALTERNATIVE_DISPLAY")
	private String alternative_display;
	
	@Column(name = "DATEVALUE")
	private OffsetDateTime datevalue;
	
	@Column(name = "TYPE", insertable=false, updatable=false)
	private int type;
	
	@ManyToOne(fetch = FetchType.LAZY,  targetEntity=KbeeDataSetMember.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name ="PARENT")
	private DataSetMember parent;
	
	@ManyToOne(fetch = FetchType.LAZY,  targetEntity=KbeeDataSet.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "DATASET_ID", updatable=false)
	private DataSet dataset;
	
	@OneToMany(orphanRemoval=true, fetch = FetchType.LAZY, cascade=CascadeType.ALL, targetEntity = KbeeMemberClassification.class)
	@JoinColumn(name = "sourcemember_id", nullable=false) 
	@OrderColumn(name="position")
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region="content")
	List<Classification> classifications = new ArrayList<Classification>();
	
	@ManyToMany(fetch = FetchType.LAZY, targetEntity=KbeeDataSetMember.class)
	@JoinTable(name = "Kb_Member_Parent", 
		joinColumns = {	@JoinColumn(name = "member_id", nullable = false, updatable = false) }, 
		inverseJoinColumns = { @JoinColumn(name = "parent_id", nullable = false, updatable = false) })
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="query")
	List<DataSetMember> parents = new ArrayList<>();
	
	@Column(name = "ATTRIBUTES")
	private String attributes;
	
	@Column(name = "notes")
	private String 	notes;

	@Column(name="external_id")
	private String externalId;

	@Column(name = "issystem")
	private boolean issystem;
	
	
	@Column(name = "key")
	private String key;
	
	public List<Classifier> getClassifiers() {
		return getDataSet().getClassifiers();
	}
	
	@Override
	public String getConsoleDisplayName() {
		return getAlternativeDisplayName();
	}
	
	@Override
	public String getKey() {
		return key;
	}
	
	@Override
	public void setKey(String key) {
		this.key=key;
	}
	
	private transient Json jattributes;
	
	
	public KbeeDataSetMember() {
	}
	
	public KbeeDataSetMember(DataSet dataset) {
		super();
		setDataSet(dataset);
		setState(dataset.getState());
	}
	
	public KbeeDataSetMember(String value, DataSet dataset) {
		super();
		setDataSet(dataset);
		setValue(value);
		setState(dataset.getState());
	}
	
//	public KbeeDataSetMember(String value, DataSetMember parent, DataSet dataset) {
//		super();
//		setDataSet(dataset);
//		setParent(parent);
//		setValue(value);
//		setState(parent.getState());
//	}

	@Override
	public Text getNotes() {
		if (this.notes==null)
			return null;
		return new KbeeText(this.notes);
	}
	
	
	@Override
	public void setNotes(String text) {
		if (text!=null)
			this.notes = text;
		else
			this.notes=null;
	}
	
	public String getAlias() {
		return String.valueOf(getId());
	}
	
	public Long getId()	{
		return id;
	}
	
	public void setId(Serializable id) {
		this.id = (Long)id;
	}
	
	public Object getValue() {
		if (dataset.getDataSetType().equals(DataSetType.STRING))			return strvalue;
		else if (dataset.getDataSetType().equals(DataSetType.LABEL))		return strvalue;
		else if (dataset.getDataSetType().equals(DataSetType.ENTITY))		return strvalue;
		else																return datevalue;
	}
	
	public void setValue(Object value) {
		
		if (value==null) {
			strvalue =null;
			return;
		}
		
		if 		(dataset.getDataSetType().equals(DataSetType.STRING))		strvalue = ((String) value).trim();
		else if (dataset.getDataSetType().equals(DataSetType.LABEL))		strvalue = ((String) value).trim();
		else if (dataset.getDataSetType().equals(DataSetType.ENTITY))		strvalue = ((String) value).trim();
		else if (dataset.getDataSetType().equals(DataSetType.EXTERNAL))		strvalue = ((String) value).trim();
		else if (dataset.getDataSetType().equals(DataSetType.SECURED))		strvalue = ((String) value).trim();
		
		
		
		else if (dataset.getDataSetType().equals(DataSetType.DATE))  // legacy 		
			datevalue = (OffsetDateTime) value;
		else {
			logger.error("Not Supported Type " + dataset.getDataSetType().getLabel());
			strvalue = ((String) value.toString()).trim();
		}
	}
	
	public String getStrValue() {
		// This should not be used. Compatility
		if (dataset.getDataSetType().getId()==DataSetType.DATE.getId()) {
			return datevalue.toString();
		}
		else
			return strvalue;
	}
	
	public OffsetDateTime getDateValue() {
		return datevalue;
	}

	public void setStrValue(String value) {
		if (value!=null)
			this.strvalue=value.trim();
	}
	
	public void setDateValue(OffsetDateTime value) {
		this.datevalue=value;
	}
	
	public DataSet getDataSet() {
		return dataset;
	}
	
	public void	setDataSet(DataSet dataset)	{
		this.dataset=dataset;
	}

	public DataSetMember getParent() {
		return parent;
	}
	
	public List<DataSetMember> getParents() {
		return parents;
	}
	
	public void setParents(List<DataSetMember> parents) {
		this.parents = parents;
	}
	
	public Acl getACL() {
		return null;
	}
	
	public int getType() {
		return type;
	}

//	@Override
//	public void setParent(DataSetMember parent) {
//		this.parent=parent;
//	}
	
	@Override
	public String getName() {
			return getStrValue();
	}
	
	@Override
	public int getLevel() {
		if (getParents()==null || getParents().isEmpty())
			return 0;
		
		int level = 0;
		
		for (DataSetMember parent : getParents()) {
			int l = parent.getLevel()+1;
			if (l>level) level = l;
		}
		
		return level;
	}
	
	public List<Classification> getClassification()	{
		return classifications;
	}

	
	@Override
	public void addClassification(Classification c) {
		if (!this.classifications.contains(c))
			this.classifications.add(c);
	}

	@Override
	public void addClassification(Classifier c, DataSetMember dm) {
		Classification cl=new KbeeMemberClassification(c, dm, this);
		if (!this.classifications.contains(cl))
			this.classifications.add(cl);
	}
	
	@Override
	public void removeClassification(Classification c) {
		this.classifications.remove(c);
	}
	
	@Override
	public void removeAllClassification(Classifier classifier) {
		List<Classification> list = null;
		for (Classification classification : classifications) {
			if (classification.getClassifier().getId().equals(classifier.getId())) {
				if (list==null)
					list=new ArrayList<Classification>();
				list.add(classification);
			}
		}
		if (list==null)
			return;
		list.forEach(item->classifications.remove(item));
	}
	
	public List<Classification> getClassification(Classifier classifier) {
		List<Classification> selection = new ArrayList<Classification>();
		for (Classification classification : classifications) {
			if (classification!=null && classification.getClassifier().equals(classifier)) {
				selection.add(classification);
			}
		}
		return selection;
	}
	
	@Override
	public void setClassification(Classifier classifier, DataSetMember member) {
		List<DataSetMember> members = new ArrayList<DataSetMember>();
		if (member!=null)
		members.add(member);
		setClassification(classifier, members);
	}
	
	@Override
	public void setClassification(Classifier classifier, List<DataSetMember> members) {
		List<Classification> classifierclassificationlist = new ArrayList<Classification>();
		List<Classification> classificationlist = getClassification();
		
		for (Classification classification : classificationlist) {
			if (classification.getClassifier().equals(classifier)) {
				classifierclassificationlist.add(classification);
			}
		}
		
		if (members.size()<classifierclassificationlist.size()) {
			for (int i=0; i<classifierclassificationlist.size()-members.size(); i++) {
				Classification classification = classifierclassificationlist.get(classifierclassificationlist.size()-i-1);
				classificationlist.remove(classification);
			}
		}
		
		int i = 0;
		for (DataSetMember member : members) {
			if (i<classifierclassificationlist.size()) {
				Classification classification = classifierclassificationlist.get(i);
				classificationlist.remove(classification);
				KbeeMemberClassification kbeeclassification = new KbeeMemberClassification();
				kbeeclassification.setClassifier(classifier);
				kbeeclassification.setSourceMember(this);
				kbeeclassification.setDataSetMember(member);
				classificationlist.add(kbeeclassification);
			}
			else {
				KbeeMemberClassification classification = new KbeeMemberClassification();
				classification.setClassifier(classifier);
				classification.setSourceMember(this);
				classification.setDataSetMember(member);
				classificationlist.add(classification);
			}
			i++;
		}		
	}
	
	@Override
	public void setAttributeValues(Attribute attribute, List<String> values) {
		if (values==null || values.isEmpty()) {
			getAttributes().remove(String.valueOf(attribute.getId()));
		}
		else {
			getAttributes().put(String.valueOf(attribute.getId()), values);
		}
		setAttributes(getAttributes());
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
	public List<String> getAttributeValues(Attribute attribute) {
		return  getAttributes().getValues(String.valueOf(attribute.getId()));
	}

	@Override
	public String getAlternativeDisplayName() {
		if (alternative_display!=null)
			return alternative_display;
		return getDisplayName();
	}
	
	public void setAlternativeDisplayName(String dname) {
		this.alternative_display=dname;
	}
	
	public String getDisplayName() {
		return getStrValue();
	}
	
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append(super.toString());
		
		str.append("\nvalue: " + (getDisplayName()!=null ? getDisplayName().toString() : "-"));
		
		if (getState()!=null)
			str.append("\nstate: " + getState()+"\n");
		
		str.append("\ndataset: " + getDataSet().getName() + "  (" + getDataSet().getDataSetType().getLabel() + ")\n");
		return str.toString();
	}
	
	@Override
	public boolean equals(Object object) {
		if (!(object instanceof KbeeDataSetMember)) 
			return false;
		if (((KbeeDataSetMember)object).getId()==null && getId()!=null)
			return false;
		return ((KbeeDataSetMember)object).getId().equals(getId());
	}
	
	@SuppressWarnings("rawtypes")
	private Json getAttributes() {
		try {
			if (jattributes==null) {
				if(this.attributes == null) {
					jattributes = new KbeeJson();
				}
				else {
					JsonParserFactory factory = JsonParserFactory.getInstance();
					JSONParser parser = factory.newJsonParser();
					Map roots = parser.parseJson(this.attributes);
					List root = (List)roots.get("root");
					Map jsonData = (Map)root.get(0);
					jattributes = new KbeeJson(jsonData);
				}
			}
			
		} catch (com.codesnippets4all.json.exceptions.JSONParsingException e) {
			logger.error(e);
			jattributes = new KbeeJson();
		}
		
		return jattributes;
		
	}


	@Override
	public Map<String, List<String>> getAttributesAsMap() {
		Map<String, List<String>> a_map = new HashMap<String, List<String>>();
		for (AttributeTemplate tem:getDataSet().getAttributes()) {
			a_map.put( tem.getAttribute().getName(), this.getAttributeValues(tem.getAttribute()));
		}
		return a_map;
	}

	
	
	public void setSystem(boolean b) {
		this.issystem=b;
	}
	
	@Override
	public boolean isSystem() {
		return this.issystem;
	}
	
	
	@Override
	public String getDescription() {
		return notes;
	}
	
	public AuditSet getAuditSet() {
		return AuditSet.DATASET_VALUE;
	}

	
	public Map<String, List<String>> getClassificationAsMapString() {
		if (this.getDomain()!=null && this.getDomain().getLocale()!=null && this.getDomain().getTimeZone()!=null)
			return getClassificationAsMapString(this.getDomain().getLocale(), getDomain().getTimeZone());
		return getClassificationAsMapString(Locale.getDefault(), ZoneId.systemDefault().getId());
	}
	
	
	@Transient
	Map<String, List<String>> classificationmap;
	
	@Transient
	private Map<String, List<String>> a_map = null;

	
	public Map<String, List<String>> getClassificationAsMapString(Locale locale, String time_zone) {
		
		if (classificationmap!=null)
			return classificationmap;
			
		classificationmap = new HashMap<String, List<String>>();

		classificationmap = new HashMap<String, List<String>>();
		
		for (Classification classification : getClassification()) {
			if (classification!=null && classification.getClassifier()!=null) {
				String classifierlabel = classification.getClassifier().getName();
				List<String> values = classificationmap.get(classifierlabel);
				if (values == null) {
					values = new ArrayList<String>();
					classificationmap.put(classifierlabel, values);
				}
				values.add(classification.getStrValue());
			}
		}
		
		for (Classifier classifier : getDataSet().getClassifiers()) {
			if (classifier.getState()==ObjectState.ENABLED) {
					String classifierlabel = classifier.getName();
					if(!classificationmap.containsKey(classifierlabel)){
						classificationmap.put(classifierlabel, new ArrayList<>());
					}
			}
		}
		for (Map.Entry<String, List<String>> e: getAttributesAsMap(locale, time_zone).entrySet()) {
			classificationmap.put(e.getKey(), e.getValue());
		}
		return classificationmap;
	}
	
	
	public Map<String, List<String>> getAttributesAsMap(Locale locale, String time_zone) {
		
		if (a_map!=null)
			return a_map;
		
		a_map = new HashMap<String, List<String>>();
		
		for (AttributeTemplate tem:getDataSet().getAttributes()) {
		if ( tem.getAttribute().getState() == ObjectState.ENABLED) {
					if (tem.getAttribute().getType()==AttributeType.DATE || tem.getAttribute().getType()==AttributeType.TIMESTAMP) {
						List<String> li=this.getAttributeValues(tem.getAttribute());
						List<String> rli=new ArrayList<String>();
						for (String s:li) {
							try {
							OffsetDateTime xdate;
							xdate = ServiceLocator.getService(DateTimeService.class).parseStrDate(s);
							ZoneId zid = ZoneId.of(time_zone);
							if (tem.getAttribute().getType()==AttributeType.DATE)
								rli.add(ServiceLocator.getService(DateTimeService.class).format(xdate, zid.getId(), locale, DateTimeService.Dow_Month_Day_year_z));
							else
								rli.add(ServiceLocator.getService(DateTimeService.class).format(xdate, zid.getId(), locale, DateTimeService.Dow_Month_Day_Year_hh_mm_z));
								
							} catch (Exception e) {
								logger.error(e);
								rli.add(s + " err -> " + e.getClass().getSimpleName());
							}
						}
						a_map.put( tem.getAttribute().getName(), rli); 
						
					}
					else
						a_map.put( tem.getAttribute().getName(), this.getAttributeValues(tem.getAttribute()));
				
				}
		}
		
		return a_map;
	}
	
	
	private void setAttributes(Json json) {
		if (json == null || ((KbeeJson)json).getData().isEmpty()) {
			attributes = null;
		} 
		else {
			JsonGeneratorFactory factory = JsonGeneratorFactory.getInstance();
			JSONGenerator generator = factory.newJsonGenerator();
			attributes = generator.generateJson(((KbeeJson)json).getData());
		}
	}
}
