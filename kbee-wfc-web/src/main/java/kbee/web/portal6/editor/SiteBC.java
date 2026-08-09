package kbee.web.portal6.editor;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.portal6.model.Site;
import com.novamens.wicket.util.BCElement;

public class SiteBC extends BCElement {
			
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	IModel<Site> model;
	
	public SiteBC(IModel<Site> model) {
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
		setResponsePage(new PortalSiteEditorPage(model));
		
	}
	
	
	@Override
	public IModel<String> getLabel() {
		return new Model<String> (this.model.getObject().getDisplayName());
	}
}
