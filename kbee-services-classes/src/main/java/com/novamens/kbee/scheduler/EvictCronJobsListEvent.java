package com.novamens.kbee.scheduler;

import com.novamens.event.AbstractEvent;

public class EvictCronJobsListEvent extends AbstractEvent {

	@Override
	public boolean distributable() {
		return true;
	}
}
