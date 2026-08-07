package com.novamens.logging;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.novamens.content.document.TreeFile;

@Entity
@DiscriminatorValue("TreeFileDeleteEvent")
public class TreeFileDeleteEvent extends TreeFileEvent {

	public TreeFileDeleteEvent() {
		super();
	}

	public TreeFileDeleteEvent(TreeFile tree_file, String description) {
		super(tree_file, description);
	}
	
	
	@Override
	public String getEventType() {
		return "TreeFileDelteEvent";
	}
	
	@Override
	public String getAction() {
		return "Delete";
	}
}
