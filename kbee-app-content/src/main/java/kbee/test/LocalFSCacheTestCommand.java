package kbee.test;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;


import com.novamens.kbee.content.command.AsyncCommand;
import com.novamens.kbfs.LocalFileServerCache;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KeyValue;
import com.novamens.wicket.util.FileUtil;
import com.novamens.content.command.CommandState;
import com.novamens.content.command.TestingCommand;

import kbee.util.logging.Logger;

public class LocalFSCacheTestCommand extends AsyncCommand implements TestingCommand {
			
	private static kbee.util.logging.Logger logger = Logger.getLogger(LocalFSCacheTestCommand.class.getName());
	
	private static long TEN_SECONDS = 1000 * 10;
	private static long MB = 1024 * 100;
	
	@Override
	public void executeAsync() {
		loadFiles();
	}
	
	
	public void loadFiles() {
		
		try {
			
					logger.debug("loadFiles()");
					
					
					LocalFileServerCache cache = ServiceLocator.getService(LocalFileServerCache.class);
					
					logger.debug("cache usage -> " + String.valueOf(cache.getTotalDisk() ) + " bytes | items -> "  + String.valueOf(cache.getTotalItems()));
					
					
					cache.setCacheDuration(1000 * 20);
					
					String bucketName[] = {"bucketName1", "bucketName2"};
					String directory = "C:" + File.separator + "Users" + File.separator + "atolo" +File.separator + "Downloads";
					
					List<KeyValue<String>> list = new ArrayList<KeyValue<String>>();
					
					int counter = 0;
					int MAX = 10;
					
					File dir = new File(directory);
					
					if ( (!dir.exists()) || (!dir.isDirectory())) { 
						logger.error("Dir not exists or the File is not Dir");
						return;
					}
					
					
					int TOTAL = dir.listFiles().length > MAX ? MAX : dir.listFiles().length;
					
					for (File fi:dir.listFiles()) {
						
								if (counter == MAX)
									break;
						
								if (!fi.isDirectory() && FileUtil.isPdf(fi)) {
									
									counter++;
					
									String objectKey = FilenameUtils.getBaseName(fi.getName()).toLowerCase().replaceAll("[ |\\t|\\s|(|)]", "")+"-"+String.format("%07d", counter);
									
									BufferedInputStream	inputStream = null;
									
									// put file
									//
									try {
										
										inputStream = new BufferedInputStream(new FileInputStream(fi));
										
										String bucket = bucketName[0];
										
										logger.debug("adding -> " + bucket  + " - " + objectKey  + "  ( " + String.valueOf(counter) +" )");
										
										cache.put(bucket, objectKey, inputStream, fi.getName());
										list.add( new KeyValue<String>(bucket, objectKey));
			
										try {
											Thread.sleep(500 * Double.valueOf(Math.random() * 2).intValue());
										} catch (InterruptedException e) {
										
										}
										
										logger.debug("cache usage -> " + String.valueOf(cache.getTotalDisk() ) + " bytes | items -> "  + String.valueOf(cache.getTotalItems()));
										
										super.setProgress( 0.5 * 100.0 * (double) counter / (double) TOTAL);
										
										
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
								
								}
					}
			
					logger.debug("cache usage -> " + String.valueOf(cache.getTotalDisk() ) + " bytes | items -> "  + String.valueOf(cache.getTotalItems()));
					
			
					try {
							Thread.sleep(1000);
						} 
						catch (InterruptedException e)	{}
				
					
					// --------------------
					//
					// Read all
					//
					
					int loop=0;
					
					while (cache.getTotalItems()>0 && loop++<100) {
						try {
							Thread.sleep(5000);
							logger.debug("loop -> " +  String.valueOf(loop) + " | cache usage -> " + String.valueOf(cache.getTotalDisk() ) + " bytes | items -> "  + String.valueOf(cache.getTotalItems()));
						} catch (InterruptedException e) {
						}
					}
					

					/**
					while (cache.getTotalItems()>0 && loop++<100) {
						
						for (KeyValue<String> kv: list) {
							
							File file = cache.get(kv.key.toString(), kv.value.toString());
													
							if (file!=null) {
								logger.debug(kv.key.toString() + " - " + kv.value.toString() + " -> " + file.getName() + "  | size -> " +  String.valueOf(file.length()/1024) + " KB");
							}
											try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
							}
							
							super.setProgress( 0.5 * 100.0 * (double) (TOTAL - cache.getTotalItems()) / (double) TOTAL);
							
						}
					
						logger.debug("loop -> " +  String.valueOf(loop) + " | cache usage -> " + String.valueOf(cache.getTotalDisk() ) + " bytes | items -> "  + String.valueOf(cache.getTotalItems()));
						
						
						try {
							Thread.sleep(5000);
						} catch (InterruptedException e) {
						}

						
					}
					**/
					
					logger.debug("cache usage -> " + String.valueOf(cache.getTotalDisk() ) + " bytes | items -> "  + String.valueOf(cache.getTotalItems()));					
					
					logger.debug("done");
					super.setProgress(100.0);
					setState(CommandState.COMPLETED);
		}
		catch (Exception e) {
		
			logger.error(e);
			setState(CommandState.ERROR);

		} finally {
			setDateTerminated(OffsetDateTime.now());
			
		}
		
	}
	


}
