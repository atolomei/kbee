package com.novamens.content.web.security.login;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.user.UserProfile;
import com.novamens.service.BrandingService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.ChoiceField;
import com.novamens.wicket.markup.html.form.PasswordField;
import com.novamens.wicket.markup.html.form.TextField;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.pages.RedirectPage;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * 
 * loginmodal-container-idoc idoc
 * 
 * 
 *
 */

public class LoginPanelV6 extends Panel {

	static final String default_learn_more_text 	= ServiceLocator.getService(BrandingService.class).getDefaultLoginLearMoreText();
	static final String default_learn_more_link  	= ServiceLocator.getService(BrandingService.class).getDefaultLoginLearMoreLink();
	
	static final String default_mesage_text  		= ServiceLocator.getService(BrandingService.class).getDefaultLoginMessage();
	
	static final String default_contact_text  		= ServiceLocator.getService(BrandingService.class).getDefaultContactText();
	static final String default_contact_link  		= ServiceLocator.getService(BrandingService.class).getDefaultContactLink();
	
	private static final long serialVersionUID = 1L;
	private List<String> domains = new ArrayList<>();
	private String domain="";
	private String usernameOrEmail ="";
	private String password="";

	private String realUsername="";

	private String previosUsernameOrEmail="";
	private String errorCode = null;

	public LoginPanelV6(String id) {
		super(id);
		addComponents(null);
	}

	public LoginPanelV6(String id, PageParameters parameters) {
		super(id);
		addComponents(parameters);
	}
	
	public void onInitialize() {
		super.onInitialize();
		
	}
	
	private void addComponents(PageParameters parameters) {
		
		WebMarkupContainer lbox = new WebMarkupContainer("box");
		add(lbox);
		
		if (parameters!=null)
			setErrorCode(parameters.get("login_error").toString());
		
		Form<?> form = new Form<Void>("form"){
			private static final long serialVersionUID = 1L;
			@Override
			protected CharSequence getActionUrl() {
				return "/j_spring_security_check";
			}
		};

		lbox.add(form);
		
		
		Label error_label = new Label("error", getLabel("errorcode-"+getErrorCode())) {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isVisible() {
				return  getErrorCode()!=null;
			}
		};


		error_label.setEscapeModelStrings(false);
		error_label.setOutputMarkupId(true);
		form.add(error_label);

		ChoiceField<String> domainSelector = new ChoiceField<>("domain", new PropertyModel<String>(this, "domain"), new PropertyModel<List<String>>(this, "domains"));
		domainSelector.setOutputMarkupId(true);
		domainSelector.setOutputMarkupPlaceholderTag(true);
		domainSelector.setVisible(false);

		form.add(domainSelector);

		@SuppressWarnings({ "unchecked", "rawtypes" })
		TextField username = new TextField("username",  new PropertyModel<String>(this, "usernameOrEmail")){
			private static final long serialVersionUID = 1L;

			@Override
			public IModel<String> getPlaceHolder() {
				return new Model<String>("username");
			}
			
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				super.onUpdate(target);
				domainSelector.setVisible(false);
				getDomains().clear();
				target.add(domainSelector);
			}

			@Override
			protected boolean autofocus() {
				return true;
			}
		};
		
		username.add(new AttributeModifier("placeholder", new StringResourceModel("username-or-email", this, null)));
		username.setOutputMarkupId(true);

		//username.getInput().add(new AttributeAppender("autofocus", "true"));
		form.add(username);

		HiddenField<String> realUsernameField = new HiddenField<String>("realUsername",  new PropertyModel<String>(this, "realUsername")){
			private static final long serialVersionUID = 1L;
			@Override
			public String getInputName() {
				return "j_username";
			}
		};
		realUsernameField.setOutputMarkupId(true);
		form.add(realUsernameField);

		PasswordField password =  new PasswordField("password",  new PropertyModel<String>(this, "password")) {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public IModel<String> getPlaceHolder() {
				return new Model<String>("password");
			}

			@Override
			public boolean isCentered() {
				return true;
			}
			
			@Override
			public boolean isShowPasswordLink() {
				return true;
			}
			
			@Override
			protected String getInputName() {
				return "j_password";
			}
		};
		form.add(password);



		Button submit = new Button("submit");
		submit.setOutputMarkupId(true);
		form.add(submit);

		AjaxSubmitLink tmpSubmit = new AjaxSubmitLink("tmpSubmit") {
			private static final long serialVersionUID = 1L;
			@Override
			protected void onSubmit(AjaxRequestTarget target) {
				super.onSubmit(target);
				boolean doSubmit = true;
				
				if (isEmail(LoginPanelV6.this.getUsernameOrEmail())) {
					List<UserProfile> userProfiles = getUsersProfilesFromEmail(LoginPanelV6.this.getUsernameOrEmail());
					boolean newUsernameOrEmailValue = !previosUsernameOrEmail.equals(usernameOrEmail);
					if(newUsernameOrEmailValue){ //new username try to update domains
						getDomains().clear();
						if (userProfiles.size()>0) {
							boolean validLogin = validateLogin(userProfiles.get(0).getUser().getUserName(), LoginPanelV6.this.getPassword());
							if (validLogin) {
								List<String> emailDomain = userProfiles.stream().map(p -> p.getDomain().getName()).collect(Collectors.toList());
								getDomains().addAll(emailDomain);
								LoginPanelV6.this.setDomain(getDomains().get(0));
								domainSelector.setValue(LoginPanelV6.this.getDomain());
								domainSelector.setVisible(true);
								target.add(domainSelector);
								setErrorCode(null);
								target.add(error_label);
								doSubmit=false;
							}
						}
					} else {
						Optional<UserProfile> firstUs = userProfiles.stream().filter(up -> up.getDomain().getName().equals(LoginPanelV6.this.getDomain())).findFirst();
						if(firstUs.isPresent()){
							realUsername = firstUs.get().getUser().getUserName();
						}
					}
				} else {
					
					// realUsername = LoginPanelV6.this.getUsernameOrEmail();
					realUsername = LoginPanelV6.this.getUsernameOrEmail().toLowerCase();
				}

				realUsernameField.setModelValue(new String[]{realUsername});
				target.add(realUsernameField);

				if(getDomains().size() <= 1 || doSubmit){
					target.appendJavaScript(
							String.format("try { Wicket.$('%s').click(); } catch(err) { if (window.console != undefined) { console.log(err); } }"
									, submit.getMarkupId()));
				}

				previosUsernameOrEmail = usernameOrEmail;
			}
		};

