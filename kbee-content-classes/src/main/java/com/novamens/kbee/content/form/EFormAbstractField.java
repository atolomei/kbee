package com.novamens.kbee.content.form;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.novamens.content.form.EFieldAwareModel;
import com.novamens.content.form.EFieldModel;
import com.novamens.content.form.EFormData;
import com.novamens.content.form.EFormEvent;
import com.novamens.content.form.EFormField;
import com.novamens.content.form.EValidatable;
import com.novamens.content.form.EValidation;
import com.novamens.content.model.Classificable;

public abstract class EFormAbstractField<T> extends EFormAbstractComponent implements EFormField<T> {
	private static final long serialVersionUID = 1L;
	
	private String text;
	
	@JsonProperty("sublabel")
	private String sublabel;
	
	@JsonProperty("help")
	private String helpText;
	private boolean required = false;
	private boolean readOnly = false;
	private EFieldModel<T> model;
	private List<EValidation> validations;
	private String calculation;
	private String onUpdate;

	public EFormAbstractField() {
	}
	
	public EFormAbstractField(String label) {
		super(label);
	}
	
	public String getSublabel() {
		return sublabel;
	}
	
	public void setSublabel(String text) {
		this.sublabel = text;
	}
	
	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
	public String getHelpText() {
		return helpText;
	}
	
	public void setHelpText(String text) {
		this.helpText = text;
	}

	@Override
	public boolean isRequired() {
		return required;
	}
	
	public void setRequired(boolean value) {
		this.required = value;
		if (!value)
		for (EValidation validation : getValidations()) {
			if (validation instanceof KbeeEMultipicityValidation) {
				validations.remove(validation);
				break;
			}
		}
	}
	
	@Override
	public boolean isReadOnly() {
		return readOnly || (getModel()!=null && getModel().isReadOnly());
	}
	
	public void setReadOnly(boolean value) {
		this.readOnly = value;
	}
	
	public EFieldModel<T> getModel() {
		return model;
	}
	
	@SuppressWarnings("unchecked")
	public void setModel(EFieldModel<?> model) {
		this.model = (EFieldModel<T>)model;
		if (model!=null && model instanceof EFieldAwareModel) {
			((EFieldAwareModel)model).setField(this);
		}
	}
	
	public void validate(EValidatable validatable) {
		for (EValidation validation : getValidations()) {
			validation.validate(validatable);
		}
	}
	
	public List<EValidation> getValidations() {
		List<EValidation> validations = new ArrayList<EValidation>();
		if (isRequired()) {
			boolean multiplicity = false;
			if (this.validations!=null)
			for (EValidation validation : this.validations) {
				if (validation instanceof KbeeEMultipicityValidation) {
					multiplicity = true;
					break;
				}
			}
			if (!multiplicity) {
				validations.add(new KbeeEMultipicityValidation());
			}
		}
		if (this.validations!=null) {
			validations.addAll(this.validations);
		}	
		return validations;
	}
	
	public void addValidation(EValidation validation) {
		if (validations==null) validations = new ArrayList<EValidation>();
		validations.add(validation);
	}
	
	public void clearValidations() {
		if (validations!=null) 
			validations.clear();
	}
	
	public String getCalculation() {
		return calculation;
	}

	public void setCalculation(String calculation) {
		this.calculation = calculation;
	}
	
	public String getOnUpdate() {
		return onUpdate;
	}

	public void setOnUpdate(String onUpdate) {
		this.onUpdate = onUpdate;
	}
	
	public Object onUpdate(EFormData data) {
		ScriptEvaluator evaluator = new ScriptEvaluator();
		Object evaluation =  evaluator.evaluate(getOnUpdate(), data);
		return evaluation;
	}

	public Object calculate(EFormData data) {
		ScriptEvaluator evaluator = new ScriptEvaluator();
		Object evaluation =  evaluator.evaluate(getCalculation(), data);
		return evaluation;
	}
	
	public Object calculate(EFormData data, EFormEvent event) {
		ScriptEvaluator evaluator = new ScriptEvaluator();
		evaluator.setBinding("event", event);
		Object evaluation =  evaluator.evaluate(getCalculation(), data);
		return evaluation;
	}
	
	@Override
	public void get(Object object, EFormData data) {
	}
	
	@Override
	public void set(Object object, EFormData data) {
		getModel().set(object, data.getData(this));
	}
	
	@Override
	public String serialize(Object formobject, T object) {
		return getModel().serialize((Classificable)formobject, object);
	}
	
	@Override
	public T deserialize(Object formobject, String token) {
		return getModel().deserialize((Classificable)formobject, token);
	}
	
	@Override
	@JsonIgnore
	public boolean isCalculable() {
		return true;
	}
	
	@Override
	@JsonIgnore
	public boolean isSingleValue() {
		return true;
	}
	
	@Override
	public String getDisplayValue(Object object) {
		return null;
	}
}