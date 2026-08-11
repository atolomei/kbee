package com.novamens.kbee.content.tree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.tree.Tree;
import com.novamens.content.tree.TreeNode;
import com.novamens.content.tree.TreePath;
import com.novamens.content.tree.TreeService;
import com.novamens.event.AppUpdateEvent;
import com.novamens.event.Event;
import com.novamens.event.EventListener;
import com.novamens.kbee.event.EvictCacheServiceEvent;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.security.acl.Permission;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

public class KbeeTreeService implements TreeService, EventListener {
 	
	public Map<Serializable, Map<Serializable, List<TreeNode>>> nodes;
	
	public KbeeTreeService() {
		initialize();
	}
	
	@Override
	public List<TreeNode> getNodes(DataSetMember member) {
		List<TreeNode> nodes = getCache(member).get(member.getId());
		if (nodes!=null) {
			return nodes;
		}
		else {
			nodes = new ArrayList<>();
			for (TreePath path : getPaths(member)) {
				nodes.add(path.getNode());
			}
			getCache(member).put(member.getId(), nodes);
		}
		return nodes;
	}
	
	public Tree getTree(DataSet dataSet) {
		return new KbeeTree(dataSet);
	}

	public Tree getPermissionTree(DataSet dataSet, Permission permission) {
	    return isRestrictedUser()
	            ? new KbeePermissionTree(dataSet, permission)
	            : new KbeeTree(dataSet);
	}
	
	@Override
	public boolean listen(Event event) {
		if (event instanceof EvictCacheServiceEvent)
			return true;
		if (event instanceof AppUpdateEvent && event.getObject() instanceof DataSetMember)
			return true;
		return event.getObject() instanceof DataSet;
	}

	@Override
	public void onEvent(Event event) {
		if (event instanceof EvictCacheServiceEvent) {
			initialize();
		}
		else {
			if (event.getObject() instanceof DataSet) {
				nodes.remove(((DataSet)event.getObject()).getId());
			}
			else {
				if (event.getObject() instanceof DataSetMember) {
					if (((DataSetMember)event.getObject()).getDataSet()!=null) {
						nodes.remove(((DataSetMember)event.getObject()).getDataSet().getId());
					}
				}
			}
		}
	}
	
	protected List<TreePath> getPaths(DataSetMember member) {
		return getPaths(member, new ArrayList<>());
	}
	
	protected List<TreePath> getPaths(DataSetMember member, List<DataSetMember> childs) {
		List<TreePath> paths = new ArrayList<TreePath>();
		List<TreeNode> nodes = getCache(member).get(member.getId());
		if (nodes!=null) {
			for (TreeNode node : nodes) {
				paths.add(node.getPath());
			}
		}
		else {
			// syncronized(node)
			
			if (childs.contains(member)) {
				return paths;
			}
			else {
				childs.add(member);
			}
			
			nodes = new ArrayList<>();
			
			if (!member.getParents().isEmpty()) {
				for (DataSetMember parent : member.getParents()) {
					for (TreePath parentPath : getPaths(parent, childs)) {
						TreeNode node = newNode(member, parentPath);
						nodes.add(node);
						paths.add(node.getPath());
					}
				}
			}
			else {
				TreeNode node = newNode(member, new KbeeTreePath());
				nodes.add(node);
				paths.add(node.getPath());
			}

			
			getCache(member).put(member.getId(), nodes);
		}
		return paths;
	}
	
	protected TreeNode newNode(DataSetMember member, TreePath path) {
		KbeeTreeNode node = new KbeeTreeNode(member);
		node.setPath(path.plus(node));
		return node;
	}
	
	protected synchronized void initialize() {
		nodes = Collections.synchronizedMap(new HashMap<Serializable, Map<Serializable, List<TreeNode>>>());
	}
	
	protected Map<Serializable, List<TreeNode>> getCache(DataSetMember member) {
		Map<Serializable, List<TreeNode>> cache = nodes.get(member.getDataSet().getId());
		if (cache==null) {
			synchronized(this) {
				cache = nodes.get(member.getDataSet().getId());
				if (cache==null) {
					cache = Collections.synchronizedMap(new HashMap<Serializable, List<TreeNode>>());
					nodes.put(member.getDataSet().getId(),  cache);
				}
			}
		}
		return cache;
	}
	
	private boolean isRestrictedUser() {
	    SecurityService service = ServiceLocator.getService(SecurityService.class);
	    return !service.isRoot() && !service.isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	}
}