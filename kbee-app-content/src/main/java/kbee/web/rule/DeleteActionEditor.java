package kbee.web.rule;

import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.rule.ActionRule;
import com.novamens.content.rule.DeleteAction;
import com.novamens.wicket.markup.html.editor.ObjectEditorPanel;
import com.novamens.wicket.markup.html.form.StaticField;

public class DeleteActionEditor extends ObjectEditorPanel<ActionRule> {
	private static final long serialVersionUID = 1L;
	
	public DeleteActionEditor(DeleteAction action) {
		super("editor");
		add(new StaticField<String>("type", new StringResourceModel("type", this)));
	}
}
