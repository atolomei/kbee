package com.novamens.content.web.admin.markup.datamanagement;


import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import org.apache.wicket.model.PropertyModel;

import com.novamens.beans.BeansService;
import com.novamens.content.command.Command;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.web.command.batch.markup.BatchCommandStatusPanel;
import com.novamens.content.web.multidimensional.ReindexDateRangeCommand;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.command.CommandService;
import com.novamens.kbee.content.command.ReindexCommand;
import com.novamens.kbee.content.command.ReindexDomainCommand;
import com.novamens.kbee.content.command.ReindexEverythingCommand;
import com.novamens.kbee.content.command.TestCommand;
import com.novamens.scheduler.SchedulerService;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.form.ChoiceField;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.Form.Disposition;

import kbee.web.form.EditButtonsV5;

import com.novamens.wicket.markup.html.form.TextAreaField;


public class ReindexCommandPanel extends ObjectEditor<Command> {
		
	private static final long serialVersionUID = 1L;

	static private Logger logger = LogManager.getLogger(ReindexCommandPanel.class.getName());

	/** 
	 */
	public class SupportCommandModel implements IModel<SupportCommand> {
		
		private static final long serialVersionUID = 1L;
		
		private SupportCommand scommand;
		
		public SupportCommandModel(SupportCommand scommand) {
			this.scommand=scommand;
		}
		
		@Override
		public void detach() {
		}

		@Override
		public SupportCommand getObject() {
			return scommand;
		}

		@Override
		public void setObject(SupportCommand object) {
			scommand = object;
		}
	}



	public class SupportCommand implements Serializable {
		private static final long serialVersionUID = 1L;
		public String name;
		public String query;
		
		
		public int batchsize;
		public int num_threads = 1;
		public int limit = 0;
		
		
		
		public int code;
		
		public boolean include_attachments=true;
		public  SupportCommand(String name, String query) {
			this(name, query, 0, NOP);
		}
		
		public  SupportCommand(String name, String query, int limit, int code, boolean include_attachments) {
			this.name=name;
			this.query=query;
			this.limit=limit;
			this.code=code;
			this.include_attachments=include_attachments;
		}

		
		public  SupportCommand(String name, String query, int limit, int code) {
		this.name=name;
		this.query=query;
		this.limit=limit;
		this.code=code;
		}
		
		public String getDisplayName() {
			return name;
		}
		
		public String toString() {
			return this.name;
		}
	}


	public enum State {
		PREPARING 		(1, "preparing"), 
		EXECUTING 		(2, "executing"),
		TERMINATED		(3, "terminated"); 
		private String label;
		private int id;
		private  State(int code, String label) {this.label = label;this.id = code;}
		public String toString() {return ("id: " + getId() + "  label: "+ getLabel());} 
		public String getLabel() {return label;}
		public int getId() {return id;}
	}
	

	private final static int NOP 		= -100;
//	private final static int ALL 		=  100;
	
	private final static int GENERIC 	=  1;
	private final static int WORKSPACE 	=  2;
//	private final static int CONTENT 	=  3;
//	private final static int SECURITY 	=  4;
//	private final static int MODEL 		=  5;
	private final static int STATEMENT	=  6;
	private final static int DOMAIN 	=  7;
	private final static int EVERYTHING =  8;
	private final static int EVERYTHING_NOATTACHMENTS =  9;
	private final static int MT_GENERIC =  10;
	private final static int CLEAN		=  12;
	
	
	
	
	private final static int REINDEX_DATA_RANGE_MODIFIED 	=  30;
	private final static int REINDEX_DATA_RANGE_EXECUTED 	=  40;
	
	private final static int TEST 		=  100;
	
