package com.novamens.workflow;

import com.novamens.security.acl.Permission;

public interface UserTrigger extends Trigger {
	public Permission getManualPermission();
}