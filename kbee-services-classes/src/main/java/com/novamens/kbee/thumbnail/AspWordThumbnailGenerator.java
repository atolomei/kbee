package com.novamens.kbee.thumbnail;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.FilenameUtils;

import com.aspose.words.Document;
import com.aspose.words.ImageSaveOptions;
import com.aspose.words.PageSet;
//import com.aspose.words.PageSet;
import com.aspose.words.SaveFormat;
import com.novamens.thumbnail.ThumbnailGenerator;
import com.novamens.thumbnail.ThumbnailSize;



public class AspWordThumbnailGenerator implements ThumbnailGenerator {
	public void generate(File file, OutputStream stream, ThumbnailSize size) throws IOException {
		try {
			
			//FileInputStream inputStream = new FileInputStream(new File(getClass().getResource("classpath:Aspose.words.lic").toURI()));
			//InputStream licstream = AspWordThumbnailGenerator.class.getResourceAsStream("Aspose.words.lic");
            //    License lic = new License();
            //    lic.setLicense(licstream);
	
			Document doc = new Document(new FileInputStream(file));

			ImageSaveOptions options = new ImageSaveOptions(SaveFormat.JPEG);
	
			// Set the "PageSet" to "0" to convert only the first page of a document.
			options.setPageSet(new PageSet(0));
	
			// Change the image's brightness and contrast.
			// Both are on a 0-1 scale and are at 0.5 by default.
			//options.setImageBrightness(0.3f);
			//options.setImageContrast(0.7f);
	
			// Change the horizontal resolution.
			// The default value for these properties is 96.0, for a resolution of 96dpi.
			//options.setHorizontalResolution(72f);
			
			options.setScale(getScale(size));
	
			// Save the document in JPEG format.
			doc.save(stream, options);
		}
		catch(Exception e) {
			throw new IOException(e);
		}
	}
	
	public void generate(String url, OutputStream stream, ThumbnailSize size) throws IOException {
	}
	
	public boolean accept(File file) {
		return "docx".equals(FilenameUtils.getExtension(file.getName()).toLowerCase());
	}
	
	protected float getScale(ThumbnailSize size) {
		float scale = 1.0f;
		switch (size) {
			case AVATAR_STATUS:
			case MINI:
				scale = 0.1f;
				break;
			case SMALL:
				scale = 0.25f;
				break;
			case MEDIUM:
				scale = 0.25f;
				break;
			case LARGE:
				scale = 0.5f;
				break;
			case W980:
				scale = 1.0f;
				break;
		};
		return scale;
	}

}
