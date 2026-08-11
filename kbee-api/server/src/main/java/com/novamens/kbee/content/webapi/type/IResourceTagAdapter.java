package com.novamens.kbee.content.webapi.type;

import com.novamens.content.base.ResourceTag;
import com.novamens.kbee.content.base.KbeeResourceTag;

import kbee.api.model.IResourceTag;

public class IResourceTagAdapter implements Adapter<ResourceTag, IResourceTag> {
	
	public IResourceTagAdapter() {
	}
	
	public IResourceTag adapt(ResourceTag tag) {
		IResourceTag itag = new IResourceTag();
		itag.setId(String.valueOf(((KbeeResourceTag)tag).getId()));
		itag.setName(tag.getAlias());
		itag.setDisplayName(tag.getDisplayName());
		itag.setMultiple(tag.isMultiple());
		itag.setLastModifiedDate(((KbeeResourceTag)tag).getLastModifiedOffsetDateTime());
		itag.setLastModifiedUser(new ApiUserProxy(((KbeeResourceTag)tag).getLastModifiedUser()));
		itag.setDomain(((KbeeResourceTag)tag).getDomain().getName());
		itag.setState(((KbeeResourceTag)tag).getState().name());
		return itag;	
	}
}