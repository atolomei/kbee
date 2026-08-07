package com.novamens.lock;

import java.util.List;

import com.novamens.service.ObjectService;

public interface ObjectLockService extends ObjectService {
	public Lock lock(LockScope scope);
	public Lock lock(LockScope scope, long timeout);
	public void unlock();
	public void unlock(Lock lock);
	public List<Lock> getLocks();
}
