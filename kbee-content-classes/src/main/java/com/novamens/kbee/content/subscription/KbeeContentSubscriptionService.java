package com.novamens.kbee.content.subscription;

import java.time.OffsetDateTime;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.novamens.content.base.Content;
import com.novamens.content.entity.Person;
import com.novamens.content.service.ContentSubscriptionService;
import com.novamens.content.subscription.ContentSubscription;
import com.novamens.dom.ObjectState;

import com.novamens.kbee.content.repository.ContentSubscriptionRepository;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.logging.SubscribeEvent;
import com.novamens.logging.UnsubscribeEvent;
import com.novamens.repository.DomRepository;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;


public class KbeeContentSubscriptionService implements ContentSubscriptionService {
			
	static private Logger txLogger = LogManager.getLogger("TxLogger");
														
	private Content content  = null;
	
    @Autowired
    private DomRepository<ContentSubscription> repository;
	
	public KbeeContentSubscriptionService() {
	}

	public KbeeContentSubscriptionService(Content content) {
		 this.content = content;
	}
	
	public Content getContent() {
		return content;
	}

	public void setContent(Content content) {
		this.content = content;
	}
	
	public boolean isSubscribed(Person person) {
		return getRepository().findBy(getContent(), person)!=null;
	}
	
	@Transactional
	public void unsubscribe(Person person) {
		ContentSubscription subscription = getRepository().findBy(getContent(), person);
		if (subscription!=null) {
			getRepository().delete(subscription);
			txLogger.info(new UnsubscribeEvent(getContent()));
		}
	}
	
	public List<ContentSubscription> getSubscriptions() {
		return getRepository().findAllBy(getContent());
	}

	@Transactional
	public void subscribe(Person person) {
		if (getRepository().findBy(getContent(), person)==null) {
			KbeeContentSubscription subscription = new KbeeContentSubscription();
			subscription.setContent(getContent());
			subscription.setPerson(person);
			subscription.setDomain(getContent().getDomain());
			subscription.setLastModifiedUser(getSessionUser());
			subscription.setLastModifiedOffsetDateTime(OffsetDateTime.now());
			subscription.setContent(getContent());
			subscription.setState(ObjectState.ENABLED);
			getRepository().save(subscription);
			txLogger.info(new SubscribeEvent(getContent()));
		}
	}
	
	@Transactional
	public void removeAll() {
		for (ContentSubscription subscription : getSubscriptions()) {
			getRepository().delete(subscription);
		}
	}
	
	public ContentSubscriptionRepository getRepository() {
		return (ContentSubscriptionRepository)repository;
	}
	
	protected KbeeUser getSessionUser() {
		return (KbeeUser)ServiceLocator.getService(SecurityService.class).getSessionUser();
	}

}