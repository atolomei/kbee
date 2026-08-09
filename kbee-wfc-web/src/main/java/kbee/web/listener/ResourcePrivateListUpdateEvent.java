package kbee.web.listener;

import kbee.web.event.wicket.ResourceListUpdateEvent;

public class ResourcePrivateListUpdateEvent extends ResourceListUpdateEvent {

	public ResourcePrivateListUpdateEvent(boolean must_refresh_public_list) {
		super(must_refresh_public_list);
		// TODO Auto-generated constructor stub
	}

}
