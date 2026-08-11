package com.novamens.content.form;

import java.util.List;

public interface EFormField<T> extends EFormComponent {
	public void validate(EValidatable validatable);
	public List<EValidation> getValidations();
	// Required in the form
	public boolean isRequired();
	// Read Only
	public boolean isReadOnly();
	// Persistence strategy
	public EFieldModel<T> getModel();
	// Update object with form data
	public void set(Object object, EFormData data);
	// Build form data from object
	public void get(Object object, EFormData data);
	// Serialize an object in the context defined by the object being edited in the form 
	public String serialize(Object formobject, T object);
	public T deserialize(Object formobject, String token);
	// Calculation Script
	public String getCalculation();
	// On Update Script
	public String getOnUpdate();
	// Help text (HTML)
	public String getHelpText();
	
	public boolean isCalculable();
	
	public boolean isSingleValue();
	
	public String getDisplayValue(Object object);
}