package com.novamens.kbee.content.service;

import com.novamens.content.model.ClassifierTemplate;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;

public class KbeeAllAccessService extends KbeeAccessService {
	
	public KbeeAllAccessService(DataSet dataset) {
		super(dataset);
	}
	
	public KbeeAllAccessService(ClassifierTemplate template) {
		super(template);
	}
	
	public boolean isReadable(DataSetMember value) {
		return true;
	}
}
