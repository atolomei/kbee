package com.novamens.content.document;


public interface TreeFileDir extends TreeFile {

	public static final String DISCRIMNATOR_CODE = "DIR";
	
	public String getDirectoryName();
	public void setDirectoryName(String directory_name);
	
}
