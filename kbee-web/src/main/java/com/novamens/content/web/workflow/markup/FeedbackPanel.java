package com.novamens.content.web.workflow.markup;

import java.util.Iterator;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;

import com.novamens.content.model.AttributeTemplate;
import com.novamens.content.model.Classifier;
import com.novamens.content.web.content.markup.ClassificationMessage;
import com.novamens.kbee.wicket.editor.Editor;
import com.novamens.workflow.WorkflowContext;

import kbee.web.eform.FieldMessage;

@SuppressWarnings("serial")
@Deprecated
public class FeedbackPanel extends org.apache.wicket.markup.html.panel.FeedbackPanel {
	private static final long serialVersionUID = 1L;

	public FeedbackPanel(String id, IModel<WorkflowContext> model) {
		super(id);

		addOrReplace(new WebMarkupContainer("feedbackul"));
	}
	
	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
		if (get("feedbackul:message")==null) {
			((WebMarkupContainer)get("feedbackul")).add(new ListView<FeedbackMessage>("message", getCurrentMessages()) {
				public void populateItem(final ListItem<FeedbackMessage> item) {
					FeedbackMessage message = item.getModelObject();
					item.add(new Label("text", message.getMessage()).setEscapeModelStrings(false));
					item.add(new AjaxLink<Void>("link") {
						public void onClick(AjaxRequestTarget target){
							FeedbackMessage message = item.getModelObject();
							if (message instanceof ClassificationMessage) {
				 				Editor<?> editor = getEditor();
								if (((ClassificationMessage)message).getClassifier()!=null) {
									Classifier classifier = ((ClassificationMessage)message).getClassifier();
									((TaskPanel<?>)editor).showAttributes(target, classifier);
								}
								if (((ClassificationMessage)message).getAttribute()!=null) {
									AttributeTemplate template = ((ClassificationMessage)message).getAttribute();
									((TaskPanel<?>)editor).showAttributes(target, template);
								}
							}
							else
							if (message.getMessage() instanceof FieldMessage) {
								message = (FieldMessage)message.getMessage();
							}
							else {
				 				Editor<?> editor = getEditor();
								((TaskPanel<?>)editor).showAttributes(target, (Classifier)null);
							}
						}
					});
				}
			});
		}
	}
	
	
	protected Editor<?> getEditor() {
		return getEditor(getPage().iterator());
	}
	
	
	protected Editor<?> getEditor(Iterator<Component> components) {
		while (components.hasNext()) {
			Component component = components.next();
			if (component instanceof Editor<?>) {
				return (Editor<?>)component;
			}
			else {
				if (component instanceof WebMarkupContainer) {
					Editor<?> editor = getEditor(((WebMarkupContainer)component).iterator());
					if (editor!=null) {
						return editor;
					}
				}
			}
		}
		return null;
	}
}
