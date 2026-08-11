package com.novamens.kbee.content.form;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.novamens.content.form.EFormComponent;
import com.novamens.content.form.EFormData;

public abstract class EFormAbstractComponent implements EFormComponent, Serializable {
	private static final long serialVersionUID = 1L;
	
	private String name;
	private String label;
	
	private String cssClass;
	private String visibleCondition;
	private String enabledCondition;
	private EFormComponent parent;
	
	public EFormAbstractComponent() {
	}
	
	public EFormAbstractComponent(String label) {
		setLabel(label);
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getLabel() {
		return label;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}
	
	public String getSublabel() {
		return null;
	}
	
	@Override
	public String getCssClass() {
		return cssClass;
	}
	
	public void setCssClass(String label) {
		this.cssClass = label;
	}

	@JsonIgnore
	public void setParent(EFormComponent parent) {
		this.parent = parent;
	}

	@JsonIgnore	
	public EFormComponent getParent() {
		return parent;
	}
	
	@Override
	public boolean isEnabled(EFormData data) {
		if (enabledCondition!=null && !"".equals(enabledCondition)) {
			return Boolean.TRUE.equals(evaluate(enabledCondition, data));
		}
		return true;
	}
	
	@Override
	public boolean isVisible(EFormData data) {
		if (visibleCondition!=null && !"".equals(visibleCondition)) {
			return Boolean.TRUE.equals(evaluate(visibleCondition, data));
		}
		return true;
	}
	
	public void setEnabledCondition(String condition) {
		this.enabledCondition = condition;
	}
	
	@Override
	public String getEnabledCondition() {
		return this.enabledCondition;
	}
	
	public void setVisibleCondition(String condition) {
		this.visibleCondition = condition;
	}
	
	@Override
	public String getVisibleCondition() {
		return this.visibleCondition;
	}
	
	@JsonIgnore
	public String getTypeLabel() {
		return getClass().getSimpleName();
	}
	
	protected Object evaluate(String condition, EFormData data) {
		return (new ScriptEvaluator()).evaluate(condition, data);
	}
}