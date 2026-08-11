package com.novamens.kbee.content.model;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.novamens.beans.BeansService;
import com.novamens.content.base.ConstraintException;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.entity.Person;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.PersonMember;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.event.BeforeUpdateEvent;
import com.novamens.event.Event;
import com.novamens.event.EventListener;
import com.novamens.hibernate.event.HibernateUpdateEvent;
import com.novamens.indexer.java.BatchIndexTaskServiceRequest;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.service.Index;
import com.novamens.scheduler.SchedulerException;
import com.novamens.scheduler.SchedulerService;
import com.novamens.security.User;
import com.novamens.security.acl.Group;
import com.novamens.service.ServiceLocator;
import com.novamens.solr.indexer.query.SolrParametersQuery;
import com.novamens.util.KbeeRuntimeException;

public class UserBeforeUpdateListener implements EventListener {

	static private Logger logger = LogManager.getLogger(UserBeforeUpdateListener.class.getName());
	
	public boolean listen(Event event) {
		return ((event instanceof BeforeUpdateEvent || event instanceof HibernateUpdateEvent) && 
			(event.getObject() instanceof Person || event.getObject() instanceof User));
	}
	
	public void onEvent(Event event) {
		updateUsersSubsets(event);
		updateWorkspace(event);
	}
	
	protected void updateWorkspace(Event event) {
		if (!(event instanceof HibernateUpdateEvent))
			return;
		HibernateUpdateEvent updateevent = (HibernateUpdateEvent)event;
		int i = 0;
		for (String propertyName : updateevent.getPropertyNames()) {
			if ("firstname".equals(propertyName.toLowerCase()) || "lastname".equals(propertyName.toLowerCase())) {
				if (updateevent.getCurrentState()[i]!=null && updateevent.getPreviousState()!=null && !updateevent.getCurrentState()[i].equals(updateevent.getPreviousState()[i])) {
					User user = getUser(event);
					if (user!=null) {
						reindexWorkspace(user);
					}
					break;
				}
			}
			else {
				i++;
			}	
		}	
	}
	
	protected void reindexWorkspace(User user) {
		try { 
			SolrParametersQuery query = new SolrParametersQuery(getIndex());
			query.getParameters().put("workspace", String.valueOf(user.getId()));
			BatchIndexTaskServiceRequest task = new BatchIndexTaskServiceRequest(query, getIndex());
			ServiceLocator.getService(SchedulerService.class).enqueue(task);
		}
		catch (SchedulerException e) {
			logger.error(e);
			logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
		}
	}
	
	protected void updateUsersSubsets(Event event) {
		if (!(event instanceof HibernateUpdateEvent))
			return;
		//if (subsets!=null && !subsets)
		//	return;
		//subsets = false;
		if (getDomain()==null)
			return;
		for (DataSet dataset : getContentDao().getDataSets(getDomain())) {
			if (dataset instanceof KbeeUserSubset) {
				//subsets = true;
				User user = getUser(event);
				Person person = getPerson(event);
				if (user!=null && person!=null) {
					Group setgroup = ((KbeeUserSubset)dataset).getGroup();
					List<DataSetMember> members = getContentDao().findMembersByEntity(person);
					DataSetMember datasetmember = null;
					for (DataSetMember member : members) {
						if (member.getDataSet().equals(dataset)) {
							datasetmember = member;
							break;
						}
					}
					boolean isgroupmember = false;
					for (Group group : user.getGroups()) {
						if (group.equals(setgroup)) {
							isgroupmember = true;
							break;
						}
					}
					try {
						if (isgroupmember && datasetmember==null) {
							datasetmember = ((KbeeUserSubset)dataset).createMember(person);
							getContentDao().save(datasetmember);
						}
						else {
							if (!isgroupmember && datasetmember!=null) {
								getContentDao().delete(datasetmember);
							}
						}
					}
					catch (ConstraintException | ContentMgmtException e) {
						throw new KbeeRuntimeException(e);
					}
				}
			}
		}
	}
	
	protected User getUser(Event event) {
		User user;
		if (event.getObject() instanceof Person) {
			Person person = (Person)event.getObject();
			if (person instanceof PersonMember) 
				person = ((KbeePersonMember)person).getPerson();
			UserProfile userprofile = person.getProfile(UserProfile.class);
			user = userprofile!=null ? userprofile.getUser() : null;
		}
		else {
			user = (User)event.getObject();
		}
		return user;
	}
	
	protected Person getPerson(Event event) {
		Person person = null;
		if (event.getObject() instanceof Person) {
			person = (Person)event.getObject();
		}
		else {
			User user = (User)event.getObject();
			UserProfile profile = getContentDao().findUserProfileByUser(user);
			if (profile!=null)
				person = profile.getPerson();
		}
		return person;
	}
	
	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	protected Index getIndex() {
		return getDomain().getService(JavaIndexerService.class).getIndex();
	}
	
	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
}
