package com.novamens.content.web.security.markup;

import java.util.Locale;

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
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.FeedbackPanel;

import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.validation.validator.EmailAddressValidator;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.user.UserProfile;
import com.novamens.email.EmailService;

import com.novamens.kbee.wicket.markup.html.page.AbstractKbeeWebPage;
import com.novamens.security.User;
import com.novamens.service.BrandingService;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.Field;

import kbee.email.EmailBuilderSelfServicePasswordReset;
import kbee.util.PropertiesFactory;
import kbee.web.page.ApplicationMenuSection;

public class ForgotPasswordPage extends com.novamens.kbee.wicket.markup.html.page.AbstractKbeeWebPage {
	
	private static final long serialVersionUID = 7790707817432573708L;

	private static final ResourceReference CSS_KBEE_BOOTSTRAP			 = new CssResourceReference(AbstractKbeeWebPage.class, "kbeebootstrap.css");
	private static final ResourceReference CSS_KBEE_BOOTSTRAP_0_360      = new CssResourceReference(AbstractKbeeWebPage.class, "kb-0-360.css");
	private static final ResourceReference CSS_KBEE_BOOTSTRAP_361_800    = new CssResourceReference(AbstractKbeeWebPage.class, "kb-361-800.css");
	private static final ResourceReference CSS_KBEE_BOOTSTRAP_801_1200   = new CssResourceReference(AbstractKbeeWebPage.class, "kb-801-1200.css");
	private static final ResourceReference CSS_KBEE_BOOTSTRAP_1201_1600  = new CssResourceReference(AbstractKbeeWebPage.class, "kb-1201-1600.css");
	private static final ResourceReference CSS_KBEE_BOOTSTRAP_1601  	 = new CssResourceReference(AbstractKbeeWebPage.class, "kb-1601.css");
	private static final ResourceReference BOOTSTRAP_CSS 				 = new CssResourceReference(Field.class, com.novamens.wicket.markup.html.form.Form.BOOTSTRAP);

 	private static final String XUA_Compatible =  PropertiesFactory.getInstance("kbee").getProperties().getProperty("com.novamens.kbee.portal.xuacompatible", "IE=Edge");
	
 	private Locale locale;
	
	public Locale getLocale() {
		return  locale;
	}
	
	
	public class FormularioReset extends Form<Void> {

		private static final long serialVersionUID = 1L;
		
		private FeedbackPanel feedback;
		
		private String email 	= "";
		private String username = "";

		public String getEmail() 					{return email;}
		public String getUsername() 				{return username;}
		public void setEmail(String email) 			{this.email=email!=null?email.trim():email;}
		public void setUsername(String username) 	{this.username=username!=null?username.trim(): username;}

