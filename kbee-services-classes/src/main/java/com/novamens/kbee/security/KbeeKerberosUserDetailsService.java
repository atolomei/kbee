package com.novamens.kbee.security; 
 
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.dao.DataAccessException; 
import org.springframework.security.core.userdetails.UserDetails; 
import org.springframework.security.core.userdetails.UserDetailsService; 
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class KbeeKerberosUserDetailsService implements UserDetailsService { 
	
	UserDetailsService localUserDetailsService;
	
	static Logger logger = LogManager.getLogger(KbeeKerberosUserDetailsService.class.getName());
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException {
		
		logger.debug("Kerberos User Details "+username);
		
		if (username.endsWith("@RIO.AR.BSCH")) {
			username = username.substring(0, username.indexOf("@")) + "@rio";
			username = username.toLowerCase();
		}
		else {
			if (!username.contains("@")) {
				username = username + "@rio";
			}
			else {
				if (username.endsWith("@rio")) {
					return null;
				}
			}
		}
		UserDetails userdetails = localUserDetailsService.loadUserByUsername(username);
		return userdetails;
	}
	
	public void setLocalService(UserDetailsService service) {
		localUserDetailsService = service;
	}
 
}