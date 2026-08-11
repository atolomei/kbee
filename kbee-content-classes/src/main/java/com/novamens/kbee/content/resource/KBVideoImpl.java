package com.novamens.kbee.content.resource;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.novamens.content.resource.KBVideo;

@Entity
@DiscriminatorValue(KBFileImpl.VIDEO_TYPE)
public class KBVideoImpl extends KBFileImpl implements KBVideo {
	private static final long serialVersionUID = -9148133458144345282L;
}
