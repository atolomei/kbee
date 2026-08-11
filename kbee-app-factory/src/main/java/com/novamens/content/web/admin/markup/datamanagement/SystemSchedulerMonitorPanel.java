package com.novamens.content.web.admin.markup.datamanagement;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.Model;

import com.novamens.content.entity.Person;
import com.novamens.content.service.DomainService;
import com.novamens.content.user.UserService;
import com.novamens.content.web.admin.markup.ActionsPanel;
import com.novamens.content.web.admin.markup.XAjaxLink;
import com.novamens.content.web.admin.markup.XStdLink;
import com.novamens.content.web.sql.markup.SQLGatewayPage;
import com.novamens.datetime.DateTimeService;
import com.novamens.dom.Domain;
import com.novamens.event.EventService;
import com.novamens.kbee.command.CommandService;
import com.novamens.kbee.content.command.TestSchedulerLoadCommand;
import com.novamens.kbee.scheduler.EvictCronJobsListEvent;
import com.novamens.kbee.scheduler.KbeeSchedulerQueue;
import com.novamens.kbee.scheduler.TestServiceRequest;
import com.novamens.kbee.wicket.markup.html.areainfo.AreaInfoPanel;
import com.novamens.kbee.wicket.markup.html.areainfo.GridInfoPanel;
import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.scheduler.AbstractCronJobRequest;
import com.novamens.scheduler.SchedulerException;
import com.novamens.scheduler.SchedulerService;
import com.novamens.security.User;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.util.NumberFormatter;
import kbee.util.Tuple;
import kbee.web.datamanagement.SchedulerRequestPage;
import kbee.web.error.ApplicationErrorPage;
import kbee.web.nav.DataManagementBC;
import kbee.web.scheduler.SchedulerCronJobsPage;

			
public class SystemSchedulerMonitorPanel extends ModelPanel<Object> {
	
	private static final long serialVersionUID = 1L;

