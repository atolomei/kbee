package com.novamens.util;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

import javax.xml.transform.TransformerException;


import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class JXPath  implements Serializable { 
	private static final long serialVersionUID = 1L;
	private String path;

	public JXPath(String path) {
		this.path = path;
	}
	
	public Object evaluate(Object object) throws InvocationTargetException, IllegalAccessException {
		Object value = object;
		
		String[] subpath = this.getPaths(this.path);
		for (int l=0; subpath[l]!=null; l++) {
			value = this.evaluate(subpath[l], value);
		}
		return value;
	}


	public List<Object> evaluateAll(Object object) throws InvocationTargetException, IllegalAccessException {
		try {
			List<Object> values = new ArrayList<Object>();
			values.add(object);
		
			String[] subpath = this.getPaths(this.path);

			for (int l=0; subpath[l]!=null; l++) {
				if (isDom(values)) {
					values = this.evaluateDom(getDom(values), getPath(subpath, l));
					break;
				}
				else {
					values = this.evaluateAll(values, subpath[l]);
				}
				if (values == null) return null;
			}
			return values;
		}
		catch (TransformerException e) {
			throw new InvocationTargetException(e);
		}
	}
	
	private String[] getPaths(String propertyPath) {
		int l = 0;

		String[] paths = new String[10];

		StringTokenizer tokenizer = new StringTokenizer(propertyPath, "/");

		while (tokenizer.hasMoreTokens()) {
			paths[l++] = tokenizer.nextToken();
		}
		return paths;
	}
	
	private List<Object> evaluateDom (Document document, String xpath) throws TransformerException {
		List<Object> evaluations = new ArrayList<Object>();
		NodeList nodes = XPathAPI.selectNodeList(document.getDocumentElement(), xpath);
		if (nodes!=null) {
			for (int c=0; c<nodes.getLength(); c++) {
				String text = getText(nodes.item(c));
				if (text!=null){
					evaluations.add(text);
				}
			}
		}
		return evaluations;
	}
	
	@SuppressWarnings("unchecked")
	private List<Object> evaluateAll (List<Object> objects, String path) throws InvocationTargetException, IllegalAccessException {
		List<Object> newobjects = new ArrayList<Object>();
		newobjects.addAll(objects);
		for (Object object : objects) {
			if (object==null) {
				return null;
			}
			Object evaluation = null;
			evaluation = evaluate(path, object);
			if (evaluation==null && objects.size()==1) return null;
			newobjects.remove(object);
			if (evaluation instanceof Collection<?>) {
				for (Object evaluationObject : (Collection<Object>)evaluation) {
					if (evaluationObject!=null)
						newobjects.add(evaluationObject);
				}
			}
			else {
				newobjects.add(evaluation);
			}
		}
		return newobjects;
	}

	private Object evaluate (String path, Object object) throws InvocationTargetException, IllegalAccessException {
		Object value = null;
		boolean evaluated = false;
		//if (!Hibernate.isInitialized(object)) Hibernate.initialize(object);
		Method methods[] = object.getClass().getMethods();
		for (int i = 0; i<methods.length; i++) {
			Method method = methods[i];
			if (method.getParameterTypes().length == 0 && !method.getReturnType().getName().equals("void")) {
				String upath = path.substring(0,1).toUpperCase() + path.substring(1);
				if (method.getName().equals(path)) {
					evaluated = true;
					value = method.invoke(object, new Object[0]);
					break;
				} 
				else if (method.getName().equals("get" + upath)) {
					evaluated = true;
					value = method.invoke(object, new Object[0]);
					break;
				} else if (method.getName().equals("is" + upath)) {
					evaluated = true;
					value = method.invoke(object, new Object[0]);
					break;
				}
			}
		}
		if (!evaluated) {
			return null;
			//throw new IllegalAccessException();
		}	
		return value;
	}
	
	private String getText(Node node) {
    	String text = null;
    	if (node.getNodeType()==Node.TEXT_NODE) {
    		text = node.getNodeValue();
    	}
    	else {
    		NodeList childs = node.getChildNodes();
    		for (int c=0; c<childs.getLength(); c++) {
    			String childtext = getText(childs.item(c));
    			text = childtext!=null ? (text!=null ? text + " " + childtext : childtext) : text; 
    		}
    	}
    	return text;
    }
    
    private boolean isDom(List<Object> values) {
    	return values.size()==1 && values.get(0) instanceof Document;
    }
    
    private Document getDom(List<Object> values) {
    	return (Document)values.get(0);
    }
    
    private String getPath(String[] paths, int from) {
		String path = "/";
		for (int i=from; i<paths.length && paths[i]!=null; i++)
			path += "/" + paths[i];
		return path;
    }
}