	private State command_state = State.PREPARING;
	private Form<Void> form;
	private String parameters;
	private List<SupportCommand> list_c  = null;
	private IModel<SupportCommand> selectedCommand = new SupportCommandModel(null);
	
	
	/** --------------------------------------------------------------------------------------------
	*/
	public ReindexCommandPanel(String id) {
		super(id);
		
		setOutputMarkupId(true);

		form = new Form<Void> ("form", Disposition.VERTICAL);
		
		TextAreaField<String> statement = new TextAreaField<String>("parameters", new PropertyModel<String>(this,"parameters")) {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isEnabled() {
				return true;
			}
		};
		statement.setRequired(false);
		
		form.add(statement);
		form.add(new ChoiceField<SupportCommand>("frequent", selectedCommand, new PropertyModel<List<SupportCommand>>(this, "frequentCommands"), true));
		
		addOrReplace(form);
		
		initialize();
		
		add(new EditButtonsV5<Command>(this) {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isVisible() {
				return getState()==State.PREPARING; 
			}
		});
		
		add(new AjaxLink<Object>("stop") {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isVisible() {
				return getState()==State.EXECUTING;
			}

			@Override
			public boolean isEnabled() {
				return getState()==State.EXECUTING;
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				((BatchCommandStatusPanel) ReindexCommandPanel.this.get("status")).stop(target);
			}
		});


		add(new AjaxLink<Object>("close") {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isVisible() {
				return getState()==State.TERMINATED;
			}

			@Override
			public boolean isEnabled() {
				return getState()==State.TERMINATED;
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
					onClose(target);
			}
		});

		add(new AjaxLink<Object>("clear") {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isVisible() {
				return getState()==State.TERMINATED;
			}

			@Override
			public boolean isEnabled() {
				return getState()==State.TERMINATED;
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				initialize();
				ReindexCommandPanel.this.replace(new Panel("status") {
					private static final long serialVersionUID = 1L;
					public boolean isVisible() {
						return false;
					}
				});
				target.add(ReindexCommandPanel.this.getParent());
			}
		});
		
		add (new Panel("status") {
			private static final long serialVersionUID = 1L;
			public boolean isVisible() {
				return false;
			}
		});
	}



	public IModel<SupportCommand> getSelectedCommand() {
		return selectedCommand;
	}


	public void setSelectedCommand(IModel<SupportCommand> selected_command) {
		this.selectedCommand = selected_command;
	}


	
	public void setParameters(String parameters) {
		this.parameters = parameters;
	}


	

	public String getParameters() {
		return this.parameters;
	}


	
	public State getState() { 
		return this.command_state;
	}

	
	public void setState(State state) {
	this.command_state = state;
	}

	
	public User getSessionUser() {
	return ServiceLocator.getService(SecurityService.class).getSessionUser();

	}

	
	public void update(AjaxRequestTarget target) {
		SupportCommand sc = this.getSelectedCommand().getObject();
		logger.info("Executing command: " + sc.name);
		execute(target, sc);
	}
	

