package kbee.importer;


import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.util.io.IOUtils;
import org.hibernate.SessionFactory;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.base.ContentClass;
import com.novamens.content.base.ContentCreationException;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.base.Resource;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ClassifierTemplate;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.resource.KBFile;
import com.novamens.content.service.ContentFactoryService;
import com.novamens.content.service.ContentService;
import com.novamens.content.service.kbfs.KBFSResourceService;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserService;
import com.novamens.content.workflow.ProcessLauncher;
import com.novamens.content.workflow.WorkflowDomainService;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.content.document.KbeeIDoc;
import com.novamens.kbee.content.model.KbeeContentTemplate;
import com.novamens.kbee.content.resource.KBFileImpl;
import com.novamens.kbee.content.resource.KbeeExternalResource;
import com.novamens.kbee.idoc.webapi.client.KbeeApiService;
import com.novamens.kbfs.FileServerException;

import com.novamens.logging.AssignationEvent;
import com.novamens.logging.ContentEvent;
import com.novamens.logging.UpdateEvent;
import com.novamens.security.User;
import com.novamens.service.ServiceLocator;
import com.novamens.service.ServiceNotFoundException;
import com.novamens.transaction.Transaction;

import kbee.api.model.ApiFile;
import kbee.api.model.ApiValue;
import kbee.api.model.IAttributeValues;
import kbee.api.model.ILogEvent;
import kbee.api.model.ApiResource;
import kbee.api.model.IResultSet;
import kbee.api.model.ITemplate;
import kbee.api.service.ApiService;
import kbee.util.logging.Logger;

@Deprecated
public class FilesImporter extends ClassificablesImporter {
	
	private int total = 0;
	private int updated = 0;
	private String criteria;
	private int maxFiles = 0;
	private boolean freeze;
	
	static private Logger logger2 = Logger.getLogger(FilesImporter.class.getName());


	FilesImporter(ApiService server, Domain  domain, String criteria) {
	//FilesImporter(ApiService server, Domain  domain, String criteria, boolean freeze) {
		super(server, null);
		setDomain(domain);
		this.freeze = freeze;
		this.criteria = criteria;
	}
	
