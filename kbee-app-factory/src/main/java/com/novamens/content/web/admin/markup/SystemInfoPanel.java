package com.novamens.content.web.admin.markup;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.codahale.metrics.Meter;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.datetime.DateTimeService;
import com.novamens.dom.Domain;
import com.novamens.dom.KBFSStorageType;
import com.novamens.indexer.java.FileIndexerService;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.java.LogIndexerService;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.command.CommandService;
import com.novamens.kbee.content.webapi.traffic.TrafficControlService;
import com.novamens.kbee.kbfs.KbeeShardedMinioFileServer;
import com.novamens.kbee.kbfs.KbeeShardedOdilonFileServer;
import com.novamens.kbee.system.SystemParameters;
import com.novamens.kbee.wicket.markup.html.areainfo.AreaInfoPanel;
import com.novamens.kbee.wicket.markup.html.areainfo.GridInfoPanel;
import com.novamens.kbfs.FileServerMinio;
import com.novamens.kbfs.FileServerOdilon;
import com.novamens.kbfs.FileServerS3;
import com.novamens.kbfs.v1.FileServerV1;
import com.novamens.metrics.SystemMetricsService;
import com.novamens.metrics.domain.DomainMetricsService;
import com.novamens.scheduler.SchedulerService;
import com.novamens.service.ApplicationServerService;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.solr.indexer.query.SolrQuery;
import com.novamens.system.properties.SystemPropertiesService;
import com.novamens.wicket.util.BCElement;

import kbee.util.NumberFormatter;
import kbee.util.PropertiesFactory;
import kbee.util.Tuple;

/**
 * 
 * EMAIL USAGE
 * ----------- 
 * select  extract( year from event_time) "Year",  event_generator_action "Action", count(*) "Total"   from kb_sendemailevent  where  extract( year from event_time) > 2017 group by event_generator_action, extract(year from event_time) order by "Year", "Action"
 * 
 * GB
 * --
 * select to_char(ts, 'yyyy-MM-dd HH:mm') "Timestamp", hard_disk_usage/1000000000 GB from kb_usage_stat where domain_id=(select id from domain where name='kbee') order by ts desc
 * 
 */
