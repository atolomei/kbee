package kbee.web.security.user;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import com.novamens.kbee.wicket.markup.html.ajax.WorkingIndicatorAjaxButton;

public class SendMailPanel extends Panel {
		
	private static final long serialVersionUID = -889711502398762216L;

	private String title;
	
	public SendMailPanel(String id) {
		super(id);
		setOutputMarkupId(true);
		SendMailForm form = new SendMailForm("send-mail-form");
		add(form);
	}
	
	public void setTitle(String title) 					{this.title = title;}
	public String getTitle() 							{return this.title;}
	
	public void close(AjaxRequestTarget target) {}
		
	public void onSubmit(AjaxRequestTarget target) {
		close(target);
	}
	
	public class SendMailForm extends Form<Void> {
	
		private static final long serialVersionUID = 1998712946245508423L;

		public SendMailForm(String id) {
			super(id);
			
			setOutputMarkupId(true);
			
			add(new WorkingIndicatorAjaxButton("ok-link", SendMailForm.this) {
			
				private static final long serialVersionUID = -2460231970138298598L;
			
				  	@Override
					protected void onSubmit(AjaxRequestTarget target) {
				  		SendMailPanel.this.onSubmit(target);
					}
					
					 @Override
					 protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
						super.updateAjaxAttributes(attributes);
					}
					
					@Override
					public boolean isEnabled() {
						return true;
					}
			});

		}

		public void onDetach() {
			super.onDetach();
		}
		
	}
}
