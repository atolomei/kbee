package com.novamens.logging;

import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.novamens.content.model.DataSetMember;
import com.novamens.content.user.UserLabel;

@Entity
@DiscriminatorValue("DataSetValueUpdateEvent")
public class DataSetValueUpdateEvent extends DataSetValueEvent {

 
	public DataSetValueUpdateEvent() {
		super();
	}
	
	public DataSetValueUpdateEvent(DataSetMember datasetmember, String description) {
		super(datasetmember, description);
	}
	
	public DataSetValueUpdateEvent(UserLabel label, String description) {
		super(label, description);
	}
	
	public DataSetValueUpdateEvent(DataSetMember datasetmember, List<String> updatedParts) {
		super(datasetmember, updatedParts);
	}
	
	@Override
	public String getAction() {
		return "Update";
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
