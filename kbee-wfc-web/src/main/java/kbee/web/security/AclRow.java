package kbee.web.security;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.wicket.model.IModel;

import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.security.acl.KbeeGroup;
import com.novamens.security.Principal;
import com.novamens.security.acl.Group;
import com.novamens.security.acl.Permission;
import com.novamens.wicket.model.ObjectModel;

public class AclRow implements Serializable {
	private static final long serialVersionUID = 1L;
	private IModel<Principal> model;
	private Map<Permission, PermissionValue> permissions = new HashMap<Permission, PermissionValue>();
	private Map<String, PermissionValue> values = new HashMap<String, PermissionValue>();
	
	public enum PermissionValue {
		GRANT("grant"),
		DENIED("deny");
		private String value;
		private PermissionValue(String value) {
			this.value = value;
		}
		public String toString() {
			return value;
		}
	}
	
	public AclRow(Principal principal) {
		if (principal!=null)
		model = new ObjectModel<Principal>(principal, principal instanceof Group ? KbeeGroup.class : KbeeUser.class);
	}
	public Principal getPrincipal() {
		return model!=null ? model.getObject() : null;
	}
	public void setValue(Permission permission, PermissionValue value) {
		permissions.put(permission, value);
		values.put(permission.toString(), value);
	}
	public void remove(Permission permission) {
		permissions.remove(permission);
		values.remove(permission.toString());
	}
	public PermissionValue getValue(Permission permission) {
		return values.get(permission.toString());
	}
	public boolean denied() {
		return values.values().contains(PermissionValue.DENIED);
	}
	public boolean grants() {
		return values.values().contains(PermissionValue.GRANT);
	}
	public Set<Permission> getPermissions() {
		return permissions.keySet();
	}
	public void detach() {
		if (model!=null)
		model.detach();
	}
}