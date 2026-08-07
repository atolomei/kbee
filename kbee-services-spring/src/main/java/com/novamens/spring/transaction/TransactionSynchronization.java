package com.novamens.spring.transaction;

import org.springframework.transaction.support.TransactionSynchronizationManager;

public class TransactionSynchronization implements org.springframework.transaction.support.TransactionSynchronization {
	
	public TransactionSynchronization() {
		if (TransactionSynchronizationManager.isSynchronizationActive())
			TransactionSynchronizationManager.registerSynchronization(this);
	}
	public void afterCompletion(int status) {
	}
	public void beforeCompletion() {
	}
	public void afterCommit() {
	}
	public void beforeCommit(boolean status) {
	}
	public void flush() {
	}
	public void suspend() {
	}
	public void resume() {
	}
}
