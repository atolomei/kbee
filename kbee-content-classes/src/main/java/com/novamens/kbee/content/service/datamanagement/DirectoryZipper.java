package com.novamens.kbee.content.service.datamanagement;



import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.novamens.util.KbeeFileUtils;


/**
 *  <p>Utility class to generate a zip file from a directory</p> 
 */
public class DirectoryZipper {
				
	static Logger logger = LogManager.getLogger(DirectoryZipper.class.getName());
	
	File srcdir;
	File destdir;
	String zipname;
	private List<String> fileList = new ArrayList<String>();
	
	byte[] buffer = new byte[4096];
	
	
	/**
	 * 
	 * @param srcdir 
	 * @param destdir 
	 * @param zipname
	 */
	public DirectoryZipper(File srcdir, File destdir, String zipname) {
		
		this.srcdir=srcdir;
		this.destdir=destdir;
		this.zipname=zipname;
		
	}
	
	public void execute() throws IOException {
		
		if (this.srcdir==null || !this.srcdir.isDirectory())
			throw new IOException ("src is not a directory");

		if (!this.srcdir.exists())
			throw new IOException ("srcdir does not exist");

		if (this.destdir==null)
			throw new IOException ("dest is null");
		
		
		if (!this.destdir.exists()) {
			KbeeFileUtils.forceMkdir(this.destdir);
		}
		else if (!this.destdir.isDirectory()) {
			try {
			KbeeFileUtils.forceDelete(this.destdir);
			KbeeFileUtils.forceMkdir(this.destdir);
			} catch (Exception e) {
				throw new IOException(e);
			}
		}

		if (zipname==null)
			throw new IOException ("zipname is null");
		
		
		generateFileList(this.srcdir);
		zipIt(destdir.getAbsolutePath() + File.separator + zipname);
		
	}
	
		/**
		 * 
	     * Zip it
	     * @param zipFile output ZIP file location
	     * 
	     */
	    public void zipIt(String zipFile) throws IOException {

	     

	     FileInputStream in 	= null;
	     FileOutputStream fos 	= null;
	     ZipOutputStream zos  		= null;
	     
	     try {

	    	 fos = new FileOutputStream(zipFile);
	    	 zos = new ZipOutputStream(fos);

	    	logger.info("Output to Zip : " + zipFile);

	    	for(String file : this.fileList) {

	    		logger.info("File to add : " + file);
	    		
	    		ZipEntry ze= new ZipEntry(file);
	        	zos.putNextEntry(ze);

	        	in = new FileInputStream(this.srcdir.getAbsoluteFile() + File.separator + file);
	        	
	        	int len;
	        	
	        	while ((len = in.read(buffer)) > 0) {
	        		zos.write(buffer, 0, len);
	        	}
	        	
	        	in.close();
	        	in = null;
	    	}

	    
	     } catch(IOException ex)   {
	       throw(ex);
	     }
	     finally {

	    	 if (in!=null)
				try {
					in.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
	    		 
	    	 if (zos!=null)
				try {
					zos.close();
				} catch (IOException e) {
					 logger.error(e);
				}
	     }
	     
	   }
	
	/**
     * Traverse a directory and get all files,
     * and add the file into fileList
     * @param node file or directory
     */
    private void generateFileList(File node) {

		    	// add file only
		    	if(node.isFile()) {
		    		fileList.add(generateZipEntry(node.getAbsolutePath()));
		    	}
		
		    	if(node.isDirectory()){
		    		String[] subNote = node.list();
		    		for(String filename : subNote){
		    			generateFileList(new File(node, filename));
		    		}
		    	}

    }
    
    /**
     * Format the file path for zip
     * @param file file path
     * @return Formatted file path
     */
    private String generateZipEntry(String file) {
    	return file.substring(this.srcdir.getAbsolutePath().length()+1, file.length());
    }
	
}
