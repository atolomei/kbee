package com.novamens.content.form;

import java.io.Serializable;

public interface EIdentifiableForm extends EForm {
	public String getName();
	public Serializable getId();
	public int getVersion();
}
