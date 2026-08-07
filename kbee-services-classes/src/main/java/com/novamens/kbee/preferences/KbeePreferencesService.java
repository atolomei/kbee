package com.novamens.kbee.preferences;

import java.util.List;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;


import com.novamens.preferences.Preferences;
import com.novamens.security.User;
import com.novamens.user.PreferencesService;

public class KbeePreferencesService implements PreferencesService {
	
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeePreferencesService.class.getName());

	private User user;
	private KbeePreferencesDao dao;

	public KbeePreferencesService() {
	}
	
	public KbeePreferencesService(Object object) {
		Assert.isInstanceOf(User.class, object);
		this.user = (User)object;
	}
	
	

	@Override
	public String getValue(String name, String key) {
		Preferences pref = getKbeePreferencesDao().findPreferences(user, name);
		if (pref!=null) {
		  if (pref.getPreference(key)!=null)
			  return pref.getPreference(key);
		}
		return null;
	}
	
	
	public String getValue(String name, String key, String defaultValue) {
		
		Preferences pref = getKbeePreferencesDao().findPreferences(user, name);
		if (pref!=null) {
			  if (pref.getPreference(key)!=null)
				  return pref.getPreference(key);
		}
		return defaultValue;
	}
	

	@Override
	public int getIntValue(String name, String key, int default_value) {
		
		Preferences pref = getKbeePreferencesDao().findPreferences(user, name);
		
		if (pref!=null) {
			  if (pref.getPreference(key)!=null) { 
				  try {
					  	int intvalue = Integer.valueOf(pref.getPreference(key));
					  	return intvalue;
				  }
				  catch (NumberFormatException e) {
					  return default_value;
				  }
			  }
		}
		return default_value;
	}

	

	public int getIntValue(String name, String key) {
		return getIntValue(name, key, -1);
	}
	
	
	
	 
	
	@Transactional(propagation = Propagation.REQUIRED)
	public void setValue(String name, String key, String value) {
		Preferences pref = getKbeePreferencesDao().findPreferences(getUser(), name);
		
		if (pref==null) 
			pref = createPreferences(getUser(), name);
		
		if ((pref.getPreference(key)==null) || !(pref.getPreference(key)!=null && pref.getPreference(key).equals(value))) {
			pref.setPreference(key, value);
			getKbeePreferencesDao().save(pref);
		}
	}
	

	@Transactional(propagation = Propagation.REQUIRED)
	public void setIntValue(String name, String key, int value) {

		Preferences pref = getKbeePreferencesDao().findPreferences(getUser(), name);

		if (pref==null) 
			pref = createPreferences(getUser(), name);
		if ( (pref.getPreference(key)==null) || !(pref.getPreference(key)!=null && pref.getPreference(key).equals(String.valueOf(value)))) {
			
			logger.debug(pref.toString());
			
			pref.setPreference(key, String.valueOf(value));
			getKbeePreferencesDao().save(pref);
		}
	}

	@Override
	public List<Preferences> getPreferencesByPrefix(String name) {
		return getKbeePreferencesDao().findPreferencesByPrefix(getUser(), name);
	}
	
	
	@Override
	@Transactional
	public void deleteAllPreferences() {
		getKbeePreferencesDao().deleteAllPreferences(getUser());
		//
		// no se puede loggear porqe esta en kbee services
		// logger.info(new com.novamens.logging.SecurityEvent(user, "Reset Preferences"));
		//
	}
	


	@Override
	@Transactional
	public void deleteAllPreferences(String prefix) {
		getKbeePreferencesDao().deleteAllPreferences(getUser(), prefix+"/%");
	}

	
	public void setKbeePreferencesDao(KbeePreferencesDao dao) {
		this.dao = dao;
	}

	public void setPreferencesDao(KbeePreferencesDao dao) {
		this.dao = dao;
	}
	
	public KbeePreferencesDao getPreferencesDao() {
		return this.dao;
	}
	
	public KbeePreferencesDao getKbeePreferencesDao() {
		return this.dao;
	}

	private KbeePreferences createPreferences(User user, String name) {
		KbeePreferences preferences = new KbeePreferences(getUser(), name);
		return preferences;
	}

	private User getUser() {	
		return this.user;
	}
	
	
	 
 }
