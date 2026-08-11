package com.novamens.kbee.content.userlist;

import java.util.List;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.properties.PropertyDao;
import com.novamens.content.userlist.UserList;
import com.novamens.content.userlist.UserListItem;
import com.novamens.content.userlist.UserListService;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.event.AppUpdateEvent;
import com.novamens.event.Event;
import com.novamens.event.EventListener;
import com.novamens.indexer.java.IndexTask;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.content.event.AppCheckinEvent;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.scheduler.SchedulerException;
import com.novamens.scheduler.SchedulerService;
import com.novamens.service.ServiceLocator;


/**
 *  There must be a bean that defines this listener
 * 
 *  {@code MonitorUserListQuery}
 *  {@code WorkspaceUserListQuery}
 * 
 */
public class UserListUpdateListener implements EventListener {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(UserListUpdateListener.class.getName());

	/**
	 * 
	 */
	@Override
	public boolean listen(Event event) {
		
		return (  
				((event instanceof AppCheckinEvent)  && (event.getObject() instanceof Content)) ||
				((event instanceof AppUpdateEvent)   && (event.getObject() instanceof Content) && ((AppUpdateEvent) event).isUpdateUserLists()) ||
				((event instanceof AppUpdateEvent)   && (event.getObject() instanceof DataSetMember)) 
			);
	}
	
	public void onEvent(Event event) {
		try {
			if (event.getObject() instanceof Content) {
				onContentEvent(event);
			}
			
			if (event.getObject() instanceof DataSetMember) {
				onDataSetMemberEvent(event);
			}
			
		}
		catch(Exception e) {
			logger.error(e);
		}
	}
	
	public Index getIndex() {
		return null;
	}

	

	
	/**
	 * Ver eventos de cambios de estado
	 */
	public void onDataSetMemberEvent(Event event) {
		
		Assert.isInstanceOf(DataSetMember.class, event.getObject());

		DataSetMember member = (DataSetMember)event.getObject();
		
		List<UserListItem> items = getPropertyDao().getMemberUserListItem(member.getId());
		
		for (UserListItem useritem : items) {
			reindex(useritem);
		}
	}

	/**
	 * 
	 * Ver eventos de cambios de estado
	 * 
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	public void onContentEvent(Event event) {
		
		Assert.isInstanceOf(Content.class, event.getObject());
		
		long start = System.currentTimeMillis();
		
		logger.debug("onContentEvent -> " + event.getClass().getName());
		
		
		/** this is the content being updated  */
		Content content = (Content)event.getObject();
		
		List<UserListItem> items = getPropertyDao().getContentUserListItem(content.getOId());
		
		for (UserListItem useritem : items) {

			String userItemConsole=useritem.getConsole();

			if (userItemConsole==null)
				userItemConsole="";

			//	
			// item keeps the archived version 
			// and the content is no longer in Archive
			//
			// ARCHIVED
			//
			if (userItemConsole.equals("archive")) {
				if (content.getState()!=ObjectState.ARCHIVED)
					useritem.getUserlist().removeItem(useritem);
			}

			//
			// MY TASKS
			//
			/** item references a content in MyTasks of owner */
			else if (userItemConsole.equals("mytasks"))  {
				
				if (!useritem.getOwner().getId().equals(content.getWorkspace())) { 
					UserList list = useritem.getUserlist();
					KbeeUser user = (KbeeUser) list.getOwner();
					user.getService(UserListService.class).removeItem(list, useritem);
				}
			}

			// MONITOR
			//
			else if (userItemConsole.equals("monitor"))  {
				if (content.getWorkspace()==null) {
					UserList list = useritem.getUserlist();
					KbeeUser user = (KbeeUser) list.getOwner();
					UserListService se=user.getService(UserListService.class);
					se.removeItem(list, useritem);
				}
			}
			else  {
			
				// PUBLISHED IN LIBRARIES
				if (useritem.getVersionMatch()==UserListItem.PUBLISHED) {
					
					if (content.isHeadVersion()) {
						if (!useritem.getContent().getId().equals(content.getId())) {
						
							//UserList list = useritem.getUserlist();
							//KbeeUser user = (KbeeUser) list.getOwner();
							
							((KbeeUserListItem)useritem).setContent(content);
							useritem.setLastModifiedOffsetDateTime(content.getLastModifiedOffsetDateTime());
							
							//user.getService(UserListService.class).updateItem(list, useritem);

							reindex(useritem);
						}
						else
							if (content.getState()==ObjectState.ENABLED && useritem.getLastModifiedOffsetDateTime().isBefore(content.getLastModifiedOffsetDateTime())) {
								useritem.setLastModifiedOffsetDateTime(content.getLastModifiedOffsetDateTime());
								reindex(useritem);
							}
					}
					if (content.getState()!=ObjectState.ENABLED) {
						useritem.getUserlist().removeItem(useritem);
					}
				}
				
				// Recycle bin
				else if (useritem.getVersionMatch()==UserListItem.SAVED_VERSION) {
					if (useritem.getLastModifiedOffsetDateTime().isBefore(content.getLastModifiedOffsetDateTime())) {
						useritem.setLastModifiedOffsetDateTime(content.getLastModifiedOffsetDateTime());
						//UserList list = useritem.getUserlist();
						//KbeeUser user = (KbeeUser) list.getOwner();
						((KbeeUserListItem)useritem).setContent(content);
						useritem.setLastModifiedOffsetDateTime(content.getLastModifiedOffsetDateTime());
						//UserListService se=user.getService(UserListService.class);
						//se.updateItem(list, useritem);
						reindex(useritem);
					}
				}
			}
			
			logger.debug(String.valueOf(System.currentTimeMillis()-start) + " ms -> " + content.getOId().toString() + " " + content.getTitle());
			
		}
	}

	
	
	
	
	private void reindex(UserListItem item) {
		try {
			ServiceLocator.getService(SchedulerService.class).enqueue(new IndexTask(item, getIndex(item.getDomain()), true));
		}
		catch (SchedulerException e) {
			logger.error(e);
		}
	}
	
	private Index getIndex(Domain domain) {
		return domain.getService(JavaIndexerService.class).getIndex();
	}
	
	private PropertyDao getPropertyDao() {
		return (PropertyDao)ServiceLocator.getService(BeansService.class).getBean("propertyDao");
	}
}