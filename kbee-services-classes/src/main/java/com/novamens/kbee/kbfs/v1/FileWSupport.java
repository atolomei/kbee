package com.novamens.kbee.kbfs.v1;

import java.io.File;

public class FileWSupport {

	protected  String 	url;	 	  	 	// relative path inside the FileServer (provided by FileServer)
  	protected  File  	cachefile;	 	 	// File inside FileServer's Cache component
  	protected  File 	destfile;	        // File inside FileServer
   	
	public FileWSupport() {
	}

	
	
	public FileWSupport(String url, File destfile) {
		this.url=url;
		this.destfile= destfile;
	}
	
	public FileWSupport(String url, File destfile, File cachefile) {
		this.url=url;
		this.destfile= destfile;
		this.cachefile=cachefile;
	}

	public File getCacheFile() {
		return cachefile;
	}
	
	public File getDestFile() {
		return destfile;
	}

	public String getUrl() {
		return url;
	}
	
	
 	public FileWSupport clone() {
		FileWSupport f = new FileWSupport();
		f.url = new String(this.url);
 		if (this.destfile!=null)
			f.destfile = new File(this.destfile.getPath());
		return f;
	}
}
