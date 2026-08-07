package com.novamens.security.acl;
 
import java.util.Enumeration;
import java.util.Locale;
import java.util.Set;

import com.novamens.security.Principal;

public interface Group extends Principal {
	
	public void setName(String name);
	public Set<Group> getGroups();
	public void setGroups(Set<Group> groups);
	
	public boolean isCanonical();
	public boolean isEmpty();
	public String getDescription();
	public void setDescription(String description);
	public int numMembers();
	public void setDerived(boolean derived);
	public boolean isDerived();
	
	public boolean isEnabled();
	public void setEnabled(boolean enabled);
	
	
	public boolean isOnlyPortal();
	public boolean isOnlyDomainKbee();
	public boolean isOnlyInternalUse();

	public void setOnlyPortal(boolean b);
	public void setOnlyDomainKbee(boolean b);
	public void setOnlyInternalUse(boolean b);
	String getAreaCode();
	public String getDisplayName(Locale locale);
	
   public boolean addMember(Principal user);
   public boolean removeMember(Principal user);
   public boolean isMember(Principal member);
   public Enumeration<? extends Principal> members();
}