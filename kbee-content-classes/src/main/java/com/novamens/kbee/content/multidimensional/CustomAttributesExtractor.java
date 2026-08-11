package com.novamens.kbee.content.multidimensional;

import com.novamens.content.base.Content;
import com.novamens.content.base.CustomAttribute;
import com.novamens.indexer.java.Extractor;
import com.novamens.indexer.service.IndexerException;

public class CustomAttributesExtractor implements Extractor {

	public Object extract(Object object) throws IndexerException  {
		String valuestext = null;
		if (!(object instanceof Content)) return null; 
		Content content = (Content)object;
		
		for (CustomAttribute attribute : content.getUserDefinedAttributes()) {
			if (attribute.getValue()!=null && !"".equals(attribute.getValue().trim())) {
				if (valuestext==null) 
					valuestext = attribute.getValue().trim();
				else
					valuestext += " " + attribute.getValue().trim();
			}
		}
		
		return valuestext;
	}
}
