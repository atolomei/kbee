package com.novamens.kbee.content.service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.sql.DataSource;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.base.ContentClass;
import com.novamens.content.base.ContentCreationException;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.base.ResourceFolder;
import com.novamens.content.base.Source;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.email.EmailTemplate;
import com.novamens.content.library.Library;
import com.novamens.content.model.AccessStrategy;
import com.novamens.content.model.AttributeTemplate;
import com.novamens.content.model.AttributeType;
import com.novamens.content.model.Classificable;
import com.novamens.content.model.Classification;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ClassifierTemplate;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.EntityMember;
import com.novamens.content.notes.Billboard;
import com.novamens.content.notification.ContentNotification;
import com.novamens.content.notification.Notification;
import com.novamens.content.notification.NotificationState;
import com.novamens.content.notification.NotificationType;
import com.novamens.content.notification.WorkNoteNotification;
import com.novamens.content.resource.KBFile;
import com.novamens.content.rule.ActionRule;
import com.novamens.content.service.ContentFactoryService;
import com.novamens.content.service.ContentService;

import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserRole;
import com.novamens.content.user.UserService;
import com.novamens.dao.SecurityDao;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.dom.Versionable;
import com.novamens.kbee.content.base.KbeeContentProxy;
import com.novamens.kbee.content.base.KbeeSource;
import com.novamens.kbee.content.email.KbeeEmailTemplate;
import com.novamens.kbee.content.library.KbeeLibrary;
import com.novamens.kbee.content.notification.KbeeContentConditionNotification;
import com.novamens.kbee.content.notification.KbeeContentPublishNotification;
import com.novamens.kbee.content.notification.KbeeNotification;
import com.novamens.kbee.content.notification.KbeeProgressNoteNotification;
import com.novamens.kbee.content.notification.KbeeWorkNoteNotification;
import com.novamens.kbee.content.notification.KbeeWorkflowNotification;
import com.novamens.kbee.content.resource.KBFileImpl;
import com.novamens.kbee.content.resource.KbeeResourceFolder;
import com.novamens.kbee.content.rule.KbeeActionRule;
import com.novamens.kbee.content.rule.KbeeIqlActionRule;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.security.acl.KbeeGroup;
import com.novamens.kbee.sql.SqlPlatform;
import com.novamens.kbee.sql.SqlPlatformFactory;
import com.novamens.logging.CreationEvent;
import com.novamens.logging.LibraryCreateEvent;
import com.novamens.logging.ObjectUpdateEvent;
import com.novamens.logging.SourceCreateEvent;
import com.novamens.metrics.SystemMetricsService;
import com.novamens.repository.DomRepository;
import com.novamens.repository.DomRepositoryService;
import com.novamens.security.User;
import com.novamens.security.acl.KbeeArea;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeRuntimeException;

/**
 * <p>Servicio que crea Contenidos.
 * 
 * Otros servicios relacionados:
 * @see
 * {@link ObjectFactoryService} para creación de objetos del Modelo de Información
 * {@link PortaFactoryService} para objetos de Portal
 * </p>
 */
public class KbeeContentFactoryService implements ContentFactoryService {

	private JdbcTemplate jdbcTemplate;
	private String schema;
	private SqlPlatform sqlplatform;
	private ContentDao contentDao;

