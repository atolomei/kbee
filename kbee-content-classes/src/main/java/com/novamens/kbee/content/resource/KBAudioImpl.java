package com.novamens.kbee.content.resource;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.novamens.content.resource.KBAudio;


@SuppressWarnings("serial")
@Entity
@DiscriminatorValue(KBFileImpl.AUDIO_TYPE)
public class KBAudioImpl extends KBFileImpl implements KBAudio {

}
