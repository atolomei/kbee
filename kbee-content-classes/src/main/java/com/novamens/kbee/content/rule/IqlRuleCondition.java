package com.novamens.kbee.content.rule;

import com.novamens.content.base.RuleCondition;

public class IqlRuleCondition implements RuleCondition {
	private String statement;
	
	public IqlRuleCondition(String statement) {
		setStatement(statement);
	}

	public String getStatement() {
		return statement;
	}

	public void setStatement(String statement) {
		this.statement = statement;
	}
}
