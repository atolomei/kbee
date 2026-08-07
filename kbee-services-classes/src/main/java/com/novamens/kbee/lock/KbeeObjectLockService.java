package com.novamens.kbee.lock;

import java.util.List;

import org.springframework.util.Assert;

import com.novamens.lock.Lock;
import com.novamens.lock.LockScope;
import com.novamens.lock.ObjectLockService;
import com.novamens.lock.SystemLockService;
import com.novamens.service.ServiceLocator;

public class KbeeObjectLockService implements ObjectLockService {
	private com.novamens.dom.Object object;
	private SystemLockService systemLockService;
	
	KbeeObjectLockService() {
	}
	
	KbeeObjectLockService(Object object) {
		Assert.isInstanceOf(com.novamens.dom.Object.class, object);
		this.object = (com.novamens.dom.Object)object;
	}
	
	public Lock lock(LockScope scope) {
		return getSystemLockService().lock(getObject(), scope);
	}
	
	public Lock lock(LockScope scope, long timeout) {
		return null;
	}
	
	public void unlock() {
		List<Lock> locks = getLocks();
		Assert.isTrue(locks.size()==1, "no lock");
		getSystemLockService().unlock(locks.get(0));
	}
	
	public void unlock(Lock lock) {
	}
	
	public List<Lock> getLocks() {
		return getSystemLockService().getLocks(getObject());
	};
	
	public SystemLockService getSystemLockService() {
		if (systemLockService==null)
			systemLockService = ServiceLocator.getService(SystemLockService.class);
		return systemLockService;
	}
	
	public com.novamens.dom.Object getObject() {
		return object;
	}
}
