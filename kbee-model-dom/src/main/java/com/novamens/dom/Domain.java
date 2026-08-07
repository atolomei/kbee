package com.novamens.dom;

import java.io.File;
import java.io.IOException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.Locale;

import com.novamens.security.SecurityEntity;

public interface Domain extends com.novamens.dom.Object, DomainObject, SecurityEntity  {
	
	public String getName(); 			
	public void setName(String name);	
	
	public String getAddress(); 			
	public void setAddress(String address);
						
	public String getWebsite(); 			
	public void setWebsite(String website);
	

	public String getVanityUrl(); 			
	public void setVanityUrl(String url);

	
	public String getOrganization();
	public void setOrganization(String organization);
	
	public String getDescription();
	public void setDescription(String des);
	
	/** Quota in GB
		NOTE. We use local Hard Disk storage for Quota. (KBFSStorageType.External does not count)
	*/
	int getQuota();
	void setQuota(int q);
	
	String getFileReaderDirectory();
	void setFileReaderDirectory(String str);
	
	/** Premium | Basic */
	public DomainType getDomainType();
	public void setDomainType(DomainType type);

	
	public boolean isEnabled();
	public void setEnabled(boolean val);
	
	int getPasswordRenewMonths();
	void setPasswordRenewMonths(int pwrm);
	
	public String getDisplayName();
	
	public boolean isTemplate();
	public void setTemplate(boolean b);
	
	public int getMaxUsers();
	public void setMaxUsers(int q);

	public boolean isTipofTheDayEnabled();
	
	boolean isCabinetTemplate();
	boolean isCabinetKnowledgeBase();
	boolean isCabinetExternal();
	public void setCabinetTemplate(boolean b);
	public void setCabinetKnowledgeBase(boolean b);
	public void setCabinetExternal(boolean b);
	
	public String getLogoUrl();
	
	public boolean isAPIEnabled();
	public void setAPIEnabled(boolean b);
	
	public void setTimeZone(String tz);
	public String getTimeZone();
	
	public KBFSStorageType getStorageType();
	public void setStorageType(KBFSStorageType storageType);

	public boolean isEncryptFiles();
	public void setEncryptFiles(boolean encryptFiles);

	public String getExternalId();
	public void setExternalId(String externalId);
	
	public boolean isPortalLibrary();
	public void setPortalLibrary(boolean b);
	
	public File getFileLogo();
	
	public Locale getLocale();
	String getDefaultPassword();

	public default boolean isOAuthAuthentication() {return false;}
	public void setOAuthAuthentication(boolean oauth);
	
	public default boolean hasIntegrationService() {return false;}

	public Certificate getCertificate();	
	public PrivateKey getPrivateKey() throws IOException;	
	
	public int getSecurityLevel();
}