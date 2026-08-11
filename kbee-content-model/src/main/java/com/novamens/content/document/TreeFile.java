package com.novamens.content.document;

import java.util.Set;

import com.novamens.content.base.Resource;

 

public interface TreeFile extends Resource, javax.swing.tree.TreeNode  {

	public Set<TreeFile> getChildren();
	public void move(TreeFile newParent);
	
	public String getTitle();

	public void addTreeFileChild(TreeFile tree_file);
	
	// DIR, KBFILE
	public String getType();  
	
	boolean isRoot();
	public int getLevel();
	
	public int getPosition();
	public void setPosition(int pos);
	
	public boolean isAccessPoint();
	public void setAccessPoint(boolean b);
	
	public TreeFile getParent();
	public int getTotalNodes();
	public long getTotalSize(); // just counts the size of kbfiles 

	public boolean isDirectory();

	public String toHTMLString();

}
