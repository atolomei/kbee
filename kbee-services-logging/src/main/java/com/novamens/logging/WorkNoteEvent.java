package com.novamens.logging;

import javax.persistence.Entity;
import javax.persistence.Transient;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.novamens.content.model.ObjectId;
import com.novamens.content.notes.Billboard;
import com.novamens.security.audit.AuditSet;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

@Entity
public abstract class WorkNoteEvent extends AbstractObjectEvent {
			
	@Transient
	static private Logger logger = LogManager.getLogger(WorkNoteEvent.class.getName());
	
	@Transient
	private boolean is_silent = false;
	
			
			
	static public String getClassEventType() {
		return "Work Note";
	}
	
	public WorkNoteEvent() {
		super();
		setAuditSet(AuditSet.DOMAIN_ADMIN);
 	}
	
	
	
	public WorkNoteEvent(Billboard note, boolean is_silent) {
		super();
		setAuditSet(AuditSet.DOMAIN_ADMIN);
		setKbeeClass(getClassEventType());
		setEventUser(ServiceLocator.getService(SecurityService.class).getSessionUser());
		setWorkNote(note);
		setSilent(is_silent);
	}
	

	public void setSilent(boolean b) {
		this.is_silent=b;
	}
	
	
	public boolean isSilent() {
		return this.is_silent;
	}
	
	@Override
	public String getDisplayName() {
		return getTitle();
	}
	
	public void setWorkNote(Object object) {
	
		if (object instanceof Billboard) {
			Billboard note = (Billboard) object;
			setObjectId((new ObjectId(note)).toString());
			setDomain(note.getDomain());
			setDomainId((Long)(note.getDomain().getId()));
			String title= note.getTitle();
			if ((title!=null) && (title.length()>255))
				title=title.substring(0, 252)+"...";
			setTitle(title);
		}
	}
	
	
	
	@Override
	public String toString() {
		return getAction()+ " | " + getTarget();
	}
	
	@Override
	public String getTarget() {
		return getKbeeClass() + " - " + getObjectId().toString(); 
	}
	
	@Override
	public String getType() {
		return getClassEventType();
	}
	
	@Deprecated
	@Override
	public String getEventType() {
		return getType();
	}
	
	@Override
	public String getObjectClass() {
		return getClassEventType();
	}
	
	@Override
	public boolean isNotifiable() {
		return true;
	}
}
