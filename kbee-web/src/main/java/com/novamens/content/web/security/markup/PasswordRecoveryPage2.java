package com.novamens.content.web.security.markup;


import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wicket.Application;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;

import org.apache.wicket.markup.html.form.PasswordTextField;

import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.string.StringValue;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.user.UserProfile;
import com.novamens.kbee.security.KbeeUser;

import com.novamens.kbee.wicket.markup.html.page.AbstractKbeeWebPage;
import com.novamens.kbee.wicket.util.GenericPhoto;
import com.novamens.security.User;
import com.novamens.service.BrandingService;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.Field;
import com.novamens.wicket.markup.html.form.PasswordField;
import com.novamens.wicket.model.ObjectModel;

import kbee.util.PropertiesFactory;
import kbee.web.page.ApplicationMenuSection;



//@Deprecated
@SuppressWarnings("serial")
public class PasswordRecoveryPage2 extends AbstractKbeeWebPage {
	private static final long serialVersionUID = 1L;

	// private static final PackageResourceReference REALPAGE_ICON = new PackageResourceReference(PasswordRecoveryPage2.class, "logo-realpage.png");

	private static final ResourceReference CSS_KBEE_BOOTSTRAP			 = new CssResourceReference(AbstractKbeeWebPage.class, "kbeebootstrap.css");
	private static final ResourceReference CSS_KBEE_BOOTSTRAP_0_360      = new CssResourceReference(AbstractKbeeWebPage.class, "kb-0-360.css");
	private static final ResourceReference CSS_KBEE_BOOTSTRAP_361_800    = new CssResourceReference(AbstractKbeeWebPage.class, "kb-361-800.css");
	private static final ResourceReference CSS_KBEE_BOOTSTRAP_801_1200   = new CssResourceReference(AbstractKbeeWebPage.class, "kb-801-1200.css");
	private static final ResourceReference CSS_KBEE_BOOTSTRAP_1201_1600  = new CssResourceReference(AbstractKbeeWebPage.class, "kb-1201-1600.css");
	private static final ResourceReference CSS_KBEE_BOOTSTRAP_1601  	 = new CssResourceReference(AbstractKbeeWebPage.class, "kb-1601.css");
	
	private static final ResourceReference BOOTSTRAP_CSS 		= new CssResourceReference(Field.class, com.novamens.wicket.markup.html.form.Form.BOOTSTRAP);

	static private Logger logger = LogManager.getLogger(PasswordRecoveryPage2.class.getName());

	private IModel<User> user_model;
	private FeedbackPanel mainFeedback;
	
	private static final String XUA_Compatible =  PropertiesFactory.getInstance("kbee").getProperties().getProperty("com.novamens.kbee.portal.xuacompatible", "IE=Edge");

	/**
	static final Image KBEE_LOGO = new Image("logo", KBEE_ICON) {
		private static final long serialVersionUID = 1L;
		protected boolean shouldAddAntiCacheParameter()	{
			return false;
		}
	}; 
	**/
	
	static final int MINIMUM_LENGTH = 8;
	static final String SPECIALS 	= "[!@#\\$%\\^-_]";

	public  PasswordRecoveryPage2 () {
		this(null);
	}

