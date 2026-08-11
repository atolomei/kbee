package com.novamens.kbee.portal.service;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.novamens.beans.BeansService;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.base.SiteIQLRule;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ExternalDao;
import com.novamens.content.model.ExternalMember;
import com.novamens.content.security.IQLRule;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.event.Event;
import com.novamens.event.EventListener;
import com.novamens.kbee.content.security.KbeeSiteSecurityRule;
import com.novamens.kbee.event.EvictCacheServiceEvent;
import com.novamens.kbee.security.acl.KbeeAcl;
import com.novamens.kbee.security.acl.KbeeAclEntry;
import com.novamens.kbee.security.acl.KbeeGroup;
import com.novamens.kbee.security.acl.KbeePermission;
import com.novamens.portal.service.PortalSecurityService;
import com.novamens.portal6.model.Site;
import com.novamens.portal6.model.SiteService;
import com.novamens.portal6.model.SiteTemplate;
import com.novamens.portal6.model.SiteType;
import com.novamens.security.User;
import com.novamens.security.acl.Acl;
import com.novamens.security.acl.AclEntry;
import com.novamens.security.acl.Group;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.security.acl.Permission;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

import kbee.util.PropertiesFactory;

public class KbeePortalSecurityService implements PortalSecurityService, EventListener {

	private static final String PREFIX = PropertiesFactory.getInstance("kbee").getProperties().getProperty("com.novamens.kbee.portal.site.group.prefix", "GRPIntra");

	static private Logger logger = LogManager.getLogger(KbeePortalSecurityService.class.getName());

	private static final String ROLE_PORTAL_ADMIN = KbeeGlobalRole.PORTAL_ADMIN.getId();
	private static final String ROLE_DOMAIN_ADMIN = KbeeGlobalRole.DOMAIN_ADMIN.getId();

	private SecurityService secu;

	private List<SiteType> st_all;
	private List<SiteType> st_admin;
	private List<SiteType> st_std;

	// Cache para consulta de un usuario x site
	//
	// TODO: HA
	//
	private Map<String, SiteUserRights> rights_user_cache = new ConcurrentHashMap<String, SiteUserRights>();

	// Para invalidar el cache de todos lo usuarios de un site
	// al invalidar un Site se recorre con iterador toda la lista y se borra
	//
	// TODO: HA
	//
	private Map<String, List<String>> rights_site_cache = new ConcurrentHashMap<String, List<String>>();

	/**
	 * Los Groups se editan en el Site. es decir que se puede invalidar el caché del
	 * Site unicamente. Ordenados por Site, pero tiene que estar por User porqe la consulta es por user.
	 */
	private class SiteUserRights {

		public SiteUserRights(String userid, String siteid, boolean admin, boolean write, boolean read) {

			this.read = read;
			this.write = write;
			this.admin = admin;
			this.site_id = siteid;
			this.user_id = userid;
		}

		public boolean read;
		public boolean write;
		public boolean admin;

		public String site_id;
		public String user_id;

		public String getKey() {
			return user_id + site_id;
		}
	}

	@Override
	public void createCanonicalGroupsIfNotExist(Domain domain) throws IOException {

		if (ServiceLocator.getService(com.novamens.service.SecurityService.class)
				.findGroupByName(ROLE_PORTAL_ADMIN, domain.getId().toString()).isEmpty()) {
			User user = getSessionUser();
			KbeeGroup portal_admin = new KbeeGroup();
			portal_admin.setLastModifiedUser(user);
			portal_admin.setName(ROLE_PORTAL_ADMIN);
			portal_admin.setCanonical(true);
			portal_admin.setDomain(domain);
			portal_admin.setLastModifiedOffsetDateTime(OffsetDateTime.now());
			try {
				ServiceLocator.getService(com.novamens.content.service.SecurityContentMgmtService.class)
						.update(portal_admin);
				logger.info("Created Group " + ROLE_PORTAL_ADMIN);
			} catch (Exception e) {
				logger.error(e);
			}
		}

		if (ServiceLocator.getService(SecurityService.class)
				.findGroupByName(ROLE_DOMAIN_ADMIN, domain.getId().toString()).isEmpty()) {
			User user = getSessionUser();
			KbeeGroup d_admin = new KbeeGroup();
			d_admin.setLastModifiedUser(user);
			d_admin.setName(ROLE_DOMAIN_ADMIN);
			d_admin.setCanonical(true);
			d_admin.setDomain(domain);
			d_admin.setLastModifiedOffsetDateTime(OffsetDateTime.now());

			try {
				ServiceLocator.getService(com.novamens.content.service.SecurityContentMgmtService.class)
						.update(d_admin);
				logger.info("Created Group " + ROLE_DOMAIN_ADMIN);

			} catch (Exception e) {
				logger.error(e);
			}
		}
	}

