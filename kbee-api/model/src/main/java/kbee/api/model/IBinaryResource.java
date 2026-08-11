package kbee.api.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class IBinaryResource extends ApiResource {
	private static final long serialVersionUID = 1L;
	private File file;
	private InputStream   stream;
	
	public IBinaryResource() {
	}
	
	public IBinaryResource(File file) {
		setFile(file);
		setName(file.getName());
	}
	
	public File getFile() {
		return file;
	}
	
	public void setFile(File file) {
		this.file = file;
	}
	
	public void setStream(InputStream stream) {
		this.stream = stream;
	}
	
	public InputStream getStream() throws IOException {
		return stream!=null ? stream : (file!=null ? new FileInputStream(file) : null);
	}
}
