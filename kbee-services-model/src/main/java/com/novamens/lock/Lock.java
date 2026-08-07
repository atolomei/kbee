package com.novamens.lock;

import java.util.Date;

import com.novamens.security.User;

public interface Lock {
	public Long getId();
	public String getObjectId();
	public User getUser();
	public Date getDate();
	public Date getTimeout();
	public LockScope getScope();
}
