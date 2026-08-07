package com.novamens.kbee.thumbnail;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.LogManager;

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;
 
/**
 */
public class PDFThumbnailGenerator extends AbstractThumbnailGenerator {

	
	 
	final private org.apache.logging.log4j.Logger logger = LogManager.getLogger(this.getClass().getName());
 	
	
	public boolean generateThumbnailToOutputStreamJpedal(File src, OutputStream out, int width, int height) throws IOException {
		return false;
	}
	
	public boolean generateThumbnailToOutputStream(File src, OutputStream out, int width, int height) throws IOException {
 		 File pdfFile = src;
         RandomAccessFile raf = new RandomAccessFile(pdfFile, "r");
         try {
        	 FileChannel channel = raf.getChannel();
        	 ByteBuffer buf = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
        	 PDFFile pdf = new PDFFile(buf);
         	 PDFPage page = pdf.getPage(0);
         	 Rectangle rect = new Rectangle(0, 0, (int) page.getBBox().getWidth(), (int) page.getBBox().getHeight());
         	 BufferedImage bufferedImage = new BufferedImage(rect.width, rect.height, BufferedImage.TYPE_INT_RGB);

         	 Image image = page.getImage(rect.width, rect.height,    // width & height
                       rect,                       // clip rect
                       null,                       // null for the ImageObserver
                       true,                       // fill background with white
                       true                        // block until drawing is done
         	);
        
         	 Graphics2D bufImageGraphics = bufferedImage.createGraphics();
         	 bufImageGraphics.drawImage(image, 0, 0, null);
        
         	 ImageIO.write(scaleImageToThumbnail(bufferedImage, height, width), "PNG", out);
         	 return true;
         }
        catch (java.nio.BufferUnderflowException | com.sun.pdfview.PDFParseException | java.lang.NullPointerException e) {
        	
        	logger.debug(e);
        	return false;
        	
        	// generateThumbnailToOutputStreamV2(src, out, width, height);
        	// try via Ghostscript
        	// stream a default file
        	// logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
         }
        finally {
        	if (raf!=null)
        		raf.close();
         }
  	}
	
//	public boolean generateThumbnailToOutputStream2(File src, OutputStream out, int width, int height) throws IOException {
//		PageDrawer pagedrawer = new	PageDrawer();
//		PDDocument document = PDDocument.load(src);
//		java.util.List pages= document.getDocumentCatalog().getAllPages();
//
//
//		 PDPage page =(PDPage)pages.get(1);
//
//
//		 java.awt.image.BufferedImage image=page.convertToImage(BufferedImage.TYPE_4BYTE_ABGR, 200);
//
//		 ImageIO.write(image, "png", out);
//		 return true;
//	 }
 	
 	/**
	 *   por algun problema con JNA cuelga la VM
	 *   posiblemente tiene que ver con que no encuentra el Ghostscript para ejecutar 
	 *   
	 *   
	 */
//	private void generateThumbnailToOutputStreamV2(File src, OutputStream out, int width, int height) throws IOException {
//	 	
//	 // load PDF document
//    PDFDocument document = new PDFDocument();
//    document.load(src);
//
//    // create renderer
//    SimpleRenderer renderer = new SimpleRenderer();
//
//    // set resolution (in DPI)
//    renderer.setResolution(300);
//
//    // render
//    List<Image> images;
//	try {
// 
//		images = renderer.render(document,0,1);
// 
//        // for (int i = 0; i < images.size(); i++) {
//        if(images.size()>0)     
//        	ImageIO.write((RenderedImage) images.get(0), "png", out);
//           // }
//    } catch (Exception e) {
//    	  	 logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
//        
//	} 
//	// catch (RendererException | DocumentException e1) {
//	//  	 logger.error(e1);
//	//}
//  }
// 	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
 	@Override
	public boolean generateThumbnailFile(File src, OutputStream out, int width, int height) throws IOException {
 		return false;
  	}
  	
	/**
	 * 
	 * @param image
	 * @param dimensionHeight
	 * @param dimensionWidth
	 * @return
	 */
 	protected BufferedImage scaleImageToThumbnail(BufferedImage image, int dimensionHeight, int dimensionWidth) {
		
 		BufferedImage biThumbnail = null;

		// Determine scale factor.
		// double scale = (double) dimension / (double) image.getHeight(null);
		double scale = (double) dimensionHeight / (double) image.getHeight(null);
		if (image.getWidth(null) > image.getHeight(null)) {
			scale = (double) dimensionWidth / (double) image.getWidth(null);
		}

		// Caculate size of new image.
		int scaledW = (int) (scale * image.getWidth(null));
		int scaledH = (int) (scale * image.getHeight(null));

		// Create an image buffer in which to paint on.
		biThumbnail = new BufferedImage(scaledW, scaledH, BufferedImage.TYPE_INT_RGB);

		// Set the scale.
		AffineTransform affineTransform = new AffineTransform();

		affineTransform.scale(scale, scale);

		// Paint image.
		Graphics2D graphics = biThumbnail.createGraphics();
		graphics.drawImage(image, affineTransform, null);
		graphics.dispose();

		return biThumbnail;
	}
  	
 }
