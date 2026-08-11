package com.novamens.content.service;

import java.io.Serializable;

public class FileSnippet implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public String snippet;
	public String file;
	public FileSnippet(String file, String snippet) {
		this.snippet = snippet;
		this.file = file;
	}
}