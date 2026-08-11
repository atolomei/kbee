package com.novamens.content.form;

import java.util.List;

import com.novamens.content.base.Content;
import com.novamens.content.model.RelationTemplate;

public interface EFormRelation extends EFormField<List<Content>> {
	public RelationTemplate getRelation();
}
