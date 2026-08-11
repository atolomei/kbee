package com.novamens.kbee.content.multidimensional;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;

import com.novamens.content.base.Content;
import com.novamens.content.model.ObjectId;
import com.novamens.indexer.java.Extractor;
import com.novamens.indexer.service.IndexerException;
import com.novamens.util.JXPath;

public class IdExtractor implements Extractor {
	
	private JXPath path;
	
	public Object extract(Object object) throws IndexerException  {
		Content content = getContent(object);
		if (content==null) return null;
		List<String> members = new ArrayList<String>();
		
		if (content instanceof HibernateProxy) {
			HibernateProxy proxy = (HibernateProxy)content;
			LazyInitializer initializer = proxy.getHibernateLazyInitializer();
			content = (Content)initializer.getImplementation();
		}
		
		String id = (new ObjectId(content)).toString();

		members.add(id);
		
		return members;
	}
	
	public void setPath(String path) {
		this.path = new JXPath(path);
	}
	
	public Content getContent(Object object) throws IndexerException {
		try {
 			List<Object> values = path.evaluateAll(object);
			if (values==null || values.size()!=1) return null;
			if (!(values.get(0) instanceof Content)) {
				return null;   
			}
			return (Content)values.get(0);
		}
		catch (IllegalAccessException e) {
			throw new IndexerException(e);
		}
		catch (InvocationTargetException e) {
			throw new IndexerException(e);
		}

	
	}
}
