package com.novamens.kbee.thumbnail;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

 
public abstract class AbstractThumbnailGenerator {
	public static final int IMAGE_THUMBNAIL_MAX_DIMENSION = 159;
	public abstract boolean generateThumbnailFile(File src, OutputStream out, int width, int height) throws IOException;
 	public abstract boolean generateThumbnailToOutputStream(File src, OutputStream out, int width, int height) throws IOException;
}
