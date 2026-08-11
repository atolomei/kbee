package com.novamens.kbee.content.webapi.type;

import com.novamens.content.user.UserSignature;
import com.novamens.kbee.content.user.KbeeUserSignature;

import kbee.api.model.IDevice;
import kbee.api.model.ApiResource;
import kbee.api.model.ISignature;

public class ISignatureAdapter implements Adapter<UserSignature, ISignature> {
	
	public ISignatureAdapter() {
	}
	
	public ISignature adapt(UserSignature signature) {
		
		ISignature isignature = new ISignature();
		
		isignature.setImage(getImage(signature));
		if (((KbeeUserSignature)signature).getDevice()!=null) {
			isignature.setDevice(getDevice(signature));
		}	
		isignature.setLastModifiedDate(signature.getLastModifiedOffsetDateTime());
		isignature.setState(String.valueOf(signature.getState().name()));
		
		return isignature;	
	}
	
	protected IDevice getDevice(UserSignature signature) {
		return (new IDeviceAdapter()).adapt(((KbeeUserSignature)signature).getDevice());
	}
	
	protected ApiResource getImage(UserSignature signature) {
		if (signature==null) return null;
		KbeeUserSignature kbeesignature = (KbeeUserSignature)signature;
		if (kbeesignature.getHandWriteImage()==null) return null;
		ApiResource image = new ApiResource();
		image.setId(String.valueOf(kbeesignature.getHandWriteImage().getId()));
		image.setHRef(UriHelper.getUri(kbeesignature.getHandWriteImage()));
		return image;
	}

}
