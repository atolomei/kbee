package kbee.web.model.procedure;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

import com.novamens.beans.BeansService;
import com.novamens.content.workflow.AttributeRule;
import com.novamens.content.workflow.ClassificationRule;
import com.novamens.content.workflow.WorkflowRule;
import com.novamens.content.workflow.WorkflowDao;
import com.novamens.kbee.content.iql.KbeeCaseExpression;
import com.novamens.kbee.content.workflow.TimeOutEndCondition;
import com.novamens.service.ServiceLocator;
import com.novamens.kbee.content.workflow.KbeeProcedure;
import com.novamens.kbee.content.workflow.KbeeTask;
import com.novamens.kbee.content.workflow.MultipleRule;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.form.BooleanField;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.StaticField;
import com.novamens.wicket.markup.html.form.TextAreaField;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.workflow.Procedure;
import com.novamens.workflow.Task;

import kbee.web.form.EditButtonsV5;

import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.markup.html.form.NumberField;

@SuppressWarnings("serial")
public class TimeoutConditionEditor extends ObjectEditor<TimeOutEndCondition> {
	private static final long serialVersionUID = 1L;

	static Logger logger = LogManager.getLogger(TimeoutConditionEditor.class.getName());
	
	private IModel<Task> taskmodel;
	
	class DueDateExpressionValidator implements IValidator<String> {
		@Override
		public void validate(final IValidatable<String> validatable) {
			final String expressionstr = validatable.getValue();
			if ("".equals(expressionstr) || expressionstr==null) {
				return;
			}
			KbeeCaseExpression expression = new KbeeCaseExpression(getDomain(), expressionstr);
			if (!expression.isValid()) {
				validatable.error(new ValidationError(this));
			}
		}
	}
	
	public TimeoutConditionEditor(IModel<Task> taskmodel, IModel<TimeOutEndCondition> model) {
		this("editor", taskmodel, model);
	}
	
	public TimeoutConditionEditor(String id, IModel<Task> taskmodel, IModel<TimeOutEndCondition> model) {
		super(id, model);
		
		setTask(taskmodel);
		
		setOutputMarkupId(true);
		
		setEditionEnabled(false);

		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);
		
		form.add(new TextField<String>("label"));
		form.add(new StaticField<String>("event"));
		form.add(new BooleanField("enabled"));
		
		form.add(new TextAreaField<String>("note"));
	
		form.add(new ClassifiersRulesEditor<Procedure>("classifiersrules") {
			@Override
			public List<ClassificationRule> getRules() {
				return TimeoutConditionEditor.this.getClassifiersRules();
			}
			@Override
			public void setRules(List<ClassificationRule> rules) {
				TimeoutConditionEditor.this.setClassifiersRules(rules);
			}
		});
		
		form.add(new AttributesRulesEditor<Procedure>("attributesrules") {
			@Override
			public List<AttributeRule> getRules() {
				return TimeoutConditionEditor.this.getAttributesRules();
			}
			@Override
			public void setRules(List<AttributeRule> rules) {
				TimeoutConditionEditor.this.setAttributesRules(rules);
			}
		});
		
		form.add(new NumberField<Integer>("duration"));
		
		add(form);
		
		add(new EditButtonsV5<TimeOutEndCondition>(this) {
			@Override
			public boolean isVisible() {
				return true;
			}
			@Override
			public boolean isEnabled() {
				return true;
			}
		});
	}
	
	public Task getTask() {
		return taskmodel.getObject();
	}
	
	public void setTask(IModel<Task> model) {
		this.taskmodel = model;
	}
	
	public void update(AjaxRequestTarget target) {
		KbeeProcedure procedure = (KbeeProcedure)((KbeeTask)getTask()).getProcedure();
		procedure.setTasks(procedure.getTasks());
		onUpdate(target);
		getWorkflowDao().update(procedure);
	}
	
	public void onUpdate(AjaxRequestTarget target) {
	}
	
	
	private void setClassifiersRules(List<ClassificationRule> classifiersrules) {
		List<WorkflowRule> rules = new ArrayList<WorkflowRule>();
		rules.addAll(classifiersrules);
		rules.addAll(getAttributesRules());
		MultipleRule rule = new MultipleRule(rules);
		getModelObject().setRule(rule);
	}
	
	private List<ClassificationRule> getClassifiersRules() {
		List<ClassificationRule> rules = new ArrayList<ClassificationRule>();
		WorkflowRule rule = getModelObject().getRule();
		if (rule instanceof ClassificationRule) {
			rules.add((ClassificationRule)rule);
		}
		else {
			if (rule instanceof MultipleRule) {
				for (WorkflowRule singlerule : ((MultipleRule)rule).getRules()) {
					if (singlerule instanceof ClassificationRule) {
						rules.add((ClassificationRule)singlerule);
					}
				}
			}	
		}
		return rules;
	}
	
	private void setAttributesRules(List<AttributeRule> attributesrules) {
		List<WorkflowRule> rules = new ArrayList<WorkflowRule>();
		rules.addAll(getClassifiersRules());
		rules.addAll(attributesrules);
		MultipleRule rule = new MultipleRule(rules);
		getModelObject().setRule(rule);
	}
	
	private List<AttributeRule> getAttributesRules() {
		List<AttributeRule> rules = new ArrayList<AttributeRule>();
		WorkflowRule rule = getModelObject().getRule();
		if (rule instanceof AttributeRule) {
			rules.add((AttributeRule)rule);
		}
		else {
			if (rule instanceof MultipleRule) {
				for (WorkflowRule singlerule : ((MultipleRule)rule).getRules()) {
					if (singlerule instanceof AttributeRule) {
						rules.add((AttributeRule)singlerule);
					}
				}
			}	
		}
		return rules;
	}
	
	private WorkflowDao getWorkflowDao() {
		return (WorkflowDao)ServiceLocator.getService(BeansService.class).getBean("WorkflowDao");
	}
	
//	private Domain getDomain() {
//		return ServiceLocator.getService(UserService.class).getDomain();
//	}
}
