package kbee.web.model.util;

import java.io.File;

import org.apache.wicket.model.IModel;

public class FileModel implements IModel<File> {
	private static final long serialVersionUID = 1L;
	
	private File file;
	private String absolutePath;
	
	public FileModel(File file) {
		this.file = file;
	}
	
	@Override
	public File getObject() {
		if (file==null && absolutePath!= null) {
			file = new File(absolutePath);
		}
		return file;
	}
	
	public void setObject(File file) {
		this.file = file;
	}
	
	@Override
	public void detach() {
		if (file!=null) {
			absolutePath = file.getAbsolutePath();
			file = null;
		}
	}
}