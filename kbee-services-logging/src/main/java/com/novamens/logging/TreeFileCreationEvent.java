package com.novamens.logging;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.novamens.content.document.TreeFile;


@Entity
@DiscriminatorValue("TreeFileCreationEvent")
public class TreeFileCreationEvent extends TreeFileEvent {
		
	
	
	public TreeFileCreationEvent() {
		super();
	}

	public TreeFileCreationEvent(TreeFile tree_file, String description) {
		super(tree_file, description);
	}
	
	
	
	@Override
	public String getEventType() {
		return "TreeFileCreationEvent";
	}
	
	@Override
	public String getAction() {
		return "Create";
	}
}
