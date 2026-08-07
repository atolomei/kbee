package com.novamens.dom;

public interface Versionable<T> extends Object {

	public T getPreviousVersion();
	public void setPreviousVersion(Object object);
	public boolean isHeadVersion();
	public void setHeadVersion(boolean value);
	public int getVersion();
	public void setVersion(int version);

	public int getNextVersion();
	
	public T clone();
}
