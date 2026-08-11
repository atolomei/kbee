package com.novamens.kbee.dom;


import java.io.Serializable;
//import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.time.ZoneId;

import java.util.Locale;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Type;

import com.novamens.datetime.DateTimeService;
import com.novamens.dom.Domain;
import com.novamens.dom.DomainObject;
import com.novamens.dom.ObjectState;
import com.novamens.dom.Object;
import com.novamens.kbee.domain.KbeeDomain;
import com.novamens.kbee.security.KbeeUser;

import com.novamens.security.User;
import com.novamens.service.ObjectService;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.service.ServiceNotFoundException;

@MappedSuperclass
public abstract class AbstractObject implements Object, DomainObject {
						
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(AbstractObject.class.getName());

	@Column(name = "state")
	@Enumerated(EnumType.ORDINAL)
	@Type(type="com.novamens.kbee.dom.ObjectStateUserType")
	private ObjectState state;
	
	@Column(name = "creationDate")
	private OffsetDateTime creationDate;
	
	@Column(name = "lastModifiedDate")
	private OffsetDateTime lastModifiedDate;

	@ManyToOne(fetch = FetchType.LAZY, cascade=CascadeType.DETACH, targetEntity = KbeeUser.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "lastModifiedUser")
	private User lastModifiedUser;

	@ManyToOne(fetch = FetchType.LAZY, targetEntity = KbeeDomain.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "domain_id", updatable=false)
	private Domain domain;
	
	@Transient
	private String last_modified_date_colloquial = null;

	@Transient
	private String creation_date_colloquial = null;
	
	public AbstractObject() {
		
	}
	
	public AbstractObject(AbstractObject source) {
		this.state=source.getState();
		this.creationDate=OffsetDateTime.now();
		this.lastModifiedDate=OffsetDateTime.now();
		this.domain=source.getDomain();
		this.lastModifiedUser=source.getLastModifiedUser();
	}
	
	public <T extends ObjectService> T getService(Class<T> service) throws ServiceNotFoundException {
		return ServiceLocator.getService(this, service);
	}
	
	
	public void setDomain(Domain domain) {
		this.domain = domain;
	}
	
	public Domain getDomain() {
		return this.domain;
	}

	@Override
	public void setCreationOffsetDateTime(OffsetDateTime date) {
		creationDate=date;
	}
	
	@Override
	public void setLastModifiedOffsetDateTime(OffsetDateTime date) {
		this.lastModifiedDate = date;
		if (creationDate==null)
			creationDate=date;
		this.last_modified_date_colloquial=null;
	}
	
	@Override
	public OffsetDateTime getCreationOffsetDateTime() {
		return creationDate!=null?creationDate:lastModifiedDate;
	}
	
	@Override
	public OffsetDateTime getLastModifiedOffsetDateTime() {
		return lastModifiedDate;
	}
	
	public void setLastModifiedUser(User user)	{
		lastModifiedUser = user;
	}
	
	public User getLastModifiedUser() {
		return lastModifiedUser;
	}
	
	public void setState(ObjectState state)	{
		this.state = state;
	}
	
	public ObjectState getState() {
		return state;
	}
	
	public abstract Serializable getId();
	
	
	/**
	 * <p>toString is used to display info of the Object for the developers</p>
	 */
	public String toString() {
		StringBuilder str = new StringBuilder();
		
		str.append( "id: " + (getId()!=null? getId().toString() :"null") );
		
		if (getState()!=null)
			str.append( " | " + "state: " +  (getState()!=null?getState().getLabel():"null"));
		
		if(getLastModifiedOffsetDateTime()!=null)  
			str.append( " | " + "modified: " + (getLastModifiedOffsetDateTime()!=null?getLastModifiedOffsetDateTime().toString():"null"));
					
		//if(getLastModifiedUser()!=null)
		//	str.append( " | " + "user: " + getLastModifiedUser().getFirstLastName());
		
		if (getDomain()!=null)
				str.append(" | " + "domain: " + getDomain().getName().toString());
		
		return str.toString();
	}
	
	/**
	 * <p>Ver como integrarlo con @PrePersist</p>
	 */

	public void setDefaultAudit() {
		
		if (getLastModifiedOffsetDateTime()==null)
			setLastModifiedOffsetDateTime(OffsetDateTime.now());

		if (getCreationOffsetDateTime()==null)
			setCreationOffsetDateTime(OffsetDateTime.now());

		if (getLastModifiedUser()==null)
			setLastModifiedUser(getSessionUser());

		if (getState()==null)
			setState(ObjectState.ENABLED);

		if (getDomain()==null)
			setDomain(getDomain());
	}
	
	
	@Override
	public String getLastModifiedOffsetDateTimeColloquial() {
		return getLastModifiedOffsetDateTimeColloquial("ago");
	}
	
	/** 
	 * <p>The text is in the Locale and TimeZone of the Session User. 
	 * If there is not Session User, then defaults.</p>
	 */
	
	public String getLastModifiedOffsetDateTimeColloquial(String css) {
		
		if (last_modified_date_colloquial!=null)
			return last_modified_date_colloquial;
			
		DateTimeService service = ServiceLocator.getService(DateTimeService.class);
		
		User user = getSessionUser();
		String zid = null;
		if (user!=null)
			zid=service.getMapZoneIds().get(user.getTimeZone());
		if (zid==null)
			zid=ZoneId.systemDefault().getId();
		Locale locale = null;
		if (user!=null)
				locale=user.getLocale();
		else
			locale=Locale.getDefault();
			
		if (getLastModifiedOffsetDateTime()==null)
			last_modified_date_colloquial="na";
		else
			last_modified_date_colloquial=service.timeElapsed(getLastModifiedOffsetDateTime(), ZoneId.of(zid), locale, DateTimeService.DATE_COLlOQUIAL_AGO, css);
			
		return last_modified_date_colloquial;
	}
	
	/**
	 * <p>The text is in the Locale and TimeZone of the Session User 
	 * If there is not Session User, then defaults.</p>
	 * 
	 */
	@Override
	public String getCreationOffsetDateTimeColloquial() {
				
		if (creation_date_colloquial!=null)
			return creation_date_colloquial;

		DateTimeService service = ServiceLocator.getService(DateTimeService.class);
		User user = getSessionUser();
		String zid = null;
		if (user!=null)
			zid=service.getMapZoneIds().get(user.getTimeZone());
		if (zid==null)
			zid=ZoneId.systemDefault().getId();
		Locale locale = null;
		if (user!=null)
			locale=user.getLocale();
		else
			locale=Locale.getDefault();
				
		creation_date_colloquial=(getCreationOffsetDateTime()==null)?"":service.timeElapsed(getCreationOffsetDateTime(), ZoneId.of(zid), locale, DateTimeService.DATE_COLlOQUIAL_AGO, null);
		return creation_date_colloquial;
	}
		
	public String getDisplayName() {
		return null;
	}
	
	protected User getSessionUser() {
		try {
			return ServiceLocator.getService(SecurityService.class).getSessionUser();
		} 
		catch (Exception e) {
			logger.error(e);
			return null;
		}
	}
	
	/** 
	 * @param object
	 */
	protected void onClone(AbstractObject object) {
		object.setDomain(getDomain());
		setLastModifiedOffsetDateTime(OffsetDateTime.now());
		object.setState(ObjectState.DRAFT);
	}

	
}
