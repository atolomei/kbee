package kbee.email.sender;

import java.io.File;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import com.novamens.beans.BeansService;
import com.novamens.service.ServiceLocator;

/**
 * <p>Default provider. Sends email by SMTP using the Spring <code>mailSender</code> bean
 * (configured with <code>email.server</code>, <code>email.port</code>, etc. in kbee.properties)</p>
 */
public class JavaMailEmailSender implements EmailSender {

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(JavaMailEmailSender.class.getName());

	public static final String NAME = "javamail";

	public JavaMailEmailSender() {
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String send(EmailMessage message) {

		JavaMailSender mailsender = getMailSender();

		try {
			final MimeMessage msg = mailsender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");

			helper.setFrom(message.getFrom());
			helper.setSubject(message.getSubject());
			helper.setText(message.getHtml() != null ? message.getHtml() : "", true);

			for (File file : message.getAttachments())
				helper.addAttachment(file.getName(), file);

			helper.setTo(new InternetAddress(message.getTo()));
			for (String address : message.getCc())
				helper.addCc(new InternetAddress(address));

			if (notEmpty(message.getReplyTo()))
				msg.setReplyTo(InternetAddress.parse(message.getReplyTo()));
			if (notEmpty(message.getInReplyTo()))
				msg.setHeader("In-Reply-To", message.getInReplyTo());
			if (notEmpty(message.getReferences()))
				msg.setHeader("References", message.getReferences());

			mailsender.send(msg);
			return OK;

		} catch (MessagingException e) {
			logger.error(e, message.toString());
			return e.getMessage();

		} catch (MailAuthenticationException e) {
			logger.error(e, message.toString());
			return e.getClass().getSimpleName();

		} catch (RuntimeException e) {
			logger.error(e, message.toString());
			return e.getMessage();
		}
	}

	private static boolean notEmpty(String s) {
		return s != null && !s.isBlank();
	}

	private JavaMailSender getMailSender() {
		BeansService beans = ServiceLocator.getService(BeansService.class);
		return (JavaMailSender) beans.getBean("mailSender");
	}
}
