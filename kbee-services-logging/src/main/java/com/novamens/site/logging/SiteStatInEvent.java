package com.novamens.site.logging;


/**
 * 
 *
 */
public class SiteStatInEvent extends SiteStatEvent {

	
	public String objectId;
	public String contentId;         // ContentId()
	public Long content_long_id;     // Content id (long)
	public String OId;               // Content Oid (long)
	public Integer content_version;   // Content version
	
	public String content_title;
	public String src;
	public String sessionId;
	public Long   render_milisecs;
	public String user_agent;
	
	
	
	public SiteStatInEvent() {
	}


	public String toString() {
		
		StringBuilder str = new StringBuilder();
		
		str.append(super.toString());
			
		if (contentId!=null) {
			str.append(" | ");
			str.append(contentId);
		}

		if (src!=null) {
			str.append(" | ");
			str.append(src);
		}
		
		
		return str.toString();
		
		
	}
	
	
	@Override
	public String getAction() {
		return "SiteStatInEvent";
	}
}
