package com.novamens.content.enoti;

import java.io.Serializable;
import java.util.List;

import com.novamens.dao.Dao;
import com.novamens.dom.Domain;
import com.novamens.security.User;

public interface ENotiRuleDao  extends Dao {

	public ENotiRule findENotiRuleById(Serializable id);
	
	public List<ENotiRule> getENotiRules(Domain domain);
	
								
	
	
	/**
	 * User email rules (does not include system rules)
	 * @param owner
	 * @return
	 */
	public List<ENotiRule> getENotiRules(User owner);
	
	public void save(ENotiRule rule);
	public void delete(ENotiRule rule);

	/**
	 * Returns Domain Rules that are ENABLED and for event event_type (both personal and system)
	 */
	public List<ENotiRule> getENotiRules(Domain domain, int event_type);
		
	
	public List<ENotiRule> getSystemENotiRules(Domain domain);
	
	
	
	
	
}
