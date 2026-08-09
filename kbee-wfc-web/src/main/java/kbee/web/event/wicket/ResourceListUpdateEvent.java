package kbee.web.event.wicket;

import com.novamens.event.AbstractEvent;

public class ResourceListUpdateEvent extends AbstractEvent {

	private boolean must_refresh_public_list = true;
	
	public ResourceListUpdateEvent(boolean must_refresh_public_list) {
		this.must_refresh_public_list=must_refresh_public_list;
	}
	
	
	public boolean must_refresh_public_list() {
		return this.must_refresh_public_list;
	}
}
