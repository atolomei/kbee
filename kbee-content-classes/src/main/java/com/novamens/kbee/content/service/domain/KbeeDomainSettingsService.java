package com.novamens.kbee.content.service.domain;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


import com.novamens.content.base.ContentCreationException;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.dao.ContentDao;

import com.novamens.content.notes.Billboard;
import com.novamens.content.service.domain.DomainSettings;
import com.novamens.content.service.domain.DomainSettingsService;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.dom.Json;
import com.novamens.event.Event;
import com.novamens.event.EventListener;
import com.novamens.kbee.content.notes.KbeeBillboard;
import com.novamens.kbee.content.notification.BillboardCronJobServiceRequest;
import com.novamens.kbee.event.EvictCacheServiceEvent;
import com.novamens.kbee.json.KbeeJson;

import com.novamens.kbee.security.KbeeUser;
import com.novamens.lock.ValueLockerService;
import com.novamens.logging.DomainCreateEvent;
import com.novamens.logging.DomainUpdateEvent;
import com.novamens.logging.WorkNoteCreateEvent;
import com.novamens.logging.WorkNoteDeleteEvent;
import com.novamens.logging.WorkNoteUpdateEvent;
import com.novamens.scheduler.AbstractCronJobRequest;
import com.novamens.scheduler.CronSchedulerService;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.system.SystemParameter;





public class KbeeDomainSettingsService implements DomainSettingsService, EventListener {
			
