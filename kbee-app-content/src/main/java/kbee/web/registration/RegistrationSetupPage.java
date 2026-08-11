package kbee.web.registration;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.string.StringValue;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.entity.Person;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.PersonMember;
import com.novamens.content.service.TokenService;
import com.novamens.content.service.UrlService;
import com.novamens.content.user.UserProfile;
import com.novamens.dom.Json;
import com.novamens.email.EmailService;
import com.novamens.kbee.wicket.markup.html.page.AbstractKbeeWebPage;
import com.novamens.kbee.wicket.services.BrandingWebService;
import com.novamens.security.User;
import com.novamens.service.BrandingService;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.Field;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.model.ObjectModel;

import kbee.email.EmailBuilderRegistrationMessage;
import kbee.util.PropertiesFactory;
import kbee.web.page.PageContentHeaderPanel;

@SuppressWarnings("serial")
public class RegistrationSetupPage extends AbstractKbeeWebPage {
	private static final long serialVersionUID = 1L;
	
	private static final ResourceReference CSS_KBEE_BOOTSTRAP			 = new CssResourceReference(AbstractKbeeWebPage.class, "kbeebootstrap.css");
	private static final ResourceReference CSS_KBEE_BOOTSTRAP_0_360      = new CssResourceReference(AbstractKbeeWebPage.class, "kb-0-360.css");
	private static final ResourceReference CSS_KBEE_BOOTSTRAP_361_800    = new CssResourceReference(AbstractKbeeWebPage.class, "kb-361-800.css");
	private static final ResourceReference CSS_KBEE_BOOTSTRAP_801_1200   = new CssResourceReference(AbstractKbeeWebPage.class, "kb-801-1200.css");
	private static final ResourceReference CSS_KBEE_BOOTSTRAP_1201_1600  = new CssResourceReference(AbstractKbeeWebPage.class, "kb-1201-1600.css");
	private static final ResourceReference CSS_KBEE_BOOTSTRAP_1601  	 = new CssResourceReference(AbstractKbeeWebPage.class, "kb-1601.css");
	private static final ResourceReference BOOTSTRAP_CSS 				 = new CssResourceReference(Field.class, com.novamens.wicket.markup.html.form.Form.BOOTSTRAP);

	private static final String XUA_Compatible =  PropertiesFactory.getInstance("kbee").getProperties().getProperty("com.novamens.kbee.portal.xuacompatible", "IE=Edge");
	
	private IModel<PersonMember> model;
	private String feedback = null;
	
	public RegistrationSetupPage(PageParameters parameters) {
		PersonMember entity = getPerson(parameters);
		if (entity!=null) {
			setModel(new ObjectModel<PersonMember>(entity));
			if (getUser()!=null) {
				setFeedbackMessage(getLabel("already-created.error", getUser().getDisplayName()).getObject());
			}
		}
	}
	
