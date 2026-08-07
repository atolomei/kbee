package com.novamens.logging;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.novamens.content.document.TreeFile;

@Entity
@DiscriminatorValue("TreeFileUpdateEvent")
public class	TreeFileUpdateEvent extends TreeFileEvent {
			
	
	public TreeFileUpdateEvent() {
		super();
	}

	public TreeFileUpdateEvent(TreeFile tree_file, String description) {
		super(tree_file, description);
	}
	
	
	@Override
	public String getEventType() {
		return "TreeFileUpdateEvent";
	}
	
	@Override
	public String getAction() {
		return "Update";
	}
}
