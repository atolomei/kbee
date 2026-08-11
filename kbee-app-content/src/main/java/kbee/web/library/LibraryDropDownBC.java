package kbee.web.library;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.library.Library;
import com.novamens.content.library.LibraryService;
import com.novamens.content.model.Classifier;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.model.ClassifierBC;
import kbee.web.model.ClassifiersBC;
import kbee.web.nav.DropDownMenuBC;
import kbee.web.nav.LibrariesBC;
import kbee.web.nav.SeparatorBC;

public class LibraryDropDownBC extends DropDownMenuBC<Void> {
			
	private static final long serialVersionUID = 1L;
	
	public LibraryDropDownBC() {
		addElement(new LibrariesBC(), true);
		addElement(new LibrariesBC());
		addElement(new SeparatorBC());
		
		for (Library library : getDomain().getService(LibraryService.class).getLibraries(ObjectState.ENABLED, "listOrder")) {
			addElement( new LibraryBC(new ObjectModel<Library> (library)));
		}
	}

	//
	// for (Library t:getContentDao().getLibraries(getDomain())) {
	//
	
	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}

	protected ContentDao getContentDao() {
		return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}

}
