package kbee.web.security;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserService;
import com.novamens.kbee.security.oauth2.KbeeMultiUser;
import com.novamens.kbee.wicket.util.FeedbackHelper;
import com.novamens.service.BrandingService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.ChoiceField;
import kbee.web.service.PortalPanelService;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.pages.RedirectPage;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@SuppressWarnings("serial")
public class LoginDomainSelectionPanelV6 extends Panel {
	private static final long serialVersionUID = 1L;

	static final String default_learn_more_text 	= ServiceLocator.getService(BrandingService.class).getDefaultLoginLearMoreText();
	static final String default_learn_more_link  	= ServiceLocator.getService(BrandingService.class).getDefaultLoginLearMoreLink();

	static final String default_mesage_text  		= ServiceLocator.getService(BrandingService.class).getDefaultLoginMessage();

	static final String default_contact_text  		= ServiceLocator.getService(BrandingService.class).getDefaultContactText();
	static final String default_contact_link  		= ServiceLocator.getService(BrandingService.class).getDefaultContactLink();

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(LoginDomainSelectionPanelV6.class.getName());

	private List<String> usernames = new ArrayList<>();
	private String username ="";
	private String domain ="";

	public LoginDomainSelectionPanelV6(String id) {
		super(id);
		addComponents(null);
	}

	public LoginDomainSelectionPanelV6(String id, PageParameters parameters) {
		super(id);
		addComponents(parameters);
	}
	
	private void addComponents(PageParameters parameters) {
		
		WebMarkupContainer lbox = new WebMarkupContainer("box");
		add(lbox);
		
		Form<?> form = new Form<Void>("form"){
			@Override
			protected CharSequence getActionUrl() {
				return "/j_spring_security_check";
			}
		};
		lbox.add(form);

		updateDomains();
		
		if(getUsers().size()>0){
			setUsername(getUsers().get(0));
		}
		
		if(getDomains().size()>0){
			setDomain(getDomains().get(0));
		}

		ChoiceField<String> domainSelector = new ChoiceField<>("domain", new PropertyModel<String>(this, "domain"), new PropertyModel<List<String>>(this, "domains"));
//		domainSelector.setOutputMarkupId(true);
//		domainSelector.setOutputMarkupPlaceholderTag(true);
		
//		ChoiceField<String> domainSelector = new ChoiceField<>("user", new PropertyModel<String>(this, "username"), new PropertyModel<List<String>>(this, "usernames"));
//		domainSelector.setOutputMarkupId(true);
//		domainSelector.setOutputMarkupPlaceholderTag(true);
		
		form.add(domainSelector);

		Button submit = new Button("submit");
		submit.setOutputMarkupId(true);
		form.add(submit);

		AjaxSubmitLink tmpSubmit = new AjaxSubmitLink("tmpSubmit") {
			@Override
			protected void onSubmit(AjaxRequestTarget target) {
				super.onSubmit(target);
				final List<UserProfile> profiles = getProfiles();
				boolean ok = false;
				Optional<UserProfile> selected = profiles.stream().filter(up -> up.getDomain().getOrganization().equals(domain)).findFirst();
				if(selected.isPresent()){
					final UserProfile userProfile = selected.get();
					try {
						ServiceLocator.getService(UserService.class).impersonate(userProfile.getUser());
						WebPage page = ServiceLocator.getService(PortalPanelService.class).getStartPage(userProfile);
						page.getSession().setLocale(userProfile.getUser().getLocale());
						setResponsePage(page);
						ok=true;
					} 
					catch (Exception e) {
						logger.error(e, "Profile id: "+ userProfile.getId());
					}
				}
				if(!ok){
					FeedbackHelper.showErrorToast(getLabel("errorcode-1").getString());
				}
			}
		};

		form.add(tmpSubmit);
		form.setDefaultButton(tmpSubmit);

		// Contact or Subscription
		//
		WebMarkupContainer ct_c= new WebMarkupContainer("contact-container");
		Link<Void> ct_l = new Link<Void> ("contact-link") {
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
		
		logo = new Image("logo", ServiceLocator.getService(com.novamens.kbee.wicket.services.BrandingWebService.class).getLoginLogo());
		lbox.add(new AttributeModifier("class", "loginmodal-container-idoc"));
		lcon.add(logo);
		
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
		Link<Void> learn_more_l = new Link<Void> ("learn-more-link") {
			private static final long serialVersionUID = 1L;
			public void onClick() {
				setResponsePage(new RedirectPage(default_learn_more_link));
			}
		};
		
		learn_more_c.setVisible((default_learn_more_link!=null && default_learn_more_link.length()>0) || (default_learn_more_text!=null && default_learn_more_text.length()>0));
		Label learn_more_t = new Label("learn-more-text", default_learn_more_text);
		learn_more_t.setEscapeModelStrings(false);
		learn_more_l.add(learn_more_t);		
		learn_more_c.add(learn_more_l);
		lbox.add(learn_more_c);
				
		
		add(lbox);
	}

	private List<UserProfile> getProfiles(){
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.getPrincipal() instanceof KbeeMultiUser) {
			return ((KbeeMultiUser) authentication.getPrincipal())
				.getUserIds().stream().map(uId -> getContentDao().findUserProfileByUserId(uId)).collect(Collectors.toList());
		}
		return new ArrayList<>();
	}

	private void updateDomains(){
		List<String> users = new ArrayList<>();
		List<UserProfile> usersProfilesFromEmail = getProfiles();
		usersProfilesFromEmail.stream().forEach(up -> users.add( up.getUser().getUserName()));
		setUsernames(users);
	}

	private ContentDao getContentDao() {
		return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}

	private StringResourceModel getLabel(String key) {
		return new StringResourceModel(key, this, null);
	}

	public List<String> getUsers() {
		return usernames;
	}
	
	public List<String> getDomains() {
		List<String> domains = new ArrayList<String>();
		for (UserProfile profile : getProfiles()) {
			domains.add(profile.getDomain().getOrganization());
		}
		return domains;
	}

	public void setUsernames(List<String> usernames) {
		this.usernames = usernames;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}
}