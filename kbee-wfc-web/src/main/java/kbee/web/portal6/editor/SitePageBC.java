package kbee.web.portal6.editor;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.portal6.model.Page;
import com.novamens.wicket.util.BCElement;

public class SitePageBC extends BCElement {
				
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	IModel<Page> model;
	
	public SitePageBC(IModel<Page> model) {
		super();
		this.model=model;
		
	}
	
	
	public void onDetach() {
		super.onDetach();
		if (model!=null)
			model.detach();
	}
	@Override
	public void onClick() {
		//setResponsePage(new PortalPageEditorPage(model));
		setResponsePage(new PortalPageStructureEditorPage(model));
		
	}
	
	
	@Override
	public IModel<String> getLabel() {
		return new Model<String> (this.model.getObject().getDisplayName());
	}
}
