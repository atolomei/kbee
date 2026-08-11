package kbee.web.library;


import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.library.Library;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.web.nav.LibrariesBC;
import kbee.web.nav.SettingsDropDownBC;
import kbee.web.object.TitleHeaderPanel;

public class LibraryHeaderPanel extends TitleHeaderPanel<Library> {
	
	private static final long serialVersionUID = 1L;
	
	IModel<String> icon = new Model<String>("far fa-university");

	public LibraryHeaderPanel(IModel<Library> model) {
		super("cabinet-panel", model);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		MenuBreadCrumbPanel<?>  bc = new MenuBreadCrumbPanel<Void>();
		bc.addElement(new SettingsDropDownBC());
		bc.addElement(new LibrariesBC());
		bc.addElement(new BCElement(new Model<String>(getModel().getObject().getDisplayName())));
		setBreadCrumbPanel(bc);
	}

	protected IModel<String> getGlyphicon() {
		return icon; 
	}
} 
