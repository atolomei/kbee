package kbee.web.security.user;

import com.novamens.content.entity.Profile;
import com.novamens.wicket.markup.html.editor.ObjectEditor;

public class ProfileEditor extends ObjectEditor<Profile>{
	private static final long serialVersionUID = 1L;

	public ProfileEditor(String id) {
		super(id);
	}
	
	public String getName() {
		return "";
	}

}
