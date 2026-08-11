package com.novamens.kbee.content.notification;


import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Locale;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;


import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Type;

import com.novamens.content.notification.Notification;
import com.novamens.content.notification.NotificationState;

import com.novamens.dom.ObjectState;
import com.novamens.kbee.dom.AbstractObject;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.security.User;

/**
 * 
 * Notificaciones del Sistema, 
 * que se muestran en el Panel de "Notifications" de la aplicación
 * 
 * -----------------------------------------
 * 1. Task ReAssigned (from Monitor)
 * 2. Work Note
 * 3. Content Published 
 * 
 * -----------------------------------------
 * 4. System Message
 * 5. Admin Message  
 * 
 * 
 */
@Entity
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="notification")
@Table(name = "kb_notification")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "notification_type", discriminatorType=DiscriminatorType.INTEGER)
public abstract class KbeeNotification extends AbstractObject implements Notification {

	@Id 
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "notification_sequencer")
	@Column(name = "ID")
	private Long id;
	
	@Column(name = "title")
	private String title;
	
	@Column(name = "notification_type", insertable=false, updatable=false)
	private Integer type;
	
	@Column(name = "text")
	private String text; 

	@ManyToOne(fetch = FetchType.EAGER, targetEntity = KbeeUser.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name ="sender_id", nullable=true)
	private User sender;
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = KbeeUser.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name ="receiver_id", nullable=true)
	private User receiver;
							
	@Column(name = "datesend", insertable=false) 				// puede usarse creation date del padre
	private OffsetDateTime datesent;

	@Column(name = "dateread")
	private OffsetDateTime dateread;
	
	@Column(name = "startpub")				// date-start-publication
	private OffsetDateTime startpub;
	
	@Column(name = "endpub")				// date-end-publication
	private OffsetDateTime endpub;
	
	@Column(name = "deleteOnAccept")
	private boolean deleteOnAccept = true;
	 
	@Column(name = "notification_state")
	@Enumerated(EnumType.ORDINAL)
	@Type(type="com.novamens.content.notification.NotificationStateUserType")
	private NotificationState notification_state;

	@Column(name = "generating_enoti_rule") 				
	private Long generating_enoti_rule;
	
	@Column(name = "generating_action_rule") 				
	private Long generating_action_rule;

	
	public KbeeNotification() {
		setState(ObjectState.ENABLED);
	}
	
	public Long getId()	{
		return id;
	}
	
	//@Override
	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public OffsetDateTime getStartpub() {
		return startpub;
	}

	public void setStartpub(OffsetDateTime startpub) {
		this.startpub = startpub;
	}

	@Override
	public OffsetDateTime getEndpub() {
		return endpub;
	}

	public void setEndpub(OffsetDateTime endpub) {
		this.endpub = endpub;
	}
	
	@Override
	public void setId(Serializable id) {
		setId(Long.valueOf(id.toString()));
	}

	public String getTitle() 										{return title;}
	public void setTitle(String  title) 							{this.title=title;}
	
	public String getText()				 							{return text;}
	public void setText(String  text) 								{this.text=text;}
	
	public User getSender() 										{return sender;}
	public void setSender(User sender) 								{this.sender=sender;}

	public User getReceiver() 										{return receiver;}
	public void setReceiver(User receiver) 							{this.receiver=receiver;}
	
	public OffsetDateTime getOffsetDateTimeSent()  					{return datesent;}
	public void setOffsetDateTimeSent(OffsetDateTime sent)  		{this.datesent=sent;}
	
	public OffsetDateTime getDateRead() 	 						{return dateread;}
	public void setDateRead(OffsetDateTime read)  					{this.dateread=read;}
	
	@Override
	public boolean deleteOnAccept()									{return deleteOnAccept;}
	public void setDeleteOnAccept(boolean value)  					{this.deleteOnAccept=value;}
		
	public NotificationState getNotificationState()  				{return notification_state;}
	public void setNotificationState(NotificationState state) 	 	{this.notification_state=state;}

	@Override
	public void setOffsetDateTimeRead(OffsetDateTime read) {
		this.dateread=read;
	}
	
	public void setGeneratingENotiRule(Serializable eid) {		generating_enoti_rule = (Long) eid;	}
	public Serializable getGeneratingENotiRule() {		return generating_enoti_rule;	}
	
	public void setGeneratingActionRule(Serializable eid) 	{		generating_action_rule = (Long) eid;	}
	public Serializable getGeneratingActionRule() 			{		return generating_action_rule;	}

	@Override
	public String getName() {
		if (id!=null)
			return id.toString();
		return null;
	}
	
	@Override
	public String getSubject(Locale locale) {
		return getNotificationType().getLabel(locale);
	}
	
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append(super.toString());
		str.append("\ntitle: " + getTitle());
		str.append("\ntext: " + getText());
		if (getNotificationState()!=null)
			str.append("\nstate: " + getNotificationState().getLabel());
		if (getSender()!=null)
			str.append("\nsender: " + getSender().getFirstLastName());
		if (getReceiver()!=null)
		str.append("\nreceiver: " + getReceiver().getDisplayName());
		if (getOffsetDateTimeSent()!=null)
		str.append("\ndate sent: " + getOffsetDateTimeSent().toString()  );
		if (getNotificationState()!=null &&  (getNotificationState()==NotificationState.READ)) {
			if (getDateRead()!=null)
				str.append("\ndate read: " + getDateRead().toString());
		}
		return str.toString();
	}
}
