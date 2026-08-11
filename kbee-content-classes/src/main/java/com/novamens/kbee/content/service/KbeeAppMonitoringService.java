package com.novamens.kbee.content.service;


import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.command.Command;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.service.AppMonitoringService;
import com.novamens.content.user.UserSelfService;
import com.novamens.content.user.UserService;
import com.novamens.datetime.DateTimeService;
import com.novamens.dom.Domain;
import com.novamens.dom.KBFSStorageType;
import com.novamens.event.Event;
import com.novamens.event.EventListener;
import com.novamens.indexer.java.FileIndexerService;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.java.LogIndexerService;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.command.CommandService;
import com.novamens.kbee.content.command.CleanIndexCommand;
import com.novamens.kbee.content.command.PingServiceRequest;
import com.novamens.kbee.content.command.RemoveOrphansCommand;
import com.novamens.kbee.content.command.mt.BatchReindexCommand;
import com.novamens.kbee.event.EvictCacheServiceEvent;
import com.novamens.kbee.kbfs.KbeeShardedMinioFileServer;
import com.novamens.kbee.kbfs.KbeeShardedOdilonFileServer;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.system.SystemParameters;
import com.novamens.kbee.vault.VaultService;
import com.novamens.kbfs.FileServerMinio;
import com.novamens.kbfs.FileServerOdilon;
import com.novamens.kbfs.FileServerS3;
import com.novamens.kbfs.LocalFileServerCache;
import com.novamens.kbfs.v1.FileServerV1;
import com.novamens.metrics.SystemMetricsService;
import com.novamens.metrics.domain.DomainMetricsService;
import com.novamens.scheduler.SchedulerService;
import com.novamens.security.User;
import com.novamens.service.ApplicationServerService;

import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.solr.indexer.query.SolrQuery;
import com.novamens.system.properties.SystemPropertiesService;

import kbee.util.NumberFormatter;
import kbee.util.PropertiesFactory;
import kbee.util.Tuple;

