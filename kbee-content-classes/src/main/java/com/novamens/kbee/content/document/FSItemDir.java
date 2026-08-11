package com.novamens.kbee.content.document;

public class FSItemDir extends AbstractFSItem {

	@Override
	public boolean isDirectory() {
		return true;
	}

	@Override
	public boolean isResource() {
		return false;
	}

}
