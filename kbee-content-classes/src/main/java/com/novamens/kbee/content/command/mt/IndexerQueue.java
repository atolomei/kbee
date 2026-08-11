package com.novamens.kbee.content.command.mt;


import java.util.Iterator;
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
public class IndexerQueue implements Queue<Indexable> {
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(IndexerQueue.class.getName());
	
	private Map<String, Object> parameters;
	private long size = -1, i=0;
	private ScrollableResults results;
	
	protected Iterator<?> iterator = null;
	
	@Override
	public synchronized Indexable dequeue() throws QueueException {
		
		if (!getResults().next()) {
			return null;
		}
		
		Object ob = getResults().get(0);
		ob = reload(ob);
		
		if (i++%1000==0) {
			try {
				((SessionFactory)ServiceLocator.getService(BeansService.class).getBean("sessionFactory")).getCurrentSession().clear();
			}
			catch(Exception e) {
	 		     logger.error(e, getStatement());
			}
		}
		
		return (Indexable) ob;
	}
	
	public void enqueue(Indexable file) throws QueueException {
		throw new UnsupportedOperationException();
	}
	public void remove(Indexable file) throws QueueException {
		throw new UnsupportedOperationException();
	}
	
	
	
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
	
	private String str_param = null;
	
	public String getStatement() {
	
		if (str_param!=null)
			return str_param;
		
		synchronized (this) {
			if (getParameters()!=null && getParameters().containsKey("statement"))
				str_param = (String)getParameters().get("statement");
			else if (getParameters()!=null && getParameters().containsKey("query"))
				str_param = (String)getParameters().get("query");
			return str_param;
		}
	}
	
	public long getLimit() {
		return (getParameters()!=null && getParameters().get("limit")!=null) ? Long.valueOf((String)getParameters().get("limit")) : -1;
	}
	
	public void close() {
		getResults().close();
	}
		
	@SuppressWarnings("deprecation")
	protected Iterator<?> getIterator() {
		if (iterator==null) {
			iterator = getQuery().iterate();
		}
		return iterator;
	}
	
	protected ScrollableResults getResults() {
		if (results==null) {
			Query<?> query = getQuery();
			query.setFetchSize(Integer.valueOf(1600));
			query.setReadOnly(true);
			query.setLockMode("a", LockMode.NONE);
			results = query.scroll(ScrollMode.FORWARD_ONLY);
		}
		return results;
	}

	
	protected Query<?> getQuery() {
		BeansService beans = ServiceLocator.getService(BeansService.class);
		logger.debug("query -> " + getStatement());
		SessionFactory sf  = (SessionFactory)beans.getBean("sessionFactory");
		return sf.getCurrentSession().createQuery(getStatement());
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
			
			if (getStatement()==null) {
				logger.error("Statement is null");
				return 0;
			}
			
			if (getStatement().toLowerCase().contains("order by")) {
				int end = getStatement().toLowerCase().split("order by")[0].length();
				String st = getStatement().substring(0, end);
				queue_size = ((Long)sf.getCurrentSession().createQuery("select count(*) " + st).uniqueResult()).longValue();
			}
			else { 
				String statment = "select count(*) " + getStatement();
				queue_size = ((Long) sf.getCurrentSession().createQuery(statment).uniqueResult()).longValue();
			}	
				
			if (getLimit()>0 && (queue_size > getLimit())) { 
				queue_size = getLimit();
			}
			
			return queue_size;
		}
		catch (Exception e) {
			logger.error(e, getStatement());
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
