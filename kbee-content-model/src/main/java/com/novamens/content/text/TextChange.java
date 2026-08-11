package com.novamens.content.text;

import java.io.Serializable;
import java.util.List;

public interface TextChange extends Serializable {
	
	public static int ADD = 0;
	public static int UPDATE = 1;
	public static int DELETE = 2;
	
	public int getType();
	public TextPart getPart();
	public List<String> getNotes();
}