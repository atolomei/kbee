package kbee.web.dataset;

import com.novamens.content.model.DataSet;
import com.novamens.content.tree.Tree;
import com.novamens.content.tree.TreeService;
import com.novamens.security.acl.Permission;
import com.novamens.service.ServiceLocator;

public class WriteableTreeProvider extends DataSetTreeProvider {
	private static final long serialVersionUID = 1L;
	
	private Tree permissionTree;
	
	public WriteableTreeProvider(DataSet dataSet, Permission permission) {
		super(dataSet);
		permissionTree = ServiceLocator
				.getService(TreeService.class)
				.getPermissionTree(dataSet, permission);
	}

	public Tree getTree() {
		return permissionTree;
	}
}