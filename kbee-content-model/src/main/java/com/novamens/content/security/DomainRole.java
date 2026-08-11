package com.novamens.content.security;

import com.novamens.dom.Indexable;

public interface DomainRole extends Role, Indexable {
	
	static public final int TYPE = 1;
	default public boolean isEntity() { return false; };
}
