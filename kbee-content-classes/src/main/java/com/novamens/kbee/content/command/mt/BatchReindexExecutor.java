package com.novamens.kbee.content.command.mt;

import java.util.List;
import java.util.Map;

import org.hibernate.SessionFactory;
import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.resource.KBFile;
import com.novamens.dom.Domain;
import com.novamens.dom.DomainObject;
import com.novamens.dom.Indexable;
import com.novamens.dom.ObjectID;
import com.novamens.event.LogEvent;
import com.novamens.indexer.java.FileIndexerService;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.service.IndexerException;
import com.novamens.indexer.service.JavaIndex;
import com.novamens.security.Identifiable;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

/**
  @see BatchReindexExecutor
  @see QueuedBatchProcessor
  @see BatchReindexCommand
  @see QueueProcessorCommand
 */
public class BatchReindexExecutor extends QueuedBatchProcessor {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(BatchReindexExecutor.class.getName());
	
	private List<ObjectID> batch;
	private Callback<ObjectID> callback;
	private Domain domain;
	private Domain domainKbee;
	private int batchNumber=0;
	private String current_user = null;
	private Map<String, Object> parameters;
	private boolean onlymetainfo = false;
	private long total_indexed = 0;
	

	public BatchReindexExecutor(List<ObjectID> batch, Callback<ObjectID> callback, Map<String, Object> parameters, int batchNumber) {
		IncrementInstances();
		setBatch(batch);
		setCallback(callback);
		setParameters(parameters);
		onlymetainfo = "false".equals(parameters.get("include-attachments"));
		this.batchNumber = batchNumber;
	}
	
	
	/**
	 * 
	 */
	public void run() {
		try {
			
			logger.debug("Starting Batch -> " + String.valueOf(this.batchNumber));
			logger.debug("Open Hibernate Session");
			
			com.novamens.hibernate.session.Session.open();
			
			int errors = 0;
			int msize=getBatch().size() / 2 + 1;			
			long start = 0;
			total_indexed = 0;
			
			
			for (ObjectID obj : getBatch()) {
			
				try {
					
					if (logger.isDebugEnabled())
						start = System.currentTimeMillis();

					Object object = load(obj);
					index(object);

					// ---------
					 if (logger.isDebugEnabled()) { 
					  	print(obj, start, System.currentTimeMillis());
					 }
					// --------
				}
				
				catch(IndexerException e) {
					logger.error(e, " | obj -> " + (obj!=null? obj.toString(): "null"));
				}				
				catch(Exception e) {
					
					errors++;
					logger.error(e.getClass().getName() +  " | obj -> " + (obj!=null? obj.toString(): "null"));
					
					if (logger.isDebugEnabled()) {
						logger.error(e);	
					}
					
					if (errors>=msize)
						break;
				}
				finally {
					if (total_indexed++%100==0) {
						((SessionFactory)ServiceLocator.getService(BeansService.class).getBean("sessionFactory")).getCurrentSession().clear();
					}
				}
			}
		}
		
		catch(Exception e) {
			logger.error(e);
		}
		
		finally {
			
			if (getIndex()!=null) {
				getIndex().commit();
			}
			
			if (getFileIndex()!=null) {
				getFileIndex().commit();
			}
			try {
					
				logger.debug("Callback for every item");
					
				if (getCallback()!=null) { 
						for (ObjectID content : getBatch()) {
							try {
									if (getCallback()!=null) {
										getCallback().execute(content);
										logger.debug("done Callback for -> " + content.toString());
									}
							}
							catch (Exception callbackexception) {
								logger.error(callbackexception);
								callbackexception.printStackTrace();
							}
						}
					}
			} catch (Exception e) {
				logger.error(e);
			}
			
			DecrementInstances();
			
			com.novamens.hibernate.session.Session.close();
			
			logger.debug("done batch -> " + String.valueOf(this.batchNumber));
			
		}
	}
	
	public List<ObjectID> getBatch() {
		return batch;
	}
	
	public void setBatch(List<ObjectID> batch) {
		this.batch = batch;
	}
	
	public void setCallback(Callback<ObjectID> callback) {
		this.callback = callback;
	}
	
	public Callback<ObjectID> getCallback() {
		return callback;
	}
	
