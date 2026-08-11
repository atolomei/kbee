package com.novamens.kbee.content.event;

import com.novamens.event.AbstractEvent;

public class AppCheckinEvent extends AbstractEvent {

	boolean silent = false;

	public AppCheckinEvent(Object object) {
		super(object);
	}

	public AppCheckinEvent(Object object, boolean silent) {
		super(object);
		this.silent = silent;
	}

	public boolean isSilent() {
		return silent;
	}

	public void setSilent(boolean silent) {
		this.silent = silent;
	}
}
