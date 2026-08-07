package com.novamens.spring.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import com.novamens.beans.BeansService;
import com.novamens.event.Event;
import com.novamens.event.EventListener;
import com.novamens.event.EventService;
import com.novamens.spring.service.SpringServiceLocator;

public class SpringEventService implements EventService {
	
	private List<EventListener> listeners;
	
    private final Set<EventListener> weakListeners =
            Collections.synchronizedSet(
                    Collections.newSetFromMap(new WeakHashMap<EventListener, Boolean>())
            );
    
	public SpringEventService() {
	}
	
	public void fire (Event event) {
		for (EventListener listener : getListeners(event)) {
			listener.onEvent(event);
		}
	}
	
	public void addListener(EventListener listener) {
		weakListeners.add(listener);
	}
	
	public List<EventListener> getListeners(Event event) {
		List<EventListener> listeners = new ArrayList<EventListener>();
		for (EventListener listener : getListeners()) {
			if (listener.listen(event))
				listeners.add(listener);
		}
        synchronized (weakListeners) {
			for (EventListener listener : getWeakListeners()) {
				if (listener.listen(event))
					listeners.add(listener);
			}
        }
		return listeners;
	}
	
	public List<EventListener> getListeners() {
		if (listeners == null) {
			listeners = new ArrayList<>();
			BeansService beansService = SpringServiceLocator.getService(BeansService.class);
			Map<String, EventListener> beans = beansService.getBeansOfType(EventListener.class);
			for (String bean : beans.keySet()) {
				listeners.add((EventListener)beansService.getBean(bean));
			}
		}
		return listeners;
	}
	
	public Set<EventListener> getWeakListeners() {
		return weakListeners;
	}
	
}
