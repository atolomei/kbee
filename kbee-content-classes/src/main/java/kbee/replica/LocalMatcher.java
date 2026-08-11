package kbee.replica;

import java.io.IOException;

import com.novamens.security.Identifiable;

import kbee.api.model.ApiObject;

public interface LocalMatcher {
	public Long getLocal(ApiObject remote);
	public void setLocal(ApiObject remote, Identifiable local) throws IOException;
	public void removeLocal(Identifiable local) throws IOException;
}