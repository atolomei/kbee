package com.novamens.content.rule;

import java.io.Serializable;

import com.novamens.content.base.Content;
import com.novamens.content.model.Classificable;

public interface Action extends Serializable {
	
	public Object execute(Content content);
	public Object execute(Classificable content);
	
	public boolean justOneTime();
	
	public Serializable getActionRuleId();
	public void setActionRuleId(Serializable aid);
		
	public String getActionRuleName();	
	public void setActionRuleName(String name);
}