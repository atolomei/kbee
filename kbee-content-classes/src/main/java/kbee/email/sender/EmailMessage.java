package kbee.email.sender;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>Provider independent email message.
 * Built by {@link kbee.email.EmailSendServiceRequest} and delivered by an {@link EmailSender}</p>
 */
public class EmailMessage {

	private String from;
	private String to;
	private List<String> cc = new ArrayList<String>();
	private String subject;
	private String html;
	private List<File> attachments = new ArrayList<File>();
	private String domainId;

	// optional headers 
	private String replyTo;
	private String inReplyTo;
	private String references;

	public EmailMessage() {
	}

	public String getReplyTo() {
		return replyTo;
	}

	public void setReplyTo(String replyTo) {
		this.replyTo = replyTo;
	}

	public String getInReplyTo() {
		return inReplyTo;
	}

	public void setInReplyTo(String inReplyTo) {
		this.inReplyTo = inReplyTo;
	}

	public String getReferences() {
		return references;
	}

	public void setReferences(String references) {
		this.references = references;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public List<String> getCc() {
		return cc;
	}

	public void addCc(String address) {
		if (address != null && !address.isBlank())
			this.cc.add(address.trim());
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getHtml() {
		return html;
	}

	public void setHtml(String html) {
		this.html = html;
	}

	public List<File> getAttachments() {
		return attachments;
	}

	public void addAttachment(File file) {
		if (file != null)
			this.attachments.add(file);
	}

	public String getDomainId() {
		return domainId;
	}

	public void setDomainId(String domainId) {
		this.domainId = domainId;
	}

	@Override
	public String toString() {
		return "from: " + from + " | to: " + to + " | cc: " + cc + " | subject: " + subject + " | attachments: " + attachments.size();
	}
}
