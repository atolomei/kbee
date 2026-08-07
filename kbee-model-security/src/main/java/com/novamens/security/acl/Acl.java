package com.novamens.security.acl;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.List;

import com.novamens.security.Auditable;
import com.novamens.security.Principal;
 
public interface Acl extends Auditable {
	public Serializable getId();
	public List<AclEntry> getEntries();
    public void setName(Principal caller, String name)    throws SecurityException;
    	    public String getName();
    	    public boolean addEntry(Principal caller, AclEntry entry)
    	      throws SecurityException;
    	    public boolean removeEntry(Principal caller, AclEntry entry)
    	          throws SecurityException;
    	    public Enumeration<Permission> getPermissions(Principal user);
    	    public Enumeration<AclEntry> entries();
    	    public boolean checkPermission(Principal principal, Permission permission);
    	    public String toString();
}
