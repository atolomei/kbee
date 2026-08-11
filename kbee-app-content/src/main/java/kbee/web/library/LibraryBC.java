package kbee.web.library;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.library.Library;
import com.novamens.wicket.util.BCElement;


public class LibraryBC extends BCElement {
	
	private static final long serialVersionUID = 1L;
	
	IModel<Library> model;
	
	public LibraryBC(IModel<Library> model) {
		super();
		this.model=model;
	}
	
	@Override
	public IModel<String> getLabel() {
		return new Model<String>(model.getObject().getName() /** + " <span class=\"ago\">("+new StringResourceModel("library", this, null).getObject()+")</span>"*/);
	}
	
	@Override
	public void onDetach() {
		this.model.detach();
		super.onDetach();
	}
	
	@Override
	public void onClick() {
		setResponsePage(new LibraryPage(this.model));
	}
}
