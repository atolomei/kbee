package com.novamens.kbee.content.model;

import com.novamens.content.entity.Person;
import com.novamens.content.user.UserProfile;
import com.novamens.event.Event;
import com.novamens.event.EventListener;
import com.novamens.event.AppUpdateEvent;
import com.novamens.hibernate.event.HibernateUpdateEvent;
import com.novamens.indexer.java.BatchIndexTaskServiceRequest;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.scheduler.SchedulerException;
import com.novamens.scheduler.SchedulerService;
import com.novamens.security.User;
import com.novamens.service.ServiceLocator;
import com.novamens.solr.indexer.query.SolrParametersQuery;

public class UserUpdateListener implements EventListener {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(UserUpdateListener.class.getName());
	
	
	public boolean listen(Event event) {
		return ((event instanceof AppUpdateEvent) && (event.getObject() instanceof Person));
	}
	
	public void onEvent(Event event) {
		updateWorkspace(event);
	}
	
	protected void updateWorkspace(Event event) {
		if (!(event instanceof HibernateUpdateEvent))
			return;
		Person person = (Person)event.getObject();
		HibernateUpdateEvent updateevent = (HibernateUpdateEvent)event;
		int p =0;
		boolean updated = false;
		for (String propertyName : updateevent.getPropertyNames()) {
			if ("firstname".equals(propertyName.toLowerCase()) || "lastname".equals(propertyName.toLowerCase())) {
				if (updateevent.getCurrentState()[p]!=null && updateevent.getPreviousState()!=null && !updateevent.getCurrentState()[p].equals(updateevent.getPreviousState()[p])) {
					updated = true;
				}
			}	
			if (updated) {
				UserProfile profile = person.getProfile(UserProfile.class);
				if (profile!=null && profile.getUser()!=null) {
					reindexWorkspace(profile.getUser());
				}
				break;
			}
			p++;
		}	
	}
	
	protected void reindexWorkspace(User user) {
		try { 
			SolrParametersQuery query = new SolrParametersQuery(getIndex((KbeeUser)user));
			query.getParameters().put("workspace", String.valueOf(user.getId()));
			BatchIndexTaskServiceRequest task = new BatchIndexTaskServiceRequest(query, getIndex((KbeeUser)user));
			ServiceLocator.getService(SchedulerService.class).enqueue(task);
		}
		catch (SchedulerException e) {
			logger.error(e);
		}
	}

	protected Index getIndex(KbeeUser user) {
		return user.getDomain().getService(JavaIndexerService.class).getIndex();
	}
}
