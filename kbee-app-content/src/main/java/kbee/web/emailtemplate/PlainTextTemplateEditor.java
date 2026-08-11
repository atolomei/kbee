package kbee.web.emailtemplate;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

import com.novamens.content.email.EmailTemplate;
import com.novamens.content.service.DOMObjectService;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.kbee.content.email.KbeeEmailTemplate;
import com.novamens.kbee.text.KbeeTextTemplate;
import com.novamens.service.ServiceLocator;
import com.novamens.text.TemplateModelInfo;
import com.novamens.text.TextTemplate;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.TextAreaField;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.event.wicket.ErrorEvent;
import kbee.web.form.EditButtonsV5;
import kbee.web.iql.KbeeIqlHelpService;
import kbee.web.template.ModelHelpModal;

@SuppressWarnings("serial")
public class PlainTextTemplateEditor extends ObjectEditor<EmailTemplate> {
	private static final long serialVersionUID = 1L;

	static kbee.util.logging.Logger logger =  kbee.util.logging.Logger.getLogger(EmailTemplateEditor.class.getName());
	
	Form<?> form;
	
	class EmailTemplateValidator implements IValidator<String> {
		@Override
		public void validate(final IValidatable<String> validatable) {
			try {
				final String template = validatable.getValue();
				TextTemplate texttemplate = new KbeeTextTemplate(template);
				texttemplate.process(texttemplate);
			}
			catch (Exception e) {
				validatable.error(new ValidationError(e.getMessage()));
			}
		}
	}
	
	public PlainTextTemplateEditor(IModel<EmailTemplate> model) {
		this("editor", model, false);
	}

	public PlainTextTemplateEditor(String id, IModel<EmailTemplate> model, boolean isnew) {
		super(id, model);
		setOutputMarkupId(true);
		setIsNew(isnew);
		setEditionEnabled(isnew);
	}

	public void onDetach() {
		super.onDetach();
		try {	
			if (getModel()!=null)
				getModel().detach();
		} catch (Exception e) {
			logger.error(e);
		}
	}
	
	
	@Override
	public void onInitialize() {
		super.onInitialize();

		form = new Form<Void>("form", Disposition.VERTICAL);
		
		TextAreaField<String> tx= new TextAreaField<String>("plainTextTemplate", new EmailTemplateValidator(), 50, 40) {
			@Override
			public boolean isHelpInfo() {
				return true;
			}
			@Override
			public void onHelp(AjaxRequestTarget target) {
				if (getHelpModel()!=null)
				getHelpModal().open(target, getHelpModel());
			}
		};
		tx.setRequired(true);
		form.add(tx);

		add(form);
		
		add(new EditButtonsV5<EmailTemplate>(this) {
			@Override
			protected String getCancelClass() {
				return "btn btn-default btn-sm";
			}
			@Override
			protected String getSubmitClass() {
				return "btn btn-primary btn-sm";
			}
			protected String getEditClass() {
				return "btn btn-primary btn-sm";
			}
		});	
		
		add(new ModelHelpModal("help-modal"));
	}

	public void onClose(AjaxRequestTarget target) {
		
	}
	
	@Override
	public void cancel(AjaxRequestTarget target) {
		if (isNew()) {
		}
		onCancel(target);
	}
	
	@Override
	public void update(AjaxRequestTarget target) {
		try {
			if (!getUpdatedParts().isEmpty()) {
				KbeeEmailTemplate library = (KbeeEmailTemplate) getModelObject();
				library.setDefault(false);
				library.getService(DOMObjectService.class).update(getUpdatedParts());
				super.reset();
				//target.add(EmailTemplateEditor.this.getPage());
			}
		}
		catch (Exception e) {
			logger.error(e);
			fire(new ErrorEvent<>(target, e));
		}
	}
	

	protected void onCancel(AjaxRequestTarget target) {
	}

	protected void onAfterSubmit(AjaxRequestTarget target) {
		setEditionEnabled(false);
		target.add(this);
	}
	
	protected Domain getDomain() {
		return (Domain)ServiceLocator.getService(UserService.class).getDomain();
	}

	protected void onUpdate(AjaxRequestTarget target) {
	}
	
	protected ModelHelpModal getHelpModal() {
		return (ModelHelpModal) get("help-modal");
	}
	
	protected IModel<String> getPredicatesHelp() {
		return new Model<String>(getDomain().getService(KbeeIqlHelpService.class).getPredicatesHelp());
	}
	
	protected TemplateModelInfo getHelpModel() {
		TemplateModelInfo model = getModelObject().getModel();
		return model;
	}
}
