package kbee.web.model.procedure;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

import com.novamens.content.model.ContentTemplate;
import com.novamens.content.web.editor.markup.ObjectEditorPanel;
import com.novamens.content.workflow.ScriptRule;
import com.novamens.kbee.content.workflow.JsEvaluator;
import com.novamens.kbee.content.workflow.KbeeScriptRule;
import com.novamens.wicket.markup.html.form.TextAreaField;
import com.novamens.wicket.markup.html.form.Field.Width;
import com.novamens.wicket.markup.html.modal.InfoDialog;

@SuppressWarnings("serial")
public class ScriptRuleEditor<T> extends ObjectEditorPanel<T> {
	private static final long serialVersionUID = 1L;
	private String script;
	
	
	class ScriptValidator implements IValidator<String> {
		@Override
		public void validate(final IValidatable<String> validatable) {
			String script = validatable.getValue();
			if (script==null)
				return;
			JsEvaluator executor = new JsEvaluator(script);
			String message = executor.validate(getTemplate());
			if (message!=null) {
				validatable.error(new ValidationError(message));
			}
		}
	}

	public ScriptRuleEditor(String id) {
		super(id);	
		
	}
	
	public String getScript() {
		return script;
	}
	
	public void setScript(String script) {
		this.script = script;
	}
	
	public ScriptRule getRule() {
		return null;
	}
	
	public void setRule(ScriptRule rule) {
	}
	
	@Override
	public void updateModel() {
		setRule(new KbeeScriptRule(getScript()));
	}
	
	public ContentTemplate getTemplate() {
		return null;
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		add(new InfoDialog("help-modal"));
		
		setScript(getRule()!=null ? getRule().getScript() : null) ;

		add(new TextAreaField<String>("script", new PropertyModel<String>(this, "script"), false, Width.W12, new ScriptValidator(), 4, 40) {
			@Override
			public boolean isHelpInfo() {
				return true;
			}
			@Override
			public void onHelp(AjaxRequestTarget target) {
				IModel<String> s= getScriptHelp();
				getHelpModal().open(target, () -> { return "How to write a Script"; }, s);
			}
		});
	}
	
	private IModel<String> getScriptHelp() {
		
		ContentTemplate  te = getTemplate();
		
		if (te==null)
			return new Model<String>("template is null");
		
		String s=JsEvaluator.GetHelpText(getTemplate());
		
		if (s==null)
			return new Model<String>("JSEvaluator is null");
		
		return new Model<String>(s);
	}
	
	private InfoDialog getHelpModal() {
		return (InfoDialog) get("help-modal");
	}
}
