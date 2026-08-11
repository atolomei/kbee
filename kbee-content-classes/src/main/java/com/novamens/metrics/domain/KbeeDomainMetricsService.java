package com.novamens.metrics.domain;



import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;

import com.novamens.beans.BeansService;
import com.novamens.cache.SelfExpiringHashMap;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.library.Library;
import com.novamens.dom.Domain;
import com.novamens.dom.KBFSStorageType;
import com.novamens.event.Event;
import com.novamens.event.EventListener;
import com.novamens.kbee.event.EvictCacheServiceEvent;
import com.novamens.kbee.logging.usage.UsageStatService;
import com.novamens.logging.usage.KbeeUsageStat;
import com.novamens.logging.usage.UsageStat;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeRuntimeException;
import kbee.util.PropertiesFactory;

/** 
 *
 */
public class KbeeDomainMetricsService implements DomainMetricsService, EventListener {
											
	static private long REFRESH_RATE = 1000 * 60 * 60 * 24; 					//  1 day
	static private long HD_USAGE_REFRESH_RATE = 1000 * 60 * 60 * 24 * 7; 	    //  7 days
	static private final int MINUTES_240 = 1000 * 60 * 240;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeDomainMetricsService.class.getName());
 
	static final boolean STATS_DISABLED = PropertiesFactory.getInstance("kbee").getProperties().getProperty("domain.stats", "enabled").trim().toLowerCase().equals("enabled"); 
			
	static private final	long secs_per_day = 60 * 60 * 24;

	static	private final double alpha = 0.1;
	static	private final double beta = 0.9;

	// TODO HA
	private Map<String, DomainData> map = new ConcurrentHashMap<String,DomainData>();
	
	// Global Data							
	private SelfExpiringHashMap<String, Long> domdata = new SelfExpiringHashMap<String, Long>(MINUTES_240);

	int total_db_head_enabled_contents;
	int total_solr_head_enabled_contents;
	OffsetDateTime health_check;

	
	public KbeeDomainMetricsService() {
	}
	

	@Override
	public OffsetDateTime getHealthCheckOffsetDateTime() {
		return this.health_check;
	}
	
	@Override
	public int getTotalSolrHeadEnabledContents() {
		return this.total_solr_head_enabled_contents;
	}
	
	@Override
	public int getTotalDBHeadEnabledContents() {
		return this.total_db_head_enabled_contents;
	}
	
	@Override
	public void setDBSolrCheck(int db, int solr, OffsetDateTime ts) {
		total_db_head_enabled_contents = db;
		total_solr_head_enabled_contents = solr;
		health_check = ts;
	}
			
	
	@Override
	public long getUsers(Domain domain) {
		return getDD(domain).users;
	}

	@Override
	public long getResources(Domain domain) {
		return getDD(domain).resources;
	}

	@Override
	public long getContents(Domain domain) {
		return getDD(domain).contents;
	}

	@Override
	public long getHardDisk(Domain domain) {
		return getDD(domain).harddisk_total;
	}
	

	@Override
	public long getTimeMeasure(Domain domain) {
		return getDD(domain).time_measured;
	}
	
	
	@Override
	public long getHardDisk(Domain domain, KBFSStorageType s) {
		
		if (s==KBFSStorageType.External)			return getDD(domain).harddisk_gateway;
		if (s==KBFSStorageType.KBFS1)				return getDD(domain).harddisk_kbfs1;
		if (s==KBFSStorageType.Minio)				return getDD(domain).harddisk_kbfs2;
		if (s==KBFSStorageType.Odilon)             return getDD(domain).harddisk_odilon;
		
		if (s==KBFSStorageType.MinioArchive)		return getDD(domain).harddisk_kbfs2archive;
		if (s==KBFSStorageType.AmazonS3)		return getDD(domain).harddisk_s3;
		return 0;
	}
	
	
	@Override
	public DomainData getDomainData(Domain domain) {
		throw new KbeeRuntimeException("error getDomainData");
	}
	
	 
	private ContentDao getContentDao() {
		BeansService beans = ServiceLocator.getService(BeansService.class);
		ContentDao dao = (ContentDao) beans.getBean("contentDao");
		return dao;
	}


	private void estimateHardDiskUsage(DomainData data, Domain domain) {
 		
		long now = System.currentTimeMillis();
		
		try {
			
			if (data.time_estimated_hd_grow==0 || (now - data.time_estimated_hd_grow)>HD_USAGE_REFRESH_RATE) {
													
				data.time_estimated_hd_grow=now; 	// when growth estimate was calculated
				data.limit_bytes = 0;  				// threshold to compare to, to determine if the system is going to hit 
				data.avg_daily_increase = 0.0;      // estimation based on last 100 measures  
				data.avg_month_increase = 0.0;      // avg_daily_increase * 30 
				data.limit_no_reachable = true;     // true if it will never reach limit_bytes 
				data.time_estimated_hd_grow = now;
				
				UsageStatService service = (UsageStatService) ServiceLocator.getService(BeansService.class).getBean("usageStatService");
				
				// use the last 1000 measurements
				//
				List<UsageStat> list = service.getUsageStat(domain.getId(), 1000);
				
				Collections.sort(list, new Comparator<UsageStat>() {
						@Override
						public int compare(UsageStat a, UsageStat b) {
							try {
							return a.getTimeStamp().compareTo(b.getTimeStamp());
							} catch (Exception e) {
								return 0;
							}
						}
				}); 
			  		
			 	int size = list.size();
	
			 	double x_secs[] 	= new double[size];
			 	double y_hardrive[] = new double[size];
			 		
			 	int nSzie=0;
			 	
			 	for (UsageStat stat: list) {
			 		if (nSzie>0) {
			 			double d = Double.valueOf(stat.getTimeStamp().toEpochSecond()).doubleValue();
			 			if (x_secs[nSzie]>0.0 && d>x_secs[nSzie-1]) {
					 		x_secs[nSzie]= d;
					 		y_hardrive[nSzie]=stat.getHardDisk();
			 			}
			 		}
			 		else {
				 		x_secs[nSzie]= Double.valueOf(stat.getTimeStamp().toEpochSecond()).doubleValue();
				 		y_hardrive[nSzie]=stat.getHardDisk();
				 		nSzie++;
			 		}
			 	}
	
			 		// if there are less than 10 points or the time span is less than one day, do not estimate
			 		//
					if (nSzie<10) {
						data.limit_no_reachable=true;
			  			data.limit_bytes = 0;
			  		    data.avg_month_increase	= 0;
			  		    logger.debug("Calculation time for "+ domain.getName() + ": " + String.valueOf( System.currentTimeMillis()-now)+" ms");
			  			return;
			 		}
	
					try {
						org.apache.commons.math3.analysis.interpolation.LinearInterpolator interpolator = new LinearInterpolator();
				 	 	PolynomialSplineFunction  function = interpolator.interpolate(x_secs, y_hardrive);
				 	 	
				 	 	if (logger.isDebugEnabled()) {
				 	 		for (int i=0; i<nSzie; i++)
				 	 			logger.info(x_secs[i] + " - " + y_hardrive[i]);	
				 	 	}
				 	 	

				 	 	// take sample_size points and the estimators are: 
				 	 	// Mean, Moving Average
		
				 	 	int sample_size = (x_secs.length<10000?x_secs.length:10000);
				 	 	int effective_sample_size = 0;
				 	 	
				 	 	double sum = 0.0;		 	 		
				 	 	
				 	 	double ma_mean_prev= 0.0;
				 	 	double ma_mean = 0.0;
				 	 		
				 	 	int start = 1;
				 	 	
				 	 	for (int st=start; st<sample_size; st++) {
				 	 		
				 	 		if( (x_secs[st] - x_secs[st-1])>60) {
				 	 			double lapse_secs = (x_secs[st] - x_secs[st-1]);
						 	 	double gr_i = (function.value(x_secs[st]) - function.value(x_secs[st-1])) / lapse_secs;  // bytes / sec
						 	 	sum  += gr_i;
					 	 		effective_sample_size++;
								if (st==start)  
									ma_mean_prev = gr_i;
								ma_mean = alpha * gr_i + beta * ma_mean_prev;
								ma_mean_prev= ma_mean;
				 	 		}
				 	 	}
				 	 	
				 	 	if (effective_sample_size>0) {
					 	 	double gr_mean_day =  sum * ((double) secs_per_day) / effective_sample_size;
					 	 	data.avg_daily_increase = gr_mean_day;
					 	 	data.avg_month_increase = gr_mean_day * 30;
					 	 	data.avg_month_increase_ma = ma_mean * 30 * ((double) secs_per_day);
				 	 	}

					} catch (Exception e) {
						data.time_estimated_hd_grow=now;
						
						logger.error(e);
						
						if (logger.isDebugEnabled()) {
							if (domain !=null && domain.getName()!=null)
								logger.debug(domain.getName());

							if (x_secs!=null && y_hardrive!=null) {
								for (int i=0; i<nSzie; i++)
									logger.debug(x_secs[i] + " - " + y_hardrive[i]);
							}
						}
					}
			}
			logger.debug(domain.getName() + " 30d growth rate. AVG: " +  String.valueOf(data.avg_month_increase / 1000000) + " mb -  MA:" + String.valueOf(data.avg_month_increase_ma / 1000000) + " mb" + " | time: " + String.valueOf( System.currentTimeMillis()-now)+" ms");
		}
		catch (RuntimeException e) {
			logger.error(e);
			data.time_estimated_hd_grow=now;
		}
	}		
	

	
	@Override
	public synchronized void forceCalculate(Domain domain) {
			calculateData(getDD(domain), domain);
	}

	OffsetDateTime last_calculate_all  = null;
	
	@Override
	public synchronized void forceCalculateAll() {
		if ( (last_calculate_all==null) || (OffsetDateTime.now().isAfter(last_calculate_all.plusMinutes(5)))) { 
			for (Domain don: getContentDao().getDomains()) {
				calculateData(getDD(don), don);
			}
			last_calculate_all=OffsetDateTime.now();
		}
	}
	
	/**
	 * @param data
	 * @param domain
	 */
	private synchronized void calculateData(DomainData data, Domain domain) {

		UsageStatService service = (UsageStatService) ServiceLocator.getService(BeansService.class).getBean("usageStatService");
		 
		List<UsageStat> l_stat = service.getUsageStat(domain.getId());
		
		boolean done = false;
		
		if (l_stat!=null && l_stat.size()>0) {
			
			UsageStat stat = l_stat.get(0);
			
			if (stat.getTimeStamp().isAfter(OffsetDateTime.now().minusDays(7))) {
				
				data.harddisk_gateway 		= stat.getGatewayHardDisk();
				 
				data.harddisk_kbfs2 		= stat.getKBFS2HardDisk();
				data.harddisk_odilon        = stat.getOdilonHardDisk();
				data.harddisk_kbfs2archive 	= stat.getKBFS2ArchiveHardDisk(); 
				data.users					= stat.getUsers();
				data.contents				= stat.getContents();
				data.contents_external		= stat.getExternalContents();
								
				data.contents_external_library		= stat.getExternalLibraryContents();
				data.contents_external_archive		= stat.getExternalArchiveContents();
				data.contents_external_recycle		= stat.getExternalRecycleContents();
				
				
				data.harddisk_s3					= stat.getS3HardDisk();
				data.harddisk_glacier				= stat.getGlacierHardDisk();
				
				
				data.resources 					= stat.getResources();
				data.harddisk_total				= stat.getHardDisk();
				data.time_measured				= System.currentTimeMillis();
				done = true;
			}
		}

		if (!done) {
			try {

				UsageStat stat = new KbeeUsageStat();
				
				stat.setDomainId(domain.getId());
				stat.setUsers(getContentDao().getTotalUsers(domain));

				stat.setResources(getContentDao().getTotalResources(domain));
				
				stat.setExternalContents(getContentDao().getTotalContents(domain, Library.EXTERNAL));
				stat.setExternalArchiveContents(getContentDao().getTotalExternalArchiveContents(domain));
				stat.setExternalLibraryContents(getContentDao().getTotalExternalLibraryContents(domain));
				stat.setExternalRecycleContents(getContentDao().getTotalExternalRecycleContents(domain));
				
				// TOTAL Stored: KBFS1, KBFS2, KBFSArchive (excluding external)
				stat.setHardDisk(getContentDao().getTotalHardDisk(domain));
				stat.setGatewayHardDisk(getContentDao().getTotalHardDisk(domain,  KBFSStorageType.External));
				 
				stat.setKBFS2HardDisk(getContentDao().getTotalHardDisk(domain, KBFSStorageType.Minio));
                stat.setOdilonHardDisk(getContentDao().getTotalHardDisk(domain, KBFSStorageType.Odilon));
                
				stat.setKBFS2ArchiveHardDisk(getContentDao().getTotalHardDisk(domain, KBFSStorageType.MinioArchive));
				
				stat.setS3HardDisk(getContentDao().getTotalHardDisk(domain, KBFSStorageType.AmazonS3));
				stat.setGlacierHardDisk(getContentDao().getTotalHardDisk(domain, KBFSStorageType.AmazonGlacier));
				
				
				data.users						= stat.getUsers();
				data.contents					= stat.getContents(); // all versions
				data.contents_external			= stat.getExternalContents(); // all versions
				data.contents_external_archive 	= stat.getExternalArchiveContents(); // head  in Archive
				data.contents_external_library 	= stat.getExternalLibraryContents(); // head  in library
				data.contents_external_recycle 	= stat.getExternalRecycleContents(); // head  in Recycle
				
				data.resources					= stat.getResources();
				data.harddisk_gateway			= stat.getGatewayHardDisk();
				data.harddisk_kbfs2				= stat.getKBFS2HardDisk();
				data.harddisk_odilon            = stat.getOdilonHardDisk();
				data.harddisk_kbfs2archive		= stat.getKBFS2ArchiveHardDisk();
				data.harddisk_total				= stat.getHardDisk();

				data.harddisk_s3  					= stat.getS3HardDisk();
				data.harddisk_glacier				= stat.getGlacierHardDisk();
				
				
				// TOTAL Stored by KBEE/RPDD: KBFS1, KBFS2, KBFSArchive
				data.harddisk_total				= stat.getHardDisk();
				data.time_measured=System.currentTimeMillis();

				data.billableUsers = stat.getBillableUsers();
				//data.billableSites = stat.getBillableSites();
				//data.units = stat.getUnits();
				
				logger.debug("Saving domain_id: "+ stat.getDomainId().toString());
				service.save(stat);
			
			} catch (Exception e) {

				logger.error(e);
				
				data.users					= getContentDao().getTotalUsers(domain);
				data.contents				= getContentDao().getTotalContents(domain);
				data.resources				= getContentDao().getTotalResources(domain);
				data.harddisk_gateway		= getContentDao().getTotalHardDisk(domain, KBFSStorageType.External);
				data.harddisk_kbfs1			= getContentDao().getTotalHardDisk(domain, KBFSStorageType.KBFS1);
				data.harddisk_kbfs2			= getContentDao().getTotalHardDisk(domain, KBFSStorageType.Minio);
				data.harddisk_odilon        = getContentDao().getTotalHardDisk(domain, KBFSStorageType.Odilon);
				data.harddisk_kbfs2archive	= getContentDao().getTotalHardDisk(domain, KBFSStorageType.MinioArchive);
				data.harddisk_s3			= getContentDao().getTotalHardDisk(domain, KBFSStorageType.AmazonS3);
				data.harddisk_glacier  		= getContentDao().getTotalHardDisk(domain, KBFSStorageType.AmazonGlacier);
				
				// TOTAL Stored: KBFS1, KBFS2, KBFSArchive (excluding external)
				//
				data.harddisk_total=getContentDao().getTotalHardDisk(domain);
				data.time_measured=System.currentTimeMillis();
			}
		}
		
		estimateHardDiskUsage(data, domain);
	}
	
	
	private DomainData getDD(Domain domain) {
		return getDD(domain, false);
	}
	
	/** 
	  
	 */
    private DomainData getDD(Domain domain, boolean calculateIfNotExists) {
    					
		String key = domain.getId().toString();
		
    	if (!map.containsKey(key)) {
    		DomainData data = new DomainData();
	    	if (calculateIfNotExists)
	    		 calculateData(data, domain);
	    	map.put(key, data);
    	} 
    	
    	long now = System.currentTimeMillis();
    	
    	if ((now-map.get(key).time_measured)>REFRESH_RATE) {
    		DomainData data = map.get(key);
    		calculateData(data, domain);
    	}
    	
    	return map.get(key);
    }

    
	@Override
	public double getMeanHardDiskIncrease30d(Domain domain) {
		return getDD(domain).avg_month_increase;
	}
	
	
	@Override
	public OffsetDateTime getDateReachLimit(Domain domain) {
		
		
		long ml=getDD(domain).time_reach_limit_milisecs;
		Instant instant = Instant.ofEpochMilli(ml);
	    LocalDateTime date = instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
	    OffsetDateTime dt = OffsetDateTime.of(date, OffsetDateTime.now().getOffset());
		return dt;
	}

	@Override
	public boolean isDateLimit(Domain domain) {
			return getDD(domain).limit_no_reachable;
	}


	@Override
	public double getMeanHardDiskIncreaseMA30d(Domain domain) {
		return getDD(domain).avg_month_increase_ma;
	}
	
	
	public void evict() {
		map.clear();
		this.domdata.clear();
	}
	
	@Override
	public boolean listen(Event event) {
		if (event instanceof EvictCacheServiceEvent)
			return true;
		return false;
	}

	@Override
	public void onEvent(Event event) {
			logger.debug(Thread.currentThread().getStackTrace()[1].getMethodName() + " | " + event.getClass().getName());
			if (event instanceof EvictCacheServiceEvent) {
				evict();
			}
	}


	public long getTotalContents() {
		try {
			if (domdata.containsKey("total-contents")) 
				return domdata.get("total-contents").longValue();
			domdata.put("total-contents", Long.valueOf(getContentDao().getTotalContents()));
			return domdata.get("total-contents").longValue();
		} catch (Exception e) {																					
			logger.error(e);
			return 0L;
		}
	}
	
	/**
	 * 
	 */
	public long getTotalResources()	{
		try {
			String key ="total-resources";
			if (domdata.containsKey(key))
				return domdata.get(key).longValue();
			domdata.put(key, Long.valueOf(getContentDao().getTotalResources()));
			return domdata.get(key).longValue();
		} catch (Exception e) {																					
			logger.error(e);
			return 0L;
		}
	}

	/**
	 * 
	 */
	public long getTotalUsers() {
			try {
				String key ="total-users";
				if (domdata.containsKey(key))
					return domdata.get(key).longValue();
				domdata.put(key, Long.valueOf(getContentDao().getTotalUsers()));
				return domdata.get(key).longValue();
			} catch (Exception e) {																					
				logger.error(e);
				return 0L;
			}
	}

	/**
	 * 
	 */
	public long getTotalResources(KBFSStorageType s) {
		try {
			String key ="total-resources-"+s.getKey();
			if (domdata.containsKey(key))
				return domdata.get(key).longValue();
			domdata.put(key, Long.valueOf(getContentDao().getTotalResources(s)));
			return domdata.get(key).longValue();
		} catch (Exception e) {																					
			logger.error(e);
			return 0L;
		}
	}
	

	/**
	 * 
	 */
	public long getTotalResources(KBFSStorageType s, Integer shard) {
		try {
			String key ="total-resources-"+s.getKey()+"-"+String.valueOf(shard);
			if (domdata.containsKey(key))
				return domdata.get(key).longValue();
			domdata.put(key, Long.valueOf(getContentDao().getTotalResources(s, shard.intValue())));
			return domdata.get(key).longValue();
		} catch (Exception e) {																					
			logger.error(e);
			return 0L;
		}
		
	}

	/**
	 * 
	 */
	public long getTotalStoredHardDisk() {
		try {
			String key ="total-hard-disk";
			if (domdata.containsKey(key))
				return domdata.get(key).longValue();
			domdata.put(key, Long.valueOf(getContentDao().getTotalStoredHardDisk()));
			return domdata.get(key).longValue();
		} catch (Exception e) {																					
			logger.error(e);
			return 0L;
		}
	}
	
	/**
	 * 
	 */
	public long getTotalEncryptedResources() {
		try {
			String key ="total-encrypted-resources";
			
			if (domdata.containsKey(key))
				return domdata.get(key).longValue();
			
			domdata.put(key, Long.valueOf(getContentDao().getTotalEncryptedResources()));
			
			return domdata.get(key).longValue();
			
		} catch (Exception e) {																					
			logger.error(e);
			return 0L;
		}
	}

	
	
	/**
	 * 
	 */
	public long getTotalHardDisk(KBFSStorageType s) {
		try {
			String key ="total-hard-disk"+s.getKey();
			if (domdata.containsKey(key))
				return domdata.get(key).longValue();
			domdata.put(key, Long.valueOf(getContentDao().getTotalHardDisk(s)));
			return domdata.get(key).longValue();
		} catch (Exception e) {																					
			logger.error(e);
			return 0L;
		}
	}

	public long getTotalHardDisk(KBFSStorageType s, Integer shard) {
		try {
			String key ="total-hard-disk"+s.getKey()+"-"+String.valueOf(shard.intValue());
			if (domdata.containsKey(key))
				return domdata.get(key).longValue();
			domdata.put(key, Long.valueOf(getContentDao().getTotalHardDisk(s, shard)));
			return domdata.get(key).longValue();
		} catch (Exception e) {																					
			logger.error(e);
			return 0L;
		}

		
	}
	
	protected User getSessionUser() {
		try {
			return ServiceLocator.getService(SecurityService.class).getSessionUser();
		} catch (Exception e) {
			return null;
		}
	}



}
