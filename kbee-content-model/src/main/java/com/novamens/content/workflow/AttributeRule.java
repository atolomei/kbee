package com.novamens.content.workflow;

import com.novamens.content.model.Attribute;

public interface AttributeRule extends WorkflowRule {
	public Attribute getAttribute();
	public String getValue();
}
