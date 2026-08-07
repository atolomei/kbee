package com.novamens.metrics;

import java.io.Serializable;
import java.util.SortedMap;

import javax.sql.DataSource;

import org.hibernate.SessionFactory;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;

import com.novamens.service.SystemService;


public interface SystemMetricsService extends SystemService {

	
	// -----------------------------------------
	//
	//

	
	
	//  Vault
		public Meter getMeterVaultEncrypt();
		public Meter getMeterVaultDeEncrypt();
		
	
	
	// -----------------------------------------
	// S3
	//
	// File Server Put and Get rates
	public Meter getMeterS3PutObject();
	public Meter getMeterS3GetObject();
	
	public Meter getMeterS3PutObject(String bucket);
	public Meter getMeterS3GetObject(String bucket);
	
	public Counter getCounterS3KBFSCacheHit();
	public Counter getCounterS3KBFSCacheMiss();
	
	
	// -----------------------------------------
	// KBFS
	//
	// File Server Put and Get rates
	public Meter getMeterV2PutObject();
	public Meter getMeterV2GetObject();
	
	public Meter getMeterV2PutObject(String bucket);
	public Meter getMeterV2GetObject(String bucket);
	
	public Counter getCounterV2KBFSCacheHit();
	public Counter getCounterV2KBFSCacheMiss();
	
	public Meter getMeterV2ShardPutObject(String shard);
	public Meter getMeterV2ShardGetObject(String shard);
	

	
	// -----------------------------------------
	// Odilon
	//
	// File Server Put and Get rates
	public Meter getMeterOdilonPutObject(String bucket);
	public Meter getMeterOdilonGetObject(String bucket);
	
	public Meter getMeterOdilonShardPutObject(String shard);
	public Meter getMeterOdilonShardGetObject(String shard);
	
	
	public Counter getCounterOdilonCacheHit();
	public Counter getCounterOdilonCacheMiss();
	
	public Meter getMeterOdilonPutObject();
	public Meter getMeterOdilonGetObject();
	
	
	
	
	
	
	
	
	//-------------------------------------------
	
	public Meter getMeterV1GetObject();
	public Meter getMeterV1GetObject(String bucketName);
						
	public Meter getMeterV1PutObject();
	public Meter getMeterV1PutObject(String bucketName);

	

	//-------------------------------------------
	
	// Active users logged  (x Dominio y Global)
	public Counter getCounterUsersLogged();
	public Counter getCounterUsersLogged(Serializable domain_id);

	// Login rate  (x Dominio y Global)
	public Meter getMeterLogin();
	public Meter getMeterLogin(Serializable domain_id);

	// Emails (x Dominio y Global)
	public Meter getMeterEmails();
	public Meter getMeterEmails(Serializable domain_id);

	// Memory usage
	public GarbageCollectorMetricSet getJVMGarbageCollectorMetricSet();
	public MemoryUsageGaugeSet getJVMMemoryUsageGaugeSet();

	// Web pages rate - Global
	public Meter getMeterWebPages();
	
	// All gauge
	@SuppressWarnings("rawtypes")
	public SortedMap<String, Gauge> getGauges();

	// EH Cache Gauge
	@SuppressWarnings("rawtypes")
	public SortedMap<String, Gauge> getEHGauges();

	public MetricRegistry getMetrics();
	public void setDataSource(DataSource dataSource);
	public DataSource getDataSource();
	public void setSessionFactory(SessionFactory sessionFactory);
	public SessionFactory getSessionFactory();

	public void mark(String string, Serializable id);
	public void inc(String string, Serializable id);
	public void dec(String string, Serializable id);
	
	public Meter getMeterContentCheckin();
	public Meter getMeterContentCheckin(Serializable domain_id);
	public Meter getMeterContentAPICheckin();
	public Meter getMeterContentAPICheckin(Serializable domain_id);
	public Meter getMeterPortalPages();

	//
	// Index jobs
	//
	public Meter getMeterIndexTasks();
	public Meter getMeterIndexAttachmentsTasks();
	public Meter getMeterIndexMetainfoTasks();
	
	// API
	//
	public Meter getMeterExternalPutObject();
	public Meter getMeterExternalGetObject();

	// API
	public Meter getMeterAPIRequestsIn();
	public Meter getMeterAPIRequestsOut();

	public Meter getMeterAPITrafficeQueueIn();
	public Meter getMeterAPITrafficeQueueOut();

	public Meter getMeterAPIGet();
	
	public Counter getCounterTrafficQueueSize();
	
	public MeanEstimator getTrafficInQueueEstimator();
	public MeanEstimator getRequestProcessingTimeEstimator();

 	public  void resetOutOfMemoryFlag();
	public  boolean isOutOfMemoryFlag();
	public  long timeOutOfMemoryFlag();
	public  void setTimeOutOfMemoryFlag(long timeinmillisecs);
	public  void setTimeOutOfMemoryFlag();
	
	
	
	/**
	 * <p>Commands executed by the thread pool</p>
	 */
	public Meter getMeterCommandsStartExecution();
	
	/**
	 * <p>Commands registered in the CommandService</p>
	 */
	public Meter getMeterCommandsIn();
	
	/**
	 * <p>Commands that tell the Service that they finished.
	 * Sometimes Commands "forget" to notify the Service they are done.
	 * </p>
	 */
	public Meter getMeterCommandsTerminated();
	public Meter  getSearchSubmitMeter();
 	
	
}
