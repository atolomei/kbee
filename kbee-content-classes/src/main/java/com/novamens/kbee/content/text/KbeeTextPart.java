package com.novamens.kbee.content.text;

import com.novamens.content.text.TextPart;

public class KbeeTextPart implements TextPart {
	private static final long serialVersionUID = 1L;
	
	private String name;
	private String title;
	private int level;
	
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public int getLevel() {
		return level;
	}
	
	public void setLevel(int level) {
		this.level = level;
	}
}
