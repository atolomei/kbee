package com.novamens.content.text.template;

import java.util.List;

public interface ContentTextTemplate {
	public String getContentId();
	public String getTitle();
	public String getText(VariableResolver resolver);
	public String getText(VariableResolver variableresolver, IncludeResolver includeresolver);
	public List<Variable> getVariables();
}
