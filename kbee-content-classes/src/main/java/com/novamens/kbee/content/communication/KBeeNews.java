package com.novamens.kbee.content.communication;

import java.io.Serializable;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.novamens.content.communication.News;
import com.novamens.content.model.ContentTemplate;

@Entity
@DiscriminatorValue(KbeeOrganizationalText.NEWS_TYPE)
public class KBeeNews extends KbeeOrganizationalText implements News, Serializable {
	private static final long serialVersionUID = -6902326460720771210L;

	public KBeeNews() {
		super();
	}
	
	public KBeeNews(ContentTemplate ct) {
		super(ct);
	}
	
}
