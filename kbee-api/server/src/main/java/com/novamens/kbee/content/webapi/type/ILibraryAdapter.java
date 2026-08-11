package com.novamens.kbee.content.webapi.type;

import com.novamens.content.library.Library;
import com.novamens.kbee.content.library.KbeeLibrary;

import kbee.api.model.ILibrary;

public class ILibraryAdapter implements Adapter<Library, ILibrary> {
	
	public ILibraryAdapter() {
	}
	
	public ILibrary adapt(Library library) {
		
		ILibrary ilibrary = new ILibrary();
		
		ilibrary.setDisplayName(library.getDisplayName());
		ilibrary.setName(library.getKey());
		ilibrary.setDomain(library.getDomain().getName());
		ilibrary.setId(String.valueOf(library.getId()));
		ilibrary.setCanonical(library.isCanonical());
		ilibrary.setState(String.valueOf(library.getState().name()));
		ilibrary.setLastModifiedDate(library.getLastModifiedOffsetDateTime());
		ilibrary.setLastModifiedUser(new ApiUserProxy(library.getLastModifiedUser()));
		
		if (((KbeeLibrary)library).getReaders()!=null) {
			ilibrary.setReaders(new IGroupProxy(((KbeeLibrary)library).getReaders()));
		}

		return ilibrary;	
	}
}
