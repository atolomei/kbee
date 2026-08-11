package com.novamens.content.web.integration;

import java.io.File;

public class ParentLinkFile extends File {
	
	private static final long serialVersionUID = 1L;

	public ParentLinkFile(String path) {
		super(path);
	}
	
	public String getName() {
		return " .. [ " + super.getName() + " ] " ;
	}
	
	public boolean isDirectory() {
		return true;
	}
	
}
