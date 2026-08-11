package com.novamens.portal6.model;

import java.io.Serializable;

import com.novamens.event.AbstractEvent;

public class EvictSiteCacheServiceEvent extends AbstractEvent {

	private Serializable siteId;
	
	public EvictSiteCacheServiceEvent(Serializable siteId) {
	 	this.siteId=siteId;
	}
	
	public Serializable getId() {
		return this.siteId;
	}
	
	@Override
	public boolean distributable() {
		return true;
	}
}
