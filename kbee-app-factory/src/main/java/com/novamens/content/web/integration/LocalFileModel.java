package com.novamens.content.web.integration;

import java.io.File;

import org.apache.wicket.model.IModel;
import org.springframework.util.Assert;


public class LocalFileModel implements IModel<File> {

     
	private static final long serialVersionUID = 7220404352371636140L;
	private File file;
     private String path;

     public  LocalFileModel(File file) {
    	 Assert.isTrue(file!=null, "file is null");
    	 setObject(file);
     }
     
     
	@Override
	public File getObject() {
		if (file==null)
			file=new File(path);
		return file;
	}
	
	
	@Override
	public void setObject( File file) {
		this.file=file;
		this.path=file.getAbsolutePath();
	}
	
	
	public void detach() {
		if (file==null)
			return;
		path=file.getAbsolutePath();
		file=null;
	}
	
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof File))
			return false;
		if (path==null)
			return false;
		return path==((File) obj).getAbsolutePath(); 
	}

}
