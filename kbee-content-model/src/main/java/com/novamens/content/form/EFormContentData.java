package com.novamens.content.form;

import com.novamens.content.base.Content;

public interface EFormContentData extends EFormData {
	public Content getContent();
	
    default Object getObject() {
        return getContent();
     }
}
