package kbee.web.emailtemplate;

import java.io.IOException;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;

import com.novamens.content.base.Content;
import com.novamens.content.email.EmailTemplate;
import com.novamens.content.service.TestService;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.kbee.content.workflow.KbeeWorkflowActivity;
import com.novamens.kbee.template.KbeeEMailTestModel;
import com.novamens.kbee.text.KbeeTextTemplate;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.text.TemplateModelInfo;
import com.novamens.text.TextTemplate;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.workflow.Task;

import freemarker.core.Environment;
import freemarker.core.InvalidReferenceException;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.TemplateModel;

@SuppressWarnings("serial")
public class EmailTemplateTestEditor2 extends ObjectEditor<EmailTemplate> {
	private static final long serialVersionUID = 1L;
	
	class TestExceptionHandler implements TemplateExceptionHandler {
		public void handleTemplateException(freemarker.template.TemplateException te, Environment env, java.io.Writer out)
			throws freemarker.template.TemplateException {
				try {
					String outmessage = "";
					if (te instanceof InvalidReferenceException) {
						String message = ((InvalidReferenceException)te).getFTLInstructionStack();
						int i = message.indexOf("${");
						if (i>0) {
							int o = message.indexOf("}",i);
							if (o>0) {
								outmessage = "["+message.substring(i+2,o)+"]";
							}
						}
					}
					out.write(outmessage);
				} 
				catch (IOException e) {
					throw new freemarker.template.TemplateException("Failed to print error message. Cause: " + e, env);
				}
		}
	}
	
	public EmailTemplateTestEditor2(IModel<EmailTemplate> model) {
		this("editor", model);
	}

	public EmailTemplateTestEditor2(String id, IModel<EmailTemplate> model) {
		super(id, model);
		setOutputMarkupId(true);
	}
	
	public void onDetach() {
		super.onDetach();
	}

	@Override
	public void onInitialize() {
		super.onInitialize();
		
		Label subjectlabel = new Label("subject", () -> getText(getModelObject().getSubject()));
		subjectlabel.setEscapeModelStrings(false);
		add(subjectlabel);
		
		Label textlabel = new Label("text", () -> getText(getModelObject().getStringTemplate()));
		textlabel.setEscapeModelStrings(false);
		add(textlabel);
		
		add(new AjaxLink<Void>("test-link") {
			public void onClick(AjaxRequestTarget target) {
				target.add(EmailTemplateTestEditor2.this);
			}
		});
	}
	
	private String getText(String template) {
		String text = "";
		try {
			TextTemplate texttemplate = new KbeeTextTemplate(template) {
				@Override
				protected TemplateExceptionHandler getExceptionHandler() {
					return new TestExceptionHandler();
				}
			};
			text = texttemplate.process(getTemplateModel());
		}
		catch (Exception e) {
		}
		return text;
	}
	
	private TemplateModel getTemplateModel() {
		KbeeWorkflowActivity activity = null;
		KbeeEMailTestModel templatemodel = new KbeeEMailTestModel();
		TemplateModelInfo model = getModelObject().getModel();
		for (TemplateModelInfo child : model.getElements()) {
			if (child.getType().equals(TemplateModelInfo.ModelType.ACTIVITY)) {
				activity = (KbeeWorkflowActivity)getModelObject().getDomain().getService(TestService.class).getSampleActivity();
				if (activity!=null) {
					templatemodel.setModel(child.getName(), activity);
				}
			}
			if (child.getType().equals(TemplateModelInfo.ModelType.CONTENT)) {
				Content content = null;
				if (activity!=null) {
					content = activity.getContent();
				}
				else {
					content = getModelObject().getDomain().getService(TestService.class).getSample();
				}
				if (content!=null) {
					templatemodel.setModel(child.getName(), content);
				}
			}
			if (child.getType().equals(TemplateModelInfo.ModelType.TASK)) {
				Content content = null;
				Task task = null;
				content = getModelObject().getDomain().getService(TestService.class).getSample();
				if (content!=null) {
					task = content.getService(WorkflowService.class).getTask();
				}
				if (task!=null) {
					templatemodel.setModel(child.getName(), task);
				}
			}
			if (child.getType().equals(TemplateModelInfo.ModelType.USER)) {
				templatemodel.setModel(child.getName(), getSessionUser());
			}
		}
		return templatemodel;
	}
	
	private User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
}
