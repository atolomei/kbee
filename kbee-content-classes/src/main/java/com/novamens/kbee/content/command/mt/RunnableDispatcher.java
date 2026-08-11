package com.novamens.kbee.content.command.mt;

import EDU.oswego.cs.dl.util.concurrent.LinkedQueue;
import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RunnableDispatcher implements Dispatcher<QueuedBatchProcessor> {

	static private Logger logger = LogManager.getLogger(RunnableDispatcher.class.getName());
						
	private int threadsPriority = Thread.currentThread().getPriority();
	private int threadNumber = 1;
	private int poolSize = 3;
	private ThreadFactory threadFactory = new ThreadFactory();
	private PooledExecutor threadPool = null;

	private class ThreadFactory implements EDU.oswego.cs.dl.util.concurrent.ThreadFactory {
		public Thread newThread(Runnable command) {
			Thread newThread = new Thread(command);
			newThread.setDaemon(true);
			newThread.setName("RunnableDispatcher Queue Worker " + threadNumber++);
			newThread.setPriority(threadsPriority);
			return newThread;
		}
	}
	
	public RunnableDispatcher(int poolSize) {
		setPoolSize(poolSize);
	}
	
	public void setPoolSize(int poolSize) {
		this.poolSize = poolSize;
	}
	
	public void dispatch(QueuedBatchProcessor batch) {
		try {
			getPool().execute(batch);
		}
		catch (InterruptedException e) {
			logger.debug("Dispatcher Error",  e);
			throw new RuntimeException(e);
		}
	}
		
	private PooledExecutor getPool() {
		if (threadPool==null) {
			synchronized (this) {
				if (threadPool==null) {
					this.threadPool = new PooledExecutor(new LinkedQueue(), poolSize);
					this.threadPool.setThreadFactory(threadFactory);
					this.threadPool.setKeepAliveTime(20000); // Threads live forever...
					this.threadPool.waitWhenBlocked(); // Wait for a free thread when blocked.
					this.threadPool.createThreads(poolSize); // Threads are created at start time.
					this.threadPool.setMinimumPoolSize(poolSize);
					this.threadPool.setMaximumPoolSize(poolSize);
				}
			}
		}
		return threadPool;
	}
}
