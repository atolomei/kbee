package com.novamens.kbee.portal.subscription;


import java.io.IOException;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.novamens.beans.BeansService;
import com.novamens.content.user.UserProfile;
import com.novamens.portal.model.diagrammablesite.DiagrammableSite;
import com.novamens.portal.subscription.SiteSubscriptionEvent;
import com.novamens.portal6.model.Site;
import com.novamens.portal6.model.SiteSubscriptionService;
import com.novamens.service.ServiceLocator;

public class KbeeSiteSubscriptionService implements SiteSubscriptionService {

	private Site site = null;
	private KbeeSiteSubscriptionDao suscriptionDao;

	@SuppressWarnings("unused")
	static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeSiteSubscriptionService.class.getName());
	

	public KbeeSiteSubscriptionService() {
	}

	public KbeeSiteSubscriptionService(Site site) {
		this.site = site;
	}

	@Override
	public int getTotalSubscribers(SiteSubscriptionEvent event) throws IOException {
		return getSiteSuscriptionDao().getTotalSubscribers((Long) getSite().getOId(), event);
	}

	@Override
	public List<UserProfile> getSubscribers(SiteSubscriptionEvent event) throws IOException {
		return getSiteSuscriptionDao().getSubscribers(getSite().getOId(), event);
	}

	@Override
	@Transactional
	public void unSubscribeContent(UserProfile userProfile) throws IOException {
		getSiteSuscriptionDao().unSubscribeContent(userProfile, getSite());
	}

	@Override
	public boolean isSubscribedUser(UserProfile userProfile, SiteSubscriptionEvent event) throws IOException {
		return getSiteSuscriptionDao().isSubscribedUser(userProfile, getSite(), event);
	}

	@Override
	@Transactional
	public void unSubscribe(UserProfile userProfile, SiteSubscriptionEvent event) throws IOException {
		getSiteSuscriptionDao().unSubscribe(userProfile, getSite(), event);
	}

	@Override
	@Transactional
	public void subscribe(UserProfile userProfile, SiteSubscriptionEvent event) throws IOException {
		getSiteSuscriptionDao().subscribe(userProfile, getSite(), event);
	}

	protected Site getSite() {
		return site;
	}

	protected void setSiteSuscriptionDao(KbeeSiteSubscriptionDao dao) {
		suscriptionDao = dao;
	}

	protected KbeeSiteSubscriptionDao getSiteSuscriptionDao() {
		if (suscriptionDao == null)
			suscriptionDao = (KbeeSiteSubscriptionDao) ServiceLocator.getService(BeansService.class)
					.getBean("siteSubscriptionDao");
		return suscriptionDao;
	}
}
