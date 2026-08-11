package com.novamens.content.web.admin.markup.datamanagement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;

import org.apache.wicket.markup.html.pages.RedirectPage;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

import com.novamens.content.user.UserService;
import com.novamens.content.web.command.batch.markup.BatchCommandStatusPanel;
import com.novamens.dom.Domain;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.command.CommandService;
import com.novamens.kbee.content.command.ExportContentsCommand;
import com.novamens.kbee.content.service.datamanagement.QuerySizeEstimator;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.solr.indexer.query.SolrQuery;

import kbee.web.query.ContentBaseQuery;
			
public class ExportPanel2 extends Panel {
	
	private static final long serialVersionUID = 1L;

	private Query query;
	
	static private Logger logger = LogManager.getLogger(ExportPanel2.class.getName());

	private static final double GB = 1000000000.0;
	
	private enum State {
		PREPARING 		(1, "preparing"), 
		EXECUTING 		(2, "executing"),
		TERMINATED		(3, "terminated"); 
		private String label;
		private int id;
		private State(int code, String label) {this.label = label;this.id = code;}
		public  String toString() {return ("id: " + getId() + "  label: "+ getLabel());} 
		public  String getLabel() {return label;}
		public int getId() {return id;}
	}
	
	private State command_state = State.PREPARING;
	
	protected Index getQueryIndex() {
		return getDomain().getService(JavaIndexerService.class).getIndex();
	}
	
	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}

	
	public ExportPanel2(String id) {
			this (id, null);
	}
			
	public ExportPanel2(String id, Query query) {
		super(id);

	if (query==null)
		query = new ContentBaseQuery(getQueryIndex());
		
	setQuery(query);
	
	CommandForm form = new CommandForm("form");
	add(form);
	
	add (new Panel("status") {
		private static final long serialVersionUID = 1L;
		public boolean isVisible() {
			return false;
		}
	});
	setOutputMarkupId(true);
	
	}	
	
	/** ---------------------------------------------------------------------------------------------------
	 */
	public class CommandForm extends Form<Void> {
	 
		private static final long serialVersionUID = -3302115450641201766L;

		final boolean is_root = ServiceLocator.getService(SecurityService.class).isRoot();
		final boolean is_domain_admin = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
		
		private String statement;
		@SuppressWarnings("unused")
		private String command;
		
		public CommandForm(String id) {
			super(id);

			this.setOutputMarkupId(true);
			
			Label query = new Label("query", new Model<String>() { 
				private static final long serialVersionUID = 1L;
				public String getObject() {
					if (getQuery() instanceof SolrQuery) {
						Map<String, Object> map =((SolrQuery) getQuery()).getParameters();
						StringBuilder str = new StringBuilder();
						for (Entry<String, Object> entry: map.entrySet()) 
							str.append( entry.getKey() + ": " + entry.getValue().toString() + ". ");
						return str.toString();
					}
					else
						return getQuery().toString();
				}
			});
			add(query);

			
			Label total = new Label("total", new Model<String>() { 
				private static final long serialVersionUID = 1L;
				public String getObject() {
					try {
					return String.valueOf(getQuery().execute().size());
					} catch (Exception e) {
						e.printStackTrace();
						return "error";
					}
				}
			});
			add(total);

			
			add(new AjaxButton("submit-button", this) {

				private static final long serialVersionUID = -8358957179617226851L;

				protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
							
							// -------------------------------------------------------------------------------------
							CommandService service = ServiceLocator.getService(CommandService.class);
							
							QuerySizeEstimator qes = new QuerySizeEstimator(getQuery());
							
							long hours = qes.getDuration().toHours(); //75
							long minutes = qes.getDuration().minusHours(hours).toMinutes(); //15
							long seconds = qes.getDuration().minusHours(hours).minusMinutes(minutes).toMillis() / 1000;
							long mili_seconds = qes.getDuration().minusHours(hours).minusMinutes(minutes).minusSeconds(seconds).toMillis();
							
							String HH = hours>0 ? (String.valueOf(hours)+"h "):"";
							String MM = minutes>0 ? (String.valueOf(minutes)+"m "):"";
							String SS = String.valueOf(seconds) + "." + String.valueOf(mili_seconds)+"s";
							
							String DUR =   HH + MM + SS;
							
							// Estimate total
							//
							logger.info("Duration: " + DUR);
							logger.info("Contents: " + String.valueOf(qes.getTotalContents()));
							logger.info("Resources: " + String.valueOf(qes.getTotalResources()));
							logger.info("Files: " + String.valueOf(qes.getTotalFiles()));
							logger.info("Errors: " + String.valueOf(qes.getTotalErrors()));
							logger.info("Total Size: " + String.valueOf( ((double) qes.getTotalSpace()) / GB ));
							
							double total_gb  =(((double) qes.getTotalSpace()) / GB );
							
							if (total_gb>20)
								return;
							
							// -------------------------------------------------------------------------------------																
							//
							//
							ExportContentsCommand command = new ExportContentsCommand();
							command.setParameters(makeParameters(getStatement()));
							command.setQuery(getQuery());
							command.setDomain(ServiceLocator.getService(UserService.class).getSessionUserProfile().getDomain());
							command.setUserId(ServiceLocator.getService(UserService.class).getSessionUserProfile().getUser().getId());
							service.add(command);
							setState(State.EXECUTING);
							BatchCommandStatusPanel panel = new BatchCommandStatusPanel("status", (long) command.getId()) {
								private static final long serialVersionUID = 1L;
								@Override
								public void onAfterExecution(AjaxRequestTarget target) {
									setState(State.TERMINATED);
									target.add(ExportPanel2.this);
								}
							};
							
							ExportPanel2.this.replace(panel);
							
							logger.debug("Sending "+ command.getId().toString());
							target.add(ExportPanel2.this);
				}
				
				@Override
				public boolean isEnabled() {
					return (is_domain_admin || is_root) && getState()!=State.EXECUTING && getState()!=State.TERMINATED;
				}
			});
			

			add(new AjaxButton("cancel-button", this) {
								
				private static final long serialVersionUID = -8358957179617226851L;

				protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
					setResponsePage(new RedirectPage("/content"));
				}
				
				@Override
				public boolean isEnabled() {
					return getState()!=State.EXECUTING;
				}
			});
			
			add(new FeedbackPanel("feedback"));
		}

		public String getStatement() {
			return this.statement;
		}
		
		public void setStatement(String statement) {
			this.statement = statement;
		}
	}
	
	private Map<String, Object> makeParameters(String str) {
		Map<String, Object> map = new HashMap<String, Object>();
		if (str!=null && str.length()>0) {
			String arr[] = str.split("\\r|\\n");
			for (String line: arr) {
					String kv[] = line.split(":", 2);
					if (kv.length==2) {
						String key = kv[0].trim().toLowerCase();
						String value = kv[1];
						map.put(key, value);
					}
			}
		}	
		return map;
	}
	
							
	public State getState() { 
		return this.command_state;
	}
	
	public void setState(State state) {
		this.command_state = state;
	}


	private Query getQuery() {
		return this.query;
	}
	private void setQuery(Query query) {
		this.query=query;
	}
	
	public List<String> getDestinations() {
		List<String> list = new ArrayList<String>();
		list.add("Amazon S3");
		list.add("Google GDrive");
		list.add("Server File System");
		return list;
	}
	
}
