package com.novamens.kbee.content.multidimensional;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.Assert;

import com.novamens.content.base.SecurityRule;
import com.novamens.indexer.java.Extractor;
import com.novamens.indexer.service.IndexerException;
import com.novamens.security.User;
import com.novamens.security.acl.Acl;
import com.novamens.security.acl.AclEntry;

public class RuleUserExtractor implements Extractor {
	
	public RuleUserExtractor() {
	}
	
	public Object extract(Object object) throws IndexerException  {
		Assert.isInstanceOf(SecurityRule.class, object);
		List<String> members = new ArrayList<String>();
		Acl acl = (Acl)((SecurityRule)object).getAcl();
		if (acl==null) return members;
		for(AclEntry entry : acl.getEntries()) {
			if (entry.getPrincipal() instanceof User) {
				String username = entry.getPrincipal().getName();
				int i = username.indexOf("@");
				if (i>0)
				username = username.substring(0,i);
				members.add(username);
			}
		}
		return members;
	}
}
