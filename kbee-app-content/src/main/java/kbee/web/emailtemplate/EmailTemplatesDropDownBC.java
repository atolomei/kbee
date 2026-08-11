package kbee.web.emailtemplate;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.email.EmailTemplate;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.nav.DropDownMenuBC;
import kbee.web.nav.EmailTemplatesBC;
import kbee.web.nav.SeparatorBC;

/**
 *
 */
public class EmailTemplatesDropDownBC extends DropDownMenuBC<Void> {
	
	private static final long serialVersionUID = 1L;
	
	public EmailTemplatesDropDownBC() {
		
		addElement(new EmailTemplatesBC(), true);
		addElement(new EmailTemplatesBC());
		addElement(new SeparatorBC());
		for (EmailTemplate t:getContentDao().getEmailTemplates(getDomain(), "en")) {
			if (t.getState()!=ObjectState.DRAFT)
				addElement( new EmailTemplateBC(new ObjectModel<EmailTemplate> (t)));
		}
	}

	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}

	protected ContentDao getContentDao() {
		return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
}
