package com.novamens.kbee.content.command.mt;

import EDU.oswego.cs.dl.util.concurrent.LinkedQueue;
import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RunnableDispatcher_V0 implements Dispatcher<Runnable> {

	static private Logger logger = LogManager.getLogger(RunnableDispatcher_V0.class.getName());
						
	private int threadsPriority = Thread.currentThread().getPriority();
	private int threadNumber = 1;
	private int poolSize = 3;
	private ThreadFactory threadFactory = new ThreadFactory();
	private PooledExecutor threadPool = null;

	private class ThreadFactory implements EDU.oswego.cs.dl.util.concurrent.ThreadFactory {
		public Thread newThread(Runnable command) {
			Thread newThread = new Thread(command);
			newThread.setDaemon(true);
			newThread.setName("RV0 Queue Worker " + threadNumber++);
			newThread.setPriority(threadsPriority);
			return newThread;
		}
	}
	
	public RunnableDispatcher_V0(int poolSize) {
		setPoolSize(poolSize);
	}
	
	public void setPoolSize(int poolSize) {
		this.poolSize = poolSize;
	}
	
	public void dispatch(Runnable batch) {
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
					this.threadPool.setKeepAliveTime(5000); // Threads live forever...
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
