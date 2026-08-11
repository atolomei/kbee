package kbee.web.registration;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
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
import com.novamens.content.service.PersonService;
import com.novamens.content.service.TokenService;
import com.novamens.dom.Json;
import com.novamens.kbee.content.user.KbeeUserDevice;
import com.novamens.kbee.wicket.markup.html.page.AbstractKbeeWebPage;
import com.novamens.kbee.wicket.services.BrandingWebService;
import com.novamens.service.BrandingService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.Field;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.model.ObjectModel;

import kbee.util.PropertiesFactory;
import kbee.web.content.eform.ContentFormEditor;
import kbee.web.page.PageContentHeaderPanel;

@SuppressWarnings("serial")
public class DeviceRegistrationPage extends AbstractKbeeWebPage {
			
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(DeviceRegistrationPage.class.getName());

	private static final ResourceReference CSS_KBEE_BOOTSTRAP			 = new CssResourceReference(AbstractKbeeWebPage.class, "kbeebootstrap.css");
	private static final ResourceReference CSS_KBEE_BOOTSTRAP_0_360      = new CssResourceReference(AbstractKbeeWebPage.class, "kb-0-360.css");
	private static final ResourceReference CSS_KBEE_BOOTSTRAP_361_800    = new CssResourceReference(AbstractKbeeWebPage.class, "kb-361-800.css");
	private static final ResourceReference CSS_KBEE_BOOTSTRAP_801_1200   = new CssResourceReference(AbstractKbeeWebPage.class, "kb-801-1200.css");
	private static final ResourceReference CSS_KBEE_BOOTSTRAP_1201_1600  = new CssResourceReference(AbstractKbeeWebPage.class, "kb-1201-1600.css");
	private static final ResourceReference CSS_KBEE_BOOTSTRAP_1601  	 = new CssResourceReference(AbstractKbeeWebPage.class, "kb-1601.css");
	private static final ResourceReference BOOTSTRAP_CSS 				 = new CssResourceReference(Field.class, com.novamens.wicket.markup.html.form.Form.BOOTSTRAP);

	private static final String XUA_Compatible =  PropertiesFactory.getInstance("kbee").getProperties().getProperty("com.novamens.kbee.portal.xuacompatible", "IE=Edge");
	
	private IModel<Person> model;
	private String deviceId, deviceDescription, deviceNumber;
	private String feedback = null;
	private boolean done = false;
	
	public DeviceRegistrationPage(PageParameters parameters) {
		Person owner = getOwner(parameters);
		if (owner!=null) {
			setModel(new ObjectModel<Person>(owner));
		}
	}
	
	@Override
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
		
		IModel<String> instructionsmodel = new Model<String>() {
			public String getObject() {
				StringResourceModel model = new StringResourceModel("instructions", DeviceRegistrationPage.this, null);
				if (getModel()!=null)
					model.setParameters(getModel().getObject().getFirstLastName(), getDeviceDescription());
				return model.getObject();
			}
		}; 
		
		Label instructions = new Label("instructions-label", instructionsmodel) {
			public boolean isVisible() {
				return getModel()!=null && !done;
			}
		};
		
		instructions.setEscapeModelStrings(false);
		
		canvas.add(instructions);
		
		setPageDescription( getLabel("registration"));
		
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
		
//		TextField<String> person = new TextField<String>("person");
//		person.setEnabled(false);
//		person.setModel(new Model<String>() {
//			public String getObject() {
//				return getModel().getObject().getDisplayName();
//			}
//		});
		
//		TextField<String> email = new TextField<String>("email");
//		email.setEnabled(false);
//		email.setModel(new Model<String>() {
//			public String getObject() {
//				return getPerson().getEmail();
//			}
//		});
		
