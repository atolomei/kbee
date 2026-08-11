package com.novamens.kbee.content.workflow;

import java.io.Serializable;

import com.novamens.content.workflow.LetterRule;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.kbee.text.KbeeTextTemplate;
import com.novamens.workflow.WorkflowContext;


public class KbeeLetterRule implements LetterRule, Serializable {
	private static final long serialVersionUID = 1L;

	private String template;
	
	public KbeeLetterRule() {
	}
	
	public void execute(WorkflowContext context) {
		
		
		KbeeTextTemplate template = new KbeeTextTemplate(getTemplate());
		String text = template.process(((KbeeContext) context).getContent());
		com.novamens.workflow.Activity a=((KbeeContext) context).getPreviousActivity();
		((KbeeWorkflowActivity)a).setResolution(text);
		//((KbeeContext)context).setResolution("LETTER");
		//getWorkflowService(context).setResolution("LETTER TEMPLATE", null);
	}
	
	public String getText() {
		return template;
	}
	
	public String getTemplate() {
		return template;
	}
	
	public void setTemplate(String text) {
		this.template = text;
	}
	
	public String getDescription() {
		return "";
	}
	
	protected WorkflowService getWorkflowService(WorkflowContext context) {
		return ((KbeeContext)context).getContent().getService(WorkflowService.class);
	}
}