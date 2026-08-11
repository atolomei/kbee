package com.novamens.content.model;

import java.util.Locale;

public interface AttributeValidatable<T> {
	public T getValue();
	public void setError(String message);
	public String getMessage();
	public Locale getLocale();
}
