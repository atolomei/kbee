package com.novamens.kbee.scheduler;

import EDU.oswego.cs.dl.util.concurrent.LinkedQueue;
import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.novamens.scheduler.Batch;
import com.novamens.scheduler.Dispatcher;

/**
 * 
 * The Dispatchers are contained by the {@link Scheduler} 
 * and they always run inside their thread.
 * 
 * It manages a pool of threads, where {@link Batch} execute.
 *  use 
 *  
 *  use instead: {@link ThreadPoolDispatcher}
 */
@Deprecated  
public class KbeeDispatcher implements Dispatcher {

	static private Logger logger = LogManager.getLogger("Scheduler");
	static private Logger startupLogger = LogManager.getLogger("StartupLogger"); 
						
	private int poolSize;
	private int priority;
	private int threadsPriority = Thread.currentThread().getPriority();
	private int threadNumber = 1;
	private ThreadFactory threadFactory = new ThreadFactory();
	private PooledExecutor threadPool;

	private int initial_size;
	
	private class ThreadFactory implements EDU.oswego.cs.dl.util.concurrent.ThreadFactory {
		public Thread newThread(Runnable command) {
			Thread newThread = new Thread(command);
			newThread.setDaemon(true);
			newThread.setName("Dispatcher Worker pr_" + getPriority() + "-" + threadNumber++);
			newThread.setPriority(threadsPriority);
			return newThread;
		}
	}
	
	/**
	 * @param priority
	 * @param poolSize
	 */
	public KbeeDispatcher(int priority, int poolSize) {
		this.priority = priority;
		this.poolSize = poolSize; // max size
		this.initial_size = poolSize>4?4:poolSize;
		startupLogger.info("Starting Dispatcher");
		makePool();
		startupLogger.info(getInfo());
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
		this.threadPool = new PooledExecutor(new LinkedQueue(), poolSize);
		this.threadPool.setThreadFactory(threadFactory);
		this.threadPool.setMinimumPoolSize(poolSize);
		this.threadPool.createThreads(initial_size); // Threads are created at start time.
		this.threadPool.setKeepAliveTime(-1); // Threads live forever...
		this.threadPool.waitWhenBlocked(); // Wait for a free thread when blocked.
		this.threadPool.setMaximumPoolSize(poolSize);
	}
	
	public String getInfo() {
		return "Priority: " + String.valueOf(priority) + ". Workers: " + String.valueOf(poolSize);
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
			logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
			throw new RuntimeException(e);
		}
	}

	public void execute(Runnable runnable) {
		try {
			this.threadPool.execute(runnable);
		}
		catch (InterruptedException e) {
			logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
			throw new RuntimeException(e);
		}
	}

	public void setPoolSize(int value) {
		this.poolSize = value;
		this.threadPool.setMaximumPoolSize(value);
	}
}
