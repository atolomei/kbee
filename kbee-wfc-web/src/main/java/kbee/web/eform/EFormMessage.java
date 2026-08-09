package kbee.web.eform;

import org.apache.wicket.Component;
import org.apache.wicket.feedback.FeedbackMessage;

import com.novamens.content.form.EForm;

public class EFormMessage extends FeedbackMessage {
	private static final long serialVersionUID = 1L;

	EForm form;

	public EFormMessage(final Component reporter, EForm form, String key, final int level) {
		super(reporter, key, level);
		this.form = form;
	}

	public EForm getForm() {
		return form;
	}
}