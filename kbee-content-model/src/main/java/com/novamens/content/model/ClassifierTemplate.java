package com.novamens.content.model;

import com.novamens.service.ObjectService;
import com.novamens.service.ServiceNotFoundException;

public interface ClassifierTemplate extends ModelElementTemplate {
	
	public Classifier getClassifier();
	
	public DataSetMember getRoot();
	public void setRoot(DataSetMember member);

	public boolean isReadOnly();
	
	public boolean isVisible();
	public void setVisible(boolean b);

	// For dependend Classifiers -> does it takes all the values related with the primary classifier ?
	// Primary -> A, B, C
	//
	// Alternatives are:
	// i. Assign all A,B,C  
	// ii. Enable Selector for the user to select A, B or C
	//
	// Inherit means i.
	public boolean isInherited();
	public void setInherited(boolean b);

	public boolean isMetadataSubtitle();
	public void setMetadataSubtitle(boolean b);

	public Multiplicity getMultiplicity();
	
	public void setMultiplicity(Multiplicity multiplicity);
	
	public AccessStrategy getAccessibility();
	
	public String getValuesCriteria();
	
	public boolean isMandatory();

	public boolean isPortalSubtitle();
	
	public boolean isReverse();
	
	public boolean isCanonical();
	
//	@Deprecated
//	public String getSubsection();
	
	public int getPosition();
	
	public String getSelectionScript();
	
	<T extends ObjectService> T getService(Class<T> service) throws ServiceNotFoundException;
} 