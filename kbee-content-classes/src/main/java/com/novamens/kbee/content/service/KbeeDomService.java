package com.novamens.kbee.content.service;

import java.lang.reflect.ParameterizedType;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.novamens.beans.BeansService;
import com.novamens.content.base.ConstraintException;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.dao.PortalDao;
import com.novamens.content.model.ModelElement;
import com.novamens.content.service.GenericDomService;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.dom.DomainObject;
import com.novamens.dom.ObjectState;
import com.novamens.event.AppDeleteEvent;
import com.novamens.event.AppUpdateEvent;
import com.novamens.event.EventService;
import com.novamens.event.LogEvent;
import com.novamens.kbee.dom.KbeeModelObject;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.logging.ObjectDeleteEvent;
import com.novamens.logging.ObjectUpdateEvent;
import com.novamens.repository.DomRepository;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

/**
 * 
 * <p>Servicios de Alta, Baja, Modificación para un objeto Dom  delete, update,  etc. <br />
 * Nota: Los objetos de tipo <@link Content} usan un servicio diferente</p>
 * <p>Para usar este servicio con una clase especifica hay que hacer dos cosas:</p>
 * <ul>
 * <li>1) Crear una subclase del servicio con la clase correspondiente y registrarla como Bean de scope prototype. Por ejemplo,
 * 			@Component @Scope("prototype")
 * 			class LibraryDomService extends KbeeDOMService<Library>
 * </li>
 * <li>2) Crear un repositorio para la clase con interfase CRUD</li>
 * </ul>  
 *  
 *  See:
 *  {@link Library}
 *  
 */
public class KbeeDomService<T extends com.novamens.dom.Object, I> implements GenericDomService<T> {
				
	private T object = null;

	static kbee.util.logging.Logger logger = new kbee.util.logging.Logger(LogManager.getLogger(KbeeDomService.class.getName()));
	
	// Logger synchronous with the TRX	*/
	static private Logger txlogger = LogManager.getLogger("TxLogger");
	
	@Autowired
	private DomRepository<I> repository;

	public KbeeDomService() {
	}
	
	public KbeeDomService(T object) {
		 this.object = object;
	}
	
	public T getObject() {
		return this.object;
	}
	
	@SuppressWarnings("unchecked")
	public boolean setObject(Object object) {
		Class<T> typeOfT = (Class<T>)
				((ParameterizedType)getClass()
				.getGenericSuperclass())
				.getActualTypeArguments()[0];
		if (typeOfT.isInstance(object)) {
			this.object = (T)object;
			return true;
		}	
		return false;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	@SuppressWarnings("unchecked")
	public void markAsDeleted()  throws ContentMgmtException {

		getObject().setState(ObjectState.DELETED);
		
		if (getSessionUser()!=null) 
			getObject().setLastModifiedUser(getSessionUser());
		
		getObject().setLastModifiedOffsetDateTime(OffsetDateTime.now());
		
		try {
			getRepository().save((I)getObject());
			txlogger.info(new ObjectDeleteEvent<T>(getObject(), "mark as Deleted"));
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
		List<String> parts = new ArrayList<String>();
		parts.add(part);
		update(parts);
	}
	
	@Override
	@Transactional
	@SuppressWarnings("unchecked")
	public void update(List<String> parts)  throws ContentMgmtException {

		if (getSessionUser()!=null) 
			getObject().setLastModifiedUser(getSessionUser());
		
		if (getObject()!=null)
			getObject().setLastModifiedOffsetDateTime(OffsetDateTime.now());

		if (getObject() instanceof DomainObject && ((DomainObject) getObject()).getDomain()==null) 
			((DomainObject) getObject()).setDomain(getDomain());
			
		getObject().setLastModifiedOffsetDateTime(OffsetDateTime.now());
		
		try {
		
			if (getObject() instanceof KbeeModelObject) {
				if (((KbeeModelObject) getObject()).getAlias()==null)
					((KbeeModelObject) getObject()).setAlias(makeAlias(getObject().getName()));
			}
			
			getRepository().save((I)getObject());
			
			txlogger.info(new ObjectUpdateEvent<T>(getObject(), parts));
			ServiceLocator.getService(EventService.class).fire(new AppUpdateEvent(getObject()));
		} 
		catch (Exception e) {
			logger.error(e);
			throw new ContentMgmtException(e);
		}
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	@SuppressWarnings("unchecked")
	public void update(LogEvent logevent)  throws ContentMgmtException {
		getRepository().save((I)getObject());
		txlogger.info(logevent);
	}
	
	/**
	 *  The Group and Rule are removed by the Cascade propagation in Hibernate
	 *  see {@link KbeeSecuredValue}. For this reason we log three remove Events.
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	@SuppressWarnings("unchecked")
	public void delete()  throws ContentMgmtException, ConstraintException {
		try {
			getRepository().delete((I)getObject());
			
			if (getObject() instanceof DomainObject) {
				ServiceLocator.getService(EventService.class).fire(new AppDeleteEvent(getObject()));
			}	
			
			txlogger.info(new ObjectDeleteEvent<T>(getObject(), "Delete"));
		} 
		catch (Exception e0) {
			try {
				if (getObject() instanceof ModelElement) {
					logger.error(e0);
					markAsDeleted();
					logger.debug(getObject().getName() +  " Marked as deleted");
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
	@SuppressWarnings("unchecked")
	public void restore()  {
		if (getObject().getState()!=ObjectState.DELETED) {
			txlogger.error("Object "+ getObject().getId().toString() +". is not in Recycly Bin.");
			return;
		}
		try {
			getObject().setState(ObjectState.ENABLED);
			getObject().setLastModifiedOffsetDateTime(OffsetDateTime.now());
			getObject().setLastModifiedUser(getSessionUser());
				
			getRepository().save((I)getObject());
			
			txlogger.info(new ObjectUpdateEvent<T>(getObject(), "Restore"));
			ServiceLocator.getService(EventService.class).fire(new AppUpdateEvent(getObject()));
		}
		catch(Exception e) {
			logger.error(e);
			throw new ContentMgmtException(e);
		}
	} 
	
	public DomRepository<I> getRepository() {
		return repository;
	}

	protected KbeeUser getSessionUser() {
		return (KbeeUser)ServiceLocator.getService(SecurityService.class).getSessionUser();
	}

	protected PortalDao getPortalDao() {
		return (PortalDao)ServiceLocator.getService(BeansService.class).getBean("portalDao");
	}
	
	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
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
}