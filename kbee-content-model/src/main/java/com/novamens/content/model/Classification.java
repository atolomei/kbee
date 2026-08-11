package com.novamens.content.model;

import java.time.OffsetDateTime;
 
import java.util.Locale;

import com.novamens.security.Identifiable;

// import com.novamens.content.userlist.UserListItem;


public interface Classification extends Identifiable  {
	
	public void setClassifier(Classifier classifier);
	public Classifier getClassifier();
	
	public void setDataSetMember(DataSetMember dmember);
	public DataSetMember getDataSetMember();
	
	public Object getValue();
	
	public String getStrValue();
	public OffsetDateTime getDateValue();
	public void setDateValue(OffsetDateTime datevalue);
	
	public DataSetType getDataSetType();
	
	public Classification clone();
	
	public int getPosition();
	public void setPosition(int pos);
	
	public String getAlternativeDisplayValue();
	
	// This is Legacy. For Classifiers of Type DATE
	//
	public String getStrValue(Locale locale);
	
	// UserListItem getUserListItem();
}