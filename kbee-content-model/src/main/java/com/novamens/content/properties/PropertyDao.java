package com.novamens.content.properties;

import java.io.Serializable;
import java.util.List;

import com.novamens.content.base.Content;
import com.novamens.content.query.SavedQuery;
import com.novamens.content.userlist.UserList;
import com.novamens.content.userlist.UserListItem;
import com.novamens.dao.Dao;
import com.novamens.dom.Object;
import com.novamens.portal6.model.Site;
import com.novamens.security.User;

public interface PropertyDao extends Dao {

	/**
	 * Properties
	 */
	public void save(Property property);
	public void delete(Property property);
	public Property reload(Property property);
	public List<Property> findPropertiesByContent(Content content);
	public List<Property> findPropertiesByUser(User user);
	public List<Property> findPropertiesByUser(User user, String set);
	public List<Property> findPropertiesByObject(Object object);
	
	public List<Content> findContentByProperty(String value);
	/**
	 * UserList
	 */
	public List<UserList> getUserLists(User user);
	public List<UserList> getUserLists(User user, String console);
	
	public void save(UserList list);
	public void delete(UserList list);
	public UserList reload(UserList list);

	public void save(UserListItem item);
	public void delete(UserListItem item);
	public void deleteAllItems(UserList list);
	
	public List<UserList> getUserLists(User user, String console, com.novamens.dom.Object object);
	
	public UserList findUserListById(Serializable id);
	/**
	 * User List Items
	 */
	public List<UserListItem> getContentUserListItem(Serializable content_oid);
	public List<UserListItem> getContentUserListItem(Serializable content_oid, Serializable user_id, String console);
	public List<UserListItem> getMemberUserListItem(Serializable cid);
	
	
	/**
	 * SavedQuewies
	 */
	public List<SavedQuery> getSavedQueries(User user, String console);
	public void save(SavedQuery sq);
	public void delete(SavedQuery sq);
	public UserList getUserList(Serializable listid);
	public long getTotalListConsole(User user, String con);
	List<Property> findPropertiesByUser(User user, String set, int maxItems);
	
	public List<SavedQuery> getSavedQueries(User user, Site site);
	public List<UserList> getUserLists(User user, Site site);
	public List<UserList> getUserLists(User user, Site site, Object object);
	
	
	
	
	
}