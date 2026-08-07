package com.novamens.kbee.lock;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.novamens.lock.ValueLockerService;
import com.novamens.service.ServiceLocator;
import com.novamens.spring.transaction.TransactionSynchronization;

public class LockTransactionSynchronization extends TransactionSynchronization {
	private Serializable value;
	
	private static Map<Thread, TransactionSynchronization> transactions = Collections.synchronizedMap(new HashMap<Thread, TransactionSynchronization>());

	public LockTransactionSynchronization(Serializable value) {
		this.value = value;
		transactions.put(Thread.currentThread(), this);
		ServiceLocator.getService(ValueLockerService.class).tryLock(value);
	}
	@Override
	public void afterCompletion(int status) {
		try {
			ServiceLocator.getService(ValueLockerService.class).tryUnlock(value);
		}	
		finally {
			transactions.remove(Thread.currentThread());
		}
	}	
}