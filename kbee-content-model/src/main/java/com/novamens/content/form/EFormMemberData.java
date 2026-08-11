package com.novamens.content.form;

import com.novamens.content.model.DataSetMember;

public interface EFormMemberData extends EFormData {
	public DataSetMember getMember();
	
    default Object getObject() {
        return getMember();
     }
}