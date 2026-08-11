package com.novamens.content.form;

import com.novamens.content.model.RelationTemplate;

public interface ERelationModel<T> extends EFieldModel<T> {
	public RelationTemplate getRelation();
	static String GetTypeLabel() {
		return "Relation";
	}
}