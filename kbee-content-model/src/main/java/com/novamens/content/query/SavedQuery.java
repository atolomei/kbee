package com.novamens.content.query;

import java.io.Serializable;
import java.util.Map;

import com.novamens.dom.DomainObject;
import com.novamens.dom.Object;
import com.novamens.security.User;

public interface SavedQuery  extends DomainObject, Object  {
	public Serializable getId();
	public User getUser();
	
	public String getTitle();
	
	public String getConsole();
	
	public String getBrowser();
	
	public Map<String, java.lang.Object> getParameters();
	public boolean isSystem();
	public boolean isHome();
	
	public String getStatement();
}