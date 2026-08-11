package com.novamens.kbee.content.multidimensional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.novamens.content.entity.Person;
import com.novamens.content.user.UserProfile;
import com.novamens.indexer.java.Extractor;
import com.novamens.indexer.service.IndexerException;
import com.novamens.security.User;
import com.novamens.security.acl.Group;

public class GroupsExtractor implements Extractor {

	public Object extract(Object object) throws IndexerException  {
		List<String> members = new ArrayList<String>();
		if (!(object instanceof Person)) return members; 
		Person person = (Person)object;
		UserProfile profile = person.getProfile(UserProfile.class);
		if (profile==null) return members;
		User user = profile.getUser();
		if (user==null) return members;
		for (Group group : user.getGroups()) {
			group.getName();
			for (Group parent : getParents(group)) {
				members.add(getPath(parent));
			}
			members.add(getPath(group));
		}
		return members;
	}
	
	private String getPath(Group group) {
		String path = String.valueOf(group.getId());
		Set<Group> parents = group.getGroups();
		if (parents.isEmpty()) return path;
		Group parent = parents.iterator().next();
		while (parent!=null) {
			String parentid = String.valueOf(parent.getId());
			if (!path.contains(parentid)) {
				path = parentid + "/" + path;
				parents = parent.getGroups();
				parent = !parents.isEmpty() ? parents.iterator().next() : null;
			}
			else
				parent = null;
		}
		return path;
	}
	
	private List<Group> getParents(Group group) {
		return getParents(group, new ArrayList<Group>());
	}
	
	private List<Group> getParents(Group group, List<Group> parents) {
		for (Group parent : group.getGroups()) {
			if (!parents.contains(parent)) {
				parents.add(parent);
				parents.addAll(getParents(parent, parents));
			}
		}
		return parents;
	}
}
