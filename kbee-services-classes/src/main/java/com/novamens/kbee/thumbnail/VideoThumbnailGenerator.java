package com.novamens.kbee.thumbnail;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

public class VideoThumbnailGenerator extends AbstractThumbnailGenerator {

	@Override
	public boolean generateThumbnailFile(File src, OutputStream out, int width, int height) throws IOException {
		
		//VideoThGen.generateThumbnail(src, src.getAbsolutePath());
		return false;
	}

	@Override
	public boolean generateThumbnailToOutputStream(File src, OutputStream out, int width, int height) throws IOException {
	
		//VideoThGen.generateThumbnail(src, src.getAbsolutePath()+"-th");
		return false;
	}

																		
	public boolean generateThumbnailToOutputStream(File src, String thumbnailname) throws IOException {
		
		//VideoThGen.generateThumbnail(src, thumbnailname);
		return false;
	}
	
	
}
