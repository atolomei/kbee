package com.novamens.content.enoti;

import java.util.List;

import com.novamens.content.base.ContentCreationException;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.dom.Domain;
import com.novamens.event.LogEvent;
import com.novamens.security.User;
import com.novamens.service.BusinessSystemService;

public interface ENotiRuleService extends BusinessSystemService {
	
	public void update(ENotiRule rule, List<String> parts) throws ContentMgmtException;
	public void delete(ENotiRule rule) throws ContentMgmtException;
	public List<ENotiRule> getEmailRules();
	
	/**
	 * Returns Domain Rules that are ENABLED and for event event_type (both personal and system)
	 */
	public List<ENotiRule> getEmailRules(Domain domain, int event_type);
	public List<ENotiRule> getEmailRules(Domain domain, LogEvent event);
	
	public List<ENotiRule> getEmailRules(Domain domain);
	public List<ENotiRule> getEmailRules(User owner);
	public List<ENotiRule> getSystemEmailRules() throws ContentCreationException;
													
	public ENotiRule createEmailRule(User user, String key, boolean isSystem) throws ContentCreationException;
	public ENotiRule createEmailRule(User user) throws ContentCreationException;
}