	@Override
	public void execute() throws ContentMgmtException  {
		
		int i=0;
		int maxFiles = getMaxFiles()==0 ? Integer.MAX_VALUE : getMaxFiles();
		Transaction transaction = null;
		try {
			
			List<ApiFile> tx = new ArrayList<ApiFile>();
			//IResultSet<IFile> files	= getServer().select2(criteria, getServer().getDomain(), 256, false, false);
			IResultSet<ApiFile> files	= null;
			//transaction = beginTransaction();
			while (files.hasNext() && updated<maxFiles && isRunning()) {
				ApiFile remote  = null;
				try {
					transaction = beginTransaction();
					remote = files.next();
					KbeeIDoc local = getLocal(KbeeIDoc.class, remote);
					if (local==null && remote.getState().equals("ARCHIVED")) {
					//if ((local==null || remote.getLastModifiedDate().isAfter(local.getLastModifiedOffsetDateTime())) && !isFreeze(remote)) {
						if (local == null) {
							local = createFile(remote);
							setOId(remote, local);
							setLocal(remote, local);
						}	
					 	//logger.info("IDoc "+ local.getId() + " "+ UriHelper.getUri(remote) + " updated");
					 	//logger2.info("IDoc "+ local.getId() + " "+ UriHelper.getUri(remote) + " updated");
						syncFile(remote, local);
						tx.add(remote);
						update(local);
						updated++;
	 					importHistory(remote, local);
						importAudit(remote, local);
						ContentEvent migrationevent = new UpdateEvent(local, "Migrated from previous version");
						update(migrationevent);
					}
					else {
						tx.add(remote);
						if (local!=null) {
							//logger.info("IDoc "+ (local.getTitle()!=null ? local.getTitle() : "") + " " + (local!=null ? local.getId() : "freeze") + " "+ UriHelper.getUri(remote));
					 		//logger2.info("IDoc "+ (local.getTitle()!=null ? local.getTitle() : "") + " " + (local!=null ? local.getId() : "freeze") + " "+ UriHelper.getUri(remote));
						}
						else {
							//logger.info("IDoc "+ UriHelper.getUri(remote));
					 		//logger2.info("IDoc "+ UriHelper.getUri(remote));
						}
					}
					i++;
					getContentDao().flush();
				 	//logger.info("commit");
				 	//logger2.info("commit");
					transaction.commit();
					for (ApiFile remotefile : tx) {
						if (!isFreeze(remotefile)) {
							freeze(remotefile);
						}
					}
					tx.clear();
					if ((i-1)%100==0) {
						((SessionFactory)ServiceLocator.getService(BeansService.class).getBean("sessionFactory")).getCurrentSession().clear();
					}
				}
				catch (Exception e) {
					logger.error(e);
					logger2.error(e);
					try {
						transaction.rollback();
					}
					catch (Exception e2) {
						logger.error(e2);
						logger2.error(e2);
					}
					if (remote!=null) {
						//logger.info("ERROR IDoc "+ " "+ UriHelper.getUri(remote));
				 		//logger2.info("ERROR IDoc "+ " "+ UriHelper.getUri(remote));
					}
				}
				
				setProgress(i);
			}
			transaction.commit();
			for (ApiFile remotefile : tx) {
				if (!isFreeze(remotefile)) {
					freeze(remotefile);
				}
			}
			tx.clear();
			
//			files = getServer().selectTask(criteria);
//			transaction = beginTransaction();
//			while (files.hasNext() && isRunning()) {
//				IFile remote = files.next();
//				KbeeIDoc local = getLocal(KbeeIDoc.class, remote);
//				if ((local==null || remote.getLastModifiedDate().isAfter(local.getLastModifiedOffsetDateTime())) && !isFreeze(remote)) {
//					if (local == null) {
//						local = createTask(remote);
//						if (local!=null) {
//							setLocal(remote, local);
//						}
//					}	
//					if (local !=null) {
//						logger.info("IDoc Task "+ local.getId() + " "+ UriHelper.getUri(remote) + " updated");
//						syncFile(remote, local);
//						update(local);
//						tx.add(remote);
//						updated++;
//						importHistory(remote, local);
//						importAudit(remote, local);
//						List<String> updates = new ArrayList<String>();
//						updates.add("Migration Process");
//						local.getService(ContentService.class).update(updates);
//					}
//					else {
//						tx.add(remote);
//						logger.info("IDoc Task "+ UriHelper.getUri(remote) + " local is locked");
//					}
//				}
//				else {
//					tx.add(remote);
//					if (local!=null) {
//						logger.info("IDoc "+ local.getId() + " "+ UriHelper.getUri(remote));
//					}
//				}
//				if (i++%10==0) {
//					transaction.commit();
//					for (IFile remotefile : tx) {
//						if (!isFreeze(remotefile)) {
//							freeze(remotefile);
//						}
//					}
//					tx.clear();
//					if ((i-1)%100==0) {
// 						((SessionFactory)ServiceLocator.getService(BeansService.class).getBean("sessionFactory")).getCurrentSession().clear();
//					}
//					transaction = beginTransaction();
//				}
//				setProgress(i);
//			}
//			transaction.commit();
//			for (IFile remotefile : tx) {
//				if (!isFreeze(remotefile)) {
//					freeze(remotefile);
//				}
//			}
 		}
		catch (Throwable e) {
			logger.error(e);
			logger2.error(e);
			transaction.rollback();
			logger.error(e);
			throw new ContentMgmtException(e);
		}
	}
	
