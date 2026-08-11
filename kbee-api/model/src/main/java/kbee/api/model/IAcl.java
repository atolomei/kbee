package kbee.api.model;

import java.util.ArrayList;
import java.util.List;

public class IAcl extends ApiObject {
	private static final long serialVersionUID = 1L;
	
	private List<IAclEntry> entries;
	
	public List<IAclEntry> getEntries() {
		return entries;
	}
	
	public void addEntry(IAclEntry entry) {
		if (entries==null) entries = new ArrayList<IAclEntry>();
		entries.add(entry);
	}
}
