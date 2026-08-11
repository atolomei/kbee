package com.novamens.content.service;

import java.util.List;

import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.security.Role;
import com.novamens.service.ObjectService;

public interface DataSetService extends ObjectService {
	
	List<DataSet> getAggregations();
	DataSet createAggregation(String name);
	void deleteAggregation(DataSet dataset);
	List<Object> getReferences();
	long getTotalMembers();
	
	/**
	 * <p>Returns the aggregator of the value.
	 */
	DataSetMember getAggregator(DataSetMember value);

	/**
	 * <p>Returns a list of DataSetMember of the built-in DataSet.
	 * Example: 
	 *  
	 * DataSet 							-> Property
	 * DataSetMember 					-> Southern Cross
	 * 
	 * DataSet buitIn  					-> Unit
	 * List<DataSetMember>				-> Unit 1A, Unit 1B,... 
	 */
	List<DataSetMember> getAggregatedValues(DataSetMember aggregator);

	DataSetMember getAggregatedValues(DataSetMember aggregator, String value);
	
	/**
	 * @return the Aggregator DataSet (for built-in DataSets), or null for regular DataSets)
	 */
	DataSet getAggregatorDataSet(); 
	
	/**
	 * @return the Aggregator Classifier (for built-in DataSets), or null for regular DataSets)
	 */
	Classifier getAggregatorClassifier();
	
	/**
	 * The Classifier associated with this parent 
	 */
	Classifier getClassifier(DataSet parent_ds);
	
	
	/**
	 * if the DataSet is built in -> the Classifier associated (there must be only one)
	 * otherwise: null
	 */
	Classifier getMainClassifier();
	
	List<Role> getRoles();
}