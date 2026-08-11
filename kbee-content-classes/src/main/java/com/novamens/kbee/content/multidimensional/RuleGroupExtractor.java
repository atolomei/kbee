package com.novamens.kbee.content.multidimensional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.util.Assert;

import com.novamens.content.base.SecurityRule;
import com.novamens.indexer.java.Extractor;
import com.novamens.indexer.service.IndexerException;
import com.novamens.security.acl.Acl;
import com.novamens.security.acl.AclEntry;
import com.novamens.security.acl.Group;

public class RuleGroupExtractor implements Extractor {
	
	public RuleGroupExtractor() {
	}
	
	public Object extract(Object object) throws IndexerException  {
		Assert.isInstanceOf(SecurityRule.class, object);
		List<String> members = new ArrayList<String>();
		Acl acl = (Acl)((SecurityRule)object).getAcl();
		if (acl==null) return members;
		for(AclEntry entry : acl.getEntries()) {
			if (entry.getPrincipal() instanceof Group) {
				members.add(getPath((Group)entry.getPrincipal()));
			}
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
}