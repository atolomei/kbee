package com.novamens.kbee.content.multidimensional;

import org.springframework.util.Assert;

import com.novamens.content.userlist.UserListItem;
import com.novamens.indexer.java.Extractor;
import com.novamens.indexer.service.IndexerException;

public class UserItemReaderExtractor implements Extractor {

	public Object extract(Object object) throws IndexerException  {
		Assert.isInstanceOf(UserListItem.class, object);
		
		if (((UserListItem)object).getContent()!=null) {
			return (new ReaderExtractor()).extract(((UserListItem)object).getContent());
		}
		
		return null;
	}
}
