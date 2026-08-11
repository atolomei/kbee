package com.novamens.kbee.content.command.mt;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;


import com.novamens.beans.BeansService;
import com.novamens.content.command.CommandState;
import com.novamens.content.dao.ContentDao;
import com.novamens.kbee.content.command.AsyncCommand;
import com.novamens.service.ServiceLocator;

/**
 * {@link IndexerQueue}
 * 
 * 
 *  @see	   	{@code BatchReindexExecutor}
  	@see   		{@code QueuedBatchProcessor}
  	@see		{@code BatchReindexCommand}
  	@see  		{@code QueueProcessorCommand}
 *
 * @param <T>
 */
public class QueueProcessorCommand<T> extends AsyncCommand {
																	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(QueueProcessorCommand.class.getName());

	static public final long SECOND  	= 1000;
	
	static public final long BATCH_SIZE  = 300;
	
	int MAX_THREADS;
	
	static final int DEFAULT_MAX_THREADS = 1;
	
	private int max_threads=-1;
	
	private AtomicLong counter = new AtomicLong(0); 
	private Queue<T> queue;
	private Dispatcher<QueuedBatchProcessor> dispatcher;
	
	long processed = 0, queueSize = 0, setSize = 0, total_size = 0;
	long batch_size = BATCH_SIZE;
	
	public QueueProcessorCommand() {
	}
	
	@Override
	protected void executeAsync() {

		try {
			
			setProgress(0);
			logger.debug("Executing -> " + this.getClass().getName() + " |  max_threads -> " + String.valueOf(MAX_THREADS));
			initQP();
			boolean is_data = true;
			final long LIMIT = getLimit();

			/**
			    before launching threads we calculate size
			    queueSize
			    setSize
			 **/
			
			int batchOrder = 0;
			
			while(isRunning() &&  (this.processed < LIMIT) && is_data) {
				if (concurrencyLimit()) {
					try {
						synchronized (this) {
							wait(SECOND);
						}
					}
					catch(InterruptedException e) {
						logger.debug(e);
					}
				}
				else {
					List<T> batch = getBatch(getQueue());
					if (batch.size()>0)
						process(batch, newCallback(),  batchOrder++);
					else 
						is_data = false;
					
					processed += batch.size();
					
				}
			}
			
			logger.debug(processed);
				
			if (getTotalItems()>0) {
				setProgress(100.0 * (double) processed / (double) getTotalItems());
			}
			
			
			while ( QueuedBatchProcessor.Instances() > 1 ) {
				try {
					synchronized (this) {
						wait(SECOND);
					}
				}
				catch(InterruptedException e) {
					logger.debug(e);
				}
			}

			if ( getTotalItems() > 0 ) {
				setProgress(100.0 * (double) processed / (double) getTotalItems());
			}

			end();
			
		}
		catch (QueueException e) {
			logger.error(e);
			super.setState(CommandState.ERROR);
			super.setResult(e.getClass().getSimpleName());
			super.setResultComments(e.getMessage());
			stop();
			
		}
		catch (Exception e) {
			logger.error(e);
			super.setState(CommandState.ERROR);
			super.setResult(e.getClass().getSimpleName());
			super.setResultComments(e.getMessage());	
			stop();
		}
		finally {
			getQueue().close();
			QueuedBatchProcessor.resetInstances();
			setDateTerminated(OffsetDateTime.now());
		}
	}
	
	
	@Override
	public double estimatedSecsToEnd() {
		return super.estimatedSecsToEnd();
	}
	
	
	@Override
	public int getThreads() {
		return getMaxThreads();
	}
	
	public Queue<T> getQueue() {
		return queue;
	}
	
	public void setQueue(Queue<T> queue) {
		this.queue = queue;
	}
	
	public Dispatcher<QueuedBatchProcessor> getDispatcher() {
		return dispatcher;
	}
	
	public void setDispatcher(Dispatcher<QueuedBatchProcessor> dispatcher) {
		this.dispatcher = dispatcher;
	}
	
	
	public int getTotalWorkingThreads() {
		return QueuedBatchProcessor.Instances();
	}
	
	public boolean concurrencyLimit() {
		return QueuedBatchProcessor.Instances()>=getMaxThreads();
	}

	
	public int getMaxThreads() {

		if (max_threads>0)
			return max_threads;
		
		try {
			
			max_threads = getParameter("max-threads")!=null ? Integer.valueOf((String) getParameter("max-threads")) : DEFAULT_MAX_THREADS;

			if (max_threads==0)
				max_threads=DEFAULT_MAX_THREADS;
			
			return max_threads;
			
		} 
		catch (Exception e) {
			logger.error(e);
			max_threads = DEFAULT_MAX_THREADS;
			return max_threads;
		}
	}
	
