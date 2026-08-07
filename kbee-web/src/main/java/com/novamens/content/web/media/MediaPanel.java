package com.novamens.content.web.media;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.novamens.content.resource.KBFile;

public class MediaPanel extends Panel {

	private static final long serialVersionUID = -8560954058338780501L;
	private IModel<KBFile> model;
	
	public MediaPanel(String id, IModel<KBFile> model) {
		super(id);
		this.model=model;
	}
	
	
	public IModel<KBFile> getModel() {
		return model;
	}
	
	public void onDetach() {
		
		if (model!=null)
			model.detach();
		
		super.onDetach();
		
	}

}
