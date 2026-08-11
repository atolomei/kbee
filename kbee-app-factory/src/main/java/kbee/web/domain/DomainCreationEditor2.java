package kbee.web.domain;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.validation.IFormValidator;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;

import com.novamens.content.service.domain.DomainBuilderService;
import com.novamens.content.user.UserProfile;
import com.novamens.dom.Domain;
import com.novamens.dom.DomainType;
import com.novamens.email.EmailService;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.form.BooleanField;
import com.novamens.wicket.markup.html.form.ChoiceField;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.Form.Disposition;

import kbee.email.EmailBuilderNewDomain;
import kbee.email.EmailBuilderWelcomeMessageBasic;
import kbee.email.EmailBuilderWelcomeMessageCompliance;
import kbee.email.EmailBuilderWelcomeMessagePremium;
import kbee.util.PropertiesFactory;
import kbee.web.form.EditButtonsV5;
import kbee.web.form.LocaleField;
import kbee.web.form.ZoneIdField;
import kbee.web.security.UsernameValidator;

import com.novamens.wicket.markup.html.form.TextField;

@Deprecated
public class DomainCreationEditor2 extends ObjectEditor<Domain> {

	private static final long serialVersionUID = 1L;

	static public final Long DEFAULT_DOMAIN_ID = Long.valueOf(-1);
					
	static String EXPRESS 				 = "Express";  				            // Basic Consulta de 1 portal library [API]
	static String PREMIUM_ASSIGN		 = "Premium - Enterprise";              // Assign Workflow";
	static String COMPLIANCE_MONITORING  = "Premium - Compliance Monitoring";
	static String NO_MODEL 				 = "Premium - No Model";


	private static final int MINIMUM_LENGTH = 8;
	
	static final int TEST_OFF = 0;
	static final int TEST_ON  = 1;
	
	static private kbee.util.logging.Logger logger = new kbee.util.logging.Logger(DomainCreationEditor2.class.getName());

	
	static String SERVICE_EMAIL = PropertiesFactory.getInstance("kbee").getProperties().getProperty("service.monitor.email", "").trim();
	static String ROOT_EMAIL = PropertiesFactory.getInstance("kbee").getProperties().getProperty("newdomain.root.email.default", SERVICE_EMAIL).trim();
	static String PWD_DEFAULT = PropertiesFactory.getInstance("kbee").getProperties().getProperty("newdomain.root.password.default", "id0cR00t").trim();
	
	static String DEFAULT_SERVICE_MONITOR = PropertiesFactory.getInstance("kbee").getProperties().getProperty("service.monitor.email",  SERVICE_EMAIL);
	static String DEFAULT_ROOT_PASSWORD   = PropertiesFactory.getInstance("kbee").getProperties().getProperty("newdomain.root.password.default",  PWD_DEFAULT);
	static String DEFAULT_ROOT_EMAIL	  = PropertiesFactory.getInstance("kbee").getProperties().getProperty("newdomain.root.email.default",  ROOT_EMAIL);

	private boolean usernameupdated = false;
	
	private String testresult;
	private String testResultCss;
	private String externalid;

	// Name ----------------------------------------------
	//
	private String name;
	private String organization;
	
	// Type ----------------------------------------------
	//
	private IModel<String> information_model;
	
	// Admin User ----------------------------------------
	//
	private String 	adminFirstName;
	private String 	adminLastName;
	private String 	adminEmail;
	private String 	adminUsername;
	private Boolean adminNotify = Boolean.valueOf(true);
	private Locale locale;
	private ZoneId zoneid;
	
						
	public void setExternalId(String afn) {this.externalid=afn;}
	public String getExternalId()		 {return this.externalid;}

	
	public void setAdminName(String afn) {this.adminFirstName=afn;}
	public String getAdminName() {return this.adminFirstName;}
	
	public void setAdminLastname(String aln) {this.adminLastName=aln;}
	public String getAdminLastname() {return this.adminLastName;}
						
	public void setAdminEmail(String ae) {this.adminEmail=ae;}
	public String getAdminEmail() {return this.adminEmail;}
				
	public void setAdminUsername(String ae) {this.adminUsername=ae;}
	public String getAdminUsername() {return this.adminUsername;}
	
	public void setAdminNotify(Boolean ae)  {this.adminNotify=ae;}
	public Boolean getAdminNotify()		    {return this.adminNotify;}
	
	private int test_state = TEST_ON;
	
	/** 
	 *  <span>Validate Domain name</span>
	 *  name is lower case, just letters and numbers and no spaces
	 *  domain name must not exists
	 */
	class NameValidator implements IValidator<String> {

