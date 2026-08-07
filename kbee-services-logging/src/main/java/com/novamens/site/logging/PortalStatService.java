package com.novamens.site.logging;

import com.novamens.service.BusinessSystemService;

public interface PortalStatService extends BusinessSystemService {

	public void addInboundLog(SiteStatInEvent stat);
	public void addOutboundLog(SiteStatOutEvent stat);
	
}
