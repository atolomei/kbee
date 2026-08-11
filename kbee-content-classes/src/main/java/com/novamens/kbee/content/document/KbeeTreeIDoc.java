package com.novamens.kbee.content.document;


import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.novamens.content.document.TreeFile;
import com.novamens.content.document.TreeIDoc;
import com.novamens.content.model.ContentTemplate;
import com.novamens.kbee.content.base.KbeeResourceContainer;


/**
 * 
 * 
 * @Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "TYPE", discriminatorType = DiscriminatorType.INTEGER)

 * 
 */
@Entity
@PrimaryKeyJoinColumn(name="content_id")
@Table(name = "kb_tree_idoc")
public class KbeeTreeIDoc extends KbeeResourceContainer implements TreeIDoc {
			
	@SuppressWarnings("unused")
	private static kbee.util.logging.Logger kblogger = kbee.util.logging.Logger.getLogger(KbeeTreeIDoc.class.getName());

	
	public String toString() {
		StringBuilder str = new StringBuilder(); 
		str.append(TreeIDoc.CLASS_CODE + (getName()!=null?getName():"null"));
		return str.toString();
	}
	
	/**
	 * This is the root of the TreeDoc
	 * A Content has only 1 TreeFile and a TreeFile is "owned" by only 1 Content
	 * The Relationship is Many to One because multiple versions of the Content can point to the same TreeFile
	 * 
	 */
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = KbeeTreeFile.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name ="tree_file_id")
	private TreeFile tree_file;
	
	
	
	@Override
	public long getTotalSize() {
	return getTreeFile().getTotalSize();	
	}
	
	@Override
	public int getTotalNodes() {
		return getTreeFile().getTotalNodes();	
	}

	
	/**
	 * for Hibernate
	 */
	public KbeeTreeIDoc() {
	
	}
	
	
	public KbeeTreeIDoc(ContentTemplate ct) {
		super(ct);
	}
	
	
	@Override
	public TreeFile getTreeFile() {
		return this.tree_file;
	}
	
	@Override
	public void setTreeFile(TreeFile doc) {
		this.tree_file=doc;
	}
	
	@Override
	public String getClassCode() {
		return TreeIDoc.CLASS_CODE;
	}	
	
	/**
	 * 
	 * @param parent_node
	 * @param resource
	 */
	
	//@Transient
	//TreeNode<FSItem> root;
	//public void addResource(TreeNode<FSItem> parent_node, Resource resource) {}
	//public void addDirectory(TreeNode<FSItem> parent_node, String directory) {}
	//public TreeNode<FSItem> getTree() {	return root;}

	
	
}
