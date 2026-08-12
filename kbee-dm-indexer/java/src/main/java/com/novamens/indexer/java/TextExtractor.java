package com.novamens.indexer.java;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

import com.novamens.content.resource.KBFile;
import com.novamens.content.resource.KBFileProxy;
import com.novamens.indexer.service.IndexerException;

import com.novamens.util.JXPath;

import kbee.util.logging.Logger;

public class TextExtractor implements Extractor {
	private JXPath jpath;

	private static Logger logger = Logger.getLogger(TextExtractor.class.getName());

	
	public TextExtractor(String path) {
		setPath(path);
	}
	
	public void setPath(String path) {
		this.jpath = new JXPath(path);
	}
	
	public Object extract(Object object) throws IndexerException {
		try {
			StringBuffer text = new StringBuffer();
			Object value = jpath.evaluateAll(object);
			              
			if (value instanceof List) {
				List<?> values = (List<?>)value;
				for(Object valueobject : values) {
					value = valueobject;
					if(value instanceof String){
						File file = new File((String)value);
						text.append(extractContentFromFile(file));
					}
					else if(value instanceof File){
						text.append(extractContentFromFile((File)value));
					}
					else if(value instanceof KBFileProxy) {
						KBFile kbFile = (KBFile)value;
						if (kbFile.isIndexable() && kbFile.isBinaryFile()) {
							text.append(extractContentFromStream(kbFile));
						}	
					}
					else if(value instanceof KBFile) {
						KBFile kbFile = (KBFile)value;
						if (isIndexable(kbFile)) {
							text.append(extractContentFromFile(kbFile.getFile()));
						}
					}
				}
			}
 			return text.toString();
		}
		catch (InvocationTargetException e) {
			logger.error(e, " Throws IndexerException");
			throw new IndexerException(e);
		} 
		catch (IllegalAccessException e) {
			logger.error(e, " Throws IndexerException");
			throw new IndexerException(e);
		} 
		catch (IOException e) {
			logger.error(e, " Throws IndexerException");
			throw new IndexerException(e);
		} 
		catch (SAXException e) {
			logger.error(e, " Throws IndexerException");
			throw new IndexerException(e);
		}
		catch (NoSuchMethodError e) {
			logger.error(e);
			return null;
		}
	}
	
	
	private boolean isIndexable(KBFile file) throws IOException {
		return file.isIndexable() && file.isBinaryFile();
	}
	
	private String extractContentFromFile(File file) throws IOException, SAXException {
		InputStream is=null;
		try {
			logger.debug("Extract File:" + (file!=null?file.getName():"null"));
			is = new FileInputStream(file);
			Parser parser = new AutoDetectParser();
			BodyContentHandler handler = new BodyContentHandler(-1);
			Metadata metadata = new Metadata();
			parser.parse(is, handler, metadata, new ParseContext());
			return handler.toString();
		}
		
		catch (TikaException e) {
			logger.error(e, "TikaException |" + e.getMessage());
			logger.error("File : " + file!=null?file.getName():"null");
			return null;
		}
		catch (RuntimeException e) {
			logger.error(e);
			logger.error("File : " + file!=null?file.getName():"null");
			
			throw e;
		}
		finally {
			if(is!=null)
				is.close();
		}
	}
	
	private String extractContentFromStream(KBFile file) throws IOException, SAXException {
		InputStream is=null;
		try {

			logger.debug("Extract File:" + (file!=null?file.getName():"null"));
			
			is = file.getInputStream();
			Parser parser = new AutoDetectParser();
			BodyContentHandler handler = new BodyContentHandler(-1);
			Metadata metadata = new Metadata();
			parser.parse(is, handler, metadata, new ParseContext());
			return handler.toString();
		}
		
		catch (TikaException e) {
			logger.error(e, "TikaException |" + e.getMessage());
			logger.error("File : " + file!=null?file.getName():"null");
			return null;
		}
		catch (RuntimeException e) {
			
			logger.error(e, "TikaException |" + e.getMessage());
			logger.error("File : " + file!=null?file.getName():"null");
			
			throw e;
		}
		finally {
			if(is!=null)
				is.close();
		}
	}

}
