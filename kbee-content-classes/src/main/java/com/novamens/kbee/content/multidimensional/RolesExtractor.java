package com.novamens.kbee.content.multidimensional;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.novamens.content.entity.Person;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserRole;
import com.novamens.indexer.java.Extractor;
import com.novamens.indexer.service.IndexerException;

public class RolesExtractor implements Extractor {
			
	static private Logger logger = LogManager.getLogger(RolesExtractor.class.getName());
	
	public Object extract(Object object) throws IndexerException  {
		List<String> members = new ArrayList<String>();
		Person person;
		try {
			if (!(object instanceof Person)) 
				return members;
			person = (Person)object;
			UserProfile profile = person.getProfile(UserProfile.class);
			if (profile==null || profile.getRoles()==null) 
				return members;
			for (UserRole userrole: profile.getRoles()) {
				members.add(String.valueOf(userrole.getRole().getId()));
			}
		} 
		catch (Exception e) {
			logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName() + " | " + e.getMessage());
			throw new IndexerException (e);
		}
		return members;
		
	}
}
