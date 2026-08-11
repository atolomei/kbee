package com.novamens.content.web.integration;

import com.novamens.wicket.util.BCElement;

public class FileServerBC extends BCElement {

	private static final long serialVersionUID = 1L;

	public FileServerBC () {
		super("bc.file-server");
	}
	
	@Override
	public void onClick() {
		setResponsePage(new FileSystemIntegrationPage());	
	}

	
}
