package kbee.email;



import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.ResourceBundle;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reflections.Reflections;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;

import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.novamens.beans.BeansService;

import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.email.EmailTemplate;
import com.novamens.content.entity.Person;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.Classifier;
import com.novamens.content.service.ContentFactoryService;

import com.novamens.dom.Domain;
import com.novamens.email.EmailBuilder;
import com.novamens.email.EmailData;
import com.novamens.email.EmailService;

import com.novamens.kbee.content.email.KbeeEmailTemplate;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.logging.EmailTemplateUpdateEvent;
import com.novamens.scheduler.SchedulerException;
import com.novamens.scheduler.SchedulerService;
import com.novamens.service.ApplicationServerService;
import com.novamens.service.BrandingService;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

import com.novamens.util.KbeeRuntimeException;

import kbee.util.FSUtils;


/**
 * 
 *  1. add insert into  kb_email_template in liquibase
 *  2. create new subclass of EmailBuilder
 *  3. add to KbeeEmailService 
 *  
 *  TABLE -> kb_email_template
 *  
 *  	insert into scheduler 
				( ID, 
				  REQUEST, 
				  TIME, 
				  PRIORITY, 
				  ERROR_COUNT, 
				  ERROR_MESSAGE, 
				  DESCRIPTION, 
				  TITLE, 
				  OBJECTID, 
				  EXECUTE_AFTER, 
				  COMMAND_CLASS_NAME, 
				  COMMAND_PARAMETERS, 
				  HOSTNAME, 
				  APPSERVERID) 
				
				VALUES
				 
				(
				(select nextval('domainid_sequence')), 
				null, 
				now(), 
				1, 
				0,
				null, 
				'check and add email templates', 
				'check and add email templates', 
				null, 
				null, 
				'com.novamens.kbee.email.CreateEmailTemplatesCommand', 
				'', 
				null, 
				'universal');
 */

public class KbeeEmailService  implements EmailService {

	static final int LIMIT		= 30000;
	static final double MAX		= (double) LIMIT * 0.6;

	static final int HTML = 1;
	static final int JSON = 2;
	 
	static final int TTL_SECS 	= 60 * 20; // 20 minutes
	
	static final String EN = "en";
	static final String ES = "es";

	 
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeEmailService.class.getName());

	private static kbee.util.logging.Logger emaillogger = kbee.util.logging.Logger.getLogger("email");
	
 	// Logger synchronous with the TRX	*/
	static private Logger txlogger = LogManager.getLogger("TxLogger");
 
	private Map<String, Map<String, EmailTemplate>> lang_default_templates;
	private String _default_noreply = null;
