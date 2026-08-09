package com.novamens.kbee.wicket.util;


import java.util.List;

import org.apache.wicket.model.IModel;

import com.novamens.util.JXPath;

public class DisplayNameExtractor {
			
	static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(DisplayNameExtractor.class.getName());
	
	public static String get(Object xvalue) {
		
		String displayValue = "";
		Object value=xvalue;
		
		if (value instanceof IModel<?>) {
			value = ((IModel<?>)value).getObject();
		}
		
 		if (value instanceof com.novamens.dom.Object) {
 			((com.novamens.dom.Object)value).getDisplayName();
 		}
 		
		JXPath path = new JXPath("label");
		try {
			List<Object> values = null;
			try {
				values = path.evaluateAll(value);
			}
			catch (IllegalAccessException e) {
			}
			
			if (values!=null && values.size()>0)
				displayValue = values.get(0).toString();
			else {
				path = new JXPath("displayName");
				try {
					values = path.evaluateAll(value);
				}
				catch (IllegalAccessException e) {
					logger.error(e);
				}
				if (values!=null && values.size()>0) {
					displayValue = values.get(0).toString();
				}
				else {
					path = new JXPath("name");
					try {
						values = path.evaluateAll(value);
					}
					catch (IllegalAccessException e) {
					}
					if (values!=null && values.size()>0) {
						displayValue = values.get(0).toString();
					}
					else
						displayValue = value!=null ? value.toString() : "";
				}
			}
		}
		
		catch (Exception e) {
			logger.error(e);
			return e.getClass().getName();
		}
		return displayValue;
	}

}
