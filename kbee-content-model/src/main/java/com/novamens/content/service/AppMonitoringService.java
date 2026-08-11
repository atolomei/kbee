package com.novamens.content.service;

import java.time.OffsetDateTime;
import java.util.List;

import com.novamens.content.base.Content;
import com.novamens.security.User;
import com.novamens.service.SystemService;

import kbee.util.Tuple;

public interface AppMonitoringService extends SystemService {

	public void attempToFixIndex(User user);
	public void attempToFixDomainIndex();
	public void attempToFixSiteIndex();
	public void attempToFixSecurityIndex();
	public void attempToReindexContent(Content object);
	public boolean isSupportEnabled();
	
	/** System monitoring info */
	public List<Tuple> keyMetricsInfo();
	public String pingSolR();
	
	/** Data Management */
	public List<Tuple> databaseInfo();
	public List<Tuple> KBFSInfo();
	
	/** System info */
	public List<Tuple> serversInfo();
	public List<Tuple> schedulerInfo();
	public List<Tuple> infrastructureInfo();

	
	public List<Tuple> vaultInfo();
	
	/**
	 * @return
	 */
	public List<Tuple> searchInfo();
	public OffsetDateTime getDateAppStarted();

	/** -----------------------------------
	 * 
	 *  API
	 *  Request mean processing time 1m 5m 1h
	 *  Requests received 1m 5m 1h
	 *  
	  -----------------------------------*/
	List<Tuple> recentActivityAPIInfo();
	List<Tuple> pingMonitorInfo();
	
	
	
	

	
	
}
