package com.novamens.service;

import java.io.Serializable;
import java.util.List;

import com.novamens.security.AuthToken;
import com.novamens.security.Principal;
import com.novamens.security.User;
import com.novamens.security.acl.Group;


/**
 * <p>Low level Security Service. At this level neither {@link Content}
 * nor {@Domain} are visible.</p>
 */
public interface SecurityService extends SystemService, SystemSecurityService {
	
	public boolean hasPermissions(User user, String page);
	
	/**
	 * Current Session User
	 */
	public User getSessionUser();
	
	public boolean isMember(User user, String goupname);
	public boolean isMember(String goupname);


	/**
	 * Current Session User is Root 
	 */
	public boolean isRoot();
	public boolean isRoot(User user);
	
	public User findUserById(Serializable id);
	public Group findGroupById(Serializable id);
			
	public Principal findPrincipalById(Serializable id);
	
	public List<Group> findGroupByName(String name, String domain_id);
	public User findUserByUsername(String username);
				
	public void authenticate(String user, String password);
	public void authenticate(String user);
	
	public AuthToken createToken(String user, String password);

	public boolean validateName(String username);

	/**
	 * Is User Active for Workflow Assign 
	 */
	public boolean isActive(User user);
	public void setActive(User user);
	public void setInActive(User user);
	
	/**
	 * Autenticación local: impersonate, comando, api, etc 
	 */
	public boolean isLocal();
	public Object getAuthentication();

	/**
	 * Temporary Session Token for Password reset 
	 */
	public String nextSecureToken();
	public void addToken(User user, String token);
	public String getUserId(String token);
	public Serializable getId(String token);
	public String getParameter(String token);
	public boolean isValid(String token);
	public void removeToken(String token);
	public void addToken(User user, String token, int duration_minutes);
	public void addToken(Serializable id, String token, int duration_minutes);
	public void addToken(User user, String token, String parameter, int duration_minutes);
	public int getTokenDBSize();

	/**
	 * 
	 * This is a method to be used by internal checks.
	 * Normally we must use {@link isMember}
	 * 
	 * This method is to be used only when we need to know if the user effectively has the group.
	 * 
	 * @param user
	 * @param groupname
	 */
	public boolean hasGroup(User user, String groupname);
	public void exit(int status, long maxDelayMillis);
	public void exit(int status);
	public int getTotalActiveUsers();
	public void registerAccessPermissions(String page, Long permissions);

	public List<Principal> getDomainAdminUsers(String domain_id);
	public List<Principal> getDomainSupportUsers(String domain_id);
}
