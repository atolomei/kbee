package com.novamens.content.model;

import java.util.List;

@Deprecated
public interface ModelSection  {
	public String getName();
	public String getDescription();
	public List<ModelElementTemplate> getStructure();
	public boolean isPortal();
}
