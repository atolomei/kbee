package com.novamens.kbee.content.workflow;

import java.io.Serializable;


import com.novamens.content.workflow.ScriptRule;
import com.novamens.workflow.WorkflowContext;

public class KbeeScriptRule implements ScriptRule, Serializable {
	private static final long serialVersionUID = 1L;

	private String script;
	
	public KbeeScriptRule() {
		
	}
	
	public KbeeScriptRule(String value) {
		setScript(value);
	}
	
	public void execute(WorkflowContext context) {
		(new JsEvaluator(getScript())).evaluate(context);
	}
	
	public String getScript() {
		return script;
	}
	
	public void setScript(String value) {
		this.script = value;
	}
	
	public String getDescription() {
		StringBuilder description = new StringBuilder();
		return description.toString();
	}
	
	public String test(Object object) {
		return null;
	}
} 