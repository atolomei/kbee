package com.novamens.kbee.content.workflow;

import org.springframework.transaction.support.TransactionSynchronizationManager;

public class WorkflowTransactionSynchronization implements org.springframework.transaction.support.TransactionSynchronization {
	
	private Long contentid;
	
	public WorkflowTransactionSynchronization(Long contentid) {
		this.contentid = contentid; 
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
	public Long getId() {
		return contentid;
	}
}