package com.novamens.content.text.template;

import java.io.Serializable;

public class Include implements Serializable {
	private static final long serialVersionUID = 1L;
	private int offset, length;
	private String name;

	public Include() {
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getValue() {
		return null;
	}
	
	public void setOffset(int value) {
		this.offset = value;
	}
	
	public int getOffset() {
		return offset;
	}
	public void setLength(int value) {
		this.length = value;
	}
	
	public int getLength() {
		return length;
	}
}