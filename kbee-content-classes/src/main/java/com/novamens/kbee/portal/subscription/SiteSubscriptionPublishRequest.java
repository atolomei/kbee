package com.novamens.kbee.portal.subscription;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.logging.log4j.LogManager;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import com.novamens.beans.BeansService;
import com.novamens.content.service.domain.DomainSettingsService;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.email.EmailData;
import com.novamens.kbee.portal.model.PortalUriHelper;
import com.novamens.logging.SendEmailEvent;
import com.novamens.metrics.SystemMetricsService;
import com.novamens.portal.diagrammablesite.dao.PortalDiagrammableDao;
import com.novamens.portal.model.diagrammablesite.DiagrammableSite;
import com.novamens.portal.subscription.SiteSubscriptionEvent;
import com.novamens.portal6.model.SiteSubscriptionService;
import com.novamens.portal6.model.ViewBK;
import com.novamens.portal6.model.ViewBKBlock;
import com.novamens.portal6.model.ViewBKContent;
import com.novamens.portal6.model.ViewBKLink;
import com.novamens.scheduler.AbstractServiceRequest;
import com.novamens.scheduler.SchedulerService;
import com.novamens.service.ServiceLocator;

import kbee.util.PropertiesFactory;

/**
 * ---------------------------------------------------------------------------------------------------------------------------
 * 
 * Nueva {@link ViewBKContent} -> Send Email ({@link Content} not null) Nueva
 * {@link ViewBKSite} -> Send Email ({@link DiagrammableSite} not null) Nueva
 * {@link ViewBKLink} -> Send Email ({@link Link} is not null) Nueva
 * {@link ViewBKBlock} -> Send Email ({@link DiagrammableBlock} is not null)
 * 
 * 
 * Modificacion de contenido
 * 
 * Modificacion de ViewBK: Notifica a los seguidores si cambio el contenido,
 * sitio, url, block si no cambio entonces no.
 * 
 *
 */
public class SiteSubscriptionPublishRequest extends AbstractServiceRequest {

	static String BRAND = PropertiesFactory.getInstance("kbee").getProperties().getProperty("brand", "kbee");

	private static final String BRANDING_APP_NAME = "brandingAppName";
	private static final String EMAIL_SERVICE_NO_REPLY = "emailServiceNoReply";
	private static final String EMAIL_SERVICE_STATUS = "emailServiceStatus";

	static Map<String, String> spa_labels = new HashMap<String, String>();
	static Map<String, String> eng_labels = new HashMap<String, String>();

	static {
		spa_labels.put("from", "%s (%s) <%s>");
		spa_labels.put("publication_subject", "%s - Nueva publicación: %s - %s");
		spa_labels.put("publication_message",
				"En el sitio %s se ha publicado:\n%s.\n\n<a  href=\"%s\" target=\"_blank\">ir al sitio</a>");

		eng_labels.put("from", "%s (%s) <%s>");
		eng_labels.put("publication_subject", "%s - New published: %s - %s");
		eng_labels.put("publication_message",
				"The site %s has new content published:\n%s.\n\n<a  href=\"%s\" target=\"_blank\">view site</a>");
	}

	static private org.apache.logging.log4j.Logger logger = LogManager.getLogger("DBEventLogger");

	private static final long serialVersionUID = 8844149720493131961L;

	private Long site_oid;
	private Long view_oid;

	/**
	 * -----------------------------------------------------------------------------------
	 */
	public SiteSubscriptionPublishRequest(DiagrammableSite site, ViewBK view) {

		site_oid = (Long) site.getOId();
		view_oid = (Long) view.getOId();

		setPriority(SchedulerService.LOW_PRIORITY);

		setCost(SchedulerService.STANDARD_PROCESSING_COST);

		setName("Site suscription: " + site.getTitle() + ". Pub: " + view.getTitle() + ". " + view.getViewType());

	}

	/**
	 * -------------------------------------------------------------------------------------------------------------
	 * 
	 * Se ejecuta al volver del {@link Scheduler} por eso que las variables de
	 * instancia tienen que ser los ids y el objeto serializable.
	 * 
	 */
	@Override
	public void execute() {

		if (site_oid == null || view_oid == null)
			return;

		try {
			DiagrammableSite site = getPortalDao().findSiteByOId(site_oid);
			ViewBK view = getPortalDao().findViewByOId(view_oid);

			if (site == null || view == null) {
				logger.error(((site == null) ? "site id: " + String.valueOf(site_oid)
						: " view id: " + String.valueOf(view_oid)) + " is null");
				return;
			}

			if (view instanceof ViewBKContent) {
				if (((ViewBKContent) view).getContent() == null) {
					logger.info("ViewBKContent. Content is null. Can not send notifications.");
					return;
				}
			} else if (view instanceof ViewBKLink) {
				if (((ViewBKLink) view).getLink() == null) {
					logger.info("ViewBKLink. link is null. Can not send notifications.");
					return;
				}
			}

			SiteSubscriptionService service = site.getService(SiteSubscriptionService.class);

			try {
				List<UserProfile> list = service.getSubscribers(SiteSubscriptionEvent.SITE_PUBLISH_CONTENT);
				for (UserProfile profile : list) {
					if (profile.isEmailNotifications())
						sendMail(profile, site, view);
					else {
						logger.debug(profile.getPersonFirstLastName() + " email notifications disabled.");
					}
				}
			} catch (IOException e) {
				logger.error(
						e.getClass().getName() + " | " + Thread.currentThread().getStackTrace()[1].getMethodName());
			}

		} catch (Exception e) {
			logger.error(e.getClass().getName() + " | " + Thread.currentThread().getStackTrace()[1].getMethodName());
		}
	}

