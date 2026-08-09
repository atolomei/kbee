package kbee.web.dataset;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.tree.Tree;
import com.novamens.content.tree.TreePath;
import com.novamens.content.tree.TreeService;
import com.novamens.kbee.wicket.markup.html.console.tree.TreeNode;
import com.novamens.kbee.wicket.markup.html.console.tree.TreeProvider;
import com.novamens.security.acl.Permission;
import com.novamens.service.ServiceLocator;

public class DataSetTreeProvider extends TreeProvider<TreeNode<DataSetMember>> {
	private static final long serialVersionUID = 1L;
	
	private Tree tree;
	
	public DataSetTreeProvider(DataSet dataSet) {
		tree = ServiceLocator.getService(TreeService.class).getTree(dataSet);
	}
	
	public DataSetTreeProvider(DataSet dataSet, Permission permission) {
		tree = ServiceLocator.getService(TreeService.class).getPermissionTree(dataSet, permission);
	}

	public Iterator<TreeNode<DataSetMember>> getRoots() {
		List<TreeNode<DataSetMember>> roots = new ArrayList<>();
		for (com.novamens.content.tree.TreeNode node : getTree().getRoots()) {
			roots.add(new DataSetNode(node));
		}
		return roots.iterator();
	}
	
	public TreeNode<DataSetMember> getNode(Object object, TreePath path) {
		com.novamens.content.tree.TreeNode node = tree.getNode((DataSetMember)object, path);
		return node!=null ? new DataSetNode(node) : null; 
	}
	
	public Iterator<TreeNode<DataSetMember>> getChildren(TreeNode<DataSetMember> node) {
 		return ((DataSetNode)node).getChilds();
	}
	
	public boolean hasChildren(TreeNode<DataSetMember> node) {
		return ((DataSetNode)node).hasChilds();
	}
	
	public Tree getTree() {
		return tree;
	}
	
	@Override
	public IModel<TreeNode<DataSetMember>> model(TreeNode<DataSetMember> object) {
		return new Model<TreeNode<DataSetMember>>(object);
	}
}