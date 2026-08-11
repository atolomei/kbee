package kbee.importer;

import java.util.HashMap;
import java.util.Map;

import com.novamens.security.Identifiable;

import kbee.api.model.ApiObject;

@Deprecated
public class MemLocalMatcher implements LocalMatcher {
	
	Map<String, Long> map = new HashMap<String, Long>();
	
	public Long getLocal(ApiObject remote) {
		return  null;
	}
	
	public void setLocal(ApiObject remote, Identifiable local) {
		
	}

}
