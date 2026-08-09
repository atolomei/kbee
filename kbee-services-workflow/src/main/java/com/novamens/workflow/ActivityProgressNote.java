package com.novamens.workflow;

import java.time.OffsetDateTime;

import com.novamens.dom.DomainObject;
import com.novamens.dom.ObjectState;
import com.novamens.security.Auditable;
import com.novamens.security.Identifiable;

public interface ActivityProgressNote extends com.novamens.dom.Object, DomainObject, Identifiable, Auditable {
	public Long getId();
	public Activity getActivity();
	public String getText();
	public OffsetDateTime getTime();
	public ObjectState getState();
}
