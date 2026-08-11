package com.novamens.content.base;

import java.io.Serializable;

import com.novamens.dom.Domain;
import com.novamens.dom.DomainObject;
import com.novamens.dom.Indexable;
import com.novamens.dom.ObjectState;

public interface Rule extends com.novamens.dom.Object,  Indexable,  DomainObject {
	
	public Serializable getId();

	public void setName(String name);
	public String getName();
	
	public String getDisplayName();
					
	public void setNotes(String notes);
	public String getNotes();

	public String getDescription();
	public void setDescription(String description);

	public String getCondition();
	public void setCondition(String condition);
	
	public String getDisplayCondition();
	public void setDisplayCondition(String condition);
	
	public boolean evaluate(Content content);
	
	public Domain getDomain();
	public void setDomain(Domain domain);
	
	public boolean isDerived();
	
	public ObjectState getState();
	
}

