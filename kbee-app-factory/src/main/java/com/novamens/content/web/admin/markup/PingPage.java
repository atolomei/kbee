package com.novamens.content.web.admin.markup;



import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.dom.Domain;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.vault.VaultService;
import com.novamens.kbee.wicket.markup.html.page.AbstractKbeeWebPage;
import com.novamens.kbfs.FileServerMinio;
import com.novamens.kbfs.FileServerOdilon;
import com.novamens.kbfs.FileServerS3;
import com.novamens.kbfs.v1.FileServerV1;
import com.novamens.metrics.SystemMetricsService;
import com.novamens.scheduler.SchedulerException;
import com.novamens.scheduler.SchedulerService;
import com.novamens.service.ServiceLocator;
import com.novamens.solr.indexer.query.SolrQuery;
import com.novamens.wicket.markup.html.form.Form;

import kbee.util.PropertiesFactory;
import kbee.util.Tuple;
import kbee.web.page.FactoryPage;


public class PingPage extends AbstractKbeeWebPage implements FactoryPage  {
			
	private static final long serialVersionUID = 1L;
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(PingPage.class.getName());
	
	static private final Properties props = PropertiesFactory.getInstance("kbee").getProperties();
	
	private static final String _OK  = PropertiesFactory.getInstance("kbee").getProperties().getProperty("ping.ok", "OK");
	
	

	private static final ResourceReference ICONS_CSS 			= new JavaScriptResourceReference(AbstractKbeeWebPage.class, "assets/css/icons/icomoon/styles.css");
	
	private static final ResourceReference COMPONENTS_CSS 	= new JavaScriptResourceReference(AbstractKbeeWebPage.class, "assets/css/components.css");
    private static final ResourceReference CORE_CSS 			= new JavaScriptResourceReference(AbstractKbeeWebPage.class, "assets/css/core.css");
	private static final ResourceReference APP_JS 				= new JavaScriptResourceReference(AbstractKbeeWebPage.class, "assets/js/core/app.js");
	
	
	
	private static final ResourceReference BOOTSTRAP_JS 		= new JavaScriptResourceReference(Form.class, com.novamens.wicket.markup.html.form.Form.BOOTSTRAP_JS);
	private static final ResourceReference BOOTSTRAP_CSS 		= new CssResourceReference(Form.class, Form.BOOTSTRAP);
	
	
	private static final ResourceReference KBEE_BOOTSTRAP_CSS 	= new CssResourceReference(AbstractKbeeWebPage.class, "kbeebootstrap.css");
	
	private static final ResourceReference CSS_KBEE_BOOTSTRAP_0_360      = new CssResourceReference(AbstractKbeeWebPage.class, "kb-0-360.css");
	private static final ResourceReference CSS_KBEE_BOOTSTRAP_361_800    = new CssResourceReference(AbstractKbeeWebPage.class, "kb-361-800.css");
	private static final ResourceReference CSS_KBEE_BOOTSTRAP_801_1200   = new CssResourceReference(AbstractKbeeWebPage.class, "kb-801-1200.css");
	private static final ResourceReference CSS_KBEE_BOOTSTRAP_1201_1600  = new CssResourceReference(AbstractKbeeWebPage.class, "kb-1201-1600.css");
	private static final ResourceReference CSS_KBEE_BOOTSTRAP_1601  	 = new CssResourceReference(AbstractKbeeWebPage.class, "kb-1601.css");

	
	private static final ResourceReference AW = new CssResourceReference(Form.class, Form.FONTAWESOME);
	private static final ResourceReference CSS_KBEE_LIMITLESS = new CssResourceReference(AbstractKbeeWebPage.class, "kbee-limitless.css");

	private final static long DELTA= 1000 * 2;
	
	private static long last_ping = 0;
	
	
	private double CPU_THRESHOLD_DOUBLE = 5.0;
	
	//
	// private static final String _CPU_THRESHOLD  = PropertiesFactory.getInstance("kbee").getProperties().getProperty("ping.cpu.threshold", "2.75");
	//static {
	//	try {
	//		CPU_THRESHOLD_DOUBLE = Double.valueOf(_CPU_THRESHOLD); 
	//	} catch (Exception e) {
	//		CPU_THRESHOLD_DOUBLE = 4.0;
	//	}
	//}
	//
	
	public PingPage() {
			this.setPageKeywords("System status, Database, Application, File System, ping");
			this.setPageRobots("NO INDEX");
			initialize();
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(getApplication().getJavaScriptLibrarySettings()
				.getJQueryReference()));  

		
		
		response.render(CssHeaderItem.forReference(ICONS_CSS));
		
		response.render(CssHeaderItem.forReference(COMPONENTS_CSS));
		response.render(JavaScriptHeaderItem.forReference(APP_JS));
		response.render(CssHeaderItem.forReference(CORE_CSS));
		