public class KbeeAppMonitoringService implements AppMonitoringService, EventListener {

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeAppMonitoringService.class.getName());

	static final Properties properties = PropertiesFactory.getInstance("kbee").getProperties();
	static final String att = properties.getProperty("attempt_fix_index_seconds", "180").trim();
	
	static final String fix_enabled = properties.getProperty("attempt_fix_index_enabled", "yes").trim();
	
	static long ATTEMP_INTERVAL_SECS = 360;
	static  {
		try {
			ATTEMP_INTERVAL_SECS = Integer.valueOf(att);
		} catch (Exception e) {
			ATTEMP_INTERVAL_SECS=180;	
		}
	}

	private static final boolean IS_API_ENABLED =  PropertiesFactory.getInstance("kbee").getProperties().getProperty("api.enabled", "yes").toLowerCase().trim().equals("yes");
	private static final String	  SOLR_URL		=  PropertiesFactory.getInstance("kbee").getProperties().getProperty("solr.url", "");
	private static String database = PropertiesFactory.getInstance("kbee").getProperties().getProperty("jdbc.url", null);
	
	static {
		if (database!=null)
			database=database.trim();
	}
	
	// TODO HA
	//
	Map<Serializable, OffsetDateTime> _map 				= new ConcurrentHashMap<Serializable, OffsetDateTime>(16, 0.9f, 1);
	Map<Serializable, OffsetDateTime> _dom_map 			= new ConcurrentHashMap<Serializable, OffsetDateTime>(16, 0.9f, 1);
	Map<Serializable, OffsetDateTime> _sec_map 			= new ConcurrentHashMap<Serializable, OffsetDateTime>(16, 0.9f, 1);
	Map<Serializable, OffsetDateTime> _site_map 		= new ConcurrentHashMap<Serializable, OffsetDateTime>(16, 0.9f, 1);

	
	private Boolean IS_SUPPORT_ENABLED;
	private OffsetDateTime start_date = OffsetDateTime.now();
	
	public KbeeAppMonitoringService () {
		
	}
	

	@Override
	public List<Tuple> pingMonitorInfo() {
		
		List<Tuple> data = new ArrayList<Tuple>();
		long start = System.currentTimeMillis();
		
		try  {

			data = pingInfo();
			PingServiceRequest ping = new PingServiceRequest();
			
			ping.setNotify(false);
			ping.execute();
			String result = ping.pingResult();
			
			String html_result="<span class= \" "+ (result.toLowerCase().equals("ok") ? "success":  "danger") +"\" />"+ result +"</span>";
			
			data.add(new Tuple( "Ping Result",	html_result));
			
		}
		catch (Exception e) {
			data.add(new Tuple( "Error ",  	e.getClass().getName() + ". " + e.getMessage()));
			logger.error(e);
		} finally {
			long end = System.currentTimeMillis();
			if (logger.isDebugEnabled())
			data.add(new Tuple("Render time ", String.valueOf(end-start)+" <span class=\"ago\">ms</span>"));
			logger.debug("Render time " + String.valueOf(end-start)+"ms");
		}
		return data;

	}
		
	public List<Tuple> vaultInfo() {
		
		List<Tuple> data = new ArrayList<Tuple>();
		long start = System.currentTimeMillis();

		try  {

			String vault_url = PropertiesFactory.getInstance("kbee").getProperties().getProperty("vault.url", null);
			
			if (vault_url==null) {
				data.add(new Tuple( "Vault Status", "<span class=\"danger\">Disabled</span>"));
				return data;
			}
			
			data.add(new Tuple( "Vault URL", "<a href=\""+ vault_url.trim() +"\" target=\"_blank\">" + vault_url.trim() +"</a>"	));

			VaultService vas=ServiceLocator.getService(VaultService.class);
			
			if (vas!=null) {
				
				String rec=vas.getRoleId();
				data.add(new Tuple( "Vault RoleId",  rec!=null?rec:""));
				
				String sec=vas.getSecretId();
				
				if (getSessionUser()!=null && getSessionUser().getUserName().equals("root@kbee")) {
					data.add(new Tuple( "Vault SecretId <span class=\"only-root\">(root)</span>",  sec!=null?sec:""));	
				}
				else 									
					data.add(new Tuple( "Vault SecretId <span class=\"only-root\">(root)</span>",  (sec!=null && sec.length()>4?sec.substring(0, 4)+"***" :"")));
				
				long dec=ServiceLocator.getService(SystemMetricsService.class).getMeterVaultDeEncrypt().getCount();
				data.add(new Tuple( "Vault Decrypt Counter", NumberFormatter.formatNumber(dec, getLocale()) ));
				
				long enc=ServiceLocator.getService(SystemMetricsService.class).getMeterVaultEncrypt().getCount();
				data.add(new Tuple( "Vault Encrypt Counter", NumberFormatter.formatNumber(enc, getLocale()) ));
				
				
				String dec_m=String.format("%12.2f  <span class=\"separator\">|</span>   %12.2f  <span class=\"separator\">|</span>   %12.2f", 
						ServiceLocator.getService(SystemMetricsService.class).getMeterVaultDeEncrypt().getOneMinuteRate() * 60,
						ServiceLocator.getService(SystemMetricsService.class).getMeterVaultDeEncrypt().getFiveMinuteRate() * 60,
						ServiceLocator.getService(SystemMetricsService.class).getMeterVaultDeEncrypt().getFifteenMinuteRate() * 60);	
				data.add(new Tuple("Decrypt/min (1m 5m 15m)", dec_m));

				String enc_m=String.format("%12.2f  <span class=\"separator\">|</span>   %12.2f  <span class=\"separator\">|</span>   %12.2f", 
						ServiceLocator.getService(SystemMetricsService.class).getMeterVaultEncrypt().getOneMinuteRate() * 60,
						ServiceLocator.getService(SystemMetricsService.class).getMeterVaultEncrypt().getFiveMinuteRate() * 60,
						ServiceLocator.getService(SystemMetricsService.class).getMeterVaultEncrypt().getFifteenMinuteRate() * 60);	
				data.add(new Tuple("Encrypt/min (1m 5m 15m)", enc_m));
				
				String ping = vas.ping();
				
				if (ping!=null && ping.equals("ok")) {
					data.add(new Tuple("Vault Ping", "<span class=\"success\">" + ping + "</span>"));
				}
				else {
					data.add(new Tuple("Vault Ping", "<span class=\"danger\">" + ping + "</span>"));
				}
			}
			else {						
				data.add(new Tuple( "Vault", "VaultService is null"));
			}
			
	}
	catch (Exception e) {
		data.add(new Tuple( "Error ",  	e.getClass().getName() + ". " + e.getMessage()));
		logger.error(e);
	} finally {
		long end = System.currentTimeMillis();
		if (logger.isDebugEnabled())
		data.add(new Tuple("Render time ", String.valueOf(end-start)+" <span class=\"ago\">ms</span>"));
		logger.debug("Render time " + String.valueOf(end-start)+"ms");
	}
	return data;
		
	}
	/**
	 * 
	 */
	public List<Tuple> keyMetricsInfo() {
		
		List<Tuple> data = new ArrayList<Tuple>();
		long start = System.currentTimeMillis();

		try  {

			// OS Load Average
			//
		 	OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
		 	
		 	String la=
		 			
		 			
		 			
		 			os.getSystemLoadAverage()>0?
		 					
		 					NumberFormatter.formatNumber(os.getSystemLoadAverage()):
		 		
		 					(!isLinux()?"Not supported in Windows":"n/a");
		 	
		 			
		 			String spercent="";
		 	int processors = Runtime.getRuntime().availableProcessors();
			
		 	Double percent = Double.valueOf(0.0);
		 	
		 	if (processors>0 && os.getSystemLoadAverage()>0) 
				percent = Double.valueOf(Double.valueOf(os.getSystemLoadAverage()) / Double.valueOf(processors));

		 	spercent= " ("+ (NumberFormatter.formatNumber(percent.doubleValue()*100.0)).trim()+ "%) ";
		 	String class_status;
		 	Double ok_value 		= Double.valueOf(SystemParameters.get("app.cpu.ok", 		"1.0"));
		 	Double warning_value 	= Double.valueOf(SystemParameters.get("app.cpu.warning", 	"2.5"));
		 	
		 	if (percent > 0 && percent <= ok_value)
		 		class_status="success";
		 	else if (percent > 1.0 && percent <= warning_value)
		 		class_status="warning";
		 	else if (percent > 2.5)
		 		class_status="danger";
		 	else
		 		class_status="warning";
		 	
		 	String val ="<span class=\""+  class_status +"\"/>"+ la + spercent+"</span>";
		 	
		 	
			long jvmStartTime = ManagementFactory.getRuntimeMXBean().getStartTime();
			LocalDateTime d=ServiceLocator.getService(DateTimeService.class).millsToLocalDateTime(jvmStartTime);
			data.add(new Tuple( "JVM Started", ServiceLocator.getService(DateTimeService.class).timeElapsed(d, getLocale(), "ago")));
			data.add(new Tuple("App CPU Load Average (1m 5m 15m)", val));
			data.add(new Tuple( "App Started", ServiceLocator.getService(DateTimeService.class).timeElapsed(start_date)));

			
			try {
				// Scheduler Status
				SchedulerService service = ServiceLocator.getService(SchedulerService.class);
				String pg=service.getStatus();
				if (pg==null)
					 pg="err";
				boolean isok = pg.toLowerCase().equals("ok");
				String s="<span class=\""+ (isok?"success":"danger") +"\"/>"+pg+"</span>";
				data.add(new Tuple("Scheduler Engine Status ", s));
				
			} catch (Exception e) {
				data.add(new Tuple("Scheduler Engine Status ",  e.getClass().getName()));
				logger.error(e);
			}		


			
			
			try {
				// -----------------------
				// Database
				//
				String pg= getContentDao().pingDataBase();
				if (pg==null)
					pg="ok";
				boolean isok = pg.toLowerCase().equals("ok");
				String s="<span class= \" "+ (isok?"success":"danger") +"\" />"+pg+"</span>";
				data.add(new Tuple("Database ", s));
			} catch (Exception e) {
				data.add(new Tuple("Database ",  e.getClass().getName()+" | "+e.getMessage()));
				logger.error(e);
			}		

	

			try {
		 			// --	
					boolean kbfs2_enabled=PropertiesFactory.getInstance("kbee").getProperties().getProperty("kbfs2.enabled", "yes").toLowerCase().trim().equals("yes");
					
					if (kbfs2_enabled) {
						// KBFS 2
						FileServerMinio fsv2=ServiceLocator.getService(FileServerMinio.class);
						if (fsv2 instanceof KbeeShardedMinioFileServer) {
								String pg=fsv2.ping();
								if (pg==null)
									 pg="err";
								boolean isok = pg.toLowerCase().equals("ok");
								String s="<span class= \" "+ (isok?"success":"danger") +"\" />"+pg+"</span>";
								data.add(new Tuple( "Minio Shard Manager Status", s));
		
								Map<Integer, FileServerMinio> map = ((KbeeShardedMinioFileServer) fsv2).getShards();
								for (Entry<Integer, FileServerMinio> entry: map.entrySet()) { 
									data.add( new Tuple("Minio Shard " + entry.getKey().toString(), 
											"<a class=\"btn-link\" target=\"_blank\" href="+entry.getValue().getEndPoint()+">"+entry.getValue().getEndPoint()+"</a>"
											));
								}
								
						}
					}
			} catch (Exception e) {
				data.add(new Tuple("KBFS 2",  e.getClass().getName()+" | "+e.getMessage()));
				logger.error(e);
			}
			
			
			
			try {
				// --	
				boolean odilon_enabled=PropertiesFactory.getInstance("kbee").getProperties().getProperty("odilon.enabled", "no").toLowerCase().trim().equals("yes");
				
				if (odilon_enabled) {
	
					FileServerOdilon fsodilon=ServiceLocator.getService(FileServerOdilon.class);
					
					if (fsodilon instanceof KbeeShardedOdilonFileServer) {
							String pg=fsodilon.ping();
							if (pg==null)
								 pg="err";
							boolean isok = pg.toLowerCase().equals("ok");
							String s="<span class= \" "+ (isok?"success":"danger") +"\" />"+pg+"</span>";
							data.add(new Tuple( "Odilon Shard Manager Status", s));
	
							Map<Integer, FileServerOdilon> map = ((KbeeShardedOdilonFileServer) fsodilon).getShards();
							for (Entry<Integer, FileServerOdilon> entry: map.entrySet()) { 
								data.add( new Tuple("Odilon Shard " + entry.getKey().toString(), 
										"<a class=\"btn-link\" target=\"_blank\" href="+entry.getValue().getEndPoint()+">"+entry.getValue().getEndPoint()+"</a>"
										));
							}
					}
				}
			
			} catch (Exception e) {
				data.add(new Tuple("Odilon",  e.getClass().getName()+" | "+e.getMessage()));
				logger.error(e);
			}
			
			
			
			try {
				FileServerS3 s3=ServiceLocator.getService(FileServerS3.class);
				
				if (s3!=null && s3.isEnabled()) {
							String pg=s3.ping();
							if (pg==null)
								 pg="err";
							boolean isok = pg.toLowerCase().equals("ok");
							String s="<span class= \" "+ (isok?"success":"danger") +"\" />"+pg+"</span>";
							data.add(new Tuple( "Amazon S3 Status", s));
				}
				else  {
					data.add(new Tuple( "Amazon S3 Status", "Disabled"));
				}
			} catch (Exception e) {
				data.add(new Tuple("Odilon",  e.getClass().getName()+" | "+e.getMessage()));
				logger.error(e);
			}




			
			try {
				// -----------------------
				// SolR Server
				//
				String pg=pingSolR();
				
				if (pg==null)
					 pg="ok";
				
				boolean isok = pg.toLowerCase().equals("ok");
				String s="<span class= \" "+ (isok?"success":"danger") +"\" />"+pg+"</span>";
				data.add(new Tuple("SolR status ", s));
				data.add(new Tuple("SolR Endpoint ", "<a href=\"+SOLR_URL+\"  target=\"_blank\" class=\"btn-link\">" +		SOLR_URL +	"</a>"));
				
			} catch (Exception e) {
				data.add(new Tuple("SolR Status",  e.getClass().getName()+" | "+e.getMessage()));
				logger.error(e);
			}		
			
			
			
			try {
			
				if (IS_API_ENABLED) {																						
					Integer api_err_5m = getContentDao().executeCountNativeQuery("select count(*) from api_logevent where (event_status=412 or  event_status=403 or event_status=500) and event_time >(now() - INTERVAL '5 minute')\\:\\:timestamp");
					Integer api_err_1h = getContentDao().executeCountNativeQuery("select count(*) from api_logevent where (event_status=412 or  event_status=403 or event_status=500) and event_time >(now() - INTERVAL '1 hour')\\:\\:timestamp");
					Integer api_err_1d = getContentDao().executeCountNativeQuery("select count(*) from api_logevent where (event_status=412 or  event_status=403 or event_status=500) and event_time >(now() - INTERVAL '1 day')\\:\\:timestamp");
					
					if (api_err_5m==0 && api_err_1h==0 && api_err_1d==0) 
						data.add(new Tuple("API. Request Status", "<span class= \"success\">ok</span>"));
					else {								
							data.add(new Tuple("API. Request Status (errors 403/412/500. 5m 1h 1d)", 							
							NumberFormatter.formatNumber(api_err_5m, getLocale())    + "<span class=\"separator\">|</span>" +     
							NumberFormatter.formatNumber(api_err_1h, getLocale())    + "<span class=\"separator\">|</span>" +
							NumberFormatter.formatNumber(api_err_1d, getLocale())));
					}
				}
			}
			catch (Exception e) {
				data.add(new Tuple( "API ",  	e.getClass().getName() + ". " + e.getMessage()));
				logger.error(e);
			}
			
			
			try {
				
				
				//int solr_size = ServiceLocator.getService(DomainMetricsService.class).getTotalSolrHeadEnabledContents();
				
				int db_total  = ServiceLocator.getService(DomainMetricsService.class).getTotalDBHeadEnabledContents();
				data.add(new Tuple( "Contents" , NumberFormatter.formatNumber(db_total, getLocale())));
									
			}
			catch (Exception e) {
				data.add(new Tuple( "Contents",  	e.getClass().getName() + ". " + e.getMessage()));
				logger.error(e);
			}
			
			/**int total=solr_size+db_total;
				
				
				if (total>0) {
				
					
					double per = Math.abs(Double.valueOf(solr_size - db_total).doubleValue()) * 100.0 / Double.valueOf(total).doubleValue(); 
					
					boolean is_ok =  (per<1.0);
					boolean is_danger =  (per> 10.0);
					
					String v= NumberFormatter.formatNumber(db_total, getLocale()) + "<span class=\"separator\">|</span>" + NumberFormatter.formatNumber(solr_size, getLocale());
					String s="<span class= \" "+ (is_ok? "success":  ( is_danger?"danger": "warning")) +"\" />"+ v +"</span>" + 
							"<span style=\"padding:4px 8px; float:left;\"> (" + ServiceLocator.getService(DateTimeService.class).format(
									ServiceLocator.getService(DomainMetricsService.class).getHealthCheckOffsetDateTime(), 
									getZoneId().getId(), 
									getLocale(),
									DateTimeService.Day_Month_Year_hh_mm_ss) +")</span>";
					data.add(new Tuple( "Total Contents Library DB | SolR"   , s));
				
				}
				else
					data.add(new Tuple( "Total Contents Library DB | SolR"   , "0 | 0"));
				
				
			}
			catch (Exception e) {
				data.add(new Tuple( "Error ",  	e.getClass().getName() + ". " + e.getMessage()));
				logger.error(e);
			}
			**/
			
			
			
			
			
			
			
			try {
					
				
				if (isVaultEnabled() ) {
						
						VaultService vas=ServiceLocator.getService(VaultService.class);
						
						data.add(new Tuple("Vault URL", "<a href=\"+vas.getUrl()+\"  target=\"_blank\" class=\"btn-link\">" +		vas.getUrl() +	"</a>"));
						
						if (vas!=null) {
							String ping = vas.ping();
						
							if (ping!=null && ping.equals("ok")) {
								data.add(new Tuple("Vault Status", "<span class=\"success\">" + ping + "</span>"));
							}
							else {
								data.add(new Tuple("Vault Status", "<span class=\"danger\">" + ping + "</span>"));
							}
						}
					}
				else {
								
					data.add(new Tuple("Vault Status ", "<span class=\"danger\">" + "Disabled" + "</span>"));
				}
			}
			catch (Exception e) {
				data.add(new Tuple( "Error ",  	e.getClass().getName() + ". " + e.getMessage()));
				logger.error(e);
			}
			

			try {
				SystemMetricsService metrics_service = ServiceLocator.getService(SystemMetricsService.class);
			 	long v= metrics_service.getCounterUsersLogged().getCount();
			 	String tot_users=String.valueOf(ServiceLocator.getService(SecurityService.class).getTotalActiveUsers()) + "<span class=\"separator\">|</span>" + String.valueOf(v>0?v:0); 
			 	data.add(new Tuple("Active Users (Sessions | Sign In - Sign Out)", tot_users));
			}
			catch (Exception e) {
				data.add(new Tuple( "Error ",  	e.getClass().getName() + ". " + e.getMessage()));
				logger.error(e);
			}
			
			
			
			
			
			 

			
			
			
			
			
			
			
			
			
			
			
			
		}
		catch (Exception e) {
			data.add(new Tuple( "Error ",  	e.getClass().getName() + ". " + e.getMessage()));
			logger.error(e);
		} finally {
			long end = System.currentTimeMillis();
			if (logger.isDebugEnabled())
			data.add(new Tuple("Render time ", String.valueOf(end-start)+" <span class=\"ago\">ms</span>"));
			logger.debug("Render time " + String.valueOf(end-start)+"ms");
		}
		return data;
	}


	/***	 
	 * 
	 * 
	 
	private List<Tuple> DBloadAverage() {

		ResourceBundle res = ResourceBundle.getBundle(getClass().getName(), getLocale());
		
		List<Tuple> data = new ArrayList<Tuple>();
		try {

			if (isLinux()) {
				StringBuilder str = new StringBuilder(); 
	  			for (String entry:  getContentDao().getDBServerLoadAvg()) { 
					if (str.length()>0)
						str.append("<span class=\"separator\">|</span>");
					str.append(entry);
				}
	  			data.add(new Tuple(res.getString("db-la"), str.toString()));
			}
			else {
				data.add(new Tuple(res.getString("db-la"), "<span class=\"warning\">Not supported in Windows (0.00%)</span>"));
			}
			
		} catch (Throwable e) {
			logger.error(e);
			data.add(new Tuple(e.getClass().getName(), e.getMessage()));
		}
		return data;
	}
	
	
	
	/**
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 */
	@Override
	public List<Tuple> KBFSInfo() {
		
		long start = System.currentTimeMillis();
		
			List<Tuple> data = new ArrayList<Tuple>();
			
			data.add(new Tuple( "Info page", "<a class=\"btn-link\"  href=\"/datamanagement/objectstorage\"target=\"_blank\"> /datamanagement/objectstorage</a>"));
			
			
			boolean kbfs1_enabled=PropertiesFactory.getInstance("kbee").getProperties().getProperty("kbfs1.enabled", "yes").toLowerCase().trim().equals("yes");
			boolean kbfs2_enabled=PropertiesFactory.getInstance("kbee").getProperties().getProperty("kbfs2.enabled", "yes").toLowerCase().trim().equals("yes");
			boolean odilon_enabled=PropertiesFactory.getInstance("kbee").getProperties().getProperty("odilon.enabled", "no").toLowerCase().trim().equals("yes");
						
			
			try {
				KBFSStorageType ty= getDefaultKBFSStorageType();
				data.add(new Tuple( "Default Storage Type ", ty.getLabel()));
				
			} catch (Exception e) {
				logger.error(e);
				KBFSStorageType ty= getDefaultKBFSStorageType();
				if (ty!=null)
					data.add(new Tuple( "Default Storage Type ", ty.getLabel()));
			}
			

			
			try {

				// KBFS 1 ---------------------------------------------------------------------
				//
				if (kbfs1_enabled) {
					String pg=ServiceLocator.getService(FileServerV1.class).ping();
					if (pg==null)
						 pg="err";
					boolean isok = pg.toLowerCase().equals("ok");
					String s="<span class= \" "+ (isok?"success":"danger") +"\" />"+pg+"</span>";
					data.add(new Tuple("File System (ping)", s));
				}
				else
					data.add(new Tuple("File System ", "disabled"));

				
				// KBFS 2 ---------------------------------------------------------------------
				//
				if (kbfs2_enabled) {

					FileServerMinio fsv2=ServiceLocator.getService(FileServerMinio.class);
					
					if (fsv2 instanceof KbeeShardedMinioFileServer) {
						String sm_pg=fsv2.ping();
						if (sm_pg==null)
							sm_pg="err";
						boolean sm_isok = sm_pg.toLowerCase().equals("ok");
						String sm_s="<span class= \" "+ (sm_isok?"success":"danger") +"\" />"+sm_pg+"</span>";
						data.add(new Tuple( "Minio Shard Manager (ping)", sm_s));
						try {
							for (Entry<Integer, FileServerMinio> entry: ((com.novamens.kbee.kbfs.KbeeShardedMinioFileServer) fsv2).getShards().entrySet()) {
								String p = entry.getValue().ping();
								if (p==null)
									p="err";
								boolean p_isok = p.toLowerCase().equals("ok");
								String px="<span class= \" "+ (p_isok?"success":"danger") +"\" />"+p+"</span>";
								data.add(new Tuple( "Minio"+ String.valueOf(entry.getKey().intValue())+ " (ping)", px));
							}
						} catch (Exception e) {
							logger.error(e);
							data.add(new Tuple( "Error",  e.getClass().getName()));
						}
						try {
							for (Entry<Integer, FileServerMinio> entry: ((com.novamens.kbee.kbfs.KbeeShardedMinioFileServer) fsv2).getShards().entrySet()) {
								String fsid = entry.getValue().getFSId();
								data.add(new Tuple( "Minio"+ String.valueOf(entry.getKey().intValue())+" endpoint", 
										
										
										"<a  class=\"btn-link\" href=\""+ entry.getValue().getEndPoint() +"\" target=\"_blank\">" + 
												entry.getValue().getEndPoint() +"</a>"
										
										 + " <span class=\"ago\">(prob: "+ NumberFormatter.formatNumber(entry.getValue().getProbability()) + ")</span>"));
								data.add(new Tuple( "Minio"+ String.valueOf(entry.getKey().intValue())+" FSId", fsid));
							}
						} catch (Exception e) {
							logger.error(e);
							data.add(new Tuple( "Error",  e.getClass().getName()));
						}	
					}
					else {
						
						String sm_pg=fsv2.ping();
						if (sm_pg==null)
							sm_pg="err";
						boolean sm_isok = sm_pg.toLowerCase().equals("ok");
						String sm_s="<span class= \" "+ (sm_isok?"success":"danger") +"\" />"+sm_pg+"</span>";
						data.add(new Tuple( "Minio Ping", sm_s));
					}
				}
				else
					data.add(new Tuple("Minio", "disabled"));
				
			} catch (Exception e) {
					logger.error(e);
					data.add(new Tuple( "Error",  e.getClass().getName()));
			}


			
			
			
			// Odilon ---------------------------------------------------------------------
			//
			
			try {
				if (odilon_enabled) {
	
					FileServerOdilon fsodilon=ServiceLocator.getService(FileServerOdilon.class);
					
					if (fsodilon instanceof KbeeShardedOdilonFileServer) {
						String sm_pg=fsodilon.ping();
						if (sm_pg==null)
							sm_pg="err";
						boolean sm_isok = sm_pg.toLowerCase().equals("ok");
						String sm_s="<span class= \" "+ (sm_isok?"success":"danger") +"\" />"+sm_pg+"</span>";
						data.add(new Tuple( "Odilon Shard Manager (ping)", sm_s));
						try {
							for (Entry<Integer, FileServerOdilon> entry: ((com.novamens.kbee.kbfs.KbeeShardedOdilonFileServer) fsodilon).getShards().entrySet()) {
								String p = entry.getValue().ping();
								if (p==null)
									p="err";
								boolean p_isok = p.toLowerCase().equals("ok");
								String px="<span class= \" "+ (p_isok?"success":"danger") +"\" />"+p+"</span>";
								data.add(new Tuple( "Odilon_"+ String.valueOf(entry.getKey().intValue())+ " (ping)", px));
							}
						} catch (Exception e) {
							logger.error(e);
							data.add(new Tuple( "Error",  e.getClass().getName()));
						}
						try {
							for (Entry<Integer, FileServerOdilon> entry: ((com.novamens.kbee.kbfs.KbeeShardedOdilonFileServer) fsodilon).getShards().entrySet()) {
								String fsid = entry.getValue().getFSId();
								data.add(new Tuple( "Odilon_"+ String.valueOf(entry.getKey().intValue())+" endpoint", 
										
										
										"<a  class=\"btn-link\" href=\""+ entry.getValue().getEndPoint() +"\" target=\"_blank\">" + 
												entry.getValue().getEndPoint() +"</a>"
										
										 + " <span class=\"ago\">(prob: "+ NumberFormatter.formatNumber(entry.getValue().getProbability()) + ")</span>"));
								data.add(new Tuple( "Odilon_"+ String.valueOf(entry.getKey().intValue())+" FSId", fsid));
							}
						} catch (Exception e) {
							logger.error(e);
							data.add(new Tuple( "Error",  e.getClass().getName()));
						}	
					}
					else {
						
						String sm_pg=fsodilon.ping();
						if (sm_pg==null)
							sm_pg="err";
						boolean sm_isok = sm_pg.toLowerCase().equals("ok");
						String sm_s="<span class= \" "+ (sm_isok?"success":"danger") +"\" />"+sm_pg+"</span>";
						data.add(new Tuple( "Odilon Ping", sm_s));
					}
				}
				else
					data.add(new Tuple("Odilon", "disabled"));
			
			} catch (Exception e) {
					logger.error(e);
					data.add(new Tuple( "Odilon Error",  e.getClass().getName()));
			}

		
			
			
			FileServerS3 fss3=ServiceLocator.getService(FileServerS3.class);
			
			if (fss3!=null) {
				if (fss3.isEnabled()) {
					
					try {
						
						String sm_pg=fss3.ping();
						if (sm_pg==null)
							sm_pg="err";
						boolean sm_isok = sm_pg.toLowerCase().equals("ok");
						String sm_s="<span class= \" "+ (sm_isok?"success":"danger") +"\" />"+sm_pg+"</span>";
						data.add(new Tuple( "S3 (ping)", sm_s));
						data.add(new Tuple( "S3 Connected ", ServiceLocator.getService(DateTimeService.class).timeElapsed(fss3.getDateConnected())));
						data.add(new Tuple( "S3 Environment", fss3.getEnvironment()));
					
						
						data.add(new Tuple( "S3 Access Key", fss3.getAccessKey()));
						data.add(new Tuple( "S3 Secret Key", fss3.getSecretKey() !=null ?  fss3.getSecretKey().substring(0, fss3.getSecretKey().length()>6?6:1)+"..." : "null"));
					
					} catch (Exception e) {
						logger.error(e);
						data.add(new Tuple( "Error",  e.getClass().getName()  + " " + e.getMessage()));
					}
				}
				else {
					data.add(new Tuple( "S3 Status ", "Disabled"));
				}
			}
			else {
				data.add(new Tuple( "S3 Status ", "Not installed"));
			}
			
		 
			
			
			
			
			try {																
				DomainMetricsService doms = ServiceLocator.getService(DomainMetricsService.class);
				
				data.add(new Tuple( "Total Resources", 					   			NumberFormatter.formatNumber(doms.getTotalResources(), getSessionUser().getLocale())));
				data.add(new Tuple( "Total Resources File System",		 		   	NumberFormatter.formatNumber(doms.getTotalResources(KBFSStorageType.KBFS1), getSessionUser().getLocale())));
				data.add(new Tuple( "Total Resources Minio", 				    	NumberFormatter.formatNumber(doms.getTotalResources(KBFSStorageType.Minio), getSessionUser().getLocale())));
				data.add(new Tuple( "Total Resources Odilon", 				    	NumberFormatter.formatNumber(doms.getTotalResources(KBFSStorageType.Odilon), getSessionUser().getLocale())));
				data.add(new Tuple( "Total Resources S3",                           NumberFormatter.formatNumber(doms.getTotalResources(KBFSStorageType.AmazonS3), getSessionUser().getLocale())));
				
				//data.add(new Tuple( "Total Resources Minio Archive", 		    	NumberFormatter.formatNumber(doms.getTotalResources(KBFSStorageType.KBFS2Archive), getSessionUser().getLocale())));
				
				data.add(new Tuple( "Total Resources Gateway",		 				NumberFormatter.formatNumber(doms.getTotalResources(KBFSStorageType.External), getSessionUser().getLocale())));
				data.add(new Tuple( "Total Resources Encrypted",	 				NumberFormatter.formatNumber(getContentDao().getTotalEncryptedResources(), getSessionUser().getLocale())));
				data.add(new Tuple( "Total Hard Disk Stored",	                   NumberFormatter.formatFileSize(doms.getTotalStoredHardDisk(), getSessionUser().getLocale(), "ago")));
				
				data.add(new Tuple( "Total Hard Disk Gateway",		 				NumberFormatter.formatFileSize(doms.getTotalHardDisk(KBFSStorageType.External), getSessionUser().getLocale(), "ago")));
				
			} catch (Exception e) {
				logger.error(e);
				data.add(new Tuple( "Error",  e.getClass().getName()));
			}
			
			
			
			try {																
				LocalFileServerCache cache = ServiceLocator.getService(LocalFileServerCache.class);
				data.add(new Tuple( "Cache Total Disk",NumberFormatter.formatFileSize(cache.getTotalDisk(), getSessionUser().getLocale(), "ago")));
				data.add(new Tuple( "Cache Total Items",NumberFormatter.formatNumber(cache.getTotalItems(), getSessionUser().getLocale())));
				
			} catch (Exception e) {
				logger.error(e);
				data.add(new Tuple( "LocalFileServerCache",  e.getClass().getName()));
			}
			
			
			
			
			long end = System.currentTimeMillis();
			if (logger.isDebugEnabled())
			data.add(new Tuple("Render time ", String.valueOf(end-start)+" <span class=\"ago\">ms</span>"));
			logger.debug("File Service time " + String.valueOf(end-start)+" ms");
			
			return data;
	}

	
	
	
	
	
	private List<Tuple> systemActivityInfo() {

		long start = System.currentTimeMillis();
		
		List<Tuple> data = new ArrayList<Tuple>();
		
		SystemMetricsService metrics_service = ServiceLocator.getService(SystemMetricsService.class);

		DomainMetricsService doms = ServiceLocator.getService(DomainMetricsService.class);
		
		try {
			
			String login=String.format("%12.2f  <span class=\"separator\">|</span>   %12.2f  <span class=\"separator\">|</span>   %12.2f", 
					  metrics_service.getMeterLogin().getOneMinuteRate() * 60,
					  metrics_service.getMeterLogin().getFiveMinuteRate() * 60,
					  metrics_service.getMeterLogin().getFifteenMinuteRate() * 60);	
			data.add(new Tuple("Login/min (1m 5m 15m)", login));
			
			
			String wp=String.format("%12.2f  <span class=\"separator\">|</span>   %12.2f   <span class=\"separator\">|</span>   %12.2f", 
					  metrics_service.getMeterWebPages().getOneMinuteRate() * 60,
					  metrics_service.getMeterWebPages().getFiveMinuteRate() * 60,
					  metrics_service.getMeterWebPages().getFifteenMinuteRate() * 60);	
											
			String em=String.format("%12.2f  <span class=\"separator\">|</span>   %12.2f   <span class=\"separator\">|</span>   %12.2f", 
					  metrics_service.getMeterEmails().getOneMinuteRate() * 60,
					  metrics_service.getMeterEmails().getFiveMinuteRate() * 60,
					  metrics_service.getMeterEmails().getFifteenMinuteRate() * 60);	
	
			data.add(new Tuple("Active users", String.valueOf(metrics_service.getCounterUsersLogged().getCount())));
	
			
			data.add(new Tuple("Emails/min (1m 5m 15m)", em));
			data.add(new Tuple("Webpages/min (1m 5m 15m)", wp));
			
			try {
				DateTimeService service = ServiceLocator.getService(DateTimeService.class);
				User user = getSessionUser();
				String zid = service.getMapZoneIds().get(user.getTimeZone());
				if (zid==null)
						zid=ZoneId.systemDefault().getId();
				long time_measure = doms.getTimeMeasure(getDomain());
				OffsetDateTime date = OffsetDateTime.ofInstant( Instant.ofEpochMilli(time_measure), ZoneId.of(zid));
				String s=ServiceLocator.getService(DateTimeService.class).timeElapsed(date, ZoneId.of(zid), user.getLocale(), DateTimeService.DATE_COLlOQUIAL_AGO, "ago");
				data.add(new Tuple( "Time  measured ", s));
			} catch (Exception e) {
				logger.error(e);
				data.add(new Tuple( "Error",  e.getClass().getName()));
			}	


		} catch (Exception e) {
			logger.error(e);
			data.add(new Tuple( "Error",  e.getClass().getName()));
		}
		finally {
		
			long end=System.currentTimeMillis();
			if (logger.isDebugEnabled())
			data.add(new Tuple("Render time ", String.valueOf(end-start)+" <span class=\"ago\">ms</span>"));
		}
  		return data;
  		
	}
	
	/**
	 *  Request mean processing time 1m 5m 1h
	 *  Requests received 1m 5m 1h
	 */
	
	@Override
	public List<Tuple> recentActivityAPIInfo() {
		
		List<Tuple> data = new ArrayList<Tuple>();
		
		if (!IS_API_ENABLED) {
			data.add(new Tuple("API", "<span class= \"warning\">disabled</span>"));
			return data;
		}
		
		try {
			// This works for PostgreSQL but not in Oracle
			//
			if (isPostgreSQL()) {
				
				long start = System.currentTimeMillis();

				SystemMetricsService metrics_service = ServiceLocator.getService(SystemMetricsService.class);

				// Mean Request processing time 1m 5m 15m
				//
				String cam=String.format("%12.2f  <span class=\"separator\">|</span> %12.2f   <span class=\"separator\">|</span>  %12.2f", 
						  					metrics_service.getMeterContentAPICheckin().getOneMinuteRate(),
						  					metrics_service.getMeterContentAPICheckin().getFiveMinuteRate(),
						  					metrics_service.getMeterContentAPICheckin().getFifteenMinuteRate());	
																									
				Integer api_err_5m = getContentDao().executeCountNativeQuery("select count(*) from api_logevent where (event_status=412 or  event_status=403 or event_status=500) and event_time >(now() - INTERVAL '5 minute')\\:\\:timestamp");
				Integer api_err_1h = getContentDao().executeCountNativeQuery("select count(*) from api_logevent where (event_status=412 or  event_status=403 or event_status=500) and event_time >(now() - INTERVAL '1 hour')\\:\\:timestamp");
				Integer api_err_1d = getContentDao().executeCountNativeQuery("select count(*) from api_logevent where (event_status=412 or  event_status=403 or event_status=500) and event_time >(now() - INTERVAL '1 day')\\:\\:timestamp");
				
				if (api_err_5m==0 && api_err_1h==0 && api_err_1d==0) 
					data.add(new Tuple("API. Requests Status", "<span class= \"success\">ok</span>"));
				else {
						data.add(new Tuple("API. Requests status (errors 403/412/500. 5m 1h 1d)", 							
						NumberFormatter.formatNumber(api_err_5m, getSessionUser().getLocale())    + "<span class=\"separator\">|</span>" +     
						NumberFormatter.formatNumber(api_err_1h, getSessionUser().getLocale())    + "<span class=\"separator\">|</span>" +
						NumberFormatter.formatNumber(api_err_1d, getSessionUser().getLocale())));
				}
				

				String trafficTokens = SystemParameters.get("com.novamens.content.webapi.traffictokens", "na");
				
				data.add(new Tuple("API Request Processing Worker Threads", trafficTokens));
				
				Integer count_1m 		= getContentDao().executeCountNativeQuery("select count(*) from api_logevent  where event_time >(now() - INTERVAL '1 minute')\\:\\:timestamp");
				Integer count_5m 		= getContentDao().executeCountNativeQuery("select count(*) from api_logevent  where event_time >(now() - INTERVAL '5 minute')\\:\\:timestamp");
				Integer count_15m 		= getContentDao().executeCountNativeQuery("select count(*) from api_logevent  where event_time >(now() - INTERVAL '15 minute')\\:\\:timestamp");
				
				Integer mean_proc_1m    = count_1m==0 	? 0  	: getContentDao().executeCountNativeQuery("select sum(event_processing_time)/"  + String.valueOf(count_1m)  + " from api_logevent  where event_time >(now() - INTERVAL '1 minute')\\:\\:timestamp");
				Integer mean_proc_5m    = count_5m==0 	? 0  	: getContentDao().executeCountNativeQuery("select sum(event_processing_time)/"  + String.valueOf(count_5m)  + " from api_logevent  where event_time >(now() - INTERVAL '5 minute')\\:\\:timestamp");
				Integer mean_proc_15m   = count_15m==0 	? 0 	: getContentDao().executeCountNativeQuery("select sum(event_processing_time)/"  + String.valueOf(count_15m) + " from api_logevent  where event_time >(now() - INTERVAL '15 minute')\\:\\:timestamp");
				
				data.add(new Tuple("API. Request mean processing time ms (1m 5m 15m)", 							
						NumberFormatter.formatNumber(mean_proc_1m, getSessionUser().getLocale())    + "<span class=\"separator\">|</span>" +     
						NumberFormatter.formatNumber(mean_proc_5m, getSessionUser().getLocale())    + "<span class=\"separator\">|</span>" +
				
						NumberFormatter.formatNumber(mean_proc_15m, getSessionUser().getLocale())));
												
				data.add(new Tuple("API. Requests total inbound traffic (1m 5m 15m)", 							
						NumberFormatter.formatNumber(count_1m,  getSessionUser().getLocale())    + "<span class=\"separator\">|</span>" +     
						NumberFormatter.formatNumber(count_5m,  getSessionUser().getLocale())    + "<span class=\"separator\">|</span>" +
						NumberFormatter.formatNumber(count_15m, getSessionUser().getLocale())));

				String api_out_rate=String.format("%12.2f  <span class=\"separator\">|</span> %12.2f   <span class=\"separator\">|</span>  %12.2f", 
						metrics_service.getMeterAPIRequestsOut().getOneMinuteRate(),
						metrics_service.getMeterAPIRequestsOut().getFiveMinuteRate(),
						metrics_service.getMeterAPIRequestsOut().getFifteenMinuteRate());	
															
				data.add(new Tuple("API Request Throughput (1m 5m 15m)", api_out_rate   + " <span class=\"ago\">reqs/sec</span>"));
				
						
				/**
				  	get sure PostgreSQL has these two indexes:
				  	
					CREATE INDEX ON api_logevent (event_time desc);
					CREATE INDEX ON api_soapevent (event_time desc);
				*/
																		
				data.add(new Tuple("Checkin API (1m 5m 15m)", cam + " <span clasS=\"ago atright\">event/sec</span>" ));
				
				Integer api_event_d 			= getContentDao().executeCountNativeQuery("select count(*) from api_logevent  where event_status=200   	and event_time >((now() - INTERVAL '1 day')\\:\\:timestamp)");
				Integer api_err_event_d 		= getContentDao().executeCountNativeQuery("select count(*) from api_logevent  where (event_status>=400  and event_status!=404) and event_time >((now()   - INTERVAL '1 day')\\:\\:timestamp)");

				Integer api_soap_d  			= getContentDao().executeCountNativeQuery("select count(*) from api_soapevent where event_status=200  	and event_time >((now() - INTERVAL '1 day')\\:\\:timestamp)");
				Integer api_err_soap_d  		= getContentDao().executeCountNativeQuery("select count(*) from api_soapevent where (event_status>=400  and event_status!=404) and event_time >((now() - INTERVAL '1 day')\\:\\:timestamp)");
				
				Integer api_event_h 			= getContentDao().executeCountNativeQuery("select count(*) from api_logevent  where event_status=200  	and event_time >                       ((now() - INTERVAL '1 hour')\\:\\:timestamp)");
				Integer api_err_event_h 		= getContentDao().executeCountNativeQuery("select count(*) from api_logevent  where event_status>=400   and event_status!=404 and event_time > ((now() - INTERVAL '1 hour')\\:\\:timestamp)");
				
				Integer api_soap_h  			= getContentDao().executeCountNativeQuery("select count(*) from api_soapevent where event_status=200  	and event_time >                       ((now() - INTERVAL '1 hour')\\:\\:timestamp)");
				Integer api_err_soap_h  		= getContentDao().executeCountNativeQuery("select count(*) from api_soapevent where event_status>=400   and event_status!=404 and event_time >      ((now() - INTERVAL '1 hour')\\:\\:timestamp)");
				
				Integer api_event_5m 			= getContentDao().executeCountNativeQuery("select count(*) from api_logevent  where event_status=200  	and event_time >((now() - INTERVAL '5 minute')\\:\\:timestamp)");
				Integer api_err_event_5m 		= getContentDao().executeCountNativeQuery("select count(*) from api_logevent  where event_status>=400   and event_status!=404	and event_time >((now() - INTERVAL '5 minute')\\:\\:timestamp)");
				
				Integer api_soap_5m  			= getContentDao().executeCountNativeQuery("select count(*) from api_soapevent where event_status=200  	and event_time >((now() - INTERVAL '5 minute')\\:\\:timestamp)");
				Integer api_err_soap_5m  		= getContentDao().executeCountNativeQuery("select count(*) from api_soapevent where event_status>=400   and event_status!=404	and event_time >((now() - INTERVAL '5 minute')\\:\\:timestamp)");

												
				data.add(new Tuple("API. Requests ok (5m 1hr 1d)", 	    
						NumberFormatter.formatNumber(api_event_5m, getSessionUser().getLocale())   + "<span class=\"separator\">|</span>" +     
						NumberFormatter.formatNumber(api_event_h, getSessionUser().getLocale())    + "<span class=\"separator\">|</span>" +
						NumberFormatter.formatNumber(api_event_d, getSessionUser().getLocale())  ));  	 

				
				data.add(new Tuple("API. Requests error 400+ less 404 (5m 1hr 1d)", 	    
							 NumberFormatter.formatNumber(api_err_event_5m, getSessionUser().getLocale())     + "<span class=\"separator\">|</span>" +     
							 NumberFormatter.formatNumber(api_err_event_h,  getSessionUser().getLocale())     + "<span class=\"separator\">|</span>" +
							 NumberFormatter.formatNumber(api_err_event_d,  getSessionUser().getLocale()) ));  	 
	 																
				data.add(new Tuple("API. SOAP Requests ok (5m 1hr 1d)", 	    
							NumberFormatter.formatNumber(api_soap_5m, getSessionUser().getLocale())     + "<span class=\"separator\">|</span>" +     
							NumberFormatter.formatNumber(api_soap_h, getSessionUser().getLocale())      + "<span class=\"separator\">|</span>" +
							NumberFormatter.formatNumber(api_soap_d, getSessionUser().getLocale())  ));  	 
	 			

				
				data.add(new Tuple("API. SOAP Requests error 400+ less 404 (5m 1hr 1d)", 	    
						NumberFormatter.formatNumber(api_err_soap_5m, getSessionUser().getLocale())    + "<span class=\"separator\">|</span>" +     
						NumberFormatter.formatNumber(api_err_soap_h, getSessionUser().getLocale())     + "<span class=\"separator\">|</span>" +
						NumberFormatter.formatNumber(api_err_soap_d,    getSessionUser().getLocale()) ));  	 
				
				
				
				
				if (isVaultEnabled() ) {
					
					try {
					VaultService vas=ServiceLocator.getService(VaultService.class);
					
					if (vas!=null) {
						String vault_ping  =  vas.ping();
						if (vault_ping.equals("ok")) {
							
						}
						else {
							
						}
					
					}
					
					} catch (Exception e) {
						data.add(new Tuple( "Vault ",  	e.getClass().getName() + ". " + e.getMessage()));
						logger.error(e);
					}
					
					
				}
				
				long end = System.currentTimeMillis();
				if (logger.isDebugEnabled())
				data.add(new Tuple("Render time ", String.valueOf(end-start)+" <span class=\"ago\">ms</span>"));
				logger.debug("Render time " + String.valueOf(end-start)+"ms");
				
			}
			
		} catch (ContentMgmtException e) {
			data.add(new Tuple( "Error ",  	e.getClass().getName() + ". " + e.getMessage() +" <br /> Probably table/s that dont exists." ));
			logger.error(e);
		
		} catch (Exception e) {
			data.add(new Tuple( "Error ",  	e.getClass().getName() + ". " + e.getMessage()));
			logger.error(e);
		}
		return data;
	
	}
	
	private boolean isVaultEnabled() {
		return PropertiesFactory.getInstance("kbee").getProperties().getProperty("vault.url", null)!=null;
		
		
	}
	public List<Tuple> pingInfo() {
		return ServiceLocator.getService(ApplicationServerService.class).pingInfo();
	}
	
	
	public List<Tuple> serversInfo() {
		return ServiceLocator.getService(ApplicationServerService.class).serversInfo();
	}
	
	public List<Tuple> schedulerInfo() {
		return ServiceLocator.getService(ApplicationServerService.class).schedulerInfo();
	}
											
	public List<Tuple> infrastructureInfo() {
		return ServiceLocator.getService(ApplicationServerService.class).infrastructureInfo();
	}
	
	

	/**	if (isSolrCompiled()) {
	File sd=new File("solr"+File.separator+"data");
	if (sd.exists() && sd.isDirectory()) {
		long solrsize=  FileUtils.sizeOf(new File("solr"+File.separator+"data"));
		data.add(new Tuple("Solr directory Data", NumberFormatter.formatFileSize(solrsize, getSessionUser().getLocale(), "ago")));
	}
	
	File au=new File("solr"+File.separator+"auditdata");
	if (au.exists() && au.isDirectory()) { 
		long solrauditsize=FileUtils.sizeOf(au);
		data.add(new Tuple("Solr directory Audit", NumberFormatter.formatFileSize(solrauditsize, getSessionUser().getLocale(), "ago")));
	}
}
**/

	public List<Tuple> databaseInfo() {
		
		long start = System.currentTimeMillis();
		
		List<Tuple> data = new ArrayList<Tuple>();
		
		ResourceBundle res = ResourceBundle.getBundle(getClass().getName(), getLocale());
		
		try {
			
			
			 
														
			data.add(new Tuple(res.getString("database-page"), "<a class=\"btn-link\"  href=\"/systeminfo/database\"target=\"_blank\"> /datamanagement/sql-gateway</a>"  ));
			data.add(new Tuple(res.getString("database-version"), getContentDao().getDatabaseVersion()));
			data.add(new Tuple(res.getString("database-url"), 	PropertiesFactory.getInstance("kbee").getProperties().getProperty("jdbc.url", ""))); 
			data.add(new Tuple(res.getString("database-size"), 	NumberFormatter.formatFileSize(getContentDao().getDatabaseSize(), getSessionUser().getLocale(), "ago")));
			data.add(new Tuple(res.getString("database-user"), 	PropertiesFactory.getInstance("kbee").getProperties().getProperty("jdbc.username", "") + " (" +
					   											PropertiesFactory.getInstance("kbee").getProperties().getProperty("jdbc.password", "") +")"
					   											
					   											
		  
			));

			
			data.add(new Tuple(res.getString("sql-gateway"), "<a class=\"btn-link\"  href=\"/datamanagement/sql-gateway\"target=\"_blank\"> /datamanagement/sql-gateway</a>"  ));
			
			/**
			try {
				List<Tuple>le=DBloadAverage();
				if (le!=null && le.size()>0) {
					for (Tuple t: le)
						data.add(new Tuple(t.label, t.value));	
				}
			} catch (Exception e) {
				data.add(new Tuple(e.getClass().getName(), e.getMessage()));
				logger.error(e);
			}**/
		
		} catch (Exception e) {
			logger.error(e);
			data.add(new Tuple(e.getClass().getName(), e.getMessage()));
		}
		finally {
			
			if (logger.isDebugEnabled())
				data.add(new Tuple(res.getString("render-time"), String.valueOf(System.currentTimeMillis()-start)+" <span class=\"ago\">ms</span>"));
			logger.debug("Render time " + String.valueOf(System.currentTimeMillis()-start)+" ms");
		}
		return data;
	}
	
	
	
