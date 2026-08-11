package com.novamens.content.notes;

import java.util.List;

import com.novamens.content.base.ContentCreationException;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.service.ObjectService;

/**
 * 
 * see {@link UserNotes}
 *  
 */
public interface UserNotesService extends ObjectService {
	
		public List<UserNote> getUserNotes();
		public UserNote createUserNote() throws ContentCreationException, ContentMgmtException;
		public UserNote createUserNote(String string, String string2) throws ContentCreationException, ContentMgmtException;
		public UserNote createWelcomeUserNote() throws ContentCreationException, ContentMgmtException;
		
		public void remove(UserNote note) throws ContentMgmtException;
		public void update(UserNote note) throws ContentMgmtException;
		public void save(UserNote note) throws ContentMgmtException;
		public void removeAllNotes() throws ContentMgmtException;
		public long getTotalNotes()  throws ContentMgmtException;;
		

}
