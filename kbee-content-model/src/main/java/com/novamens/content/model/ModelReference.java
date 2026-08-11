package com.novamens.content.model;

import java.io.Serializable;

public interface ModelReference extends Serializable {
	public String getGroup();
	public String getDescription();
	public String getObject();
	public String getUrl();
	
	public String getModelElementClass();  // Classifier, Content Template, DataSet, ...
}
