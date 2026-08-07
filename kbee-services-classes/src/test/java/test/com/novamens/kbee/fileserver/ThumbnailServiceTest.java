package test.com.novamens.kbee.fileserver;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;




import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.util.Assert;

import com.novamens.kbee.thumbnail.KbeeThumbnailService;
import com.novamens.kbfs.v1.FSInputStream;
import com.novamens.service.ServiceLocator;
import com.novamens.spring.service.SpringServiceLocator;
import com.novamens.thumbnail.ThumbnailService;


public class ThumbnailServiceTest {

	static private final int BUFFER_SIZE = 8192;
	
	
	private final String domainname = "test";
	
 	public class Tuple {
		public String id;
		public long size;
		public int width;
		public int height;
		public String filename;
		public String thumbnailfilename;
		
		public long ts_add;
		public long ts_read;
		
 		public Tuple(String id, String name, long size, int width, int height) {
			this.id=id;
			this.size=size;
			this.filename=name;
			this.width=width;
			this.height=height;
		}
 	}
	
	private Logger logger = LogManager.getLogger(this.getClass().getName());
	
	private List<Tuple> ids = new ArrayList<Tuple>();
	
	
	@SuppressWarnings("unused")
	private boolean isImage(File file) {
		return file.getName().toLowerCase().matches("^.*\\.(png|jpg|webp|gif|bmp)$"); 
	}
						
	@SuppressWarnings("unused")
	private boolean isPdf(File file) {
		return file.getName().toLowerCase().matches("^.*\\.pdf"); 
	}
	
	@BeforeAll
	public void setUp() throws Exception {
		ServiceLocator.setInstance(new SpringServiceLocator("kbee"));
	}
	
	@AfterAll
	public void tearDown() throws Exception {
	}
	
	/**
	 *
	 * adds 1.000 files to the server
	 * reads 1.000 files and calculate checksum
	 * 
	 * removes 100 files
	 * get files removed
	 * 
	 */
	@Test
	public void test() {

			long to,t1;
			
			// String srcdir = "E:\\test-src";
			// String srcdir = "C:\\Users\\atolomei\\Dropbox\\Camera Uploads";
			// 
			
			String srcdir = "C:\\Users\\atolomei\\Dropbox\\Camera Uploads";
			String destdir = "d:\\test-dest";
			
			int max = 10;
			
			to=System.currentTimeMillis();
			ThumbnailService thserver = ServiceLocator.getService(KbeeThumbnailService.class);

			logger.info("Testing Thumbnail Server");
			logger.info(thserver.toString());
			
			logger.info("Testing getting on the fly");
			addFiles(thserver, srcdir, domainname, max, ONTHEFLY);
			
			
			// logger.info("Adding to Thumbnail Server");
			// addFiles(thserver, srcdir, domainname, max, STANDARD);
			
			logger.info("Reading from Thumbnail Server and copying");
			readFiles(thserver, destdir);
			
			t1=System.currentTimeMillis();
			// System.out.println("Done. Duration: " + (t1-to)/1000.0 + " secs.");
	}
	
	static private final int ONTHEFLY = 1;
	static private final int STANDARD = 2;
	
	/**
	 * Adds files to the thumbnail Server
	 *  
	 * @param fserver
	 * @param srcdir
	 * @param domainname
	 * @param max
	 */
	private void addFiles(ThumbnailService fserver, String srcdir, String domainname, int max, int testmode) {

 	  			 	  			
	}	
	/**
	 *
	 *  Reads the thumbnails already stored in the thumbnailSever
	 *  and copies in {@code destdir}
	 *  
	 * @param fserver
	 * @param destdir
	 */
	private void readFiles(ThumbnailService fserver, String destdir) {
		
 		
 	}

	/**
	 * 
	 * @param fserver
	 */
	private void testGenerateOnTheFly(ThumbnailService fserver) {
		
		// File fserver.getThumbnailFile(kbfile.getId().toString(),kbfile.getDomain().getName(),  kbfile.getFile(),  width,   height);
		
		

		
	}
	
}
