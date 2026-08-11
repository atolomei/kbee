package com.novamens.kbee.content.form;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.hibernate.SessionFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.form.EClassifierModel;
import com.novamens.content.form.EFieldModel;
import com.novamens.content.form.EFormDataSource;
import com.novamens.content.form.EFormEvent;
import com.novamens.content.model.Classificable;
import com.novamens.content.model.Classification;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ClassifierTemplate;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.ModelElement;
import com.novamens.event.Event;
import com.novamens.kbee.content.model.KbeeClassifier;
import com.novamens.service.ServiceLocator;

public abstract class KbeeEModelElementFieldModel<T> implements EFieldModel<T> {
					
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeEModelElementFieldModel.class.getName());
	
	
	private String parentClassifierId;
	@JsonProperty("parent")
	private String parentClassifierName;
	
	@Override
	public boolean handle(Event event) {
		if (!(event instanceof EFormEvent)) 
			return false;
		if (((EFormEvent)event).getField()==null)
			return false;
		EFieldModel<?> eventModel = ((EFormEvent)event).getField().getModel();
		if (!(eventModel instanceof EClassifierModel<?>))
			return false;
		Classifier eventClassifier = ((EClassifierModel<?>)eventModel).getClassifier();
		if (eventClassifier==null)
			return false;
		return eventClassifier.equals(getParentClassifier());
	}
	
	// A parent value is an entity in the model that define the field values through relations
	// This model is a relation or attribute of the parent value.
	public List<DataSetMember> getParentValues(Event event) {
		// handle(event) must be true
		List<DataSetMember> parentValues = new ArrayList<DataSetMember>();
		Object data = ((EFormEvent)event).getFormData().getData(((EFormEvent)event).getField());;
		if (data instanceof DataSetMember) {
			parentValues.add((DataSetMember)data);
		} 
		else
		if (data instanceof List<?>) {
			for (Object value : ((List<?>)data)) {
				if (value instanceof DataSetMember) {
					parentValues.add((DataSetMember)value);
				}
			}
		}
		return parentValues;
	}
	
	@Override
	public List<T> onEvent(Event event) {
		List<T> values = new ArrayList<T>();
		for (DataSetMember parentValue : getParentValues(event)) {
			values.addAll(getValuesFrom(parentValue));
		}
		return values;
	}
	
	public abstract ModelElement getElement();
	
	public void setParentClassifier(Classifier classifier) {
		this.parentClassifierId = classifier!=null ? String.valueOf(classifier.getId()) : null;
	}
	
	public void setParentClassifier(String parentClassifierId) {
		this.parentClassifierId = parentClassifierId;
	}
	
	public String getParentClassifierId() {
		return this.parentClassifierId;
	}
	
	public void setParentClassifierName(String name) {
		this.parentClassifierName = name;
	}
	
	public String getParentClassifierName() {
		return this.parentClassifierName;
	}
	
	public EFormDataSource<T> getDataSource(Classificable object) {
		return null;
	}
	
	@JsonIgnore
	public Classifier getParentClassifier() {
		if (parentClassifierId!=null) {
			SessionFactory sf = (SessionFactory)ServiceLocator.getService(BeansService.class).getBean("sessionFactory");
			Classifier classifier = (Classifier)sf.getCurrentSession().load(KbeeClassifier.class, Long.valueOf(this.parentClassifierId));
			return classifier;
		}
		else {
			if (parentClassifierName!=null) {
				for (Classifier classifier : getContentDao().getClassifiers(getContentDao().getDomain())) {
					if (parentClassifierName.equals(classifier.getAlias())) {
						parentClassifierId = String.valueOf(classifier.getId());
						return classifier;
					}
				}
			}
		}
		return null;
	}
	
	@Override
	@JsonIgnore
	public String getMetainfoMessage() {
		if (getParentClassifierId()!=null) {
			return getLabel("derived", getParentClassifier().getDisplayName());
		}
		else {
			return null;
		}
	}
	
	@Override
	public String serialize(Classificable formobject, T value) {
		return value!=null ? value.toString() : null;
	}
	
	
	
	 
	
	@Override
	@JsonIgnore
	public String getModelObjectName(Locale locale) {
		return this.getClass().getName();
		//ResourceBundle res = ResourceBundle.getBundle( KbeeEAttributeFieldModel.class.getName(), Locale.getDefault());
		//return res.getString("attribute");
	}
	
	
	@Override
	@JsonIgnore
	public boolean isReadOnly() {
		return false;
	}
	
	@Override
	public String getErrorMessage(Object object) {
		if ((parentClassifierId!=null || parentClassifierName!=null) && getParentClassifier()==null) {
			String message = "Classifier ";
			message += parentClassifierName!=null ? parentClassifierName : parentClassifierId;
			message += " not found";
			return message;
		}
		if (object!=null && !(object instanceof Classificable)) {
			String message = "Only for classificable objects"; 
			return message;
		}
		if (object!=null && (object instanceof Content) && getParentClassifier()!=null) {
			boolean found = false;
			ContentTemplate template = ((Content)object).getContentTemplate();
			for (ClassifierTemplate classifiertemplate : template.getClassifiers()) {
				if (classifiertemplate.getClassifier()!=null && classifiertemplate.getClassifier().equals(getParentClassifier())) {
					found = true;
					break;
				}
			}
			if (!found) {
				String message = "Classifier ";
				message += parentClassifierName!=null ? parentClassifierName : parentClassifierId;
				message += " not found in "+ template.getDisplayName() + " template";
				return message;
			}
		}
		return null;
	}
	
	protected abstract List<T> getValuesFrom(DataSetMember parentValue);
	
	protected DataSetMember getParentValue(Classificable formobject) {
		Classifier parentClassifier = getParentClassifier();
		if (parentClassifier!=null) {
			for (Classification  classification : formobject.getClassification()) {
				if (classification!=null && 
						classification.getClassifier()!=null && 
						classification.getClassifier().equals(parentClassifier) &&
						classification.getDataSetMember()!=null) {
					return classification.getDataSetMember();
				}
			}
		}	
		return null;
	}
	
	protected String getLabel(String key, String... parameter) {
		try {
			ResourceBundle resources = ResourceBundle.getBundle(KbeeEModelElementFieldModel.class.getName(), Locale.getDefault());
			String text = "";
			if (resources!=null) {
				text = resources.getString(key);
			}
			if (text!=null) {
				for (int p = 0; p<parameter.length; p++) {
					text = text.replace("{"+String.valueOf(p)+"}", parameter[p]);
				}
			}
			return text;
		}
		catch (MissingResourceException e) {
			logger.error(e);
		}
		catch (Exception e) {
			logger.error(e);
		}
		return null;
	}
	
	protected ContentDao getContentDao() {
		 BeansService beans = ServiceLocator.getService(BeansService.class);
		 return  (ContentDao) beans.getBean("contentDao");
	}
}