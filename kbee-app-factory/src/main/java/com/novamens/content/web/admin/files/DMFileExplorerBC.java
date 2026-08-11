package com.novamens.content.web.admin.files;

import com.novamens.content.web.admin.markup.datamanagement.SystemDataManagementGeneralPage;
import com.novamens.wicket.util.BCElement;

public class DMFileExplorerBC extends BCElement {
			
	private static final long serialVersionUID = 1L;

	public DMFileExplorerBC() {
		super("bc.file-explorer");
	}
	
	@Override
	public void onClick() {
		setResponsePage(new SystemDataManagementGeneralPage("file-explorer"));
	}
	

}
