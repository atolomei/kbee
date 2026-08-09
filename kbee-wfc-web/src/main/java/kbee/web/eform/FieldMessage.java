package kbee.web.eform;

import org.apache.wicket.Component;
import org.apache.wicket.feedback.FeedbackMessage;

import com.novamens.content.form.EForm;
import com.novamens.content.form.EFormField;

public class FieldMessage extends FeedbackMessage {
	private static final long serialVersionUID = 1L;

	EForm form;
	EFormField<?> field;

	public FieldMessage(final Component reporter, EForm form, EFormField<?> field, String key, final int level) {
		super(reporter, key, level);
		this.field = field;
		this.form = form;
	}

	public EForm getForm() {
		return form;
	}
	
	public EFormField<?> getField() {
		return field;
	}
}