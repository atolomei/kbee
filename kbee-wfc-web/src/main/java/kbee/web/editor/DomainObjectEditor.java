package kbee.web.editor;


import java.time.ZoneId;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.dao.PortalDao;
import com.novamens.content.entity.Person;
import com.novamens.content.security.ContentSecurityDao;
import com.novamens.content.user.UserService;
import com.novamens.dao.SecurityDao;
import com.novamens.dom.Domain;
import com.novamens.dom.DomainObject;
import com.novamens.dom.DomainType;
import com.novamens.repository.DomRepository;
import com.novamens.repository.DomRepositoryService;
import com.novamens.security.User;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.LanguageService;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
			
public class DomainObjectEditor<T extends DomainObject> extends ObjectEditor<T>  {
	private static final long serialVersionUID = 1L;
	 
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(DomainObjectEditor.class.getName());
	
	private Boolean is_domain_kbee = null;

	public DomainObjectEditor(String id) {
		super(id);
	}
	
	public DomainObjectEditor(String id, IModel<T> model) {
		super(id, model);
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
	
	protected PortalDao getPortalDao() {
		return (PortalDao)ServiceLocator.getService(BeansService.class).getBean("portalDao");
	}
	
	protected SecurityDao getSecurityDao() {
		return (SecurityDao)ServiceLocator.getService(BeansService.class).getBean("securityDao");
	}
	
	protected ContentSecurityDao getContentSecurityDao() {
		return (ContentSecurityDao)ServiceLocator.getService(BeansService.class).getBean("contentSecurityDao");
	}
	
	protected boolean isExpressVersion() {
		try {
		return getDomain().getDomainType()==DomainType.EXPRESS;
		} catch (Exception e) {
			logger.error(e);
			return false;
		}
	}
			
	protected boolean isDomainKbee() {
		if (this.is_domain_kbee == null) {
			try {
				this.is_domain_kbee = Boolean.valueOf(getPerson().getDomain().getName().toLowerCase().trim().equals("kbee"));
			} 
			catch (Exception e) {
				this.is_domain_kbee = Boolean.valueOf(false);
			}
		}
		return this.is_domain_kbee.booleanValue();
	}
	
	protected Person getPerson() {
		return ServiceLocator.getService(UserService.class).getSessionUserProfile().getPerson();
	}
	
	
	protected Domain getDomain() {
		return (Domain)ServiceLocator.getService(UserService.class).getDomain();
	}
	
	protected ZoneId getDomainZoneId() {
		return ZoneId.of(getDomain().getTimeZone());
	}

	protected boolean isFactoryAdminSessionUser() {
		return ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_FACTORY_MANAGER.getId());
	}
		
	protected boolean isServiceAdminSessionUser() {
		return ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SERVICE_ADMIN.getId());
	}
	
	public IModel<String> getText(String key) {
		return new StringResourceModel(key, this, null);
	}

	protected boolean isAdminSessionUser() {
		return ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	}
	
	protected boolean isSupportSessionUser() {
		return ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	}
	
	
	protected String parseAlias(String s) {
		if (s==null)
			return null;
		return parsePredicate(s).toLowerCase().trim();
	}
	
	
	protected String parsePredicate(String s) {
		
		if (s==null)
			return null;
		
		String a0 = s.toLowerCase().replace("ñ", "enie").replace(" de ", ""); 
		String a1 = StringUtils.stripAccents(ServiceLocator.getService(LanguageService.class).removeStopWords(a0, getDomain().getLocale()));
		String a2=WordUtils.capitalizeFully(a1).replaceAll("[ |\\t|\\s|(|)]", "");
		return a2.trim();
	}
	
	protected <R> DomRepository<R> getRepository(Class<R> objectclass) {
		DomRepository<R> repository = ServiceLocator.getService(DomRepositoryService.class).getRepository(objectclass);
		return repository;
	}
	
}