		Button submit = new AjaxButton("submit", form) {
			protected void onSubmit(AjaxRequestTarget target) {
				try {
					registerDevice();
					done = true;
					IModel<String> donemodel = new Model<String>() {
						public String getObject() {
							StringResourceModel model = new StringResourceModel("done", DeviceRegistrationPage.this, null);
							model.setParameters(DeviceRegistrationPage.this.getModel().getObject().getFirstLastName(), getDeviceDescription());
							return model.getObject();
						}
					}; 
					setFeedbackMessage(donemodel.getObject());
					target.add(canvas);
				}
				catch (Exception e) {
					setFeedbackMessage(e.getMessage());
				}
				target.add(canvas);
			}
			@Override
			protected void onError(AjaxRequestTarget target) {
				//target.add(feedback);
			}
		};

		submit.add(new AttributeModifier("value", (new StringResourceModel("submit.label", this, null)).getString()));
		
		//form.add(person);
		//form.add(email);
		form.add(submit);
		formpanel.add(form);
		
		WebMarkupContainer errorpanel = new WebMarkupContainer("error-panel") {
			public boolean isVisible() {
				return getModel()==null || getFeedbackMessage()!=null;
			}
		};
		
		errorpanel.add(new Label("error.message", ()->getFeedbackMessage()));
		((Label)errorpanel.get("error.message")).setEscapeModelStrings(false);
		
		canvas.add(formpanel);
		canvas.add(errorpanel);
		
		add(canvas);
	}

	public IModel<Person> getModel() {
		return model;
	}

	public void setModel(IModel<Person> model) {
		this.model = model;
	}
	
	public String getFeedbackMessage() {
		return feedback;
	}
	
	public String getDeviceId() {
		return deviceId;
	}
	
	public String getDeviceDescription() {
		return deviceDescription;
	}

	public String getDeviceNumber() {
		return deviceNumber;
	}

	public void setDeviceNumber(String deviceNumber) {
		this.deviceNumber = deviceNumber;
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
	
	public void onDetach() {
		super.onDetach();
		if (model!=null) {
			model.detach();
		}
	}
	
	@Override
	protected ResourceReference getCssResource() {
		return BOOTSTRAP_CSS;
	}
	
	private void registerDevice() {
		KbeeUserDevice device = new KbeeUserDevice();
		device.setDeviceId(getDeviceId());
		device.setDescription(getDeviceDescription());
		device.setNumber(getDeviceNumber());
		getMember().getService(PersonService.class).updateDevice(device);
	}
	
	private PersonMember getMember() {
		List<DataSetMember> members = getContentDao().findMembersByEntity(getModel().getObject());
		return (PersonMember)members.get(0);
	}
	
	private String getOrganization() {
		return getModel()!=null ? getModel().getObject().getDomain().getOrganization()  : null;
	}
	
	private Person getOwner(PageParameters parameters) {
		
		try {
				Person person = null;
				OffsetDateTime date = null;
				StringValue token = parameters.get("token");
				if (!token.isNull() && !token.isEmpty()) {
					Json data = ServiceLocator.getService(TokenService.class).decode(token.toString());
					if (data!=null) {
						String personid = (String)data.get("owner");
						if (personid!=null) {
							person = getContentDao().findPersonById(Long.valueOf(personid));
						}
						deviceId = (String)data.get("id");
						deviceDescription = (String)data.get("description");
						deviceNumber = (String)data.get("number");
						date = getDate((String)data.get("date"));
					}
				}
				if (person==null) {
					setFeedbackMessage(getLabel("owner-not-found.error").getObject());
				}
		//		if (date!=null && !isValid(date)) {
		//			setFeedbackMessage(getLabel("token-expired.error").getObject());
		//			person = null;
		//		}
				return person;
		} catch (Exception e) {
			logger.error(e);
			return null;
		}
	}
	
	//private boolean isValid(OffsetDateTime date) {
	//	return ChronoUnit.SECONDS.between(date, OffsetDateTime.now()) < 1800; 
	//}
	
	private OffsetDateTime getDate(String data) {
		LocalDateTime localdatetime = LocalDateTime.parse(data, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
		ZonedDateTime zoned = localdatetime.atZone(ZoneId.systemDefault());
		OffsetDateTime date = zoned.toOffsetDateTime();
		return date;
	}
	
	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
}