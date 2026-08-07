package com.novamens.kbee.metrics;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.engine.spi.SessionImplementor;


import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ehcache.InstrumentedEhcache;
import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.novamens.kbee.sql.SqlPlatform;
import com.novamens.kbee.sql.SqlPlatformFactory;
import com.novamens.metrics.MeanEstimator;
import com.novamens.metrics.SystemMetricsService;
import com.novamens.util.KbeeRuntimeException;

import net.sf.ehcache.CacheManager;


/**
 *
 *
 *
 */
public class KbeeSystemMetricsService implements SystemMetricsService {

	static private org.apache.logging.log4j.Logger startupLogger = LogManager.getLogger("StartupLogger");
	static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeSystemMetricsService.class.getName());
	
	private DataSource dataSource;
	private SessionFactory sessionFactory;

	final MetricRegistry metrics = new MetricRegistry();
	final HealthCheckRegistry healthChecks = new HealthCheckRegistry();
	
	// JVM MetricsSets
	private GarbageCollectorMetricSet garbageCollectorMetricSet; 
	private MemoryUsageGaugeSet memoryUsageGaugeSet;

	// Login. Cantidad de Logins x unidad de tiempo, cantidad de usuarios con sesiones activas
	// para todos los dominios
	private Map<Serializable, Meter> map_metric_login  = new ConcurrentHashMap<Serializable, Meter>();
	private Meter metric_login = null;

	// KBFS 1  
	private Map<String, Meter> map_metric_v1_putobject  = new ConcurrentHashMap<String, Meter>();
	private Meter metric_v1_putobject = null;

	private Map<String, Meter> map_metric_v1_getobject  = new ConcurrentHashMap<String, Meter>();
	private Meter metric_v1_getobject = null;

	// KBFS 2 global
	private Meter metric_putobject = null;
	private Meter metric_getobject = null;

	// KBFS 2 externo global
	private Meter metric_external_putobject = null;
	private Meter metric_external_getobject = null;
	
	
	/** ----------
	 *  S3
	 */
	// S3 x Domain
	private Map<String, Meter> map_metric_s3_putobject  = new ConcurrentHashMap<String, Meter>();
	private Map<String, Meter> map_metric_s3_getobject  = new ConcurrentHashMap<String, Meter>();

	// Amazon S3
	private Meter metric_s3_putobject = null;
	private Meter metric_s3_getobject = null;
						
	// Vault
	private Meter metric_vault_encrypt = null;
	private Meter metric_vault_decrypt = null;
	
	public Meter getMeterVaultEncrypt() { return metric_vault_encrypt;}
	public Meter getMeterVaultDeEncrypt() { return metric_vault_decrypt;}

	// Contador de s3 cache hits
	private Counter s3_cache_hits = null;
	private Counter s3_cache_miss = null;

	/** ----------
	 *  KBFS2
	 */
	// KBFS 2 x Domain
	private Map<String, Meter> map_metric_putobject  = new ConcurrentHashMap<String, Meter>();
	private Map<String, Meter> map_metric_getobject  = new ConcurrentHashMap<String, Meter>();
	
	// KBFS 2 x Shard						
	private Map<String, Meter> map_metric_shard_putobject  = new ConcurrentHashMap<String, Meter>();
	private Map<String, Meter> map_metric_shard_getobject  = new ConcurrentHashMap<String, Meter>();

	// Contador de KBFS cache hits
	private Counter kbfs_cache_hits = null;
	private Counter kbfs_cache_miss = null;


	/** ----------
	 *  Odilon
	 */
	// Odilon 2 x Domain
	private Map<String, Meter> map_metric_odilon_putobject  = new ConcurrentHashMap<String, Meter>();
	private Map<String, Meter> map_metric_odilon_getobject  = new ConcurrentHashMap<String, Meter>();
	
	// Odilon 2 x Shard						
	private Map<String, Meter> map_metric_odilon_shard_putobject  = new ConcurrentHashMap<String, Meter>();
	private Map<String, Meter> map_metric_odilon_shard_getobject  = new ConcurrentHashMap<String, Meter>();

	// Contador de Odilon cache hits
	private Counter odilon_cache_hits = null;
	private Counter odilon_cache_miss = null;
	
	// Contador de usuarios logeados
	private Map<Serializable, Counter> map_users_logged  = new ConcurrentHashMap<Serializable, Counter>();
	private Counter users_logged = null;

	// Tareas de Workflow y Publicaci#n/despublicaci#n contenidos
	// private Map<Serializable, Meter> map_metric_workflow_tasks  = new ConcurrentHashMap<Serializable, Meter>();
	// private Meter metric_workflow_tasks  = null;
	
	// Emails enviados
	private Map<Serializable, Meter> map_metric_emails  = new ConcurrentHashMap<Serializable, Meter>();
	private Meter metric_emails = null;

	// Actividad de p#ginas web (p#ginas Wicket servidas)
	// private Map<Serializable, Meter> map_metric_web_pages  = null;
	private Meter metric_web_pages = null;
						
	private Meter metric_portal_pages = null;
	
	private Meter metric_search_submit = null;
	
	
	// Indexer tasks
	private Meter indexer_jobs = null;
	private Meter indexer_attachmets_jobs = null;
	private Meter indexer_metainfo_jobs = null;

					
	private Meter commands_start_execution = null;
	private Meter commands_in = null;
	private Meter commands_terminated = null;

	public Meter getMeterCommandsStartExecution() {return commands_start_execution;}
	public Meter getMeterCommandsIn() { return commands_in;}
	public Meter getMeterCommandsTerminated() { return  commands_terminated;} 

	

	// ----------------------------------------------------
	// Cantidad de Contenidos Publicados Total (1h 1d 1w)
	//
	private Map<Serializable, Meter> map_content_checkin  = new ConcurrentHashMap<Serializable, Meter>();
	private Meter metric_content_checkin = null;

	// ----------------------------------------------------
	// Cantidad de Contenidos Publicados API (1h 1d 1w)
	//
	private Map<Serializable, Meter> map_content_api_checkin  = new ConcurrentHashMap<Serializable, Meter>();
	private Meter metric_content_api_checkin = null;

	
	// API GET recibidos
	private Meter metric_api_gets = null;
	
	// Requests Recibidos (no incluye GET)
	private Meter metric_api_requests_in = null;
	
	// Requests Completados
	private Meter metric_api_requests_out = null;

	// evento de agregar un request a la cola del traffic controller
	private Meter metric_api_traffic_queue_in = null;

	// evento de sacar un request a la cola del traffic controller
	private Meter metric_api_traffic_queue_out = null;
	

	// evento de agregar un request a la cola del traffic controller
	private Counter counter_api_traffic_queue_size= null;

	/*
	// OutOfMemory
	private SortedMap<OffsetDateTime, String> map_out_of_memory  = new  ConcurrentSkipListMap<>( new Comparator<OffsetDateTime>() {
		@Override
        public int compare(OffsetDateTime s1, OffsetDateTime s2) {
            return s1.isAfter(s2) ? -1:1;
        }
	});
	
		
	@Override
	public  SortedMap<OffsetDateTime, String> getOutOfMemoryMap() {
		return this.map_out_of_memory;
	}
	
	@Override
	public  void resetOutOfMemoryMap() {
		this.map_out_of_memory.clear();
	}
	
	@Override
	public  void putOutOfMemoryError(OffsetDateTime time, String errorMsg) {
		this.map_out_of_memory.put(time,errorMsg);
	}
	*/
	
	AtomicLong  time_out_of_memory = new AtomicLong(0);
 	
	@Override
	public  void resetOutOfMemoryFlag() {
		 time_out_of_memory = new AtomicLong(0);
 	}
	
	@Override
	public  boolean isOutOfMemoryFlag() {
		 return time_out_of_memory.get()>0;
	}
	
	@Override
	public  long timeOutOfMemoryFlag() {
		 return time_out_of_memory.get();
	}
	
	@Override
	public  void  setTimeOutOfMemoryFlag(long timeinmillisecs) {
		 time_out_of_memory.set(timeinmillisecs);
	}

	@Override
	public  void  setTimeOutOfMemoryFlag() {
		 time_out_of_memory.set(System.currentTimeMillis());
	}
	
	
 	/** 
	 * 
	 */
	public KbeeSystemMetricsService() {
		startupLogger.info("Starting SystemMetricsService");
		startEHCacheMetrics();
		startJVMMetrics();
		init();
		
		// DatabaseHealthCheck dbck = new DatabaseHealthCheck();
		// healthChecks.register("database", dbck);
	}
	
	@Override
	public GarbageCollectorMetricSet getJVMGarbageCollectorMetricSet() {
		return garbageCollectorMetricSet;
	}
	
	@Override
	public MemoryUsageGaugeSet getJVMMemoryUsageGaugeSet() {
		return memoryUsageGaugeSet;
	}
	

	
	@SuppressWarnings("rawtypes")
	@Override
	public SortedMap<String, Gauge> getGauges() {
		return getMetrics().getGauges();
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public SortedMap<String, Gauge> getEHGauges() {
		final String eh_name = "net.sf.ehcache.Cache.";
		MetricFilter filter = new MetricFilter() {
			@Override
			public boolean matches(String name, Metric metric) {
				return (metric instanceof Gauge && name.startsWith(eh_name));
			}
		};
		return getMetrics().getGauges(filter);
	}
	

	@Override
	public MetricRegistry getMetrics() {
		return metrics;
	}
	
	@Override
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	@Override
	public DataSource getDataSource() {
		return this.dataSource;
	}
	
	@Override
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	@Override
	public SessionFactory getSessionFactory() {
		return this.sessionFactory;
	}

	
	private void startJVMMetrics() {
		garbageCollectorMetricSet = new GarbageCollectorMetricSet();  
		getMetrics().registerAll(garbageCollectorMetricSet);
	}
	
	private void startEHCacheMetrics() {
		for (CacheManager manager : CacheManager.ALL_CACHE_MANAGERS) {
			for (String str: manager.getCacheNames()) {
				InstrumentedEhcache.instrument(getMetrics(), manager.getCache(str));
			}
		 }	
	}
	
	
	@SuppressWarnings("unused")
	private SqlPlatform getSqlPlatform() {
		Connection connection = null;
		try {
			connection = getDataSource().getConnection();
			return SqlPlatformFactory.getPlatformFor(connection.getMetaData());
		}
		catch (SQLException e) {
			logger.error(e);
			throw new KbeeRuntimeException(e);
		}
		finally {
			if (connection!=null) {
				try {
					connection.close();
				}				
				catch (SQLException e) {
					logger.error(e);
					throw new KbeeRuntimeException(e);
				}
			}
		}
	}

	

	private void init() {


		this.odilon_cache_miss = getMetrics().counter(MetricRegistry.name(KbeeSystemMetricsService.class, "odilon-cache-miss"));
		this.odilon_cache_hits = getMetrics().counter(MetricRegistry.name(KbeeSystemMetricsService.class, "odilon-cache-hits"));

			
		
		this.metric_vault_encrypt = getMetrics().meter(MetricRegistry.name(KbeeSystemMetricsService.class, "vault-encrypt"));
		this.metric_vault_decrypt = getMetrics().meter(MetricRegistry.name(KbeeSystemMetricsService.class, "valut-decrypt"));
		
		this.metric_s3_putobject = getMetrics().meter(MetricRegistry.name(KbeeSystemMetricsService.class, "s3-putobject"));
		this.metric_s3_getobject = getMetrics().meter(MetricRegistry.name(KbeeSystemMetricsService.class, "s3-getobject"));

		this.s3_cache_miss = getMetrics().counter(MetricRegistry.name(KbeeSystemMetricsService.class, "s3-cache-miss"));
		this.s3_cache_hits = getMetrics().counter(MetricRegistry.name(KbeeSystemMetricsService.class, "s3-cache-hits"));
		
		
		this.users_logged = getMetrics().counter(MetricRegistry.name(KbeeSystemMetricsService.class, "users-logged"));
		this.metric_login = getMetrics().meter(MetricRegistry.name(KbeeSystemMetricsService.class, "login"));
		
		this.indexer_jobs = getMetrics().meter(MetricRegistry.name(KbeeSystemMetricsService.class, "indexer-tasks"));
		this.indexer_attachmets_jobs = getMetrics().meter(MetricRegistry.name(KbeeSystemMetricsService.class, "indexer-attachment-tasks"));
		this.indexer_metainfo_jobs = getMetrics().meter(MetricRegistry.name(KbeeSystemMetricsService.class, "indexer-metainfot-tasks"));
		
		this.commands_start_execution = getMetrics().meter(MetricRegistry.name(KbeeSystemMetricsService.class, "commands-start-execution"));
		this.commands_in = getMetrics().meter(MetricRegistry.name(KbeeSystemMetricsService.class, "commands-in"));
		this.commands_terminated =getMetrics().meter(MetricRegistry.name(KbeeSystemMetricsService.class, "commands-terminated")); 

		
		
		this.kbfs_cache_miss = getMetrics().counter(MetricRegistry.name(KbeeSystemMetricsService.class, "kbfs-cache-miss"));
		this.kbfs_cache_hits = getMetrics().counter(MetricRegistry.name(KbeeSystemMetricsService.class, "kbfs-cache-hits"));
		
		this.metric_putobject = getMetrics().meter(MetricRegistry.name(KbeeSystemMetricsService.class, "v2-putobject"));
		this.metric_getobject = getMetrics().meter(MetricRegistry.name(KbeeSystemMetricsService.class, "v2-getobject"));
		
		this.metric_v1_putobject = getMetrics().meter(MetricRegistry.name(KbeeSystemMetricsService.class, "v1-putobject"));
		this.metric_v1_getobject = getMetrics().meter(MetricRegistry.name(KbeeSystemMetricsService.class, "v1-getobject"));

		this.metric_external_putobject = getMetrics().meter(MetricRegistry.name(KbeeSystemMetricsService.class, "gateway-putobject"));
		this.metric_external_getobject = getMetrics().meter(MetricRegistry.name(KbeeSystemMetricsService.class, "gateway-getobject"));
		
						
		this.metric_api_requests_in = getMetrics().meter(MetricRegistry.name(KbeeSystemMetricsService.class, "api-requests-in"));
		this.metric_api_requests_out = getMetrics().meter(MetricRegistry.name(KbeeSystemMetricsService.class, "api-requests-out"));

		this.metric_api_gets = getMetrics().meter(MetricRegistry.name(KbeeSystemMetricsService.class, "api-gets"));

		
		this.metric_api_traffic_queue_in = getMetrics().meter(MetricRegistry.name(KbeeSystemMetricsService.class, "api-traffic-queue-in"));
		this.metric_api_traffic_queue_out = getMetrics().meter(MetricRegistry.name(KbeeSystemMetricsService.class, "api-traffic-queue-out"));
										
		this.metric_content_checkin = getMetrics().meter(MetricRegistry.name(KbeeSystemMetricsService.class, "content-checkin"));
		this.metric_content_api_checkin = getMetrics().meter(MetricRegistry.name(KbeeSystemMetricsService.class, "content-api-checkin"));
		
		this.metric_emails = getMetrics().meter(MetricRegistry.name(KbeeSystemMetricsService.class, "emails"));
		this.metric_web_pages = getMetrics().meter(MetricRegistry.name(KbeeSystemMetricsService.class, "metric_web_pages"));
		this.metric_portal_pages = getMetrics().meter(MetricRegistry.name(KbeeSystemMetricsService.class, "metric_portal_pages"));
		
		this.metric_search_submit = getMetrics().meter(MetricRegistry.name(KbeeSystemMetricsService.class, "metric_search_submit"));
									
		this.counter_api_traffic_queue_size = getMetrics().counter(MetricRegistry.name(KbeeSystemMetricsService.class, "counter_api_traffic_queue_size"));
		
	}

	// Users logged
	//
	//
	@Override
	public Counter getCounterUsersLogged() {
		return users_logged;
	}

	
	@Override
	public Counter getCounterUsersLogged(Serializable domain_id) {
		if (map_users_logged.get(domain_id.toString())==null) 
			map_users_logged.put(domain_id.toString(), getMetrics().counter(MetricRegistry.name(KbeeSystemMetricsService.class, domain_id.toString()+"."+"users-logged")));
		return map_users_logged.get(domain_id.toString());
	}

	
	// Login
	//
	//
	@Override
	public Meter getMeterLogin(Serializable domain_id) {
		if (map_metric_login.get(domain_id.toString())==null)
			map_metric_login.put(domain_id.toString(), getMetrics().meter(MetricRegistry.name(KbeeSystemMetricsService.class, domain_id.toString()+"."+"login")));
		return map_metric_login.get(domain_id.toString());
	}

	@Override
	public Meter getMeterLogin() {
		return metric_login;
	}


	@Override
	public Meter getMeterExternalPutObject() {
		return metric_external_putobject;
	}
	
	@Override
	public Meter getMeterExternalGetObject() {
		return metric_external_getobject;
	}


	
	@Override
	public Meter getMeterV1PutObject(String bucket) {
		if (map_metric_v1_putobject.get(bucket)==null)
			map_metric_v1_putobject.put(bucket, getMetrics().meter(MetricRegistry.name(KbeeSystemMetricsService.class, "v1-"+bucket+"."+"putobject")));
		return map_metric_v1_putobject.get(bucket);
	}

	@Override
	public Meter getMeterV1GetObject(String bucket) {
		if (map_metric_v1_getobject.get(bucket)==null)
			map_metric_v1_getobject.put(bucket, getMetrics().meter(MetricRegistry.name(KbeeSystemMetricsService.class, "v1-"+bucket+"."+"getobject")));
		return map_metric_v1_getobject.get(bucket);
	}

	@Override
	public Meter getMeterV1PutObject() {
		return metric_v1_putobject;
	}
	
	@Override
	public Meter getMeterV1GetObject() {
		return metric_v1_getobject;
	}
		
	
	
	@Override
	public Counter getCounterTrafficQueueSize() {
		return this.counter_api_traffic_queue_size;
	}

	
	// ContentCheckin
	//
	@Override
	public Meter getMeterContentCheckin() {
		return metric_content_checkin;
	}

	@Override
	public Meter getMeterContentCheckin(Serializable domain_id) {
		if (map_content_checkin.get(domain_id.toString())==null) 
			map_content_checkin.put(domain_id.toString(), getMetrics().meter(MetricRegistry.name(KbeeSystemMetricsService.class, domain_id.toString()+"."+"content-checkin")));
		return map_content_checkin.get(domain_id.toString());
	}

	// ContentCheckin API
	//
	@Override
	public Meter getMeterContentAPICheckin() {
		return metric_content_api_checkin;
	}

	@Override
	public Meter getMeterAPIGet() {
		return this.metric_api_gets;
	}
	
	//  API Requests
	//
	@Override
	public Meter getMeterAPIRequestsIn() {
		return metric_api_requests_in;
	}

	@Override
 	public Meter getMeterAPIRequestsOut() {
		return metric_api_requests_out;
 	}

	@Override
	public Meter getMeterAPITrafficeQueueIn() {
		return metric_api_traffic_queue_in;
	}

	@Override
	public Meter getMeterAPITrafficeQueueOut() {
		return metric_api_traffic_queue_out;
	}
	
	@Override
	public Meter getMeterContentAPICheckin(Serializable domain_id) {
		if (map_content_api_checkin.get(domain_id.toString())==null) 
			map_content_api_checkin.put(domain_id.toString(), getMetrics().meter(MetricRegistry.name(KbeeSystemMetricsService.class, domain_id.toString()+"."+"content-api-checkin")));
		return map_content_api_checkin.get(domain_id.toString());
	}

	// Index tasks
	//
	// Main Index Objects
	// Audit Index Objects
	// Attachment indexing
	//
	@Override
	public Meter getMeterIndexTasks() {
		return this.indexer_jobs;
	}
	
	
	@Override
	public Meter getMeterIndexAttachmentsTasks() {
			return this.indexer_attachmets_jobs;
	}

	@Override
	public Meter getMeterIndexMetainfoTasks() {
			return this.indexer_metainfo_jobs;
	}

	
	// Emails
	//
	@Override
	public Meter getMeterEmails() {
		return metric_emails;
	}

	@Override
	public Meter getMeterEmails(Serializable domain_id) {
		if (map_metric_emails.get(domain_id.toString())==null) 
			map_metric_emails.put(domain_id.toString(), getMetrics().meter(MetricRegistry.name(KbeeSystemMetricsService.class, domain_id.toString()+"."+"emails")));
		return map_metric_emails.get(domain_id.toString());
	}
	
	
	// Webpages
	//
	@Override
	public Meter getMeterWebPages() {
		return metric_web_pages;
	}

	// PortalPages
	//
	@Override
	public Meter getMeterPortalPages() {
		return metric_portal_pages;
	}


	public Meter  getSearchSubmitMeter() {
		return metric_search_submit;
	}

	
	@Override
	public void mark(String string, Serializable id) {
		
		if (string.equals("login")) {
			getMeterLogin(id).mark();
			getMeterLogin().mark();
			
		} else if (string.equals("email")) {
			if (getMeterEmails(id)!=null)
				getMeterEmails(id).mark();
			getMeterEmails().mark();
		}
	}
					
	@Override
	public void dec(String string, Serializable id) {
		
		if (string.equals("users_logged")) {
			getCounterUsersLogged(id).dec();
			getCounterUsersLogged().dec();
		}
	}

	@Override
	public void inc(String string, Serializable id) {
		if (string.equals("users_logged")) {
			if (getCounterUsersLogged(id)!=null)
				getCounterUsersLogged(id).inc();
			getCounterUsersLogged().inc();
		}
	}


	private  MeanEstimator traffic_inqueue_mean_time = new MeanEstimator("Traffic InQueue time");
	private  MeanEstimator api_request_mean_processing_time = new MeanEstimator("Request Processing time");

	
	@Override
	public MeanEstimator getTrafficInQueueEstimator() 			{ return traffic_inqueue_mean_time;}
	
	
	@Override
	public MeanEstimator getRequestProcessingTimeEstimator()	{ return api_request_mean_processing_time; } 

	
	/** ----------
	 *  Minio
	 */

	@Override
	public Meter getMeterV2PutObject(String bucket) {
		if (map_metric_putobject.get(bucket)==null)
			map_metric_putobject.put(bucket, getMetrics().meter(MetricRegistry.name(KbeeSystemMetricsService.class, bucket+"."+"putobject")));
		return map_metric_putobject.get(bucket);
	}

	@Override
	public Meter getMeterV2GetObject(String bucket) {
		if (map_metric_getobject.get(bucket)==null)
			map_metric_getobject.put(bucket, getMetrics().meter(MetricRegistry.name(KbeeSystemMetricsService.class, bucket+"."+"getobject")));
		return map_metric_getobject.get(bucket);
	}

	@Override
	public Meter getMeterV2ShardPutObject(String shard) {
		if (map_metric_shard_putobject.get(shard)==null)
			map_metric_shard_putobject.put(shard, getMetrics().meter(MetricRegistry.name(KbeeSystemMetricsService.class, "shard_"+shard+"."+"putobject")));
		return map_metric_shard_putobject.get(shard);
	}

	@Override
	public Meter getMeterV2ShardGetObject(String shard) {
		if (map_metric_shard_getobject.get(shard)==null)
			map_metric_shard_getobject.put(shard, getMetrics().meter(MetricRegistry.name(KbeeSystemMetricsService.class, "shard_"+shard+"."+"getobject")));
		return map_metric_shard_getobject.get(shard);
	}
	
	/** ----------
	 *  Odilon
	 */
	
	@Override
	public Meter getMeterOdilonPutObject(String bucket) {
		if (map_metric_odilon_putobject.get(bucket)==null)
			map_metric_odilon_putobject.put(bucket, getMetrics().meter(MetricRegistry.name(KbeeSystemMetricsService.class, bucket+"."+"putobject")));
		return map_metric_odilon_putobject.get(bucket);
	}

	@Override
	public Meter getMeterOdilonGetObject(String bucket) {
		if (map_metric_odilon_getobject.get(bucket)==null)
			map_metric_odilon_getobject.put(bucket, getMetrics().meter(MetricRegistry.name(KbeeSystemMetricsService.class, bucket+"."+"getobject")));
		return map_metric_odilon_getobject.get(bucket);
	}

	@Override
	public Meter getMeterOdilonShardPutObject(String shard) {
		if (map_metric_odilon_shard_putobject.get(shard)==null)
			map_metric_odilon_shard_putobject.put(shard, getMetrics().meter(MetricRegistry.name(KbeeSystemMetricsService.class, "shard_"+shard+"."+"putobject")));
		return map_metric_odilon_shard_putobject.get(shard);
	}

	@Override
	public Meter getMeterOdilonShardGetObject(String shard) {
		if (map_metric_odilon_shard_getobject.get(shard)==null)
			map_metric_odilon_shard_getobject.put(shard, getMetrics().meter(MetricRegistry.name(KbeeSystemMetricsService.class, "shard_"+shard+"."+"getobject")));
		return map_metric_odilon_shard_getobject.get(shard);
	}
	

	@Override
	public Counter getCounterV2KBFSCacheHit() {
		return kbfs_cache_hits;
	}

	@Override
	public Counter getCounterV2KBFSCacheMiss() {
		return kbfs_cache_miss;
	}

	@Override
	public Meter getMeterV2PutObject() {
		return metric_putobject;
	}
	
	@Override
	public Meter getMeterV2GetObject() {
		return metric_getobject;
	}

	/**
	 * Odilon
	 */
	
	@Override
	public Counter getCounterOdilonCacheHit() {
		return kbfs_cache_hits;
	}

	@Override
	public Counter getCounterOdilonCacheMiss() {
		return kbfs_cache_miss;
	}

	@Override
	public Meter getMeterOdilonPutObject() {
		return metric_putobject;
	}
	
	@Override
	public Meter getMeterOdilonGetObject() {
		return metric_getobject;
	}

	
	/**
	 * S3
	 */
	@Override
	public Meter getMeterS3PutObject() {
		return metric_s3_putobject;
	}
	
	@Override
	public Meter getMeterS3GetObject() {
		return metric_s3_getobject;
	}
	
									
	@Override
	public Meter getMeterS3PutObject(String bucket) {
		if (map_metric_s3_putobject.get(bucket)==null)
			map_metric_s3_putobject.put(bucket, getMetrics().meter(MetricRegistry.name(KbeeSystemMetricsService.class, bucket+"."+"s3.putobject")));
		return map_metric_s3_putobject.get(bucket);
	}

	@Override
	public Meter getMeterS3GetObject(String bucket) {
		if (map_metric_s3_getobject.get(bucket)==null)
			map_metric_s3_getobject.put(bucket, getMetrics().meter(MetricRegistry.name(KbeeSystemMetricsService.class, bucket+"."+"s3.getobject")));
		return map_metric_s3_getobject.get(bucket);
	}

	@Override
	public Counter getCounterS3KBFSCacheHit() {
		return s3_cache_hits;
	}

	@Override
	public Counter getCounterS3KBFSCacheMiss() {
		return s3_cache_miss;
	}

	
	
	
	private class DatabaseHealthCheck extends HealthCheck {
		
		protected DataSource dataSource;
		protected SessionFactory sessionFactory;

		public DatabaseHealthCheck() {
		}
		
	    public DatabaseHealthCheck(DataSource dataSource, SessionFactory sessionFactory) {
	    	this.dataSource=dataSource;
	    	this.sessionFactory=sessionFactory;
	    }
	    @Override
	    public HealthCheck.Result check() throws Exception {
	    	     if (checkDB()) {
	            		return HealthCheck.Result.healthy();
	        	} else {
	            return HealthCheck.Result.unhealthy("Cannot connect to Database ");
	       }
	    }
	    
		private boolean checkDB() {
			Connection connection = null;
			PreparedStatement statement = null;
			try {
				connection = getConnection(getSessionFactory().getCurrentSession());
				String pindb = "Select 1";
				statement = connection.prepareStatement(pindb);
				statement.execute();
				return true;
			} 
			catch (SQLException e) {
				logger.error(e);
				return false;
			} 
			finally {
				if (statement != null)
					try {
						statement.close();
					} catch (SQLException e) {
						logger.error(e);
					}
				
				if (connection != null) {
					try {
						connection.close();
					} catch (SQLException e) {
						logger.error(e);
					}
				}
					
			}
		}

		private Connection getConnection(Session session) {
			return ((SessionImplementor)session).connection();
		}
	}

	
	
}