	public void onInitialize() {
		super.onInitialize();
		
		setPageXUACompatible(XUA_Compatible);

		WebMarkupContainer canvas = new WebMarkupContainer("canvas");
		
		canvas.setOutputMarkupId(true);
		
		canvas.add(new Image("logo", ServiceLocator.getService(BrandingWebService.class).getLoginLogo()) {
			protected boolean shouldAddAntiCacheParameter()	{
				return false;
			}
		});
		
		canvas.add(new Label("organization", () -> getOrganization()) {
			public boolean isVisible() {
				return getOrganization()!=null;
			}
		});
		
		setPageDescription( new Model<String>("Registration"));
		
		PageContentHeaderPanel<?> panel=new PageContentHeaderPanel<Void>();

		setPageTitle(new Model<String>("KBEE"));
		panel.setTitle("HEADER");
		
		canvas.add(new AttributeModifier("class", ServiceLocator.getService(BrandingService.class).getLoginCss()));
		
		WebMarkupContainer formpanel = new WebMarkupContainer("form-panel") {
			public boolean isVisible() {
				return getModel()!=null && getFeedbackMessage()==null;
			}
		};
		
		Form<Void> form = new Form<Void>("form");
		
		TextField<String> person = new TextField<String>("person");
		person.setEnabled(false);
		person.setModel(new Model<String>() {
			public String getObject() {
				return getModel().getObject().getDisplayName();
			}
		});
		
		TextField<String> email = new TextField<String>("email");
		email.setEnabled(false);
		email.setModel(new Model<String>() {
			public String getObject() {
				return getPerson().getEmail();
			}
		});
		
		Button submit = new AjaxButton("submit", form) {
			protected void onSubmit(AjaxRequestTarget target) {
				setFeedbackMessage((new StringResourceModel("done", RegistrationSetupPage.this, null)).getObject());
				sendInstructions();
				target.add(canvas);
			}
			@Override
			protected void onError(AjaxRequestTarget target) {
				//target.add(feedback);
			}
		};

		submit.add(new AttributeModifier("value", (new StringResourceModel("submit.label", this, null)).getString()));
		
		form.add(person);
		form.add(email);
		form.add(submit);
		formpanel.add(form);
		
		WebMarkupContainer errorpanel = new WebMarkupContainer("error-panel") {
			public boolean isVisible() {
				return getModel()==null || getFeedbackMessage()!=null;
			}
		};
		
		errorpanel.add(new Label("error.message", ()->getFeedbackMessage()));
		
		canvas.add(formpanel);
		canvas.add(errorpanel);
		
		add(canvas);
	}

	public IModel<PersonMember> getModel() {
		return model;
	}

	public void setModel(IModel<PersonMember> model) {
		this.model = model;
	}
	
	public String getFeedbackMessage() {
		return feedback;
	}

	public void setFeedbackMessage(String errorMessage) {
		this.feedback = errorMessage;
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
	protected ResourceReference getCssResource() {
		return BOOTSTRAP_CSS;
	}
	
	private void sendInstructions() {
		EmailBuilderRegistrationMessage builder = new EmailBuilderRegistrationMessage();
		builder.setReceiver(getPerson());
		builder.setParameter("registration-url", getRegistrationLink());
		ServiceLocator.getService(EmailService.class).send(builder);
	}
	
	private PersonMember getPerson(PageParameters parameters) {
		DataSetMember person = null;
		StringValue token = parameters.get("token");
		if (!token.isNull() && !token.isEmpty()) {
			Json data = ServiceLocator.getService(TokenService.class).decode(token.toString());
			if (data!=null) {
				String personid = (String)data.get("id");
				if (personid!=null) {
					person = (DataSetMember)getContentDao().findMemberById(Long.valueOf(personid));
					if (person!=null) {
						if (!person.getCreationOffsetDateTime().toString().equals(data.get("date"))) {
							person = null;
						}
					}
				}
			}
		}
		if (person==null || !(person instanceof PersonMember)) {
			setFeedbackMessage(getLabel("not-found.error").getObject());
			person = null;
		}
		return (PersonMember)person;
	}
	
	private User getUser() {
		PersonMember member = getPerson();
		if (member==null) return null;
		Person person = member.getPerson();
		if (person==null) return null;
		UserProfile profile = person.getProfile(UserProfile.class);
		if (profile==null) return null;
		User user = profile.getUser();
		return user;
	}
	
	private String getOrganization() {
		PersonMember member = getPerson();
		if (member==null) return null;
		String organization = member.getDomain().getOrganization();
		return organization;
	}
	
	private String getRegistrationLink() {
		Person person = getPerson();
		String token = ServiceLocator.getService(SecurityService.class).nextSecureToken();
		ServiceLocator.getService(SecurityService.class).addToken(person.getId(), token, 120);
		String url = person.getService(UrlService.class).getServerUrl() + "/registration/";
		url+=token;
		return url;
	}	
	
	private PersonMember getPerson() {
		return getModel()!=null ? getModel().getObject() : null;
	}
	
	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
}