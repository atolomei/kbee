package com.novamens.logging;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.ContentId;
import com.novamens.content.model.ObjectId;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

@Entity
@DiscriminatorValue("AssignEvent")
public class AssignationEvent extends ContentEvent {
 
	static public String getClassEventType() {
		return "Assign";
	}
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = KbeeUser.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "EVENT_USER_TO", updatable=false)
	private User user_to;

	public AssignationEvent() {
	}
	

	public AssignationEvent(Content content, User user, String note) {
		super(content);
		setContent(content);
		StringBuilder str = new StringBuilder();
		setUserTo(user);
		str.append("To: " + user.getFirstLastName());
		if (note!=null)
			str.append(" | "+ note);
		setParameters(str.toString());
		setEventUser(ServiceLocator.getService(SecurityService.class).getSessionUser());
	}
	
	@Override
	public String getEventType() {
		return getClassEventType(); 
	}
	
	public void setUserTo(User user) {
		this.user_to=user;
	}
	
	public User getUserTo() {
		return user_to;
	}
	
	@Override
	public Object getContent() {
		ObjectId oid = new ObjectId(this.getObjectId());
		ContentId cid = new ContentId(oid.getClassName(), oid.getId());
		Content content = getContentDao().findContentById(cid);
		return  content;
	}
	
	@Override
	public String getDescription() {
		String parameters = super.getDescription();
		int pipe = parameters.indexOf("|");
		String description = pipe>0 ? parameters.substring(pipe+1) : "";
		return description;
	}
	
	@Override
	public String getAction() {
		String action = getEventType();
		String parameters = super.getDescription();
		int pipe = parameters.indexOf("|");
		action += " To " + parameters.substring(3, pipe>0 ? pipe : parameters.length()) ; 
		return action;
	}
	
	private ContentDao getContentDao() {
		 BeansService beans = ServiceLocator.getService(BeansService.class);
		 return (ContentDao) beans.getBean("contentDao");
	}
}
