package kbee.web.listener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.novamens.content.base.Content;
import com.novamens.content.base.KnowledgeSharing;
import com.novamens.content.questionanswer.Answer;
import com.novamens.content.questionanswer.Question;
import com.novamens.content.social.Comment;
import com.novamens.content.social.KnowledgeSharingEvent;
import com.novamens.event.Event;
import com.novamens.event.EventListener;
import com.novamens.kbee.content.service.EventSubscriptionNotificationSendRequest;
import com.novamens.portal.model.diagrammablesite.DiagrammableSite;
import com.novamens.portal.service.PortalDirectoryService;
import com.novamens.portal.service.PortalUrlService;
import com.novamens.portal6.model.Site;
import com.novamens.scheduler.SchedulerException;
import com.novamens.scheduler.SchedulerService;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

/**
 * 
 * <p>
 * The function of this listener is to notify all subscribers of a content that
 * it has a new {@link Comment} or {@link Answer}.
 * </p>
 *
 * Comment:
 * <ul>
 * <li>{@link Content} not Question</li>
 * <li>{@link Question}</li>
 * <li>{@link Answer}</li>
 * </ul>
 * 
 */
public class KnowledgeSharingEventListener implements EventListener {

	static private Logger logger = LogManager.getLogger(KnowledgeSharingEventListener.class.getName());

	@Override
	public boolean listen(Event event) {
		return event instanceof KnowledgeSharingEvent;
	}

	@Override
	public void onEvent(Event event) {

		User user = ((KnowledgeSharingEvent) event).getUser();
		KnowledgeSharing ks = ((KnowledgeSharingEvent) event).getSocialObject();

		// Comment ---------------------------------------------------------------
		//
		// to Question, Content, Answer
		//
		if (ks instanceof Comment) {
			Comment comment = (Comment) ks;
			Content parent = comment.getReferencedContent();
			sendNotificationComment(comment, parent, user);
		}
		// Answer ---------------------------------------------------------------
		//
		else if (ks instanceof Answer) {
			Answer answer = (Answer) ks;
			sendNotificationAnswer(answer, user);
		}
	}

	/**
	 * 
	 * TODO: Locale
	 * 
	 */
	private void sendNotificationComment(Comment comment, Content parent_content, User user) {

		String subject = user.getFirstLastName() + " agregó un comentario a: " + parent_content.getTitle();
		StringBuilder str = new StringBuilder();
		str.append("Usuario:\n" + user.getFirstLastName() + "\n\n");
		str.append("Comentario: " + comment.getText() + "\n");
		Site content_home_site = null;
		try {
			content_home_site = ServiceLocator.getService(PortalUrlService.class).getContentHomeSite(parent_content);
		} catch (Exception e) {
			logger.error(e);
		}

		if (content_home_site != null) {
			str.append("Ir al contenido:\n<a href=\"" + getUrl(parent_content) + "\"> " + parent_content.getTitle()
					+ "</a>");
		} else if (comment.getSiteOId() != null) {
			Site site = null;
			try {
				site = ServiceLocator.getService(PortalDirectoryService.class).findSiteByOId(comment.getSiteOId());
				if (site != null)
					str.append("Ir al Sitio:\n<a href=\"" + getUrl(parent_content) + "\"> " + parent_content.getTitle()
							+ "</a>");
			} catch (Exception e) {
				logger.error(e);

			}
		}

		String text = str.toString();
		String link = null;

		logger.info(text);

		EventSubscriptionNotificationSendRequest request = new EventSubscriptionNotificationSendRequest(
				parent_content.getOId(), com.novamens.content.subscription.SubscriptionEvent.COMMENT_CONTENT, subject,
				text, link);
		try {
			ServiceLocator.getService(SchedulerService.class).enqueue(request);
		} catch (SchedulerException e) {
			logger.error(" {} | {} | {} | {} ", (getSessionUser() != null ? getSessionUser().getUserName() : ""),
					e.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName(), e.getMessage());
		}
	}

	/**
	 * 
	 * 
	 *
	 */
	private void sendNotificationAnswer(Answer answer, User user) {
		String subject = user.getFirstLastName() + " respondió la pregunta: " + answer.getTitle();
		StringBuilder str = new StringBuilder();
		str.append("Usuario:\n" + user.getFirstLastName() + "\n\n");
		str.append("Respuesta:\n" + answer.getText() + "\n");
		Question question = answer.getQuestion();

		str.append("Ir al contenido:\n<a href=\"" + getUrl(question) + "\"> " + question.getTitle() + "</a>\n\n");
		String text = str.toString();
		String link = null;
		EventSubscriptionNotificationSendRequest request = new EventSubscriptionNotificationSendRequest(
				question.getOId(), com.novamens.content.subscription.SubscriptionEvent.UPDATE_CONTENT, subject, text,
				link);
		try {
			ServiceLocator.getService(SchedulerService.class).enqueue(request);
		} catch (SchedulerException e) {
			logger.error(" {} | {} | {} | {} ", (getSessionUser() != null ? getSessionUser().getUserName() : ""),
					e.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName(), e.getMessage());
		}
	}

	private String getUrl(Content content) {
		return ServiceLocator.getService(PortalUrlService.class).getContentUrl(content);
	}

	private User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}

}
