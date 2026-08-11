package com.novamens.kbee.content.form;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.novamens.content.form.EComboField;
import com.novamens.content.form.EFormData;
import com.novamens.content.form.EFormDataSource;
import com.novamens.content.model.Classificable;
import com.novamens.content.model.DataSetMember;

@JsonTypeName("member combo")
public class KbeeEMemberComboField extends EFormAbstractField<DataSetMember> implements EComboField<DataSetMember> {
	private static final long serialVersionUID = 1L;

	@JsonIgnore
	public EFormDataSource<DataSetMember> getChoicesSource(Classificable object) {
		return getModel().getDataSource(object);
	}
	
	@Override
	public void get(Object object, EFormData data) {
		data.setData(this, getModel().get(object));
	}
	
	@Override
	@JsonIgnore
	public String getTypeLabel() {
		return "Combo";
	}
}