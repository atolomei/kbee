package com.novamens.kbee.content.form;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.novamens.content.base.Resource;
import com.novamens.content.form.EFormData;
import com.novamens.content.form.EResourceField;

@JsonTypeName("resource")
public class KbeeEResource extends EFormAbstractField<Resource> implements EResourceField  {
	private static final long serialVersionUID = 1L;
	
	private int width;
	private int viewMode;
	private boolean toolbar;
	
	@Override
	public void get(Object object, EFormData data) {
		data.setData(this, getModel().get(object));
	}
	
	@Override
	public void set(Object object, EFormData data) {
		getModel().set(object, data.getData(this));
	}
	
	public int getViewMode() {
		return viewMode;
	}
	
	public void setViewMode(int mode) {
		viewMode = mode;
	}
	
	public int getWidth() {
		return width;
	}
	
	public void setWidth(int value) {
		width = value;
	}
	
	public boolean isToolbar() {
		return toolbar;
	}

	public void setToolbar(boolean toolbar) {
		this.toolbar = toolbar;
	}

	@Override
	@JsonIgnore
	public String getTypeLabel() {
		return "Resource";
	}
}