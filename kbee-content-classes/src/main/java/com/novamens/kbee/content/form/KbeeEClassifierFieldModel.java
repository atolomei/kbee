package com.novamens.kbee.content.form;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.hibernate.ObjectNotFoundException;
import org.hibernate.SessionFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.form.EClassifierModel;
import com.novamens.content.form.EFormDataSource;
import com.novamens.content.model.AccessStrategy;
import com.novamens.content.model.Classificable;
import com.novamens.content.model.Classification;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ClassifierTemplate;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.ModelElement;
import com.novamens.content.model.ModelElementTemplate;
import com.novamens.kbee.content.model.KbeeClassifier;
import com.novamens.kbee.content.model.KbeeClassifierTemplate;
import com.novamens.service.ServiceLocator;

@JsonTypeName("classifier")
public class  KbeeEClassifierFieldModel extends KbeeEModelElementFieldModel<DataSetMember> implements EClassifierModel<DataSetMember> {
	private static final long serialVersionUID = 1L;
	
	private String classifierId;
	@JsonProperty("classifier")
	private String classifierName;
	private boolean reverse;
	private String iql;
	private AccessStrategy accessStrategy;
	
	@Override
	public void set(Object object, List<DataSetMember> data) {
		((Classificable)object).setClassification(getClassifier(), data);
	}
	
	@Override
	public void set(Object object, Object data) {
		((Classificable)object).setClassification(getClassifier(), (DataSetMember)data);
	}
	
	public DataSetMember get(Object object) {
		List<DataSetMember> values = getValues(object);
		DataSetMember value = !values.isEmpty() ? values.get(0) : null;
		return value; 
	}
	
	public List<DataSetMember> getValues(Object object) {
		List<DataSetMember> values = new ArrayList<DataSetMember>();
		for (Classification classification :((Classificable)object).getClassification(getClassifier())) {
			if (classification.getDataSetMember()!=null) {
				values.add(classification.getDataSetMember());
			}
		}
		return values;
	}
	
	@Override
	@JsonIgnore
	public ModelElement getElement() {
		return getClassifier();
	}
	
	public void setClassifier(Classifier classifier) {
		this.classifierId = classifier!=null ? String.valueOf(classifier.getId()) : null;
	}
	
	public void setClassifier(String classifierId) {
		this.classifierId = classifierId;
	}
	
	public String getClassifierId() {
		return this.classifierId;
	}
	
	public void setClassifierName(String name) {
		this.classifierName = name;
	}
	
	public String getClassifierName() {
		return this.classifierName;
	}
	
	@JsonIgnore
	public Classifier getClassifier() {
		if (classifierId!=null) {
			SessionFactory sf = (SessionFactory)ServiceLocator.getService(BeansService.class).getBean("sessionFactory");
			Classifier classifier = null;
			try {
				classifier = (Classifier)sf.getCurrentSession().load(KbeeClassifier.class, Long.valueOf(this.classifierId));
			}
			catch (ObjectNotFoundException e) {
				
			}
			return classifier;
		}
		else {
			if (classifierName!=null) {
				for (Classifier classifier : getContentDao().getClassifiers(getContentDao().getDomain())) {
					if (classifierName.equals(classifier.getAlias())) {
						classifierId = String.valueOf(classifier.getId());
						return classifier;
					}
				}
			}
		}
		return null;
	}
	
	public void setReverse(boolean reverse) {
		this.reverse = reverse;
	}
	
	public boolean getReverse() {
		return this.reverse;
	}
	
	@JsonProperty("access")
	public AccessStrategy getAccessStrategy() {
		return accessStrategy;
	}
	
	@JsonProperty("access")
	public void setAccessStrategy(AccessStrategy strategy) {
		this.accessStrategy = strategy;
	}
	
	public String getIql() {
		return iql;
	}

	public void setIql(String iql) {
		this.iql = iql;
	}

	@Override
	public EFormDataSource<DataSetMember> getDataSource(Classificable object) {
		return new KbeeEClassifierSource(object) {
			@Override
			public ClassifierTemplate getRelation() {
				return getRelationTemplate(object);	
			}
		};
	}
	
	public ClassifierTemplate getRelationTemplate(Classificable object) {
		KbeeClassifierTemplate relation = new KbeeClassifierTemplate(getClassifier());
		
		ClassifierTemplate template = null; 
		
		if (object instanceof Content) {
			template = getClassifierTemplate((Content)object, getClassifier()); 
		}
		else {
			if (object instanceof DataSetMember) {
				template = getClassifierTemplate((DataSetMember)object, getClassifier());
			}	
		}
		
		relation.setParent(getParentClassifier());
		relation.setReverse(template==null? false : template.isReverse());
		relation.setValuesCriteria(getIql());
		
		AccessStrategy accessibility = getAccessStrategy()==null && template!=null ? template.getAccessibility() : getAccessStrategy();
		relation.setAccessibility(accessibility);
		
		return relation;
	}
	
