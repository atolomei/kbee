package kbee.web.model.procedure;

import java.util.List;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

import com.novamens.content.workflow.LetterRule;
import com.novamens.content.workflow.WorkflowRule;
import com.novamens.kbee.content.workflow.KbeeLetterRule;
import com.novamens.kbee.content.workflow.ManualEndCondition;
import com.novamens.kbee.content.workflow.MultipleRule;
import com.novamens.wicket.markup.html.editor.ObjectEditorPanel;
import com.novamens.wicket.markup.html.form.TextAreaField;
import com.novamens.workflow.Task;


@SuppressWarnings("serial")
public class LetterEditor extends ObjectEditorPanel<ManualEndCondition> {
	private static final long serialVersionUID = 1L;
	
	private String text = null;

	IModel<Task> taskmodel;
	
	public LetterEditor(IModel<Task> taskmodel) {
		super("letter");
		
		this.taskmodel=taskmodel;
		setOutputMarkupId(true);

	}

	public void onDetach() {
		super.onDetach();
		if (this.taskmodel!=null)
			this.taskmodel.detach();
	}
	
	
	public IModel<Task> getTaskModel() {
		return this.taskmodel;
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();

		setRule(getModelObject().getRule());
		
		
		add(new TextAreaField<String>("template", new PropertyModel<String>(this, "template")) {
			
			@Override
			public boolean isVisible() {
				return true;
			}
			
			@Override
			protected IModel<String> getHelpText() {
				//return new Model<String>("The text above will replace the macro ${text} in the Email Template -> " + getNotificationEmailTemplateLink());
				//return new StringResourceModel("isemail.text", ENotiRuleEditor.this, null).setParameters(new Object[] {getPublishEmailTemplateLink(), getPendingEmailTemplateLink()});
				return null;
			}
		});
		
		
		

	}

	@Override
	public void updateModel() {
		WorkflowRule multiplerule = getModelObject().getRule();
		
		if (multiplerule!=null) {
			if (multiplerule instanceof MultipleRule) {
				List<WorkflowRule> rules = ((MultipleRule)multiplerule).getRules();
				for (WorkflowRule rule : rules) {
					if (rule instanceof LetterRule) {
						rules.remove(rule);
						break;
					}
				}
			}
			else {
				multiplerule = null;
			}
		}
		
		if (multiplerule==null) {
			multiplerule = new MultipleRule();
			getModelObject().setRule(multiplerule);
		}
		
		if (getTemplate()!=null) {
			KbeeLetterRule rule = new KbeeLetterRule();
			rule.setTemplate(getTemplate());
			List<WorkflowRule> rules = ((MultipleRule)multiplerule).getRules();
			rules.add(rule);
		}
	}
	
	public void setTemplate(String text) {
		this.text = text;
	}
	
	public String getTemplate() {
		return text;
	}
	
	
	public void setRule(WorkflowRule multiplerule) {
		if (multiplerule!=null) {
			if (multiplerule instanceof MultipleRule) {
				List<WorkflowRule> rules = ((MultipleRule)multiplerule).getRules();
				for (WorkflowRule rule : rules) {
					if (rule instanceof LetterRule) {
						setTemplate(((LetterRule)rule).getTemplate());
						break;
					}
				}
			}
		}
	}
}