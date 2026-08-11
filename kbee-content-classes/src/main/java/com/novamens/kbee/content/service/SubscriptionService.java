package com.novamens.kbee.content.service;

import java.io.IOException;
import java.util.List;

import com.novamens.content.base.Content;
import com.novamens.content.subscription.SubscriptionEvent;
import com.novamens.content.user.UserProfile;
import com.novamens.service.BusinessObjectService;

@Deprecated
public interface SubscriptionService extends BusinessObjectService {

	public List<UserProfile> getSubscribers(SubscriptionEvent event) throws IOException;
	public void unSubscribeContent(UserProfile userProfile) throws IOException;
	public boolean isSubscribedUser(UserProfile userProfile, SubscriptionEvent event) throws IOException;
	public void unSubscribe(UserProfile userProfile, SubscriptionEvent event) throws IOException;
	public void subscribe(UserProfile userProfile, SubscriptionEvent event) throws IOException;
	public Content getContent();
}