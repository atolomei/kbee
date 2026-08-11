package kbee.web.emailtemplate;

import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.email.EmailTemplate;
import com.novamens.content.service.DOMObjectService;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.email.EmailService;
import com.novamens.kbee.content.email.KbeeEmailTemplate;
import com.novamens.kbee.text.KbeeTextTemplate;
import com.novamens.kbee.wicket.util.FeedbackHelper;
import com.novamens.service.ServiceLocator;
import com.novamens.text.TemplateModelInfo;
import com.novamens.text.TextTemplate;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.StaticField;
import com.novamens.wicket.markup.html.form.TextAreaField;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.event.wicket.ErrorEvent;
import kbee.web.form.EditButtonsV5;
import kbee.web.iql.KbeeIqlHelpService;
import kbee.web.template.ModelHelpModal;

@SuppressWarnings("serial")
public class EmailTemplateEditor extends ObjectEditor<EmailTemplate> {
	private static final long serialVersionUID = 1L;

	static kbee.util.logging.Logger logger =  kbee.util.logging.Logger.getLogger(EmailTemplateEditor.class.getName());
	
	
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
	
	public EmailTemplateEditor(IModel<EmailTemplate> model) {
		this("editor", model, false);
	}

	public EmailTemplateEditor(String id, IModel<EmailTemplate> model, boolean isnew) {
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
	
	Form<?> form;
	
	@Override
	public void onInitialize() {
		super.onInitialize();

		form = new Form<Void>("form", Disposition.VERTICAL);
		
		form.add(new StaticField<String>("key", new Model<String>(getModel().getObject().getKey())));
		form.add(new StaticField<String>("id", new Model<String>( String.valueOf(getModel().getObject().getId()))));
		form.add(new StaticField<String>("language", new Model<String>(getModel().getObject().getLanguage())));
		form.add(new StaticField<String>("description", new Model<String>(getModel().getObject().getDescription())));
		form.add(new TextField<String>("title",true, new EmailTemplateValidator()));
	
		
		
		
		AjaxLink<Void> loadDefault = new AjaxLink<Void>("default") {
			@SuppressWarnings("unchecked")
			@Override
			public void onClick(AjaxRequestTarget target) {
				 
				Map<String, EmailTemplate> map = ServiceLocator.getService(EmailService.class).getDefaultTemplates( EmailTemplateEditor.this.getModel().getObject().getLanguage());

				 String err = null;
				 boolean ok =false;
				 
				 if (map!=null) {
					 
					 EmailTemplate em = map.get(EmailTemplateEditor.this.getModel().getObject().getKey());
					 
				
					 if (em!=null) {
						 
							String defaultValue = em.getStringTemplate();
							((TextAreaField<String>) form.get("stringTemplate")).setValue(defaultValue);
							EmailTemplateEditor.this.getModel().getObject().setStringTemplate(defaultValue);
							
							String defaultJsonValue = em.getStrModel();
							EmailTemplateEditor.this.getModel().getObject().setModel(defaultJsonValue);
							ok =true;
							
							logger.debug(" ------------------------------- " );
							logger.debug(" HTML -> " +  EmailTemplateEditor.this.getModel().getObject().getStringTemplate());
							
							logger.debug(" ------------------------------- " );
							
							logger.debug(" JSON MODEL -> " + EmailTemplateEditor.this.getModel().getObject().getStrModel());
							
							logger.debug(" ------------------------------- " );
							
					 }
					 err = "Default Template for key = '" +EmailTemplateEditor.this.getModel().getObject().getKey() + "' is null";
				 }
				 err = "Default Templates is null";
				 
				 if (ok)
					 FeedbackHelper.showInfoToast("Set default value");
				 else
					 FeedbackHelper.showErrorToast(err);
				 
				target.add(form);
			}
			
		};
				
		
		form.add(loadDefault);
		
		
		
		form.add(new TextField<String>("subject", true, new EmailTemplateValidator()) {
			@Override
			public boolean isHelpInfo() {
				return true;
			}
			@Override
			public void onHelp(AjaxRequestTarget target) {
				getHelpModal().open(target);
			}
		});
		
		TextAreaField<String> tx= new TextAreaField<String>("stringTemplate", new EmailTemplateValidator(), 50, 40) {
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
		
		Link<EmailTemplate> ol =new Link<EmailTemplate>("bbb", getModel()) {
			@Override
			public void onClick() {
					String k=getModel().getObject().getKey();
					String l=getModel().getObject().getLanguage();
					String la= (l!=null && l.equals("es") ? "en" : "es");
					EmailTemplate tem = getContentDao().findEmailTemplate(getDomain(), la, k);
					if (tem!=null)
						setResponsePage(new EmailTemplatePage( new ObjectModel<EmailTemplate>(tem), false));
			}
		};
		
		
		String l=getModel().getObject().getLanguage();
		String la= ( (l!=null && l.equals("es")) ? "la-en" : "la-es");
		ol.add( new Label("lang-label", new StringResourceModel(la, EmailTemplateEditor.this, null)));

		add(ol);
		
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
				target.add(EmailTemplateEditor.this.getPage());
			}
		}
		catch (Exception e) {
			logger.error(e);
			fire(new ErrorEvent<>(target, e));
		}
	}
	
	public IModel<String> getText(String key) {
		return new StringResourceModel(key, this, null);
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
//		KbeeTemplateModelInfo model = new KbeeTemplateModelInfo();
//		model.setName("Model");
//		model.setType(ModelType.COMPOUND);
//		model.setDescription("Modelo del template para la generación de la notificación de un evento de inicio de tarea");
//		List<TemplateModelInfo> elements = new ArrayList<TemplateModelInfo>();
//		KbeeTemplateModelInfo e;
//		
//		e = new KbeeTemplateModelInfo();
//		e.setName("activity");
//		e.setType(KbeeTemplateModelInfo.ModelType.ACTIVITY);
//		elements.add(e);
//		
//		e = new KbeeTemplateModelInfo();
//		e.setName("content");
//		e.setType(KbeeTemplateModelInfo.ModelType.CONTENT);
//		elements.add(e);
//		
//		e = new KbeeTemplateModelInfo();
//		e.setName("receiver");
//		e.setType(KbeeTemplateModelInfo.ModelType.USER);
//		elements.add(e);
//		
//		model.setElements(elements);
//		
//		((KbeeEmailTemplate)getModelObject()).setModel(model);
//		
//		return model;
	}
	
	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
}
