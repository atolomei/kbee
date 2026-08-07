package com.novamens.kbee.event;

import com.novamens.event.AbstractEvent;

public class EvictCacheServiceEvent extends AbstractEvent {

	@Override
	public boolean distributable() {
		return true;
	}
}