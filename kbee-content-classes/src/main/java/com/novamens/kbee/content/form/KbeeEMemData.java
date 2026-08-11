package com.novamens.kbee.content.form;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.model.IModel;

import com.novamens.content.base.SignedData;
import com.novamens.content.form.EForm;
import com.novamens.content.form.EFormData;
import com.novamens.content.form.EFormField;
import com.novamens.content.form.EListField;

public abstract class KbeeEMemData implements EFormData {
	
	private EForm form;
	private Map<String, Object> data = new HashMap<String, Object>();
	private List<SignedData> signatures;
	
	public KbeeEMemData(EForm form) {
		this.form = form;
	}
	
	@Override
	public Object getData(String name) {
		return data.get(name);
	}
	
	@Override
	public Object getObject(String name) {
		return data.get(name);
	}
	
	@Override
	public Object getData(EFormField<?> field) {
		Object value =  data.get(field.getName());
		return value;
	}
	
	public void setData(String name, Object value) {
		if (value!=null)
			data.put(name, value);
		else
			data.remove(name);
	}

	public void setData(EFormField<?> field, Object value) {
		setData(field.getName(), value);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T> List<T> getValues(EListField<T> field) {
		ParameterizedType t = (ParameterizedType)field.getClass().getGenericSuperclass();
		Type type = t.getActualTypeArguments()[0];
		Class<T> c = (Class<T>)type;
		List<T> values = new ArrayList<T>();
		
		Object value =  data.get(field.getName());
		
		if (value instanceof List<?>) {
			for (Object object : (List<?>)value) {
				if (object instanceof IModel<?>) {
					object = ((IModel<?>)object).getObject();
				}
				if (c.isInstance(object)) {
					values.add((T)object);
				}
			}
		}
		else if (value instanceof IModel<?>) {
			Object object = ((IModel<?>)value).getObject();
			if (c.isInstance(value)) {
				values.add((T)object);
			}
		}
		else if (c.isInstance(value)) {
			values.add((T)value);
		}
		return values;
	}
	
	@Override
	public EForm getForm() {
		return form;
	}
	
	public Map<String, Object> getData() {
		return data;
	}

	public void setData(Map<String, Object> data) {
		this.data = data;
	}

	@Override
	public boolean isEmpty() {
		return data.isEmpty();
	}
	
	public boolean isSigned() {
		return getSignatures()!=null && !getSignatures().isEmpty();
	}
	
	public List<SignedData> getSignatures() {
		return signatures;
	}
	
	@Override
	public void setSignature(SignedData signature) {
		if (this.signatures==null) signatures = new ArrayList<SignedData>(); 
		this.signatures.add(signature);
	}
	
	@Override
	public void setSignatures(List<SignedData> signatures) {
		this.signatures = new ArrayList<SignedData>();
		this.signatures.addAll(signatures);
	}
	
	public void clearSignatures() {
		this.signatures = new ArrayList<SignedData>();
	}
	
	public abstract EFormData clone();
	
	@Override
	public String getObjectTitle() {
		return null;
	}
}