package com.novamens.content.version;

import com.novamens.service.ObjectService;

public interface VersionService extends ObjectService {
	public Object checkout();
	public void checkin();
	public void dropCheckout();
}
