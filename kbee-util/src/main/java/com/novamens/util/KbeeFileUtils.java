package com.novamens.util;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import org.apache.commons.io.FileUtils;

public class KbeeFileUtils  {
																						
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeFileUtils.class.getName());
	static final int BUFFER_SIZE = 16384;
	
	public static void forceMkdir(File directory) throws IOException {
		logger.debug("FileUtils.forceMkdir( " + directory.getAbsolutePath());
		 FileUtils.forceMkdir(directory);
	}
	public static long sizeOf(File file) {
		return FileUtils.sizeOf(file);
	}
	
	public static void moveDirectory(File srcDir, File destDir)  throws IOException {
		logger.debug("FileUtils.moveDirectory( " + srcDir.getAbsolutePath() +", " + destDir.getAbsolutePath() +" )");
		FileUtils.moveDirectory(srcDir, destDir);
	}
	
	public static boolean deleteQuietly(File file) {
		logger.debug("FileUtils.deleteQuietly( "+file.getAbsolutePath()+" )");
		return FileUtils.deleteQuietly(file);
	}
	
	public static void forceDelete(final File file) throws IOException {
		logger.debug("FileUtils.forceDelete( "+file.getAbsolutePath()+" )");
		FileUtils.forceDelete(file);
	}
	
	
	
	
	
	
 	
	public static String calculateSHA256String(final File file) throws IOException, NoSuchAlgorithmException {
		
			logger.debug("FileUtils.calculateSHA256String( "+file.getAbsolutePath()+" )");
			
			long start=System.currentTimeMillis();
			
			byte[] buffer= new byte[BUFFER_SIZE];
			
		    int count = 0;
		    MessageDigest digest;
		    
			try {

				digest = MessageDigest.getInstance("SHA-256");
				
			} catch (NoSuchAlgorithmException e) {
				logger.error(e);
				throw e; 
			}
		    
			BufferedInputStream bis = null;
			
			try {

				bis = new BufferedInputStream(new FileInputStream(file));
			    
				while ((count = bis.read(buffer)) > 0)
			        digest.update(buffer, 0, count);

			    String sha256 = Base64.getEncoder().encodeToString(digest.digest());
			    
			    logger.debug( file.getAbsolutePath()  +" -> " +  sha256 + " | size->" + sha256.length() + " | " + String.valueOf(System.currentTimeMillis()-start)+" ms");
			    
			    return sha256;
		    
			} catch (FileNotFoundException e) {
				logger.error(e);
				throw e;
				
			} catch (IOException e) {
				logger.error(e);
				throw e;
			}
			finally {
				if (bis!=null) {
					try {
						bis.close();
						
					} catch (IOException e) {
						logger.error(e);
					}
				}
			}
		
	}
	
}
