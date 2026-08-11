package kbee.web.content.workflow;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.IFormModelUpdateListener;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

import com.novamens.content.base.Content;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.kbee.content.workflow.KbeeContext;
import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.TextAreaField;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.markup.html.modal.ConfirmationDialog;
import com.novamens.wicket.markup.html.modal.Dialog;
import com.novamens.wicket.markup.html.modal.Dialog.Button;
import com.novamens.workflow.WorkflowContext;

@SuppressWarnings("serial")
public class TokenValidationPanel <T extends Content> extends ModelPanel<WorkflowContext> implements IFormModelUpdateListener {
	private static final long serialVersionUID = 1L;
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(TaskResolutionPanel.class.getName());
	
	private String token, feedback, note;
	boolean validate = true;
	
	class TokenValidator implements IValidator<String> {
		@Override
		public void validate(final IValidatable<String> validatable) {
			String token = validatable.getValue();
			if (!TokenValidationPanel.this.validate(token)) {
				validatable.error(new ValidationError(this));
			}
		}
	}
	
	public TokenValidationPanel(String id, IModel<WorkflowContext> model) {
		super(id, model);
		setOutputMarkupId(true);
	}
	
	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
	
	
	public String getFeedback() {
		return feedback;
	}

	public void setFeedback(String feedback) {
		this.feedback = feedback;
	}
	

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}
	
	@Override
	public void updateModel() {
		((KbeeContext)getModel().getObject()).setParameter("delivery-receiver", getNote());
	}
	
	public boolean tokenValidation() {
		return "token".equals(getModelObject().getParameter("delivery-validation"));
	}

	@Override
	public void onInitialize() {
		super.onInitialize();
		
		TextField<String> tokenField = new TextField<String>("token", new PropertyModel<String>(this, "token"), true, new TokenValidator()) {
			@Override
			protected IModel<String> getHelpText() {
				return TokenValidationPanel.this.getHelpText();
			}
			@Override
			public boolean isHelpVisible() {
				return true;
			}
			@Override
			public boolean isVisible() {
				return tokenValidation();
			}
		};
		
		add(tokenField);
		
		WebMarkupContainer feedback = new WebMarkupContainer("feedback");
		Label feedbackMessage = new Label("message", () -> getFeedback());
		feedbackMessage.add(new AttributeModifier("style", new Model<String>() {
			public String getObject() {
				return validate ? "color: #006400;" : "color: #a94442;";
			}
		}));
		feedback.add(feedbackMessage);
		feedback.setOutputMarkupId(true);
		add(feedback);
		
		tokenField.onBeforeRender();
		tokenField.getInput().add(new AjaxFormComponentUpdatingBehavior("input") {
			protected void onUpdate(AjaxRequestTarget target) {
				String value = tokenField.getValue();
				validate(value);
				target.add(feedback);
			}
		});
		tokenField.getInput().add(new AjaxEventBehavior("blur") {
			@Override
			protected void onEvent(AjaxRequestTarget target) {
				target.add(tokenField);
			}
		});
		
		add(new AjaxLink<Void>("resend-link")  {
			@Override
			public void onClick(AjaxRequestTarget target) {
				getConfirmationDialog().open(target, 
						getLabel("resend.confirmation.message"), 
						Dialog.Ok, 
						new Dialog.Handler() {
							@Override
							public void onClick(AjaxRequestTarget target, Button button) {
								Content content = ((KbeeContext)TokenValidationPanel.this.getModelObject()).getContent();
								content.getService(WorkflowService.class).resendToken();
							}
				});
			}
			@Override
			public boolean isVisible() {
				return tokenValidation();
			}
		});
		
		add(new TextAreaField<String>("receiver", new PropertyModel<String>(this, "note"), 8, 10) {
			@Override
			public boolean isEnabled() {
				return true;
			}
			@Override
			public boolean isVisible() {
				return true;
			}
		});
		
		add(new ConfirmationDialog("confirmation-dialog"));
	}
	
	
	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
	}
	
	protected boolean validate(String value) {
		if (getModelObject().getParameter("delivery-token")==null || !getModelObject().getParameter("delivery-token").equals(value)) {
			if (value==null || value.length()!=6 || !isDigits(value)) {
				setFeedback(getLabelString("token.error.format"));
			}
			else {
				setFeedback(getLabelString("token.error.validation"));
			}
			validate = false;
		}
		else {
			validate = true;
			setFeedback(getLabelString("token.ok"));
		}
		return validate;
	}
	
	protected void refresh(AjaxRequestTarget target) {
		target.add(this);
	}
	
	protected ConfirmationDialog getConfirmationDialog() {
		return (ConfirmationDialog) get("confirmation-dialog");
	}
	
	protected IModel<String> getHelpText() {
		IModel<String> model;
		if ("true".equals(getModelObject().getParameter("delivery-error"))) {
			model = getLabel("delivery.error", getModelObject().getParameter("delivery-feedback"));
		}
		else {
			model = getLabel("delivery.message", 
				getModelObject().getParameter("delivery-email"),
				getModelObject().getParameter("delivery-phone"));
		}
		return model;
	}
	
	
	
	protected User getSessionUser() {
		try {
			return ServiceLocator.getService(SecurityService.class).getSessionUser();
		} 
		catch (Exception e) {
			logger.error(e);
			return null;
		}
	}
	
	private boolean isDigits(String value) {
		for (int c = 0; c < value.length(); c++) {
			if (!Character.isDigit(value.charAt(c))) {
				return false;
			}
		}
		return true;
	}
}