package com.novamens.content.service;

import java.util.List;

import com.novamens.content.entity.Person;
import com.novamens.content.subscription.ContentSubscription;
import com.novamens.service.ObjectService;

public interface ContentSubscriptionService extends ObjectService {
	public boolean isSubscribed(Person person);
	public void subscribe(Person person);
	public void unsubscribe(Person person);
	public void removeAll();
	public List<ContentSubscription> getSubscriptions();
}
