package com.novamens.security;

import java.io.Serializable;

public interface Identifiable  {
	public Serializable getId();
	public String getDisplayName();
}