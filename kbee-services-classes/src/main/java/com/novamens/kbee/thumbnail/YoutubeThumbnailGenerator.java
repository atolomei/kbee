package com.novamens.kbee.thumbnail;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import com.novamens.thumbnail.ThumbnailGenerator;
import com.novamens.thumbnail.ThumbnailSize;

import kbee.util.logging.Logger;
 
public class YoutubeThumbnailGenerator implements ThumbnailGenerator {
	
	private static Logger logger = Logger.getLogger(YoutubeThumbnailGenerator.class.getName());
	 
	static private final int BUFFER_SIZE = 8192;

	public void generate(File file, OutputStream stream, ThumbnailSize size) throws IOException {
		
	}

	public void generate(String urlStr, OutputStream stream, ThumbnailSize size) throws IOException {
		
		try {
			BufferedInputStream reader=null;
			String[] partes = urlStr.split("v=");
			if(partes!=null&&partes.length==2){
				String urlThumnbail = "http://img.youtube.com/vi/"+partes[1].trim()+"/hqdefault.jpg";
//				String urlThumnbail = "http://img.youtube.com/vi/"+partes[1].trim()+"/default.jpg";
				URL url = new URL(urlThumnbail);
				URLConnection conn = url.openConnection();
				reader = new BufferedInputStream( conn.getInputStream());
				int read = 0;
				byte [] bufferin = new byte[BUFFER_SIZE];
				try {
			        while ((read = reader.read(bufferin)) >= 0) {
			        	stream.write(bufferin, 0, read); 
			        }
				}
				finally{
					stream.close();
					reader.close();
				}
			}
		}
		catch (Exception e) {
			logger.error(e);
		}
		
	}
	
	public boolean accept(File file) {
		return false;
	}
}
