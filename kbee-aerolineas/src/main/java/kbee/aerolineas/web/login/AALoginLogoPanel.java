package kbee.aerolineas.web.login;

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

public class AALoginLogoPanel extends Panel {

	static final String default_learn_more_text = ServiceLocator.getService(BrandingService.class).getDefaultLoginLearMoreText();
	static final String default_learn_more_link = ServiceLocator.getService(BrandingService.class).getDefaultLoginLearMoreLink();

	static final String default_mesage_text = ServiceLocator.getService(BrandingService.class).getDefaultLoginMessage();

	static final String default_contact_text = ServiceLocator.getService(BrandingService.class).getDefaultContactText();
	static final String default_contact_link = ServiceLocator.getService(BrandingService.class).getDefaultContactLink();

	private static final long serialVersionUID = 1L;
	private List<String> domains = new ArrayList<>();
	private String domain = "";
	private String usernameOrEmail = "";
	private String password = "";
	private String errorCode = null;


	public AALoginLogoPanel(String id) {
		super(id);

	}

	public void onInitialize() {
		super.onInitialize();

		WebMarkupContainer lcon = new WebMarkupContainer("logo-container");
		add(lcon);
		WebMarkupContainer logo_link = new WebMarkupContainer("logo-link");
		logo_link.add(new AttributeModifier("href", getServerUrl()));
		lcon.add(logo_link);
		Image logo = new Image("logo", new org.apache.wicket.request.resource.PackageResourceReference(AALoginLogoPanel.class, "aalogo.png"));
		// logo = new Image("logo",
		// ServiceLocator.getService(com.novamens.kbee.wicket.services.BrandingWebService.class).getLoginLogo());
		logo_link.add(logo);
		lcon.add(logo_link);

	}

	private ContentDao getContentDao() {
		return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
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
		String protocol = ((WebRequest) RequestCycle.get().getRequest()).getUrl().getProtocol();
		String host = ((WebRequest) RequestCycle.get().getRequest()).getUrl().getHost();
		Integer iport = ((WebRequest) RequestCycle.get().getRequest()).getUrl().getPort();
		String port = (iport.equals(80) || iport.equals(443) ? "" : (":" + iport.toString()));
		return protocol + "://" + host + port;
	}

}
