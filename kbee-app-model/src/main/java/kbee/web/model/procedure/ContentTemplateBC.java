package kbee.web.model.procedure;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.model.ContentTemplate;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.BCElement;

import kbee.web.model.contentclass.ContentTemplatePage;

public class ContentTemplateBC extends BCElement {
	private static final long serialVersionUID = 1L;
	
	private IModel<ContentTemplate> model;
	
	public ContentTemplateBC(IModel<ContentTemplate> model) {
		super();
		this.model=model;
	}
	
	public ContentTemplateBC(ContentTemplate template) {
		super();
		this.model = new ObjectModel<ContentTemplate>(template);
	}
	
	@Override
	public IModel<String> getLabel() {
		return new Model<String>(model.getObject().getName() + ( model.getObject().isOnlyRootEdit() ? (" ( <span class=\"ago\"> "+ getLabelString("system") + " </span> )  ") : ""));
	}
	
//	@Override
//	public void onDetach() {
//		this.model.detach();
//		super.onDetach();
//	}
	
	@Override
	public void onClick() {
		setResponsePage(new ContentTemplatePage(this.model, false, false));
	}
}