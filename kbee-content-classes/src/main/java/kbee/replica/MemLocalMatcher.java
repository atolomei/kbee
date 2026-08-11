package kbee.replica;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.novamens.security.Identifiable;

import kbee.api.model.ApiObject;

public class MemLocalMatcher implements LocalMatcher {
	
	private Map<String, Long> map = new HashMap<>();

	public Long getLocal(ApiObject remote) {
		return map.get(getKey(remote));
	}
	
	public void setLocal(ApiObject remote, Identifiable local) throws IOException {
		map.put(getKey(remote), Long.valueOf((long)local.getId()));
	}
	
	public void removeLocal(Identifiable local) throws IOException {
	}
	
	private String getKey(ApiObject remote) {
		return remote.getClass().getSimpleName()+remote.getId();
	}
}