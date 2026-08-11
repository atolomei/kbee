package com.novamens.kbee.content.form;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.hibernate.SessionFactory;
import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.form.EFieldAwareModel;
import com.novamens.content.form.EForm;
import com.novamens.content.form.EFormAwareModel;
import com.novamens.content.form.EFormData;
import com.novamens.content.form.EFormAttributeModel;
import com.novamens.content.form.EFormDataSource;
import com.novamens.content.form.EFormField;
import com.novamens.content.model.Classificable;
import com.novamens.event.Event;
import com.novamens.security.Identifiable;
import com.novamens.service.ServiceLocator;

public abstract class KbeeEFormAttributeModel<T> implements EFormAttributeModel<T>, EFormAwareModel, EFieldAwareModel {
	private static final long serialVersionUID = 1L;
	
	private String formId;
	private EFormField<?> field;
	
	@JsonIgnore
	public EForm getForm() {
		if (formId==null) return null;
		SessionFactory sf = (SessionFactory)ServiceLocator.getService(BeansService.class).getBean("sessionFactory");
		EForm form = (EForm)sf.getCurrentSession().load(KbeeEForm.class, Long.valueOf(this.formId));
		return form;
	}
	
	public void setForm(EForm form) {
		this.formId = form!=null && form instanceof Identifiable ? String.valueOf(((Identifiable)form).getId()) : null;;
	}
	
	@JsonIgnore
	public EFormField<?> getField() {
		return field;
	}
	
	@Override
	public void setField(EFormField<?> field) {
		this.field = field;
	}
	
	@Override
	public boolean handle(Event event) {
		return false;
	}
	
	@Override
	public List<T> onEvent(Event event) {
		return null;
	}
	
	// get data from object
	@Override
	public T get(Object object) {
		Assert.isInstanceOf(Content.class, object);
		
		if (getForm()==null) return null;
		
		EFormData formdata = ((Content)object).getFormData(getForm());
		
		Object data =  formdata.getData(getField());
		
		T value = data!=null ? 
			getValueOf(data.toString()) : 
			null;
				
		return value;
	}
	
	
	public List<T> getValues(Object object) {
		Assert.isInstanceOf(Classificable.class, object);
		List<T> values = new ArrayList<T>();
		return values;
	}
	
	@Override
	public void set(Object object, Object data) {
		Assert.isInstanceOf(Content.class, object);
		EFormData formdata = ((Content)object).getFormData(getForm());
		formdata.setData(getField(), toString(data));
	}
	
	@Override
	public void set(Object object, List<T> data) {
	}
	
	@Override
	@JsonIgnore
	public String getMetainfoMessage() {
		return null;
	}
	
	@Override
	public String serialize(Classificable formobject, T value) {
		return value!=null ? value.toString() : null;
	}
	
	@Override
	public T deserialize(Classificable formobject, String token) {
		return token!=null ? getValueOf(token) : null;
	}
	
	public EFormDataSource<T> getDataSource(Classificable object) {
		return null;
	}
	
	protected abstract T getValueOf(String value);
	
	protected abstract String toString(Object value);
	
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
	public String getTypeLabel() {
		return EFormAttributeModel.GetTypeLabel();
	}
	
	@JsonIgnore
	public String getModelObjectName(Locale locale) {
		ResourceBundle res = ResourceBundle.getBundle( getClass().getName(), locale);
		return res.getString("form-data");
	}
}