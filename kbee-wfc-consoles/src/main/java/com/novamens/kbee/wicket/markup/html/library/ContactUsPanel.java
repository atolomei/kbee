package com.novamens.kbee.wicket.markup.html.library;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.email.EmailData;
import com.novamens.email.EmailService;
import com.novamens.kbee.wicket.markup.html.ajax.WorkingIndicatorAjaxSubmitLink;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.service.ServiceLocator;

import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.markup.html.form.TextAreaField;
import com.novamens.wicket.markup.html.form.TextField;

public class ContactUsPanel extends Panel {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ContactUsPanel.class.getName());

	
	private static final long serialVersionUID = 1L;

	private String name;
	private String email;
	private String message;
	
	private String emailTo;
	 

	private IModel<String> fd_model;
	private IModel<String> fd_class_model;
	
	private boolean isClose = true;
	
	
	public boolean isClose() {
		return isClose;
	}

	public void setClose(boolean isClose) {
		this.isClose = isClose;
	}

	public ContactUsPanel(String id, String emailto) {
		super(id);
		setEmailTo(emailto);
		add(new InvisiblePanel("main"));	
	}
	
	private boolean created= false;
	
	@Override
	public void onInitialize() {
		super.onInitialize();
	}
	
	@Override
	public void onBeforeRender() {
		super.onBeforeRender();

		fd_model = new Model<String>();
		fd_class_model = new Model<String>();
		
		if (!created) {
			
			WebMarkupContainer main = new WebMarkupContainer("main"); 
			addOrReplace(main);
			main.add(getForm());
			created = true;
		}
		
	}
	
	
	private Form<Void> getForm() {
		setOutputMarkupId(true);
		Form<Void> form = new Form<Void>("form", Disposition.VERTICAL);
		

		form.add(new TextField<String>("name", 				new PropertyModel<String>(this, "name"), true));
		form.add(new TextField<String>("email", 			new PropertyModel<String>(this, "email"), true));							
		form.add(new TextAreaField<String>("message", 		new PropertyModel<String>(this, "message"), 6, 40));
		
		form.add(new WorkingIndicatorAjaxSubmitLink("submit", new StringResourceModel("submit", ContactUsPanel.this, null).getString(), form) {
			private static final long serialVersionUID = 1L;
				
			@Override
			public String getAjaxIndicatorMarkupId() {
				return getId();
			}
			
			protected void onSubmit(AjaxRequestTarget target) {
	            	ContactUsPanel.this.onSubmit(target);   	
	            }
				
				protected String getLabel() {
					return new StringResourceModel("submit", ContactUsPanel.this, null).getString();
				}
				
				@Override
				protected String getWorkingLabel() {
					return new StringResourceModel("sending", ContactUsPanel.this, null).getString();
				}
	        });
							

		AjaxLink<Void> close = new AjaxLink<Void>("close") {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				ContactUsPanel.this.onClose(target);
			}
			@Override
			public boolean isVisible() {
				return isClose(); 
			}
		};

							

		
		
		form.add(close);
						
		WebMarkupContainer feedbackcontainer = new WebMarkupContainer("feedbackContainer");
		
				
		
		
		feedbackcontainer.setOutputMarkupId(true);
		
		Label fee=new Label("feedback", getFeedbackModel());
		fee.setOutputMarkupId(true);
		fee.add(new AttributeModifier("class", getFeedbackClassModel()));

		feedbackcontainer.add(fee);
		form.add(feedbackcontainer);
		
		return form;
	}


	protected void setFeedbackClass(String s) {
		 getFeedbackClassModel().setObject(s);
	}

	
	protected void setFeedback(String s) {
		 getFeedbackModel().setObject(s);
	}
	
	
	private IModel<String> getFeedbackModel() {
		return this.fd_model;
	}

	
	private IModel<String> getFeedbackClassModel() {
		return this.fd_class_model;
	}
	
	
	boolean isFeedBack = false;
	
	
	protected boolean isFeedback() {
		return isFeedBack;
	}

	public String getEmailTo() {
		return emailTo;
	}

	public void setEmailTo(String emailTo) {
		this.emailTo = emailTo;
	}
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	protected void onClose(AjaxRequestTarget target)  {
	}
	
	
	protected void onSubmit(AjaxRequestTarget target) {
		
		if (getName()==null || getEmail()==null || getMessage()==null) {
			this.setFeedback(getLabel("all_fields").getObject());
			this.setFeedbackClass("alert alert-warning");
			(get("main:form:feedbackContainer")).setVisible(true);
		}else {
			sendEmail();
			clear();
		}
		target.add(this);
	}

	
	@SuppressWarnings("unchecked")
	private void clear() {
		
		setName("");
		setMessage("");
		setEmail("");
		
		((TextField<String>) get("main:form:name")).setValue(null);;
		((TextField<String>) get("main:form:email")).setValue(null);
		((TextAreaField<String>) get("main:form:message")).setValue(null);
		
		((Form<Void>) get("main:form")).clearInput();
		
	 
	}

	protected IModel<String> getLabel(String key, String... parameter) {
		StringResourceModel model = new StringResourceModel(key, this);
		model.setParameters((Object[]) parameter);
		return model;
	}
	
	private void sendEmail() {

		try {

			
			
			String from_email = String.format("%s <%s>", getName(), getEmail());
			String to_email = getEmailTo();
									
			String subject = new StringResourceModel("subject", ContactUsPanel.this).getString() + getName();
			
			
			StringBuilder msg = new StringBuilder();
			msg.append( getLabel("email_body", getName(), getEmail(), getMessage()).getObject());
			
			ServiceLocator.getService(EmailService.class).send(	new EmailData(from_email, to_email, subject, msg.toString(), null, "Contact"), getDomain());;
			
			this.setFeedback( getLabel("sent").getObject());
			this.setFeedbackClass("alert alert-info");
			
			
			Label fee=new Label("feedback", getFeedbackModel());
			fee.setOutputMarkupId(true);
			fee.add(new AttributeModifier("class", getFeedbackClassModel()));
			get("main:form:feedbackContainer:feedback").replaceWith(fee);
			
			/*
			((Label) get("main:form:feedbackContainer:feedback")).setDefaultModelObject(this.getFeedbackModel().getObject());
			((Label) get("main:form:feedbackContainer:feedback")).add(new AttributeModifier("class", this.getFeedbackClassModel()));
			((Label) get("main:form:feedbackContainer:feedback")).setVisible(true);
			*/
			
			(get("main:form:feedbackContainer")).setVisible(true);
			

			Thread.sleep(750);
			
			
					
		} catch (Exception e) {

			this.setFeedback("Error:  " + e.getClass().getSimpleName());
			this.setFeedbackClass("alert alert-danger");
			
			Label fee=new Label("feedback", getFeedbackModel());
			fee.setOutputMarkupId(true);
			fee.add(new AttributeModifier("class", getFeedbackClassModel()));
			get("main:form:feedbackContainer:feedback").replaceWith(fee);

			/*
			((Label) get("main:form:feedbackContainer:feedback")).setDefaultModelObject(this.getFeedbackModel().getObject());
			((Label) get("main:form:feedbackContainer:feedback")).add(new AttributeModifier("class", this.getFeedbackClassModel()));
			((Label) get("main:form:feedbackContainer:feedback")).setVisible(true);
			 	*/
			(get("main:form:feedbackContainer")).setVisible(true);

			logger.error(e);
		}

	}
	
	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
	
}


