package com.novamens.kbee.content.resource;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.novamens.content.resource.KBImage;

@Entity
@DiscriminatorValue(KBFileImpl.IMAGE_TYPE)
public class KBImageImpl extends KBFileImpl implements KBImage {
	private static final long serialVersionUID = 2784950062694417814L;

	public KBImageImpl() {
		super();
	}

}


