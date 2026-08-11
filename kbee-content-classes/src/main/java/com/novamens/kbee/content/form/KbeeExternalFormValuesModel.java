package com.novamens.kbee.content.form;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.hibernate.SessionFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.novamens.beans.BeansService;
import com.novamens.content.form.EFieldAwareModel;
import com.novamens.content.form.EFieldModel;
import com.novamens.content.form.EForm;
import com.novamens.content.form.EFormAttributeModel;
import com.novamens.content.form.EFormAwareModel;
import com.novamens.content.form.EFormData;
import com.novamens.content.form.EFormDataSource;
import com.novamens.content.form.EFormField;
import com.novamens.content.model.AccessStrategy;
import com.novamens.content.model.Classificable;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ClassifierTemplate;
import com.novamens.content.model.DataSetMember;
import com.novamens.event.Event;
import com.novamens.kbee.content.model.KbeeClassifier;
import com.novamens.kbee.content.model.KbeeClassifierTemplate;
import com.novamens.service.ServiceLocator;

public class KbeeExternalFormValuesModel implements EFieldModel<DataSetMember>, EFormAwareModel, EFieldAwareModel {
	private static final long serialVersionUID = 1L;
	
	private String classifierId;
	private EForm form;
	private EFormField<?> field;
	
	@Override
	public void set(Object object, List<DataSetMember> data) {
//		((Classificable)object).setClassification(getClassifier(), data);
	}
	
	@Override
	public void set(Object object, Object data) {
//		((Classificable)object).setClassification(getClassifier(), (DataSetMember)data);
	}
	
	public DataSetMember get(Object object) {
		return null;
	}
	
	public List<DataSetMember> getValues(Object object) {
//		List<DataSetMember> values = new ArrayList<DataSetMember>();
//		for (Classification classification :((Classificable)object).getClassification(getClassifier())) {
//			if (classification.getDataSetMember()!=null) {
//				values.add(classification.getDataSetMember());
//			}
//		}
//		return values;
		return null;
	}
	
	public EFormData getData() {
		return null;
	}

	public EForm getForm() {
		return form;
	}

	public void setForm(EForm form) {
		this.form = form;
	}

	public EFormField<?> getField() {
		return field;
	}

	public void setField(EFormField<?> field) {
		this.field = field;
	}
	
	@Override
	@JsonIgnore
	public boolean isReadOnly() {
		return false;
	}
	
	@Override
	public String getErrorMessage(Object object) {
		return null;
	}
	
	@Override
	@JsonIgnore
	public String getMetainfoMessage() {
		return null;
	}
	
	@Override
	public String serialize(Classificable formobject, DataSetMember value) {
		return null;
	}
	
	@Override
	public DataSetMember deserialize(Classificable formobject, String token) {
		return null;
	}
	
	@Override
	public boolean handle(Event event) {
		return false;
	}
	
	@Override
	public List<DataSetMember> onEvent(Event event) {
		return null;
	}
	
	@Override
	@JsonIgnore
	public String getTypeLabel() {
		return EFormAttributeModel.GetTypeLabel();
	}
	
	@JsonIgnore
	public String getModelObjectName(Locale locale) {
		ResourceBundle res = ResourceBundle.getBundle( getClass().getName(), locale);
		return res.getString("form-data");
	}
	
	@Override
	@JsonIgnore 
	public String getDescription(Locale locale) {
		return "String";
	}
	
	public void setClassifier(Classifier classifier) {
		this.classifierId = classifier!=null ? String.valueOf(classifier.getId()) : null;
	}
	
	@JsonIgnore
	public Classifier getClassifier() {
		SessionFactory sf = (SessionFactory)ServiceLocator.getService(BeansService.class).getBean("sessionFactory");
		Classifier classifier = (Classifier)sf.getCurrentSession().load(KbeeClassifier.class, Long.valueOf(this.classifierId));
		return classifier;
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
		relation.setAccessibility(AccessStrategy.All);
		return relation;
	}
}
