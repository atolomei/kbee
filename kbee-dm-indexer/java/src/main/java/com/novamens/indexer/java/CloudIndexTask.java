package com.novamens.indexer.java;

import java.io.Serializable;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.base.ResourceContainer;
import com.novamens.indexer.service.Index;
import com.novamens.security.Identifiable;
import com.novamens.service.ServiceLocator;

import kbee.api.model.ApiEvent;
import kbee.api.model.ApiEventType;
import kbee.api.model.ApiFile;
import kbee.api.model.ApiObject;
import kbee.api.model.ApiResource;
import kbee.api.service.ApiSerializer;
import kbee.queue.QueueService;

public class CloudIndexTask extends ObjectIndexTaskServiceRequest {
	private static final long serialVersionUID = 1L;
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(CloudIndexTask.class.getName());
	
	public CloudIndexTask(Object object, Index index) {
		super(object, index);
		setName("IndexTask");
	}
	
	public CloudIndexTask(Object object, Index index, boolean force) {
		super(object, index);
		setName("IndexTask");
	}
	
	@Override
	public synchronized void execute() {
		try {
 			assert(detached());
 			
 			if (getObject() instanceof ResourceContainer) {
 				ApiFile file = (ApiFile)serialize(getObject());
 				for (ApiResource resource : file.getResources()) {
 					ApiEvent event = ApiEvent.builder()
 						.type(ApiEventType.Update)
 						.object(resource)
 						.build();
 					ServiceLocator
 						.getService(QueueService.class)
 						.sendMessage("indexer.kbee", event);
 				}
				ApiEvent event = ApiEvent.builder()
					.type(((Content)getObject()).isHeadVersion() 
						? ApiEventType.CheckIn
						: ApiEventType.Update)
					.object(file)
					.build();
				ServiceLocator
					.getService(QueueService.class)
					.sendMessage("indexer.kbee", event);
 			}
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
	
	private ApiObject serialize(Object object) {
		return ((ApiSerializer)ServiceLocator
			.getService(BeansService.class)
			.getBean("ApiSerializer"))
			.serialize(object);
	}
}