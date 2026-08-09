package kbee.web.nav;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.novamens.content.library.Library;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.util.BCElement;

import kbee.web.service.ApplicationSiteMapService;

public class LibraryBC extends BCElement {
	private static final long serialVersionUID = 1L;
	
	private IModel<Library> model;
	
	public LibraryBC(IModel<Library> mcabinet) {
		this.model=mcabinet;
	}

	@Override
	public IModel<String> getLabel() {
		return new Model<String>(getLibraryModel().getObject().getDisplayName());
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		if (model!=null)
			model.detach();
		
	}
	
	public IModel<Library> getLibraryModel() {
		return model;
	}
	
	public void onClick() {
		//setResponsePage( new ContentBasePage(getLibraryModel()));
		PageParameters pa= new PageParameters();
	    pa.add("library", getLibraryModel().getObject().getKey());
	    setResponsePage( ServiceLocator.getService(ApplicationSiteMapService.class).getPage("library-contentbase-page", pa));
	}
}
