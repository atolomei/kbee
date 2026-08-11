package com.novamens.content.relationshipsbycriteria;

import java.util.List;
import java.util.Map;

import com.novamens.content.base.Content;
import com.novamens.service.ObjectService;

public interface RelationshipsByCriteriaService extends ObjectService {
	public List<Content> getRelated();
	public Map<RelationshipByCriteriaTemplate, List<Content>> getRelatedTemplates();
}