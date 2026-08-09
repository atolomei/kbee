package kbee.web.resource;

import org.apache.wicket.model.IModel;

import com.novamens.content.base.Resource;
import com.novamens.content.base.ResourceFolder;
import com.novamens.content.base.ResourceNode;
import com.novamens.kbee.content.resource.KbeeResourceNode;
import com.novamens.wicket.model.ObjectModel;

public class ResourceNodeModel implements IModel<ResourceNode> {
	private static final long serialVersionUID = 1L;
	
	private ResourceNode node;
	private boolean index = false;
	private IModel<Resource> resourcemodel;
	private IModel<ResourceFolder> foldermodel;
	
	public ResourceNodeModel(ResourceNode resource) {
		setObject(resource);
	}
	
	public ResourceNode getObject() {
		if (node==null) {
			node = new KbeeResourceNode(getResource(), getFolder());
			((KbeeResourceNode)node).setIndex(index);
		}
		return node;
	}
	
	public void setObject(ResourceNode node) {
		this.node = node;
		setResource(node.getResource());
		setFolder(node.getFolder());
	}
	
	public void setResource(Resource resource) {
		resourcemodel = resource!=null ? new ResourceModel(resource) : null;
	}
	
	public Resource getResource() {
		return resourcemodel!=null ? resourcemodel.getObject() : null;
	}
	
	public ResourceFolder getFolder() {
		return foldermodel!=null ? foldermodel.getObject() : null;
	}
	
	public void setFolder(ResourceFolder folder) {
		foldermodel = folder!=null ? new ObjectModel<ResourceFolder>(folder) : null;
	}
	
	public void detach() {
		if (resourcemodel!=null) resourcemodel.detach();
		if (foldermodel!=null) {
			if (node!=null && (node.getFolder()==null || !node.getFolder().equals(foldermodel.getObject()))) {
				setFolder(node.getFolder());
			}
			if (node!=null) {
				index = node.isIndex();
			}
			if (foldermodel!=null)
			foldermodel.detach();
			
		}
		else {
			if (node!=null) {
				setFolder(node.getFolder());
				index = node.isIndex();
			}
		}
		node = null;
	}
}
