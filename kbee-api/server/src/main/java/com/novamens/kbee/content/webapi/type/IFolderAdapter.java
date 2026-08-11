package com.novamens.kbee.content.webapi.type;

import org.springframework.http.HttpStatus;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.Classificable;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.tree.Tree;
import com.novamens.content.tree.TreeNode;
import com.novamens.content.tree.TreePath;
import com.novamens.content.tree.TreeService;
import com.novamens.kbee.content.document.KbeeIDoc;
import com.novamens.kbee.content.tree.KbeeTreePath;
import com.novamens.service.ServiceLocator;

import kbee.api.model.ApiObject;
import kbee.api.model.ApiViewMode;
import kbee.api.model.INode;
import kbee.api.service.ApiError;
import kbee.api.service.ApiException;

public class IFolderAdapter implements Adapter<Classificable, ApiObject> {
	
	INode folder;
	
	public IFolderAdapter(INode folder) {
		this.folder = folder;
	}
	
	public ApiObject adapt(Classificable classificable) {
		ApiObject iobject = new ApiObject();
		
		iobject = classificable instanceof KbeeIDoc
			? (new IDocAdapter("0", ApiViewMode.Site, false, false, false))
				.adapt((KbeeIDoc)classificable)
			:  adapt((DataSetMember)classificable);		
		
		return iobject;	
	}
	
	private INode adapt(DataSetMember value) {
		Tree tree = ServiceLocator.getService(TreeService.class).getTree(value.getDataSet());
		
		TreeNode node = null;
		
		TreePath treePath = new KbeeTreePath();
        String path[] = folder.getId().split("/");
        for (int n=0; n<path.length; n++) {
        	String valueid = path[n].trim();
    		DataSetMember parent = (DataSetMember)getContentDao().findMemberById(Long.valueOf(valueid));
     		if (parent==null) {
    			throw new ApiException(HttpStatus.NOT_FOUND, ApiError.VALUE_NOT_FOUND);
    		}
       		node = tree.getNode(parent, treePath);
    		if (node==null) {
    			throw new ApiException(HttpStatus.NOT_FOUND, ApiError.VALUE_NOT_FOUND);
    		}
    		treePath = node.getPath();
        }
        
   		node = tree.getNode(value, treePath);
   		
		if (node==null) {
			throw new ApiException(HttpStatus.NOT_FOUND, ApiError.VALUE_NOT_FOUND);
		}
		
		INode inode = (new INodeAdapter()).adapt(node);
		
		return inode;
	}
	
	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
}