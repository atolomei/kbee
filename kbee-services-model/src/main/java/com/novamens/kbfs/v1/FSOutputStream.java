package com.novamens.kbfs.v1;

import java.io.IOException;
import java.io.OutputStream;

/**
 * 
 *
 */
public interface FSOutputStream {

	public void setBufferSize(int size); 
	public int  getBufferSize();
	
	public String getRelativeUrl();
	public String getAbsolutePath();
	
	
	public String getId();
	public String getName();

	public int bytesWritten();
	
	public OutputStream getStream();
	
	public void open() throws IOException;
	public void close() throws IOException;
	
	public void write(byte[] buffer, int off, int len) throws IOException;
	public void write(byte[] buffer) throws IOException;
	
}
