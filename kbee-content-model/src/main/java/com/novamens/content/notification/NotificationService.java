package com.novamens.content.notification;

import java.time.OffsetDateTime;
import java.util.List;

import com.novamens.content.base.Content;
import com.novamens.content.notes.Billboard;
import com.novamens.event.LogEvent;
import com.novamens.kbee.event.EventType;
import com.novamens.security.User;
import com.novamens.service.SystemService;

/** -----------------------------------------------------------------------------------------------------------------------------------
 * 
 * <p>Servicio de Notificaciones internas ante eventos de Workflow y Asignación.
 * Es el único servicio de Notificaciones de los tres que existen 
 * (los otros son {@link SubscriptionService}, {@link ENotiRuleService})
 * que genera una notificación interna.
 * </p>
 * <p>
 * Está enganchado con el sistema de logging (Log4j) que lo invoca
 * ante eventos que debe notificar.
 * </p>
 *
 *@see
 *{@link KbeeNotificationService} implementación
 * 
 */
public interface NotificationService extends SystemService {
	
	public void process(LogEvent event);
	
	public void sendPublishNotification(Content content,  User user);
	public void sendNotification(NotificationType type, Content content, String message, User user);
	
	public void subscribe(EventType type, Content content, User user);
	public void subscribe(EventType type, User user);
	
	public void unsubscribe(EventType type, Content content, User user);
	public void unsubscribe(EventType type, User user);
	
	public boolean isSubscribed(EventType type, User user);
	public boolean isSubscribed(EventType type, Content content, User user);

	public void save(Notification noti);
	public void markAsRead(Notification notification);
	public void markAsDelete(Notification notification);
	public List<Notification> getNotifications(User user);
	public int getTotalNotifications(User user);
	public int getTotalBillboardNotifications(User user);

	public void deleteForWorkNote(User receiver, Billboard note);

	public void deleteAll(User receiver);
	public void deleteWorkNoteNotification(OffsetDateTime date);

	public void evict();

	void deleteContentPublishNotification(OffsetDateTime date);
}