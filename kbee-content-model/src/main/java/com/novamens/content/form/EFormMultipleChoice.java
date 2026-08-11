package com.novamens.content.form;

import java.util.List;

public interface EFormMultipleChoice extends EFormContainer {
	public List<EFormChoice> getChoices();
	public String getText();
}