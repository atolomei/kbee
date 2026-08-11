package com.novamens.kbee.content.communication;

import java.io.Serializable;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.novamens.content.communication.Interview;
import com.novamens.content.communication.OrganizationalText;
import com.novamens.content.model.ContentTemplate;

@Entity
@DiscriminatorValue(OrganizationalText.INTERVIEW_TYPE)
public class KbeeInterview extends KbeeOrganizationalText implements Interview,	Serializable {
	private static final long serialVersionUID = -8291323252121520963L;

	public KbeeInterview() {
		super();
	}
	
	public KbeeInterview (ContentTemplate ct) {
		super(ct);
	}

}
