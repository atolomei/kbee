package com.novamens.kbee.content.webapi.handler;

import java.io.IOException;
import java.io.Serializable;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

import com.novamens.content.base.Resource;
import com.novamens.content.entity.Person;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.PersonMember;
import com.novamens.content.resource.KBFile;
import com.novamens.content.service.PersonService;
import com.novamens.content.user.UserDevice;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserService;
import com.novamens.kbee.content.webapi.type.UriHelper;
import com.novamens.security.User;
import com.novamens.service.ServiceLocator;
import com.novamens.signature.CertificateParser;
import com.novamens.signature.SignatureException;

import kbee.api.model.ApiProxy;
import kbee.api.model.ISignature;
import kbee.api.model.ITransaction;
import kbee.api.service.ApiError;
import kbee.api.service.ApiException;

public class SignatureUpdateHandler extends AbstractRequestHandler {
	
	@Transactional
	public ITransaction update(ISignature isignature) {

		UserDevice device = getDevice(isignature);
		
		if (device==null) {
            throw new ApiException(HttpStatus.FORBIDDEN, ApiError.DEVICE_NOT_REGISTERED);
		}
		
		KBFile imageFile = getImageFile(isignature);
		
		if (imageFile==null) {
			throw new ApiException(HttpStatus.NOT_FOUND, ApiError.FILE_NOT_FOUND);
		}

		try {
			Certificate certificate = null;
			if (isignature.getCertificate()!=null) {
				certificate = CertificateParser.Get().read(isignature.getCertificate().getData());
			}
			getMember().getService(PersonService.class).updateSignature(device, certificate, imageFile);
		}
		catch (SignatureException | CertificateException | IOException e) {
			throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ApiError.INTERNAL_ERROR, e.getMessage());
		}

		ITransaction transaction  = getTransaction(getProxy(getUser()));
		
		return transaction;
	}
	
	private PersonMember getMember() {
		Person person = getPerson(getUser());
		List<DataSetMember> members = getContentDao().findMembersByEntity(person);
		PersonMember member = (PersonMember)members.get(0);
		return member;
	}
	
	private UserDevice getDevice(ISignature signature) {
		if (signature.getDevice()==null) return null;
		String deviceId = signature.getDevice().getId();
		for (UserDevice device : getUserProfile().getDevices()) {
			if (device.getDeviceId().equals(deviceId)) {
				return device;
			}
		}
		return null;
	}
	
	private KBFile getImageFile(ISignature signature) {
		if (signature.getImage()==null) return null;
		Serializable resourceId = getId(signature.getImage().getHRef());
		if (resourceId==null) return null;
		Resource kbfile = getContentDao().findResourceById(KBFile.class, resourceId);
		return (KBFile)kbfile;
	}
	
	private String getId(String href) {
		if (href==null) return null;
		int s = href.lastIndexOf("/");
		if (s<=0 || s==href.length()-1) return null;
		String id = href.substring(s+1);
		id = id.toLowerCase();
		return id;
	}
	
	protected ApiProxy getProxy(User user) {
		return new ApiProxy(String.valueOf(user.getId()), user.getLastFirstName(), UriHelper.getUri(user), "user");
	}
	
	private UserProfile getUserProfile() {
		return ServiceLocator.getService(UserService.class).getSessionUserProfile();
	}
	
	private Person getPerson(User user) {
		return getContentDao().findUserProfileByUser(user).getPerson();
	}
}