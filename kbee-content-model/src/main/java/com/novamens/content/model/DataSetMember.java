package com.novamens.content.model;

import com.novamens.content.text.Text;
import com.novamens.dom.Indexable;
import com.novamens.dom.ObjectState;
import com.novamens.security.acl.Acl;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;


/**
 * 
 * 
 * 
 */

public interface DataSetMember extends ModelObject, Classificable, Indexable  {
	
	public static final String TYPE_RESOURCE_KEY = "resource";
	
	public DataSet	getDataSet();
	public void setDataSet(DataSet dataset);
	
	public List<DataSetMember> getParents();
	
	public Acl	getACL();
	
	public Object getValue();
	
	public String getStrValue();
	public OffsetDateTime getDateValue();
	
	public String getDisplayName();
	public String getConsoleDisplayName();

	public void setStrValue(String value); 		
	public void setDateValue(OffsetDateTime value);
	
	public String getAlternativeDisplayName();
	public void setAlternativeDisplayName(String dname);
	
	public int getLevel();
	
	public ObjectState getState();
	
	public void setAttributeValues(Attribute name, List<String> values);
	public List<String> getAttributeValues(Attribute name);
	
	public Text getNotes();
	public void  setNotes(String notes);

	String getExternalId();
	void setExternalId(String externalId);
	
	public default boolean isSystem() {return false;}
	default public String getModelObjectClassName() { return "DataSet Value";}

	public String getKey();
	public void setKey(String s);
	
	public Map<String, List<String>> getClassificationAsMapString();
	
}
