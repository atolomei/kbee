package com.novamens.indexer.query;

import java.util.List;

public interface SemanticEngine {
	public List<SemanticResult> getObjects(String text);
}
