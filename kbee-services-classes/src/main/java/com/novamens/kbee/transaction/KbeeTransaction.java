package com.novamens.kbee.transaction;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate5.SessionFactoryUtils;
import org.springframework.orm.hibernate5.SessionHolder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.novamens.transaction.Transaction;

public class KbeeTransaction implements Transaction {
	private Session session = null;
	private TransactionStatus status = null;
	private PlatformTransactionManager manager;
	
	public KbeeTransaction(SessionFactory sessionFactory, PlatformTransactionManager manager) {
		session = sessionFactory.openSession();
		TransactionSynchronizationManager.bindResource(sessionFactory, new SessionHolder(session));
		this.manager = manager;
		DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
		definition.setName("KbeeTx");
		definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
		status = manager.getTransaction(definition);
	}
	
	public KbeeTransaction(PlatformTransactionManager manager) {
		this.manager = manager;
		DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
		definition.setName("KbeeTx");
		definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
		status = manager.getTransaction(definition);
	}
	
	public boolean isActive() {
		return status!=null;
	}
	
	public boolean isCompleted() {
		return status!=null && status.isCompleted();
	}
	
	public void commit() {
		manager.commit(status);
		if (session!=null) {
			TransactionSynchronizationManager.unbindResource(session.getSessionFactory());
			SessionFactoryUtils.closeSession(session);
		}
	}
	
	public void rollback() {
		try {
			manager.rollback(status);
		}
		finally {
			if (session!=null) {
				TransactionSynchronizationManager.unbindResource(session.getSessionFactory());
				SessionFactoryUtils.closeSession(session);
			}
		}
	}
}
