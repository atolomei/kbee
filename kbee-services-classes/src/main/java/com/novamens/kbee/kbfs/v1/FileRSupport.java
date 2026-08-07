package com.novamens.kbee.kbfs.v1;

import java.io.File;

public class FileRSupport {

 	protected long  	filesize;		 	// size of original file, (used only by FMIpuntStream, this may be shorter than file size of "file" because of encryption padding)
	protected String 	url;	 	  	 	// relative path inside the file server (provided by FileServer)
	
	protected File 		serverfile;			// File provided by server to use (source in the case of FMInputStream, dest in the case of FMOutputStream)
	protected File 		cachefile;			// Cache File provided by server to use to read
    protected File      cachetowrite;       // When reading an ecrypted file, it will try to stream to this file at the same time for next call
	
 	public void setFileSize(long size)	{
		this.filesize=size;
	}
	
	public FileRSupport() {
	}

	public FileRSupport(String url, long size) {
		this.url = url;
		this.filesize=size;
	}
	
	public FileRSupport clone() {
		FileRSupport f = new FileRSupport();
		f.filesize = this.filesize;
		f.url = new String(this.url);
		if (this.serverfile!=null)
			f.serverfile = new File(this.serverfile.getPath());
		return f;
	}
	
	public long getSize() {
		return filesize;
	}
	
	public String getUrl() {
		return url;
	}
	
	public void setServerFile(File file) {
		this.serverfile=file;
	}
	
}
