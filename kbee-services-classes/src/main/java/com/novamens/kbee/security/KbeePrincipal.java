package com.novamens.kbee.security;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Locale;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.novamens.datetime.DateTimeService;
import com.novamens.dom.Domain;
import com.novamens.dom.DomainObject;
import com.novamens.dom.Indexable;
import com.novamens.kbee.domain.KbeeDomain;

import com.novamens.security.Principal;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

@Entity
@Table(name = "PRINCIPAL")
@Inheritance(strategy = InheritanceType.JOINED)
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="entity")
public abstract class KbeePrincipal implements Principal,  Comparable<Principal>, DomainObject, Indexable {
					
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeePrincipal.class.getName());
	
	@Id 
	@SequenceGenerator(name = "principal_sequencer", sequenceName = "security_sequence", allocationSize=1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "principal_sequencer")
	@Column(name = "id")
	private Long id;
	
	@Column(name = "lastModifiedDate")
	private OffsetDateTime lastModifiedDate;
	
	@Column(name = "creationDate")
	private OffsetDateTime creationDate;
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = KbeeUser.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "lastModifiedUser")
	private User lastModifiedUser;
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = KbeeDomain.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "domain_id")
	private Domain domain;
	
	public Long getId() {
		return id;
	}

	@Override
	public String getLastModifiedOffsetDateTimeColloquial() {
		return getLastModifiedOffsetDateTimeColloquial("ago");
	}
	
	@Override
	public String getCreationOffsetDateTimeColloquial() {
		return getLastModifiedOffsetDateTimeColloquial(getCreationOffsetDateTime(), "ago");
	}
	
	public String getLastModifiedOffsetDateTimeColloquial(String classago) {
		return getLastModifiedOffsetDateTimeColloquial(getLastModifiedOffsetDateTime(), classago);
	}

	
	protected KbeeUser getSessionUser() {
		return (KbeeUser)ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	
	@Override
	public String getDisplayName() {
		return getName();
	}
	
	@Override
	public OffsetDateTime getLastModifiedOffsetDateTime() {
		return lastModifiedDate;
	}
	
	@Override
	public void setLastModifiedOffsetDateTime(OffsetDateTime lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}

	@Override
	public OffsetDateTime getCreationOffsetDateTime() {
		return creationDate;
	}
	
	@Override
	public void setCreationOffsetDateTime(OffsetDateTime date) { 
		this.creationDate=date;
	}

	@Override
	public User getLastModifiedUser() {
		return lastModifiedUser;
	}

	@Override
	public void setLastModifiedUser(User lastModifiedUser) {
		this.lastModifiedUser = lastModifiedUser;
	}
	
	public void setDomain(Domain domain) {
		this.domain = domain;
	}
	
	public Domain getDomain() {
		return this.domain;
	}
	
	@Override
	public void setDefaultAudit() {
		
		if (this.getCreationOffsetDateTime()==null)
			this.setCreationOffsetDateTime(OffsetDateTime.now());
		
		if (this.getLastModifiedOffsetDateTime()==null)
		this.setLastModifiedOffsetDateTime(OffsetDateTime.now());
 		
	}
	
	@Override
	public int compareTo(com.novamens.security.Principal o) {
		try {
		if (getName()==null)
			return (o.getName()==null)?0:1;
		if (o.getName()==null)
			return -1;
		return getName().compareToIgnoreCase(o.getName());
		} catch (Exception e) {
			logger.error(e);
			return 0;
		}
	}
	
	private String getLastModifiedOffsetDateTimeColloquial(OffsetDateTime date, String classago) {
		if (date==null) 
			return "";
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
		return service.timeElapsed(date, ZoneId.of(zid), locale, DateTimeService.DATE_COLlOQUIAL_AGO, classago);
		
	}

}