		response.render(CssHeaderItem.forReference(BOOTSTRAP_CSS));
		response.render(JavaScriptHeaderItem.forReference(BOOTSTRAP_JS));

		
		response.render(CssHeaderItem.forReference(AW));
		
		response.render(CssHeaderItem.forReference(KBEE_BOOTSTRAP_CSS));
		response.render(CssHeaderItem.forReference(CSS_KBEE_LIMITLESS));
		
		response.render(CssHeaderItem.forReference(CSS_KBEE_BOOTSTRAP_0_360));
		response.render(CssHeaderItem.forReference(CSS_KBEE_BOOTSTRAP_361_800));
		response.render(CssHeaderItem.forReference(CSS_KBEE_BOOTSTRAP_801_1200));
		response.render(CssHeaderItem.forReference(CSS_KBEE_BOOTSTRAP_1201_1600));
		response.render(CssHeaderItem.forReference(CSS_KBEE_BOOTSTRAP_1601));
	}

	
	
	protected String execPing() {
		List<String> results = new ArrayList<String>();
		
		results.add(pingDataBase());
		results.add(pingCPULoad());
		results.add(pingScheduler());
		results.add(pingVault());
		results.add(pingSolR());
		results.add(pingOutOfMemoryFlag());
		
		boolean api_enabled=props.getProperty("api.enabled", "yes").toLowerCase().trim().equals("yes");
		
		if (api_enabled)
			results.add(pingAPI());
		
		boolean kbfs1_enabled=props.getProperty("File System.enabled", "yes").toLowerCase().trim().equals("yes");
		boolean kbfs2_enabled=props.getProperty("kbfs2.enabled", "yes").toLowerCase().trim().equals("yes");
					
		boolean odilon_enabled=props.getProperty("odilon.enabled", "yes").toLowerCase().trim().equals("yes");

		
		FileServerS3 fss3=ServiceLocator.getService(FileServerS3.class);
		boolean s3_enabled = (fss3!=null && fss3.isEnabled());  
		
		
		if (kbfs1_enabled)
			results.add(pingKBFS1());
		
		if (kbfs2_enabled)
			results.add(pingKBFSMinio());

		if (s3_enabled)
			results.add(pingS3());

		if (odilon_enabled)
			results.add(pingOdilon());

		
		StringBuilder rs = new StringBuilder();
		
		for (String str: results) {
			if (str!=null) {
				if (rs.length()>0)
					rs.append(" | ");
				rs.append(str);
			}
		}
		
		String result = rs.toString();
		
		if (result!=null && result.length()>0) 
			return result;
		else
			return _OK;
	}
	/**
	 * 
	 */
	@Override
	protected ResourceReference getCssResource() {
		return KBEE_BOOTSTRAP_CSS;
	}
	
	
	static private final long TWO_HOURS = 1000 * 60 * 2;
	/**
	 * 
	 */
	private synchronized void initialize() {
		
		long now = System.currentTimeMillis();

		if ((now-last_ping)<DELTA) {
				long sleep = DELTA - (now-last_ping);
				try {
					Thread.sleep(sleep);
				} catch (InterruptedException e) {
					logger.error(e);
				}
		}
		
		try {
			CPU_THRESHOLD_DOUBLE =  Double.valueOf(getContentDao().findSystemParameterValueByKey("ping.cpu.threshold", "5.0")).doubleValue();
		} catch (Exception e) {
			logger.error(e);
			CPU_THRESHOLD_DOUBLE =  5.0;
		}
		
		String res =  execPing();
		
		if (res.equals(_OK)) {
			try {
				Thread.sleep(500);
				res =  execPing();
			} catch (InterruptedException e) {
				logger.error(e);
			}
		}
		
		logger.debug(res);
		
		Label l_result = new Label("result", res);
		add(l_result);
		
		last_ping  = System.currentTimeMillis();
		logger.debug("Render time " + String.valueOf(last_ping-now)+" ms");
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
				logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName() + " | " + e.getMessage());
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
	
	// int return getContentDao().getIdleTransactions();
	// --------
	// select pid,datname, xact_start, query_start , substr(query,1,250) from pg_stat_activity where state = 'idle in transaction' and now()- xact_start > '5 minute'::interval
	// -------
		
	
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

	
	/**
	 * 
	 * @return
	 * 
 		<p>if load Average is over to 500% #CPU</p>
	 */
	private String pingCPULoad() {
		
		
		int processors = Runtime.getRuntime().availableProcessors();
		
		OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
		double load_average = os.getSystemLoadAverage();

		if (processors>0 && load_average>0.0) {
			Double percent = Double.valueOf (Double.valueOf(load_average) / Double.valueOf(processors));
			if (percent > CPU_THRESHOLD_DOUBLE)
				return "CPU load "+ String.format("%6.2f", percent.doubleValue()*100.0)+ "%";
		}
		return null;
	}

	
	private String pingDataBase() {
		return getContentDao().pingDataBase();
	}
	
	
	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
}
