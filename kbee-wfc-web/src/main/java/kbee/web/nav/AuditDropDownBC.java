package kbee.web.nav;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.user.UserService;

import com.novamens.dom.Domain;
import com.novamens.service.ServiceLocator;

public class AuditDropDownBC extends DropDownMenuBC<Void> {
	
	private static final long serialVersionUID = 1L;

	public AuditDropDownBC() {
		addElement(new AuditBC());
	}

	public void onDetach() {
		super.onDetach();
	}

	protected ContentDao getContentDao() {
		return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
//	@SuppressWarnings("unused")
//	private Domain getDomain() {
//		return ServiceLocator.getService(UserService.class).getDomain();
//	}

}
