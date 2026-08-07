package com.novamens.kbee.kbfs.v1;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.novamens.kbfs.v1.FSInputStream;

public class SimpleFSInputStream extends InputStream implements FSInputStream {

	static private final int BUFFER_SIZE = 8192;
	
	private int bufferSize = BUFFER_SIZE;
	
	private FileRSupport		 	 fs	= null;
	private BufferedInputStream   	 in = null;

	public SimpleFSInputStream(FileRSupport fs) throws IOException {
		this.fs = fs;
		open();
	}

	public void setBufferSize(int size) {
		bufferSize = size;
	} 
	
	public int  getBufferSize() {
		return bufferSize;
	} 
 		 
	public void open() throws IOException  {
		in = new BufferedInputStream(new FileInputStream(fs.serverfile), BUFFER_SIZE);
	}

	public int available() throws IOException {
		return in.available();
	}

	public int read(byte[] buffer, int offset, int len) throws IOException 	{
		return in.read(buffer, offset, len);
	}
	
	public int read(byte[] buffer) throws IOException 	{
		return read(buffer, 0, buffer.length);
	}
	
	public void close() throws IOException {
		if (in!=null)
			in.close();
	}
	
	@Override
	public long skip(long value) throws IOException{
		return in.skip(value);
	}

	@Override
	public int read() throws IOException {
		throw new IOException("Not implemented");
	}
}
