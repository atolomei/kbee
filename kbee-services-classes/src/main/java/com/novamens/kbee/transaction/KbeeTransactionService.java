package com.novamens.kbee.transaction;

import org.hibernate.SessionFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

import com.novamens.transaction.ReadOperation;
import com.novamens.transaction.Transaction;
import com.novamens.transaction.TransactionService;

public class KbeeTransactionService implements TransactionService {
	private SessionFactory sessionFactory;
	private PlatformTransactionManager transactionManager;
	
	public Transaction beginTransaction() {
		return new KbeeTransaction(getSessionFactory(), transactionManager);
	}
	
	public Transaction beginTransaction(boolean opensession) {
		if (opensession)
		return new KbeeTransaction(getSessionFactory(), transactionManager);
		else
		return new KbeeTransaction(transactionManager);
	}
	
	@Transactional(readOnly=true)
	public <T> T execute(ReadOperation<T> operation) {
		return operation.execute();
	}
	
	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}
	
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	public void setTransactionManager(PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}
}