public class SystemInfoPanel extends AbstractSystemInfoPanel {
			
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SystemInfoPanel.class.getName());
	
	static private final double GB = 1000000000.0;
	private static final boolean IS_PORTAL_ENABLED = PropertiesFactory.getInstance("kbee").getProperties().getProperty("license.portal", "no").trim().toLowerCase().equals("yes");
	private static final boolean IS_API_ENABLED =  PropertiesFactory.getInstance("kbee").getProperties().getProperty("api.enabled", "yes").toLowerCase().trim().equals("yes");
									
 	private IModel<Domain> domain_model;

	private AbstractAjaxTimerBehavior timer;
	
	private String database = PropertiesFactory.getInstance("kbee").getProperties().getProperty("jdbc.url", null);
	
	
	public SystemInfoPanel() {
		this("info-panel");
	}
	
	public SystemInfoPanel(String id) {
		super(id);
 		setOutputMarkupId(true);
 	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		AreaInfoPanel area = new AreaInfoPanel("info");
		add(area);
		area.setSections(AreaInfoPanel.ONE_SECTION);
		area.setCss("col-lg-12");	
														
		try {
				area.addPanel(new GridInfoPanel("element",  ServiceLocator.getService(ApplicationServerService.class).serversInfo(), new Model<String>("Servers"), true));
		}
		catch (Exception e) {
			logger.error(e);
		}

		try {
			area.addPanel(new GridInfoPanel("element",  keyMetricsInfo(), 			new Model<String>("Key Metrics"), true));
			
			//area.addPanel(new GridInfoPanel("element",	ServiceLocator.getService(ApplicationServerService.class).serversInfo(), 			new Model<String>("Scheduler"), true));
		}
		catch (Exception e) {
			logger.error(e);
		}
		
		if (isKbeeDomain()) {
			area.addPanel(new GridInfoPanel("element",  ObjectStorageInfo(), 			new Model<String>("Object Storage"), true));
		}
		
		area.addPanel(new GridInfoPanel("element",  recentActivityAPIInfo(), 	new Model<String>("API"), true));
		area.addPanel(new GridInfoPanel("element",  serverInfo(), 				new Model<String>("Server"), true));
		
		if (isKbeeDomain())								
			area.addPanel(new GridInfoPanel("element", searchInfo(), 			new Model<String>("Search"), true));
		
		if (isKbeeDomain())
			area.addPanel(new GridInfoPanel("element", dataManagementInfo(), 	new Model<String>("Database"), true));
		
		area.addPanel(new GridInfoPanel("element",  commandsInfo(), 			new Model<String>("Commands"), true));
	}
	
	/***
	 * 
	 */							
	protected List<Tuple> serverInfo() {

		long start = System.currentTimeMillis();
		
		List<Tuple> data = new ArrayList<Tuple>();

		try {
			data.add(new Tuple("Available processors", String.valueOf(Runtime.getRuntime().availableProcessors())+  " cores" ));	 
			
			OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
		 	String la=os.getSystemLoadAverage()>0?String.valueOf(os.getSystemLoadAverage()):"N/A";
		 	String spercent="";
		 	int processors = Runtime.getRuntime().availableProcessors();
			if (processors>0 && os.getSystemLoadAverage()>0) {
				Double percent = Double.valueOf (Double.valueOf(os.getSystemLoadAverage()) / Double.valueOf(processors));
				spercent= "<span class=\"ago\"> ("+ (NumberFormatter.formatNumber(percent.doubleValue()*100.0)).trim()+ " %) </span>";
			}
			data.add(new Tuple("CPU Load Average", la + spercent));
			
			
			data.add(new Tuple("Free memory", String.format("%6.4f", (double) Runtime.getRuntime().freeMemory()/ GB )+  " <span class=\"ago\">GB<span>"));
			long maxMemory = Runtime.getRuntime().maxMemory();
			data.add(new Tuple("Maximum memory", (maxMemory == Long.MAX_VALUE ? "no limit" : String.format("%6.4f", (double) maxMemory / GB ))+  " <span class=\"ago\">GB<span>"));
			data.add(new Tuple("Total memory", String.format("%6.4f",(double) Runtime.getRuntime().totalMemory() / GB )+  " <span class=\" ago\">GB<span>"));
		    String strJavaVersion = System.getProperty("java.specification.version");
		    data.add(new Tuple("JVM Spec", strJavaVersion));
		    
		    long jvmStartTime = ManagementFactory.getRuntimeMXBean().getStartTime();
			LocalDateTime d=ServiceLocator.getService(DateTimeService.class).millsToLocalDateTime(jvmStartTime);
			data.add(new Tuple( "Started", ServiceLocator.getService(DateTimeService.class).timeElapsed(d, getSessionUser().getLocale(), "atright ago")));

			/**
			Enumeration<NetworkInterface> e;
		    
			try {
				
				e = NetworkInterface.getNetworkInterfaces();
				
				while(e.hasMoreElements())
			    {
			        NetworkInterface n=(NetworkInterface) e.nextElement();
			        Enumeration<InetAddress> ee = n.getInetAddresses();
			        while(ee.hasMoreElements())
			        {
			            InetAddress i= (InetAddress) ee.nextElement();
			            data.add(new Tuple("Inet Hostname | Address", i.getHostName()  + " | " + i.getHostAddress()));
			        }
			    }
			} catch (SocketException e1) {
				
				data.add(new Tuple("Inet Address", "ERROR"));
				data.add(new Tuple("Inet Address error", e1.getMessage()));				
			}
			**/

		}
		catch (Exception e) {
			logger.error(e);
			data.add(new Tuple("Error ",  e.getClass().getName() +" " + e.getMessage()));
		}
		
		finally {
			long end=System.currentTimeMillis();
			
			if (logger.isDebugEnabled())
				data.add(new Tuple("Render time ", String.valueOf(end-start)+" <span class=\"ago\">ms</span>"));
		}

		return data;
	}


	
	/**
	 * 
	 */
	protected AbstractAjaxTimerBehavior getTimer() {
		return timer;
	}

	/**
	 * 
	 * @return
	 */
	protected List<Tuple> serversInfo() {
	
		List<Tuple> data = new ArrayList<Tuple>();
		long start = System.currentTimeMillis();

		try  {
				data.add(new Tuple("Web Application", getServerHost()));
				data.add(new Tuple("Database", this.database));
				data.add(new Tuple( "SolR", "<a class=\"btn-link\" href=\""+ PropertiesFactory.getInstance("kbee").getProperties().getProperty("solr.url", "").trim()+"\" target=\"_blank\">" + PropertiesFactory.getInstance("kbee").getProperties().getProperty("solr.url", "").trim() +"</a>" ));
				data.add(new Tuple( "solr.content-core", 	
						PropertiesFactory.getInstance("kbee").getProperties().getProperty("solr.content-core", "").trim()));
				data.add(new Tuple( "solr.file-core", 	    
						PropertiesFactory.getInstance("kbee").getProperties().getProperty("solr.file-core", "")!=null ?
						PropertiesFactory.getInstance("kbee").getProperties().getProperty("solr.file-core", "").trim() :""));

				int n = 1;
				for (String s: getObjectStorageSeversList()) {
					data.add(new Tuple("Object Storage_" + String.valueOf(n++), s));	
				}
				
				
				
				try {
					FileServerS3 s3=ServiceLocator.getService(FileServerS3.class);
					if (s3!=null) {
						  if(s3.isEnabled())
							  data.add(new Tuple("Amazon S3", s3.getEnvironment()));			  
						  else
							  data.add(new Tuple("Amazon S3", "disabled"));
					}
					else {
						data.add(new Tuple("Amazon S3", "not installed"));
					}
				} catch (Exception e) {
					data.add(new Tuple("Amazon S3", e.getClass().getName()));
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

			
	/**
	 * 
	 * @return
	 */
	protected List<Tuple> keyMetricsInfo() {
	
		List<Tuple> data = new ArrayList<Tuple>();
		long start = System.currentTimeMillis();

		try  {
		
			data.add(new Tuple("Server URL (via WebRequest)","<a href=\""+ getServerUrl()+"\" target=\"_blank\">" + getServerUrl() +"</a>"));
			
			// Server id
			data.add(new Tuple( "Server Id", PropertiesFactory.getInstance("kbee").getProperties().getProperty("server.id", "")));
							
			long jvmStartTime = ManagementFactory.getRuntimeMXBean().getStartTime();
			LocalDateTime d=ServiceLocator.getService(DateTimeService.class).millsToLocalDateTime(jvmStartTime);
			data.add(new Tuple( "Started", ServiceLocator.getService(DateTimeService.class).timeElapsed(d, getSessionUser().getLocale(), "ago")));

			
			// OS Load Average
		 	OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
		 	
		 	String la=os.getSystemLoadAverage()>0?String.valueOf(os.getSystemLoadAverage()):(!isLinux()?"Not supported in Windows":"n/a");
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
		 	
		 	String val ="<span class=\""+  class_status +"\"/>"+la + spercent+"</span>";
		 	
			data.add(new Tuple("App CPU Load Average", val));
			
			 

			/**
			try {
				List<Tuple>le=DBloadAverage();
				if (le!=null && le.size()>0) {
					for (Tuple t: le) {
						data.add(new Tuple(t.label, t.value));
					}
				}
			} catch (Exception e) {
				data.add(new Tuple(e.getClass().getName(), e.getMessage()));
				logger.error(e);
			}**/
			

			
			try {
				// Scheduler Status
				SchedulerService service = ServiceLocator.getService(SchedulerService.class);
				String pg=service.getStatus();
				if (pg==null)
					 pg="err";
				boolean isok = pg.toLowerCase().equals("ok");
				String s="<span class=\""+ (isok?"success":"danger") +"\"/>"+pg+"</span>";
				data.add(new Tuple("Scheduler Engine status ", s));
				
			} catch (Exception e) {
				data.add(new Tuple("Scheduler Engine status ",  e.getClass().getName()));
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

				// -----------------------
				// SolR Server
				//
				String pg=pingSolR();
				
				if (pg==null)
					 pg="ok";
				
				boolean isok = pg.toLowerCase().equals("ok");
				String s="<span class= \" "+ (isok?"success":"danger") +"\" />"+pg+"</span>";
				data.add(new Tuple("SolR status ", s));
			} catch (Exception e) {
				data.add(new Tuple("SolR status ",  e.getClass().getName()+" | "+e.getMessage()));
				logger.error(e);
			}		
			
			
			

			FileServerS3 s3=ServiceLocator.getService(FileServerS3.class);
			
			if (s3!=null && s3.isEnabled()) {
						String pg=s3.ping();
						if (pg==null)
							 pg="err";
						boolean isok = pg.toLowerCase().equals("ok");
						String s="<span class= \" "+ (isok?"success":"danger") +"\" />"+pg+"</span>";
						data.add(new Tuple( "Amazon S3 status", s));
			}
			else  {
				data.add(new Tuple( "Amazon S3 status", "disabled"));
			}


			
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
						data.add(new Tuple( "Minio Shard status", s));
				}
			}
			
			
			if (IS_API_ENABLED) {																						
				Integer api_err_5m = getContentDao().executeCountNativeQuery("select count(*) from api_logevent where (event_status=412 or  event_status=403 or event_status=500) and event_time >(now() - INTERVAL '5 minute')\\:\\:timestamp");
				Integer api_err_1h = getContentDao().executeCountNativeQuery("select count(*) from api_logevent where (event_status=412 or  event_status=403 or event_status=500) and event_time >(now() - INTERVAL '1 hour')\\:\\:timestamp");
				Integer api_err_1d = getContentDao().executeCountNativeQuery("select count(*) from api_logevent where (event_status=412 or  event_status=403 or event_status=500) and event_time >(now() - INTERVAL '1 day')\\:\\:timestamp");
				
				if (api_err_5m==0 && api_err_1h==0 && api_err_1d==0) 
					data.add(new Tuple("API. Request status", "<span class= \"success\">ok</span>"));
				else {								
						data.add(new Tuple("API. Request status (errors 403/412/500. 5m 1h 1d)", 							
						NumberFormatter.formatNumber(api_err_5m, getSessionUser().getLocale())    + "<span class=\"separator\">|</span>" +     
						NumberFormatter.formatNumber(api_err_1h, getSessionUser().getLocale())    + "<span class=\"separator\">|</span>" +
						NumberFormatter.formatNumber(api_err_1d, getSessionUser().getLocale())));
				}
			}

			
			try {
				
				int solr_size = ServiceLocator.getService(DomainMetricsService.class).getTotalSolrHeadEnabledContents();
				int db_total  = ServiceLocator.getService(DomainMetricsService.class).getTotalDBHeadEnabledContents();
				
				int total=solr_size+db_total;
				
				if (total>0) {
				
					
					double per = Math.abs(Double.valueOf(solr_size - db_total).doubleValue()) * 100.0 / Double.valueOf(total).doubleValue(); 
					
					boolean is_ok =  (per<1.0);
					boolean is_danger =  (per> 10.0);
					
					String v= NumberFormatter.formatNumber(db_total, getSessionUser().getLocale()) + "<span class=\"separator\">|</span>" + 
							NumberFormatter.formatNumber(solr_size, getSessionUser().getLocale());
					
					String s="<span class= \" "+ (is_ok? "success":  ( is_danger?"danger": "warning")) +"\" />"+ v +"</span>" + 
							"<span style=\"padding:4px 8px; float:left;\"> (" + ServiceLocator.getService(DateTimeService.class).format(
									ServiceLocator.getService(DomainMetricsService.class).getHealthCheckOffsetDateTime(), 
									getSessionUser().getZoneId().getId(), 
									getSessionUser().getLocale(),
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
	
	/**
	 *  Request mean processing time 1m 5m 1h
	 *  Requests received 1m 5m 1h
	 */
	protected List<Tuple> recentActivityAPIInfo() {
		
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
					data.add(new Tuple("API. Requests status", "<span class= \"success\">ok</span>"));
				else {
						data.add(new Tuple("API. Requests status (errors 403/412/500. 5m 1h 1d)", 							
						NumberFormatter.formatNumber(api_err_5m, getSessionUser().getLocale())    + "<span class=\"separator\">|</span>" +     
						NumberFormatter.formatNumber(api_err_1h, getSessionUser().getLocale())    + "<span class=\"separator\">|</span>" +
						NumberFormatter.formatNumber(api_err_1d, getSessionUser().getLocale())));
				}
				
				String trafficTokens = SystemParameters.get("com.novamens.content.webapi.traffictokens", String.valueOf( TrafficControlService.DEFAULT_TOKENS));
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

	/**
	 *
	 */
	protected List<Tuple> recentActivityInfo() {
		
		long start = System.currentTimeMillis();
		
		List<Tuple> data = new ArrayList<Tuple>();
		
		try {
			
		
		SystemMetricsService metrics_service = ServiceLocator.getService(SystemMetricsService.class);
		
		String em=String.format("%12.2f  <span class=\"separator\">|</span> %12.2f   <span class=\"separator\">|</span>  %12.2f", 
				  metrics_service.getMeterEmails().getOneMinuteRate() 		* 60,
				  metrics_service.getMeterEmails().getFiveMinuteRate() 		* 60,
				  metrics_service.getMeterEmails().getFifteenMinuteRate() 	* 60);	
				
		String cm=String.format("%12.2f  <span class=\"separator\">|</span> %12.2f   <span class=\"separator\">|</span>  %12.2f", 
				  metrics_service.getMeterContentCheckin().getOneMinuteRate() 		* 60,
				  metrics_service.getMeterContentCheckin().getFiveMinuteRate() 		* 60,
				  metrics_service.getMeterContentCheckin().getFifteenMinuteRate() 	* 60);	

		data.add(new Tuple("Emails rate (1m 5m 15m)", em 			+ " <span clasS=\"ago atright\">event/min</span>" ));
		data.add(new Tuple("Checkin Internal (1m 5m 15m)", cm 		+ " <span clasS=\"ago atright\">event/min</span>" ));

	 	long v= metrics_service.getCounterUsersLogged().getCount();
	 	String tot_users=String.valueOf(ServiceLocator.getService(SecurityService.class).getTotalActiveUsers()) + "<span class=\"separator\">|</span>" + String.valueOf(v>0?v:0); 
	 	data.add(new Tuple("Active Users (Sessions | Sign In - Sign Out)", tot_users));

		
		// These meters are in: events/minute
		//
		String login=String.format("%12.2f  <span class=\"separator\">|</span> %12.2f   <span class=\"separator\">|</span>  %12.2f", 
				  metrics_service.getMeterLogin().getOneMinuteRate() 	 * 60,
				  metrics_service.getMeterLogin().getFiveMinuteRate() 	 * 60,
				  metrics_service.getMeterLogin().getFifteenMinuteRate() * 60);	
		
		String wp=String.format("%12.2f  <span class=\"separator\">|</span> %12.2f   <span class=\"separator\">|</span>  %12.2f", 
				  metrics_service.getMeterWebPages().getOneMinuteRate() 	* 60,
				  metrics_service.getMeterWebPages().getFiveMinuteRate() 	* 60,
				  metrics_service.getMeterWebPages().getFifteenMinuteRate() * 60);	

				
		// Mean rate is in events/sec
		//
		String login_mean= NumberFormatter.formatNumber(metrics_service.getMeterLogin().getMeanRate() 	* 60) 	+ " <span class=\"ago\">login/min</span>";
		String em_mean   = NumberFormatter.formatNumber(metrics_service.getMeterEmails().getMeanRate() 	* 60) 	+ " <span class=\"ago\">email/min</span>";
		String wp_mean   = NumberFormatter.formatNumber(metrics_service.getMeterWebPages().getMeanRate() * 60) 	+ " <span class=\"ago\">page/min</span>";
		
		
		data.add(new Tuple("Login rate (1m 5m 15m)", login 			+ " <span clasS=\"ago atright\">event/min</span>" ));
		data.add(new Tuple("Total Webpages/min (1m 5m 15m)", wp 	+ " <span clasS=\"ago atright\">event/min</span>" ));		
	
		
		data.add(new Tuple("Login mean rate", login_mean));
		data.add(new Tuple("Emails mean rate", em_mean));
		data.add(new Tuple("Total Webpages mean rate", wp_mean));
		
		if (IS_PORTAL_ENABLED) {
					  String portal=String.format("%12.2f  <span class=\"separator\">|</span> %12.2f   <span class=\"separator\">|</span>  %12.2f", 
					  metrics_service.getMeterPortalPages().getOneMinuteRate() 		* 60,
					  metrics_service.getMeterPortalPages().getFiveMinuteRate() 	* 60,
					  metrics_service.getMeterPortalPages().getFifteenMinuteRate() 	* 60);
					  data.add(new Tuple("Portal Webpages (1m 5m 15m)", portal+ "<span clasS=\"ago atright\">event/min</span>" ));
		}
	 	
	 	
	 	OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
	 	
	 	String la=os.getSystemLoadAverage()>0?String.valueOf(os.getSystemLoadAverage()):"N/A";
	 	String spercent="";
	 	int processors = Runtime.getRuntime().availableProcessors();
		if (processors>0 && os.getSystemLoadAverage()>0) {
			Double percent = Double.valueOf(os.getSystemLoadAverage()) / Double.valueOf(processors);
			spercent= "<span class=\"ago\"> ("+ (NumberFormatter.formatNumber(percent.doubleValue()*100.0)).trim()+ "%) </span>";
		}
	 		
		data.add(new Tuple("CPU Load Average", la + spercent));
		
		long jvmStartTime = ManagementFactory.getRuntimeMXBean().getStartTime();
		LocalDateTime d=ServiceLocator.getService(DateTimeService.class).millsToLocalDateTime(jvmStartTime);
		data.add(new Tuple( "Started", ServiceLocator.getService(DateTimeService.class).timeElapsed(d, getSessionUser().getLocale(), "ago")));
		
		} catch (Exception e) {
			data.add(new Tuple( "Error ",  	e.getClass().getName() + ". " + e.getMessage()));
			logger.error(e);
		}
		finally {
			long end = System.currentTimeMillis();
			data.add(new Tuple("Render time ", String.valueOf(end-start)+" <span class=\"ago\">ms</span>"));
			logger.debug("Render time " + String.valueOf(end-start)+" ms");
		}
		
		return data;
	}


	
	
	
	
	
	/**
	 * 	
	 * 
	 * @return
	 */
	private List<Tuple> searchInfo() {
		
		long start = System.currentTimeMillis();
		List<Tuple> data = new ArrayList<Tuple>();
		
		try {
			
			try {
			
				data.add(new Tuple( "solr.url", 			
						
						"<a href=\""+ PropertiesFactory.getInstance("kbee").getProperties().getProperty("solr.url", "").trim()+"\" target=\"_blank\">" + PropertiesFactory.getInstance("kbee").getProperties().getProperty("solr.url", "").trim() +"</a>"
						));
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
	

	protected Index getFileIndex() {
		return getDomain().getService(FileIndexerService.class).getIndex();
	}

	
	protected Index getQueryIndex() {
		return getDomain().getService(JavaIndexerService.class).getIndex();
	}

	protected Index getAuditIndex() {
		return getDomain().getService(LogIndexerService.class).getIndex();
	}
	
	
	/**
	 * 	
	 * @return
	
	private List<Tuple> schedulerInfo() {
		
		long start = System.currentTimeMillis();
		
		List<Tuple> data = new ArrayList<Tuple>();

		SchedulerService service = ServiceLocator.getService(SchedulerService.class);
	
	
		try {
			String pg=service.getStatus();
			if (pg==null)
				 pg="err";
			boolean isok = pg.toLowerCase().equals("ok");
			String s="<span class= \" "+ (isok?"success":"danger") +"\" />"+pg+"</span>";
			data.add(new Tuple("Scheduler Engine status ", s));
		} catch (Exception e) {
			data.add(new Tuple("Scheduler Engine status. ",  e.getClass().getName()));
			logger.error(e);
		}
		
		try {
			data.add(new Tuple( "Scheduler. Queue in db (Std Err)",  			NumberFormatter.formatNumber(service.getQueueSize()) + "<span class=\"separator\">|</span>" + String.valueOf(service.getErrorQueueSize())));
			data.add(new Tuple("Scheduler. Total items (Batches | Requests)", 	NumberFormatter.formatNumber(service.getTotalBatches()) + "<span class=\"separator\">|</span>" +String.valueOf(service.getTotalInBatches()))); 						// total batches in the system			
		} catch (Exception e) {
			data.add(new Tuple( "Scheduler. Queue Std Size", e.getClass().getName() + " | " + e.getMessage()));
		}

		
		
		try {
			double diff1 = service.getOneMinuteInputRateHp() - service.getOneMinuteThroughPutHP();
			double diff5 = service.getFiveMinuteInputRateHp() - service.getFiveMinuteThroughPutHP();
			double diff15 = service.getFifteenMinuteInputRateHp() - service.getFifteenMinuteThroughPutHP();
			
			
			double diffthp = (diff1>0?diff1:0) + (diff5>0?diff5:0) +  (diff15>0?diff15:0);  

			String cs_hp;
			
			if (diffthp>1.15) 				cs_hp = "warning";
			else if (diffthp>1.4)			cs_hp = "danger";
			else 							cs_hp = "stack";

			double diff1l 	= service.getOneMinuteInputRateLp() - service.getOneMinuteThroughPutLP();
			double diff5l 	= service.getFiveMinuteInputRateLp() - service.getFiveMinuteThroughPutLP();
			double diff15l 	= service.getFifteenMinuteInputRateLp() - service.getFifteenMinuteThroughPutLP();

			double difftlp = (diff1l>0?diff1l:0) + (diff5l>0?diff5l:0) +  (diff15l>0?diff15l:0);
			
			String cs_lp;
			
			if (difftlp>1.15)				cs_lp = "warning";
			else if (difftlp>1.5)			cs_lp = "danger";
			else							cs_lp = "stack";

			String v1a = NumberFormatter.formatNumber(service.getOneMinuteInputRateHp()).trim();
			String v1b = NumberFormatter.formatNumber( service.getOneMinuteThroughPutHP()).trim();
			
			String v2a = NumberFormatter.formatNumber(service.getFiveMinuteInputRateHp()).trim();
			String v2b = NumberFormatter.formatNumber(service.getFiveMinuteThroughPutHP()).trim();
			
			String v3a = NumberFormatter.formatNumber(service.getFifteenMinuteInputRateHp()).trim();
			String v3b = NumberFormatter.formatNumber(service.getFifteenMinuteThroughPutHP()).trim();
			
			

			
			String rate_hp = 	"<div class=\""+ cs_hp +"\"> <b>&nbsp;1m.&nbsp;</b>  " 	+ v1a + "<span class=\"internal-separator\">/</span>" + v1b +  "<span class=\"separator\">|</span></div>" +
								"<div class=\""+ cs_hp +"\"> <b>&nbsp;5m.&nbsp;</b>  " 	+ v2a + "<span class=\"internal-separator\">/</span>" + v2b + "<span class=\"separator\">|</span></div>" + 
								"<div class=\""+ cs_hp +"\"> <b>15m.&nbsp;</b> " 		+ v3a + "<span class=\"internal-separator\">/</span>" + v3b + "</div>";
																					
			String vl1a = NumberFormatter.formatNumber( service.getOneMinuteInputRateLp()).trim();
			String vl1b = NumberFormatter.formatNumber( service.getOneMinuteThroughPutLP()).trim();
			
			String vl2a = NumberFormatter.formatNumber( service.getFiveMinuteInputRateLp()).trim();
			String vl2b = NumberFormatter.formatNumber(service.getFiveMinuteThroughPutLP()).trim();
			
			String vl3a = NumberFormatter.formatNumber(service.getFifteenMinuteInputRateLp()).trim();
			String vl3b = NumberFormatter.formatNumber(service.getFifteenMinuteThroughPutLP()).trim();
						
			String rate_lp = 	"<div class=\""+ cs_lp +"\"> <b>&nbsp;1m.&nbsp;</b> " + vl1a + "<span class=\"internal-separator\">/</span>" + vl1b + "<span class=\"separator\">|</span></div>" +
								"<div class=\""+ cs_lp +"\"> <b>&nbsp;5m.&nbsp;</b> " + vl2a + "<span class=\"internal-separator\">/</span>" + vl2b + "<span class=\"separator\">|</span></div>" + 
								"<div class=\""+ cs_lp +"\"> <b>15m.&nbsp;</b>" + vl3a + "<span class=\"internal-separator\">/</span>" + vl3b + "</div>";
			

			
			
			data.add(new Tuple( "Scheduler HP I/O req/sec (1m 5m 15m) ",  rate_hp));
			data.add(new Tuple( "Scheduler LP I/O req/sec (1m 5m 15m) ",  rate_lp));

			String mean_rate_i_hp = NumberFormatter.formatNumber(service.getMeanHPIn(), getSessionUser().getLocale())  + " <span class=\"atright ago\">req/sec</span>";  
			String mean_rate_i_lp = NumberFormatter.formatNumber(service.getMeanLPIn(), getSessionUser().getLocale())  + " <span class=\"atright ago\">req/sec</span>";
			String mean_rate_o_hp = NumberFormatter.formatNumber(service.getMeanHPOut(), getSessionUser().getLocale()) + " <span class=\"atright ago\">req/sec</span>";
			String mean_rate_o_lp = NumberFormatter.formatNumber(service.getMeanLPOut(), getSessionUser().getLocale()) + " <span class=\"atright ago\">req/sec</span>";
			 
			data.add(new Tuple( "Scheduler HP In mean rate ",  mean_rate_i_hp));
			data.add(new Tuple( "Scheduler LP In mean rate ",  mean_rate_i_lp));
											
			data.add(new Tuple( "Scheduler HP Out mean rate ",  mean_rate_o_hp));
			data.add(new Tuple( "Scheduler LP Out mean rate ",  mean_rate_o_lp));
			
			
		} catch (Exception e) {
			data.add(new Tuple("Scheduler Engine status. ",  e.getClass().getName()));
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
 */
	
	/**
	 * 
	 * 
	 * @param meter
	 * @param unit
	 * @return
	 */
	private String getMeterStr(Meter meter, String unit) {
		String c1 	= NumberFormatter.formatNumber(meter.getOneMinuteRate(), getSessionUser().getLocale()).trim();
		String c2 	= NumberFormatter.formatNumber(meter.getFiveMinuteRate(), getSessionUser().getLocale()).trim();
		String c3   = NumberFormatter.formatNumber(meter.getFifteenMinuteRate(), getSessionUser().getLocale()).trim();
		return    c1 + "<span class=\"separator\">|</span>" +
				  c2 + "<span class=\"separator\">|</span>" +
				  c3 + ( unit!=null?" <span class=\"ago\">"+unit+"</span>":"");
	}


	/**
	 * 
	 * @return
	 */
	private List<Tuple> commandsInfo() {
		
		long start = System.currentTimeMillis();
		
		List<Tuple> data = new ArrayList<Tuple>();

		try {
			SystemMetricsService ms = ServiceLocator.getService(SystemMetricsService.class);
			CommandService 	cs = ServiceLocator.getService(CommandService.class);
										
			data.add(new Tuple("Metrics. Register in Service (1m 5m 15m)  comm/sec", getMeterStr(ms.getMeterCommandsIn(), null)));
			data.add(new Tuple("Metrics. Start Async execution (1m 5m 15m) comm/sec", getMeterStr(ms.getMeterCommandsStartExecution(), null)));
			data.add(new Tuple("Metrics. Terminated (1m 5m 15m) comm/sec", getMeterStr(ms.getMeterCommandsTerminated(), null)));
			data.add(new Tuple("Total Commands",  	NumberFormatter.formatNumber(cs.getTotalCommands(), getSessionUser().getLocale()).trim() ));
			data.add(new Tuple("Total Terminated",  NumberFormatter.formatNumber(cs.getTotalTerminatedCommands(), getSessionUser().getLocale()).trim()));
			data.add(new Tuple("Date last clean up", cs.getDateLastCleanUp()!=null?cs.getDateLastCleanUp().toString():""));
		
		} catch (Exception e) {
			data.add(new Tuple("Scheduler Engine status. ",  e.getClass().getName()));
			logger.error(e);
					
		} finally {
			long end = System.currentTimeMillis();
			if (logger.isDebugEnabled())
			data.add(new Tuple("Render time ", String.valueOf(end-start)+" <span class=\"ago\">ms</span>"));
			logger.debug("Render time " + String.valueOf(end-start)+" ms");
		}
		return data;
	}

	/***
	 * @return
	 */
	private boolean isPostgreSQL() {
		database=database.trim();
		if (database==null)
			return false;
		if(database.contains("oracle"))  
			return false;
		return true;
	}

	
	
	protected boolean isSolrCompiled() {
		return PropertiesFactory.getInstance("kbee").getProperties().getProperty("jdbc.url", null)==null; 
	}
	/** 
	 * API_logEvent 
	 * API_soapEvent
	 * 
	 * @return
	 */
	
	protected List<Tuple> dataManagementInfo() {
	
		long start = System.currentTimeMillis();
		
		List<Tuple> data = new ArrayList<Tuple>();
		
		try {
			data.add(new Tuple("Database. Version", getContentDao().getDatabaseVersion()));
			data.add(new Tuple("Database. URL", PropertiesFactory.getInstance("kbee").getProperties().getProperty("jdbc.url", ""))); 
			data.add(new Tuple("Database. Size", NumberFormatter.formatFileSize(getContentDao().getDatabaseSize(), getSessionUser().getLocale(), "ago")));
	
			
			if (isSolrCompiled()) {
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
			

			/**try {
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
			data.add(new Tuple("Render time ", String.valueOf(System.currentTimeMillis()-start)+" <span class=\"ago\">ms</span>"));
			logger.debug("Render time " + String.valueOf(System.currentTimeMillis()-start)+" ms");
		}
		return data;
	}
	
	

	@Override
	public void onDetach() {
		if(domain_model!=null)
			domain_model.detach();
		super.onDetach();
	}


	protected List<Tuple> dbInfo() {
		
		long start = System.currentTimeMillis();
		
		List<Tuple> data = new ArrayList<Tuple>();
		
		try {
			data.add(new Tuple("Database. Version", getContentDao().getDatabaseVersion()));
			data.add(new Tuple("Database. URL", PropertiesFactory.getInstance("kbee").getProperties().getProperty("jdbc.url", ""))); 
			data.add(new Tuple("Database. Size", NumberFormatter.formatFileSize(getContentDao().getDatabaseSize()))); 
			data.add(new Tuple("User", PropertiesFactory.getInstance("kbee").getProperties().getProperty("jdbc.username", "") + " (" +
									   PropertiesFactory.getInstance("kbee").getProperties().getProperty("jdbc.password", "") +")"		
			));
		}
		catch (Exception e) {
			logger.error(e, "dbInfo ");
			data.add(new Tuple("Error ", e.getClass().getName()));
		}
		finally {
			long end = System.currentTimeMillis();
			if (logger.isDebugEnabled())
			data.add(new Tuple("Render time ", String.valueOf(end-start)+" <span class=\"ago\">ms</span>"));
			logger.debug("Render time " + String.valueOf(end-start)+" ms");
		}
		return data;
	}
	
	protected BCElement getPageBCElement() {
		return new BCElement(new Model<String>("Dashboard"));
	}

	/***
	 */
	protected List<Tuple> fileServerInfo() {
			
		long start = System.currentTimeMillis();
		
		List<Tuple> data = new ArrayList<Tuple>();
			
		try {
				File frep = new File(ServiceLocator.getService(FileServerV1.class).getRootDirectory());
				data.add(new Tuple( "Root dir.", ServiceLocator.getService(FileServerV1.class).getRootDirectory()));
	
				if (frep!=null) {		
					double fr_tot = (double) frep.getTotalSpace()/GB;
					double fr_usa = (double) frep.getUsableSpace()/GB;
					double usa_por = (fr_tot>0 ?fr_usa / fr_tot : 0) * 100.0; 
					String usa_por_str = String.format("%6.2f", usa_por).trim(); 
					data.add(new Tuple( "Absolute path", frep.getAbsolutePath()));
					data.add(new Tuple("File System Files", NumberFormatter.formatNumber(ServiceLocator.getService(FileServerV1.class).getTotalFiles())));
					data.add(new Tuple("File System Size",  NumberFormatter.formatFileSize(ServiceLocator.getService(FileServerV1.class).getSize())));
					data.add(new Tuple("Disk Total",  NumberFormatter.formatNumber(fr_tot) +  " <span class=\"ago\">GB</span>"));
					data.add(new Tuple("Disk Usable", NumberFormatter.formatNumber(fr_usa)+  " <span class=\"ago\">GB ( " +  usa_por_str + " %) </span>"));
				}
				
				data.add(new Tuple( "Ping", ServiceLocator.getService(FileServerV1.class).ping()));
				data.add(new Tuple( "Encrypted.", ServiceLocator.getService(FileServerV1.class).isEncrypted()?"YES":"No"));
				
				long end = System.currentTimeMillis();
				if (logger.isDebugEnabled())
				data.add(new Tuple("Render time ", String.valueOf(end-start)+" <span class=\"ago\">ms</span>"));
				logger.debug("File Service time " + String.valueOf(end-start)+" ms");
				
		} catch (Exception e) {
					logger.error(e);
					data.add(new Tuple( "Error",  e.getClass().getName()));
		}
		return data;
	}

	
	private KBFSStorageType getDefaultKBFSStorageType() {
		return KBFSStorageType.getByKey(getContentDao().findSystemParameterValueByKey("kbfs.storage.default", ServiceLocator.getService(SystemPropertiesService.class).getDefaultKBFSService()));
	}
	
	
	
	
	private List<String> getObjectStorageSeversList() {
		List<String> list = new ArrayList<String>();
		boolean kbfs2_enabled=PropertiesFactory.getInstance("kbee").getProperties().getProperty("Minio.enabled", "yes").toLowerCase().trim().equals("yes");
		if (kbfs2_enabled) {
			FileServerMinio fsv2=ServiceLocator.getService(FileServerMinio.class);
			if (fsv2 instanceof KbeeShardedMinioFileServer) {
				try {
					for (Entry<Integer, FileServerMinio> entry: ((KbeeShardedMinioFileServer) fsv2).getShards().entrySet()) {
						list.add(entry.getValue().getEndPoint());
					}
				} catch (Exception e) {
					logger.error(e);
					list.add(e.getClass().getName());
				}	
			}
			else {
				list.add(fsv2.getEndPoint());
			}
		}
		else
			list.add("Minio disabled");
		
		
		
		return list;
				
	}
	
	/***
	 * 
	 * 
	 * 
	 * 
	 * 
	 */						
	protected List<Tuple> ObjectStorageInfo() {
		
		long start = System.currentTimeMillis();
		
			List<Tuple> data = new ArrayList<Tuple>();
			
			boolean kbfs1_enabled=PropertiesFactory.getInstance("kbee").getProperties().getProperty("kbfs1.enabled", "yes").toLowerCase().trim().equals("yes");
			boolean kbfs2_enabled=PropertiesFactory.getInstance("kbee").getProperties().getProperty("kbfs2.enabled", "yes").toLowerCase().trim().equals("yes");
			boolean odilon_enabled=PropertiesFactory.getInstance("kbee").getProperties().getProperty("odilon.enabled", "yes").toLowerCase().trim().equals("yes");
			            
			
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
                
            } catch (Exception e) {
                data.add(new Tuple("File System",  e.getClass().getName()));
            }

            // Minio ---------------------------------------------------------------------
            //
			try {

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
							for (Entry<Integer, FileServerMinio> entry: ((KbeeShardedMinioFileServer) fsv2).getShards().entrySet()) {
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
							for (Entry<Integer, FileServerMinio> entry: ((KbeeShardedMinioFileServer) fsv2).getShards().entrySet()) {
								String fsid = entry.getValue().getFSId();
								data.add(new Tuple( "Minio"+ String.valueOf(entry.getKey().intValue())+
										" endpoint", 
										"<a href=\""+ entry.getValue().getEndPoint() +"\" target=\"_blank\">" + 
												entry.getValue().getEndPoint() +"</a>" +
										 " <span class=\"ago\">(prob: "+ NumberFormatter.formatNumber(entry.getValue().getProbability()) + ")</span>"
										
										
										
										));
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

                    FileServerOdilon fsv2=ServiceLocator.getService(FileServerOdilon.class);
                    
                    if (fsv2 instanceof KbeeShardedOdilonFileServer) {
                        String sm_pg=fsv2.ping();
                        if (sm_pg==null)
                            sm_pg="err";
                        boolean sm_isok = sm_pg.toLowerCase().equals("ok");
                        String sm_s="<span class= \" "+ (sm_isok?"success":"danger") +"\" />"+sm_pg+"</span>";
                        data.add(new Tuple( "Odilon Shard Manager (ping)", sm_s));
                        try {
                            for (Entry<Integer, FileServerOdilon> entry: ((KbeeShardedOdilonFileServer) fsv2).getShards().entrySet()) {
                                String p = entry.getValue().ping();
                                if (p==null)
                                    p="err";
                                boolean p_isok = p.toLowerCase().equals("ok");
                                String px="<span class= \" "+ (p_isok?"success":"danger") +"\" />"+p+"</span>";
                                data.add(new Tuple( "Odilon"+ String.valueOf(entry.getKey().intValue())+ " (ping)", px));
                            }
                        } catch (Exception e) {
                            logger.error(e);
                            data.add(new Tuple( "Odilon",  e.getClass().getName()));
                        }
                        
                        try {
                            for (Entry<Integer, FileServerOdilon> entry: ((KbeeShardedOdilonFileServer) fsv2).getShards().entrySet()) {
                                String fsid = entry.getValue().getFSId();
                                data.add(new Tuple( "Odilon"+ String.valueOf(entry.getKey().intValue())+
                                        " endpoint", 
                                        "<a href=\""+ entry.getValue().getEndPoint() +"\" target=\"_blank\">" + 
                                                entry.getValue().getEndPoint() +"</a>" +
                                         " <span class=\"ago\">(prob: "+ NumberFormatter.formatNumber(entry.getValue().getProbability()) + ")</span>"
                                        ));
                                
                                data.add(new Tuple( "Odilon"+ String.valueOf(entry.getKey().intValue())+" FSId", fsid));
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
                        data.add(new Tuple( "Odilon Ping", sm_s));
                    }
                }
                else
                    data.add(new Tuple("Odilon", "disabled"));
                
            } catch (Exception e) {
                    logger.error(e);
                    data.add(new Tuple( "Error",  e.getClass().getName()));
            }

			
            // S3 ---------------------------------------------------------------------
            //
			
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
						
						data.add(new Tuple( "S3 Environment", fss3.getEnvironment()));
						data.add(new Tuple( "S3 Access Key", fss3.getAccessKey()));
						//data.add(new Tuple( "S3 Secret Key", fss3.getSecretKey()));
					
					} catch (Exception e) {
						logger.error(e);
						data.add(new Tuple( "Error",  e.getClass().getName()  + " " + e.getMessage()));
					}
				}
				else {
					data.add(new Tuple( "Amazon S3 Status ", "Disabled"));
				}
			}
			else {
				data.add(new Tuple( "Amazon S3 Status ", "Not installed"));
			}
			
		 
			
			long end = System.currentTimeMillis();
			if (logger.isDebugEnabled())
			data.add(new Tuple("Render time ", String.valueOf(end-start)+" <span class=\"ago\">ms</span>"));
			logger.debug("File Service time " + String.valueOf(end-start)+" ms");
			
			return data;
	}
	
	
	/***	 
	 * 
	 * 
	 
	private List<Tuple> DBloadAverage() {

		List<Tuple> data = new ArrayList<Tuple>();
		try {

			if (isLinux()) {
				StringBuilder str = new StringBuilder(); 
	  			for (String entry:  getContentDao().getDBServerLoadAvg()) { 
					if (str.length()>0)
						str.append("<span class=\"separator\">|</span>");
					str.append(entry);
				}
	  			data.add(new Tuple("DB CPU Load Average (1m 5m 15m)", str.toString()));
			}
			else {
				data.add(new Tuple("DB CPU Load Average (1m 5m 15m)", "<span class=\"warning\">Not supported in Windows (0.00%)</span>"));
			}
			
		} catch (Throwable e) {
			logger.error(e);
			data.add(new Tuple(e.getClass().getName(), e.getMessage()));
		}
		return data;
	}
	*/
	
	
	
	private String pingSolR() {
		
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

	
	/**
	private List<Tuple> propertiesJavaInfo() {
		List<Tuple> data = systemEnv();
		Collections.sort(data, new Comparator<Tuple>() {
			@Override
			public int compare(Tuple o1,	Tuple o2) {
				try {
					return o1.label.toLowerCase().compareTo(o2.label.toLowerCase());
				} catch (Exception e) {
					return 0;
				}
				}
			}); 
		return data;
	}

	
	private List<Tuple> systemEnv() {
		return dumpVars(System.getenv());
	}
**/

	/***
	 * 
	 * 
	


	private List<Tuple> dumpVars(Map<String, ?> m) {
		List<Tuple> list = new ArrayList<Tuple>(m.size());
		List<String> keys = new ArrayList<String>(m.keySet());
		  for (String k : keys) {
			  list.add(new Tuple(k,m.get(k).toString()));
		  }
		return list;
	}
		
 */		
	
	
	
	
	
}
