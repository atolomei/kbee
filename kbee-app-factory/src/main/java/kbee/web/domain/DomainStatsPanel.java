package kbee.web.domain;


import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.datetime.DateTimeService;
import com.novamens.dom.Domain;
import com.novamens.dom.KBFSStorageType;
import com.novamens.metrics.SystemMetricsService;
import com.novamens.metrics.domain.DomainMetricsService;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KeyValue;

import kbee.util.PropertiesFactory;

public class DomainStatsPanel extends Panel {
	private static final long serialVersionUID = 665040124379746403L;

	static private double GB = 1000000000.0;
	
	static private Logger logger = LogManager.getLogger(DomainStatsPanel.class.getName());
	
	static String version 	= PropertiesFactory.getInstance("kbee").getProperties().getProperty("com.novamens.kbee.version", "");
	static String date_str 	= PropertiesFactory.getInstance("kbee").getProperties().getProperty("com.novamens.kbee.version.date", null);
	
	final boolean is_root = ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot();
	
	private IModel<Domain> model;
	private List<KeyValue<String>> data;
	private List<KeyValue<String>> activity_data;
	
	/**
	 * @param id
	 * @param name
	 * @param console
	 */
	public DomainStatsPanel(String id, IModel<Domain> model) {
		super(id);
		setModel(model);

		
		DataView<KeyValue<String>> application_info = new DataView<KeyValue<String>>("application-info",new ListDataProvider<KeyValue<String>>(applicationInfo())) {
			private static final long serialVersionUID = 1L;
			protected void populateItem(final Item<KeyValue<String>> item){
				item.add( new Label("label", item.getModelObject().getKey()));
				item.add( (new Label("value", item.getModelObject().getDisplayName())).setEscapeModelStrings(false));
  			}				
		};
		add(application_info);
		
		
		WebMarkupContainer actc =new WebMarkupContainer("activity-container");
		add(actc);
		actc.setVisible(is_root);
		
		DataView<KeyValue<String>> activity_info = new DataView<KeyValue<String>>("activity-info",new ListDataProvider<KeyValue<String>>(recentActivityInfo())) {
			private static final long serialVersionUID = 1L;
			protected void populateItem(final Item<KeyValue<String>> item){
				item.add( new Label("label", item.getModelObject().getKey()));
				item.add( (new Label("value", item.getModelObject().getDisplayName())).setEscapeModelStrings(false));
  			}			
		};
		actc.add(activity_info);
		activity_info.setVisible(is_root);
		
	}
	
	public void setModel(IModel<Domain> model) {
		this.model = model;
	}
	
	public IModel<Domain> getModel() {
		return this.model;
	}
	

	
	
	private DomainMetricsService getDomainMetricsServices() {
		return  ServiceLocator.getService(DomainMetricsService.class);
	}

	
	
