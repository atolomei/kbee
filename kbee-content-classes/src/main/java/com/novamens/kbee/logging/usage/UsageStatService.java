package com.novamens.kbee.logging.usage;

import java.io.Serializable;
import java.util.List;

import com.novamens.content.base.ContentMgmtException;
import com.novamens.logging.usage.UsageStat;
import com.novamens.service.SystemService;

public interface UsageStatService extends SystemService {

	
	public void save(UsageStat stat);
	
	// 
	public void nonTrxSave(UsageStat stat); /* to be used by Scheduler */	
	public void nonTrxSaveApiUsage(String from, String to) throws ContentMgmtException;
	
	
	public List<UsageStat> getUsageStat(Serializable domain_id);

	public void delete(Serializable domain_id);
	
	public List<UsageStat> getUsageStat(Serializable domain_id, int max_elements);
	public List<UsageStat> getGlobalUsageStat();

	public void saveApiUsage(String from, String to) throws ContentMgmtException;

	
 	
	
}
