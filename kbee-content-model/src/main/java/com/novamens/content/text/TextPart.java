package com.novamens.content.text;

import java.io.Serializable;

public interface TextPart extends Serializable {
	public String getName();
	public String getTitle();
	public int getLevel();
}
