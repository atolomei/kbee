package com.novamens.indexer.service;

import java.io.Serializable;

public interface IndexProxy extends JavaIndex, Serializable {
	public Index getIndex();
}
