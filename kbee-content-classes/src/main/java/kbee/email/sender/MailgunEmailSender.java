package kbee.email.sender;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.util.Base64;
import java.util.Properties;
import java.util.UUID;

import kbee.util.PropertiesFactory;

/**
 * <p>Sends email using the Mailgun HTTP API (<code>POST /v3/{domain}/messages</code>).</p>
 * 
 * <p>kbee.properties:</p>
 * <pre>
 * email.provider=mailgun
 * email.mailgun.api.key=key-xxxx
 * email.mailgun.baseurl=https://api.mailgun.net/v3/{domain}/messages
 * email.mailgun.from=Kbee &lt;postmaster@{domain}&gt;
 * email.mailgun.timeout.secs=30
 * </pre>
 * 
 * <p>Mailgun only accepts senders of a verified domain, therefore the email is always sent
 * from <code>email.mailgun.from</code> and the original sender is set as <code>Reply-To</code>.</p>
 * 
 * <p>The request is sent as <code>multipart/form-data</code> to support attachments.</p>
 */
public class MailgunEmailSender implements EmailSender {

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(MailgunEmailSender.class.getName());
	private static kbee.util.logging.Logger elogger = kbee.util.logging.Logger.getLogger("email");

	public static final String NAME = "mailgun";

	public static final String API_KEY 		= "email.mailgun.api.key";
	public static final String BASE_URL 	= "email.mailgun.baseurl";
	public static final String FROM 		= "email.mailgun.from";
	public static final String TIMEOUT_SECS = "email.mailgun.timeout.secs";

	private static final String CRLF = "\r\n";

	private volatile HttpClient httpClient;

	public MailgunEmailSender() {
	}

	@Override
	public String getName() {
		return NAME;
	}

	/**
	 * @return true if api key and base url are configured
	 */
	public boolean isConfigured() {
		return notEmpty(getApiKey()) && notEmpty(getBaseUrl());
	}

	@Override
	public String send(EmailMessage message) {

		if (!isConfigured()) {
			logger.error("Mailgun is not configured. Check " + API_KEY + " and " + BASE_URL + " in kbee.properties");
			return "Mailgun is not configured";
		}

		try {
			String boundary = "----kbee" + UUID.randomUUID().toString().replace("-", "");
			byte[] body = buildMultipartBody(message, boundary);

			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create(getBaseUrl()))
					.timeout(Duration.ofSeconds(getTimeoutSecs()))
					.header("Authorization", buildBasicAuth("api", getApiKey()))
					.header("Content-Type", "multipart/form-data; boundary=" + boundary)
					.POST(HttpRequest.BodyPublishers.ofByteArray(body))
					.build();

			HttpResponse<String> response = getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

			elogger.info("Mailgun -> " + message.toString() + " | response: " + response.statusCode() + " - " + response.body());

			if (response.statusCode() != 200) {
				logger.error("Mailgun send failed [" + response.statusCode() + "]: " + response.body() + " | " + message.toString());
				return "Mailgun error " + response.statusCode() + ": " + response.body();
			}
			return OK;

		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			logger.error(e, message.toString());
			return e.getClass().getSimpleName();

		} catch (IOException | RuntimeException e) {
			logger.error(e, message.toString());
			return e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
		}
	}

	private byte[] buildMultipartBody(EmailMessage message, String boundary) throws IOException {

		ByteArrayOutputStream out = new ByteArrayOutputStream();

		String verifiedFrom = getFrom();
		String originalFrom = message.getFrom();

		String replyTo = message.getReplyTo();

		if (notEmpty(verifiedFrom)) {
			addField(out, boundary, "from", verifiedFrom);
			if (!notEmpty(replyTo) && notEmpty(originalFrom) && !originalFrom.equalsIgnoreCase(verifiedFrom))
				replyTo = originalFrom;
		} else {
			addField(out, boundary, "from", originalFrom);
		}

		if (notEmpty(replyTo))
			addField(out, boundary, "h:Reply-To", replyTo);
		if (notEmpty(message.getInReplyTo()))
			addField(out, boundary, "h:In-Reply-To", message.getInReplyTo());
		if (notEmpty(message.getReferences()))
			addField(out, boundary, "h:References", message.getReferences());

		addField(out, boundary, "to", message.getTo());

		for (String cc : message.getCc())
			addField(out, boundary, "cc", cc);

		addField(out, boundary, "subject", message.getSubject() != null ? message.getSubject() : "");
		addField(out, boundary, "html", message.getHtml() != null ? message.getHtml() : "");

		for (File file : message.getAttachments())
			addFile(out, boundary, "attachment", file);

		out.write(("--" + boundary + "--" + CRLF).getBytes(StandardCharsets.UTF_8));
		return out.toByteArray();
	}

	private void addField(ByteArrayOutputStream out, String boundary, String name, String value) throws IOException {
		StringBuilder str = new StringBuilder();
		str.append("--").append(boundary).append(CRLF);
		str.append("Content-Disposition: form-data; name=\"").append(name).append("\"").append(CRLF);
		str.append("Content-Type: text/plain; charset=UTF-8").append(CRLF);
		str.append(CRLF);
		str.append(value != null ? value : "").append(CRLF);
		out.write(str.toString().getBytes(StandardCharsets.UTF_8));
	}

	private void addFile(ByteArrayOutputStream out, String boundary, String name, File file) throws IOException {
		String contentType = Files.probeContentType(file.toPath());
		if (contentType == null)
			contentType = "application/octet-stream";

		String filename = file.getName().replace("\"", "");

		StringBuilder str = new StringBuilder();
		str.append("--").append(boundary).append(CRLF);
		str.append("Content-Disposition: form-data; name=\"").append(name).append("\"; filename=\"").append(filename).append("\"").append(CRLF);
		str.append("Content-Type: ").append(contentType).append(CRLF);
		str.append(CRLF);
		out.write(str.toString().getBytes(StandardCharsets.UTF_8));
		out.write(Files.readAllBytes(file.toPath()));
		out.write(CRLF.getBytes(StandardCharsets.UTF_8));
	}

	private HttpClient getHttpClient() {
		if (httpClient == null) {
			synchronized (this) {
				if (httpClient == null)
					httpClient = HttpClient.newBuilder()
							.connectTimeout(Duration.ofSeconds(getTimeoutSecs()))
							.build();
			}
		}
		return httpClient;
	}

	private static String buildBasicAuth(String username, String password) {
		String credentials = username + ":" + password;
		return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
	}

	private Properties getProperties() {
		return PropertiesFactory.getInstance("kbee").getProperties();
	}

	private String getProperty(String key) {
		String s = getProperties().getProperty(key);
		return (s != null) ? s.trim() : null;
	}

	private String getApiKey() {
		return getProperty(API_KEY);
	}

	private String getBaseUrl() {
		return getProperty(BASE_URL);
	}

	private String getFrom() {
		return getProperty(FROM);
	}

	private long getTimeoutSecs() {
		try {
			String s = getProperty(TIMEOUT_SECS);
			return notEmpty(s) ? Long.parseLong(s) : 30;
		} catch (NumberFormatException e) {
			return 30;
		}
	}

	private static boolean notEmpty(String s) {
		return s != null && !s.isBlank();
	}
}
