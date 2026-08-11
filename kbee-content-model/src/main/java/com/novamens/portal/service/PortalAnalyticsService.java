package com.novamens.portal.service;

import java.util.List;

import com.novamens.portal6.model.PortalService;
import com.novamens.portal6.model.Site;
import com.novamens.portal6.model.ViewBK;
import com.novamens.security.User;
import com.novamens.service.BusinessSystemService;

/**
 * 
 * <p>Analytics -> Site and Page visits</p>
 *
 */
public interface PortalAnalyticsService extends BusinessSystemService, PortalService {

	public List<ViewBK> getRecentViews(User user);
	
	public void add(User user, Site site, ViewBK view, boolean include_recent);
	public void add(User user, Site site, ViewBK view);

}
