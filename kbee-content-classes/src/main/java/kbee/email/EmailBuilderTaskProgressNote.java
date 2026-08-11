package kbee.email;

import com.novamens.content.base.Content;
import com.novamens.content.email.EmailTemplate;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.dom.Domain;
import com.novamens.email.EmailBuilder;
import com.novamens.email.EmailData;
import com.novamens.kbee.content.workflow.KbeeActivityProgressNote;
import com.novamens.kbee.content.workflow.KbeeWorkflowActivity;
import com.novamens.kbee.template.KbeeEMailTemplateModel;
import com.novamens.kbee.text.KbeeTextTemplate;
import com.novamens.text.TextTemplate;
import com.novamens.workflow.ActivityProgressNote;
import com.novamens.workflow.Task;

import freemarker.template.TemplateModel;
import kbee.util.logging.Logger;

public class EmailBuilderTaskProgressNote extends EmailBuilderWorkflowTask  implements EmailBuilder {
		
	private ActivityProgressNote note;
	private static Logger logger = Logger.getLogger(EmailBuilderTaskProgressNote.class.getName());

	public EmailBuilderTaskProgressNote() {
		super();
	}
	
	public EmailBuilderTaskProgressNote(ActivityProgressNote note) {
		this.note = note;
	}
	
	@Override
	public String getKey() {
		return EmailTemplate.TASK_PROGRESS_NOTE;
	}
	
	public ActivityProgressNote getNote() {
		return this.note;
	}
	
	@Override
	public Domain getDomain() {
		return ((KbeeActivityProgressNote)getNote()).getDomain();
	}
	
	@Override
	public EmailData build() {
		
		try {
			EmailTemplate template = getEmailTemplate(getDomain(), getLanguage(), getKey());
			
			TemplateModel emailmodel = getTemplateModel();
		
			TextTemplate fromtemplate = new KbeeTextTemplate(template.getFrom());
			String from = fromtemplate.process(emailmodel);
			
			
			String t_subject = template.getSubject();
			
			if (t_subject==null) {
				t_subject = template.getDisplayName() +" -> subject is null";
				logger.debug(t_subject);
			}
			
			TextTemplate subjecttemplate = new KbeeTextTemplate(template.getSubject());
			String subject = subjecttemplate.process(emailmodel);
			
			TextTemplate texttemplate = new KbeeTextTemplate(template.getStringTemplate());
			String text = texttemplate.process(emailmodel);
			
			EmailData data = new EmailData(from, getEmailTo(), subject, text, null, getKey(), null);
			return data;
			
		} catch (Exception e) {
			logger.error(e);
			throw (e);
		}
		
	}
	
	public TemplateModel getTemplateModel() {
		KbeeEMailTemplateModel model = new KbeeEMailTemplateModel();
		ActivityProgressNote note = getNote();
		if (note==null) 
			throw new IllegalArgumentException("note is null");
		Content content = ((KbeeWorkflowActivity)note.getActivity()).getContent();
		if (content==null) 
			throw new IllegalArgumentException("content is null for note " + note.getDisplayName());
		model.setModel("note", note);
		model.setContent(content);
		KbeeWorkflowActivity activity = (KbeeWorkflowActivity)note.getActivity();
		if (activity==null)					 
			throw new IllegalArgumentException(" activity is null for note " + note.getDisplayName());
		Task task = content.getService(WorkflowService.class).getContext().getProcedure().getTask(activity.getTaskName());
		activity.setTask(task);
		model.setModel("activity", activity);
		return model;
	}
}