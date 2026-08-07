package test.com.novamens.kbee.fileserver;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.novamens.kbee.kbfs.KbeeMinioFileServer;
import com.novamens.kbfs.FileServerException;
import com.novamens.util.KeyValue;


import kbee.util.FSUtils;

public class FileServerV2Test {

	static Logger logger = LogManager.getLogger(FileServerV2Test.class.getName());

	static final long MB = 1000000;
	
	KbeeMinioFileServer minio;
	
	String directory = "D:\\temp\\testdata2";
	int size = 3;
	int imported;
	
	boolean test_success = false;
	
	String bucketName = "chacra-test1";
	
 	List<String> errors = new ArrayList<String>();
	

	//int BUFFER_SIZE = 8192;
	//byte[] buf = new byte[BUFFER_SIZE];
	//int bytesRead;

 	
	public static void main(String[] args) {
		
		FileServerV2Test ag = new FileServerV2Test();
		
		ag.addFilesTest();
		
		if (!ag.getErrors().isEmpty()) {
			logger.info("Errors:");
			for (String s:ag.getErrors()) {
					logger.info(s);
			}
		}
		else
			logger.info("All Tests OK");
		
		logger.info("-------------------------------------------------------------------------------------------");
	}
	
	
	public FileServerV2Test() {
		try {
			init();
		} catch (FileServerException e) {
			logger.error(e);
		}
	}
	
	
	public List<String> getErrors() {
		return this.errors;
	}
	
	
	
	public void addFilesTest() {
		
		File dir = new File(directory);
		
		if (!dir.exists() || !dir.isDirectory()) { 
			logger.error("Dir not exists or the File is not Dir");
			return;
		}
		
		
		if (dir.list()==null) {
			logger.error("Dir is Empty");
			return;
		}

		int counter = 0;
		
		List<KeyValue<String>> list = new ArrayList<KeyValue<String>>();
	
		  
		
		/**---------------------------------------------------------------------
		 * Uploads 10 files
		 */
		
		for (File fi:dir.listFiles()) {
			
			if (!fi.isDirectory()) {
				counter++;
				
				String objectKey = FilenameUtils.getName(fi.getName())+"/"+String.format("%07d", counter);
				
				try {
					
					logger.info(fi.getName() + "  " + String.valueOf(fi.length()));
					
					BufferedInputStream	inputStream = null;
					
					try {
						inputStream = new BufferedInputStream(new FileInputStream(fi));
						minio.putObject(bucketName, objectKey, fi.getAbsolutePath());
						
					} catch (FileNotFoundException e) {
							logger.error(e);
					}
					finally {
						if (inputStream!=null) { 
							try {
								inputStream.close();
							} catch (IOException e) {
								logger.error(e);
							}
						}
					}
					
					
					list.add(new KeyValue<String>(objectKey, FilenameUtils.getName(fi.getName())));
					
					
					
				} catch (FileServerException e) {
					errors.add("add " + fi.getName() + " " + e.getMessage());
					logger.error(fi.getName() + "| " + e.getStackTrace());
				}
			}
			
			if (counter>=size)
				break;
		}

		if (errors.size()>0)
			return;
		
		
		/**---------------------------------------------------------------------
		 * Check that all files are there
		 */
		for (KeyValue<String> k: list) {
			try {
				logger.info(minio.presignedGetObject(bucketName, k.getValue()));
			} catch (FileServerException e) {
				errors.add("check " + k.getDisplayName() + " " + e.getMessage());
				logger.error(k + "| " + e.getStackTrace());
			}
		}
		

		if (errors.size()>0)
			return;

		
		/** ---------------------------------------------------------------------
		 * Downloads files to local disk
		 */
		for (KeyValue<String> k: list) {
			try {
				File file = minio.getDownloadedFile(bucketName, k.key.toString(), k.value);
				if (file.exists()) {
					logger.info(file.getName() + "  " + String.valueOf(file.length()));
				} else {
					logger.error(k.key.toString() + "  " + k.value + " error");
					errors.add("donwload " + k.key.toString() + " does not exist");
				}
				
			} catch (FileServerException e) {
				errors.add(k + "| " + e.getStackTrace());
				logger.error(k + "| " + e.getStackTrace());
			}
		}
		
	}


	private String getContentType(String src) {

		if (FSUtils.isPdf(src))
			return "application/pdf";
		
		if (FSUtils.isImage(src))  {
			String str = FilenameUtils.getExtension(src);
			if (str!=null && (str.toLowerCase().equals("jpg") ||  str.toLowerCase().equals("jpeg")))
				return "image/jpeg"; 
			return "image/"+str;
		}
		if (FSUtils.isVideo(src)) {
			return "video/"+FilenameUtils.getExtension(src);
		}
		
		if (FSUtils.isAudio(src))
			return "audio/"+FilenameUtils.getExtension(src);
		
		return "application/octet-stream";
	}
	
	private void init() throws FileServerException {
		minio = new KbeeMinioFileServer();
				
	}
	
	
	
	

}
