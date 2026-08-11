package com.novamens.kbee.content.service;


import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Element;

import com.novamens.content.base.Content;
import com.novamens.content.base.ContentLink;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.base.Relation;
import com.novamens.content.base.Resource;
import com.novamens.content.base.ResourceContainer;
import com.novamens.content.base.ResourceFolder;
import com.novamens.content.base.ResourceNode;
import com.novamens.content.base.ResourceTag;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.form.EForm;
import com.novamens.content.form.EFormData;
import com.novamens.content.form.EFormField;
import com.novamens.content.form.EResourceField;
import com.novamens.content.form.UpdatedField;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.AttributeTemplate;
import com.novamens.content.model.AttributeType;
import com.novamens.content.model.Classification;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ClassifierTemplate;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.DataSetType;
import com.novamens.content.model.LabelSet;
import com.novamens.content.model.ModelElementTemplate;
import com.novamens.content.properties.PropertyService;
import com.novamens.content.resource.ExternalResource;
import com.novamens.content.resource.KBFile;
import com.novamens.content.security.ContentSystemSecurityService;
import com.novamens.content.service.ContentFactoryService;
import com.novamens.content.service.ContentService;
import com.novamens.content.service.ContentSubscriptionService;
import com.novamens.content.service.FileService;
import com.novamens.content.service.FileSnippet;
import com.novamens.content.service.LabelsService;
import com.novamens.content.social.SocialService;
import com.novamens.content.text.AncordResolver;
import com.novamens.content.text.ImageResolver;
import com.novamens.content.text.Text;
import com.novamens.content.text.TextChange;
import com.novamens.content.user.UserDevice;
import com.novamens.content.user.UserLabel;
import com.novamens.content.user.UserSignature;
import com.novamens.content.version.VersionService;
import com.novamens.content.workflow.ProcessLauncher;
import com.novamens.content.workflow.WorkflowDomainService;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.datetime.DateTimeService;
import com.novamens.dom.ObjectState;
import com.novamens.dom.Versionable;

import com.novamens.event.AppAssignEvent;
import com.novamens.event.AppCheckoutEvent;
import com.novamens.event.AppCreateEvent;
import com.novamens.event.AppDeleteEvent;
import com.novamens.event.AppRecycleEvent;
import com.novamens.event.AppRestoreEvent;

import com.novamens.event.AppUpdateEvent;
import com.novamens.event.EventService;
import com.novamens.indexer.java.FileIndexerService;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.query.TextQuery;
import com.novamens.indexer.service.JavaIndex;
import com.novamens.kbee.content.base.KbeeContent;
import com.novamens.kbee.content.base.KbeeContentLink;
import com.novamens.kbee.content.base.KbeeSignedData;
import com.novamens.kbee.content.event.AppCheckinEvent;
import com.novamens.kbee.content.resource.KBFileImpl;
import com.novamens.kbee.content.text.KbeeText;
import com.novamens.kbee.text.KbeeTextTemplate;
import com.novamens.lock.LockScope;
import com.novamens.lock.ObjectLockService;
import com.novamens.logging.AssignationEvent;
import com.novamens.logging.CheckinEvent;
import com.novamens.logging.CheckoutEvent;
import com.novamens.logging.ContentEvent;
import com.novamens.logging.CreationEvent;
import com.novamens.logging.DropcheckoutEvent;
import com.novamens.logging.RemoveEvent;
import com.novamens.logging.SignEvent;
import com.novamens.logging.UpdateAddResourceEvent;
import com.novamens.logging.UpdateEvent;
import com.novamens.logging.UpdateFormEvent;
import com.novamens.metrics.SystemMetricsService;
import com.novamens.security.User;
import com.novamens.security.acl.Acl;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.signature.SignatureException;
import com.novamens.signature.SystemSignatureService;
import com.novamens.workflow.WorkflowContext;



/** 
 * <p>Servicios de alta, baja y cambio de estado de un {@link Content}:</p>
 * <ul>
 * <li>Create</li>
 * <li>Delete</li>
 * <li>Update</li>
 * <li>Restore</li>
 * <li>archive</li>
 * <li>Add property</li> 
 * </ul>
 * <p>Existen otros servicios de la capa de negocios asociados con un {@link Content}:
 * {@link SubscriptionService} suscripción para seguimiento de un contenido.</p>
 * 
 * <p>
 * Para DOM {@link Objects} diferentes a Content se usa:
 *  {@link KbeeObjectService}
 * </p>
 * <p>
 * <b>txlogger</b> 
 * is a log4j2 logger that is executed by the same thread. Logs Audit events.
 * </p>
 * <p>
 * <b>txLogger</b> 
 * is a log4j2 logger that is executed by the same thread (default) or can be set up to execut async for* performance. Logs Audit events.
 *</p>
 *<p>
 * <b>Spring ServiceLocator.getService(EventsService.class)</b> 
 * is also executed in the same thread. It serves for additional 
 * actions inside the same database transaction. 
 * </p>
 *  
 * <p>
 * see {@code log4j2.xml}
 * <br />
 * <br />
 *{@code <Logger name="txLogger" level="info" additivity="false"> 
			<AppenderRef ref="SyncAuditor"/>
			<AppenderRef ref="EventNotifier"/>
		</Logger>
   
   }
   </p>
 */

