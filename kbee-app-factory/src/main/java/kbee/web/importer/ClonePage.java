package kbee.web.importer;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.PropertyModel;

import com.novamens.content.command.Command;
import com.novamens.content.command.CommandState;
import com.novamens.content.service.DomainService;
import com.novamens.content.web.command.batch.markup.BatchCommandStatusPanel;
import com.novamens.dom.Domain;
import com.novamens.kbee.command.CommandService;
import com.novamens.kbee.domain.KbeeDomain;
import com.novamens.kbee.domain.KbeeReplica;
import com.novamens.scheduler.SchedulerService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.CheckField;
import com.novamens.wicket.markup.html.form.ChoiceField;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.markup.html.form.TextField;

import kbee.replica.KbeeLocalReplica;
import kbee.replica.KbeeReplicaStandBy;
import kbee.replica.Replica;
import kbee.replica.ReplicaCommand;
import kbee.replica.ReplicaType;
import kbee.web.page.AbstractApplicationPage;

@SuppressWarnings("serial")
public class ClonePage extends AbstractApplicationPage<Void> {
	private static final long serialVersionUID = 1L;
	
	static Logger logger = LogManager.getLogger(ClonePage.class.getName());
	
	private boolean groups;
	private boolean users;
	private boolean templates;
	private boolean eMailTemplates;
	private boolean files;
	private boolean classifiers;
	private boolean datasets;
	private boolean values;
	private boolean libraries;
	private boolean settings;
	private boolean attributes;
	private boolean resourcetags;
	private boolean launchergroups;
	private boolean roles;
	private boolean facets;
	private boolean force;
	private String domainName;
	private String criteria;
	private String maxfiles;
	
	private String location = "Local";
	
	private String remote = "http://localhost:8080/api";
	private String user = "root@novamens";
	private String password = "1Aqqqqqq";
	
	
	private boolean submitted = false;
	
	Long commandId;
	
	public ClonePage() {
		
		setTopNavigation(getMainTopbar());
		  
		setMenu(getMainLaternalMenu());
		
		final WebMarkupContainer panel = new WebMarkupContainer("form-panel");
		
		panel.setOutputMarkupId(true);
		
		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);
		
