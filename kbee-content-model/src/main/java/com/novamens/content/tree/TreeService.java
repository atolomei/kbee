
package com.novamens.content.tree;

import java.util.List;

import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.security.acl.Permission;
import com.novamens.service.SystemService;

public interface TreeService extends SystemService {
	public List<TreeNode> getNodes(DataSetMember member);
	public Tree getPermissionTree(DataSet dataSet, Permission permission);
	public Tree getTree(DataSet dataSet);
  
}