package com.novamens.kbee.content.document;


import javax.persistence.MappedSuperclass;

import com.novamens.content.document.Document;
import com.novamens.content.model.ContentTemplate;
import com.novamens.kbee.content.base.KbeeContent;
@Deprecated
@MappedSuperclass
public class KbeeDocument extends KbeeContent implements Document {

	public KbeeDocument() {
		super();
	}
	
	public KbeeDocument(ContentTemplate ct) {
		super(ct);
	}
}
