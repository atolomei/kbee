package com.novamens.lock;

import java.io.Serializable;

import com.novamens.service.SystemService;

public interface ValueLockerService extends SystemService {
	public void lock(Serializable value);
	public void tryLock(Serializable value);
	public void unlock(Serializable value);
	public void tryUnlock(Serializable value);
}
