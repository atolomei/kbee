package com.novamens.content.web.command.batch.markup;

import java.io.File;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;

import com.novamens.content.command.Command;
import com.novamens.content.command.CommandState;
import com.novamens.datetime.DateTimeService;
import com.novamens.kbee.command.CommandService;
import com.novamens.kbee.content.command.AbstractCommand;
import com.novamens.kbee.wicket.markup.html.ajax.WorkingAjaxLink;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

import kbee.web.resource.WebFileReference;

@SuppressWarnings("serial")
public class BatchCommandStatusPanel extends Panel {
	private static final long serialVersionUID = 1L;

	private AbstractAjaxTimerBehavior timer;

	private long command_id;
	private List<Long> command_list;
	
	static private Logger logger = LogManager.getLogger(BatchCommandStatusPanel.class.getName());

	private boolean isExecuting  = false;
	private boolean isFinished   = false;
	private boolean isStopButton = true;
	

	
	public BatchCommandStatusPanel(String id, List<Long> command_list) {
		this(id,  0, command_list, true);
}

	
	public BatchCommandStatusPanel(String id, long command_id) {
			this(id, command_id, null, true);
			
	}
	
	public BatchCommandStatusPanel(String id, long command_id,  boolean stopButtonEnabled) {
		this(id, command_id, null, stopButtonEnabled);
	}
	
	
	public BatchCommandStatusPanel(String id, long command_id, List<Long> list, boolean stopButtonEnabled) {
		super(id);
		setCommandId(command_id);
		setIsExecuting(true);
		setOutputMarkupId(true);
		this.command_list=list;
		isStopButton = stopButtonEnabled;
		load();
	}

	
	public void  setCommandId(long cmd) {
		this.command_id=cmd;
	}

	
	public long  getCommandId() {
		return command_id;   	
	}

	
	protected void refresh(AjaxRequestTarget target) {
		target.add(BatchCommandStatusPanel.this);		
	}

	
	protected AbstractAjaxTimerBehavior getTimer() {
		return timer;
	}
	
	private void load () {
								
		WebMarkupContainer wstatus = new WebMarkupContainer("status") {
			@Override
			public boolean isVisible() {
				return isExecuting() || isFinished();
			}
		};
		
		Label status = new Label("status", new Model<String>("status") {
			@Override
			public String getObject() {
				return getStatusAsString();
			}
		});
		
		WebMarkupContainer wti = new WebMarkupContainer("totalitems-container") {
			@Override
			public boolean isVisible() {
				return isExecuting() || isFinished();
			}
		};
		
		Label lti = new Label("total-items", new Model<String>() {
			@Override
			public String getObject() {
				return getTotalItemsAsString()!=null?getTotalItemsAsString():"";
			}
		});

		wti.add(lti);
		wstatus.add(wti);
		
		WebMarkupContainer wtip = new WebMarkupContainer("progressitems-container") {
			@Override
			public boolean isVisible() {
				return isExecuting() || isFinished();
			}
		};
		
		Label ltip = new Label("total-items-processed", new Model<String>() {
			@Override
			public String getObject() {
				return getTotalItemsProcessedAsString();
			}
		});
		
		
		wtip.add(ltip);
		wstatus.add(wtip);

		
		Label startdate = new Label("date-start", new Model<String>("date-start") {
			@Override
			public String getObject() {
				return  getDateStartAsString();
			}
		});
		
		startdate.setEscapeModelStrings(false);
		
		Label enddate = new Label("date-end",  new Model<String>("date-end") {
			@Override
			public String getObject() {
				return getDateEndAsString();
			}
		});
		
		enddate.setEscapeModelStrings(false);
		
		Label result = new Label("result",  new Model<String>("result") {
			@Override
			public String getObject() {
				return getResult();
			}
		});
		
		result.setEscapeModelStrings(false);
				
		Label result_details = new Label("result-details",  new Model<String>("result_details") {
			@Override
			public String getObject() {
				return getResultDetails();
			}
		});

		result_details.setEscapeModelStrings(false);
		
		Label resultcomments = new Label("resultcomments",  new Model<String>("resultcomments") {
			@Override
			public String getObject() {
				return getResultComments();
			}
		});
		
		resultcomments.setEscapeModelStrings(false);
		
		Label progress = new Label("progress",  new Model<String>("progress") {
			@Override
			public String getObject() {
				return getProgressAsString();
			}
		});
		
		Label etc = new Label("etc",  new Model<String>("etc") {
			@Override
			public String getObject() {
				return getEstimatedTimeComplete();
			}
		});
		
		Label duration = new Label("duration",  new Model<String>("duration") {
			@Override
			public String getObject() {
				return getDurationAsString();
			}
		});
		
		WebMarkupContainer status_container = new WebMarkupContainer("status-container") {
			@Override
			public boolean isVisible() {
				return true;
			}
		};

		status_container.add(status);
		wstatus.add(status_container);
		
		WebMarkupContainer startdate_container = new WebMarkupContainer("date-start-container") {
			@Override
			public boolean isVisible() {
				return true; 
			}
		};
		startdate_container.add(startdate);
		wstatus.add(startdate_container);

		WebMarkupContainer  progress_container = new WebMarkupContainer("progress-container") {
			@Override
			public boolean isVisible() {
				return !(getStatus()==CommandState.UNKNOWN);
			}
		};
		progress_container.add(progress);
		wstatus.add(progress_container);
		
		WebMarkupContainer etc_container = new WebMarkupContainer("etc-container") {
			@Override
			public boolean isVisible() {
				return !(getStatus()==CommandState.UNKNOWN);
			}
		};
		etc_container.add(etc);
		wstatus.add(etc_container);

		// ----------------------------------- Terminated --------------------------------------------
		//	
		WebMarkupContainer results_cont = new WebMarkupContainer("command-result") {
			@Override
			public boolean isVisible() {
				return getStatus()!=CommandState.NOT_STARTED && getStatus()!=CommandState.RUNNING;
			}
		};
		
		wstatus.add(results_cont);
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
			@Override
			public boolean isVisible() {
				return true;
			}
		};
		result_container.add(result);
		results_cont.add(result_container);

