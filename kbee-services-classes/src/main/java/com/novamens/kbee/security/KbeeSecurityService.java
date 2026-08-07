package com.novamens.kbee.security;

import java.io.Serializable;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.novamens.beans.BeansService;
import com.novamens.cache.SelfExpiringHashMap;
import com.novamens.dom.Domain;
import com.novamens.event.Event;
import com.novamens.event.EventListener;

import com.novamens.kbee.event.EvictCacheServiceEvent;
import com.novamens.security.AuthToken;
import com.novamens.security.Principal;
import com.novamens.security.User;
import com.novamens.security.acl.Group;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeRuntimeException;

/** 
 * <p>Servicio de Seguridad de bajo nivel. No conoce {@link Content} ni {@link Domain}</p>
 */
public class KbeeSecurityService implements SecurityService, EventListener {
	
	static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeSecurityService.class.getName());

	static private final int ONE_DAY_MINUTES = 24 * 60 * 1;
	static private final int ONE_DAY_MILISECONDS = 1 * 24 * 60 * 60 * 1000;
	static private final int ONE_HOUR_MILISECONDS = 1 * 60 * 60 * 1000;
	
	private class SecurityRecord {
		@SuppressWarnings("unused")
		public String token;
		public String parameter;
		public String user_id;
		public Serializable id;
		public Instant expiration;
	}
	
    private RSAPrivateKey privateKey;
	
	private KbeeSecurityDao userDao;
	private SecureRandom random = new SecureRandom();
	
	// TODO: HA
	// 
	private Map<String, User> active_users = Collections.synchronizedMap(new HashMap<String, User>());
	private String defaultUser;

	private Map<String, Long> permission_matrix = new HashMap<String, Long>();
									
	private Map<Serializable, Long> user_groups_bits = Collections.synchronizedMap(new HashMap<Serializable, Long>());
	
	private SelfExpiringHashMap<String, SecurityRecord> user_tokens = 
			new SelfExpiringHashMap<String, SecurityRecord>(ONE_DAY_MILISECONDS);
	
	private SelfExpiringHashMap<String, List<Principal>> domain_admins = 
			new SelfExpiringHashMap<String, List<Principal>>(ONE_HOUR_MILISECONDS);
	private SelfExpiringHashMap<String, List<Principal>> domain_support = 
			new SelfExpiringHashMap<String, List<Principal>>(ONE_HOUR_MILISECONDS);
	
	
	@Autowired
	@Qualifier("com.novamens.security.service.AuthenticationManager")
	protected AuthenticationManager authenticationManager;

	static private Set<String> reservednames = new HashSet<String>();
	
	static {
		
		reservednames.add("root");
		reservednames.add("suroot");
			
		reservednames.add("workflow");
		reservednames.add("suworkflow");
			
		reservednames.add("pending");
		reservednames.add("supending");
			
		reservednames.add("support1");
		reservednames.add("support2");
			
		reservednames.add("susupport1");
		reservednames.add("susupport1");
	}
	
	@Override
	public String getUserId(String token) {
		if (user_tokens.containsKey(token))
			return user_tokens.get(token).user_id;
		return null;
	}
	
	@Override
	public Serializable getId(String token) {
		if (user_tokens.containsKey(token))
			return user_tokens.get(token).id;
		return null;
	}
	
	@Override
	public String getParameter(String token) {
		if (user_tokens.containsKey(token))
			return user_tokens.get(token).parameter;
		return null;
	}

	
	@Override
	public boolean isValid(String token) {
		if (user_tokens.containsKey(token)) {
			if (user_tokens.get(token).expiration.compareTo(Instant.now())>0)
				return true;
			removeToken(token);
			return false;
		}
		return false;
	}
	
	public User getSessionUser() {
		try {
			if (SecurityContextHolder.getContext().getAuthentication()==null) 
				return null;
			
			Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			String username;
			
			if (principal instanceof UserDetails) {
				username = ((UserDetails)principal).getUsername();
			} 
			else {
				username = principal.toString();
			}
			
			User user = null;
			
			if (user == null) {
				user = getSecurityDao().findUserByName(username);
				if (user==null && getDefaultUser()!=null) {
					user = getSecurityDao().findUserByName(getDefaultUser());
				}
			}
			return user;
		} 
		catch (Exception e) {
			logger.error(e, "inside getSessionUser(). returns null");
			return null;
		}
	}
	
	public boolean isMember(String groupname) {
		return isMember(getSessionUser(), groupname);
	}

	public boolean isMember(User user, String groupname) {
		if (groupname==null)
			throw new IllegalArgumentException("groupname is null");
		
		if (user==null)
			return false;
		
		
		if (isRoot(user) && !groupname.equals("support"))
			return true;
		
		
		try {
			for (Group group: user.getGroups()) {
				if (group.getName().equals(groupname))
					return true;
			};
			return false;
			
		} 
		catch (org.hibernate.exception.GenericJDBCException e) {
			logger.error(e, "database hang ?");
			throw(e);
		} 
		catch (Exception e1) {
			logger.error(e1);
			throw(e1);
		}
	}

	@Override
	public boolean hasGroup(User user, String groupname) {
		for (Group group: user.getGroups()) {
			if (group.getName().equals(groupname))
				return true;
		};
		
		return false;
	}
	
	public void update(Principal principal) {
		throw new KbeeRuntimeException("not implemented.");
	}
	
	public AuthToken createToken(String user, String password) {
		
		UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(user, password);
		Authentication authentication = authenticationManager.authenticate(authRequest);
		SecurityContext securityContext = SecurityContextHolder.getContext();
		securityContext.setAuthentication(authentication);

        Date now = new Date();

        Date expiration = new Date(
        		System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 365 * 10
        );

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
        	.subject(authentication.getName())
            .issuer("kbee")
            .issueTime(now)
            .expirationTime(expiration)
            .claim("principal", user)
//            .claim("authorities",
//            	authentication.getAuthorities()
//                   	.stream()
//                       .map(a -> a.getAuthority())
//                       .collect(Collectors.toList()))
            .build();

        SignedJWT jwt = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256)
                        .keyID("kbee-internal")
                        .build(),
                claims
        );
        
        try {

        	jwt.sign(new RSASSASigner(privateKey));
        	KbeeAuthToken token = new KbeeAuthToken(ONE_DAY_MILISECONDS);
        	token.setTokenValue(jwt.serialize());
        	return token;
        }
        catch (Exception e) {
        	throw new RuntimeException(e);
        }
	}
	
	public void authenticate(String user, String password) {
		BeansService beans = ServiceLocator.getService(BeansService.class);
		AuthenticationManager authenticationManager = (AuthenticationManager)beans.getBean("com.novamens.security.service.AuthenticationManager");
		UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(user, password);
		Authentication authentication = authenticationManager.authenticate(authRequest);
		SecurityContext securityContext = SecurityContextHolder.getContext();
		securityContext.setAuthentication(authentication);
		getSessionUser();
	}

	public void authenticate(String user) {
		BeansService beans = ServiceLocator.getService(BeansService.class);
		AuthenticationManager authenticationManager = (AuthenticationManager)beans.getBean("com.novamens.security.service.AuthenticationManager");
		LocalAuthenticationToken authRequest = new LocalAuthenticationToken(user);
		Authentication authentication = authenticationManager.authenticate(authRequest);
		SecurityContext securityContext = SecurityContextHolder.getContext();
		securityContext.setAuthentication(new LocalAuthentication(authentication));
		getSessionUser();
		
	}
	
	public void logout(HttpServletRequest request, HttpServletResponse response) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		SecurityContextLogoutHandler h = new SecurityContextLogoutHandler();
		h.logout(request, response, auth);
	}
	
	
	@Override
	public Principal findPrincipalById(Serializable id) {
		 return getSecurityDao().findPrincipalById(id);
	}
	
	@Override
	public void registerAccessPermissions(String page, Long permissions) {
		getPermissionMatrix().put(page, permissions);
	}
	
	@Override
	public boolean hasPermissions(User user, String page) {
		if (!getPermissionMatrix().containsKey(page))
			return true;
		long required_rights = getPermissionMatrix().get(page).longValue();
		long user_rights = getUserCanonicalGroupsBits(user);
		long eval = required_rights & user_rights;
		return (eval==required_rights);
	}

	public User findUserById(Serializable id) {
		try {
			User user = getSecurityDao().findUserById(Long.valueOf(id.toString()));
			return user;
		} 
		catch (Exception e) {
			logger.error(e);
			return null;
		}
	}
	
	public Group findGroupById(Serializable id) {
		try {
			Group group = getSecurityDao().findGroupById(Long.valueOf(id.toString()));
			return group;
		} 
		catch (Exception e) {
			logger.error(e);
			return null;
		}
	}
	
	@Override
	public void removeToken(String token) {
		user_tokens.remove(token);
	}

	@Override
	public int getTokenDBSize() {
		return user_tokens.size();
	}
	
	@Override
	public void addToken(User user, String token) {
		addToken(user, token, ONE_DAY_MINUTES);
	}
	
	
	@Override
	public void addToken(User user, String token,  int duration_minutes) {
		addToken(user, token, null,  duration_minutes);
	}
	
	@Override
	public void addToken(Serializable id, String token,  int duration_minutes) {
		SecurityRecord record = new SecurityRecord();
		record.token = token;
		record.parameter=null;
		record.id=id;
		
		// duration_minutes expiration period
		//
		record.expiration=Instant.now().plusSeconds(60*duration_minutes);
		
		user_tokens.put(token, record, duration_minutes * 60 * 60);
		
		
	}
	
	@Override
	public void addToken(User user, String token, String parameter, int duration_minutes) {
		SecurityRecord record = new SecurityRecord();
		record.token = token;
		record.parameter=parameter;
		record.user_id=user.getId().toString();
		
		// duration_minutes expiration period
		record.expiration=Instant.now().plusSeconds(60*duration_minutes);
		
		user_tokens.put(token, record, duration_minutes * 60 * 60);
	}
	
	public void setDefaultUser(String userName) {
		this.defaultUser = userName;
	}
	
	public String getDefaultUser() {
		return this.defaultUser;
	}
	
	public void setSecurityDao(KbeeSecurityDao userDao) {
		this.userDao = userDao;
	}
	
	public KbeeSecurityDao getSecurityDao() {
		return this.userDao;
	}
	
    public void setPrivateKey(RSAPrivateKey privateKey) {
        this.privateKey = privateKey;
    }
	
	public void setAuthenticationManager(AuthenticationManager authenticationManager) {

	}

	public AuthenticationManager getAuthenticationManager() {
		return authenticationManager;
	}

	public User findUserByUsername(String username) {
		User user = getSecurityDao().findUserByName(username);
		return user;
	}
	
	public List<Group> findGroupByName(String groupname, String domain_id) {
		List<Group> groups = getSecurityDao().findGroupByName(groupname, domain_id);
		return groups;
	}
	
	public void onUpdate(KbeeUser user) {
		if (this.active_users.containsKey(user.getId().toString()))
			this.active_users.replace(user.getId().toString(), user);
	}
	
	@Override
	public boolean isRoot(User user) {
		try {
			return user!=null && (user.getName().startsWith("root@"));
		} 
		
		catch (Throwable e) {
			logger.error(e);
			return false;
		}
	}
	
	@Override
	public boolean isRoot() {
		return isRoot(getSessionUser());
	}
	
	@Override
	public boolean isLocal() {
		try {
			if (SecurityContextHolder.getContext().getAuthentication()==null) 
				return false;
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			return authentication instanceof LocalAuthentication || authentication instanceof UsernamePasswordAuthenticationToken;
		} 
		catch (Exception e) {
			logger.error(e, "inside Local. returns false");
			return false;
		}
	}
	
	public 	Object getAuthentication() {
		return SecurityContextHolder.getContext().getAuthentication();
	}
	
	@Override
	public boolean isActive(User user) {
		return getActiveUsers().containsKey(user.getId().toString());
	}
	
	@Override
	public void setActive(User user) {
		getActiveUsers().put(user.getId().toString(), user);
	}
	
	@Override
	public void setInActive(User user) {
		getActiveUsers().remove(user.getId().toString());
	}
	
	@Override
	public String nextSecureToken() {
		return new BigInteger(130, random).toString(32);
	}
	
	@Override
	public boolean validateName(String username) {
		if (username==null || username.length()==0) {
			return false;
		}
		
		if (!username.matches("[a-z|0-9]+")) {
			return false;
		}
		
		if (reservednames.contains(username)) {
			return false;
		}
		return true;
	}
	

	@Override
	public void exit(final int status) {
		exit(status, 20000);
	}

	@Override
	public int getTotalActiveUsers() {
		return getActiveUsers().size();
	}
	
	

	
	/**
	 * 
	 */
	@Override
	public List<Principal> getDomainAdminUsers(String domain_id) {
		
		if (domain_admins.containsKey(domain_id))
			return domain_admins.get(domain_id);
		
		synchronized (this.domain_admins) {

			List<Principal> set = new ArrayList<>();
			
			for (Principal p:getSecurityDao().getDomainAdminUsers(domain_id)) 
					set.add(new ProxyUserPrincipal(p.getName(), ((User) p).getUserName()));
			
			Collections.sort(set, new Comparator<Principal>() {
				@Override
				public int compare(Principal a, Principal b) {
					try {
						if (a instanceof User && b instanceof User) {
							return ((User) a).getUserName().compareToIgnoreCase(((User) a).getUserName());
						}
						return 0;
					} catch (Exception e) {
						return 0;
					}
				}
			});
			
			domain_admins.put(domain_id, set);
		}
		return domain_admins.get(domain_id);
	}


	/**
	 * 
	 */
	@Override								
	public List<Principal> getDomainSupportUsers(String domain_id) {
		
		if (domain_support.containsKey(domain_id))
			return domain_support.get(domain_id);
		
		synchronized (this.domain_support) {

			List<Principal> set = new ArrayList<>();
			
			for (Principal p:getSecurityDao().getDomainSupportUsers(domain_id)) 
					set.add(new ProxyUserPrincipal(p.getName(), ((User) p).getUserName()));
			
			Collections.sort(set, new Comparator<Principal>() {
				@Override
				public int compare(Principal a, Principal b) {
					try {
						if (a instanceof User && b instanceof User) {
							return ((User) a).getUserName().compareToIgnoreCase(((User) a).getUserName());
						}
						return 0;
					} catch (Exception e) {
						return 0;
					}
				}
			});
			
			domain_support.put(domain_id, set);
		}
		return domain_support.get(domain_id);
	}

	@Override
	public  void exit(final int status, long maxDelayMillis) {
		try {
			  // setup a timer, so if nice exit fails, the nasty exit happens
			  Timer timer = new Timer();
			  timer.schedule(new TimerTask() {
			    @Override
			    public void run() {
			      Runtime.getRuntime().halt(status);
			    }
			  }, maxDelayMillis);
			  
			  // try to exit nicely
			  System.exit(status);
	  
		} catch (Throwable ex) {
		  // exit nastily if we have a problem
		  Runtime.getRuntime().halt(status);
		  
		} finally {
			
		  // should never get here
		  Runtime.getRuntime().halt(status);
		}
		
	}
	
	// que eventos? 
	// los que son de edicion de usuarios, o los que son de refresh
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

	private Map<String, User> getActiveUsers() {
		return active_users;
	}
	
	private Map<String, Long> getPermissionMatrix() {
		return permission_matrix;
	}
	
	private Long getUserCanonicalGroupsBits(User user) {
		if (user_groups_bits.containsKey(user.getId()))
				return user_groups_bits.get(user.getId());
		synchronized (this) {
			double val=0.0;
			for (KbeeGlobalRole role: KbeeGlobalRole.getRoles()) {
				if (isMember(user, role.getId())) {
					double bit = Math.pow(2.00, Double.valueOf(role.getInternalId()).doubleValue());
					val+=bit;
				}
			}
			user_groups_bits.put(user.getId(), Long.valueOf((long) val));
		}
		return user_groups_bits.get(user.getId());
	}

	private void evict() {
		
		synchronized (this) {
			
			try {
					this.user_groups_bits.clear();
					this.domain_admins.clear();
					
					List<String> list = new ArrayList<String>();
					
					for ( Entry<String, SecurityRecord> entry: user_tokens.entrySet() ) {
							SecurityRecord sec= entry.getValue();
							Instant now = Instant.now();
							if (sec.expiration==null || sec.expiration.isBefore(now)) {
								list.add(entry.getKey());
							}
						}
							
					for (String s:list) {
								user_tokens.remove(s);
					}
					
			} catch (Exception e) {
				logger.error(e);
			}
		}
		
		
	}
}
