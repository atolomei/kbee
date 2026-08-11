package kbee.web.domain;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.validation.IFormValidator;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

import com.novamens.beans.BeansService;
import com.novamens.content.command.Command;
import com.novamens.content.command.CommandState;
import com.novamens.content.dao.ContentDao;

import com.novamens.dom.Domain;
import com.novamens.dom.DomainType;
import com.novamens.kbee.command.CommandService;
import com.novamens.kbee.content.domain.provisioning.CreateDomainCommand;
import com.novamens.kbee.content.webapi.controller.LocalApiServiceWrapper;
import com.novamens.kbee.content.workflow.WebTask;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.scheduler.SchedulerService;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.form.BooleanField;
import com.novamens.wicket.markup.html.form.ChoiceField;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.Form.Disposition;

import kbee.util.PropertiesFactory;
import kbee.web.form.EditButtonsV5;
import kbee.web.form.LocaleField;
import kbee.web.form.ZoneIdField;
import kbee.web.security.UsernameValidator;

import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.workflow.Task;

@SuppressWarnings("serial")
public class DomainCreationEditor extends ObjectEditor<Domain> {
	private static final long serialVersionUID = 1L;

	static private kbee.util.logging.Logger logger = new kbee.util.logging.Logger(DomainCreationEditor.class.getName());
	
	private static final int MINIMUM_LENGTH = 8;
	
	static String SERVICE_EMAIL = PropertiesFactory.getInstance("kbee").getProperties().getProperty("service.monitor.email", "").trim();
	static String ROOT_EMAIL = PropertiesFactory.getInstance("kbee").getProperties().getProperty("newdomain.root.email.default", SERVICE_EMAIL).trim();
	static String PWD_DEFAULT = PropertiesFactory.getInstance("kbee").getProperties().getProperty("newdomain.root.password.default", "id0cR00t").trim();
	
	static String DEFAULT_SERVICE_MONITOR = PropertiesFactory.getInstance("kbee").getProperties().getProperty("service.monitor.email",  SERVICE_EMAIL);
	static String DEFAULT_ROOT_PASSWORD   = PropertiesFactory.getInstance("kbee").getProperties().getProperty("newdomain.root.password.default",  PWD_DEFAULT);
	static String DEFAULT_ROOT_EMAIL	  = PropertiesFactory.getInstance("kbee").getProperties().getProperty("newdomain.root.email.default",  ROOT_EMAIL);

	private boolean usernameupdated = false;
	
	private String externalid;

	// Name ----------------------------------------------
	//
	private String name;
	private String organization;
	
	// Type ----------------------------------------------
	//
	//private IModel<String> information_model;
	private IModel<Domain> domain_model;
	
	// Admin User ----------------------------------------
	//
	private String adminFirstName;
	private String adminLastName;
	private String adminEmail;
	private String adminUsername;
	private Boolean adminNotify = Boolean.valueOf(true);
	private Locale locale;
	private ZoneId zoneid;
	
	private boolean submitted = false;
	
	static final public String domain_valid_regex = "[A-Za-z0-9\\-]+";
	
	/** 
	 *  <span>Validate Domain name</span>
	 *  name is lower case, just letters and numbers and no spaces
	 *  domain name must not exists
	 */
	class NameValidator implements IValidator<String> {
		@Override
		public void validate(final IValidatable<String> validatable) {
			
			final String dname = validatable.getValue();

			if (!dname.matches(domain_valid_regex)) {
				validatable.error(new ValidationError(this, "invalid_chars"));
				return;
			}
			
			if ((dname.toLowerCase().equals("kbee"))) {
				validatable.error(new ValidationError(this, "domainexists"));
				return;
			}
			
			if (!(dname.toLowerCase().equals(dname))) {
				validatable.error(new ValidationError(this, "mustbelowercase"));
				return;
			}
						
			Domain dom = getContentDao().findDomainByName(dname);
			if (dom!=null) {
				validatable.error(new ValidationError(this, "domainexists"));
				return;
			}
		}
	}
	
	/** 
	 *  Validate Passwords
	 *  8 characters minimum
	 *  1 Capital letter
	 *  1 Digit
	 */

	class PasswordsValidator implements IFormValidator {
		
		public String SPECIALS = "[!@#\\$%\\^-_]";
		
		public boolean hasNumber(String pwd) {
			return pwd.matches(".*[0-9].*");
		}
		
		public boolean hasSpecials(String pwd) {
			return pwd.matches(SPECIALS);
		}
		
