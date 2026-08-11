package kbee.replica;

import java.io.IOException;

import com.novamens.security.Identifiable;

import kbee.api.model.ApiObject;

public interface ReplicaStandBy extends Replica {
	public <T> T getLocal(Class<T> localclass, ApiObject remote);
	public void setLocal(ApiObject remote, Identifiable local) throws IOException;
}