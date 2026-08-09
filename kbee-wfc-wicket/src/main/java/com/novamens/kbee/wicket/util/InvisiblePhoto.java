package com.novamens.kbee.wicket.util;

import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

public class InvisiblePhoto extends Image {
			
	
private static final long serialVersionUID = 1L;

private static final ResourceReference EMPTY_PHOTO = new PackageResourceReference(GenericPhoto.class, "NoPicture.gif");
	
	
	public InvisiblePhoto(String id) {
		super(id, EMPTY_PHOTO);
	}
	
	@Override
	protected boolean shouldAddAntiCacheParameter()	{
		return false;
	}
	
	@Override
	public boolean isVisible() {
		return false;
	}
}
