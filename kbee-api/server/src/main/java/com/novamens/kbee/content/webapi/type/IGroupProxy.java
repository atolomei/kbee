package com.novamens.kbee.content.webapi.type;

import com.novamens.kbee.content.security.GroupProxy;
import com.novamens.security.acl.Group;

import kbee.api.model.ApiProxy;

public class IGroupProxy extends ApiProxy {
	private static final long serialVersionUID = 1L;
	
	public IGroupProxy(Group group) {
		group = group instanceof GroupProxy ?  ((GroupProxy)group).getGroup() : group;
		setHRef(UriHelper.getUri(group));
		setId(String.valueOf(group.getId()));
		setName(group.getName());
		setRel("group");
	}
}
