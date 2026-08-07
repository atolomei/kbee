package com.novamens.kbee.kbfs.v1;

import java.io.File;

public class FileSupport {

		@Override
	public String toString() {
		return "FileSupport [id=" + id + ", srcfile=" + srcfile + ", filename=" + filename + ", filesize=" + filesize
				+ ", url=" + url + ", domainname=" + domainname + ", serverfile=" + serverfile + "]";
	}

		// write
		protected String 	id;				 // id of original file
		protected File 		srcfile;		 // src file if exists (sometimes original file is a stream from the web)
		protected String 	filename;		 // original filename (without path)
		
		// read
		protected long  	filesize;		 	// size of original file, (used only by FMIpuntStream, this may be shorter than file size of "file" because of encryption padding)
		protected String 	url;	 	  	 	// relative path inside the file server (provided by FileServer) 
		
		// both
		protected String 	domainname;		 	// domain where the belongs
		protected File 		serverfile;			// File provided by server to use (source in the case of FMInputStream, dest in the case of FMOutputStream)
		

		public void setId(String id) 				{this.id=id;}
		public void setSrcFile(File src)			{this.srcfile=src;}
		public void setFileName(String name)		{this.filename=name;}
		public void setDomainName(String domain)	{this.domainname=domain;}
		public void setFileSize(long size)			{this.filesize=size;}
		
		public File getSrcFile() 					{return this.srcfile;}
 		
		public FileSupport() {
		}
		
 		public FileSupport clone() {
			FileSupport f = new FileSupport();
			f.id = new String(this.id);
			f.filename =  new String(this.filename);	
			f.domainname = new String(this.domainname);
			f.filesize = this.filesize;
			f.url = new String(this.url);
			if (this.srcfile!=null)
				f.srcfile = new File(this.srcfile.getPath());
			if (this.serverfile!=null)
				f.serverfile = new File(this.serverfile.getPath());
			return f;
		}
	}


