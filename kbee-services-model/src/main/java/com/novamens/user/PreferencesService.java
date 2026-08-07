package com.novamens.user;

import java.util.List;

import com.novamens.preferences.Preferences;
import com.novamens.service.ObjectService;

public interface PreferencesService extends ObjectService {
	
	public String getValue(String setName, String key);
	public String getValue(String setName, String key, String defaultValue);
	
	public int getIntValue(String setName, String key);
	public int getIntValue(String setName, String key, int default_value);
	
	public void setValue(String setName, String key, String value);
	public void setIntValue(String setName, String key, int value);
	
	
	public List<Preferences> getPreferencesByPrefix(String name);
	
	
	public void deleteAllPreferences(String consoleKey);
	public void deleteAllPreferences();
	
	
}