	/** 
	 * @return
	 */
	private List<KeyValue<String>> applicationInfo() {
		
		if (data!=null)
			return data;
		
		data = new ArrayList<KeyValue<String>>();
	
		try {
			
			getDomainMetricsServices().forceCalculate(getModel().getObject());
			
			NumberFormat nf = NumberFormat.getInstance(getSessionUser().getLocale());
			nf.setMinimumFractionDigits(2);
			nf.setMaximumFractionDigits(2);
			nf.setRoundingMode(RoundingMode.HALF_UP);
			
			NumberFormat nint = NumberFormat.getInstance(getSessionUser().getLocale());
			nint.setMinimumFractionDigits(0);
			nint.setMaximumFractionDigits(0);
			nint.setRoundingMode(RoundingMode.HALF_UP);
			
			long total_external = getDomainMetricsServices().getHardDisk(getModel().getObject(), KBFSStorageType.External);

			// total includes: KBFS1, KBFS2, KBFSArchive
			//
			long total = getDomainMetricsServices().getHardDisk(getModel().getObject());
			
			long total_kbfs1 = getDomainMetricsServices().getHardDisk(getModel().getObject(), KBFSStorageType.KBFS1);
			long total_kbfs2 = getDomainMetricsServices().getHardDisk(getModel().getObject(), KBFSStorageType.Minio);
	        long total_odilon = getDomainMetricsServices().getHardDisk(getModel().getObject(), KBFSStorageType.Odilon);
			long total_kbfs2archive = getDomainMetricsServices().getHardDisk(getModel().getObject(), KBFSStorageType.MinioArchive);
			
			int qta = getModel().getObject().getQuota();
				
			if (qta>0) {
				
				// Storage Allocation
				//
				String squota =  nf.format(qta) + " <span class=\"ago\">GB<span>";
				data.add(new KeyValue<String>(getLabel("quota"),  squota));
				
				Double per = Double.valueOf(100 * (double) total / ((double) GB * qta));
				String sval =  nf.format(Double.valueOf((double) total / (double) GB).doubleValue())+" <span class=\"ago\"> GB</span> ";
				
				// Storage Used
				//
				data.add(new KeyValue<String>(getLabel("usage"),   sval));
				
				
				// Storage remaining
				Double remaining = Double.valueOf(qta) - Double.valueOf((double) total / (double) GB);
				
				data.add(new KeyValue<String>(getLabel("remaining"),  nf.format(remaining)  +" <span class=\"ago\">GB</span> "));

				// Percentage Used
				data.add(new KeyValue<String>(getLabel("usageper"),   nf.format(per.doubleValue()) + " <span class=\"ago\">%</span> "));
				
				// Percentage Remaining
				//
				if (remaining>0) {
					data.add( new KeyValue<String>(getLabel("remainingper"), nf.format(100.0 * remaining / Double.valueOf(qta)) + " <span class=\"ago\">%</span> "));
				} else
					data.add(new KeyValue<String>(getLabel("remainingper"),  "0.00<span class=\"ago\"> %</span> "));
			}					
			else {
				String sval =  nf.format(Double.valueOf((double) total / (double) GB).doubleValue())+" <span class=\"ago\">GB</span> ";
				
				// Storage Allocation
				data.add(new KeyValue<String>(getLabel("quota"),  "on demand"));
				
				// Storage Used
				data.add(new KeyValue<String>(getLabel("usage"),  sval ));
			}
			

			
			// Storage Gateway
			//										
			String gat =  nf.format(Double.valueOf((double) total_external / (double) GB).doubleValue())+" <span class=\"ago\"> GB</span> ";
			
			
			if (getDomainMetricsServices().getMeanHardDiskIncrease30d(getModel().getObject())>0) {
				String sval_30d = nf.format(Double.valueOf((double) getDomainMetricsServices().getMeanHardDiskIncrease30d(getModel().getObject()) / (double) GB).doubleValue()) + " <span class=\"ago\">GB / month<span>";

				// Storage Growth Rate
				data.add(new KeyValue<String>(getLabel("grate"), sval_30d));
			}
			
			long con = getContentDao().getTotalContents(getModel().getObject());
			long res = getContentDao().getTotalResources(getModel().getObject());
			
			String res_x_content;
			
			if (con>0) { 
				res_x_content = nf.format(Double.valueOf(res) / Double.valueOf(con)); 
			}
			else
				res_x_content = "n/a";
				

			data.add(new KeyValue<String>("Gateway Storage",   gat));

			
			data.add(new KeyValue<String>(getLabel("contents") , nint.format(con)));
			data.add(new KeyValue<String>(getLabel("resources") ,nint.format(res) + " <span class=\"ago\"> (" +  res_x_content.trim() + " res / file) </span> "));
										
			DateTimeService service = ServiceLocator.getService(DateTimeService.class);
			User user = getSessionUser();
			String zid = service.getMapZoneIds().get(user.getTimeZone());
			if (zid==null)
					zid=ZoneId.systemDefault().getId();
			
			data.add(new KeyValue<String>( getLabel("modified"), service.timeElapsed(getModel().getObject().getLastModifiedOffsetDateTime(), ZoneId.of(zid), user.getLocale(), DateTimeService.DATE_COLlOQUIAL_AGO, "ago")));
			
			if (getModel().getObject().getCreationOffsetDateTime()!=null)
				data.add(new KeyValue<String>(getLabel("created"), service.timeElapsed(getModel().getObject().getCreationOffsetDateTime(), ZoneId.of(zid), user.getLocale(), DateTimeService.DATE_COLlOQUIAL_AGO, "ago")));
			

		} catch (Exception e) {
			logger.error(e);
			data.add(new KeyValue<String>("Error.", e.getClass().getName()+ " " + e.getMessage()));
			

		}
		return data;
	}
	
