package com.novamens.workflow;

import com.novamens.security.acl.Permission;

public interface ManualTrigger extends Trigger {
	public Permission getManualPermission();
}
