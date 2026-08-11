package kbee.web.importer;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.PropertyModel;

import com.novamens.content.base.DomainProxy;
import com.novamens.content.command.Command;
import com.novamens.content.command.CommandState;

import com.novamens.content.web.command.batch.markup.BatchCommandStatusPanel;
import com.novamens.kbee.command.CommandService;
import com.novamens.kbee.content.webapi.controller.LocalApiServiceWrapper;
import com.novamens.scheduler.SchedulerService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.CheckField;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.markup.html.form.TextField;

import kbee.importer.CloneCommand;
import kbee.web.page.AbstractApplicationPage;

@SuppressWarnings("serial")
public class ImportPage extends AbstractApplicationPage<Void> {
	private static final long serialVersionUID = 1L;
	
	static Logger logger = LogManager.getLogger(ImportPage.class.getName());
	
	private boolean groups;
	private boolean users;
	private boolean templates;
	private boolean files;
	private boolean classifiers;
	private boolean datasets;
	private boolean values;
	private boolean rules;
	private boolean freeze = true;
	private String server;
	private String domainName;
	private String criteria;
	private String maxfiles;
	
	private boolean submitted = false;
	
	Long commandId;
	
	public ImportPage() {
		
		setTopNavigation(getMainTopbar());
		  
		setMenu(getMainLaternalMenu());
		
		final WebMarkupContainer panel = new WebMarkupContainer("form-panel");
		
		panel.setOutputMarkupId(true);
		
		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);
		
		form.add(new TextField<String>("server", new PropertyModel<String>(this, "server")));
		form.add(new TextField<String>("domain", new PropertyModel<String>(this, "domainName")));
		form.add(new CheckField("groups",new PropertyModel<Boolean>(this, "groups")));
		form.add(new CheckField("templates",new PropertyModel<Boolean>(this, "templates")));
		form.add(new CheckField("users", new PropertyModel<Boolean>(this, "users")));
		form.add(new CheckField("datasets", new PropertyModel<Boolean>(this, "dataSets")));
		form.add(new CheckField("classifiers", new PropertyModel<Boolean>(this, "classifiers")));
		form.add(new CheckField("values", new PropertyModel<Boolean>(this, "values")));
		form.add(new CheckField("rules", new PropertyModel<Boolean>(this, "rules")));
		form.add(new CheckField("files", new PropertyModel<Boolean>(this, "files")));
		form.add(new TextField<String>("maxfiles", new PropertyModel<String>(this, "maxFiles")));
		form.add(new TextField<String>("criteria", new PropertyModel<String>(this, "criteria")));
		form.add(new CheckField("freeze", new PropertyModel<Boolean>(this, "freeze")));

		panel.add(new AjaxSubmitLink("start-button", form) {
			protected void onSubmit(AjaxRequestTarget target) {
				start(target);
				target.add(panel);
			}
		});
		
