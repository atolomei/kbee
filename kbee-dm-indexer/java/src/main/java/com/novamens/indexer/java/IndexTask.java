package com.novamens.indexer.java;

import java.io.Serializable;
import java.time.Instant;
 
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.novamens.content.base.Content;
import com.novamens.dom.Versionable;
import com.novamens.indexer.service.Index;
import com.novamens.indexer.service.IndexerException;
import com.novamens.metrics.SystemMetricsService;
import com.novamens.security.Identifiable;
import com.novamens.service.ServiceLocator;
import com.novamens.spring.transaction.TransactionSynchronization;
import com.novamens.util.KbeeRuntimeException;

public class IndexTask extends ObjectIndexTaskServiceRequest {

	private static final long serialVersionUID = 1L;
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(IndexTask.class.getName());
	
	private static Map<Thread, TransactionSynchronization> transactions = Collections.synchronizedMap(new HashMap<Thread, TransactionSynchronization>());
	
	private Instant version;
	
	// indexa siempre independientemente de la version del objeto
	//
	private boolean force = false;
	
	
	public IndexTask(Object object, Index index) {
		super(object, index);
		setName("IndexTask");
		setVersion(object);
	}
	
	public IndexTask(Object object, Index index, boolean force) {
		super(object, index);
		setName("IndexTask");
		setVersion(object);
		this.force = force;
	}
	
	@Override
	public synchronized void execute() {
		try {
 			assert(detached());
			
			//boolean onlymetainfo = (getObject() instanceof Versionable) && ((Versionable<?>)getObject()).isHeadVersion() ? false : true;
			boolean onlymetainfo = !(getObject() instanceof Versionable) || ((Versionable<?>)getObject()).isHeadVersion() ? false : true;
			
			if (checkVersion(getObject()) || force) {
				((KbeeJavaIndex)this.getIndex()).index(getObject(), onlymetainfo, false, !onlymetainfo || force);
				addTransactionSynchronization();
			}
				else {
				logger.warn("Version Error:", toString());
			}
 		}
		catch (IndexerException e) {
			logger.error(e, getDescription(e));
			throw new KbeeRuntimeException(e);
		}
		catch (java.lang.OutOfMemoryError e) {
			logger.error(e);
			ServiceLocator.getService(SystemMetricsService.class).setTimeOutOfMemoryFlag();
			throw e;
		}
		catch (Exception e) {
			logger.error(e, getDescription(e));
			throw e;
		}
		finally {
			detach();
		}
	}
	
	public String toString() {
		try {
			String s = null;
			if (	getObject() instanceof Content && 
					((Content)getObject()).getOId()!=null && 
					((Content)getObject()).getId()!=null) {
				
				String t;
				if(((Content)getObject()).getTitle()!=null)
					t=" | "+((Content)getObject()).getTitle();
				else
					t="";
				
				s=((Content)getObject()).getOId().toString()+"/"+((Content)getObject()).getId().toString()+" " + t;
				
			}
			else
				s="";
				s = super.toString() + " | " + s;
				
			detach();
			
			return s;
			
		} catch (Exception e)  {
			logger.error(e);
			return "error in toString()";
		}
		
	}
	
	public boolean isSynchronous() {
		return false;
	}
	
	protected void setVersion(Object object) {
		version = getVersion(object);
	}
	
	protected boolean checkVersion(Object object) {
		return object!=null && (version==null || version.compareTo(getVersion(object))==0);
	}
	
	protected Instant getVersion(Object object) {
		return object instanceof com.novamens.dom.Object ? ((com.novamens.dom.Object)object).getLastModifiedOffsetDateTime().toInstant() : null;
	}
	
	protected String getDescription(Exception e) {
		StringBuilder str = new StringBuilder();
		if (getObject()!=null) {
			if ( getObject() instanceof Identifiable) {
				String dn = ((Identifiable) getObject()).getDisplayName();
				Serializable id = ((Identifiable) getObject()).getId();
				str.append(dn!=null?dn:"(null display)" + " (" + id!=null?id:"(null id)" +")");
			}	
			else
				str.append(getObject().toString());
		}
		return str.toString();
	}
	
	protected void addTransactionSynchronization() {
		if (transactions.get(Thread.currentThread()) == null) {
			transactions.put(Thread.currentThread(), new TransactionSynchronization() {
				public void afterCompletion(int status) {
					try {
						if (status == STATUS_COMMITTED) {
							getIndex().commit();
						}
					}
					catch (IndexerException e) {
						logger.error(e);
						throw new KbeeRuntimeException(e);
					}
					catch (RuntimeException e) {
						logger.error(e);
						throw new KbeeRuntimeException(e);
					}
					finally {
						transactions.remove(Thread.currentThread());
					}
				}
			});
		};
	}
}