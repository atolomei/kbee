package com.novamens.kbee.content.webapi.type;

import com.novamens.content.model.LauncherGroup;
import com.novamens.kbee.content.model.KbeeLauncherGroup;

import kbee.api.model.ILauncherGroup;

public class ILauncherGroupAdapter implements Adapter<LauncherGroup, ILauncherGroup> {
	
	public ILauncherGroupAdapter() {
	}
	
	public ILauncherGroup adapt(LauncherGroup group) {
		ILauncherGroup igroup = new ILauncherGroup();
		igroup.setId(String.valueOf(((KbeeLauncherGroup)group).getId()));
		igroup.setName(group.getAlias());
		igroup.setDisplayName(group.getDisplayName());
		igroup.setLastModifiedDate(((KbeeLauncherGroup)group).getLastModifiedOffsetDateTime());
		igroup.setLastModifiedUser(new ApiUserProxy(((KbeeLauncherGroup)group).getLastModifiedUser()));
		igroup.setDomain(((KbeeLauncherGroup)group).getDomain().getName());
		igroup.setState(((KbeeLauncherGroup)group).getState().name());
		return igroup;	
	}
}