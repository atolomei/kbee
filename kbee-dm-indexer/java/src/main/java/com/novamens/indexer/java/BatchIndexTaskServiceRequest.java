package com.novamens.indexer.java;


import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.ResultSet;
import com.novamens.indexer.service.Index;
import com.novamens.indexer.service.IndexerException;
import com.novamens.scheduler.SchedulerService;
import com.novamens.util.KbeeRuntimeException;
		
public class BatchIndexTaskServiceRequest extends AbstractIndexerTaskServiceRequest {
	private static final long serialVersionUID = 1L;
	
	private Query query;
	
	private AtomicBoolean is_stop = new AtomicBoolean(false);
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(BatchIndexTaskServiceRequest.class.getName());
	
	
	
	public BatchIndexTaskServiceRequest(Query query, Index index) {
		super(index);
		this.query = query;
		setName("BatchIndexTask");
		setPriority(SchedulerService.LOW_PRIORITY);
	}


	public BatchIndexTaskServiceRequest(Map<String, String> map) {
		super();
		setName("BatchIndexTask");
		setPriority(SchedulerService.LOW_PRIORITY);
		throw new KbeeRuntimeException("not done");
		//		query ???
	}
	
	
	
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append(super.toString());
		str.append(" | " + (this.query!=null?this.query.toString():""));
		return str.toString();	
	}

	public Object getObject() {
		return null;
	}
	
	@Override
	public void execute() {
		ResultSet resultSet = null;
		try {
			resultSet = query.execute();
			while (resultSet.hasNext() && !isStopped()) {
				Object object = resultSet.next().getObject();
				if (object!=null) {
					((KbeeJavaIndex)this.getIndex()).index(object);
				}
			}
			this.getIndex().commit();
			 
		}
		
		catch (IndexerException e) {
			logger.error(e);
			throw new RuntimeException(e);
		}
		finally {
 			if (resultSet!=null)
				resultSet.close();
		}
	}
	
	@Override
	public int getPriority() {
		return 2;
	}
	
	private boolean isStopped() {
		return is_stop.get();
	}

	@Override
	public void stop() {
		is_stop.set(true);
	}
	


}