		form.add(new ChoiceField<String>("location", new PropertyModel<String>(this, "location"), ()->getLocations()) {
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				super.onUpdate(target);
				updateModel();
				target.add(panel);
			}
		});
		
		WebMarkupContainer local = new WebMarkupContainer("local") { 
			public boolean isVisible() {
				return "Local".equals(getLocation());
			}
		};
		
		local.add(new TextField<String>("domain", new PropertyModel<String>(this, "domainName")));
		
		form.add(local);
		
		WebMarkupContainer remote = new WebMarkupContainer("remote") { 
			public boolean isVisible() {
				return "Remote".equals(getLocation());
			}
		};
		
		remote.add(new TextField<String>("remote", new PropertyModel<String>(this, "remote")));
		remote.add(new TextField<String>("user", new PropertyModel<String>(this, "user")));
		remote.add(new TextField<String>("password", new PropertyModel<String>(this, "password")));
		
		form.add(remote);
		
		form.add(new CheckField("groups",new PropertyModel<Boolean>(this, "groups")));
		form.add(new CheckField("templates",new PropertyModel<Boolean>(this, "templates")));
		form.add(new CheckField("emailtemplates",new PropertyModel<Boolean>(this, "eMailTemplates")));
		form.add(new CheckField("classifiers", new PropertyModel<Boolean>(this, "classifiers")));
		form.add(new CheckField("attributes", new PropertyModel<Boolean>(this, "attributes")));
		form.add(new CheckField("resourcetags", new PropertyModel<Boolean>(this, "resourcetags")));
		form.add(new CheckField("launchergroups", new PropertyModel<Boolean>(this, "launchergroups")));
		form.add(new CheckField("datasets", new PropertyModel<Boolean>(this, "dataSets")));
		form.add(new CheckField("libraries", new PropertyModel<Boolean>(this, "libraries")));
		form.add(new CheckField("values", new PropertyModel<Boolean>(this, "values")));
		form.add(new CheckField("roles", new PropertyModel<Boolean>(this, "roles")));
		form.add(new CheckField("settings", new PropertyModel<Boolean>(this, "settings")));
		form.add(new CheckField("facets", new PropertyModel<Boolean>(this, "facets")));
		
		form.add(new CheckField("force", new PropertyModel<Boolean>(this, "force")));

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
				target.add(ClonePage.this.get("form-panel"));
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
	
	
	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getRemote() {
		return remote;
	}

	public void setRemote(String remote) {
		this.remote = remote;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String passowrd) {
		this.password = passowrd;
	}
	
	public List<String> getLocations() {
		List<String> locations = new ArrayList<String>();
		locations.add("Local");
		locations.add("Remote");
		return locations;
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
	
	public void setValues(boolean value) {
		this.values = value;
	}
	
	public boolean isAttributes() {
		return attributes;
	}

	public void setAttributes(boolean attributes) {
		this.attributes = attributes;
	}

	public boolean isResourcetags() {
		return resourcetags;
	}

	public void setResourcetags(boolean resourcetags) {
		this.resourcetags = resourcetags;
	}


	public boolean isLaunchergroups() {
		return launchergroups;
	}


	public void setLaunchergroups(boolean launchergroups) {
		this.launchergroups = launchergroups;
	}


	public boolean isLibraries() {
		return libraries;
	}

	public void setLibraries(boolean libraries) {
		this.libraries = libraries;
	}

	public boolean isSettings() {
		return settings;
	}

	public void setSettings(boolean settings) {
		this.settings = settings;
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
	
	public boolean isRoles() {
		return roles;
	}

	public void setRoles(boolean roles) {
		this.roles = roles;
	}

	public boolean iseMailTemplates() {
		return eMailTemplates;
	}

	public void seteMailTemplates(boolean eMailTemplates) {
		this.eMailTemplates = eMailTemplates;
	}

	public boolean isFacets() {
		return facets;
	}

	public void setFacets(boolean facets) {
		this.facets = facets;
	}

	public boolean isForce() {
		return force;
	}


	public void setForce(boolean force) {
		this.force = force;
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
			
			ReplicaType replicaType = "Local".equals(getLocation()) 
				? ReplicaType.LOCAL
				: ReplicaType.STANDBY;
			
			Replica replica = getReplica(replicaType);
			
//			CloneCommand command = new CloneCommand(api) {
//				public Task createTask() {
//					return new WebTask();
//				}
//			};
			
			ReplicaCommand command = new ReplicaCommand(replica);
			
			command.setParameter("groups", getGroups() ? "true" : "false");
			command.setParameter("templates", getTemplates() ? "true" : "false");
			command.setParameter("emailtemplates", iseMailTemplates() ? "true" : "false");
			command.setParameter("datasets", getDataSets() ? "true" : "false");
			command.setParameter("classifiers", getClassifiers() ? "true" : "false");
			command.setParameter("values", getValues() ? "true" : "false");
			command.setParameter("attributes", isAttributes() ? "true" : "false");
			command.setParameter("resourcetags", isResourcetags() ? "true" : "false");
			command.setParameter("launchergroups", isLaunchergroups() ? "true" : "false");
			command.setParameter("roles", isRoles() ? "true" : "false");
			command.setParameter("libraries", isLibraries() ? "true" : "false");
			command.setParameter("force", isForce() ? "true" : "false");
			command.setParameter("users", getUsers() ? "true" : "false");
			command.setParameter("files", getFiles() ? "true" : "false");
			command.setParameter("settings", isSettings() ? "true" : "false");
			command.setParameter("facets", isFacets() ? "true" : "false");
			command.setParameter("criteria", getCriteria());
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
					target.add(ClonePage.this.get("form-panel"));
				}
			};
				
			statuspanel.replace(commandstatus);
			
			submitted = true;
			
			Thread.currentThread().sleep(5000);
			
			target.add((WebMarkupContainer)get("form-panel"));
			
		}
		catch (Exception e) {
			e.printStackTrace();
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
	
	private Replica getReplica (ReplicaType type) {
		KbeeReplica replica = null;
		
		KbeeDomain domain = (KbeeDomain)getContentDao().reload(getDomain());
		
		for (Replica domainreplica : ((KbeeDomain)domain).getReplicas()) {
			if (domainreplica.getType().equals(type)) {
				if ((ReplicaType.STANDBY.equals(type) && domainreplica.getServer().equals(getRemote())) ||
					(ReplicaType.LOCAL.equals(type) && ((KbeeLocalReplica)domainreplica).getLocalDomain().getName().equals(getDomainName()))) {
					replica = (KbeeReplica)domainreplica;
					break;
				}
			}
		}
		
		if (replica==null) {
			if (type.equals(ReplicaType.STANDBY)) {
				replica = new KbeeReplicaStandBy();
				replica.setUser(getUser());
				replica.setPassword(getPassword());
				replica.setServer(getRemote());
			}	
			if (type.equals(ReplicaType.LOCAL)) {
				replica = new KbeeLocalReplica();
				Domain localDomain = getContentDao().findDomainByName(getDomainName());
				if (localDomain==null) throw new RuntimeException("domain not found");
				((KbeeLocalReplica)replica).setLocalDomain(localDomain);
			}	
			replica.setLastModifiedDate(OffsetDateTime.now());
			replica.setLastModifiedUser(getSessionUser());
			domain.addReplica(replica);
			domain.getService(DomainService.class).update(List.of("replicas"));
		}
		
		return replica;
	}
}
