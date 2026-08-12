package com.novamens.indexer.java;

public class FileFieldSchema extends FieldSchema {
	
	public void setPath(String path) {
		setExtractor(new TextExtractor(path));
	}
	
	public boolean isMetainfo() {
		return false;
	}
}