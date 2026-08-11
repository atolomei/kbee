package com.novamens.content.form;

import com.novamens.content.model.RelationTemplate;

public interface ERelationResourceModel<T> extends EResourceModel<T> {
	public RelationTemplate getRelation();
	static String GetTypeLabel() {
		return "Relation Resource";
	}
   }