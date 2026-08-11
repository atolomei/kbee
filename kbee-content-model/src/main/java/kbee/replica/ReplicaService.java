package kbee.replica;

import com.novamens.service.SystemService;

import kbee.api.model.ApiObject;

public interface ReplicaService extends SystemService {
	public Object replicate(Replica replica, ApiObject object) throws ReplicaException;
}
