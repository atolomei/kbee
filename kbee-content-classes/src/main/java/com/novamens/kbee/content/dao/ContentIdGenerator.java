package com.novamens.kbee.content.dao;

import javax.persistence.Table;

@Table(name = "contentid_sequence")
public class ContentIdGenerator {

	public ContentIdGenerator() {};
	
	public synchronized Long generateContentId() {
		return Long.valueOf(1);
	}
}
