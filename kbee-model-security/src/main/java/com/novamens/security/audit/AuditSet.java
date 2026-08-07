package com.novamens.security.audit;

import java.util.Locale;
import java.util.ResourceBundle;

import com.novamens.security.PersistentEnum;


/**
 * 
 * 
 * EmptyRecycleBin
 * 
 *  DomainAdmin
 *  
 * 
 */
public enum AuditSet implements PersistentEnum {

	CONTENT 			(10, "content"), 
	RESOURCE 			(12, "resource"),
	TREEFILE 			(14, "treefile"),
	SECURITY 			(20, "security"), 
	ENTITY	 			(25, "security"), 
	DATASET_VALUE		(30, "datasetvalue"),
	MODEL 				(40, "model"),
	DOMAIN_ADMIN		(50, "domainadmin"),
	PORTAL				(60, "portal"),
	EMAIL				(70, "email"),
	SYSTEM				(80, "system"),
	AUTHENTICATION		(90, "authentication"),
	REPORT				(95, "report"),
	SUPPORT				(100, "support"),
	GENERAL				(0, "general");
	
	
	private String label;
	private int id;
	
				
	private AuditSet(int code, String label) {
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
		ResourceBundle res = ResourceBundle.getBundle(AuditSet.this.getClass().getName(), locale);
		return res.getString(this.label);
	}
	
	public int getId() {
		return id;
	}

	public String getHTMLLabel(Locale locale) {
		ResourceBundle res = ResourceBundle.getBundle(AuditSet.this.getClass().getName(), locale);
		return  "<span class=\"" + getLabel() + "\">" + res.getString(this.label) + "</span>";
	}
	
}
