package com.novamens.kbee.content.form;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.novamens.content.model.DataSetMember;

@JsonTypeName("members")
public class KbeeEMembersListField extends KbeeEListField<DataSetMember> {
	private static final long serialVersionUID = 1L;
	
	@Override
	@JsonIgnore
	public String getTypeLabel() {
		return "List";
	}
}