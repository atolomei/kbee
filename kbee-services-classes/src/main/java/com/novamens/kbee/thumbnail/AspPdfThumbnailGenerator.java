package com.novamens.kbee.thumbnail;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.FilenameUtils;

import com.aspose.pdf.Document;
import com.aspose.pdf.devices.JpegDevice;
import com.aspose.pdf.devices.Resolution;
import com.novamens.thumbnail.ThumbnailGenerator;
import com.novamens.thumbnail.ThumbnailSize;

public class AspPdfThumbnailGenerator implements ThumbnailGenerator {
	
	public void generate(File file, OutputStream stream, ThumbnailSize size) throws IOException {
		Resolution resolution = getResolution(size);
        JpegDevice jpegDevice = new JpegDevice(resolution.getX(), resolution.getY(), resolution);
        Document pdf = new Document(new FileInputStream(file));
        jpegDevice.process(pdf.getPages().get_Item(1), stream);
        stream.close();
        pdf.close();
	}
	
	public void generate(String url, OutputStream stream, ThumbnailSize size) throws IOException {
	}
	
	public boolean accept(File file) {
		return "pdf".equals(FilenameUtils.getExtension(file.getName()).toLowerCase());
	}
	
	public Resolution getResolution(ThumbnailSize size) {
		float w = size.getWidth();
		float h = size.getWidth() * 4/3;
		Resolution resolution = new Resolution((int)w, (int)h);
		return resolution;
	}
}
