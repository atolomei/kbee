package kbee.web.listener;


import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.novamens.content.base.Content;
import com.novamens.content.base.Social;

import com.novamens.content.questionanswer.Answer;
import com.novamens.content.questionanswer.Question;
import com.novamens.content.service.domain.DomainSettingsService;
import com.novamens.content.social.Comment;
import com.novamens.content.social.Report;
import com.novamens.content.social.SocialEvent;
import com.novamens.content.social.Vote;
import com.novamens.dom.Domain;
import com.novamens.email.EmailData;
import com.novamens.event.Event;
import com.novamens.event.EventListener;
import com.novamens.kbee.content.service.EventSubscriptionNotificationSendRequest;
import com.novamens.kbee.portal.model.PortalUriHelper;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.url.UriHelper;
import com.novamens.portal.service.PortalDirectoryService;
import com.novamens.portal.service.PortalUrlService;
import com.novamens.portal6.model.PortalException;
import com.novamens.portal6.model.Site;
import com.novamens.scheduler.SchedulerException;
import com.novamens.scheduler.SchedulerService;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeRuntimeException;

import kbee.email.EmailSendServiceRequest;
import kbee.util.PropertiesFactory;

/**
 * <p>
 * The function of this listener is to notify all subscribers of a content that
 * it has a new {@link Vote} or {@link Report}.
 * </p>
 *
 * Vote:
 * <ul>
 * <li>{@link Content} not Question</li>
 * <li>{@link Question}</li>
 * <li>{@link Answer}</li>
 * </ul>
 *
 * Report
 * <ul>
 * <li>{@link Content} not Question</li>
 * <li>{@link Question}</li>
 * <li>{@link Answer}</li>
 * <li>{@link Comment}</li>
 * </ul>
 *
 * <p>
 * It executes synchronously with the calling object (normally a Wicket Page).
 * For that reason it sends the actual task to the Scheduler.
 * </p>
 *
 */
public class SocialEventListener implements EventListener {

	static private Logger logger = LogManager.getLogger(SocialEventListener.class.getName());

	static Map<String, String> spa_labels = new HashMap<String, String>();
	static Map<String, String> eng_labels = new HashMap<String, String>();

	private static String BRANDING_APP_NAME = "brandingAppName";
	private static String EMAIL_SERVICE_NO_REPLY = "emailServiceNoReply";

	static private final String BRAND = PropertiesFactory.getInstance("kbee").getProperties().getProperty("brand",	"kbee");
	

	static {

		spa_labels.put("from", "%s (%s) <%s>");
		spa_labels.put("vote_subject", "%s - Votó: %s (%s)");
		spa_labels.put("vote_message", "Al usuario %s le gustó %s:\n%s\n\n");

		spa_labels.put("from", "%s (%s) <%s>");
		spa_labels.put("report_subject", "%s - Reportó: %s (%s)");
		spa_labels.put("report_message", "El usuario %s reportó el contenido como inapropiado:\n%s.\n\n");

		eng_labels.put("from", "%s (%s) <%s>");
		eng_labels.put("vote_subject", "%s - Voted: %s (%s)");
		eng_labels.put("vote_message", "The user %s has voted %s:\n%s.\n\n");

		eng_labels.put("report_subject", "%s - Reported: %s (%s)");
		eng_labels.put("report_message", "The user %s has reported:\n%s.");

	}

	@Override
	public boolean listen(Event event) {
		return event instanceof SocialEvent;
	}

	@Override
	public void onEvent(Event event) {

		User user = ((SocialEvent) event).getUser();
		Social social = ((SocialEvent) event).getSocialObject();

		// Vote
		//
		//
		if (social instanceof Vote) {
			Content content = social.getContent();
			if (content instanceof Answer)
				sendNotificationVote((Answer) event.getObject(), user);
			else if (content instanceof Question)
				sendNotificationVote((Question) event.getObject(), user);
			else
				sendNotificationVote((Content) event.getObject(), user);

			// Report
			//
			//
		} else if (social instanceof Report) {
			Content content = social.getContent();

			if (content instanceof Comment) {
				Content ref = ((Comment) content).getReferencedContent();
				sendNotificationReport((Comment) content, user, ref);
			} else if (content instanceof Answer) {
				Question ref = ((Answer) content).getQuestion();
				sendNotificationReport((Answer) content, user, ref);
			} else {
				sendNotificationReport(content, user);
			}
		}
	}

