package com.novamens.kbee.content.form;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.novamens.content.base.ResourceNode;
import com.novamens.content.form.EFormData;
import com.novamens.content.form.EListField;
import com.novamens.content.form.EResourceDistributionField;

@JsonTypeName("resourcedistribution")
public class KbeeEResourceDistribution extends KbeeEResourceSystemV2 implements EResourceDistributionField, EListField<ResourceNode> {
	private static final long serialVersionUID = 1L;
	
	@Override
	@JsonIgnore
	public String getTypeLabel() {
		return "Resource Distribution";
	}
	
	@Override
	public void get(Object object, EFormData data) {
		data.setData(this, getModel().getValues(object));
	}
	
	@Override
	public void set(Object object, EFormData data) {
		KbeeEResourceSystemV2 rs= new KbeeEResourceSystemV2();
		rs.setName(getName());
		rs.setModel(getModel());
		getModel().set(object, data.getValues(rs));
	}
	

}	
