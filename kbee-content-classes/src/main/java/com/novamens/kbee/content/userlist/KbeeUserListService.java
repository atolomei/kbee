package com.novamens.kbee.content.userlist;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.novamens.content.properties.PropertyDao;
import com.novamens.content.query.SavedQuery;
import com.novamens.content.userlist.UserList;
import com.novamens.content.userlist.UserListItem;
import com.novamens.content.userlist.UserListService;
import com.novamens.portal6.model.Site;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

/**
 * User List service
 * 
 * 
 *
 */
public class KbeeUserListService implements UserListService {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeUserListService.class.getName());
	
	private User user = null;
	private PropertyDao dao = null;
	
	public KbeeUserListService() {
	}
	
	public KbeeUserListService(User user) {
		this.user=user;
	}
	
	@Override
	public List<UserList> getUserLists() {
		return  getPropertyDao().getUserLists(getUser());
	}
	

	@Override
	public List<UserList> getUserLists(String console) {
		return  getPropertyDao().getUserLists(getUser(), console);
	}


	@Override
	public List<UserList> getUserLists(Site site) {
		return  getPropertyDao().getUserLists(getUser(), site);
	}

	
	
	
	
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void delete(UserList list) {
		logger.debug("delete-> " + list.getTitle());
		getPropertyDao().delete(list);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void save(UserList list) {
		logger.debug("save-> " + list.getTitle());
		list.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		list.setLastModifiedUser(getSessionUser());
		((KbeeUserList) list).setTotalItems(list.getItems()!=null?list.getItems().size():0);
		for (UserListItem t: list.getItems()) {
			logger.debug((t.getTitle()!=null?t.getTitle():"null") + " " + (t.getId()!=null?t.getId().toString():"null"));
		}
		getPropertyDao().save(list);
	}
	
	public void updateItem(UserList list, UserListItem useritem) {
		getPropertyDao().save(useritem);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void removeItem(UserList list, UserListItem useritem) {
		getPropertyDao().delete(useritem);

		// esto no anduvo porque tira error Spring (y no llega al log !)
		// UserList lo=getPropertyDao().getUserList(list.getId());
		// lo.setTotalItems(total+100);
		// lo.setTitle(lo.getTitle()+ String.valueOf(total+1));
		// getPropertyDao().save(lo);
		// ((KbeeUserList) list).setTotalItems(list.getTotalItems()>0? (list.getTotalItems()-1) : 0);
		// getPropertyDao().save(list);
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void deleteAllItems(UserList list) {
		logger.debug("deleteAllItems-> " + list.getTitle());
		((KbeeUserList) list).getItems().clear();
		((KbeeUserList) list).setTotalItems(0);
		save(list);
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void  deleteAllLists(String console) {
		logger.debug("deleteAllists-> " + console);
		for (UserList list: getPropertyDao().getUserLists(getUser(),  console)) {
			delete(list);
		};
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void deleteAllLists(Site site) {
		logger.debug("deleteAllists-> " + site.getTitle());
		for (UserList list: getPropertyDao().getUserLists(getUser(),  site)) {
			delete(list);
		};
	}

	
	/**
	 * Used by grids to populate a cell with all lists 
	 * for a content + user + console 
	 */
	@Override
	public List<UserList> getUserLists(String console, com.novamens.dom.Object object) {
		return getPropertyDao().getUserLists(getUser(), console, object);
	}

	
	
	@Override
	public List<UserList> getUserLists(Site site, com.novamens.dom.Object object) {
		return getPropertyDao().getUserLists(getUser(), site, object);
	}
	
	
	/**
	 * SavedQueries
	 */
	@Override
	public List<SavedQuery> getSavedQueries(String console) {
		logger.debug("getSavedQueries -> " + console);
		return getPropertyDao().getSavedQueries(getUser(), console);
	}

	/**
	 * SavedQueries
	 */
	
	@Override
	public List<SavedQuery> getSavedQueries(Site site) {
		logger.debug("getSavedQueries Site -> " + site.getName());
		return getPropertyDao().getSavedQueries(getUser(), site);
	}
	
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void delete(SavedQuery query) {
		logger.debug("delete-> " + query.getTitle());
		getPropertyDao().delete(query);
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void emptySavedQueriesList(String console) {
		for (SavedQuery q:getSavedQueries(console)) {
			getPropertyDao().delete(q);
		}
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void save(SavedQuery query) {
		logger.debug("save-> " + query.getTitle());
		query.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		query.setLastModifiedUser(getSessionUser());
		getPropertyDao().save(query);
	}

	
	
	
	public PropertyDao getPropertyDao() {
		return this.dao;
	}
	
	public void setPropertyDao(PropertyDao dao) {
		this.dao = dao;
	}
	
	public User getUser() {
		return user;
	}

	public void  setUser(User user) {
		this.user=user;
	}
	
	
	protected User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}


	



	

	
}
