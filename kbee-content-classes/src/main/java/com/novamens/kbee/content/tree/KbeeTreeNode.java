package com.novamens.kbee.content.tree;

import java.io.Serializable;
import java.util.List;

import org.hibernate.ObjectNotFoundException;
import org.hibernate.SessionFactory;

import com.novamens.beans.BeansService;
import com.novamens.content.base.SecurityRule;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.SecuredMember;
import com.novamens.content.security.ContentSystemSecurityService;
import com.novamens.content.tree.TreeNode;
import com.novamens.content.tree.TreePath;
import com.novamens.kbee.content.dao.Proxy;
import com.novamens.security.acl.Acl;
import com.novamens.security.acl.Permission;
import com.novamens.service.ServiceLocator;

public class KbeeTreeNode implements TreeNode, Serializable {
	private static final long serialVersionUID = 1L;
	
	private String displayName;
	private TreePath path;
	private boolean secured = false;
	private Serializable memberId;
	private boolean includeAcl = false;
	private Class<?> clazz = null;
	private transient DataSetMember object;
	
	KbeeTreeNode(DataSetMember member) {
		memberId = member.getId();
		setDisplayName(member.getDisplayName());
		clazz = getClass(member);
		member = (DataSetMember)Proxy.Unproxy(member);
		if (member instanceof SecuredMember) {
			SecurityRule rule = ((SecuredMember)member).getSecurityRule();
			includeAcl = rule!=null && rule.getAcl()!=null && !((Acl)rule.getAcl()).getEntries().isEmpty();
			secured = true;
		}
		else {
			secured = false;
		}
	}
	
	public String getId() {
		return String.valueOf(memberId);
	}
	
	@Override
	public TreePath getPath() {
		return path;
	}
	
	public void setPath(TreePath path) {
		this.path = path;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	
	public List<TreeNode> getChilds() {
		return null;
	}
	
	public boolean hasChilds() {
		return true;
	}
	
	public boolean includeAcl() {
		return includeAcl;
	}
	
	public boolean isDescendant(TreeNode node) {
		return !node.equals(this) && getPath().isDescendant(node.getPath());
	}

	@Override
	public boolean equals(Object node) {
		if (!(node instanceof KbeeTreeNode)) return false;
		return ((KbeeTreeNode)node).getId().equals(getId());
	}
	
	public DataSetMember getObject() {
			SessionFactory sf = (SessionFactory)ServiceLocator.getService(BeansService.class).getBean("sessionFactory");
			try {
				object = (DataSetMember)Proxy.Unproxy(sf.getCurrentSession().load(clazz, memberId));
			}	
			catch (ObjectNotFoundException e) {
				throw new RuntimeException(e);
			}
		return object;
	}

	public boolean isWriteable() {
		return !secured || 
			ServiceLocator
				.getService(ContentSystemSecurityService.class)
				.isWriteable((SecuredMember)getObject());
	}
	
	public boolean hasPermission(Permission permission) {
		return !secured || 
			ServiceLocator
				.getService(ContentSystemSecurityService.class)
				.hasPermission((SecuredMember)getObject(), permission);
	}
	
	private Class<?> getClass(DataSetMember member) {
		try {
			String classname = member.getClass().getName();
			int i = classname.indexOf("_");
			if (i>0) classname = classname.substring(0, i);
			i = classname.indexOf("$");
			if (i>0) classname = classname.substring(0, i);
			Class<?> clazz = Class.forName(classname);
			return clazz;
		}	
		catch (ClassNotFoundException  e ) {
			throw new RuntimeException(e);
		}
	}
}