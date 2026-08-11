
package com.novamens.content.tree;

import java.util.List;

import com.novamens.content.model.DataSetMember;
import com.novamens.security.acl.Permission;

public interface TreeNode {
	public String getId();
	public String getDisplayName();
	public TreePath getPath();
	public boolean hasChilds();
	public List<TreeNode> getChilds();
	public boolean isWriteable();
	public boolean hasPermission(Permission permission);
	public DataSetMember getObject();
	public boolean includeAcl();
	public boolean isDescendant(TreeNode node);
}
