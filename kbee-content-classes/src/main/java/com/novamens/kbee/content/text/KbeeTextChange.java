package com.novamens.kbee.content.text;

import java.util.List;

import com.novamens.content.text.TextChange;
import com.novamens.content.text.TextPart;

public class KbeeTextChange implements TextChange {
	private static final long serialVersionUID = 1L;
	
	int type;
	TextPart part;
	List<String> notes;
	
	public int getType() {
		return type;
	}
	
	public void setType(int type) {
		this.type = type;
	}
	
	public TextPart getPart() {
		return part;
	}
	
	public void setPart(TextPart part) {
		this.part = part;
	}
	
	public List<String> getNotes() {
		return notes;
	}
	
	public void setNotes(List<String> notes) {
		this.notes = notes;
	}
}
