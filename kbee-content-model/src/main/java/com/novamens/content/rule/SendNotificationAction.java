package com.novamens.content.rule;

import java.util.List;

import com.novamens.content.entity.Person;
import com.novamens.content.security.Role;

public interface SendNotificationAction extends Action {

	public void setText(String template);
	public void setSubtitle(String subject);

	public String getText();
	public String getSubtitle();

	/** roles that receive the notification */
	public Role getRole();
	public void setRole(Role role);
	
	/** list of users that receive the notification */
	public List<Person> getNotifyPersonList();
	public void setNotifyPersonList(List<Person> list);

}
