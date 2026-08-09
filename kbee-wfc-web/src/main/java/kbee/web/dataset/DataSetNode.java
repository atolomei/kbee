package kbee.web.dataset;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.model.IDetachable;

import com.novamens.content.model.DataSetMember;
import com.novamens.content.tree.TreePath;
import com.novamens.kbee.wicket.markup.html.console.tree.TreeNode;
import com.novamens.security.acl.Permission;

public class DataSetNode implements Serializable, IDetachable, TreeNode<DataSetMember> {
	private static final long serialVersionUID = 1L;
	
	private com.novamens.content.tree.TreeNode node;
	
	public DataSetNode(com.novamens.content.tree.TreeNode node) {
		this.node = node; 
	}
	
	public DataSetMember getObject() {
		return getNode().getObject();
	}
	
	public String getDisplayName() {
		return getNode()!=null ? getNode().getDisplayName() : "-";
	}
	
	public String getPath() {
		return getNode()!=null && getNode().getPath()!=null ? getNode().getPath().asString() : null;
	}
	
	public TreePath getTreePath() {
		return getNode().getPath();
	}
	
	public List<DataSetNode> getNodesPath() {
		List<DataSetNode> childs = new ArrayList<>();
		for (com.novamens.content.tree.TreeNode node  : getNode().getPath().getNodes()) {
			childs.add(new DataSetNode(node));
		};
		return childs;
	}
	
	public Iterator<TreeNode<DataSetMember>> getChilds() {
		List<TreeNode<DataSetMember>> childs = new ArrayList<>();
		for (com.novamens.content.tree.TreeNode node  : getNode().getChilds()) {
			childs.add(new DataSetNode(node));
		};
		return childs.iterator();
	}
	
	public boolean hasChilds() {
		return getNode()!=null && getNode().hasChilds();
	}
	
	public boolean isWriteable() {
		return getNode().isWriteable();
	}
	
	public boolean hasPermission(Permission permission) {
		return getNode().hasPermission(permission);
	}
	
	com.novamens.content.tree.TreeNode getNode() {
		return node;
	}
	
	public void detach() {
	}
	
	@Override
	public boolean equals(Object object) {
		if (!(object instanceof DataSetNode)) 
			return false;
		if (((DataSetNode)object).getPath()!=null && ((DataSetNode)object).getPath().equals(getPath())) {
			return true;
		}
		return false;
	}
}
