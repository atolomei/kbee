package com.novamens.kbee.security.acl;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import com.novamens.security.acl.Permission;

public class KbeePermission implements Permission, Serializable {
	private static final long serialVersionUID = 1L;
					
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeePermission.class.getName());

	private String name;
	private String label;
	private String action;
	private String actionLabel;
	private Boolean is_canonical = Boolean.valueOf(false);
	
	private static Map<String, KbeePermission>  Permissions = Collections.synchronizedMap(new HashMap<String, KbeePermission>());
	
	static final public KbeePermission READ 	 = new KbeePermission("read", "Read", Boolean.valueOf(true));
	static final public KbeePermission WRITE 	 = new KbeePermission("write", "Write", Boolean.valueOf(true));
	static final public KbeePermission DELETE 	 = new KbeePermission("delete", "Delete", Boolean.valueOf(true));
	static final public KbeePermission MONITOR 	 = new KbeePermission("monitor", "Reassign/Cancel", Boolean.valueOf(true));
	static final public KbeePermission TERMINATE = new KbeePermission("terminate", "Force Termination", Boolean.valueOf(true));
	static final public KbeePermission CREATE 	 = new KbeePermission("create", "Create", Boolean.valueOf(true));
	static final public KbeePermission PRIVATE 	 = new KbeePermission("private", "Private Notes R/W", Boolean.valueOf(true));
	static final public KbeePermission CHILDS 	 = new KbeePermission("childs", "Create Childs", Boolean.valueOf(true));
	static final public KbeePermission AUDIT_LOG = new KbeePermission("audit", "Access to full Audit logs", Boolean.valueOf(true));

	public KbeePermission(String name) {
		this.name = name;
		this.label = name;
		this.is_canonical=Boolean.valueOf(false);
	}
										
	public KbeePermission(String name, String label) {
		this.name = name;
		this.label = label;
		this.is_canonical=Boolean.valueOf(false);
	}
	
	public KbeePermission(String name, String label, Boolean iscanonical) {
		this.name = name;
		this.label = label;
		this.is_canonical=iscanonical;
	}
	
	public String toString() {
		return name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}
	
	public String getLabel() {
		return getLabel(Locale.getDefault());
	}
	
	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}
	
	public String getActionLabel() {
		return actionLabel;
	}

	public void setActionLabel(String actionLabel) {
		this.actionLabel = actionLabel;
	}

	public boolean isCanonical() {
		return is_canonical.booleanValue();
	}
	
	public String getLabel(Locale locale) {
		if (!isCanonical()) {
			return label; 
		}
		ResourceBundle res = ResourceBundle.getBundle(KbeePermission.class.getName(),locale);
		try {
			return res.getString(this.name);
		} 
		catch (MissingResourceException e) {
			logger.error("Permission " + label + "  lang: " + locale.getLanguage() + " is missing");
			return label;
		}
	}
	
	
	@Override
	public boolean equals(Object object) {
		if (!(object instanceof KbeePermission)) return false;
		return ((Permission)object).toString().equals(toString());
	}
	
	public static KbeePermission valueOf(String name) {
		if (Permissions.isEmpty()) {
			synchronized (Permissions) {
				Permissions.put(READ.toString().toLowerCase(), READ);
				Permissions.put(WRITE.toString().toLowerCase(), WRITE);
				Permissions.put(DELETE.toString().toLowerCase(), DELETE);
				Permissions.put(MONITOR.toString().toLowerCase(), MONITOR);
				Permissions.put(PRIVATE.toString().toLowerCase(), PRIVATE);
				Permissions.put(CREATE.toString().toLowerCase(), CREATE);
				Permissions.put(AUDIT_LOG.toString().toLowerCase(), AUDIT_LOG);
			}
		}
		KbeePermission permission = Permissions.get(name);
		if (permission == null) {
			synchronized (Permissions) {
				permission = Permissions.get(name);
				if (permission == null) {
					permission = new KbeePermission(name);
					Permissions.put(name, permission);
				}
			}
		}
		return permission;	
	}
}
