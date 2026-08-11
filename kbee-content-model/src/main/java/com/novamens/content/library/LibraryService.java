package com.novamens.content.library;

import java.util.List;

import com.novamens.content.base.Content;
import com.novamens.dom.ObjectState;
import com.novamens.service.ObjectService;

public interface LibraryService extends ObjectService {
	public List<Library> getLibraries();
	
	public List<Library> getLibraries(ObjectState state, String order);
	public List<Library> getLibraries(Content content);
	public Library getDefault();
	public boolean readables();
}
