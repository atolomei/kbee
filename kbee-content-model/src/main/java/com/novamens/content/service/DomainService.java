package com.novamens.content.service;



import java.io.File;
import java.io.Serializable;
import java.security.cert.Certificate;
import java.util.List;


import com.novamens.content.base.ConstraintException;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.document.TreeFile;
import com.novamens.content.library.Library;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.resource.KBFile;
import com.novamens.scheduler.AbstractCronJobRequest;
import com.novamens.scheduler.SchedulerException;
import com.novamens.scheduler.ServiceRequest;
import com.novamens.security.User;
import com.novamens.service.BusinessObjectService;

/**  
 *  Software Services 
 *
 */
public interface DomainService extends BusinessObjectService {
	
	public final String WORKFLOW_USER = "workflow";
	
	public ContentTemplate getResourcesTemplate();
	public DataSetMember getResourcesTypeDataSetMember();
	public Classifier getResourcesTypeClassifier();
	
	public void update(List<String> updateParts) throws ContentMgmtException;
	
	public void delete() throws ContentMgmtException, ConstraintException;
	public void archive() throws ContentMgmtException;
	public void restore() throws ContentMgmtException;
	public void markAsDeleted() throws ContentMgmtException;
	
	public User getWorkflowUser();
	public User getRootUser();
	public User getPublicResourcesUser();
	
	public void save(Library cabinet) throws ContentMgmtException;
	
	public List<Library> getLibraries();
	public List<Library> getAllCabinets();
	
	public void evict();
	
	public KBFile importFileFromLocalDisk(File local_file) throws ContentMgmtException;
	public TreeFile importTreeFileFromLocalDisk(File local_file) throws ContentMgmtException;

	public void saveSubscriptionSchedule(AbstractCronJobRequest subscriptionSchedule) throws SchedulerException;
	public void saveBillboardSchedule(AbstractCronJobRequest request) throws SchedulerException;
	
	public List<DataSet> getEntitySets();
	
	public List<Classifier> getClassifiers();
	
	/**
	 * 
	 * The goal of this method is to submit a Service Request inside a DB TRX
	 * 
	 * @param request 
	 * @return id of the ServiceRequest
	 * @throws SchedulerException
	 */
	public Serializable enqueueRequest(ServiceRequest request) throws SchedulerException;

	
	public User createPublicResourcesUserIfNotExists(); 
	public ContentTemplate createResourcesContentTemplatefNotExists();
	
	
	public void createLabelsIfNotExists();
	
	public DataSet createSiteDataSetIfNotExists() throws ContentMgmtException;
	public DataSet createSiteRepositoryDataSetIfNotExists() throws ContentMgmtException;
	public Classifier createPortalHomeClassifierIfNotExists() throws ContentMgmtException;

	public String getDefaultTitleRule( ContentTemplate ct);
	public String getDefaultSubTitleRule( ContentTemplate ct);
	
	public Certificate getCertificate();
}