
package com.novamens.kbee.kbfs.v1;



import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.novamens.kbfs.v1.FSOutputStream;


/**
 *
 */
public class SimpleFSOutputStream extends OutputStream implements FSOutputStream {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SimpleFSOutputStream.class.getName());

	
	static private final int BUFFER_SIZE = 8192;
	// static private final boolean cacheenabled = false;
	
	private String name = null;
	private FileWSupport fs = null;
  
	private BufferedOutputStream out = null;
	// private BufferedOutputStream outcache = null;
	
	private int bytesWritten = 0;
	private int bufferSize = BUFFER_SIZE;

	private KbeeFileServer fileserver = null;
 	

	/*
	private void writeObject(java.io.ObjectOutputStream out)  throws IOException {
		if (out!=null)
			out.close();
		out=null;
		this.fileserver = null;
	}
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
	}
	*/

	protected SimpleFSOutputStream(KbeeFileServer fileserver, FileWSupport fs) throws IOException {
		this.fs = fs;
		this.fileserver = fileserver;
		// this.fcache = fs.cachefile;
		init();
	}
	

	public void setBufferSize(int size) {
		bufferSize = size; 	
	} 
	public int  getBufferSize() {
		return bufferSize; 	
	} 
	public String getRelativeUrl() {
		return fs.url;	
	} 		
	
	public String getId() {
		return getRelativeUrl();	
	}
	
	public String getName()	{
		return name;	    	
	}


	public int bytesWritten() { 
		return bytesWritten;
	}

	public void open() throws IOException {
		
		fileserver.createDirsIfNotExist(fs);
		
		logger.debug(fs.destfile.getAbsolutePath());
		
		out	= new BufferedOutputStream(new FileOutputStream(fs.destfile), getBufferSize());		
		
		// if (cacheenabled) {
		// 	outcache = new BufferedOutputStream(new FileOutputStream(fcache), getBufferSize());
		// }
		
		bytesWritten = 0;
	}
	
	public OutputStream getStream() {
		return out;
	}
	
	public void write(byte[] buffer, int off, int len) throws IOException {
		out.write(buffer, off, len);
		
		// if (cacheenabled)
		//	 outcache.write(buffer, off, len);
		
		if (len>0)
			bytesWritten+=len;
	
	}
	
	public void write(byte[] buffer) throws IOException {
		out.write(buffer,0,buffer.length);
		
		// if (cacheenabled)
		//	outcache.write(buffer, 0, buffer.length);
		
		if (buffer.length>0)
			bytesWritten+=buffer.length;
	}
	
	public void close() throws IOException {

		if (out!=null)
			out.close();

		
		// if (outcache!=null)
		//	outcache.close();
		//
		// if (cacheenabled)
		//	 fileserver.addToCache(fs.url, fcache);
		//
		
		if (fileserver!=null)
			fileserver.addSize((long) bytesWritten);
	}
	@Override
	public void write(int arg0) throws IOException {
		throw new IOException("Not implemented");
	}
	protected void setName(String name) {
		this.name=name;	
	}
	
	protected FileWSupport getFileWSupport() {
		return fs;			
	}
	
	private void init() throws IOException {
		open();
	}


	@Override
	public String getAbsolutePath() {
		if (fs!=null && fs.destfile!=null)
			return fs.destfile.getAbsolutePath();
		return null;
	}
}
 