	/** 
	 * @return
	 */				
	private List<KeyValue<String>> apiInfo() {
		
		List<KeyValue<String>> data = new ArrayList<KeyValue<String>>();
	
		try {
		
			try {
				if (isPostgreSQL()) {
					
					//	This works on PostgreSQL but not in Oracle
					//
					String even_domain = " EVENT_DOMAIN='" + getModel().getObject().getName()+"' ";
					
					Integer api_event_w 		    = getContentDao().executeCountNativeQuery("select count(*) from api_logevent where  " + even_domain+ "  and event_status=200  and event_time >((now() - INTERVAL '1 week')\\:\\:timestamp)");
					Integer api_err_event_w 	    = getContentDao().executeCountNativeQuery("select count(*) from api_logevent where  " + even_domain+ "  and event_status!=200 and event_time >((now() - INTERVAL '1 week')\\:\\:timestamp)");
					
					Integer api_soap_w  		    = getContentDao().executeCountNativeQuery("select count(*) from api_soapevent where " + even_domain + " and event_status=200  and event_time >((now() - INTERVAL '1 week')\\:\\:timestamp)");
					Integer api_err_soap_w  	    = getContentDao().executeCountNativeQuery("select count(*) from api_soapevent where " + even_domain+ " and event_status!=200 and event_time >((now() - INTERVAL  '1 week')\\:\\:timestamp)");
																																
					Integer api_event_d 		    = getContentDao().executeCountNativeQuery("select count(*) from api_logevent where "  + even_domain+ " and  event_status=200  and event_time >((now() - INTERVAL '1 day')\\:\\:timestamp)");
					Integer api_err_event_d 	    = getContentDao().executeCountNativeQuery("select count(*) from api_logevent where "  + even_domain+ " and  event_status!=200 and event_time >((now() - INTERVAL '1 day')\\:\\:timestamp)");
					Integer api_soap_d  		    = getContentDao().executeCountNativeQuery("select count(*) from api_soapevent where " + even_domain+ " and  event_status=200  and event_time >((now() - INTERVAL '1 day')\\:\\:timestamp)");
					Integer api_err_soap_d  	    = getContentDao().executeCountNativeQuery("select count(*) from api_soapevent where " + even_domain+ " and  event_status!=200 and event_time >((now() - INTERVAL '1 day')\\:\\:timestamp)");
																																	
					Integer api_event_h 			= getContentDao().executeCountNativeQuery("select count(*) from api_logevent where 	" + even_domain+" and 	event_status=200  and event_time >((now() - INTERVAL '1 hour')\\:\\:timestamp)");
					Integer api_err_event_h 		= getContentDao().executeCountNativeQuery("select count(*) from api_logevent where 	" + even_domain+" and 	event_status!=200 and event_time >((now() - INTERVAL '1 hour')\\:\\:timestamp)");
					
					Integer api_soap_h  			= getContentDao().executeCountNativeQuery("select count(*) from api_soapevent where " + even_domain+" and 	event_status=200  and event_time >((now() - INTERVAL '1 hour')\\:\\:timestamp)");
					Integer api_err_soap_h  		= getContentDao().executeCountNativeQuery("select count(*) from api_soapevent where " + even_domain+" and 	event_status!=200 and event_time >((now() - INTERVAL '1 hour')\\:\\:timestamp)");
																						
					Integer api_event_5m 			= getContentDao().executeCountNativeQuery("select count(*) from api_logevent where 	" + even_domain+" and 	event_status=200  and event_time >((now() - INTERVAL '5 minute')\\:\\:timestamp)");
					Integer api_err_event_5m 		= getContentDao().executeCountNativeQuery("select count(*) from api_logevent where 	" + even_domain+" and 	event_status!=200 and event_time >((now() - INTERVAL '5 minute')\\:\\:timestamp)");
					Integer api_soap_5m  			= getContentDao().executeCountNativeQuery("select count(*) from api_soapevent where " + even_domain+" and 	event_status=200  and event_time >((now() - INTERVAL '5 minute')\\:\\:timestamp)");
					Integer api_err_soap_5m  		= getContentDao().executeCountNativeQuery("select count(*) from api_soapevent where " + even_domain+" and 	event_status!=200 and event_time >((now() - INTERVAL '5 minute')\\:\\:timestamp)");
																						
					data.add(new KeyValue<String>("API. Requests ok/error (5m 1hr 1d 1w)", 	    String.valueOf(api_event_5m) + "/" + String.valueOf(api_err_event_5m)    + "<span class=\"separator\">|</span>" +     
									  												String.valueOf(api_event_h)  + "/" + String.valueOf(api_err_event_h)     + "<span class=\"separator\">|</span>" +
									  												String.valueOf(api_event_d)  + "/" + String.valueOf(api_err_event_d)     + "<span class=\"separator\">|</span>" +  	 
									  												String.valueOf(api_event_w)  + "/" + String.valueOf(api_err_event_w))); 			
					
					data.add(new KeyValue<String>("API. SOAP Requests ok/error (5m 1hr 1d 1w)", 	    
							    String.valueOf(api_soap_5m) + "/" + String.valueOf(api_err_soap_5m)     + "<span class=\"separator\">|</span>" +     
								String.valueOf(api_soap_h)  + "/" + String.valueOf(api_err_soap_h)     + "<span class=\"separator\">|</span>" +
								String.valueOf(api_soap_d)  + "/" + String.valueOf(api_err_soap_d)     + "<span class=\"separator\">|</span>" +  	 
								String.valueOf(api_soap_w)  + "/" + String.valueOf(api_err_soap_w)  )); 			
				}
				
			} catch (Exception e) {
				logger.error(e.getStackTrace());
			}

			
		} catch (RuntimeException e) {
			
			logger.error(e);
		}
		return data;
	}
	

