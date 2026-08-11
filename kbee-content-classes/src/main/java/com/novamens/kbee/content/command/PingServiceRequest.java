package com.novamens.kbee.content.command;




import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.dom.Domain;
import com.novamens.email.EmailData;
import com.novamens.email.EmailService;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.event.EvictCacheServiceEvent;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.vault.VaultService;
import com.novamens.kbfs.FileServerMinio;
import com.novamens.kbfs.FileServerOdilon;
import com.novamens.kbfs.FileServerS3;
import com.novamens.kbfs.v1.FileServerV1;
import com.novamens.metrics.SystemMetricsService;
import com.novamens.scheduler.AbstractCronJobRequest;
import com.novamens.scheduler.SchedulerException;
import com.novamens.scheduler.SchedulerService;
import com.novamens.service.ApplicationServerService;
import com.novamens.service.BrandingService;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.solr.indexer.query.SolrQuery;

import kbee.util.PropertiesFactory;


/**
 * <p>
 * 
 *  ping.enabled 		= yes | no (execute the ping commmand) <br />
 *  ping.notify 		= yes | no (notify if errrors by email) <br />
 *  ping.email 			= email to send Ping error  <br />
 *  ping.cpu.threshold 	= 5
 *  ping.ok				= OK
 *  ping.time.notif		= 
 *  
 *  Only 1 email every  FIVE_MINUTES is sent, regardless of the number of Pings executed in the lapse. <br />
 *  
 *  
 *  
 *  
 *  <br />
 *  {@code delete from kb_cronjob where name like 'Ping%';}
 *  <br />
 *  <br />
	{@code insert into  kb_cronjob (id, lastmodifieduser, name, description, cronexpression, clazz) values ((select nextval('objectid_sequence')), (select id from users where username='root@kbee'), 'PingServiceRequest', 'Ping. System Parameters: ping.enabled = yes/no|  ping.notify = yes/no | ping.email = email to send Ping error.', '15 * * * * *', 'com.novamens.kbee.content.command.PingServiceRequest');
	}



 *  </p>  
 */

public class PingServiceRequest extends AbstractCronJobRequest {

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(PingServiceRequest.class.getName());
	
	
	static private final Properties props = PropertiesFactory.getInstance("kbee").getProperties();
	private static final String _OK  = PropertiesFactory.getInstance("kbee").getProperties().getProperty("ping.ok", "ok");

	static private final long TWO_HOURS = 1000 * 60 * 60 * 2;
	static private final long FIVE_MINUTES = 1000 * 60 * 5;
	
	 
	private double CPU_THRESHOLD_X = 5.0;

	private OffsetDateTime lastVaultReconnect = OffsetDateTime.now();
	
	private boolean is_notify = true;
	
	
	public void setNotify(boolean b) {
		this.is_notify = b;
	}
	
	public boolean isNotify() {
		return this.is_notify; 
	}
	

	/**
	 *  com.novamens.kbee.content.command.PingServiceRequest
	 */
	private static final long serialVersionUID = 1L;
	
	public PingServiceRequest() {
		super();
		setName(this.getClass().getName());
		setDescription("Ping. System Parameters: ping.enabled |  ping.notify = yes | no | ping.email = email to send Ping error ");
	}
	
