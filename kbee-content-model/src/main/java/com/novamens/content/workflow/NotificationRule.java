package com.novamens.content.workflow;

import java.util.List;

import com.novamens.content.security.Role;

public interface NotificationRule extends WorkflowRule {
	public List<Role> getReceivers();
	public String getText();
}