		public FormularioReset(String id) {
			super(id);
	
			setOutputMarkupId(true);
			
			this.feedback = new FeedbackPanel("feedback") {
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
			
			feedback.setEscapeModelStrings(false);
	        feedback.setOutputMarkupId(true);
	        
	        add(feedback);
	        	        
			RequiredTextField<String> user = new RequiredTextField<String>("username");
			user.setModel(new PropertyModel<String>(this, "username"));
			add(user);
			
			RequiredTextField<String> correo = new RequiredTextField<String>("email");
			correo.setModel(new PropertyModel<String>(this, "email"));
			correo.add(EmailAddressValidator.getInstance());
			add(correo);

			Button submit = new AjaxButton("submit", this) {
				private static final long serialVersionUID = 1L;
			
				@Override 
				protected void onSubmit(AjaxRequestTarget target) {
					String msg = ((FormularioReset) getForm()).update();
					 if (msg!=null) {
						 ((FormularioReset) getForm()).getFeedbackPanel().error(msg);
						 onError(target);
					} else {
						
						 StringResourceModel rsm = new StringResourceModel("forgotpassword.successmsg", this);
						 rsm.setParameters(getEmail(), getUsername());
						 setUsername("");
						 setEmail("");
						 target.add(getForm());
						 ((FormularioReset) getForm()).getFeedbackPanel().info(rsm.getString());
						 onError(target);
					}
				}
				@Override
				protected void onError(AjaxRequestTarget target) {
					target.add(feedback);
				}
			};

			submit.add(new AttributeModifier("value", (new StringResourceModel("forgotpassword.label.submit", ForgotPasswordPage.this, null)).getString()));
			
			add(submit);
		}
		
		public FeedbackPanel getFeedbackPanel() {
			return feedback;
		}
 
		public String update() {
			
			// -----
			//
			// get users with the same email address
			//
			// -----
		 
			
			
			
			String errcode = null;
			
			User user =  ServiceLocator.getService(SecurityService.class).findUserByUsername(getUsername().trim());

			if (user!=null) {
				
						UserProfile userprofile = getContentDao().findUserProfileByUser(user);
						
						if (!user.isEnabled()) {
							errcode = "6";
						}
						else if (userprofile.getEntity().getEmail()!=null && userprofile.getEntity().getEmail().toLowerCase().trim().equals(getEmail().toLowerCase().trim())) {
 							EmailBuilderSelfServicePasswordReset builder = new EmailBuilderSelfServicePasswordReset(userprofile.getPerson());
							ServiceLocator.getService(EmailService.class).send(builder);
							return null;
 						}
							else 
								errcode = "4";
				}
				else
					errcode = "3";
			
			if (errcode.equals("5")) {
				StringResourceModel rsm = new StringResourceModel("forgotpassword.errorkbeemsg", this);
				rsm.setParameters(errcode);
				return rsm.getString();
			}
			else {
				StringResourceModel rsm = new StringResourceModel("forgotpassword.errormsg", this);
				rsm.setParameters(errcode);
				return rsm.getString();
			}
		}
	}
	


	/** ---------------------------------------------------------------
	 * 
	 * 
	 */

	public ForgotPasswordPage() {

		locale = ((WebRequest)RequestCycle.get().getRequest()).getLocale();
		//locale = Locale.forLanguageTag("es");
		
		getSession().setLocale(locale);
		
		setPageFonts(getFonts());
		setPageTitle(new Model<String>(ServiceLocator.getService(BrandingService.class).getApplicationName()));
		setPageXUACompatible(XUA_Compatible);
		
		Image logo = null;
		
		WebMarkupContainer lcon = new WebMarkupContainer("logo-container");
		
		logo = new Image("logo", ServiceLocator.getService(com.novamens.kbee.wicket.services.BrandingWebService.class).getLoginLogo()) {
					private static final long serialVersionUID = 1L;
					protected boolean shouldAddAntiCacheParameter()	{
						return false;
					}
		};

		WebMarkupContainer canvas = new WebMarkupContainer("canvas");
		
		Label fpi =new Label("forgotpassword-instructions", getContentDao().findSystemParameterValueByKey("forgot-password-instructions", new StringResourceModel("forgotpassword.instructions", this, null).getObject()));
		fpi.setEscapeModelStrings(false);
		canvas.add(fpi);
		
		lcon.add(logo);
		canvas.add(lcon);
		canvas.add(new FormularioReset("form"));
		canvas.add(new AttributeModifier("class", ServiceLocator.getService(BrandingService.class).getLoginCss())); 
		add(canvas);
	}

	
	/** ---------------------------------------------------------------
	 */

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

	
	/** 
	 * Este debe sobrecargar a kbee2.css para usar bootstrap
	 */
	@Override
	protected ResourceReference getCssResource() {
		return BOOTSTRAP_CSS;
	}
	
	@Override
	public String getPageHelpKey() {
		return  getApplicationMenuSection().getKey() + "-" + this.getClass().getSimpleName().toLowerCase();
	}

	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.GENERAL;
	}

	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
}

