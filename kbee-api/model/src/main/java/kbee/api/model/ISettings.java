package kbee.api.model;

import java.io.Serializable;

public class ISettings implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private int quota;
	private int maxUsers;
	
	public int getQuota() {
		return quota;
	}
	public void setQuota(int quota) {
		this.quota = quota;
	}
	public int getMaxUsers() {
		return maxUsers;
	}
	public void setMaxUsers(int maxUsers) {
		this.maxUsers = maxUsers;
	}
	
}