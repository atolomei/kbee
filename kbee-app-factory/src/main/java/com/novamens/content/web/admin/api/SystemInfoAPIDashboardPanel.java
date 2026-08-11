package com.novamens.content.web.admin.api;


import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.model.Model;

import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.web.admin.markup.AbstractSystemInfoPanel;
import com.novamens.content.web.sql.markup.SQLFiltersPanel;
import com.novamens.kbee.content.webapi.traffic.TrafficControlService;
import com.novamens.kbee.system.SystemParameters;
import com.novamens.kbee.wicket.markup.html.areainfo.AreaInfoPanel;
import com.novamens.kbee.wicket.markup.html.areainfo.GridInfoPanel;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.metrics.SystemMetricsService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.util.NumberFormatter;
import kbee.util.PropertiesFactory;
import kbee.util.Tuple;

/**
 * 
 * 			String sql = "select ts \"Date\", total \"TOTAL\",  mean_time_total \"MEAN TIME (ms)\", total_post \"TOTAL POST\",  mean_time_post \"POST mean time (ms)\",  totdel \"TOTAL DEL\", meantimedel \"DEL mean time (ms)\",  total_bounced \"Total Bounced\" from kb_api_usage_stat order by ts desc limit 720";
			SQLFiltersPanel sqlpanel = new SQLFiltersPanel("panel", sql);
			sqlpanel.setWide(false);
			add(sqlpanel);
 *
 */

public class SystemInfoAPIDashboardPanel extends AbstractSystemInfoPanel {
	private static final long serialVersionUID = 1L;

	private static final boolean IS_API_ENABLED =  PropertiesFactory.getInstance("kbee").getProperties().getProperty("api.enabled", "yes").toLowerCase().trim().equals("yes");

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SystemInfoAPIDashboardPanel.class.getName());
	
	private String database = PropertiesFactory.getInstance("kbee").getProperties().getProperty("jdbc.url", null);
	
	public SystemInfoAPIDashboardPanel() {
		this("info-panel");
	}

	public SystemInfoAPIDashboardPanel(String id) {
		super(id);
	}

	/**
	@Override
	protected MenuBreadCrumbPanel getMenuBreadCrumbPanel() {
		MenuBreadCrumbPanel  bc =new MenuBreadCrumbPanel();
		 bc.addElement(new APIBC());
		 bc.addElement(getPageBCElement());
		 return bc;
	}

	@Override
	protected BCElement getPageBCElement() {
		return new BCElement(new Model<String>("Dashboard"));
	}
	**/
	
	/**
	 * 
	 */
	@Override
	public void onInitialize() {
		super.onInitialize();

		setOutputMarkupId(true);
		
		
		
		AreaInfoPanel area = new AreaInfoPanel("info");
		add(area);
		area.setSections(AreaInfoPanel.ONE_SECTION);
		area.setCss("col-lg-12");
		
		area.addPanel(new GridInfoPanel("element",  recentActivityAPIInfo(), new Model<String>("Dashboard"), true));
		
		// 90 days totals
		String start_date = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(OffsetDateTime.now().minusDays(10).truncatedTo(ChronoUnit.DAYS));   //;" 00:00:00.000";
		String end_date = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(OffsetDateTime.now().truncatedTo(ChronoUnit.DAYS));   //;" 00:00:00.000";
		String d_sql="select date(event_time) \"Date\", "
				
				+ " to_char(count(*), '99,999,999' ) \"TOTAL\",  "
				+ " to_char(avg(event_processing_time), '99,999' ) \"AVG TIME (ms)\",  "
				
				+ " to_char(count(event_request='POST'),   '99,999,999' ) \"POST\",  "
				+ " to_char(count(event_request like 'DEL%'),   '99,999,999' ) \"DEL\"  "
				
				+ " from api_logevent where (event_time >= '"+start_date+"' and event_time < '"+end_date+"') group by date(event_time) order by date(event_time) desc limit 10 ";
		
		logger.debug(d_sql);
		
		SQLFiltersPanel sqlpanel = new SQLFiltersPanel("element", d_sql);
		sqlpanel.setWide(false);
		area.addPanel(sqlpanel);
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		// area.addPanel(new GridInfoPanel("element",  previousDaysActivityAPIInfo(), new Model<String>("Recent activity"), true));
		
		
		
		
	}
	