	@Override
	public int getTotal() {
		if (total == 0) {
//			IResultSet<IFile> files	= getServer().select2(criteria, getServer().getDomain(), 256, false, false);
//			IResultSet<IFile> tasks	= getServer().selectTask(criteria);
//			total = (int)files.getSize() + (int)tasks.getSize();
		}
		return total;
	}
	
	@Override
	public String getResult() {
		String result = "<p>"+String.valueOf(getTotal())+" files processed. ";
		result += String.valueOf(updated)+" files updated</p>";
		return result;
	}
	
	public void setMaxFiles(int value) {
		this.maxFiles = value;
	}
	
	public int getMaxFiles() {
		return this.maxFiles;
	}
	
	public boolean isRunning() {
		return true;
	}
	
	private KbeeIDoc createFile(ApiFile remote) throws ContentCreationException, ContentMgmtException {
		//ITemplate remotetemplate = getServer().get(ITemplate.class, remote.getContentClass().getHRef());
		//ContentTemplate localtemplate = getLocal(KbeeContentTemplate.class, remotetemplate);
		//KbeeIDoc file = (KbeeIDoc)createFile(localtemplate.getName());
		KbeeIDoc file = (KbeeIDoc)createFile("File");
		return file;
	}
	
	
	private KbeeIDoc createTask(ApiFile remote) throws ContentCreationException, ContentMgmtException, IOException {
		if (remote.getPreviousVersion()!=null) {
			
			ApiFile remoteversion = getServer().get(ApiFile.class, remote.getPreviousVersion().getHRef());
			
			if (remoteversion==null) {
				throw new ContentMgmtException("null remote version");
			}
			
			KbeeIDoc localversion = getLocal(KbeeIDoc.class, remoteversion);
			
			if (localversion==null) {
				//throw new ContentMgmtException("null local version");
				return null;
			}
			
			if (localversion.isLocked()) {
				// no se procesa. quedara para una proxima iteración 
				return null;
			}
			
			getContentDao().save(localversion);
			
			ProcessLauncher launcher = getProcessLauncher(remote);
			KbeeIDoc local = (KbeeIDoc)localversion.getService(ContentService.class).checkout();
			
			local.getService(WorkflowService.class).startProcess(launcher.getProcedure());
			update(local);
			
			if (remote.getWorkspace()!=null) {
				User workspaceuser = getLocalUser(remote.getWorkspace());
				AssignationEvent assignevent = getAssignationEvent(remote);
				String note = "Migrated from previous version</br>";
				if (assignevent!=null) {
					if (assignevent.getDescription()!=null && !"".equals(assignevent.getDescription())) {
						note += "Note from " + assignevent.getEventUser().getDisplayName() + ":";
						note += assignevent.getDescription();
					}
					else {
						note += "Assigned by " + assignevent.getEventUser().getDisplayName();
					}
				}
				local.getService(WorkflowService.class).reassign(workspaceuser, note);
			}	

			return local;
		}
		else {
			KbeeIDoc local = createFile(remote);
			setOId(remote, local);
			
			local.setWorkspace((long)getUser().getId());
			local.setHeadVersion(false);
			
			List<String> updates = new ArrayList<String>();
			updates.add("Migration Process");
			local.getService(ContentService.class).update(updates);
			
			ProcessLauncher launcher = getProcessLauncher(remote);
			local.getService(WorkflowService.class).startProcess(launcher.getProcedure());
			update(local);
			
			if (remote.getWorkspace()!=null) {
				User workspaceuser = getLocalUser(remote.getWorkspace());
				AssignationEvent assignevent = getAssignationEvent(remote);
				String note = "Migrated from previous version</br>";
				if (assignevent!=null) {
					if (assignevent.getDescription()!=null && !"".equals(assignevent.getDescription())) {
						note += "Note from " + assignevent.getEventUser().getDisplayName() + ":";
						note += assignevent.getDescription();
					}
					else {
						note += "Assigned by " + assignevent.getEventUser().getDisplayName();
					}
				}
				local.getService(WorkflowService.class).reassign(workspaceuser, note);
			}	
			return local;
		}
	}
	
