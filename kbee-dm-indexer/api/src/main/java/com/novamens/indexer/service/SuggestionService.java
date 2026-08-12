package com.novamens.indexer.service;

import java.util.List;
import java.util.Map;

import com.novamens.indexer.query.Suggestion;
import com.novamens.service.ObjectService;

public interface SuggestionService extends ObjectService {
	public List<Suggestion> getSuggestions(String pattern, Map<String, Object> parameters);
	public List<Suggestion> getSuggestions(String pattern);
}
