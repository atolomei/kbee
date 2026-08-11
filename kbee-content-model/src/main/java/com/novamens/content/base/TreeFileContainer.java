package com.novamens.content.base;

import com.novamens.content.document.TreeFile;

public interface TreeFileContainer extends ResourceContainer {

	public void setTreeFile(TreeFile tree_file);
	public void removeTreeFile();
	public TreeFile getTreeFile();

}
