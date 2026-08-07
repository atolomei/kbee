package com.novamens.kbee.lock;

import org.springframework.util.Assert;

import com.novamens.lock.MemLockService;
import com.novamens.lock.ValueLockerService;
import com.novamens.service.ServiceLocator;

public class KbeeMemLockService implements MemLockService {
	private com.novamens.dom.Object object;
	
	KbeeMemLockService() {
	}
	
	KbeeMemLockService(Object object) {
		Assert.isInstanceOf(com.novamens.dom.Object.class, object);
		this.object = (com.novamens.dom.Object)object;
	}
	
	public void lock() {
		ServiceLocator.getService(ValueLockerService.class).lock(getObject().getId());
	}
	
	public void unlock() {
		ServiceLocator.getService(ValueLockerService.class).unlock(getObject().getId());
	}
	
	public com.novamens.dom.Object getObject() {
		return object;
	}
}
