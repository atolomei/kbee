package kbee.web.command.panel;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.command.Command;
import com.novamens.content.command.CommandState;
import com.novamens.content.entity.Person;
import com.novamens.content.user.UserService;
import com.novamens.datetime.DateTimeService;
import com.novamens.kbee.command.CommandService;
import com.novamens.kbee.content.command.ReclassifyContentCommand;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;


public class CommandStatusPanel extends Panel {

	private static final long serialVersionUID = 8249609983412219559L;

    private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(CommandStatusPanel.class.getName());
    									
	private IModel<Command> model;

	private AbstractAjaxTimerBehavior timer;

	private CommandService service = null;

	final boolean is_root		     = ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot();
	
	 
	public CommandStatusPanel(String id, IModel<Command> model) {
		super(id);
		setModel(model);
		setOutputMarkupId(true);
	
	}
	
	private Boolean is_domain_kbee = null;
	protected boolean isDomainKbee() {
		if (this.is_domain_kbee == null) {
			try {
				this.is_domain_kbee = Boolean.valueOf(
						getPerson().getDomain().getName().toLowerCase().trim().equals("kbee"));
			} catch (Exception e) {
				logger.error(e);
				this.is_domain_kbee = Boolean.valueOf(false);
			}
		}
		return this.is_domain_kbee.booleanValue();
	}

	protected Person getPerson() {
		return ServiceLocator.getService(UserService.class).getSessionUserProfile().getPerson();
	}
	
	public void onInitialize() {
		super.onInitialize();
		add(getStatusPanel());	
	}

