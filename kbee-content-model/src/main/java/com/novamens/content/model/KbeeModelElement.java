package com.novamens.content.model;

import java.io.Serializable;

import java.time.OffsetDateTime;

import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.security.User;
import com.novamens.service.ObjectService;
import com.novamens.service.ServiceNotFoundException;

/**
 * Clase intermedia utilizada solamente para la edición de las estructuras como wrapper de atributos o clasificadores
 */
public class KbeeModelElement implements ModelElement {
	
	private ModelElement element;
	private ModelElement parent;
	private boolean reverse = false;
	
	public KbeeModelElement(ModelElement parent, ModelElement element) {
		this.element = element;
		this.parent = parent;
	}
	
	public KbeeModelElement(ModelElement parent, ModelElement element, boolean reverse) {
		this.element = element;
		this.parent = parent;
		this.reverse = reverse;
	}	
	
	public String getName() {
		return getParent().getName() + "->" + getElement().getName();
	}
	
	public ModelElement getElement() {
		return element;
	}
	
	public ModelElement getParent() {
		return parent;
	}
	
	public Multiplicity getMultiplicity() {
		return getElement().getMultiplicity();
	}

	public boolean isVisible(String context) {
		return getElement().isVisible(context);
	}
	
	public void setVisibility(String context, boolean value) {
		getElement().setVisibility(context, value);
	}
	
	public boolean isOrdered() {
		return getElement().isOrdered();
	}

	public Domain getDomain() {
		return null;
	}
	
	public void setDomain(Domain domain) {
		
	}
	
	public String getAlias() {
		return null;
	}
	public Serializable getId() {
		return null;
	}
	
	public void setId(Serializable id) {
		
	}
	
	public String getDisplayName() {
		return getName();
	}
	
	public <T extends ObjectService> T getService(Class<T> service) throws ServiceNotFoundException {
		return null;
	}
	
	public void setState(ObjectState enabled) {
		
	}
	
	public ObjectState getState() {
		return null;
	}
	
	public String getLastModifiedOffsetDateTimeColloquial() {
		return null;
	}
	
	public String getCreationOffsetDateTimeColloquial() {
		return null;
	}
	
	public void setDefaultAudit() {
		
	}

	public void setLastModifiedUser(User user) {
		
	}
	
	public User getLastModifiedUser() {
		return null;
	}
	
	public void setCreationOffsetDateTime(OffsetDateTime date) {
		
	}
	
	public OffsetDateTime getCreationOffsetDateTime() {
		return null;
	}
	
	public void setLastModifiedOffsetDateTime(OffsetDateTime date) {
		
	}
	
	public OffsetDateTime getLastModifiedOffsetDateTime(String css) {
		return null;
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public boolean isDefaultStructure() {
		return false;
	}
	
	public boolean isReverse() {
		return reverse;
	}

	@Override
	public boolean isOnlyRootEdit() {
		return false;
	}

	@Override
	public OffsetDateTime getLastModifiedOffsetDateTime() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getLastModifiedOffsetDateTimeColloquial(String css) {
		return null;
	}

	@Override
	public void setAlias(String alias) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setName(String name) {
		// TODO Auto-generated method stub
		
	}
}
