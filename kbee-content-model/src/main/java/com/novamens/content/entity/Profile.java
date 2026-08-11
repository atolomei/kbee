package com.novamens.content.entity;

import java.time.OffsetDateTime;

import com.novamens.dom.Domain;

public interface Profile {
	public String getName();
	public Entity getEntity();
	public Domain getDomain();
	
	public OffsetDateTime getLastModifiedOffsetDateTime();
	public OffsetDateTime getCreationOffsetDateTime();
}