		public boolean hasCapitalLetter(String pwd) {
			return pwd.matches(".*[A-Z].*");
		}

		
		@Override
		public void validate(org.apache.wicket.markup.html.form.Form<?> form) {
			
			if (    ((String) getPassword1Field().getInput().getDefaultModelObject())==null || 
					((String)getPassword2Field().getInput().getDefaultModelObject())==null) {
				
				ValidationError error = new ValidationError();
				error.addKey(getClass().getSimpleName());
				
				getPassword1Field().setError(error);
				getPassword2Field().setError(error);
				
				return;
			}
			
			String pwd1 = (String) getPassword1Field().getInput().getDefaultModelObject();
			
			if (((String)getPassword1Field().getInput().getDefaultModelObject()).length()<MINIMUM_LENGTH) {
				ValidationError error = new ValidationError();
				error.addKey("minimunlength");
				getPassword1Field().setError(error);
				return;
			
			}
			
			else if (!hasNumber(pwd1)) {
				ValidationError error = new ValidationError();
				error.addKey("musthavedigit");
				getPassword1Field().setError(error);
				return;
			}
			
			else if (!hasCapitalLetter(pwd1)) {
				ValidationError error = new ValidationError();
				error.addKey("musthavecapitalletter");
				getPassword1Field().setError(error);
				return;
			}
			
			else {
				
				String password1 = (String)getPassword1Field().getInput().getDefaultModelObject();
				String password2 = (String)getPassword2Field().getInput().getDefaultModelObject();
				if (!password1.equals(password2)) {
					ValidationError error = new ValidationError();
					error.addKey(getClass().getSimpleName());
					getPassword2Field().setError(error);
					return;
				}
			}
		}
		@Override
		public FormComponent<?>[] getDependentFormComponents() {
			return new FormComponent<?>[0];
		}
	}	

		
	/**
	 * 
	 *  
	 *  
	 *  
	 *  
	 *  
	 *  
	 *  
	 * DomainType is Serializable
	 */
	public class DomainTypeModel implements IModel <DomainType> {
		
		private static final long serialVersionUID = 1L;
		
		private  DomainType type;

		public DomainTypeModel( DomainType type) {
			setObject(type);
		}
		
		@Override
		public void detach() {
		}

		@Override
		public DomainType getObject() {
			return type;
		}

		@Override
		public void setObject(DomainType object) {
				this.type=object;
		}
	};
	
	public class StatusFragment extends Fragment {
		long commandId;
		public StatusFragment(String id, long commandId) {
			super(id, "status-fragment", DomainCreationEditor.this);
			setOutputMarkupId(true);
			this.commandId = commandId;
			
			add(new Label("step", new Model<String>() {
				public String getObject() {
					Command command = getCommand();
					if (command!=null) {
						return ((CreateDomainCommand)command).getStep();
					}
					return null;
				}
			}));
			
			add(new AbstractAjaxTimerBehavior(java.time.Duration.ofSeconds(1)) {
				@Override
				protected void onTimer(AjaxRequestTarget target) {
					if (getStatus()==CommandState.COMPLETED || getStatus()==CommandState.CANCELED || getStatus()==CommandState.ERROR) {
						this.stop(target);
						onAfterExecution(target);
					}
					refresh(target);
				}
			});
		}
		public void onAfterExecution(AjaxRequestTarget target) {
		}
		protected void refresh(AjaxRequestTarget target) {
			target.add(StatusFragment.this);		
		}
		private CommandState getStatus() {
			return getCommand()!=null ? getCommand().getState() : CommandState.UNKNOWN;
		}
		private Command getCommand() {
			return getCommandService().getCommand(commandId);
		}
		private CommandService getCommandService() { 
			return (CommandService) ServiceLocator.getService(CommandService.class);
		}
	}	
	
	public DomainCreationEditor() {
		super("editor");
		setOutputMarkupId(true);
	}
	
