package com.novamens.service;

public interface WebSessionService extends SystemService  {
    void expireUserSessions(String username);
    long countUserActiveSessions(String username);

    long countDomainTotalActiveSessions(String domainName);
    long countUsersWithActiveSessions(String domainName);

}
