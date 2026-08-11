package com.novamens.content.base;

import com.novamens.security.acl.Acl;
import com.novamens.security.audit.AuditSet;

public interface SecurityRule extends Rule {
	
	static public final int RULE_COLLOQUIAL_IQL = 1;
	static public final int RULE_WIZARD_IQL 	= 2;
	static public final int RULE_SECURED_MEMBER	= 3;

	public int getType();
	public Acl getAcl();
	
	public void setType(int type);
	/** 
	 * Parent Object is the {@link Resource} 
	 * or other Information Object from  which this Rule is derived
	 */ 
	public String getParentObjectId();
	public void setParentObjectId(String objectid);

	public default AuditSet getAuditSet() {
		return AuditSet.SECURITY;
	}	 
}