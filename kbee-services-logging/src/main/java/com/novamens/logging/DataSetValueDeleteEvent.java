package com.novamens.logging;

import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.novamens.content.model.DataSetMember;
import com.novamens.content.user.UserLabel;

@Entity
@DiscriminatorValue("DataSetValueDeleteEvent")
public class DataSetValueDeleteEvent extends DataSetValueEvent {

 

	public DataSetValueDeleteEvent() {
		super();
	}
	
	public DataSetValueDeleteEvent(DataSetMember datasetmember, String description) {
		super(datasetmember, description);
	}
	
	public DataSetValueDeleteEvent(UserLabel label, String description) {
		super(label, description);
	}
	
	public DataSetValueDeleteEvent(DataSetMember datasetmember, List<String> updatedParts) {
		super(datasetmember, updatedParts);
	}
	
	@Override
	public String getAction() {
		return "Delete";
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