	@Override
	public boolean isCreateExternalSessionUser() {
		return getSecurityService().isMember(ROLE_DOMAIN_ADMIN) || getSecurityService().isMember(ROLE_PORTAL_ADMIN);
	}

	@Override
	public boolean isCreateIntranetSiteSessionUser() {
		if (getSecurityService().isMember(ROLE_DOMAIN_ADMIN) || getSecurityService().isMember(ROLE_PORTAL_ADMIN))
			return true;
		return false;
	}

	@Override
	public boolean isCreateSiteSessionUser(SiteType site_type) {

		if (getSecurityService().isMember(ROLE_DOMAIN_ADMIN) || getSecurityService().isMember(ROLE_PORTAL_ADMIN))
			return true;

		boolean b_value;

		switch (site_type) {

		
		case HOME: {
			b_value = false;
			break;
		}
		default: {
			b_value = false;
		}
		}
		;
		return b_value;
	}

	@Override
	public List<SiteType> getSiteTypesCreateSessionUser() {

		if (getSecurityService().isMember(ROLE_DOMAIN_ADMIN))
			return getStAll();

		if (getSecurityService().isMember(ROLE_PORTAL_ADMIN))
			return getStAdmin();

		return getStStd();
	}

	List<SiteTemplate> site_templates = null;

	

	@Override
	public boolean isPortalAdminSessionUser() {
		if (getSecurityService().isMember(ROLE_DOMAIN_ADMIN) || getSecurityService().isMember(ROLE_PORTAL_ADMIN))
			return true;
		return false;
	}

	public void evict() {
		if (rights_user_cache!=null)
		rights_user_cache.clear();
	}
	
	/**
	 */
	public void invalidateCache(Site site) {
		
		List<String> list = rights_site_cache.get(site.getOId().toString());
		
		if (list != null) {
			for (String str : list)
				rights_user_cache.remove(str);
			list.clear();
		}
	}

	@Override
	public boolean isReadSiteSessionUser(Site site) {
		User user = getSessionUser();
		if (user == null)
			return false;
		String key = user.getId().toString() + site.getOId().toString();
		if (this.rights_user_cache.containsKey(key))
			return this.rights_user_cache.get(key).read;
		SiteUserRights ur = addToCache(key, user, site);
		return ur.read;
	}

	@Override
	public boolean isWriteSiteSessionUser(Site site) {
		User user = getSessionUser();
		String key = user.getId().toString() + site.getOId().toString();
		if (this.rights_user_cache.containsKey(key))
			return this.rights_user_cache.get(key).write;

		SiteUserRights ur = addToCache(key, user, site);
		return ur.write;
	}

