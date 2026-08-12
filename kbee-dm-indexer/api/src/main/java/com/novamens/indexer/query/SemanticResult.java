package com.novamens.indexer.query;

import java.io.Serializable;

public interface SemanticResult extends Serializable {
	public Object getObject();
	public int getScore();
}