	/**
	 * -----------------------------------------------------------------------------------
	 * 
	 * @param profile
	 * @param site
	 * @param view
	 */

	private void sendMail(UserProfile profile, DiagrammableSite site, ViewBK view) {

		try {
			Map<String, String> labels = (profile.getUser().getLocale().equals(Locale.ENGLISH) ? eng_labels
					: spa_labels);

			String fromname = site.getTitle();

			String APPLICATION, FROM;
			if (site.getDomain() != null) {
				APPLICATION = site.getDomain().getService(DomainSettingsService.class).get(BRANDING_APP_NAME);
				if (APPLICATION == null)
					APPLICATION = PropertiesFactory.getInstance("kbee").getProperties()
							.getProperty("com.novamens.kbee.application", BRAND);
				FROM = site.getDomain().getService(DomainSettingsService.class).get(EMAIL_SERVICE_NO_REPLY);
				if (FROM == null)
					FROM = PropertiesFactory.getInstance("kbee").getProperties().getProperty(
							"com.novamens.kbee.notification.noreplyemailaddress",
							 "noreply@novamens.com" );
				;
			} else {
				APPLICATION = PropertiesFactory.getInstance("kbee").getProperties()
						.getProperty("com.novamens.kbee.application", BRAND);
				FROM = PropertiesFactory.getInstance("kbee").getProperties().getProperty(
						"com.novamens.kbee.notification.noreplyemailaddress",
						"noreply@novamens.com");
				;
			}

			String title = view.getTitle();
			String to = profile.getPerson().getEmail();
			String subject = String.format(labels.get("publication_subject"), site.getTitle(), title,
					view.getViewType());

			String url = PortalUriHelper.getInstance().getPortalURL(site.getDomain().getName()) + site.getUrl();
			String msg = String.format(labels.get("publication_message"), fromname, title, url);
			String from_email = String.format(labels.get("from"), fromname, APPLICATION, FROM);

			Map<String, String> params = new HashMap<String, String>();
			params.put("from-email", from_email);
			params.put("to-email", to);
			params.put("subject", subject);
			params.put("texto", msg);
			params.put("domain_id", profile.getDomain().getId().toString());

			boolean is_active;

			String mode = site.getDomain().getService(DomainSettingsService.class).get(EMAIL_SERVICE_STATUS);
			if (mode == null)
				is_active = true;
			else
				is_active = mode.equals("yes");

			if (is_active) {
				String sentcode = sendbyemail(params);
				EmailData em = new EmailData(from_email, to, subject, msg, "site-subscription-"+site.getId().toString());
				em.setContextInfo("Site Suscription");
				logger.info(new SendEmailEvent(em, sentcode, profile.getDomain()));
			} else {
				try {
					logger.info("Mode NOSEND: Simulating sending email...");
					EmailData em = new EmailData(from_email, to, subject, msg, null,
							"Site Suscription. Publish " + view.getViewType());
					logger.info(new SendEmailEvent(em, "ok mode.nosend", profile.getDomain()));

					logger.info(em.toString());
					Thread.sleep(400 * (((int) Math.random()) * 10 % 8 + 12));
				} catch (InterruptedException e) {
					logger.error(
							e.getClass().getName() + " | " + Thread.currentThread().getStackTrace()[1].getMethodName());
				}
			}
		} catch (RuntimeException e) {
			logger.error(e.getClass().getName() + " | " + Thread.currentThread().getStackTrace()[1].getMethodName());
		}
	}

	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}

	/**
	 * -----------------------------------------------------------------------------------
	 */
	private PortalDiagrammableDao getPortalDao() {
		return (PortalDiagrammableDao) ServiceLocator.getService(BeansService.class).getBean("portalDao");
	}

	/**
	 * -----------------------------------------------------------------------------------
	 */
	private String sendbyemail(Map<String, String> params) {

		if (params.get("to-email") == null) {
			logger.error("to-email is null");
			return "to-email is null";
		}

		BeansService beans = ServiceLocator.getService(BeansService.class);
		JavaMailSender mailsender = (JavaMailSender) beans.getBean("mailSender");

		try {
			final MimeMessage msg = mailsender.createMimeMessage();

			MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
			helper.setFrom(params.get("from-email"));
			helper.setTo(params.get("to-email"));
			helper.setSubject(params.get("subject"));

			StringBuilder strMensaje = new StringBuilder();

			if (!params.get("texto").isEmpty())
				strMensaje.append(params.get("texto").replaceAll("\n", "<br/>") + "<br/>");

			// use the true flag to indicate the text included is HTML
			//
			//
			helper.setText(strMensaje.toString(), true);

			mailsender.send(msg);

			if (params.get("domain_id") != null)
				ServiceLocator.getService(SystemMetricsService.class).mark("email", (String) params.get("domain_id"));

			return "ok";

		} catch (MessagingException e) {
			logger.error(e.getClass().getName() + " | " + Thread.currentThread().getStackTrace()[1].getMethodName());
			return e.getMessage();

		} catch (MailAuthenticationException e) {
			logger.error(e.getClass().getName() + " | " + Thread.currentThread().getStackTrace()[1].getMethodName());
			return e.getMessage();

		} catch (RuntimeException e1) {
			logger.error(e1.getClass().getName() + " | " + Thread.currentThread().getStackTrace()[1].getMethodName());
			return e1.getMessage();
		}

	}

}
