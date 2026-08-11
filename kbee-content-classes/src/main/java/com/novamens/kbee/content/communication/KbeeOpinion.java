package com.novamens.kbee.content.communication;

import java.io.Serializable;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.novamens.content.communication.Opinion;
import com.novamens.content.communication.OrganizationalText;
import com.novamens.content.model.ContentTemplate;

@Entity
@DiscriminatorValue(OrganizationalText.OPINION_TYPE)
public class KbeeOpinion extends KbeeOrganizationalText implements Opinion,	Serializable {
	private static final long serialVersionUID = 7633301302797561738L;

	public KbeeOpinion() {
		super();
	}
	
	public KbeeOpinion(ContentTemplate ct) {
		super(ct);
	}
}
