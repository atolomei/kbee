package com.novamens.lock;

import java.util.List;

import com.novamens.service.SystemService;

public interface SystemLockService extends SystemService {
	public Lock lock(Object object, LockScope scope);
	public Lock lock(Object object, LockScope scope, long timeout);
	public void unlock(Lock lock);
	public List<Lock> getLocks(Object object);
}
