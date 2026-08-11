package com.novamens.content.service;

import java.util.List;

import com.novamens.content.base.ContentCreationException;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.security.Role;
import com.novamens.dom.Domain;
import com.novamens.dom.DomainType;
import com.novamens.dom.Json;
import com.novamens.security.acl.Group;
import com.novamens.service.FactoryService;
import com.novamens.service.SystemService;
import com.novamens.system.SystemParameter;

/**
 *   {@link Domain} Factory 
 *
 */
public interface DomainLifeCycleService extends SystemService, FactoryService {

	

	/**
	 * New Domains must have a Root user and an Admin User.
	 * 
	 * Admin user is for the client
	 * Root user is for internal use.
	 * 
	 * @param name
	 * @param organization
	 * @param type
	 * @param root_password
	 * @param root_email
	 * @param noreply_email
	 * @param admin_username
	 * @param admin_fisrtname
	 * @param admin_lastname
	 * @param admin_email
	 * @return
	 * @throws ContentMgmtException
	 * @throws ContentCreationException
	 */
	
	public Domain createDomain(		String name, 
									String organization, 
									DomainType type,
									boolean isAPI,
									String root_password, 
									String root_email, 
									String noreply_email,
									String admin_username,
									String admin_fisrtname,
									String admin_lastname,
									String admin_email) throws ContentMgmtException, ContentCreationException;
	
	
	
	public void setUpBasicDomainTemplate(Domain domain, boolean createmembers) 		throws ContentMgmtException; // Basic API
	public void setUpEnterpriseDomainTemplate(Domain domain) 						throws ContentMgmtException; // Premium Standard NO API
	public void setUpComplianceDomainTemplate(Domain domain) 						throws ContentMgmtException; // Premium Compliance  NO API 
	public void setUpAssignDomainTemplate(Domain domain) 							throws ContentMgmtException; // Premium Assign NO API
							
	 
	// Returns true if it needs reindexing
	//
	public boolean  kbeeDomainStartUp() 		throws ContentMgmtException;
	public void 	kbeeDomainInitRoot() 		throws ContentMgmtException;

	
	public Domain setDefaultSettings(Domain domain, Json json) throws ContentMgmtException, ContentCreationException;
	public Domain setDefaultSecurityModel(Domain domain, String root_password, String root_email, String admin_username, String admin_fisrtname, String admin_lastname, String admin_email) throws ContentCreationException, ContentMgmtException;
	
	public Domain createCabinetsIfNotExists(Domain domain) throws ContentMgmtException, ContentCreationException;
	public Domain createCanonicalGroupsIfNotExists(Domain domain) throws ContentMgmtException, ContentCreationException;

 
	/**
	 * Physically deletes all binary files for this domain.
	 * 
	 * The Domain must be in state ObjectState.DELETE 
	 * which is a logical delete.
	 * 
	 * @param domain
	 * @throws DataManagementException 
	 */
	public void deleteAllResources(Domain domain) throws DataManagementException;
	

	
	
	/**
	 * Physically destroys the domain.
	 * Including the binary files in the File System.
	 * 
	 * The Domain must be in state ObjectState.DELETE 
	 * which is a logical delete.
	 * 
	 * @param domain
	 * @throws DataManagementException 
	 */
	
	public void wipe(Domain domain) throws ContentMgmtException;

	public void saveSystemParameter(SystemParameter pa) throws ContentMgmtException;
	public void deleteSystemParameter(SystemParameter pa) throws ContentMgmtException;


	
	/**
	 * Roles
	 * 
	 * @param domain
	 * @throws ContentMgmtException
	 * @throws ContentCreationException
	 */														
	public List<Role> createCanonicalRolesIfNotExists(Domain domain, List<Group> canonical_groups) 	throws ContentMgmtException, ContentCreationException;
	public List<Role> createCanonicalRolesIfNotExistsNoTrx(Domain domain)	throws ContentMgmtException, ContentCreationException;


	public List<Role> addEntityRolesIfNotExists(Domain domain, List<Group> canonical_groups)	throws ContentMgmtException, ContentCreationException;
	public List<Role> addEntityRolesIfNotExistsNoTrx(Domain domain) 	throws ContentMgmtException, ContentCreationException;

	
}
