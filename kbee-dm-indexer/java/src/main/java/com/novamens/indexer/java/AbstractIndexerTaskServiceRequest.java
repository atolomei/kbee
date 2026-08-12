package com.novamens.indexer.java;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.dom.Domain;
import com.novamens.indexer.service.Index;
import com.novamens.indexer.service.IndexProxy;
import com.novamens.scheduler.AbstractServiceRequest;
import com.novamens.service.ServiceLocator;

/**
 * 
 * 
 * Service Request to index
 *
 */
public abstract class AbstractIndexerTaskServiceRequest extends AbstractServiceRequest implements IndexerTask {

	private static final long serialVersionUID = 1L;
	private Index index;
	
	
	public AbstractIndexerTaskServiceRequest() {
		this.index = getQueryIndex();
	}
	
	
	public AbstractIndexerTaskServiceRequest(Index index) {
		this.index = index;
	}
	
	public abstract void execute();
	
	public Index getIndex() {
		return ((IndexProxy)index).getIndex();
	}
	
	public boolean isTransactional() {
		return true;
	}
	
	public boolean isSynchronous() {
		return true;
	}
	
	public int getPriority() {
		return 1;
	}
	
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append(getName()!=null? (getName()+  " | "):"");
		str.append(super.toString());
		return str.toString();	
	}
	
	public ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	private Index getQueryIndex() {
		return getDomainKbee().getService(JavaIndexerService.class).getIndex();
	}
	
	private Domain getDomainKbee() {
		return getContentDao().findDomainByName ("kbee");
	}
	

}

