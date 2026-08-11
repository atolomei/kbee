package com.novamens.kbee.content.reportsubscription;

import java.io.File;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.ObjectId;
import com.novamens.content.reportsubscription.ReportSubscription;
import com.novamens.content.resource.KBFile;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.dom.Domain;
import com.novamens.kbee.content.resource.KBFileImpl;
import com.novamens.logging.AbstractObjectEvent;
import com.novamens.security.User;
import com.novamens.security.audit.AuditSet;
import com.novamens.service.ServiceLocator;


@Entity
@DiscriminatorValue("ReportSubEvent")
public class ReportSubscriptionEvent extends AbstractObjectEvent {
	
	static public String getClassEventType() {
		return "ReportSubscription";
	}
	
	
	
	public ReportSubscriptionEvent(ReportSubscription sub, KBFile file) {
		super();
		setReportSubscription(sub);
		setKBFile(file);
		setAuditSet(AuditSet.REPORT);
	}
	 
	
	public void setReportSubscription(Object object) {

		if (object instanceof ReportSubscription) {
				ReportSubscription sub = (ReportSubscription) object;
				setKbeeClass(sub.getClass().getSimpleName());
				setObjectId((new ObjectId(sub)).toString());
				setDomain(sub.getDomain());
				setDomainId((Long)(sub.getDomain().getId()));
				String title= sub.getDisplayName(); 
				if ((title!=null) && (title.length()>255))
					title=title.substring(0, 252)+"...";
				setTitle(title);
			}
	}
	
	
	public void setKBFile(KBFile file) {
		if (file==null)
			throw new IllegalArgumentException("file con not be null");
		Long id = (Long) file.getId();
		super.setAuditResourceKBFileId(id);
	}
	
	public KBFile getKBFile() {
		Long id = super.getAuditResourceKBFileId();
		if (id==null)
			return null; 
		return (KBFile) getContentDao().findResourceById(KBFileImpl.class, id);
	}
	 
	@Override
	public String getAction() {
		return "ReportSubscription";
	}

	@Override
	public String toString() {
		return getAction()+ " | " + getTarget();
	}
	
	
	private ContentDao getContentDao() {
		 BeansService beans = ServiceLocator.getService(BeansService.class);
		 return (ContentDao) beans.getBean("contentDao");
	}
	
}
