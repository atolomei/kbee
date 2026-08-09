package com.novamens.workflow;

import java.util.Locale;

public interface ProcedurePhase {
	public String getName();
	public String getIcon();
	public String getLabel();
	public String getLabel(Locale locale);
}
