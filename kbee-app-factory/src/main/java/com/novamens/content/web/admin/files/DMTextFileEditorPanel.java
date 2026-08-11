package com.novamens.content.web.admin.files;



import java.io.File;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.novamens.content.web.admin.markup.datamanagement.AbstractDataManagementPanel;

import kbee.web.error.ErrorPanel;

public class DMTextFileEditorPanel extends AbstractDataManagementPanel {
			
	private static final long serialVersionUID = 1L;
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(DMTextFileEditorPanel.class.getName());

	IModel<File> model;
	
	
	/**
	public DMTextFileEditorPanel(IModel<File> model) {
		super("info-panel");
		setOutputMarkupId(true);
		this.model=model;
	}**/
	
	
	public DMTextFileEditorPanel(String id, PageParameters parameters) {
		super(id);
		 setOutputMarkupId(true);
		 
		 if (parameters!=null) {
			 
			 String fn= parameters.get("file").toOptionalString();
			 if (fn!=null) {
				 File fi=new File(fn);
				 if (fi.exists() && !fi.isDirectory())
					 model=new Model<File>(fi);
			 }
		 }
	}

	
	public void onDetach() {
		super.onDetach();
		model.detach();
	}
	
	public IModel<File> getModel() {
		return model;
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		if (getModel()!=null)
			add( new TextFileEditor("text-file-editor", getModel()));
		else
		add( new ErrorPanel("text-file-editor", new Model<String>("file not found")));
		
	}

	/**
	protected List<BCElement> getPageBreadCrumb() {
		List<BCElement> li = new ArrayList<BCElement>();
		li.add(new DMFileExplorerBC());
		li.add(new BCElement(new Model<String>("Text File Editor")));
		return li;
	}
	**/

	

}
