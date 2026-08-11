package com.novamens.kbee.content.service;

import java.util.Map;

import com.novamens.content.model.Classificable;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;

public class KbeeReverseAllAccessService extends KbeeAccessService {
	
	public KbeeReverseAllAccessService(DataSet dataset) {
		super(dataset);
	}
	
	public boolean isReadable(DataSetMember value) {
		return true;
	}
	
	protected String getStatement(String pattern, Classificable object, Map<String, Object> parameters) {
		String statement = super.getStatement(pattern, object, parameters);
		return statement;
	}	
}