	public PasswordRecoveryPage2 (PageParameters parameters) {

		getSession().setLocale(((WebRequest)RequestCycle.get().getRequest()).getLocale());
		
		setPageXUACompatible(XUA_Compatible);

		setPageFonts(getFonts());
		
		setPageTitle(new Model<String>(ServiceLocator.getService(BrandingService.class).getProductKey()));
		
		Image logo = null;
		
		WebMarkupContainer lbox = new WebMarkupContainer("lbox");
		add(lbox);
		
		WebMarkupContainer lcon = new WebMarkupContainer("logo-container");
		lbox.add(lcon);
		
		logo = new Image("logo", ServiceLocator.getService(com.novamens.kbee.wicket.services.BrandingWebService.class).getLoginLogo());
		
		logo.add(new AttributeModifier("style",	"width: 40%;	    padding: 10%;"));
		
		lcon.add(logo);
		
		Label welcome= new Label("title", new Model<String>() {
			private static final long serialVersionUID = 1L;
			@Override
			public String getObject() {
				return  new StringResourceModel("reset-password", PasswordRecoveryPage2.this, null).getObject();
			}
		});

		lbox.add(welcome);
		
		boolean valid = false;
		
		if (parameters!=null) {
			 if (parameters.get("key")!=null) {
				 String token = parameters.get("key").toString();
				 if (ServiceLocator.getService(SecurityService.class).isValid(token)) {
					 String userid=ServiceLocator.getService(SecurityService.class).getUserId(token);
					 setUser(ServiceLocator.getService(SecurityService.class).findUserById(userid));
					 valid=true;
				 }
				 else
					 valid = false;
			 }
		 }
		
		lbox.add((new PasswordResetForm("form", valid)).setVisible(valid));

		this.mainFeedback = new FeedbackPanel("feedback") {
			
			private static final long serialVersionUID = 1L;
				@Override
				protected void onComponentTag(ComponentTag tag) {
					super.onComponentTag(tag);
					if (this.anyErrorMessage()) {
						tag.append("class", "error", " ");
					} 
					else if (anyMessage(FeedbackMessage.SUCCESS)) {
						tag.append("class", "ok", " ");
					}
				}
			};

			this.mainFeedback.setVisible(false);
			this.mainFeedback.setEscapeModelStrings(false);
			this.mainFeedback.setOutputMarkupId(true);
		
			lbox.add(mainFeedback);
	        
	        if (!valid) {
	        	this.mainFeedback.error("The page has expired.");
	        	this.mainFeedback.setVisible(true);
	        }
	        
	}

	
	@Override
	public void onDetach() {
		super.onDetach();
		
		if (user_model!=null)
			user_model.detach();
			
	}

	
	@Override
	public String getPageHelpKey() {
		return  getApplicationMenuSection().getKey() + "-" + this.getClass().getSimpleName().toLowerCase();
	}

	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.GENERAL;
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(CSS_KBEE_BOOTSTRAP));
		response.render(CssHeaderItem.forReference(CSS_KBEE_BOOTSTRAP_0_360));
		response.render(CssHeaderItem.forReference(CSS_KBEE_BOOTSTRAP_361_800 ));
		response.render(CssHeaderItem.forReference(CSS_KBEE_BOOTSTRAP_801_1200));
		response.render(CssHeaderItem.forReference(CSS_KBEE_BOOTSTRAP_1201_1600));
		response.render(CssHeaderItem.forReference(CSS_KBEE_BOOTSTRAP_1601));
	}
	
	@Override
	protected void onAfterRender()	{
		super.onAfterRender();

		// 
		/**
		 * 		Application webapplication = Application.get();
		 * 
		if (!(webapplication instanceof BaseWebApplication))
			return;
			
		if (!((BaseWebApplication)webapplication).maintenance())
			return;
		StringValue supervisorparameter = getPageParameters().get("supervisor");
		boolean supervisor = supervisorparameter!=null && "true".equals(supervisorparameter.toString()) ? true  : false;
		if (!supervisor)
			getRequestCycle().setResponsePage(new MaintenancePage());
		**/
	}
	

	/** ------------------------------------------------------------------------------------------------------
	 * Este debe sobrecargar a kbee2.css para usar bootstrap
	 */
	@Override
	protected ResourceReference getCssResource() {
		return BOOTSTRAP_CSS;
	}
								
	private String getUserName() {
		return ( (this.user_model!=null && this.user_model.getObject()!=null) ? this.user_model.getObject().getUserName():"");
	}
	
	
	private void setUser(User user) {
		this.user_model = new ObjectModel<User>(user);
	}
	
	
	private User getUser() {
		if (this.user_model!=null && this.user_model.getObject()!=null)
			return this.user_model.getObject();
		return null;
	}
	
	public class PasswordResetForm extends Form<Void> {
		private static final long serialVersionUID = 1L;
		
		private FeedbackPanel feedback;
		
		private String password = "";
		private String passwordcheck = "";
		
		public String getPassword() 						{return password;}
		public String getPasswordCheck() 					{return passwordcheck;}
		public void setPassword(String password) 			{this.password=password;}
		public void setPasswordCheck(String passwordcheck) 	{this.passwordcheck=passwordcheck;}
		
		public PasswordResetForm (String id) {
				this(id, false);
		}
		
		public PasswordResetForm (String id, boolean valid) {
			super(id);

			setOutputMarkupId(true);
		
			Label username = new Label("username", getUserName());
			username.setVisible(valid);
			add(username);
						
			feedback = new FeedbackPanel("feedback") {
				@Override
				protected void onComponentTag(ComponentTag tag) {
					super.onComponentTag(tag);
					if (this.anyErrorMessage()) {
						tag.append("class", "error", " ");
					} 
					else if (anyMessage(FeedbackMessage.SUCCESS)) {
						tag.append("class", "ok", " ");
					}
				}
			};

			feedback.setVisible(false);
			feedback.setEscapeModelStrings(false);
			feedback.setOutputMarkupId(true);
			add(feedback);
			
			if (!valid)
				setVisible(false);
		
			
			add(new PasswordField("password", new PropertyModel<String>(this, "password"), true) {
				
				public boolean isShowPasswordLink() {
					return true;
				}
				
				public boolean isCentered() {
					return true;
				}
				
				
				
			});
			add(new PasswordField("password-verification", new PropertyModel<String>(this, "passwordcheck"), true) {
				
				public boolean isShowPasswordLink() {
					return true;
				}
				public boolean isCentered() {
					return true;
				}
			});
			
			
			/**
			PasswordTextField pwd = new PasswordTextField("password");
			pwd.setModel(new PropertyModel<String>(this, "password"));
			pwd.setRequired(true);
			pwd.setResetPassword(false);
			pwd.setVisible(valid);
			add(pwd);
			PasswordTextField pwdcheck = new PasswordTextField("password-verification");
			pwdcheck.setModel(new PropertyModel<String>(this, "passwordcheck"));
			pwdcheck.setRequired(true);
			pwdcheck.setResetPassword(false);
			pwdcheck.setVisible(valid);
			add(pwdcheck);
			**/
			
		
			Button submit = new AjaxButton("submit", this) {
				@Override 
				protected void onSubmit(AjaxRequestTarget target) {
					
					String msg = ((PasswordResetForm) getForm()).update();
					
					KbeeUser user = (KbeeUser) getUser();
					
					Locale locale = user.getLocale();
					
					ResourceBundle res = ResourceBundle.getBundle(PasswordRecoveryPage2.class.getName(), locale);
					
					if (msg!=null && msg.length()>0) {
						((PasswordResetForm) getForm()).getFeedbackPanel().error(msg);
						onError(target);
						PasswordResetForm.this.feedback.setVisible(true);
					} 
					else {
						ServiceLocator.getService(SecurityService.class).authenticate(user.getName());
						// solo una instancia de usuario por sesion: la del profile de usario
						UserProfile profile = getContentDao().findUserProfileByUser(user);
						user = (KbeeUser)profile.getUser();
						user.setPassword(getPassword());
						try {
							List<String> ulist = new ArrayList<String>();
							ulist.add("Password Reset");
							ServiceLocator.getService(com.novamens.content.service.SecurityContentMgmtService.class).update(user, ulist);
							setPassword("");
							setPasswordCheck("");
							target.add(getForm());
							
							feedback.info(res.getString("success")); 
							feedback.add(new AttributeModifier("class", "alert alert-info col-xs-12"));
							 
							PasswordResetForm.this.feedback.setVisible(true);
							target.add(feedback);
						} 
						catch (Exception e) {
							if (logger.isDebugEnabled()) {
								logger.error("error", e);
							}
							((PasswordResetForm) getForm()).getFeedbackPanel().error(res.getString("error")+" " + e.getMessage());
							PasswordResetForm.this.feedback.setVisible(true);
							onError(target);
						}
					}
				}
				@Override
				protected void onError(AjaxRequestTarget target) {
					PasswordResetForm.this.feedback.setVisible(true);
					target.add(feedback.getParent());
				}
			};
		
			
			submit.add(new AttributeModifier("value", new StringResourceModel("save", this, null).getObject()));
			
			submit.setVisible(valid);
			add(submit);
		}
	
		/** 
		 * 
		 * 
		 * 
		 */
		private String update() {
			Locale locale = Locale.getDefault(); 
			ResourceBundle res = ResourceBundle.getBundle(PasswordRecoveryPage2.class.getName(), locale);
			StringBuilder str= new StringBuilder();

			if (getPassword()==null) 
				str.append(res.getString("complete-pwd"));  // "Completar contraseña"

			if (getPasswordCheck()==null) {
				if(str.length()>0)
					str.append("<br />");
				str.append(res.getString("complete-verification"));  // 
			}
		
			if (getPasswordCheck()!=null && getPassword()!=null) {
				if (getPassword().length()<MINIMUM_LENGTH) {
					if(str.length()>0)
						str.append("<br />");
					str.append(res.getString("minimum-8chars")); // "Minimo 8 caracteres"
				}
				if (!getPasswordCheck().equals(getPassword())) {
					if(str.length()>0)
						str.append("<br />");
					str.append(res.getString("password-verification-no-match")); 
				}
				if (!hasNumber(getPassword())) { 
					if(str.length()>0)
						str.append("<br />");
					str.append(res.getString("does-not-have-number"));
				}
				if (!hasCapitalLetter(getPassword())) { 
					if(str.length()>0)
						str.append("<br />");
					str.append(res.getString("does-not-have-capital-letter"));
				}
			}
		
			if(str.length()>0)
				str.append(".");
		
			return str.toString();
		}
		
		public boolean hasNumber(String pwd) {
			return pwd.matches(".*[0-9].*");
		}
		
		public boolean hasSpecials(String pwd) {
			return pwd.matches(SPECIALS);
		}
		
		public boolean hasCapitalLetter(String pwd) {
			return pwd.matches(".*[A-Z].*");
		}
		
		public FeedbackPanel getFeedbackPanel() {
			return feedback;
		}
	
		private ContentDao getContentDao() {
			return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
		}
	}
}
