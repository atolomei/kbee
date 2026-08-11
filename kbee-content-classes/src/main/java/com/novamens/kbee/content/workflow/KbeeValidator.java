package com.novamens.kbee.content.workflow;

import java.io.Serializable;

import com.novamens.content.base.Content;
import com.novamens.content.workflow.Validator;
import com.novamens.dom.Domain;
import com.novamens.indexer.iql.Expression;
import com.novamens.indexer.iql.IqlService;
import com.novamens.kbee.content.security.JavaIqlEvaluator;
import com.novamens.workflow.WorkflowContext;

public class KbeeValidator implements Validator, Serializable  {
	private static final long serialVersionUID = 1L;
	
	private String condition;
	private String message;
	
	transient private Expression expression;
	
	public boolean validate(WorkflowContext context) {
		if (condition==null || "".equals(condition)) 
			return false;
		
		Content content = ((KbeeContext)context).getContent();
		
		try {JavaIqlEvaluator evaluator = new JavaIqlEvaluator(getCondition(content.getDomain()));
			boolean evaluation = evaluator.evaluate(content);
			return evaluation;
		} 
		catch (RuntimeException e) {
			//logger.error(e.getStackTrace());
			return false;		
		}
		catch (Exception e) {
			//logger.error(e.getStackTrace());
			return false;		
		}
	}
	
	public String getCondition() {
		return condition;
	}
	
	public void setCondition(String  condition) {
		this.condition = condition;
	}
	
	public String getMessage() {
		return message;
	}
	
	public void setMessage(String  message) {
		this.message = message;
	}
	
	public Expression getCondition(Domain domain) {
		if (this.expression == null) 
			this.expression = domain.getService(IqlService.class).getExpression(getCondition());
		return this.expression;
	}
	
	@Override
	public String toString() {
		return getCondition();
	}
}