	// sinc with the trx thread, so the log appender will create an entry in the DB upon commit
	//
	static private Logger trx_logger = LogManager.getLogger("TxLogger");
	static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeContentFactoryService.class.getName());
	
	public KbeeContentFactoryService() {
	}

	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)																	
	public EmailTemplate createEmailTemplate(String key, String title, String lang, String from, String subject, String text, boolean isDefault, String model) throws ContentCreationException, ContentMgmtException {
			return createEmailTemplateNoTrx(key, title, lang, from, subject, text, isDefault, model);
	}


	@Override
	@Transactional(propagation = Propagation.REQUIRED)																	
	public EmailTemplate createEmailTemplate(Domain domain, String key, String title, String lang, String from, String subject, String text, boolean isDefault, String model) throws ContentCreationException, ContentMgmtException {
			return createEmailTemplateNoTrx(domain, key, title, lang, from, subject, text, isDefault, model);
	}

	/**
	 * 
	 */
	@Override
	public EmailTemplate createEmailTemplateNoTrx(String key, String title, String lang, String from, String subject, String text, boolean isDefault, String model) throws ContentCreationException, ContentMgmtException {
		UserProfile userProfile = ServiceLocator.getService(UserService.class).getSessionUserProfile();
		return createEmailTemplateNoTrx(userProfile.getDomain(), key, title, lang, from, subject, text, isDefault, model);
	}
	/**
	 * 
	 */
	
	@Override
	public EmailTemplate createEmailTemplateNoTrx(Domain domain, String key, String title, String lang, String from, String subject, String text, boolean isDefault, String model) throws ContentCreationException, ContentMgmtException {
		
		KbeeEmailTemplate noti = new KbeeEmailTemplate();

		UserProfile userProfile = ServiceLocator.getService(UserService.class).getSessionUserProfile();
	
		noti.setDefault(isDefault);
		noti.setId(getNewOId());
		noti.setCreationOffsetDateTime(OffsetDateTime.now());
		noti.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		noti.setLastModifiedUser(userProfile.getUser());
		noti.setTitle(title);
		noti.setLanguage(lang);
		noti.setKey(key);
		noti.setFrom(from);
		noti.setSubject(subject);
		noti.setModel(model);
		noti.setState(ObjectState.ENABLED);
		
		if(text!=null) {
			Document doc = Jsoup.parse(Jsoup.clean(text, Safelist.basic()));
			String str = doc.text();
			noti.setStringTemplate(str);
		}
		
		noti.setDomain(domain);
		
		getContentDao().save(noti);
		// trx_logger.info(new EmailTemplateUpdateEvent(noti, "Create"));
		return noti;
	}
	
	/**
	 * 
	 * WorkNotes are Billboard Alerts
	 *  
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public WorkNoteNotification createWorkNoteNotification(Billboard note, User receiver) throws ContentCreationException, ContentMgmtException {
		
		KbeeWorkNoteNotification noti = new KbeeWorkNoteNotification(note);
		
		noti.setId(getNewNotificationId());
		noti.setCreationOffsetDateTime(OffsetDateTime.now());
		noti.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		noti.setOffsetDateTimeSent(OffsetDateTime.now());
		noti.setState(ObjectState.ENABLED);
		noti.setTitle(note.getTitle());
		
		// ---------------------------
		//
		// Start: Today at
		//
		noti.setStartpub(note.getStartpub()!=null?note.getStartpub() : OffsetDateTime.now());
		
		if (note.getEndpub()!=null)
			noti.setEndpub(note.getEndpub());
		
		if(note.getText()!=null) {
			Document doc = Jsoup.parse(Jsoup.clean(note.getText(), Safelist.basic()));
			String str = doc.text();
			if (str.length()>30)
				str=str.substring(0, 30)+"...";
			noti.setText(str);
		}
		else
			noti.setText("");
		
		noti.setNotificationState(NotificationState.PENDING);
		User sender = note.getLastModifiedUser();

		noti.setDeleteOnAccept(false);
		noti.setAlert(note.isAlert());
		noti.setBillboard(note.isBillboard());
		
		noti.setLastModifiedUser(sender);
		noti.setSender(sender);
		noti.setReceiver(receiver);
		noti.setDomain(note.getDomain());
		

		getContentDao().save(noti);
		
		return noti;
		
	}
	
	
	
	/**
	 * 
	 * 
	 * 
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public ContentNotification createContentPublishNotification(Content content, User receiver) throws ContentCreationException, ContentMgmtException {
		return this.createContentPublishNotification(content, receiver, false);
	}

	/**
	 * 
	 * 
	 * 
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public ContentNotification createContentPublishNotification(Content content, User receiver, boolean deleteOnAccept) throws ContentCreationException, ContentMgmtException {
																							
		KbeeContentPublishNotification noti = new KbeeContentPublishNotification(content);
		noti.setId(getNewNotificationId());
		noti.setCreationOffsetDateTime(OffsetDateTime.now());
		noti.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		noti.setOffsetDateTimeSent(OffsetDateTime.now());
		noti.setState(ObjectState.ENABLED);
		noti.setDeleteOnAccept(deleteOnAccept);
		noti.setTitle(content.getTitle());
		
		noti.setStartpub(OffsetDateTime.now().truncatedTo(ChronoUnit.DAYS));
		
		noti.setNotificationState(NotificationState.PENDING);
		User sender = content.getLastModifiedUser();

		noti.setLastModifiedUser(sender);
		noti.setSender(sender);
		noti.setReceiver(receiver);
		noti.setDomain(content.getDomain());
		
		getContentDao().save(noti);
		return noti;
	}

	/**
	 * 
	 * 
	 * 
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	public Notification createNotification(NotificationType type, Content content, String text, User receiver) throws ContentCreationException, ContentMgmtException {
									
		KbeeNotification notification;
		
		if (NotificationType.WORKFLOW.equals(type)) {
			notification = new KbeeWorkflowNotification(content);
		}
		else
		if (NotificationType.PROGRESS_NOTE.equals(type)) {
			notification = new KbeeProgressNoteNotification(content);
		}
		else
		if (NotificationType.CONTENT_AUDIT.equals(type)) {
			notification = new KbeeContentPublishNotification();
			notification.setDeleteOnAccept(false);
		}
		else
		if (NotificationType.CONTENT.equals(type)) {
			notification = new KbeeContentPublishNotification();
		}
		else {
			notification = new KbeeContentConditionNotification(content);
		}
		
		notification.setId(getNewNotificationId());
		
		notification.setCreationOffsetDateTime(OffsetDateTime.now());
		notification.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		notification.setOffsetDateTimeSent(OffsetDateTime.now());
		notification.setState(ObjectState.ENABLED);
		notification.setText(text);
		
		notification.setTitle(content.getTitle());
		
		notification.setStartpub(OffsetDateTime.now().truncatedTo(ChronoUnit.DAYS));
		
		notification.setNotificationState(NotificationState.PENDING);
		User sender = content.getLastModifiedUser();

		notification.setLastModifiedUser(sender);
		notification.setSender(sender);
		notification.setReceiver(receiver);
		notification.setDomain(content.getDomain());
		
		getContentDao().save(notification);
		return notification;
	}
	
	/**
	 * 
	 * 
	 * 
	 */
	@Override
	@Transactional
	public Content create(String templatename) throws ContentCreationException, ContentMgmtException {
		return create(templatename, true, false);
	}
	
	/**
	 * 
	 * 
	 * 
	 */
	@Override
	@Transactional
	public Content create(String templatename, boolean workspace) throws ContentCreationException, ContentMgmtException {
		return create(templatename, workspace, false);
	}

	/**
	 * 
	 */
	@Override
	@Transactional
	public Content create(String templatename, KBFile file) throws ContentCreationException {
		try {
			Content content = create(templatename, true, false);
			content.setTitle(FilenameUtils.getBaseName(file.getBaseName()));
			content.getService(ContentService.class).addFile(file);
			return content;
		}
		catch (Exception e) {
			logger.error(e);					
			throw new ContentCreationException(e);
		}
	}	
	

	/**
	 * 
	 */
	@Override
	@Transactional
	public Content create(String templatename, KBFile file, ObjectState state) throws ContentCreationException {
		try {
			Content content = create(templatename, true, false);
			// content.setTitle(FilenameUtils.getBaseName(file.getBaseName()));
			content.setTitle(file.getTitle());
			content.setState(state);
			content.getService(ContentService.class).addFile(file);
			return content;
		}
		catch (Exception e) {
			logger.error(e);					
			throw new ContentCreationException(e);
		}
	}	


	/**
	 * 
	 */
	@Override
	@Transactional
	public Content create(String templatename, KBFile file, ObjectState state, User user) throws ContentCreationException {
		
		
		
		
		
		
		try {
	
			UserProfile userProfile = ServiceLocator.getService(UserService.class).getSessionUserProfile();
			
			
			ContentTemplate template = getContentDao().findContentTemplateByName(templatename, userProfile.getDomain().getId());
			
			if (template == null)
				throw new InstantiationException("templatename not found " + templatename);
			
			
			ContentClass contentClass = template.getContentClass();
		
			Class<?> javaclass = Class.forName(contentClass.getJavaClass());
			
			Object instance = javaclass.getDeclaredConstructor().newInstance(); 
			
			if (!(instance instanceof Content))
				throw new InstantiationException(instance.getClass().getName() +" is not Content");
			
			Content content = (Content) instance;
			
			content.setWorkspace((Long) user.getId());
			((Versionable<?>)content).setHeadVersion(false);
			
			content.setTitle(getDefaultTitle(template));
			content.setContentTemplate(template);
			content.setOId(getNewOId());
			content.setLastModifiedUser(userProfile.getUser());
			content.setCreationOffsetDateTime(OffsetDateTime.now());
			content.setLastModifiedOffsetDateTime(OffsetDateTime.now());
			content.setCommentsEnabled(true); 
			classify(content);
			
			content.setTitle(file.getTitle());
			content.setState(state);
			
			getContentDao().save(content);
			trx_logger.info(new CreationEvent(content));
			
			content.getService(ContentService.class).addFile(file);

			
			return content;
			
		}
		catch (ClassNotFoundException | InstantiationException | IllegalAccessException e)  {
			logger.error(e);
			throw new ContentCreationException(e);
		}
		catch (java.lang.OutOfMemoryError e) {
			logger.error(e);
			ServiceLocator.getService(SystemMetricsService.class).setTimeOutOfMemoryFlag();
			throw e;
		}
		catch (Exception e)  {
			logger.error(e);
			throw new ContentCreationException(e);
		}
		
		
		
	}	
	
	/**
	 * 
	 *	Id: id unico del objeto de informacion (version del recurso)
	 *	oId: id del recurso (todos las versiones del recurso tienen ese id)
	 * 
	 * @return
	 * @throws ContentMgmtException 
	 */
	@Override
	@Transactional
	public Content create(String templatename, boolean workspace, boolean quiet) throws ContentCreationException, ContentMgmtException {
		try {
			UserProfile userProfile = ServiceLocator.getService(UserService.class).getSessionUserProfile();
			
			ContentTemplate template = getContentDao().findContentTemplateByName(templatename, userProfile.getDomain().getId());
			
			if (template == null)
				throw new InstantiationException("templatename not found " + templatename);
			
			
			ContentClass contentClass = template.getContentClass();
		
			Class<?> javaclass = Class.forName(contentClass.getJavaClass());
			
			Object instance = javaclass.getDeclaredConstructor().newInstance(); 
			
			if (!(instance instanceof Content))
				throw new InstantiationException(instance.getClass().getName() +" is not Content");
			
			Content content = (Content) instance;
			
			if (workspace) {
				content.setWorkspace((Long)userProfile.getUser().getId());
				((Versionable<?>)content).setHeadVersion(false);
			}
			
			content.setTitle(getDefaultTitle(template));
			content.setContentTemplate(template);
			content.setOId(getNewOId());
			content.setLastModifiedUser(userProfile.getUser());
			content.setCreationOffsetDateTime(OffsetDateTime.now());
			content.setLastModifiedOffsetDateTime(OffsetDateTime.now());
			content.setCommentsEnabled(true); 
			classify(content);
			
			// logger.debug(content.toString());
			
			
			if (!quiet) {
				getContentDao().save(content);
				trx_logger.info(new CreationEvent(content));
			} 
			
			return content;
			
		}
		catch (ClassNotFoundException | InstantiationException | IllegalAccessException e)  {
			logger.error(e);
			throw new ContentCreationException(e);
		}
		catch (java.lang.OutOfMemoryError e) {
			logger.error(e);
			ServiceLocator.getService(SystemMetricsService.class).setTimeOutOfMemoryFlag();
			throw e;
		}
		catch (Exception e)  {
			logger.error(e);
			throw new ContentCreationException(e);
		}
	}
	
	/** 
	 * 
	 * 
	 * 
	 */
	
	@Override
	@Transactional
	public Content createProxy(Content content) throws ContentCreationException, ContentMgmtException {
		try {
			KbeeContentProxy proxy = new KbeeContentProxy();
			proxy.setContent(content);
			proxy.setLastModifiedUser(getSessionUser());
			proxy.setCreationOffsetDateTime(OffsetDateTime.now());
			proxy.setLastModifiedOffsetDateTime(OffsetDateTime.now());
			getContentDao().save(proxy);
			return proxy;
			
		}
		catch (java.lang.OutOfMemoryError e) {
			logger.error(e);
			ServiceLocator.getService(SystemMetricsService.class).setTimeOutOfMemoryFlag();
			throw e;
		}
		catch (Exception e)  {
			logger.error(e);
			throw new ContentCreationException(e);
		}
	}
	
	/** 
	 * 
	 * 
	 * 
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public Source createSource(String name, String displayName) throws ContentCreationException, ContentMgmtException {
		
		User user  = ServiceLocator.getService(SecurityService.class).getSessionUser();
		KbeeSource source= new KbeeSource();
		source.setName(name.toLowerCase());
		source.setDisplayName(displayName);
		source.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		source.setCreationOffsetDateTime(OffsetDateTime.now());
		source.setLastModifiedUser(user);
		source.setDomain(((KbeeUser)user).getDomain());
		getContentDao().save(source);
		trx_logger.info(new SourceCreateEvent(source, "Create"));
		return source;
	}
	
	/**
	 * 
	 * Al crear el Domain desde el domain kbee se debe usar este metodo por el Domain 
	 * 
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public Source createSource(String name, String displayName, Domain domain) throws ContentCreationException, ContentMgmtException {
		
		User user  = ServiceLocator.getService(SecurityService.class).getSessionUser();
		KbeeSource source= new KbeeSource();
		source.setName(name.toLowerCase());
		source.setDisplayName(displayName);
		source.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		source.setCreationOffsetDateTime(OffsetDateTime.now());
		source.setLastModifiedUser(user);
		source.setDomain(domain);
		getContentDao().save(source);
		trx_logger.info(new SourceCreateEvent(source, "Create"));
		return source;
	}

	
 	/**
	 * 
	 * 
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public Library createLibrary(String name) throws ContentCreationException, ContentMgmtException {
		UserProfile userProfile = ServiceLocator.getService(UserService.class).getSessionUserProfile();
		return createLibrary(name, userProfile.getDomain());
		
		/**
		KbeeLibrary library = new KbeeLibrary();
		if (name==null)
			name=library.getId().toString();
		library.setName(name);
		String key = name;
		if (key.length()>24)
			key=key.substring(0, 23);
		logger.debug("Library name " + name + " key:  " + key.toLowerCase());
		library.setKey(key.toLowerCase().replaceAll("[°,¡!?¿:\\/\"-().\\s]", "-"));
		library.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		library.setCreationOffsetDateTime(OffsetDateTime.now());
		library.setLastModifiedUser(userProfile.getUser());
		library.setState(ObjectState.ENABLED);
		library.setDomain(userProfile.getDomain());
		KbeeGroup readers = new KbeeGroup();
		readers.setCanonical(true);
		readers.setName(name);
		readers.setArea(KbeeArea.CONTENT);
		readers.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		readers.setCreationOffsetDateTime(OffsetDateTime.now());
		readers.setLastModifiedUser(userProfile.getUser());
		readers.setDomain(userProfile.getDomain());
		getSecurityDao().save(readers);
		library.setReaders(readers);
		getContentDao().save((Library)library);
		trx_logger.info(new LibraryCreateEvent((Library)library, "Create"));
		// create library portal
		ServiceLocator.getService(SiteFactoryService.class).createLibrarySite(library);
		return library;
		**/
	}
	
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public Library createLibrary(String name, Domain domain) throws ContentCreationException, ContentMgmtException {
		
		UserProfile userProfile = ServiceLocator.getService(UserService.class).getSessionUserProfile();
		
		KbeeLibrary library = new KbeeLibrary();
		library.setName(name);
	
		String key = name;
		
		if (key.length()>24)
			key=key.substring(0, 23);
		
		logger.debug("Library name " + name + " key:  " + key.toLowerCase());
		
		library.setKey(key.toLowerCase().replaceAll("[°,¡!?¿:\\/\"-().\\s]", "-"));
		
		library.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		library.setCreationOffsetDateTime(OffsetDateTime.now());
		library.setLastModifiedUser(userProfile.getUser());
		library.setState(ObjectState.ENABLED);
		library.setDomain(domain);
		library.setCriteria("ishead(true)");
		library.setOrder(getContentDao().getLibraries(domain).size());
		
		
		
		// ----
		//
		KbeeGroup readers = new KbeeGroup();
		readers.setCanonical(true);
		readers.setName(name);
		readers.setArea(KbeeArea.CONTENT);
		readers.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		readers.setCreationOffsetDateTime(OffsetDateTime.now());
		readers.setLastModifiedUser(userProfile.getUser());
		readers.setDomain(domain);
		getSecurityDao().save(readers);
		
		library.setReaders(readers);
		getContentDao().save((Library)library);
		
		trx_logger.info(new LibraryCreateEvent((Library)library, "Create"));
		
		// ServiceLocator.getService(SiteFactoryService.class).createLibrarySite(library);
		
		return library;
	}
	
	/**
	 * 
	 * 
	 * 
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	public ActionRule createRule() throws ContentCreationException, ContentMgmtException {
		
		return createRule(null);
		/**
		UserProfile userProfile = ServiceLocator.getService(UserService.class).getSessionUserProfile();
		Domain domain = ServiceLocator.getService(UserService.class).getDomain();
		
		KbeeActionRule rule = new KbeeIqlActionRule();
		
		rule.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		rule.setCreationOffsetDateTime(OffsetDateTime.now());
		rule.setLastModifiedUser(userProfile.getUser());
		rule.setState(ObjectState.ENABLED);
		rule.setDomain(domain);
		rule.setName("New Rule by " + getSessionUser()!=null ? getSessionUser().getDisplayName() : "");
		
		getRepository(ActionRule.class).save(rule);
		
		trx_logger.info(new ObjectUpdateEvent<KbeeActionRule>(rule, "Create"));
		
		return rule;
		**/
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public ActionRule createRule(Content content) throws ContentCreationException, ContentMgmtException {
		UserProfile userProfile = ServiceLocator.getService(UserService.class).getSessionUserProfile();
		Domain domain = ServiceLocator.getService(UserService.class).getDomain();
		KbeeIqlActionRule rule = new KbeeIqlActionRule();
		rule.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		rule.setCreationOffsetDateTime(OffsetDateTime.now());
		rule.setLastModifiedUser(userProfile.getUser());
		rule.setState(ObjectState.ENABLED);
		rule.setDomain(domain);
		
		if (content!=null) {
			rule.setName(content.getTitle());
			rule.setContentRule(true);
			rule.setContentOid(content.getOId());
			rule.setCondition("contentOId(" + String.valueOf(content.getOId())+")");
		}
		else {
			rule.setName("New Rule - " + getSessionUser()!=null ? getSessionUser().getDisplayName() : "");	
		}

		getRepository(ActionRule.class).save(rule);
		
		trx_logger.info(new ObjectUpdateEvent<KbeeActionRule>(rule, "Create"));
		return rule;
	}
	
	
	
	/**
	 * 
	 * 
	 * 
	 */
	@Override
	public Long getNewNotificationId() {
		SqlPlatform sqlplatform = getSqlPlatform(); 
		String sequencename = "objectid_sequence";
		if (getSchema()!=null && !getSchema().equals("")) sequencename = getSchema() + "." + sequencename;
		Long value = (Long)this.jdbcTemplate.query(sqlplatform.nextSequenceQuery(sequencename), new ResultSetExtractor<Long>() {
			public Long extractData(ResultSet rs) throws SQLException, DataAccessException {
				if (rs.next())
				return rs.getLong(1);
				return null;
			}
		}); 
		return value;
	}
	
	
	/**
	 * 
	 *	Id: id unico del objeto de informacion (version del recurso)
	 *	oId: id del recurso (todos las versiones del recurso tienen ese id)
	 * 
	 * @return
	 */
	@Override
	public Long getNewResourceOId() {
		SqlPlatform sqlplatform = getSqlPlatform(); 
		String sequencename = "resourceid_sequence";
		if (getSchema()!=null && !getSchema().equals("")) sequencename = getSchema() + "." + sequencename;
		Long value = (Long)this.jdbcTemplate.query(sqlplatform.nextSequenceQuery(sequencename), new ResultSetExtractor<Long>() {
			public Long extractData(ResultSet rs) throws SQLException, DataAccessException {
				if (rs.next())
				return rs.getLong(1);
				return null;
			}
		}); 
		return value;
	}
	
	/**
	 * 
	 *	Id: id unico del objeto de informacion (version del recurso)
	 *	oId: id del recurso (todos las versiones del recurso tienen ese id)
	 * 
	 * @return
	 */
	@Override
	public Long getNewOId() {
		SqlPlatform sqlplatform = getSqlPlatform(); 
		String sequencename = "contentid_sequence";
		if (getSchema()!=null && !getSchema().equals("")) sequencename = getSchema() + "." + sequencename;
		Long value = (Long)this.jdbcTemplate.query(sqlplatform.nextSequenceQuery(sequencename), new ResultSetExtractor<Long>() {
			public Long extractData(ResultSet rs) throws SQLException, DataAccessException {
				if (rs.next())
				return rs.getLong(1);
				return null;
			}
		}); 
		return value;
	}

	
	
	/**
	 * 
	 *	Id: id unico del objeto de informacion (version del recurso)
	 *	oId: id del recurso (todos las versiones del recurso tienen ese id)
	 * 
	 * @return
	 */
	@Override
	public Long getResourceNewOId() {
		SqlPlatform sqlplatform = getSqlPlatform(); 
		String sequencename = "resourceid_sequence";
		if (getSchema()!=null && !getSchema().equals("")) sequencename = getSchema() + "." + sequencename;
		Long value = (Long)this.jdbcTemplate.query(sqlplatform.nextSequenceQuery(sequencename), new ResultSetExtractor<Long>() {
			public Long extractData(ResultSet rs) throws SQLException, DataAccessException {
				if (rs.next())
				return rs.getLong(1);
				return null;
			}
		}); 
		return value;
	}
	
	@Override
	public void setDataSource(DataSource dataSource) {
		jdbcTemplate = new JdbcTemplate(dataSource);
	}
	
	@Override
	public void setSchema(String schema) {
		this.schema = schema;
	}
	
	@Override
	public String getSchema() {
		return this.schema;
	}
	
	@Transactional(propagation = Propagation.REQUIRED)
	public KBFile createKBFile(String name) throws ContentCreationException, ContentMgmtException {
		return createKBFileNoTrx(name); 
	}
	
	@Override
	public KBFile createKBFileNoTrx(String name) throws ContentCreationException, ContentMgmtException {
		try {
			Domain domain = ServiceLocator.getService(UserService.class).getSessionUserProfile().getDomain();
			
			KBFileImpl file = new KBFileImpl();
			file.setOId(getResourceNewOId());
			
			file.setVersion(0);
			file.setInPortalVersion(true);
			file.setDomain(domain);
			file.setName(name);
			String title = FilenameUtils.getBaseName(name).replaceAll("(-|_)", " ");
			title= ((title!=null &&title.length()>1) ? title.replace(".", "") : "-");
			file.setTitle(title);
			file.setState(ObjectState.ENABLED);
			file.setCreationOffsetDateTime(OffsetDateTime.now());
			file.setLastModifiedUser(ServiceLocator.getService(SecurityService.class).getSessionUser());
			file.setUploadOffsetDateTime(OffsetDateTime.now());
			
			return file;
		} 
		catch (Exception e) {
			throw new ContentCreationException(e);
		}
	}
	
	@Transactional(propagation = Propagation.REQUIRED)
	public ResourceFolder createFolder(String name) throws ContentCreationException, ContentMgmtException {
		try {
			Domain domain = ServiceLocator.getService(UserService.class).getSessionUserProfile().getDomain();
			
			KbeeResourceFolder folder = new KbeeResourceFolder();
			folder.setOId(getResourceNewOId());
			
			folder.setDomain(domain);
			//String title = FilenameUtils.getBaseName(name).replaceAll("(-|_)", " ");
			//title= ((title!=null &&title.length()>1) ? title.replace(".", "") : "-");
			folder.setTitle(name);
			folder.setName(name);
			folder.setState(ObjectState.ENABLED);
			folder.setCreationOffsetDateTime(OffsetDateTime.now());
			folder.setLastModifiedUser(ServiceLocator.getService(SecurityService.class).getSessionUser());
			
			getContentDao().save(folder);
			
			return folder;
		} 
		catch (Exception e) {
			throw new ContentCreationException(e);
		}
	}

	
	
	private String getDefaultTitle(ContentTemplate template) {
		User user = ServiceLocator.getService(SecurityService.class).getSessionUser();
		Locale locale = user!=null && user.getLocale()!=null ? user.getLocale() : Locale.getDefault(); 
		ResourceBundle resourcebundle = ResourceBundle.getBundle(getClass().getName(), locale);
		String title = resourcebundle.getString("new-label") + " " + template.getName();
		return title;
	}
	
	private void classify(Content content) {
		
		for (ClassifierTemplate template : content.getContentTemplate().getClassifiers()) {
			if (AccessStrategy.Roles.equals(template.getAccessibility())) {
				List<EntityMember> entities = new ArrayList<EntityMember>();
				for (UserRole userrole : ServiceLocator.getService(UserService.class).getSessionUserProfile().getRoles()) {
					if (userrole.getEntity()!=null && userrole.getEntity().getDataSet().equals(template.getClassifier().getDataSet())) {
						entities.add(userrole.getEntity());
					}
				}
				if (entities.size()==1) {
					List<DataSetMember> members = new ArrayList<DataSetMember>(1);
					members.add(entities.get(0));
					content.setClassification(template.getClassifier(), members);
				}
			}
		}
		
		boolean update = true;
		List<Classifier> classified = new ArrayList<Classifier>();
		while (update) {
			update = false;
			for (Classification classification : content.getClassification()) {
				for (ClassifierTemplate template : content.getContentTemplate().getClassifiers()) {
					if (template.getParent()!=null && template.getParent().equals(classification.getClassifier())) {
						Classifier classifier = template.getClassifier();
						if (!classified.contains(classifier)) {
							List<Classification> memberclassification = ((Classificable)classification.getDataSetMember()).getClassification(classifier);
							classified.add(classifier);
							if (memberclassification.size()==1) {
								List<DataSetMember> members = new ArrayList<DataSetMember>(1);
								members.add(memberclassification.get(0).getDataSetMember());
								content.setClassification(classifier, members);
								update = true;
								break;
							}
						}
					}
				}
				if (update) break;
			}
		}
		
		for (AttributeTemplate template : content.getContentTemplate().getAttributes()) {
			if (template.getAttribute().getType().equals(AttributeType.BOOLEAN)) {
				List<String> values = new ArrayList<String>();
				values.add("false");
				content.setAttributeValues(template.getAttribute(), values);
			}	
		}
	}
	
	public ContentDao getContentDao() {
		return contentDao;
	} 
	
	public void setContentDao(ContentDao dao) {
		contentDao=dao;
	}
	
	private SqlPlatform getSqlPlatform() {
		if (sqlplatform!=null) 
			return sqlplatform;
		Connection connection = null;
		try {
			connection = this.jdbcTemplate.getDataSource().getConnection();
			sqlplatform = SqlPlatformFactory.getPlatformFor(connection.getMetaData());
		}
		catch (SQLException e) {
			logger.error(e);
			throw new KbeeRuntimeException(e);
		}
		finally {
			if (connection!=null) {
				try {
					connection.close();
				}				
				catch (SQLException e) {
					logger.error(e);
					throw new KbeeRuntimeException(e);
				}
			}
		}
		return sqlplatform;
	}
	
	private User getSessionUser() {
		try {
			return ServiceLocator.getService(SecurityService.class).getSessionUser();
		} 
		catch (Exception e) {
			logger.error(e);
		}
		return null;
	}
	
	private SecurityDao getSecurityDao() {
		return (SecurityDao)ServiceLocator.getService(BeansService.class).getBean("securityDao");
	}
	
	private <R> DomRepository<R> getRepository(Class<R> objectclass) {
		DomRepository<R> repository = ServiceLocator.getService(DomRepositoryService.class).getRepository(objectclass);
		return repository;
	}
	
}