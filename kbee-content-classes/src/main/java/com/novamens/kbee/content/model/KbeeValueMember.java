package com.novamens.kbee.content.model;

import java.util.Map;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;

import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.dom.Domain;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeRuntimeException;

@Entity
@DiscriminatorValue(value="1")
public class KbeeValueMember extends KbeeDataSetMember {
	
	public KbeeValueMember() {
		super();
	}
	
	public KbeeValueMember(DataSet dataset) {
		super(dataset);
	}
	
	public KbeeValueMember(String value, DataSet dataset) {
		super(value, dataset);
	}
	
	public String getDisplayName() {
		return getStrValue();
	}
	
	static public DataSetMember createFromMap(Map<String, String> map) throws KbeeRuntimeException {

		BeansService beans = ServiceLocator.getService(BeansService.class);
		ContentDao dao = (ContentDao) beans.getBean("contentDao");
		
		if (map.get("domain_id")==null) 
			throw new KbeeRuntimeException("domain is null");
		
		if (map.get("dataset")==null)
			throw new KbeeRuntimeException("dataset is null");
		
		DataSet dataset = (DataSet) dao.findModelObjectByName(DataSet.class, map.get("dataset"),map.get("domain_id"));
		
		if (dataset==null)
			throw new KbeeRuntimeException("DataSet does not exist");
		
		DataSetMember datasetmember = new KbeeValueMember(map.get("value"), dataset);
		
		if (map.get("domain_id")!=null) 
			datasetmember.setDomain((Domain) dao.findDomainById(map.get("domain_id")));
			
		return datasetmember;
	}

	@Override
	public boolean isOnlyRootEdit() {
		return false;
	}

	@Override
	public void setName(String name) {
		setStrValue(name);
	}
}