public class KbeeContentService implements ContentService {
			
														
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeContentService.class.getName());
	
	private Content content  = null;
	private ContentDao contentDao;

	public static  final  String PROPERTY_UNREAD = "unread";
	public static  final  String PROPERTY_NOTE = "note";
	public static  final  String PROPERTY_SENDER = "sender";
	public static  final  String PROPERTY_WORKFLOW = "workflow";
	public static  final  String PROPERTY_ALERTS = "alerts";
	public static  final  String PROPERTY_DUEDATE_ALERTS = "duedate-alerts";
	public static  final  String PROPERTY_ASSIGNATION_TIME = "assignation-time";
	public static  final  String PROPERTY_ACTIVITY_RESOURCES = "activity-resources";
	public static  final  String CONSOLES_PERSISTS_LABELS = "consolesPersistLabels";
					
	/**
	 *   The </b>TxLogger</b> is set up in Log4J to log synchronoulsy with the Thread.
	 *   This is different from all the other logs that work asynchronously
     */
	static private Logger txLogger = LogManager.getLogger("TxLogger");
	
	
	public KbeeContentService() {
	}

	public KbeeContentService(Content content) {
		 this.content = content;
	}
	

	/**
	 * 
	 * 
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void delete(String parameter) throws ContentMgmtException {
		

		// esto deberia ser resuelto por el cascade pero no se pudo
		for (Relation relation : getContent().getReverseRelations()) {
			if (relation!=null) {
			relation.getSource().removeRelation(relation);
			}
		}

		
		getContentDao().delete(getContent());

		// see log4j2.xml configuration
		RemoveEvent event = new RemoveEvent(getContent());
		event.setParameters(parameter);
		txLogger.info(event);
		
		// Spring
		ServiceLocator.getService(EventService.class).fire(new AppDeleteEvent(getContent()));
	}

	/**
	 * 
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void delete() throws ContentMgmtException {

		// esto deberia ser resuelto por el cascade pero no se pudo
		for (Relation relation : getContent().getReverseRelations()) {
			if (relation!=null) {
			relation.getSource().removeRelation(relation);
			}
		}
		
		getContentDao().delete(getContent());

		// Log 
		RemoveEvent event = new RemoveEvent(getContent());
		event.setParameters("Delete from System");
		txLogger.info(event);
		
		// Spring
		ServiceLocator.getService(EventService.class).fire(new AppDeleteEvent(getContent()));
	}

	/**
	 * 
	 * 
	 */

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void create() throws ContentMgmtException {
	
		getContentDao().save(getContent());
		// Log
		txLogger.info(new CreationEvent(getContent()));
		
		// Spring
		ServiceLocator.getService(EventService.class).fire(new AppCreateEvent(getContent()));
	}
	

	/**
	 * Send to the Recycle Bin
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRED) 
	public void recycle()  throws ContentMgmtException {
		
		if (!getContent().isHeadVersion()) {
			throw new ContentMgmtException("only head version can be deleted");
		}

		getContent().setState(ObjectState.DELETED);
		getContent().setLastModifiedUser(getUser());
		getContent().setLastModifiedOffsetDateTime(OffsetDateTime.now());
		getContentDao().save(getContent());
			
		// Log
		RemoveEvent event = new RemoveEvent(getContent());
		
		//if (getContent().isHeadVersion())
		event.setParameters("Recycle Bin");

		// Spring
		txLogger.info(event);
		ServiceLocator.getService(EventService.class).fire(new AppRecycleEvent(getContent()));
	}

	/** 
	 * Restore from the Recycle Bin 
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void restore()  throws ContentMgmtException {
		
		if (getContent().getState()!=ObjectState.DELETED) {
			logger.error("Object "+ getContent().getTitle() + "  Id:" +getContent().getId().toString() +". is not in Recycly Bin.");
			return;
		}

		getContent().setState(ObjectState.ENABLED);
		getContent().setLastModifiedUser(getUser());
		getContent().setLastModifiedOffsetDateTime(OffsetDateTime.now());
			
		User user = ServiceLocator.getService(SecurityService.class).getSessionUser();
			
		if (!getContent().isHeadVersion()) {
			
				if (((KbeeContent)getContent()).getPreviousVersion()!=null) {
					Content previousversion = ((KbeeContent)getContent()).getPreviousVersion();
					if (!previousversion.isHeadVersion() || previousversion.isLocked()) {
						return;
					}
					ObjectLockService lockService = previousversion.getService(ObjectLockService.class);
		 	 		lockService.lock(LockScope.EXCLUSIVE);
					previousversion.setLocked(true);
					getContentDao().save(previousversion);
				}
				else {
					((KbeeContent)getContent()).setVersion(1);
				}
				
				getContent().setWorkspace((Long)user.getId());
				getContentDao().save(getContent());
				
				ProcessLauncher launcher = getContextLauncher(); 
				if (launcher == null) {
					logger.error("Object "+ getContent().getTitle() + "  Id:" +getContent().getId().toString() +". context launcher nor found!");
					return;
				}
				
				// Log 
				txLogger.info(new UpdateEvent(getContent(), "Restore"));

				// Spring
				ServiceLocator.getService(EventService.class).fire(new AppUpdateEvent(getContent()));
				

				getContent().getService(WorkflowService.class).startProcess(launcher.getProcedure());
				
			}	
			else {
				getContentDao().save(getContent());

				// Log Spring
				txLogger.info(new UpdateEvent(getContent(), "Restore"));
				ServiceLocator.getService(EventService.class).fire(new AppRestoreEvent(getContent()));
		}
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public Content become(String templateName)  throws ContentMgmtException {
		Content newcontent = ServiceLocator.getService(ContentFactoryService.class).create(templateName);
		
		for (ResourceTag tag : getContent().getContentTemplate().getResourceTags()) {
			for (ResourceTag newtag : newcontent.getContentTemplate().getResourceTags()) {
				if (tag.equals(newtag)) {
					for (Resource resource : ((ResourceContainer)getContent()).getResources(tag.getName())) {
						((ResourceContainer)newcontent).addResource(resource, tag);
					}
				}
			}
		}
		
		for (Classification classification : getContent().getClassification()) {
			for (ModelElementTemplate modelTemplate : newcontent.getContentTemplate().getStructure()) {
				if (classification.getClassifier().equals(modelTemplate.getElement())) {
					newcontent.setClassification(classification.getClassifier(), classification.getDataSetMember());
				}
			}
		}
		
		for (AttributeTemplate modelTemplate : newcontent.getContentTemplate().getAttributes()) {
			List<String> values =  getContent().getAttributeValues(modelTemplate.getAttribute());
			if (!values.isEmpty()) {
				newcontent.setAttributeValues(modelTemplate.getAttribute(), values);
			}
		}
		
		return newcontent;
	}
	
	@Transactional(propagation = Propagation.REQUIRED)
	public Content becomeAndLaunch(String templateName, String launcherName)  throws ContentMgmtException {
		boolean launched = false;
		Content newcontent = become(templateName);
		for (ProcessLauncher launcher : newcontent.getContentTemplate().getProcessLaunchers()) {
			if (launcher.getLabel().equals(launcherName)) {
				newcontent.getService(WorkflowService.class).startProcess(launcher, null, true);
				launched = true;
			}
		}
		if (!launched) {
			throw new ContentMgmtException("launcher not found");
		}
		return newcontent;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void update() throws ContentMgmtException {
			updateNoTrx();
	}
	
	static String PROPERTY_HAS_TAGS ="tags";
	
	@Override
	public void updateNoTrx() throws ContentMgmtException {
		
		
		
		getContentDao().save(getContent());
		
		// TAGS ---
		String nr = "0";
		for (com.novamens.content.model.Classification  ca: getContent().getClassification()) {
			if (ca!=null && ca.getDataSetMember()!=null && ca.getDataSetMember().getDataSet() instanceof LabelSet) {
				nr="has-tags";
				break;
			}
		}
		getContent().getService(PropertyService.class).setProperty(PROPERTY_HAS_TAGS, nr);
		// --

		
		
		txLogger.info(new UpdateEvent(getContent()));
		ServiceLocator.getService(EventService.class).fire(new AppUpdateEvent(getContent()));
	}
	

	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)		
	public void update(String updatedPart) throws ContentMgmtException {
		List<String> updatedParts = new ArrayList<String>();
		updatedParts.add(updatedPart);
		update(updatedParts);
	}
	
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)		
	public void update(Resource resource, String updatedPart) throws ContentMgmtException {
		getContentDao().save(getContent());
		txLogger.info(new UpdateEvent(getContent(), resource, updatedPart));
		ServiceLocator.getService(EventService.class).fire(new AppUpdateEvent(getContent()));
	}
	
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void update(List<String> updatedParts) throws ContentMgmtException {
		updatedParts.removeIf(part -> "".equals(part));
		getContentDao().save(getContent());
		
		// TAGS ---
		String nr = "0";
		for (com.novamens.content.model.Classification  ca: getContent().getClassification()) {
			if (ca!=null && ca.getDataSetMember()!=null && ca.getDataSetMember().getDataSet() instanceof LabelSet) {
				nr="has-tags";
				break;
			}
		}
		getContent().getService(PropertyService.class).setProperty(PROPERTY_HAS_TAGS, nr);
		// --

		
		txLogger.info(new UpdateEvent(getContent(), updatedParts));
		ServiceLocator.getService(EventService.class).fire(new AppUpdateEvent(getContent()));
	}
	
	@Transactional(propagation = Propagation.REQUIRED)
	public void updateFields(List<UpdatedField> updatedFields) throws ContentMgmtException {
		getContentDao().save(getContent());
		Map<EForm, List<UpdatedField>> updates = new HashMap<EForm, List<UpdatedField>>();
		for (UpdatedField update : updatedFields) {
			List<UpdatedField> formupdates = updates.get(update.getForm());
			if (formupdates==null) {
				formupdates = new ArrayList<UpdatedField>();
				updates.put(update.getForm(), formupdates);
			}
			formupdates.add(update);
		}
		for (EForm form : updates.keySet()) {
			txLogger.info(new UpdateFormEvent(getContent(), form, updates.get(form)));
		}
		ServiceLocator.getService(EventService.class).fire(new AppUpdateEvent(getContent()));
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void update(Object event) throws ContentMgmtException {
		getContentDao().save(getContent());
		txLogger.info((ContentEvent)event);
		
		// when the user reads a task we dont propagate the Spring listener
		//
		boolean require_update_user_lists=false;
		ServiceLocator.getService(EventService.class).fire(new AppUpdateEvent(getContent(), require_update_user_lists));
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void updateAcl(List<String> updatedParts) throws ContentMgmtException {
		getContentDao().save(getContent());
		ServiceLocator.getService(EventService.class).fire(new AppUpdateEvent(getContent()));
	}

	/** 
	 * 
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public Content checkout() throws ContentMgmtException {
		VersionService versionService = getContent().getService(VersionService.class);
		Content content = (Content)versionService.checkout();
		for (UserLabel label : getContent().getService(LabelsService.class).getUserLabels()) {
			content.getService(LabelsService.class).setLabel(label);
		}

		// FIXME --------------------------------------------------------------------
		// Pasar a Listener para actualizar los votos.
		// 
		if (getContent().getService(SocialService.class)!=null) {
			int votes = getContent().getService(SocialService.class).getVotes();
			PropertyService properties = content.getService(PropertyService.class);
			properties.setProperty("votes", votes);
		}
		
		
		//--------------------------------------------------------------------
//		for (ContentSubscription subscription : getContent().getService(ContentSubscriptionService.class).getSubscriptions()) {
//			content.getService(ContentSubscriptionService.class).subscribe(subscription.getPerson());
//		}
		
		// Log Spring
		txLogger.info(new CheckoutEvent(content));
		ServiceLocator.getService(EventService.class).fire(new AppCheckoutEvent(getContent()));
		return content;
	}
	
	@Override
	@SuppressWarnings("rawtypes")
	@Transactional(propagation = Propagation.REQUIRED)
	public Content clone() throws ContentMgmtException {
		Content clone = null;
		try {
			User user = ServiceLocator.getService(SecurityService.class).getSessionUser();
			clone = (Content)((Versionable)getContent()).clone();
			((Content)clone).setOId(ServiceLocator.getService(ContentFactoryService.class).getNewOId());
			((Content)clone).setCommentsEnabled(true);
			((Content)clone).setWorkspace((Long)user.getId());

			((Content)clone).setLastModifiedUser(user);
			((Content)clone).setLastModifiedOffsetDateTime(OffsetDateTime.now());
			
			((Versionable<?>)clone).setHeadVersion(false);

			getContentDao().save((Content)clone);
			
		}
		catch (Exception e) {
			logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName() + " | " + e.getClass().getName());
			throw new ContentMgmtException(e);
		}
		return clone;
	}


	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void dropCheckout() {
		VersionService versionService = getContent().getService(VersionService.class);
		
		PropertyService properties = getContent().getService(PropertyService.class);
		properties.removeProperty(PROPERTY_NOTE);
		properties.removeProperty(PROPERTY_SENDER);
		properties.removeProperty(PROPERTY_ASSIGNATION_TIME);
		properties.removeProperty(PROPERTY_WORKFLOW);
		
		versionService.dropCheckout();
		txLogger.info(new DropcheckoutEvent(getContent()));
		ServiceLocator.getService(EventService.class).fire(new AppCheckoutEvent(getContent()));
	}
	
	/**
	 * CHECK IN
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void checkin() throws ContentMgmtException {
			checkin(false);
		
	}
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void checkin(boolean is_silent) throws ContentMgmtException {
		
		PropertyService properties = getContent().getService(PropertyService.class);
		properties.removeProperty(PROPERTY_NOTE);
		properties.removeProperty(PROPERTY_SENDER);
		properties.removeProperty(PROPERTY_ASSIGNATION_TIME);
		properties.removeProperty(PROPERTY_WORKFLOW);
		properties.removeProperty(PROPERTY_DUEDATE_ALERTS);
		
		getContent().getService(ContentSubscriptionService.class).removeAll();
		
		VersionService versionService = getContent().getService(VersionService.class);		
		
		versionService.checkin();
		
		/** 
		 * Log Event Spring Event  
		 * */
		txLogger.info(new CheckinEvent(content, is_silent));
		ServiceLocator.getService(EventService.class).fire(new AppCheckinEvent(content, is_silent));
		
		try {
			SystemMetricsService se = ServiceLocator.getService(SystemMetricsService.class);
			if (getContent().getExternalId()!=null) {
				se.getMeterContentAPICheckin().mark();
				se.getMeterContentAPICheckin(getContent().getDomain().getId()).mark();
			}
			else  {
				se.getMeterContentCheckin().mark();
				se.getMeterContentCheckin(getContent().getDomain().getId()).mark();
			}
			
		} 
		catch (Exception e) {
			logger.error(e, (getSessionUser()!=null?getSessionUser().getUserName():"null"));
			throw new ContentMgmtException(e);
		}
	}


	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void unArchive() throws ContentMgmtException {
		
		if (getContent().getState().equals(ObjectState.ARCHIVED)) {
			
			getContent().setState(ObjectState.ENABLED);
			getContent().setLastModifiedUser(getUser());
			getContent().setLastModifiedOffsetDateTime(OffsetDateTime.now());
			
			try {
				
				getContentDao().save(getContent());
				UpdateEvent event = new UpdateEvent(content);
				event.setParameters("Unarchive");
		
				// Log Spring
				txLogger.info(event);
				ServiceLocator.getService(EventService.class).fire(new AppUpdateEvent(getContent()));
				
			} 
			catch (Exception e) {
				logger.error(e, (getSessionUser()!=null?getSessionUser().getUserName():"null"));

				throw new ContentMgmtException(e);
			}
		}
	}


	/**
	 * 
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void archive() throws ContentMgmtException {
		
		// pasar los resources al slow sub dir del file server
		//
		getContent().setState(ObjectState.ARCHIVED);
		getContent().setLastModifiedUser(getUser());
		getContent().setLastModifiedOffsetDateTime(OffsetDateTime.now());
		
		UpdateEvent event = new UpdateEvent(content);
		event.setParameters("Archive");
		
		try {
			getContentDao().save(getContent());
			
			// Log Spring
			txLogger.info(event);
			ServiceLocator.getService(EventService.class).fire(new AppUpdateEvent(getContent()));
		} 
		catch (Exception e) {
			logger.error(e, (getSessionUser()!=null?getSessionUser().getUserName():"null"));
			throw new ContentMgmtException(e);
		}
	}


	/**
	 * 
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void checkin(List<String> updatedParts) throws ContentMgmtException {
		checkin(updatedParts,false);
	}
	

	/**
	 * 
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void checkin(List<String> updatedParts, boolean silentMode) throws ContentMgmtException {
		getContentDao().save(getContent());
		
		getContent().getService(LabelsService.class).removeAll();
		
		PropertyService properties = getContent().getService(PropertyService.class);
		
		properties.removeProperty(PROPERTY_UNREAD);
		properties.removeProperty(PROPERTY_NOTE);
		properties.removeProperty(PROPERTY_SENDER);
		properties.removeProperty(PROPERTY_ASSIGNATION_TIME);
		properties.removeProperty(PROPERTY_ACTIVITY_RESOURCES);
		properties.removeProperty(PROPERTY_WORKFLOW);
		properties.removeProperty(PROPERTY_ALERTS);
		
		// Log Spring
		txLogger.info(new UpdateEvent(getContent(), updatedParts));
		ServiceLocator.getService(EventService.class).fire(new AppUpdateEvent(content));

		
		VersionService versionService = getContent().getService(VersionService.class);
		versionService.checkin();
		
		// Log Spring
		txLogger.info(new CheckinEvent(content, silentMode));
		ServiceLocator.getService(EventService.class).fire(new AppCheckinEvent(content, silentMode));

		
		try {
			SystemMetricsService se = ServiceLocator.getService(SystemMetricsService.class);
			if (getContent().getExternalId()!=null) {
				se.getMeterContentAPICheckin().mark();
				se.getMeterContentAPICheckin(getContent().getDomain().getId()).mark();
			}
			else {
				se.getMeterContentCheckin().mark();
				se.getMeterContentCheckin(getContent().getDomain().getId()).mark();
			}
			
		} catch (Exception e) {
			logger.error(e, (getSessionUser()!=null?getSessionUser().getUserName():"null"));
			throw new ContentMgmtException(e);
		}
	}
	
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void assign(User user, String note)   throws ContentMgmtException {
		User usender = getUser(); 
		getContent().setWorkspace((Long)user.getId());
		try {
			
			getContentDao().save(getContent());
			
			// Log Spring
			txLogger.info(new AssignationEvent(content, user, note));
			ServiceLocator.getService(EventService.class).fire(new AppAssignEvent(getContent()));

			
		}
		catch (Exception e) {
			logger.error(e, (getSessionUser()!=null?getSessionUser().getUserName():"null"));

			throw new ContentMgmtException(e);
		}
		
		WorkflowService workflowService = content.getService(WorkflowService.class);
		
		if (workflowService!=null && workflowService.getTask()!=null) {
			workflowService.getActivity().assign(user);
		}
		
		getContent().getService(LabelsService.class).setLabelForAssign();
		
		PropertyService properties = getContent().getService(PropertyService.class);
		if (note!=null) 
			properties.setProperty(PROPERTY_NOTE, note);
		else
			properties.removeProperty(PROPERTY_NOTE);
		properties.setProperty(PROPERTY_SENDER, usender.getFirstLastName());
		properties.setProperty(PROPERTY_ASSIGNATION_TIME, Long.valueOf( OffsetDateTime.now().toInstant().toEpochMilli()));
		
		properties.removeProperty(PROPERTY_ACTIVITY_RESOURCES);
		properties.removeProperty(PROPERTY_ALERTS);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void removeLabel(String label) throws ContentMgmtException {
		getContent().getService(LabelsService.class).removeLabel(label);
	}
	
	
	public boolean labeled(String label)  {
		return getContent().getService(LabelsService.class).labeled(label);
	}

	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void addExternalResource(ExternalResource resource) throws ContentMgmtException {
		
		getContentDao().save(resource);
		((ResourceContainer)getContent()).addResource(resource);
		getContentDao().save(getContent());
		
		PropertyService properties = getContent().getService(PropertyService.class);
		String activityResources = (String)properties.getProperty(PROPERTY_ACTIVITY_RESOURCES);
		activityResources = (activityResources == null) ? "" : activityResources+";";
		activityResources += resource.getId();
		properties.setProperty(PROPERTY_ACTIVITY_RESOURCES, activityResources);
		
		List<String> updatedParts = new ArrayList<String>();
		updatedParts.add("add "+ resource.getName());
		
		
		// Log Spring
		txLogger.info(new UpdateEvent(getContent(), updatedParts));
		ServiceLocator.getService(EventService.class).fire(new AppUpdateEvent(getContent()));

		
	}
	
	/**
	 * Add File creates an entry in the Log for the Content
	 */
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void addFile(KBFile file) throws ContentMgmtException {
		this.addFile(file, true);
	}

	@Override
	@Deprecated
	@Transactional(propagation = Propagation.REQUIRED)
	public void addFile(KBFile file, boolean ispublic) throws ContentMgmtException {
		try {
			addFileInternal(file, null, ispublic);
			List<String> updatedParts = new ArrayList<String>();
			updatedParts.add("add -> "+file.getName() + " ["+  (file.getId()!=null ? String.valueOf(file.getId()):"")+"]");
			txLogger.info(new UpdateAddResourceEvent(getContent(), file, updatedParts));
			ServiceLocator.getService(EventService.class).fire(new AppUpdateEvent(getContent()));
		}
	 	catch(RuntimeException e) {
			logger.error(e);
			throw e;
		}
	}
	
	@Override
	@Deprecated
	@Transactional(propagation = Propagation.REQUIRED)
	public void addFile(KBFile file, ResourceTag group, boolean ispublic) throws ContentMgmtException {
		try {
			addFileInternal(file, group, ispublic);
			List<String> updatedParts = new ArrayList<String>();
			updatedParts.add("add -> "+file.getName() + " ["+  (file.getId()!=null ? String.valueOf(file.getId()):"")+"]");
			txLogger.info(new UpdateAddResourceEvent(getContent(), file, updatedParts));
			ServiceLocator.getService(EventService.class).fire(new AppUpdateEvent(getContent()));
		}
	 	catch(RuntimeException e) {
			logger.error(e);
			throw e;
		}
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void replaceFile(KBFile file, KBFile version) throws ContentMgmtException {
		try {
			int fileversion = file.getVersion();
			if (fileversion==0) {
				((KBFileImpl)file).setVersion(1);
				fileversion = 1;
			}
			((KBFileImpl)version).setPreviousVersion(file);
			((KBFileImpl)version).setVersion(fileversion+1);
			((KBFileImpl)version).setOId(file.getOId());
			ResourceTag group = ((ResourceContainer)getContent()).getTag(file);
			((ResourceContainer)getContent()).removeFile(file);
			addFileInternal(version, group);
			List<String> updatedParts = new ArrayList<String>();
			updatedParts.add("replace -> "+file.getName() + " ["+  (file.getId()!=null ? String.valueOf(file.getId()):"")+"] by " + 
				version.getName() + " ["+  (version.getId()!=null ? String.valueOf(version.getId()):"")+"]");
			txLogger.info(new UpdateAddResourceEvent(getContent(), version, updatedParts));
			ServiceLocator.getService(EventService.class).fire(new AppUpdateEvent(getContent()));
		} 
		catch(RuntimeException e) {
			logger.error(e, (getSessionUser()!=null?getSessionUser().getUserName():"null"));
			throw e;
		}
	}
	
	@Transactional(propagation = Propagation.REQUIRED)
	public void restoreFile(KBFile file) throws ContentMgmtException {
		try {
			((ResourceContainer)getContent()).restoreFile(file);
			//getContentDao().save(file);
			List<String> updatedParts = new ArrayList<String>();
			updatedParts.add("restore -> "+file.getName() + " ["+  (file.getId()!=null ? String.valueOf(file.getId()):"")+"]");
			txLogger.info(new UpdateAddResourceEvent(getContent(), file, updatedParts));
			ServiceLocator.getService(EventService.class).fire(new AppUpdateEvent(getContent()));
		} 
		catch(RuntimeException e) {
			logger.error(e, (getSessionUser()!=null?getSessionUser().getUserName():"null"));
			throw e;
		}
	}
	
	
	@Transactional(propagation = Propagation.REQUIRED)
	public void sign(EFormData data, String snapshot, UserSignature signature, UserDevice device) throws SignatureException {
		try {
			if (signature==null) return;
			
			EForm eform = data.getForm();
			EFormData contentdata = getContent().getKbeeData(eform);
			
			KbeeSignedData signed = new KbeeSignedData();
			
			signed.setSignature(signature);
			signed.setDate(OffsetDateTime.now());
			signed.setSnapshot(snapshot);
			signed.setDevice(device);
			
			for (Resource resource : ((ResourceContainer)getContent()).getResources()) {
				signed.addResource(resource);
			}
			
			String signeddata = ServiceLocator.getService(SystemSignatureService.class).sign(signed.getDigest(), signature.getPrivateKey());
			signed.setSignedData(signeddata);
			
			contentdata.setSignature(signed);
			data.setSignature(signed);
			getContentDao().save(signed);
			
			for (EFormField<?> field : data.getForm().getFields()) {
				if (field instanceof EResourceField) {
					Object resourceobject = data.getData(field);
					if (resourceobject instanceof KBFile) {
						KBFile signedfile = ((KBFile)resourceobject).getService(FileService.class).sign(signature, null);
						replaceFile((KBFile)resourceobject, signedfile);
					}
				}
			}
			
			txLogger.info(new SignEvent(getContent(), data));
		}
		catch (IOException e) {
			throw new SignatureException(e);
		}
	}
	
	@Transactional(propagation = Propagation.REQUIRED)
	public void sign(EFormData data, String snapshot, UserSignature signature, String stream) throws SignatureException {
		sign(data, snapshot, signature, null, stream);
	}
	
	@Transactional(propagation = Propagation.REQUIRED)
	public void sign(EFormData data, String snapshot, UserSignature signature, UserDevice device, String signatureStream) throws SignatureException {
		try {
			if (signature==null) return;
			
			EForm eform = data.getForm();
			EFormData contentdata = getContent().getKbeeData(eform);
			
			KbeeSignedData signed = new KbeeSignedData();
			
			signed.setSignature(signature);
			signed.setDevice(device);
			signed.setDate(OffsetDateTime.now());
			signed.setSnapshot(snapshot);
			
			for (Resource resource : ((ResourceContainer)getContent()).getResources()) {
				signed.addResource(resource);
			}
			
			String signeddata = ServiceLocator.getService(SystemSignatureService.class).sign(signed.getDigest(), signature.getPrivateKey());
			signed.setSignedData(signeddata);
			
			contentdata.setSignature(signed);
			data.setSignature(signed);
			getContentDao().save(signed);
			
			for (EFormField<?> field : data.getForm().getFields()) {
				if (field instanceof EResourceField) {
					Object resourceobject = data.getData(field);
					if (resourceobject instanceof KBFile) {
						KBFile signedfile = ((KBFile)resourceobject).getService(FileService.class).sign(signature, signatureStream);
						replaceFile((KBFile)resourceobject, signedfile);
					}
				}
			}
			
			txLogger.info(new SignEvent(getContent(), data));
		}
		catch (IOException e) {
			throw new SignatureException(e);
		}
	}  
	
	@Override
	public void sign(EFormData data, String snapshot, String signed, UserSignature signature, UserDevice device, String signatureStream) throws SignatureException {
		try {
			if (signature==null) {
				throw new SignatureException("No Signature");
			}
			
			if (!ServiceLocator.getService(SystemSignatureService.class).verify(snapshot, signed, signature.getCertificate())) {
				throw new SignatureException("Invalid Signature");
			}

			EForm eform = data.getForm();
			EFormData contentdata = getContent().getKbeeData(eform);
			
			KbeeSignedData signedData = new KbeeSignedData();
			
			signedData.setSignature(signature);
			signedData.setDevice(device);
			signedData.setDate(OffsetDateTime.now());
			signedData.setSnapshot(snapshot);
			signedData.setSignedData(signed);
			//for (Resource resource : ((ResourceContainer)getContent()).getResources()) {
			//	signed.addResource(resource);
			//}
			
			//String signeddata = ServiceLocator.getService(SystemSignatureService.class).sign(signed.getDigest(), signature.getPrivateKey());
			//signedData.setSignedData(signedData);
			
			contentdata.setSignature(signedData);
			data.setSignature(signedData);
			getContentDao().save(signedData);
			
//			for (EFormField<?> field : data.getForm().getFields()) {
//				if (field instanceof EResourceField) {
//					Object resourceobject = data.getData(field);
//					if (resourceobject instanceof KBFile) {
//						KBFile signedfile = ((KBFile)resourceobject).getService(FileService.class).sign(signature, signatureStream);
//						replaceFile((KBFile)resourceobject, signedfile);
//					}
//				}
//			}
			
			txLogger.info(new SignEvent(getContent(), data));
		}
		catch (Exception e) {
			throw new SignatureException(e);
		}
	}

	
	@Transactional(propagation = Propagation.REQUIRED)
	public void unsign(EFormData data) {
		getContent().unsign(data);
		for (EFormField<?> field : data.getForm().getFields()) {
			if (field instanceof EResourceField) {
				Object resourceobject = data.getData(field);
				if (resourceobject instanceof KBFile && ((KBFile)resourceobject).isSigned()) {
					Resource unsigned = ((KBFile)resourceobject).getPreviousVersion();
					KBFile version = (KBFile)getContentDao().unproxy(unsigned);
					restoreFile(version );
				}
			}
		}
	}
	
	@Override
	public List<String> getAlerts() {
		List<String> alerts = new ArrayList<String>();
		String values = getProperty(PROPERTY_ALERTS);
		if (values!=null) {
			StringTokenizer tokens = new StringTokenizer(values,";");
			while (tokens.hasMoreTokens()) {
				String alert = tokens.nextToken();
				alerts.add(alert.trim());
			}
		}
		return alerts;
	}
	
	@Override
	public void setAlert(String alert) {
		if (!getAlerts().contains(alert)) {
			String values = getProperty(PROPERTY_ALERTS);
			values = values==null || "".equals(values.trim()) ? alert : values + ", "+alert;  
			PropertyService properties = getContent().getService(PropertyService.class);
			properties.setProperty(PROPERTY_ALERTS, values);
		}
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void deleteAllVersions() throws ContentMgmtException {
		deleteAllVersions("Delete All Versions");
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void deleteAllVersions(String updatePart) throws ContentMgmtException {
	
		RemoveEvent event = new RemoveEvent(getContent());
		event.setParameters(updatePart);
		getContentDao().deleteAllVersions(getContent());
		
		// Log Spring
		txLogger.info(event);
		ServiceLocator.getService(EventService.class).fire(new AppDeleteEvent(getContent()));
	}

	
	@Override
	@SuppressWarnings("unchecked")
	public Content getValidVersion() {
		if (isValid() || isWriteable()) {
			return getContent();
		}
		else {
			if (getContent() instanceof Versionable) {
				Content version = ((Versionable<Content>)getContent()).getPreviousVersion();
				while (version!=null) {
					if (isValid(version)) {
						return version;
					}
					else {
						version = ((Versionable<Content>)version).getPreviousVersion();
					}	
				}
			}
		}
		return null;
	}
	
	public boolean isValid() {
		return isValid(getContent());
	}
	
	public boolean isValidVersion() {
		return isValid(getContent());
	}

	
	/**
	 * 
	 *  Directorio de trabajo
	 * 
	 * @param file
	 * @throws IOException
	 */
	public User getUser() {
		User user = null;
		Long workspace = ((KbeeContent)getContent()).getWorkspace();
		if (workspace!=null) {
			user = ServiceLocator.getService(SecurityService.class).findUserById(workspace);
		}
		return user;
	}

	public String getNote() {
		return getProperty(PROPERTY_NOTE);
	}

	public String getSender() {
		return getProperty(PROPERTY_SENDER);
	}
	
	public OffsetDateTime getAssignationTime() {
		Long time =  (Long) getContent().getService(PropertyService.class).getProperty(PROPERTY_ASSIGNATION_TIME);
		if (time==null) 
			return null;
		Instant instant = Instant.ofEpochMilli(time);
		OffsetDateTime date = OffsetDateTime.ofInstant(instant, ZoneId.systemDefault());
		return date;
	}

	public List<String> getActivityResources() {
		List<String> resources = new ArrayList<String>();
		String values = getProperty(PROPERTY_ACTIVITY_RESOURCES);
		if (values!=null) {
			StringTokenizer tokens = new StringTokenizer(values,";");
			while (tokens.hasMoreTokens()) {
				String resourceid = tokens.nextToken();
				resources.add(resourceid);
			}
		}
		return resources;
	}
	

	@Override
	public String getPortalSubtitle() {
		return getStringFromRule(getContent().getContentTemplate().getPortalsSubtitleRule());		
	}
	
	@Override
	public String getConsoleSubtitle() {
		return getStringFromRule(getContent().getContentTemplate().getConsoleSubtitleRule());
	}

	
	@Override
	public String getConsoleSubtitleDefaultIfNull() {
		String s=getStringFromRule(getContent().getContentTemplate().getConsoleSubtitleRule());
		if (s!=null && s.length()>0)
			return s;
		
		StringBuilder str = new StringBuilder();
		try {
		
			if (getContent().getWorkspace()!=null && getContent().getService(WorkflowService.class).getActivity()!=null) {
				String task=getContent().getService(WorkflowService.class).getActivity().getTask().getDisplayName();
				str.append(task);
				if (task!=null)
					str.append(" - ");
			}
		
			String ta=getContent().getContentTypeClassificationAsString();
			if (ta!=null &&  ta.length()>0) {
					str.append(ta);
					str.append(", ");
			}
			String st=getContent().getWorkflowStatusClassificationAsString();
			str.append(st);
			
			} catch (Exception e) {
				logger.error(e);
				str.append(e.getClass().getCanonicalName());
				
			}
		
			return str.toString();
	}
	
	/**
	 * 
	 * - Applies Rule
	 * - if rule returns null -> metadata as string
	 * - if metadata as string returns null -> "type" [taken from classifiers semantically defined as "content type"]
	 *  
	 */
	public String getStringFromRule(String rule) {
		try {
			
			if (rule==null) 
				return "";
			
			KbeeTextTemplate template = new KbeeTextTemplate(rule);
			String s = template.process(getContent());
			
			if (s!=null && s.length()>0)
				return s;
			
			s = getContent().getContentTypeClassificationAsString();

			if (s!=null && s.length()>0)
				return s;
			
			return getContent().getMetadataAsString(); 
		
		} catch (Exception e) {
			logger.error(e);
			return e.getClass().getSimpleName()+" - " + e.getMessage();
		}
	}
	
	
	public String getSummary() {
		int index = 0;
		StringBuffer summary = new StringBuffer();
		for (Classifier classifier : getCanonicalClassifiers()) {
			Classification classification = getClassification(classifier);
			if (classification!=null && classification.getDataSetMember()!=null) {
				if (index>0)
					summary.append(" · ");
				if (classifier.getDataSetType().equals(DataSetType.DATE)) {
					if (classification.getDataSetMember().getDateValue()!=null) {
						//DateTimeFormatter dt = DateTimeFormatter.ofPattern("MM/dd/yy");
						//String label =dt.format(classification.getDataSetMember().getDateValue());
						String zid = ServiceLocator.getService(DateTimeService.class).getMapZoneIds().get(getSessionUser().getTimeZone());
						if (zid==null) 
								zid=ZoneId.systemDefault().getId();
						String label = ServiceLocator.getService(DateTimeService.class).format(classification.getDataSetMember().getDateValue(), zid, getSessionUser().getLocale(), DateTimeService.Month_Day_Year);
						summary.append(label);
					}
				}
				else {
					summary.append(classification.getDataSetMember().getDisplayName());
				}
				index++;
			}
		}
		for (AttributeTemplate template : getCanonicalAttributes()){
			List<String> values = getValues(template.getAttribute());
			if (!values.isEmpty()) {
				if (index>0)
					summary.append(" · ");
				summary.append(format(template, values.get(0)));
				index++;
			}	
		}
		return summary.toString();
	}
	
	public List<FileSnippet> getSnippets(String textquery) {
		return this.getSnippets(textquery, false);
	}
	
	public List<FileSnippet> getSnippets(String textquery, boolean portal) {
	
		if (textquery==null)
			return null;
		
		List<FileSnippet> snippets = new ArrayList<FileSnippet>();
		
		StringBuilder statement = new StringBuilder("(" + textquery + ")");
		int r = 0;
//		Boolean privateEnabled = null;
		StringBuilder filesstatement = new StringBuilder();
		for (Resource resource : ((ResourceContainer)getContent()).getResources()) {
			boolean enabled = true;
//			if (!((ResourceContainer)getContent()).isPublic(resource)) {
//				if (privateEnabled == null) {
//					privateEnabled = ServiceLocator.getService(ContentSystemSecurityService.class).isPrivateEnabled(getContent());
//				}
//				enabled = privateEnabled;
//			}
			if (enabled && portal) {
				enabled = resource.isInPortalVersion();
			}
			if (enabled) {
				if (r++>0) filesstatement.append(" OR ");
				filesstatement.append("id:kb*file*#" + String.valueOf(resource.getId()));
			}
		}
		
		if (filesstatement.length()>0) {
			statement.append(" AND (");
			statement.append(filesstatement);
			statement.append(")");
		}
		else {
			return snippets;
		}
		statement.append(" AND type:kbfile");
		
		 
		TextQuery query = new TextQuery(statement.toString());
		
		query.setHighlight(true);

		query.setHighlightMaxChars(100000);
		query.setDefaultField("text");
		
		QueryResponse response = (QueryResponse)getIndex().execute(query);
		SolrDocumentList results = response.getResults();
		for (SolrDocument file : results) {
			Object documentId = file.getFieldValue("id");
			Map<String, List<String>> snippetsmap = response.getHighlighting().get(documentId);
			if (snippetsmap!=null) {
				for (List<String> filesnippets : snippetsmap.values()) {
					//List<String> values = new ArrayList<String>();
					String snippet = "";
					Collections.sort(filesnippets, new Comparator<String>() {
						@Override
						public int compare(String a, String b) {
							int na = StringUtils.countMatches(a, "<em>");
							int nb = StringUtils.countMatches(b, "<em>");
							return nb-na;
						}
					});
					int s = 0;
					for (String pa : filesnippets) {
						if (pa.length()>1000) pa = pa.substring(0,1000);
						if (pa.contains("</em>")) {
						snippet += "<p>"+pa + "</p>";
						if (s++>1) break;
						}
					}
					if (!"".equals(snippet)) {
						snippets.add(new FileSnippet(documentId.toString(), snippet));
					}
					
				}
			}
		}
		return snippets;
	}
	
	@Transactional
	public com.novamens.workflow.Process startProcess(ProcessLauncher launcher, 
			Object initialData, 
			List<ResourceNode> resources, 
			User collaborator, 
			String note, 
			ResourceTag doneTag, 
			ResourceTag targetTag) {
		ContentTemplate template = launcher.getContentTemplate();
		Content newcontent = ServiceLocator.getService(ContentFactoryService.class).create(template.getName());
		
		for (Classification classification : getContent().getClassification()) {
			for (ModelElementTemplate modelTemplate : template.getStructure()) {
				if (classification.getClassifier().equals(modelTemplate.getElement())) {
					newcontent.setClassification(classification.getClassifier(), classification.getDataSetMember());
				}
			}
		}
		
		for (AttributeTemplate modelTemplate : template.getAttributes()) {
			List<String> values =  getContent().getAttributeValues(modelTemplate.getAttribute());
			if (!values.isEmpty()) {
				newcontent.setAttributeValues(modelTemplate.getAttribute(), values);
			}
		}
		
		
		List<ResourceFolder> folders = new ArrayList<>();
		while (folders!=null) {
			List<ResourceFolder> childs = new ArrayList<>();
			for (ResourceNode node : resources) {
				if (node.getResource() instanceof ResourceFolder) {
					ResourceFolder nodefolder = node.getFolder();
					if (nodefolder!=null) {
						boolean found = false;
						for (ResourceNode nodeparent : resources) {
							if (nodeparent.getResource().equals(nodefolder)) {
								found = true;
								break;
							}
						}
						if (!found) nodefolder = null;
					}
					if ((folders.isEmpty() && nodefolder==null) || 
						(nodefolder!=null && folders.contains(nodefolder))) {
						((ResourceContainer)newcontent).addResource(node.getResource(), nodefolder, targetTag);
						childs.add((ResourceFolder)node.getResource());
						((ResourceContainer)getContent()).setTag(node.getResource(), doneTag);
					}
				}
			}
			folders = childs.isEmpty() ? null : childs;
		}
		
		
		for (ResourceNode node : resources) {
			if (!(node.getResource() instanceof ResourceFolder)) {
				ResourceFolder nodefolder = node.getFolder();
				if (nodefolder!=null) {
					boolean found = false;
					for (ResourceNode nodeparent : resources) {
						if (nodeparent.getResource().equals(nodefolder)) {
							found = true;
							break;
						}
					}
					if (!found) nodefolder = null;
				}
				((ResourceContainer)newcontent).addResource(node.getResource(), nodefolder, targetTag);
				((ResourceContainer)getContent()).setTag(node.getResource(), doneTag);
				((ResourceContainer)getContent()).setFolder(node.getResource(), nodefolder);
			}
		}
		
		newcontent.getService(ContentService.class).update();
		
		com.novamens.workflow.Process process = newcontent.getService(WorkflowService.class).startProcess(launcher, initialData, note, collaborator);
		
		return process;
	}
	
	public WorkflowContext getWorkflow() {
		if (!getContent().isLocked())
			return null;
		Content wv = getContentDao().findLastVersion(getContent().getOId());
		if (getContent().equals(wv)) {
			return null;
		}
		WorkflowContext wc = wv.getService(WorkflowService.class).getContext();
		return wc;
	}
	
	public Text getText() {
		String text = null;
		for (AttributeTemplate template : content.getContentTemplate().getAttributes()) {
			if (AttributeType.HTML.equals(template.getAttribute().getType())) {
				List<String> texts = content.getAttributeValues(template.getAttribute());
				if (!texts.isEmpty()) {
					text = texts.get(0);
					break;
				}
			}
		}
		if (text==null) return null;
		KbeeText ktext = new KbeeText(text);
		return ktext;
	}
	
	@SuppressWarnings("unchecked")
	public List<TextChange> getTextChanges() {
		KbeeText text = (KbeeText)getText();
		if (text==null) return null;
		
		Versionable<Content> versionable = (Versionable<Content>)getContent();
		Content version = versionable.getPreviousVersion();
		if (version==null) return null;
		
		KbeeText previoustext = (KbeeText)version.getService(ContentService.class).getText();
		if (previoustext==null) return null;
		
		List<TextChange> changes = text.getChanges(previoustext);
		
		return changes;
	}

	
	@Override
	public List<ContentLink> getLinks() {
		List<ContentLink> links = new ArrayList<>();
		KbeeText ktext = (KbeeText)getText();
		if (ktext == null) return links;
		ktext.getText(new AncordResolver() {
			@Override
			public Element resolve(Element ancord) {
				try {
					String href = ancord.getAttribute("href");
					String id, anchor = null;
					int a = href.indexOf("#");
					if (a>0) {
						id = href.substring(0, a);
						int p = href.indexOf("?", a);
						if (p>0) {
							anchor = href.substring(a, p);
						}
						else {
							anchor = href.substring(a+1);
						}
					}
					else {
						int p = href.indexOf("?");
						if (p>0) {
							id = href.substring(0, p);
						}
						else {
							id = href;
						}
					}
					Content target = getContentDao().findContentByOId(Long.valueOf(id));
					if (target!=null) {
						KbeeContentLink link = new KbeeContentLink();
						link.setTarget(target);
						link.setAnchor(anchor);
						link.setLastModifiedDate(OffsetDateTime.now());
						link.setLastModifiedUser(getSessionUser());
						links.add(link);
					}
				}
				catch(Exception e) {
					logger.error(e);
				}
				
				return ancord;
			}
		}, new ImageResolver() {
			@Override
			public Element resolve(Element image) {
				return image;
			}
		});
		return links;
	
	}
	
	@Override
	public void reindex() {
		JavaIndex index = getIndex(getContent());
		index.index(getContent(), true, true, true);		
	}
	
	public String getProperty(String propertyName) {
		return (String)getContent().getService(PropertyService.class).getProperty(propertyName);
	}

	public Acl getAcl() {
		return null;
	}
	
	public Content getContent() {
		return content;
	}
	
	public ContentDao getContentDao()	{
		return contentDao;
	}
	
	public void setContentDao(ContentDao dao) 	{
		contentDao=dao;
	}
	
	public boolean isWriteable() {
		return ServiceLocator.getService(ContentSystemSecurityService.class).isWriteable(getContent());
	}
	
	protected JavaIndex getIndex() {
		return (JavaIndex)getContent().getDomain().getService(FileIndexerService.class).getIndex();
	}
	
	protected ProcessLauncher getContextLauncher() {
		List<ProcessLauncher> launchers = getContent().getDomain().getService(WorkflowDomainService.class).getContextLaunchers(getContent());
		if (launchers.isEmpty()) return null;
		return launchers.get(0);
	}
	
	protected User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	
	protected List<Classifier> getCanonicalClassifiers() {
		List<Classifier> classifiers = new ArrayList<Classifier>();
		for (ClassifierTemplate template : getContent().getContentTemplate().getClassifiers()) {
			if (template.isMetadataSubtitle()) {
				classifiers.add(template.getClassifier());
			}
		}
		return classifiers;
	}
	
	protected List<AttributeTemplate> getCanonicalAttributes() {
		List<AttributeTemplate> attributes = new ArrayList<AttributeTemplate>();
		for (AttributeTemplate template : getContent().getContentTemplate().getAttributes()) {
			if (template.isMetadataSubtitle()) {
				attributes.add(template);
			}
		}
		return attributes;
	}
	
	protected Classification getClassification(Classifier classifer) {
		for (Classification classification : getContent().getClassification()) {
			if (classification.getClassifier().equals(classifer)) {
				return classification;
			}
		}
		return null;
	}
	
	protected List<String> getValues(Attribute attribute) {
		List<String> values = getContent().getAttributeValues(attribute);
		return values;
	}
	
	private void addFileInternal(KBFile newfile, ResourceTag group) throws ContentMgmtException {
		checkTitle(newfile);
		getContentDao().save(newfile);
		((ResourceContainer)getContent()).addFile(newfile, group);
		PropertyService properties = getContent().getService(PropertyService.class);
		String activityResources = (String)properties.getProperty(PROPERTY_ACTIVITY_RESOURCES);
		activityResources = (activityResources == null) ? "" : activityResources+";";
		activityResources += newfile.getId();
		properties.setProperty(PROPERTY_ACTIVITY_RESOURCES, activityResources);
	}
	
	@Deprecated
	private void addFileInternal(KBFile newfile, ResourceTag group, boolean ispublic) throws ContentMgmtException {
		checkTitle(newfile);
		getContentDao().save(newfile);
		((ResourceContainer)getContent()).addFile(newfile, group, ispublic);
		PropertyService properties = getContent().getService(PropertyService.class);
		String activityResources = (String)properties.getProperty(PROPERTY_ACTIVITY_RESOURCES);
		activityResources = (activityResources == null) ? "" : activityResources+";";
		activityResources += newfile.getId();
		properties.setProperty(PROPERTY_ACTIVITY_RESOURCES, activityResources);
	}
	
	private void checkTitle(KBFile newfile) {
		boolean title_changed = false;
		
		/**
		 * if the file title exists we add ( ... )
		 */
		String filetitle = newfile.getTitle();
		String index_str = null;
		
		if (filetitle!=null) {
			int n = 1;
			String basename = null;
			for (KBFile file : ((ResourceContainer)getContent()).getFiles() ) {
				if (file.getTitle()!=null) {
					if (file.getTitle().equals(filetitle)) {
						if (basename==null)
							basename = file.getTitle();
						index_str =  String.valueOf(n++);
						filetitle = basename + " " + index_str;
						title_changed = true;
					}
				}
			}
		}

		/**
		 * now the file name
		 */
		if (title_changed) {
			
			newfile.setTitle(filetitle);
			
			if (newfile instanceof KBFileImpl) {
				
				String name=((KBFileImpl)newfile).getName();
				String segs[]=name.split("\\.");
				
				String new_name=segs[0]+"_"+index_str;
				
				StringBuilder str = new StringBuilder();
				str.append(new_name);
				
				if (segs.length>1) {
					for (int n=1; n<segs.length; n++)
							str.append("."+segs[n]);
				}
				
				logger.debug(str.toString());
				((KBFileImpl)newfile).setName(str.toString());
			}	
		}
	}
	
//	@SuppressWarnings("unchecked")
	private boolean isValid(Content content) {
		String validFrom = getValidFrom(content);
		String validTo = getValidTo(content);
		
		String now = getValidity(OffsetDateTime.now());
		
		logger.debug("validity "+now+ " "+validFrom+ " "+validTo);
				
		if ((validFrom==null || validFrom.compareTo(now)<=0) &&
			(validTo==null || validTo.compareTo(now)>=0)) {
			if (!validity(content)) return false;
			logger.debug("validity true");
			return true;
		}
		logger.debug("validity false");
		return false;
	}

	
	private boolean validity(Content content) {
		for (AttributeTemplate template : content.getContentTemplate().getAttributes()) {
			if (template.getAttribute().getType().equals(AttributeType.VALIDITY_FROM) || template.getAttribute().getType().equals(AttributeType.VALIDITY_TO)) {
				return true;
			}
		}
		return false;
	}
	
	private String getValidFrom(Content version) {
		String validity = null;
		for (AttributeTemplate template : version.getContentTemplate().getAttributes()) {
			if (template.getAttribute().getType().equals(AttributeType.VALIDITY_FROM)) {
				validity = getValidity(version, template.getAttribute());
				break;
			}
		}
		return validity;
	}
	
	private String getValidTo(Content version) {
		String validity = null;
		for (AttributeTemplate template : version.getContentTemplate().getAttributes()) {
			if (template.getAttribute().getType().equals(AttributeType.VALIDITY_TO)) {
				validity = getValidity(version, template.getAttribute());
				break;
			}
		}
		return validity;
	}
	
	private String getValidity(Content version, Attribute attribute) {
		String validity = null;
		List<String> attributevalues = version.getAttributeValues(attribute);
		if (attributevalues.size()==1) {
			String value = attributevalues.get(0);
			try {
				OffsetDateTime time = OffsetDateTime.parse(value);
				validity = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(time);
			}
			catch (Exception e) {
			}
		}
		return validity;
	}
	
	private String getValidity(OffsetDateTime time) {
		String validity = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(time);
		return validity;
	}
	
	private String format(AttributeTemplate template, String value) {
		if (template.getAttribute().getType().equals(AttributeType.DATE)) {
			OffsetDateTime odate = ServiceLocator.getService(DateTimeService.class).parseStrDate(value);
			value = ServiceLocator.getService(DateTimeService.class).getDateDisplayString(odate, getSessionUser().getLocale());
			return value;
		}
		else {
			return value;
		}
	}

	private JavaIndex getIndex(Object object) {
		return (JavaIndex) getContent().getDomain().getService(JavaIndexerService.class).getIndex();
	}
}
