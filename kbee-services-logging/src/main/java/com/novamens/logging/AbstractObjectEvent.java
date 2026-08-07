package com.novamens.logging;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;

import com.novamens.content.base.DomainProxy;
import com.novamens.content.model.ObjectId;
import com.novamens.dom.Domain;
import com.novamens.dom.DomainObject;
import com.novamens.event.LogEvent;

/**  
 *   Events that are saved in the Activity Log
 *   {@link LogEvent}
 */
@Entity
public abstract class AbstractObjectEvent extends AbstractLogEvent implements DomainObject {
	
	@Column(name = "EVENT_TITLE")
	private String title;
	
	// Object Id includes the KbeeClass and the Id
	@Column(name = "EVENT_OBJECT_ID")
	private String objectId;
	
	// it seems that this is the high level class to display in the Audit Log (File, KBase File, Property..)
	@Column(name = "EVENT_KBEECLASS")
	private String kbeeclass;
	
	@Column(name = "EVENT_TASK")
	private String task;
	
	@Column(name = "EVENT_PROCEDURE")
	private String procedure;
	
	@Column(name = "EVENT_DOMAIN_ID")
	private Long domainId;
	
	private transient Domain domain;
	
	public AbstractObjectEvent() {
		super();
	}
	
	public AbstractObjectEvent(String id) {
		super();
		setObjectId(id);
	}
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getTask() {
		return task;
	}

	public void setProcedure(String procedure) {
		this.procedure = procedure;
	}

	public String getProcedure() {
		return procedure;
	}

	public void setTask(String task) {
		this.task = task;
	}
	
	public String getObjectId() {
		return objectId;
	}

	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}
	
	public void setKbeeClass(String kb) {
		kbeeclass=kb;
	}
	
	public String getKbeeClass() {
		return kbeeclass;
	}
	
	public Long getDomainId() {
		return domainId;
	}

	public void setDomainId(Long domainId) {
		this.domainId = domainId;
	}
	
	public Domain getDomain() {
		if (domain==null) domain = new DomainProxy(domainId);
		return domain;
	}

	public void setDomain(Domain domain) {
		this.domain = domain;
		if (domain!=null)
		setDomainId((Long)domain.getId());
	}
	
	public void setObject(com.novamens.dom.Object object) {
		setObjectId((new ObjectId(object)).toString());
		String kbeeclass = object.getClass().getSimpleName();
		if (kbeeclass.startsWith("Kbee")) 
			kbeeclass = kbeeclass.substring(4);
		setKbeeClass(kbeeclass);
		if (object instanceof DomainObject)
		setDomainId((Long)((DomainObject)object).getDomain().getId());
		setTitle(object.getDisplayName());
	}
	
	protected String getDescription(List<String> updatedParts) {
		StringBuilder description = new StringBuilder();
		for (String part : updatedParts) {
			if (description.length()>0)
				description.append(", ");
			description.append(part);
		}
		return description.toString();
	}
	
}
