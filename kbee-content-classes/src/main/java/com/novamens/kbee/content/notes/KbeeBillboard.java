package com.novamens.kbee.content.notes;


import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.notes.Billboard;
import com.novamens.content.resource.KBFile;
import com.novamens.content.security.Role;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserService;
import com.novamens.datetime.DateTimeService;
import com.novamens.dom.Domain;
import com.novamens.kbee.content.resource.KBFileImpl;
import com.novamens.kbee.content.security.KbeeAbstractRole;
import com.novamens.kbee.domain.KbeeDomain;
import com.novamens.kbee.security.KbeePrincipal;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.scheduler.CronExpressionJ8;
import com.novamens.security.Principal;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;


/**
 * <p>Billboard Alerts</p>
 * (former name was KbeeWorkNote)
 *
 */
@Entity
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="content")
@Table(name = "kb_work_note")
@DynamicInsert
public class KbeeBillboard implements Billboard {
			
	@SuppressWarnings("unused")
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeBillboard.class.getName());
	
	@Id 
	@SequenceGenerator(name = "worknote_sequencer", sequenceName = "objectid_sequence")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "worknote_sequencer")
	@Column(name = "ID")
	private Long id;

	@ManyToMany(fetch = FetchType.LAZY, targetEntity = KbeePrincipal.class)
	@JoinTable(name = "KB_WORKNOTE_PRINCIPAL", joinColumns = {
			@JoinColumn(name = "NOTE_ID", nullable = false, updatable = false) }, inverseJoinColumns = {
					@JoinColumn(name = "PRINCIPAL_ID", nullable = false, updatable = false) })
	List<Principal> receivers = new ArrayList<Principal>();
	
	@ManyToMany(fetch = FetchType.LAZY, targetEntity = KbeeAbstractRole.class)
	@JoinTable(name = "KB_WORK_NOTE_ROLE", joinColumns = {
			@JoinColumn(name = "NOTE_ID", nullable = false, updatable = false) }, inverseJoinColumns = {
					@JoinColumn(name = "ROLE_ID", nullable = false, updatable = false) })
	List<Role> rolereceivers = new ArrayList<Role>();

	@Column(name = "startpub")				// date-start-publication
	private OffsetDateTime startpub;

	@Column(name = "endpub")				// date-end-publication
	private OffsetDateTime endpub;

	@ManyToOne(fetch = FetchType.LAZY, targetEntity = KbeeUser.class)
	@Fetch(FetchMode.SELECT)	
	@JoinColumn(name = "user_id", updatable=false)
	private User user;

	@Column(name = "title")
	private String title;
	
	@Column(name = "timezone")
	private String timezone;

	@Column(name = "notetext")
	private String text;
	
	@Column(name = "cronexpression")
	private String cronExpressionstr;

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

	@Column(name = "send_notification")
	private boolean isfirstversion = true;

	@Column(name = "isemail")
	private boolean isemail = false;
	
	@Column(name = "isalert")
	private boolean isalert = false;

	
	@Column(name = "isbillboard")
	private boolean isbillboard = false;
	
	
	@Column(name = "glyphicon")
	private String glyphicon;
	
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = KBFileImpl.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "kfile")
	private KBFile kfile;


	@ManyToOne(fetch = FetchType.LAZY, targetEntity = KBFileImpl.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "sideimage")
	private KBFile side;
	
	@Transient
	private String last_modified_date_colloquial = null;

	@Transient
	private String last_modified_date_colloquial_ago = null;
	
	@Transient
	private String creation_date_colloquial = null;
	
	@Transient
	private String creation_date_colloquial_ago = null;

	
	public KbeeBillboard() {
	}
	
	public KbeeBillboard(User user) {
		setUser(user);
		this.setLastModifiedUser(user);
		this.setCreationOffsetDateTime(OffsetDateTime.now());
		this.setLastModifiedOffsetDateTime(OffsetDateTime.now());	
	}
	
	@Override
	public String getGlyphicon() {
		return glyphicon;
	}

	public void setGlyphicon(String glyphicon) {
		this.glyphicon = glyphicon;
	}
	
	@Override
	public OffsetDateTime getStartpub() {
		return startpub;
	}
	
	public void setStartpub(OffsetDateTime startpub) {
		this.startpub = startpub;
	}

	@Override
	public OffsetDateTime getEndpub() {
		return endpub;
	}

	public void setEndpub(OffsetDateTime endpub) {
		this.endpub = endpub;
	}


	public String getFontAwesomeIcon() {
			
		if (this.getGlyphicon()==null)
			return "";
			
		if (fai.containsKey(this.getGlyphicon()))
				return fai.get(this.getGlyphicon());
			
		return this.getGlyphicon();
			
		//return DEFAULT_ICON;
	}

	public void sendNotification(boolean b) {
		this.isfirstversion=b;
	}
	
	@Override
	public boolean isSendNotification() {
		return this.isfirstversion;
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
	public void setDomain(Domain domain) {
		this.domain = domain;
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
		
	/** 
	 * The text is in the Locale and TimeZone of the Session User 
	 * If there is not Session User, then defaults.
	 * 
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
	public String getLastModifiedOffsetDateTimeColloquial(String css) {
		return getLastModifiedOffsetDateTimeColloquial(css!=null?false:true); 
	}
	
	
	/** 
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
	
	@Override
	public String getDisplayName() {
		return getTitle()!=null?getTitle():String.valueOf(getId());
	}
	
	
	@Override
	public String toString() {
		StringBuilder str= new StringBuilder();
		str.append(getTitle());
		str.append(getText()!=null? (" | " + getText()):"");
		return str.toString();
	}

	protected KbeeUser getSessionUser() {
		return (KbeeUser)ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	
	protected Domain getSessionDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}


	@Override
	public void setReceivers(List<Principal> receivers) {
		this.receivers.clear();
		if (receivers!=null)
			this.receivers.addAll(receivers);
	}
	
	@Override
	public List<Principal> getReceivers() {
		return this.receivers;
	}
	
	@Override
	public List<Role> getRoleReceivers() {
		return rolereceivers;
	}

	public void setRoleReceivers(List<Role> receivers) {
		this.rolereceivers.clear();
		this.rolereceivers.addAll(receivers);
	}

	public void setEmail(boolean isemail) {
		this.isemail=isemail;
	}
	
	public void setIsEmail(boolean isemail) {
		this.isemail=isemail;
	}
	
	public void setAlert(boolean isalert) {
		this.isalert=isalert;
	}
	
	public void setIsAlert(boolean isalert) {
		this.isalert=isalert;
	}
	
	@Override
	public boolean isEmail() {
		return this.isemail;
	}

	@Override
	public boolean isAlert() {
		return this.isalert;
	}

	public void setBillboard(boolean b) {
		this.isbillboard=b;
	}
	
	@Override
	public boolean isBillboard() {
		return this.isbillboard;
	}


	@Override
	public void setDefaultAudit() {
		 this.setCreationOffsetDateTime(OffsetDateTime.now());
		 this.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		 this.setDomain(getDomain());
	}

	@Override
	public OffsetDateTime getLastModifiedOffsetDateTime() {
		return this.lastmodified;
	}


	public CronExpressionJ8 getCronExpression() {
		if (cronExpressionstr==null)
			return null;
		return new CronExpressionJ8(cronExpressionstr);
	}

	public void setCronExpression(CronExpressionJ8 cronExpression) {
		if (cronExpression!=null)
			cronExpressionstr=cronExpression.getExpression();
	}

	public String getCronExpressionStr() {
		return cronExpressionstr;
	}

	public void setCronExpressionStr(String cronExpression) {
		this.cronExpressionstr = cronExpression;
	}

	public void setTimeZone(String timeZone) {
		this.timezone=timeZone;
	}
	
	public String getTimeZone() {
		return timezone;
	}

	static Map<String, String> fai = new HashMap<String, String>();
	static List<String> icon_list = new ArrayList<String>();
	
	static final String DEFAULT_ICON= "fal fa-exclamation"; 
	static  {
		
		fai.put("tools", "fal fa-tools");
		fai.put("exclamation", "fal fa-exclamation");
		fai.put("hourglass", "fal fa-hourglass");
		fai.put("cabinet", "fal fa-cabinet-filing");
		fai.put("christmas tree", "fal fa-tree-christmas");
		fai.put("glass cheers", "fal fa-glass-cheers");
		fai.put("hat santa", "fal fa-hat-santa");
		fai.put("phone", "fal fa-phone");
		fai.put("thanksgiving", "fal fa-turkey");
		fai.put("anniversary", "fal fa-birthday-cake");
		fai.put("plug", "fal fa-plug");
		fai.put("desktop", "fal fa-desktop");
		fai.put("comment exclamation", "fal fa-comment-exclamation");
		fai.put("comment", "fal fa-comment");
		fai.put("birthday cake", "fal fa-birthday-cake");
		fai.put("exclamation-triangle", "fal fa-exclamation-triangle");
		fai.put("thumbs up", "fal fa-thumbs-up");
		fai.put("thumbs down", "fal fa-thumbs-down");
		fai.put("engine warning", "fal fa-engine-warning");
		fai.put("bullhorn", "fal fa-bullhorn");
		fai.put("coffee", "fal fa-coffee");
		fai.put("paper plane", "fal fa-paper-plane");
		fai.put("envelope", "fal fa-envelope");
		fai.put("concierge-bell", "fal fa-concierge-bell");
		fai.put("key", "fal fa-key");
		fai.put("phone", "fal fa-phone");
		fai.put("info circle", "fa fa-info-circle");
		fai.put("info", "fal fa-info-circle");
		fai.put("calendar", "fal fa-calendar-alt");
		fai.put("lightbulb", "fal fa-lightbulb");
		fai.put("car", "fal fa-car-side");
		fai.put("shopping bags", "fal fa-bags-shopping");
		fai.put("shopping cart", "fal fa-shopping-cart");
		fai.put("gift","far fa-gift");
		fai.put("heart","fal fa-heart");
		fai.put("info", "fal fa-info");
		fai.put("info square","fal fa-info-square");
		fai.put("info circle", "fa-info-circle");
		fai.put("question", "fal fa-question");
		fai.put("exclamation-circle", "fal fa-exclamation-circle");
		fai.put("times", "fal fa-times");
		fai.put("flower tulip", "fal fa-flower-tulip");
		fai.put("winter hat", "fal fa-hat-winter");
		fai.put("umbrella beach", "fal fa-umbrella-beach");
		fai.put("seedling", "fal fa-seedling");
		
		for (Entry<String, String> entry : fai.entrySet()) {
			icon_list.add(entry.getKey());
		}
		Collections.sort(icon_list);
	}

	public static List<String> getIconList() {
		return icon_list;
	}
	
	public static String getFontAwesomeIcon(String icon) {
		if (icon==null)
			return "";
		if (fai.containsKey(icon))
			return fai.get(icon);
		// return DEFAULT_ICON;
		return icon;
	}
	
	
	@Override
	public void setFile( KBFile kfile) {
		this.kfile=kfile;
	}
	
	@Override
	public KBFile getFile() {
		return this.kfile;
	}

	@Override
	public String getLastModifiedOffsetDateTimeColloquial() {
		return getLastModifiedOffsetDateTimeColloquial("ago");
	}

	
	@Override
	public void setSideImage(KBFile kfile) {
		this.side=kfile;
	}

	@Override
	public KBFile getSideImage() {
		return this.side;
	}
	
	
	public KBFile getsideimage() {
		return this.side;
	}
}
