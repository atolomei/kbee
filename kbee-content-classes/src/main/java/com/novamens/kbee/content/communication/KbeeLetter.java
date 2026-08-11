package com.novamens.kbee.content.communication;

import java.io.Serializable;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.novamens.content.communication.Letter;
import com.novamens.content.communication.OrganizationalText;
import com.novamens.content.model.ContentTemplate;

@Entity
@DiscriminatorValue(OrganizationalText.LETTER_TYPE)
public class KbeeLetter extends KbeeOrganizationalText implements Letter, Serializable {
	private static final long serialVersionUID = -2932929255403552102L;

	public KbeeLetter() {
		super();
	}
	
	public KbeeLetter (ContentTemplate ct) {
		super(ct);
	}
}
