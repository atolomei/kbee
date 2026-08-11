package com.novamens.portal.subscription;

import com.novamens.security.PersistentEnum;

public enum SiteSubscriptionEvent implements PersistentEnum {

	SITE_PUBLISH_CONTENT (1, "CONTENT PUBLISHING"); 
	
	private String label;
    private int id;
    
    private  SiteSubscriptionEvent(int code, String label) {this.label = label;this.id = code;}
    public String toString() {return ("id: " + getId() + "  label: "+ getLabel());} 
    public String getLabel() {return label;}
	
    public int getId() {return id;}

}
