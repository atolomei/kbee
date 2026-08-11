package com.novamens.kbee.content.indexer;

import com.novamens.dom.Domain;
import com.novamens.indexer.service.Index;

public interface JavaIndexFactory {
	public String getName();
	public Index getIndex(Domain domain);
}
