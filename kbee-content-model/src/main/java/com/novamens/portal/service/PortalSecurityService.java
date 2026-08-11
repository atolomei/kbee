package com.novamens.portal.service;

import java.io.IOException;
import java.util.List;

import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.model.ExternalMember;
import com.novamens.dom.Domain;
import com.novamens.portal6.model.PortalService;
import com.novamens.portal6.model.Site;
import com.novamens.portal6.model.SiteTemplate;
import com.novamens.portal6.model.SiteType;
import com.novamens.security.acl.Group;
import com.novamens.service.BusinessSystemService;
import com.novamens.service.SystemSecurityService;

/**
 * 
 * 
 * kbee-content-classes
 *
 */
public interface PortalSecurityService extends BusinessSystemService,  PortalService, SystemSecurityService {

	public List<SiteType> getSiteTypesCreateSessionUser();
	
	public boolean isPortalAdminSessionUser();

	public boolean isReadSiteSessionUser(Site site);
	public boolean isWriteSiteSessionUser(Site site);
	public boolean isAdminSiteSessionUser(Site site);

	
	/**
	 * Users from groups: Domain Admin, Porta Admin
	 * are enabled to create external Sites.
	 */
	public boolean isCreateExternalSessionUser();

	
	/**
	 * Users from groups: Domain Admin, Porta Admin
	 * are enabled to create Sites.
	 */
	public boolean isCreateIntranetSiteSessionUser();
	public boolean isCreateSiteSessionUser(SiteType site_type);

	public void addSiteGroupsIfNotExists(Site site);
	public void createCanonicalGroupsIfNotExist(Domain domain) throws IOException;
	public List<Group> addSiteGroupsIfNotExists(Site site, boolean force);
	
	public void addSiteGroupsRulesIfNotExist(Site site, ExternalMember member_site, ExternalMember member_repo, boolean force) throws ContentMgmtException;
	
	public String getAdminGroupStr(Site site);
	public String getReadGroupStr(Site site);
	public String getWriteGroupStr(Site site);

	public Group getAdminGroup(Site site);
	public Group getWriteGroup(Site site);
	public Group getReadGroup(Site site);

	

}
