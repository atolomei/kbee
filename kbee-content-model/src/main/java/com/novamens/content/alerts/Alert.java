package com.novamens.content.alerts;

import java.io.Serializable;

import com.novamens.content.base.Content;
import com.novamens.security.acl.Acl;

public interface Alert {

	public Serializable getId();
	public String getName();
	public boolean evaluate(Content content);
	
	public Acl getAcl();
}