	public void setParameters(Map<String, Object> parameters) {
		this.parameters = parameters;
	}
	
	public Map<String, Object> getParameters() {
		return this.parameters;
	}
	
	private Domain getDomain() {
		return domain;
	}
	
	private void index(Object obj) throws IndexerException {
		
		if (obj instanceof DomainObject) {
			
			Domain domain = ((DomainObject) obj).getDomain();
			
			if (getDomain()==null || !getDomain().equals(domain))
				su(domain);
			
			JavaIndex index = getIndex(obj);
			index.index(obj, onlymetainfo, true, true);
		}
		else {
			
			Domain domain = getDomainKbee();
			
			if (getDomain()==null || !getDomain().equals(domain))
				su(domain);
			
			JavaIndex index = getIndex(obj);
			index.index(obj, onlymetainfo, true, true);
		}
	}
	
	private void su(Domain domain) {
 		this.domain = domain;
		String suusername;
		suusername = "root@" + domain.getName();

		if (current_user!=null && current_user.equals(suusername))
			return;
			
  		ServiceLocator.getService(SecurityService.class).authenticate(suusername);
  		 current_user=suusername;
	
	}
	
	
	private Domain getDomainKbee() {
		if (domainKbee==null)
			domainKbee = getContentDao().findDomainByName("kbee"); 
		return domainKbee; 
	}
	
	private Indexable load(ObjectID object) {
		return (Indexable) getContentDao().findObjectById(object);
	}

	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	private JavaIndex getIndex(Object obj) {
		JavaIndex index = obj instanceof KBFile ? (JavaIndex) getDomain().getService(FileIndexerService.class).getIndex() :
			(JavaIndex) getDomain().getService(JavaIndexerService.class).getIndex();
		return index;
	}
	
	private JavaIndex getIndex() {
		JavaIndex index = (JavaIndex) getDomain().getService(JavaIndexerService.class).getIndex();
		return index;
	}
	
	private JavaIndex getFileIndex() {
		JavaIndex index = (JavaIndex) getDomain().getService(FileIndexerService.class).getIndex();
		return index;
	}
	
	private void print( Object content, long start, long end) {
		if (content instanceof Content) {
				StringBuilder str  = new StringBuilder();
				str.append( " [ "+String.format("%4d", end-start) + " ms | " );
				Content xcontent = ((Content) content); 
				str.append("Batch: " + String.format("%6d", this.batchNumber));
				str.append(" | Id: " + String.format("%12d", xcontent.getOId())+" / "+ String.format("%12d", xcontent.getId()));
				str.append(" | " + String.format("%12s", xcontent.getContentTemplate().getContentClassCode()));		
				str.append(" | " + (xcontent.getTitle()!=null?xcontent.getTitle():"null"));
				logger.debug(str.toString());
			
		}
		else if (content instanceof LogEvent) {
			
				StringBuilder str  = new StringBuilder();
				str.append( " [ "+String.format("%4d", end-start) + " ms | ");
				LogEvent o = (LogEvent) content; 
				str.append("Batch: " + String.format("%6d", this.batchNumber));
				str.append(" | Id: " + String.format("%16d", o.getId()));
				str.append(" | " + o.toString());
				logger.debug(str.toString());							
		}
		else if (content instanceof Identifiable) {
			
				StringBuilder str  = new StringBuilder();
				str.append( " [ "+String.format("%4d", end-start) + " ms - | ");
				Identifiable o = (Identifiable) content; 
				
				str.append("Batch: " + String.format("%6d", this.batchNumber));
				str.append(" | Id: " + String.format("%16d", o.getId()));
				
				if (o.getDisplayName()!=null)
					str.append(" | " + (o.getDisplayName().length()<80?o.getDisplayName():o.getDisplayName().substring(0,80)));
				else
					str.append(" | name -> is null" );
				logger.debug(str.toString());
		}
		
		else {
			
			StringBuilder str  = new StringBuilder();
			str.append( " [ "+String.format("%4d", end-start) + " ms - | ");
			str.append("Batch: " + String.format("%6d", this.batchNumber));
			str.append(" | " + content.getClass().getName() );
			str.append(" | " + content.toString());
			
			logger.debug(str.toString());
		}
	}
}
 