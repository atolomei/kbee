package com.novamens.kbee.content.communication;

import java.io.Serializable;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.novamens.content.communication.InternalComm;
import com.novamens.content.communication.OrganizationalText;
import com.novamens.content.model.ContentTemplate;

@Entity
@DiscriminatorValue(OrganizationalText.INTERNAL_COMM_TYPE)
public class KbeeInternalComm extends KbeeOrganizationalText implements InternalComm, Serializable {
	private static final long serialVersionUID = 3940825877472437927L;
	

	public KbeeInternalComm() {
		super();
	}
	
	public KbeeInternalComm (ContentTemplate ct) {
		super(ct);
	}

}
