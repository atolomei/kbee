package kbee.content.support;

import java.io.IOException;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

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

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.resource.KBFile;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserService;
import com.novamens.datetime.DateTimeService;
import com.novamens.dom.Domain;
import com.novamens.kbee.content.resource.KBFileImpl;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.security.User;
import com.novamens.security.audit.AuditSet;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;


@Entity
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="content")
@Table(name = "kb_support_ticket")
@DynamicInsert
public class KbeeSupportTicket extends com.novamens.kbee.dom.AbstractObject implements SupportTicket {

	static private ObjectMapper mapper = new ObjectMapper();
	
	static  {
		//smapper.registerModule(new JavaTimeModule());
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeSupportTicket.class.getName());
	
	@Id 
	@SequenceGenerator(name = "worknote_sequencer", sequenceName = "objectid_sequence")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "worknote_sequencer")
	@Column(name = "ID")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, targetEntity = KbeeUser.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "user_id", updatable=false)
	private User user;

	@Column(name = "subject")
	private String subject;
	
	@Column(name = "text")
	private String text;
	
	@Column(name = "context")
	private String context;
	
	@Column(name = "priority")		 
	private int priority;

	@ManyToOne(fetch = FetchType.LAZY, targetEntity = KBFileImpl.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "kfile")
	private KBFile kfile;
	
	@Column(name = "deliverystatus")
	private int deliverystatus;

	@Column(name = "deliverymsg")
	private String deliverystatusmsg;
	
	@Column(name = "error_count")
	private int error_count;

	
	
	
	@Transient
	private String last_modified_date_colloquial = null;

	@Transient
	private String last_modified_date_colloquial_ago = null;
	
	@Transient
	private String creation_date_colloquial = null;
	
	@Transient
	private String creation_date_colloquial_ago = null;

	@Transient 
	private 
	Map<String, String> map = null;
	

	
	/**
	 * Used by Hibernate
	 */
	public KbeeSupportTicket() {
	}
	
	
	public KbeeSupportTicket(User user) {
			this(user, null, null);
	}
	public KbeeSupportTicket(User user, String subject, String text) {
		setUser(user);
		setSubject(subject);
		setText(text);
		this.setLastModifiedUser(user);
		this.setCreationOffsetDateTime(OffsetDateTime.now());
		this.setLastModifiedOffsetDateTime(OffsetDateTime.now());	
	}
	
	
	@Override
	public User getUser() {
			return this.user;
	}
	
	@Override
	public void setUser(User user) {
		this.user=user;
		UserProfile profile = getContentDao().findUserProfileByUser(user);
		if (profile!=null)
			setDomain(profile.getDomain());
	}

	
	@Override
	public String getDeliveryStatusMsg() {
		return deliverystatusmsg;
	}

	@Override
	public void setDeliveryStatusMsg(String deliverystatusmsg) {
		this.deliverystatusmsg = deliverystatusmsg;
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
	 */

	@Override
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
	@Override
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
			
			if (this.getLastModifiedOffsetDateTime()==null) {
				if (ago)
					last_modified_date_colloquial_ago="na";
				else
					last_modified_date_colloquial="na";
			}
			else {
			
				if (ago)  
					last_modified_date_colloquial_ago=service.timeElapsed(getLastModifiedOffsetDateTime(), ZoneId.of(zid), locale, DateTimeService.DATE_COLlOQUIAL_AGO, "ago");
				else
					last_modified_date_colloquial=service.timeElapsed(getLastModifiedOffsetDateTime(), ZoneId.of(zid), locale, DateTimeService.DATE_COLlOQUIAL, null);
			}
			return ago?last_modified_date_colloquial_ago:last_modified_date_colloquial;
		}
	
	@Override
	public String getDisplayName() {
		return getSubject()!=null?getSubject():String.valueOf(getId());
	}
	
	@Override
	public String toString() {
		StringBuilder str= new StringBuilder();
		str.append(getDisplayName());
		str.append(getText()!=null? (" | " + getText()):"");
		return str.toString();
	}

	@Override
	public Serializable getId() {
		return this.id;
	}

	@Override
	public void setDefaultAudit() {
		
	}

	@Override
	public AuditSet getAuditSet() {
		return AuditSet.SUPPORT;
	}

	

	
	@Override
	public String getSubject() {
		return subject;
	}

	@Override
	public void setSubject(String subject) {
		this.subject = subject;
	}

	@Override
	public String getText() {
		return text;
	}

	@Override
	public void setText(String text) {
		this.text = text;
	}

	
	@Override
	public int getPriority() {
		return priority;
	}

	
	@Override
	public void setPriority(int priority) {
		this.priority = priority;
	}

	@Override
	public KBFile getKBFile() {
		return kfile;
	}
	
	@Override
	public void setKBFile(KBFile kfile) {
		this.kfile = kfile;
	}

	@Override
	public int getDeliveryStatus() {
		return deliverystatus;
	}

	@Override
	public void setDeliveryStatus(int deliverystatus) {
		this.deliverystatus = deliverystatus;
	}

	
	
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, String> getContext() {
		
		if (map!=null)
			return map;
		 try {
			 map = mapper.readValue(context, Map.class);
		} catch (IOException e) {
			map = null;
			logger.error(e);
		}
		 return new HashMap<String, String>();
	}
	
	@Override
	public void setContext(Map<String, String> map) {
		
		this.map=null;
		
		if (map==null)
			context=null;
		else {
				String json;
				try {
					json = mapper.writeValueAsString(map);
					context = json;
				} catch (JsonProcessingException e) {
					logger.error(e);
				}
		}
	}

	
	@Override
	public void setId(Serializable id) {
		this.id= (Long) id;
	}

	@Override
	public String getName() {
		return getDisplayName();
	}
	
	 
	@Override
	public int getErrorCount() {
		return error_count;
	}

	@Override
	public void setErrorCount(int error_count) {
		this.error_count = error_count;
	}

	protected KbeeUser getSessionUser() {
		return (KbeeUser)ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	
	protected Domain getSessionDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}

	
	private ContentDao getContentDao() {
		 BeansService beans = ServiceLocator.getService(BeansService.class);
		 return (ContentDao) beans.getBean("contentDao");
    }

}
