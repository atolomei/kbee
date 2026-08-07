package com.novamens.kbee.thumbnail;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.FilenameUtils;

import net.coobird.thumbnailator.Thumbnails;
 
@Deprecated
public class ImageThumbnailGenerator extends AbstractThumbnailGenerator {

	@Override
	public boolean generateThumbnailFile(File src, OutputStream out, int width, int height) throws IOException {
		return false;
	}

	@Override
	public boolean generateThumbnailToOutputStream(File src, OutputStream out,	int width, int height) throws IOException {
			
			if (kbee.util.FSUtils.isImage(src)) {
				String ext = FilenameUtils.getExtension(src.getName());
				Thumbnails.of(src)
		        .size(width, height)
		        .outputFormat(ext.toLowerCase().equals("png")?"PNG":"JPEG")
		        .toOutputStream(out);		
			} 
			return true;
	 }
}
