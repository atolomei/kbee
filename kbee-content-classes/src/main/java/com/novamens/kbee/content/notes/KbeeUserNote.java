package com.novamens.kbee.content.notes;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Locale;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.notes.UserNote;
import com.novamens.content.user.UserProfile;
import com.novamens.datetime.DateTimeService;
import com.novamens.dom.Domain;
import com.novamens.kbee.domain.KbeeDomain;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

@Entity
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="content")
@Table(name = "kb_user_note")
@DynamicInsert
public class KbeeUserNote implements UserNote {
			
	static Logger logger = LogManager.getLogger(KbeeUserNote.class.getName());
	
	@Id 
	@SequenceGenerator(name = "usernote_sequencer", sequenceName = "objectid_sequence")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "usernote_sequencer")
	@Column(name = "ID")
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = KbeeUser.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "user_id", updatable=false)
	private User user;

	@Column(name = "title")
	private String title;
	
	@Column(name = "notetext")
	private String text;
	
	@Column(name = "creationdate")
	private OffsetDateTime created;

	@Column(name = "lastmodifieddate")
	private OffsetDateTime lastmodified;
	
	@Column(name = "priority")		 
	private String priority;

	@ManyToOne(fetch = FetchType.LAZY, targetEntity = KbeeDomain.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "domain_id", updatable=false)
	private Domain domain;
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = KbeeUser.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "lastmodifieduser", updatable=false)
	private User lastmodifieduser;

	@Transient
	private String last_modified_date_colloquial = null;

	@Transient
	private String last_modified_date_colloquial_ago = null;
	
	@Transient
	private String creation_date_colloquial = null;
	
	@Transient
	private String creation_date_colloquial_ago = null;
	
	
	/**
	 * Used by Hibernate
	 */
	public KbeeUserNote() {
	}
	
	
	public KbeeUserNote(User user) {
		setUser(user);
		this.setLastModifiedUser(user);
		this.setCreationOffsetDateTime(OffsetDateTime.now());
		this.setLastModifiedOffsetDateTime(OffsetDateTime.now());	
	}
	
	@Override
	public void setId(Long id) {
		this.id=id;
	}

	@Override
	public Long getId() {
		return this.id;
	}

	@Override
	public User getUser() {
		return this.user;
	}

	@Override
	public String getTitle() {
		return this.title;
	}

	@Override
	public void setTitle(String title) {
			this.title=title;
	}

	@Override
	public String getText() {
		return this.text;
	}

	@Override
	public void setText(String text) {
		this.text=text;

	}

	@Override
	public String getPriority() {
		return this.priority;
	}

	@Override
	public void setPriority(String priority) {
		this.priority=priority;

	}

	@Override
	public OffsetDateTime getCreationOffsetDateTime() {
		return this.created;
	}

	@Override
	public void setCreationOffsetDateTime(OffsetDateTime date) {
			this.created=date;
	}

	@Override
	public OffsetDateTime getModifiedOffsetDateTime() {
		return this.lastmodified;
	}

	@Override
	public void setLastModifiedOffsetDateTime(OffsetDateTime date) {
		this.lastmodified=date;

	}

	@Override
	public Domain getDomain() {
		return this.domain;
	}
	
	@Override
	public void setLastModifiedUser(User user) {
		this.lastmodifieduser=user;
	}
		
	public void setUser(User user) {
		this.user=user;
		UserProfile profile = getContentDao().findUserProfileByUser(user);
		if (profile!=null)
			this.domain=profile.getDomain();
	}

	@Override
	public User getLastModifiedUser() {
		return this.lastmodifieduser;
	}
	
	private ContentDao getContentDao() {
			 BeansService beans = ServiceLocator.getService(BeansService.class);
			 return (ContentDao) beans.getBean("contentDao");
	}

	
	
	@Override
	public String getCreationOffsetDateTimeColloquial() {
		return getCreationOffsetDateTimeColloquial(false);
	}
	
	@Override
	public String getCreationOffsetDateTimeColloquialAgo() {
		return getCreationOffsetDateTimeColloquial(true);
	}

	/** -----------------------------------------------------------------------------------------------------------
	 * The text is in the Locale and TimeZone of the Session User 
	 * If there is not Session User, then defaults.
	 */
	public String getCreationOffsetDateTimeColloquial(boolean ago) {
	
		
		if (ago) {
			if (creation_date_colloquial_ago!=null)
				return creation_date_colloquial_ago;
		} else {
			if (creation_date_colloquial!=null)
				return creation_date_colloquial;
		};
				
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
				
				if (getCreationOffsetDateTime()==null) {
						creation_date_colloquial_ago="na";
						creation_date_colloquial="na";
				}
				else
					if (ago)
						creation_date_colloquial_ago=service.timeElapsed(getCreationOffsetDateTime(), ZoneId.of(zid), locale, DateTimeService.DATE_COLlOQUIAL_AGO, "ago");
					else
						creation_date_colloquial=service.timeElapsed(getCreationOffsetDateTime(), ZoneId.of(zid), locale, DateTimeService.DATE_COLlOQUIAL, null);
				
				return ago?creation_date_colloquial_ago:creation_date_colloquial;
	}


	@Override
	public String getLastModifiedOffsetDateTimeColloquialAgo() {
		return getLastModifiedOffsetDateTimeColloquial(true);
	}
	
	@Override
	public String getLastModifiedOffsetDateTimeColloquial() {
		return getLastModifiedOffsetDateTimeColloquial(false); 
	}
	/** -----------------------------------------------------------------------------------------------------------
	 * The text is in the Locale and TimeZone of the Session User 
	 * If there is not Session User, then defaults.
	 * 
	 */
	
	public String getLastModifiedOffsetDateTimeColloquial(boolean ago) {
			
		if (ago) {
			if (last_modified_date_colloquial_ago!=null)
				return last_modified_date_colloquial_ago;
			} else {
				if (last_modified_date_colloquial!=null)
					return last_modified_date_colloquial;
			};
			
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
			
			if (getModifiedOffsetDateTime()==null) {
				if (ago)
					last_modified_date_colloquial_ago="na";
				else
					last_modified_date_colloquial="na";
			}
			else {
				if (ago)  
					last_modified_date_colloquial_ago=service.timeElapsed(getModifiedOffsetDateTime(), ZoneId.of(zid), locale, DateTimeService.DATE_COLlOQUIAL_AGO, "ago");
				else
					last_modified_date_colloquial=service.timeElapsed(getModifiedOffsetDateTime(), ZoneId.of(zid), locale, DateTimeService.DATE_COLlOQUIAL, null);
			}
			return ago?last_modified_date_colloquial_ago:last_modified_date_colloquial;
		}
	
	
	protected KbeeUser getSessionUser() {
		return (KbeeUser)ServiceLocator.getService(SecurityService.class).getSessionUser();
	}

	@Override
	public String getDisplayName() {
		return getTitle()!=null?getTitle():String.valueOf(getId());
	}


	@Override
	public void setDomain(Domain domain) {
		this.domain=domain;
	}
	
}
