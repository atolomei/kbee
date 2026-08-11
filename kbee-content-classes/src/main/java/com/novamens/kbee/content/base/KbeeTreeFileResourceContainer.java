package com.novamens.kbee.content.base;


import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.novamens.content.base.TreeFileContainer;
import com.novamens.content.document.TreeFile;
import com.novamens.content.model.ContentTemplate;
import com.novamens.kbee.content.document.KbeeTreeFile;


@MappedSuperclass
public class KbeeTreeFileResourceContainer extends KbeeResourceContainer implements TreeFileContainer {

	/**
	 * This is the root of the tree
	 * A Content has only 1 TreeFile and a TreeFile is "owned" by only 1 Content
	 * The Relationship is Many to One because multiple versions of the Content can point to the same TreeFile
	 * 
	 */

	@ManyToOne(fetch = FetchType.LAZY, targetEntity = KbeeTreeFile.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name ="tree_file_id", updatable=false, nullable=true)
	private TreeFile tree_file;

	
	public KbeeTreeFileResourceContainer(ContentTemplate template) {
		 super(template);
	}
	
	public KbeeTreeFileResourceContainer() {
	}

	
	@Override
	public void setTreeFile(TreeFile tree_file) {
		this.tree_file=tree_file;

	}

	@Override
	public void removeTreeFile() {
		this.tree_file=null;
	}

	@Override
	public TreeFile getTreeFile() {
		return this.tree_file;
	}

}