	public DomainCreationEditor(String id) {
		super(id);
		setOutputMarkupId(true);
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public IModel<Domain> getDomainModel() {
		return domain_model;
	}
	
	public void setDomainModel(IModel<Domain> model) {
		this.domain_model = model;
	}
	
	public String getOrganization() {
		return organization;
	}
	
	public void setOrganization(String organization) {
		this.organization = organization;
	}
	
	public void setExternalId(String afn) {
		this.externalid=afn;
	}
	
	public String getExternalId() {
		return this.externalid;
	}
	
	public void setAdminName(String afn) {
		this.adminFirstName=afn;
	}
	
	public String getAdminName() {
		return this.adminFirstName;
	}
	
	public void setAdminLastname(String aln) {
		this.adminLastName=aln;
	}
	
	public String getAdminLastname() {
		return this.adminLastName;
	}
						
	public void setAdminEmail(String ae) {
		this.adminEmail=ae;
	}
	
	public String getAdminEmail() {
		return this.adminEmail;
	}
				
	public void setAdminUsername(String ae) {
		this.adminUsername=ae;
	}
	
	public String getAdminUsername() {
		return this.adminUsername;
	}
	
	public void setAdminNotify(Boolean ae)  {
		this.adminNotify=ae;
	}
	
	public Boolean getAdminNotify()	{
		return this.adminNotify;
	}
		
	public Locale getLocale() {
		return this.locale;
	}

	public void setLocale(Locale locale) {
		this.locale=locale;
	}

	public ZoneId getZoneId() {
		return this.zoneid;
	}

	public void setZoneId(ZoneId zone) {
		this.zoneid=zone;
	}
	
	public List<IModel<Domain>> getDomains() {

		List<IModel<Domain>> domains = new ArrayList<IModel<Domain>>();
		
		
		for (Domain domain : getContentDao().getDomains()) {
			if (domain.isTemplate()) {
				domains.add(new ObjectModel<Domain>(domain));
			}
		}
		return domains;
	}
	
	@Override
	public void edit(AjaxRequestTarget target) {
		logger.info("Edit");
	}

	@Override
	@SuppressWarnings("unchecked")
	public org.apache.wicket.markup.html.form.Form<?> getForm() {
		return (Form<Void>) get("form");
	}

	@Override
	public IModel<Domain> getModel() {
		logger.info("model");
		return null;
	}
	
	@Override
	public Domain getModelObject() {
		return null;
	}

	@Override
	public boolean isEditionEnabled() {
		return true;
	}
	
	@Override
	public boolean isReadOnly() {
		return false;
	}
	
	@Override
	public boolean isFullWidth() {
		return false;
	}

	@Override
	public List<String> getUpdatedParts() {
		return null;
	}

	@Override
	public void setUpdatedPart(String updatedPart) {
	}
	
	public void cancel(AjaxRequestTarget target) {
		setResponsePage( new DomainsPage());
	}
	
	public void update(AjaxRequestTarget target) {
						
		String root_password = getContentDao().findSystemParameterValueByKey("newdomain.root.password",  DEFAULT_ROOT_PASSWORD );
		String root_email = getContentDao().findSystemParameterValueByKey("newdomain.root.email", 	DEFAULT_ROOT_EMAIL );
		
		WebMarkupContainer statuspanel = (WebMarkupContainer)get("status");
		
		CommandService commandService = ServiceLocator.getService(CommandService.class);
		
		Domain  domain = getDomainModel()!=null ? getDomainModel().getObject() : null;
		
		CreateDomainCommand command = new CreateDomainCommand(new LocalApiServiceWrapper(domain)) {
			public Task createTask() {
				WebTask task = new WebTask();
				task.setCancelEnabled(true);
				return task;
			}
		};
		
		command.setParameter("externalid", this.getExternalId());
		command.setParameter("name", this.getName());
		command.setParameter("organization", this.getOrganization());
		command.setParameter("domain", this.getDomainModel()!=null ? this.getDomainModel().getObject().getId() : null);
		command.setParameter("root_password", root_password);
		command.setParameter("root_email",root_email ); 
		command.setParameter("admin_username", this.getAdminUsername()); 
		command.setParameter("admin_firstname", this.getAdminName());
		command.setParameter("admin_lastname", this.getAdminLastname());
		command.setParameter("admin_email", this.getAdminEmail());
		command.setParameter("locale", getLocale());
		command.setParameter("timezone", getZoneId());
		command.setParameter("type", domain!=null? domain.getDomainType() : DomainType.PREMIUM);
		//command.setParameter("type", String.valueOf(DomainType.PREMIUM));
		command.setParameter("api",  "yes");
		command.setParameter("ismodel", "no");
		
		command.setPriority(SchedulerService.HIGH_PRIORITY);
		
		if (submitted) {
			logger.warn("ALREADY RUNNING");
			return;
		}
		
		commandService.add(command);
		
		StatusFragment commandstatus = new StatusFragment("command", (long) command.getId()) {
			@Override
			public void onAfterExecution(AjaxRequestTarget target) {
				target.add(DomainCreationEditor.this);
				setResponsePage(new DomainsPage());
			}
		};
			
		statuspanel.replace(commandstatus);
		
		submitted = true;
		
		try {
			Thread.sleep(1000);
		}
		catch (Exception e) {
			
		}
		
		target.add(this);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		addForm();
		addButtons();
		addResultsPanel();
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
	}
	
	
	private void addButtons() {
		add(new EditButtonsV5<Domain>(this) {
			protected String getSubmitClass() {
				return "btn btn-primary btn-lg";
			}
			
			protected String getEditClass() {
				return "btn btn-primary btn-lg";
			}

			protected String getCancelClass() {
				return "btn btn-default btn-lg";
			}
		});
	}
	
	
	@SuppressWarnings({"unchecked"})
	private void addForm() {

		Form<Void> form = new Form<Void>("form", Disposition.VERTICAL);
		
		setLocale(getSessionUser().getLocale());

		form.add(new LocaleField("locale", new PropertyModel<Locale>(this, "locale"), true) {
			@Override
			public List<Locale> getLocales() {
				List<Locale> list = new ArrayList<Locale>();
				list.add(Locale.ENGLISH);
				list.add(Locale.forLanguageTag("es"));
				return list;
			}
		});
		
		setZoneId(ZoneId.of(getContentDao().findSystemParameterValueByKey("timezone.default", getSessionUser().getTimeZone())));
		form.add(new ZoneIdField("timezone", new PropertyModel<ZoneId>(this, "zoneId"), true));

		// Name
		//
		form.add(new TextField<String>("name", new PropertyModel<String>(this, "name"), true, new NameValidator()));
		form.add(new TextField<String>("organization", 	new PropertyModel<String>(this, "organization"), true));
		
		// Template
		//
		form.add(new ChoiceField<IModel<Domain>> ("domain", new PropertyModel<IModel<Domain>>(this, "domainModel"),	new PropertyModel<List<IModel<Domain>>>(this, "domains"), false));
		
		boolean not_check_availability = false;
		
		// Admin user 
		form.add(new TextField<String>("admin-name", new PropertyModel<String>(this, "adminName"), false) {
			public void onUpdate(AjaxRequestTarget target) {
				DomainCreationEditor.this.onUpdate(target);
			}
		});

		form.add(new TextField<String>("admin-username", new PropertyModel<String>(this, "adminUsername"), true, new UsernameValidator(not_check_availability)) {
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				usernameupdated = true;
				String userName = getValue();
				if (userName != null)
					userName = userName.replaceAll("\\s", "");		

				    setAdminUsername(userName);			
				
				  ((TextField<String>) getForm().get("admin-username")).setValue(userName);
				  
				  target.add(getForm());
			}
		});
		
		form.add(new TextField<String>("admin-lastname", new PropertyModel<String>(this, "adminLastname"), true) {
			public void onUpdate(AjaxRequestTarget target) {
				DomainCreationEditor.this.onUpdate(target);
			}
		});

		form.add(new TextField<String>("admin-email", 	new PropertyModel<String>(this, "adminEmail")));							
		form.add(new BooleanField 	  ("admin-notify", 	new PropertyModel<Boolean>(this, "adminNotify")));
		form.add(new TextField<String>("externalid", 	new PropertyModel<String>(this, "externalid")));							
		
		add(form);
		
		WebMarkupContainer status = new WebMarkupContainer("status") {
			public boolean isVisible() {
				return true;
			}
		};
		
		status.add(new WebMarkupContainer("command"));

// ----------------------------------------
//
//		StatusFragment commandstatus = new StatusFragment("command", (long) 0) {
//			@Override
//			public void onAfterExecution(AjaxRequestTarget target) {
//				target.add(DomainCreationEditor.this);
//				setResponsePage(new DomainsPage());
//			}
//		};
//		
//		status.add(commandstatus);
//		
// ----------------------------------------
		
		
		
		add(status);
	}
	
	@SuppressWarnings("unchecked")
	protected void onUpdate(AjaxRequestTarget target) {

		if (!this.usernameupdated) {
			String firstName = ((TextField<String>)getForm().get("admin-name")).getValue();
			String lastName = ((TextField<String>)getForm().get("admin-lastname")).getValue();
			
			String userName = "";
			if (firstName!=null && !"".equals(firstName)) {
				userName += firstName.toLowerCase().charAt(0);
			}
			if (lastName!=null && !"".equals(lastName)) {
				userName += lastName.toLowerCase().trim();
			}
			if (!"".equals(userName)) {
				userName=userName.replaceAll("\\s", "");
				setAdminUsername(userName);
				((TextField<String>)getForm().get("admin-username")).setValue(userName);
				target.add(getForm());
			}
		}
	}

	public TextField<?> getNameField() {
		return (TextField<?>)getForm().get("name");
	}
	
	public TextField<?> getPassword1Field() {
		return (TextField<?>)getForm().get("password1");
	}
	
	public TextField<?> getPassword2Field() {
		return (TextField<?>)getForm().get("password2");
	}

	protected User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	
	private void addResultsPanel() {
		add(new InvisiblePanel("results"));
	}
}