	protected boolean hashistory(ApiFile remotefile) {
		try {
			//List<IFile> history = getServer().getHistory(remotefile);
			List<ApiFile> history = null;
			for (ApiFile remoteversion : history) {
				KbeeIDoc localversion = getLocal(KbeeIDoc.class, remoteversion);
				if (localversion!=null) {
					return true;
				}
			}
			return false;
		}
		catch (Throwable e) {
			logger.error(e);
			logger.error(e);
			throw e;
		}
	}

	
	protected void importHistory(ApiFile remotefile, KbeeIDoc localfile) throws ContentMgmtException, ContentCreationException, IOException  {
		try {
			KbeeIDoc nextversion = localfile;
			//List<IFile> history = getServer().getHistory(remotefile);
			List<ApiFile> history = null;
			for (ApiFile remoteversion : history) {
				KbeeIDoc localversion = getLocal(KbeeIDoc.class, remoteversion);
				if (localversion==null || remoteversion.getLastModifiedDate().isAfter(localversion.getLastModifiedOffsetDateTime())) {
					if (localversion == null) {
						localversion = createFile(remoteversion);
						setLocal(remoteversion, localversion);
					}
					nextversion.setPreviousVersion(localversion);
					syncFile(remoteversion, localversion);
					localversion.setOId(localfile.getOId());
					localversion.setHeadVersion(false);
					localversion.setVersion(remoteversion.getVersion());
					update(localversion);
					update(nextversion);
				}
				nextversion = localversion;
			}
		}
		catch (Throwable e) {
			logger.error(e);
			logger2.error(e);
			throw e;
		}
	}
	
	protected void importAudit(ApiFile remotefile, KbeeIDoc localfile) throws IOException {
		//(new LogEventsImporter(getServer(), getDomain())).execute(remotefile, localfile);
	}

	private void setOId(ApiFile remote, KbeeIDoc local) throws IOException {
		Serializable oid = null;
		if (remote.getPreviousVersion()!=null) {
			ApiFile remoteversion = getServer().get(ApiFile.class, remote.getPreviousVersion().getHRef());
			while(remoteversion!=null) {
				KbeeIDoc localversion = getLocal(KbeeIDoc.class, remoteversion);
				if (localversion!=null) {
					local.setOId(localversion.getOId());
					oid = local.getOId();
					break;
				}
				else {
					if (remoteversion.getPreviousVersion()!=null) 
						remoteversion = getServer().get(ApiFile.class, remoteversion.getPreviousVersion().getHRef());
					else
						remoteversion = null;
				}
			}	
		}
		if (oid==null) {
			oid = ServiceLocator.getService(ContentFactoryService.class).getNewOId();
			local.setOId((Long)oid);
		}
	}
	
