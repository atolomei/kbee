package com.novamens.kbee.content.multidimensional;

import java.util.ArrayList;
import java.util.List;

import com.novamens.content.entity.Person;
import com.novamens.content.user.UserProfile;
import com.novamens.indexer.java.Extractor;
import com.novamens.indexer.service.IndexerException;
import com.novamens.security.User;

public class UserIdExtractor implements Extractor {

	public Object extract(Object object) throws IndexerException  {
		List<String> members = new ArrayList<String>();
		if (!(object instanceof Person)) return members; 
		Person person = (Person)object;
		UserProfile profile = person.getProfile(UserProfile.class);
		if (profile==null) return members;
		User user = profile.getUser();
		if (user==null) return members;
		members.add(String.valueOf(user.getId()));
		return members;
	}
}
