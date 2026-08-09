package com.novamens.workflow;

import java.util.Locale;
import java.util.ResourceBundle;

public enum TriggerType {
	
	AUTOMATIC			 	("automatic", "Automatic"), 
	TIMER 					("timer", "Timer"), 
	MANUAL 					("manual", "Manual (Pending)"), 
	MANUAL_LASTUSER			("manual-lastuser", "Manual (Pending) or Last User"), 
	OLDUSERAUTOMATIC		("userautomaic", "User Automatic"), 
	USERAUTOMATIC			("userautomaic", "Round Robin"), 
	USERAUTOMATIC_LASTUSER	("userautomaic-lastuser", "Round Robin or Last User"), 
	USERAUTOMATIC_ROLE		("userautomaic-role", "Round Robin and Role or Round Robin"), 
	ROLE					("role", "Workflow-Profile"), 
	COLLABORATOR			("collaborator", "Collaborator (User Selector)"); 
	
	private String label;
	private String id;
	
	private TriggerType(String id, String label) {
		this.label = label;
		this.id = id;
	}
	
	public String toString() {
		return ("id: " + getId() + "  label: "+ getLabel());
	}
	
	public String getLabel() {
		return getLabel( Locale.getDefault());
	}
	
	public String getLabel(Locale locale) {
		ResourceBundle res = ResourceBundle.getBundle(TriggerType.this.getClass().getName(), locale);
		return res.getString(this.id);
	}
	
	public String getId() {
		return id;
	}
	
	public static TriggerType fromString(String value) {
		if (value != null) {
			for (TriggerType t : TriggerType.values()) {
				if (t.getLabel().equalsIgnoreCase(value)) {
					return t;
				}
			}
		}
		return null;
	}
}
