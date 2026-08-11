package com.novamens.kbee.content.ad;

import java.io.Serializable;

import javax.persistence.MappedSuperclass;

import com.novamens.content.model.ContentTemplate;
import com.novamens.kbee.content.base.KbeeResourceContainer;

@MappedSuperclass
public abstract class AbstractAd extends KbeeResourceContainer implements Serializable {
	private static final long serialVersionUID = 9071873064921696351L;

	public AbstractAd() {
		super();
	}
	
	public AbstractAd(ContentTemplate ct) {
		super(ct);
	}
	
}
