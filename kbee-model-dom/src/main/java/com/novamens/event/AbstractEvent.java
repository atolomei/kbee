package com.novamens.event;

import java.time.Instant;

/**
 * Application Events managed by the Spring Framework
 * and sent by a Spring fire() method.
 * 
 *  NOTE: They are different from Log4J2 events (LogEvent).
 *  
 *
 */
public class AbstractEvent implements Event {
			
	public Instant time;
	public Object object;
	
	public AbstractEvent() {
		setTime(Instant.now());
	}
	
	public AbstractEvent(Object object) {
		setObject(object);
		setTime(Instant.now());
	}
	
	public Object getObject() {
		return object;
	}
	
	public void setObject(Object object) {
		this.object = object;
	}
	
	public Instant getTime() {
		return time;
	}
	
	public void setTime(Instant time) {
		this.time = time;
	}
}