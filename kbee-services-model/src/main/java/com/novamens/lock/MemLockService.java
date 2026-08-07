package com.novamens.lock;

import com.novamens.service.ObjectService;

public interface MemLockService extends ObjectService {
	public void lock();
	public void unlock();
}
