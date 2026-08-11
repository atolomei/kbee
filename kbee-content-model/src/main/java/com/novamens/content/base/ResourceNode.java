package com.novamens.content.base;

// ResourceNode es un Resource o un ResourceFolder en un sistema (arbol) de recursos
public interface ResourceNode extends Resource {
	public Resource getResource();
	public ResourceFolder getFolder();
	public boolean isIndex();
}