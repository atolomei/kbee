package com.novamens.kbfs.v1;


import java.io.IOException;

/**
 * 
 */
public interface FSInputStream  {
	
	public void setBufferSize(int size); 
	public int  getBufferSize(); 
	
	public void open() throws IOException; 
	public void close() throws IOException;
	
	public int available() throws IOException;

	public int read(byte[] buffer) throws IOException;
	public int read(byte[] buffer, int offset, int len) throws IOException;
	
	
}
