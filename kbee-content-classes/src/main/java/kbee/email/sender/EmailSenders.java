package kbee.email.sender;

import com.novamens.beans.BeansService;
import com.novamens.service.ServiceLocator;

/**
 * <p>Resolves the Spring bean <code>emailSender</code> (see services-context.xml).
 * Must be called at execution time (not stored in serializable scheduler requests).</p>
 */
public final class EmailSenders {

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(EmailSenders.class.getName());

	public static final String BEAN = "emailSender";

	private EmailSenders() {
	}

	public static EmailSender get() {
		try {
			BeansService beans = ServiceLocator.getService(BeansService.class);
			Object sender = beans.getBean(BEAN);
			if (sender instanceof EmailSender)
				return (EmailSender) sender;
		} catch (Exception e) {
			logger.warn("bean '" + BEAN + "' not found, using " + EmailSenderSelector.class.getSimpleName());
		}
		return new EmailSenderSelector();
	}

	/**
	 * <p>Same conversion used historically by kbee: plain text with '\n' to HTML</p>
	 */
	public static String textToHtml(String text) {
		if (text == null || text.isEmpty())
			return "";
		return text.replaceAll("\n", "<br/>") + "<br/>";
	}
}
