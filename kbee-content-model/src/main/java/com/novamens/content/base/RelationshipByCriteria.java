package com.novamens.content.base;

import com.novamens.content.relationshipsbycriteria.RelationshipByCriteriaTemplate;

public interface RelationshipByCriteria {
	public Content getSource();
	public RelationshipByCriteriaTemplate getTemplate();
	public String getCriteria();
	public boolean includes(Content content);
	public RelationshipByCriteria clone();
}