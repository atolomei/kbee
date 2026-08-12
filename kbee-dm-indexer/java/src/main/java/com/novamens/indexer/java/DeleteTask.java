package com.novamens.indexer.java;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


import com.novamens.indexer.service.Index;
import com.novamens.indexer.service.IndexerException;
import com.novamens.spring.transaction.TransactionSynchronization;


public class DeleteTask extends ObjectIndexTaskServiceRequest {
	private static final long serialVersionUID = 1L;
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(DeleteTask.class.getName());
	
	private static Map<Thread, TransactionSynchronization> transactions = Collections.synchronizedMap(new HashMap<Thread, TransactionSynchronization>());

	public DeleteTask(Object object, Index index) {
		super(object, index);
		setName("DeleteTask");
	}
	
	public void execute() {
		try {
			((KbeeJavaIndex)this.getIndex()).delete(getObjectId());
			addTransactionSynchronization();
		}
		catch (IndexerException e) {
			logger.error(e);
			throw new RuntimeException(e);
		}
	}
	
	protected void addTransactionSynchronization() {
		if (transactions.get(Thread.currentThread()) == null) {
			transactions.put(Thread.currentThread(), new TransactionSynchronization() {
				public void afterCompletion(int status) {
					try {
						if (status == STATUS_COMMITTED) {
							getIndex().commit();
						}
					}
					catch (IndexerException e) {
						logger.error(e);
						throw new RuntimeException(e);
					}
					catch (RuntimeException e) {
						logger.error(e);
						throw new RuntimeException(e);
					}
					finally {
						transactions.remove(Thread.currentThread());
					}
				}
			});
		};
	}

}
