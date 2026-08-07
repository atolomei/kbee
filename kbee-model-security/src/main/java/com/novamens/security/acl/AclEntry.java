package com.novamens.security.acl;

import java.util.Enumeration;
import java.util.List;

import com.novamens.security.Principal;

public interface AclEntry  {
	public void setPermissions(List<Permission> permissions);
    public boolean setPrincipal(Principal user);
    public Principal getPrincipal();
    public void setNegativePermissions();
    public boolean isNegative();
    public boolean addPermission(Permission permission);
    public boolean removePermission(Permission permission);
    public boolean checkPermission(Permission permission);
    public Enumeration<Permission> permissions();
    public String toString();
    public Object clone();
}