	/**  
     *
	 * BatchCommandEditorPanel (el editor tiene tener una serie de metodos) 
	 * BatchCommandStatusPanel (el status panel tiene que tener una serie de metodos)
	 *  
	 * @return
	 */
	private WebMarkupContainer getStatusPanel() {

		
		// ----------------------------------- Status --------------------------------------------
		//	
		WebMarkupContainer canvas = new WebMarkupContainer("canvas") {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isVisible() {
				return true;
			}
		};
		
		add(canvas);
		canvas.setEscapeModelStrings(false);
		canvas.setOutputMarkupId(true);
		
		
		WebMarkupContainer status_panel = new WebMarkupContainer("status-panel") {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isVisible() {
				return true;
			}
		};
		
		canvas.add(status_panel);
		
		WebMarkupContainer wstatus = new WebMarkupContainer("status-container") {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isVisible() {
				return true;
			}
		};
		
		status_panel.add(wstatus);
		
		Label status = new Label("status", new Model<String>("status") {
			private static final long serialVersionUID = 1L;
			@Override
			public String getObject() {
				try {
					Command cmd = getCommand();
					String css = cmd.getState().getCss();
					StringBuilder str = new StringBuilder();
					str.append("<span class=\"" + css + "\">");
					str.append(cmd.getState().getLabel());
					str.append("</span>");
					return str.toString();
				} catch (Exception e) {
					logger.error(e);
					return e.getClass().getName();
				}
			}
		});

		status.setEscapeModelStrings(false);
		wstatus.add(status);
		status.setOutputMarkupId(true);

		
		WebMarkupContainer wth = new WebMarkupContainer("threads-container") {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isVisible() {
				return isDomainKbee() || is_root;
			}
		};
		
		status_panel.add(wth);
		
		Label th = new Label("threads", new Model<String>("threads") {
			private static final long serialVersionUID = 1L;
			@Override
			public String getObject() {
				try {
					Command cmd = getCommand();
					StringBuilder str = new StringBuilder();
					str.append(String.valueOf(cmd.getThreads()));
					return str.toString();
				} catch (RuntimeException e) {
					logger.error(e);
					return e.getClass().getName();
				}
			}
		});

		th.setEscapeModelStrings(false);
		wth.add(th);
		th.setOutputMarkupId(true);

		
		
		 
		
		WebMarkupContainer wdesc = new WebMarkupContainer("desc-container") {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isVisible() {
				return true;
			}
		};
		

		Label desc = new Label("desc", new Model<String>() {
			private static final long serialVersionUID = 1L;
			@Override
			public String getObject() {
				try {
					Command cmd = getCommand();
					String des = cmd.getDescription();
					return des;
				} catch (RuntimeException e) {
					logger.error(e);
					return e.getClass().getName();
				}
			}
		});

		wdesc.add(desc);
		desc.setEscapeModelStrings(false);
		status_panel.add(wdesc);
		
							
		WebMarkupContainer wparan = new WebMarkupContainer("param-container") {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isVisible() {
				return isDomainKbee() || is_root;
			}
		};
		
		
		Label param = new Label("param", new Model<String>() {
			private static final long serialVersionUID = 1L;
			@Override
			public String getObject() {
				try {
					Command cmd = getCommand();
					Map<String, Object> map = cmd.getParameters();
					StringBuilder str = new StringBuilder();
					for(Entry<String, Object> entry: map.entrySet()) {
						String lab = entry.getKey().toString();
						String val = entry.getValue().toString();
						if (str.length()>0)
							str.append(", ");
						str.append(lab+": ");
						str.append(val);
					}
					return str.toString();
					
				} catch (RuntimeException e) {
					logger.error(e);
					return e.getClass().getName();
				}
			}
		});

		
		wparan.add(param);
		param.setEscapeModelStrings(false);
		status_panel.add(wparan);
		
		
		Label startdate = new Label("date-start", new Model<String>("date-start") {
			private static final long serialVersionUID = 1L;
			@Override
			public String getObject() {
				try {
					Command cmd = getCommand();
					return ServiceLocator.getService(DateTimeService.class).timeElapsed(cmd.getDateStarted());
				}  catch (Exception e) {
					logger.error(e);
					return e.getClass().getName();
				}
			}
		});

		startdate.setEscapeModelStrings(false);
		
		Label enddate = new Label("date-end",  new Model<String>("date-end") {
			private static final long serialVersionUID = 1L;
			@Override
			public String getObject() {
				try {
				Command cmd = getCommand();
				OffsetDateTime date = cmd.getDateTerminated();
				User user = getUser();
					if (date!=null && user!=null) {
						DateTimeService service = ServiceLocator.getService(DateTimeService.class);
						String zid = service.getMapZoneIds().get(user.getTimeZone());
						if (zid==null)
							zid=ZoneId.systemDefault().getId();
						return service.timeElapsed(date, ZoneId.of(zid), getLocale(), DateTimeService.DATE_COLlOQUIAL_AGO, "ago");
					}
				return "n/a";
				}  catch (RuntimeException e) {
					logger.error(e);
					return e.getClass().getName();
				}

			}
		});
		enddate.setEscapeModelStrings(false);
		
		Label result = new Label("result",  new Model<String>("result") {
			private static final long serialVersionUID = 1L;
			@Override
			public String getObject() {
				try {
				Command cmd = getCommand();
				return cmd.getResult();
				}  catch (RuntimeException e) {
					logger.error(e);
					return e.getClass().getName();
				}

			}
		});
		
		Label result_details = new Label("result-details",  new Model<String>("result_details") {
			private static final long serialVersionUID = 1L;
			@Override
			public String getObject() {
				try {	
				Command cmd = getCommand();

				return cmd.getResultDetails();
				}  catch (RuntimeException e) {
					logger.error(e);
					return e.getClass().getName();
				}

			}
		});
		
		result_details.setEscapeModelStrings(false);
		
		Label resultcomments = new Label("resultcomments",  new Model<String>("resultcomments") {
			private static final long serialVersionUID = 1L;
			@Override
			public String getObject() {
				try {
				Command cmd = getCommand();
				return cmd.getResultComment();
				}  catch (RuntimeException e) {
					logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
					return e.getClass().getName();
				}

			}
		});
		
		Label progress = new Label("progress",  new Model<String>("progress") {
			private static final long serialVersionUID = 1L;
			@Override
			public String getObject() {
				try {
				String css=null;
				return String.format(css!=null?"<span class=\""+ css+"\"> %3d ":"%3d ", getCommand().getProgress())+" %</span>";
				}  catch (RuntimeException e) {
					logger.error(e);
					return e.getClass().getName();
				}
			}
		});

		progress.setEscapeModelStrings(false);
		
		Label etc = new Label("etc",  new Model<String>("etc") {
			private static final long serialVersionUID = 1L;
			@Override
			public String getObject() {
				try {
				return getEstimatedTimeComplete();
				}  catch (RuntimeException e) {
					logger.error(e);
					return e.getClass().getName();
				}

			}
		});

		etc.setEscapeModelStrings(false);

		
		Label duration = new Label("duration",  new Model<String>("duration") {
			private static final long serialVersionUID = 1L;
			@Override
			public String getObject() {
				try {

					Command cmd = getCommand();
					DateTimeService service = ServiceLocator.getService(DateTimeService.class);
					String tst = service.formatLapseSeconds(cmd.getDuration(), getUser().getLocale(), "ago");
					return tst;
				
				}  catch (RuntimeException e) {
					logger.error(e);
					return e.getClass().getName();
				}

			}
		});

		duration.setEscapeModelStrings(false);

		
		WebMarkupContainer startdate_container = new WebMarkupContainer("date-start-container") {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isVisible() { 
				try {
					return getCommand().getState()!=CommandState.NOT_STARTED;
				}  catch (RuntimeException e) {
					logger.error(e);
					return false;
				}

			}
		};

		startdate_container.add(startdate);
		status_panel.add(startdate_container);

		WebMarkupContainer  progress_container = new WebMarkupContainer("progress-container") {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isVisible() {
				try {
				return getCommand().getState()!=CommandState.UNKNOWN;
				}  catch (RuntimeException e) {
					logger.error(e);
					return false;
				}

			}
		};

		progress_container.add(progress);
		status_panel.add(progress_container);
		
		
		WebMarkupContainer  etc_container = new WebMarkupContainer("etc-container") {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isVisible() {
				try {
				return !getCommand().isTerminated();
				}  catch (RuntimeException e) {
					logger.error(e);
					return false;
				}


			}
		};
		etc_container.add(etc);
		status_panel.add(etc_container);

 									
		WebMarkupContainer  statusinfo_container = new WebMarkupContainer("statusinfo-container") {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isVisible() {
				try { 
				return getCommand().getState()!=CommandState.UNKNOWN;
				}  catch (RuntimeException e) {
					logger.error(e);
					return false;
				}


			}
		};

		
		Label statusinfo = new Label("statusinfo", new Model<String>() {
			private static final long serialVersionUID = 1L;
			@Override
					public String getObject() {
						try {
							return getCommand().getStatusInfo();
						}  catch (Exception e) {
							logger.error(e);
							return e.getClass().getName();
						}
					}
			});

		statusinfo.setEscapeModelStrings(false);
		statusinfo_container.add(statusinfo);
		status_panel.add(statusinfo_container);
		
		// ----------------------------------- Terminated --------------------------------------------
		//
		//
		WebMarkupContainer results_cont = new WebMarkupContainer("command-result-panel") {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isVisible() {
				try {
				return getCommand().isTerminated();
				}  catch (RuntimeException e) {
					logger.error(e);
					return false;
				}


			}
		};
		
		canvas.add(results_cont);
		results_cont.setOutputMarkupId(true);
		
		WebMarkupContainer enddate_container = new WebMarkupContainer("date-end-container") {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isVisible() {
				return true;
			}
		};
		
		enddate_container.add(enddate);
		results_cont.add(enddate_container);

		WebMarkupContainer result_container = new WebMarkupContainer("result-container") {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isVisible() {
				return true;
			}
		};
		result_container.add(result);
		results_cont.add(result_container);

		WebMarkupContainer result_details_container = new WebMarkupContainer("result-details-container") {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isVisible() {
				return true;
			}
		};
		result_details_container.add(result_details);
		results_cont.add(result_details_container);

		WebMarkupContainer resultcomments_container = new WebMarkupContainer("resultcomments-container") {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isVisible() {
				try {
				CommandState state = getCommand().getState();
				
				return !(state==CommandState.NOT_STARTED 	|| 
						state==CommandState.PAUSED 		||
								state==CommandState.UNKNOWN 		||
										state==CommandState.RUNNING &&  getModel().getObject().getResultComment().length()>0);
				}  catch (RuntimeException e) {
					logger.error(e);
					return false;
				}

			}
		};
		
		resultcomments_container.add(resultcomments);
		results_cont.add(resultcomments_container);
		
		WebMarkupContainer duration_container = new WebMarkupContainer("duration-container") {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isVisible() {
				try {
				CommandState state = getCommand().getState();
				return state!=CommandState.NOT_STARTED;
				}  catch (RuntimeException e) {
					logger.error(e);
					return false;
				}
			}
		};
		
		duration_container.add(duration);
		results_cont.add(duration_container);
		
		// ----------------------------------- Terminated --------------------------------------------
		//	
		//
		//
		WebMarkupContainer stop_container = new WebMarkupContainer("stop-container") {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isVisible() {
				try {
				return !getModel().getObject().isTerminated();
				}  catch (RuntimeException e) {
					logger.error(e);
					return false;
				}

			}
		};
		
		status_panel.add(stop_container);
		
		stop_container.add(new AjaxLink<Void>("stop-link") {
			private static final long serialVersionUID = 1L;
			public void onClick(AjaxRequestTarget target) {
				try {
					getCommand().stop();
					getTimer().stop(target);
					try {
						Thread.sleep(600);
					} catch (InterruptedException e) {
						logger.error(e);
					}
					target.add(CommandStatusPanel.this.get("canvas"));
				}  catch (RuntimeException e) {
					logger.error(e);
				}
			}
			
			@Override
			public boolean isVisible() {
				try {
					CommandState state = getCommand().getState();
					return state==CommandState.NOT_STARTED || state==CommandState.RUNNING;
				}  catch (RuntimeException e) {
					logger.error(e);
					return false;
				}

			}
		});
		
		timer = new AbstractAjaxTimerBehavior(Duration.of(1,   ChronoUnit.SECONDS)) {
			private static final long serialVersionUID = 1L;
			@Override
	        protected void onTimer(AjaxRequestTarget target) {
				try {
					if (getCommand().isTerminated()) {
						this.stop(target);
					}
					target.add(CommandStatusPanel.this.get("canvas"));
				}  catch (RuntimeException e) {
					logger.error(e);
				}

	        }
	    };
	    
	    status_panel.add(timer);
	    status_panel.setOutputMarkupId(true);
		
	    return canvas;
	}


