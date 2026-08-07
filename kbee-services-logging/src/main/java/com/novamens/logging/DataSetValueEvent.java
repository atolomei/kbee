package com.novamens.logging;

import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.novamens.beans.BeansService;

import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.ObjectId;
import com.novamens.content.user.UserLabel;
import com.novamens.content.user.UserLabelDao;
import com.novamens.content.user.UserProfile;
import com.novamens.security.User;
import com.novamens.security.audit.AuditSet;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

@Entity
@DiscriminatorValue("DataSetValueEvent")
public abstract class DataSetValueEvent extends AbstractObjectEvent {

 

	public DataSetValueEvent() {
		super();
		setAuditSet(AuditSet.DATASET_VALUE);
	}
	
	public DataSetValueEvent(DataSetMember datasetmember, String description) {
		super();
		setAuditSet(AuditSet.DATASET_VALUE);
		setDataSetMember(datasetmember);
		setParameters(description);
	}
	
	public DataSetValueEvent(UserLabel label, String description) {
		super();
		setAuditSet(AuditSet.DATASET_VALUE);
		setUserLabel(label);
		setParameters(description);
	}
	
	public DataSetValueEvent(DataSetMember datasetmember, List<String> updatedParts) {
		super();
		setAuditSet(AuditSet.DATASET_VALUE);
		setDataSetMember(datasetmember);
		setParameters(getDescription(updatedParts));
	}
	
	
	public void setDataSetMember(DataSetMember dm) {
		setObjectId((new ObjectId(dm)).toString());
		setDomainId((Long) dm.getDomain().getId());
		String title = dm.getDisplayName(); 
		setTitle(title);
		setKbeeClass(dm.getDataSet().getName());
		setEventUser(ServiceLocator.getService(SecurityService.class).getSessionUser());
	}
	
	public void setUserLabel(UserLabel label) {
		setObjectId((new ObjectId(label)).toString());
		setKbeeClass(label.getClassName());
		setDomainId((Long) getUserProfile( label.getUser()).getDomain().getId());
		setTitle(label.getLabel());
		setEventUser(ServiceLocator.getService(SecurityService.class).getSessionUser());
	}

	
	public void setUserLabel(UserLabel label, User creator) {
		setObjectId((new ObjectId(label)).toString());
		setKbeeClass(label.getClassName());
		setDomainId((Long) getUserProfile( label.getUser()).getDomain().getId());
		setTitle(label.getLabel());
		setEventUser(creator);
	}
	
	
	@Deprecated
	@Override
	public String getEventType() {
		return "DataSet Value";
	}
	
	// 
	// Action:  para Create, Update, Delete
	//
	@Override
	public String getAction() {
		return getEventType();
	}
	
	@Override
	public String getType() {
		return "DataSet Value";
	}

	@Override
	public String toString() {
		return getAction()+ " | " + getTarget();
	}
	
	

	@Override
	public String getTarget() {
		return getKbeeClass() + " - "  + getObjectId().toString();
	}

	@Override
	public String getObjectClass() {
		return getKbeeClass();
	}
	
	private UserProfile getUserProfile(User user) {
		return getContentDao().findUserProfileByUser(user);
	}
	
	private ContentDao getContentDao() {
		return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	@SuppressWarnings("unused")
	private UserLabelDao  getLabelDao() {
		return	(UserLabelDao)ServiceLocator.getService(BeansService.class).getBean("userLabelDao");
	}
	
	

}
