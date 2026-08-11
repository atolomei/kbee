package com.novamens.kbee.content.service;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.security.KeyPair;
import java.security.cert.Certificate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.novamens.event.EventService;
import com.novamens.logging.*;
import com.novamens.repository.DomRepositoryService;
import com.novamens.scheduler.AbstractCronJobRequest;
import com.novamens.scheduler.CronSchedulerService;
import com.novamens.scheduler.SchedulerException;
import com.novamens.scheduler.SchedulerService;
import com.novamens.scheduler.ServiceRequest;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


import com.novamens.beans.BeansService;
import com.novamens.content.base.ConstraintException;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.document.TreeFile;
import com.novamens.content.document.TreeFileDir;
import com.novamens.content.entity.Person;
import com.novamens.content.library.Library;
import com.novamens.content.model.AttributeTemplate;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ClassifierTemplate;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.DataSetType;
import com.novamens.content.model.ExternalDao;
import com.novamens.content.model.Multiplicity;
import com.novamens.content.model.PersonSet;
import com.novamens.content.resource.KBFile;
import com.novamens.content.service.ContentFactoryService;
import com.novamens.content.service.DomainService;
import com.novamens.content.service.ObjectFactoryService;
import com.novamens.content.service.TreeFileFactoryService;
import com.novamens.content.service.TreeFileService;
import com.novamens.content.service.kbfs.KBFSResourceService;
import com.novamens.content.user.UserProfile;
import com.novamens.content.workflow.WorkflowDomainService;
import com.novamens.dao.SecurityDao;
import com.novamens.datetime.DateTimeService;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.event.Event;
import com.novamens.event.EventListener;
import com.novamens.hibernate.query.HibernateQuery;
import com.novamens.kbee.command.CommandService;
import com.novamens.kbee.content.library.KbeeLibrary;
import com.novamens.kbee.content.model.KbeeClassifier;
import com.novamens.kbee.content.model.KbeeLabelSet;
import com.novamens.kbee.content.resource.KBFileImpl;
import com.novamens.kbee.content.service.domain.DeleteDomainCommand;
import com.novamens.kbee.domain.KbeeDomain;
import com.novamens.kbee.event.EvictCacheServiceEvent;
import com.novamens.kbee.security.KbeeSecurityDao;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbfs.FileServerException;
import com.novamens.security.ReservedUsername;
import com.novamens.security.User;
import com.novamens.service.LanguageService;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.service.ServiceNotFoundException;
import com.novamens.signature.SignatureException;
import com.novamens.signature.SystemSignatureService;
import com.novamens.workflow.Procedure;


