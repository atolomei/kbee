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

import com.novamens.beans.BeansService;
import com.novamens.content.command.Command;

import com.novamens.content.dao.ContentDao;
import com.novamens.content.web.command.batch.markup.BatchCommandStatusPanel;
import com.novamens.dom.Domain;
import com.novamens.kbee.command.CommandService;
import com.novamens.kbee.content.command.AssignPhotoUsersCommand;
import com.novamens.kbee.content.command.ResetSupportUsersValuesCommand;
import com.novamens.kbee.content.command.TestCommand;
import com.novamens.kbee.content.domain.provisioning.AddGeneralRolesCommand;
import com.novamens.kbee.content.email.TestEmailSendCommand;
import com.novamens.kbee.content.notes.CreateWelcomeNoteCommand;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
 

public class CommandPanel2 extends Panel {
			
	private static final long serialVersionUID = 1L;

	private static final boolean is_admin = true;
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
	
	public CommandPanel2(String id) {
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
				private static final long serialVersionUID = 1L;
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

				private static final long serialVersionUID = -8358957179617226851L;

				protected void onSubmit(AjaxRequestTarget target) {
					try {
						if (getCommand()!=null) {
							
							logger.info(getCommand() + ". " + getStatement());
							CommandService service = ServiceLocator.getService(CommandService.class);
							Command cmd;
							

							//--------------------------------------------------------------------------------------------------------------------------------
							if (getCommand().equals("Roles. Create Default Domain Roles")) {
									
									cmd = new AddGeneralRolesCommand();
									cmd.execute();
									service.add(cmd);
									
									CommandPanel2.this.setCommandId((Long) cmd.getId());
									setState(State.EXECUTING);
									BatchCommandStatusPanel panel = new BatchCommandStatusPanel("status", (long) cmd.getId()) {
										private static final long serialVersionUID = 1L;
										@Override
										public void onAfterExecution(AjaxRequestTarget target) {
											setState(State.TERMINATED);
											target.add(CommandPanel2.this);
										}
									};
									CommandPanel2.this.replace(panel);
									logger.debug("Sending "+ cmd.getId().toString());
									target.add(CommandPanel2.this);
							}

							//--------------------------------------------------------------------------------------------------------------------------------
							// SAME THREAD
							//
							else if (getCommand().equals("Roles. Create Default Entity Roles")) {
									
							
									
							}


							//--------------------------------------------------------------------------------------------------------------------------------
							// 
							//
							else if (getCommand().equals("Restart Application")) {
									
									cmd = new com.novamens.kbee.content.command.RestartCommand();
									service.register(cmd);
									
									// Same thread, No scheduler
									cmd.execute();
									service.executed(cmd);
									
									CommandPanel2.this.setCommandId((Long) cmd.getId());
									setState(State.EXECUTING);
									
									BatchCommandStatusPanel panel = new BatchCommandStatusPanel("status", (long) cmd.getId()) {
										private static final long serialVersionUID = 1L;
										@Override
										public void onAfterExecution(AjaxRequestTarget target) {
											setState(State.TERMINATED);
											target.add(CommandPanel2.this);
										}
									};
									
									CommandPanel2.this.replace(panel);
									logger.debug("Sending "+ cmd.getId().toString());
									target.add(CommandPanel2.this);
									
							}

							
							//--------------------------------------------------------------------------------------------------------------------------------
							else if (getCommand().equals("Test Command 1")) {
									
									cmd = new TestCommand();
									service.add(cmd);
									
									CommandPanel2.this.setCommandId((Long) cmd.getId());
									setState(State.EXECUTING);
									BatchCommandStatusPanel panel = new BatchCommandStatusPanel("status", (long) cmd.getId()) {
										private static final long serialVersionUID = 1L;
										@Override
										public void onAfterExecution(AjaxRequestTarget target) {
											setState(State.TERMINATED);
											target.add(CommandPanel2.this);
										}
									};
									CommandPanel2.this.replace(panel);
									logger.debug("Sending "+ cmd.getId().toString());
									target.add(CommandPanel2.this);
							}


							// --------------------------------------------------------------------------------------------------------------------------------

							
							else if (getCommand().equals("Test Command 10")) {
								List<Long> list = new ArrayList<Long>();
								for (int n=0; n<10;n++) {
									cmd = new TestCommand();
									list.add(Long.valueOf(cmd.getId().toString()));
									service.add(cmd);
									Thread.sleep(600);
								}
								
								CommandPanel2.this.setCommandIds(list);
								
								setState(State.EXECUTING);
								BatchCommandStatusPanel panel = new BatchCommandStatusPanel("status", list.get(list.size()-1).longValue()) {
									private static final long serialVersionUID = 1L;
									@Override
									public void onAfterExecution(AjaxRequestTarget target) {
										setState(State.TERMINATED);
										target.add(CommandPanel2.this);
									}
								};
								CommandPanel2.this.replace(panel);
								logger.debug("Sending "+ String.valueOf(list.size()) +" Commands" );
								target.add(CommandPanel2.this);
						}
							

							// --------------------------------------------------------------------------------------------------------------------------------

							else if (getCommand().equals("Test Command N")) {
								List<Long> list = new ArrayList<Long>();
								
								String totals = getStatement();
								Integer to;
								try {
									to = Integer.valueOf(totals);
								} catch (Exception e) {
									to=100;
								}
								
								for (int n=0; n<to.intValue();n++) {
									cmd = new TestCommand();
									list.add(Long.valueOf(cmd.getId().toString()));
									service.add(cmd);
									Thread.sleep(500);
								}
								CommandPanel2.this.setCommandIds(list);
								setState(State.EXECUTING);
								BatchCommandStatusPanel panel = new BatchCommandStatusPanel("status", list.get(list.size()-1).longValue()) {
									private static final long serialVersionUID = 1L;
									@Override
									public void onAfterExecution(AjaxRequestTarget target) {
										setState(State.TERMINATED);
										target.add(CommandPanel2.this);
									}
								};
								CommandPanel2.this.replace(panel);
								logger.debug("Sending "+ String.valueOf(list.size()) +" Commands" );
								target.add(CommandPanel2.this);
						}
							

							// --------------------------------------------------------------------------------------------------------------------------------
							
							else if (getCommand().toLowerCase().startsWith("remove old")) {
								cmd = new RemoveOldExportsCommand();
								((RemoveOldExportsCommand) cmd).setPreserveDays(0);
								
								service.add(cmd);
								CommandPanel2.this.setCommandId((Long) cmd.getId());
								setState(State.EXECUTING);
								BatchCommandStatusPanel panel = new BatchCommandStatusPanel("status", (long) cmd.getId()) {
									private static final long serialVersionUID = 1L;
									@Override
									public void onAfterExecution(AjaxRequestTarget target) {
										setState(State.TERMINATED);
										target.add(CommandPanel2.this);
									}
								};
								CommandPanel2.this.replace(panel);
								logger.debug("Sending "+ cmd.getId().toString());
								target.add(CommandPanel2.this);
							}
							
			
							// 	--------------------------------------------------------------------------------------------------------------------------------
							
							else if (getCommand().toLowerCase().startsWith("test emailsendservicerequest")) {
								
								String totals = getStatement();
								Integer to;
								try {
									to = Integer.valueOf(totals);
								} catch (Exception e) {
									to=20;
								}
								
								// this is async command, so it will terminate immediately
								//
								cmd = new TestEmailSendCommand(to.intValue());
								service.add(cmd);
								CommandPanel2.this.setCommandId((Long) cmd.getId());
								setState(State.EXECUTING);

								BatchCommandStatusPanel panel = new BatchCommandStatusPanel("status", (long) cmd.getId()) {
									private static final long serialVersionUID = 1L;
									@Override
									public void onAfterExecution(AjaxRequestTarget target) {
										setState(State.TERMINATED);
										target.add(CommandPanel2.this);
									}
								};
								CommandPanel2.this.replace(panel);
								logger.debug("Sending "+ cmd.getId().toString());
								target.add(CommandPanel2.this);
							}
							
							
							// --------------------------------------------------------------------------------------------------------------------------------
							
							else if (getCommand().toLowerCase().startsWith("create welcome note")) {
								cmd = new CreateWelcomeNoteCommand();
								service.add(cmd);
								CommandPanel2.this.setCommandId((Long) cmd.getId());
								setState(State.EXECUTING);
								BatchCommandStatusPanel panel = new BatchCommandStatusPanel("status", (long) cmd.getId()) {
									private static final long serialVersionUID = 1L;
									@Override
									public void onAfterExecution(AjaxRequestTarget target) {
										setState(State.TERMINATED);
										target.add(CommandPanel2.this);
									}
								};
								CommandPanel2.this.replace(panel);
								logger.debug("Sending "+ cmd.getId().toString());
								target.add(CommandPanel2.this);
							}
							// --------------------------------------------------------------------------------------------------------------------------------
							else if (getCommand().toLowerCase().startsWith("reset all support")) {
								
								String pwd = getStatement();
								
								if (pwd!=null) {
									cmd = new   ResetSupportUsersValuesCommand(pwd); 
									service.add(cmd);
									CommandPanel2.this.setCommandId((Long) cmd.getId());
									setState(State.EXECUTING);
									BatchCommandStatusPanel panel = new BatchCommandStatusPanel("status", (long) cmd.getId()) {
										private static final long serialVersionUID = 1L;
										@Override
										public void onAfterExecution(AjaxRequestTarget target) {
											setState(State.TERMINATED);
											target.add(CommandPanel2.this);
										}
									};
									CommandPanel2.this.replace(panel);
									logger.debug("Sending "+ cmd.getId().toString());
									target.add(CommandPanel2.this);
								}
								else {
									if (get("feedback")!=null)
										get("feedback").error("Must include the password in the parameters.");
								}
								
							}
							
							// --------------------------------------------------------------------------------------------------------------------------------
							else if (getCommand().toLowerCase().contains("assign photo")) {
								
								String domainname = getStatement();
								
								Domain domain = null; 
								
								if (domainname!=null)  {
									domainname = domainname.toLowerCase().trim();
								    domain = getContentDao().findDomainByName(domainname);
								}
								
								 
								cmd = new AssignPhotoUsersCommand(domain);
								service.add(cmd);
								CommandPanel2.this.setCommandId((Long) cmd.getId());
								setState(State.EXECUTING);
								BatchCommandStatusPanel panel = new BatchCommandStatusPanel("status", (long) cmd.getId()) {
										private static final long serialVersionUID = 1L;
										@Override
										public void onAfterExecution(AjaxRequestTarget target) {
											setState(State.TERMINATED);
											target.add(CommandPanel2.this);
										}
								};
								CommandPanel2.this.replace(panel);
								logger.debug("Sending "+ cmd.getId().toString());
								target.add(CommandPanel2.this);
 							}
						}
					}
					catch (Exception e) {
						e.printStackTrace();
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
				private static final long serialVersionUID = -5848063566372226285L;
				
				@Override
				protected void onSubmit(AjaxRequestTarget target) {
					if (getCommandService().getCommand(getCommandId())!=null) {
						getCommandService().getCommand(getCommandId()).stop();
						setState(State.TERMINATED);
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					CommandPanel2.this.get("status").setVisible(false);
					target.add(CommandPanel2.this);
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
		 *  
		 * @return
		 * 
		 */
		private List<String> getCommands() {
						
			List<String> list = new ArrayList<String>();

			if (isRoot()) {
				
				list.add("Resource. Calculate CRC 32 for all files");
				
				list.add("Resource. Generate Width x Height");
				list.add("Index. Reindex");
				
				list.add("Domain File System Reader. Execute");
				
				list.add("Restart Application");
				list.add("Domain. Reset All Domain Passwords");
				list.add("Domain. Assign Photo to Users that don't have one");
				
				// list.add("SysMessage. Send");
				// list.add("SysMessage. Test");
				
				list.add("Reset all Support Passwords");
				list.add("Create Welcome Note");
				list.add("Remove Old Exports");
				list.add("Roles. Create Default Entity Roles");
				list.add("Roles. Create Default Domain Roles");
				
				list.add("Test. Test Scheduler");
			}

			if (isAdmin())
				list.add("Reindex Workspace");
			
			
			list.add("Test Command 1");
			list.add("Test Command 10");
			list.add("Test Command N");
			
			list.add("Test EmailSendServiceRequest");
			list.add("Classify Contents");
			
			
			Collections.sort(list);

			
			// Add Group to all users
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

	private Map<String, Object> getParametersMap(String str) {
		Map<String, Object> map = new HashMap<String, Object>();
		if (str!=null && str.length()>0) {
			String arr[] = str.split("\\r|\\n");
			for (String line: arr) {
				String kv[] = line.split("(?<!\\\\)(?:=)", 2);//not escaped '='
				if (kv.length==2) {
					String key = kv[0].trim().toLowerCase();
					String value = kv[1];
					map.put(key, value);
				} else {
					String kv2[] = line.split(":", 2);
					if (kv2.length==2) {
						String key = kv[0].trim().toLowerCase();
						String value = kv[1];
						map.put(key, value);
					}
				}
			}
		}
		return map;
	}
	
	private long  getCommandId() {
		return command_id;   	
	}

	
	List<Long> command_list;
	
	private void  setCommandIds(List<Long> list) {
		this.command_list = list;
	}
	
	private void  setCommandId(long cmd) {
		this.command_id=cmd;
	}
	
	private CommandService getCommandService() { 
		try {
			CommandService service = (CommandService) ServiceLocator.getService(CommandService.class);
			return  service;
		} catch (Exception e) {
			logger.error("error", e);
			return null;
		}
		
	}
	private boolean isAdmin() {
		return is_admin;
	}

	private boolean isRoot() {
		return is_root;
	}

	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}

}
