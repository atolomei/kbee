package com.novamens.wicket.request.http;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.apache.wicket.request.Response;

import kbee.util.logging.Logger;

public class CachedResponse extends Response {
				
	private static Logger logger = Logger.getLogger(CachedResponse.class.getName());
	
	
	private Response response;
	private Writer writer;
	
	public CachedResponse(Response response) {
		this.response = response;
	}
	
	public void write(CharSequence sequence) {
		response.write(sequence);
		try {
			Writer writer = getCacheWriter();
			if (writer!=null) writer.write(sequence.toString());
		}
		catch (IOException e) {
			logger.error(e);
		}
	}
	
	public void write(byte[] array, int offset, int length) {
		response.write(array, offset, length);
		try {
			Writer writer = getCacheWriter();
			if (writer!=null) writer.write(new String(array), offset, length);
		}
		catch (IOException e) {
			logger.error(e);
		}
	}
	
	public void write(byte[] array) {
		response.write(array);
		try {
			Writer writer = getCacheWriter();
			if (writer!=null) writer.write(new String(array));
		}
		catch (IOException e) {
			logger.error(e);
		}
	}
	
	public String encodeURL(CharSequence url) {
		return response.encodeURL(url);
	}
	
	public Object getContainerResponse() {
		return response.getContainerResponse();
	}
	
	@Override
	public void close() {
		response.close();
		try {
			if (getCacheWriter()!=null) {
				getCacheWriter().close();
			}
		}
		catch (IOException e) {
			logger.error(e);
		}
	}
	
	private Writer getCacheWriter() {
		try {
			if (writer == null) {
				File cacheFile = new File("cache.txt");
				writer = new FileWriter(cacheFile);
			}
			return writer;
		}
		catch (IOException e) {
			logger.error(e);
			return null;
		}
	}
}