	protected void execute(AjaxRequestTarget target, SupportCommand sc ) {

		try {
			
			
			@SuppressWarnings("unchecked")
			Map<String, Object> map = makeParametersMap(((TextAreaField<String>) get("form:parameters")).getValue());

			
			try {
				sc.batchsize = Integer.valueOf( (String) map.get("batchsize")).intValue();
			} catch (Exception e) {
				logger.error(e);
				sc.batchsize  = 0;
			}


			try {
				sc.num_threads = Integer.valueOf( (String) map.get("threads")).intValue();
			} catch (Exception e) {
				logger.error(e);
				sc.num_threads = 1;
			}

			
			
			
			CommandService service = ServiceLocator.getService(CommandService.class);
			Command cmd;
			
			if (sc.code==TEST) {
				cmd = new TestCommand();
			}
			
			else if (sc.code==EVERYTHING_NOATTACHMENTS) {
				cmd = new  ReindexEverythingCommand(false);
			
			}
			
			else if (sc.code==EVERYTHING) {
				cmd = new ReindexEverythingCommand(true);
			
			}
			
			else if (sc.code==STATEMENT) {
				cmd = new ReindexCommand(this.getParameters());
				((ReindexCommand) cmd).setMaxElements(sc.limit);
			
			}
			
			else if (sc.code==REINDEX_DATA_RANGE_MODIFIED) {
				cmd = (Command) new ReindexDateRangeCommand("modifiedmember");
			
			}
			else if (sc.code==REINDEX_DATA_RANGE_EXECUTED) {
				cmd = (Command) new ReindexDateRangeCommand("executedmember");
			}
			else if (sc.code==DOMAIN) {
				cmd = new ReindexDomainCommand(getContentDao().findDomainByName(sc.query));
				 ((ReindexDomainCommand) cmd).setIncludeAttachments(sc.include_attachments);
			 
			}
			else if (sc.code==CLEAN) {
				cmd = (Command) ServiceLocator.getService(BeansService.class).getBean("CleanBatchCommand");
				if (sc.batchsize>0)
					cmd.getParameters().put("batchsize", String.valueOf(sc.batchsize));
				if (sc.num_threads>0)
					cmd.getParameters().put("max-threads", String.valueOf(sc.num_threads));
				cmd.getParameters().put("statement", sc.query);
			}
			// -------------------------------------------
			// VALID FOR CONTENT ONLY
			else if (sc.code==MT_GENERIC) {
				cmd = (Command) ServiceLocator.getService(BeansService.class).getBean("ReindexBatchCommand");
				if (sc.limit>0)
					cmd.getParameters().put("limit", String.valueOf(sc.limit));
				if (sc.batchsize>0)
					cmd.getParameters().put("batchsize", String.valueOf(sc.batchsize));
				if (sc.num_threads>0)
					cmd.getParameters().put("max-threads", String.valueOf(sc.num_threads));
				cmd.getParameters().put("statement", sc.query);
			}
			else {
				cmd = new ReindexCommand(sc.query);
				((ReindexCommand) cmd).setMaxElements(sc.limit);
			}
			
			cmd.setPriority(SchedulerService.HIGH_PRIORITY);
			
			cmd.setExactlyOneSemantics(true);
			service.add(cmd);
			setState(State.EXECUTING);
			BatchCommandStatusPanel panel = new BatchCommandStatusPanel("status", (long) cmd.getId(), false) {
					private static final long serialVersionUID = 1L;
					@Override
					public void onAfterExecution(AjaxRequestTarget target) {
						setState(State.TERMINATED);
						target.add(ReindexCommandPanel.this);
					}
			};
				
			ReindexCommandPanel.this.replace(panel);
			logger.debug("Sending "+ cmd.getId().toString());
			target.add(ReindexCommandPanel.this);
			
		}
		catch (Exception e) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				PrintStream ps = new PrintStream(baos);
				e.printStackTrace(ps);
				logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
				logger.error(e.getClass().getName(), e);
				String message =  baos.toString();
				form.error(message);
		}
	}
		

	public Form<Void> getForm() {
		return this.form;
	}
	
 
	/**
	 * 
	 * query=from KbeeContent where lastmodifieddate > '2019-12-16 00:00:00.000'
	 * 
	 * @return
	 */
	public List<SupportCommand> getFrequentCommands() {
		
		if (list_c!=null)
			return list_c;
		
		
		String this_month = String.valueOf(OffsetDateTime.now().getMonth().ordinal());
		
		if (this_month.length()==1)
			this_month = "0" +this_month;
		
		String this_year = String.valueOf(OffsetDateTime.now().getYear()); 
		String last_year = String.valueOf(OffsetDateTime.now().getYear()-1);
		String two_last_year = String.valueOf(OffsetDateTime.now().getYear()-2);
		String three_last_year = String.valueOf(OffsetDateTime.now().getYear()-3);
		
		String five_last_year = String.valueOf(OffsetDateTime.now().getYear()-5);
		
		list_c = new ArrayList<SupportCommand>();
									
		list_c.add(new SupportCommand("1. Generic Reindex", "all", 0, STATEMENT));
															
		list_c.add(new SupportCommand("Workspaces (all)", 				"from KbeeContent where workspace>0", 0, MT_GENERIC));
		list_c.add(new SupportCommand("Workspaces (100 most recent)", 	"from KbeeContent K where workspace>0 order by lastmodifieddate desc", 100, WORKSPACE));
		list_c.add(new SupportCommand("Workspaces (1000 most recent)", 	"from KbeeContent K where workspace>0 order by lastmodifieddate desc", 1000, WORKSPACE));
		list_c.add(new SupportCommand("Workspaces (clean)", 			"workspace:true", 0, CLEAN));
		
		list_c.add(new SupportCommand("All Contents (100 most recent)", 	"from KbeeContent order by lastmodifieddate desc", 100, GENERIC));
		list_c.add(new SupportCommand("All Contents (1000 most recent)", 	"from KbeeContent order by lastmodifieddate desc", 1000, GENERIC));
		list_c.add(new SupportCommand("All Contents (10000 most recent)",	"from KbeeContent order by lastmodifieddate desc", 10000, MT_GENERIC));
		list_c.add(new SupportCommand("All Contents (30000 most recent)", 	"from KbeeContent order by lastmodifieddate desc", 30000, MT_GENERIC));
		list_c.add(new SupportCommand("All Contents (100000 most recent)", 	"from KbeeContent order by lastmodifieddate desc", 100000, MT_GENERIC));
																		
		list_c.add(new SupportCommand("All Contents (month " +this_month +")",       "from KbeeContent where lastmodifieddate >= '"+       this_year+"-" + this_month + "-01 00:00:00.000'", 0, MT_GENERIC));
		
		list_c.add(new SupportCommand("All Contents (year " +this_year +"+)", 		"from KbeeContent where lastmodifieddate > '"+this_year+"-01-01 00:00:00.000'", 0, MT_GENERIC));
		list_c.add(new SupportCommand("All Contents (year " +last_year +"+)", 		"from KbeeContent where lastmodifieddate > '"+last_year+"-01-01 00:00:00.000'", 0, MT_GENERIC));
		list_c.add(new SupportCommand("All Contents (year " +two_last_year +"+)", 	"from KbeeContent where lastmodifieddate > '"+ two_last_year+"-01-01 00:00:00.000'", 0, MT_GENERIC));
		list_c.add(new SupportCommand("All Contents (year " +three_last_year +"+)", "from KbeeContent where lastmodifieddate > '"+ three_last_year+"-01-01 00:00:00.000'", 0, MT_GENERIC));
		list_c.add(new SupportCommand("All Contents (year " +five_last_year +"+)", 	"from KbeeContent where lastmodifieddate > '"+ five_last_year+"-01-01 00:00:00.000'", 0, MT_GENERIC));
												
		list_c.add(new SupportCommand("All Contents (year " +this_year +")",       "from KbeeContent where lastmodifieddate >= '"+       this_year+"-01-01 00:00:00.000' and lastmodifieddate <= '"+        this_year+"-12-31 23:59:59.999'", 0, MT_GENERIC));
		list_c.add(new SupportCommand("All Contents (year " +last_year +")",       "from KbeeContent where lastmodifieddate >= '"+       last_year+"-01-01 00:00:00.000' and lastmodifieddate <= '"+        last_year+"-12-31 23:59:59.999'", 0, MT_GENERIC));
		list_c.add(new SupportCommand("All Contents (year " +two_last_year +")",   "from KbeeContent where lastmodifieddate >= '"+   two_last_year+"-01-01 00:00:00.000' and lastmodifieddate <= '"+    two_last_year+"-12-31 23:59:59.999'", 0, MT_GENERIC));
		list_c.add(new SupportCommand("All Contents (year " +three_last_year +")", "from KbeeContent where lastmodifieddate >= '"+ three_last_year+"-01-01 00:00:00.000' and lastmodifieddate <= '"+  three_last_year+"-12-31 23:59:59.999'", 0, MT_GENERIC));
		list_c.add(new SupportCommand("All Contents (year " +five_last_year +")",  "from KbeeContent where lastmodifieddate >= '"+  five_last_year+"-01-01 00:00:00.000' and lastmodifieddate <= '"+   five_last_year+"-12-31 23:59:59.999'", 0, MT_GENERIC));
		
		list_c.add(new SupportCommand("All Contents", "from KbeeContent order by lastmodifieddate desc", 0, MT_GENERIC));

		list_c.add(new SupportCommand("All Contents (clean)", "(type:idoc or type:text)", 0, CLEAN));

		list_c.add(new SupportCommand("Recycle Bin", "from KbeeContent where state=" + String.valueOf(ObjectState.DELETED.getId()) +"  order by lastmodifieddate desc", 0, GENERIC));
		
		list_c.add(new SupportCommand("DataSetMembers", "from KbeeDataSetMember", 0, GENERIC));
		
		list_c.add(new SupportCommand("Users and Groups", "from KbeePrincipal", 0, GENERIC));
		list_c.add(new SupportCommand("Rules", "from KbeeSecurityRule", 0, GENERIC));
										
		list_c.add(new SupportCommand("Dataset", "from KbeeDataSet", 0, 				MT_GENERIC));
		list_c.add(new SupportCommand("Classifier", "from KbeeClassifier", 0, 			GENERIC));
		list_c.add(new SupportCommand("Content Class", "from KbeeContentTemplate", 0, 	GENERIC));
		
		list_c.add(new SupportCommand("Test", "test", 0, TEST));
		
		
		//list_c.add(new SupportCommand("Activity Log (1000 most recent)", "from AbstractLogEvent order by event_id desc", 1000, TEST));
		//list_c.add(new SupportCommand("Activity Log (50000 most recent)", "from AbstractLogEvent order by event_id desc", 50000, TEST));
		//list_c.add(new SupportCommand("Email Log (1000 most recent)", "from SendEmailEvent order by event_id desc", 1000, TEST));
		//list_c.add(new SupportCommand("Work Notes", "from KbeeWorkNote", 0, GENERIC));
		
		List<Domain> domains = getContentDao().getDomains();
		
		for (Domain domain: domains) {
			list_c.add(new SupportCommand("Domain. " + domain.getName() + " - [ All ]",  domain.getName(), 0, DOMAIN));
			list_c.add(new SupportCommand("Domain. " + domain.getName() + " - [ All no Attachments ]",  domain.getName(), 0, DOMAIN, false));
			List<ContentTemplate> cts = getContentDao().getTemplates(domain);
			for (ContentTemplate ct: cts) 
				list_c.add(new SupportCommand("Domain. " + domain.getName() + " - " + ct.getName(), "from KbeeContent C where C.domain.id="+String.valueOf(domain.getId()) + "  and  C.contenttemplate.id=" + String.valueOf(ct.getId()), 0, GENERIC));
		}
		
		//list_c.add(new SupportCommand("Reindex Date Range (modified)", "", 0, REINDEX_DATA_RANGE_MODIFIED));
		//list_c.add(new SupportCommand("Reindex Date Range  (executed)" , "", 0, REINDEX_DATA_RANGE_EXECUTED));
		
		list_c.add(new SupportCommand("Everything  (no attachments)", "", 0, EVERYTHING_NOATTACHMENTS));
		list_c.add(new SupportCommand("Everything"                  , "", 0, EVERYTHING));
		
		
		Collections.sort(list_c, new Comparator<SupportCommand>() {
			@Override
			public int compare(SupportCommand o1, SupportCommand o2) {
				return o1.getDisplayName().compareToIgnoreCase(o2.getDisplayName());
			}
		});
		
		
		return list_c;
	}

	
	@SuppressWarnings("unchecked")
	private void initialize() {
		setState(State.PREPARING);
		this.selectedCommand = new SupportCommandModel( getFrequentCommands().get(0));
		setParameters("");
		((TextAreaField<String>) get("form:parameters")).setValue("");
		((ChoiceField<SupportCommand>) get("form:frequent")).setModel(getSelectedCommand());
	}

	
	protected void onClose(AjaxRequestTarget target) {
		setResponsePage(new SystemDataManagementPage());
	}
	
	
	/**
	 * line separator is \n
	 * key = value
	 * 
	 * @param str
	 * @return
	 */
	private Map<String, Object> makeParametersMap(String str) {
		Map<String, Object> map = new HashMap<String, Object>();
		if (str!=null && str.length()>0) {
			String arr[] = str.split("\\r|\\n");
			for (String line: arr) {
				String kv[] = line.split("=", 2);
				if (kv.length==2) {
					String key = kv[0].trim().toLowerCase();
					String value = kv[1].trim();
					map.put(key, value);
				} else {
					String kv2[] = line.split(":", 2);
					if (kv2.length==2) {
						String key = kv[0].trim().toLowerCase();
						String value = kv[1].trim();
						map.put(key, value);
					}
				}
			}
		}
		return map;
	}
	
	
}

