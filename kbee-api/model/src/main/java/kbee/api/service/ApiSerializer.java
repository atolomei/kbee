package kbee.api.service;

import kbee.api.model.ApiObject;

public interface ApiSerializer {
	public ApiObject serialize(Object object);
}
