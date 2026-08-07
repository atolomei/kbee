package com.novamens.dom;

import java.io.Serializable;
import java.util.Map;

public interface Url extends Serializable {
	public String getPath();
	public Map<String, String> getParameters();
}
