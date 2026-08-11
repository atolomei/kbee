package com.novamens.kbee.content.multidimensional;

import java.lang.reflect.InvocationTargetException;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;

import org.springframework.util.Assert;

import com.novamens.indexer.java.Extractor;
import com.novamens.indexer.service.IndexerException;
import com.novamens.util.JXPath;

public class SimpleDateExtractor implements Extractor {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SimpleDateExtractor.class.getName());
	
	private JXPath datePath;
	
	public Object extract(Object object) throws IndexerException  {
		Date date = getDate(object);
		return date;
	}
	
	public void setPath(String path) {
		datePath = new JXPath(path);
	}
	
	public Date getDate(Object object) throws IndexerException {
		try {
 			List<Object> values = datePath.evaluateAll(object);
			if (values==null) return null;
			Assert.isTrue(values.size()==1, "date not found");
			
			if (values.get(0) instanceof OffsetDateTime) {
				return Date.from(((OffsetDateTime)values.get(0)).toInstant()); 
			}
			
			Assert.isInstanceOf(Date.class, values.get(0));
			return (Date)values.get(0);
		}
		catch (IllegalAccessException e) {
			logger.error(e);
			throw new IndexerException(e);
		}
		catch (InvocationTargetException e) {
			logger.error(e);
			throw new IndexerException(e);
		}

	
	}
}
