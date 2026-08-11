package kbee.web.registration;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
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
import com.novamens.content.model.PersonMember;
import com.novamens.kbee.wicket.markup.html.page.AbstractKbeeWebPage;
import com.novamens.kbee.wicket.services.BrandingWebService;
import com.novamens.service.BrandingService;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.Field;
import com.novamens.wicket.model.ObjectModel;

import kbee.util.PropertiesFactory;
import kbee.web.page.PageContentHeaderPanel;

@SuppressWarnings("serial")
public class RegistrationPage extends AbstractKbeeWebPage {
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
	private String feddbackMessage;
	
	public RegistrationPage(PageParameters parameters) {
		PersonMember entity= getPerson(parameters);
		if (entity!=null) {
			setModel(new ObjectModel<PersonMember>(entity));
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
		
		if (getModel()!=null) {
			formpanel.add(new PersonAccountEditor("account-editor", getModel()) {
				@Override
				protected void onCreate(AjaxRequestTarget target) {
					setFeedbackMessage((new StringResourceModel("done", this, null)).getObject());
					target.add(canvas);
				}
			});
		}
		
		WebMarkupContainer errorpanel = new WebMarkupContainer("error-panel") {
			public boolean isVisible() {
				return getModel()==null || getFeedbackMessage()!=null;
			}
		};
		
		errorpanel.add(new Label("message", ()->getFeedbackMessage()));
		
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
	
	public PersonMember getPerson() {
		return model!=null ? model.getObject() : null;
	}

	public String getFeedbackMessage() {
		return feddbackMessage;
	}

	public void setFeedbackMessage(String message) {
		this.feddbackMessage = message;
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
	
	private String getOrganization() {
		PersonMember member = getPerson();
		if (member==null) return null;
		String organization = member.getDomain().getOrganization();
		return organization;
	}
	
	private PersonMember getPerson(PageParameters parameters) {
		PersonMember person = null;
		StringValue token = parameters.get("token");
		if (!token.isNull() && !token.isEmpty() && ServiceLocator.getService(SecurityService.class).isValid(token.toString())) {
			Long id = (Long)ServiceLocator.getService(SecurityService.class).getId(token.toString());
			person = (PersonMember)getContentDao().findMemberById(id);
			if (person == null) {
				setFeedbackMessage("error1");
			}
		}
		else {
			setFeedbackMessage((new StringResourceModel("token.error", this, null)).getObject());
		}
		return person;
	}
	
	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
}