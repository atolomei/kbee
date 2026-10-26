package kbee.aerolineas.web.login;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.EntityMember;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.ChoiceField;
import com.novamens.wicket.markup.html.form.TextAreaField;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.model.ObjectModel;

import kbee.aerolineas.service.AerolineasService;
import kbee.aerolineas.service.SignUpData;
import kbee.web.panel.AlertPanel;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.validation.validator.EmailAddressValidator;

import java.util.List;
import java.util.Locale;

public class AASignUpPanel extends Panel {
	private static final long serialVersionUID = 1L;

//	static final String default_learn_more_text = ServiceLocator.getService(BrandingService.class).getDefaultLoginLearMoreText();
//	static final String default_learn_more_link = ServiceLocator.getService(BrandingService.class).getDefaultLoginLearMoreLink();
//
//	static final String default_mesage_text = ServiceLocator.getService(BrandingService.class).getDefaultLoginMessage();
//
//	static final String default_contact_text = ServiceLocator.getService(BrandingService.class).getDefaultContactText();
//	static final String default_contact_link = ServiceLocator.getService(BrandingService.class).getDefaultContactLink();


//	private String errorCode = null;

	private Form<Void> form = new Form<Void>("form");

	private  WebMarkupContainer box = new WebMarkupContainer("box");

	private Locale locale;

	private String email = "";
	private String name = "";
	private String lastname = "";
	private String legajo = "";
	private String comment = "";
	private IModel<EntityMember> areamodel;
	
	public Locale getLocale() {
		return locale;
	}

	public String getEmail() {
		return email;
	}


	public void setEmail(String email) {
		this.email = email != null ? email.trim() : email;
	}


	
	
	
	public AASignUpPanel(String id) {
		super(id);
		addComponents(null);
	}

	public AASignUpPanel(String id, PageParameters parameters) {
		super(id);
		addComponents(parameters);
	}

	public void onInitialize() {
		super.onInitialize();

	}

	
	private void addComponents(PageParameters parameters) {

		add(box);
		box.add(new AALoginLogoPanel("logo-panel"));

		box.add(form);
		
		Label fpi = new Label("signup-instructions", getLabel("instructions"));
		fpi.setEscapeModelStrings(false);
		form.add(fpi);

		TextField<String> lastname = new  TextField<String>("lastname");
		lastname.setModel(new PropertyModel<String>(this, "lastname"));
		form.add(lastname);


		TextField<String> name = new  TextField<String>("name");
		name.setModel(new PropertyModel<String>(this, "name"));
		form.add(name);

		TextField<String> correo = new TextField<String>("email");
		correo.setModel(new PropertyModel<String>(this, "email"));
		correo.add(EmailAddressValidator.getInstance());
		form.add(correo);


		TextField<String> legajo = new TextField<String>("legajo");
		legajo.setModel(new PropertyModel<String>(this, "legajo"));
		form.add(legajo);
		
		ChoiceField<EntityMember> area = new ChoiceField<EntityMember>("area", () -> getAreas(), true);
		area.setModel(new PropertyModel<EntityMember>(this, "area"));
		form.add(area);
		
		TextAreaField<String> comments = new TextAreaField<String>("comments");
		comments.setModel(new PropertyModel<String>(this, "comment"));
		form.add(comments);

		CaptchaPanel captcha = new CaptchaPanel("captcha");
		form.add(captcha);

		Button submit = new AjaxButton("submit", form) {
			@Override
			protected void onSubmit(AjaxRequestTarget target) {
				String msg = update();
				if (msg != null) {
					AlertPanel<String> alert = new AlertPanel<String>("feedback",AlertPanel.DANGER, Model.of(msg), null, Model.of(msg));
					form.addOrReplace(alert);
					target.add(form);
				} else {

					target.add(form);
				}
			}

			@Override
			protected void onError(AjaxRequestTarget target) {
			    captcha.refresh();
				target.add(captcha);
			}
		};

		submit.add(new AttributeModifier("value", getLabel("button.send").getObject()));

		form.add(submit);
		
		form.add(new InvisiblePanel("feedback"));
		

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
		
		SignUpData data = SignUpData.builder()
			.name(name)
			.lastName(lastname)
			.email(email)
			.employeeId(legajo)
			.area(getArea())
			.commnent(comment)
			.build();
		
		ServiceLocator
			.getService(AerolineasService.class)
			.singUp(data);
		
		return getLabel("done").getObject();
	}
	
	public EntityMember getArea() {
		return areamodel!=null 
			? areamodel.getObject()
			: null;		
	}
	
	public void setArea(EntityMember area) {
		areamodel = new ObjectModel<EntityMember>(area); 
	}
	
	private List<EntityMember> getAreas() {
		return ServiceLocator.getService(AerolineasService.class).getAreas();
	}
}
