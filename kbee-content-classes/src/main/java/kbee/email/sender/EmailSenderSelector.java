package kbee.email.sender;

import kbee.util.PropertiesFactory;

/**
 * <p>Spring bean <code>emailSender</code> (see services-context.xml).
 * Delegates to the provider selected in <b>kbee.properties</b>:</p>
 * <pre>
 * email.provider=javamail   (default)
 * email.provider=mailgun
 * </pre>
 * <p>If <code>mailgun</code> is selected but not configured, it falls back to JavaMail.</p>
 */
public class EmailSenderSelector implements EmailSender {

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(EmailSenderSelector.class.getName());

	public static final String PROVIDER = "email.provider";

	private EmailSender javaMailSender;
	private MailgunEmailSender mailgunSender;

	public EmailSenderSelector() {
	}

	@Override
	public String getName() {
		return getSender().getName();
	}

	@Override
	public String send(EmailMessage message) {
		EmailSender sender = getSender();
		logger.debug("send by " + sender.getName() + " -> " + message.toString());
		return sender.send(message);
	}

	public EmailSender getSender() {
		String provider = getProvider();

		if (MailgunEmailSender.NAME.equalsIgnoreCase(provider)) {
			if (getMailgunSender().isConfigured())
				return getMailgunSender();
			logger.warn(PROVIDER + "=mailgun but Mailgun is not configured. Using " + JavaMailEmailSender.NAME);
		}
		else if (provider != null && !provider.isBlank() && !JavaMailEmailSender.NAME.equalsIgnoreCase(provider)) {
			logger.warn("Unknown " + PROVIDER + "=" + provider + ". Using " + JavaMailEmailSender.NAME);
		}
		return getJavaMailSender();
	}

	public String getProvider() {
		String s = PropertiesFactory.getInstance("kbee").getProperties().getProperty(PROVIDER, JavaMailEmailSender.NAME);
		return s.trim();
	}

	public EmailSender getJavaMailSender() {
		if (javaMailSender == null)
			javaMailSender = new JavaMailEmailSender();
		return javaMailSender;
	}

	public void setJavaMailSender(EmailSender javaMailSender) {
		this.javaMailSender = javaMailSender;
	}

	public MailgunEmailSender getMailgunSender() {
		if (mailgunSender == null)
			mailgunSender = new MailgunEmailSender();
		return mailgunSender;
	}

	public void setMailgunSender(MailgunEmailSender mailgunSender) {
		this.mailgunSender = mailgunSender;
	}
}
