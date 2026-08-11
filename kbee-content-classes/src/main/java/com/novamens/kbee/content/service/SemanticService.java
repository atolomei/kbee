package com.novamens.kbee.content.service;

import java.util.List;

import com.novamens.content.base.Content;
import com.novamens.service.ObjectService;

public interface SemanticService extends ObjectService {

	public List<Content> generateSemanticRelated();
	public List<Content> getSemanticRelated();
	public List<Content> generateSemanticRelatedNoTrx();
	
	public void removeSemanticRelated();
	public boolean hasSemanticRelatedCalculated();
	
}
