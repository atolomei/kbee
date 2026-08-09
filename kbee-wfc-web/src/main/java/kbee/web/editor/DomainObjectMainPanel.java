package kbee.web.editor;


import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.wicket.model.IModel;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.entity.Person;
import com.novamens.content.properties.Property;
import com.novamens.content.user.UserService;

import com.novamens.dom.Domain;
import com.novamens.dom.DomainObject;
import com.novamens.dom.DomainType;
import com.novamens.kbee.content.user.UserPropertyService;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.security.User;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.user.PreferencesService;
				
public class DomainObjectMainPanel<T extends DomainObject> extends ModelPanel<T> {
			
	private static final long serialVersionUID = 1L;

	//private int initial_tab = 0;
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(DomainObjectMainPanel.class.getName());

	IModel<T> model;
	private Boolean is_domain_kbee = null;
	
	
	
	public DomainObjectMainPanel(String id, IModel<T> model) {
		super(id);
		setModel(model);
	}
	
	
	//public void setInitialTab(int n) {
	//	this.initial_tab=n;
	//}
	
	//public int getInitialTab() {
	//	return initial_tab;
	//}
	
	public IModel<T> getModel() {
		return model;
	}
	public void setModel(IModel<T> model) {
		this.model = model;
	}

	public void onDetach() {
		super.onDetach();
		if (this.model!=null)
			this.model.detach();
	}
	
	
	protected boolean isRoot() {
		return ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot(getSessionUser());
	}
	
	protected User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	
	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	
	protected boolean isDomainKbee() {
		if (this.is_domain_kbee == null) {
			try {
				this.is_domain_kbee = Boolean.valueOf(getPerson().getDomain().getName().toLowerCase().trim().equals("kbee"));
			} 
			catch (Exception e) {
				this.is_domain_kbee = new Boolean (false);
			}
		}
		return this.is_domain_kbee.booleanValue();
	}
	
	protected Person getPerson() {
		return ServiceLocator.getService(UserService.class).getSessionUserProfile().getPerson();
	}

	
	//protected boolean isFreeVersion() {
	//		return getDomain().getDomainType()==DomainType.FREE;
	//}
	
	protected Domain getDomain() {
		return (Domain)ServiceLocator.getService(UserService.class).getDomain();
	}

	protected boolean isAdminSessionUser() {
		return ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	}
	
	 
	protected boolean isSupportSessionUser() {
		return ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	}

	
	protected void setUserPreference(String key, String value) {
		KbeeUser user = (KbeeUser) getSessionUser();
		if (user!=null)
			user.getService(PreferencesService.class).setValue(this.getClass().getSimpleName(), key, value);
	}	

	
	
	protected void saveUserHistory(String key, String text) {
		
		if (text==null)
			return;
		
		try {
			List<Property> list = ((KbeeUser) getSessionUser()).getService(UserPropertyService.class).getPropertiesSet(key, 30);
			for (Property p:list) {
					if (p.getValue().toString().toLowerCase().trim().equals(text.toLowerCase().trim()))
							return;
			}
			DateTimeFormatter dt= DateTimeFormatter.ISO_DATE_TIME;
			((KbeeUser) getSessionUser()).getService(UserPropertyService.class).setProperty(key+"-"+dt.format(OffsetDateTime.now()), key, text);
		} catch (Exception e) {
			logger.error(e);
		}
	}
	
	
}
