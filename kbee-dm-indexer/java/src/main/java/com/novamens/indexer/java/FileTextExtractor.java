package com.novamens.indexer.java;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.ocr.TesseractOCRConfig;
import org.apache.tika.parser.pdf.PDFParserConfig;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

import com.novamens.content.resource.KBFile;
import com.novamens.content.resource.KBFileProxy;
import com.novamens.indexer.service.IndexerException;
import com.novamens.kbfs.FileServerException;
import com.novamens.util.JXPath;

import kbee.util.PropertiesFactory;
import kbee.util.logging.Logger;

public class FileTextExtractor implements Extractor {
	private JXPath jpath;

	static Logger logger = new Logger(LogManager.getLogger(TextExtractor.class.getName()));
	
	static String TESSERACT_PATH = PropertiesFactory.getInstance("kbee").getProperties().getProperty("tesseract_path", "").trim();
	static String TESSERACT_DATA_PATH = PropertiesFactory.getInstance("kbee").getProperties().getProperty("tesseract_data_path", "").trim();
	
	public FileTextExtractor() {
	}
	
	public FileTextExtractor(String path) {
		setPath(path);
	}
	
	public void setPath(String path) {
		this.jpath = new JXPath(path);
	}
	
	public boolean isOCREnabled() {
		return false;
	}
	
	public Object extract(Object object) throws IndexerException {
		try {
			StringBuffer text = new StringBuffer();
			
			if (object instanceof KBFileProxy) {
				text.append(extractText((KBFile)object));
 			}
			else
			if (object instanceof KBFile) {
				text.append(extractText((KBFile)object));
			}
			else {
				Object value = jpath.evaluateAll(object);
				if (value instanceof List) {
					List<?> values = (List<?>)value;
					for(Object valueobject : values) {
						value = valueobject;
						if(value instanceof File){
							text.append(extractText((File)value));
						}
					}
				}
			}
			
			return text.toString();
		}
		catch (InvocationTargetException | IllegalAccessException | NoSuchMethodError | SAXException | IOException e) {
			logger.error(e);
			return null;
		}
	}
	
	private String extractText(File file) throws IOException, SAXException {
		InputStream is=null;
		try {
			is = new FileInputStream(file);
			Parser parser = new AutoDetectParser();
			BodyContentHandler handler = new BodyContentHandler(-1);
			Metadata metadata = new Metadata();
			parser.parse(is, handler, metadata, new ParseContext());
			return handler.toString();
		}
		catch (TikaException e) {
			logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
			return null;
		}
		catch (RuntimeException e) {
			logger.error("ERROR INDEXANDO " + file.getName());
			throw e;
		}
		finally{
			if(is!=null)
				is.close();
		}
	}
	
	private String extractText(KBFile file) throws IOException, SAXException {
		InputStream is = null;
		try {
			is = file.getInputStream();
			Parser parser = new AutoDetectParser();
			BodyContentHandler handler = new BodyContentHandler(-1);
			
			ParseContext parseContext = new ParseContext();
			if (isOCREnabled()) {
				TesseractOCRConfig config = new TesseractOCRConfig();
				//config.setLanguage("en");
				//config.setTesseractPath("C:\\Program Files\\Tesseract-OCR");
				//config.setTessdataPath("C:\\Program Files\\Tesseract-OCR\\tessdata");
				//config.setTesseractPath(TESSERACT_PATH);
				//config.setTessdataPath(TESSERACT_DATA_PATH);
				PDFParserConfig pdfConfig = new PDFParserConfig();
				pdfConfig.setExtractInlineImages(true);
				pdfConfig.setExtractUniqueInlineImagesOnly(false);
				pdfConfig.setOcrStrategy(PDFParserConfig.OCR_STRATEGY.OCR_AND_TEXT_EXTRACTION);

				parseContext = new ParseContext();
				parseContext.set(TesseractOCRConfig.class, config);
				parseContext.set(PDFParserConfig.class, pdfConfig);
				parseContext.set(Parser.class, parser);
			}
		
			Metadata metadata = new Metadata();
			parser.parse(is, handler, metadata, parseContext);
			return handler.toString();
		}
		catch (TikaException e) {
			logger.error(e);
			return null;
		}
		catch (IOException e) {
			logger.error(e);
			return null;
		}
		catch (RuntimeException e) {
			logger.error(e);
			throw e;
		}
		finally{
			if(is!=null)
				is.close();
		}
	}

}