		private static final long serialVersionUID = 1L;

		@Override
		public void validate(final IValidatable<String> validatable) {
			
			final String dname = validatable.getValue();

			if (!dname.matches("[a-z|0-9]+")) {
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
		
		private static final long serialVersionUID = 1L;
		
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
	 * DomainType is Serializable, so no need to do any work.
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
	
	

	public DomainCreationEditor2() {
			super("editor");
	}
	
	public DomainCreationEditor2(String id) {
		super(id);
		
	
	}


	@Override
	public void onInitialize() {
		super.onInitialize();
		setInformationModel(new Model<String>(PREMIUM_ASSIGN));
		addForm();
		addButtons();
		addResultsPanel();
	}
	
	
	public String getTestResultCss() 							{return this.testResultCss;}
	public void setTestResultCss(String css) 					{this.testResultCss = css;}
	
	public String getName() 									{return name;}
	public void setName(String name) 							{this.name = name;}
	
	public String getOrganization() 							{return organization;}
	public void setOrganization(String organization) 			{this.organization = organization;}

	
	public String getTestresult() 								{return testresult;}
	public void setTestresult(String res)	 					{this.testresult=res;}

	
	
	/**
	 * 
	 * 
	 */
	public void update(AjaxRequestTarget target) {
						
		DomainBuilderService service = ServiceLocator.getService(DomainBuilderService.class);

		String service_monitor_email 		= getContentDao().findSystemParameterValueByKey("service.monitor.email",    DEFAULT_SERVICE_MONITOR );
		String root_password 		 		= getContentDao().findSystemParameterValueByKey("newdomain.root.password",  DEFAULT_ROOT_PASSWORD );
		String root_email 	 		 		= getContentDao().findSystemParameterValueByKey("newdomain.root.email", 	DEFAULT_ROOT_EMAIL );
		
		Domain domain = null;
		boolean isAPI   = ! getInformationModel().getObject().equals(NO_MODEL);
		boolean isModel = ! getInformationModel().getObject().equals(NO_MODEL);
		
		DomainType dt;
		
		
		if (getInformationModel().getObject().equals(PREMIUM_ASSIGN)) 						dt = DomainType.PREMIUM; 
		else if (getInformationModel().getObject().equals(COMPLIANCE_MONITORING))			dt = DomainType.COMPLIANCE;
		else if (getInformationModel().getObject().equals(NO_MODEL))						dt = DomainType.PREMIUM;
		else																				dt = DomainType.EXPRESS;
		
		Map<String, Object> map = new HashMap<String, Object>();
					
		map.put("externalid", this.getExternalId());
		map.put("name", this.getName());
		map.put("organization", this.getOrganization());
		map.put("type", String.valueOf(dt.getId()));
		map.put("root_password", root_password);
		map.put("root_email",root_email ); 
		map.put("admin_username", this.getAdminUsername()); 
		map.put("admin_firstname", this.getAdminName());
		map.put("admin_lastname", this.getAdminLastname());
		map.put("admin_email", this.getAdminEmail());
		map.put("api",  isAPI?"yes":"no");
		map.put("ismodel", isModel?"yes":"no");
					
		map.put("locale", getLocale());
		map.put("timezone", getZoneId());
		
		
		domain = service.createDomain(this.getName(), map);
		
		if (dt==DomainType.EXPRESS) {
			service.setUpModelBasic(domain);
			service.setUpRolesBasic(domain);
			service.setUpUsersBasic(domain, map);
		}
		else if (dt==DomainType.PREMIUM) {
			service.setUpModelPremium(domain, isModel ? "premium-api" : "premium-none");
			service.setUpRolesPremium(domain, isModel ? "premium-api" : "premium-none");
			service.setUpUsersPremium(domain, map, isModel ? "premium-api" : "premium-none");
		}
		else if (dt==DomainType.COMPLIANCE) {
			service.setUpModelPremium(domain, "premium-api");
			service.setUpRolesPremium(domain, "premium-api");
			service.setUpUsersPremium(domain, map, "premium-api");
		}

		final String adminEmail = this.getAdminEmail();
		if (domain!=null && getAdminNotify() && adminEmail != null) {
			logger.debug(this.getAdminUsername()+"@" + domain.getName());
			User admin_user = ServiceLocator.getService(com.novamens.service.SecurityService.class).findUserByUsername(this.getAdminUsername()+"@" + domain.getName());
			if ( admin_user!=null) {
				UserProfile profile = getContentDao().findUserProfileByUser(admin_user);
				
				DomainType type=profile.getPerson().getDomain().getDomainType();


				if (type==DomainType.COMPLIANCE){
					EmailBuilderWelcomeMessageCompliance builder = new EmailBuilderWelcomeMessageCompliance(profile.getPerson(), adminEmail, admin_user.getFirstLastName());
					ServiceLocator.getService(EmailService.class).send(builder);
				}
				else if (type==DomainType.EXPRESS){
					EmailBuilderWelcomeMessageBasic builder = new EmailBuilderWelcomeMessageBasic(profile.getPerson(), adminEmail, admin_user.getFirstLastName());
					ServiceLocator.getService(EmailService.class).send(builder);
				}
				else {
					EmailBuilderWelcomeMessagePremium builder = new EmailBuilderWelcomeMessagePremium(profile.getPerson(), adminEmail, admin_user.getFirstLastName());
					ServiceLocator.getService(EmailService.class).send(builder);
				}
			}
			else
				logger.error(this.getAdminUsername()+"@" + domain.getName() + "  does not exist.");
		}
		
		
		EmailBuilderNewDomain  builder = new EmailBuilderNewDomain (domain,  service_monitor_email, "Service Monitor");
		ServiceLocator.getService(EmailService.class).send(builder);
		
		//ServiceLocator.getService(EmailService.class).sendNewDomainMessage(domain, map, service_monitor_email, "Service Monitor");
		
		setResponsePage(new DomainsPage());
	}
	
	@Override
	public void edit(AjaxRequestTarget target) {
		logger.info("Edit");
		
	}


	@SuppressWarnings("unchecked")
	@Override
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
	

	private void addButtons() {
		add(new EditButtonsV5<Domain>(this));
	}

	
	

	private void addForm() {
		
		setOutputMarkupId(true);

		Form<Void> form = new Form<Void>("form", Disposition.VERTICAL);
		
		
		setLocale(getSessionUser().getLocale());

		form.add(new LocaleField("locale", new PropertyModel<Locale>(this, "locale"), true) {
			private static final long serialVersionUID = 1L;
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
		
		// Type
		//
		form.add(new ChoiceField<String> 	 ("informationmodel",  	information_model, 	new PropertyModel<List<String>>(this, "InformationModels"), true));
		
		boolean  not_check_availability = false;
		
		// Admin user 
		form.add(new TextField<String>("admin-name", 			new PropertyModel<String>(this, "adminName"), false) {
			private static final long serialVersionUID = 1L;
			public void onUpdate(AjaxRequestTarget target) {
				DomainCreationEditor2.this.onUpdate(target);
			}
		});

		form.add(new TextField<String>("admin-username", 	new PropertyModel<String>(this, "adminUsername"), true, new UsernameValidator(not_check_availability)) {
			private static final long serialVersionUID = 1L;
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
			private static final long serialVersionUID = 1L;

			public void onUpdate(AjaxRequestTarget target) {
				DomainCreationEditor2.this.onUpdate(target);
			}
		});

		form.add(new TextField<String>("admin-email", 			new PropertyModel<String>(this, "adminEmail")));							
		form.add(new BooleanField 	  ("admin-notify", 			new PropertyModel<Boolean>(this, "adminNotify")));
		form.add(new TextField<String>("externalid", 			new PropertyModel<String>(this, "externalid")));							

		
		add(form);
	}
	
	@SuppressWarnings("unchecked")
	protected void onUpdate(AjaxRequestTarget target) {

		if (!usernameupdated) {
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
	
	public IModel<String> getInformationModel() {
		return information_model;
	}

	public void  setInformationModel( IModel<String> m) {
		information_model = m;
	}
	
	public List<String> getInformationModels() {
		List<String> list = new ArrayList<String>();
		list.add(EXPRESS);
		list.add(PREMIUM_ASSIGN);
		list.add(COMPLIANCE_MONITORING);
		list.add(NO_MODEL);
		return list;
	}
	

	public TextField<?> getNameField() {
		return (TextField<?>)getForm().get("name");
	}
	

	@Override
	public void onDetach() {
		super.onDetach();
	}
	
	public TextField<?> getPassword1Field() {
		return (TextField<?>)getForm().get("password1");
	}

	
	public TextField<?> getPassword2Field() {
		return (TextField<?>)getForm().get("password2");
	}

	protected int getTestState() {
		return test_state;
	}
	
	protected void setTestState(int state) {
		test_state=state;
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
	

	protected User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	
	private void addResultsPanel() {
		add(new InvisiblePanel("results"));
	}
}
