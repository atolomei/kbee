package test.com.novamens.kbee.fileserver;

import java.io.BufferedInputStream;
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

import com.novamens.io.FileInputStream;
import com.novamens.kbfs.v1.FSInputStream;
import com.novamens.kbfs.v1.FSOutputStream;
import com.novamens.kbfs.v1.FileServerV1;
import com.novamens.service.ServiceLocator;
import com.novamens.spring.service.SpringServiceLocator;
import com.novamens.util.KbeeFileUtils;

public class FileServerTest {

	static private final int BUFFER_SIZE = 8192;
 	
 	public class Tuple {
		public String id;
		public long size;
		public String filename;
		
		public long ts_add;
		public long ts_read;
		
 		public Tuple(String id, String name, long size) {
			this.id=id;
			this.size=size;
			this.filename=name;
		}
 	}
	
	private Logger logger = LogManager.getLogger(this.getClass().getName());
	
	private List<Tuple> ids = new ArrayList<Tuple>();
	
	private boolean isImage(File file) {
		return file.getName().toLowerCase().matches("^.*\\.(png|jpg|webp|gif|bmp)$"); 
	}
	
	@BeforeAll
	public void setUp() throws Exception {
		ServiceLocator.setInstance(new SpringServiceLocator("kbee"));
	}
	
	@AfterAll
	public void tearDown() throws Exception {
	}
	
	/**
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
			
			String domainname = "test";
			String srcdir = "E:\\test-src";
			String destdir = "d:\\test-dest";
			
			int max = 1000;
			
			to=System.currentTimeMillis();
			FileServerV1 fileserver = ServiceLocator.getService(FileServerV1.class);

			logger.info("Testing File Server");
			logger.info(fileserver.toString());
			
			logger.info("Adding to File Server");
			addFiles(fileserver, srcdir, domainname, max);
			
			logger.info("Reading from File Server and copying");
			readFiles(fileserver, destdir);
			
			t1=System.currentTimeMillis();
			// System.out.println("Done. Duration: " + (t1-to)/1000.0 + " secs.");
	}
 
	/**
	 * 
	 * @param fserver
	 * @param destdir
	 */
	private void readFiles(FileServerV1 fserver, String destdir) {
 		
 		BufferedOutputStream out;
  		byte buffer[] = new byte[BUFFER_SIZE]; 
 		int read;
 		
 		
 		File dir = new File(destdir);
 		if (!(dir.exists() && dir.isDirectory())) {
				try {
					KbeeFileUtils.forceMkdir(dir);
				}  catch (IOException e) {
					logger.error(e);
					return;
				 }
		}

		logger.info("Starting to read and copy " + ids.size() + " files");

		long start, end;
		int total = 0;
  		for (Tuple tuple: ids) {

  			try {
 				
				FSInputStream in = fserver.getFSInputStream(tuple.id, 0);
 				
				start = System.currentTimeMillis();

				out	= new BufferedOutputStream(new FileOutputStream(destdir + File.separator + tuple.filename), BUFFER_SIZE);		
				while (in.available()>0) {
 					read=in.read(buffer, 0, BUFFER_SIZE);
					out.write(buffer, 0, read);
				}
				in.close();
				out.close();
				end = System.currentTimeMillis();
				
				tuple.ts_read = end-start;
				
				logger.info("Copied file:  " + tuple.filename + "      add: " + (double)tuple.ts_add/1000.0 + "     read: " +  (double)tuple.ts_read/1000.0);
				total++;
 				
 			} catch (IOException e) {
 				logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
 			}
 		}
  		
  		logger.info("Total files " + total);
  		
  	}

	/**
	 * 
	 * @param fserver
	 * @param srcdir
	 * @param domainname
	 * @param max
	 */
	private void addFiles(FileServerV1 fserver, String srcdir, String domainname, int max) {
 		
 	  	try {
		  		
 	  			BufferedInputStream in;
 	  			FSOutputStream fout;
	  		
 	  			byte buffer[] = new byte[BUFFER_SIZE]; 
 	  			int read;

 	  			File dir = new File(srcdir);
 	  			File[] listOfFiles = dir.listFiles();
 	  			File file;
 	  			long start, end;

 	  			logger.info("Starting to add to FileServer... ");
 	  		
 	  			for (int n = 0; n<listOfFiles.length && n<max; n++) {
 	  				file = listOfFiles[n];
 	  					
 	  				if (!file.isDirectory() && isImage(file)) {
 	  					
 	  						String id =String.valueOf(Math.abs(file.getAbsolutePath().hashCode()));

 	  						start = System.currentTimeMillis();
 	  						fout = fserver.getFSOutputStream(file.getName(), id, domainname);
 	  						in = new BufferedInputStream(new FileInputStream(file), BUFFER_SIZE);		
	 					
 	  						read=in.read(buffer, 0, BUFFER_SIZE);
	 					
 	  						while (read>0) {
 	  							fout.write(buffer, 0, read);
 	  							read=in.read(buffer, 0, BUFFER_SIZE);
 	  						}
 	  						end = System.currentTimeMillis();


 	  						in.close();
 	  						fout.close();
 	  						
 	  						logger.info("added: " + file.getName() + "  (" + (double) (end-start)/1000.0f + " secs)");

 	  						Tuple tuple=new Tuple(fout.getRelativeUrl(), file.getName(), FileUtils.sizeOf(file));
 	  						tuple.ts_add = end-start;
 	  						
 	  						ids.add(tuple);
 	  					}
 	  				}	
			
 	  			in = null;
				fout = null;
				
	 	  	}catch (IOException e) {
	 	  		logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
	 	  	}
 	}
	
}

