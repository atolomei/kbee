package com.novamens.kbee.content.enoti;


import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.novamens.content.base.ContentCreationException;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.enoti.ENotiRule;
import com.novamens.content.enoti.ENotiRuleDao;
import com.novamens.content.enoti.ENotiRuleService;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.event.LogEvent;
import com.novamens.logging.SecurityCreateEvent;
import com.novamens.logging.SecurityDeleteEvent;
import com.novamens.logging.SecurityUpdateEvent;
import com.novamens.security.Principal;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;


/**
 * 
 * 
 *
 */
public class KbeeENotiRuleService implements ENotiRuleService {
				
	private ENotiRuleDao dao;
	
	static private Logger trx_logger = LogManager.getLogger("TxLogger");
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeENotiRuleService.class.getName());


	public KbeeENotiRuleService() {}

	@Override
	@Transactional
	public void update(ENotiRule rule, List<String> updatedParts) throws ContentMgmtException {
		
		if (((KbeeENotiRule)rule).getState().equals(ObjectState.DRAFT))
			((KbeeENotiRule)rule).setState(ObjectState.ENABLED);
		getDao().save(rule);
		trx_logger.info(new SecurityUpdateEvent(rule, updatedParts));
	
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void delete(ENotiRule rule) throws ContentMgmtException {
		getDao().delete(rule);
		List<String> list= new ArrayList<String>();
		list.add("Delete");
		trx_logger.info(new SecurityDeleteEvent(rule, list));
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public ENotiRule createEmailRule(User user) throws ContentCreationException {
			return createEmailRule(user, null, false);
	}
	
	@Transactional(propagation = Propagation.REQUIRED)
	public ENotiRule createEmailRule(User user, String key, boolean isSystem) throws ContentCreationException {
		try {
			User caller = ServiceLocator.getService(SecurityService.class).getSessionUser();
			KbeeENotiRule rule = new KbeeENotiRule();
			
			rule.setOwner(user);
			
			List<Principal> list = new ArrayList<Principal>();
			if (!isSystem)
				list.add(getSessionUser());
			
			rule.setReceivers(list);
			rule.setLastModifiedUser(caller);
			rule.setCreationOffsetDateTime(OffsetDateTime.now());
			rule.setLastModifiedOffsetDateTime(OffsetDateTime.now());
			rule.setDomain(getDomain());
			rule.setEventType(ENotiRule.EVENT_PUBLISH_CONTENT);
			rule.setKey(key);
			rule.setState(ObjectState.ENABLED);
			rule.setIsSystem(isSystem);
			
			rule.setEmail(true);
			rule.setAlert(true);
			
			logger.debug(rule.toString());
			
			getDao().save(rule);
			trx_logger.info(new SecurityCreateEvent(rule, "Create"));
			return rule;
			
		} 
		catch (ContentCreationException e) {
			throw e;
		} 
		catch (Exception e) {
			logger.error(e);
			throw new ContentCreationException(e);
		}
	}

	@Override
	public List<ENotiRule> getSystemEmailRules() {
		return getDao().getSystemENotiRules(getDomain());
	}

	
	@Override
	public List<ENotiRule> getEmailRules() {
		return getDao().getENotiRules(getDomain());
	}
	
	@Override
	public List<ENotiRule> getEmailRules(Domain domain) {
		return getDao().getENotiRules(domain);
	}
	
	@Override
	public List<ENotiRule> getEmailRules(User owner) {
		return getDao().getENotiRules(owner);
	}

	@Override
	public List<ENotiRule> getEmailRules(Domain domain, int event_type) {
		return getDao().getENotiRules(domain, event_type);
	}
	
	public List<ENotiRule> getEmailRules(Domain domain, LogEvent event) {
		List<ENotiRule> rules = new ArrayList<ENotiRule>();
		for (ENotiRule rule : getDao().getENotiRules(domain)) {
			if (rule.includes(event.getEventType())) {
				rules.add(rule);
			}
		}
		return rules;
	}
	
	public void setDao(ENotiRuleDao dao) {
		this.dao = dao;
	}
	
	public ENotiRuleDao getDao() {
		return dao;
	}
	
	private Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
	
	private User getSessionUser() {
		return ServiceLocator.getService(com.novamens.service.SecurityService.class).getSessionUser();
	}
}
