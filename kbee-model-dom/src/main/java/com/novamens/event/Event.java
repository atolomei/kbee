package com.novamens.event;

import java.time.Instant;

/**
 * Application Events
 *
 */
public interface Event {
	
	
	public Instant getTime();
	public Object getObject();
	
	public default boolean distributable() {
		return false;
	}
	
	
}