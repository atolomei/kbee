package com.novamens.kbee.thumbnail;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import org.apache.commons.io.FilenameUtils;

import com.novamens.io.FileInputStream;
import com.novamens.thumbnail.ThumbnailGenerator;
import com.novamens.thumbnail.ThumbnailSize;

import kbee.util.FSUtils;
import kbee.util.logging.Logger;
import net.coobird.thumbnailator.Thumbnails;
 
public class ImageThumbnailGenerator2 implements ThumbnailGenerator {
	
	static private final int BUFFER_SIZE = 8192;
	private byte buffer[] = new byte[ BUFFER_SIZE ];

	private static Logger logger = Logger.getLogger(ImageThumbnailGenerator2.class.getName());
	
	public class Size { 
		int width, height;
		public Size(int width, int height) {
			this.width = width;
			this.height = height;
		}
		public int getWidth() {
			return width;
		}
		public void setWidth(int width) {
			this.width = width;
		}
		public int getHeight() {
			return height;
		}
		public void setHeight(int height) {
			this.height = height;
		}
	}
	
	public void generate(File imagefile, OutputStream writer, ThumbnailSize size) throws IOException {
		
		BufferedImage image = ImageIO.read(imagefile);
		
		boolean webp = "webp".equals(FilenameUtils.getExtension(imagefile.getName()).toLowerCase());
		
		Size calculatedsize = webp
			? new Size(size.getWidth(), size.getHeight())
			: calculateSize(image, size);
		
		if (webp || calculatedsize.equals(new Size(image.getWidth(), image.getHeight()))) {
			copy(imagefile, writer);	
		}
		else {
			generate(imagefile, writer, calculatedsize);
		}
		
        writer.close();
	}
	
	public void generate(String url, OutputStream stream, ThumbnailSize size) throws IOException {
	}
	
    private void copy(File file, OutputStream out) throws IOException {
    	BufferedInputStream in = null;
    	try {
    		in = new BufferedInputStream(new FileInputStream(file), BUFFER_SIZE);
			int read;
			read=in.read(buffer, 0, BUFFER_SIZE);
			while (read>0) {
				out.write(buffer, 0, read);
				read=in.read(buffer, 0, BUFFER_SIZE);
			}
		} 
    	finally {
    		if (in!=null) in.close();
		}
    }
	
	public boolean accept(File file) {
		return  FSUtils.isImage(file.getName());
	}
	
	private void generate(File src, OutputStream out, Size size) throws IOException {
		String ext = FilenameUtils.getExtension(src.getName());
		Thumbnails.of(src)
			.size(size.getWidth(), size.getHeight())
			.outputFormat(ext.toLowerCase().equals("png")?"PNG":"JPEG")
			.toOutputStream(out);		
	}
	
	private Size calculateSize(BufferedImage image, ThumbnailSize requestedSize) {
		try {
			
			int requestedWidth = requestedSize.getWidth();
			int requestedHeight = requestedSize.getHeight();
			int imageWidth = image.getWidth();
			int imageHeight = image.getHeight();
			
			if (requestedWidth!=0 && requestedHeight!=0) {
				
				// Si la imagen es más chica que el thumbnail requerido -> devuelve la imagen
				if (imageWidth<=requestedWidth && imageHeight<=requestedHeight) {
					return new Size(imageWidth, imageHeight);
				}				
				
				// Si la imagen es más grande que el thumbnail requerido, recorta
				int delta_w = imageWidth - requestedWidth;
				int delta_h = imageHeight - requestedHeight;
				
				int height, width;
				if (delta_h<delta_w) {
					height = requestedHeight;
					width =  imageHeight>0 ? imageWidth*requestedHeight/imageHeight : requestedWidth;
				} 
				else {
					width = requestedWidth;
					height = imageWidth>0 ? imageHeight*requestedWidth/imageWidth : requestedHeight;
				}
				
				return new Size(width, height);
			}
			// Si h es 0
			else if (requestedHeight==0 && requestedWidth!=0) {
				
				int delta_w = imageWidth - requestedWidth;
				
				// Si el ancho del th es mas grande que el ancho de la imagen devuelve la imagen
				if (delta_w<0) {
					return new Size(imageWidth, imageHeight);
				}
				else {
					// Si el ancho del th es mas chico que el ancho de la imagen resizea proporcional
					int width = requestedWidth;
					int height =  imageWidth>0? imageHeight*requestedWidth/imageWidth : imageHeight;
					if (height<=0) height = imageHeight;
					return new Size(width, height);
				}
			}
			// Si w es 0
			//	
			else {
							
				int delta_h = imageHeight - requestedHeight;
				
				// Si el alto del th es mas grande que el alto de la imagen devuelve la imagen
				//
				if (delta_h<0) {
					return new Size(imageWidth, imageHeight);
				}
				else {
					// Si el alto del th es mas chico que el alto de la imagen resizea proporcional
					int height = requestedHeight;
					int width =  imageHeight>0 ? imageWidth*requestedHeight/imageHeight : imageWidth;
					if (width<=0) width = imageWidth;
					return new Size(width, height);
				}
			}
		
		} 
		catch (Exception e) {
			logger.error(e);
			return null;
		}
	}
	
}
