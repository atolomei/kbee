package com.novamens.kbee.content.form;

import java.lang.reflect.Method;
import java.util.Locale;
import java.util.ResourceBundle;

import org.springframework.util.ReflectionUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.novamens.content.base.ResourceTag;
import com.novamens.content.form.EResourceModel;
import com.novamens.content.resource.KBFile;

@JsonTypeName("file property")
public class  KbeeEFilePropertyModel extends KbeeEPropertyFieldModel<KBFile> implements EResourceModel<KBFile> {
	private static final long serialVersionUID = 1L;
	
	public KbeeEFilePropertyModel() {
		
	}
		
	public KbeeEFilePropertyModel(String property) {
		setProperty(property);
	}
	
	public ResourceTag getTag() {
		return null;
	}
	
	protected String toString(Object value) {
		return value!=null ? value.toString() : null;	
	}
	
	protected KBFile getValueOf(String stringvalue) {
		return null;
	}
	
	@JsonIgnore
	public String getModelObjectName(Locale locale) {
		ResourceBundle res = ResourceBundle.getBundle( KbeeEAttributeFieldModel.class.getName(), locale);
		return res.getString("file property");
	}
	
	@Override
	protected Method getSetter(Object object) {
		String property = getProperty();
		property = property.substring(0,1).toUpperCase() + property.substring(1);
		Method method = ReflectionUtils.findMethod(object.getClass(), "set"+property, KBFile.class);
		return method;
	}
}