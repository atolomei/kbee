package com.novamens.kbee.wicket.util;

import java.io.File;
import java.nio.file.Paths;

import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.resource.FileSystemResourceReference;

public class FileSystemPhoto extends Image {

	
	FileSystemResourceReference ref;
	String name;
	String path;
	
	public FileSystemPhoto(String id, String name, File file) {
		super(id);
		this.name =name;
		this.path= file.getAbsolutePath();
	}
	
	
	public void onInitialize() {
		super.onInitialize();

	}
	
	
	public void onBeforeRender() {
		super.onBeforeRender();
		ref = new FileSystemResourceReference(name, Paths.get(path));
		
	}
	
	public void onDetach() {
		super.onDetach();
		ref = null;
	}

}
