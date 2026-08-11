package com.novamens.content.resource;

import java.util.List;

import com.novamens.content.base.Resource;

public interface HTMLText extends Resource {

	public String getText();
	public List<com.novamens.content.resource.KBFile> getFiles();
	public void add(KBFile file);
	public String toString();
	

}
