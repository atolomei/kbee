package com.novamens.content.web.admin.markup;


import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wicket.model.Model;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.novamens.datetime.DateTimeService;
import com.novamens.dom.KBFSStorageType;
import com.novamens.kbee.wicket.markup.html.areainfo.AreaInfoPanel;
import com.novamens.kbee.wicket.markup.html.areainfo.GridInfoPanel;
import com.novamens.metrics.SystemMetricsService;
import com.novamens.metrics.domain.DomainMetricsService;
import com.novamens.service.ApplicationServerService;
import com.novamens.service.BrandingService;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.util.BCElement;

import kbee.util.NumberFormatter;
import kbee.util.PropertiesFactory;
import kbee.util.Tuple;


/**
 * 
 */
public class SystemInfoServerPanel extends AbstractSystemInfoPanel {
				
	private static final long serialVersionUID = 1L;

	static private Logger logger = LogManager.getLogger(SystemInfoServerPanel.class.getName());

	public SystemInfoServerPanel() {
		this("info-panel");
	}
	
	public SystemInfoServerPanel(String id) {
		super(id);
	}
	
	
	/**
	 * 
	 */
	@Override
	public void onInitialize() {
		super.onInitialize();
  
		
		AreaInfoPanel area = new AreaInfoPanel("info");
		add(area);
		
		area.setSections(AreaInfoPanel.ONE_SECTION);
		area.setCss("col-lg-12");
		
		area.addPanel(new GridInfoPanel("element", serverInfo(), new Model<String>("Server"), true));
		area.addPanel(new GridInfoPanel("element", serverDirInfo(), new Model<String>("Directories"), true));
		area.addPanel(new GridInfoPanel("element", infrastructureInfo(), new Model<String>("Hardware"), true));
		area.addPanel(new GridInfoPanel("element", JVMActivityInfo(), new Model<String>("JVM"), true));
		area.addPanel(new GridInfoPanel("element", applicationInfo(),  new Model<String>("Application"), true));
		area.addPanel(new GridInfoPanel("element", emailInfo(),  new Model<String>("Email"), true));
		
		
		
	
	}

	/**
	 * 
	 * @return
	 */
	/*
	protected List<Tuple> dataManagementInfo() {
		
		long start = System.currentTimeMillis();
		
		List<Tuple> data = new ArrayList<Tuple>();
		
		data.add(new Tuple("Database. Version", getContentDao().getDatabaseVersion()));
		data.add(new Tuple("Database. URL", PropertiesFactory.getInstance("kbee").getProperties().getProperty("jdbc.url", ""))); 
		data.add(new Tuple("Database. Size", NumberUtil.formatFileSize(getContentDao().getDatabaseSize(), getSessionUser().getLocale(), "ago")));

 		long solrsize=FileUtils.sizeOf(new File("solr"+File.separator+"data"));
		data.add(new Tuple("Solr directory Data", NumberUtil.formatFileSize(solrsize, getSessionUser().getLocale(), "ago")));

		long solrauditsize=FileUtils.sizeOf(new File("solr"+File.separator+"auditdata"));
		data.add(new Tuple("Solr directory Audit", NumberUtil.formatFileSize(solrauditsize, getSessionUser().getLocale(), "ago")));

		long end = System.currentTimeMillis();
						if (logger.isDebugEnabled())
		data.add(new Tuple("Render time ", String.valueOf(end-start)+" <span class=\"ago\">ms</span>"));
		logger.debug("Render time " + String.valueOf(end-start)+" ms");
		
		return data;
	}
	*/

