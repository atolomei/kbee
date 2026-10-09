package kbee.email.sender;

/**
 * <p>Email delivery provider (JavaMail SMTP, Mailgun HTTP API, ...).</p>
 * <p>The provider is selected in <b>kbee.properties</b> with <code>email.provider</code>.
 * See {@link EmailSenderSelector}</p>
 */
public interface EmailSender {

	public static final String OK = "OK";

	/**
	 * @param message email to deliver
	 * @return {@link #OK} if the email was accepted by the provider, otherwise an error description
	 */
	public String send(EmailMessage message);

	/**
	 * @return provider name, used for logging
	 */
	public String getName();
}