	@Override
	public void onDetach() {
		getModel().detach();
		service=null;
		super.onDetach();
	}
	

	public IModel<Command> getModel() {
		return model;
	}


	public void setModel(IModel<Command> model) {
		this.model=model;
	}

	/** 
	 * @return
	 */
	protected AbstractAjaxTimerBehavior getTimer() {
		return timer;
	}
	

	protected KbeeUser getUser() {
		return (KbeeUser)ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	
	
	protected String getEstimatedTimeComplete() {
			
		Command cmd = getCommandService().getCommand((Long) getModel().getObject().getId());
		Double value =  Double.valueOf(cmd.estimatedSecsToEnd() * 1000.0);
			Long lv = value.longValue();
			if (lv<0)
				return "N/A";
			
			DateTimeService service = ServiceLocator.getService(DateTimeService.class);
			return service.formatLapseSeconds(lv, getUser().getLocale(), "ago");
			
	}


	private CommandService getCommandService() { 
		if (service==null) { 
			try {
				service = (CommandService) ServiceLocator.getService(CommandService.class);
			} catch (RuntimeException e) {
				logger.error(e);
				return null;
			}
		}
		return service;
	}
	
	private Command getCommand() {
		return getCommandService().getCommand( (Long) getModel().getObject().getId());
	}
	
	
}