	/**
	 * 
	 * Diagramar, Archivar, Borrar
	 * 
	 * @param site
	 * @return
	 */
	@Override
	public boolean isAdminSiteSessionUser(Site site) {
		User user = getSessionUser();
		String key = user.getId().toString() + site.getOId().toString();
		if (this.rights_user_cache.containsKey(key))
			return this.rights_user_cache.get(key).admin;
		SiteUserRights ur = addToCache(key, user, site);
		return ur.admin;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void addSiteGroupsRulesIfNotExist(Site site, ExternalMember site_member, ExternalMember repo_member,
			boolean force) throws ContentMgmtException {

		List<Group> groups = addSiteGroupsIfNotExists(site, force);

		List<SiteIQLRule> list = ServiceLocator
				.getService(com.novamens.content.service.SecurityContentMgmtService.class)
				.findRuleByRelatedObjectId(site.getOId().toString());

		UserProfile userProfile = ServiceLocator.getService(UserService.class).getSessionUserProfile();

		if (userProfile == null) {
			logger.error("Caller user is null.");
			return;
		}

		if (site.getDomain() == null) {
			logger.error("Site Domain is null.");
			return;
		}

		if (groups == null || groups.size() < 3) {
			logger.error("Groups are null or less than 3.");
			return;
		}

		KbeeGroup gwrite = null;
		KbeeGroup gread = null;
		KbeeGroup gadmin = null;

		gadmin = (KbeeGroup) groups.get(0);
		gwrite = (KbeeGroup) groups.get(2);
		gread = (KbeeGroup) groups.get(1);

		if (gadmin == null || gwrite == null || gread == null) {
			logger.error("Groups are null.");
			return;
		}

		if (site_member == null) {
			logger.error("DataSetMember Site External is null.");
			return;
		}

		if (repo_member == null) {
			logger.error("DataSet Site Repo External is null.");
			return;
		}

		Domain domain = userProfile.getDomain();

		if (domain == null)
			throw new ContentMgmtException("Domain is null");

		Classifier xsite_repo_clasi = getExternalDao().getSiteRepositoryClassifier(domain);
		Classifier xsite_clasi = getExternalDao().getSiteClassifier(domain);

		if (xsite_repo_clasi == null)
			throw new ContentMgmtException("Site Repository Classifier is null");

		if (xsite_clasi == null)
			throw new ContentMgmtException("Site Classifier is null");

		String repo_condition = xsite_repo_clasi.getPredicate() + "(" + repo_member.getId().toString() + ")";
		String site_condition = xsite_clasi.getPredicate() + "(" + site_member.getId().toString() + ")";

		boolean bsite_rule = false;
		boolean bsite_repo_rule = false;

		if (list != null) {
			for (SiteIQLRule rule : list) {
				if (rule.getCondition().equals(site_condition)) {
					bsite_rule = true;
				} else if (rule.getCondition().equals(repo_condition)) {
					bsite_repo_rule = true;
				}

				if (bsite_rule && bsite_repo_rule)
					return;
			}
		}

		User caller = userProfile.getUser();

		if (!bsite_rule) {

			KbeeSiteSecurityRule site_rule = new KbeeSiteSecurityRule();
			site_rule.setType(IQLRule.RULE_WIZARD_IQL);
			site_rule.setLastModifiedUser(caller);
			site_rule.setLastModifiedOffsetDateTime(OffsetDateTime.now());
			site_rule.setDomain(getDomain());

			site_rule.setName("Site - " + site.getOId().toString());
			site_rule.setDescription(site.getTitle());
			site_rule.setRelatedObjectId(site.getOId().toString());

			site_rule.setCondition(site_condition);

			Acl acl = new KbeeAcl();

			try {
				acl.setName(caller, "Site - " + site.getOId().toString() + " ACL");
				AclEntry entry_r = new KbeeAclEntry(acl, gread, false);
				List<Permission> permissions_r = new ArrayList<Permission>();
				permissions_r.add(KbeePermission.READ);
				entry_r.setPermissions(permissions_r);
				acl.addEntry(caller, entry_r);

				AclEntry entry_w = new KbeeAclEntry(acl, gwrite, false);
				List<Permission> permissions_w = new ArrayList<Permission>();
				permissions_w.add(KbeePermission.WRITE);
				permissions_w.add(KbeePermission.DELETE);
				permissions_w.add(KbeePermission.READ);
				entry_w.setPermissions(permissions_w);
				acl.addEntry(caller, entry_w);

				AclEntry entry_a = new KbeeAclEntry(acl, gadmin, false);
				List<Permission> permissions_a = new ArrayList<Permission>();
				permissions_a.add(KbeePermission.WRITE);
				permissions_a.add(KbeePermission.READ);
				permissions_a.add(KbeePermission.DELETE);
				permissions_a.add(KbeePermission.MONITOR);
				entry_a.setPermissions(permissions_a);
				acl.addEntry(caller, entry_a);

				site_rule.setAcl(acl);

				List<String> updatedParts = new ArrayList<String>();
				updatedParts.add("Create Site Rule " + site.getOId().toString());

				try {
					ServiceLocator.getService(com.novamens.content.service.SecurityContentMgmtService.class)
							.update(site_rule, updatedParts);
				} catch (Exception e) {
					logger.error(
							e.getClass().getName() + " | " + Thread.currentThread().getStackTrace()[1].getMethodName());
				}
			} catch (SecurityException e) {
				logger.error(e.getStackTrace());
			}
		}

		if (!bsite_repo_rule) {

			KbeeSiteSecurityRule site_repo_rule = new KbeeSiteSecurityRule();
			site_repo_rule.setType(IQLRule.RULE_WIZARD_IQL);
			site_repo_rule.setLastModifiedUser(caller);
			site_repo_rule.setLastModifiedOffsetDateTime(OffsetDateTime.now());
			site_repo_rule.setDomain(getDomain());

			site_repo_rule.setName("Site Repo - " + site.getOId().toString());
			site_repo_rule.setDescription("Site Repository " + site.getTitle());
			site_repo_rule.setRelatedObjectId(site.getOId().toString());
			site_repo_rule.setCondition(repo_condition);

			Acl acl = new KbeeAcl();

			try {

				acl.setName(caller, "Site Repo - " + site.getOId().toString() + " ACL");

				AclEntry entry_w = new KbeeAclEntry(acl, gwrite, false);
				List<Permission> permissions_w = new ArrayList<Permission>();
				permissions_w.add(KbeePermission.WRITE);
				permissions_w.add(KbeePermission.DELETE);
				permissions_w.add(KbeePermission.READ);
				entry_w.setPermissions(permissions_w);
				acl.addEntry(caller, entry_w);

				AclEntry entry_a = new KbeeAclEntry(acl, gadmin, false);
				List<Permission> permissions_a = new ArrayList<Permission>();
				permissions_a.add(KbeePermission.WRITE);
				permissions_a.add(KbeePermission.READ);
				permissions_a.add(KbeePermission.DELETE);
				permissions_a.add(KbeePermission.MONITOR);
				entry_a.setPermissions(permissions_a);
				acl.addEntry(caller, entry_a);

				site_repo_rule.setAcl(acl);

				List<String> updatedParts = new ArrayList<String>();
				updatedParts.add("Create Site Repo Rule " + site.getOId().toString());

				try {
					ServiceLocator.getService(com.novamens.content.service.SecurityContentMgmtService.class)
							.update(site_repo_rule, updatedParts);

				} catch (Exception e) {
					logger.error(
							e.getClass().getName() + " | " + Thread.currentThread().getStackTrace()[1].getMethodName());
				}
			} catch (SecurityException e) {
				logger.error(
						e.getClass().getName() + " | " + Thread.currentThread().getStackTrace()[1].getMethodName());
			}
		}

	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void addSiteGroupsIfNotExists(Site site) {
		addSiteGroupsIfNotExists(site, false);
	}

	/**
	 * @param site
	 * 
	 *             Each Site must have 3 Groups:
	 *
	 *             Read Write Admin
	 * 
	 *             And also a SecurityRule
	 * 
	 *             Logs all operations, with the exception of those that are
	 *             delegated to the SecurityService, which logs them.
	 * 
	 */

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public List<Group> addSiteGroupsIfNotExists(Site site, boolean force) {

		return null;
			
		/**
		UserProfile userProfile = ServiceLocator.getService(UserService.class).getSessionUserProfile();
		User caller = userProfile.getUser();

		boolean b_admin;
		boolean b_write;
		boolean b_read;

		KbeeGroup gwrite = null;
		KbeeGroup gread = null;
		KbeeGroup gadmin = null;
										
		String nadmin = site.getService(SiteService.class).getAdminGroupStr();
		String nwrite = site.getService(SiteService.class).getWriteGroupStr();
		String nread = site.getService(SiteService.class).getReadGroupStr();

		List<Group> list = new ArrayList<Group>();

		if (force) {
			b_admin = true;
			b_write = true;
			b_read = true;
		} else {
			List<Group> alist = getSecurityService().findGroupByName(nadmin, site.getDomain().getId().toString());	b_admin = (alist == null || alist.isEmpty());
			List<Group> wlist = getSecurityService().findGroupByName(nwrite, site.getDomain().getId().toString());	b_write = (wlist == null || wlist.isEmpty());
			List<Group> rlist = getSecurityService().findGroupByName(nread, site.getDomain().getId().toString());	b_read = (rlist == null || rlist.isEmpty());
		}

		//
		// Admin
		//
		try {
			if (b_admin) {
				gadmin = new KbeeGroup();
				gadmin.setLastModifiedUser(caller);
				gadmin.setName(nadmin);
				gadmin.setDomain(userProfile.getDomain());
				gadmin.setLastModifiedOffsetDateTime(OffsetDateTime.now());
				gadmin.setDescription(site.getTitle() + ". Admin");

				// Se agrega el Usuario al Grupo Admin
				//
				if (!ServiceLocator.getService(PortalSecurityService.class).isPortalAdminSessionUser())
					gadmin.addMember(caller);

				// SecurityService logs
				ServiceLocator.getService(com.novamens.content.service.SecurityContentMgmtService.class).update(gadmin);
				list.add(gadmin);
			}

			// Read
			//
			if (b_read) {
				gread = new KbeeGroup();
				gread.setLastModifiedUser(caller);
				gread.setName(nread);
				gread.setDomain(userProfile.getDomain());
				gread.setLastModifiedOffsetDateTime(OffsetDateTime.now());
				gadmin.setDescription(site.getTitle() + ". Read");

				// SecurityService logs
				ServiceLocator.getService(com.novamens.content.service.SecurityContentMgmtService.class).update(gread);
				list.add(gread);
			}

			// Write
			//
			if (b_write) {
				gwrite = new KbeeGroup();
				gwrite.setLastModifiedUser(caller);
				gwrite.setName(nwrite);
				gwrite.setDomain(userProfile.getDomain());
				gwrite.setLastModifiedOffsetDateTime(OffsetDateTime.now());
				gadmin.setDescription(site.getTitle() + ". Write");

				// SecurityService logs
				ServiceLocator.getService(com.novamens.content.service.SecurityContentMgmtService.class).update(gwrite);
				list.add(gwrite);
			}

			return list;

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		*/
	}

	@Override
	public String getAdminGroupStr(Site site) {
		return PREFIX + site.getOId().toString() + "Admin";
	}

	@Override
	public String getReadGroupStr(Site site) {
		return PREFIX + site.getOId().toString() + "Read";
	}

	@Override
	public String getWriteGroupStr(Site site) {
		return PREFIX + site.getOId().toString() + "Write";
	}

	@Override
	public Group getAdminGroup(Site site) {
		List<Group> gadmin = getSecurityService().findGroupByName(getAdminGroupStr(site),
				site.getDomain().getId().toString());
		if (gadmin != null && !gadmin.isEmpty())
			return gadmin.get(0);
		return null;
	}

	@Override
	public Group getWriteGroup(Site site) {
		List<Group> gwrite = getSecurityService().findGroupByName(getWriteGroupStr(site),
				site.getDomain().getId().toString());
		if (gwrite != null && !gwrite.isEmpty())
			return gwrite.get(0);
		return null;
	}

	@Override
	public Group getReadGroup(Site site) {
		List<Group> gread = getSecurityService().findGroupByName(getReadGroupStr(site),
				site.getDomain().getId().toString());
		if (gread != null && !gread.isEmpty())
			return gread.get(0);
		return null;
	}

	private SecurityService getSecurityService() {
		if (this.secu != null)
			return this.secu;
		this.secu = ServiceLocator.getService(SecurityService.class);
		return this.secu;
	}

	private List<SiteType> getStAll() {
		if (this.st_all != null)
			return this.st_all;
		initStList();
		return this.st_all;
	}

	private List<SiteType> getStAdmin() {
		if (this.st_admin != null)
			return this.st_admin;
		initStList();
		return this.st_admin;
	}

	private List<SiteType> getStStd() {
		if (this.st_std != null)
			return this.st_std;
		initStList();
		return this.st_std;
	}

	private User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}

	private void initStList() {

		this.st_all = new ArrayList<SiteType>();
		this.st_all.add(SiteType.HOME);
		this.st_admin = new ArrayList<SiteType>();
		this.st_admin.add(SiteType.HOME);
		this.st_std = new ArrayList<SiteType>();

	}

	private SiteUserRights addToCache(String key, User user,Site site) {

		return null;
		/**
		SiteUserRights sur;
		boolean bread = false;
		String sid = site.getOId().toString();
		if (getSecurityService().isMember(ROLE_DOMAIN_ADMIN) || getSecurityService().isMember(ROLE_PORTAL_ADMIN)) {
			sur = new SiteUserRights(user.getId().toString(), site.getOId().toString(), true, true, true);
			rights_user_cache.put(sur.getKey(), sur);
			if (rights_site_cache.get(sid) == null)
				rights_site_cache.put(sid, new ArrayList<String>());
			rights_site_cache.get(sid).add(sur.getKey());
			return sur;
		}

		// ADMIN ------------------------------------------------
		//
		//
		Group gadmin = site.getService(SiteService.class).getAdminGroup();
		if (gadmin != null && gadmin.isMember(user)) {
			sur = new SiteUserRights(user.getId().toString(), site.getOId().toString(), true, true, true);
			this.rights_user_cache.put(sur.getKey(), sur);
			if (this.rights_site_cache.get(sid) == null)
				this.rights_site_cache.put(sid, new ArrayList<String>());
			this.rights_site_cache.get(sid).add(sur.getKey());
			return sur;
		}

		// WRITE ------------------------------------------------
		//
		//
		Group gwrite = site.getService(SiteService.class).getWriteGroup();
		if (gwrite != null && gwrite.isMember(user)) {
			sur = new SiteUserRights(user.getId().toString(), site.getOId().toString(), false, true, true);
			this.rights_user_cache.put(sur.getKey(), sur);
			if (this.rights_site_cache.get(sid) == null)
				this.rights_site_cache.put(sid, new ArrayList<String>());
			this.rights_site_cache.get(sid).add(sur.getKey());
			return sur;
		}

		// READ ------------------------------------------------
		//
		//

		if (site.isPublic())
			bread = true;
		else {
			Group gread = site.getService(SiteService.class).getReadGroup();
			if (gread != null && gread.isMember(user))
				bread = true;
		}
		sur = new SiteUserRights(user.getId().toString(), site.getOId().toString(), false, false, bread);
		this.rights_user_cache.put(sur.getKey(), sur);
		if (this.rights_site_cache.get(sid) == null)
			this.rights_site_cache.put(sid, new ArrayList<String>());
		this.rights_site_cache.get(sid).add(sur.getKey());
		return sur;
		*/
	}

	private ExternalDao getExternalDao() {
		return (ExternalDao) ServiceLocator.getService(BeansService.class).getBean("externalDao");
	}

	@SuppressWarnings("unused")
	private String getSiteRepositoryId(Domain domain) {
		Classifier clasi = getExternalDao().getSiteRepositoryClassifier(domain);
		if (clasi != null)
			return clasi.getId().toString();
		return null;
	}

	@SuppressWarnings("unused")
	private String getSiteId(Domain domain) {
		Classifier clasi = getExternalDao().getSiteClassifier(domain);
		if (clasi != null)
			return clasi.getId().toString();
		return null;
	}

	private Domain getDomain() {
		try {
			return ServiceLocator.getService(UserService.class).getSessionUserProfile().getDomain();
		} catch (Exception e) {
			logger.error(e);
			return null;
		}
	}

	@Override
	public boolean listen(Event event) {
		if (event instanceof EvictCacheServiceEvent)
			return true;
		return false;
	}

	@Override
	public void onEvent(Event event) {
			if (event instanceof EvictCacheServiceEvent)
				evict();
	}
}
