package com.novamens.preferences;

import java.util.Properties;

import com.novamens.security.User;

public interface Preferences {

	public void setId(Long id);
	public Long getId();

	public User getUser();
	public String getName();
	public void setName(String name);

	public String getPreference(String key);
	public void setPreference(String key, String value);
	
	
	/**
	 * get All pairs as a Properties: key, value
	 * @return
	 */
	public Properties getProperties();
	
	
}
