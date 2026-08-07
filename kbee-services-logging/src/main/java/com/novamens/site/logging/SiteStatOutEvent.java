package com.novamens.site.logging;

public class SiteStatOutEvent extends SiteStatEvent {

	public Long view_id;
	public String  view_type;
	
	public Long view_site_id;
	public Long view_content_id;
	public String view_link;
	
	public Long block_id;
	public String block_title;
	public String view_title;
	
	public SiteStatOutEvent() {
	}

	@Override
	public String getAction() {
		return "SiteStatEvent";
	}
	
	
}
