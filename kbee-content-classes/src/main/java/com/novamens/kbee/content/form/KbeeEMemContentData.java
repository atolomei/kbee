package com.novamens.kbee.content.form;

import com.novamens.content.base.Content;
import com.novamens.content.form.EForm;
import com.novamens.content.form.EFormContentData;
import com.novamens.content.form.EFormData;

public class KbeeEMemContentData extends KbeeEMemData  implements EFormContentData {
	
	private Content content;
	
	public KbeeEMemContentData(EForm form, Content content) {
		super(form);
		this.content = content;
	}
	
	public Content getContent() {
		return content;
	}
	
	public EFormData clone() {
		KbeeEMemData clone = new KbeeEMemContentData(getForm(), getContent());
		clone.setData(getData());
		return clone;
	}
	
	@Override
	public String getObjectTitle() {
		return getContent().getTitle();
	}
}