		form.add(tmpSubmit);
		form.setDefaultButton(tmpSubmit);
		
		
		
		
		
		// Contact or Subscription
		//
		WebMarkupContainer ct_c= new WebMarkupContainer("contact-container");
		Link<Void> ct_l = new Link<Void> ("contact-link") {
			private static final long serialVersionUID = 1L;
			public void onClick() {
				setResponsePage(new RedirectPage(default_contact_link));
			}
		};
		
		ct_c.setVisible((default_contact_text!=null && default_contact_text.length()>0) || (default_contact_link!=null && default_contact_link.length()>0));
		Label ctla_t = new Label("contact-text", default_contact_text);
		ctla_t.setEscapeModelStrings(false);
		ct_l.add(ctla_t);
		ct_c.add(ct_l);
		form.add(ct_c);

		
		

		
		Image logo = null;
		WebMarkupContainer lcon = new WebMarkupContainer("logo-container");
		lbox.add(lcon);
		
		
		WebMarkupContainer logo_link = new WebMarkupContainer("logo-link");
		logo_link.add(new AttributeModifier("href", getServerUrl()));
		lcon.add(logo_link);
		
		logo = new Image("logo", ServiceLocator.getService(com.novamens.kbee.wicket.services.BrandingWebService.class).getLoginLogo());
		logo_link.add(logo);
		
		
		
		lbox.add(new AttributeModifier("class", "loginmodal-container-idoc"));
		/**
		 * 
		 */
		// Disclaimer
		//
		WebMarkupContainer message_c= new WebMarkupContainer("message-container");
		
		message_c.setVisible(default_mesage_text!=null && default_mesage_text.length()>0);
		Label message = new Label("message", default_mesage_text);
		message.setEscapeModelStrings(false);
		message_c.add(message);
		lbox.add(message_c);

		
		
		

		
		

		// Learn more text and link
		//
		WebMarkupContainer learn_more_c= new WebMarkupContainer("learn-more-container");

		WebMarkupContainer lml = new WebMarkupContainer("learn-more-link");
		lml.add( new AttributeModifier("target", "_blank"));
		lml.add( new AttributeModifier("href", default_learn_more_link));
		Label learn_more_t = new Label("learn-more-text", default_learn_more_text);
		lml.add(learn_more_t);
		learn_more_c.add(lml);
		
				
		/**
		Link<Void> learn_more_l = new Link<Void> ("learn-more-link") {
			private static final long serialVersionUID = 1L;
			public void onClick() {
				setResponsePage(new RedirectPage(default_learn_more_link));
			}
		};
		**/
		
		
		learn_more_c.setVisible((default_learn_more_link!=null && default_learn_more_link.length()>0) || (default_learn_more_text!=null && default_learn_more_text.length()>0));
		
		//Label learn_more_t = new Label("learn-more-text", default_learn_more_text);
		//learn_more_t.setEscapeModelStrings(false);
		//learn_more_l.add(learn_more_t);		
		//learn_more_c.add(learn_more_l);
		
		
		lbox.add(learn_more_c);
				
		
		add(lbox);
	}
	

	private boolean validateLogin(String username, String password){
		AuthenticationManager authManager = (AuthenticationManager)ServiceLocator.getService(BeansService.class).getBean("com.novamens.security.service.AuthenticationManager");
		UsernamePasswordAuthenticationToken authReq
				= new UsernamePasswordAuthenticationToken(username, password);
		try {
			Authentication auth = authManager.authenticate(authReq);
		}catch (BadCredentialsException e){
			return false;
		}
		return true;

	}

	private boolean isEmail(String email) {
		String regex = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
		return email.matches(regex);
	}


	private List<UserProfile> getUsersProfilesFromEmail(String email){
		ContentDao contentDao = getContentDao();
		List<UserProfile> userByEmail = contentDao.findUserProfileByPersonEmail(email);
		return userByEmail;

	}

	private ContentDao getContentDao() {
		return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}


	private void setErrorCode(String err) {
		errorCode=err;
	}

	private String getErrorCode() {
		return errorCode;
	}
	
	private StringResourceModel getLabel(String key) {
		return new StringResourceModel(key, this, null);
	}



	public List<String> getDomains() {
		return domains;
	}

	public void setDomains(List<String> domains) {
		this.domains = domains;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getUsernameOrEmail() {
		return usernameOrEmail;
	}

	public void setUsernameOrEmail(String usernameOrEmail) {
		this.usernameOrEmail = usernameOrEmail;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	

	protected String getServerUrl() {
		String protocol =((WebRequest)RequestCycle.get().getRequest()).getUrl().getProtocol();
		String host =((WebRequest)RequestCycle.get().getRequest()).getUrl().getHost();
		Integer iport =((WebRequest)RequestCycle.get().getRequest()).getUrl().getPort(); 
		String port = (iport.equals(80) || iport.equals(443) ? "":  ( ":" + iport.toString()) );
		return protocol +"://" + host + port;
	}

	
}
	
			

