package com.novamens.logging;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.ContentId;
import com.novamens.content.model.ObjectId;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

@Entity
@DiscriminatorValue("CheckinEvent")
public class CheckinEvent extends ContentEvent {
	
	static public String getClassEventType() {
		return "Checkin";
	}
	
	public CheckinEvent() {
		 
	}
	public CheckinEvent(Content content){
		this(content, false);
	}
	
	public CheckinEvent(Content content, boolean silentMode ) {
		super();
		setContent(content);
		setEventUser(ServiceLocator.getService(SecurityService.class).getSessionUser());
		setSilentMode(silentMode);
	}
	
	@Override
	public String getEventType() {
		return  getClassEventType();
	}
	
	@Override
	public Object getContent() {
		ObjectId oid = new ObjectId(this.getObjectId());
		ContentId cid = new ContentId(oid.getClassName(), oid.getId());
		Content content = getContentDao().findContentById(cid);
		return  content;
	}
	
	@Override
	public String getAction() {
		return getEventType();
	}


	@Override
	public String getType() {
		return "Content";
	}
	
	@Override
	public String getObjectClass() {
		return "Content"; // o lo que sea !!!  VER
	}
	
	@Override
	public boolean isNotifiable() {
		return true;
	}
	
	private ContentDao getContentDao() {
		 BeansService beans = ServiceLocator.getService(BeansService.class);
		 return (ContentDao) beans.getBean("contentDao");
	}
}