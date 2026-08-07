package com.novamens.kbee.service;


import com.novamens.service.WebSessionService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
public class KbeeWebSessionService implements WebSessionService {

    static private Logger logger = LogManager.getLogger(KbeeWebSessionService.class.getName());
    SessionRegistry sessionRegistry;

    public void expireUserSessions(String username) {
        if (sessionRegistry != null) {
            final Object principal = sessionRegistry.getAllPrincipals().stream().filter(u -> ((UserDetails) u).getUsername().equals(username)).findFirst().orElse(null);

            if (principal != null) {
                final List<SessionInformation> allSessions = sessionRegistry.getAllSessions(principal, false);
                allSessions.stream().filter(sess -> !sess.isExpired()).forEach(sess -> sess.expireNow());
            }
        }
    }

    public long countUserActiveSessions(String username) {
        if (sessionRegistry != null) {
            final Object principal = sessionRegistry.getAllPrincipals().stream().filter(u -> ((UserDetails) u).getUsername().equals(username)).findFirst().orElse(null);
            if (principal != null) {
                return sessionRegistry.getAllSessions(principal, false).size();
            }
        }
        return 0;
    }

    @Override
    public long countDomainTotalActiveSessions(String domainName) {
        long count = 0;
        if (sessionRegistry != null) {
            for (Object principal : sessionRegistry.getAllPrincipals()) {
                final String sessionUsername = ((UserDetails) principal).getUsername();
                if (sessionUsername.endsWith("@" + domainName)) {
                    count += sessionRegistry.getAllSessions(principal, false).size();
                }
            }
        }
        return count;
    }

    @Override
    public long countUsersWithActiveSessions(String domainName) {
        long count = 0;
        if (sessionRegistry != null) {
            for (Object principal : sessionRegistry.getAllPrincipals()) {
                final String sessionUsername = ((UserDetails) principal).getUsername();
                if (sessionUsername.endsWith("@" + domainName)) {
                    count++;
                }
            }
        }
        return count;
    }

    public SessionRegistry getSessionRegistry() {
        return sessionRegistry;
    }

    public void setSessionRegistry(SessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }
}
