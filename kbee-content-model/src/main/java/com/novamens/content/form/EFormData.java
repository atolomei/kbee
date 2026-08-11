package com.novamens.content.form;

import java.util.List;

import com.novamens.content.base.SignedData;

public interface EFormData {
	public EForm getForm();
	public Object getData(String component);
	public Object getObject(String component);
	public Object getData(EFormField<?> field);
	public <T> List<T> getValues(EListField<T> field);
	public void setData(String component, Object value);
	public void setData(EFormField<?> field, Object value);
	public boolean isEmpty();
	public boolean isSigned();
	public List<SignedData> getSignatures();
	public void setSignatures(List<SignedData> signed);
	public void setSignature(SignedData signed);
	public void clearSignatures();
	public EFormData clone();
	public String getObjectTitle();
	public Object getObject();
}