package com.novamens.portal6.model;

import java.io.IOException;
import java.util.List;

import com.novamens.content.user.UserProfile;
import com.novamens.portal.subscription.SiteSubscriptionEvent;
import com.novamens.service.BusinessObjectService;

public interface SiteSubscriptionService extends BusinessObjectService, PortalService {

	public List<UserProfile> getSubscribers(SiteSubscriptionEvent event) throws IOException;
	public void unSubscribeContent(UserProfile userProfile) throws IOException;
	public boolean isSubscribedUser(UserProfile userProfile, SiteSubscriptionEvent event) throws IOException;
	public void subscribe(UserProfile userProfile, SiteSubscriptionEvent event) throws IOException;
	public void unSubscribe(UserProfile userProfile, SiteSubscriptionEvent event) throws IOException;
	public int getTotalSubscribers(SiteSubscriptionEvent event) throws IOException;
	
}
