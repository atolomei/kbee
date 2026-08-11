package com.novamens.kbee.content.form;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.novamens.content.form.EFormAccessLevel;
import com.novamens.content.form.EDisposition;
import com.novamens.content.form.EForm;
import com.novamens.content.form.EFormComponent;
import com.novamens.content.form.EFormContainer;
import com.novamens.content.form.EFormData;
import com.novamens.content.form.EFormField;

public class KbeeEMemForm implements EForm, Serializable {
	private static final long serialVersionUID = 1L;
	
	private List<EFormComponent> components;
	private String jsoncomponents;
	private String name;
	private String cssClass;

	public KbeeEMemForm() {
	}
	
	public KbeeEMemForm(String components) {
		this.jsoncomponents = components;
	}
	
	public List<EFormComponent> getComponents() {
		if (components==null) {
			if (jsoncomponents!=null) {
				components = parseComponents(jsoncomponents);
			}
			else {
				return  new ArrayList<EFormComponent>();
			}
		}	
		return components;
	}
	
	public void setComponents(List<EFormComponent> components) {
		this.components = components;
	}
	
	public List<EFormField<?>> getFields() {
		return getFields(getComponents());
	}
	
	public EFormField<?> getField(String name) {
		for (EFormField<?> field : getFields(getComponents())) {
			if (name.equals(field.getName())) {
				return field;
			}
		}
		return null;
	}
	
	@Override
	public boolean isEnabled() {
		return true;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	public String getCssClass() {
		return cssClass;
	}

	public void setCssClass(String cssClass) {
		this.cssClass = cssClass;
	}

	@Override
	public String getDisplayName() {
		return name;
	}
	
	@Override
	public EDisposition getDisposition() {
		return EDisposition.VERTICAL;
	}
	
	@Override
	public EFormAccessLevel getFormAccessLevel() {
		return EFormAccessLevel.GENERAL;
	}
	
	@Override
	public boolean isUseInline() {
		return false;
	}

	@Override
	public boolean isFileContainer() {
		return false;
	}
	
	@Override
	public boolean isVisible(EFormData data) {
		return true;
	}
	
	@Override
	public String getViewer() {
		return null;
	}
	
	@Override
	public List<String> getBehaviors() {
		return new ArrayList<String>();
	}
	
	private List<EFormField<?>> getFields(List<EFormComponent> components) {
		List<EFormField<?>> fields = new ArrayList<EFormField<?>>();
		for (EFormComponent component : components) {
			if (component instanceof EFormField) {
				fields.add((EFormField<?>)component);
			}
			if (component instanceof EFormContainer) {
				fields.addAll(getFields(((EFormContainer)component).getComponents()));
			}
		}
		return fields;
	}
	
	private List<EFormComponent> parseComponents(String json) {
		return EFormParser.Get().getComponents(json);
	}

	@Override
	public boolean hasToolbar() {
		// TODO Auto-generated method stub
		return false;
	}
	
	//@Override
	//public boolean isTitleVisible() {
	//	return false;
	//}

}