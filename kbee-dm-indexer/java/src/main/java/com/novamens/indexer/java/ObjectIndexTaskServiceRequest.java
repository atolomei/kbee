package com.novamens.indexer.java;

import java.util.Map;

import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.model.ObjectId;
import com.novamens.dom.ObjectID;
import com.novamens.indexer.service.Index;
import com.novamens.scheduler.SchedulerService;
import com.novamens.security.Identifiable;
import com.novamens.util.KbeeRuntimeException;

public abstract class ObjectIndexTaskServiceRequest extends AbstractIndexerTaskServiceRequest {
	
	private static final long serialVersionUID = 1L;
	
	private transient Object object;
	private ObjectId objectId;
												
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ObjectIndexTaskServiceRequest.class.getName());
	
	public ObjectIndexTaskServiceRequest(Object object, Index index) {
		super(index);
		setName("ObjectIndexTask");
			try {
				this.object = object;
				this.objectId = new ObjectId(object);
				if (object instanceof Identifiable)
					setObjectID(new ObjectID((Identifiable) object).toString());
			} catch (Exception e) {
				if ( object!=null && object instanceof Identifiable)
					logger.error(e, ((Identifiable) object).getDisplayName() + " (" + ((Identifiable) object).getId().toString()+")");
				else
					logger.error(e);
			}
		
	}
	
	
	public ObjectIndexTaskServiceRequest(Map<String, String> map) {
		super();
		setName("ObjectIndexTask");
		setPriority(SchedulerService.LOW_PRIORITY);
		throw new KbeeRuntimeException("not done");
		// ---------
		//	object, index ?
		// -----------
	}

	
	public ObjectId getObjectId() {
		return this.objectId;
	}
	
	public Object getObject() {

		if (this.object==null) {
			try {

				this.object = getContentDao().findObjectById(objectId);
			} 
			catch (ContentMgmtException e) {
				if ( objectId!=null)
					logger.error(e, objectId.toString());
				else
					logger.error(e);
				this.object=null;
			}
		}	
		return this.object;
	}
	
	protected void detach() {
		this.object = null;
	}
	
	protected boolean detached() {
		return this.object == null;
	}
	
	@Override
	public String toString() {
		if (getObjectId()!=null)
			return super.toString() + " | OId: " + getObjectId().toString();
		return super.toString();
	}
}