		WebMarkupContainer result_details_container = new WebMarkupContainer("result-details-container") {
			@Override
			public boolean isVisible() {
				return true;
			}
		};
		result_details_container.add(result_details);
		results_cont.add(result_details_container);

		WebMarkupContainer resultcomments_container = new WebMarkupContainer("resultcomments-container") {
			@Override
			public boolean isVisible() {
				return !(getStatus()==CommandState.NOT_STARTED || 
					getStatus()==CommandState.PAUSED ||
					getStatus()==CommandState.UNKNOWN ||
					getStatus()==CommandState.RUNNING &&  getResultComments().length()>0);
			}
		};
		resultcomments_container.add(resultcomments);
		results_cont.add(resultcomments_container);
		
		WebMarkupContainer duration_container = new WebMarkupContainer("duration-container") {
			@Override
			public boolean isVisible() {
				return ((getStatus()==CommandState.COMPLETED) || (getStatus()==CommandState.CANCELED));
			}
		};
		
		duration_container.add(duration);
		results_cont.add(duration_container);
		
		WebMarkupContainer log_container = new WebMarkupContainer("log-container") {
			@Override
			public boolean isVisible() {
				return ((getStatus()==CommandState.COMPLETED) || (getStatus()==CommandState.CANCELED)) && getLogFile()!=null;
			}
		};
		results_cont.add(log_container);
		
		WebMarkupContainer loglink = new WebMarkupContainer("log-link");
		loglink.add(new AttributeModifier("href", new Model<String>() {
			public String getObject() {
				if (getLogFile()!=null) {
					return RequestCycle.get().urlFor(new WebFileReference(getLogFile()), null).toString();
				}
				else {
					return "";
				}
			}
		}));
		loglink.add(new Label("log-name", new Model<String>() {
			public String getObject() {
				return getLogFile()!=null ? getLogFile().getName() : "";
			}
		}));
		log_container.add(loglink);
		
		wstatus.add(new WorkingAjaxLink<Void>("stop-link") {
			public void onClick(AjaxRequestTarget target) {
				stop(target);
			}
			@Override
			public boolean isVisible() {
				return isStopButtonEnabled() && (getStatus()==CommandState.RUNNING || getStatus()==CommandState.NOT_STARTED);
			}
		});
		
		this.timer = new AbstractAjaxTimerBehavior(Duration.ofSeconds(2)) {
			@Override
	        protected void onTimer(AjaxRequestTarget target) {
				if (getStatus()==CommandState.COMPLETED || getStatus()==CommandState.CANCELED || getStatus()==CommandState.ERROR) {
					this.stop(target);
					setIsExecuting(false);
					setIsFinished(true);
					onAfterExecution(target);
					refresh(target);
					
				}
				else  {
					if (getStatus()==CommandState.RUNNING)
						setIsExecuting(true);
					refresh(target);
				}
			}
		};
		