	/**
	 * 
	 */
	protected List<KeyValue<String>> recentActivityInfo() {
		
		if (activity_data!=null)
			return activity_data; 
		
		activity_data = new ArrayList<KeyValue<String>>();
		
		SystemMetricsService metrics_service = ServiceLocator.getService(SystemMetricsService.class);
		
		String login=String.format("%12.2f  <span class=\"separator\">|</span> %12.2f   <span class=\"separator\">|</span>  %12.2f", 
				  metrics_service.getMeterLogin(getModel().getObject().getId()).getOneMinuteRate() * 60,
				  metrics_service.getMeterLogin(getModel().getObject().getId()).getFiveMinuteRate() * 60,
				  metrics_service.getMeterLogin(getModel().getObject().getId()).getFifteenMinuteRate() * 60);	
		
		String em=String.format("%12.2f  <span class=\"separator\">|</span> %12.2f   <span class=\"separator\">|</span>  %12.2f", 
				  metrics_service.getMeterEmails(getModel().getObject().getId()).getOneMinuteRate() * 60,
				  metrics_service.getMeterEmails(getModel().getObject().getId()).getFiveMinuteRate() * 60,
				  metrics_service.getMeterEmails(getModel().getObject().getId()).getFifteenMinuteRate() * 60);	

		activity_data.add(new KeyValue<String>("Login/min (1m 5m 15m)", login));
		activity_data.add(new KeyValue<String>("Emails/min (1m 5m 15m)", em));
		
		long v= metrics_service.getCounterUsersLogged(getModel().getObject().getId()).getCount();
		
		activity_data.add(new KeyValue<String>("Users online (Sign In/Out)", String.valueOf(v>0?v:0)));
		
	 	OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
	 	
	 	String la=os.getSystemLoadAverage()>0?String.valueOf(os.getSystemLoadAverage()):"N/A";
	 	String spercent="";
	 	
	 	int processors = Runtime.getRuntime().availableProcessors();
	 	
		if (processors>0 && os.getSystemLoadAverage()>0) {
			Double percent = Double.valueOf (Double.valueOf(os.getSystemLoadAverage()) / Double.valueOf(processors));
			spercent= " ("+ (String.format("%6.2f", percent.doubleValue()*100.0)).trim()+ "%)";
		}
	 		
		activity_data.add(new KeyValue<String>("CPU Load Average", la + spercent));
		
		
		try {
			if (isPostgreSQL()) {
				
				//	This works on PostgreSQL but not in Oracle
				//
				String even_domain = " EVENT_DOMAIN='" + getModel().getObject().getName()+"' ";
				
				Integer api_event_w 		    = getContentDao().executeCountNativeQuery("select count(*) from api_logevent where  " + even_domain+ "  and event_status=200  and event_time >((now() - INTERVAL '1 week')\\:\\:timestamp)");
				Integer api_err_event_w 	    = getContentDao().executeCountNativeQuery("select count(*) from api_logevent where  " + even_domain+ "  and event_status!=200 and event_time >((now() - INTERVAL '1 week')\\:\\:timestamp)");
				Integer api_soap_w  		    = getContentDao().executeCountNativeQuery("select count(*) from api_soapevent where " + even_domain + " and event_status=200  and event_time >((now() - INTERVAL '1 week')\\:\\:timestamp)");
				Integer api_err_soap_w  	    = getContentDao().executeCountNativeQuery("select count(*) from api_soapevent where " + even_domain+ " and event_status!=200 and event_time >((now() - INTERVAL  '1 week')\\:\\:timestamp)");
																															
				Integer api_event_d 		    = getContentDao().executeCountNativeQuery("select count(*) from api_logevent where "  + even_domain+ " and  event_status=200  and event_time >((now() - INTERVAL '1 day')\\:\\:timestamp)");
				Integer api_err_event_d 	    = getContentDao().executeCountNativeQuery("select count(*) from api_logevent where "  + even_domain+ " and  event_status!=200 and event_time >((now() - INTERVAL '1 day')\\:\\:timestamp)");
				Integer api_soap_d  		    = getContentDao().executeCountNativeQuery("select count(*) from api_soapevent where " + even_domain+ " and  event_status=200  and event_time >((now() - INTERVAL '1 day')\\:\\:timestamp)");
				Integer api_err_soap_d  	    = getContentDao().executeCountNativeQuery("select count(*) from api_soapevent where " + even_domain+ " and  event_status!=200 and event_time >((now() - INTERVAL '1 day')\\:\\:timestamp)");
																																
				Integer api_event_h 			= getContentDao().executeCountNativeQuery("select count(*) from api_logevent where 	" + even_domain+" and 	event_status=200  and event_time >((now() - INTERVAL '1 hour')\\:\\:timestamp)");
				Integer api_err_event_h 		= getContentDao().executeCountNativeQuery("select count(*) from api_logevent where 	" + even_domain+" and 	event_status!=200 and event_time >((now() - INTERVAL '1 hour')\\:\\:timestamp)");
				Integer api_soap_h  			= getContentDao().executeCountNativeQuery("select count(*) from api_soapevent where " + even_domain+" and 	event_status=200  and event_time >((now() - INTERVAL '1 hour')\\:\\:timestamp)");
				Integer api_err_soap_h  		= getContentDao().executeCountNativeQuery("select count(*) from api_soapevent where " + even_domain+" and 	event_status!=200 and event_time >((now() - INTERVAL '1 hour')\\:\\:timestamp)");
																					
				Integer api_event_5m 			= getContentDao().executeCountNativeQuery("select count(*) from api_logevent where 	" + even_domain+" and 	event_status=200  and event_time >((now() - INTERVAL '5 minute')\\:\\:timestamp)");
				Integer api_err_event_5m 		= getContentDao().executeCountNativeQuery("select count(*) from api_logevent where 	" + even_domain+" and 	event_status!=200 and event_time >((now() - INTERVAL '5 minute')\\:\\:timestamp)");
				Integer api_soap_5m  			= getContentDao().executeCountNativeQuery("select count(*) from api_soapevent where " + even_domain+" and 	event_status=200  and event_time >((now() - INTERVAL '5 minute')\\:\\:timestamp)");
				Integer api_err_soap_5m  		= getContentDao().executeCountNativeQuery("select count(*) from api_soapevent where " + even_domain+" and 	event_status!=200 and event_time >((now() - INTERVAL '5 minute')\\:\\:timestamp)");
																					
				activity_data.add(new KeyValue<String>("API. Requests ok/error (5m 1hr 1d 1w)", 	    String.valueOf(api_event_5m) + "/" + String.valueOf(api_err_event_5m)    + "<span class=\"separator\">|</span>" +     
								  												String.valueOf(api_event_h)  + "/" + String.valueOf(api_err_event_h)     + "<span class=\"separator\">|</span>" +
								  												String.valueOf(api_event_d)  + "/" + String.valueOf(api_err_event_d)     + "<span class=\"separator\">|</span>" +  	 
								  												String.valueOf(api_event_w)  + "/" + String.valueOf(api_err_event_w))); 			
				
				activity_data.add(new KeyValue<String>("API. SOAP  ok/error (5m 1hr 1d 1w)", 	    
						    String.valueOf(api_soap_5m) + "/" + String.valueOf(api_err_soap_5m)     + "<span class=\"separator\">|</span>" +     
							String.valueOf(api_soap_h)  + "/" + String.valueOf(api_err_soap_h)     + "<span class=\"separator\">|</span>" +
							String.valueOf(api_soap_d)  + "/" + String.valueOf(api_err_soap_d)     + "<span class=\"separator\">|</span>" +  	 
							String.valueOf(api_soap_w)  + "/" + String.valueOf(api_err_soap_w)  )); 			
			}
			
		} catch (Exception e) {
			activity_data.add(new KeyValue<String>("Error.", e.getClass().getName()+ " " + e.getMessage()));
			logger.error(e);
		}

		return 	activity_data;
	}
	
	
	private String getCPULoad() {
		int processors = Runtime.getRuntime().availableProcessors();
		OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
		double load_average = os.getSystemLoadAverage();
		if (processors>0 && load_average>0.0) {
			Double percent = new Double (Double.valueOf(load_average) / Double.valueOf(processors));
			NumberFormat nf = NumberFormat.getInstance(getSessionUser().getLocale());
			nf.setMinimumFractionDigits(2);
			nf.setMaximumFractionDigits(2);
			nf.setRoundingMode(RoundingMode.HALF_UP);
			String cpu_load=  String.valueOf(load_average) + " <span class=\"ago\"> (" +  nf.format(percent.doubleValue()*100.0) +" %)</span> ";
			return cpu_load;
		}
		return "n/a";
	}
	
	
	String database = PropertiesFactory.getInstance("kbee").getProperties().getProperty("jdbc.url", null);
	private boolean isPostgreSQL() {
		database=database.trim();
		if (database==null)
			return false;
		if(database.contains("oracle"))  
			return false;
		return true;
	}

	
	

	private String getLabel(String key) {
		return (new StringResourceModel(key, this, null)).getString();
	}
	
	
	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	protected User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	

}
