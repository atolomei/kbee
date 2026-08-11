package com.novamens.kbee.content.service.domain;


import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.novamens.content.service.DomainPreferences;
import com.novamens.content.service.domain.DomainPreferencesService;
import com.novamens.dom.Domain;
import com.novamens.preferences.Preferences;

public class KbeeDomainPreferencesService implements DomainPreferencesService {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeDomainPreferencesService.class.getName());

	private Domain domain;
	private KbeeDomainPreferencesDao dao;

	public KbeeDomainPreferencesService() {
		
	}

	public KbeeDomainPreferencesService(Object object) {
		Assert.isInstanceOf(Domain.class, object);
		this.domain = (Domain)object;
	}
	
	
	
	/**
	private String getDefaultContentColumns(String name) {
		if (name==null)
			return null;
		if (name.equals("workspace")) {
			for (getContentDao().getClassifiers(getDomain())) {
			}
		}
		return null;
	}
	**/

	@Override
	public String getValue(String name, String key) {
		
		if (name==null || key==null)
			return null;
		
		DomainPreferences pref = getDomainPreferencesDao().findDomainPreferences(domain, name);
	
		if (pref!=null) {
		  if (pref.getPreference(key)!=null)
			  return pref.getPreference(key);
		}
		return null;
	}
	
	
	@Override
	public String getValue(String name, String key, String defaultValue) {

		if (name==null || key==null)
			return defaultValue;

		DomainPreferences pref = getDomainPreferencesDao().findDomainPreferences(domain, name);
		
		if (pref!=null) {
			  if (pref.getPreference(key)!=null)
				  return pref.getPreference(key);
		}
		return defaultValue;
	}
	

	
	@Override
	public int getIntValue(String name, String key, int default_value) {
		
		DomainPreferences pref = getDomainPreferencesDao().findDomainPreferences(domain, name);
		if (pref!=null) {
			  if (pref.getPreference(key)!=null) { 
				  try {
					  	int intvalue = Integer.valueOf(pref.getPreference(key));
					  	return intvalue;
				  }
				  catch (NumberFormatException e) {
					  logger.error(e);
					  return default_value;
				  }
			  }
		}
		return default_value;
	}


	@Override
	public int getIntValue(String name, String key) {
		return getIntValue(name, key, -1);
	}
	
	
	@Transactional(propagation = Propagation.REQUIRED)
	public void setValue(String name, String key, String value) {
		DomainPreferences pref = getDomainPreferencesDao().findDomainPreferences(getDomain(), name);
		if (pref==null) 
			pref = createPreferences(getDomain(), name);
		if ((pref.getPreference(key)==null) || !(pref.getPreference(key)!=null && pref.getPreference(key).equals(value))) {
			pref.setPreference(key, value);
			getDomainPreferencesDao().save(pref);
		}
	}
	

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void setPreferences(List<Preferences> list) {
		
		if (logger.isDebugEnabled()) {
			for (Preferences p: list) {
				logger.debug(p.toString());	
			}
		}
		
		for (Preferences p: list) {
			Properties prop=p.getProperties();
			for ( Entry<Object, Object> entry: prop.entrySet()) {
					setValue(p.getName(), entry.getKey().toString(), entry.getValue().toString());	
			}
		}
		
	}
	
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void setIntValue(String name, String key, int value) {

		DomainPreferences pref = getDomainPreferencesDao().findDomainPreferences(getDomain(), name);

		if (pref==null) 
			pref = createPreferences(getDomain(), name);
		
		if ( (pref.getPreference(key)==null) || !(pref.getPreference(key)!=null && pref.getPreference(key).equals(String.valueOf(value)))) {
			pref.setPreference(key, String.valueOf(value));
			getDomainPreferencesDao().save(pref);
		}
	}

	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void deleteAllPreferences() {
		getDomainPreferencesDao().deleteAllDomainPreferences(getDomain());
	}
	
	public void setDomainPreferencesDao(KbeeDomainPreferencesDao dao) {
		this.dao = dao;
	}
	
	public KbeeDomainPreferencesDao getDomainPreferencesDao() {
		return this.dao;
	}
	
	private KbeeDomainPreferences createPreferences(Domain domain, String name) {
		return new KbeeDomainPreferences(domain, name);
	}

	private Domain getDomain() {	
		return this.domain;
	}
	
}