	/**
	 * 
	 * <p>
	 * Notificación por email a los usuarios que siguen este contenido. Como el
	 * Listener es Sincrónico con el componente Wicket que lo llama. Se genera un
	 * {@link EventNotificationRequest} que se envía al Scheduler
	 * </p>
	 *
	 * TODO: Locale
	 * 
	 * @param content
	 * @param user
	 */
	private void sendNotificationVote(Content content, User user) {
		String url = getUrl(content);
		Map<String, String> labels = (user.getLocale().equals(Locale.ENGLISH) ? eng_labels : spa_labels);
		String fromname = user.getFirstLastName();
		String title = content.getTitle();
		String tipo;
		if (user.getLocale().equals(Locale.ENGLISH)) {
			if (content instanceof Question)
				tipo = "the Question";
			else if (content instanceof Answer)
				tipo = "the Answer";
			else
				tipo = "the Content";
		} else {
			if (content instanceof Question)
				tipo = "la Pregunta";
			else if (content instanceof Answer)
				tipo = "la Respuesta";
			else
				tipo = "el Contenido";
		}

		String APPLICATION, FROM;
		if (content.getDomain() != null) {
			APPLICATION = content.getDomain().getService(DomainSettingsService.class).get(BRANDING_APP_NAME);
			if (APPLICATION == null)
				APPLICATION = PropertiesFactory.getInstance("kbee").getProperties()
						.getProperty("com.novamens.kbee.application", BRAND);
			FROM = content.getDomain().getService(DomainSettingsService.class).get(EMAIL_SERVICE_NO_REPLY);
			if (FROM == null)
				FROM = PropertiesFactory.getInstance("kbee").getProperties().getProperty(
						"com.novamens.kbee.notification.noreplyemailaddress","noreply@novamens.com");
			;
		} else {
			APPLICATION = PropertiesFactory.getInstance("kbee").getProperties()
					.getProperty("com.novamens.kbee.application", BRAND);
			FROM = PropertiesFactory.getInstance("kbee").getProperties().getProperty(
					"com.novamens.kbee.notification.noreplyemailaddress",
					"noreply@novamens.com" );
			;
		}

		String subject = String.format(labels.get("vote_subject"), user.getFirstLastName(), title, APPLICATION);
		String msg = String.format(labels.get("vote_message"), fromname, tipo, title);
		EventSubscriptionNotificationSendRequest request = new EventSubscriptionNotificationSendRequest(
				content.getOId(), com.novamens.content.subscription.SubscriptionEvent.VOTE_CONTENT, subject, msg, url);
		try {
			ServiceLocator.getService(SchedulerService.class).enqueue(request);

		} catch (SchedulerException e) {
			logger.error(e.getStackTrace());
		}
	}

	/**
	 * Report de un Comment
	 * 
	 * TODO: Locale
	 * 
	 * @param content
	 * @param user
	 */
	private void sendNotificationReport(Comment comment, User user, Content parent_content) {

		String url = getUrl(comment.getReferencedContent());

		String APPLICATION, FROM;
		if (parent_content.getDomain() != null) {
			APPLICATION = parent_content.getDomain().getService(DomainSettingsService.class).get(BRANDING_APP_NAME);
			if (APPLICATION == null)
				APPLICATION = PropertiesFactory.getInstance("kbee").getProperties()
						.getProperty("com.novamens.kbee.application", BRAND);
			FROM = parent_content.getDomain().getService(DomainSettingsService.class).get(EMAIL_SERVICE_NO_REPLY);
			if (FROM == null)
				FROM = PropertiesFactory.getInstance("kbee").getProperties().getProperty(
						"com.novamens.kbee.notification.noreplyemailaddress");
			
		} else {
			APPLICATION = PropertiesFactory.getInstance("kbee").getProperties()
					.getProperty("com.novamens.kbee.application", BRAND);
			FROM = PropertiesFactory.getInstance("kbee").getProperties().getProperty(
					"com.novamens.kbee.notification.noreplyemailaddress","noreply@novamens.com" );
			
		}

		Map<String, String> labels = (user.getLocale().equals(Locale.ENGLISH) ? eng_labels : spa_labels);
		String fromname = user.getFirstLastName();
		String title = comment.getTitle();
		String subject = String.format(labels.get("report_subject"), user.getDisplayName(), title, APPLICATION);
		String msg = String.format(labels.get("report_message"), fromname, title);
		EventSubscriptionNotificationSendRequest request = new EventSubscriptionNotificationSendRequest(
				comment.getOId(), com.novamens.content.subscription.SubscriptionEvent.REPORT_CONTENT, subject, msg,
				url);
		try {
			ServiceLocator.getService(SchedulerService.class).enqueue(request);

		} catch (SchedulerException e) {
			logger.error(e);
		}
	}

