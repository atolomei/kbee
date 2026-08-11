package kbee.web.model;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.model.Attribute;
import com.novamens.content.model.ContentTemplate;
import com.novamens.wicket.util.BCElement;

import kbee.web.model.contentclass.ContentTemplatePage;

public class AttributeBC extends BCElement {

				
	IModel<Attribute> model;
	
	public AttributeBC(IModel<Attribute> model) {
		super();
		this.model=model;
	}
	
	@Override
	public IModel<String> getLabel() {
		return new Model<String>(model.getObject().getName()!=null?model.getObject().getName(): ("id->"+model.getObject().getId().toString()) /* + " <span class=\"ago\">("+new StringResourceModel("attribute", this, null).getObject()+")</span>"*/);
	}
	
	@Override
	public void onDetach() {
		this.model.detach();
		super.onDetach();
	}
	
	@Override
	public void onClick() {
		setResponsePage(new AttributeModelPage(this.model));
	}
}
