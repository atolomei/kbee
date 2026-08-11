package com.novamens.content.web.admin.markup.datamanagement;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;

import com.novamens.content.command.Command;
import com.novamens.content.web.command.batch.markup.BatchCommandStatusPanel;
import com.novamens.kbee.command.CommandService;

import com.novamens.kbee.content.command.ListMissingFilesCommand;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

@SuppressWarnings("serial")
public class FileServerCommandPanel extends Panel {
	private static final long serialVersionUID = 1L;

	private static final boolean is_root = true;
	
	static private Logger logger = LogManager.getLogger(CommandPanel2.class.getName());

	private long command_id;
	
	public enum State {
		PREPARING 		(1, "preparing"), 
		EXECUTING 		(2, "executing"),
		TERMINATED		(2, "terminated"); 
		private String label;
		private int id;
		private  State(int code, String label) {this.label = label;this.id = code;}
		public String toString() {return ("id: " + getId() + "  label: "+ getLabel());} 
		public String getLabel() {return label;}
		public int getId() {return id;}
	}
	
	private State command_state = State.PREPARING;
	
	public FileServerCommandPanel(String id) {
		super(id);
				
			CommandForm form = new CommandForm("form");
			add(form);
			
			add ( new Panel("status") {
				private static final long serialVersionUID = 1L;
				public boolean isVisible() {
					return false;
				}
			});
			
			setOutputMarkupId(true);
	}	
	
	public class CommandForm extends Form<Void> {
	 
		private static final long serialVersionUID = -3302115450641201766L;
		
		private String statement;
		private String command;
		
		public CommandForm(String id) {
			super(id);

			this.setOutputMarkupId(true);
			
			DropDownChoice<String> f_command = new DropDownChoice<String>("command", this.getCommands());
			f_command.setMarkupId("orden"+getMarkupId());
			f_command.setModel(new PropertyModel<String>(this,"command"));
			f_command.setModelObject(this.getCommands().get(0));
			f_command.setChoiceRenderer(new ChoiceRenderer<String>() {
				@Override
				public Object getDisplayValue(String object) {
					return object;
				}
				@Override
				public String getIdValue(String object, int index) {
					return object;
				};
			});
			add(f_command);
			
			TextArea<String> statement = new TextArea<String>("statement");
			statement.setModel(new PropertyModel<String>(this,"statement"));
			add(statement);
			
			add(new AjaxButton("submit-button", this) {

				protected void onSubmit(AjaxRequestTarget target) {
					try {
						if (getCommand()!=null) {
							
							logger.info(getCommand() + ". " + getStatement());
							CommandService service = ServiceLocator.getService(CommandService.class);
							Command cmd;
							
							if (getCommand().toLowerCase().startsWith("list missing")) {
								
									cmd = new ListMissingFilesCommand();
									service.add(cmd);
									
									FileServerCommandPanel.this.setCommandId((Long) cmd.getId());
									setState(State.EXECUTING);
									BatchCommandStatusPanel panel = new BatchCommandStatusPanel("status", (long) cmd.getId()) {
										private static final long serialVersionUID = 1L;
										@Override
										public void onAfterExecution(AjaxRequestTarget target) {
											setState(State.TERMINATED);
											target.add(FileServerCommandPanel.this);
										}
									};
									FileServerCommandPanel.this.replace(panel);
									logger.debug("Sending "+ cmd.getId().toString());
									target.add(FileServerCommandPanel.this);
								
							}
						}
					}
					catch (Exception e) {
						logger.error(e);
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						PrintStream ps = new PrintStream(baos);
						e.printStackTrace(ps);
						logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
						String message =  baos.toString();
						getForm().error(message);
					}
					target.add(CommandForm.this);
				}
				
				@Override
				public boolean isEnabled() {
					boolean role_root = ServiceLocator.getService(SecurityService.class).isRoot();
					return role_root && getState()!=State.EXECUTING;
				}
				
				@Override
				public boolean isVisible() {
					return true;
				}
			});
			
			add(new AjaxButton("reset-button", this) {
				@Override
				protected void onSubmit(AjaxRequestTarget target) {
					if (getCommandService().getCommand(getCommandId())!=null) {
						getCommandService().getCommand(getCommandId()).stop();
						setState(State.TERMINATED);
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							logger.error(e);
						}
					}
					FileServerCommandPanel.this.get("status").setVisible(false);
					target.add(FileServerCommandPanel.this);
				}
				
				@Override
				public boolean isEnabled() {
					boolean role_root = ServiceLocator.getService(SecurityService.class).isRoot();
					return role_root && getState()!=State.EXECUTING;
				}
			});
			
			add(new FeedbackPanel("feedback"));
		}
		
		
		
		/** 
		 * @return
		 */
		private List<String> getCommands() {
						
			List<String> list = new ArrayList<String>();

			if (isRoot()) {
				
				list.add("List missing Files");
				list.add("Calculate CRC 32 for all files");
				
			}
			
			Collections.sort(list);
			
			return list;
		}

		public String getStatement() {
			return this.statement;
		}
		
		public void setStatement(String statement) {
			this.statement = statement;
		}
		
		public String getCommand() {
			return this.command;
		}
		
		public void setCommand(String command) {
			this.command = command;
		}
	}

	
	public State getState() { return this.command_state;}
	
	public void setState(State state) {
		this.command_state = state;
	}

	@SuppressWarnings("unused")
	private Map<String, Object> makeParameters(String str) {
		Map<String, Object> map = new HashMap<String, Object>();
		if (str!=null && str.length()>0) {
			String arr[] = str.split("\\r|\\n");
			for (String line: arr) {
					String kv[] = line.split(":", 2);
					if (kv.length==2) {
						String key = kv[0].trim().toLowerCase();
						// String value;
						// if (kv.length>2) {
						//	 StringBuilder x=new StringBuilder();
						//	 for (int n=1; n<kv.length; n++) {
						// 		if (n>1)
						//	 		x.append(":");
						//	 	x.append(kv[n]);
						//	 }
						//	 value = x.toString();
						// }  else
						String value = kv[1];
						map.put(key, value);
					}
			}
		}	
		return map;
	}
	
	private long  getCommandId() {
		return command_id;   	
	}

	private void  setCommandId(long cmd) {
		this.command_id=cmd;
	}
	private CommandService getCommandService() { 
		try {
			CommandService service = (CommandService) ServiceLocator.getService(CommandService.class);
			return  service;
		} catch (RuntimeException e) {
			logger.error(e.getStackTrace());
			return null;
		}
		
	}
	
	private boolean isRoot() {
		return is_root;
	}
}
