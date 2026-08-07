package com.novamens.site.logging;

import java.time.OffsetDateTime;

import com.novamens.event.LogEvent;
import com.novamens.security.User;
import com.novamens.security.audit.AuditSet;

 
public class SiteStatEvent implements LogEvent {

	public Long  site_id; 
	public String site_title;
	
	public Long page_id;
	public String page_title;

	public String page_type;

	public Long user_id;
	public String user_name;
	public OffsetDateTime timestamp;
	public Long domain_id;
	
	
	public SiteStatEvent() {
	}

	@Override
	public String getType() {
		return "Site Stat";
	}

	
	@Override
	public Long getId() {
		return null;
	}

	@Override
	public String getTitle() {
		return null;
	}
	
	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public User getEventUser() {
		return null;
	}

	@Override
	public OffsetDateTime getTime() {
		return null;
	}

	@Override
	public String getEventType() {
		return null;
	}

	@Override
	public String getAction() {
		return "SiteStatEvent";
	}
	
	@Override
	public String getTarget() {
		return getObjectClass() + " - Site: "  + (site_id!=null?site_id.toString():"[null] Page: ") + (page_id!=null?page_id.toString():"| [null]");
	}
	
	/**
	 * DataSet, Classifier, Content Class
	 */
	@Override
	public String getObjectClass() {
		return "SiteStat";
	}
	
	@Override
	public String toString() {
		return  getAction()  + ". " + getTarget();
	}
	
	@Override
	public String getDisplayName() {
		return this.getClass().getName();
	}

	@Override
	public String getParameters() {
		return null;
	}

	@Override
	public AuditSet getAuditSet() {
		return AuditSet.PORTAL;
	}

	@Override
	public Long getAuditResourceKBFileId() {
		return null;
	}

	@Override
	public boolean isSilentMode() {
		return false;
	}
	
	@Override
	public boolean isNotifiable() {
		return false;
	}
}
