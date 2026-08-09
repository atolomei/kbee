package com.novamens.kbee.wicket.util;

import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;



public class GenericPhoto extends Image {
	
	private static final long serialVersionUID = 1L;
	
	private static final ResourceReference GENERIC_PHOTO = new PackageResourceReference(GenericPhoto.class, "user-1699635_640.png");
	
	
	public GenericPhoto(String id) {
		super(id, GENERIC_PHOTO);
	}
	
	@Override
	protected boolean shouldAddAntiCacheParameter()	{
		return false;
	}

}
