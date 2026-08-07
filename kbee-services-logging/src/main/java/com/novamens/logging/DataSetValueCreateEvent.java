package com.novamens.logging;

import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.novamens.content.model.DataSetMember;
import com.novamens.content.user.UserLabel;

@Entity
@DiscriminatorValue("DataSetValueCreateEvent")
public class DataSetValueCreateEvent extends DataSetValueEvent {
	
	public DataSetValueCreateEvent() {
		super();
	}
	
	public DataSetValueCreateEvent(DataSetMember datasetmember, String description) {
		super(datasetmember, description);
	}
	
	public DataSetValueCreateEvent(UserLabel label, String description) {
		super(label, description);
	}
	
	public DataSetValueCreateEvent(DataSetMember datasetmember, List<String> updatedParts) {
		super(datasetmember, updatedParts);
	}
	
	@Override
	public String getAction() {
		return "Create";
	}
	
	@Override
	public String getType() {
		return "DataSet Value";
	}
	
	@Override
	public String getObjectClass() {
		return "DataSet Value";
	}
	

}
