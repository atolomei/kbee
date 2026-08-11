package com.novamens.kbee.content.notification;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.novamens.content.base.Content;
import com.novamens.content.notification.ContentNotification;
import com.novamens.content.notification.NotificationType;
import com.novamens.content.service.UrlService;
import com.novamens.kbee.content.base.KbeeContent;

@Entity
@DiscriminatorValue("30") // NotificationType.WORKFLOW
public class KbeeWorkflowNotification extends KbeeNotification implements ContentNotification {
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = KbeeContent.class)
	@Fetch(FetchMode.JOIN)
	@JoinColumn(name ="content_id", nullable=true)
	private Content content = null;
	
	public KbeeWorkflowNotification() {
	}
	
	public KbeeWorkflowNotification(Content content) {
		this.content=content;
	}
	
	@Override
	public Content getContent() {
		return content;
	}
	
	public void setContent(Content content) {
		this.content=content;
	}

	@Override
	public NotificationType getNotificationType() {
		return NotificationType.WORKFLOW;
	}

	@Override
	public boolean isBillboard() {
		return false;
	}

	@Override
	public boolean isAlert() {
		return false;
	}
	
	@Override
	public String getUrl() {
		return getContent().getService(UrlService.class).getUrl(false);
	}
	
	@Override
	public String getTypeStr() {
		return NotificationType.WORKFLOW.getLabel(getSessionUser().getLocale());
	}
	
	@Override
	public String getIcon() {
		return "fal fa-coffee";
	}
}