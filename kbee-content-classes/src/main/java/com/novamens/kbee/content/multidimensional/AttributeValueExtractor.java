package com.novamens.kbee.content.multidimensional;

import java.util.List;

import com.novamens.content.base.Content;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.userlist.UserListItem;
import com.novamens.indexer.java.Extractor;
import com.novamens.indexer.service.IndexerException;

public class AttributeValueExtractor implements Extractor {
	private Attribute attribute;
	private int maxChars = 0;

	public AttributeValueExtractor() {
	}
	
	public AttributeValueExtractor(Attribute attribute) {
		setAttribute(attribute);
	}
	
	public Object extract(Object object) throws IndexerException  {
		List<String> values = null;
		if (object instanceof Content) {
			Content content = (Content)object;
			values = content.getAttributeValues(getAttribute());
		}
		else {
			if (object instanceof DataSetMember) {
				DataSetMember member = (DataSetMember)object;
				values = member.getAttributeValues(getAttribute());
			}
			else {
				if (object instanceof UserListItem) {
					Content content = ((UserListItem)object).getContent();
					if (content!=null) {
						 values = content.getAttributeValues(getAttribute());
					}
				}
			}
		}
		
		if (values!=null) {
			int i = 0;
			for (String value : values) {
				if (value!=null && getMaxChars()>0 && value.length()>getMaxChars()) {
					value = value.substring(0, getMaxChars());
					values.set(i, value);
				}
				i++;
			}
		}
		
		return values;
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