package kbee.aerolineas.web.login;

import com.novamens.service.BrandingService;
import com.novamens.service.ServiceLocator;
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
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.util.ArrayList;
import java.util.List;

public class AALoginPanel2 extends Panel {

	static final String default_learn_more_text = 
			ServiceLocator.getService(BrandingService.class).getDefaultLoginLearMoreText();
	static final String default_learn_more_link = 
			ServiceLocator.getService(BrandingService.class).getDefaultLoginLearMoreLink();
	
	static final String default_mesage_text	= 
			ServiceLocator.getService(BrandingService.class).getDefaultLoginMessage();
	
	static final String default_contact_text = 
			ServiceLocator.getService(BrandingService.class).getDefaultContactText();
	
	static final String default_contact_link = 
			ServiceLocator.getService(BrandingService.class).getDefaultContactLink();
	
	private static final long serialVersionUID = 1L;
	private List<String> domains = new ArrayList<>();
	private String domain="";
	private String usernameOrEmail ="";
	private String password="";

	private String errorCode = null;

	
	WebMarkupContainer box = new WebMarkupContainer("box");
	private String realUsername="";
	
	
	public AALoginPanel2(String id) {
		super(id);
		addComponents(null);
	}

	public AALoginPanel2(String id, PageParameters parameters) {
		super(id);
		addComponents(parameters);
	}
	
	public void onInitialize() {
		super.onInitialize();
		
	}
	
	
	private void addComponents(PageParameters parameters) {
		
		add(box);
		
		//box.add(new AttributeModifier("class", "loginmodal-container-idoc"));
		//box.add( new AALoginLogoPanel("logo-panel"));
		
		if (parameters!=null)
			setErrorCode(parameters.get("login_error").toString());
		
		Form<?> form = new Form<Void>("form"){
			private static final long serialVersionUID = 1L;
			@Override
			protected CharSequence getActionUrl() {
				return "/j_spring_security_check";
			}
		};

		box.add(form);
		
		
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

		 

		TextField<String> username = new TextField<String>("username",  new PropertyModel<String>(this, "usernameOrEmail")){
			private static final long serialVersionUID = 1L;

			@Override
			public IModel<String> getPlaceHolder() {
				return  getLabel("username-or-email");
			}
			
			 

			@Override
			protected boolean autofocus() {
				return true;
			}
		};
		
		username.setLabel(getLabel("username-or-email"));
		
		
		username.add(new AttributeModifier("placeholder", new StringResourceModel("username-or-email", this, null)));
		username.setOutputMarkupId(true);

		form.add(username);
		

		
		PasswordField password =  new PasswordField("password",  new PropertyModel<String>(this, "password")) {
			private static final long serialVersionUID = 1L;
			@Override
			public IModel<String> getPlaceHolder() {
				return getLabel("password");
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

		password.setLabel(getLabel("password"));


		Button submit = new Button("submit");
		submit.setOutputMarkupId(true);
		form.add(submit);

	 
		HiddenField<String> realUsernameField = new HiddenField<String>("realUsername",  new PropertyModel<String>(this, "realUsername")){
			private static final long serialVersionUID = 1L;
			@Override
			public String getInputName() {
				return "j_username";
			}
		};
		realUsernameField.setOutputMarkupId(true);
		form.add(realUsernameField);
		
		AjaxSubmitLink tmpSubmit = new AjaxSubmitLink("tmpSubmit") {
			private static final long serialVersionUID = 1L;
			@Override
			protected void onSubmit(AjaxRequestTarget target) {
				super.onSubmit(target);
				boolean doSubmit = true;
				realUsername = AALoginPanel2.this.getUsernameOrEmail().toLowerCase();
				

				realUsernameField.setModelValue(new String[]{realUsername});
				target.add(realUsernameField);

				if(getDomains().size() <= 1 || doSubmit){
					target.appendJavaScript(
							String.format("try { Wicket.$('%s').click(); } catch(err) { if (window.console != undefined) { console.log(err); } }"
									, submit.getMarkupId()));
				}

			 
			}
		};
		
		
		tmpSubmit.add(new AttributeModifier("value",  getLabel("signin").getString()));
		

		form.add(tmpSubmit);
		form.setDefaultButton(tmpSubmit);

	 
		 
	}
	

//	private boolean validateLogin(String username, String password){
//		AuthenticationManager authManager = (AuthenticationManager)ServiceLocator.getService(BeansService.class).getBean("com.novamens.security.service.AuthenticationManager");
//		UsernamePasswordAuthenticationToken authReq
//				= new UsernamePasswordAuthenticationToken(username, password);
//		try {
//			Authentication auth = authManager.authenticate(authReq);
//		}catch (BadCredentialsException e){
//			return false;
//		}
//		return true;
//
//	}
//
//	private boolean isEmail(String email) {
//		String regex = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
//		return email.matches(regex);
//	}
//
//
//	private List<UserProfile> getUsersProfilesFromEmail(String email){
//		ContentDao contentDao = getContentDao();
//		List<UserProfile> userByEmail = contentDao.findUserProfileByPersonEmail(email);
//		return userByEmail;
//
//	}

//	private ContentDao getContentDao() {
//		return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
//	}


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
	
			

