package com.novamens.kbee.content.webapi.type;


import com.novamens.content.user.UserDevice;

import kbee.api.model.IDevice;

public class IDeviceAdapter implements Adapter<UserDevice, IDevice> {
	
	public IDeviceAdapter() {
	}
	
	public IDevice adapt(UserDevice device) {
		
		IDevice idevice = new IDevice();
		
		idevice.setId(device.getDeviceId());
		idevice.setNumber(device.getNumber());
		idevice.setDisplayName(device.getDescription());
		idevice.setState(String.valueOf(device.getState().name()));
		
		return idevice;	
	}
}
