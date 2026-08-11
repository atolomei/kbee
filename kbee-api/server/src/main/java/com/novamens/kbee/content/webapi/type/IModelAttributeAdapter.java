package com.novamens.kbee.content.webapi.type;

import com.novamens.content.model.Attribute;

import kbee.api.model.IModelAttribute;

public class IModelAttributeAdapter implements Adapter<Attribute, IModelAttribute> {
	
	public IModelAttributeAdapter() {
	}
	
	public IModelAttribute adapt(Attribute attribute) {
		IModelAttribute iattribute = new IModelAttribute();
		iattribute.setDisplayName(attribute.getDisplayName());
		iattribute.setDomain(attribute.getDomain().getName());
		iattribute.setAlias(attribute.getAlias());
		iattribute.setId(String.valueOf(attribute.getId()));
		iattribute.setMultiplicity(attribute.getMultiplicity()!=null ? attribute.getMultiplicity().name() : null);
		iattribute.setUniqueName(attribute.getUniqueName());
		iattribute.setPredicate(attribute.getPredicate());
		iattribute.setType(attribute.getType().name());
		iattribute.setState(attribute.getState().name());
		iattribute.setFilterable(attribute.isFilterable());
		iattribute.setLastModifiedDate(attribute.getLastModifiedOffsetDateTime());
		iattribute.setLastModifiedUser(new ApiUserProxy(attribute.getLastModifiedUser()));
		return iattribute;	
	}
}