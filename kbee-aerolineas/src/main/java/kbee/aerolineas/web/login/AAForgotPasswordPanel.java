package kbee.aerolineas.web.login;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.user.UserProfile;
import com.novamens.email.EmailService;
import com.novamens.security.User;
import com.novamens.service.BrandingService;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.ChoiceField;
import com.novamens.wicket.markup.html.form.PasswordField;
import com.novamens.wicket.markup.html.form.TextField;

import kbee.email.EmailBuilderSelfServicePasswordReset;
import kbee.web.panel.AlertPanel;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.markup.html.form.RequiredTextField;
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
import org.apache.wicket.validation.validator.EmailAddressValidator;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

public class AAForgotPasswordPanel extends Panel {

	static final String default_learn_more_text = ServiceLocator.getService(BrandingService.class).getDefaultLoginLearMoreText();
	static final String default_learn_more_link = ServiceLocator.getService(BrandingService.class).getDefaultLoginLearMoreLink();

	static final String default_mesage_text = ServiceLocator.getService(BrandingService.class).getDefaultLoginMessage();

	static final String default_contact_text = ServiceLocator.getService(BrandingService.class).getDefaultContactText();
	static final String default_contact_link = ServiceLocator.getService(BrandingService.class).getDefaultContactLink();

	private static final long serialVersionUID = 1L;

	private String errorCode = null;

	private Form<Void> form = new Form<Void>("form");

	private  WebMarkupContainer box = new WebMarkupContainer("box");

	private Locale locale;

	private String email = "";
	private String username = "";


	
	public Locale getLocale() {
		return locale;
	}

		public String getEmail() {
		return email;
	}

	public String getUsername() {
		return username;
	}

	public void setEmail(String email) {
		this.email = email != null ? email.trim() : email;
	}

	public void setUsername(String username) {
		this.username = username != null ? username.trim() : username;
	}

	
	
	
	public AAForgotPasswordPanel(String id) {
		super(id);
		addComponents(null);
	}

	public AAForgotPasswordPanel(String id, PageParameters parameters) {
		super(id);
		addComponents(parameters);
	}

	public void onInitialize() {
		super.onInitialize();

	}

	
	private void addComponents(PageParameters parameters) {

		add(box);
		box.add(new AALoginLogoPanel("logo-panel"));

		// Label fpi =new Label("forgotpassword-instructions",
		// getContentDao().findSystemParameterValueByKey("forgot-password-instructions",
		// new StringResourceModel("forgotpassword.instructions", this,
		// null).getObject()));

		box.add(form);
		
		Label fpi = new Label("forgotpassword-instructions", getLabel("instructions"));
		fpi.setEscapeModelStrings(false);
		form.add(fpi);

		TextField<String> user = new  TextField<String>("username");
		user.setModel(new PropertyModel<String>(this, "username"));
		form.add(user);

		TextField<String> correo = new TextField<String>("email");
		correo.setModel(new PropertyModel<String>(this, "email"));
		correo.add(EmailAddressValidator.getInstance());
		form.add(correo);

		Button submit = new AjaxButton("submit", form) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target) {
				String msg = update();
				if (msg != null) {
					AlertPanel<String> alert = new AlertPanel<String>("feedback",AlertPanel.DANGER, Model.of(msg), null, Model.of(msg));
					form.addOrReplace(alert);
					target.add(form);
				} else {

					StringResourceModel rsm = new StringResourceModel("forgotpassword.successmsg", this);
					rsm.setParameters(getEmail(), getUsername());
					setUsername("");
					setEmail("");
					AlertPanel<String> alert = new AlertPanel<String>("feedback",AlertPanel.INFO, Model.of("success"), null, rsm);
					form.addOrReplace(alert);
					target.add(form);
				}
			}

			@Override
			protected void onError(AjaxRequestTarget target) {
			}
		};

		submit.add(new AttributeModifier("value", getLabel("send").getObject()));

		form.add(submit);
		
		form.add(new com.novamens.kbee.wicket.util.InvisiblePanel("feedback"));
		

	}

	protected String getServerUrl() {
		String protocol = ((WebRequest) RequestCycle.get().getRequest()).getUrl().getProtocol();
		String host = ((WebRequest) RequestCycle.get().getRequest()).getUrl().getHost();
		Integer iport = ((WebRequest) RequestCycle.get().getRequest()).getUrl().getPort();
		String port = (iport.equals(80) || iport.equals(443) ? "" : (":" + iport.toString()));
		return protocol + "://" + host + port;
	}

	
	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}

	
	protected IModel<String> getLabel(String key) {
		return new StringResourceModel(key, this, null);
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
		
		//if (errcode.equals("5")) {
		//	StringResourceModel rsm = new StringResourceModel("forgotpassword.errorkbeemsg", this);
		//	rsm.setParameters(errcode);
		//	return rsm.getString();
		//}
		///lse {
		
		StringResourceModel rsm = new StringResourceModel("forgotpassword.errormsg", this);
		rsm.setParameters(errcode);

		String s=rsm.getString();
		
		return s;
		
		//}
	}
	
}
