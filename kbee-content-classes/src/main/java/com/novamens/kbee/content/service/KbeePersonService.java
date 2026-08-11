package com.novamens.kbee.content.service;

import java.security.cert.Certificate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.base.SignedData;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.dao.PortalDao;
import com.novamens.content.entity.Person;
import com.novamens.content.model.AttributeTemplate;
import com.novamens.content.model.Classification;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.DataSetType;
import com.novamens.content.model.EntityMember;
import com.novamens.content.model.PersonMember;
import com.novamens.content.model.PersonSet;
import com.novamens.content.model.UserSet;
import com.novamens.content.model.UserSubset;
import com.novamens.content.resource.KBFile;
import com.novamens.content.security.ContentSecurityDao;
import com.novamens.content.security.EntityRole;
import com.novamens.content.security.Role;
import com.novamens.content.security.RolesService;
import com.novamens.content.service.PersonService;
import com.novamens.content.service.SignatureService;
import com.novamens.content.user.UserDevice;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserRole;
import com.novamens.content.user.UserSelfService;
import com.novamens.content.user.UserSignature;
import com.novamens.content.user.externalLogin.ExternalPlatformId;
import com.novamens.content.user.externalLogin.UserExternalLoginPlatform;
import com.novamens.content.user.externalLogin.UserExternalPlatformIdType;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.email.EmailService;
import com.novamens.kbee.content.entity.KbeePerson;
import com.novamens.kbee.content.model.KbeePersonMember;
import com.novamens.kbee.content.user.KbeeUserDevice;
import com.novamens.kbee.content.user.KbeeUserExternalLoginPlatform;
import com.novamens.kbee.content.user.KbeeUserProfile;
import com.novamens.kbee.content.user.KbeeUserRole;
import com.novamens.kbee.security.KbeeAuthToken;
import com.novamens.kbee.security.KbeeTokenSubmission;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.sms.KbeeSmsMessage;
import com.novamens.portal6.model.Site;
import com.novamens.security.AuthToken;
import com.novamens.security.TokenSubmission;
import com.novamens.security.User;
import com.novamens.security.acl.Group;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.BrandingService;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.signature.SignatureException;
import com.novamens.sms.SmsService;

import kbee.email.EmailBuilderRegisterDevice;
import kbee.email.EmailBuilderSendToken;
import kbee.util.PropertiesFactory;

