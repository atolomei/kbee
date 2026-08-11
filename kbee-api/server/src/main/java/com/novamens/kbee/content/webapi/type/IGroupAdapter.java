package com.novamens.kbee.content.webapi.type;

import com.novamens.kbee.security.acl.KbeeGroup;
import com.novamens.security.acl.Group;

import kbee.api.model.IGroup;

public class IGroupAdapter implements Adapter<Group, IGroup> {
	
	
	public IGroupAdapter() {
	}
	
	public IGroup adapt(Group group) {
		IGroup igroup = new IGroup();
		
		igroup.setId(String.valueOf(group.getId()));
		igroup.setName(group.getName());
		igroup.setCanonical(group.isCanonical());
		igroup.setArea(group.getAreaCode());
		igroup.setDomain(((KbeeGroup)group).getDomain().getName());
		igroup.setLastModifiedDate(group.getLastModifiedOffsetDateTime());
		
		return igroup;	
	}
}
