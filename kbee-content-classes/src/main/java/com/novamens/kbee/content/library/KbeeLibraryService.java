package com.novamens.kbee.content.library;

import java.util.ArrayList;
import java.util.List;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.library.Library;
import com.novamens.content.library.LibraryService;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.repository.DomRepository;
import com.novamens.repository.DomRepositoryService;
import com.novamens.service.ServiceLocator;

public class KbeeLibraryService implements LibraryService {
	
	private Domain  domain;
	
	public KbeeLibraryService() {
	}
	
	public KbeeLibraryService(Domain domain) {
		 this.domain = domain;
	}
	
	
	public List<Library> getLibraries(ObjectState state, String order) {
		List<Library> libraries = getRepository().findAll(getDomain(), state, order);
		return libraries;
	}
	
	
	@Override
	public List<Library> getLibraries() {
		List<Library> libraries = getRepository().findAll(getDomain());
		return libraries;
	}
	
	@Override
	public List<Library> getLibraries(Content content) {
		List<Library> libraries = new ArrayList<Library>();
		for (Library library : getRepository().findAll(getDomain())) {
			if (library.includes(content)) {
				libraries.add(library);
			}
		}
		return libraries;
	}
	
	@Override
	public Library getDefault() {
		for (Library library : getRepository().findAll(getDomain())) {
			if (library.isCanonical()) {
				return library;
			}
		}
		return null;
	}
	
	@Override
	public boolean readables() {
		for (Library library : getLibraries()) {
			if (library.isReadable())
				return true;
		}
		return false;
	}
	
	public Domain getDomain() {
		return domain;
	}
	
	protected DomRepository<Library> getRepository() {
		return ServiceLocator.getService(DomRepositoryService.class).getRepository(Library.class);
	}
	
	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
}
