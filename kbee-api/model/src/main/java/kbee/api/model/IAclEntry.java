package kbee.api.model;

import java.util.ArrayList;
import java.util.List;

public class IAclEntry {
	ApiProxy principal;
	boolean negative;
	List<String> permissions;
	
	public IAclEntry() {
	}
	public ApiProxy getPrincipal() {
		return principal;
	};
	public void setPrincipal(ApiProxy principal) {
		this.principal = principal;
	};
	public void setNegative(boolean value) {
		this.negative = value;
	}
	public boolean isNegative() {
		return this.negative;
	}
	public List<String> getPermissions() {
		return permissions;
	}
	public void addPermission(String permission) {
		if (permissions==null) permissions = new ArrayList<String>();
		permissions.add(permission);
	}
};