package com.novamens.content.security;

import com.novamens.content.model.Classifier;

public interface EntityRole extends Role {
	
	static public final int TYPE = 2;
	
	public Classifier getClassifier();
	public boolean enableUserAdmin();
	
	default public boolean isEntity() { return true; };
}