package com.novamens.kbee.content.multidimensional;

import java.util.ArrayList;
import java.util.List;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserRole;
import com.novamens.indexer.java.Extractor;
import com.novamens.indexer.service.IndexerException;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

public class ContentRoleExtractor implements Extractor {

	public Object extract(Object object) throws IndexerException  {
		List<String> members = new ArrayList<String>();
		
		if (!(object instanceof Content)) return null;
		
		Content content = (Content)object;
		
		Long workspace = content.getWorkspace();
		
		if (workspace==null) 
			return null;
		
		User user = ServiceLocator.getService(SecurityService.class).findUserById(workspace);
		
		if (user==null) return members;
		
		UserProfile userprofile = getContentDao().findUserProfileByUser(user);
		
		if (userprofile==null) return members;
		
		for (UserRole userrole : userprofile.getRoles()) {
			members.add(String.valueOf(userrole.getRole().getId()));
		}
		
		return members;
	}
	
	public ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
}
