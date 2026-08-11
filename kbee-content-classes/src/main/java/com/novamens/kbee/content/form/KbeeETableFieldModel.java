package com.novamens.kbee.content.form;

import java.util.List;
import java.util.Locale;

import org.hibernate.SessionFactory;
import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.form.EFieldAwareModel;
import com.novamens.content.form.EForm;
import com.novamens.content.form.EFormAwareModel;
import com.novamens.content.form.EFormData;
import com.novamens.content.form.EFormField;
import com.novamens.content.form.EResourceModel;
import com.novamens.content.form.ETableModel;
import com.novamens.security.Identifiable;
import com.novamens.service.ServiceLocator;

@JsonTypeName("table")
public class KbeeETableFieldModel extends KbeeEAbstractFieldModel<List<?>> implements ETableModel, EFormAwareModel, EFieldAwareModel {
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
	
	public List<?> get(Object object) {
		Assert.isInstanceOf(Content.class, object);
		
		EFormData formdata = ((Content)object).getFormData(getForm());
		
		Object data =  formdata.getData(getField());
		
		List<?> value = data instanceof List<?> ? (List<?>)data : null; 
				
		return value;
	}
	
	@Override
	public void set(Object object, Object data) {
		Assert.isInstanceOf(Content.class, object);
		EFormData formdata = ((Content)object).getFormData(getForm());
		formdata.setData(getField(), data);
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
	public String getDescription(Locale locale) {
		String description = getModelObjectName(locale) + " ";
		return description;
	}
	
	@Override
	@JsonIgnore
	public String getTypeLabel() {
		return ETableModel.GetTypeLabel();
	}
	
	@JsonIgnore
	public String getModelObjectName(Locale locale) {
		return "";
	}
} 