	final boolean is_root 			=  ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot(); 
	final boolean is_service_admin	=  (is_root || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SERVICE_ADMIN.getId()));
	final boolean is_factory_admin	=  (is_root || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_FACTORY_MANAGER.getId()));
	final boolean is_api			=  (is_root || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.API_DEVELOPER.getId()));
	final boolean is_domain_admin	=  (is_root || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId()));

	
	static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SystemDataManagementPanel.class.getName());

	public SystemSchedulerMonitorPanel(String id) {
		super(id);
	}
	
	
	/**
	 * System Information
	 * 
	 * Service Management
	 * -----------------
	 * Data Management
	 * Scheduler
	 * Commands
	 * File Server
	 * 
	 */
	
	public void onInitialize() {
	super.onInitialize();
		 
	setOutputMarkupId(true); 
	
	
	AreaInfoPanel area = new AreaInfoPanel("panel");
	area.addPanel(new GridInfoPanel("element",schedulerInfo(), new Model<String>("Status"), true));
	area.addPanel(new GridInfoPanel("element",schedulerQueueInfo(), new Model<String>("Queue"), true));
	area.addPanel(new GridInfoPanel("element",schedulerSetup(), new Model<String>("Set up parameters"), true));														
	area.addPanel(new GridInfoPanel("element",schedulerRequestsExecutingInfo(), new Model<String>("Executing Requests"), true));
	
	// area.addPanel(new GridInfoPanel("element", cronJobsUserInfo(), new Model<String>("CronJobs (User)"), true));
	// area.addPanel(new GridInfoPanel("element", cronJobsInfo(), new Model<String>("CronJobs (System)"), true));
	
				
	SchedulerService service = ServiceLocator.getService(SchedulerService.class);
	
	try {
					Map<String, List<String>> batchs = service.getRequestStatus();
					Map<String, String> batch_info = service.getBatchStatus();
					
					int ba=0;
					int MAX = 2000;
					int count=0;
					
					for (Entry<String, List<String>> entry: batchs.entrySet()) {
						
						if (count++>MAX)
							break;
						
						List<Tuple> data = new ArrayList<Tuple>();
						
						try {
								int n = 0;
								data.add(new Tuple (" Batch Status ", batch_info.get(entry.getKey()))); 
								for (String s :entry.getValue()) 
									data.add(new Tuple (" Request " + String.valueOf(++n), s )); 
							} catch (Exception e) {
								logger.error(e);
								data.add(new Tuple( "Error",  e.getClass().getName()));
							}
							area.addPanel(new GridInfoPanel("element", data, new Model<String>("Batch " +  String.valueOf(++ba) )));
					}
				} catch (Exception e) {
					logger.error(e);
	}
				

				
				
				area.setSections(AreaInfoPanel.ONE_SECTION);
				area.setCss("col-lg-12");
				
				ActionsPanel actions = new ActionsPanel("actions", new Model<String>("Actions"));


				/**
				XStdLink x0 = new XStdLink( new Model<String>("Execute Request")) {
					private static final long serialVersionUID = 1L;


					@Override
					public boolean isEnabled() {
						return is_service_admin || is_domain_admin;

					}
					
					@Override
					public void onClick() {
							
						
						try {
						
							setResponsePage(new SchedulerRequestPage());
							
						} 
						catch (Exception e) {
							logger.error(e);
							setResponsePage(new ErrorPage<Void>(e));				

						}
					}
				};
**/

				XStdLink cron = new XStdLink( new Model<String>("Cron jobs")) {
					private static final long serialVersionUID = 1L;


					@Override
					public boolean isEnabled() {
						return is_service_admin || is_domain_admin;

					}
					
					@Override
					public void onClick() {
						try {
						
							setResponsePage(new SchedulerCronJobsPage());
							
						} 
						catch (Exception e) {
							logger.error(e);
							setResponsePage(new ApplicationErrorPage<Void>(e));				

						}
					}
				};

				
				
				XStdLink x1 = new XStdLink( new Model<String>("Queue Standard")) {
					
					private static final long serialVersionUID = 1L;
					
					@Override
					public  boolean isVisible() {
							return is_service_admin || is_domain_admin;
					}

					@Override
					public void onClick() {
						 
						String query = "select id, time, priority, error_count, description, objectid, error_message, appserverid, hostname from scheduler where error_count<3 order by time desc";
						SQLGatewayPage page = new SQLGatewayPage(query);
						page.setTwoPanels(false);
						setResponsePage(page);					
					}
				};
				
				x1.setIsNewTab(true);

				XStdLink x2 = new XStdLink( new Model<String>("Queue Error")) {
					private static final long serialVersionUID = 1L;
					@Override
					public void onClick() {
						String query = "select id, time, priority, error_count, title, description, objectid,  error_message, appserverid, hostname from scheduler where error_count>2 order by time desc";
						SQLGatewayPage page = new SQLGatewayPage(query);
						page.setTwoPanels(false);
						setResponsePage(page);					
					}
					@Override
					public  boolean isVisible() {
							return is_service_admin || is_domain_admin;
					}

					
				};
				
				x2.setIsNewTab(true);
				
				XStdLink x3 = new XStdLink( new Model<String>("Queue All")) {
					private static final long serialVersionUID = 1L;
					@Override
					public void onClick() {
						String query = "select id, time, priority, error_count, title, description, objectid,  error_message, appserverid, hostname  from scheduler order by time desc";
						SQLGatewayPage page = new SQLGatewayPage(query);
						page.setTwoPanels(false);
						setResponsePage(page);					
					}
					@Override
					public  boolean isVisible() {
							return is_service_admin || is_domain_admin;
					}

				};
				
				XAjaxLink x4 = new XAjaxLink( new Model<String>("Empty Error Queue")) {
					private static final long serialVersionUID = 1L;
					@Override
					public boolean isEnabled() {
						SecurityService ss = ServiceLocator.getService(SecurityService.class);
						return ss.isRoot();
					}
					@Override
					public  boolean isVisible() {
							return is_service_admin || is_domain_admin;
					}

					
					@Override
					public void onClick(AjaxRequestTarget target) {
						String query = "delete from scheduler where error_count > 2";
						SQLGatewayPage page = new SQLGatewayPage(query);
						page.setTwoPanels(false);
						setResponsePage(page);					
					}
				};
				

				XStdLink x5 = new XStdLink( new Model<String>("Restart Scheduler")) {
					private static final long serialVersionUID = 1L;
					@Override
					public boolean isEnabled() {
						return is_service_admin || is_domain_admin;

					}
					
					@Override
					public void onClick() {
							
						
						try {
							
							restartScheduler();
							setResponsePage(new SystemSchedulerMonitorPage());
							
						} 
						catch (Exception e) {
							logger.error(e);
							setResponsePage(new ApplicationErrorPage<Void>(e));				

						}
					}
				};


				XStdLink test_1 = new XStdLink( new Model<String>("Send TestRequest")) {
					private static final long serialVersionUID = 1L;

					@Override
					public boolean isEnabled() {
						return true;

					}
					
					@Override
					public void onClick() {
						try {
							getDomain().getService(DomainService.class).enqueueRequest(new TestServiceRequest());
							Thread.sleep(1000);
							setResponsePage(new SystemSchedulerMonitorPage());
						} 
						catch (Exception e) {
							logger.error(e);
							setResponsePage(new ApplicationErrorPage<Void>(e));				

						}
					}
				};


				XStdLink test_2 = new XStdLink( new Model<String>("Test heavy load (5m)")) {
					private static final long serialVersionUID = 1L;

					@Override
					public boolean isEnabled() {
						return is_root;

					}
					
					@Override
					public void onClick() {
						try {
							TestSchedulerLoadCommand command = new TestSchedulerLoadCommand();
							ServiceLocator.getService(CommandService.class).add(command);
							Thread.sleep(10000);
							setResponsePage(new SystemSchedulerMonitorPage());
							
						} 
						catch (Exception e) {
							logger.error(e);
							setResponsePage(new ApplicationErrorPage<Void>(e));				

						}
					}
				};

				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				/**
				XStdLink x5 = new XStdLink( new Model<String>("Restart Scheduler")) {
					private static final long serialVersionUID = 1L;


					@Override
					public boolean isEnabled() {
						return is_service_admin || is_domain_admin;

					}
					
					@Override
					public void onClick() {
							
						SchedulerService service = ServiceLocator.getService(SchedulerService.class);
						try {
							
							logger.debug("Restarting");
							service.restart();
							logger.debug("done");
							setResponsePage(new SystemSchedulerMonitorPage());
							
						} 
						catch (SchedulerException e) {
							logger.error(e);
							setResponsePage(new ErrorPage<Void>(e));				

						}
					}
				};

						
				XStdLink x6 = new XStdLink( new Model<String>("Restart Scheduler - Force Stop All")) {
					private static final long serialVersionUID = 1L;

					@Override
					public boolean isEnabled() {
						return is_service_admin || is_domain_admin;

					}

					
					@Override
					public void onClick() {
						SchedulerService service = ServiceLocator.getService(SchedulerService.class);
						try {
							logger.debug("Restarting");
							service.restart(true);
							logger.debug("done");
							
							setResponsePage(new SystemSchedulerMonitorPage());
							
							
						} catch (SchedulerException e) {
							logger.error(e);
							setResponsePage(new ErrorPage<Void>(e));				

						}
					
					}
				};
				*/
							
				XStdLink x7 = new XStdLink( new Model<String>("Reload DB CronJobs")) {
					private static final long serialVersionUID = 1L;
					
					@Override
					public boolean isEnabled() {
						return is_service_admin || is_domain_admin;
					}


					
					@Override
					public void onClick() {
						EventService service = ServiceLocator.getService(EventService.class);
						try {
							logger.debug("Reloading CronJobs from Database");
							service.fire(new EvictCronJobsListEvent());
							logger.debug("done");
							
							Thread.sleep(2000);
							setResponsePage(new SystemSchedulerMonitorPage());
						} 
						catch (Exception e) {
							logger.error(e);
							setResponsePage(new ApplicationErrorPage<Void>(e));				
						}
					}
				};

				
				x3.setIsNewTab(true);

				actions.add(cron);
				actions.add(x1);
				actions.add(x2);
				actions.add(x3);
				actions.add(x4);
				actions.add(x5);
				actions.add(x7);
				
				actions.add(test_1);
				actions.add(test_2);
				
				area.setActionsPanel(actions);
				
				add(area);
	}
		

	private List<Tuple> schedulerRequestsExecutingInfo() {
		
		long start = System.currentTimeMillis();
		
		List<Tuple> data = new ArrayList<Tuple>();
		
		SchedulerService service = ServiceLocator.getService(SchedulerService.class);

		try {

			Map<String, String> map = service.getRunningRequestsStr();
			
			for (Entry<String, String> e: map.entrySet()) {
				data.add(new Tuple( e.getKey(), (e.getValue()!=null ? e.getValue().replace("\n", "<br />"):"null")));
			}

			data.sort(new Comparator<Tuple>() {
				@Override
				public int compare(Tuple arg0, Tuple arg1) {
					try {
						return arg0.label.compareToIgnoreCase(arg1.label);
					} catch (Exception e) {
						return 0;
					}
				}
			});
			
		} 
		catch (Exception e) {
			data.add(new Tuple("Scheduler Queue error. ",  e.getClass().getName()));
			logger.error(e);
		}

		finally {
			long end=System.currentTimeMillis();
			data.add(new Tuple("Render time ", String.valueOf(end-start)+" <span class=\"ago\">ms</span>"));
		}
				
		return data;
	}

	@Override
	public void onDetach() {
		super.onDetach();
	}
	
	
	protected Person getPerson() {
		return ServiceLocator.getService(UserService.class).getSessionUserProfile().getPerson();
	}


	
	private List<Tuple> schedulerQueueInfo() {

		List<Tuple> data = new ArrayList<Tuple>();
			
		SchedulerService service = ServiceLocator.getService(SchedulerService.class);

		try {
			
			KbeeSchedulerQueue<?> qu = (KbeeSchedulerQueue<?>) service.getQueue();
												
			String hp_in = NumberFormatter.formatNumber(qu.getCounterInHP().getCount()).trim();
			String hp_out = NumberFormatter.formatNumber(qu.getCounterOutHP().getCount()).trim();
			data.add(new Tuple("Queue HP total in/out", hp_in + " | " + hp_out));

			String lp_in = NumberFormatter.formatNumber(qu.getCounterInLP().getCount()).trim();
			String lp_out = NumberFormatter.formatNumber(qu.getCounterOutLP().getCount()).trim();
			data.add(new Tuple("Queue LP total in/out", lp_in + " | " + lp_out));
			
			String hp_m_in = NumberFormatter.formatNumber(qu.getMeterOutHP().getMeanRate()).trim();
			String lp_m_in = NumberFormatter.formatNumber(qu.getMeterOutLP().getMeanRate()).trim();
			data.add(new Tuple("Queue HP out rate (req/sec)", hp_m_in));
			data.add(new Tuple("Queue LP out rate (req/sec)", lp_m_in));
			
		} 
		catch (Exception e) {
			data.add(new Tuple("Scheduler Queue error. ",  e.getClass().getName()));
			logger.error(e);
		}
				
		return data;
	}

	
	/**
	 * @return
	 */
	private List<Tuple> schedulerInfo() {

		List<Tuple> data = new ArrayList<Tuple>();
			
		SchedulerService service = ServiceLocator.getService(SchedulerService.class);

		try {
			data.add(new Tuple("Started", ServiceLocator.getService(DateTimeService.class).timeElapsed(service.getStartDateTime())));
			}
		catch (Exception e) {
			data.add(new Tuple("Started ",  e.getClass().getName()+ " | " + e.getMessage()));
			logger.error(e);
		}
		

		try {
			data.add(new Tuple("Restarted", ServiceLocator.getService(DateTimeService.class).timeElapsed(service.lastRestart())));
			}
		catch (Exception e) {
			data.add(new Tuple("Restarted ",  e.getClass().getName()+ " | " + e.getMessage()));
			logger.error(e);
		}

		
		try {
			String pg=service.getStatus();
			if (pg==null)
				 pg="err";
			boolean isok = pg.toLowerCase().equals("ok");
			String s="<span class= \" "+ (isok?"success":"danger") +"\" />"+pg+"</span>";
			data.add(new Tuple("Scheduler status ", s));
		} 
		catch (Exception e) {
			data.add(new Tuple("Scheduler status ",  e.getClass().getName()));
			logger.error(e);
		}

		
		
		try {
			data.add(new Tuple( "Scheduler. Queue in db (Std Err)",  			NumberFormatter.formatNumber(service.getQueueSize()) + "<span class=\"separator\">|</span>" + String.valueOf(service.getErrorQueueSize())));
			data.add(new Tuple("Scheduler. Total items (Batches | Requests)", 	NumberFormatter.formatNumber(service.getTotalBatches()) + "<span class=\"separator\">|</span>" +String.valueOf(service.getTotalInBatches()))); 						// total batches in the system
		} 
		catch (SchedulerException e) {
			data.add(new Tuple( "Scheduler. Queue Std Size", e.getClass().getName()));
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
																						
			String rate_hp = 	"<div class=\""+ cs_hp +"\"> <b>&nbsp;1m.&nbsp;</b>  " + v1a + "<span class=\"internal-separator\">/</span>" + v1b +  "<span class=\"separator\">|</span></div>" +
								"<div class=\""+ cs_hp +"\"> <b>&nbsp;5m.&nbsp;</b>  " + v2a + "<span class=\"internal-separator\">/</span>" + v2b + "<span class=\"separator\">|</span></div>" + 
								"<div class=\""+ cs_hp +"\"> <b>15m.&nbsp;</b> "       + v3a + "<span class=\"internal-separator\">/</span>" + v3b + "</div>";
					
			String vl1a = NumberFormatter.formatNumber( service.getOneMinuteInputRateLp()).trim();
			String vl1b = NumberFormatter.formatNumber( service.getOneMinuteThroughPutLP()).trim();
			
			String vl2a = NumberFormatter.formatNumber( service.getFiveMinuteInputRateLp()).trim();
			String vl2b = NumberFormatter.formatNumber(service.getFiveMinuteThroughPutLP()).trim();
			
			String vl3a = NumberFormatter.formatNumber(service.getFifteenMinuteInputRateLp()).trim();
			String vl3b = NumberFormatter.formatNumber(service.getFifteenMinuteThroughPutLP()).trim();
						
			String rate_lp = "<div class=\""+ cs_lp +"\"> <b>&nbsp;1m.&nbsp;</b> " + vl1a + "<span class=\"internal-separator\">/</span>" + vl1b + "<span class=\"separator\">|</span></div>" +
							 "<div class=\""+ cs_lp +"\"> <b>&nbsp;5m.&nbsp;</b> " + vl2a + "<span class=\"internal-separator\">/</span>" + vl2b + "<span class=\"separator\">|</span></div>" + 
							 "<div class=\""+ cs_lp +"\"> <b>15m.&nbsp;</b>"       + vl3a + "<span class=\"internal-separator\">/</span>" + vl3b + "</div>";
			
			data.add(new Tuple( "Scheduler HP I/O req/sec (1m 5m 15m) ",  rate_hp));
			data.add(new Tuple( "Scheduler LP I/O req/sec (1m 5m 15m) ",  rate_lp));
			
			String mean_rate_i_hp = NumberFormatter.formatNumber(service.getMeanHPIn(),  getSessionUser().getLocale())  +  " <span class=\"atright ago\">req/seg</span>";  
			String mean_rate_i_lp = NumberFormatter.formatNumber(service.getMeanLPIn(),  getSessionUser().getLocale())  +  " <span class=\"atright ago\">req/seg</span>";
			String mean_rate_o_hp = NumberFormatter.formatNumber(service.getMeanHPOut(), getSessionUser().getLocale())  +  " <span class=\"atright ago\">req/seg</span>";
			String mean_rate_o_lp = NumberFormatter.formatNumber(service.getMeanLPOut(), getSessionUser().getLocale())  +  " <span class=\"atright ago\">req/seg</span>";
			 
			data.add(new Tuple( "Scheduler In mean rate ",  "<b>High Priority</b>.&nbsp;" + mean_rate_i_hp + " <span class=\"separator\">| </span>" +" <b>Low Priority</b>.&nbsp;" + mean_rate_i_lp));
			data.add(new Tuple( "Scheduler Out mean rate ",  "<b>High Priority</b>.&nbsp;" + mean_rate_o_hp + " <span class=\"separator\">| </span>" +" <b>Low Priority</b>.&nbsp;" + mean_rate_o_lp));
		 
					
			data.add(new Tuple("Total Requests in Batches ", String.valueOf(service.getTotalInBatches())));
			data.add(new Tuple("Total Batches ", String.valueOf(service.getTotalBatches())));
			
			
			// Dispatcher Status -------------
			//
			try {
				Map<String, String> dis = service.getDispatchersStatus();
				for (Entry<String, String> entry: dis.entrySet()) {
					data.add(new Tuple ("Dispatcher " + entry.getKey(), entry.getValue()));
				}
			} 
			catch (Exception e) {
				data.add(new Tuple("Disptacher ",  e.getClass().getName()));
			}

			
			
		} 
		catch (Exception e) {
			data.add(new Tuple("Scheduler Engine status. ",  e.getClass().getName()));
			logger.error(e);
		}
		
		/**
		try {
			Map<String, String> dis = service.getConfigurableParameters();
			for (Entry<String, String> entry: dis.entrySet()) {
				data.add(new Tuple (entry.getKey(), entry.getValue()));
			}
		} 
		catch (Exception e) {
			data.add(new Tuple("Configurable Parameters ",  e.getClass().getName()));
		}
		**/
		
		return data;
	}


	
	
	/**
	 * @return
	 */
	private List<Tuple> schedulerSetup() {

		List<Tuple> data = new ArrayList<Tuple>();
			
		SchedulerService service = ServiceLocator.getService(SchedulerService.class);

		/**
			try {
				Map<String, String> dis = service.getDispatchersStatus();
				for (Entry<String, String> entry: dis.entrySet()) {
					data.add(new Tuple ("Dispatcher " + entry.getKey(), entry.getValue()));
				}
			} 
			catch (Exception e) {
				data.add(new Tuple("Disptacher ",  e.getClass().getName()));
			}
**/
		
		try {
			Map<String, String> dis = service.getConfigurableParameters();
			for (Entry<String, String> entry: dis.entrySet()) {
				data.add(new Tuple (entry.getKey(), entry.getValue()));
			}
		} 
		catch (Exception e) {
			data.add(new Tuple("Configurable Parameters ",  e.getClass().getName()));
		}
		
		return data;
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	private List<Tuple> cronJobsInfo() {
		List<Tuple> data = cronJobsSystem();
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

		return data;
	}
	
	private List<Tuple> cronJobsUserInfo() {
		
		List<Tuple> data = cronJobsUser();
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
		return data;
	}

	/**
	 * @return
	 */
	private List<Tuple> cronJobsSystem() {
		List<Tuple> data = new ArrayList<Tuple>();
		try {
			for (AbstractCronJobRequest job : getCronJobs())
				if (!job.isUserRequest())
					
					
					data.add(new Tuple (job.getClass().getSimpleName(), job.getCronExpression().toHTMLString("predicate") + " " + ZoneId.systemDefault().getId().toString()+" " + 
							(job.getDescription()!=null?("<br />" + job.getDescription()):"") + 
							(job.getParameters()!=null?("  <br />" + job.getParameters().toString()):"") +
							(job.isEnabled()?"<br /> Enabled: True": "Enabled: False") +
							(job.getParameters()!=null?("<br />" + job.getDescription()):"") +
							"<br />" +job.getClass().getName()
							));
		}
		catch (Exception e) {
			logger.error(e);
			data.add(new Tuple( "Error",  e.getClass().getName()));
		}
		return data;
	}

	
	private List<Tuple> cronJobsUser() {
		List<Tuple> data = new ArrayList<Tuple>();
		try {
			for (AbstractCronJobRequest job : getCronJobs())
				if (job.isUserRequest()) {

					ZonedDateTime zd = job.getCronExpression().nextTimeAfter(ZonedDateTime.now( ZoneId.of(job.getTimeZone())));
					ZoneId userZoneId = ZoneId.of(getSessionUser().getTimeZone());
			        ZonedDateTime userDateTime = zd.withZoneSameInstant(userZoneId);
					String user_nextda=ServiceLocator.getService(DateTimeService.class).format(userDateTime);
					
					
					data.add(new Tuple (job.getClass().getSimpleName(), 
							(job.getName()!=null?(job.getName()):"")
							+ "  <br /> Expression -> " + job.getCronExpression().toHTMLString("predicate")+ "  " + job.getTimeZone()+" "  
							+ (" <br />  Next (in user's TimeZone) -> " + user_nextda )
							+ (job.getDescription()!=null?("  <br /> Description -> " + job.getDescription()):"")
							+ (job.getParameters()!=null?("  <br /> Parameters -> " + job.getParameters().toString()):"")
							+ (job.getDomainId()!=null?("  <br />" + "Domain id -> " + String.valueOf(job.getDomainId())):"")
							+ (job.isEnabled()?"<br /> Enabled -> True": "Enabled -> False")
							+ "<br />" +job.getClass().getName()
							));
				}
		} 
		catch (Exception e) {
			logger.error(e);
			data.add(new Tuple( "Error",  e.getClass().getName()));
		}
		return data;
	}
	
	
	protected void restartScheduler() {
		try {
			ServiceLocator.getService(SchedulerService.class).restart();
			
		} catch (Exception e) {
			logger.error(e);
		}
	}
			

	
	protected User getSessionUser() {
		try {
			return ServiceLocator.getService(SecurityService.class).getSessionUser();
		} 
		catch (Exception e) {
			return null;
		}
	}
	
	protected Domain getDomain() {
        return (Domain) ServiceLocator.getService(UserService.class).getDomain();
    }
	
	
	
	private final List<AbstractCronJobRequest> getCronJobs() {
		SchedulerService service = ServiceLocator.getService(SchedulerService.class);
		return service.getCronJobs();
	}
}
