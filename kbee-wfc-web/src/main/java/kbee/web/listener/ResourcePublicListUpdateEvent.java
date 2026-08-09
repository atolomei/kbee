package kbee.web.listener;

import kbee.web.event.wicket.ResourceListUpdateEvent;

public class ResourcePublicListUpdateEvent extends ResourceListUpdateEvent {

	public ResourcePublicListUpdateEvent(boolean must_refresh_public_list) {
		super(must_refresh_public_list);
	}

}
