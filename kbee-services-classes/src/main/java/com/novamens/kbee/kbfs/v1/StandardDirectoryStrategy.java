package com.novamens.kbee.kbfs.v1;

import java.io.File;
import java.io.IOException;

import com.novamens.kbfs.v1.FileServerV1;

import kbee.util.FSUtils;

public class StandardDirectoryStrategy implements SubDirectoryGenerationStrategy {

	String root;
	
	private String name;

	public String getName() {return name;}
	public void setName(String name) {this.name=name;}

	
	public StandardDirectoryStrategy(String root) {
		this.root=root;
		setName("Standard strategy");
	}
	
	@Override
	public String generateRelativePath(SubDirGenerationStrategyContext context) throws IOException {

		String filename = context.filename; 			// filename 
  		String domain = context.domain.toLowerCase();	// domain	(param 1 is id, not used)
  		// String bucket = context.bucket;					// bucket may be null
  		
		String d1;
		String d2;

		String hashcode	= String.valueOf(Math.abs(filename.hashCode()));
			
		
 		if (context.repo_type==null)
 			context.repo_type=FileServerV1.FAST;

		
		// d1: 000 - 999
		if (hashcode.length()>3)
			d1=hashcode.substring(0, 3);
		else
			d1="kb"+hashcode;
					
	    // d2: 000 - 999 
		d2 = String.valueOf(Math.random()*10000).substring(0,3);
					
		// subdirectories (relative to FServer root) 
		// String relpath = domain + (bucket!=null?File.separator+bucket:"") + File.separator + d1 + File.separator + d2; 
		String relpath = domain + File.separator + d1 + File.separator + d2;
												
			
		// subdirectories + filename (relative to FServer root)
		String urlbase 	= relpath + File.separator + filename;
		    
		// absolute path (root +  repo_type + subdirectories + filename)
		String abspath 	= root + File.separator +  context.repo_type  + File.separator + urlbase;
		    
		File destFile = new File(abspath);
		   
			String url 		= urlbase;
			int counter 	= 1;
			boolean bexists = destFile.exists();
		    
			String[] arrprefix;
			
			String   prefix;
			String   suffix;
			
			String basename = FSUtils.getBaseName(filename);
			String ext = FSUtils.getExtension(filename);
			String newfilename;
			arrprefix = basename.split("_v");
			if (arrprefix.length>1) 
		    	  prefix = basename.substring(0, basename.length()-2-arrprefix[arrprefix.length-1].length());
		    else
		    	prefix = basename;
			
			while (bexists && counter++<10000) {
				suffix = "_v" + String.valueOf(counter);
				newfilename = prefix + suffix + "." + ext; 
				url = relpath + File.separator + newfilename; 
				abspath = root + File.separator + url;
				bexists = (new File(abspath)).exists();
			}
		   		if (bexists)
		   				throw new IOException("too many versions, can not create file.");
	   		return url;
		}
}
