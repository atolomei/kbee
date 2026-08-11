package com.novamens.kbee.content.workflow;

import com.novamens.content.model.ContentTemplate;
import com.novamens.workflow.Router;
import com.novamens.workflow.RouterType;
import com.novamens.workflow.Task;
import com.novamens.workflow.WorkflowContext;

public class KbeeScriptRouter implements Router {
	
	private String script;
	
	public RouterType getType() {
		return RouterType.SCRIPT;
	}
	
	public boolean isPublisher() {
		return false;
	}
	
	public boolean isCanceller() {
		return false;
	}
	
	public void setScript(String script) {
		this.script = script;
	}
	
	public String getScript() {
		return this.script;
	}
	
	public Task getNextTask(WorkflowContext context, String event) {
		KbeeContext kbeecontext = (KbeeContext)context;
		KbeeProcedure procedure = (KbeeProcedure)kbeecontext.getProcedure();
		Object evaluation = (new JsEvaluator(getScript())).evaluate(context);
		Task task = getTask(procedure, (String)evaluation);
		return task;
	}
	
	public String getEvent(WorkflowContext context) {
		Object evaluation = (new JsEvaluator(getScript())).evaluate(context);
		return (String)evaluation;
	}
	
	public String validate(ContentTemplate template) {
		return (new JsEvaluator(getScript())).validate(template);
	}
	
	public static String GetHelpText(ContentTemplate template) {
		return JsEvaluator.GetHelpText(template);
	}
	
	private Task getTask(KbeeProcedure procedure, String name) {
		if (name==null) return null;
		for (Task task : procedure.getTasks()) {
			if (task.getId().toLowerCase().equals(name.toLowerCase())) {
				return task;
			}
		}
		return null;
	}
}