	protected String executePing() {
		List<String> results = new ArrayList<String>();
		try {
			final int defaultThreshold = Runtime.getRuntime().availableProcessors() + 2;
			CPU_THRESHOLD_X =  Double.valueOf(getContentDao().findSystemParameterValueByKey("ping.cpu.threshold", String.valueOf(defaultThreshold))).doubleValue();
			
		} catch (Exception e) {
			logger.error(e);
			CPU_THRESHOLD_X =  5;
		}
		results.add(pingDataBase());
		results.add(pingCPULoad());
		results.add(pingScheduler());
		results.add(pingSolR());

		String vault_url = PropertiesFactory.getInstance("kbee").getProperties().getProperty("vault.url", null);
		
		if (vault_url!=null) {
			String s_v;
			s_v = pingVault();
			if (s_v!=null) {
				tryReconnectVault();
				s_v = pingVault();
			}
			if (s_v!=null)
				results.add(s_v);
			}
		
		results.add(pingOutOfMemoryFlag());
		
		boolean api_enabled=props.getProperty("api.enabled", "yes").toLowerCase().trim().equals("yes");

		if (api_enabled)
			results.add(pingAPI());
		
		boolean kbfs1_enabled=props.getProperty("kbfs1.enabled", "yes").toLowerCase().trim().equals("yes");
		boolean kbfs2_enabled=props.getProperty("kbfs2.enabled", "yes").toLowerCase().trim().equals("yes");
		boolean odilon_enabled=props.getProperty("odilon.enabled", "no").toLowerCase().trim().equals("yes");
		
		
		FileServerS3 fss3=ServiceLocator.getService(FileServerS3.class);
		
		boolean s3_enabled = (fss3!=null && fss3.isEnabled());  
		
		if (kbfs1_enabled)
			results.add(pingKBFS1());
		
		if (kbfs2_enabled)
			results.add(pingKBFSMinio());

		if (odilon_enabled)
			results.add(pingOdilon());

		
		if (s3_enabled)
			results.add(pingS3());
		
		StringBuilder rs = new StringBuilder();
		
		for (String str: results) {
			if (str!=null) {
				if (rs.length()>0)
					rs.append(" | ");
				rs.append(str);
			}
		}
		String result = rs.toString();
		String r = result!=null && result.length()>0?result: _OK;
		
		pingResult = r;
		return pingResult;
	}
	
	
	
	String pingResult;
	
	public String pingResult() {
		return pingResult;
	}
	
	
	@Override
	public void execute() {
		try {
			
			pingResult = null;
			
			boolean ping_enabled = getContentDao().findSystemParameterValueByKey("ping.enabled","yes").toLowerCase().trim().equals("yes");
				
				if (!ping_enabled) 
					return;
			
				String r=executePing();
				
				if (r.equals(_OK) && this.isNotify()) {
					try {
						Thread.sleep(700);
						r =  executePing();
					} catch (InterruptedException e) {
						logger.error(e);
					}
				}
				
				
				logger.debug(r);
				
				if (!r.equals(_OK)) {
					
					boolean notify_ping = getContentDao().findSystemParameterValueByKey("ping.notify","yes").toLowerCase().trim().equals("yes");
					
					if (this.isNotify() &&  notify_ping) {
						
						String email=getContentDao().findSystemParameterValueByKey("ping.email","null").toLowerCase().trim();
						
							String last_notify=PropertiesFactory.getInstance("kbee").getProperties().getProperty("ping.time.notify", "0");
							
							Long last_notify_long;
							try {
								last_notify_long = Long.valueOf(last_notify);
							} catch (Exception e) {
								last_notify_long = Long.valueOf(0);
							}
							
							if ((System.currentTimeMillis()-last_notify_long.longValue())<FIVE_MINUTES) {
								logger.debug("Email was sent less than 5 min. ago");
							}
							else {
								
								String noreply= ServiceLocator.getService(BrandingService.class).getNoReplyEmailAddress();
								
								if (!email.equals("null")) {
									EmailData emaildata = new EmailData(noreply, email, "Ping Error - " + 
											ServiceLocator.getService(ApplicationServerService.class).getServerHost(),  r, "ping-error");
									
									ServiceLocator.getService(EmailService.class).send(emaildata, getContentDao().findDomainByName("kbee"));
									PropertiesFactory.getInstance("kbee").getProperties().setProperty("ping.time.notify", String.valueOf(System.currentTimeMillis()));
								} else {
									logger.error("Ping Email is null. Please complete System Parameter: ping.email");
								}
							}
					}
 				}
				
		} catch (Exception e) {
			logger.error(e);
  		} finally {
				
	 	}
	}
	
				

