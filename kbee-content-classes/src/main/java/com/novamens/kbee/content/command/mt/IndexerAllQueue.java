package com.novamens.kbee.content.command.mt;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hibernate.LockMode;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.dom.Indexable;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeRuntimeException;


/**
 * Indexable 
 */
public class IndexerAllQueue implements Queue<Indexable> {
			
	private Map<String, Object> parameters;
	private long size = -1, i=0;
	private int stament_index=-1;
	protected Iterator<?> iterator = null;
	private ScrollableResults results;
	private List<String> statements = null;
	
														
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(IndexerAllQueue.class.getName());
	
	@Override
	public synchronized Indexable dequeue() throws QueueException {
		
		Object object = getNextObject();
		
		if (object==null) 
			return null;
		
		object = reload(object);
		
		if (i++%1000==0) {
			try {
				((SessionFactory)ServiceLocator.getService(BeansService.class).getBean("sessionFactory")).getCurrentSession().clear();
			}
			catch(Exception e) {
	 		     logger.error(e, getStatements().get(stament_index));
			}
		}
		
		return (Indexable) object;
	}
	
	public void enqueue(Indexable file) throws QueueException {}
	
	public void remove(Indexable file) throws QueueException {}
	
	
	public long size() throws QueueException {
		if (size<=0) {
			this.size=calculateSize();
		}	
		return size;
	}
		
	public void setParameters(Map<String, Object> parameters) {
		this.parameters = parameters;
	}
	
	public Map<String, Object> getParameters() {
		return this.parameters;
	}
	
	@SuppressWarnings("unchecked")
	public List<String> getStatements() {
		
		if (statements!=null)
			return statements;
		
		synchronized (this) {
			if (getParameters()!=null && getParameters().containsKey("statements"))
				statements = (List<String>)getParameters().get("statements");
			return statements;
		}
	}
	
	public long getLimit() {
		return (getParameters()!=null && getParameters().get("limit")!=null) ? Long.valueOf((String)getParameters().get("limit")) : -1;
	}
	
	public void close() {
	}
	
	protected Object getNextObject() {
		
		while (results==null || !results.next()) {
			if (results!=null) {
				results.close();
				results = null;
			}
			
			if (stament_index==getStatements().size()-1) 
				return null;
			
			Query<?> query = getQuery(getStatements().get(++stament_index));
			
			query.setFetchSize(Integer.valueOf(1600));
			
			query.setReadOnly(true);
			
			query.setLockMode("a", LockMode.NONE);
			
			results = query.scroll(ScrollMode.FORWARD_ONLY);
			
		}
		Object object = results.get(0);
		return object;
	}
	
	protected Query<?> getQuery(String statement) {
		BeansService beans = ServiceLocator.getService(BeansService.class);
		logger.debug("query -> " + statement);
		SessionFactory sf  = (SessionFactory)beans.getBean("sessionFactory");
		return sf.getCurrentSession().createQuery(statement);
	}
	
	/**
	 * Must be called by the thread that runs QueueProcessor
	 */
	protected synchronized void init() {
		this.size=calculateSize();
	}
	
	protected synchronized long calculateSize() {
		try {
			
			long queue_size = 0;
			BeansService beans = ServiceLocator.getService(BeansService.class);
			SessionFactory sf = (SessionFactory)beans.getBean("sessionFactory");
			
			for (String statement : getStatements()) {
				if (statement.toLowerCase().contains("order by")) {
					int end = statement.toLowerCase().split("order by")[0].length();
					String st = statement.substring(0, end);
					queue_size += ((Long)sf.getCurrentSession().createQuery("select count(*) " + st).uniqueResult()).longValue();
				}
				else { 
					queue_size += ((Long) sf.getCurrentSession().createQuery("select count(*) " + statement).uniqueResult()).longValue();
				}	
					
				if (getLimit()>0 && (queue_size > getLimit())) { 
					queue_size = getLimit();
				}
			}
			
			return queue_size;
		}
		catch (Exception e) {
			logger.error(e);
			throw new KbeeRuntimeException(e);
		}
	}
	
	private Object reload(Object object) {
		return getContentDao().reload(object);
	}

	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
}
