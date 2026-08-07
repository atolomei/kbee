package com.novamens.content.web.security.markup;


import java.util.HashMap;
import java.util.List;
import java.util.Map;



import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.validation.validator.EmailAddressValidator;

import com.novamens.beans.BeansService;

import com.novamens.content.dao.ContentDao;
import com.novamens.content.entity.Person;

import com.novamens.content.service.domain.DomainSettingsService;
import com.novamens.content.user.UserProfile;
import com.novamens.dom.Domain;
import com.novamens.email.EmailService;
import com.novamens.kbee.wicket.markup.html.page.AbstractKbeeWebPage;
import com.novamens.kbee.wicket.services.BrandingWebService;
import com.novamens.service.BrandingService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.Field;

import kbee.email.EmailBuilderGetMyUserName;
import kbee.util.PropertiesFactory;
import kbee.web.page.ApplicationMenuSection;

@Deprecated
public class ForgotUsernamePage extends AbstractKbeeWebPage {
	
	private static final long serialVersionUID = 1L;
	
	private static final ResourceReference BOOTSTRAP_CSS = new CssResourceReference(Field.class, com.novamens.wicket.markup.html.form.Form.BOOTSTRAP);
	
	private static final ResourceReference CSS_KBEE_BOOTSTRAP			 = new CssResourceReference(AbstractKbeeWebPage.class, "kbeebootstrap.css");
	private static final ResourceReference CSS_KBEE_BOOTSTRAP_0_360      = new CssResourceReference(AbstractKbeeWebPage.class, "kb-0-360.css");
	private static final ResourceReference CSS_KBEE_BOOTSTRAP_361_800    = new CssResourceReference(AbstractKbeeWebPage.class, "kb-361-800.css");
	private static final ResourceReference CSS_KBEE_BOOTSTRAP_801_1200   = new CssResourceReference(AbstractKbeeWebPage.class, "kb-801-1200.css");
	private static final ResourceReference CSS_KBEE_BOOTSTRAP_1201_1600  = new CssResourceReference(AbstractKbeeWebPage.class, "kb-1201-1600.css");
	private static final ResourceReference CSS_KBEE_BOOTSTRAP_1601  	 = new CssResourceReference(AbstractKbeeWebPage.class, "kb-1601.css");
	
	
	
 	private static final String XUA_Compatible =  PropertiesFactory.getInstance("kbee").getProperties().getProperty("com.novamens.kbee.portal.xuacompatible", "IE=Edge");
 	

	/** ---------------------------------------------------------------------------------
	 */
	
	public class FormularioReset extends Form<Void> {

		private static final long serialVersionUID = 1L;
		
		private FeedbackPanel feedback;
		
		private String email = "";
		private String phone = "";
		public String getEmail() 					{return email;}
		public String getPhone() 					{return phone;}
		public void setEmail(String email) 			{this.email=email;}
		public void setPhone(String phone) 			{this.phone=phone;}

		/** -----------------------------------------------------
		 */
		
		public FormularioReset(String id) {
			super(id);
	
			setOutputMarkupId(true);
			
			feedback = new FeedbackPanel("feedback") {
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
	        	        
			RequiredTextField<String> email = new RequiredTextField<String>("email");
			email.setModel(new PropertyModel<String>(this, "email"));
			email.add(EmailAddressValidator.getInstance());
			add(email);
			
			RequiredTextField<String> phone = new RequiredTextField<String>("phone");
			phone.setModel(new PropertyModel<String>(this, "phone"));
			add(phone);

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
						 rsm.setParameters(getEmail(), getPhone());
						 setPhone("");
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

			submit.add(new AttributeModifier("value", (new StringResourceModel("submit", ForgotUsernamePage.this, null)).getString()));
			add(submit);
		}
		
		/** -----------------------------------------------------
		 */
		public FeedbackPanel getFeedbackPanel() {
			return feedback;
		}
 
		/** -----------------------------------------------------
		 */
		
		public String update() {
			String errcode = null;
			List<Person> list = getContentDao().findPersonByEmail(getEmail());
			boolean bexists = false;
			if (list!=null) {
						
				for (Person person: list) {
					
					if (	person.getPhone()!=null &&  
							person.getPhone().length()>=4 && 
							person.getPhone().endsWith(getPhone())) {
							bexists = true;
							
							
							
							String to =  getEmail();
							String language = person.getProfile(UserProfile.class).getUser().getLocale().getLanguage();
							
							/**
							Map<String, Object> map = new HashMap<String, Object>();
							map.put("${user-username}", username);
							map.put("${person-phone-last-four-digits}", fourdigits);
							map.put("${person-email-address}", person.getEmail());
							map.put("${person-displayname}", person.getFirstLastName());
							map.put("${domain-noreply}", noreply);
							map.put("${domain-name}", person.getDomain().getName());
							**/

							
							String username = person.getProfile(UserProfile.class).getUser().getUserName();
							String fourdigits= person.getPhone().substring(person.getPhone().length()-4, person.getPhone().length());
							String noreply = person.getDomain().getService(DomainSettingsService.class).get(DomainSettingsService.EMAIL_SERVICE_NO_REPLY);

							
							EmailService service = (EmailService) ServiceLocator.getService(EmailService.class);
							EmailBuilderGetMyUserName builder  = new EmailBuilderGetMyUserName(person, to);
							
							service.send(builder);
							
							//EmailBuilderGetMyUserName builder  = new EmailBuilderGetMyUserName(person.getDomain(), language, to, map);
							//service.sendGetMyUsername(person.getDomain(), language, to, map);
					}
				}
				
				if (!bexists) {
					StringResourceModel rsm = new StringResourceModel("forgotpassword.errormsg", this, null);
					rsm.setParameters(errcode);
					return rsm.getString();
				}
				else
					return null;
			}
			else 
				errcode = "3";
			
			StringResourceModel rsm = new StringResourceModel("forgotpassword.errormsg");
			rsm.setParameters(errcode);
			return rsm.getString();
		}
	}
	
	 
	public ForgotUsernamePage() {

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
		lcon.add(logo);
		canvas.add(lcon);
		canvas.add(new FormularioReset("form"));
		canvas.add(new AttributeModifier("class", ServiceLocator.getService(BrandingService.class).getLoginCss())); 
		add(canvas);
	}
	
	
	@Override
	public String getPageHelpKey() {
		return  getApplicationMenuSection().getKey() + "-" + this.getClass().getSimpleName().toLowerCase();
	}

	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.GENERAL;
	}

	
	/** ---------------------------------------------------------------------------------
	 */
	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}

	/** ---------------------------------------------------------------------------------
	 * Este debe sobrecargar a kbee2.css para usar bootstrap
	 */
	@Override
	protected ResourceReference getCssResource() {
		return BOOTSTRAP_CSS;
	}
	
	/** ---------------------------------------------------------------------------------
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

	
	

}
