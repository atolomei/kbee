package com.novamens.kbee.content.webapi.type;


import java.util.Enumeration;

import com.novamens.kbee.security.acl.KbeeAcl;
import com.novamens.security.acl.Acl;
import com.novamens.security.acl.AclEntry;
import com.novamens.security.acl.Group;
import com.novamens.security.acl.Permission;

import kbee.api.model.ApiProxy;
import kbee.api.model.IAcl;
import kbee.api.model.IAclEntry;

public class IAclAdapter implements Adapter<Acl, IAcl> {
	
	public IAclAdapter() {
	}
	
	public IAcl adapt(Acl acl) {
		
		IAcl iacl = new IAcl();
		
		KbeeAcl kbeeacl = (KbeeAcl)acl;
		
		iacl.setId(String.valueOf(kbeeacl.getId()));
		iacl.setLastModifiedDate(kbeeacl.getLastModifiedOffsetDateTime());
		
		for (AclEntry entry : kbeeacl.getEntries()) {
			IAclEntry ientry = new IAclEntry();
			ientry.setNegative(entry.isNegative());
			if (entry.getPrincipal() instanceof Group) {
				ientry.setPrincipal(new IGroupProxy((Group)entry.getPrincipal()));
			}
			else {
				ientry.setPrincipal(new ApiProxy("principal", UriHelper.getUri(entry.getPrincipal())));
			}
			iacl.addEntry(ientry);
			Enumeration<Permission> permissions = entry.permissions();
			while (permissions.hasMoreElements()) {
				ientry.addPermission(permissions.nextElement().toString());
			}
		}
		
		return iacl;	
	}
}
