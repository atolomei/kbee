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
@DiscriminatorValue("SendByEmailEvent")
public class SendByEmailEvent extends ContentEvent {
 
	 

	public SendByEmailEvent() {
	}
	
	public SendByEmailEvent(Content content, String email) {
		super(content);
		setContent(content);
		setParameters(email);
		setEventUser(ServiceLocator.getService(SecurityService.class).getSessionUser());
	}

	@Override
	public String getEventType() {
		return "Email";
	}
	
	@Override
	public String getType() {
		return "Content";
	}

	@Override
	public String getAction() {
		return "Send By Email";
	}

	@Override
	public String getObjectClass() {
		return "IDoc"; // o lo que sea !!!  VER
	}
	
	@Override
	public Object getContent() {
		ObjectId oid = new ObjectId(this.getObjectId());
		ContentId cid = new ContentId(oid.getClassName(), oid.getId());
		Content content = getContentDao().findContentById(cid);
		return  content;
	}
	
	private ContentDao getContentDao() {
		 BeansService beans = ServiceLocator.getService(BeansService.class);
		 return (ContentDao) beans.getBean("contentDao");
	}
}
