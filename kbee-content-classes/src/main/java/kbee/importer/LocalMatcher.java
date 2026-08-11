package kbee.importer;

import java.io.IOException;

import com.novamens.security.Identifiable;

import kbee.api.model.ApiObject;

@Deprecated
public interface LocalMatcher {
	public Long getLocal(ApiObject remote);
	public void setLocal(ApiObject remote, Identifiable local) throws IOException;
}