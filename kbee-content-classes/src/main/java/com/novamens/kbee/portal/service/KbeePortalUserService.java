package com.novamens.kbee.portal.service;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.transaction.annotation.Transactional;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.PortalDao;
import com.novamens.event.Event;
import com.novamens.event.EventListener;
import com.novamens.kbee.event.EvictCacheServiceEvent;
import com.novamens.kbee.portal.model.KbeeSiteFavorites;

import com.novamens.portal.service.PortalUserService;
import com.novamens.portal6.model.Site;
import com.novamens.portal.favorites.SiteFavorites;
import com.novamens.security.User;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KeyValue;

/**
 * SITE_CACHE contains (user, List[SiteId, SiteTitle]) for the user
 */

public class KbeePortalUserService implements PortalUserService, EventListener {
		
	// TODO HA
	// TODO: Reset Cache via Event
	//
	static Map<String, List<KeyValue<String>>> SITE_CACHE = new ConcurrentHashMap<String, List<KeyValue<String>>>();

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeePortalUserService.class.getName());

	private User user = null;
	private PortalDao dao = null;

	public KbeePortalUserService() {
	}

	public KbeePortalUserService(User user) {
		this.user = user;
	}

	@Override
	public boolean isSiteInFavorites(Site site) {
		if (SITE_CACHE.containsKey(getKey())) {
			List<KeyValue<String>> list = SITE_CACHE.get(getKey());
			for (KeyValue<String> pair : list) {
				if (pair.getKey().equals(site.getId().toString()))
					return true;
			}
			return false;
		}

		try {
			SiteFavorites sf = getPortalDao().getSiteFavorites(this.user);
			if (sf == null || sf.getFavorites() == null)
				return false;

			addToCache(getKey(), sf);

			return (sf.getFavorites().contains(site));

		} catch (Exception e) {
			logger.error(e);
			return false;
		}
	}


	public synchronized void resetCache() {
		logger.debug("reset user cache" + getKey());
		SITE_CACHE.remove(getKey());
	}

	public synchronized void evict() {
		logger.debug("reset all caches");
		SITE_CACHE.clear();
	}

	
	public List<KeyValue<String>> getListModel() {
		if (SITE_CACHE.containsKey(getKey()))
			return SITE_CACHE.get(getKey());
		synchronized (this) {
			addToCache(getKey(), getList());
		}
		return SITE_CACHE.get(getKey());
	}

	@Override
	@Transactional
	public void addFavorite(Site site) {

		SiteFavorites sitefav = getPortalDao().getSiteFavorites(user);

		if (sitefav != null) {
			try {
				sitefav.addFavoriteSite(site);
				getPortalDao().save(sitefav);
			} catch (Exception e) {
				logger.error(e);
			}
		} else {
			try {
				KbeeSiteFavorites sf = new KbeeSiteFavorites(user);
				sf.addFavoriteSite(site);
				getPortalDao().save(sf);
			} catch (Exception e) {
				logger.error(e);
			}
		}
		SITE_CACHE.remove(getKey());
	}

	@Override
	@Transactional
	public void removeFavorite(Site site) {

		try {
			SiteFavorites sitefav = getPortalDao().getSiteFavorites(user);
			if (sitefav != null) {
				sitefav.removeFavoriteSite(site);
				getPortalDao().save(sitefav);
			}

		} catch (Exception e) {
			logger.error(e);
		}
		SITE_CACHE.remove(getKey());
	}


	private String getKey() {
		return user.getId().toString();
	}


	private SiteFavorites getList() {
		return getPortalDao().getSiteFavorites(this.user);
	}


	private PortalDao getPortalDao() {
		if (this.dao == null)
			this.dao = (PortalDao) ServiceLocator.getService(BeansService.class).getBean("portalDao");
		return this.dao;
	}


	private static synchronized void addToCache(String key, SiteFavorites sf) {

		if (sf == null)
			return;

		List<Site> list = sf.getFavorites();
		List<KeyValue<String>> list_pair = new ArrayList<KeyValue<String>>(list.size());

		Collections.sort(list, new Comparator<Site>() {
			@Override
			public int compare(Site a, Site b) {
				try {
					return a.getTitle().compareToIgnoreCase(b.getTitle());
				} catch (Exception e) {
					logger.error(e);
					return 0;
				}
			}

		});

		for (Site site : list)
			list_pair.add(new KeyValue<String>(site.getId().toString(), site.getTitle()));

		SITE_CACHE.put(key, list_pair);
	}

	
	/**
	@Override
	public List<ViewBK> getRecentActivity() {
		try {
			return ServiceLocator.getService(PortalAnalyticsService.class).getRecentViews(getUser());
		} catch (Exception e) {
			logger.error(e.getClass().getName() + " | getRecentActivity()");
			return new ArrayList<ViewBK>();
		}
	}
	 */
	
	public User getUser() {
		return user;
	}

	@Override
	public boolean listen(Event event) {
		if (event instanceof EvictCacheServiceEvent)
			return true;
		return false;
	}

	@Override
	public void onEvent(Event event) {
		if (event instanceof EvictCacheServiceEvent)
			evict();
	}

}