//	private Map<String, Object> default_macros_map = null;	
	private Domain KBEE = null;
	private ContentDao dao;
 	
	private List<Class<? extends com.novamens.email.EmailBuilder>> builder_classes = null;
	private Map<String, Class<? extends com.novamens.email.EmailBuilder>>  key_classes = null;	
	
	
	public KbeeEmailService() {
	}

 	@Override
	@Transactional(propagation = Propagation.REQUIRED) 
	public void send(EmailBuilder builder) throws ContentMgmtException {
 		logger.debug("send -> " + builder.getClass().getName());
 		if (isSendEmail(builder)) {
			EmailData data = builder.build();
			send(data, builder.getDomain());
		}
	}
	
 	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void sendEmail(Person receiver, EmailData data) {
		send(data, receiver.getDomain());
	}
	
	/**
	 * <p>THis method can be used when the DB TRX includes other actions and we want the email to be delivered only
	 * when all the actions were atomically completed.</p>
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void send(EmailData emaildata, Domain domain) {
		try {

			if (emaildata==null) {
				logger.debug("'emaildata' is null");
				return;
			}
			
			if (emaildata.to==null) {
				logger.debug("'to' is null");
				return;
			}
			
			logger.debug(emaildata);
			emaillogger.debug("send -> "+ emaildata);
			
			ServiceLocator.getService(SchedulerService.class).enqueue(new EmailSendServiceRequest(emaildata, domain));
			
		} catch (SchedulerException e) {
		     logger.error(e);
		}
	}

 	@Override
	@Transactional(propagation = Propagation.REQUIRED) 
	public void save(EmailTemplate template, List<String> parts) throws ContentMgmtException {
		template.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		template.setLastModifiedUser(getSessionUser());
		getContentDao().save(template);
		emaillogger.debug("Save -> " + template.toString());
		txlogger.info(new EmailTemplateUpdateEvent(template, parts));
	}

	@Override
	public boolean hasEmailTemplate(Domain domain) {
		return getContentDao().hasEmailTemplates(domain);
	}

/**
 *  Domain lang
 *  Domain en
 *  KBEE lang
 *  KBEE en
 *  
 *  "default" 
 */
	@Override
	public EmailTemplate getEmailTemplate(Domain domain, String s_language, String key) {

		String language=s_language;
		
		if (language==null)											
			language = EN;
		
		else if (language.equals(ES) || language.equals("spa"))		
			language = ES;

		EmailTemplate tm = getContentDao().findEmailTemplate(domain, language, key);
		
		if (tm!=null)
			return tm;
		
		tm = getDefaultTemplates(language).get(key);
		
		if (tm!=null)
			return tm;
		
		tm = getDefaultTemplates(ES).get(key);
		
		if (tm!=null)
			return tm;

		tm = getDefaultTemplates(EN).get(key);
		
		if (tm!=null)
			return tm;
		
		
		tm = getContentDao().findEmailTemplate(getDomainKbee(), language, key);
		
		if (tm!=null)
			return tm;
		

		tm = getContentDao().findEmailTemplate(getDomainKbee(), ES, key);

		if (tm!=null)
			return tm;
		
		tm = getContentDao().findEmailTemplate(getDomainKbee(), EN, key);
		
		if (tm!=null)
			return tm;
		
		logger.error("Can not find a Email Template for " + (key!=null?key:"null") + " " + language);
		emaillogger.error("Can not find a Email Template for " + (key!=null?key:"null") + " " + language);
		 
		tm = getDefaultTemplates(EN).get("default");
		
		if (tm==null) 
			throw new KbeeRuntimeException("getDefaultTemplates(\"en\").get(\"default\") is null");
		
		return tm;
	}

	@Override
	public String getNoReplyEmailAddress() {
		if (_default_noreply!=null)
			return _default_noreply;
		synchronized (this) {		
			_default_noreply = ServiceLocator.getService(BrandingService.class).getNoReplyEmailAddress();
			emaillogger.debug(_default_noreply);
		}
		return _default_noreply;
	}

	
	@Override
	public Map<String, String> getTemplateMacros(Domain domain) {
			return generateTemplateMacros(domain);

	}
	
	@Override
	public void  setUpTemplates(Domain domain) {
		
		
		List<EmailTemplate> list = getContentDao().getEmailTemplates(domain);
		
		List<EmailTemplate> result = new ArrayList<EmailTemplate>();
		
		Map<String, EmailTemplate> t_map = new HashMap<String, EmailTemplate>();

		for (EmailTemplate em: list)
			t_map.put(em.getKey()+"-"+em.getLanguage(), em);
		
		Map<String, EmailTemplate> map_en = this.getDefaultTemplates(EN);
		for (Entry<String, EmailTemplate> entry: map_en.entrySet()) {
			String key =entry.getKey()+"-" + EN; 
			if (!t_map.containsKey(key)) {
					result.add(entry.getValue());
			}
		}
		
		Map<String, EmailTemplate> map_es = this.getDefaultTemplates(ES);
		for (Entry<String, EmailTemplate> entry: map_es.entrySet()) {
			String key = entry.getKey()+"-" + ES; 
			if (!t_map.containsKey(key)) {
					result.add(entry.getValue());
			}
		}
		
		int total = 0;
		for (EmailTemplate et:  result) {
			try {
				ServiceLocator.getService(ContentFactoryService.class).createEmailTemplate(
						domain,
						et.getKey(),
						et.getTitle(), 
						et.getLanguage(), 
						et.getFrom(), 
						null /*et.getSubject()*/, 
						null /*et.getStringTemplate()*/, 
						true,
						null /*et.getStrModel()*/);
				
				total++;
			} catch (Exception e) {
				logger.error(e);
			}
		}

		logger.debug("------------------------------------------------------------------- ");
		logger.debug("Added " + String.valueOf(total));
		logger.debug("------------------------------------------------------------------- ");
	}
	

	@Override
	public Map<String, Class<? extends com.novamens.email.EmailBuilder>> getEmailBuilderKeyClassMap() {
		
		if (key_classes!=null)
			return key_classes;
		
		this.key_classes = new HashMap<String, Class<? extends com.novamens.email.EmailBuilder>> ();
		
		List<Class<? extends com.novamens.email.EmailBuilder>> list = getAllEmailBuilderClasses();
		
		for (Class<? extends com.novamens.email.EmailBuilder> c: list) {
			
			try {
				com.novamens.email.EmailBuilder i = c.newInstance();
				this.key_classes.put( i.getKey(),c);
			} catch (InstantiationException | IllegalAccessException e) {
					logger.error(e);
			}
		}
		return this.key_classes;
	}
	
	/**
	 * 
	 */
	@Override
	public List<Class<? extends com.novamens.email.EmailBuilder>> getAllEmailBuilderClasses() {
		
		 if (builder_classes!=null) 
			 return builder_classes;

		 
		builder_classes = new ArrayList<Class<? extends com.novamens.email.EmailBuilder>>();
			
		try {	
			
			String name="kbee.email";
			Reflections reflections = new Reflections(name);
			Set<Class<? extends EmailBuilder>> classes = reflections.getSubTypesOf(EmailBuilder.class);
			//logger.debug(classes.toString());
			classes.forEach(item -> 
			{
				if (!Modifier.isAbstract( item.getModifiers()))
					builder_classes.add(item);
			}
				);
	
			builder_classes.sort(new Comparator<Class<? extends com.novamens.email.EmailBuilder>>() {
				@Override
				public int compare(Class<? extends com.novamens.email.EmailBuilder> o1, Class<? extends com.novamens.email.EmailBuilder> o2) {
					return o1.getSimpleName().compareToIgnoreCase(o2.getSimpleName());
				}
				
			});
			} catch (Exception e) {
				logger.error(e);
			}

		return builder_classes;

	}
	
	
	 private void addDefaultTemplateEn(	Map<String, EmailTemplate> lang_en_default_templates) {
		 addDefaultTemplate (lang_en_default_templates, EN);
 	 }

	 
	 private void addDefaultTemplateSpa(	Map<String, EmailTemplate> lang_es_default_templates) {
		 addDefaultTemplate (lang_es_default_templates, ES);
	 }
	 
	 

	 
	 
	 /**
	  * @param lang_es_default_templates
	  */
	 private void addDefaultTemplate(	Map<String, EmailTemplate> map, String lang) {

		 
		    map.put(EmailTemplate.DB_EXPORT,	new KbeeEmailTemplate 		(EmailTemplate.DB_EXPORT,  		getLabel(EmailTemplate.DB_EXPORT, lang), 			lang, getLabel(EmailTemplate.DB_EXPORT+"-subject", lang) ));

		    
			//lang_es_default_templates.put("db-export", 								new KbeeEmailTemplate     ("db-export", 		 			"${from}",  			"DB Export",          							ES,   
		    //"Exportación de la Base for ${domain-name} - ${application-name}"	, 
		    // "", null));

			
			
		 
		   /*"default"*/																																																		
		 	map.put("default", 									new KbeeEmailTemplate     	("default",                           		       getLabel( "default", lang),        			                        lang, getLabel("default-subject", lang)));
		 																																															
		    /*"Tarea - reasignada usuario anterior"
		     * "${content.title} - ${task-displayname} - Reasignación - ${application-name}"
		     * */
		    map.put(EmailTemplate.TASK_REASSIGN_FORMER_OWNER,	new KbeeEmailTemplate 		(EmailTemplate.TASK_REASSIGN_FORMER_OWNER,  		getLabel(EmailTemplate.TASK_REASSIGN_FORMER_OWNER, lang), 			lang, getLabel(EmailTemplate.TASK_REASSIGN_FORMER_OWNER+"-subject", lang) ));

		 	//"Tarea - asignada"
		    // "${content.title} - ${activity.task.displayname} - ${application-name}"
		 	map.put(EmailTemplate.TASK_ASSIGN, 					new KbeeEmailTemplate     	(EmailTemplate.TASK_ASSIGN,  						getLabel(EmailTemplate.TASK_ASSIGN, lang),  						lang, getLabel(EmailTemplate.TASK_ASSIGN+"-subject", lang) ));
		 	
		 	//"Tarea - Nota de Avance de Tarea"
		 	// "Nota - ${content.title} - ${activity.task.displayname} - ${application-name}"
		 	map.put(EmailTemplate.TASK_PROGRESS_NOTE, 			new KbeeEmailTemplate     	(EmailTemplate.TASK_PROGRESS_NOTE,	        		getLabel(EmailTemplate.TASK_PROGRESS_NOTE, lang) , 			    	lang, getLabel(EmailTemplate.TASK_PROGRESS_NOTE+"-subject", lang) ));
		 	
		 	//"Tarea - Pendiente"
		 	// "${content.title} - Pending - ${application-name}"
		 	map.put(EmailTemplate.TASK_PENDING,					new KbeeEmailTemplate     	(EmailTemplate.TASK_PENDING,   						getLabel(EmailTemplate.TASK_PENDING, lang), 						lang, getLabel(EmailTemplate.TASK_PENDING+"-subject", lang) 	));
		 	
		 	// "Tarea - Expirada"
		 	// "${content.title} - Tarea expirada - ${application-name}"
		 	map.put(EmailTemplate.TASK_TIMEOUT, 				new KbeeEmailTemplate 		(EmailTemplate.TASK_TIMEOUT, 						getLabel(EmailTemplate.TASK_TIMEOUT, lang),				   			lang, getLabel(EmailTemplate.TASK_TIMEOUT+"-subject", lang)  	));
			
		 	//"Contenido - Enviar por correo electrónico"
		 	// "${content.title} - ${application-name}"
		 	map.put(EmailTemplate.SEND_EMAIL, 					new KbeeEmailTemplate     	(EmailTemplate.SEND_EMAIL,  						getLabel(EmailTemplate.SEND_EMAIL, lang),							lang, getLabel(EmailTemplate.SEND_EMAIL+"-subject", lang) 	));

		 	// "Seguridad - Bienvenido"
		 	//"Bienvenido a ${application-fullname} – Información para ingresar"  )
		 	map.put(EmailTemplate.WELCOME,						new KbeeEmailTemplate     	(EmailTemplate.WELCOME,  							getLabel(EmailTemplate.WELCOME, lang) , 				  			lang, getLabel(EmailTemplate.WELCOME+"-subject", lang) ));
		 	
		 	// "Seguridad - Olvidé mi contraseña"
		 	// "Ingreso de nueva contraseña - ${application-name}"
		 	map.put(EmailTemplate.FORGOT_PASSWORD, 				new KbeeEmailTemplate     	(EmailTemplate.FORGOT_PASSWORD,  	 				getLabel(EmailTemplate.FORGOT_PASSWORD, lang) ,	 	  				lang, getLabel(EmailTemplate.FORGOT_PASSWORD+"-subject", lang) ));
		 	

		 	// "Seguridad - Olvidé mi contraseña"
		 	// "Ingreso de nueva contraseña - ${application-name}"
		 	map.put(EmailTemplate.FORGOT_USERNAME, 				new KbeeEmailTemplate     	(EmailTemplate.FORGOT_USERNAME,  	 				getLabel(EmailTemplate.FORGOT_USERNAME, lang) ,	 	  				lang, getLabel(EmailTemplate.FORGOT_USERNAME + "-subject", lang) ));

		 	// "Seguridad - Ingresar nueva Contraseña - Admin"
		 	// "Requerimiento de nueva contraseña - ${application-name}"
		 	map.put(EmailTemplate.ADMIN_SEND_PASSWORD_RESET,	new KbeeEmailTemplate 		(EmailTemplate.ADMIN_SEND_PASSWORD_RESET,   		getLabel(EmailTemplate.ADMIN_SEND_PASSWORD_RESET, lang) , 			lang, getLabel(EmailTemplate.ADMIN_SEND_PASSWORD_RESET+"-subject", lang) ));
		 	
		 	// "Seguridad - Registrar dispositivo Teléfono o Tablet"
		 	// "${application-name} - Registro de Dispositivo"
		 	map.put(EmailTemplate.REGISTER_DEVICE, 				new KbeeEmailTemplate	  	(EmailTemplate.REGISTER_DEVICE, 					getLabel(EmailTemplate.REGISTER_DEVICE, lang) ,      				lang, getLabel(EmailTemplate.REGISTER_DEVICE+"-subject", lang) ));
			
			// "Reglas - Publicación de Contenido"
		 	// "${content.title}  - Publicación - ${application-name}"
		 	map.put(EmailTemplate.PUBLISH_EMAIL_TEMPLATE, 		new KbeeEmailTemplate 		(EmailTemplate.PUBLISH_EMAIL_TEMPLATE, 	 			getLabel(EmailTemplate.PUBLISH_EMAIL_TEMPLATE, lang),				lang, getLabel(EmailTemplate.REGISTER_DEVICE+"-subject", lang) ));
		 	
		 	// "Fabrica - Nuevo Dominio"
		 	// "Nuevo Dominio ${domain-name} - ${application-name}"
		 	map.put(EmailTemplate.NEW_DOMAIN,		  			new KbeeEmailTemplate 		(EmailTemplate.NEW_DOMAIN, 							getLabel(EmailTemplate.NEW_DOMAIN, lang) ,							lang, getLabel(EmailTemplate.NEW_DOMAIN+"-subject", lang) ));
		 	
		 	// "${application-name} - Envío de Token de Seguridad"
		 	// "${application-name}"
		 	map.put(EmailTemplate.SEND_TOKEN, 					new KbeeEmailTemplate     	(EmailTemplate.SEND_TOKEN, 				   			getLabel(EmailTemplate.SEND_TOKEN, lang) ,							lang, getLabel(EmailTemplate.SEND_TOKEN+"-subject", lang) 	));


		 						
		 	map.put(EmailTemplate.TASK_DUE_DATE_NOTIFICATION,	new KbeeEmailTemplate     	(EmailTemplate.TASK_DUE_DATE_NOTIFICATION, 	  			getLabel(EmailTemplate.SEND_TOKEN, lang) ,							lang, getLabel(EmailTemplate.TASK_DUE_DATE_NOTIFICATION+"-subject", lang) 	));

		 	
		 	
			/**
			 * 
			lang_es_default_templates.put("alert-rule-publish", 					new KbeeEmailTemplate     ("alert-rule-publish", 				"${from}",         	  	"Rule Publish",         			ES,   "${file-title} - Publicación - ${application-name}"						, "<p>${person-displayname}publicó: ${file-title}<br/> ${file-attributes}	</p><p>Puede ver el contenido en: ${file-library-url}<br/>o ingresar a la Biblioteca en: ${library-url} por más información.</p>", null));
			lang_es_default_templates.put("alert-rule-publish-user", 				new KbeeEmailTemplate 	  ("alert-rule-publish-user",			"${from}",         	  	"Rule Publish (User)",     			ES,   "${file-title} - Published - ${application-name}"			, "<p>${person-displayname} has published: ${file-title}<br/> ${file-attributes}	</p><p>View the file here: ${file-library-url}<br/>or go to Library at ${library-url} to retrieve additional information.</p>", null));
			lang_es_default_templates.put("alert-rule-publish-domain", 				new KbeeEmailTemplate     ("alert-rule-publish-domain",			"${from}",         	  	"Rule Publish (Domain)",   						ES,   "${file-title} - Published - ${application-name}"			, "<p>${person-displayname} has published: ${file-title}<br/> ${file-attributes}	</p><p>View the file here: ${file-library-url}<br/>or go to Library at ${library-url} to retrieve additional information.</p>", null));
			lang_es_default_templates.put("alert-rule-publish-requires-accept",		new KbeeEmailTemplate     ("alert-rule-publish-requires-accept", "${from}",         	  	"Rule Publish (Requires Accept)", 			ES,   "${file-title} - Published - ${application-name}"			, "<p>${person-displayname} has published: ${file-title}<br/> ${file-attributes}	</p><p>View the file here: ${file-library-url}<br/>or go to Library at ${library-url} to retrieve additional information.</p>", null));
			lang_es_default_templates.put("user-defined-alert",						new KbeeEmailTemplate      ("user-defined-alert", 			"${from}", 			 	"Work Note",									ES,	"${alert-title} - ${application-name}"				                , "${alert-text}"     , null));
			lang_es_default_templates.put("report_subscription",					new KbeeEmailTemplate	  ("report_subscription",				"${from}",    		"Report Subscription",           		        ES,   "${report-schedule-name}"									, "<p>${person-displayname}</p><p>${report-schedule-description}</p> <p>Puede desuscribrse del reporte en esta página: ${report-subscription-url} </p>"				+ "", null));	
			lang_es_default_templates.put("workflow-notification",					new KbeeEmailTemplate	  ("workflow-notification",				"${from}",			"Workflow Notification",						ES,	"${file-title}"												, "<p>${person-displayname},</p>,"				+ "<p>${report-schedule-description}</p> "				+ "<p></p>"				+ "", null));
			lang_es_default_templates.put("notification-by-action-rule",			new KbeeEmailTemplate	  ("notification-by-action-rule",		"${from}", 			"Regla dependiente del tiempo", 			    ES,   "${subtitle}"	 			, "<p>${action-rule-subtitle}</p><p>${action-rule-text}</p> <p>${title} <br /> ${file-attributes}  <br /> ${file-library-url}</p> <p>Alert generated by Rule: <br /> ${action-rule-rule-name} - ${rule-source} - Created by: ${rule-modified-by} - ${rule-lastmodified}</p>", null));
			lang_es_default_templates.put("content-home-searcher-portal-url",		new KbeeEmailTemplate	  ("content-home-searcher-portal-url",	"${from}", 			"Searcher Portal URL",  	      		        ES,   "${content-home-searcher-home-portal}"				, "<p>${person-displayname},</p>,<p>${report-schedule-description}</p><p>You can unsubscribe to this email report on this page: ${report-subscription-url} </p>", null));
			lang_es_default_templates.put("support-ticket-submitter",				new KbeeEmailTemplate	  ("support-ticket-submitter",			"${from}",			"Support Ticket Submitter",	ES,	"${support-ticket-subject}"									, "<p>${support-ticket-text}</p>"				+ "<p>${support-ticket-text}</p> "	+ "<p></p>"	+ "", null));
			lang_es_default_templates.put("support-ticket-receiver",				new KbeeEmailTemplate	  ("support-ticket-receiver",			"${from}",			"Support Ticket Receiverr",	ES,	"${support-ticket-subject}"									, "<p>${support-ticket-text}<br/><br/>${support-ticket-context}</p>"				+ "<p>${support-ticket-text}<br/><br/>${support-ticket-context}</p> "	+ "<p></p>"	+ "", null));
			
																																		
			lang_es_default_templates.put("forgot-username",						new KbeeEmailTemplate     ("forgot-username",  	 			"${from}",   			"Forgot Username", 								ES,  
			 "Olvidé mi usuario - ${application-name}"							, "<p>${person-displayname},</p><p>Recibimos su pedido de enviarle su usuario de ${application-name}. <br/>Tenemos los siguientes usuarios asociados a su correo electrónico y teléfono:</p><p>Email: ${person-email-address}<br/>Phone: ${person-phone-last-four-digits}</p><p>Cuenta de usuario: ${user-username}</p>", null));
			
			
			// lang_en_default_templates.put("welcome_basic", 							new KbeeEmailTemplate     ("welcome_basic",		 		    "${from}",				"Welcome Basic",			  					"en",	"Welcome to ${application-fullname} – Login Information", "<p>${person-displayname},</p><p>Welcome to <b>${application-fullname}</b>.</p>. You now have access to ${application-name} where you will be able to access your company's Enterprise files from a centralized location. ${application-name} allows you to search for specific files and provide restricted access by user, so you can give 'View Only' access to owners, investors and auditors. Never lose a document again with ${application-name}’s superior search functionality that will help you find any document available in seconds. </p><p><b>Url is</b>: ${domain-url}</p><p><b>Your username is</b>: ${username}</p><p>	To set up your account password, please visit the link below and follow	the instructions<br /> <b>Password reset</b>: ${password-url}</p> <p>To view training on this product, please visit the link below:<br /> <br />${training-url}</p>", null));
			// lang_en_default_templates.put("welcome_p  ium", 						new KbeeEmailTemplate     ("welcome_premium",     			"${from}",  			"Welcome Premium",  				  			"en",	"Welcome to ${application-fullname} – Login Information", "<p>${person-displayname},</p><p>Welcome to <b>${application-fullname}</b>.</p>. ${application-name} is an enterprise document management solution designed to meet the enterprise document management needs of any company. ${application-name} allows you store documents for all areas of your business while having the ability to restrict access by user so you can give view only access to investors and auditors or control access by document type. Never lose a document again, ${application-name} superior search functionality will help you find in seconds any document ever stored.</p><p>Your username is: ${username}</p><p>To set up your account password, please visit the link below and follow the instructions:</p><p>${domain-url}</p><p>To view training on this product, please visit the link below:<br /><br /> ${training-url} ", null));
			// lang_en_default_templates.put("welcome_compliance", 					new KbeeEmailTemplate     ("welcome_compliance",    		"${from}",  			"Welcome Compliance",				  			"en",	"Welcome to ${application-fullname} – Login Information", "<p>${person-displayname},</p><p>Welcome to <b>${application-fullname}</b>.</p>. ${application-name} is an enterprise document management solution designed to meet the enterprise document management needs of any company. ${application-name} allows you store documents for all areas of your business while having the ability to restrict access by user so you can give view only access to investors and auditors or control access by document type. Never lose a document again, ${application-name} superior search functionality will help you find in seconds any document ever stored.</p><p>Your username is: ${username}</p><p>To set up your account password, please visit the link below and follow the instructions:</p><p>${domain-url}</p><p>To view training on this product, please visit the link below:<br /><br /> ${training-url} ", null));
			// lang_en_default_templates.put("welcome_standard", 						new KbeeEmailTemplate     ("welcome_standard",     	 		"${from}",  			"Welcome Standard", 				  			"en",	"Welcome to ${application-fullname} – Login Information", "<p>${person-displayname},</p><p>Welcome to <b>${application-fullname}</b>.</p>. ${application-name} is an enterprise document management solution designed to meet the enterprise document management needs of any company. ${application-name} allows you store documents for all areas of your business while having the ability to restrict access by user so you can give view only access to investors and auditors or control access by document type. Never lose a document again, ${application-name} superior search functionality will help you find in seconds any document ever stored.</p><p>Your username is: ${username}</p><p>To set up your account password, please visit the link below and follow the instructions:</p><p>${domain-url}</p><p>To view training on this product, please visit the link below:<br /><br /> ${training-url} ", null));
			// lang_es_default_templates.put("welcome_basic", 							new KbeeEmailTemplate     ("welcome_basic",		 		    "${from}",				"Welcome Basic",			  					ES,	"Bienvenido a ${application-fullname} – Información para ingresar", "<p>${person-displayname},</p><p>Bienvenido a <b>${application-fullname}</b>.</p>. ${application-name}  ", "${application-name} es un servicio de gestión documental para empresas de cualquier tamaño. puede Ejecutar procesos de carga y publicación, organizar, buscar y definir permisos de acceso flexibles.<p>YSu usuario es: <b>${username}</b></p><p>Puede ingresar su contraseña en este enlace:</p><p>${domain-url}</p>" ));
			// lang_es_default_templates.put("welcome_premium", 						new KbeeEmailTemplate     ("welcome_premium",     			"${from}",  			"Welcome Premium",  				  			ES,	"Bienvenido a ${application-fullname} – Información para ingresar", "<p>${person-displayname},</p><p>Bienvenido a <b>${application-fullname}</b>.</p>. ${application-name}  ", "${application-name} es un servicio de gestión documental para empresas de cualquier tamaño. puede Ejecutar procesos de carga y publicación, organizar, buscar y definir permisos de acceso flexibles.<p>YSu usuario es: <b>${username}</b></p><p>Puede ingresar su contraseña en este enlace:</p><p>${domain-url}</p>" ));
			// lang_es_default_templates.put("welcome_compliance", 					new KbeeEmailTemplate     ("welcome_compliance",    		"${from}",  			"Welcome Compliance",				  			"sn",	"Welcome to ${application-fullname} – Login Information", "<p>${person-displayname},</p><p>Welcome to <b>${application-fullname}</b>.</p>. ${application-name} is an enterprise document management solution designed to meet the enterprise document management needs of any company. ${application-name} allows you store documents for all areas of your business while having the ability to restrict access by user so you can give view only access to investors and auditors or control access by document type. Never lose a document again, ${application-name} superior search functionality will help you find in seconds any document ever stored.</p><p>Your username is: ${username}</p><p>To set up your account password, please visit the link below and follow the instructions:</p><p>${domain-url}</p><p>To view training on this product, please visit the link below:<br /><br /> ${training-url} ", null));
			// lang_en_default_templates.put("reassign-task-receiver", 				new KbeeEmailTemplate     ("reassign-task-receiver", 		"${from}",      "Task reassigned",   		      			"en",   "${file-title} - ${task-displayname} - Reassigned - ${application-name}"	, "<p>{from-displayname} has assigned the following task: ${task-displayname} - ${file-title}.<br/>Please go to My Tasks at ${mytasks-url} to retrieve additional information about this file.</p><p>Comment:<br/>${comment}</p>", null));
			// lang_es_default_templates.put("reassign-task-receiver", 				new KbeeEmailTemplate     ("reassign-task-receiver", 			"${from}",        	"Task reassigned",   		      				ES,   "${file-title} - ${task-displayname} - Reasignación - ${application-name}"		, "<p>{from-displayname} asignó: ${task-displayname} - ${file-title}.<br/><p>por favor ingrese ${mytasks-url} para mayor información.</p><p>Comentario:<br/>${comment}</p>", null));
			**/
	 }
	 
	 
	@Override
	public Map<String, EmailTemplate> getDefaultTemplates(String language) {
		
		
		if (lang_default_templates != null ) {
			if (lang_default_templates.containsKey(language))
					return this.lang_default_templates.get(language);
			return this.lang_default_templates.get(EN);
		} 


		synchronized (this) {
			
			this.lang_default_templates = new HashMap<String, Map<String, EmailTemplate>>();
			
			Map<String, EmailTemplate> lang_en_default_templates = new HashMap<String, EmailTemplate>();
			Map<String, EmailTemplate> lang_es_default_templates = new HashMap<String, EmailTemplate>();
			
			this.lang_default_templates.put(EN, lang_en_default_templates);
			this.lang_default_templates.put(ES, lang_es_default_templates);

			addDefaultTemplateSpa(lang_es_default_templates);
			addDefaultTemplateEn(lang_en_default_templates);
			
			
			if (lang_es_default_templates.size()!=lang_en_default_templates.size()) {
				
				logger.error("Default templates do not match EN ("+ String.valueOf(lang_en_default_templates.size() ) + ") / ES (" + String.valueOf(lang_es_default_templates.size() ) +	")" );
			
				for (Entry<String, EmailTemplate> entry :lang_en_default_templates.entrySet()) 
					if ( !lang_es_default_templates.containsKey(entry.getKey()))
						logger.error(entry.getKey() + "- [en] -> " + " not in [es]");
				
				for (Entry<String, EmailTemplate> entry :lang_es_default_templates.entrySet()) 
					if ( !lang_en_default_templates.containsKey(entry.getKey()))
						logger.error(entry.getKey() + "- [es] -> " + " not in [en]");
			}
			
			
			/**-- REPLACE DEFAULT TEXT WITH THE HTML FROM THE FILE SYSTEM IF EXISTS -------------------------------------- */
			
			List<File> list_html = getCandidateFiles(HTML);
			List<File> list_json = getCandidateFiles(JSON);
			
			Map<String, File> map_html = new HashMap<String, File>();
			Map<String, File> map_json = new HashMap<String, File>();
			
			
			list_html.forEach(item -> map_html.put(FilenameUtils.getBaseName(item.getName()), item));
			list_json.forEach(item -> map_json.put(FilenameUtils.getBaseName(item.getName()), item));
			
			
			// ResourceBundle resources_es= ResourceBundle.getBundle(getClass().getName(), Locale.forLanguageTag("es"));
			
			for (Entry<String, EmailTemplate> entry :lang_es_default_templates.entrySet()) {
				
				if ( map_html.containsKey(entry.getKey()+"-"+ES)) {
					String text = readFile(map_html.get(entry.getKey()+"-"+ES));
					entry.getValue().setStringTemplate(text);
					/**
					try {
						String s_model= resources_es.getString(entry.getKey());
						entry.getValue().setModel(s_model);
						logger.debug(s_model);
					} catch (Exception e) {
						logger.error(e);
						entry.getValue().setModel("");
					}**/
				}
				if ( map_json.containsKey(entry.getKey()+"-"+ES)) {
					String text = readFile(map_json.get(entry.getKey()+"-"+ES));
					entry.getValue().setModel(text);
				}
				
			}
			
			
			// ResourceBundle resources_en = ResourceBundle.getBundle(getClass().getName(), Locale.ENGLISH);
			
			for (Entry<String, EmailTemplate> entry :lang_en_default_templates.entrySet()) {
				if ( map_html.containsKey(entry.getKey()+"-"+EN)) {
					String text = readFile( map_html.get(entry.getKey()+"-"+EN));
					entry.getValue().setStringTemplate(text);
				}
				if ( map_json.containsKey(entry.getKey()+"-"+EN)) {
					String text = readFile(map_json.get(entry.getKey()+"-"+EN));
					entry.getValue().setModel(text);
				}
			}
			
			/**------------------------------------------------------------------------------------------------------------ */
		}													
		
		
		
		//this.lang_default_templates.get(EN).forEach((k, v) -> logger.debug(k + " -> " + v));
		
		if (lang_default_templates.containsKey(language)) 
			return this.lang_default_templates.get(language);
		
		
		return this.lang_default_templates.get(EN);
	}


	
	/**
	 * @param file
	 * @return
	 */
	   private String readFile(File file) {
		try {
			return  Files.readString(Path.of(file.getAbsolutePath()));
		} catch (IOException e) {
			logger.error(e);
			return e.getClass().getName() + " | " + e.getMessage(); 
		}
	}
	 

	 
		 
	  private List<File> getSubDirFiles(File dir, int suffixType) {
		
		  List<File> files = new ArrayList<File>();
		  
		  if (!dir.exists() || !dir.isDirectory())
			  return files;
		  
		  File arrfiles [] = dir.listFiles();
		  
		  for (File file: arrfiles) {
			  
			  if (file.isFile()) {
					if (suffixType==HTML && FSUtils.isHTML(file.getName())) {
						logger.debug(file.getName());
						files.add(file);
					}
					else if (suffixType==JSON && FSUtils.isJSON(file.getName())) {
						logger.debug(file.getName());
						files.add(file);
					}
			  }
			  else {
				  if (file.isDirectory()) {
					  List<File> ls = getSubDirFiles(file, suffixType);
					  ls.forEach(item -> files.add(item));
				  }
			  }
		  }
		  return files;
	   }
	  

	  
	private List<File> getCandidateFiles( int suffixType) {
			List<File> files = new ArrayList<File>();
			File base = new File(ServiceLocator.getService(ApplicationServerService.class).getEmailTemplatesDir());
			if (!base.exists() || !base.isDirectory())
				return files;
			getSubDirFiles(base, suffixType).forEach(item -> files.add(item));
			return files;
		}

	private boolean isSendEmail(EmailBuilder builder) {
		return builder.isSendEnabled();
	}

	
	/**
	 * 
	 */
	private Map<String, String> generateTemplateMacros(Domain domain) {
		
		HashMap<String, String> macros = new HashMap<String, String>();
		
		macros.put("domain-info", EmailBuilder.DOMAIN);
		macros.put("domain-name", EmailBuilder.DOMAIN);
		macros.put("domain-url", EmailBuilder.DOMAIN);
		macros.put("password-url", EmailBuilder.DOMAIN);

		
		//general  --------------------------------------------
		
		macros.put("application", EmailBuilder.GENERAL);
		macros.put("application-name", EmailBuilder.GENERAL);
		macros.put("application-fullname", EmailBuilder.GENERAL);
		macros.put("service-noreply", EmailBuilder.GENERAL);
		macros.put("my-tasks-url",  EmailBuilder.GENERAL);
		macros.put("pending-tasks-url", EmailBuilder.GENERAL);
		macros.put("training-url", EmailBuilder.GENERAL);


		// context  --------------------------------------------
		
		macros.put("database-export-url", EmailBuilder.CONTEXT);
		macros.put("sender",  EmailBuilder.CONTEXT);
		macros.put("publisher", EmailBuilder.CONTEXT);
		macros.put("receiver", EmailBuilder.CONTEXT);
		macros.put("subscriber", EmailBuilder.CONTEXT);
		macros.put("from-name", EmailBuilder.CONTEXT);
		macros.put("comment", EmailBuilder.CONTEXT);
		macros.put("from-displayname", EmailBuilder.CONTEXT);
		macros.put("person-displayname", EmailBuilder.CONTEXT);
		macros.put("person-phone-last-four-digits", EmailBuilder.CONTEXT);
		macros.put("person-email-address", EmailBuilder.CONTEXT);
		macros.put("person-displayname", EmailBuilder.CONTEXT);
		macros.put("person-displayname", EmailBuilder.CONTEXT);
		macros.put("event-name", EmailBuilder.CONTEXT);
		macros.put("url", EmailBuilder.CONTEXT);
		macros.put("text", EmailBuilder.CONTEXT);
		
		
		// rule --------------------------------------------
		
		macros.put("rule-title", EmailBuilder.RULE);
		macros.put("rule-modified-by", EmailBuilder.RULE);
		macros.put("rule-lastmodified",EmailBuilder.RULE);
		macros.put("rule-event",EmailBuilder.RULE);
		macros.put("rule-name",EmailBuilder.RULE);
		macros.put("rule-owner", EmailBuilder.RULE);
		macros.put("rule-title",EmailBuilder.RULE);
		macros.put("rule-id",EmailBuilder.RULE);
		macros.put("rule-description",EmailBuilder.RULE);
		macros.put("rule-subject",EmailBuilder.RULE);
		macros.put("rule-condition",EmailBuilder.RULE);
		macros.put("rule-metadata",EmailBuilder.RULE);
		macros.put("rule-source",EmailBuilder.RULE);
		macros.put("action-rule-ruleid",EmailBuilder.RULE);
		macros.put("action-rule-rule-name",EmailBuilder.RULE);
		macros.put("action-rule-subtitle", EmailBuilder.RULE);
		macros.put("action-rule-text", EmailBuilder.RULE);


		// file  --------------------------------------------
		
		macros.put("file-send-by-email-text", EmailBuilder.CONTENT);
		macros.put("file-console-subtitle", EmailBuilder.CONTENT);
		macros.put("file-portal-subtitle", EmailBuilder.CONTENT);
		macros.put("file-title", EmailBuilder.CONTENT);
		macros.put("file-library-url", EmailBuilder.CONTENT);
		macros.put("file-metadata", EmailBuilder.CONTENT);
		macros.put("file-content-classifier", EmailBuilder.CONTENT);
		
		
		macros.put("forgot-username", EmailBuilder.GENERAL);
		macros.put("forgot-password", EmailBuilder.GENERAL);
		
		macros.put("file-content-home-searcher-portal-url", EmailBuilder.CONTENT);
		
		try {
			for (Classifier c: getContentDao().getClassifiers(domain)) {
				if (c.getName()!=null)	
					macros.put("file-attribute."+c.getName().toLowerCase(), EmailBuilder.CONTENT);
			}
			for (Attribute a: getContentDao().getAttributes(domain)) {
				if (a.getName()!=null)
				macros.put("file-attribute."+a.getName().toLowerCase(), EmailBuilder.CONTENT);
			}
		} catch (Exception e) {
				logger.error(e);
		}
		
		// Workflow Alerts --------------------------------------------
		// 
		macros.put("procedure", EmailBuilder.WORKFLOW);
		macros.put("task",  EmailBuilder.WORKFLOW);
		macros.put("task-displayname",  EmailBuilder.WORKFLOW);
		macros.put("task-person-name",  EmailBuilder.WORKFLOW);
		
		
		macros.put("task-start-date",  EmailBuilder.WORKFLOW);
		
		macros.put("task-url",  EmailBuilder.WORKFLOW);
		
		macros.put("previous-task",  EmailBuilder.WORKFLOW);
		macros.put("previous-task-user",  EmailBuilder.WORKFLOW);
		macros.put("previous-task-person-name",  EmailBuilder.WORKFLOW);
		macros.put("previous-task-action",  EmailBuilder.WORKFLOW);
		macros.put("previous-task-displayname",  EmailBuilder.WORKFLOW);
		macros.put("previous-task",  EmailBuilder.WORKFLOW);
		
		
		
		// macros.put("task-action", "task-action"); // not yet supported

		// support ticket  --------------------------------------------
		
		macros.put("support-ticket-subject",  EmailBuilder.SUPPORT);
		macros.put("support-ticket-text",     EmailBuilder.SUPPORT);
		macros.put("support-ticket-context",  EmailBuilder.SUPPORT);
		
		macros.put("support-ticket-person", EmailBuilder.SUPPORT);
		macros.put("support-ticket-person-username", EmailBuilder.SUPPORT);
		macros.put("support-ticket-person-email", EmailBuilder.SUPPORT);
		
		
		
		return macros;
	}
	
	private Domain getDomainKbee() {
		if (KBEE==null) 
			KBEE = ((ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao")).findDomainByName("kbee");
		return KBEE;
	}



	private ContentDao getContentDao() {
		if (this.dao==null)	 {
			 BeansService beans = ServiceLocator.getService(BeansService.class);
			 this.dao = (ContentDao) beans.getBean("contentDao");
		 }
		return this.dao;
	}

	private KbeeUser getSessionUser() {
		return (KbeeUser) ServiceLocator.getService(SecurityService.class).getSessionUser();
	}


	private String getLabel(String key, String lang) {
		 
		 ResourceBundle resources = ResourceBundle.getBundle(getClass().getName(), Locale.forLanguageTag(lang));

		 if (resources==null)
			 return key+ " / " + lang;
		 
		 String s=resources.getString(key);

		 if (s!=null)
			 return s;
		 
		 return key+ " / " + lang;
			 
		 
	 }

	
}



