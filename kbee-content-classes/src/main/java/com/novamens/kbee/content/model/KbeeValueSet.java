package com.novamens.kbee.content.model;

import java.time.OffsetDateTime;
import java.util.Map;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.DataSetType;
import com.novamens.content.model.ValueSet;
import com.novamens.dom.Domain;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeRuntimeException;

@Entity
@DiscriminatorValue(value="1")
public class KbeeValueSet extends KbeeDataSet implements ValueSet {
	
	
	public KbeeValueSet() {
		super(null, DataSetType.STRING);
	}
	
	public KbeeValueSet(String name) {
		super(name, DataSetType.STRING);
	}
	
	public KbeeValueSet(String name, DataSetType type) {
		super(name, type);
	}
	
	public DataSetMember createMember() {
		DataSetMember member;
			member = new KbeeValueMember(this);
			member.setCreationOffsetDateTime(OffsetDateTime.now());
			member.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		return member;
	}
	
	@Override
	public DataSet clone() {
		KbeeValueSet clone = new KbeeValueSet();
		super.clone(clone);
		return clone;
	}

	static public DataSet createFromMap(Map<String, String> map) throws KbeeRuntimeException {
		BeansService beans = ServiceLocator.getService(BeansService.class);
		ContentDao dao = (ContentDao) beans.getBean("contentDao");
		
		if (map.get("domain_id")==null) 
			throw new KbeeRuntimeException("domain is null");
		
		if (map.get("name")==null)
			throw new KbeeRuntimeException("name is null");
		
		else if (dao.findModelObjectByName(DataSet.class, map.get("name"), map.get("domain_id"))!=null)
			throw new KbeeRuntimeException("DataSet already exists");
		
		KbeeValueSet dataset = new KbeeValueSet(map.get("name"));
		
		if (map.get("type")!=null) {
			 String stype = map.get("type").trim().toLowerCase();
			 DataSetType dst = DataSetType.valueOf(stype);
			 if (dst!=null)
				 dataset.setDataSetType(dst);
			 else
				 dataset.setDataSetType(DataSetType.STRING);
		}
		
		dataset.setDomain((Domain) dao.findDomainById(map.get("domain_id")));
		
		return dataset;
	}
}
