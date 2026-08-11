package com.novamens.content.base;

public interface ContentLink {
	public Content getSource();
	public Content getTarget();
	public String getAnchor();
	public Resource getResource();
	public ContentLink clone();
	public boolean isTargetUpdated();
}
