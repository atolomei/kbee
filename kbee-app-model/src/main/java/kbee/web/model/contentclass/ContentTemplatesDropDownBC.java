package kbee.web.model.contentclass;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.model.procedure.ContentTemplateBC;
import kbee.web.nav.DropDownMenuBC;
import kbee.web.nav.SeparatorBC;

public class ContentTemplatesDropDownBC extends DropDownMenuBC<Void> {
			
	private static final long serialVersionUID = 1L;

	public ContentTemplatesDropDownBC() {
		addElement(new ContentClassesBC(), true);
		addElement(new ContentClassesBC());
		addElement(new SeparatorBC());
		for (ContentTemplate t:getContentDao().getTemplates( getDomain())) {
			if ( t.getState()!=ObjectState.DELETED) {
			
				
				addElement( new ContentTemplateBC(new ObjectModel<ContentTemplate> (t)));
			}
		}
	}

	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}

	protected ContentDao getContentDao() {
		return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}

		 
}
