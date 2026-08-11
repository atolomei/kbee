package com.novamens.content.model;


import com.novamens.content.base.SecurityRule;
import com.novamens.security.acl.Acl;

public interface SecuredMember extends DataSetMember {
	public SecurityRule getSecurityRule();
	public Acl getInheritedAcl();
}