	private void syncFile(ApiFile remote, KbeeIDoc local) throws IOException {
		local.setTitle(remote.getDisplayName());
		local.setVersion(remote.getVersion());
		if (remote.getWorkspace()!=null) {
			local.setHeadVersion(false);
		}
		else {
			local.setHeadVersion(true);
			local.setWorkspace(null);
		}
		
		if (remote.getState().equals("ARCHIVED"))
			local.setState(ObjectState.ARCHIVED);
		if (remote.getState().equals("DELETED"))
			local.setState(ObjectState.DELETED);
		
		local.setLastModifiedUser(getLocalUser(remote.getLastModifiedUser()));
		local.setLastModifiedOffsetDateTime(remote.getLastModifiedDate());
				
		syncClassifiers(remote, local, getClassifiers(local.getContentTemplate()));
		
		for(ApiResource resource : remote.getResources()) {
			if ("file".equals(resource.getRel())) {
				KBFile localfile = getLocal(KBFileImpl.class, resource);
				if (localfile==null) {
					localfile = getRemoteFile(resource);
					localfile.setDomain(local.getDomain());
					update(localfile);
					if (resource.getCRC()!=null) {
						String localcrc = getCRC(localfile);
						if (localcrc==null || !resource.getCRC().equals(localcrc)) {
							logger.error("invalid crc "+resource.getHRef()+" "+resource.getCRC()+ " " +localcrc);
						}
					}
					local.addFile(localfile);
					setLocal(resource, localfile);
				}
				else {
					if (localfile!=null && !containsresource(local, localfile)) {
						local.addFile(localfile);
					}
				}
				syncResource(resource, localfile);
				update(localfile);
			}
			else {
				KbeeExternalResource localresource = getLocal(KbeeExternalResource.class, resource);
				if (localresource==null || resource.getLastModifiedDate().isAfter(localresource.getLastModifiedOffsetDateTime())) {
					if (localresource==null) { 
						localresource = createExternal();
						localresource.setDomain(local.getDomain());
						syncResource(resource, localresource);
						update(localresource);
						setLocal(resource, localresource);
					}
					syncResource(resource, localresource);
					update(localresource);
					if (!containsresource(local, localresource)) {
						local.addResource(localresource);
					}
				}
			}
			//}
		}
		update(local);
	}
	
	private void syncResource(ApiResource remote, KBFile local) throws IOException {
		local.setLastModifiedUser(getLocalUser(remote.getLastModifiedUser()));
		local.setLastModifiedOffsetDateTime(remote.getLastModifiedDate());
		local.setUploadOffsetDateTime(remote.getLastModifiedDate());
		local.setUploadUser(local.getLastModifiedUser());
		String name = remote.getName();
		int i = name.lastIndexOf(".");
		if (name.length()-i<5) {
			name = name.substring(0,i);
		}
		local.setTitle(name);
		if (remote.getTitle()!=null || remote.getDescription()!=null) {
			String description = "";
			if (remote.getTitle()!=null) {
				description = remote.getTitle();
			}
			if (remote.getDescription()!=null) {
				if (!"".equals(description))
					description += ": ";
				description += remote.getDescription();
			}
			local.setDescription(description);
		}
		else {
			local.setDescription(null);
		}
	}
	
	private void syncResource(ApiResource remote, KbeeExternalResource local) throws IOException {
		local.setLastModifiedUser(getLocalUser(remote.getLastModifiedUser()));
		local.setLastModifiedOffsetDateTime(remote.getLastModifiedDate());
		local.setTitle(remote.getTitle());
		local.setDescription(remote.getDescription());
	}
	
	private boolean containsresource(KbeeIDoc idoc, Resource file) {
		for (KBFile idocfile : idoc.getFiles()) {
			if (idocfile.getId().equals(file.getId())) {
				return true;
			}
		}
		return false;
	}
	
	private List<Classifier> getClassifiers(ContentTemplate template) {
		List<Classifier> classifiers = new ArrayList<Classifier>();
		for (ClassifierTemplate classifiertemplate : template.getClassifiers()) {
			classifiers.add(classifiertemplate.getClassifier());
		}
		return classifiers;
	}
	
	private KBFile getRemoteFile(ApiResource resource) throws IOException {

		BufferedInputStream reader = null;
		
		try {
			
			logger.info("GET "+resource.getHRef());
			logger2.info("GET "+resource.getHRef());
			
			//reader = new BufferedInputStream(getServer().getResource(resource.getHRef()));
			String filepath = new String(resource.getName().getBytes("Windows-1252"), "UTF-8");
			
			// KBFileImpl file = new KBFileImpl();
			KBFileImpl file = (KBFileImpl) ServiceLocator.getService(ContentFactoryService.class).createKBFileNoTrx(filepath);
			file.setOId(ServiceLocator.getService(ContentFactoryService.class).getResourceNewOId());
			
			file.setName(filepath);
			file.setInPortalVersion(true);
			
			file.setCreationOffsetDateTime(resource.getLastModifiedDate());
			file.setLastModifiedUser(getLocalUser(resource.getLastModifiedUser()));
			file.setLastModifiedOffsetDateTime(resource.getLastModifiedDate());
			file.setUploadUser(getLocalUser(resource.getLastModifiedUser()));
			file.setDomain(getDomain());
			file.setState(ObjectState.ENABLED);
			
			try {
				file.getService(KBFSResourceService.class).putObject(filepath, reader);
			} 
			catch (FileServerException | ServiceNotFoundException e) {
				logger.error(e);
			} 
			finally {
				if (reader!=null)
					IOUtils.closeQuietly(reader);
			}
			
			return file;
		}
		finally {
			if (reader!=null)
			reader.close();
		}
	}
	
