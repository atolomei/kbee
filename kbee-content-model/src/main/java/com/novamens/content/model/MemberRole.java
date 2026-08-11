package com.novamens.content.model;

import com.novamens.content.base.SecurityRule;
import com.novamens.content.security.Role;

public interface MemberRole {
	public Role getRole();
	public SecurityRule getSecurityRule();
	public EntityMember getEntity();
}
