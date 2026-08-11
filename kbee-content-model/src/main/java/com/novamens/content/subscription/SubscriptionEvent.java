package com.novamens.content.subscription;

import com.novamens.security.PersistentEnum;

/**
 *  <p>Content Events that users can subscribe to.
 *  They are managed by the {@link SubscriptionService}
 *  and {@link SubscriptionDao}</p>
 *
 *
	SITE_PUBLISH_CONTENT 	(6, "SITE PUBLISH CONTENT")
 */
public enum SubscriptionEvent implements PersistentEnum {
	
	UPDATE_CONTENT			(1, "Update"), 
	DELETE_CONTENT 			(2, "Delete"),
	REPORT_CONTENT 			(3, "Report"),
	VOTE_CONTENT 			(4, "Vote"),
	COMMENT_CONTENT 		(5, "Comment"); 
	
	private String label;
    private int id;
    
    private  SubscriptionEvent(int code, String label) {this.label = label;this.id = code;}
    public String toString() {return ("id: " + getId() + "  label: "+ getLabel());} 
    public String getLabel() {return label;}
    public String getAction() {return label;}
    public String getEventType() {return label;}
    
    public int getId() {return id;}
	
}