		panel.add(new AjaxLink<Void>("cancel-button") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				cancel();
				target.add(ImportPage.this.get("form-panel"));
			}
			@Override
			public boolean isVisible() {
				return isRunning(); 
			}
		});
		
		panel.add(form);
		
		WebMarkupContainer status = new WebMarkupContainer("status") {
			public boolean isVisible() {
				return true;
			}
		};
		
		status.add(new WebMarkupContainer("command"));
		
		status.setOutputMarkupId(true);
		
		panel.add(status);
		
		add(panel);
	}
	
	public void setGroups(boolean value) {
		this.groups = value;
	}
	
	public boolean getGroups() {
		return groups;
	}
	
	public void setTemplates(boolean value) {
		this.templates = value;
	}
	
	public boolean getTemplates() {
		return templates;
	}
	
	public void setUsers(boolean value) {
		this.users = value;
	}
	
	public boolean getUsers() {
		return users;
	}
		
	public void setDataSets(boolean value) {
		this.datasets = value;
	}
	
	public boolean getDataSets() {
		return datasets;
	}
	
	public void setClassifiers(boolean value) {
		this.classifiers = value;
	}
	
	public boolean getClassifiers() {
		return classifiers;
	}
	
	public void setRules(boolean value) {
		this.rules = value;
	}
	
	public boolean getRules() {
		return rules;
	}
	
	public void setValues(boolean value) {
		this.values = value;
	}
	
	public boolean getValues() {
		return values;
	}
	
	public void setFiles(boolean value) {
		this.files = value;
	}
	
	public boolean getFiles() {
		return files;
	}
	
	public void setFreeze(boolean value) {
		this.freeze = value;
	}
	
	public boolean getFreeze() {
		return freeze;
	}
	
	public void setServer(String url) {
		this.server = url;
	}
	
	public String getServer() {
		return server;
	}
	
	public void setDomainName(String name) {
		this.domainName = name;
	}
	
	public String getDomainName() {
		return domainName;
	}
	
	public void setCriteria(String criteria) {
		this.criteria = criteria;
	}
	
	public String getCriteria() {
		return criteria;
	}
	
	public void setMaxFiles(String value) {
		this.maxfiles = value;
	}
	
	public String getMaxFiles() {
		return maxfiles;
	}
	
	private boolean isRunning() {
		if (commandId==null) return false;
		Command command = ServiceLocator.getService(CommandService.class).getCommand(commandId);
		if (command==null) return false;
		return command.getState()==CommandState.RUNNING;
	}
	
	@SuppressWarnings("static-access")
	private void start(AjaxRequestTarget target) {
		try {
			
			WebMarkupContainer statuspanel = (WebMarkupContainer)get("form-panel:status");
			
			CommandService service = ServiceLocator.getService(CommandService.class);
			
			//ImporterCommand command = new ImporterCommand();
			
			CloneCommand command = new CloneCommand(new LocalApiServiceWrapper(new DomainProxy(0, getDomainName())));
			
			//ContentDao contentdao = getContentDao();
			
			command.setParameter("groups", getGroups() ? "true" : "false");
			command.setParameter("templates", getTemplates() ? "true" : "false");
			command.setParameter("datasets", getDataSets() ? "true" : "false");
			command.setParameter("classifiers", getClassifiers() ? "true" : "false");
			command.setParameter("values", getValues() ? "true" : "false");
			command.setParameter("rules", getRules() ? "true" : "false");
			command.setParameter("users", getUsers() ? "true" : "false");
			command.setParameter("files", getFiles() ? "true" : "false");
			command.setParameter("freeze", getFreeze() ? "true" : "false");
			command.setParameter("criteria", getCriteria());
			command.setParameter("server", getServer());
//			String user = contentdao.findSystemParameterByKey("remote_user_importer").getValue();
//			command.setParameter("user", user);
//			String password = contentdao.findSystemParameterByKey("remote_password_importer").getValue();
//			command.setParameter("password", password);
//			command.setParameter("maxfiles", getMaxFiles());
			command.setParameter("domain", getDomain().getId());
			
			command.setPriority(SchedulerService.HIGH_PRIORITY);
			
			if (submitted) {
				logger.warn("ALREADY RUNNING");
				return;
			}
			
			commandId = (Long)command.getId();
			
			service.add(command);
			
			BatchCommandStatusPanel commandstatus = new BatchCommandStatusPanel("command", (long) command.getId(), false) {
				@Override
				public void onAfterExecution(AjaxRequestTarget target) {
					target.add(ImportPage.this.get("form-panel"));
				}
			};
				
			statuspanel.replace(commandstatus);
			
			submitted = true;
			
			Thread.currentThread().sleep(5000);
			
			target.add((WebMarkupContainer)get("form-panel"));
			
		}
		catch (Exception e) {
			logger.error(e);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PrintStream ps = new PrintStream(baos);
			e.printStackTrace(ps);
		}
	}
	
	private void cancel() {
		
		if (commandId==null) 
			return;
		Command command = ServiceLocator.getService(CommandService.class).getCommand(commandId);
		if (command==null) 
			return;
		command.stop();
	}
}
