package com.novamens.kbee.content.rule;

import java.io.Serializable;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.Classificable;
import com.novamens.content.rule.Action;
import com.novamens.service.ServiceLocator;

public abstract class KbeeAbstractAction implements Action, Serializable {
	private static final long serialVersionUID = 1L;

	private Serializable actionRuleId;
	private String actionRuleName;
	
	public Serializable getActionRuleId() {
		return this.actionRuleId;	
	}
	
	public void setActionRuleId(Serializable aid) {		
		actionRuleId = aid;	
	}

	public String getActionRuleName() { 
		return actionRuleName;
	}
	
	public void setActionRuleName(String name) {
		actionRuleName=name;
	}
	
	public boolean justOneTime() {
		return false;
	}
	
	public Object execute(Content content) {
		return null;
	}

	public Object execute(Classificable classificable) {
		return null;
	}
	
	protected ContentDao getContentDao() {
		return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
}