protected List<Tuple> previousDaysActivityAPIInfo() {
		
		List<Tuple> data = new ArrayList<Tuple>();
		
		long start = System.currentTimeMillis();
		
		try {
				if (!IS_API_ENABLED) {
					data.add(new Tuple("API", "<span class= \"warning\">disabled</span>"));
					return data;
				}
				
				
				
				
				
				
				
		
				
				
				
				
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
			// This works on PostgreSQL but not in Oracle
			//
			if (isPostgreSQL()) {
				
				long start = System.currentTimeMillis();

				SystemMetricsService metrics_service = ServiceLocator.getService(SystemMetricsService.class);

				String cam=String.format("%12.2f  <span class=\"separator\">|</span> %12.2f   <span class=\"separator\">|</span>  %12.2f", 
						  metrics_service.getMeterContentAPICheckin().getOneMinuteRate() * 60,
						  metrics_service.getMeterContentAPICheckin().getFiveMinuteRate() * 60,
						  metrics_service.getMeterContentAPICheckin().getFifteenMinuteRate() * 60);	
									
				Integer api_err_5m = getContentDao().executeCountNativeQuery("select count(*) from api_logevent where (event_status=412 or event_status=403 or event_status=500)  and event_time >(now() - INTERVAL '5 minute')\\:\\:timestamp");
				Integer api_err_1h = getContentDao().executeCountNativeQuery("select count(*) from api_logevent where (event_status=412 or event_status=403 or event_status=500)  and event_time >(now() - INTERVAL '1 hour')\\:\\:timestamp");
				Integer api_err_1d = getContentDao().executeCountNativeQuery("select count(*) from api_logevent where (event_status=412 or event_status=403 or event_status=500)  and event_time >(now() - INTERVAL '1 day')\\:\\:timestamp");
				
				if (api_err_5m==0 && api_err_1h==0 && api_err_1d==0) 
					data.add(new Tuple("API Status", "<span class= \"success\">ok</span>"));
				else {
					data.add(new Tuple("API Status (errors 403/412/500. 5m 1h 1d)", 							
						NumberFormatter.formatNumber(api_err_5m, getSessionUser().getLocale())    + "<span class=\"separator\">|</span>" +     
						NumberFormatter.formatNumber(api_err_1h, getSessionUser().getLocale())    + "<span class=\"separator\">|</span>" +
						NumberFormatter.formatNumber(api_err_1d, getSessionUser().getLocale())));
				}
				
				String api_in_rate=String.format("%12.2f  <span class=\"separator\">|</span> %12.2f   <span class=\"separator\">|</span>  %12.2f", 
						metrics_service.getMeterAPIRequestsIn().getOneMinuteRate(),
						metrics_service.getMeterAPIRequestsIn().getFiveMinuteRate(),
						metrics_service.getMeterAPIRequestsIn().getFifteenMinuteRate());	
						
				String api_out_rate=String.format("%12.2f  <span class=\"separator\">|</span> %12.2f   <span class=\"separator\">|</span>  %12.2f", 
						metrics_service.getMeterAPIRequestsOut().getOneMinuteRate(),
						metrics_service.getMeterAPIRequestsOut().getFiveMinuteRate(),
						metrics_service.getMeterAPIRequestsOut().getFifteenMinuteRate());	

				String trafficTokens = SystemParameters.get("com.novamens.content.webapi.traffictokens", String.valueOf( TrafficControlService.DEFAULT_TOKENS) );
				
				data.add(new Tuple("API Request Processing Worker Threads", trafficTokens));
				data.add(new Tuple("API Request inbound rate (1m 5m 15m)", 	api_in_rate + " <span class=\"ago\">reqs/sec</span>" ));
				data.add(new Tuple("API Request Throughput (1m 5m 15m)",    api_out_rate   + " <span class=\"ago\">reqs/sec</span>"));

						
				String api_get_rate=String.format("%12.2f  <span class=\"separator\">|</span> %12.2f   <span class=\"separator\">|</span>  %12.2f", 
						metrics_service.getMeterAPIGet().getOneMinuteRate(),
						metrics_service.getMeterAPIGet().getFiveMinuteRate(),
						metrics_service.getMeterAPIGet().getFifteenMinuteRate());	
									
				data.add(new Tuple("API GET rate (1m 5m 15m)",    api_get_rate   + " <span class=\"ago\">reqs/sec</span>"));
 				
				String traffic_queue_in_rate=String.format("%12.2f  <span class=\"separator\">|</span> %12.2f   <span class=\"separator\">|</span>  %12.2f", 
						metrics_service.getMeterAPITrafficeQueueIn().getOneMinuteRate(),
						metrics_service.getMeterAPITrafficeQueueIn().getFiveMinuteRate(),
						metrics_service.getMeterAPITrafficeQueueIn().getFifteenMinuteRate());
				
				String traffic_queue_out_rate=String.format("%12.2f  <span class=\"separator\">|</span> %12.2f   <span class=\"separator\">|</span>  %12.2f", 
						metrics_service.getMeterAPITrafficeQueueOut().getOneMinuteRate(),
						metrics_service.getMeterAPITrafficeQueueOut().getFiveMinuteRate(),
						metrics_service.getMeterAPITrafficeQueueOut().getFifteenMinuteRate());
									
				data.add(new Tuple("Traffic Controller inqueue rate estimator (1m 5m 15m)",  traffic_queue_in_rate + " <span class=\"ago\">reqs/sec</span>" ));
				data.add(new Tuple("Traffic Controller outqueue rate estimator (1m 5m 15m)",  traffic_queue_out_rate + " <span class=\"ago\">reqs/sec</span>" ));

				
				Integer count_1m;
				Integer count_5m;
				Integer count_15m;
				
				count_1m  = Integer.valueOf(0);
				count_5m  =  Integer.valueOf(0);
				count_15m =  Integer.valueOf(0);
				 
				try {
					 count_1m   = getContentDao().executeCountNativeQuery("select count(*) from api_logevent  where event_time >(now() - INTERVAL '1 minute')\\:\\:timestamp");
					 count_5m   = getContentDao().executeCountNativeQuery("select count(*) from api_logevent  where event_time >(now() - INTERVAL '5 minute')\\:\\:timestamp");
					 count_15m  = getContentDao().executeCountNativeQuery("select count(*) from api_logevent  where event_time >(now() - INTERVAL '15 minute')\\:\\:timestamp");
					
					data.add(new Tuple("API Requests total inbound traffic (1m 5m 15m)", 							
							NumberFormatter.formatNumber(count_1m,  getSessionUser().getLocale())    + "<span class=\"separator\">|</span>" +     
							NumberFormatter.formatNumber(count_5m,  getSessionUser().getLocale())    + "<span class=\"separator\">|</span>" +
							NumberFormatter.formatNumber(count_15m, getSessionUser().getLocale())    + " <span class=\"ago\">reqs</span>"));
				} catch (Exception e) {
					logger.error(" {} | {} | {} | {} ", (getSessionUser()!=null?getSessionUser().getUserName():""), e.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName(), e.getMessage());
					data.add( new Tuple(e.getClass().getName(), e.getMessage()));
				}
				
				try {
					
					Integer mean_proc_1m    = count_1m==0  ? 0 : getContentDao().executeCountNativeQuery("select sum(event_processing_time)/" + String.valueOf(count_1m)  + " from api_logevent  where event_time >(now() - INTERVAL '1 minute')\\:\\:timestamp");
					Integer mean_proc_5m    = count_5m==0  ? 0 : getContentDao().executeCountNativeQuery("select sum(event_processing_time)/" + String.valueOf(count_5m)  + " from api_logevent  where event_time >(now() - INTERVAL '5 minute')\\:\\:timestamp");
					Integer mean_proc_15m   = count_15m==0 ? 0 : getContentDao().executeCountNativeQuery("select sum(event_processing_time)/" + String.valueOf(count_15m) + " from api_logevent  where event_time >(now() - INTERVAL '15 minute')\\:\\:timestamp");
	
					String mc1  = null;
					String mc5  = null;
					String mc15 = null;
					
					if(mean_proc_1m < 650) 			     	mc1 = "nofloat pad48 success";
					else if (mean_proc_1m<1350) 			mc1 = "nofloat pad48 warning";
					else									mc1 = "nofloat pad48 danger";
									
					if(mean_proc_5m < 650) 				    mc5 = "nofloat pad48 success";
					else if (mean_proc_5m<1350) 			mc5 = "nofloat pad48 warning";
					else									mc5 = "nofloat pad48 danger";
					
					if(mean_proc_15m < 650) 				mc15 = "nofloat pad48 success";
					else if (mean_proc_15m<1350) 			mc15 = "nofloat pad48 warning";
					else									mc15 = "nofloat pad48 danger";
					
					data.add(new Tuple("API Request mean processing time (1m 5m 15m)", 							
							"<span class=\""+mc1+"\">" +  NumberFormatter.formatNumber(mean_proc_1m, getSessionUser().getLocale())    + "</span> <span class=\"separator\">|</span>" +     
							"<span class=\""+mc5+"\">" +  NumberFormatter.formatNumber(mean_proc_5m, getSessionUser().getLocale())    + "</span> <span class=\"separator\">|</span>" +
							"<span class=\""+mc15+"\">" +  NumberFormatter.formatNumber(mean_proc_15m, getSessionUser().getLocale())   + "</span> <span class=\"ago\">ms</span>"  ));
					
				} catch (Exception e) {
					logger.error(" {} | {} | {} | {} ", (getSessionUser()!=null?getSessionUser().getUserName():""), e.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName(), e.getMessage());
					data.add(new Tuple(e.getClass().getName(), e.getMessage()));
				}

				
				// Mean Request processing time 1m 5m 15m
				
				/**
				  	get sure PostgreSQL has these two indexes:
					CREATE INDEX ON api_logevent (event_time desc);
					CREATE INDEX ON api_soapevent (event_time desc);
				*/
																	
				try {
					data.add(new Tuple("API Request mean processing time estimator", NumberFormatter.formatNumber(metrics_service.getRequestProcessingTimeEstimator().getMean(), getSessionUser().getLocale())+" <span class=\"ago\">ms</span>"));
					data.add(new Tuple("API Request mean processing time avg", NumberFormatter.formatNumber(metrics_service.getRequestProcessingTimeEstimator().getAverageMean(), getSessionUser().getLocale())+" <span class=\"ago\">ms</span>"));
	
					data.add(new Tuple("Traffic Controller Queue inbound rate (1m 5m 15m)",  traffic_queue_in_rate + " <span class=\"ago\">reqs/sec</span>" ));
					data.add(new Tuple("Traffic Controller Queue outbound rate (1m 5m 15m)",  traffic_queue_out_rate + " <span class=\"ago\">reqs/sec</span>" ));
					
					// exponential weighted moving average 
					data.add(new Tuple("Traffic Controller in Queue time estimator", NumberFormatter.formatNumber(metrics_service.getTrafficInQueueEstimator().getMean(), getSessionUser().getLocale())+" <span class=\"ago\">ms</span>"));
					
					// arithmetic average
					data.add(new Tuple("Traffic Controller in Queue time avg", NumberFormatter.formatNumber(metrics_service.getTrafficInQueueEstimator().getAverageMean(), getSessionUser().getLocale())+" <span class=\"ago\">ms</span>"));
					
					data.add(new Tuple("Checkin API (1m 5m 15m)", cam + " <span clasS=\"ago atright\">event/min</span>" ));
					
				} catch (Exception e) {
					logger.error(" {} | {} | {} | {} ", (getSessionUser()!=null?getSessionUser().getUserName():""), e.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName(), e.getMessage());
					data.add( new Tuple(e.getClass().getName(), e.getMessage()));
				} 

				try {
					Integer api_event_d 			= getContentDao().executeCountNativeQuery("select count(*) from api_logevent  where event_status=200   						  and event_time >((now() - INTERVAL '1 day')\\:\\:timestamp)");
					Integer api_err_event_d 		= getContentDao().executeCountNativeQuery("select count(*) from api_logevent  where (event_status>=400  and event_status!=404) and event_time >((now()   - INTERVAL '1 day')\\:\\:timestamp)");
	
					Integer api_soap_d  			= getContentDao().executeCountNativeQuery("select count(*) from api_soapevent where event_status=200  	and event_time >((now() - INTERVAL '1 day')\\:\\:timestamp)");
					Integer api_err_soap_d  		= getContentDao().executeCountNativeQuery("select count(*) from api_soapevent where (event_status>=400   and event_status!=404) and event_time >((now() - INTERVAL '1 day')\\:\\:timestamp)");
					
					Integer api_event_h 			= getContentDao().executeCountNativeQuery("select count(*) from api_logevent  where event_status=200  	and event_time >                       ((now() - INTERVAL '1 hour')\\:\\:timestamp)");
					Integer api_err_event_h 		= getContentDao().executeCountNativeQuery("select count(*) from api_logevent  where event_status>=400    and event_status!=404 and event_time > ((now() - INTERVAL '1 hour')\\:\\:timestamp)");
					
					Integer api_soap_h  			= getContentDao().executeCountNativeQuery("select count(*) from api_soapevent where event_status=200  	and event_time >                       ((now() - INTERVAL '1 hour')\\:\\:timestamp)");
					Integer api_err_soap_h  		= getContentDao().executeCountNativeQuery("select count(*) from api_soapevent where event_status>=400    and event_status!=404 and event_time >      ((now() - INTERVAL '1 hour')\\:\\:timestamp)");
					
					Integer api_event_5m 			= getContentDao().executeCountNativeQuery("select count(*) from api_logevent  where event_status=200  	and event_time >((now() - INTERVAL '5 minute')\\:\\:timestamp)");
					Integer api_err_event_5m 		= getContentDao().executeCountNativeQuery("select count(*) from api_logevent  where event_status>=400    and event_status!=404	and event_time >((now() - INTERVAL '5 minute')\\:\\:timestamp)");
					
					Integer api_soap_5m  			= getContentDao().executeCountNativeQuery("select count(*) from api_soapevent where event_status=200  	and event_time >((now() - INTERVAL '5 minute')\\:\\:timestamp)");
					Integer api_err_soap_5m  		= getContentDao().executeCountNativeQuery("select count(*) from api_soapevent where event_status>=400    and event_status!=404	and event_time >((now() - INTERVAL '5 minute')\\:\\:timestamp)");
									
					Integer api_bounced_d 			= getContentDao().executeCountNativeQuery("select count(*) from api_logevent  where event_status=429   						  and event_time >((now() - INTERVAL '1 day')\\:\\:timestamp)");
					Integer api_bounced_h 			= getContentDao().executeCountNativeQuery("select count(*) from api_logevent  where event_status=429   						  and event_time >((now() - INTERVAL '1 hour')\\:\\:timestamp)");
					Integer api_bounced_5m 			= getContentDao().executeCountNativeQuery("select count(*) from api_logevent  where event_status=429   						  and event_time >((now() - INTERVAL '5 minute')\\:\\:timestamp)");
					
					data.add(new Tuple("Requests ok/error 400+ less 404  (5m 1hr 1d)", 	    
								NumberFormatter.formatNumber(api_event_5m, getSessionUser().getLocale()) + "/" + NumberFormatter.formatNumber(api_err_event_5m, getSessionUser().getLocale())     + "<span class=\"separator\">|</span>" +     
								NumberFormatter.formatNumber(api_event_h, getSessionUser().getLocale())  + "/" + NumberFormatter.formatNumber(api_err_event_h,  getSessionUser().getLocale())     + "<span class=\"separator\">|</span>" +
								NumberFormatter.formatNumber(api_event_d, getSessionUser().getLocale())  + "/" + NumberFormatter.formatNumber(api_err_event_d,  getSessionUser().getLocale()) + " <span class=\"ago\">reqs</span>" ));  	 
	
					data.add(new Tuple("SOAP Requests ok/400+ less 404 (5m 1hr 1d)", 	    
								NumberFormatter.formatNumber(api_soap_5m, getSessionUser().getLocale()) + "/" + NumberFormatter.formatNumber(api_err_soap_5m, getSessionUser().getLocale())    + "<span class=\"separator\">|</span>" +     
								NumberFormatter.formatNumber(api_soap_h, getSessionUser().getLocale())  + "/" + NumberFormatter.formatNumber(api_err_soap_h, getSessionUser().getLocale())     + "<span class=\"separator\">|</span>" +
								NumberFormatter.formatNumber(api_soap_d, getSessionUser().getLocale())  + "/" + NumberFormatter.formatNumber(api_err_soap_d,    getSessionUser().getLocale()) + " <span class=\"ago\">reqs</span>" ));  	 
					

					Integer soap_count_1m;
					Integer soap_count_5m;
					Integer soap_count_15m;
					
					soap_count_1m  = Integer.valueOf(0);
					soap_count_5m  =  Integer.valueOf(0);
					soap_count_15m =  Integer.valueOf(0);

					try {
						
						 soap_count_1m   = getContentDao().executeCountNativeQuery("select count(*) from api_soapevent  where event_time >(now() - INTERVAL '1 minute')\\:\\:timestamp");
						 soap_count_5m   = getContentDao().executeCountNativeQuery("select count(*) from api_soapevent  where event_time >(now() - INTERVAL '5 minute')\\:\\:timestamp");
						 soap_count_15m  = getContentDao().executeCountNativeQuery("select count(*) from api_soapevent  where event_time >(now() - INTERVAL '15 minute')\\:\\:timestamp");
						 
						Integer soap_mean_proc_1m    = soap_count_1m==0  ? 0 : getContentDao().executeCountNativeQuery("select sum(event_processing_time)/" + String.valueOf(soap_count_1m)  + " from api_soapevent  where event_time >(now() - INTERVAL '1 minute')\\:\\:timestamp");
						Integer soap_mean_proc_5m    = soap_count_5m==0  ? 0 : getContentDao().executeCountNativeQuery("select sum(event_processing_time)/" + String.valueOf(soap_count_5m)  + " from api_soapevent  where event_time >(now() - INTERVAL '5 minute')\\:\\:timestamp");
						Integer soap_mean_proc_15m   = soap_count_15m==0 ? 0 : getContentDao().executeCountNativeQuery("select sum(event_processing_time)/" + String.valueOf(soap_count_15m) + " from api_soapevent  where event_time >(now() - INTERVAL '15 minute')\\:\\:timestamp");
		
						String soap_mc1  = null;
						String soap_mc5  = null;
						String soap_mc15 = null;
						
						if(soap_mean_proc_1m < 1600) 			    soap_mc1 = "nofloat pad48 success";
						else if (soap_mean_proc_1m<3200) 			soap_mc1 = "nofloat pad48 warning";
						else										soap_mc1 = "nofloat pad48 danger";
										
						if(soap_mean_proc_5m < 1200) 				    soap_mc5 = "nofloat pad48 success";
						else if (soap_mean_proc_5m<3200) 			soap_mc5 = "nofloat pad48 warning";
						else									soap_mc5 = "nofloat pad48 danger";
						
						if(soap_mean_proc_15m < 1600) 				soap_mc15 = "nofloat pad48 success";
						else if (soap_mean_proc_15m<3200) 			soap_mc15 = "nofloat pad48 warning";
						else									soap_mc15 = "nofloat pad48 danger";
						
						data.add(new Tuple("SOAP API Request mean processing time (1m 5m 15m)", 							
								"<span class=\""+soap_mc1+"\">" +  NumberFormatter.formatNumber(soap_mean_proc_1m, getSessionUser().getLocale())    + "</span> <span class=\"separator\">|</span>" +     
								"<span class=\""+soap_mc5+"\">" +  NumberFormatter.formatNumber(soap_mean_proc_5m, getSessionUser().getLocale())    + "</span> <span class=\"separator\">|</span>" +
								"<span class=\""+soap_mc15+"\">" +  NumberFormatter.formatNumber(soap_mean_proc_15m, getSessionUser().getLocale())   + "</span> <span class=\"ago\">ms</span>"  ));
						
					} catch (Exception e) {
						logger.error(" {} | {} | {} | {} ", (getSessionUser()!=null?getSessionUser().getUserName():""), e.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName(), e.getMessage());
						data.add(new Tuple(e.getClass().getName(), e.getMessage()));
					}

					try {
						data.add(new Tuple("Traffic Controller (com.novamens.content.webapi.traffictokens)", 
								SystemParameters.get("com.novamens.content.webapi.traffictokens",  String.valueOf(TrafficControlService.DEFAULT_TOKENS))));
					
				} catch (Exception e) {
					logger.error(" {} | {} | {} | {} ", (getSessionUser()!=null?getSessionUser().getUserName():""), e.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName(), e.getMessage());
					data.add(new Tuple(e.getClass().getName(), e.getMessage()));
				}

					
					
					
					
					
					
					
					
					
					
					
					
					
					
					
					
					
					
					
					
					
					
					
					
					
					
					
					
					
					data.add(new Tuple("Requests bounced back by Traffic Controller 429 (5m 1hr 1d)", 	    
							NumberFormatter.formatNumber(api_bounced_5m, getSessionUser().getLocale())  + "<span class=\"separator\">|</span>" +     
							NumberFormatter.formatNumber(api_bounced_h, getSessionUser().getLocale())   + "<span class=\"separator\">|</span>" +
							NumberFormatter.formatNumber(api_bounced_d, getSessionUser().getLocale())   + "<span class=\"ago\">reqs</span>" ));
					
				} catch (Exception e) {
					logger.error(" {} | {} | {} | {} ", (getSessionUser()!=null?getSessionUser().getUserName():""), e.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName(), e.getMessage());
					data.add( new Tuple(e.getClass().getName(), e.getMessage()));
					 
				}
				
				
				try {
					Integer not_closed_errors = getContentDao().executeCountNativeQuery("select count(*) from api_logevent where event_status>=400 and event_status!=404 and not event_closed");
					data.add(new Tuple("400+ 500+ not closed errors", 	NumberFormatter.formatNumber(not_closed_errors)));
				} 
				catch (Exception e) {
					logger.error(" {} | {} | {} | {} ", (getSessionUser()!=null?getSessionUser().getUserName():""), e.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName(), e.getMessage());
					data.add( new Tuple(e.getClass().getName(), e.getMessage()));
				}

				long end = System.currentTimeMillis();
			
				if (logger.isDebugEnabled())
					data.add(new Tuple("Render time ", String.valueOf(end-start)+" <span class=\"ago\">ms</span>"));
				logger.debug("Render time " + String.valueOf(end-start)+"ms");
			}
			
		} catch (ContentMgmtException e) {
			data.add(new Tuple( "Error ",  	e.getClass().getName() + ". " + e.getMessage() +" <br /> Probably table/s that dont exists." ));
			logger.error(" {} | {} | {} | {} ", (getSessionUser()!=null?getSessionUser().getUserName():""), e.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName(), e.getMessage());
		
		} catch (Exception e) {
			data.add(new Tuple( "Error ",  	e.getClass().getName() + ". " + e.getMessage()));
			logger.error(" {} | {} | {} | {} ", (getSessionUser()!=null?getSessionUser().getUserName():""), e.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName(), e.getMessage());
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
	
}
