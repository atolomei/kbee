package com.novamens.kbee.content.service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import com.novamens.content.base.Source;
import com.novamens.logging.*;
import com.novamens.scheduler.SchedulerException;
import com.novamens.scheduler.SchedulerService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.novamens.content.base.ConstraintException;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.email.EmailTemplate;
import com.novamens.content.library.Library;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.ModelObject;
import com.novamens.content.model.PersonMember;
import com.novamens.content.model.RelationTemplate;
import com.novamens.content.rule.ActionRule;
import com.novamens.content.service.DOMObjectService;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.dom.DomainObject;
import com.novamens.dom.ObjectState;
import com.novamens.event.AppDeleteEvent;
import com.novamens.event.AppUpdateEvent;
import com.novamens.event.EventService;
import com.novamens.event.LogEvent;
import com.novamens.kbee.command.CommandWrapperServiceRequest;
import com.novamens.kbee.content.command.RemoveDataSetMemberCommand;
import com.novamens.kbee.content.command.RemoveInformationModelObjectCommand;
import com.novamens.kbee.content.event.AppModelUpdateEvent;
import com.novamens.kbee.content.rule.KbeeActionRule;
import com.novamens.kbee.dom.KbeeModelObject;
import com.novamens.kbee.security.KbeeUser;

import com.novamens.security.User;
import com.novamens.security.acl.Group;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

import kbee.content.support.SupportTicket;

/**
 * <p>
 *  Service for the management of  Object CRUD
 *  Note: <@link Content} and its subclasses use other services </p>
 * 
 * <p> This Service
 * 
 * 		Values
 * 		------
 * 		{@link DataSetMember}
 * 
 * 		Model
 * 		-----
 * 		{@link Classifier}
 * 		{@link DataSet}
 * 		{@link Attribute}
 * 		{@link ContentTemplate}
 * 
 * 		Log
 *      ---
 *      {@link LogEvent}
 * 
 * 
 * 		Support Ticket
 *      --------------
 * 
 * Other Services
 * --------------
 * 
 * 		{@link User} :  {@link SecurityService}
 * 		{@link Group}:  {@link SecurityService}
 * 		{@link WorkflowRule} :  {@link SecurityService}
 * 
 * 		{@Userlabel}  {@link UserLabelsService} Factory 
 * 					  {@link LabelsService} Content Service, to apply and remove labels to Content.
 * 
 * 		{@link TreeFile}: {@link TreeFileService}
 * 
 *</p>  
 */
public class KbeeObjectService implements DOMObjectService {
				
	private com.novamens.dom.Object object = null;

	static kbee.util.logging.Logger logger = new kbee.util.logging.Logger(LogManager.getLogger(KbeeObjectService.class.getName()));
	
	// Logger synchronous with the TRX	*/
	static private Logger txlogger = LogManager.getLogger("TxLogger");
	


	public KbeeObjectService() {
	}
	
