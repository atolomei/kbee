package com.novamens.content.base;

import com.novamens.content.model.RelationTemplate;

public interface Relation {
	public Content getSource();
	public Content getTarget();
	public RelationTemplate getTemplate();
	public Relation clone();
}
