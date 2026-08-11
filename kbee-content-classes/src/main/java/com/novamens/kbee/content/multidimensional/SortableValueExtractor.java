package com.novamens.kbee.content.multidimensional;

import java.util.List;

import com.novamens.content.base.Content;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.DataSetMember;
import com.novamens.indexer.java.Extractor;
import com.novamens.indexer.service.IndexerException;

public class SortableValueExtractor implements Extractor {
	private Attribute attribute;
	private int maxChars = 0;
	
	public SortableValueExtractor() {
	}
	
	public SortableValueExtractor(Attribute attribute) {
		setAttribute(attribute);
	}
	
	public Object extract(Object object) throws IndexerException  {
		List<String> values = null;
		String value = null;
		if (object instanceof Content) {
			Content content = (Content)object;
			values = content.getAttributeValues(getAttribute());
		}
		else {
			if (object instanceof DataSetMember) {
				DataSetMember member = (DataSetMember)object;
				values = member.getAttributeValues(getAttribute());
			}
		}
		if (values!=null && !values.isEmpty()) {
			for (String v : values) {
				if (value==null || value.toLowerCase().compareTo(v.toLowerCase())<0) {
					value = v.toLowerCase();
				}
			}
		}
		if (value!=null && getMaxChars()>0 && value.length()>getMaxChars()) {
			value = value.substring(0, getMaxChars());
		}
		return value;
	}

	public void setAttribute(Attribute attribute) {
		this.attribute = attribute;
	}
	
	public Attribute getAttribute() {
		return attribute;
	}

	public int getMaxChars() {
		return maxChars;
	}

	public void setMaxChars(int maxChars) {
		this.maxChars = maxChars;
	}
	
}