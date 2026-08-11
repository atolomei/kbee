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
import com.novamens.content.dao.ContentDao;
import com.novamens.dom.Indexable;
import com.novamens.dom.ObjectID;
import com.novamens.security.Identifiable;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeRuntimeException;

public class IndexerAllListIDQueue implements Queue<ObjectID> { 
				
	private Map<String, Object> parameters;
	private long size = -1, i=0;
	private int stament_index=-1;
	protected Iterator<?> iterator = null;
	boolean isInitialized = false;
	int index = -1;
	private Long limit;
	
	private ScrollableResults res;
	
	private List<String> statements = null;
				
	private List<ObjectID> result_kobject_ids = null;
	
														
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(IndexerAllListIDQueue.class.getName());
	
	/**
	 * index -> el ultimo item sacado de la cola
	 * -1 no se saco ninguno
	 */
	
	@Override
	public synchronized ObjectID dequeue() throws QueueException {
		
		if (!isInitialized) 
			throw new KbeeRuntimeException("must be initialized");
		
		if (result_kobject_ids==null)
			return null;
		
		if (index < result_kobject_ids.size()-1) {
			index = index + 1;
			return result_kobject_ids.get(index);	
		}
		
		index=-1;
		loadList();
		
		if (result_kobject_ids==null || result_kobject_ids.size()==0)
			return null;
		
		index = index + 1;
		return result_kobject_ids.get(index);	
		

		
		
	}
	
	public void enqueue(ObjectID file) throws QueueException { 
		throw new QueueException("not implemented");
	}
	
	public void remove(ObjectID file) throws QueueException {
		throw new QueueException("not implemented");
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
	
		if (limit!=null)
			return limit;
		
		synchronized (this) {
		 limit = Long.valueOf( (getParameters()!=null && getParameters().get("limit")!=null) ? Long.valueOf((String)getParameters().get("limit")) : -1);
		 return limit;
		}
	}
	
	
	
	
	private void loadQuery(String statement) {
		
		try {
			
			logger.debug("loadQuery -> " + statement);

			com.novamens.hibernate.session.Session.open();
			ServiceLocator.getService(SecurityService.class).authenticate("root@kbee");
			logger.debug("Session opened");
			
			Query<?> query = getQuery(statement);
			query.setFetchSize(Integer.valueOf(1600));
			query.setReadOnly(true);
			query.setLockMode("a", LockMode.NONE);
			
			res = query.scroll(ScrollMode.FORWARD_ONLY);
			result_kobject_ids = new ArrayList<ObjectID>();
			
			while (res.next()) {
				try {
					Object obj=res.get(0);
					if (obj instanceof Identifiable) {
						ObjectID oid = new ObjectID((Identifiable) obj);
						logger.debug(obj.getClass().getName() + " -> " + oid.toString());
						result_kobject_ids.add(oid);	
					}
					else {
						logger.error( "Not Identifiable -> " + obj.getClass().getName());
					}
				} catch (Exception e) {
					logger.error(e);
				}
			}
			
		} finally {
			
			try {
				
				if (res!=null)
					res.close();
				
				com.novamens.hibernate.session.Session.close();
				logger.debug("Session closed");
				logger.debug("end loadQuery() -> " + result_kobject_ids.size());
				logger.debug("----------------------------------------");
			} catch (Exception e) {
				logger.error(e);
				throw(e);
			}
		}
	}
	
	
	private void loadList() {
		
		try {
			
			logger.debug("loadList()");
			
			
			// stament_index es la ultima que levanto
			//
			if (stament_index==getStatements().size()-1) {
				logger.debug("no more Statements");
				result_kobject_ids = null;
				return;
			}
			
			while ((stament_index<getStatements().size()-1)) {
				loadQuery(getStatements().get(++stament_index));
				if (result_kobject_ids!=null && result_kobject_ids.size()>0)
					return;
			}
			
		} catch (Exception e) {
			logger.error(e);
			throw(e);
			
		}
	}

	private Query<?> getQuery(String statement) {
		BeansService beans = ServiceLocator.getService(BeansService.class);
		logger.debug("query -> " + statement);
		SessionFactory sf  = (SessionFactory)beans.getBean("sessionFactory");
		return sf.getCurrentSession().createQuery(statement);
	}
	
	/**
	 * Must be called by the thread that runs QueueProcessor
	 */
	protected synchronized void init() {
		if (!isInitialized) { 
				this.size=calculateSize();
				loadList();
				isInitialized=true;
		}
		
	}
	
	/**
	 * @return
	 */
	private synchronized long calculateSize() {
		try {
			
			
			
			logger.debug("----------------------------------------");
			
			logger.debug("calculateSize()");

			com.novamens.hibernate.session.Session.open();
			ServiceLocator.getService(SecurityService.class).authenticate("root@kbee");
			logger.debug("Session opened");

			long queue_size = 0;
			
			BeansService beans = ServiceLocator.getService(BeansService.class);
			SessionFactory sf = (SessionFactory)beans.getBean("sessionFactory");
			
			int index=0;
			
			for (String statement : getStatements()) {
			
				long size = 0;
				
				if (statement.toLowerCase().contains("order by")) {
					int end = statement.toLowerCase().split("order by")[0].length();
					String st = statement.substring(0, end);

					logger.debug("select count(*) " + st);
					size=((Long)sf.getCurrentSession().createQuery("select count(*) " + st).uniqueResult()).longValue();
					queue_size += size;
					
				}
				else { 
					logger.debug("select count(*) " + statement);
					size=((Long) sf.getCurrentSession().createQuery("select count(*) " + statement).uniqueResult()).longValue();
					queue_size += size;
				}	
					
				
				logger.debug("Statement [ " + String.valueOf(index++) + " ] -> " + statement + " -> " + String.valueOf(size));
				
				if (getLimit()>0 && (queue_size > getLimit())) { 
					queue_size = getLimit();
				}
				
			}
			
			logger.debug("Queue size -> " + String.valueOf(queue_size));
			return queue_size;
			
		}
		catch (Exception e) {
			logger.error(e);
			throw new KbeeRuntimeException(e);
		}
		
		finally {
		
			com.novamens.hibernate.session.Session.close();
			
			logger.debug("Session closed");
			
			logger.debug("end calculateSize()");
			
			logger.debug("----------------------------------------");
		}
	}
	
	
	
	
	
	//private Object load(ObjectID object) {
	//	return null;
		// return getContentDao().findObjectById(object);
	//}

	
	
	public void close() {
		//if (this.results!=null)
		//	this.results.close();
		isInitialized = false;
	}	

	//	private ContentDao getContentDao() {
	//				return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	//	}
	
}
