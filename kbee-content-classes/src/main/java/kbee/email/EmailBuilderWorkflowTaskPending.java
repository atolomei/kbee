package kbee.email;

import java.util.HashMap;
import java.util.Map;

import com.novamens.content.base.Content;
import com.novamens.content.email.EmailTemplate;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.email.EmailBuilder;
import com.novamens.email.EmailData;
import com.novamens.kbee.content.workflow.KbeeContext;
import com.novamens.kbee.content.workflow.KbeeWorkflowActivity;
import com.novamens.kbee.template.KbeeEMailTemplateModel;
import com.novamens.kbee.text.KbeeTextTemplate;
import com.novamens.text.TextTemplate;
import com.novamens.workflow.Task;

import freemarker.template.TemplateModel;

/**
 * @see TaskPendingNotificationTaskServiceRequest
 */
public class EmailBuilderWorkflowTaskPending extends EmailBuilderWorkflowTask implements EmailBuilder {
			
	public EmailBuilderWorkflowTaskPending() {
	}

	public EmailBuilderWorkflowTaskPending(Content content) {
		super(null, content);
	}
	
	@Override
	public String getKey() {
		return EmailTemplate.TASK_PENDING;
	}
	
	@Override
	public EmailData build() {

		EmailTemplate template = getEmailTemplate(getDomain(), getLanguage(), getKey());
		
		TemplateModel emailmodel = getTemplateModel();
	
		TextTemplate fromtemplate = new KbeeTextTemplate(template.getFrom());
		String from = fromtemplate.process(emailmodel);
		
		TextTemplate subjecttemplate = new KbeeTextTemplate(template.getSubject());
		String subject = subjecttemplate.process(emailmodel);
		
		TextTemplate texttemplate = new KbeeTextTemplate(template.getStringTemplate());
		String text = texttemplate.process(emailmodel);
		
		EmailData data = new EmailData(from, getEmailTo(), subject, text, null, getKey(), null);
		
		return data;
	}
	
	@Override
	public Map<String, Object> getBuilderObjects() {
		Map<String, Object> r=new HashMap<String, Object> ();
		r.put("content", getContent());
		return r;
	}
	
	public TemplateModel getTemplateModel() {
		KbeeEMailTemplateModel model = new KbeeEMailTemplateModel();
		Content content = getContent();
		KbeeContext context = (KbeeContext)content.getService(WorkflowService.class).getContext(); 
		model.setContent(content);
		Task task = context.getTask();
		model.setModel("task", task);
		KbeeWorkflowActivity previousactivity = (KbeeWorkflowActivity) context.getPreviousActivity();
		model.setModel("previousactivity", previousactivity);
		if (getReceivers().isEmpty()) {
			model.setModel("receiver", getReceivers().get(0));
		}
		return model;
	}

}