package com.novamens.content.userlist;

import java.util.List;

import com.novamens.content.query.SavedQuery;
import com.novamens.dom.Object;
import com.novamens.portal6.model.Site;
import com.novamens.service.ObjectService;

public interface UserListService extends ObjectService {
	
	 /** UserLists */
	 public List<UserList> getUserLists(String console);
	 public List<UserList> getUserLists(Site site);
	 
	 public List<UserList> getUserLists(String console, com.novamens.dom.Object object);
	 public List<UserList> getUserLists(Site site, Object object); 

	 public List<UserList> getUserLists();
	 
	 public void  deleteAllLists(String console);
	 public void  deleteAllLists(Site site);
	 
	 public void save(UserList list);
	 public void delete(UserList list);
	 public void deleteAllItems(UserList list);

  
	 /** SavedQueries */
	public void save(SavedQuery query);
	public void delete(SavedQuery savedQuery);
	public void emptySavedQueriesList(String console);
	public void removeItem(UserList list, UserListItem useritem);
	public void updateItem(UserList list, UserListItem useritem);
	public List<SavedQuery> getSavedQueries(String console);
	public List<SavedQuery> getSavedQueries(Site site);
	
	
	
	
	
}