	protected KbeeUser getUser() {
		return (KbeeUser)ServiceLocator.getService(SecurityService.class).getSessionUser();
	}

	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	



private String pingOutOfMemoryFlag() {
	if  (ServiceLocator.getService(SystemMetricsService.class).isOutOfMemoryFlag()) {
		long time = ServiceLocator.getService(SystemMetricsService.class).timeOutOfMemoryFlag();
		if ((System.currentTimeMillis()-time)< TWO_HOURS) {
 			return "OutOfMemory " + String.valueOf((System.currentTimeMillis()-time)/1000.0)+ " secs ago";
		}
 	}
	return null;
}


private String pingSolR() {

	 	try {
			SolrQuery q = new SolrQuery(getQueryIndex()) {
				private static final long serialVersionUID = 1L;
				@Override
				public String getStatement() {
					return "type:datasetmember";
				}
				@Override
				public String getSolrStatement() {
					return "type:datasetmember";
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



private Index getQueryIndex() {
	return getDomainKbee().getService(JavaIndexerService.class).getIndex();
}

	
private Domain getDomainKbee() {
	return getContentDao().findDomainByName ("kbee");
}



private String pingKBFS1() {
	try {
		String s=ServiceLocator.getService(FileServerV1.class).ping();
		if (s!=null && s.toLowerCase().equals("ok"))
			return null;
		return s;
	} catch (Exception e) {
		logger.error(e);
		return e.getClass().getName();
	}
}

private String pingKBFSMinio() {
	try {
		String s=ServiceLocator.getService(FileServerMinio.class).ping();
		if (s!=null && s.toLowerCase().equals("ok"))
			return null;
		return s;
	} catch (Exception e) {
		logger.error(e);
		return e.getClass().getName();
	}
}


private String pingS3() {
	try {
		String s=ServiceLocator.getService(FileServerS3.class).ping();
		if (s!=null && s.toLowerCase().equals("ok"))
			return null;
		return s;
	} catch (Exception e) {
		logger.error(e);
		return e.getClass().getName() + " " + e.getMessage();
	}
}

@SuppressWarnings("unused")
private String pingApplication() {
	return null;
}

	

private String pingScheduler() {
	try {
		String s=ServiceLocator.getService(SchedulerService.class).getStatus();
		if (s!=null && s.toLowerCase().equals("ok"))
			return null;
		return s;
	} catch (SchedulerException e) {
		logger.error(e);
		return e.getClass().getName() + " | " + e.getMessage();
	}
}




private void tryReconnectVault() {
	
	String vault_url = PropertiesFactory.getInstance("kbee").getProperties().getProperty("vault.url", null);
	
	if (vault_url==null)
		return;
	
	try {
		if (lastVaultReconnect!=null && OffsetDateTime.now().isAfter(lastVaultReconnect.plusMinutes(5))) {
			ServiceLocator.getService(VaultService.class).onEvent(new EvictCacheServiceEvent());
			lastVaultReconnect =OffsetDateTime.now();
			Thread.sleep(1000);
		}
	} catch (Exception e) {
		logger.error(e);
	}
}


private String pingVault() {
 	try {
	
		String vault_url = PropertiesFactory.getInstance("kbee").getProperties().getProperty("vault.url", null);
		
		if (vault_url==null) {
			return null;
		}
 		
 		String vrs=ServiceLocator.getService(VaultService.class).ping();
 		
 		if (vrs!=null && vrs.equals("ok"))
 			return null;
 		
		return vrs;
		
	
	} catch (Exception e) {
		
		logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName() + " | " + e.getMessage());
		return "Vault. "+e.getClass().getSimpleName()+ " | " + e.getMessage();
	}
	
}

					

private String pingAPI() {
	try {
		String s=getContentDao().pingAPI();
		if (s!=null && s.toLowerCase().equals("ok"))
			return null;
		return s;
	} catch (Exception e) {
		logger.error(e);
		return e.getClass().getName() + " | " + e.getMessage();
	}
}

private String pingOdilon() {
	try {
		String s=ServiceLocator.getService(FileServerOdilon.class).ping();
		if (s!=null && s.toLowerCase().equals("ok"))
			return null;
		return s;
	} catch (Exception e) {
		logger.error(e);
		return e.getClass().getName();
	}
}

/**
 * @return
 */
private String pingCPULoad() {
	
	int processors = Runtime.getRuntime().availableProcessors();
	OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
	double load_average = os.getSystemLoadAverage();
	if (processors>0 && load_average>0.0) {
		Double percent = Double.valueOf (Double.valueOf(load_average) / Double.valueOf(processors));
		if (percent > CPU_THRESHOLD_X)
			return "CPU load "+ String.format("%6.2f", percent.doubleValue()*100.0)+ "%";
	}
	return null;
}


/**
 * SELECT pg_cancel_backend(pid) from (select pid from pg_stat_activity where state = 'idle in transaction' and now()- xact_start > '160 minute'::interval) AS ACT
 */

private String pingDataBase() {
	return getContentDao().pingDataBase();
}



	
	
}
