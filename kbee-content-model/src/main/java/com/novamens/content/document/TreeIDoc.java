package com.novamens.content.document;


/***
 * 
 * 
 */
public interface TreeIDoc extends Document {

	public static final String CLASS_CODE = "tidoc";

	public TreeFile getTreeFile();
	public 	void setTreeFile(TreeFile doc);

	public long getTotalSize();
	
	//public int getTotalFiles();
	//public int getTotalDirs();
	
	public int getTotalNodes();
	
}
