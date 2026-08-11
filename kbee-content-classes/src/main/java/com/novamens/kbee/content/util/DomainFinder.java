package com.novamens.kbee.content.util;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.Locale;

import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate5.SessionFactoryUtils;
import org.springframework.orm.hibernate5.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.novamens.content.dao.ContentDao;
import com.novamens.content.resource.KBFile;
import com.novamens.dom.Domain;
import com.novamens.dom.DomainType;
import com.novamens.dom.KBFSStorageType;
import com.novamens.dom.ObjectState;
import com.novamens.security.User;
import com.novamens.security.audit.AuditSet;
import com.novamens.service.ObjectService;
import com.novamens.service.ServiceNotFoundException;

public class DomainFinder {
	
	private ContentDao dao;
	
	SessionFactory sessionFactory;
	
	public class DomainClone implements Domain, Serializable {

		private static final long serialVersionUID = 1L;
		
		private Serializable id;
		
		public DomainClone(Domain domain) {
			setId(domain.getId());
		}
		public Serializable getId() {
			return id;
		}
		public void setId(Serializable id) {
			this.id = id;
		}
		public <T extends ObjectService> T getService(Class<T> service) throws ServiceNotFoundException {
			return null;
		}
		public User getLastModifiedUser() {
			return null;
		}
		public Date getLastModifiedDate() {
			return null;
		}
		public void setLastModifiedDate(Date date) {
		}
		public void setLastModifiedUser(User user) {
		}
		public void setState(ObjectState enabled) {
		}
		public ObjectState getState() {
			return null;
		}
		public String getName() {
			return null;
		}
		public void setName(String name) {
		}	
		public String getAddress() {
			return null;
		}
		public void setAddress(String address) {
		}	
		public String getWebsite() {
			return null;
		}
		public void setWebsite(String website) {
		}
		public boolean isEnabled() {
			return true;
		}
		public void setEnabled(boolean val) {
		}
		@Override
		public String getOrganization() {
			return null;
		}
		@Override
		public void setOrganization(String organization) {
		}
		@Override
		public String getDescription() {
			return null;
		}
		@Override
		public void setDescription(String des) {
		}
		@Override
		public int getQuota() {
			return 0;
		}
		@Override
		public void setQuota(int q) {
		}
		@Override
		public String getFileReaderDirectory() {
			return null;
		}
		@Override
		public void setFileReaderDirectory(String str) {
		}
		@Override
		public DomainType getDomainType() {
			return null;
		}
		@Override
		public void setDomainType(DomainType type) {
		}
		@Override
		public int getPasswordRenewMonths() {
			return 0;
		}
		@Override
		public void setPasswordRenewMonths(int pwrm) {
		}
		@Override
		public String getDisplayName() {
			return null;
		}
		@Override
		public boolean isTemplate() {
			return false;
		}
		@Override
		public void setTemplate(boolean b) {
		}
		@Override
		public int getMaxUsers() {
			return 0;
		}
		@Override
		public void setMaxUsers(int q) {
		}
		@Override
		public void setLastModifiedOffsetDateTime(OffsetDateTime date) {
		}
		@Override
		public OffsetDateTime getLastModifiedOffsetDateTime() {
			return null;
		}
		@Override
		public OffsetDateTime getCreationOffsetDateTime() {
			return null;
		}
		@Override
		public boolean isTipofTheDayEnabled() {
			return false;
		}
		@Override
		public String getLastModifiedOffsetDateTimeColloquial(String css) {
			return null;
		}
		@Override
		public String getCreationOffsetDateTimeColloquial() {
			return null;
		}
		@Override
		public boolean isCabinetTemplate() {
			return false;
		}
		@Override
		public boolean isCabinetKnowledgeBase() {
			return false;
		}
		@Override
		public boolean isCabinetExternal() {
			return false;
		}
		@Override
		public void setCabinetTemplate(boolean b) {
		}
		@Override
		public void setCabinetKnowledgeBase(boolean b) {
		}
		@Override
		public void setCabinetExternal(boolean b) {
		}
		@Override
		public String getLogoUrl() {
			return null;
		}
		@Override
		public boolean isAPIEnabled() {
			return false;
		}
		@Override
		public void setAPIEnabled(boolean b) {
		}
		@Override
		public void setCreationOffsetDateTime(OffsetDateTime date) {
		}
		@Override
		public void setDefaultAudit() {
		}
		@Override
		public void setTimeZone(String tz) {
		}
		@Override
		public String getTimeZone() {
			return null;
		}
		@Override
		public Domain getDomain() {
			return null;
		}
		@Override
		public void setDomain(Domain domain) {
		}
		@Override
		public KBFSStorageType getStorageType() {
			return null;
		}
		@Override
		public void setStorageType(KBFSStorageType storageType) {
		}
		@Override
		public boolean isEncryptFiles() {
			return false;
		}
		@Override
		public void setEncryptFiles(boolean encryptFiles) {
		}
		@Override
		public String getExternalId() {
			return null;
		}
		@Override
		public void setExternalId(String externalId) {
		}
		@Override
		public boolean isPortalLibrary() {
			return false;
		}
		@Override
		public void setPortalLibrary(boolean b) {
		}
		public KBFile getLogo() {
			return null;
		}
		@Override
		public Locale getLocale() {
			return null;
		}
		@Override
		public File getFileLogo() {
			return null;
		}
		@Override
		public String getDefaultPassword() {
			return null;
		}
		@Override
		public AuditSet getAuditSet() {
			return AuditSet.SYSTEM;
		}
		@Override
		public void setOAuthAuthentication(boolean oauth) {
		}
		@Override
		public String getVanityUrl() {
			return null;
		}
		@Override
		public void setVanityUrl(String url) {
		}
		@Override
		public Certificate getCertificate() {
			return null;
		}
		@Override
		public PrivateKey getPrivateKey() throws IOException {
			return null;
		}
		@Override
		public int getSecurityLevel() {
			return 0;
		}

	}
	
	public DomainFinder() {
	}
	
	public void setContentDao(ContentDao dao) {
		this.dao = dao;
	}
	
	public void setSessionFactory(SessionFactory factory) {
		sessionFactory = factory;
	}
	
	public Domain find(String domainid) {
		TransactionSynchronizationManager.bindResource(sessionFactory, new SessionHolder(sessionFactory.openSession()));
		Domain domain = (Domain)dao.findDomainById(Long.valueOf(domainid));
		SessionHolder sessionHolder = (SessionHolder) TransactionSynchronizationManager.unbindResource(sessionFactory);
		domain = new DomainClone(domain);
		SessionFactoryUtils.closeSession(sessionHolder.getSession());
		return domain;
	}
	
	
	public String getClassCode() {
		return this.getClass().getSimpleName();
	}
	
	
	

}