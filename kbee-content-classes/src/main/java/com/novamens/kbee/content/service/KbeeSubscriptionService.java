package com.novamens.kbee.content.service;

import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.subscription.SubscriptionEvent;
import com.novamens.content.user.UserProfile;
import com.novamens.kbee.content.dao.SubscriptionDao;
import com.novamens.service.ServiceLocator;

/**
 * Servicio de suscripción para seguimiento de un contenido, incluyendo {@link Question}
 * Un usuario puede suscribirse para seguir eventos sobre un contenido.
 * 
 * {@link SubscriptionService} Suscripción de notificación ante eventos sobre un contenido. Envía un email. 
 * Los evento sobre un contenido son: 
 * publicación del contenido, 
 * alta de comentario. 
 * No se esta considerando votación (ver {@link ToolsPanel}.
 * 
 * 
 * {@link ENotiRuleService} 
 * Notificaciones ante eventos basado en reglas. 
 * Envía un email mediante el {@link NotificationService}
 *  
 * {@link NotificationService} Notificationes internas. Genera una notificación de sistema y envía un email.
 * 
 */
@Deprecated
public class KbeeSubscriptionService implements SubscriptionService {

	private Content content = null;
	private SubscriptionDao suscriptionDao;
	
	@SuppressWarnings("unused")
	static private Logger logger = LogManager.getLogger(KbeeSubscriptionService.class.getName());

	public KbeeSubscriptionService() {
	}
	
	public KbeeSubscriptionService(Content content) {
		 this.content = content;
	}

	@Override
	public List<UserProfile> getSubscribers(SubscriptionEvent event) throws IOException {
		return getSuscriptionDao().getSubscribers(getContent().getOId(), event);
	}

	@Override
	@Transactional
	public void unSubscribeContent(UserProfile userProfile)  throws IOException {
		getSuscriptionDao().unSubscribeContent(userProfile, getContent());
	}
	
	@Override
	public boolean isSubscribedUser(UserProfile userProfile, SubscriptionEvent event) throws IOException {
		return getSuscriptionDao().isSubscribedUser(userProfile,content,event);
	}

	@Override
	@Transactional
	public void unSubscribe(UserProfile userProfile, SubscriptionEvent event) throws IOException {
		getSuscriptionDao().unSubscribe(userProfile, getContent(), event);
	}
	
	@Override
	@Transactional
	public void subscribe(UserProfile userProfile, SubscriptionEvent event) throws IOException {
		getSuscriptionDao().subscribe(userProfile, getContent(), event);
	}
	
	@Override
	public Content getContent() {
		return content;
	}
	

	public void setSuscriptionDao(SubscriptionDao dao) {
		suscriptionDao = dao;
	}
	

	public SubscriptionDao getSuscriptionDao() {
		if (suscriptionDao==null) 
			 suscriptionDao = (SubscriptionDao) ServiceLocator.getService(BeansService.class).getBean("subscriptionDao");
		return suscriptionDao;
	}
}
