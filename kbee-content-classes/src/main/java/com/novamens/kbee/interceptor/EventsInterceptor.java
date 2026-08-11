package com.novamens.kbee.interceptor;

import java.io.Serializable;
import java.util.Collection;

import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;

import com.novamens.content.model.Classification;
import com.novamens.content.security.IQLRule;
import com.novamens.dom.Indexable;
import com.novamens.event.AppCreateEvent;
import com.novamens.event.AppDeleteEvent;
import com.novamens.event.EventService;
import com.novamens.event.LogEvent;
import com.novamens.hibernate.event.HibernateUpdateEvent;
import com.novamens.security.Principal;
import com.novamens.service.ServiceLocator;

public class EventsInterceptor extends EmptyInterceptor {
	private static final long serialVersionUID = 1L;
	
	@Override
	public boolean onSave(
			Object entity, 
			Serializable id, 
			Object[] state, 
			String[] propertyNames, 
			Type[] types) {
		ServiceLocator.getService(EventService.class).fire(new AppCreateEvent(entity));
		return true;
	}
	
	@Override
	public void onDelete(
			Object entity, 
			Serializable id, 
			Object[] state, 
			String[] propertyNames, 
			Type[] types) {
		ServiceLocator.getService(EventService.class).fire(new AppDeleteEvent(entity));
	}
	
	@Override
	public int[] findDirty(
			Object entity,
			Serializable id,
			Object[] currentState,
			Object[] previousState,
			String[] propertyNames,
			Type[] types) {
		if ((entity  instanceof com.novamens.dom.Object || 
				entity  instanceof Classification || 
				entity  instanceof LogEvent ||
				entity  instanceof Indexable ||
				entity  instanceof Principal ||
				entity  instanceof IQLRule) && changeState(currentState, previousState, propertyNames)) {
			ServiceLocator.getService(EventService.class).fire(new HibernateUpdateEvent(entity, currentState, previousState, propertyNames));
		}
		return null;
	}
	
	@Override
	public boolean onLoad(
			Object entity, 
			Serializable id, 
			Object[] state, 
			String[] propertyNames, 
			Type[] types) {
		return false;
	}
	
	private boolean changeState(Object[] currentState, Object[] previousState, String[] propertyNames) {
		if (previousState==null || currentState.length!=previousState.length)
			return true;
		for (int p=0; p<currentState.length; p++) {
			if ((currentState[p]==null && previousState[p]!=null) ||
					(currentState[p]!=null && previousState[p]==null) ||
					(currentState[p]!=null && 
					!(currentState[p] instanceof Collection) && currentState[p]!=previousState[p] && !currentState[p].equals(previousState[p]))) {
				if (currentState[p]==null || !currentState[p].getClass().isArray()) {
					if (!propertyNames[p].endsWith("Backref"))
					return true;
				}
			}
		}
		return false;
	}
}