    static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeDomainSettingsService.class.getName());

    
	// TODO: HA
	//
	static Map<String, Json> cache;
	static {
		cache = new ConcurrentHashMap<String, Json>();
	}
	
	/**
    The </b>TxLogger</b> is set up in Log4J to log synchronoulsy with the Thread.
    This is different from all the other logs that work asynchronously
	 */
	static private Logger txlogger = LogManager.getLogger("TxLogger");
			
	// private static com.novamens.logging.Logger logger = com.novamens.logging.Logger.getLogger(KbeeDomainSettingsService.class.getName());
	//
	// static private Logger trx_logger = LogManager.getLogger("TxLogger");
	
	private Domain domain = null;

	
	public KbeeDomainSettingsService() {
	}
	
	public KbeeDomainSettingsService(Domain domain) {
		this.domain=domain;
	}

	@Override
	@Transactional
	public void SetValues(String category, Json values) {
		DomainSettings settings = getContentDao().findDomainSettings(domain, category);
		if (settings==null) {
			String valueskey = domain.getId().toString()+"-"+category;
			try {
				ServiceLocator.getService(ValueLockerService.class).lock(valueskey);
				settings = getContentDao().findDomainSettings(domain, category);
				if (settings==null) {
					DomainSettings new_settings = new KbeeDomainSettings(domain);
					new_settings.setValues(values);
					new_settings.setCategory(category);
					cache.remove(domain.getId().toString()+"-"+category);
					getContentDao().save(new_settings);
					txlogger.info(new DomainUpdateEvent(domain, "Settings"));
				}
				else {
					SetValues(category, values);
				}
			}
			finally {
				ServiceLocator.getService(ValueLockerService.class).unlock(valueskey);
			}
		} 
		else {
			settings.setValues(values);
			cache.remove(domain.getId().toString()+"-"+category);
			getContentDao().save(settings);
			txlogger.info(new DomainUpdateEvent(domain, "Settings"));
		}
	}
	
	@Override
	@Transactional			
	public void SetValues(Json values, List<String> updatedParts) {
	
		DomainSettings settings = getContentDao().findDomainSettings(domain);
		if (settings==null) {
			DomainSettings new_settings = new KbeeDomainSettings(domain);
			new_settings.setValues(values);
			cache.remove(domain.getId().toString()+"-"+domain.getName());
			getContentDao().save(new_settings);
			txlogger.info(new DomainUpdateEvent(domain, updatedParts));
		} 
		else {
			// Se debe remover del Cache antes de grabar porque se
			// queja Hibernate que hay dos versiones que mapean
			// al mismo Hibernate object
			//
			cache.remove(domain.getId().toString()+"-"+domain.getName());
			settings.setValues(values);
			getContentDao().save(settings);
			txlogger.info(new DomainUpdateEvent(domain, updatedParts));
		}
	}

	
	@Override
	@Transactional
	public void SetValues(Json values) {
		
		DomainSettings settings = getContentDao().findDomainSettings(domain);
		if (settings==null) {
			DomainSettings new_settings = new KbeeDomainSettings(domain);
			new_settings.setValues(values);
			cache.remove(domain.getId().toString()+"-"+domain.getName());
			getContentDao().save(new_settings);
			txlogger.info(new DomainUpdateEvent(domain, "Settings"));
			
		} 
		else {

			/** Se debe remover del Cache antes de grabar porque se
			 	queja Hibernate que hay dos versiones que mapean
			 	al mismo Hibernate object
			*/
			cache.remove(domain.getId().toString()+"-"+domain.getName());
			settings.setValues(values);
			getContentDao().save(settings);
			txlogger.info(new DomainUpdateEvent(domain, "Settings"));
		}
		
	}

	@Override
	public Json getValues(String category) {
		DomainSettings settings = getContentDao().findDomainSettings(domain, category);
		if (settings!=null)
			return settings.getValues();
		return null;
	}
	
	@Transactional
	public DomainSettings create() {
		DomainSettings settings = getDefaultSettings();
		update(settings);
		List<String> list = new ArrayList<String>();
		list.add("new Domain Settings");
		txlogger.info(new DomainCreateEvent(domain, list));
		ServiceLocator.getService(UserService.class).evict();
		return settings;
	}
	
	@Override
	public Json getValues() {
		DomainSettings settings = getContentDao().findDomainSettings(domain, domain.getName());
		if (settings!=null)
			return settings.getValues();
		return null;
	}

	@Override
	@Transactional
	public void delete() {
		DomainSettings settings = getContentDao().findDomainSettings(domain);
		if (settings!=null) {
			getContentDao().delete(settings);
			cache.remove(domain.getId().toString()+"-"+domain.getName());
			
		}
	}

	@Override
	@Transactional
	public void delete(String category) {
		DomainSettings settings = getContentDao().findDomainSettings(domain, category);
		if (settings!=null) { 
			cache.remove(domain.getId().toString()+"-"+category);
			getContentDao().delete(settings);
		}
	}
	
	@Transactional
	public void update(DomainSettings settings) {
		if (settings.getCategory()!=null)
			cache.remove(domain.getId().toString()+"-"+settings.getCategory());
		else
			cache.remove(domain.getId().toString()+"-"+domain.getName());
		getContentDao().save(settings);
		txlogger.info(new DomainUpdateEvent(domain, "Settings"));
	}

	@Override
	public String get(String label) {
		return get(label, domain.getName());
	}

	public void evict() {
		cache.clear();
	}
	
	@Override
	public String get(String label, String category) {
		try {
		ServiceLocator.getService(ValueLockerService.class).lock(domain.getId().toString()+"-"+category);
		if (cache.containsKey(domain.getId().toString()+"-"+category)) {
			Json map = cache.get(domain.getId().toString()+"-"+category);
			if (map.get(label)!=null) {
				return (String) map.get(label);
			}
			else
				return null;
		} 
		else {
			Json json = getValues(category);
			if (json!=null){
				Json al = json;
				cache.put(domain.getId().toString()+"-"+category, al);
				if (cache.get(domain.getId().toString()+"-"+category).get(label)!=null)
					return (String) cache.get(domain.getId().toString()+"-"+category).get(label);
			}
		}
		}
		finally {
			ServiceLocator.getService(ValueLockerService.class).unlock(domain.getId().toString()+"-"+category);
		}
		return null;
	}

	
	
	private Domain getDomain() {
		return this.domain;
	}
	
	/**
	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}*/
	
	
	// Spring 
	//
	private ContentDao contentDao;
	public ContentDao getContentDao()							 	{		return contentDao;} // return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	public void setContentDao(ContentDao dao) 						{		contentDao=dao;}


	
	
	private DomainSettings getDefaultSettings() {
		
		DomainSettings settings = new KbeeDomainSettings(this.domain);
		
		Json json = settings.getValues();
		
		if (json==null)
			json = new KbeeJson();
		
		json.put(EMAIL_SERVICE_STATUS, "yes");
		json.put(CONSOLES_PERSISTS_LABELS, "no");
		json.put(EMAIL_SERVICE_NO_REPLY, "noreply@" + domain.getName());
		json.put(CONSOLES_SHOW_RESOURCES, "yes");
		json.put(CONSOLES_ENABLE_TEMPLATE, "yes");
										
		json.put(TIP_OF_THE_DAY, "yes");
		json.put(PORTAL, "no");
		json.put(RESTRICT_ACCOUNT_INFO_EDITION, "no");
		json.put(WORKFLOW_ENABLE_PENDING_TASKS, "yes");
		json.put(CONSOLES_TEMPLATE_NAME, "Templates");

		String nonw = getContentDao().findSystemParameterValueByKey("nonworkabledays", null);
		
		if (nonw!=null)
			json.put(CALENDAR_NON_WORKABLE_DAYS, nonw);
		
		return settings;
		
	}

	
	private BillboardCronJobServiceRequest getCronRequest(Billboard note) {
		
		List<AbstractCronJobRequest> list = ServiceLocator.getService(CronSchedulerService.class).getCronJobs(getDomain());

		//if (logger.isDebugEnabled()) {
		//	for (AbstractCronJobRequest r:list) 
		//		 logger.debug(r.toString());
		//	logger.debug(list.size());
		//}
		
		// --
		
		 for (AbstractCronJobRequest r:list) {
			 if (r instanceof BillboardCronJobServiceRequest) {
				 Serializable sid = ((BillboardCronJobServiceRequest) r).getBillboardId();
					 if (sid!=null) {
						 if (sid.equals(note.getId().toString()))
							 return (BillboardCronJobServiceRequest) r;
					 }
				 }
			 }
		 return null;
	}

	
	/**
	 * 
	 * 
	 * 
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void save(Billboard note) throws ContentMgmtException {
		
		note.setLastModifiedUser(getSessionUser());
		note.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		getContentDao().save(note);
		
		boolean is_silent = false;
		
		BillboardCronJobServiceRequest re = getCronRequest(note);
		
		if (note.getCronExpression()!=null) {
				if (re!=null)
					ServiceLocator.getService(CronSchedulerService.class).deleteCronJob(re);
				BillboardCronJobServiceRequest nreq = new BillboardCronJobServiceRequest(note);
				ServiceLocator.getService(CronSchedulerService.class).saveCronJob(nreq);
			is_silent = true;
			
		} else {
			if (re!=null)
				ServiceLocator.getService(CronSchedulerService.class).deleteCronJob(re);
			is_silent = false;
		}
		txlogger.info(new WorkNoteUpdateEvent(note, is_silent));
	}
	
	/**
	 *  Billboard [CronExpression]  == null -> Event(is_silent=false) se enviara una vez desde aca
	 *  Billboard [CronExpression]  != null -> Event(is_silent=true) se enviara desde los cron request 
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void update(Billboard note) throws ContentMgmtException {
		note.setLastModifiedUser(getSessionUser());
		note.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		getContentDao().update(note);
		boolean is_silent = false;
		BillboardCronJobServiceRequest re = getCronRequest(note);
		if (note.getCronExpression()!=null) {
			if (re!=null)
				ServiceLocator.getService(CronSchedulerService.class).deleteCronJob(re);
			BillboardCronJobServiceRequest nreq= new BillboardCronJobServiceRequest(note);
			ServiceLocator.getService(CronSchedulerService.class).saveCronJob(nreq);
			is_silent = true;
		} else {
			if (re!=null)
				ServiceLocator.getService(CronSchedulerService.class).deleteCronJob(re);
			is_silent = false;
		}
		txlogger.info(new WorkNoteUpdateEvent(note, is_silent));
	}

	/**
	 * Billboard [CronExpression]  == null -> Event(is_silent=false) se enviara una vez desde aca
	 * Billboard [CronExpression]  != null -> Event(is_silent=true) se enviara desde los cron request 
	 */

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void remove(Billboard note) throws ContentMgmtException {
		
		if (note.getCronExpression()!=null) {
			BillboardCronJobServiceRequest re = getCronRequest(note);
			if (re!=null)
				ServiceLocator.getService(CronSchedulerService.class).deleteCronJob(re);
		}
		getContentDao().delete(note);
		txlogger.info(new WorkNoteDeleteEvent(note));
	}


	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public Billboard createBillboard() throws ContentCreationException, ContentMgmtException {
		String title= String.valueOf(OffsetDateTime.now().getDayOfMonth())+" " + OffsetDateTime.now().getMonth().getDisplayName(TextStyle.SHORT, getSessionUser().getLocale())+ " " +String.valueOf(OffsetDateTime.now().getYear());
		return createBillboard(title, null, true);
	}
	
	/**
	 *  Billboard [CronExpression]  == null -> Event(is_silent=false) se enviara una vez desde aca
	 *  Billboard [CronExpression]  != null -> Event(is_silent=true) se enviara desde los cron request 
	 */

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public Billboard createBillboard(String title, String text, boolean isAlert) throws ContentCreationException, ContentMgmtException {
		
		KbeeBillboard note = new KbeeBillboard(getSessionUser());
		note.setTitle(title);
		note.setAlert(isAlert);
		note.setBillboard(!isAlert);
		note.setCronExpressionStr(null);
		
		if (!isAlert) {
			note.setEmail(false);
			ZonedDateTime zdt = ZonedDateTime.now(ZoneId.of(getDomain().getTimeZone())).truncatedTo(ChronoUnit.DAYS);
			note.setStartpub(zdt.toOffsetDateTime());
		}
			
		if (text!=null)
			note.setText(text);
		
		boolean is_silent = (note.getCronExpression()!=null);
		
		getContentDao().save(note);
		txlogger.info(new WorkNoteCreateEvent(note, is_silent));
		return note;
	}

	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public Billboard createWelcomeBillboard() throws  ContentCreationException, ContentMgmtException {
		
		Locale locale = getSessionUser()!=null? getSessionUser().getLocale(): Locale.getDefault();
		String lang=locale.getLanguage();
		ResourceBundle res = ResourceBundle.getBundle(KbeeDomainSettingsService.this.getClass().getName(), locale);
		
		SystemParameter title_p = getContentDao().findSystemParameterByKey("welcome-work-note.title."+lang);
		String default_title  =  res.getString("title"); // "What are Work Notes ?"
		String title = title_p!=null? title_p.getValue():default_title;
		
		String default_text  =  res.getString("text"); // "<p>Work Notes is a web page where you will find relevant notes from the Company for our work.</p>"
		SystemParameter text_p = getContentDao().findSystemParameterByKey("welcome-work-note.text."+lang);
		String text = text_p!=null?text_p.getValue():default_text;
		
		return createBillboard(title, text, true);
		
	}

	@Override
	public List<Billboard> getBillboards() {
		return getContentDao().getBillboards(getDomain());
	}

	
	@Override
	public boolean listen(Event event) {
		if (event instanceof EvictCacheServiceEvent)
			return true;
		return false;
	}

	@Override
	public void onEvent(Event event) {
			if (event instanceof EvictCacheServiceEvent)
				this.evict();
	}
	
	protected KbeeUser getSessionUser() {
		User session_user = ServiceLocator.getService(SecurityService.class).getSessionUser();
		return (KbeeUser) session_user;
	}

	@Override
	public Billboard createRegularAlert(String title, String text) throws ContentCreationException, ContentMgmtException {
		return  createBillboard(title, text, true);
	}
	
	@Override
	public Billboard createBillboard(String title, String text) throws ContentCreationException, ContentMgmtException {
		return  createBillboard(title, text, false);
	}
	

	
}
