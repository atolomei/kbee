package kbee.web.emailtemplate;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.email.EmailTemplate;
import com.novamens.wicket.util.BCElement;

public class EmailTemplateBC extends BCElement {
				
	private static final long serialVersionUID = 1L;
	
	IModel<EmailTemplate> model;
	
	public EmailTemplateBC(IModel<EmailTemplate> model) {
		super();
		this.model=model;
	}
	
	@Override
	public IModel<String> getLabel() {
		return new Model<String>(model.getObject().getTitle() /** + " <span class=\"ago\">("+new StringResourceModel("emailtemplate", this, null).getObject()+")</span>"*/);
	}
	
	@Override
	public void onDetach() {
		this.model.detach();
		super.onDetach();
	}
	
	@Override
	public void onClick() {
		setResponsePage(new EmailTemplatePage(this.model));
	}
	
}
