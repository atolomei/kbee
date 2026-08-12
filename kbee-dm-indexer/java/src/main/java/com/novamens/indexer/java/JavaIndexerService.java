package com.novamens.indexer.java;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.novamens.indexer.service.Index;
import com.novamens.indexer.service.IndexerException;
import com.novamens.indexer.service.IndexerService;
import com.novamens.indexer.service.JavaIndex;
import com.novamens.scheduler.SchedulerException;
import com.novamens.scheduler.SchedulerService;
import com.novamens.service.ServiceLocator;
import com.novamens.spring.transaction.TransactionSynchronization;

import kbee.util.PropertiesFactory;

/**
 * 
 *<p>Index Java objects
  * Separate the indexing of an object in the Sync and Async part</p>
 *
 *   see:
 *  {@link /kbee-content-classes/src/main/resources/META-INF/kbee/content/spring/content-index-context.xml}
 *
 */
public abstract class JavaIndexerService implements IndexerService {
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(JavaIndexerService.class.getName());
	
	private Index index;
	private Map<Thread, List<IndexerTask>> queues = Collections
			.synchronizedMap(new HashMap<Thread, List<IndexerTask>>());
	private Map<Thread, TransactionSynchronization> transactions = Collections
			.synchronizedMap(new HashMap<Thread, TransactionSynchronization>());
	private Map<Class<?>, DocumentSchema> schemas = Collections
			.synchronizedMap(new HashMap<Class<?>, DocumentSchema>());
	
	boolean cloudEnabled = "true".equals(PropertiesFactory
		.getInstance("kbee")
		.getProperties()
		.getProperty("kbee.cloud.enabled", "false").trim());
	
	public JavaIndexerService(Index index) {
		setIndex(index);
	}
	
	/**
	 * Hibernate listeners that listen for Object updates * 
	 * they call index.
	 * 
	 * @param object
	 * @throws IndexerException
	 */
	public void index(Object object) throws IndexerException {
		if (((JavaIndex)getIndex()).isIndexable(object) && !scheduled(object)) {
			if (!isApi()) {
				schedule(new IndexMetainfoTask(object, getIndex()));
				if (cloudEnabled) {
					schedule(new CloudIndexMetainfoTask(object, getIndex()));
					schedule(new CloudIndexTask(object, getIndex()));
				}
				schedule(new IndexTask(object, getIndex()));
			}
			else {
				IndexTask task = new IndexTask(object, getIndex());
				task.setPriority(2);
				schedule(task);
			}
		}
	}
	
	public void delete(Object object) throws IndexerException {
		schedule(new DeleteTask(object, getIndex()));
	}
	
	/**
	 * 
	 * In-memory indexing queue for Metainfo
	 * 
	 * @param task
	 * @throws IndexerException
	 */
	public void schedule(IndexerTask task) throws IndexerException {
		try {
		
			/** Metadata + Attachments */
			if (task.isTransactional()) {
				ServiceLocator.getService(SchedulerService.class).enqueue(task);
			}
			
			/** Metadata */
			if (task.isSynchronous()) {
				getThreadQueue().add(task);
				addTransactionSynchronization();
			}
		}
		catch (SchedulerException e) {
			logger.error(e);
			throw new IndexerException(e);
		}
	}
	
	public boolean scheduled(Object object) {
		List<IndexerTask> queue = queues.get(Thread.currentThread());
		if (queue!=null)
			for (IndexerTask task : queue) {
				if (task.getObject()!=null && task.getObject().equals(object))
					return true;
			}
		return false;
	}
		
	public void setIndex(Index index) {
		this.index = index;
	}
	
	public Index getIndex() {
		return index;
	}
	
	public void addSchema(DocumentSchema schema) {
		schemas.put(schema.getJavaClass(), schema);
	}
	
	public DocumentSchema getSchema(Object entity) {
		return schemas.get(entity.getClass());
	}



	public final Map<Class<?>, DocumentSchema> getSchemas() {
		 return schemas;
	}

	
	public void execute(List<IndexerTask> tasks) {
		for (IndexerTask task : tasks) {
			task.execute();
		}
	}
	
	protected boolean isApi() {
		return false;
	}
	
	/**
	 * 
	 * TRX is synchronous within a thread
* 		each TRX has its indexing queue.
	 * 
	 * @return
	 */
	protected List<IndexerTask> getThreadQueue() {
		List<IndexerTask> queue = queues.get(Thread.currentThread());
		if (queue ==null) {
			queue = new ArrayList<IndexerTask>(2);
			queues.put(Thread.currentThread(), queue);
		}
		return queue;
	}
	
	/**
	 * 
	 * Listener that registers in the TRX 
	 * is invoked after the TRX completes
	 * 
	 */
	private void addTransactionSynchronization() {
		if (transactions.get(Thread.currentThread()) == null) {
			transactions.put(Thread.currentThread(), new TransactionSynchronization() {
				public void afterCompletion(int status) {
					try {
						if (status == STATUS_COMMITTED) {
							execute(getThreadQueue());
							getIndex().commit();
						}
					}
					catch (IndexerException e) {
						logger.error(e);
					}
					catch (RuntimeException e) {
						logger.error(e);
						if (!(e.getCause() instanceof IndexerException))
							throw e;
					}
					finally {
						queues.remove(Thread.currentThread());
						transactions.remove(Thread.currentThread());
					}
				}
			});
		}
	}
	
}
