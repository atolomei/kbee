package kbee.api.model;

import java.util.ArrayList;
import java.util.List;

public class IRole extends ApiObject {
	private static final long serialVersionUID = 1L;
	
	private String name;
	private String type;
	private String description;
	private String alias;
	private String condition;
	private List<String> permissions;
	private ApiProxy scope;
	private boolean canonical;
	private ApiProxy group;
	private List<ApiProxy> groups;
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String name) {
		this.type = name;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

	public List<String> getPermissions() {
		return permissions;
	}

	public void setPermissions(List<String> permissions) {
		this.permissions = permissions;
	}
	
	public void addPermission(String permission) {
		if (permissions == null) permissions = new ArrayList<String>();			
		permissions.add(permission);
	}

	public ApiProxy getScope() {
		return scope;
	}

	public void setScope(ApiProxy scope) {
		this.scope = scope;
	}
	

	public ApiProxy getGroup() {
		return group;
	}

	public void setGroup(ApiProxy group) {
		this.group = group;
	}

	public List<ApiProxy> getGroups() {
		return groups;
	}

	public void setGroups(List<ApiProxy> groups) {
		this.groups = groups;
	}
	
	public void addGroup(ApiProxy group) {
		if (groups == null) groups = new ArrayList<ApiProxy>();			
		groups.add(group);
	}

	public boolean isCanonical() {
		return canonical;
	}

	public void setCanonical(boolean canonical) {
		this.canonical = canonical;
	}
}