@Override
public String pingSolR() {
		
	 	try {
			SolrQuery q = new SolrQuery(getQueryIndex()) {
				private static final long serialVersionUID = 1L;
				@Override
				public String getStatement() {
					return "domain:*";
				}
				@Override
				public String getSolrStatement() {
					return "domain:*";
				}
			};
			
			@SuppressWarnings("unused")
			int qsize = q.execute().size();

			return null;
		
		} catch (Exception e) {
			logger.error(e);
			return "SolR. "+e.getClass().getSimpleName()+ " | " + e.getMessage();
		}
	}

	protected Index getFileIndex() {
		return getDomain().getService(FileIndexerService.class).getIndex();
	}

	
	protected Index getQueryIndex() {
		return getDomain().getService(JavaIndexerService.class).getIndex();
	}

	protected Index getAuditIndex() {
		return getDomain().getService(LogIndexerService.class).getIndex();
	}

	protected Locale getLocale() {
		User user = getSessionUser();
	 return user!=null?user.getLocale():Locale.getDefault();
			
	}

	protected ZoneId getZoneId() {
		User user = getSessionUser();
	 return user!=null?user.getZoneId():ZoneId.systemDefault();
	}
	protected User getSessionUser() {
		try {
			return ServiceLocator.getService(SecurityService.class).getSessionUser();
		} catch (Exception e) {
			return null;
		}
	}	
	protected boolean isLinux() {
		if  (System.getenv("OS")!=null && System.getenv("OS").toLowerCase().contains("windows")) 
			return false;
		return true;
	}

	private KBFSStorageType getDefaultKBFSStorageType() {
		return KBFSStorageType.getByKey(getContentDao().findSystemParameterValueByKey("kbfs.storage.default", ServiceLocator.getService(SystemPropertiesService.class).getDefaultKBFSService()));
	}

	
	/***
	 * @return
	 */
	private boolean isPostgreSQL() {
		
		if (database==null)
			return false;
		
		if(database.contains("oracle"))  
			return false;
		return true;
	}


	/**
	 * 	
	 * 
	 * @return
	 */
	
	@Override
	
	public List<Tuple> searchInfo() {
		
		long start = System.currentTimeMillis();
		List<Tuple> data = new ArrayList<Tuple>();
		
		try {
			
			try {
																									
											
				data.add(new Tuple( "Info page", "<a class=\"btn-link\"  href=\"/datamanagement/reindex\"target=\"_blank\"> /datamanagement/reindex</a>"));

				
				
				data.add(new Tuple( "solr.url", 			"<a href=\""+ PropertiesFactory.getInstance("kbee").getProperties().getProperty("solr.url", "").trim()+"\" target=\"_blank\">" + PropertiesFactory.getInstance("kbee").getProperties().getProperty("solr.url", "").trim() +"</a>" ));
				data.add(new Tuple( "solr.content-core", 	PropertiesFactory.getInstance("kbee").getProperties().getProperty("solr.content-core", "").trim()	));
				data.add(new Tuple( "solr.file-core", 	    
						PropertiesFactory.getInstance("kbee").getProperties().getProperty("solr.file-core", "")!=null ?
						PropertiesFactory.getInstance("kbee").getProperties().getProperty("solr.file-core", "").trim() :""));
				
				try {
					int solr_size = ServiceLocator.getService(DomainMetricsService.class).getTotalDBHeadEnabledContents();
					int db_total  = ServiceLocator.getService(DomainMetricsService.class).getTotalSolrHeadEnabledContents();
							
					boolean is_ok =  Math.abs((solr_size - db_total)) == 0;
					boolean is_danger =  Math.abs((solr_size - db_total)) > 40;
					
					String v= NumberFormatter.formatNumber(db_total, getSessionUser().getLocale()) + "<span class=\"separator\">|</span>" + NumberFormatter.formatNumber(solr_size, getSessionUser().getLocale());
					
					
					String s;
					
					 s="<span class= \" "+ (is_ok? "success":  ( is_danger?"danger": "warning")) +"\" />"+ v +"</span>" +
							"<span style=\"padding:4px 8px; float:left;\"> (" + ServiceLocator.getService(DateTimeService.class).format(
									ServiceLocator.getService(DomainMetricsService.class).getHealthCheckOffsetDateTime(), 
									getSessionUser().getZoneId().getId(), 
									getSessionUser().getLocale(),
									DateTimeService.Day_Month_Year_hh_mm_ss) +")</span>";
					data.add(new Tuple( "Total Contents Library DB | SolR"   , s));
				}
				catch (Exception e) {
					data.add(new Tuple( "Error ",  	e.getClass().getName() + ". " + e.getMessage()));
					logger.error(e);
				}

				
				
				
			} catch (Exception e) {
				data.add(new Tuple( "solr variables ",  e.getClass().getName()+" | " + e.getMessage()));
				logger.error(e);		
			}
			
				
			SystemMetricsService service = ServiceLocator.getService(SystemMetricsService.class);
		
			String rate_ig;
			String rate_im;
			String rate_ia;

			String imm;
			String iam;
			
			String igm;

			try {
				long s_start = System.currentTimeMillis();
				
				SolrQuery q = new SolrQuery(getQueryIndex()) {
					private static final long serialVersionUID = 1L;
					@Override
					public String getStatement() {
						return "*:*";
					}
					@Override
					public String getSolrStatement() {
						return "*:*";
					}
				};
				int qsize = q.execute().size();
				long s_end = System.currentTimeMillis();
				data.add(new Tuple( "Total Java Index", NumberFormatter.formatNumber(qsize, getSessionUser().getLocale()).trim() +"  (" +  String.valueOf(s_end-s_start) + " ms)"));
				
			} catch (Exception e) {
				data.add(new Tuple( "Total Java Index",  e.getClass().getName()+" | " + e.getMessage()));
				logger.error(e);		
			}
			
			
			/**
			try {
				long s_start = System.currentTimeMillis();

				SolrQuery q = new SolrQuery(getAuditIndex()) {
					private static final long serialVersionUID = 1L;
					@Override
					public String getStatement() {
						return "*:*";
					}
					@Override
					public String getSolrStatement() {
						return "*:*";
					}
				};
				int qsize = q.execute().size();
				long s_end = System.currentTimeMillis();
				data.add(new Tuple( "Total Audit Index",  NumberUtil.formatNumber(qsize, getSessionUser().getLocale()).trim() +"  (" +  String.valueOf(s_end-s_start) + " ms)"));
				
			} catch (Exception e) {
				data.add(new Tuple( "Total Audit Index   ",  e.getClass().getName()+" | " + e.getMessage()));
				logger.error(e);		
			}
			*/


			
			try {
				long s_start = System.currentTimeMillis();

				SolrQuery q = new SolrQuery(getFileIndex()) {
					private static final long serialVersionUID = 1L;
					@Override
					public String getStatement() {
						return "*:*";
					}
					@Override
					public String getSolrStatement() {
						return "*:*";
					}
				};
				int qsize = q.execute().size();
				long s_end = System.currentTimeMillis();
				data.add(new Tuple( "Total File Index",  NumberFormatter.formatNumber(qsize, getSessionUser().getLocale()).trim() +"  (" +  String.valueOf(s_end-s_start) + " ms)"));
				
			} catch (Exception e) {
				data.add(new Tuple( "Total File Index   ",  e.getClass().getName()+" | " + e.getMessage()));
				logger.error(e);		
			}

			
			try {	
				String ig1 	= NumberFormatter.formatNumber(service.getMeterIndexTasks().getOneMinuteRate(), getSessionUser().getLocale()).trim();
				String ig5 	= NumberFormatter.formatNumber(service.getMeterIndexTasks().getFiveMinuteRate(), getSessionUser().getLocale()).trim();
				String ig15 = NumberFormatter.formatNumber(service.getMeterIndexTasks().getFifteenMinuteRate(), getSessionUser().getLocale()).trim();

				igm  = NumberFormatter.formatNumber(service.getMeterIndexTasks().getMeanRate(), getSessionUser().getLocale()).trim() + " <span class=\"ago\">task/sec</span>";
						
				rate_ig = ig1 + "<span class=\"separator\">|</span>" +
						  ig5 + "<span class=\"separator\">|</span>" +
						  ig15;
				
			} catch (Exception e) {
				rate_ig=e.getClass().getName() + " | " + e.getMessage();
				igm=e.getClass().getName();
				logger.error(" {} | {} | {} | {} ", (getSessionUser()!=null?getSessionUser().getUserName():""), e.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName(), e.getMessage());
			}
		
			try {
													
				String im1 	= NumberFormatter.formatNumber(service.getMeterIndexMetainfoTasks().getOneMinuteRate()).trim();
				String im5 	= NumberFormatter.formatNumber(service.getMeterIndexMetainfoTasks().getFiveMinuteRate()).trim();
				String im15 = NumberFormatter.formatNumber(service.getMeterIndexMetainfoTasks().getFifteenMinuteRate()).trim();
				
				imm = String.format("%6.2f", service.getMeterIndexMetainfoTasks().getMeanRate()).trim() + " <span class=\"ago\">task/sec</span>";;
						
				rate_im = im1 + "<span class=\"separator\">|</span>" +
						  im5 + "<span class=\"separator\">|</span>" +
						  im15;
				
				String ia1 	= NumberFormatter.formatNumber( service.getMeterIndexAttachmentsTasks().getOneMinuteRate()).trim();
				String ia5 	= NumberFormatter.formatNumber( service.getMeterIndexAttachmentsTasks().getFiveMinuteRate()).trim();
				String ia15 = NumberFormatter.formatNumber( service.getMeterIndexAttachmentsTasks().getFifteenMinuteRate()).trim();
				
				iam = NumberFormatter.formatNumber(service.getMeterIndexAttachmentsTasks().getMeanRate()).trim() + " <span class=\"ago\">task/sec</span>";
							
				rate_ia = ia1 + "<span class=\"separator\">|</span>" +
						  ia5 + "<span class=\"separator\">|</span>" +
						  ia15;
				
			} catch (Exception e) {
				
				rate_im=e.getClass().getName();
				rate_ia=e.getClass().getName();
				imm=e.getClass().getName();
				iam=e.getClass().getName();
				logger.error(e);
			}
			
			data.add(new Tuple( "Index Metainfo 	task/sec (1m 5m 15m) ",  rate_im));
			data.add(new Tuple( "Index Attachments  task/sec (1m 5m 15m) ",  rate_ia));
			data.add(new Tuple( "Index Total		task/sec (1m 5m 15m) ",  rate_ig));
			
			data.add(new Tuple( "Index Metainfo 	mean rate ",  imm));
			data.add(new Tuple( "Index Attachments  mean rate ",  iam));
			data.add(new Tuple( "Index Total		mean rate",  igm));
										
			data.add(new Tuple( "Index Metainfo Total ",  	NumberFormatter.formatNumber(service.getMeterIndexMetainfoTasks().getCount()).trim()));
			data.add(new Tuple( "Index Attachments Total ", NumberFormatter.formatNumber(service.getMeterIndexAttachmentsTasks().getCount()).trim()));
			
			
			
		} catch (Exception e) {
			data.add(new Tuple( "Index. ",  e.getClass().getName()+" | " + e.getMessage()));
			logger.error(e);		
		}
		finally {
			long end = System.currentTimeMillis();
			if (logger.isDebugEnabled())
			data.add(new Tuple("Render time ", String.valueOf(end-start)+" <span class=\"ago\">ms</span>"));
			logger.debug("Render time " + String.valueOf(end-start)+" ms");
		}
		return data;
	}

	public void attempToFixSiteIndex() {
		
		if (fix_enabled.equals("no"))
			return;

		if (!_site_map.containsKey(getDomain().getId()))
			_site_map.put(getDomain().getId(), OffsetDateTime.now().minusSeconds(ATTEMP_INTERVAL_SECS+1));
		
		synchronized (this) {
			
			if (OffsetDateTime.now().isAfter(  _site_map.get(getDomain().getId()).plusSeconds(ATTEMP_INTERVAL_SECS)) ) {
					_site_map.put(getDomain().getId(),OffsetDateTime.now());
					logger.debug("----------------------------------------------");
					logger.debug("attempt to fix sites of domain  " + getDomain().getName());
					
                    try {

                    	// Reindex
                    	//
                    	BatchReindexCommand command;
                    	command = (BatchReindexCommand) ServiceLocator.getService(BeansService.class).getBean("ReindexBatchCommand");
    					Map<String, Object> mz = new HashMap<String, Object>();
    					mz.put("statement", "from KbeeSite K where K.domain.id=" + getDomain().getId().toString() );
    					command.setParameters(mz); 
    			        CommandService service = ServiceLocator.getService(CommandService.class);
    			        service.add(command);
    			        
    			        Thread.sleep(500);
    			        
    			        int n=0;
    			        while (!command.isTerminated() && n++<10) 
    			        	Thread.sleep(1000);
    			        
                    	// Clean
                    	//
    					Map<String, Object> map = new HashMap<String, Object>();
    					map.put("statement", "type:site");
    			        Command command_clean = (Command) ServiceLocator.getService(BeansService.class).getBean("CleanBatchCommand");
    			        command_clean.setParameters(map);
    		        	service.add(command_clean);
    		        	logger.debug("done");
                    } 
                    catch (Exception e) {
                        logger.error(e);
                    }
                    
			}
		}
	}
	
	
	@Override
	public void attempToFixIndex(User user) {
		
		if (fix_enabled.equals("no"))
				return;
		
		if (!_map.containsKey(user.getId()))
			_map.put(user.getId(), OffsetDateTime.now().minusSeconds(ATTEMP_INTERVAL_SECS+1));
		
		synchronized (this) {
			
			if (OffsetDateTime.now().isAfter(  _map.get(user.getId()).plusSeconds(ATTEMP_INTERVAL_SECS)) ) {
					_map.put(user.getId(),OffsetDateTime.now());
					logger.debug("----------------------------------------------");
					logger.debug("attempt to fix workflow consoles -> " + user.getUserName());
					
                    try {
                        ((KbeeUser) user).getService(UserSelfService.class).sessionFlush();
                        ((KbeeUser) user).getService(UserSelfService.class).reindex();
                        RemoveOrphansCommand ro = new com.novamens.kbee.content.command.RemoveOrphansCommand(((KbeeUser) user).getId());
                        ro.execute();
                    } 
                    catch (Exception e) {
                        logger.error(e);
                    }
			}
		}
	}


	@Override
	public void attempToFixSecurityIndex() {
		
		if (fix_enabled.equals("no"))
				return;
		
		String key = getDomain().getId().toString();
		if (!_sec_map.containsKey(key))
			_sec_map.put(key, OffsetDateTime.now().minusSeconds(ATTEMP_INTERVAL_SECS+1));
		
		synchronized (this) {
			
			if (OffsetDateTime.now().isAfter(  _sec_map.get(key).plusSeconds(ATTEMP_INTERVAL_SECS)) ) {
					_sec_map.put(key,OffsetDateTime.now());
					logger.debug("----------------------------------------------");
					logger.debug("attempt to fix Security (Users, Roles) -> " + key);
					
                    try {

                    	
                        CleanIndexCommand cleanIndexCommand = new CleanIndexCommand("type:datasetmember or type:user or type:role", key);
                        cleanIndexCommand.execute();

                        CommandService service = ServiceLocator.getService(CommandService.class);
                        
    					BatchReindexCommand command_1;
    					command_1 = (BatchReindexCommand) ServiceLocator.getService(BeansService.class).getBean("ReindexBatchCommand");
    					Map<String, Object> mz_1 = new HashMap<String, Object>();
    					mz_1.put("statement", "from KbeeUser" );
    					mz_1.put("limit", "50000" );
    					command_1.setParameters(mz_1); 
    			        service.register(command_1);
    			        command_1.execute();
    					logger.debug("----------------------------------------------");


    					BatchReindexCommand command_3;
    					command_3 = (BatchReindexCommand) ServiceLocator.getService(BeansService.class).getBean("ReindexBatchCommand");
    					Map<String, Object> mz_3 = new HashMap<String, Object>();
    					mz_3.put("statement", "from KbeeAbstractRole" );
    					mz_3.put("limit", "50000" );
    					command_3.setParameters(mz_3); 
    			        service.register(command_3);
    			        command_3.execute();
    					logger.debug("----------------------------------------------");

    					
    					BatchReindexCommand command_2;
    					command_2 = (BatchReindexCommand) ServiceLocator.getService(BeansService.class).getBean("ReindexBatchCommand");
    					Map<String, Object> mz_2 = new HashMap<String, Object>();
    					mz_2.put("statement", "from KbeePersonMember K where K.domain.id="+getDomain().getId().toString() );
    					mz_2.put("limit", "100000" );
    					command_2.setParameters(mz_2); 
    			        service.register(command_2);
    			        command_2.execute();
    					logger.debug("----------------------------------------------");
                        
                    } 
                    catch (Exception e) {
                        logger.error(e);
                    }
					
			}
		}
	}

	
	
	public void attempToReindexContent(Content content) {

		if (fix_enabled.equals("no"))
			return;

		
		if (content==null)
			return;
		
		synchronized (this) {
        	BatchReindexCommand command;
			command = (BatchReindexCommand) ServiceLocator.getService(BeansService.class).getBean("ReindexBatchCommand");
			Map<String, Object> mz = new HashMap<String, Object>();
			mz.put("statement", "from KbeeContent where id="+content.getId().toString() );
			
			logger.debug( "from KbeeContent where id="+content.getId().toString() );
			
			command.setParameters(mz); 
	        CommandService service = ServiceLocator.getService(CommandService.class);
	        service.add(command);
	        try {
            	Thread.sleep(500);
		        int n=0;
		        while (!command.isTerminated() && n++<10) 
		        	Thread.sleep(1000);
            }
	        catch (Exception e) {
                logger.error(e);
	        }
		}
	}
	
	
	/**
	 * 
	 * 
	 */
	@Override
	public void attempToFixDomainIndex() {
		
		if (fix_enabled.equals("no"))
			return;

		if (!_dom_map.containsKey("kbee"))
			_dom_map.put("kbee", OffsetDateTime.now().minusSeconds(ATTEMP_INTERVAL_SECS+1));
		
		synchronized (this) {
			
			if (OffsetDateTime.now().isAfter(  _dom_map.get("kbee").plusSeconds(ATTEMP_INTERVAL_SECS)) ) {
				 	_dom_map.put("kbee",OffsetDateTime.now());
					logger.debug("----------------------------------------------");
					logger.debug("attempt to fix domains ");
					
                    try {

                    	// Reindex
                    	//
                    	BatchReindexCommand command;
    					command = (BatchReindexCommand) ServiceLocator.getService(BeansService.class).getBean("ReindexBatchCommand");
    					Map<String, Object> mz = new HashMap<String, Object>();
    					mz.put("statement", "from KbeeDomain" );
    					command.setParameters(mz); 
    			        CommandService service = ServiceLocator.getService(CommandService.class);
    			        service.add(command);
    			        
    			        Thread.sleep(500);
    			        
    			        int n=0;
    			        while (!command.isTerminated() && n++<10) 
    			        	Thread.sleep(1000);
    			        
                    	// Clean
                    	//
    					Map<String, Object> map = new HashMap<String, Object>();
    					map.put("statement", "type:domain");
    			        Command command_clean = (Command) ServiceLocator.getService(BeansService.class).getBean("CleanBatchCommand");
    			        command_clean.setParameters(map);
    		        	service.add(command_clean);
    		        	logger.debug("done");
                    } 
                    catch (Exception e) {
                        logger.error(e);
                    }
			}
		}
	}

	
	  @Override
	    public boolean listen(Event event) {
	        if (event instanceof EvictCacheServiceEvent)
	            return true;
	        return false;
	    }

	    @Override
	    public void onEvent(Event event) {
	    	if (event instanceof EvictCacheServiceEvent) {
			    	_map.clear();
			    	_dom_map.clear();
			    	_sec_map.clear();
			    	_site_map.clear();
	    	}
	    	
	    }

	@Override
	public boolean isSupportEnabled() {
		
		if (IS_SUPPORT_ENABLED==null) {
			try {
			String s=getContentDao().findSystemParameterValueByKey("support.enable", PropertiesFactory.getInstance("kbee").getProperties().getProperty("support.enable", "no"));
			IS_SUPPORT_ENABLED = Boolean.valueOf(s.equals("yes"));
			} catch (Exception e) {
				logger.error(e);
				IS_SUPPORT_ENABLED = Boolean.valueOf(false);
			}
		}
		return IS_SUPPORT_ENABLED.booleanValue();
	}
	

	@Override
	public OffsetDateTime getDateAppStarted() {
		return start_date;
	}
	
	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
	
	private ContentDao getContentDao() {
		return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}

	
	
	
}
