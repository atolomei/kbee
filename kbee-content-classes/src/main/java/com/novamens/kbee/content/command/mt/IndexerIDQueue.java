package com.novamens.kbee.content.command.mt;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hibernate.LockMode;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import com.novamens.beans.BeansService;
import com.novamens.dom.ObjectID;
import com.novamens.security.Identifiable;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeRuntimeException;

public class IndexerIDQueue implements Queue<ObjectID> {
		
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(IndexerIDQueue.class.getName());
	
	private Map<String, Object> parameters;
	private long size = -1;
	
	private int index = -1;
	private ScrollableResults results;
	
	protected Iterator<?> iterator = null;
	
	private String str_param = null;
				
	private List<ObjectID> result_kobject_ids = null;

	boolean isInitialized = false;

	
	@Override
	public synchronized ObjectID dequeue() throws QueueException {

		if (!isInitialized)
				loadList();
		
		if (index>=result_kobject_ids.size()-1)
			return null;
		
		index++;
		return result_kobject_ids.get(index);
	}
	
	public void enqueue(ObjectID file) throws QueueException { 
		throw new UnsupportedOperationException();
	}
	
	public void remove(ObjectID file) throws QueueException {
		throw new UnsupportedOperationException();
	}
	
	
	public long size() throws QueueException {
		if (!isInitialized)
				init();
		return size;
	}

	public void setParameters(Map<String, Object> parameters) {
		this.parameters = parameters;
	}
	
	public Map<String, Object> getParameters() {
		return this.parameters;
	}
	
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
	
	
		
	protected synchronized void init() {
		if (!isInitialized)
				loadList();
	}
	/**
	 * Must be called by the thread that runs QueueProcessor
	 */
	private void calculateSize() {
		 
		try {
			
			if (getStatement()==null) {
				logger.error("Statement is null");
				this.size = 0;
				return;
			}
			
			BeansService beans = ServiceLocator.getService(BeansService.class);
			SessionFactory sf = (SessionFactory)beans.getBean("sessionFactory");
			
			long queue_size = 0;
			
			if (getStatement().toLowerCase().contains("order by")) {
				int end = getStatement().toLowerCase().split("order by")[0].length();
				String st = getStatement().substring(0, end);
				logger.debug("select count(*) " + st);
				queue_size = ((Long)sf.getCurrentSession().createQuery("select count(*) " + st).uniqueResult()).longValue();
			}
			else { 
				String statment = "select count(*) " + getStatement();
				logger.debug(statment);
				queue_size = ((Long) sf.getCurrentSession().createQuery(statment).uniqueResult()).longValue();
			}	
				
			if (getLimit()>0 && (queue_size > getLimit())) { 
				queue_size = getLimit();
			}
			
			logger.debug("Queue Size -> " + String.valueOf(queue_size));
			
			this.size =  queue_size;
		}
		catch (Exception e) {
			logger.error(e, getStatement());
			throw new KbeeRuntimeException(e);
		}
	}
	


	
	
	private void loadList() {
		
		try {
			
			logger.debug("----------------------------------------");
			logger.debug("loadList()");

			com.novamens.hibernate.session.Session.open();
			ServiceLocator.getService(SecurityService.class).authenticate("root@kbee");
			logger.debug("Session opened");

			calculateSize();
			
			BeansService beans = ServiceLocator.getService(BeansService.class);
			logger.debug("query -> " + getStatement());
			SessionFactory sf  = (SessionFactory)beans.getBean("sessionFactory");
			Query<?> query = sf.getCurrentSession().createQuery(getStatement());
			
			
			query.setFetchSize(Integer.valueOf(1600));
			query.setReadOnly(true);
			query.setLockMode("a", LockMode.NONE);
			
			this.results = query.scroll(ScrollMode.FORWARD_ONLY);
			this.result_kobject_ids = new ArrayList<ObjectID>();
			
			while (this.results.next()) {
				Object obj=results.get(0);
				if (obj instanceof Identifiable) {
						ObjectID oid = new ObjectID( (Identifiable) obj);
						this.result_kobject_ids.add(oid);
				
				}
				else {
					logger.error( "Not Identifiable -> " + obj.getClass().getName());
				}
			}
		} finally {
			
			try {
			
				if (this.results!=null)
					this.results.close();
				
			} catch (Exception e) {
				logger.error(e);
			}

			logger.debug("end load list | Items -> " + this.result_kobject_ids.size() );
			
			com.novamens.hibernate.session.Session.close();
			
			logger.debug("Session closed");
			logger.debug("end loadList()");
			logger.debug("----------------------------------------");
			
			isInitialized = true;
		}
	}

	@Override
	public void close() {
		
		if (this.results!=null)
			this.results.close();

		isInitialized = false;
	}
	
}
