package com.novamens.content.user;

import java.time.OffsetDateTime;

import com.novamens.dom.ObjectState;
import com.novamens.security.Auditable;
import com.novamens.security.Identifiable;

public interface UserDevice extends Identifiable, Auditable {
	public String getDeviceId();
	public String getDescription();
	public String getNumber();
	public OffsetDateTime getRegistrationTime();
	public ObjectState getState();
	public boolean isWebRegistered();

	public boolean isAndroid();
	public boolean isIOS();
	
	
	// Phone | Tablet | ...
	//public String getDeviceCategory();
}