	private Content createFile(String templatename) throws ContentCreationException, ContentMgmtException {
		try {
			
			UserProfile userProfile = ServiceLocator.getService(UserService.class).getSessionUserProfile();
			
			ContentTemplate template = getContentDao().findContentTemplateByName(templatename, userProfile.getDomain().getId());
			
			if (template == null)
				throw new InstantiationException();
			
			ContentClass contentClass = template.getContentClass();
		
			Class<?> javaclass = Class.forName(contentClass.getJavaClass());
			
			Object instance = javaclass.newInstance(); 
			
			if (!(instance instanceof Content))
				throw new InstantiationException();
			
			Content content = (Content)instance;
			
			content.setContentTemplate(template);
			content.setCommentsEnabled(true); 
			
			getContentDao().save(content);
			
			return content;
		}
		catch (ClassNotFoundException e)  {
			throw new ContentCreationException(e);
		}
		catch (InstantiationException e)  {
			throw new ContentCreationException(e);
		}
		catch (IllegalAccessException e)  {
			throw new ContentCreationException(e);
		}
		catch (Exception e)  {
			throw new ContentCreationException(e);
		}
	}
	
	private boolean isFreeze(ApiFile file) {
		for (IAttributeValues values : file.getAttributes()) {
			for (ApiValue value : values.getValues()) {
				if ("Freeze".equals(value.getDisplayName())) {
					return true;
				}
			}
		}
		return false;
	}
	
	private void freeze(ApiFile file) {
		//if (freeze())
		//getServer().freeze(file);
	}
	
	private KbeeExternalResource createExternal() {
		return new KbeeExternalResource();
	}
	
	private boolean freeze() {
		return freeze;
	}
	
	private AssignationEvent getAssignationEvent(ApiFile remote) {
		List<ILogEvent> events = null;
		//List<ILogEvent> events = getServer().getAudit(remote);
		for (ILogEvent remoteevent : events) {
			if ("Assign".equals(remoteevent.getType())) {
				AssignationEvent event = new AssignationEvent();
				event.setParameters(remoteevent.getParameters());
				event.setEventUser(getLocalUser(remoteevent.getUser()));
				return event;
			}
		}
		return null;
	}
	
	private String getCRC(Resource resource) {
		try {
			if (resource instanceof KBFile) {
				KBFile file = (KBFile)resource;
				long crc32 = org.apache.commons.io.FileUtils.checksumCRC32(file.getFile());
				return String.valueOf(crc32);
			}
			else {
				return null;
			}
		}
		catch (IOException e) {
			logger.error(e);
			e.printStackTrace();
			return null;
		}
	}
	
	private ProcessLauncher getProcessLauncher(ApiFile remote) {
		ITemplate remotetemplate = getServer().get(ITemplate.class, remote.getContentClass().getHRef());
		ContentTemplate localtemplate = getLocal(KbeeContentTemplate.class, remotetemplate);
 		for (ProcessLauncher launcher :	getDomain().getService(WorkflowDomainService.class).getLaunchers()) {
			if (launcher.getContentTemplate().getName().equals(localtemplate.getName()) && launcher.getProcedure().getDisplayName().equals("Edition"))
				return launcher;
		};
		return null;
	}
}
