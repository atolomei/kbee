package com.novamens.content.service;

import java.util.List;
import java.util.Map;

import com.novamens.content.model.Classificable;
import com.novamens.content.model.DataSetMember;
import com.novamens.indexer.query.Suggestion;
import com.novamens.service.ObjectService;

public interface DataAccessService extends ObjectService {
	public boolean isReadable(DataSetMember value);
	public List<DataSetMember> getAll();
	public List<DataSetMember> getAll(Classificable object);
	public long getTotalMembers();
	public List<Suggestion> getSuggestions(String pattern, Classificable object, Map<String, Object> parameters);
	public List<Suggestion> getSuggestions(String pattern, Classificable object);
	public List<Suggestion> getSuggestions(String pattern);
}