	public KbeeObjectService(com.novamens.dom.Object object) {
		 this.object = object;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void markAsDeleted()  throws ContentMgmtException {

		getObject().setState(ObjectState.DELETED);
		
		if (getSessionUser()!=null) 
			getObject().setLastModifiedUser(getSessionUser());
		
		getObject().setLastModifiedOffsetDateTime(OffsetDateTime.now());
		
		try {
		
			getContentDao().save(getObject());
		
			
			if (getObject() instanceof DataSetMember) {
				
				
				if (((DataSetMember) getObject()).getStrValue()==null)
					((DataSetMember) getObject()).setStrValue(((DataSetMember) getObject()).getId().toString().replace( _DELETED_ , "")+_DELETED_);
				else
					((DataSetMember) getObject()).setStrValue(((DataSetMember) getObject()).getStrValue().replace( _DELETED_ , "")+_DELETED_);
				
				txlogger.info(new DataSetValueDeleteEvent((DataSetMember) getObject(), "mark as Deleted"));
				ServiceLocator.getService(EventService.class).fire(new AppDeleteEvent(getObject()));
			}
			else if (getObject() instanceof ContentTemplate) {
				
				if (((ContentTemplate) getObject()).getName()==null)
					((ContentTemplate) getObject()).setName(((ContentTemplate) getObject()).getId().toString().replace( _DELETED_ , "")+_DELETED_);
				else
					((ContentTemplate) getObject()).setName(((ContentTemplate) getObject()).getName().replace( _DELETED_ , "")+_DELETED_);
				
				ServiceLocator.getService(EventService.class).fire(new AppModelUpdateEvent(((ContentTemplate) getObject()).getDomain()));
				txlogger.info(new ModelDeleteEvent((ContentTemplate) getObject(), "mark as Deleted"));
				ServiceLocator.getService(EventService.class).fire(new AppDeleteEvent(getObject()));
			}
			else if (getObject() instanceof DataSet) {

				if (((DataSet) getObject()).getName()==null)
					((DataSet) getObject()).setName(((DataSet) getObject()).getId().toString().replace( _DELETED_ , "")+_DELETED_);
				else
				(( DataSet) getObject()).setName((( DataSet) getObject()).getName().replace( _DELETED_ , "")+_DELETED_);
				
				ServiceLocator.getService(EventService.class).fire(new AppModelUpdateEvent(((DataSet) getObject()).getDomain()));
				txlogger.info(new ModelDeleteEvent((DataSet) getObject(), "mark as Deleted"));
				//ServiceLocator.getService(EventService.class).fire(new AppDeleteEvent(getObject()));
			}
			else if (getObject() instanceof Classifier) {

				if (((Classifier) getObject()).getName()==null)
					((Classifier) getObject()).setName(((Classifier) getObject()).getId().toString().replace( _DELETED_ , "")+_DELETED_);
				else
					((Classifier) getObject()).setName(((Classifier) getObject()).getName().replace( _DELETED_ , "")+_DELETED_);
				
				ServiceLocator.getService(EventService.class).fire(new AppModelUpdateEvent(((Classifier)getObject()).getDomain()));
				txlogger.info(new ModelDeleteEvent((Classifier) getObject(), "mark as Deleted"));
				ServiceLocator.getService(EventService.class).fire(new AppDeleteEvent(getObject()));
			}
			else if (getObject() instanceof Attribute) {
				
				if (((Attribute) getObject()).getName()==null)
					((Attribute) getObject()).setName(((Attribute) getObject()).getId().toString().replace( _DELETED_ , "")+_DELETED_);
				else
					((Attribute) getObject()).setName(((Attribute) getObject()).getName().replace( _DELETED_ , "")+_DELETED_);
				
				ServiceLocator.getService(EventService.class).fire(new AppModelUpdateEvent(((Attribute)getObject()).getDomain()));
				txlogger.info(new ModelDeleteEvent((Attribute) getObject(), "mark as Deleted"));
				ServiceLocator.getService(EventService.class).fire(new AppDeleteEvent(getObject()));
			}
			else {
				logger.error( "Class not considered: " + getObject().getClass().getName());
			}
			
		} 
		catch (Exception e) {
			logger.error(e);
			throw new ContentMgmtException(e);
		}
	}

	
	
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void update()  throws ContentMgmtException {
		update("update");
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void update(String part)  throws ContentMgmtException {
		List<String> s= new ArrayList<String>();
		s.add(part);
		update(s);
	}

	
	private String makeAlias(String name) {
		if (name == null)
			return null;
		String s=name.toLowerCase().replaceAll("[°,¡!?¿:\\/\"-().\\s]", "")
				.replace("á", "a")
				.replace("é", "e")
				.replace("í", "i")
				.replace("ó", "o")
				.replace("ú", "o")
				.replace("ñ", "n")
				.trim();
		return s;
	}


	
	/**
	 * Send email to submitter 
	 * */
	//@Transactional(propagation = Propagation.REQUIRED)
	//private void internal_update_support_ticket() {
		// SupportTicket ticket = (SupportTicket) getObject();
		// EmailBuilder email = new EmailBuilderSupportTicket(ticket);
		// ServiceLocator.getService(EmailService.class).send(email);
	//}

		
	@Transactional(propagation = Propagation.REQUIRED)
	private void internal_update(List<String> parts) {

		if (getSessionUser()!=null) 
			getObject().setLastModifiedUser(getSessionUser());
		
		if (getObject()!=null)
			getObject().setLastModifiedOffsetDateTime(OffsetDateTime.now());

		if (getObject() instanceof DomainObject && ((DomainObject) getObject()).getDomain()==null) 
			((DomainObject) getObject()).setDomain(getDomain());
			
		getObject().setLastModifiedOffsetDateTime(OffsetDateTime.now());
		
		try {
		
			 if (getObject() instanceof KbeeModelObject) {
				 if  (((KbeeModelObject) getObject()).getAlias()==null)
					 ((KbeeModelObject) getObject()).setAlias(makeAlias(getObject().getName()));
			 }
				
			getContentDao().save(getObject());
																
			if (getObject() instanceof PersonMember) {
				UserProfile profile = ((PersonMember)getObject()).getProfile(UserProfile.class);
				User user = profile!=null ? profile.getUser() : null;
				if (user!=null) {
					txlogger.info(new SecurityUpdateEvent(user, parts));
				}
				else {
					txlogger.info(new DataSetValueUpdateEvent((DataSetMember) getObject(), parts));
				}
				ServiceLocator.getService(EventService.class).fire(new AppUpdateEvent(getObject()));
 			}
			else
			if (getObject() instanceof DataSetMember) {		
				txlogger.info(new DataSetValueUpdateEvent((DataSetMember) getObject(), parts));
				ServiceLocator.getService(EventService.class).fire(new AppUpdateEvent(getObject()));
			}	
			else if (getObject() instanceof ContentTemplate) {
				ServiceLocator.getService(EventService.class).fire(new AppModelUpdateEvent(((ContentTemplate)getObject()).getDomain()));
				txlogger.info(new ModelUpdateEvent((ContentTemplate) getObject(),  parts));
			}
			else if (getObject() instanceof DataSet) {
				ServiceLocator.getService(EventService.class).fire(new AppModelUpdateEvent(((DataSet)getObject()).getDomain()));
				txlogger.info(new ModelUpdateEvent((DataSet) getObject(),  parts));
			}
			else if (getObject() instanceof Classifier) {
				ServiceLocator.getService(EventService.class).fire(new AppModelUpdateEvent(((Classifier)getObject()).getDomain()));
				txlogger.info(new ModelUpdateEvent((Classifier) getObject(),  parts));
			}
			else if (getObject() instanceof Attribute) {
				ServiceLocator.getService(EventService.class).fire(new AppModelUpdateEvent(((Attribute)getObject()).getDomain()));
				txlogger.info(new ModelUpdateEvent((Attribute) getObject(),  parts));
			}
			else if (getObject() instanceof Library) {
				txlogger.info(new LibraryUpdateEvent((Library) getObject(), parts));
			}
			else if (getObject() instanceof ActionRule) {
				txlogger.info(new ObjectUpdateEvent<KbeeActionRule>((KbeeActionRule)getObject(), parts));
			}
			else if (getObject() instanceof Source) {
				txlogger.info(new SourceUpdateEvent((Source) getObject(), parts));
			}
			else if (getObject() instanceof RelationTemplate) {
				txlogger.info(new ModelUpdateEvent((RelationTemplate) getObject(), parts));
			}
			else if (getObject() instanceof EmailTemplate) {
				txlogger.info(new EmailTemplateUpdateEvent((EmailTemplate) getObject(), parts));	
			}
			//else if (getObject() instanceof SupportTicket) {
			//	internal_update_support_ticket();
			//}
			
			
		} 
		catch (Exception e) {
			logger.error(e);
			throw new ContentMgmtException(e);
		}
	}
	
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void update(List<String> parts)  throws ContentMgmtException {
		internal_update(parts);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void update(LogEvent logevent)  throws ContentMgmtException {
		 getContentDao().save(getObject());
		 txlogger.info(logevent);
 	}
	

	
	/**
	 * this works only for Information Model Objects
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void asyncDelete()  throws ContentMgmtException {
		
		markAsDeleted();
		
		if (getObject() instanceof DataSetMember) {
			
			RemoveDataSetMemberCommand com= new RemoveDataSetMemberCommand ((DataSetMember) getObject());
			try {
				ServiceLocator.getService(SchedulerService.class).enqueue(new CommandWrapperServiceRequest(com));
			} catch (SchedulerException e) {
				logger.error(e);
				throw new ContentMgmtException(e);
			}
			
		}
		
		else if (getObject() instanceof ModelObject) {
			RemoveInformationModelObjectCommand com= new RemoveInformationModelObjectCommand ((ModelObject) getObject());
			try {
				ServiceLocator.getService(SchedulerService.class).enqueue(new CommandWrapperServiceRequest(com));
			} catch (SchedulerException e) {
				logger.error(e);
				throw new ContentMgmtException(e);
			}
		}
	}
	
	
	
	
	/**
     *  <p>
     *  The Group and Rule are removed by the Cascade propagation in Hibernate see {@link KbeeSecuredValue}.<br/> 
	 *  For this reason we log three remove Events. <br/>
	 *  </p>
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void delete()  throws ContentMgmtException, ConstraintException {

		try {

			 getContentDao().delete(getObject());
			
			if (getObject() instanceof DataSet) {
				ServiceLocator.getService(EventService.class).fire(new AppModelUpdateEvent(((DataSet)getObject()).getDomain()));
				txlogger.info(new ModelDeleteEvent((DataSet) getObject(), "Delete"));
			}
			else if (getObject() instanceof Classifier) {
				ServiceLocator.getService(EventService.class).fire(new AppModelUpdateEvent(((Classifier)getObject()).getDomain()));
				txlogger.info(new ModelDeleteEvent((Classifier) getObject(), "Delete"));
			}
			else if (getObject() instanceof Attribute) {
				ServiceLocator.getService(EventService.class).fire(new AppModelUpdateEvent(((Attribute)getObject()).getDomain()));
				txlogger.info(new ModelDeleteEvent((Attribute) getObject(), "Delete"));
			}
			else if (getObject() instanceof ContentTemplate)	{
				ServiceLocator.getService(EventService.class).fire(new AppModelUpdateEvent(((ContentTemplate)getObject()).getDomain()));
				txlogger.info(new ModelDeleteEvent((ContentTemplate) getObject(), "Delete"));
			}
			else if (getObject() instanceof DataSetMember) {
				ServiceLocator.getService(EventService.class).fire(new AppModelUpdateEvent(((DataSetMember)getObject()).getDomain()));
				txlogger.info(new DataSetValueDeleteEvent((DataSetMember) getObject(), "Delete"));
			}
			
			else if (getObject() instanceof SupportTicket) {
				// ServiceLocator.getService(EventService.class).fire(new AppModelUpdateEvent(((SupportTicket)getObject()).getDomain()));
			}
			
		}
		
		catch (Exception e0) {
			try {
				if (getObject() instanceof DataSetMember) {
					logger.error(e0);
					//markAsDeleted();
					//logger.debug( ((DataSetMember) getObject()).getName() +  " Marked as deleted");
				}
				else
					throw new ContentMgmtException(e0);
			} 
			catch (Exception e1) {
				logger.error(e1);
				throw new ContentMgmtException(e1);
			}
		}
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public void restore()  {
	
			if (getObject().getState()!=ObjectState.DELETED) {
				txlogger.error("Object "+ getObject().getId().toString() +". is not in Recycly Bin.");
				return;
			}
			try {
					getObject().setState(ObjectState.ENABLED);
					getObject().setLastModifiedOffsetDateTime(OffsetDateTime.now());
					getObject().setLastModifiedUser(getSessionUser());
					
					 if (getObject() instanceof DataSetMember) {
						((DataSetMember) getObject()).setStrValue(((DataSetMember) getObject()).getStrValue().replace( _DELETED_ , ""));
						 txlogger.info(new DataSetValueUpdateEvent((DataSetMember) getObject(), "Restore"));
						 ServiceLocator.getService(EventService.class).fire(new AppUpdateEvent(getObject()));
					 }
				 
					 else if (getObject() instanceof DataSet) {
						 ((DataSet) getObject()).setName(((DataSet) getObject()).getName().replace( _DELETED_ , ""));
						 txlogger.info(new ModelUpdateEvent((DataSet) getObject(), "Restore"));
					 }
		
					 else if (getObject() instanceof Classifier) {
						 ((Classifier) getObject()).setName(((Classifier) getObject()).getName().replace( _DELETED_ , ""));
						 txlogger.info(new ModelUpdateEvent((Classifier) getObject(), "Restore"));
					 }
					 
					 else if (getObject() instanceof Attribute) {
						 ((Attribute) getObject()).setName(((Attribute) getObject()).getName().replace( _DELETED_ , ""));
						 txlogger.info(new ModelUpdateEvent((Attribute) getObject(), "Restore"));
					 }
				 
					 else if (getObject() instanceof ContentTemplate) { 
						 ((ContentTemplate) getObject()).setName(((ContentTemplate) getObject()).getName().replace( _DELETED_ , ""));
						txlogger.info(new ModelUpdateEvent((ContentTemplate) getObject(), "Restore"));
					 }
					 
					else if (getObject() instanceof SupportTicket) {
						
					}
					 else 
						logger.error( "Class not considered: " + getObject().getClass().getName());
					 
					 getContentDao().save(getObject()); 
					 
			}
			catch(Exception e) {
				logger.error(e);
				throw new ContentMgmtException(e);
			}
    } 
	
	public com.novamens.dom.Object getObject() {
		return this.object;
	}

	protected KbeeUser getSessionUser() {
		return (KbeeUser)ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	
	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
	
	// Spring 
	//														
	private ContentDao contentDao;
	public ContentDao getContentDao()							 	{return contentDao;} 
	public void setContentDao(ContentDao dao) 						{contentDao=dao;}

 	
}
