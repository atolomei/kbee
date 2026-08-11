package com.novamens.content.service;


import java.util.List;
import java.util.Set;

import com.novamens.content.base.ConstraintException;
import com.novamens.content.base.ContentCreationException;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.base.SiteIQLRule;
import com.novamens.content.entity.Person;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.UserSet;
import com.novamens.content.security.IQLRule;
import com.novamens.content.security.Role;
import com.novamens.content.user.UserProfile;
import com.novamens.dom.Domain;
import com.novamens.security.User;
import com.novamens.security.acl.Acl;
import com.novamens.security.acl.Group;
import com.novamens.service.BusinessSystemService;
import com.novamens.service.SystemSecurityService;

/**
 * 
 * 
 * <p>High level Security Service.
 *
 * 
 * It can manage Domain and other Content Management Objects.
 * The other, low level, Security Service {@link SecurityService}
 * manages just {@link Users} and {@link Group} at a lower level.  
 *</p>
 *
 */
public interface SecurityContentMgmtService extends BusinessSystemService, SystemSecurityService {
	
	public Group createGroup() throws ContentCreationException;
	public Group createGroup(String name, Domain domain, User caller, boolean isCanonical, String areacode) throws ContentCreationException;
	public Group createGroup(String name, Domain domain, User caller, boolean isCanonical, boolean isPortalOnly, String areacode) throws ContentCreationException;
	
	public Person createUser(UserSet dataSet) throws ContentCreationException, ContentMgmtException;
	
	public IQLRule createRule(int type) throws ContentCreationException;
	public IQLRule createRule(int type, Domain domain) throws ContentCreationException;
	public IQLRule createRule(int type, Domain domain, User basicUser) throws ContentCreationException, ContentMgmtException;
	
	public void enable(User user);
	public void disable(User user);
	
	public List<Group> getRootsGroups();
	public List<Group> getDefaultGroups(Domain domain);
	
	public void update(IQLRule rule, List<String> updatedParts) throws ContentMgmtException;
	
	public void update(User user, List<String> updatedParts) throws ContentMgmtException;
	public void update(User user) throws ContentMgmtException;
	
	public void update(Group group) throws ContentMgmtException; 	
	public void update(Acl acl) throws ContentMgmtException; 	
	public void update(UserProfile user, List<String> updatedParts) throws ContentMgmtException;
	public void update(Person person, List<String> updatedParts) throws ContentMgmtException;
	
	public void delete(Group group) throws ContentMgmtException;
	public void delete(IQLRule group) throws ContentMgmtException;
	public void delete(Person group) throws ContentMgmtException, ConstraintException;
	
	public List<SiteIQLRule> findRuleByRelatedObjectId(String id);
	public void update(Person person, String string) throws ContentMgmtException;
	
	public void markAsDeleted(Person person) throws ContentMgmtException;
	public void restore(Person person) throws ContentMgmtException;
	public void disable(Person object) throws ContentMgmtException;
	
	// Role
	public Role createRole(int type) throws ContentCreationException;
	public Role createRole(int type, Domain domain) throws ContentCreationException;
	public Role createRoleNoTrx(int type, Domain domain) throws ContentCreationException;
									
	public Role createRole(int type, Classifier clasi) throws ContentCreationException;
	public Role createRole(int type, Domain domain, Classifier clasi) throws ContentCreationException;
	public Role createRoleNoTrx(int type, Domain domain, Classifier clasi) throws ContentCreationException;
	
	public void update(Role role, String part) throws ContentMgmtException;
	public void updateNoTrx(Role role, String part) throws ContentMgmtException;;
	
	public void update(Role role, List<String> updatedParts) throws ContentMgmtException;
	public void delete(Role role) throws ContentMgmtException;
	
	public void startApplicationServer();
	public void startApplicationServer(long milisecs);
	public void enable(Person person);

	public Set<String> getReservedUserNames();
}
