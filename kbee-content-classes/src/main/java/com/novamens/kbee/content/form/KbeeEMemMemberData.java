package com.novamens.kbee.content.form;

import com.novamens.content.form.EForm;
import com.novamens.content.form.EFormData;
import com.novamens.content.form.EFormMemberData;
import com.novamens.content.model.DataSetMember;

public class KbeeEMemMemberData extends KbeeEMemData implements EFormMemberData {
	
	private DataSetMember member;
	
	public KbeeEMemMemberData(EForm form, DataSetMember member) {
		super(form);
		this.member = member;
	}
	
	public DataSetMember getMember() {
		return member;
	}
	
	public EFormData clone() {
		KbeeEMemData clone = new KbeeEMemMemberData(getForm(), getMember());
		clone.setData(getData());
		return clone;
	}
	
	@Override
	public String getObjectTitle() {
		return getMember().getDisplayName();
	}

}