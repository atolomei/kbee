package com.novamens.content.security;

import java.util.Set;

import com.novamens.content.entity.Person;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.EntityMember;
import com.novamens.dom.Domain;
import com.novamens.dom.DomainObject;
import com.novamens.dom.Indexable;
import com.novamens.dom.ObjectState;
import com.novamens.security.Identifiable;
import com.novamens.security.acl.Group;


public interface Role extends Identifiable, Indexable, DomainObject {
	
	public String getName();
	public Domain getDomain();
	public void setRole(Person person);
	public void setRole(Person user, EntityMember entity);
	public void removeRole(Person user);
	public void removeRole(Person user, EntityMember entity);
	public String getCondition();
	public boolean isCanonical();
	public boolean isAdministrator();
	public void setCanonical(boolean canonical);
	public String getAlias();
	public void setAlias(String alias);
	public int getType();
	
	public boolean isApiEnabled();
	public void setApiEnabled(boolean b);

	public String getDescription();
	public void setDescription(String str);
	
	public Set<Group> getGroups();

	/**
	 * 
	 * Dynamic or General
	 */
	public String getRoleType();
	
	default public boolean isEntity() { return false; }
	public boolean isDefault();
	public boolean getIsDefault();
	boolean isOnlyRootEditable();
	void setOnlyRootEditable(boolean onlyrooteditable);
	
	public ObjectState getState();
	
	public boolean manage(DataSet dataset);
}