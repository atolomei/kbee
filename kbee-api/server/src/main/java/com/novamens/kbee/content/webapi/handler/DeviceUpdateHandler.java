package com.novamens.kbee.content.webapi.handler;

import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

import com.novamens.content.entity.Person;
import com.novamens.content.service.PersonService;
import com.novamens.content.user.UserDevice;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserService;
import com.novamens.dom.ObjectState;
import com.novamens.email.EmailService;
import com.novamens.kbee.content.user.KbeeUserDevice;
import com.novamens.security.User;
import com.novamens.service.ServiceLocator;

import kbee.api.model.IDevice;
import kbee.api.model.ITransaction;
import kbee.api.service.ApiError;
import kbee.api.service.ApiException;
import kbee.email.EmailBuilderRegisterDevice;

public class DeviceUpdateHandler extends AbstractRequestHandler {
	
	@Transactional
	public ITransaction add(IDevice idevice) {
		
		UserDevice device = null;
		for (UserDevice d : getUserProfile().getDevices()) {
			if (d.getDeviceId().equals(idevice.getId())) {
				device = d;
				break;
			}
		}
		
		if (device!=null && device.getState().equals(ObjectState.ENABLED)) {
			throw new ApiException(HttpStatus.NOT_MODIFIED, ApiError.NOT_MODIFIED);
		}
		
		UserProfile userProfile = getUserProfile();
		
		if (userProfile.getDomain().getSecurityLevel()>1) {
			User owner = userProfile.getUser();
			EmailBuilderRegisterDevice builder = new EmailBuilderRegisterDevice(getDevice(idevice));
			builder.setLanguage(owner.getLocale().getLanguage());
			builder.setReceiver(owner);
			ServiceLocator.getService(EmailService.class).send(builder);
		}
		else {
			device = new KbeeUserDevice();
			((KbeeUserDevice)device).setDeviceId(idevice.getId());
			((KbeeUserDevice)device).setDescription(idevice.getDisplayName());
			Person person = userProfile.getPerson();
			person.getService(PersonService.class).updateDevice(device);
		}

		ITransaction transaction  = getTransaction(null);
		
		return transaction;
	}
	
	private UserDevice getDevice(IDevice idevice) {
		KbeeUserDevice device = new KbeeUserDevice();
		device.setDeviceId(idevice.getId());
		device.setDescription(idevice.getDisplayName());
		device.setUserProfile(getUserProfile());
		device.setNumber("".equals(idevice.getNumber()) ? null : idevice.getNumber());
		device.setDomain(getDomain());
		return device;
	}
	
	private UserProfile getUserProfile() {
		return ServiceLocator.getService(UserService.class).getSessionUserProfile();
	}
}