package com.novamens.logging;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;

import com.novamens.content.base.Content;
import com.novamens.content.base.Resource;
import com.novamens.content.model.ObjectId;
import com.novamens.content.resource.KBFile;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.security.audit.AuditSet;

@Entity
public abstract class ContentEvent extends AbstractObjectEvent {

	 // This is the Content OId
	// the ObjectId in the parent class contains the KbeeClass and Id 
	@Column(name = "EVENT_CONTENT_ID")
	private long contentOId;

	// This is the Content Id
	//
	@Column(name = "EVENT_CONTENT_XID")
	private long contentId;
	
	@Column(name = "EVENT_VERSION")
	private int version;

	@Column(name = "EVENT_ACTIVITY_ID")
	private Long event_activity_id;

	@Column(name = "EVENT_RESOURCE_ID")
	private Long resourceId;
	
	public ContentEvent() {
		super();
		setAuditSet(AuditSet.CONTENT);
	}
	
	public ContentEvent(Content content) {
		super();
		setContent(content);
		setAuditSet(AuditSet.CONTENT);
	}
	
	public Serializable getActivityId() {
		return this.event_activity_id;
	}
	
	public void setActivityId(Serializable id) {
		if (id!=null)
		event_activity_id = Long.valueOf((long) id);
	}
	
	public void setContent(Object object) {
		if (object instanceof Content) {
			
			Content content = (Content)object;
			setKbeeClass(content.getContentTemplate().getName());
			setObjectId((new ObjectId(content)).toString());
			setDomain(((Content)content).getDomain());
			setDomainId((Long)(content.getDomain().getId()));
			
			setContentOId((Long)content.getOId());
			setContentId((Long)content.getId());
			
			String title= content.getTitle(); 
			if ((title!=null) && (title.length()>255))
				title=title.substring(0, 252)+"...";
			setTitle(title);
			setVersion(content.getVersion());
			
			try {
				if (content.getService(WorkflowService.class)!=null && content.getService(WorkflowService.class).getTask()!=null) {
					StringBuilder task = new StringBuilder();  
					task.append(content.getService(WorkflowService.class).getContext().getProcedure().getName());
					task.append(". ");
					task.append(content.getService(WorkflowService.class).getTask().getName());
					setTask(task.toString());
					if (content.getService(WorkflowService.class).getActivity()!=null) {
						setActivityId(content.getService(WorkflowService.class).getActivity().getId());
					}
				}
			} 
			catch (RuntimeException e) {
				throw(e);
			}
		}
	}
	
	public void setResource(Resource resource) {
		setResourceId((Long)resource.getId());
	}
	
	public Serializable getResourceId() {
		return this.resourceId;
	}
	
	public void setResource(KBFile file) {
		if (file!=null && file.getId()!=null) {
			setResourceId((long)file.getId());
		}	
	}
	
	@Override
	public String toString() {
		return getAction()+ " | " + getTarget();
	}
	
	public String getTarget() {
		return getKbeeClass() + " - "  + String.valueOf(getContentOId()) +"/"+String.valueOf(getVersion());
	}
	
	public Object getContent() {
		return null;
	}

	public Long getContentOId() {
		return contentOId;
	}

	public void setContentOId(long contentId) {
		this.contentOId = contentId;
	}
	
	public Long getContentId() {
		return Long.valueOf(contentId);
	}
	
	public void setContentId(long contentId) {
		this.contentId = contentId;
	}
	
	public void setResourceId(long resourceId) {
		this.resourceId = resourceId;
	}
	
	public int getVersion() {
		return version;
	}
	
	public void setVersion(int version) {
		this.version = version;
	}
	
	@Override
	public String getType() {
		return "Content";
	}
	
	/**
	 * IDoc, Text, Question, etc.
	 */
	@Override
	public String getObjectClass() {
		return "Content";
	}
}