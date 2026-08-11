package com.novamens.content.base;

public interface ContentResource {
	public Resource getResource();
	@Deprecated
	public boolean isPublicArea();
	public boolean isIndex();
	public ResourceTag getTag();
	public ResourceFolder getFolder();
	public int getOrder();
}