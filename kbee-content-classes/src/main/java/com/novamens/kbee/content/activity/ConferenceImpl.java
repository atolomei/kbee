package com.novamens.kbee.content.activity;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.novamens.content.activity.Activity;
import com.novamens.content.model.ContentTemplate;

@Entity
@DiscriminatorValue(Activity.CONFERENCE)
public class ConferenceImpl extends KbeeActivity {
	private static final long serialVersionUID = -2381745070725271326L;
	
	public  ConferenceImpl() {
		super();
	}
	
	public  ConferenceImpl(ContentTemplate ct) {
		super(ct);
	}
	
}