	private List<Tuple> serverInfo() {

		long start = System.currentTimeMillis();
		
		List<Tuple> data = new ArrayList<Tuple>();
		
		try {
				data.add(new Tuple("Server", getServerHost() ));
				
				data.add(new Tuple("Available processors", String.valueOf(Runtime.getRuntime().availableProcessors())+  " cores" ));	 
				data.add(new Tuple("Free memory", String.format("%6.4f", (double) Runtime.getRuntime().freeMemory()/ GB )+  " GB"));
				
				long maxMemory = Runtime.getRuntime().maxMemory();
				
				data.add(new Tuple("Maximum memory", (maxMemory == Long.MAX_VALUE ? "no limit" : String.format("%6.4f", (double) maxMemory / GB ))+  " GB"));
				data.add(new Tuple("Total memory", String.format("%6.4f",(double) Runtime.getRuntime().totalMemory() / GB )+  " GB"));
				
			    String strOSName = System.getProperty("os.name");
			    
			    if (strOSName != null) 
			        	data.add(new Tuple("OS", strOSName));
			    
			    String strOSVersion = System.getProperty("os.version");
			    
			    if (strOSVersion != null) 
		       	data.add(new Tuple("OS Version", strOSVersion));
			    
			    if (System.getenv()!=null) {
			       	data.add(new Tuple("Username", System.getenv().get("USERNAME")));
			       	data.add(new Tuple("Profile", System.getenv().get("USERPROFILE")));
			    }
		
			   	data.add(new Tuple("user.country", System.getProperty("user.country")));
			   	data.add(new Tuple("user.dir", System.getProperty("user.dir")));
			   	data.add(new Tuple("user.home", System.getProperty("user.home")));
			   	data.add(new Tuple("user.language", System.getProperty("user.language")));
			    
			    String strJavaVersion = System.getProperty("java.specification.version");
			    data.add(new Tuple("JVM Spec", strJavaVersion));
		   	
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
					e1.printStackTrace();
				}
			    
				if (strOSName!=null & strOSName.toLowerCase().startsWith("windows")) {
					File[] roots = File.listRoots();
					int fsn=0;
					for (File root : roots) {
						double totalspace = (double) root.getTotalSpace()/GB;
						double freespace = (double) root.getFreeSpace()/GB;
						double usablespace = (double) root.getUsableSpace()/GB;			
						data.add(new Tuple("FS root "+ String.valueOf(++fsn), root.getAbsolutePath() + " (Total: " + String.format("%-7.2f", totalspace)+  " GB, Free: "  + String.format("%7.2f",freespace)+  " GB, Usable: " + String.format("%7.2f",usablespace)+  " GB)"));
				      }
				}
				else if (strOSName!=null & strOSName.toLowerCase().startsWith("linux")) {
					int fsn=0;
					NumberFormat nf = NumberFormat.getNumberInstance();
					for (Path root : FileSystems.getDefault().getRootDirectories()) {
							try {
									FileStore store = Files.getFileStore(root);
									String usable  = nf.format((double) (store.getUsableSpace())/ GB);
									long t=store.getTotalSpace();
									String total   = nf.format((double)t/GB);
									data.add(new Tuple("FS root "+ String.valueOf(++fsn), root.toString() + " (Total: " + total + " GB, Free: " + usable +  " GB)"));
							} catch (IOException e1) {
								logger.error(e1);
							}	
					}
				}
		
				Collections.sort(data, 
						new Comparator<Tuple>() {
					@Override
					public int compare(Tuple a,Tuple b) {
						try {
							return a.getLabel().compareToIgnoreCase(b.getLabel());
						} catch (Exception e) {
							return 0;
						}
						}});
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
	
	
	
	
	
	
	private List<Tuple> serverDirInfo() {

		long start = System.currentTimeMillis();
		List<Tuple> data = new ArrayList<Tuple>();
		
		try {
			
			
			ApplicationServerService service = ServiceLocator.getService(ApplicationServerService.class);
		    
			data.add(new Tuple("home", service.getHomeDir() ));
			data.add(new Tuple("work", service.getWorkDirAbsolutePath() ));
			data.add(new Tuple("drive", service.getDriveDir() ));
			 
			
			
			Collections.sort(data, 
					new Comparator<Tuple>() {
				@Override
				public int compare(Tuple a,Tuple b) {
					try {
						return a.getLabel().compareToIgnoreCase(b.getLabel());
					} catch (Exception e) {
						return 0;
					}
					}});
			
			} catch (Exception e) {
				data.add(new Tuple(e.getClass().getName(), e.getMessage()));
				logger.error(e);
	
			} finally {
				long end=System.currentTimeMillis();
				if (logger.isDebugEnabled())
				data.add(new Tuple("Render time ", String.valueOf(end-start)+" <span class=\"ago\">ms</span>"));
			}

		return data;
	}
	
	

	protected BCElement getPageBCElement() {
		return new BCElement(new Model<String>("Server"));
	}
	
	
	protected List<Tuple> emailInfo() {
		
		long start = System.currentTimeMillis();
		
		Properties properties = PropertiesFactory.getInstance("kbee").getProperties();
			
		List<Tuple> data = new ArrayList<Tuple>();
							
		data.add(new Tuple( "Mode",  properties.getProperty("email", null)));
		data.add(new Tuple( "Server",  properties.getProperty("email.server", null)));
		data.add(new Tuple( "Port",  properties.getProperty("email.port", null)));
		data.add(new Tuple( "Username",  properties.getProperty("email.username", null)));
		data.add(new Tuple( "Password",  properties.getProperty("email.password", null)));
		data.add(new Tuple( "Auth",  properties.getProperty("email.auth", null)));
		data.add(new Tuple( "Email address",  properties.getProperty("email.contact.address", null)));
		data.add(new Tuple( "Subject",  properties.getProperty("email.contact.subject", null)));
		
		if (logger.isDebugEnabled()) {
			long end=System.currentTimeMillis();
			if (logger.isDebugEnabled())
			data.add(new Tuple("Render time ", String.valueOf(end-start)+"<span class=\"ago\"> ms</span>"));
		}

		return data;
}


	
	
	private List<Tuple> applicationInfo() {
		
		long start = System.currentTimeMillis();
		List<Tuple> data = new ArrayList<Tuple>();
		
		try {

			long jvmStartTime = ManagementFactory.getRuntimeMXBean().getStartTime();
			
			data.add(new Tuple( "Server Id", PropertiesFactory.getInstance("kbee").getProperties().getProperty("server.id", "")));
			
			LocalDateTime d=ServiceLocator.getService(DateTimeService.class).millsToLocalDateTime(jvmStartTime);
			data.add(new Tuple( "Started", ServiceLocator.getService(DateTimeService.class).timeElapsed(d, getSessionUser().getLocale(), "atright ago")));
	
			data.add(new Tuple( "Server", PropertiesFactory.getInstance("kbee").getProperties().getProperty("server", "").trim()));
			
			
			data.add(new Tuple( "Port",
					ServiceLocator.getService(ApplicationServerService.class).getJettyPort()
					));
								
			data.add(new Tuple( "Vanity server mask", PropertiesFactory.getInstance("kbee").getProperties().getProperty("vanity-server", "")));
			data.add(new Tuple( "Vanity Port", PropertiesFactory.getInstance("kbee").getProperties().getProperty("vanity-port", "")));
			
			
			data.add(new Tuple( "Brand", ServiceLocator.getService(BrandingService.class).getProductKey()));
			data.add(new Tuple( "Application", PropertiesFactory.getInstance("kbee").getProperties().getProperty("com.novamens.kbee.application", "")));

			data.add(new Tuple( "Portal", PropertiesFactory.getInstance("kbee").getProperties().getProperty("license.portal", "")));
			
			SecurityService service = ServiceLocator.getService(SecurityService.class);
			data.add(new Tuple( "Total Security Tokens", String.valueOf(service.getTokenDBSize())));
			
			Collections.sort(data, new Comparator<Tuple>() {
				@Override
				public int compare(Tuple o1,	Tuple o2) {
					return o1.label.toLowerCase().compareTo(o2.label.toLowerCase());
					}
				}); 

		} catch (Exception e) {
			data.add(new Tuple(e.getClass().getName(), e.getMessage()));
			logger.error(e);

		} finally {
			long end=System.currentTimeMillis();
			if (logger.isDebugEnabled())
			data.add(new Tuple("Render time ", String.valueOf(end-start)+" <span class=\"ago\">ms</span>"));
		}

		return data;
	}
	
	
	/***
	 * 
	 * 
	 */
	@SuppressWarnings("rawtypes")
	private List<Tuple> JVMActivityInfo() {
		
		//long start = System.currentTimeMillis();
		
		 List<Tuple> data = new ArrayList<Tuple>();
		 SystemMetricsService metrics_service = ServiceLocator.getService(SystemMetricsService.class);
		 GarbageCollectorMetricSet setg = metrics_service.getJVMGarbageCollectorMetricSet();
		 for (Entry<String, Metric> entry: setg.getMetrics().entrySet())
				data.add(new Tuple(entry.getKey(), ((Gauge) entry.getValue()).getValue().toString()));
		return data;
	}
	
	
	/***
	 * 
	 * 
	 * @return
	 */
	private List<Tuple> systemActivityInfo() {

		long start = System.currentTimeMillis();
		
		List<Tuple> data = new ArrayList<Tuple>();
		
		SystemMetricsService metrics_service = ServiceLocator.getService(SystemMetricsService.class);

		DomainMetricsService doms = ServiceLocator.getService(DomainMetricsService.class);
		
		try {																
			data.add(new Tuple( "Total Contents (all versions)", 				NumberFormatter.formatNumber(doms.getTotalContents(), getSessionUser().getLocale())));
			data.add(new Tuple( "Total Resources", 					   			NumberFormatter.formatNumber(doms.getTotalResources(), getSessionUser().getLocale())));
			data.add(new Tuple( "Total Users", 							   		NumberFormatter.formatNumber(doms.getTotalUsers(), getSessionUser().getLocale())));
			data.add(new Tuple( "Total Resources File System",		 	    	NumberFormatter.formatNumber(doms.getTotalResources(KBFSStorageType.KBFS1), getSessionUser().getLocale())));
			data.add(new Tuple( "Total Resources Minio", 				    	NumberFormatter.formatNumber(doms.getTotalResources(KBFSStorageType.Minio), getSessionUser().getLocale())));
            data.add(new Tuple( "Total Resources Odilon",                       NumberFormatter.formatNumber(doms.getTotalResources(KBFSStorageType.Odilon), getSessionUser().getLocale())));
	         
			data.add(new Tuple( "Total Resources Minio Archive", 		    	NumberFormatter.formatNumber(doms.getTotalResources(KBFSStorageType.MinioArchive), getSessionUser().getLocale())));
			data.add(new Tuple( "Total Resources Gateway",		 				NumberFormatter.formatNumber(doms.getTotalResources(KBFSStorageType.External), getSessionUser().getLocale())));

			data.add(new Tuple( "Total Hard Disk Stored (KBFS1/2/Archive)",		NumberFormatter.formatFileSize(doms.getTotalStoredHardDisk(), getSessionUser().getLocale(), "ago")));
			data.add(new Tuple( "Total Hard Disk Gateway",		 				NumberFormatter.formatFileSize(doms.getTotalHardDisk(KBFSStorageType.External), getSessionUser().getLocale(), "ago")));

		} catch (Exception e) {
			logger.error(e);
		}
		
		String login=String.format("%12.2f  <span class=\"separator\">|</span>   %12.2f  <span class=\"separator\">|</span>   %12.2f", 
				  metrics_service.getMeterLogin().getOneMinuteRate() * 60,
				  metrics_service.getMeterLogin().getFiveMinuteRate() * 60,
				  metrics_service.getMeterLogin().getFifteenMinuteRate() * 60);	
											
		String wp=String.format("%12.2f  <span class=\"separator\">|</span>   %12.2f   <span class=\"separator\">|</span>   %12.2f", 
				  metrics_service.getMeterWebPages().getOneMinuteRate() * 60,
				  metrics_service.getMeterWebPages().getFiveMinuteRate() * 60,
				  metrics_service.getMeterWebPages().getFifteenMinuteRate() * 60);	
										
		String em=String.format("%12.2f  <span class=\"separator\">|</span>   %12.2f   <span class=\"separator\">|</span>   %12.2f", 
				  metrics_service.getMeterEmails().getOneMinuteRate() * 60,
				  metrics_service.getMeterEmails().getFiveMinuteRate() * 60,
				  metrics_service.getMeterEmails().getFifteenMinuteRate() * 60);	

		data.add(new Tuple("Active users", String.valueOf(metrics_service.getCounterUsersLogged().getCount())));

		data.add(new Tuple("Login/min (1m 5m 15m)", login));
		data.add(new Tuple("Emails/min (1m 5m 15m)", em));
		data.add(new Tuple("Webpages/min (1m 5m 15m)", wp));

		if (logger.isDebugEnabled()) {
			long end=System.currentTimeMillis();
			data.add(new Tuple("Render time ", String.valueOf(end-start)+" <span class=\"ago\">ms</span>"));

		}

		
  		return data;
	}

	/***
	 * 
	 * 
	 */
	
//	private List<Tuple> schedulerActivityInfo() {
//	
//		long start = System.currentTimeMillis();
//		
//		List<Tuple> data = new ArrayList<Tuple>();
//			
//		SchedulerService service = ServiceLocator.getService(SchedulerService.class);
//		
//		try {
//			data.add(new Tuple( "Scheduler Std Queue Size (in db).",  String.valueOf(service.getQueueSize())));
//
//		} catch (SchedulerException e) {
//			logger.error(" {} | {} | {} | {} ", (getSessionUser()!=null?getSessionUser().getUserName():""), e.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName(), e.getMessage());
//			data.add(new Tuple( "Scheduler Std Queue Size.", e.getClass().getName()));
//		}
//							
//		try {
//			data.add(new Tuple("Scheduler Engine status. ",  service.getStatus()));
//			
//		} catch (SchedulerException e) {
//			data.add(new Tuple("Scheduler Engine status. ",  e.getClass().getName()));
//			logger.error(" {} | {} | {} | {} ", (getSessionUser()!=null?getSessionUser().getUserName():""), e.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName(), e.getMessage());
//		}
//		
//		
//		data.add(new Tuple("Total Requests in Batches. ", String.valueOf(service.getTotalInBatches())));
//		
//		
//		
//		String v1a = String.format("%6.2f", service.getOneMinuteInputRateHp()).trim();
//		String v1b = String.format("%6.2f", service.getOneMinuteThroughPutHP()).trim();
//		
//		
//		String v2a = String.format("%6.2f", service.getFiveMinuteInputRateHp()).trim();
//		String v2b = String.format("%6.2f",service.getFiveMinuteThroughPutHP()).trim();
//		
//		String v3a = String.format("%6.2f",service.getFifteenMinuteInputRateHp()).trim();
//		String v3b = String.format("%6.2f",service.getFifteenMinuteThroughPutHP()).trim();
//		
//		String rate_hp = 		v1a + "<span class=\"internal-separator\">/</span>" + v1b + "<span class=\"separator\">|</span>" +
//								v2a + "<span class=\"internal-separator\">/</span>" + v2b + "<span class=\"separator\">|</span>" +
//								v3a + "<span class=\"internal-separator\">/</span>" + v3b;
//				
//
//		
//		String vl1a = String.format("%6.2f", service.getOneMinuteInputRateLp()).trim();
//		String vl1b = String.format("%6.2f", service.getOneMinuteThroughPutLP()).trim();
//		
//		
//		String vl2a = String.format("%6.2f", service.getFiveMinuteInputRateLp()).trim();
//		String vl2b = String.format("%6.2f",service.getFiveMinuteThroughPutLP()).trim();
//		
//		String vl3a = String.format("%6.2f",service.getFifteenMinuteInputRateLp()).trim();
//		String vl3b = String.format("%6.2f",service.getFifteenMinuteThroughPutLP()).trim();
//		
//		String rate_lp = 		vl1a + "<span class=\"internal-separator\">/</span>" + vl1b + "<span class=\"separator\">|</span>" +
//								vl2a + "<span class=\"internal-separator\">/</span>" + vl2b + "<span class=\"separator\">|</span>" +
//								vl3a + "<span class=\"internal-separator\">/</span>" + vl3b;
//
//		data.add(new Tuple( "Scheduler HP I/O (1m 5m 15m). ",  rate_hp +   " <span class=\"atright ago\">reqs/seg<span>"));
//		data.add(new Tuple( "Scheduler LP I/O (1m 5m 15m). ",  rate_lp +  " <span class=\"atright ago\">reqs/seg<span>"));
// 		
//		if (logger.isDebugEnabled()) {
//			long end=System.currentTimeMillis();
//					if (logger.isDebugEnabled())	data.add(new Tuple("Render time ", String.valueOf(end-start)+" <span class=\"ago\">ms</span>"));
//
//		}
//
//		
//		return data;
//		
//	}
	
	/***
	 * 
	 * 
	 */
	protected List<Tuple> infrastructureInfo() {

		long start = System.currentTimeMillis();
		
		List<Tuple> data = new ArrayList<Tuple>();

		try {
			data.add(new Tuple("Available processors", String.valueOf(Runtime.getRuntime().availableProcessors())+  " cores" ));	 
			
			OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
		 	String la=os.getSystemLoadAverage()>0?String.valueOf(os.getSystemLoadAverage()):"N/A";
		 	String spercent="";
		 	int processors = Runtime.getRuntime().availableProcessors();
			if (processors>0 && os.getSystemLoadAverage()>0) {
				Double percent = new Double (Double.valueOf(os.getSystemLoadAverage()) / Double.valueOf(processors));
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

		}
		catch (Exception e) {
			logger.error(e);
		}
		
		if (logger.isDebugEnabled()) {
			long end=System.currentTimeMillis();
			if (logger.isDebugEnabled())
			data.add(new Tuple("Render time ", String.valueOf(end-start)+" <span class=\"ago\">ms</span>"));
		}

		return data;
	}



	
}
