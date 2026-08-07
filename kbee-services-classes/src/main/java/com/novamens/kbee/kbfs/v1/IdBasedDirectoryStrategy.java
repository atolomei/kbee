package com.novamens.kbee.kbfs.v1;

import java.io.File;
import java.io.IOException;

import com.novamens.kbfs.v1.FileServerV1;

public class IdBasedDirectoryStrategy implements SubDirectoryGenerationStrategy {

	private String name;

	public String getName() {return name;}
	public void setName(String name) {this.name=name;}
	
	public IdBasedDirectoryStrategy() {
		setName("Id based strategy");
	}
	/**
	 * Since id must be unique. it is not verified that the file does not exist.
	 * 
	 */
	@Override
	public String generateRelativePath(SubDirGenerationStrategyContext context) throws IOException {
	
 		String filename = context.filename;					// filename
		String id; 											// param[ID] is the id
		String domain 	= context.domain.toLowerCase();		// domain
 		// String bucket 	= context.bucket;					// may be null
 		
 		if (context.repo_type==null)
 			context.repo_type=FileServerV1.FAST;
 		
		String d1;
		String d2;

		if (context.id.length()<6) 
			id= context.id + "123456";
		else
			id=context.id;

		d1 = id.substring(0,3);
		d2 = id.substring(3,6);
		
		// subdirectories (relative to FServer root) 
		// String relpath = domain + (bucket!=null?File.separator + bucket:"") + File.separator + d1 + File.separator + d2; 
		String relpath = context.repo_type + File.separator + domain + File.separator + d1 + File.separator + d2;
			
		// subdirectories + filename (relative to FServer root)
		return relpath + File.separator + filename;
		
	}
}
