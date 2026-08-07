package com.novamens.content.web.workflow.markup;

import java.io.Serializable;

import org.apache.wicket.Component;
import org.apache.wicket.feedback.FeedbackMessage;


public class ResolutionMessage extends FeedbackMessage {
	private static final long serialVersionUID = 1L;

	public ResolutionMessage(final Component reporter, final Serializable message, final int level) {
		super(reporter, message, level);
	}
}