	@Override
	public String serialize(Classificable formobject, DataSetMember value) {
		String classname = value.getClass().getName();
		int i = classname.indexOf("_");
		if (i>0) classname = classname.substring(0, i);
		i = classname.indexOf("$");
		if (i>0) classname = classname.substring(0, i);
		String serialized = classname+"-"+value.getId();
		DataSetMember parent = getParentValue(formobject);
		if (parent!=null) {
			serialized += "-" + String.valueOf(parent.getId());
		}
		return serialized;
	}
	
	@Override
	public DataSetMember deserialize(Classificable formobject, String token) {
		DataSetMember value = null;
		int i0 = token.indexOf("-");
		if (i0<=0) return null;
		//String classname = token.substring(0, i0);
		int i1 = token.indexOf("-", i0+1);
		String id;
		if (i1>0) {
			id = token.substring(i0+1, i1);
			try {
				value = getContentDao().findMemberById(Long.valueOf(id));
			}
			catch (Exception e) {
				value = null;
			}
			if (value!=null && formobject!=null) {
				String parentId = token.substring(i1+1);
				DataSetMember parentValue = getParentValue(formobject);
				if (parentValue==null || !String.valueOf(parentValue.getId()).equals(parentId)) {
					return null;
				}
			}
		}
		else {
			id = token.substring(i0+1);
			value = getContentDao().findMemberById(Long.valueOf(id));
			if (formobject==null || getParentValue(formobject)!=null) {
				return null;
			}
		}
		return value;
	}
	
	@Override
	public String getErrorMessage(Object object) {
		if (getClassifier()==null) {
			String message =  getModelObjectName() + " ";
			message += classifierName!=null ? classifierName : classifierId;
			message += " not found";
			return message;
		}
		if (object!=null && (object instanceof Content)) {
			boolean found = false;
			ContentTemplate template = ((Content)object).getContentTemplate();
			for (ClassifierTemplate classifiertemplate : template.getClassifiers()) {
				if (classifiertemplate.getClassifier()!=null && classifiertemplate.getClassifier().equals(getClassifier())) {
					found = true;
					break;
				}
			}
			if (!found) {
				String message =  getModelObjectName() + " ";
				message += classifierName!=null ? classifierName : classifierId;
				message += " not found in "+ template.getDisplayName() + " template";
				return message;
			}
		}
		return super.getErrorMessage(object);
	}
	
	@Override
	@JsonIgnore
	public String getDescription(Locale locale) {
		String description = ""; 
		if (getParentClassifier()!=null) {
			description += getParentClassifier().getDisplayName() + "->";
		}
		description += getClassifier()!=null ? ( getClassifier().getDisplayName()): " not found";
		
		description += " ( " +  getModelObjectName(locale) + " )";
		
		return description;
	}
	

	
	@JsonIgnore
	public String getModelObjectName(Locale locale) {
		ResourceBundle res = ResourceBundle.getBundle( KbeeEClassifierFieldModel.class.getName(), locale);
		return res.getString("classifier");
	}

	
	
	@Override
	@JsonIgnore
	public String getTypeLabel() {
		return EClassifierModel.GetTypeLabel();
	}
	
	protected ClassifierTemplate getClassifierTemplate(Content content, Classifier classifier) {
		for (ClassifierTemplate template : content.getContentTemplate().getClassifiers()) {
			if (template!=null && template.getClassifier()!=null && template.getClassifier().equals(classifier)) {
				return template;
			}
		}
		return null;
	}
	
	protected ClassifierTemplate getClassifierTemplate(DataSetMember member, Classifier classifier) {
		for (ModelElementTemplate template : member.getDataSet().getStructure()) {
			if (template!=null && template instanceof ClassifierTemplate) {
				if (((ClassifierTemplate)template).getClassifier()!=null && ((ClassifierTemplate)template).getClassifier().equals(classifier)) {
					return (ClassifierTemplate)template;
				}
			}
		}
		return null;
	}
	
	protected ContentDao getContentDao() {
		return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	@Override
	protected List<DataSetMember> getValuesFrom(DataSetMember parentValue) {
		List<DataSetMember> values = new ArrayList<DataSetMember>();
		for (Classification classification : parentValue.getClassification(getClassifier())) {
			if (classification!=null) values.add(classification.getDataSetMember());
		};
		return values;
	}
}