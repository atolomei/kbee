package com.novamens.content.userlist;


import java.util.List;

import com.novamens.dom.DomainObject;
import com.novamens.dom.Indexable;
import com.novamens.dom.Object;
import com.novamens.portal6.model.Site;
import com.novamens.security.User;
import com.novamens.security.audit.AuditSet;

public interface UserList extends DomainObject, Object, Indexable {
	
	public String getTitle();
	public void setTitle(String title);
	public String getDescription();
	public void setDescription(String description);
	public String getConsole(); 	
	public void setConsole(String console);
	
	public List<UserListItem> getItems();
	public int getTotalItems();
	public boolean belongs(com.novamens.dom.Object ob);
	
	public void removeItem(UserListItem item);
	public void remove(Object object);
	public void add(Object object);
	public void add(Object modelObject, int versionMatch); // UserListItem.NEWEST | PUBLISHED | SAVED_VERSION
	public User getOwner();
	public int getVersionMatch();
	
	public void setTotalItems(int total);
	public Site getSite();
	public void  setSite(Site site);
	
	public default AuditSet getAuditSet() {
		return AuditSet.SYSTEM;
	}
	
}