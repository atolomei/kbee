package com.novamens.kbee.content.communication;

import java.io.Serializable;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.novamens.content.communication.OrganizationalText;
import com.novamens.content.communication.Publications;
import com.novamens.content.model.ContentTemplate;

@Entity
@DiscriminatorValue(OrganizationalText.PUBLICATIONS_TYPE)
public class KBeePublications extends KbeeOrganizationalText implements Publications, Serializable {
	private static final long serialVersionUID = -7644274232539658324L;

	public KBeePublications() {
		super();
	}
	
	public KBeePublications(ContentTemplate ct) {
		super(ct);
	}
	
}
