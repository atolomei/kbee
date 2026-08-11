package kbee.api.model;

import java.io.Serializable;

public class ITrigger implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String type;
	
	private IKeyValue manualPermission;
	private IKeyValue permission;
	private IKeyValue backupPermission;
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}

	public IKeyValue getManualPermission() {
		return manualPermission;
	}

	public void setManualPermission(IKeyValue manualPermission) {
		this.manualPermission = manualPermission;
	}

	public IKeyValue getPermission() {
		return permission;
	}

	public void setPermission(IKeyValue permission) {
		this.permission = permission;
	}

	public IKeyValue getBackupPermission() {
		return backupPermission;
	}

	public void setBackupPermission(IKeyValue backupPermission) {
		this.backupPermission = backupPermission;
	}
	
	
	
}