	public long getLimit() {
		try {
			if (getParameter("limit")!=null) {
				Long li = Long.valueOf((String) getParameter("limit"));
				if (li.longValue()<=0)
					return Long.MAX_VALUE;
				return li.longValue();
			}
			else
				return Long.MAX_VALUE;
		}
		catch (Exception e) {
			logger.error(e);
			return Long.MAX_VALUE;
		}
	}


	
	/**
	 * Set up:
	 * processed = 0
	 * queueSize =
	 * setSize    = 
	 * 
	 */
	private synchronized void initQP() {
		
		try {
			
			MAX_THREADS = (int) Math.round(Math.floor( Double.valueOf( Runtime.getRuntime().availableProcessors() - 1)));
			
			if (MAX_THREADS<1)
				MAX_THREADS=1;				
			
			try {
				batch_size = getParameter("batch-size")!=null ? Integer.valueOf( String.valueOf(getParameter("batch-size"))) : BATCH_SIZE;
				
			} catch (Exception e) {
				logger.error(e);
				batch_size =	BATCH_SIZE;
			}
			
			if (logger.isDebugEnabled()) { 
				getParameters().forEach((k, v) -> logger.debug(String.valueOf(k)+" -> " + String.valueOf(v)));
			}

			getQueue().setParameters(getParameters());
			
			((RunnableDispatcher) getDispatcher()).setPoolSize(getMaxThreads());
			
			QueuedBatchProcessor.resetInstances();
			
			this.processed = 0;
			
			if (this.queueSize<=0) 
				this.queueSize = getQueue().size();
			
			this.setSize = (getLimit()>0 && this.queueSize>getLimit()) ? getLimit() : this.queueSize;
			
		}
		catch (Exception e) {
			logger.error(e);
			this.queueSize = 0;
			this.setSize =0;
		}
	}
	
	/**
	 * 
	 */
	@Override
	public double getProgress() {
		try {
			long va=counter.get();
			return va>0 && getSetSize() >0 ? (double) va/(double) getSetSize() * 100 : 0.0;
		}
		catch (Exception e) {
			logger.error(e);
			return 0;
		}
	}
	
	public long getQueueSize() {
		return this.queueSize;
	}
	
	public long getSetSize() {
		return this.setSize;
	}
	
	@Override
	public long getTotalItems() {
		if (getLimit()>0 && getQueueSize()>getLimit())
			return getLimit();
		else
			return getQueueSize();
	}
	
	@Override
	public long getTotalItemsProcessed() {
		return  processed;
	}
	
	
 
	public long getBatchSize() {
		try {
			return batch_size; 
		}
		catch (Exception e) {
			logger.error(e);
			return BATCH_SIZE;
		}
	}
	
	protected Callback<T> newCallback() {
		return new Callback<T>() {
			public void execute(T file) throws Exception {
				
				synchronized (QueueProcessorCommand.this) {
					try {
						QueueProcessorCommand.this.notify();
						incCounter();
					} catch (Exception e) {
						logger.error(e);
					}
				}	
			}
		};	
	}
	
	
	protected List<T> getBatch(Queue<T> queue) throws QueueException {
		List<T> batch = new ArrayList<T>();
		try {
			T file = queue.dequeue();
			int index = 0;
			while (file!=null) {
				batch.add(file);
				if (++index<getBatchSize()) {
					file = queue.dequeue();
				}
				else {
					break;
				}
			}
		} catch (Exception e) {
			logger.error(e);
		}
		return batch;
	}
	
	protected void incCounter() {
		this.counter.incrementAndGet();
	}
	
	protected void incCounter(long delta) {
		this.counter.getAndAdd(delta);
	}
	
	protected void process(List<T> batch, Callback<T> callback, int  batchOrder) {
		getDispatcher().dispatch(getProcessor(batch, callback, batchOrder));
	}
	
	protected QueuedBatchProcessor getProcessor(List<T> batch, Callback<T> callback, int  batchOrder) {
		QueuedBatchProcessor processor = (QueuedBatchProcessor)ServiceLocator.getService(BeansService.class).getBean(getProcessorBean(), batch, callback, getParameters(), batchOrder);
		logger.debug("Processor -> " + processor.getClass().getName());
		return processor;
	}
	
	protected String getProcessorBean() {					
		return getParameter("processor-bean")!=null ? (String) getParameter("processor-bean") : "processor-bean";
	}
	
	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
}
