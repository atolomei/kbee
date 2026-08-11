package kbee.replica;

import java.util.Locale;
import java.util.ResourceBundle;

import com.novamens.security.PersistentEnum;

public enum ReplicaType implements PersistentEnum {
	
	MASTER	(1, "MASTER"), 
	STANDBY	(2, "STANDBY"),
	LOCAL	(3, "LOCAL");
		
	private String label;
	private int id;
	
	private ReplicaType(int code, String label) {
		this.label = label;
		this.id = code; 
	}
	
	public String toString() {
		return ("id: " + getId() + ". label: "+ getLabel());
	}

	public String getDisplayName() {
		return getLabel();
	}
	
	public String getLabel() {
		return getLabel(Locale.getDefault());
	}
	
	public String getLabel(Locale locale) {
		ResourceBundle res = ResourceBundle.getBundle(ReplicaType.this.getClass().getName(), locale);
		return res.getString(this.label);
	}
	
	public int getId() {
		return id;
	}
}