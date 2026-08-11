package com.novamens.content.form;

import com.novamens.content.base.ResourceTag;

public interface EResourceModel<T> extends EFieldModel<T> {
	public ResourceTag getTag();
	static String GetTypeLabel() {
		return "Resource";
	}
}