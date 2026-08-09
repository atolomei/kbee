package com.novamens.kbee.wicket.markup.html.event;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.apache.wicket.behavior.Behavior;


public abstract class WicketEventListener<T extends com.novamens.event.Event> extends Behavior {
					
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(WicketEventListener.class.getName());

	private static final long serialVersionUID = 1L;
	
	private Class<?> eventclass;
	
	
	public WicketEventListener(T event) {
		this.eventclass = event.getClass();
	}
	
	public WicketEventListener() {
		try {
			Type superclass = getClass().getGenericSuperclass();
			Type tType = ((ParameterizedType)superclass).getActualTypeArguments()[0];
			String typename = tType.toString();
			if (typename.startsWith("class ")) {
				typename = typename.substring(6); 
			}
			if (typename.indexOf("<")>0) {
				typename = typename.substring(0, typename.indexOf("<"));
			}
			@SuppressWarnings("unchecked")
			Class<T> eventclass = (Class<T>)Class.forName(typename);
			this.eventclass = eventclass;
		}
		catch (Exception e) {
			logger.error(e);
			throw new RuntimeException(e);
		}
	}
	
	@Deprecated
	public WicketEventListener(Class<T> eventclass) {
		this.eventclass = eventclass;
	}
	
	public boolean handles(Class<T> claz) {
		return this.eventclass.equals(claz);
	}
	
	public boolean handle(com.novamens.event.Event event) {
		return eventclass.isInstance(event);
	}
	
	
	public Class<?> getEventClass() {
		return this.eventclass;
	}
	
	public abstract void onEvent(T event);
}