public class KbeeDomainService implements com.novamens.content.service.DomainService, EventListener {
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeDomainService.class.getName());
	
	static final long TWO_MINUTES = 1000*60*2;
	
	// Logger sincronico en la TRX
	static private Logger txlogger = LogManager.getLogger("TxLogger");
	
	private Domain domain = null;
	private User workflow_user = null;
	private User pr_user = null;
	
	private Classifier  m_typeClassifier;
	
	private List<DataSet> entitiessets;
	
	private ContentTemplate resources;
	private DataSetMember resoures_member;
	
	Classifier  typeClassifier;

	private ContentDao contentDao;
	
	private long last_check = 0;
	
	
	
	public KbeeDomainService() {
	}

	public KbeeDomainService(Domain domain) {
		 this.domain = domain;
	}
	
	public List<Classifier> getClassifiers() {
		return getContentDao().getClassifiers(getDomain());
	}
	
	public List<DataSet> getEntitySets() {
		if ((System.currentTimeMillis() - last_check) > TWO_MINUTES) 
			entitiessets = null;
		if (entitiessets!=null)
			return entitiessets;
		synchronized (this) {
			entitiessets = new ArrayList<DataSet>();
			for (DataSet ds : getContentDao().getDataSets(getDomain().getId(), ObjectState.ENABLED)) {
				if (ds.getDataSetType()==DataSetType.ENTITY && hasHome(ds))
					entitiessets.add(ds);
			}
			last_check = System.currentTimeMillis();
		}
		return entitiessets;
	}

	@Override
	public boolean listen(Event event) {
		if (event instanceof EvictCacheServiceEvent)
			return true;
		return false;
	}

	@Override
	public void onEvent(Event event) {
		logger.debug(Thread.currentThread().getStackTrace()[1].getMethodName() + " | " + event.getClass().getName());
		if (event instanceof EvictCacheServiceEvent)
			evict();
	}
	
	public DataSetMember getResourcesTypeDataSetMember() {
		
		if (resoures_member!=null)
			return resoures_member;
		
		Classifier typeclassifier = getResourcesTypeClassifier();

		if (typeclassifier==null)
			return null;

		DataSetMember boxType = getContentDao().findMemberByKey( typeclassifier.getDataSet(), DataSetMember.TYPE_RESOURCE_KEY);
		resoures_member = boxType;
		return resoures_member;
	}
	
	@Override
	public ContentTemplate getResourcesTemplate() {
		if (resources!=null)
			return resources;
		String str = ContentTemplate.RESOURCES;
		for (ContentTemplate t:getContentDao().getTemplates(getDomain())) {
			 if (t.getAlias()!=null && t.getAlias().equals(str)) {
				 resources=t;
				 return this.resources;
			 }
		 }
		return null;
	}
	
	@Override
	public Classifier getResourcesTypeClassifier() {
		if (m_typeClassifier!=null)
			return m_typeClassifier; 
		
		for (ClassifierTemplate classifier : getResourcesTemplate().getClassifiers())	{
			if (classifier.getClassifier().isContentType()) {
				m_typeClassifier =classifier.getClassifier();
				break;
			}
		}
		return m_typeClassifier;
	}

	@SuppressWarnings("serial")
	public class AllUsersQuery extends HibernateQuery {
		@Override
		public String getStatement() {
			return "from "+ KbeeUser.class.getSimpleName() + " U where U.domain.id=" + String.valueOf(domain.getId());
		}	
	}

	@Transactional
	public void update(List<String> list) throws ContentMgmtException {
		getContentDao().save(getDomain());
		txlogger.info(new DomainUpdateEvent(getDomain(), list));
	}
	
	
	@Transactional
	public void delete() throws ContentMgmtException, ConstraintException {
		ServiceLocator.getService(CommandService.class).add(new DeleteDomainCommand(getDomain()));
	}
	
	@Transactional
	public void markAsDeleted() throws ContentMgmtException {
		getDomain().setState(ObjectState.DELETED);
		getDomain().setAPIEnabled(false);
		getContentDao().save(getDomain());
		 txlogger.info(new DomainUpdateEvent( getDomain(), "Mark as Deleted"));
	}
	
	@Transactional
	public void archive() throws ContentMgmtException {
		getDomain().setState(ObjectState.ARCHIVED);
		getContentDao().save(getDomain());
		txlogger.info(new DomainUpdateEvent(getDomain(), "Archive"));
	}
	
	@Override
	public List<Library> getLibraries() {
		return ServiceLocator.getService(DomRepositoryService.class).getRepository(Library.class).findAll(getDomain());
	}
	
	@Override
	public List<Library> getAllCabinets() {
		return ServiceLocator.getService(DomRepositoryService.class).getRepository(Library.class).findAll(getDomain(), null, "listOrder");
	}

	@Override
	@Transactional
	public void save(Library Library) throws ContentMgmtException {
		if ( ((KbeeLibrary) Library).getDomain() !=null && !((KbeeLibrary) Library).getDomain().equals(getDomain())) {
			throw (new ContentMgmtException("Library Domain can not be different from  " +  getDomain().getName()));
		}
		if (((KbeeLibrary) Library).getDomain() ==null)
			((KbeeLibrary) Library).setDomain(getDomain());
		if (((KbeeLibrary) Library).getLastModifiedUser() ==null)
			((KbeeLibrary) Library).setLastModifiedUser(getSessionUser());
		getContentDao().save(Library);
		txlogger.info(new DomainUpdateEvent(getDomain(), "Library " + Library.getDisplayName()));
	}

	@Transactional
	public void restore() throws ContentMgmtException  {
		if (getDomain().getState()!=ObjectState.DELETED) {
			txlogger.error("Object "+ getDomain().getId().toString() +". is not in Recycly Bin.");
			return;
		}
		getDomain().setState(ObjectState.ENABLED);
		getDomain().setLastModifiedOffsetDateTime(OffsetDateTime.now());
		getDomain().setLastModifiedUser(getSessionUser());
		getContentDao().save(getDomain());
	    txlogger.info(new DomainUpdateEvent(getDomain(), "Restore"));
	} 
	
	@Override	
	public User getPublicResourcesUser() {
		try {
			if (pr_user==null) 
				pr_user = ServiceLocator.getService(com.novamens.service.SecurityService.class).findUserByUsername(ReservedUsername.PUBLICRESOURCES.getUserName() + "@" + getDomain().getName());
			return pr_user;
		} catch (Exception e) {
			pr_user = null;
			return null;
		}
	}
		
	@Override	
	public User getWorkflowUser() {
		try {
			if (workflow_user==null) 
				workflow_user = ServiceLocator.getService(com.novamens.service.SecurityService.class).findUserByUsername(DomainService.WORKFLOW_USER+"@"+getDomain().getName());
			return workflow_user;
		} catch (Exception e) {
			workflow_user = null;
			return null;
		}
	}
	
	public Domain getDomain() {
		return domain;
	}

	@Override
	public synchronized void evict() {
		m_typeClassifier = null;
		resources =  null;
		resoures_member = null;
		entitiessets = null;
	}
	
	@Override
	public User getRootUser() {
		User rootuser = ((KbeeSecurityDao)getSecurityDao()).findUserByName("root@"+ this.domain.getName());
		return rootuser;
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void createLabelsIfNotExists() {
		
		String label_name = getContentDao().findSystemParameterValueByKey("dataset_label.name", "Label");
		DataSet dt_label=getContentDao().findDataSetByName(label_name,  getDomain().getId());
		
		if (dt_label==null) {
			
			logger.debug("Creating labels for Domain " + getDomain().getDisplayName());
			
			KbeeLabelSet d_tag = new KbeeLabelSet();
			d_tag.setDomain(getDomain());
			d_tag.setCanonical(true);
			d_tag.setReadonly(false);
			d_tag.setName(getContentDao().findSystemParameterValueByKey("dataset_label.name", "Label"));
			d_tag.setLastModifiedUser(getSessionUser());
			getContentDao().save(d_tag);
			txlogger.info(new ModelCreateEvent(d_tag, "create"));
			String vals=getContentDao().findSystemParameterValueByKey("dataset_label.values", "follow up; duplicate; delete; draft");
			String vs[] = vals.split(";");
			for (String str: vs )
				addDataSetMember(d_tag, str);
			KbeeClassifier c_tag = new KbeeClassifier();
			c_tag.setDomain(getDomain());
			c_tag.setName(d_tag.getName());
			c_tag.setAPIClassifier(false);
			c_tag.setUniqueName("tag"); // tiene que ser consistente con el esquema solr fijo en schema.xml
			c_tag.setPredicate("tag");
			c_tag.setMultiplicity(Multiplicity.M0N);
			c_tag.setContentType(false);
			c_tag.setMetadataSubtitle(false);
			c_tag.setRuleCondition(false);
			c_tag.addDataSet(d_tag);
			c_tag.setLastModifiedUser(getSessionUser());
			getContentDao().save(c_tag);
			txlogger.info(new ModelCreateEvent(c_tag, "create"));
			logger.debug("done");
		}
		else
			logger.debug("Domain " + getDomain().getDisplayName() + " has Labels ");
	}
	

	@Override
	public TreeFile importTreeFileFromLocalDisk(File local_file) throws ContentMgmtException {
	
		if (!local_file.exists())
			 throw  new ContentMgmtException(local_file.getAbsolutePath() + " does not exist" );
	
		if (!local_file.isDirectory())
			 throw  new ContentMgmtException(local_file.getAbsolutePath() + " is a not a Directory" );
		
		try {	
			
			Map<String, Number> metrics = new ConcurrentHashMap<String, Number>(5, 0.9f, 1);
			AtomicInteger total_dirs 			= new AtomicInteger(0);
			AtomicInteger total_files	 		= new AtomicInteger(0);
			AtomicLong	total_disk 				= new AtomicLong(0);
			AtomicInteger total_files_to_import = new AtomicInteger(0);
			AtomicInteger total_dirs_to_import 	= new AtomicInteger(0);
			AtomicLong total_size_to_import 	= new AtomicLong(0);
			
			metrics.put("dirs",  total_dirs);
			metrics.put("files", total_files);
			metrics.put("size", total_disk);
			metrics.put("total_files_to_import", total_files_to_import);
			metrics.put("total_dirs_to_import",  total_dirs_to_import);
			metrics.put("total_size_to_import",  total_size_to_import);

			TreeFileDir tree_file_root = (TreeFileDir) ServiceLocator.getService(TreeFileFactoryService.class).createTreeFileDir();
			
			tree_file_root.setTitle(local_file.getName());
			
			tree_file_root.getService(TreeFileService.class).addDirectory(local_file, metrics);
			tree_file_root.getService(TreeFileService.class).save();
			
			return tree_file_root;
			
		} catch (Exception e) {
			logger.error(e);
			throw new ContentMgmtException(e);
		}
	}
	
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public KBFile importFileFromLocalDisk(File local_file) throws ContentMgmtException {
		
		if (!local_file.exists())
				 throw  new ContentMgmtException(local_file.getAbsolutePath() + " does not exist" );
		
		if (local_file.isDirectory())
			 throw  new ContentMgmtException(local_file.getAbsolutePath() + " is a Directory" );
			
			
		KBFileImpl file = (KBFileImpl) ServiceLocator.getService(ContentFactoryService.class).createKBFileNoTrx(local_file.getName());
		
		// KBFileImpl file = new KBFileImpl();
		//f ile.set OId(ServiceLocator.getService(ContentFactoryService.class).getResource NewOId());
		// file.setName(local_file.getName());
		
		file.setDomain(getDomain());

		String title = FilenameUtils.getBaseName(local_file.getName()).replaceAll("(-|_)", " ");
		file.setTitle(title);
		
		file.setState(ObjectState.ENABLED);
		file.setCreationOffsetDateTime(OffsetDateTime.now());
		file.setLastModifiedUser(ServiceLocator.getService(SecurityService.class).getSessionUser());
		file.setUploadOffsetDateTime(OffsetDateTime.now());

		
		// KBFS V1, V2
 		KBFSResourceService service = file.getService(KBFSResourceService.class);
		BufferedInputStream stream = null;
		try {
			
			stream = new BufferedInputStream(new FileInputStream(local_file), 4096);
			service.putObject(file.getName(), stream);
			getContentDao().save(file);
			return file;
		} 
		catch (FileNotFoundException  | FileServerException | ServiceNotFoundException e) {
			logger.error(e);
			throw new  ContentMgmtException(e);
		} 
		finally {
			if (stream!=null)
				org.apache.commons.io.IOUtils.closeQuietly(stream);
		}
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public DataSet createSiteDataSetIfNotExists() throws ContentMgmtException {
		DataSet site_dataset = getExternalDao().createSiteDataSetIfNotExists(domain);
		getExternalDao().createSiteClassifierIfNotExists(getDomain(), site_dataset);
		return site_dataset;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public Classifier createPortalHomeClassifierIfNotExists() throws ContentMgmtException {
		Classifier portal_home = getExternalDao().createPortalHomeClassifierIfNotExists(getDomain());
		return portal_home;
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public DataSet createSiteRepositoryDataSetIfNotExists() throws ContentMgmtException {
		DataSet site_repository_dataset 	= getExternalDao().createSiteRespositoryDataSetIfNotExists(domain);
		getExternalDao().createSiteRepositoryClassifierIfNotExists(getDomain(), site_repository_dataset);
		return site_repository_dataset;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void saveSubscriptionSchedule(AbstractCronJobRequest subscriptionSchedule) throws SchedulerException {
		ServiceLocator.getService(CronSchedulerService.class).saveCronJob(subscriptionSchedule);
		txlogger.info(new CronJobUpdateEvent(subscriptionSchedule));
		EventService service = ServiceLocator.getService(EventService.class);
		try {
			logger.info("Reloading CronJobs from Database");
			service.fire(new EvictCacheServiceEvent());
			logger.info("done");
		}
		catch (Exception e) {
			logger.error(e);
		}
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void saveBillboardSchedule(AbstractCronJobRequest request) throws SchedulerException {
		ServiceLocator.getService(CronSchedulerService.class).saveCronJob(request);
		txlogger.info(new CronJobUpdateEvent(request));
		EventService service = ServiceLocator.getService(EventService.class);
		try {
			logger.debug("Reloading CronJobs from Database");
			service.fire(new EvictCacheServiceEvent());
			logger.debug("done");
		}
		catch (Exception e) {
			logger.error(e);
		}
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public Serializable enqueueRequest(ServiceRequest request) throws SchedulerException  {
		return ServiceLocator.getService(SchedulerService.class).enqueue(request);
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public ContentTemplate createResourcesContentTemplatefNotExists() {
		ContentTemplate ct = getResourcesTemplate();
		if (ct!=null)
			return ct;
		return null;
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public User createPublicResourcesUserIfNotExists() {
		User user = getSecurityDao().findUserByName( ReservedUsername.PUBLICRESOURCES.getUserName() + "@"+getDomain().getName());
		if (user!=null)
			return user;
		Person person = (Person) ServiceLocator.getService(ObjectFactoryService.class).createUser( 
				ReservedUsername.PUBLICRESOURCES.getUserName() + "@" + getDomain().getName()
				);
		person.setLastName( getLanguageService().getString("shared-resources", getDomain().getLocale()));
		person.setState(ObjectState.ARCHIVED);
		return person.getProfile(UserProfile.class).getUser();
	}
	
	
	
	public Classifier getTypeClassifier() {
		if (typeClassifier!=null)
			return typeClassifier;
		for (Classifier c:getContentDao().getClassifiers(getDomain())) {
			if (c.isContentType()) {
				typeClassifier=c;
				return c;
			}
		}
		return null;
	}
	
	public String getDefaultTitleRule(ContentTemplate ct) {

		StringBuilder str = new StringBuilder();
		
		Classifier c_type=getTypeClassifier();
		
		
		if (c_type!=null) {
			str.append("<#if "+c_type.getAlias()+"??>"+ "${"+c_type.getAlias()+"}</#if> ");
		}
		
		int count=0;
		for (ClassifierTemplate cla_t: ct.getClassifiers())  {
			
			if (cla_t.getClassifier()!=null) {
				if (    (!(c_type!=null && c_type.getAlias().equals(cla_t.getClassifier().getAlias()))) 
						&&  cla_t.getClassifier()!=null 
						&&	cla_t.getClassifier().getDataSet()!=null
						&&	cla_t.getClassifier().getDataSet().getDataSetType()!=null
						&&  cla_t.getClassifier().getState()==ObjectState.ENABLED) {
					
					
					DataSet dataset = cla_t.getClassifier().getDataSet();

					// skip All but Entity String || 
					if (! (dataset.getDataSetType()==DataSetType.ENTITY || dataset.getDataSetType()==DataSetType.STRING ||  dataset.getDataSetType()==DataSetType.PEOPLE)) {
						
					}
					// skip Workflow Status
					if (cla_t.getClassifier().isWorkflowStatus()) {
						
					}
					
					// skip Distribution
					else if (cla_t.getClassifier().isDistribution()) {
					}

					// skip Labels
					else if ( cla_t.getClassifier().getDataSet().getDataSetType()==DataSetType.LABEL) {
						
					}
					
					else if (cla_t.getClassifier().getDataSet() instanceof PersonSet) {
						str.append("<#if "+cla_t.getClassifier().getAlias()+"??>");
						str.append( (str.length()>0? " - ":"") +  "${"+cla_t.getClassifier().getAlias()+"} ");
						str.append("</#if>");
						
					}
					
					else if (cla_t.getClassifier().hasHome()) {
						str.append("<#if "+cla_t.getClassifier().getAlias()+"??>"); 
						str.append( (str.length()>0? " - ":"") +  "${"+cla_t.getClassifier().getAlias()+"} ");
						str.append("</#if>");
						count++;
					}
				
					else if (cla_t.getClassifier().isDefaultStructure()) {
						str.append("<#if "+cla_t.getClassifier().getAlias()+"??>");
						str.append( (str.length()>0? " - ":"") +  "${"+cla_t.getClassifier().getAlias()+"} ");
						str.append("</#if>");
						count++;
					}
				}
			
			if (count>3)
				break;
			}
		}
		
		for (AttributeTemplate cla_t: ct.getAttributes())  {
			
			if (cla_t.getAttribute()==null ||
				cla_t.getAttribute(). getType()==null ||
				cla_t.getAttribute().getState()!=ObjectState.ENABLED)
				break;
					
			if (count>5)
				break;
				 
			if (cla_t.getAttribute().isRequired() || cla_t.getAttribute().isDefaultStructure()) {
					if (cla_t.getAttribute().isDate()) {
						String mask= ServiceLocator.getService(DateTimeService.class).getDefaultDateMask(ct.getDomain().getLocale());
						str.append("<#if "+cla_t.getAttribute().getAlias()+"??>");
						str.append( (str.length()>0? " - ":"") +  "${"+cla_t.getAttribute().getAlias()+  
									" ?string[\""+  mask  +"\"]} ");
						str.append("</#if>");
							count++;
					}
			}
		}

		if (count<2)
			str.append((str.length()>0? " - ":"") + "${oid}");

		
		logger.debug(str.toString());
		
		return str.toString();
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public Certificate getCertificate() {
		Certificate certificate = getDomain().getDomain().getCertificate();
		if (certificate==null) {
			try {
				KeyPair keys = ServiceLocator.getService(SystemSignatureService.class).createKeys();
				certificate = ServiceLocator.getService(SystemSignatureService.class).createCertificate(getDomain(), keys);
				((KbeeDomain)getDomain()).setCertificate(certificate);
				((KbeeDomain)getDomain()).setPrivateKey(keys.getPrivate());
				getContentDao().save(getDomain());
			}
			catch (SignatureException | IOException e) {
				certificate = null;
				logger.error(e);
			}
			
			catch (Exception e1) {
				certificate = null;
				logger.error(e1);
			}
		}
		return certificate;
	}
	
	public String getDefaultSubTitleRule( ContentTemplate ct) {
		return getDefaultTitleRule(ct);
	}
	
	public ContentDao getContentDao() {
		return contentDao;
	}
	
	public void setContentDao(ContentDao dao) {		
		contentDao=dao;
	}
	
	protected LanguageService getLanguageService() {
		return ServiceLocator.getService(LanguageService.class);
	}
	
	protected SecurityDao getSecurityDao() {
		return	(SecurityDao)ServiceLocator.getService(BeansService.class).getBean("securityDao");
	}
	
	protected List<Procedure> getProcedureLibrary(Domain domain) {
		return domain.getService(WorkflowDomainService.class).getProceduresLibrary();
	}
	
	private ExternalDao getExternalDao() {
		return (ExternalDao) ServiceLocator.getService(BeansService.class).getBean("externalDao");
	}
	
	private boolean hasHome(DataSet ds) {
		for (Classifier classifier : getContentDao().getClassifiers(ds.getDomain())) {
			if (classifier.getDataSet()!=null && classifier.getDataSet().equals(ds) && classifier.hasHome()) {
				return true;
			}
		}
		return false;
	}

	private User getSessionUser() {
		return ServiceLocator.getService(com.novamens.service.SecurityService.class).getSessionUser();
	}

	private void addDataSetMember(DataSet dataset, String value) throws ContentMgmtException {
		User domain_root = getRootUser(dataset.getDomain());
		DataSetMember mt_1 = dataset.createMember();
		mt_1.setDomain(dataset.getDomain());
		mt_1.setCreationOffsetDateTime(OffsetDateTime.now());
		mt_1.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		mt_1.setLastModifiedUser(domain_root);
		mt_1.setState(ObjectState.ENABLED);
		mt_1.setStrValue(value);
		getContentDao().save(mt_1);
		txlogger.info(new DataSetValueCreateEvent(mt_1, "create"));
	}

	private User getRootUser(Domain domain) {
		User rootuser = ((KbeeSecurityDao) getSecurityDao()).findUserByName("root@"+ domain.getName());
		return rootuser;
	}
}