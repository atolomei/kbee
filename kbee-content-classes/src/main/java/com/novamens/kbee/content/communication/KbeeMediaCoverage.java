package com.novamens.kbee.content.communication;

import java.io.Serializable;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.novamens.content.communication.MediaCoverage;
import com.novamens.content.communication.OrganizationalText;
import com.novamens.content.model.ContentTemplate;

@Entity
@DiscriminatorValue(OrganizationalText.MEDIA_COVERAGE_TYPE)
public class KbeeMediaCoverage extends KbeeOrganizationalText implements MediaCoverage, Serializable {
	private static final long serialVersionUID = -1329597510086532654L;
	
	public KbeeMediaCoverage() {
		super();
	}
	
	public KbeeMediaCoverage(ContentTemplate ct) {
		super(ct);
	}
}