public class KbeePersonService implements PersonService {
		
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeePersonService.class.getName());
	
	static Boolean ACCEPT_ALL_SIGNATURES = "yes".equals(PropertiesFactory.getInstance("kbee").getProperties().getProperty("accept-all-signatures", "no").trim());
	
	private PersonMember member;
	private Person person;
	
	public KbeePersonService() {
	}

	/**
	 * Person 
	 * PersonMember
	 * UserMember
	 */
	public KbeePersonService(Object object) {
		if (object instanceof PersonMember) {
			member = (PersonMember)object;
		}
		else {
			if (object instanceof Person) {
				person = (Person)object;
			}
		}
	}
	
	public PersonMember getMember() {
		return member;
	}
	
	public KbeePerson getPerson() {
		return person!=null ? (KbeePerson)person : (KbeePerson)member.getPerson();
	}
	

	
	/*
	 * 
	 * 
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public TokenSubmission sendToken(Content content) {
		KbeeTokenSubmission submission = new KbeeTokenSubmission();
		try {
			String feedback = null;
			boolean mailError = false, phoneError = false;
			
			AuthToken token = new KbeeAuthToken(3600);
			String tokenvalue = token.getTokenValue();
			
			submission.setTokenValue(tokenvalue);
			
			String message = "Código de Seguridad KBEE: " + tokenvalue;
			
			
			try {
			
				EmailBuilderSendToken builder = new EmailBuilderSendToken(content, getPerson(), tokenvalue);
				ServiceLocator.getService(EmailService.class).send(builder);
				submission.setEmail(getPerson().getEmail());
			}
			catch(Exception e) {
				logger.error(e);
				feedback = e.getMessage();
				mailError = true;
			}

			try {
				String phone = getPhone();
				if (!"".equals(phone) && phone!=null) {
					
					if (ACCEPT_ALL_SIGNATURES) {
						logger.debug("Token -> " + message);	
					}
					else {
						ServiceLocator.getService(SmsService.class).sendMessage(new KbeeSmsMessage(phone, message));
					}
					submission.setPhone(phone);
				}
			}
			catch(Exception e) {
				logger.error(e);
				feedback = feedback!=null ? feedback+"\n"+e.getMessage() : e.getMessage();
				phoneError = true;
			}
			
			submission.setError(phoneError&&mailError);
			submission.setFeedback(feedback);
		}
		catch (Exception e) {
			logger.error(e);
			submission.setError(true);
			submission.setFeedback(e.getMessage());
		}
		return submission;
	}
	
	/**
	 * This method replaces the Person referenced by the UserMember
	 * with this Person (referenced by the PersonMember from a PersonSet) 
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void linkToUser(User user) {

		UserProfile up=getContentDao().findUserProfileByUser(user);
		
		// delete old Person ???
		Person oldPerson = (Person) up.getEntity();
		getContentDao().delete(oldPerson);
		
		// Assign new Person ??
		((KbeeUserProfile) up).setEntity(getMember());
		getContentDao().save(((PersonMember) getMember()).getPerson());
		getContentDao().save((DataSetMember) getMember());
		getContentDao().save(user);
		getContentDao().flush();
		
	
	}
	
	@Transactional(propagation = Propagation.REQUIRED)
	public User createUser() {
		//ServiceLocator.getService(SecurityService.class).authenticate("root@"+getModelObject().getDomain().getName());
		List<ExternalPlatformId> oauthPlatforms = new ArrayList<ExternalPlatformId>();
		oauthPlatforms.add(ExternalPlatformId.GOOGLE);
		oauthPlatforms.add(ExternalPlatformId.FACEBOOK);
		List<Role> roles = new ArrayList<Role>();
		roles.addAll(getDefaultRoles());
		User user = createUser(roles, oauthPlatforms);
		return user;
	}
	
	@Transactional(propagation = Propagation.REQUIRED)
	public User createUser(List<Role> roles, List<ExternalPlatformId> platforms) {
		
		// Creates PersonMember -----------------------------------------------------------
		KbeePersonMember member = new KbeePersonMember(getUserSet());
		member.setPerson(getPerson());
		member.setCreationOffsetDateTime(OffsetDateTime.now());
		member.setState(ObjectState.ENABLED);
		getContentDao().save((Person)member);
				
		// Creates User -----------------------------------------------------------------
		KbeeUser user = new KbeeUser();
		user.setUserName(getUserName(getMember().getFirstName(), getMember().getLastName()));
		user.setFirstName(getMember().getFirstName());
		user.setLastName(getMember().getLastName());
		user.setLastModifiedUser(getSessionUser());
		user.setCreationOffsetDateTime(OffsetDateTime.now());
		user.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		user.setEmail(getMember().getEmail());
		user.setDomain(getDomain());

		user.setStateEnabled();
		user.setActive(true);
		
		user.setCanonical(false);
		user.setLocale(getDomain().getLocale());
		user.setTimeZone(getDomain().getTimeZone());
		user.setUitheme(ServiceLocator.getService(BrandingService.class).getDefaultUITheme());
		
		user.addGroup(getGroup(KbeeGlobalRole.USER));

		if (getDomain().getDefaultPassword()!=null)
			user.setPassword(getDomain().getDefaultPassword());
		
		KbeeUserProfile userProfile = new KbeeUserProfile();
		getPerson().addProfile(userProfile);
		member.setPerson(getPerson());
		
		userProfile.setLastModifiedUser(getSessionUser());
		userProfile.setDomain(getDomain());
		userProfile.setCreationOffsetDateTime(OffsetDateTime.now());
		userProfile.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		userProfile.setEditPersonEnabled(true);
		userProfile.setUser(user);
		userProfile.setStartPage(getStartPage(roles));
		userProfile.setEmailNotifications(true);
		userProfile.setTipOfTheDay(false);
		userProfile.setEditPersonEnabled(true);
		
		getContentDao().save(((PersonMember)member).getPerson());
		getContentDao().save((DataSetMember)member);
		getContentDao().save(user);
		getContentDao().flush();
		
		updateEntityRoles();
		updateDomainRoles(roles);
		
		if (platforms!=null) {
			for (ExternalPlatformId platformId : platforms) {
				user.getService(UserSelfService.class).addLinkLoginPlatform(platformId, UserExternalPlatformIdType.EMAIL, getMember().getEmail());
			}
		}
		
		return user;
	}
	
	/**
	 * 
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	public User updateUser(List<Role> roles, List<ExternalPlatformId> platformsIds) {
		
		updateDomainRoles(roles);
		
		UserProfile userProfile = getPerson().getProfile(UserProfile.class);
		
		if (userProfile!=null) {
			if (platformsIds!=null) {
				List<UserExternalLoginPlatform> platforms = new ArrayList<UserExternalLoginPlatform>();
				for (ExternalPlatformId platformId : platformsIds) {
					UserExternalLoginPlatform platform = new KbeeUserExternalLoginPlatform();
					platform.setEnabled(true);
					platform.setPlatformId(platformId.getId());
					platform.setUserPlatformIdType(UserExternalPlatformIdType.EMAIL.getId());
					platform.setUserPlatformId(getMember().getEmail());
					platform.setUserProfile(userProfile);
					platforms.add(platform);
				}
				userProfile.setUserExternalLoginPlatforms(platforms);
			}
			userProfile.setStartPage(getStartPage(roles));
			getContentDao().save(userProfile);
		}
		User user = userProfile!=null? userProfile.getUser() : null;
		if (user!=null) {
			user.addGroup(getGroup(KbeeGlobalRole.USER));
		}
		return user;
	}
	
	@Transactional(propagation = Propagation.REQUIRED)
	public User updateUser() {
		
		updateEntityRoles();
		
		UserProfile userProfile = getPerson().getProfile(UserProfile.class);
		if (userProfile!=null) {
			for (UserExternalLoginPlatform platform : userProfile.getUserExternalLoginPlatforms()) {
				platform.setUserPlatformId(getMember().getEmail());
			}
			userProfile.setStartPage(getStartPage(getRoles()));
		} 
		
		User user = userProfile!=null? userProfile.getUser() : null;
		if (user!=null && !user.isMember(getGroup(KbeeGlobalRole.USER))) {
			user.addGroup(getGroup(KbeeGlobalRole.USER));
		}
		
//        ServiceLocator
//        	.getService(EventService.class)
//        	.fire(new BeforeUpdateEvent(getPerson()));
        
		return user;
	}
	
	@Transactional(propagation = Propagation.REQUIRED)
	public void delete() {
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void addTo(PersonSet dataset) {
		PersonMember member = (PersonMember)dataset.createMember();
		((KbeePersonMember)member).setPerson(getPerson());
		member.setStrValue(getPerson().getLastFirstName());
		getContentDao().save((DataSetMember)member);
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void registerDevice(UserDevice userDevice) {
		EmailBuilderRegisterDevice builder = new EmailBuilderRegisterDevice(userDevice);
		builder.setLanguage(getUser().getLocale().getLanguage());
		builder.setReceiver(getUser());
		ServiceLocator.getService(EmailService.class).send(builder);
	}
	
	@Transactional(propagation = Propagation.REQUIRED)
	public void updateDevice(UserDevice userDevice) {
		KbeeUserDevice kbeedevice = null;
		if (getDevice(userDevice.getDeviceId())==null) {
			kbeedevice = new KbeeUserDevice();
			kbeedevice.setDeviceId(userDevice.getDeviceId());
			kbeedevice.setDescription(userDevice.getDescription());
			kbeedevice.setNumber(userDevice.getNumber());
			kbeedevice.setRegistrationTime(OffsetDateTime.now());
			kbeedevice.setState(ObjectState.ENABLED);
			kbeedevice.setLastModifiedOffsetDateTime(OffsetDateTime.now());
			kbeedevice.setLastModifiedUser(getSessionUser());
			kbeedevice.setDomain(getDomain());
			getPerson().getProfile(UserProfile.class).addDevice(kbeedevice);
		}
		else {
			kbeedevice = (KbeeUserDevice)getDevice(userDevice.getDeviceId());
			kbeedevice.setState(ObjectState.ENABLED);
			kbeedevice.setLastModifiedOffsetDateTime(OffsetDateTime.now());
			kbeedevice.setLastModifiedUser(getSessionUser());
		}
	}
	
	@Transactional(propagation = Propagation.REQUIRED)
	public UserSignature updateSignature(UserDevice device) throws SignatureException {
		return updateSignature(device, null, null);
	}
	
	@Transactional(propagation = Propagation.REQUIRED)
	public UserSignature updateSignature(UserDevice device, Certificate certificate, KBFile handwriteImage) throws SignatureException {
		return getPerson().getService(SignatureService.class).updateSignature(device, certificate, handwriteImage);
	}
	
	@Transactional(propagation = Propagation.REQUIRED)
	public void delete(UserSignature signature) {
		getPerson().getService(SignatureService.class).delete(signature);
	}
	
	@Transactional(propagation = Propagation.REQUIRED)
	public void delete(UserDevice device) {
		if (hasSignedData(device)) {
		//if (hasSignatures(device)) {
			((KbeeUserDevice)device).setState(ObjectState.ARCHIVED);
			((KbeeUserDevice)device).setLastModifiedOffsetDateTime(OffsetDateTime.now());
		}
		else {
			getUserProfile().delete(device);
		}
	}
	
	@Override
	public String getIdentityType() {
		for (DataSetMember member : getMembers())
		for (Classification classification : member.getClassification()) {
			if (classification!=null && classification.getClassifier().isIdentityDocumentType()) {
				DataSetMember value = classification.getDataSetMember();
				return value!=null ? value.getDisplayName() : null;
			}
		}
		return null;
	}
	
	@Override
	public String getIdentityDocument() {
		for (DataSetMember member : getMembers())
		for (AttributeTemplate template : member.getDataSet().getAttributes()) {
			if (template!=null && template.getAttribute().isIdentityDocument()) {
				List<String> values = member.getAttributeValues(template.getAttribute());
				return values!=null && !values.isEmpty() ? values.get(0): null;
			}
		}
		return null;
	}
	
	@Override
	public String getOrganization() {
		for (DataSetMember member : getMembers())
			for (Classification classification : member.getClassification()) {
				if (classification!=null && classification.getClassifier().isOrganization()) {
					DataSetMember value = classification.getDataSetMember();
					return value!=null ? value.getDisplayName() : null;
				}
			}
		return null;
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void setUserFrom(Person person) {
		
		Assert.isNull(getUserProfile(), "user already exist");
		UserProfile userProfile = person.getProfile(UserProfile.class);
		getPerson().addProfile(userProfile);
		((KbeePerson)person).removeProfile(userProfile);
		
		KbeeUser user = (KbeeUser)userProfile.getUser();
		user.setFirstName(getPerson().getFirstName());
		user.setLastName(getPerson().getLastName());
		user.setEmail(getPerson().getEmail());
		
		// member de userset
		KbeePersonMember member = (KbeePersonMember)getUserMember(person);
		member.setPerson(getPerson());
		
		getContentDao().save(getPerson());
		getContentDao().save(userProfile);
		getContentDao().save(person);
		getContentDao().flush();
		updateEntityRoles();
		getContentDao().delete(person);
	}
	
	public UserDevice getDevice(String id) {
		for (UserDevice device : getPerson().getProfile(UserProfile.class).getDevices()) {
			if (device.getDeviceId().equals(id)) {
				return device; 
			}
		};
		return null;
	}
	
	public String getPhone() {
		String phone =null;
		for (UserDevice device : getUserProfile().getDevices()) {
			if (ObjectState.ENABLED.equals(device.getState()) && device.getNumber()!=null) {
				phone = device.getNumber();
				break;
			}
		}
		if (phone==null) {
			phone = getPerson().getPhone();
		}
		return phone;
	}
	
	public User getUser() {
		UserProfile userProfile = getUserProfile();
		if (userProfile==null) return null;
		User user = userProfile.getUser();
		return user;
	}
	
	public UserProfile getUserProfile() {
		return getPerson().getProfile(UserProfile.class);
	}
	
	public String getUserName() {
		if (getUserProfile()!=null) {
			return getUser().getName();
		}
		else {
			return getUserName(getMember().getFirstName(), getMember().getLastName());
		}
	}
	
	private boolean hasSignedData(UserDevice device) {
		List<SignedData> signed = getContentDao().findSignedByDevice(device);
		return signed!=null && !signed.isEmpty();
	}
	
	private Domain getDomain() {
		return getMember()!=null? getMember().getDomain() : getPerson().getDomain();
	}
	
	private List<Role> getRoles() {
		List<Role> roles = new ArrayList<Role>();
		if (getMainRole()!=null) roles.add(getMainRole());
		return roles;
	}
	
	private Role getMainRole() {
		UserProfile userProfile = getPerson().getProfile(UserProfile.class);
		if (userProfile==null) return null;
		for (UserRole userRole : userProfile.getRoles()) {
			if (!userRole.getRole().isCanonical() && !userRole.getRole().isEntity()) {
				return userRole.getRole();
			}
		}
		return null;
	}
	
	@Override
	public PersonMember getUserMember() {
		List<DataSetMember> members = getContentDao().findMembersByEntity(getPerson());
		for (DataSetMember member : members) {
			if (member.getDataSet().equals(getUserSet())) {
				return (PersonMember)member;
			}
		}
		return null;
	}
	
	// Actualiza la lista de roles de una persona (si el PersonService esta pedido a un personmember para que haya un dataset de contexto)
	private void updateEntityRoles() {
		UserProfile userProfile = getPerson().getProfile(UserProfile.class);
		if (userProfile==null) return;
		User user = userProfile.getUser();
		if (user==null) return;
		List<UserRole> userRoles = new ArrayList<UserRole>();
		for (UserRole userRole: userProfile.getRoles()) {
			Role role = userRole.getRole();
			if (!role.isEntity() || !role.isDefault()) {
				userRoles.add(userRole);
			}
		}
		for (Role role: getContentSecurityDao().getRoles(getDomain())) {
			if (role.isEntity() && role.isDefault()) {
				role = (Role)getContentDao().reload(role);
				Classifier roleclassifier = ((EntityRole)role).getClassifier();
				// si el rol es del personset donde esta incluida la persona (member)
				if (roleclassifier.includes(getMember().getDataSet())) {
					KbeeUserRole k_ur = new KbeeUserRole(role, user, getMember());
					userRoles.add(k_ur);
				}
				// si el rol es de un dataset donde la persona (member) esta relacionada
				for (Classification classification : getMember().getClassification()) {
					if (classification!=null && roleclassifier.includes(classification.getClassifier().getDataSet()) && classification.getDataSetMember()!=null) {
						KbeeUserRole k_ur = new KbeeUserRole(role, user, (EntityMember)classification.getDataSetMember());
						userRoles.add(k_ur);
					}
				}
			}
		}
		getMember().getService(RolesService.class).update(userRoles);
	}
	
	private void updateDomainRoles(List<Role> roles) {
		UserProfile userProfile = getPerson().getProfile(UserProfile.class);
		if (userProfile==null) return;
		User user = userProfile.getUser();
		if (user==null) return;
		List<UserRole> userRoles = new ArrayList<UserRole>();
		for (UserRole userRole: userProfile.getRoles()) {
			Role role = userRole.getRole();
			if (role.isDefault() || role.isEntity() || role.isCanonical()) {
				userRoles.add(userRole);
			}
		}
		for (Role role: roles) {
			if (!role.isEntity()) {
				KbeeUserRole userRole = new KbeeUserRole(role, user, null);
				userRoles.add(userRole);
			}
		}
		getMember().getService(RolesService.class).update(userRoles);
	}
	
	private String getUserName(String firstName, String lastName) {
		int iteration=0;
		String name = null;
		boolean exist = true;
		while (exist) {
			name = firstName!=null && firstName.trim().length()>0 ? firstName.substring(0,1) : "";
			name += lastName!=null ? lastName.trim() : getPerson().getId();
			name = cleanUserName(name);
			if (iteration>0) name += String.valueOf(iteration);
			name += "@" + getDomain().getName();
			User user = ServiceLocator.getService(com.novamens.service.SecurityService.class)
				.findUserByUsername(name);
			if (user!=null) {
				iteration++;
			}
			else {
				exist = false;
			}
		}
		return name;
	}
	
	private UserSet getUserSet() {
		UserSet userset= null;
		for (DataSet dataset : getDataSets()) {
			if (dataset.getDataSetType().equals(DataSetType.USER) && !(dataset instanceof UserSubset)) {
				userset = (UserSet)getContentDao().reload(dataset);
				break;
			}
		}

		return userset;
	}
	
	private Group getGroup(KbeeGlobalRole role) {
		return getContentSecurityDao().findGroupByName(role.getId(), getDomain());
	}
	
	private List<DataSet> getDataSets() {
		return getContentDao().getDataSets(getDomain());
	}
	
	private User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	
	private String cleanUserName(String s) {
		if (s==null)
			return null;
		String a1=s.toLowerCase().trim().replace(" de ", " ").replace(" a ", " ").replace(" por ", " ");
		a1=a1.replace("á", "a")
			 .replace("é", "e")
			 .replace("í", "i")
			 .replace("ó", "o")
			 .replace("ú", "u")
			 .replace("ñ", "nn");
		return a1;
	}
	
	private String getStartPage(List<Role> roles) {
		String page = "home";
		boolean external = false;
		for (Role role : roles) {
			if ("external".equals(role.getAlias())) {
				external = true;
				break;
			}
		}
		if (external) {
			for (Site site: getPortalDao().getSites(getDomain())) {
				if (site.getState()==ObjectState.ENABLED && !site.isExternal()) {
					page = site.getKey();
					break;
				}	
			}
		}
		return page;
	}
	
	private List<Role> getDefaultRoles() {
		List<Role> roles = new ArrayList<Role>();
		for (Role role: getContentSecurityDao().getRoles(getDomain())) {
			if (role.isDefault() && !role.isEntity()) {
				roles.add(role);
			}
		}
		return roles;
	}
	
	private List<DataSetMember> getMembers() {
		List<DataSetMember> members = new ArrayList<DataSetMember>();
		if (getMember()!=null) {
			members.add(getMember());
		}
		else {
			members.addAll(getContentDao().findMembersByEntity(getPerson()));
		}
		return members;
	}
	
	private DataSetMember getUserMember(Person person) {
		UserSet userset = getUserSet();
		for (DataSetMember member : getContentDao().findMembersByEntity(person)) {
			if (userset.equals(member.getDataSet())) {
				return member;
			}
		}
		return null;
	}
	
//	private String getLabel(String key, String... parameter) {
//		User user = getUser();
//		Locale locale = user!=null ? user.getLocale() : Locale.getDefault();
//		ResourceBundle res = ResourceBundle.getBundle(getClass().getName(), locale);
//		String label =  res.getString(key);
//		for (int i=0; i<parameter.length; i++) {
//			label = label.replace("{"+i+"}", parameter[i]);
//		}
//		return  label;
//	}
	
	private ContentSecurityDao getContentSecurityDao() {
		return (ContentSecurityDao)ServiceLocator.getService(BeansService.class).getBean("contentSecurityDao");
	}
	
	private ContentDao getContentDao() {
		return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	private PortalDao getPortalDao() {
		return (PortalDao)ServiceLocator.getService(BeansService.class).getBean("portalDao");
	}
}