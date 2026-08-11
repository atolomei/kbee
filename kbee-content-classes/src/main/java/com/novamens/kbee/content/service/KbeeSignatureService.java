package com.novamens.kbee.content.service;


import java.io.IOException;
import java.security.KeyPair;
import java.security.cert.Certificate;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.novamens.beans.BeansService;
import com.novamens.content.base.SignedData;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.entity.Person;
import com.novamens.content.resource.KBFile;
import com.novamens.content.service.PersonService;
import com.novamens.content.service.SignatureService;
import com.novamens.content.user.SignatureType;
import com.novamens.content.user.UserDevice;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserSignature;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.content.entity.KbeePerson;
import com.novamens.kbee.content.user.KbeeUserSignature;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.signature.SignatureException;
import com.novamens.signature.SystemSignatureService;


public class KbeeSignatureService implements SignatureService {
			
	private Person person;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeSignatureService.class.getName());

	
	public KbeeSignatureService() {
	}
	
	public KbeeSignatureService(Person person) {
		 this.person = person;
	}
	
	public KbeePerson getPerson() {
		return (KbeePerson)person;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public UserSignature updateSignature(UserDevice device) throws SignatureException {
		return updateSignature(device, null, null);
	}	

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public UserSignature updateSignature(UserDevice device, Certificate certificate, KBFile handwriteImage) throws SignatureException {
		try {
			KbeeUserSignature kbeesignature = (KbeeUserSignature)getSignature(device);
			
			if (kbeesignature!=null && (hasSignedData(kbeesignature)||kbeesignature.getCertificate()==null)) {
				kbeesignature.setState(ObjectState.ARCHIVED);
				kbeesignature.setLastModifiedOffsetDateTime(OffsetDateTime.now());
				kbeesignature.setLastModifiedUser(getSessionUser());
				getContentDao().save(kbeesignature);
				kbeesignature = null;
			}
			
			
			if  (kbeesignature == null) {
				kbeesignature = (KbeeUserSignature)createSignature(device);
			}	
			
			// VER ESTO
			kbeesignature.setType(handwriteImage!=null ? SignatureType.PHONE_APP : SignatureType.SMS);
			
			kbeesignature.setHandWriteImage(handwriteImage);
			if (certificate!=null) {
				kbeesignature.setCertificate(certificate);
				kbeesignature.setPrivateKey(null);
			}
			kbeesignature.setLastModifiedOffsetDateTime(OffsetDateTime.now());
			kbeesignature.setLastModifiedUser(getSessionUser());
			getContentDao().save(kbeesignature);
			return kbeesignature;
		}
		catch (IOException e) {
			throw new SignatureException(e);
		}
	}
	
	
	/**
	 * 
	 * 
	 */
	public boolean verify(UserDevice device, String data, String signedData) throws SignatureException {
		KbeeUserSignature kbeesignature = (KbeeUserSignature)getSignature(device);
		if (kbeesignature!=null) {
			boolean result = ServiceLocator.getService(SystemSignatureService.class).verify(data, signedData, kbeesignature.getCertificate());
			return result;
		}
		return false;
	}

	/**
	 * 
	 * 
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void delete(UserSignature signature) {
		if (hasSignedData(signature)) {
			((KbeeUserSignature)signature).setState(ObjectState.ARCHIVED);
			((KbeeUserSignature)signature).setLastModifiedOffsetDateTime(OffsetDateTime.now());
			getContentDao().save((KbeeUserSignature)signature);
		}
		else {
			getUserProfile().delete(signature);
		}
	}
	
	/**
	 * 
	 * @param device
	 * @return
	 */
	public UserSignature getSignature(UserDevice device) {
		for (UserSignature signature : getUserProfile().getSignatures()) {
			if (signature.getDevice()!=null && signature.getDevice().equals(device) && ObjectState.ENABLED.equals(signature.getState())) {
				return signature;
			} 
		}
		return null;
	}
	
	public UserSignature getSignature() {
		for (UserSignature signature : getUserProfile().getSignatures()) {
			if (ObjectState.ENABLED.equals(signature.getState())) {
				return signature;
			}
		}
		return null;
	}
	
	public UserSignature createSignature(UserDevice device) throws SignatureException {
		KbeeUserSignature kbeesignature = null;
		kbeesignature = new KbeeUserSignature();
		kbeesignature.setDomain(getPerson().getDomain());
		kbeesignature.setState(ObjectState.ENABLED);
		kbeesignature.setCreationOffsetDateTime(OffsetDateTime.now());
		kbeesignature.setLastModifiedUser(getSessionUser());
		kbeesignature.setDevice(device);
		
		try {
			KeyPair keys = ServiceLocator.getService(SystemSignatureService.class).createKeys();
			Map<String, String> dn = new HashMap<String, String>();
			String organization = getPerson().getService(PersonService.class).getOrganization();
			if (organization!=null) dn.put("O", organization);
			String title = getPerson().getBusinessTitle();
			if (title!=null) dn.put("T", title);
			Certificate certificate = ServiceLocator.getService(SystemSignatureService.class).createCertificate(getUserProfile().getUser(), keys, dn);
			kbeesignature.setPrivateKey(keys.getPrivate());
			kbeesignature.setCertificate(certificate);
		}
		catch (IOException e) {
			logger.error(e);
			throw new SignatureException(e); 
		}
		
		kbeesignature.setUserProfile(getUserProfile());
		getPerson().getProfile(UserProfile.class).addSignature(kbeesignature);
		return kbeesignature;
	}
	
	public UserProfile getUserProfile() {
		return getPerson().getProfile(UserProfile.class);
	}
	
	private boolean hasSignedData(UserSignature signature) {
		List<SignedData> signed = getContentDao().findSignedBySignature(signature);
		return signed!=null && !signed.isEmpty();
	}
	
	private User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	
	private ContentDao getContentDao() {
		return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
}