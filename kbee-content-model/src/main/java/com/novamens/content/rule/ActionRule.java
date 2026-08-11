package com.novamens.content.rule;

import java.time.OffsetDateTime;

import com.novamens.content.base.Content;
import com.novamens.content.base.Rule;
import com.novamens.content.base.RuleCondition;
import com.novamens.content.model.Classificable;
import com.novamens.security.audit.AuditSet;

public interface ActionRule extends Rule {
	
	static final public String PRODUCTION = "production";
	static final public String TEST_CONDITION = "test-condition";
	static public final String EMAIL_TEMPLATE_KEY = "notification-by-action-rule";
	
	public Action getAction();
	public void evaluate();
	public void evaluate(String mode);
	public boolean evaluate(Classificable classificable);
	public RuleCondition getRuleCondition();
	public String getType();
	public Boolean getCalendar();
	
	public boolean isContentRule();
	public Long getContentOId();
	
	public OffsetDateTime getExecutionDate();
	public OffsetDateTime getExecutionDate(Content conent);
	public Content getContent();
	
	public default AuditSet getAuditSet() {
		return AuditSet.SYSTEM;
	}

}