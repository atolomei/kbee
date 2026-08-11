package com.novamens.content.service;

import java.time.OffsetDateTime;
import java.util.List;

import com.novamens.content.base.Content;
import com.novamens.content.base.ContentLink;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.base.Resource;
import com.novamens.content.base.ResourceNode;
import com.novamens.content.base.ResourceTag;
import com.novamens.content.form.EFormData;
import com.novamens.content.form.UpdatedField;
import com.novamens.content.resource.ExternalResource;
import com.novamens.content.resource.KBFile;
import com.novamens.content.text.Text;
import com.novamens.content.text.TextChange;
import com.novamens.content.user.UserDevice;
import com.novamens.content.user.UserSignature;
import com.novamens.content.workflow.ProcessLauncher;
import com.novamens.security.User;
import com.novamens.service.BusinessObjectService;
import com.novamens.signature.SignatureException;
import com.novamens.workflow.Process;
import com.novamens.workflow.WorkflowContext;


/**
 * 
 * <p>KbeeDBException (checked exception)
 *  
 * <ul>
 * <li>create</li>
 * update
 * delete
 * 
 * checkin
 * archive
 * recycle
 * unArchive
 * restore
 * 
 * addFile
 * addExternalResource
 * </ul>
 * </p>
 *  
 */
public interface ContentService extends BusinessObjectService {
	
	public void delete(String parameter) 						throws ContentMgmtException;
	public void delete() 										throws ContentMgmtException;
	public void update() 										throws ContentMgmtException;
	public void updateNoTrx() 									throws ContentMgmtException;
	
	public void create() 										throws ContentMgmtException;
	
	public void addFile(KBFile file)  							throws ContentMgmtException;
	public void addFile(KBFile file, boolean ispublic)			throws ContentMgmtException;
	public void addFile(KBFile file, ResourceTag group, boolean ispublic) throws ContentMgmtException;
	public void replaceFile(KBFile file, KBFile version)		throws ContentMgmtException;
	public void addExternalResource(ExternalResource resource)  throws ContentMgmtException;
	public void update(List<String> updatedParts) 				throws ContentMgmtException;
	public void updateFields(List<UpdatedField> updatedParts)	throws ContentMgmtException;
	public void update(Object event) 							throws ContentMgmtException;
	public void update(String part) 							throws ContentMgmtException;
	public void update(Resource resource, String part)			throws ContentMgmtException;
	
	public void checkin(List<String> updatedParts) 						throws ContentMgmtException;
	public void checkin() 												throws ContentMgmtException;
	public void checkin(List<String> updatedParts, boolean is_silent) 	throws ContentMgmtException;
	public void checkin(boolean is_silent) 								throws ContentMgmtException;
	
	
	public void deleteAllVersions() 					throws ContentMgmtException;
	public void deleteAllVersions(String updatedParts) 	throws ContentMgmtException;
	
	public boolean isValid() 							throws ContentMgmtException;;
	public boolean isValidVersion()						throws ContentMgmtException;;
	public Content getValidVersion() 					throws ContentMgmtException;;
	
	public Content checkout()							throws ContentMgmtException;
	
	public void dropCheckout() 							throws ContentMgmtException;
	public void archive()								throws ContentMgmtException;
	public void unArchive() 							throws ContentMgmtException;
	
	public void recycle() 								throws ContentMgmtException;
	public void restore() 								throws ContentMgmtException;
	
	public Content become(String template)				throws ContentMgmtException;
	public Content becomeAndLaunch(String template, String launcher) throws ContentMgmtException;
	
	public void removeLabel(String label) 				throws ContentMgmtException;
	public void assign(User user, String note) 			throws ContentMgmtException;;
	
	public void updateAcl(List<String> updatedParts) 	throws ContentMgmtException;
	
	public WorkflowContext getWorkflow();
	
	public String getSender();
	
	public OffsetDateTime getAssignationTime();
	
	public List<String> getActivityResources();
	
	public List<String> getAlerts();
	public void setAlert(String alert);

	public boolean labeled(String label);
	public String getNote();
	public String getSummary();
	public String getStringFromRule(String portalsSubtitleRule);
		
	public List<FileSnippet> getSnippets(String query);
	public List<FileSnippet> getSnippets(String query, boolean portal);
	
	public String getPortalSubtitle();
	
	public String getConsoleSubtitle();
	public String getConsoleSubtitleDefaultIfNull();
	
	public void sign(EFormData data, String digest, UserSignature signature, UserDevice device) throws SignatureException;
	public void sign(EFormData data, String digest, UserSignature signature, String signatureStream) throws SignatureException;
	public void sign(EFormData data, String digest, UserSignature signature, UserDevice device, String signatureStreamm) throws SignatureException;
	public void sign(EFormData data, String digest, String signed, UserSignature signature, UserDevice device, String signatureStream) throws SignatureException;
	
	public void unsign(EFormData data);
	
	public Process startProcess(ProcessLauncher launcher, Object initialData,
			List<ResourceNode> resources, 
			User collaborator, 
			String note, 
			ResourceTag sourceTag, 
			ResourceTag targetTag);
	
	public Text getText();
	public List<TextChange> getTextChanges();
	public List<ContentLink> getLinks();
	
	public Content clone();
	
	public void reindex();
}