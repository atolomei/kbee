package fileserver;



import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

import org.junit.Test;

import com.novamens.kbfs.LocalFileServerCache;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KeyValue;

import kbee.util.logging.Logger;

public class LocalFSCacheTest  {

	private static kbee.util.logging.Logger logger = Logger.getLogger(LocalFSCacheTest.class.getName());
	
	private static long TEN_SECONDS = 10000;
	private static long MB = 1024 * 100;

	//@Override
	@Test
	public void run() {
		loadFiles();
	}

	
	
	/**
	 * load and reads 100 files
	 * 
	 */
	public void loadFiles() {
		
		
		logger.debug("loadFiles()");
		
		LocalFileServerCache cache = ServiceLocator.getService(LocalFileServerCache.class);
		
		// 2 seconds cache duration
		//
		cache.setCacheDuration(TEN_SECONDS); 
		
		String bucketName[] = {"bucketName1", "bucketName2"};
		String directory = "c:\\Users\\atolo\\Download";
		
		List<KeyValue<String>> list = new ArrayList<KeyValue<String>>();
		
		int counter = 0;
		int MAX = 100;
		
		File dir = new File(directory);
		
		if (!dir.exists() || !dir.isDirectory()) { 
			logger.error("Dir not exists or the File is not Dir");
			return;
		}
		
		if (dir.list()==null) {
			logger.error("Dir is Empty");
			return;
		}

		
	}
	
	

	
	
	

	
	
	
}
