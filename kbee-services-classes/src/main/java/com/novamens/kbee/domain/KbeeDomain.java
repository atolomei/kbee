package com.novamens.kbee.domain;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Type;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.resource.KBFile;
import com.novamens.datetime.DateTimeService;
import com.novamens.dom.Domain;
import com.novamens.dom.DomainType;
import com.novamens.dom.Indexable;
import com.novamens.dom.KBFSStorageType;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.security.User;
import com.novamens.security.audit.AuditSet;
import com.novamens.service.ObjectService;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.service.ServiceNotFoundException;
import com.novamens.signature.CertificateParser;

import kbee.replica.Replica;

/**
 * 
 * <p>A Domain is an organization that has its' own "instance" of the application</p>
 */
@Entity
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="entity")
@Inheritance(strategy=InheritanceType.JOINED)
@Table(name = "DOMAIN")
public class KbeeDomain implements Domain, Indexable {

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeDomain.class.getName());

	@Id
	@SequenceGenerator(name = "domain_sequencer", sequenceName = "domainid_sequence")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "domain_sequencer")
	@Column(name = "id")
	private Long id;
	
	@Column(name = "state")
	@Enumerated(EnumType.ORDINAL)
	@Type(type="com.novamens.kbee.dom.ObjectStateUserType")
	private ObjectState state;

	@Column(name = "lastModifiedDate")
	private OffsetDateTime lastModifiedDate;
	
	@Column(name = "creationDate")
	private OffsetDateTime creationDate;

	@ManyToOne(fetch = FetchType.LAZY, targetEntity = KbeeUser.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "lastModifiedUser", updatable=false)
	private User lastModifiedUser;
	
	@Column(name="vanityurl")
	private String vanityurl;
	
	@Column(name = "timezone")
	private String timezone;
	
	@Column(name = "lang")
	private String lang;
	
	@Column(name="name")
	private String name;
	
	@Column(name="address")
	private String address;

	@Column(name="phone") 
	private String phone;
	
	@Column(name="email")
	private String email;
	
	@Column(name="external_id")
	private String externalId;

	@Column(name="website")
	private String website;

	@Column(name="description")
	private String description;

	@Column(name="organization")
	private String organization;
	
	@Column(name="password_renew_months")
	private int password_renew_months;
	
	@Enumerated(EnumType.ORDINAL)
	@Type(type="com.novamens.kbee.dom.DomainTypeUserType")
	@Column(name="type")
	private DomainType type;

	@Column(name="service")
	private int service;

	// in GB. 0 for no limit
	@Column(name="quota")
	private int quota;  
	
	// 0 for no limit
	@Column(name="maxusers")
	private int maxusers;  

	@Column(name = "locale_str")
	private String locale_str;

	@Transient 
	Locale locale = null;
	
	@Column(name = "storageMode")
	@Enumerated(EnumType.ORDINAL)
	@Type(type="com.novamens.kbee.content.resource.KBFSStorageTypeUserType")
	private KBFSStorageType storageType;
	
	@Column(name="file_reader_directory")
	private String file_reader_directory;

	@Column(name="istemplate")
	private boolean istemplate;  

	@Column(name="tipoftheday")
	private boolean istipoftheday;  

	@Column(name="cabinet_template")
	private boolean cabinet_template;  
	
	@Column(name="cabinet_kbase")
	private boolean cabinet_kbase;
	
	@Column(name="cabinet_external")
	private boolean cabinet_external;

	@Column(name="isapienabled")
	private boolean isapienabled;  
				
	@Column(name="portal_library")
	private boolean portalLibrary;

	@Column(name="encrypt_files")
	private boolean encryptFiles;
	
	@Column(name="defaultpassword")
	private String defaultpassword;
	
	@Column(name="oauth")
	private boolean oauth;
	
	@Column(name="logo_url")
	private String logo_url;
	
	@Column(name="certificate")
	private String certificate;
	
	@Column(name="private_key")
	private String privateKey;
	
	@Column(name="integration_service")
	private boolean integration_service;
	
	@OneToMany(orphanRemoval=true, fetch = FetchType.LAZY, cascade=CascadeType.ALL, targetEntity = KbeeReplica.class,  mappedBy = "domain")
	//@JoinColumn(name = "DOMAIN_ID", nullable=false) 
	//@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region="entity")
	List<Replica> replicas = new ArrayList<Replica>();
	
	@Transient
	KBFile logo;
	
	@Transient
	private String last_modified_date_colloquial = null;

	@Transient
	private String creation_date_colloquial = null;

	
	public KbeeDomain() {
		super();
		this.creationDate=OffsetDateTime.now();
	}
	
	public KbeeDomain(String name) {
		super();
		setName(name);
		this.creationDate=OffsetDateTime.now();
	}
	
	public Long getId()	{
		return id;
	}
	
	public void setId(Serializable id) {
		this.id = (Long)id;
	}
	
	public String getName()	{
		return name;
	}
	
	public void setName(String name) {
		this.name=name;
	}
	
	public String getOrganization()	{
		return this.organization;
	}
	
	public void setOrganization(String organization)	{
		this.organization=organization;
	}
	
	@Override
	public String getDescription() {
 		return description;
	}

	@Override
	public void setDescription(String des) {
		description=des;
	}

	@Override
	public DomainType getDomainType() {
		return type;
	}

	@Override
	public void setDomainType(DomainType type) {
		this.type=type;
	}
	
	@Override
	public String getDisplayName() {
		if (getOrganization()!=null)
			return getOrganization();
		else
			return getName();
	}
	
	@Override
	public String getDefaultPassword() {
		return this.defaultpassword;
	}
	
	public void setDefaultPassword(String a) {
		this.defaultpassword=a;
	}

	@Override
	public boolean isPortalLibrary() {
		return this.portalLibrary;
	}
	
	@Override
	public void setPortalLibrary(boolean b)	{
		this.portalLibrary=b;
	}
	
	@Override
	public boolean isAPIEnabled() {
		return this.isapienabled;
	}
	
	@Override
	public void setAPIEnabled(boolean b) {
		this.isapienabled=b;
	}

	@Override
	public boolean isCabinetTemplate() 	{
		return this.cabinet_template;
	}
	
	@Override
	public boolean isCabinetKnowledgeBase()	{
		return this.cabinet_kbase;
	}
	
	@Override
	public boolean isCabinetExternal() {
		return this.cabinet_external;
	}

	@Override
	public void setCabinetTemplate( boolean b) {
		this.cabinet_template=b;
	}
	
	@Override
	public void setCabinetKnowledgeBase(boolean b)  {
		this.cabinet_kbase=b;
	}
	
	@Override
	public void setCabinetExternal(boolean b) {
		this.cabinet_external=b;
	}
	
	@Override
	public  boolean hasIntegrationService() {
		return integration_service;
	}
	
	public  boolean getIntegrationService() {
		return integration_service;
	}

	public  void setIntegrationService(boolean value) {
		integration_service = value;
	}

	public void setLastModifiedOffsetDateTime(OffsetDateTime date) {
		this.lastModifiedDate = date;
	}

	@Override
	public int getQuota()	{
		return quota;
	}

	@Override
	public void setQuota(int q) {
		this.quota=q;
	}

	@Override
	public int getMaxUsers() {
		return maxusers;
	}

	@Override
	public void setMaxUsers(int q) {
		this.maxusers=q;
	}

	@Override
	public String getFileReaderDirectory()	{
		return file_reader_directory;
	}
	
	@Override
	public void setFileReaderDirectory(String str)	{
		this.file_reader_directory=str;
	}

	@Override
	public String getLogoUrl()	{
		return logo_url;
	}
	
	public void setLogoUrl(String photo_url)	{
		this.logo_url=photo_url;
	}
	
	public KBFile getLogo() {
		if (this.logo==null) {
			if (getLogoUrl()!=null) {
					this.logo = (KBFile) getContentDao().findResourceById(KBFile.class, this.logo_url);
			}
		}
		return this.logo;
	}
	
	public String getAddress()	{
		return address;
	}
	
	public void setAddress(String address)	{
		this.address=address;
	}

	@Override
	public int getPasswordRenewMonths()	{
		return this.password_renew_months;
	}
	
	@Override
	public void setPasswordRenewMonths(int pwrm)	{
		this.password_renew_months=pwrm;
	}
	
	public String getWebsite()	{
		return website; 
	}
	
	public void setWebsite(String website) {
		this.website=website;
	}
	
	public boolean isEnabled() {
		return getState()==ObjectState.ENABLED;
	}
	
	public void setEnabled(boolean val) {
		if (val)
			setState(ObjectState.ENABLED);
		else
			setState(ObjectState.ARCHIVED);
	}

	@Override
	public boolean isTemplate() {
		return this.istemplate;
	}

	@Override
	public void setTemplate(boolean b) {
		this.istemplate=b;
	}

	@Override
	public boolean isTipofTheDayEnabled() {
		return istipoftheday;
	}
	
	public void setLocale(String locale_str) {
		this.locale_str=locale_str;
		this.locale=null;
	}
	
	public void setLocale(Locale locale) {
		this.locale_str=locale.getLanguage();
		this.locale=null;
	}
	
	@Override
	public Locale getLocale() {
		if (this.locale==null) {
			if (this.locale_str==null)
				 this.locale=Locale.getDefault();
			 
			else if (this.locale_str.trim().toLowerCase().equals("en"))
				this.locale=Locale.ENGLISH;
			 
		    else if (locale_str.trim().toLowerCase().equals("es"))
				locale=new Locale("es");
			
		    else
				this.locale=Locale.getDefault();
		}
		return this.locale;
	}

	public String getLanguage() {
		return this.lang;
	}
	
	public void setLanguage(String lang)  {
		this.lang=lang;
	}

	@Override
	public String getTimeZone() {
		return this.timezone;
	}

	@Override
	public void setTimeZone(String tz) {
		this.timezone=tz;
	}
	
	@Override
	public void setDefaultAudit() {
		if (this.getCreationOffsetDateTime()==null)
			this.setCreationOffsetDateTime(OffsetDateTime.now());
		if (this.getLastModifiedOffsetDateTime()==null)
			this.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		if (this.getLastModifiedUser()==null)
			this.setLastModifiedUser(getSessionUser());
	}

	@Override
	public Domain getDomain() {
		return this;
	}

	@Override
	public void setDomain(Domain domain) {
	}
	
	@Override
	public String getExternalId() {
		return externalId;
	}

	@Override
	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	@Override
	public KBFSStorageType getStorageType() {
		return storageType;
	}
	
	@Override
	public void setStorageType(KBFSStorageType storageType) {
		this.storageType = storageType;
	}

	@Override
	public boolean isEncryptFiles() {
		return encryptFiles;
	}

	@Override
	public void setEncryptFiles(boolean encryptFiles) {
		this.encryptFiles = encryptFiles;
	}
	
	@Override
	public File getFileLogo() {
		try {
			if (getLogo()!=null)
				return getLogo().getFile();
			return null;
		} 
		catch (Exception e) {
			logger.error(e);
			return null;

		}
	}
	
	@Override
	public AuditSet getAuditSet() {
		return AuditSet.SYSTEM;
	}

	@Override
	public boolean isOAuthAuthentication() {
		return this.oauth;
	}

	@Override
	public void setOAuthAuthentication(boolean oauth) {
		this.oauth=oauth;
	}

	@Override
	public String getVanityUrl() {
		return this.vanityurl;
	}

	@Override
	public void setVanityUrl(String url) {
		this.vanityurl=url;
	}
	
	@Override
	public Certificate getCertificate() {
		try {
			if (this.certificate==null) 
				return null;
			
			Certificate certificate = CertificateParser.Get().read(this.certificate);
			return certificate;
		}
		catch (CertificateException | IOException e) {
			logger.error(e);
			return null;
		}
	}
	
	public void setCertificate(Certificate certificate) throws IOException {
		this.certificate = CertificateParser.Get().write(certificate);
	}
	
	@Override
	public PrivateKey getPrivateKey() throws IOException {
		try {
			if (this.privateKey==null)
				return null;
			PrivateKey key  = CertificateParser.Get().readPrivateKey(this.privateKey);
			return key;
		}
		catch (CertificateException e) {
			logger.error(e);
			return null;
		}
	}
	
	public void setPrivateKey(PrivateKey key) throws IOException {
		this.privateKey = CertificateParser.Get().writePrivateKey(key);
	}
	
	@Override
	public int getSecurityLevel() {
		return 0;
	}
	
	public List<Replica> getReplicas() {
		return replicas;
	}
	
	public void addReplica(Replica replica) {
		((KbeeReplica)replica).setDomain(this);
		replicas.add(replica);	
	}
	
	public Date getLastModifiedDate() {
		return  Date.from(getLastModifiedOffsetDateTime().toInstant());
	}
	
	/** 
	 * The text is in the Locale and TimeZone of the Session User 
	 * If there is not Session User, then defaults.
	 */
	@Override
	public String getLastModifiedOffsetDateTimeColloquial(String css) {

		if (this.last_modified_date_colloquial!=null)
			return this.last_modified_date_colloquial;
		
		DateTimeService service = ServiceLocator.getService(DateTimeService.class);
		User user = getSessionUser();
		String zid = null;
		if (user!=null)
			zid=service.getMapZoneIds().get(user.getTimeZone());
		if (zid==null)
			zid=ZoneId.systemDefault().getId();
		Locale locale = null;
		if (user!=null)
				locale=user.getLocale();
		else
			locale=Locale.getDefault();
		this.last_modified_date_colloquial=service.timeElapsed(getLastModifiedOffsetDateTime(), ZoneId.of(zid), locale, DateTimeService.DATE_COLlOQUIAL_AGO, "ago");
		
		return this.last_modified_date_colloquial;
	}
					
	public Date getCreationDate() {
		return  Date.from(getCreationOffsetDateTime().toInstant());
	}

	public User getLastModifiedUser() {
		return lastModifiedUser;
	}
	
	public void setState(ObjectState state)	{
		this.state = state;
	}
	
	public ObjectState getState() {
		return state;
	}
	
	@Override
	public OffsetDateTime getLastModifiedOffsetDateTime() {
		return this.lastModifiedDate;
	}
	 
	@Override
	public OffsetDateTime getCreationOffsetDateTime() {
		return this.creationDate;
	}

	@Override
	public void setLastModifiedUser(User user) {
		this.lastModifiedUser=user;
	}

	@Override
	public void setCreationOffsetDateTime(OffsetDateTime date) {
		this.creationDate=date;
	}
	
	/** 
	 * The text is in the Locale and TimeZone of the Session User 
	 * If there is not Session User, then defaults.
	 */
	@Override
	public String getCreationOffsetDateTimeColloquial() {
		if (this.creation_date_colloquial!=null)
			return this.creation_date_colloquial;
			
		DateTimeService service = ServiceLocator.getService(DateTimeService.class);
		User user = getSessionUser();
		String zid = null;
		if (user!=null)
			zid=service.getMapZoneIds().get(user.getTimeZone());
		if (zid==null)
			zid=ZoneId.systemDefault().getId();
		Locale locale = null;
		if (user!=null)
				locale=user.getLocale();
		else
			locale=Locale.getDefault();
		this.creation_date_colloquial=service.timeElapsed(getCreationOffsetDateTime(), ZoneId.of(zid), locale, DateTimeService.DATE_COLlOQUIAL_AGO, "ago");
		
		return this.creation_date_colloquial;
	}
	
	public <T extends ObjectService> T getService(Class<T> service) throws ServiceNotFoundException {
		return ServiceLocator.getService(this, service);
	}
	
	@Override
	public boolean equals(Object object) {
		if (!(object instanceof Domain)) return false;
		return ((Domain)object).getId().equals(getId());
	}
	
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append( super.toString());
		if (getName()!=null)
			str.append("\nname: " + getName());
		if ( getAddress()!=null)
			str.append("\naddress: " +  getAddress());
		if (  getWebsite() !=null)
		str.append("\nwebsite: "   + getWebsite());
		str.append("\nenabled: "   + (isEnabled()?"YES":"NO"));
		return str.toString();
	}
	
	protected KbeeUser getSessionUser() {
		return (KbeeUser)ServiceLocator.getService(SecurityService.class).getSessionUser();
	}

	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
}