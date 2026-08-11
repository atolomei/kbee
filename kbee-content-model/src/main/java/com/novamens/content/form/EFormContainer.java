package com.novamens.content.form;

import java.util.List;

public interface EFormContainer extends EFormComponent {
	public List<EFormComponent> getComponents();
	public void setComponents(List<EFormComponent> components);
}
