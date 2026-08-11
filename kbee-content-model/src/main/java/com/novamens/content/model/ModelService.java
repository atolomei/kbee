package com.novamens.content.model;

import java.util.List;

import com.novamens.service.ObjectService;

public interface ModelService  extends ObjectService {
	public List<ModelReference> getReferences(ModelElement element);
}
