package com.novamens.kbee.content.util;

import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate5.SessionFactoryUtils;
import org.springframework.orm.hibernate5.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.novamens.dao.SecurityDao;
import com.novamens.security.acl.Acl;

public class AclFinder {
	private SecurityDao dao;
	SessionFactory sessionFactory;
	
	public AclFinder() {
	}
	
	public void setSecurityDao(SecurityDao dao) {
		this.dao = dao;
	}
	
	public void setSessionFactory(SessionFactory factory) {
		sessionFactory = factory;
	}
	
	public Acl find(String aclid) {
		boolean session = false;
		if (!TransactionSynchronizationManager.hasResource(sessionFactory)) {
			TransactionSynchronizationManager.bindResource(sessionFactory, new SessionHolder(sessionFactory.openSession()));
			session = true;
		}
		Acl acl = (Acl)dao.findAclById(Long.valueOf(aclid));
		if (session) {
			SessionHolder sessionHolder = (SessionHolder) TransactionSynchronizationManager.unbindResource(sessionFactory);
			SessionFactoryUtils.closeSession(sessionHolder.getSession());
		}
		return acl;
	}
}
