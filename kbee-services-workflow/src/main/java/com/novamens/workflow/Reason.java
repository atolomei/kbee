package com.novamens.workflow;

import java.io.Serializable;

public interface Reason extends Serializable {
	public String getCode();
	public String getLabel();
}