	/**
	 *
	 * Report de un Answer
	 * 
	 * @param content
	 * @param user
	 */
	private void sendNotificationReport(Answer answer, User user, Question question) {
		String subject = user.getFirstLastName() + " reportó una respuesta en la pregunta: " + question.getTitle();
		String msg = "El usuario " + user.getFirstLastName() + " reportó la respuesta: \n" + answer.getText() + "\n"
				+ "del usuario: " + answer.getUser().getDisplayName() + "\n\n\n";
		String link = null;
		EventSubscriptionNotificationSendRequest request = new EventSubscriptionNotificationSendRequest(
				question.getOId(), com.novamens.content.subscription.SubscriptionEvent.REPORT_CONTENT, subject, msg,
				link);
		try {
			ServiceLocator.getService(SchedulerService.class).enqueue(request);
		} catch (SchedulerException e) {
			logger.error(e.getClass().getName() + " | " + Thread.currentThread().getStackTrace()[1].getMethodName());
		}
	}

	/**
	 * 
	 * Report de un Content
	 * 
	 * @param content
	 * @param user
	 */
	private void sendNotificationReport(Content content, User user) {
		String subject = user.getFirstLastName() + " reportó el contenido: " + content.getTitle();
		String msg = "El usuario " + user.getDisplayName() + " reportó el contenido: \n" + content.getTitle() + "\n"
				+ "\n\n\n";

		Site site = getContentHomeSite(content);
		if (site == null)
			site = getHomeSite(content);

		//String toemail = getContentDao()site.getOwner();
		String toemail = "zxz";

		String APPLICATION, FROM;
		if (content.getDomain() != null) {
			APPLICATION = content.getDomain().getService(DomainSettingsService.class).get(BRANDING_APP_NAME);
			if (APPLICATION == null)
				APPLICATION = PropertiesFactory.getInstance("kbee").getProperties()
						.getProperty("com.novamens.kbee.application", BRAND);
			FROM = content.getDomain().getService(DomainSettingsService.class).get(EMAIL_SERVICE_NO_REPLY);
			if (FROM == null)
				FROM = PropertiesFactory.getInstance("kbee").getProperties().getProperty(
						"com.novamens.kbee.notification.noreplyemailaddress",
						"noreply@novamens.com");
			;
		} else {
			APPLICATION = PropertiesFactory.getInstance("kbee").getProperties()
					.getProperty("com.novamens.kbee.application", BRAND);
			FROM = PropertiesFactory.getInstance("kbee").getProperties().getProperty(
					"com.novamens.kbee.notification.noreplyemailaddress",
					"noreply@novamens.com");
			
		}

		String from_email = String.format("%s <%s>", APPLICATION, FROM);
		EmailData edata = new EmailData(from_email, toemail, subject, msg, null, "Report Content");
		EmailSendServiceRequest req = new EmailSendServiceRequest(edata, content.getDomain());
		try {
			ServiceLocator.getService(SchedulerService.class).enqueue(req);
		} catch (SchedulerException e) {
			logger.error(e);
		}

	}

	private String getUrl(Content content) {
		String url = "not implemented";
		if (content instanceof Question) {
			String content_id = content.getOId().toString();
			throw new KbeeRuntimeException("not implemented");
			//url = UriHelper.getInstance().getServerURLAndPort(content.getDomain().getName()) + "/drbit/question/"
			//		+ content_id + "/" + UriHelper.getInstance().getTitle(content);
			
		} else if (content instanceof Answer) {
			String content_id = ((Answer) content).getQuestion().getOId().toString();
			//url = UriHelper.getInstance().getServerURLAndPort(content.getDomain().getName()) + "/drbit/question/"
			//		+ content_id + "/" + UriHelper.getInstance().getTitle(content);
			throw new KbeeRuntimeException("not implemented");
		} else {
			
			
			String base = PortalUriHelper.getInstance().getPortalURL(content.getDomain().getName());
			Site site = getContentHomeSite(content);
			if (site == null)
				site = getHomeSite(content);
			String site_url;
			if (site != null)
				site_url = site.getUrl();
			else {
				logger.error("Home Site not found.");
				site_url = "home";
			}
			String content_id = content.getOId().toString();
			url = base + site_url + "/" + content_id + "/" + UriHelper.getInstance().getTitle(content);
		}
		return url;
	}

	private Site getHomeSite(Content content) {
		PortalDirectoryService service = ServiceLocator.getService(PortalDirectoryService.class);
		Domain domain = content.getDomain();
		Site site = null;
		try {
			site = service.getHomeSite(domain);
			if (site == null)
				logger.error("Home Site is null. Please create HomeSite");
			return site;
		} catch (Exception e) {
			logger.error(e);
			return null;
		}
	}

	//private KbeeUser getSessionUser() {
	//	return (KbeeUser) ServiceLocator.getService(SecurityService.class).getSessionUser();
	//}

	private Site getContentHomeSite(Content content) {
		try {
			return ServiceLocator.getService(PortalUrlService.class).getContentHomeSite(content);
		} catch (PortalException e) {
			logger.error(e);
			return null;
		}
	}
}
