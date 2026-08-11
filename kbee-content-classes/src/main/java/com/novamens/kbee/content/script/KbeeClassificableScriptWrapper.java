package com.novamens.kbee.content.script;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.base.Relation;
import com.novamens.content.base.Resource;
import com.novamens.content.base.ResourceContainer;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.form.EForm;
import com.novamens.content.form.EFormData;
import com.novamens.content.model.AttributeTemplate;
import com.novamens.content.model.Classificable;
import com.novamens.content.model.Classification;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ClassifierTemplate;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.ExtractionRule;
import com.novamens.content.model.ModelElementTemplate;
import com.novamens.content.model.Multiplicity;
import com.novamens.content.model.RelationTemplate;
import com.novamens.content.properties.PropertyService;
import com.novamens.content.service.DataAccessService;
import com.novamens.content.user.UserProfile;
import com.novamens.datetime.DateTimeService;
import com.novamens.dom.Domain;
import com.novamens.dom.DomainObject;
import com.novamens.kbee.content.model.KbeeClassifierTemplate;
import com.novamens.kbee.content.model.KbeeRelation;
import com.novamens.kbee.content.workflow.KbeeTaskForm;
import com.novamens.security.User;
import com.novamens.service.ServiceLocator;

public class KbeeClassificableScriptWrapper {

	private Classificable classificable;
	
	public KbeeClassificableScriptWrapper(Classificable value) {
		this.classificable = value;
	}
	
	public String getDisplayName() {
		return classificable.getDisplayName();
	}
	
	public String getTitle() {
		if (classificable instanceof Content) {
			return ((Content)classificable).getTitle();
		}
		else {
			return getDisplayName();
		}
	}
	
	public Object getValue(String attribute) {
		List<?> values = getValues(attribute);
		return values!=null && !values.isEmpty() ? 
			(values.get(0) instanceof Classificable ? new KbeeClassificableScriptWrapper((Classificable)values.get(0)) : values.get(0))  :
			null;
	}
	
	public void setValue(String attribute, Object value) {
		ClassifierTemplate classifiertemplate = getClassifierTemplate(attribute);
		if ( classifiertemplate!=null) {
			if (value instanceof User) {
				value = getMember(classifiertemplate.getClassifier().getDataSet(), (User)value);
			}
			if (value instanceof String) {
				value = findMemberByValue(classifiertemplate.getClassifier().getDataSet(), (String)value);
			}
			if (value instanceof DataSetMember) {
				DataSetMember member = (DataSetMember)value;
				if (member.getDataSet().equals(classifiertemplate.getClassifier().getDataSet())) {
					classificable.setClassification(classifiertemplate.getClassifier(), member);
				}
			}
		}
		else {
			AttributeTemplate attributetemplate = getAttributeTemplate(attribute);
			if (attributetemplate!=null && value!=null) {
				List<String> values = new ArrayList<String>();
				values.add(value.toString());
				classificable.setAttributeValues(attributetemplate.getAttribute(), values);
			}
		}
		if (modificableTitle()) {
			updateTitle();
		}
	}
	
	
	public EFormData getFormData(String name) {
		if (classificable instanceof Content) {
			EForm eform = null;
			Content content = (Content)classificable;
			for (EForm templateform : content.getContentTemplate().getForms()) {
				if (templateform.getName().equals(name)) {
					eform = templateform;
					break;
				}
			}
			if (eform!=null) {
				EFormData data = content.getFormData(new KbeeTaskForm(eform));
				return data;
			}
		}
		return null;
	}
	
	public String getDate(String attribute) {
		List<?> values = getValues(attribute);
		String datevalue = values!=null && !values.isEmpty() ? (String)values.get(0) : null;
		if (datevalue==null) return null;
		OffsetDateTime date = ServiceLocator.getService(DateTimeService.class).parseStrDate(datevalue);
		String value = ServiceLocator.getService(DateTimeService.class).getDateDisplayString(date);
		return value;
	}
	
	public OffsetDateTime getDateTime(String attribute) {
		List<?> values = getValues(attribute);
		String datevalue = values!=null && !values.isEmpty() ? (String)values.get(0) : null;
		if (datevalue==null) return null;
		OffsetDateTime date = ServiceLocator.getService(DateTimeService.class).parseStrDate(datevalue);
		return date;
	}
	
	public Object getObject() {
		return classificable;
	}
	
	public String getLabel(String attribute) {
		Object value = getValue(attribute);
		return value instanceof KbeeClassificableScriptWrapper ? ((KbeeClassificableScriptWrapper)value).getDisplayName() : (String)value;
	}
	
	public List<?> getValues(String attribute) {
		ClassifierTemplate classifiertemplate = getClassifierTemplate(attribute);
		if ( classifiertemplate!=null) {
			List<DataSetMember> values;
			if ( classifiertemplate.isReverse()) {
				values =  classifiertemplate.getService(DataAccessService.class).getAll(classificable);
			}
			else {
				values = new ArrayList<DataSetMember>();
				for (Classification classification : classificable.getClassification()) {
					if ( classifiertemplate.getClassifier().equals(classification.getClassifier())) {
						values.add(classification.getDataSetMember());
					}
				}
			}
			return values;
		}
		else {
			AttributeTemplate attributetemplate = getAttributeTemplate(attribute);
			if (attributetemplate!=null) {
				List<String> values = classificable.getAttributeValues(attributetemplate.getAttribute());
				return values;
			}
			else {
				return null;
			}
		}	
	}
	
	public void setValues(String attribute, List<DataSetMember> values) {
		ClassifierTemplate template = getClassifierTemplate(attribute);
		if (template!=null) {
			classificable.setClassification(template.getClassifier(), values);
		}
	}
	
