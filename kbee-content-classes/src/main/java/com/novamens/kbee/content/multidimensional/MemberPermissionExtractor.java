package com.novamens.kbee.content.multidimensional;

import java.util.ArrayList;
import java.util.List;

import com.novamens.content.model.SecuredMember;
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

public class MemberPermissionExtractor implements Extractor {
	
	private KbeePermission permission;
	
	public MemberPermissionExtractor(KbeePermission permissiom) {
		this.permission = permissiom;
	}

	public Object extract(Object object) throws IndexerException  {
		
		if (!(object instanceof SecuredMember)) return null;
		
		List<String> readersids = new ArrayList<String>();
		List<Principal> readers = new ArrayList<Principal>();

		Acl acl = ServiceLocator.getService(ContentSystemSecurityService.class).getAcl((SecuredMember)object);
		
		for (AclEntry entry : ((KbeeAcl)acl).getEntries()) {
			if (entry.checkPermission(permission) && !entry.isNegative()) {
				readers.add((Principal)entry.getPrincipal());
			}
		}
		
		for (AclEntry entry : ((KbeeAcl)acl).getEntries()) {
			if (entry.checkPermission(permission) && entry.isNegative()) {
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
