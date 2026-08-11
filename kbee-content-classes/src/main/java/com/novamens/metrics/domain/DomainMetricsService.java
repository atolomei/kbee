package com.novamens.metrics.domain;


import java.time.OffsetDateTime;

import com.novamens.dom.Domain;
import com.novamens.dom.KBFSStorageType;
import com.novamens.service.SystemService;

public interface DomainMetricsService extends SystemService {
	
	
	/**
	 * 
	 * @param domain
	 * @return
	 * total users including archived, enabled, deleted, ddraft
	 */
	public long getUsers(Domain domain);
	public long getResources(Domain domain);
	public long getContents(Domain domain);
	
	/**
	 * in bytes
	 * 
	 * @param domain
	 */
	public long getHardDisk(Domain domain);
	public DomainData getDomainData(Domain domain);
	public double getMeanHardDiskIncrease30d(Domain domain);
	public OffsetDateTime getDateReachLimit(Domain domain);
	public boolean isDateLimit(Domain domain);
	public double getMeanHardDiskIncreaseMA30d(Domain domain);
	public long getHardDisk(Domain domain, KBFSStorageType s);
	
	// 
	// cache 4h duration
	//
	public long getTotalContents();
	public long getTotalResources();
	public long getTotalUsers();
	public long getTotalResources(KBFSStorageType s);
	public long getTotalResources(KBFSStorageType kbfs2, Integer shard);
	
	public long getTotalStoredHardDisk();
	public long getTotalHardDisk(KBFSStorageType s);
	public long getTotalHardDisk(KBFSStorageType kbfs2, Integer shard);
	
	public long getTimeMeasure(Domain domain);
	public OffsetDateTime getHealthCheckOffsetDateTime();
	public int getTotalSolrHeadEnabledContents();
	public int getTotalDBHeadEnabledContents();
	public void setDBSolrCheck(int db, int solr, OffsetDateTime ts);
	
	public void forceCalculateAll();
	public void forceCalculate(Domain domain);
	public long getTotalEncryptedResources();
	
	
}
