package com.novamens.kbee.content.event;

import com.novamens.dom.Domain;
import com.novamens.event.AbstractEvent;

public class AppModelUpdateEvent extends AbstractEvent {

	public AppModelUpdateEvent(Domain domain) {
		super(domain);
	}

	@Override
	public boolean distributable() {
		return true;
	}
}
