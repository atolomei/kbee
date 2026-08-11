package com.novamens.kbee.content.multidimensional;

import com.novamens.content.base.Content;
import com.novamens.indexer.java.Extractor;
import com.novamens.indexer.service.IndexerException;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

public class WorkspaceNameExtractor implements Extractor {

	public Object extract(Object object) throws IndexerException  {
		if (!(object instanceof Content)) return null;
		
		Content content = (Content)object;
		
		Long workspace = content.getWorkspace();
		
		if (workspace==null) 
			return null;
		
		User user = ServiceLocator.getService(SecurityService.class).findUserById(workspace);
		
		if (user==null) 
			return null;
		
		return  user.getLastFirstName();
	}
}
