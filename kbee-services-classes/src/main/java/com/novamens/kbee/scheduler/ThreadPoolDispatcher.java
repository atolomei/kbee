package com.novamens.kbee.scheduler;


import EDU.oswego.cs.dl.util.concurrent.LinkedQueue;
import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;
import com.novamens.scheduler.Batch;
import com.novamens.scheduler.Dispatcher;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 
 * The Dispatchers are contained by the {@link Scheduler} 
 * and they always run inside their thread.
 * 
 * It manages a pool of threads, where {@link Batch} execute.
 */
public class ThreadPoolDispatcher implements Dispatcher {

	static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ThreadPoolDispatcher.class.getName());

	static private Logger startupLogger = LogManager.getLogger("StartupLogger");

	private int priority;
	private int maxPoolSize;
	private int minPoolSize;
	private int initialSize;
	private int keeepAliveMS; /* Time in ms*/

	
	private String id;

	private int threadsPriority = Thread.currentThread().getPriority();
	private int threadNumber = 1;
	private ThreadFactory threadFactory = new ThreadFactory();
	private PooledExecutor threadPool;

	// Map<String, Thread> i_threads = new HashMap<String, Thread>();
	
	private class ThreadFactory implements EDU.oswego.cs.dl.util.concurrent.ThreadFactory {
		public Thread newThread(Runnable command) {
			Thread newThread = new Thread(command);
			newThread.setDaemon(true);
			newThread.setName(getId()+"_Worker" + "-" + String.valueOf(threadNumber++));
			newThread.setPriority(threadsPriority);

			// AT
			// i_threads.put(newThread.getName(), newThread);
			
			return newThread;
		}
	}
	
	/**
	 * @param priority
	 * @param poolSize
	 */
	public ThreadPoolDispatcher(int priority, int poolSize) {
		this(priority,poolSize, "DP");
	}

	public ThreadPoolDispatcher(int priority, int poolSize, String id) {
		this.priority = priority;
		this.maxPoolSize = poolSize; // max size
		this.minPoolSize= poolSize;
		this.initialSize = poolSize >4?4: poolSize;
		this.keeepAliveMS =-1;
		this.id=id;
		startupLogger.info("Starting Dispatcher");
		makePool();
		startupLogger.info(getInfo());
	}


	
	public ThreadPoolDispatcher(int priority, int maxPoolSize, int minPoolSize, int initialSize, int keeepAliveMS, String id) {
		//on the current implementation, it doesnt make sense to have maxPoolSize != minPoolSize, because queue is unbounded so maxPoolSize will never be used
		assert(maxPoolSize == minPoolSize);

		this.priority = priority;
		this.maxPoolSize = maxPoolSize; // max size
		this.minPoolSize= maxPoolSize;
		this.initialSize = initialSize;
		this.keeepAliveMS = keeepAliveMS;
		this.minPoolSize = minPoolSize;
		this.id = id;
		makePool();
		startupLogger.info(getInfo());
	}

	public String getId() { 
		return id;
	}
	
	@Override
	public void restart() {
		restart(false);
	}

	@Override
	public void restart(boolean force) {
		if (force) {
			logger.info("shuting down now.");
			this.threadPool.shutdownNow();
		}
		else {
			logger.info("Waiting for all current processes to terminate and shutdown.");
			this.threadPool.shutdownAfterProcessingCurrentlyQueuedTasks();
		}
		logger.info("restarting threads.");
		makePool();
	}
	
	
	private void makePool() {
		this.threadPool = new PooledExecutor(new LinkedQueue(), maxPoolSize);
		this.threadPool.setThreadFactory(threadFactory);
		this.threadPool.setMinimumPoolSize(minPoolSize);
		this.threadPool.createThreads(initialSize); // Threads are created at start time.
		this.threadPool.setKeepAliveTime(keeepAliveMS);
		this.threadPool.setMaximumPoolSize(maxPoolSize);
		this.threadPool.waitWhenBlocked(); // Wait for a free thread when blocked.

	}
	
	public String getInfo() {
		return "Priority: " + String.valueOf(priority) + ". Workers: " + String.valueOf(maxPoolSize);
	}
	
	
	@Override
	public String getStatus() {
		StringBuilder str = new StringBuilder(); 
		int poolsize=this.threadPool.getPoolSize();
		str.append("Priority: " + String.valueOf(this.priority) + " | ");
		str.append("PoolSize: " + String.valueOf(poolsize)+ " | ");
		str.append("MaxPoolSize: " + String.valueOf(this.threadPool.getMaximumPoolSize()));
		return str.toString();
	}

	@Override
	public void shutDownNow() {
		this.threadPool.shutdownNow();
	}
	
	public int getPoolSize() {
		return threadPool.getPoolSize();
	}
					
	public int getMaximumPoolSize() {
		return threadPool.getMaximumPoolSize();
	}

	public int getPriority() {
		return this.priority;
	}
	
	public void setPriority(int value) {
		this.priority = value;
	}
	
	public void dispatch(Batch batch) {
		try {
			batch.setDispatched(true);
			batch.setTimestampDispatched(System.currentTimeMillis());
			this.threadPool.execute(batch);
		}
		catch (InterruptedException e) {
			logger.error(e);
			throw new RuntimeException(e);
		}
	}

	public void execute(Runnable runnable) {
		try {
			this.threadPool.execute(runnable);
		}
		catch (InterruptedException e) {
			logger.error(e);
			throw new RuntimeException(e);
		}
	}

	public void setMaxPoolSize(int value) {
		this.maxPoolSize = value;
		this.threadPool.setMaximumPoolSize(value);
	}
	

	/**
	public Map<String, String> getThreadStatus() {
		
		Map<String, String> m=new HashMap<String, String>();
		
		for (Entry<String, Thread> entry:i_threads.entrySet()) {
			// m.put(entry.getValue().get);
		}
		
		return null;
	}
	**/
}
