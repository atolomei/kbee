package kbee.web.workflow;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;

import kbee.web.eform.FieldMessage;
import kbee.web.workflow.task.ValidationEvent;

@SuppressWarnings("serial")
public class FeedbackPanel extends org.apache.wicket.markup.html.panel.FeedbackPanel {
	private static final long serialVersionUID = 1L;

	WebMarkupContainer feedbackul;
	
	public FeedbackPanel(String id) {
		super(id);
		setOutputMarkupId(true);
		
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		feedbackul = new WebMarkupContainer("feedbackul");
		addOrReplace(feedbackul);
	}
	
	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
		
		if (feedbackul.get("message")==null) {
			feedbackul.addOrReplace(new ListView<FeedbackMessage>("message", () -> getCurrentMessages()) {
				public void populateItem(final ListItem<FeedbackMessage> item) {
					FeedbackMessage message = item.getModelObject();
					if (message.getMessage() instanceof FieldMessage) {
						message = (FieldMessage)message.getMessage();
					}
					Label te=new Label("text", message.getMessage());
					te.setEscapeModelStrings(false);
					item.add(te);
					
					item.add(new AjaxLink<Void>("link") {
						public void onClick(AjaxRequestTarget target){
							FeedbackMessage message = item.getModelObject();
							if (message.getMessage() instanceof FieldMessage) {
								message = (FieldMessage)message.getMessage();
							}
							if (message instanceof FieldMessage) {
								(new ValidationEvent(target, ((FieldMessage)message).getForm(), ((FieldMessage)message).getField())).fire(getPage());;
							}
						}
						public boolean isVisible() {
							FeedbackMessage message = item.getModelObject();
							return (message.getMessage() instanceof FieldMessage); 
						}
					});
				}
			});
		}
	}
}
