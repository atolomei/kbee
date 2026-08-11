package com.novamens.kbee.content.document;

public class FSItemResource extends AbstractFSItem {

	@Override
	public boolean isDirectory() {
		return false;
	}

	@Override
	public boolean isResource() {
		return true;
	}
}
