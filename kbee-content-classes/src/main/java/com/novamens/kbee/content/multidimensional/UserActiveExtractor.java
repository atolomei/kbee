package com.novamens.kbee.content.multidimensional;

import java.util.ArrayList;
import java.util.List;

import com.novamens.content.entity.Person;
import com.novamens.content.user.UserProfile;
import com.novamens.indexer.java.Extractor;
import com.novamens.indexer.service.IndexerException;
import com.novamens.security.User;

public class UserActiveExtractor implements Extractor {

	public Object extract(Object object) throws IndexerException  {
		List<String> members = new ArrayList<String>();
		if (!(object instanceof Person)) return null; 
		Person person = (Person)object;
		UserProfile profile = person.getProfile(UserProfile.class);
		if (profile==null) return null;
		User user = profile.getUser();
		if (user==null) return null;
		members.add(String.valueOf(user.isActive()));
		return members;
	}
}
