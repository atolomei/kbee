package com.novamens.content.web.markup;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.feedback.IFeedbackMessageFilter;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;

@SuppressWarnings("serial")
public abstract class EditorForm extends Form<Void> {
	private static final long serialVersionUID = 1L;
	
	private FeedbackPanel feedback;
	
	public class MessageFilter implements IFeedbackMessageFilter {
		List<FeedbackMessage> messages = new ArrayList<FeedbackMessage>();
		
		public void clearMessages(){
			messages.clear();
		}

		@Override
		public boolean accept(FeedbackMessage currentMessage){
			for(FeedbackMessage message: messages){
				if(currentMessage.getMessage()!=null && message.getMessage().toString().equals(currentMessage.getMessage().toString()))
					return false;
			}
			messages.add(currentMessage);
			return true;
		}
	}
	
	public EditorForm(String id) {
		super(id);
		setFeedbackPanel(newFeedbackPanel());
		add(getFeedbackPanel());
	}
	
	public void setFeedbackPanel(FeedbackPanel panel) {
		this.feedback = panel;
	}
							
	public FeedbackPanel getFeedbackPanel() {
		return feedback;
	}
	
	public void onError(AjaxRequestTarget target) {
		target.add(getFeedbackPanel());
	}
						
	public void onInfo(AjaxRequestTarget target) {
		target.add(getFeedbackPanel());
	}
	
	public void error(String message) {
		getFeedbackPanel().error(message);
	}
	
	public void info(String message) {
		getFeedbackPanel().info(message);
	}
	
	public void clearFeedback() {
		((MessageFilter)getFeedbackPanel().getFilter()).clearMessages();
	}
	
	public String update() {
		return null;
	}
	
	public String update(boolean auto) {
		return null;
	}
	
	public boolean isEditionEnabled() {
		return true;
	}

	public void reset() {
	}
	
	protected void beforeUpdateFormComponentModels() {
	}

	protected FeedbackPanel newFeedbackPanel() {
		FeedbackPanel feedback = new FeedbackPanel("feedback") {
			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				if (this.anyErrorMessage()) {
					tag.append("class", "error", " ");
				} 
				else if (anyMessage(FeedbackMessage.SUCCESS)) {
					tag.append("class", "ok", " ");
				}
			}
		};
		feedback.setEscapeModelStrings(false);
		feedback.setOutputMarkupId(true);
		feedback.setFilter(new MessageFilter());
		
		return feedback;
	}
}
