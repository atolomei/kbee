package com.novamens.content.form;

import java.io.Serializable;

public interface UpdatedField extends Serializable {
	public String getField();
	public String getLabel();
	public EForm getForm();
	public String getAction();
	public boolean same(UpdatedField field);
}