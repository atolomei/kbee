package kbee.email;


import java.util.HashMap;
 
import java.util.Map;

import com.novamens.content.base.Content;
import com.novamens.content.email.EmailTemplate;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.dom.Domain;
import com.novamens.email.EmailBuilder;
import com.novamens.email.EmailData;
import com.novamens.kbee.content.workflow.KbeeContext;
import com.novamens.kbee.content.workflow.KbeeWorkflowActivity;
import com.novamens.kbee.template.KbeeEMailTemplateModel;
import com.novamens.kbee.text.KbeeTextTemplate;
import com.novamens.text.TextTemplate;
import com.novamens.workflow.Activity;
import com.novamens.workflow.Task;

import freemarker.template.TemplateModel;

/**
 * @see TaskStartNotificationTaskServiceRequest
 *
 */
public class EmailBuilderWorkflowTaskAssigned extends EmailBuilderWorkflowTask implements EmailBuilder {
			
	static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(EmailBuilderWorkflowTaskAssigned.class.getName());
	
	private Activity activity;

	public EmailBuilderWorkflowTaskAssigned() {
		super();
	}

	public EmailBuilderWorkflowTaskAssigned(Activity activity) {
		super();
		setActivity(activity);
	}
	
	public Activity getActivity() {
		return activity;
	}

	public void setActivity(Activity activity) {
		this.activity = activity;
	}

	@Override
	public String getKey() {
		return EmailTemplate.TASK_ASSIGN;
	}
	
	@Override
	public Domain getDomain() {
		return ((KbeeWorkflowActivity)getActivity()).getContent().getDomain();
	}
	
	@Override
	public EmailData build() {
		try{ 
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
		catch (Exception e) {
			logger.error(e);
			throw(e);
			
		}
	}

	@Override
	public boolean isSendEnabled()  {
		return true;
	}

	@Override
	public Map<String, Object> getBuilderObjects() {
		Map<String, Object> r=new HashMap<String, Object> ();
		r.put("content", getContent());
		return r;
	}
	
	public TemplateModel getTemplateModel() {
		KbeeEMailTemplateModel model = new KbeeEMailTemplateModel();
		KbeeWorkflowActivity activity = (KbeeWorkflowActivity)getActivity();
		Content content = activity.getContent();
		
		model.setModel("activity", activity);
		model.setContent(content);
		Task task = content.getService(WorkflowService.class).getContext().getProcedure().getTask(activity.getTaskName());
		activity.setTask(task);
		KbeeContext context = (KbeeContext)content.getService(WorkflowService.class).getContext();
		KbeeWorkflowActivity previousactivity = (KbeeWorkflowActivity) context.getPreviousActivity();
		model.setModel("previousactivity", previousactivity);
		if (getReceivers().isEmpty()) {
			model.setModel("receiver", getReceivers().get(0));
		}
		return model;
	}
}
