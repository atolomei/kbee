package com.novamens.content.relationshipsbycriteria;

import com.novamens.content.base.Content;

public interface RelationshipByCriteriaTemplate {
	public String getName();
	public String getTargetLabel();
	public boolean includes(Content content);
	public boolean related(Content source, Content target);
	public String getReverseLabel();
}
