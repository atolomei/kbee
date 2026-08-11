package com.novamens.kbee.content.multidimensional;

import java.lang.reflect.InvocationTargetException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.springframework.util.Assert;

import com.novamens.indexer.java.Extractor;
import com.novamens.indexer.service.IndexerException;
import com.novamens.kbee.content.multidimensional.DateFacet.DateRange;
import com.novamens.util.JXPath;

@Deprecated
public class DateExtractor implements Extractor {
	private JXPath datePath;
	
	public Object extract(Object object) throws IndexerException  {
		Date date = getDate(object);
		if (date==null) return null;
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		List<String> members = new ArrayList<String>();
		String member = String.valueOf(calendar.get(Calendar.YEAR)) + "/" + 
				String.valueOf(calendar.get(Calendar.MONTH)+1);
		members.add(member);
		DateRange ranges[] = DateRange.values();
		for (int r=0; r<ranges.length; r++) {
			if (ranges[r].contains(date)) {
				member = ranges[r].value();
				members.add(member);
			}
		}
		return members;
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
			throw new IndexerException(e);
		}
		catch (InvocationTargetException e) {
			throw new IndexerException(e);
		}

	
	}
}
