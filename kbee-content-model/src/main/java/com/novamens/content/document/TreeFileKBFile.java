package com.novamens.content.document;

import com.novamens.content.resource.KBFile;

public interface TreeFileKBFile extends TreeFile {

	public static final String DISCRIMNATOR_CODE = "KBFILE";

	public void setFile(KBFile file);
	public KBFile getFile();

	
}
