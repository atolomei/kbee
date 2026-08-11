package com.novamens.kbee.content.multidimensional;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.Assert;

import com.novamens.content.base.Content;
import com.novamens.content.security.ContentSystemSecurityService;
import com.novamens.indexer.java.Extractor;
import com.novamens.indexer.service.IndexerException;
import com.novamens.kbee.security.acl.KbeeAcl;
import com.novamens.kbee.security.acl.KbeePermission;
import com.novamens.security.Principal;
import com.novamens.security.acl.Acl;
import com.novamens.security.acl.AclEntry;
import com.novamens.security.acl.Group;
import com.novamens.service.ServiceLocator;

public class ReaderExtractor implements Extractor {

	public Object extract(Object object) throws IndexerException  {
		Assert.isInstanceOf(Content.class, object);
		
		List<String> readersids = new ArrayList<String>();
		List<Principal> readers = new ArrayList<Principal>();

		Acl acl = ServiceLocator.getService(ContentSystemSecurityService.class).getAcl((Content)object);
		
		for (AclEntry entry : ((KbeeAcl)acl).getEntries()) {
			if (entry.checkPermission(KbeePermission.READ) && !entry.isNegative()) {
				readers.add((Principal)entry.getPrincipal());
			}
		}
		
		for (AclEntry entry : ((KbeeAcl)acl).getEntries()) {
			if (entry.checkPermission(KbeePermission.READ) && entry.isNegative()) {
				Principal principal = (Principal)entry.getPrincipal();
				if (principal instanceof Group) {
					Group group = (Group)principal;
					for (Principal reader : readers) {
						if (group.isMember(reader)) {
							readers.remove(reader);
							break;
						}
					}
				}
			}
		}
		
		for (Principal reader : readers) {
			readersids.add(String.valueOf((Long)reader.getId()));
		}
		
		return readersids;
	}
}
