package com.novamens.content.service.domain;

import com.novamens.dom.Domain;
import com.novamens.dom.Json;
import com.novamens.security.Identifiable;

public interface DomainSettings extends Identifiable {

	public void setDomain(Domain domain);

	public void setCategory(String category);
	public String getCategory();
	
	public void setValues(Json values);
	public Json getValues();

	
}
