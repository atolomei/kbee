package com.novamens.content.base;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.security.PrivateKey;
import java.security.cert.Certificate;

import java.time.OffsetDateTime;
import java.util.Date;
import java.util.Locale;

import com.novamens.content.resource.KBFile;
import com.novamens.dom.Domain;
import com.novamens.dom.DomainType;
import com.novamens.dom.KBFSStorageType;
import com.novamens.dom.ObjectState;
import com.novamens.security.User;
import com.novamens.security.audit.AuditSet;
import com.novamens.service.ObjectService;
import com.novamens.service.ServiceLocator;
import com.novamens.service.ServiceNotFoundException;

public class DomainProxy implements Domain, Serializable {
	private static final long serialVersionUID = 1L;
	
	private Serializable id;
	private String title;
	
	public DomainProxy(Serializable id, String title) {
		setId(id);
		this.title=title;
	}
	
	public DomainProxy(Serializable id) {
		setId(id);
	}
	
	public DomainProxy(Domain domain) {
		setId(domain.getId());
	}
	
	public Serializable getId() {
		return id;
	}
	
	public void setId(Serializable id) {
		this.id = id;
	}
	
	public <T extends ObjectService> T getService(Class<T> service) throws ServiceNotFoundException {
		return ServiceLocator.getService(this, service);
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
		return this.title!=null?this.title:null;
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
	public boolean equals(Object object) {
		if (!(object instanceof Domain)) return false;
		return ((Domain)object).getId().equals(getId());
	}

	@Override
	public String getOrganization() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setOrganization(String organization) {
		// TODO Auto-generated method stub
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setDescription(String des) {
		// TODO Auto-generated method stub
	}

	@Override
	public int getQuota() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setQuota(int q) {
		// TODO Auto-generated method stub
	}

	@Override
	public String getFileReaderDirectory() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setFileReaderDirectory(String str) {
		// TODO Auto-generated method stub
	}

	@Override
	public DomainType getDomainType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setDomainType(DomainType type) {
		// TODO Auto-generated method stub
	}

	@Override
	public int getPasswordRenewMonths() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setPasswordRenewMonths(int pwrm) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public String getDisplayName() {
		return getName();
	}

	@Override
	public boolean isTemplate() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setTemplate(boolean b) {
		// TODO Auto-generated method stub
	}

	@Override
	public int getMaxUsers() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setMaxUsers(int q) {
		// TODO Auto-generated method stub
	}

	@Override
	public OffsetDateTime getLastModifiedOffsetDateTime() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OffsetDateTime getCreationOffsetDateTime() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setLastModifiedOffsetDateTime(OffsetDateTime date) {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean isTipofTheDayEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getLastModifiedOffsetDateTimeColloquial(String css) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getCreationOffsetDateTimeColloquial() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isCabinetTemplate() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isCabinetKnowledgeBase() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isCabinetExternal() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setCabinetTemplate(boolean b) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setCabinetKnowledgeBase(boolean b) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setCabinetExternal(boolean b) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getLogoUrl() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isAPIEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setAPIEnabled(boolean b) {
	}

	@Override
	public void setCreationOffsetDateTime(OffsetDateTime date) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setDefaultAudit() {
		// TODO Auto-generated method stub
	}

	@Override
	public void setTimeZone(String tz) {
		// TODO Auto-generated method stub
	}

	@Override
	public String getTimeZone() {
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setStorageType(KBFSStorageType storageType) {
		// TODO Auto-generated method stub
		
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setExternalId(String externalId) {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean isPortalLibrary() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setPortalLibrary(boolean b) {
		// TODO Auto-generated method stub
		
	}
	
	public KBFile getLogo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Locale getLocale() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public File getFileLogo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDefaultPassword() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public AuditSet getAuditSet() {
		return AuditSet.SYSTEM;
	}

	@Override
	public void setOAuthAuthentication(boolean oauth) {
		// TODO Auto-generated method stub
	}

	@Override
	public String getVanityUrl() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setVanityUrl(String url) {
		// TODO Auto-generated method stub
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