	public void addRelation(String relationname, Content target) {
		if (!(classificable instanceof Content)) return;
		Content content = (Content)classificable;
		for (RelationTemplate template : content.getContentTemplate().getRelations()) {
			if (template.getName().toLowerCase().equals(relationname.toLowerCase())) {
				if (template.getTargetTemplates().contains(target.getContentTemplate())) {
					KbeeRelation relation = new KbeeRelation();
					relation.setTemplate(template);
					relation.setSource(content);
					relation.setTarget(target);
					content.addRelation(relation);
				}
			}
		}
	}
	
	public List<Content> getRelated(String relationname) {
		if (!(classificable instanceof Content)) return null;
		List<Content> related = new ArrayList<>();
		Content content = (Content)classificable;
		for (RelationTemplate template : content.getContentTemplate().getRelations()) {
			if (template.getName().toLowerCase().equals(relationname.toLowerCase())) {
				for (Relation relation : content.getRelations(template)) {
					related.add(relation.getTarget());
				}
			}
		}
		return related;
	}

	
	public List<Resource> getResources() {
		return classificable instanceof ResourceContainer ? ((ResourceContainer)classificable).getResources() : new ArrayList<Resource>();
	}
	
	public List<Resource> getResources(String tag) {
		return classificable instanceof ResourceContainer ? ((ResourceContainer)classificable).getResources(tag) : new ArrayList<Resource>();
	}
	
	
	public List<Relation> getRelations(String tag) {
		List<Relation> relations = new ArrayList<>();
		if (classificable instanceof Content) {
			for (Relation relation : ((Content)classificable).getRelations()) {
				if (relation.getTemplate().getName().equals(tag)) {
					relations.add(relation);
				}
			}
		}
		return relations;
	}
	                      
	public List<Relation> getReverse(String tag) {
		List<Relation> relations = new ArrayList<>();
		if (classificable instanceof Content) {
			for (Relation relation : ((Content)classificable).getReverseRelations()) {
				if (relation.getTemplate().getName().equals(tag)) {
					relations.add(relation);
				}
			}
		}
		return relations;
	}
	
	public Domain getDomain() {
		return classificable instanceof DomainObject ? ((DomainObject)classificable).getDomain() : null;
	}
	
	private ClassifierTemplate getClassifierTemplate(String attribute) {
		if (classificable instanceof DataSetMember) {
			for (ModelElementTemplate template : ((DataSetMember)classificable).getDataSet().getStructure()) {
				if (template!=null && 
						template instanceof ClassifierTemplate && 
						((ClassifierTemplate)template).getClassifier()!=null && 
						attribute.equals(((ClassifierTemplate)template).getClassifier().getAlias())) {
					return ((ClassifierTemplate)template);
				}
			}
			Classifier classifier = null, parent = null;
			for (Classifier c : getContentDao().getClassifiers(((DataSetMember)classificable).getDomain())) {
				if (c.getAlias()!=null && attribute.toLowerCase().equals(c.getAlias().toLowerCase())) {
					classifier = c;
				}
				if (c.getDataSet().equals(((DataSetMember)classificable).getDataSet())) {
					parent = c;
				}
			}
			if (classifier!=null && parent!=null) {
				KbeeClassifierTemplate template = new KbeeClassifierTemplate();
				template.setClassifier(classifier);
				template.setParent(parent);
				template.setMultiplicity(Multiplicity.M0N);
				template.setReverse(true);
				return template;
			}
		}
		if (classificable instanceof Content) {
			for (ClassifierTemplate template : ((Content)classificable).getContentTemplate().getClassifiers()) {
				if (template!=null && attribute.toLowerCase().equals(template.getClassifier().getAlias().toLowerCase())) {
					return ((ClassifierTemplate)template);
				}
			}
		}
		return null;
	}
	
	private AttributeTemplate getAttributeTemplate(String attribute) {
		if (classificable instanceof DataSetMember) {
			for (ModelElementTemplate template : ((DataSetMember)classificable).getDataSet().getStructure()) {
				if (template!=null && 
						template instanceof AttributeTemplate && 
						((AttributeTemplate)template).getAttribute()!=null && 
						attribute.equals(((AttributeTemplate)template).getAttribute().getAlias())) {
					return ((AttributeTemplate)template);
				}
			}
		}
		if (classificable instanceof Content) {
			for (AttributeTemplate template : ((Content)classificable).getContentTemplate().getAttributes()) {
				if (template!=null && attribute.toLowerCase().equals(template.getAttribute().getAlias().toLowerCase())) {
					return ((AttributeTemplate)template);
				}
			}
		}
		return null;
	}
	
	private DataSetMember getMember(DataSet dataSet, User user) {
		UserProfile profile = getContentDao().findUserProfileByUser(user);
		if (profile!=null) {
			List<DataSetMember> members = getContentDao().findMembersByEntity(profile.getPerson());
			for (DataSetMember member : members) {
				if (member.getDataSet().equals(dataSet)) {
					return member;
				}
			}
		}
		return null;
	}
	
	private DataSetMember findMemberByValue(DataSet dataSet, String value) {
		DataSetMember member = getContentDao().findMemberByValue(dataSet, value);
		return member;
	}

	
	private void updateTitle() {
		if (!(classificable instanceof Content)) 
			return;
		Content content = (Content)classificable;
		ExtractionRule rule = content.getContentTemplate().getTitleRule();
		String title = (String)rule.extract(content);
		content.setTitle(title);
	}
	
	private boolean modificableTitle() {
		if (!(classificable instanceof Content) || classificable.getId()==null) 
			return false;
		Content content = (Content)classificable;
		return !"true".equals(content.getService(PropertyService.class).getProperty("title"));
	}
		
	private ContentDao getContentDao() {
		return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
}
