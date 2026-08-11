package com.novamens.kbee.content.webapi.type;


import java.util.ArrayList;
import java.util.List;

import com.novamens.content.model.DataSetMember;
import com.novamens.content.tree.TreeNode;

import kbee.api.model.ApiProxy;
import kbee.api.model.INode;

public class INodeAdapter implements Adapter<TreeNode, INode> {
	
	
	public INodeAdapter() {
	}
	
	public INode adapt(TreeNode node) {
		
		INode inode =  new INode();

		List<INode> breadCrumb = new ArrayList<>();
		String id ="";
		for (TreeNode path : node.getPath().getNodes()) {
			if (!node.getId().equals(path.getId())) {
				breadCrumb.add(adapt(path));
			}
			if (!"".equals(id)) {
				id += "/";
			}
			id += path.getId();
		}
		
		DataSetMember value = node.getObject();
		
		inode.setId(id);
		inode.setState(value.getState().name());
		inode.setDisplayName(node.getDisplayName());
		inode.setHasChilds(node.hasChilds());
		inode.setDomain(value.getDomain().getName());
		inode.setDomainRef(new ApiProxy(String.valueOf(value.getDomain().getId()), 
			value.getDomain().getName(), 
			UriHelper.getUri(value.getDomain()), 
			"domain"));
		INode i2 =  new INode();
		i2.setId(id);
		i2.setDisplayName(node.getDisplayName());
		breadCrumb.add(i2);
		inode.setBreadCrumb(breadCrumb);
		
		return inode;	
	}
	
}