		wstatus.add(this.timer);
		wstatus.setOutputMarkupId(true);
		add(wstatus);
	}
	
	protected String getTotalItemsProcessedAsString() {
		
		if (getCommand()==null)
			return "n/a";
		
		if (getCommand().getTotalItemsProcessed()<0)
			return "n/a";
		return String.valueOf(getCommand().getTotalItemsProcessed());
	}


	protected String getTotalItemsAsString() {
		
		if (getCommand()==null)
			return "n/a";
		
		if (getCommand().getTotalItems()<1)
			return "n/a";
		
		return String.valueOf(getCommand().getTotalItems());
	}

	/** --------------------------------------------------------------------------------------------
	*/
	public synchronized void stop(AjaxRequestTarget target) {
	
		if (getCommandId()!=0) {
			if (getCommandService().getCommand(getCommandId())!=null) {
				getCommandService().getCommand(getCommandId()).stop();
				getTimer().stop(target);
				setIsExecuting(false);
				setIsFinished(true);
				try {
					Thread.sleep(600);
				} catch (InterruptedException e) {
							logger.error(e);
				}
				onAfterExecution(target);
			}
			refresh(target);
		}
		else {
			
			if (this.command_list!=null) {
				
				
				for (Long id: command_list) {
					if (getCommandService().getCommand(id)!=null)
						getCommandService().getCommand(id).stop();
				}
				getTimer().stop(target);
				setIsExecuting(false);
				setIsFinished(true);
				try {
					Thread.sleep(600);
				} catch (InterruptedException e) {
					logger.error(e);
				}
				onAfterExecution(target);
			}
		}
		
	}
	
	
	protected boolean isStopButtonEnabled() {
		return this.isStopButton;
	}
	
	public boolean isExecuting() {
		return isExecuting; 	
	}
	
	public void setIsExecuting(boolean ie) {
		isExecuting=ie; 	 
	}

	public boolean isFinished() {
		return isFinished; 	
	}
	
	public void setIsFinished(boolean f) {
		isFinished=f; 	 
	}

	
	private String getDateStartAsString() {
		if (getCommandId()>0) {
			if (getCommandService().getCommand(getCommandId())!=null) {
				OffsetDateTime date = getCommandService().getCommand(getCommandId()).getDateStarted();
				if (date!=null)
					return ServiceLocator.getService(DateTimeService.class).timeElapsed(date);
			}
		}
		return "-";
	}
						
	private String getDateEndAsString() {
		if (getCommandId()>0) {
			if (getCommandService().getCommand(getCommandId())!=null) {
				OffsetDateTime date = getCommandService().getCommand(getCommandId()).getDateTerminated();
				if (date!=null)
					return ServiceLocator.getService(DateTimeService.class).timeElapsed(date);
			}
		}
		return "N/A";
	}

	private String getResult() {
		if (getCommandId()>0) {
			if (getCommandService().getCommand(getCommandId())!=null) {
				return getCommandService().getCommand(getCommandId()).getResult();
			}
		}
		return "N/A";
	}
	
	private String getResultDetails() {
		if (getCommand()!=null) {
			return getCommand().getResultDetails();
		}
		return "N/A";
	}
	
	private String getResultComments() {
		if (getCommand()!=null) {
			String str = getCommand().getResultComment();
			if (str!=null)
				return str;
		}
		return "";
	}
	
	private CommandState getStatus() {
		if (getCommand()!=null) {
			return getCommand().getState();
		}
		return CommandState.UNKNOWN;
	}
	
	private String getStatusAsString() {
		if (getCommand()!=null) {
			return getCommand().getState().getLabel();
		}
		return "N/A";   		
	}
						

	private String getDurationAsString() {
		if (getCommand()!=null) {
			return ServiceLocator.getService(DateTimeService.class).formatLapseSeconds(getCommand().getDuration(),  getSessionUser().getLocale());

		}
		return "N/A";
	}


	protected String getEstimatedTimeComplete() {
		if (getCommand()!=null) {
			Double value =  Double.valueOf(getCommand().estimatedSecsToEnd() * 1000.0);
			Long lv = value.longValue();
			if (lv<0)
				return "N/A";
			return ServiceLocator.getService(DateTimeService.class).formatLapseSeconds(lv,  getSessionUser().getLocale());
		}
		return "N/A";
	}

	private String getProgressAsString() {
		if (getCommand()!=null) {
			double progress = getCommandService().getProgress(getCommandId());
			progress = (double)((int)(progress*100))/100; 
			return String.valueOf(progress)+" %";
		}
		return "N/A"; 	 	
	}
	
	private String getLogPath() {
		return getCommand()!=null && getCommand() instanceof AbstractCommand ? ((AbstractCommand)getCommand()).getLogPath() : null; 
	}
	
	private File getLogFile() {
		if (getLogPath()!=null) {
			return new File(getLogPath());
		}
		else {
			return null;
		}
	}
	
	private Command getCommand() {
		if (getCommandId()==0)
			return null;
		return getCommandService().getCommand(getCommandId());
	}
	
	
	private CommandService getCommandService() { 
		try {
			return (CommandService) ServiceLocator.getService(CommandService.class);
		} 
		catch (RuntimeException e) {
			logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
			return null;
		}
	}

	public void onAfterExecution(AjaxRequestTarget target) {
		
	}
	
	protected User getSessionUser() {
		try {
			return ServiceLocator.getService(SecurityService.class).getSessionUser();
		} catch (Exception e) {
			return null;
		}
	}
}
