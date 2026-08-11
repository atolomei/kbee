package com.novamens.content.user;

import com.novamens.content.model.Label;
import com.novamens.security.User;

public interface UserLabel extends Label {
	
	static public final String CSS[] = {	
			"Purple",
			"Blue",
			"Green",
			"Yellow",
			"Orange",
			"Red",
			"Pink",
			"Brown",
			"Gray",
			"Light-blue"
			};
	
	public void setUser(User user);
	public User getUser();
	public String getContext();
	public void setContext(String ct);

}
