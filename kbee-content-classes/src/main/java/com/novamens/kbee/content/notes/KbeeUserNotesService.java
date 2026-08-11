package com.novamens.kbee.content.notes;

import java.time.OffsetDateTime;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.jsoup.Jsoup;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.novamens.content.base.ContentCreationException;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.notes.UserNote;
import com.novamens.content.notes.UserNotesService;
import com.novamens.security.User;
import com.novamens.system.SystemParameter;

public class KbeeUserNotesService implements UserNotesService {
			
	static private org.apache.logging.log4j.Logger logger = LogManager.getLogger(KbeeUserNotesService.class.getName());

	private User user;
	private ContentDao dao;
	
	public KbeeUserNotesService() {
	}
	
	public KbeeUserNotesService(Object object) {
		Assert.isInstanceOf(User.class, object);
		this.user = (User)object;
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void save(UserNote note) throws ContentMgmtException {
		note.setLastModifiedUser(getUser());
		note.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		
		String text=note.getText();
		if (text!=null) {
			
			
			text=removeTrailingSpaces(text);
			
			 String xtext=Jsoup.parse(text).text();
			
			
			
			text=text.replaceAll("<br><br><br>", "<p></p>");
			text=text.replaceAll("<br><br>", "<p></p>");
			
			text=text.replaceAll("<br/><br/><br/>", "<p></p>");
			text=text.replaceAll("<br/><br/>", "<p></p>");
			text=text.replaceAll("<p></p>", "<br/>");
			text=text.replaceAll("<p>&nbsp;</p>", "<br/>");
			note.setText(text);
		}
		
		getContentDao().save(note);
	}
	
	
	public static String removeTrailingSpaces(String param) 
    {
        if (param == null)
            return null;
        
        int len = param.length();
        for (; len > 0; len--) {
            if (!Character.isWhitespace(param.charAt(len - 1)))
                break;
        }
        return param.substring(0, len);
    }
	
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void update(UserNote note) throws ContentMgmtException {
		note.setLastModifiedUser(getUser());
		note.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		
		String text=note.getText();
		if (text!=null) {
			text=text.replaceAll("<br><br><br><br>", "<br>");
			text=text.replaceAll("<br><br><br>", "<br>");
			text=text.replaceAll("<br><br>", "<br>");
			
			text=text.replaceAll("<br/><br/><br/>", "<br>");
			text=text.replaceAll("<br/><br/>", "<br>");
			text=text.replaceAll("<p></p>", "<br>");
			text=text.replaceAll("<p>&nbsp;</p>", "<br>");
			
			text=text.replaceAll("<br>", "<br/>");

			
			
			note.setText(text);
		}
		
		getContentDao().update(note);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void remove(UserNote note) throws ContentMgmtException {
		getContentDao().delete(note);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void removeAllNotes() throws ContentMgmtException {
		getContentDao().deleteAllNotes(getUser());
	}
	
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public UserNote createUserNote() throws ContentCreationException, ContentMgmtException {
		Locale locale = getUser().getLocale();
		ResourceBundle res = ResourceBundle.getBundle(KbeeUserNotesService.this.getClass().getName(), locale);
		String title= res.getString("title") + String.valueOf(OffsetDateTime.now().getDayOfMonth())+ " " + OffsetDateTime.now().getMonth().getDisplayName(TextStyle.SHORT, getUser().getLocale())+ " " +String.valueOf(OffsetDateTime.now().getYear());
		return createUserNote(title, null);
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public UserNote createUserNote(String title, String text) throws ContentCreationException, ContentMgmtException {
		KbeeUserNote note = new KbeeUserNote(getUser());
		note.setTitle(title);
		if (text!=null)
			note.setText(text);
		
		getContentDao().save(note);
		return note;
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public UserNote createWelcomeUserNote() throws  ContentCreationException, ContentMgmtException {
		SystemParameter title_p = getContentDao().findSystemParameterByKey("welcome-note.title");
		Locale locale = getUser().getLocale();
		ResourceBundle res = ResourceBundle.getBundle(KbeeUserNotesService.this.getClass().getName(), locale);
		String default_title= res.getString("default.welcome_title");
		String title = title_p!=null?title_p.getValue():default_title;
		String default_text= res.getString("default.welcome_text");
		SystemParameter text_p = getContentDao().findSystemParameterByKey("welcome-note.text");
		String text = text_p!=null?text_p.getValue():default_text;
		return createUserNote(title, text);
	}

	@Override
	public List<UserNote> getUserNotes() {
		return getContentDao().getUserNotes(getUser());
	}

	@Override
	public long getTotalNotes() throws ContentMgmtException {
		try {
			return getContentDao().getTotalUserNotes(getUser());
		} catch (Exception e) {
			logger.error(e);
			throw new ContentMgmtException(e);
		}
	}

	public User getUser() {
		return this.user;
	}

	public void setContentDao(ContentDao dao) {
		this.dao=dao;
	}
	
	private ContentDao getContentDao() {
		return dao;
	}

	
}
